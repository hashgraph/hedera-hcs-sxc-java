# simple-message-demo

Simple demo to showcase HCS-SXC, this shows how up to three separate app instances can communicate with each other using HCS.

## Pre-requisites

Have docker and docker-compose installed.

## Build and run

### Unix/Mac

In terminal

```shell
mkdir hcs-sxc
cd hcs-sxc
git init
git clone https://github.com/hashgraph/hedera-hcs-sxc.git
cd hedera-hcs-sxc
cp hcs-relay/src/main/resources/config.yaml.sample hcs-relay/src/main/resources/config.yaml
nano cp hcs-relay/src/main/resources/config.yaml
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

open up to three additional terminal windows

```shell
cd hcs-sxc/
cd hedera-hcs-sxc
cd hcs-sxc-examples/simple-message-demo
./runapp x
```

where x is 0, 1 or 2, one value per terminal window

### Windows

In command line

```shell
mkdir hcs-sxc
cd hcs-sxc
git init
git clone https://github.com/hashgraph/hedera-hcs-sxc.git
cd hedera-hcs-sxc
copy hcs-relay\src\main\resources\config.yaml.sample hcs-relay\src\main\resources\config.yaml
edit cp hcs-relay\src\main\resources\config.yaml
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

open up to three additional terminal windows

```shell
cd hcs-sxc/
cd hedera-hcs-sxc
cd hcs-sxc-examples/simple-message-demo
runapp x
```

where x is 0, 1 or 2, one value per terminal window

