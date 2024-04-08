FROM openjdk:17-jdk-slim-buster
WORKDIR /code

COPY ./target/core-app-0.0.1-SNAPSHOT.jar /code/
ARG PROPERTIES_PATH=./src/main/resources
RUN echo $PROPERTIES_PATH
COPY $PROPERTIES_PATH/application.properties /etc/aloy/application.properties
ARG XMX_XMS_VALUE=1024m
RUN echo $XMX_XMS_VALUE
ENV JAVA_OPTS="-Xms$XMX_XMS_VALUE -Xmx$XMX_XMS_VALUE -Dspring.config.location=/etc/aloy/ -XX:-OmitStackTraceInFastThrow -XX:+UseG1GC -XX:+UseStringDeduplication"
RUN echo $JAVA_OPTS
ENTRYPOINT exec java -jar $JAVA_OPTS -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:8004 core-app-0.0.1-SNAPSHOT.jar