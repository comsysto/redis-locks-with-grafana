package de.comsystoreply.redislocks.configuration;

import de.comsystoreply.redislocks.MetricsReporter;
import de.comsystoreply.redislocks.locks.RedisLock;
import de.comsystoreply.redislocks.locks.RedisLockBroken;
import de.comsystoreply.redislocks.locks.RedisLockWithDeleteCheck;
import de.comsystoreply.redislocks.locks.RedisLockWithDeleteCheckAndLogging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.function.Supplier;

@Configuration
@Import({RedisConfig.class, MetricsConfig.class})
public class LocksConfig {

    private static final Logger LOG = LoggerFactory.getLogger(LocksConfig.class);

    @Value("${application.name:'unknown'}")
    private String applicationName;

    @Value("${application.locks.sweetroll.expiration.millis:5000}")
    private long sweetrolllockExpirationMillis;

    @Bean
    public Supplier<Long> timeSupplier() {
        return System::currentTimeMillis;
    }

    @Bean
    public RedisLock sweetrollLock(
            RedisTemplate<String, String> redisTemplate,
            MetricsReporter metricsReporter) {
        RedisLock lock = new RedisLock(
                redisTemplate,
                metricsReporter,
                timeSupplier(),
                sweetrolllockExpirationMillis,
                "sweetroll",
                applicationName
        );
        return lock;
    }

    @Bean
    public RedisLockBroken sweetrollLockBroken(
            RedisTemplate<String, String> redisTemplate) {
        RedisLockBroken lock = new RedisLockBroken(
                redisTemplate,
                timeSupplier(),
                sweetrolllockExpirationMillis,
                "sweetroll",
                applicationName
        );
        return lock;
    }

    @Bean
    public RedisLockWithDeleteCheck sweetrollLock_withDeleteCheck(
            RedisTemplate<String, String> redisTemplate) {
        RedisLockWithDeleteCheck lock = new RedisLockWithDeleteCheck(
                redisTemplate,
                timeSupplier(),
                sweetrolllockExpirationMillis,
                "sweetroll",
                applicationName
        );
        return lock;
    }

    @Bean
    public RedisLockWithDeleteCheckAndLogging sweetrollLock_withDeleteCheckAndLogging(
            RedisTemplate<String, String> redisTemplate) {
        RedisLockWithDeleteCheckAndLogging lock = new RedisLockWithDeleteCheckAndLogging(
                redisTemplate,
                timeSupplier(),
                sweetrolllockExpirationMillis,
                "sweetroll",
                applicationName
        );
        return lock;
    }


}
