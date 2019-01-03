package de.comsystoreply.redislocks.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class RedisConfig {

    private static final Logger LOG = LoggerFactory.getLogger(RedisConfig.class);

    @Value("${application.name}")
    private String applicationName;

    @Value("${spring.redis.host:localhost}")
    private String host;

    @Value("${spring.redis.port:6379}")
    private int port;


    //todo example with sentinel connection
    @Bean
    public JedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
        return new JedisConnectionFactory(config);
    }


    //todo notice here the name and the type - otherwise see RedisAutoConfiguration
    @Bean
    public StringRedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        LOG.info("Application will access Redis using identifier : {}", applicationName);
        StringRedisTemplate redisTemplate = new StringRedisTemplate();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }


}
