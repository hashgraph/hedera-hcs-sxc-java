[![CircleCI](https://circleci.com/gh/hashgraph/hedera-hcs-sxc.svg?style=svg&circle-token=4f151711fb43e44d3d469cc1fbeaa17de4ab0c23)](https://circleci.com/gh/hashgraph/hedera-hcs-sxc)
[![codecov](https://img.shields.io/codecov/c/github/hashgraph/hedera-hcs-sxc/master)](https://codecov.io/gh/hashgraph/hedera-hcs-sxc)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](https://opensource.org/licenses/Apache-2.0)

# HCS-SXC

The HCS SXC (Secure eXchange Communication) is a set of pre-built components that aim to provide additional functionality over and above HCS to make it easier and quicker to develop applications, particularly if they require secure communications between participants.

These components use the Hedera Java SDK to communicate with Hedera's HCS service and add a number of features (Italicizised still in development/planning) as follows:

- Sending messages to Hedera's HCS service
    - *With optional message encryption* (subject to plug in development)
    - *With optional key rotation* (subject to plug in development)
    - *With message signature*
    - Optionally across multiple topics
- Chunking and re-assembly of large messages (Hedera transactions are limited to 4k)
- Protobuf application message structure
- Persistence of transactions sent and messages sent/received
    - In memory or in database via plugins. All messages are automatically persisted and made available to the application through an interface such that it can perform audit for example.
- Mirror node topic subscription
    - Via relay
    - Direct to mirror node
- Example applications
    - Simple console-based messaging app between two participants
    - Settlement use case demonstration with web UI

## Terminology used in this document

- AppNet - a network of applications using HCS to exchange messages
- Application Message - a message sent between AppNet participants using a standard envelope (the message itself may be broken up into several HCS transactions if too large)
- Business Process Message - an AppNet specific message which is sent to other participants inside an Application Message

## Components

Looking through the java project, we have the following Maven components/artifacts.

* HCS-SXC 
    * hcs-lib
    * hcs-relay
    * hcs-sxc-interfaces
    * hcs-sxc-examples
        * simple-message-demo
        * settlement-demo
    * hcs-sxc-proto
    * hcs-sxc-plugins
        * lib-persistence-in-memory
        * lib-persistence-in-h2
        * lib-mirror-direct
        * lib-mirror-queue-artemis

### HCS-Lib

This component does the bulk of the work and is imported into a project (see example applications).

### HCS-Relay

This component subscribes to topic(s) from a mirror node and forwards messages to a message queue. `AppNet` participants subscribe to the queue to receive messages.

### HCS-Interfaces

A set of standard interfaces or structures for the various components to communicate with each other. Listed below are those that are used in the context of plug-ins which have to satisfy particular interface requirements.

* HCSCallBackFromMirror - so that an app can register with the `hcs-lib` for callbacks 
* HCSCallBackToAppInterface - so that the `hcs-lib` can call back to an app
* MirrorSubscriptionInterface - so that plugins can be made to subscribe to mirror notifications
* LibMessagePersistence - so that plugins can be used to persist data

Defined in the `HCS-Interfaces` project, these are data structures that are shared between components.

* HCSRelayMessage - a message from the `HCS-Relay` components
* HCSResponse - a application message id and message
* LibConsensusMessage - a (temporary) POJO for consensus messages (until these can be serialized)
* MessagePersistenceLevel - a list of pre-defined persistence levels

### HCS-SXC-Plugins

This project contains a series of plugins to be used in conjunction with the lib, at the time of writing, the following plug-ins are available.
The choice of a plug-in architecture is to enable additional plugins to be developed without needing to change the projects that may later depend on them and so to offer extensibility with a choice of options.

* lib-mirror-direct - plugin to enable the `hcs-lib` to subscribe to mirror notifications directly
* lib-mirror-queue-artemis - plugin to enable the `hcs-lib` to subscribe to mirror notifications via an Artemis Message Queue (which receives messages via the `hcs-relay` component)
* lib-persistence-in-h2 - plug in to provide data persistence in a database (H2)
* lib-persistence-in-memory - plug in to provide data persistence in memory

### HCS-SXC Proto

Defines the protobuf messages used within `hcs-lib`.

### Artemis Message Queue

This is not a java project, but a component which is started by way of a docker image if necessary.

## Choosing which plugins to use

To choose whether to use the `direct` or `hcs-relay+activeMQ` subscription method, include either the first or second dependency below in your application's `pom.xml`

for direct

```
<groupId>com.hedera</groupId>
<artifactId>lib-mirror-direct</artifactId>
<version>0.0.3-SNAPSHOT</version>
```

for Artemis Message Queue

```
<groupId>com.hedera</groupId>
<artifactId>lib-mirror-queue-artemis</artifactId> 
<version>0.0.3-SNAPSHOT</version>
```

If you choose Artemis Message Queue, you must run a `hcs-relay` to ensure the queue is given messages to persist on behalf of `AppNet` participants.

To choose whether to use the `in memory` or `in database` persistence plug in, include either the first or second dependency below in your application's `pom.xml`

for in memory

```
<groupId>com.hedera</groupId> 
<artifactId>lib-persistence-in-memory</artifactId> 
<version>0.0.3-SNAPSHOT</version>
```

for in database

```
<groupId>com.hedera</groupId>
<artifactId>lib-persistence-in-h2</artifactId>
<version>0.0.3-SNAPSHOT</version>
```

*Note 1: Version numbers may change over time.*

*Note 2: Class loading should happen from the class path if the correct jar is found, it may therefore not be absolutely necessary to declare dependencies here since loading of a class matching the appropriate interface (`MirrorSubscriptionInterface` or `LibMessagePersistence`) will happen dynamically when the application starts.*

## Configuration files

A number of configuration files are necessary in order to provide the components the necessary information such as which TopicId(s) to use or subscribe to, etc... These files are listed and explained below.

### HCS-relay

The `relay-config.yaml` file contains the necessary configuration for the `hcs-relay` component and is found in `/src/main/resources` of the corresponding java project. A sample file is provided as a starting point.

*Note: If a `relay-config.yaml` file is found in the root of the project, it will override the file from src/main/resources*

```
# Address of the mirror node's subscription end point
mirrorAddress: "34.66.214.12:6552"

# The frequency (in minutes) at which a subscription to mirror node is restarted
mirrorReconnectDelay: 10  

# The topic IDs to subscribe to
topics:
  - topic: "0.0.1044"
  
# Should relay catch up with message history on startup
catchupHistory: true

# Keeps the consensus time of the last received message in this file
lastConsensusTimeFile: "./lastConsensusTime.txt"

# Connection details to the Artemis MQ component
queue:
  initialContextFactory: "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory"
  tcpConnectionFactory: "tcp://hcsqueue:61616?jms.redeliveryPolicy.initialRedeliveryDelay=0&jms.redeliveryPolicy.backOffMultiplier=1&jms.redeliveryPolicy.maximumRedeliveries=5&jms.redeliveryPolicy.redeliveryDelay=500&jms.redeliveryPolicy.useExponentialBackOff=false"
```

### lib-mirror-queue-artemis

The `queue-config.yaml` file contains the necessary configuration for the `lib-mirror-queue-artemis` component and is found in the `/src/main/resources` of the corresponding java project. A sample file is provided as a starting point.

*Note: If a `queue-config.yaml` file is found in the root of the project, it will override the file from src/main/resources*

```
queue:
  tcpConnectionFactory: "tcp://hcsqueue:61616?jms.redeliveryPolicy.initialRedeliveryDelay=0&jms.redeliveryPolicy.backOffMultiplier=1&jms.redeliveryPolicy.maximumRedeliveries=5&jms.redeliveryPolicy.redeliveryDelay=500&jms.redeliveryPolicy.useExponentialBackOff=false"
  initialContextFactory: "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory"
```

### Applications

Applications will vary in use cases, however the `hcs-lib` expects the application to provide a number of configurable parameters, these are defined in the `config.yaml` file which resides in the `/src/main/resources` folder of the application's project.

*Note: If a `config.yaml` file is found in the root of the project, it will override the file from src/main/resources*

```
appNet:
  # Should messages be signed
  signMessages: false
  # Should messages be encrypted
  encryptMessages: false
  # Should messages be rotated
  rotateKeys: false
  # How often (messages) should keys be rotated
  rotateKeyFrequency: 0
  # HCS topics to be used
  topics:
    - topic: 0.0.1072
  # Which level of persistence should be used
  persistenceLevel: "FULL"
  # Should history of messages be caught up
  catchupHistory: true
  
# Default HCS transaction fee in tinybar
HCSTransactionFee: 100000000

mirrorNode:
  # Address of the mirror node's subscription end point
  address: "34.66.214.12:6552"
  # automatically disconnect/reconnect from mirror node every reconnectDelay minutes
  reconnectDelay: 10  

# List of Hedera Nodes to send HCS messages to, if more than one is specified, the SDK will randomly choose a node with each transaction
nodes:
  - address: 34.66.214.12:50211
    account: 0.0.4
# Commented out due to bug in java SDK
#  - address: 1.testnet.hedera.com:50211
#    account: 0.0.4
#  - address: 2.testnet.hedera.com:50211
#    account: 0.0.5
#  - address: 3.testnet.hedera.com:50211
#    account: 0.0.6
```

In addition to the `config.yaml` file, a `.env` file may be provided (or environment variables set) for the application to be able to submit transactions to Hedera. Again, a sample file is provided with the examples.

```
OPERATOR_KEY=
OPERATOR_ID=0.0.2
```

The `OPERATOR_KEY` is the private key of the account identified by `OPERATOR_ID`.

## Docker

Docker is a convenient way of starting a number of individual components, we use it extensively in the `settlement-demo` example, but also to start up the `hcs-relay` and `Artemis MQ` components when necessary. Below is an example `docker-compose.yml` file for this purpose. You may wish to extend it with your own application-specific images if necessary.

```
version: '3.3'
services:
  hcs-queue:
    container_name: hcs-queue
    image: vromero/activemq-artemis:2.10.1-alpine
    restart: on-failure
    ports:
      # ui
      - "8161:8161"
      # jms
      - "61616:61616"
      #- 61616
    networks:
      - backing-services
    volumes:
      - "activemq-data:/var/lib/artemis/data"
      - "activemq-data:/var/lib/artemis/etc"
      - "activemq-data:/var/lib/artemis/etc-override"
      - "activemq-data:/var/lib/artemis/lock"
      - "activemq-data:/opt/jmx-exporter/etc-override"

    environment:
      DISABLE_SECURITY: true
      ARTEMIS_USERNAME: hcsdemo
      ARTEMIS_PASSWORD: hcsdemo
      RESTORE_CONFIGURATION: true

  hcs-relay:
    container_name: hcs-relay
    depends_on:
      - hcs-queue
    image: hederahashgraph/hcs-relay:latest
    restart: on-failure
    networks:
      - backing-services

volumes:
  activemq-data: {}

networks:
  backing-services:
    driver: bridge
```

## Sample code

These are merely sample lines of code, please refer to the example projects for more details

### Sending a HCS message via the lib

```java
    TransactionId transactionId = new OutboundHCSMessage(appData.getHCSLib())
            .sendMessage(appData.getTopicIndex(), myMessage.toByteArray());
```

*Note 1: `myMessage` may be larger than 4k, in which case the libarary will take care of breaking it up into multiple transactions, and recombining the contents of each transaction post-consensus to rebuild the message.*

*Note 2: If the `hcs-lib` is setup to encrypt, sign, key-rotate (subject to availability), this will all happen automatically, the application developer need not worry about it*

### Susbcribing to a topic via `hcs-lib`

```java
    public HCSIntegration() throws Exception {
        this.appData = new AppData();
        // create a callback object to receive the message
        OnHCSMessageCallback onHCSMessageCallback = new OnHCSMessageCallback(appData.getHCSLib());
        onHCSMessageCallback.addObserver(hcsMessage -> {
            processHCSMessage(hcsMessage);
        });
    }
```

### Handling a notification from `hcs-lib`

```
    public void processHCSMessage(HCSResponse hcsResponse) {
        try {
            ApplicationMessage applicationMessage = ApplicationMessage.parseFrom(hcsResponse.getMessage());
            ...
            ...
        }
    }
```

## Examples

### Simple-message-demo

### Settlement-demo

