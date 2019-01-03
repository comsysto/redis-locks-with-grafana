package de.comsystoreply.redislocks.configuration;

import de.comsystoreply.redislocks.MetricsReporter;
import de.comsystoreply.redislocks.locks.RedisLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
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


}
