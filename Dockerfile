FROM openjdk:8-jre-slim
COPY ./target/redis-locks-0.0.1-SNAPSHOT.jar /usr/src/locksapp/
WORKDIR /usr/src/locksapp
ENV APP_CYCLES=-1
ENTRYPOINT exec java -jar redis-locks-0.0.1-SNAPSHOT.jar $APP_CYCLES
