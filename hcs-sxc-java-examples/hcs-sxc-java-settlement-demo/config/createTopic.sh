#!/bin/sh
cd /hedera-hcs-sxc-java/hcs-sxc-java-examples/hcs-sxc-java-settlement-demo

export LOGBACK_CONFIG_FILE_LOCATION=target/classes/logback.xml
java -Dlogback.configurationFile=${LOGBACK_CONFIG_FILE_LOCATION} -jar target/hcs-sxc-java-settlement-demo-0.0.3-SNAPSHOT.jar "createTopic"
