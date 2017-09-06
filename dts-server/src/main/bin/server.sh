#!/bin/bash

### ====================================================================== ###
##                                                                          ##
##  Hybrid Server Startup Script                                            ##
##                                                                          ##
### ====================================================================== ###
### 2015-03-10 by diwayou

PROG_NAME=$0
ACTION=$1

usage() {
    echo "Usage: ${PROG_NAME} {start|stop|restart}"
    exit 1;
}

if [ $# -lt 1 ]; then
    usage
fi

if [ `whoami` == "root" ]; then
    echo DO NOT use root user to launch me.
    exit 1;
fi

cd `dirname $0`/..
BASE_DIR="`pwd`"
CONF_DIR=${BASE_DIR}/conf
PID_FILE=${BASE_DIR}/bin/server.pid

export JAVA_HOME=/home1/irteam/apps/jdk
export PATH=${JAVA_HOME}/bin:$PATH
export LD_LIBRARY_PATH=${BASE_DIR}/lib:${LD_LIBRARY_PATH}

JAVA_OPTS="-Duser.home=${BASE_DIR} -Dhybrid.type=server"
JAVA_OPTS="${JAVA_OPTS} -Djava.net.preferIPv4Stack=true"
JAVA_OPTS="${JAVA_OPTS} -server -Xms2048m -Xmx2048m -Xmn512m -XX:PermSize=128m -XX:MaxPermSize=196m"
JAVA_OPTS="${JAVA_OPTS} -XX:+UseConcMarkSweepGC"
JAVA_OPTS="${JAVA_OPTS} -XX:+CMSParallelRemarkEnabled"
JAVA_OPTS="${JAVA_OPTS} -XX:CMSInitiatingOccupancyFraction=75"
JAVA_OPTS="${JAVA_OPTS} -XX:+UseCMSInitiatingOccupancyOnly"
JAVA_OPTS="${JAVA_OPTS} -XX:+HeapDumpOnOutOfMemoryError"
JAVA_OPTS="${JAVA_OPTS} -XX:+PrintGCDetails"
JAVA_OPTS="${JAVA_OPTS} -XX:+PrintGCDateStamps"
JAVA_OPTS="${JAVA_OPTS} -XX:+DisableExplicitGC"
JAVA_OPTS="${JAVA_OPTS} -Xloggc:${BASE_DIR}/logs/gc-server.log"
#JAVA_OPTS="$JAVA_OPTS -Xdebug -Xrunjdwp:transport=dt_socket,address=8787,server=y,suspend=y"

if [ ! -f "${JAVA_HOME}/bin/java" ]; then
    echo "please set JAVA_HOME"
    exit 1;
fi

for jar in `ls ${BASE_DIR}/lib/*.jar`
do
    CLASSPATH="${CLASSPATH}:""${jar}"
done

LOG_PATH=${BASE_DIR}/logs
START_LOG=${LOG_PATH}/start.log

start()
    {
        echo "========================================================================="
        echo ""
        echo "  Hybrid server Startup Environment"
        echo ""
        echo "  BASE_DIR: ${BASE_DIR}"
        echo ""
        echo "  JAVA_HOME: ${JAVA_HOME}"
        echo ""
        echo "  JAVA_VERSION: `${JAVA_HOME}/bin/java -version`"
        echo ""
        echo "  JAVA_OPTS: ${JAVA_OPTS}"
        echo ""
        echo "  CLASS_PATH: ${CLASSPATH}"
        echo ""
        echo "========================================================================="
        echo ""

        #init logPath and logFile
        if [ ! -d "${LOG_PATH}" ]; then
            mkdir ${LOG_PATH}
        fi
        if [ ! -f "${START_LOG}" ]; then
            touch ${START_LOG}
        fi

        #check Server Already Running
        if [ -f "${PID_FILE}" ]; then
            PID_NUM=`cat ${PID_FILE}`
            if [ "" != "${PID_NUM}" ]; then
                RUN__PID=`ps aux | grep -v "grep" | grep "hybrid.type=server" | awk '{print $2}' | sed -n '1P' | grep ${PID_NUM}`
                if [ "" != "${RUN__PID}" ]; then
                    echo "Hybrid Server Already Running..............!"
                    exit 1;
                fi

            fi
        fi

        #Start Java Process
        ${JAVA_HOME}/bin/java ${JAVA_OPTS}  -classpath ${CONF_DIR}:${CLASSPATH}:. com.diwayou.hybrid.HybridServer ${BASE_DIR}/conf/server.properties >> ${START_LOG} 2>&1 &
		#write Process Pid To File
        PID_NUM=$!
        echo ${PID_NUM} > ${PID_FILE}
    }

stop()
    {
        if [ -f "${START_LOG}" ]; then
            mv -f ${START_LOG} "${START_LOG}.`date '+%Y%m%d%H%M%S'`"
        fi

        if [ -f "${PID_FILE}" ]; then
            PID_NUM=`cat ${PID_FILE}`

            if [ "" != "${PID_NUM}" ]; then

                kill ${PID_NUM}
                rm -f ${PID_FILE}
                echo kill server pid is "${PID_NUM}"
            fi

        fi

    }

case "${ACTION}" in
    start)
        start
    ;;
    stop)
        stop
    ;;
    restart)
        stop
        sleep 1
        start
    ;;
    *)
        usage
    ;;
esac
