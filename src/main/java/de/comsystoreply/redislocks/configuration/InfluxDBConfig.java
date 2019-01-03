package de.comsystoreply.redislocks.configuration;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Pong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class InfluxDBConfig {

    private static final Logger LOG = LoggerFactory.getLogger(InfluxDBConfig.class);

    @Value("${application.influxdb.url}")
    private String influxDbUrl;

    @Value("${application.influxdb.name}")
    private String influxDbName;

    @Value("${application.influxdb.user}")
    private String influxDbUser;

    @Value("${application.influxdb.password}")
    private String influxDbPassword;

    @Bean
    public InfluxDB influxDB() {
        InfluxDB influxDB = InfluxDBFactory.connect(influxDbUrl, influxDbUser, influxDbPassword);
        influxDB.setDatabase(influxDbName);
        influxDB.enableBatch(100, 10, TimeUnit.SECONDS);
        Pong pong = influxDB.ping();
        LOG.info("Connection with influxdb at {}:{} established for user {} with reponse {}", influxDbUrl, influxDbName, influxDbUser, pong);
        return influxDB;
    }
}
