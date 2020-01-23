#!/bin/sh
cp ../../hcs-sxc-plugins/hcs-sxc-plugins-persistence-in-memory/target/hcs-sxc-plugins-persistence-in-memory-0.0.3-SNAPSHOT.jar ./target/lib
cp ../../hcs-sxc-plugins/hcs-sxc-plugins-mirror-queue-artemis/target/hcs-sxc-plugins-mirror-queue-artemis-0.0.3-SNAPSHOT.jar ./target/lib
cp .env ./target
cd target
java -cp 'hcs-sxc-simple-message-demo-0.0.3-SNAPSHOT.jar:lib/*' com.hedera.hcsapp.App $1

