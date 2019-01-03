package de.comsystoreply.redislocks.configuration;

import de.comsystoreply.redislocks.App;
import de.comsystoreply.redislocks.locks.RedisLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(LocksConfig.class)
public class ApplicationConfig {

    @Value("${application.name}")
    private String applicationName;

    @Bean
    public App app(RedisLock lock) {
        return new App(lock, applicationName);
    }


}
