package de.comsystoreply.redislocks.locks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.Duration;
import java.util.Collections;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.function.Supplier;

public class RedisLock {
    private static final Logger LOG = LoggerFactory.getLogger(RedisLock.class);

    private final RedisTemplate<String, String> redis;
    private final Supplier<Long> currentTimeSupplier;
    private final long lockExpiryMillis;
    private final String lockName;
    private final String appName;

    public RedisLock(RedisTemplate<String, String> redis,
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

    /**
     * Run the runnable within a held lock if possible otherwise returns false.
     * This method is used when the operation within confined lock does not need to return a value.
     *
     * @param runnable the action that should be performed
     * @return true if the action could be performed in the lock. False if no lock could be acquired.
     */
    public boolean withLock(Runnable runnable) {
        LockAttemptId lockAttemptId = new LockAttemptId(appName, lockName, currentTimeSupplier);
        notifyLockAttempt(lockAttemptId);
        boolean locked = tryLock(lockAttemptId);
        if (!locked) {
            notifyLockBusy(lockAttemptId);
            return false;
        }
        notifyLockSuccess(lockAttemptId);
        try {
            runnable.run();
            return true;
        } finally {
            String heldLockValue = redis.execute(
                    deleteScript(),
                    Collections.singletonList(lockAttemptId.getKey()),
                    new Object[]{lockAttemptId.getValue()}
            );
            //noinspection ConstantConditions
            if (heldLockValue == null) {
                notifyLockExpired(lockAttemptId);
            } else if (!heldLockValue.equals(lockAttemptId.getValue())) {
                notifyRaceCondition(heldLockValue, lockAttemptId);
            }
        }
    }

    private void notifyLockExpired(LockAttemptId lockAttemptId) {
        //another process could have obtained the lock and finished his process before
        LOG.warn("LOCK '{}', execution for value '{}' was finished after lock expired - possible race condition detected",
                lockAttemptId.getKey(), lockAttemptId.getValue());
    }

    private void notifyRaceCondition(String heldLockValue, LockAttemptId lockAttemptId) {
        LOG.warn("LOCK '{}', execution for value '{}' was finished after another process obtained the lock with value '{}' - race condition detected",
                lockAttemptId.getKey(), lockAttemptId.getValue(), heldLockValue);
    }

    private void notifyLockSuccess(LockAttemptId lockAttemptId) {
        LOG.info("LOCK '{}', an attempt to obtain with value '{}' SUCCESS", lockAttemptId.getKey(), lockAttemptId.getValue());
    }

    private void notifyLockBusy(LockAttemptId lockAttemptId) {
        LOG.info("LOCK '{}', an attempt to obtain with value '{}' FAILED - lock busy", lockAttemptId.getKey(), lockAttemptId.getValue());
    }

    private void notifyLockAttempt(LockAttemptId lockAttemptId) {
        LOG.info("LOCK '{}', an attempt to obtain with value '{}'", lockAttemptId.getKey(), lockAttemptId.getValue());
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

    private class LockAttemptId {
        private final String id;
        private final String appName;
        private final String lockName;
        private final long timestamp;

        LockAttemptId(String appName, String lockName, Supplier<Long> currentTimeSupplier) {
            this.id = UUID.randomUUID().toString();
            this.appName = appName;
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
