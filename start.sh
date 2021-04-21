#!/bin/sh

export K8S_HOME=/home/tmax/hyperesm
/usr/bin/java -jar -Dlogback.configurationFile=${K8S_HOME}/logback.xml ${K8S_HOME}/lib/hyperesm.jar
