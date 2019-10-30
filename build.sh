#!/bin/sh
cd hcs-lib
./mvnw install -DskipTests

cd ../hcs-relay
./mvnw install -DskipTests

cd ../examples/simple-message-demo
./mvnw install -DskipTests

cd ../..

