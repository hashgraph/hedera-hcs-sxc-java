cd hcs-lib
mvnw.cmd install -DskipTests

cd ..\hcs-relay
mvnw.cmd install -DskipTests

cd ..\examples/simple-message-demo
mvnw.cmd install -DskipTests
