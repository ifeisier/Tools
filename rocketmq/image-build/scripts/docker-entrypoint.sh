#!/bin/bash
set -e


nohup ./mqnamesrv > /dev/null 2>&1 &
echo "启动：mqnamesrv"


nohup ./mqbroker -n localhost:9876 --enable-proxy > /dev/null 2>&1 &
echo "启动：mqbroker"



nohup java -jar ./rocketmq-dashboard-1.0.1.jar --server.port=8087 &
echo "启动：dashboard"




tail -f /dev/null

# exec ./mqcontroller "${@}"
