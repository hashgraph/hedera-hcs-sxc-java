# simple-message-demo

Simple demo to showcase HCS-SXC, this shows how up to three separate app instances can communicate with each other using HCS.

## Pre-requisites

- docker and docker-compose installed
- docker is running
- maven installed

## Build and run

### Unix/Mac

In terminal

```shell
mkdir java-sdk
cd java-sdk
git init
git clone -b HCS https://github.com/mike-burrage-hedera/hedera-sdk-java
cd hedera-sdk-java
mvn install -DskipTests

cd ../..

mkdir hcs-sxc
cd hcs-sxc
git init
git clone https://github.com/hashgraph/hedera-hcs-sxc.git
cd hedera-hcs-sxc
cp hcs-relay/src/main/resources/config.yaml.sample hcs-relay/src/main/resources/config.yaml
nano hcs-relay/src/main/resources/config.yaml
```

sample config.yaml

```
mirrorAddress: "35.222.103.151:6551"
topics:
  - topic: "0.0.1004"

queue:
  initialContextFactory: "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory"
  tcpConnectionFactory: "tcp://hcsqueue:61616"
```

In terminal

```shell
cp hcs-sxc-examples/simple-message-demo/src/main/resources/config.yaml.sample hcs-sxc-examples/simple-message-demo/src/main/resources/config.yaml
nano hcs-sxc-examples/simple-message-demo/src/main/resources/config.yaml
```

sample config.yaml

```
appNet:
  signMessages: false
  encryptMessages: false
  rotateKeys: false
  rotateKeyFrequency: 0
  topics:
    - topic: 0.0.1004
  persistenceLevel: "FULL"

queue:
  tcpConnectionFactory: "tcp://localhost:61616"
  initialContextFactory: "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory"
    
HCSTransactionFee: 100000000

nodes:
  - address: 35.222.103.151:50211
    account: 0.0.3
```

In terminal

```shell
./mvnw clean install -Pdocker
docker-compose up
```

open an additional terminal window

```shell
cd hcs-sxc
cd hedera-hcs-sxc
cd hcs-sxc-examples/simple-message-demo
cp .env.sample .env
nano .env
```

sample .env file

```
OPERATOR_KEY=your privake key (302e020100....)
OPERATOR_ID=0.0.2
APP_ID=0
```

```
./runapp.sh 0
```

then in two additional terminal windows

```shell
cd hcs-sxc
cd hedera-hcs-sxc
cd hcs-sxc-examples/simple-message-demo
./runapp.sh 1
```

```shell
cd hcs-sxc
cd hedera-hcs-sxc
cd hcs-sxc-examples/simple-message-demo
./runapp.sh 2
```

### Windows

In command line

```shell
mkdir java-sdk
cd java-sdk
git init
git clone -b HCS https://github.com/mike-burrage-hedera/hedera-sdk-java
cd hedera-sdk-java
mvn install -DskipTests

cd ..\..

mkdir hcs-sxc
cd hcs-sxc
git init
git clone https://github.com/hashgraph/hedera-hcs-sxc.git
cd hedera-hcs-sxc
copy hcs-relay\src\main\resources\config.yaml.sample hcs-relay\src\main\resources\config.yaml
edit hcs-relay\src\main\resources\config.yaml
```

sample config.yaml

```
mirrorAddress: "35.222.103.151:6551"
topics:
  - topic: "0.0.1004"

queue:
  initialContextFactory: "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory"
  tcpConnectionFactory: "tcp://hcsqueue:61616"
```

In command line

```shell
copy hcs-sxc-examples\simple-message-demo\src\main\resources\config.yaml.sample hcs-sxc-examples\simple-message-demo\src\main\resources\config.yaml
edit hcs-sxc-examples\simple-message-demo\src\main\resources\config.yaml
```

sample config.yaml

```
appNet:
  signMessages: false
  encryptMessages: false
  rotateKeys: false
  rotateKeyFrequency: 0
  topics:
    - topic: 0.0.1004
  persistenceLevel: "FULL"

queue:
  tcpConnectionFactory: "tcp://localhost:61616"
  initialContextFactory: "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory"
    
HCSTransactionFee: 100000000

nodes:
  - address: 35.222.103.151:50211
    account: 0.0.3
```

In command line

```shell
mvnw clean install -Pdocker
docker-compose up
```

open an additional terminal window

```shell
cd hcs-sxc
cd hedera-hcs-sxc
cd hcs-sxc-examples\simple-message-demo
copy .env.sample .env
edit .env
```

sample .env file

```
OPERATOR_KEY=your privake key (302e020100....)
OPERATOR_ID=0.0.2
APP_ID=0
```

```
runapp 0
```

then in two additional terminal windows

```shell
cd hcs-sxc
cd hedera-hcs-sxc
cd hcs-sxc-examples\simple-message-demo
runapp 1
```

```shell
cd hcs-sxc
cd hedera-hcs-sxc
cd hcs-sxc-examples\simple-message-demo
runapp 2
```
