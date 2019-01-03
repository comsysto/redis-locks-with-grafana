package de.comsystoreply.redislocks;

import de.comsystoreply.redislocks.configuration.ApplicationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

import java.util.Arrays;

@SpringBootApplication
@Import(ApplicationConfig.class)
public class RedisLocksApplication {
    private static final Logger LOG = LoggerFactory.getLogger(RedisLocksApplication.class);

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(RedisLocksApplication.class, args);
        if (args.length != 3) {
            throw new IllegalArgumentException("Got wrong amount of arguments, expected: " +
                    "'long sweetrollConsumeDuration long eatingPauseDuration long cyclesAmount' " +
                    "got:" + Arrays.toString(args)
            );
        }
        long sweetrollConsumeDuration = Long.valueOf(args[0]);
        long eatingPauseDuration = Long.valueOf(args[1]);
        long cyclesAmount = Long.valueOf(args[2]);
        if (sweetrollConsumeDuration < 0 || eatingPauseDuration < 0 || cyclesAmount < 0) {
            throw new IllegalArgumentException(String.format("All arguments must be positive, got: %d %d %d",
                    sweetrollConsumeDuration, eatingPauseDuration, cyclesAmount)
            );
        }

        LOG.info("Launching an application with arguments {}, {}, {}", sweetrollConsumeDuration, eatingPauseDuration, cyclesAmount);
        context.getBean(App.class).fightForSweetroll(sweetrollConsumeDuration, eatingPauseDuration, cyclesAmount);
    }

}

