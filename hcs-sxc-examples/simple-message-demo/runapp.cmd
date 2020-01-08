copy ..\..\hcs-sxc-plugins\hcs-sxc-plugins-persistence-in-memory\target\hcs-sxc-plugins-persistence-in-memory-0.0.3-SNAPSHOT.jar target\lib
copy ..\..\hcs-sxc-plugins\hcs-sxc-plugins-mirror-queue-artemis\target\hcs-sxc-plugins-mirror-queue-artemis-0.0.3-SNAPSHOT.jar target\lib
copy .env target
cd target
java -Dcom.hedera.hashgraph.sdk.experimental=true -cp 'simple-message-demo-0.0.3-SNAPSHOT.jar:lib/*' com.hedera.hcsapp.App $1
