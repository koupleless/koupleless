#!/bin/bash
## 返回值 0 : 成功
## 返回值 1 : 检查失败导致
## 返回值 2 : 链接超时导致
##
## Usage  :    sh bin/healthcheck.sh
server_port=$1
HEALTH_URL="http://localhost:${server_port}/health"
HEALTH_CHECK_COMMOND="curl -s --connect-timeout 3 --max-time 5 ${HEALTH_URL}"

echo "        -- SOFA Boot CheckService"
echo "        -- HealthCheck URL : ${HEALTH_URL}"
#success:0;failure:1;timeout:2,and default value is failure=1
status=1
#default 120s
times=30

for num in $(seq $times); do
	sleep 1
	COSTTIME=$(($times - $num ))

	HEALTH_CHECK_CODE=`${HEALTH_CHECK_COMMOND} -o /dev/null -w %{http_code}`
#	reference : https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-monitoring.html#production-ready-health-access-restrictions
	if [ "$HEALTH_CHECK_CODE" == "200" ]; then
	    #success
		status=0;
		break;
    elif [ "$HEALTH_CHECK_CODE" == "503" ] ; then
        echo -n -e  "\r        -- HealthCheck Cost Time `expr $num` seconds."
        # failure
        status=1;
        break;
	else
	    # starting
		# echo -n -e  "\r        -- HealthCheck Remaining Time `expr $COSTTIME` seconds."
		status=2;
	fi
done

SOFA_BOOT_HEALTH_CHECK_RESULT="SUCCESS";

if [ $status -eq 1 ]; then
    echo "        -- HealthCheck Failed.-- Current Server Responded Http Code ${HEALTH_CHECK_CODE}"
    SOFA_BOOT_HEALTH_CHECK_RESULT=`${HEALTH_CHECK_COMMOND}`;
    # 重定向到标准错误流,zpaas 平台捕获打印
    echo -e "Health Check Result \n$SOFA_BOOT_HEALTH_CHECK_RESULT"  >&2
    exit 1;
fi

if [ $status -eq 2 ]; then
    SOFA_BOOT_HEALTH_CHECK_RESULT="Could Not Connect to ${HEALTH_URL}.HealthCheck ${times} Seconds Timeout!";
    # 重定向到标准错误流,zpaas 平台捕获打印
    echo -e "Health Check Result \n$SOFA_BOOT_HEALTH_CHECK_RESULT"  >&2
    exit 2;
fi

# success
# 重定向到标准错误流,zpaas 平台捕获打印
echo -e "Health Check Result \n$SOFA_BOOT_HEALTH_CHECK_RESULT"  >&2