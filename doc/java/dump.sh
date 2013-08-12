#!/bin/bash

ROOTDIR="/var/opt/gsmr"


# ======================= functions =======================
function invalidUsage {
    echo "================== !!!!!!!!!!!!!! ==================="
    echo "  $1                                                 "
    echo "  Usage:                                             "
    echo "      ./dump.sh [instanceNumber default 1]          "
    echo "================== !!!!!!!!!!!!!! ==================="
    exit 1
}

function isNumber() {
    case $1 in     
        ''|*[!0-9]*)
            return 0
            ;; 
        *) 
            return 1 
    esac 
}

# ======================= Verify instance number =======================
INSTANCE_NUMBER=1

if [ $# -gt 0 ]
then
    INSTANCE_NUMBER=$1
fi

case $INSTANCE_NUMBER in
    ''|*[!0-9]*) invalidUsage "instance number, must be a number"
    ;;
esac    

# ============== Verify process id info ==============
cd $ROOTDIR

PID_FNAME=./pid${INSTANCE_NUMBER}.txt
PID_VALUE=0

if [ -f $PID_FNAME ]
then
    read PID_VALUE < $PID_FNAME
else 
    PID_VALUE="null"    
fi


PS_RESULT=0
isNumber $PID_VALUE
if [ $? -eq 1 ]; 
then 
    PS_RESULT=$(ps -p $PID_VALUE | grep -i java| wc -l | tr -d ' ');
fi    
  
IS_RUNNING="false"  
if [ $PS_RESULT -gt 0 ]
then
    IS_RUNNING="true"
fi  
  
echo "Using PID fileName=$PID_FNAME, ProcessId=$PID_VALUE, Is running=$IS_RUNNING"    
if [ $PS_RESULT -eq 0 ];
then
    echo "GSMR process not found"
    exit 1
fi


HOST=`hostname`
SITE=`hostname | cut -d "-" -f1 | /usr/xpg4/bin/tr [:upper:] [:lower:]`
ENV=`hostname | cut -d "-" -f2 | /usr/xpg4/bin/tr [:upper:] [:lower:]`
NAME=`hostname | cut -d "-" -f3 | /usr/xpg4/bin/tr [:upper:] [:lower:]`
SERVERNUMBER=`hostname | cut -d "-" -f4`
UNIQUE_NAME=`echo ${ENV}_GS_${SERVERNUMBER}_${INSTANCE_NUMBER} | /usr/xpg4/bin/tr [:lower:] [:upper:]`
TODAY=`date +"%Y-%m-%d"`
GOTIME=`date +"%H%M%S"`

export JAVA_HOME=/usr/jdk/instances/jdk1.6.0
mkdir -p /var/opt/gsmr/logs-jvm

FileName="${ROOTDIR}/logs-jvm/${TODAY}-Router_${UNIQUE_NAME}_threaddump-${GOTIME}.txt"
echo "jstack threaddump file: $FileName"    
${JAVA_HOME}/bin/jstack -l -d64 $PID_VALUE > $FileName

FileName="${ROOTDIR}/logs-jvm/${TODAY}-Router_${UNIQUE_NAME}_heapdump-${GOTIME}.hprof"
echo "jmap dump file: $FileName"    
${JAVA_HOME}/bin/jmap -dump:live,file=$FileName -d64 $PID_VALUE

FileName="${ROOTDIR}/logs-jvm/${TODAY}-Router_${UNIQUE_NAME}_heap-${GOTIME}.txt"
echo "jmap heap file: $FileName"    
${JAVA_HOME}/bin/jmap -heap -d64 $PID_VALUE > $FileName 

FileName="${ROOTDIR}/logs-jvm/${TODAY}-Router_${UNIQUE_NAME}_permstat-${GOTIME}.txt"
echo "jmap permstat file: $FileName"    
${JAVA_HOME}/bin/jmap -permstat -d64 $PID_VALUE > $FileName 

FileName="${ROOTDIR}/logs-jvm/${TODAY}-Router_${UNIQUE_NAME}_histo-${GOTIME}.txt"
echo "jmap histo file: $FileName"    
${JAVA_HOME}/bin/jmap -histo:live -d64 $PID_VALUE > $FileName 

FileName="${ROOTDIR}/logs-jvm/${TODAY}-Router_${UNIQUE_NAME}_pmap-${GOTIME}.txt"
echo "jmap pmap file: $FileName"    
${JAVA_HOME}/bin/jmap -d64 $PID_VALUE > $FileName 

FileName="${ROOTDIR}/logs-jvm/${TODAY}-Router_${UNIQUE_NAME}_finalizer-${GOTIME}.txt"
echo "jmap finalizerinfo file: $FileName"    
${JAVA_HOME}/bin/jmap -finalizerinfo -d64 $PID_VALUE > $FileName 

FileName="${ROOTDIR}/logs-jvm/${TODAY}-Router_${UNIQUE_NAME}_prstat-${GOTIME}.txt"
echo "prstat file: $FileName"    
prstat -mL -c -p $PID_VALUE 1 5 > $FileName 




