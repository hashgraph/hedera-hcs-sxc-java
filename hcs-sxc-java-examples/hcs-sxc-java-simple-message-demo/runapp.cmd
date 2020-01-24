copy ..\..\hcs-sxc-java-plugins\hcs-sxc-java-plugins-persistence-in-memory\target\hcs-sxc-java-plugins-persistence-in-memory-0.0.3-SNAPSHOT.jar target\lib
copy ..\..\hcs-sxc-java-plugins\hcs-sxc-java-plugins-mirror-queue-artemis\target\hcs-sxc-java-plugins-mirror-queue-artemis-0.0.3-SNAPSHOT.jar target\lib
xcopy /i /h /y config\*.* target\config
cd target
java -cp 'hcs-sxc-java-simple-message-demo-0.0.3-SNAPSHOT.jar:lib/*' com.hedera.hcsapp.App $1
