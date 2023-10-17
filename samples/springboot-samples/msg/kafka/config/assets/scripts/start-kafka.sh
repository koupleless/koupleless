#!/bin/sh

WORK_DIR=/opt/kafka

KAFKA_CLUSTER_ID="$($WORK_DIR/bin/kafka-storage.sh random-uuid)"
$WORK_DIR/bin/kafka-storage.sh format -t $KAFKA_CLUSTER_ID -c $WORK_DIR/config/kraft/server.properties
$WORK_DIR/bin/kafka-server-start.sh $WORK_DIR/config/kraft/server.properties
