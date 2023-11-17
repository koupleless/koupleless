#!/bin/sh
./mqnamesrv &
sleep 5
./mqbroker -n localhost:9876 -c /home/rocketmq/rocketmq-4.9.7/bin/broker.conf


