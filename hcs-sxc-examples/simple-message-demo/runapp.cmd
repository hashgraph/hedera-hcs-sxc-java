copy ..\..\hcs-sxc-plugins\lib-persistence-in-memory\target\lib-persistence-in-memory-0.0.3-SNAPSHOT.jar target\lib
copy ..\..\hcs-sxc-plugins\lib-mirror-queue-artemis\target\lib-mirror-queue-artemis-0.0.3-SNAPSHOT.jar target\lib
copy .env target
cd target
java -Dcom.hedera.hashgraph.sdk.experimental=true -cp 'simple-message-demo-0.0.3-SNAPSHOT.jar:lib/*' com.hedera.hcsapp.App $1
