#!/bin/bash

function kill_java_process() {
    local bootpids=`ps aux|grep java|grep sofa-runtimetest| grep -v grep |awk '{print $2;}'`
    local boot_pid_array=($bootpids)
    echo -e "\\nkilling SOFABoot processes:${boot_pid_array[@]}"
    for bootpid in "${boot_pid_array[@]}"
    do
        if [ -z "$bootpid" ]; then
            continue;
        fi
        echo "kill $bootpid"
	    kill $bootpid
	    /bin/sleep 3
	    killed_pid=`ps aux|grep java|grep $bootpid |awk '{print $2;}'`

	    if [[ "$killed_pid" == "$bootpid" ]]; then
	    echo "Kill $bootpid don't work and kill -9 $bootpid used violently!"
        kill -9 $bootpid
	    fi
    done
}



#dobbo common-model
ROOTDir=$(pwd)
testSuite=$1
echo "start testsuite:$testSuite"
if [[ $testSuite == "jdk8" ]];then
  suiteReg="*[^3|^dubbo|^dubbo32]-samples"
else
#  suiteReg="*[3|dubbo]-samples"
  suiteReg="*[3]-samples"
fi
#测试路径
for TEST_DIR in $(find $(pwd) -name "$suiteReg");do
  TESTAPP_DIR=$TEST_DIR
  echo "TESTAPP_DIR=$TESTAPP_DIR"
  cd ${TESTAPP_DIR}
  mvn clean install -U -Dmaven.test.skip=true
  for BaseDir in $( find $(pwd)  -type d -name "*base" |grep -v src|grep -v target|grep -v mybatis|grep -v logs);do
    echo "BaseDir $BaseDir"
    export BaseDir=$BaseDir
    cd $BaseDir

    echo "start clean old java processes"
    kill_java_process

    baseJar=$(find . -name "*[base|bootstrap]*.jar"|grep -v facade)
    echo "Deployed base app $baseJar"
    if [[ "$baseJar" == "" ]];then
      echo "找不到基座jar包！"
      exit 1
    fi
    java -Dtest=sofa-runtimetest -Drpc_bind_network_interface=eth0 -jar $baseJar >/dev/null 2>&1 &
    sleep 5

    echo "Start health check"
    if echo $BaseDir | grep "apollo"; then
      bash $ROOTDir/.github/workflows/ccbin/healthcheck.sh 8081
    else
      bash $ROOTDir/.github/workflows/ccbin/healthcheck.sh 8080
    fi
    echo "Start module biz Test"
    bash $ROOTDir/.github/workflows/ccbin/moduletest.sh

    echo "测试通过 $BaseDir"

  done
done


