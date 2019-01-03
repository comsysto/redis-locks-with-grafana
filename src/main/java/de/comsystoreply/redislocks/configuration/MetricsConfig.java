package de.comsystoreply.redislocks.configuration;

import de.comsystoreply.redislocks.MetricsReporter;
import org.influxdb.InfluxDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(InfluxDBConfig.class)
public class MetricsConfig {

    private static final Logger LOG = LoggerFactory.getLogger(MetricsConfig.class);

    @Bean
    public MetricsReporter metricsReporter(InfluxDB influxDB) {
        MetricsReporter metricsReporter = new MetricsReporter(influxDB);
        return metricsReporter;
    }
}
