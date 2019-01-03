package de.comsystoreply.redislocks;

import org.influxdb.InfluxDB;
import org.influxdb.dto.Point;
import org.springframework.data.util.Pair;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

public class MetricsReporter {
    private final InfluxDB influxDb;

    public MetricsReporter(InfluxDB influxDb) {
        this.influxDb = influxDb;
    }


    public final void collectMetric(long timestamp, String groupValue,
                                    Collection<Pair<String, String>> tags,
                                    Collection<Pair<String, String>> fields) {
        Point.Builder builder = Point.measurement(groupValue)
                .time(timestamp, TimeUnit.MILLISECONDS);
        for (Pair<String, String> field : fields) {
            builder.addField(field.getFirst(), field.getSecond());
        }
        for (Pair<String, String> tag : tags) {
            builder.tag(tag.getFirst(), tag.getSecond());
        }
        Point point = builder.build();
        //save due to enabled batch by default
        influxDb.write(point);
    }
}
