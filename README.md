Build jar file locally: \
`mvn clean install` 

Run all applications in local docker: \
`docker-compose -f docker-compose-redis-standalone-grafana.yml up --build`

Log in to Grafana Admin Tool: \
`http://localhost:13000`

Login as admin/admin as it is in default configs for Grafana Docker image:
![alt text](img/grafana_login.png)

Configure InfluxDB as Data Source:  
![alt text](img/grafana_datasource.png)

Go to Dashboards and create a new Panel:
![alt text](img/grafana_create_graf.png)

Configure metrics:
![alt text](img/grafana_metrics.png)

Configure axes:
![alt text](img/grafana_axes.png)

Configure legend:
![alt text](img/grafana_legend.png)

Configure styles for graphic:
![alt text](img/grafana_display.png)

And as a result we have a live metrics showing attempts to acquire a lock on redis
per application.
That could be extended to track failed attempts or to discover race conditions (and trigger an alert if needed)