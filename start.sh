#!/bin/sh

export K8S_HOME=/home/tmax/service-broker
/usr/bin/java -jar -Dlogback.configurationFile=${K8S_HOME}/logback.xml ${K8S_HOME}/lib/template-service-broker.jar
