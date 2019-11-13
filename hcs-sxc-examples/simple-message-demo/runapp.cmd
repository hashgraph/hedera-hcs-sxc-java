copy ..\..\hcs-sxc-plugins\lib-persistence-in-memory\target\lib-persistence-in-memory-0.0.1-SNAPSHOT.jar target\lib
copy .env target
cd target
java -cp 'simple-message-demo-0.0.1-SNAPSHOT.jar:lib/*' com.hedera.hcsapp.App $1
