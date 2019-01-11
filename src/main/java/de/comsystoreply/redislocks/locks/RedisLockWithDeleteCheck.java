package de.comsystoreply.redislocks.locks;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.Duration;
import java.util.Collections;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.function.Supplier;

public class RedisLockWithDeleteCheck {

    public boolean withLock(Runnable runnable) {
        LockAttemptId lockAttemptId = new LockAttemptId(appName, lockName, currentTimeSupplier);
        boolean locked = tryLock(lockAttemptId);
        if (!locked) {
            return false;
        }
        try {
            runnable.run();
            return true;
        } finally {
            redis.execute(
                    deleteScript(),
                    Collections.singletonList(lockAttemptId.getKey()),
                    new Object[]{lockAttemptId.getValue()}
            );
        }
    }

    private static RedisScript<String> deleteScript() {
//        Redis uses the same Lua interpreter to run all the commands.
//        Also Redis guarantees that a script is executed in an atomic way:
//        no other script or Redis command will be executed while a script is being executed.
//        This semantic is similar to the one of MULTI / EXEC.
//        From the point of view of all the other clients the effects of a script are either still not visible or already completed.
//        If a key with expiry (ttl) exists at the start of eval, it will not get expired during the evaluation of a script.
        return new DefaultRedisScript<>(
                "local current = redis.call('GET', KEYS[1]) " +
                        "if current == ARGV[1] then " +
                        "   redis.call(\"del\", KEYS[1]) " +
                        "   return ARGV[1] " +
                        "else " +
                        "   return current " +
                        "end ",
                String.class
        );
    }

    private boolean tryLock(LockAttemptId lockAttemptId) {
        boolean lockFree = redis.opsForValue().setIfAbsent(
                lockAttemptId.getKey(),
                lockAttemptId.getValue(),
                Duration.ofMillis(lockExpiryMillis)
        );

        if (lockFree) {
            //lock is free and was acquired by this thread/node
            return true;
        }

        return false;
    }

    private final RedisTemplate<String, String> redis;
    private final Supplier<Long> currentTimeSupplier;
    private final long lockExpiryMillis;
    private final String lockName;
    private final String appName;


    public RedisLockWithDeleteCheck(RedisTemplate<String, String> redis,
                                    Supplier<Long> currentTimeSupplier,
                                    long lockExpiryMillis,
                                    String lockName,
                                    String appName) {
        this.redis = redis;
        this.currentTimeSupplier = currentTimeSupplier;
        this.lockExpiryMillis = lockExpiryMillis;
        this.lockName = lockName;
        this.appName = appName;
    }


    private class LockAttemptId {
        private final String id;
        private final String appName;
        private final String lockName;
        private final long timestamp;

        LockAttemptId(String appName, String lockName, Supplier<Long> currentTimeSupplier) {
            this.appName = appName;
            this.id = UUID.randomUUID().toString();
            this.lockName = lockName;
            this.timestamp = currentTimeSupplier.get();
        }

        String getKey() {
            return lockName;
        }

        String getValue() {
            return new StringJoiner("_")
                    .add(lockName)
                    .add(appName)
                    .add(id)
                    .add(String.valueOf(timestamp))
                    .toString();
        }
    }


}
