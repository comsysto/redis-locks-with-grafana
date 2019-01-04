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
                                    Collection<Pair<String, Object>> fields) {
        Point.Builder builder = Point.measurement(groupValue)
                .time(timestamp, TimeUnit.MILLISECONDS);
        for (Pair<String, Object> field : fields) {
            if (field.getSecond() instanceof Number)
                builder.addField(field.getFirst(), Number.class.cast(field.getSecond()));
            if (field.getSecond() instanceof String)
                builder.addField(field.getFirst(), String.class.cast(field.getSecond()));
            if (field.getSecond() instanceof Boolean)
                builder.addField(field.getFirst(), Boolean.class.cast(field.getSecond()));
        }
        for (Pair<String, String> tag : tags) {
            builder.tag(tag.getFirst(), tag.getSecond());
        }
        Point point = builder.build();
        //save due to enabled batch by default
        influxDb.write(point);
    }
}
