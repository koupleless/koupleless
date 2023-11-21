#!/bin/bash

function module_biz_install_test() {
  bizName=$1
  bizDir=$2

  /usr/bin/expect <<EOF
    set fp [open "test_output.txt" w]
    set timeout 5
    spawn telnet localhost 1234
    expect {
      "Escape character is '^]'." {
        puts -nonewline \$fp  "Telnet connection success!\n"
      }
      timeout {
        puts -nonewline \$fp "Telnet connection failed!\n"
      }
      eof {
        puts -nonewline \$fp "Telnet connection failed!\n"
      }
    }
    send "biz -i file://$bizDir\r"
    expect {
      "Start to process install command now, pls wait and check." {
        puts -nonewline \$fp "module installing!\n"
      }
      timeout {
        puts -nonewline \$fp "module install failed!\n"
      }
      eof {
        puts -nonewline \$fp "Telnet connection failed!\n"
      }
    }
    sleep 10
    send "biz -a\r"
    expect {
      "$bizName*activated" {
        puts -nonewline \$fp "module install success!\n"
      }
      timeout {
        puts -nonewline \$fp "module install failed!\n"
      }
      eof {
        puts -nonewline \$fp "Telnet connection failed!\n"
      }
    }
    send "exit\r"
    expect {
      "Connection closed by foreign host" {
        puts -nonewline \$fp "close telnet success!\n"
      }
      timeout {
        puts -nonewline \$fp "close telnet failed!\n"
      }
      eof {
        puts -nonewline \$fp "close telnet failed!\n"
      }
    }
    close  \$fp

EOF
}

function arkctl_module_biz_install_test() {
  bizName=$1
  bizDir=$2
  arkctl deploy $bizDir
  arkStatus=$(arkctl status)
  # 提取数据部分
  data=$(echo $arkStatus | awk -F "QueryAllBiz " '{print $2}')
  # 校验状态
  if echo $data | grep -q "\"bizName\":\"$bizName\",\"bizState\":\"ACTIVATED\""; then
      echo "biz $bizName install success"
  else
      echo "biz $bizName install failed：$arkStatus"
      exit 1
  fi
}

set -e
#测试路径
echo "BaseDir=$BaseDir"
cd $BaseDir/..

BIZ_INSTALL_URL="http://localhost:8080/module/install"
BIZ_LIST_URL="http://localhost:8080/module/list"

for moduleBootDir in $(find $(pwd) -type d -path "*/biz[1-9]"  -o -path "*/*biz" |grep -v src|grep -v target|grep -v log);do
  echo "start deploy $moduleBootDir"
  cd $moduleBootDir

  echo "找到$(find $(pwd) -name "*-ark-biz.jar" | wc -l)个模块！"
  for moduleJar in $(find $(pwd) -name "*-ark-biz.jar");do
    if echo "$moduleJar" | grep -q "redis-biz-bootstrap";then
      moduleName=$(echo $moduleJar |awk -F "target/redis-" '{print $2}' |awk -F "-" '{print $1}')
    else
      moduleName=$(echo $moduleJar |awk -F "target/" '{print $2}' |awk -F "-" '{print $1}')
    fi
    echo "find one module:$moduleName, jar:$moduleJar"
    echo ''>test_output.txt
    arkctl_module_biz_install_test ${moduleName} $moduleJar
#    echo "start check module install result "
#    cat test_output.txt|while read line;do
#      if [[ $line =~ "failed" ]];then
#          echo "module install fail:$line"
#          exit 1
#      fi
#    done
  done
  echo "$BaseDir 测试完成！"
done

