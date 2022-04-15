#!/bin/bash

HOME=`eval echo ~$USER`
REPO_HOME=$PWD
DOWNLOADS="${REPO_HOME}/DOWNLOADS"
REDIS_HOME="${REPO_HOME}/redis"
KAFKA_HOME="${REPO_HOME}/kafka_2.12-3.1.0"
FLINK_HOME="${REPO_HOME}/flink"
FLINK_CHECKPOINT_DIR="${REPO_HOME}/flink_checkpoint"

USAGE_MSG="$0 <install, stop, start>"

PARALLELISM=1
J1_ARG=38
J2_ARG=100
CHECKPOINT_INTERVAL=-1
REPORT_MODE=0   #0=report q1 and q2, 1=report q1, 2=report q2

function help() {
    echo "Syntax: $0 install| start [-p parallelim] [-i inteval1] [-j interval2] | stop"
    echo "options:"
    echo "install   Intall the necessary software stack (utilities, processing platforms)"
    echo "build     Build application from source code"
    echo "start     Deploy and start the processes for fetching and analysing data"
    echo "      optional start arguments:"
    echo "      -p <number>  Flink parallelism (Default: 1)"
    echo "      -i <number>  Interval 1 (Default 38)"
    echo "      -j <number>  Interval 2 (Default 100)"
    echo "      -c <minutes> Checkpointing interval in minutes (Default: no checkpointing)" 
    echo "      -q <number>  Specify the reported queries. 1 for Q1, 2 for Q2. (Default report both queries)"
    echo "stop      Stops processing and processing platform"
    echo ""
    echo "e.g. ./manage.sh start -p 2 -i 50 -j 90 -q 1"
}

function install_utilities() {
    echo "$(date +'%d/%m/%y %T') Install necessary dependencies. This may take a while. Please wait"
#    echo $(hostname -I | cut -d\  -f1) $(hostname) | sudo tee -a /etc/hosts
    sudo apt-get update > /dev/null 2>&1
    sudo apt-get install -y htop build-essential openjdk-8-jdk maven git > /dev/null 2>&1
    sudo timedatectl set-timezone Europe/Athens
	cd ${REPO_HOME}
    mkdir -p $DOWNLOADS
    mkdir -p ${FLINK_CHECKPOINT_DIR}
}

function redis_install() {
    echo "$(date +'%d/%m/%y %T') Install Redis"
#    cd ${REPO_HOME}/${DOWNLOADS}
    cd ${DOWNLOADS}
    wget --quiet https://download.redis.io/releases/redis-6.2.6.tar.gz > /dev/null
    tar -zxvf  redis-6.2.6.tar.gz > /dev/null 2>&1

    cd ${REPO_HOME}
    ln -sf ${DOWNLOADS}/redis-6.2.6 ${REDIS_HOME}

    PROC=`nproc`    # number of cpu cores
    cd ${REDIS_HOME}
    make -j $PROC > /dev/null 2>&1
}

# start redis in standalone mode listening on the localhost
function redis_standalone_start() {
    echo "$(date +'%d/%m/%y %T') Redis stadalone mode start"
    ${REDIS_HOME}/src/redis-server ${REDIS_HOME}/redis.conf --daemonize yes
}

function redis_shutdown() { 
    echo "$(date +'%d/%m/%y %T') Redis stop"
    ${REDIS_HOME}/src/redis-cli SHUTDOWN
}

function flink_install() {
    echo "$(date +'%d/%m/%y %T') Install Flink"
#    cd ${REPO_HOME}/${DOWNLOADS}
    cd ${DOWNLOADS}
    wget --quiet https://archive.apache.org/dist/flink/flink-1.14.3/flink-1.14.3-bin-scala_2.12.tgz
    tar -zxvf flink-1.14.3-bin-scala_2.12.tgz > /dev/null 2>&1

    cd ${REPO_HOME}
    ln -sf ${DOWNLOADS}/flink-1.14.3 ${FLINK_HOME}
    
    flink_config
}

function flink_config() {
    #JM_IP=$(hostname -I | cut -d\  -f1)
    #sed -i -e "/jobmanager\.rpc\.address:/ s/: .*/: ${JM_IP}/" ${FLINK_HOME}/conf/flink-conf.yaml
    sed -i -e "/taskmanager\.memory\.process\.size:/ s/: .*/: 5000m/" ${FLINK_HOME}/conf/flink-conf.yaml
    sed -i -e "/taskmanager\.numberOfTaskSlots:/ s/: .*/: ${PARALLELISM}/" ${FLINK_HOME}/conf/flink-conf.yaml 
}

# start/stop flink job manager
flink_manage_jm() {
    if [ $# -lt 1 ]; then
        echo "Wrong number of arguments for jobmanager!"
        echo "Params should be start/stop"
        exit 1
    fi

    if [ "$1" != "start" ] && [ "$1" != "stop" ] ;
    then
        echo "Wrong arguments for jomanager"
        exit 1
    fi

    echo "jobmanager " $1
    cd ${FLINK_HOME}/bin && ./jobmanager.sh $1
}

# start/stop flink task manager
flink_manage_tm() {
    if [ $# -lt 1 ]; then
        echo "Wrong number of arguments for jobmanager!"
        echo "Params should be start/stop"
        exit 1
    fi

    if [ "$1" != "start" ] && [ "$1" != "stop" ] ;
    then
        echo "Wrong arguments for jomanager"
        exit 1
    fi

    echo "taskmanager " $1

    cd ${FLINK_HOME}/bin && ./taskmanager.sh $1
}

function flink_clean() {
    echo "$(date +'%d/%m/%y %T') Flink clean logs"
    rm -rf ${FLINK_HOME}/log/*
}

function kafka_install() {
    echo "$(date +'%d/%m/%y %T') Install Kafka"
#    cd ${REPO_HOME}/${DOWNLOADS}
    cd ${DOWNLOADS}
    wget --quiet --no-check-certificate https://dlcdn.apache.org/kafka/3.1.0/kafka_2.12-3.1.0.tgz
    cd ${REPO_HOME}
    tar -zxvf ${DOWNLOADS}/kafka_2.12-3.1.0.tgz > /dev/null 2>&1
    echo "transaction.max.timeout.ms=90000000" >> kafka_2.12-3.1.0/config/server.properties
}

function kafka_start() {
    echo "$(date +'%d/%m/%y %T') Start Kafka"
    # start zookeeper
    ${KAFKA_HOME}/bin/zookeeper-server-start.sh -daemon ${KAFKA_HOME}/config/zookeeper.properties
    sleep 2
    # start kafka server
    ${KAFKA_HOME}/bin/kafka-server-start.sh -daemon ${KAFKA_HOME}/config/server.properties 
    sleep 3
}

function kafka_stop() {
    echo "$(date +'%d/%m/%y %T') Stop Kafka"
    ${KAFKA_HOME}/bin/kafka-server-stop.sh
    ${KAFKA_HOME}/bin/zookeeper-server-stop.sh
}

function kafka_create_topics() {
    echo "$(date +'%d/%m/%y %T') Create Kafka topics"
    ${KAFKA_HOME}/bin/kafka-topics.sh --create --topic topic --partitions 1 --replication-factor 1 --bootstrap-server localhost:9092
    ${KAFKA_HOME}/bin/kafka-topics.sh --create --topic topicQ1 --partitions 1 --replication-factor 1 --bootstrap-server localhost:9092
    ${KAFKA_HOME}/bin/kafka-topics.sh --create --topic topicQ2 --partitions 1 --replication-factor 1 --bootstrap-server localhost:9092
}

function kafka_clean() {
    echo "$(date +'%d/%m/%y %T') Kafka clean"
    rm -rf /tmp/zookeeper
    rm -rf /tmp/kafka-logs
    rm -rf ${KAFKA_HOME}/logs/*
}

function application_build() {
    echo "$(date +'%d/%m/%y %T') Build binaries"

    # use a predefined folder containig src code for TESTING
    DATA_LOADER_HOME=${REPO_HOME}/gRPC
    cd ${DATA_LOADER_HOME}
    mvn clean package

    FLINK_JOB=${REPO_HOME}/StockAnalysisApp
    cd ${FLINK_JOB}
    mvn clean package
}

function ingest_job_start() {
    echo "$(date +'%d/%m/%y %T') Start ingesting data"
	cd ${REPO_HOME}
    BINARY=${REPO_HOME}/gRPC/target/gRPC-1.0-SNAPSHOT-jar-with-dependencies.jar
    nohup java -jar ${BINARY} ${REPORT_MODE} > ingest.log 2>&1 &
}

function flink_job_start() {
    echo "$(date +'%d/%m/%y %T') Start flink job"
#    PARALLELISM=1
    APP_BIN="${REPO_HOME}/StockAnalysisApp/target/StockAnalysisApp-0.1.jar"
    APP_PARAMS="${J1_ARG} ${J2_ARG} ${CHECKPOINT_INTERVAL} ${FLINK_CHECKPOINT_DIR}"
    ${FLINK_HOME}/bin/flink run -d -p ${PARALLELISM} ${APP_BIN} ${APP_PARAMS}
}

function platform_start() {
	kafka_start
	sleep 5
	kafka_create_topics

	flink_manage_jm start
	flink_manage_tm start
}

function platform_stop() {
	kafka_stop
	sleep 2
	kafka_clean
	flink_manage_jm stop
	flink_manage_tm stop
}

function parse_start_args() {
#    shift
    
    while getopts p:i:j:c:q: opt; do
        case $opt in
            p)
                PARALLELISM=$OPTARG
            ;;
            i)
                J1_ARG=$OPTARG
                ;;
            j)
                J2_ARG=$OPTARG
                ;;
            c)
                CHECKPOINT_INTERVAL=$OPTARG
                ;;
            q)
                REPORT_MODE=$OPTARG
                ;;
            \?) 
                echo "Invalid argument"
                echo ""
                help
                exit;
            ;;
        esac
    done
#    echo "-- IN -- par: $PARALLELISM j1: $J1_ARG, j2: $J2_ARG"
}



# Check num of arguments
if [ $# -lt 1 ]; then
    echo "Wrong arguments!"
#    echo $USAGE_MSG
    help
  exit 1
fi

ACTION=$1

case "$ACTION" in
    install)
    	install_utilities
	    kafka_install
    	flink_install
#	    redis_install
        exit
	    ;;
    build)
        application_build
        exit
        ;;
    start)
        shift   # ignre "start" parameter and parse next params
        parse_start_args "$@"
        echo "par: $PARALLELISM j1: $J1_ARG, j2: $J2_ARG, c: $CHECKPOINT_INTERVAL, q: $REPORT_MODE"
#    	application_build
	    sudo sh -c "sync; echo 3 > /proc/sys/vm/drop_caches "
    	kafka_start
	    sleep 8
    	kafka_create_topics
        flink_config
	    flink_manage_jm start
    	flink_manage_tm start
#	    redis_standalone_start
    	sleep 3
	    flink_job_start
    	sleep 10
	    ingest_job_start
        exit
    	;;
    stop)
    	kafka_stop
	    sleep 2
    	kafka_clean
	    flink_manage_jm stop
	    flink_manage_tm stop
#	    redis_shutdown
	    sudo sh -c "sync; echo 3 > /proc/sys/vm/drop_caches "
    	flink_clean
        exit
    	;;
    *)
        echo "Unknown argument $ACTION"
#        echo $USAGE_MSG
        echo ""
        help
        exit
        ;;
esac

