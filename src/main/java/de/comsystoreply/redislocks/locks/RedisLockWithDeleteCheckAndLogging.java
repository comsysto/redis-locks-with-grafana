package de.comsystoreply.redislocks.locks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.util.Pair;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.function.Supplier;

public class RedisLockWithDeleteCheckAndLogging {
    private static final Logger LOG = LoggerFactory.getLogger(RedisLockWithDeleteCheckAndLogging.class);

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

    private static RedisScript<String> deleteScript() {
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


    public RedisLockWithDeleteCheckAndLogging(RedisTemplate<String, String> redis,
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


    private static class LockAttemptId {
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
