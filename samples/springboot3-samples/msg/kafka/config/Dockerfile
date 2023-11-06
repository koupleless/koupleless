# Kafka
FROM alpine:3.18.4

## 国内网络访问不顺畅，需要开代理
RUN apk add --update openjdk8-jre supervisor bash gcompat

ENV SCALA_VERSION 2.13
ENV KAFKA_VERSION 3.6.0
ENV KAFKA_HOME /opt/kafka_"$SCALA_VERSION"-"$KAFKA_VERSION"

ENV KAFKA_DOWNLOAD_URL https://downloads.apache.org/kafka/"$KAFKA_VERSION"/kafka_"$SCALA_VERSION"-"$KAFKA_VERSION".tgz
RUN wget -q $KAFKA_DOWNLOAD_URL -O /tmp/kafka_"$SCALA_VERSION"-"$KAFKA_VERSION".tgz
RUN tar xfz /tmp/kafka_"$SCALA_VERSION"-"$KAFKA_VERSION".tgz -C /opt && rm /tmp/kafka_"$SCALA_VERSION"-"$KAFKA_VERSION".tgz

RUN mv /opt/kafka_"$SCALA_VERSION"-"$KAFKA_VERSION" /opt/kafka

ENV WORKSPACE /opt/kafka

ADD assets/scripts/start-kafka.sh /usr/bin/start-kafka.sh
RUN chmod +x /usr/bin/start-kafka.sh $WORKSPACE

ADD assets/supervisor/kafka.ini /etc/supervisor.d/

# 2181 is zookeeper, 9092 is kafka
EXPOSE 9092

CMD ["supervisord", "-n"]