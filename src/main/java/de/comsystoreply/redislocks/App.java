package de.comsystoreply.redislocks;

import de.comsystoreply.redislocks.locks.RedisLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
    private static final Logger LOG = LoggerFactory.getLogger(App.class);

    private final String appName;
    private final RedisLock lock;

    public App(RedisLock lock, String appName) {
        this.appName = appName;
        this.lock = lock;
    }

    public void fightForSweetroll(long sweetrollConsumeDuration, long eatingPauseDuration, long cycles) {
        int cycle = 0;
        //todo check if incremented after comparison
        while (cycle++ < cycles) {

            boolean lockObtained = lock.withLock(() -> {
                LOG.info("{} is eating a sweetroll", appName);

                try {
                    Thread.sleep(sweetrollConsumeDuration);
                } catch (InterruptedException e) {
                    LOG.warn("Thread interrupted");
                }

                LOG.info("{} ate a sweetroll", appName);
            });

            if (lockObtained) {
                LOG.info("{} is full and relaxes", appName);
            } else {
                LOG.info("{} is hungry and yearns", appName);
            }

            try {
                Thread.sleep(eatingPauseDuration);
            } catch (InterruptedException e) {
                LOG.warn("Thread interrupted");
            }

        }
    }
}
