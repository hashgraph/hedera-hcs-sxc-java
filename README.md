[![CircleCI](https://circleci.com/gh/hashgraph/hedera-hcs-sxc.svg?style=svg&circle-token=4f151711fb43e44d3d469cc1fbeaa17de4ab0c23)](https://circleci.com/gh/hashgraph/hedera-hcs-sxc)
[![codecov](https://img.shields.io/codecov/c/github/hashgraph/hedera-hcs-sxc/master)](https://codecov.io/gh/hashgraph/hedera-hcs-sxc)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](https://opensource.org/licenses/Apache-2.0)

# HCS-SXC

The HCS SXC (Secure eXtension for Communications) is a set of pre-built components that aim to provide additional functionality over and above HCS to make it easier and quicker to develop applications, particularly if they require secure communications between participants.

These components use the Hedera Java SDK to communicate with Hedera's HCS service and add a number of features (Italicised still in development/planning) as follows:

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

## The anatomy of an app with HCS SXC

An HCS SXC application is part of a network of applications which share the same code. They communicate messages with each other via the Hedera Consensus Service rather than in a peer to peer or other fashion.

The application code is likely to be akin to a state machine which responds to user inputs and generates HCS transactions as a result. It will also receive messages from a mirror node which may result in an update to the application's state.

All things being equal and making sure applications do not behave in an unpredictable way (using random number generators or external data sources independently for example), the state of all applications running in the app net should be the same at a given HCS message sequence number.

*Note: it is perfectly reasonable for an app to generate a random number and communicate this number to others via a HCS message, or fetch some data from an internet service and share it with others, but it would not be appropriate for each instance of an app to generate its own random number upon receiving a HCS message.*

### Messaging, signing and encryption

The most basic option for sending messages with HCS SXC Core is to send a non-encrypted message. This however will mean anyone with access to a mirror node will be able to read the messages that are being exchanged between app net participants on a given topic id.

In order to provide some privacy, the HCS SXC Core component will implement plugins for message encryption. These plugins may be used to provide different encryption methodologies.

Further, an encrypted message is only truly safe if the key used to decrypt it isn't known to the public, but only to the intended recipient of the message. One solution is to implement key rotation, whereby the keys used to encrypt and decrypt messages are rotated more or less frequently. Assuming rotated keys are discarded, it should not be possible to subsequently decrypt messages.

Finally, the HCS SXC Core component will provide functionality such that messages can be signed (whether encrypted or not), this can be used to prove the origin of a message.

### Persistence

The HCS SXC Core component provides some level of persistence, however this is not meant to implement application-specific persistence. The persistence afforded by the HCS SXC Core component provides transaction and message level persistence.

It is fully expected that an application would need to persist some data itself, data from messages exchanges with HCS for example, or data created as a result of HCS transactions (e.g. Bob bought this token from Alice). This application-level persisted data would constitute the application's state and will depend on each and every application rather than being common to all.

*Note: The settlement demo makes use of application level persistence, however it has been implemented such that a transaction sent to HCS is not considered part of state (it remains pending) until the transaction has been confirmed by mirror node at which point the state transition is confirmed.
This is to ensure application state does not end up out of sync with other applications in the event of a transaction processing failure for example.*

## Components

Looking through the java project, we have the following Maven components/artifacts.

* HCS-SXC
    * hcs-sxc-core
    * hcs-sxc-relay
    * hcs-sxc-interfaces
    * hcs-sxc-examples
        * hcs-sxc-simple-message-demo
        * hcs-sxc-settlement-demo
    * hcs-sxc-proto
    * hcs-sxc-plugins
        * hcs-sxc-plugins-persistence-in-memory
        * hcs-sxc-plugins-persistence-in-h2
        * hcs-sxc-plugins-mirror-direct
        * hcs-sxc-plugins-mirror-queue-artemis

### HCS-sxc-core

This component does the bulk of the work and is imported into a project (see example applications).

### HCS-sxc-relay

This component subscribes to topic(s) from a mirror node and forwards messages to a message queue. `AppNet` participants subscribe to the queue to receive messages.

### HCS-Interfaces

A set of standard interfaces or structures for the various components to communicate with each other. Listed below are those that are used in the context of plug-ins which have to satisfy particular interface requirements.

* HCSCallBackFromMirror - so that an app can register with the `hcs-sxc-core` for callbacks
* HCSCallBackToAppInterface - so that the `hcs-sxc-core` can call back to an app
* MirrorSubscriptionInterface - so that plugins can be made to subscribe to mirror notifications
* SxcMessagePersistence - so that plugins can be used to persist data

Defined in the `HCS-Interfaces` project, these are data structures that are shared between components.

* HCSRelayMessage - a message from the `hcs-sxc-relay` components
* HCSResponse - a application message id and message
* SxcConsensusMessage - a (temporary) POJO for consensus messages (until these can be serialized)
* MessagePersistenceLevel - a list of pre-defined persistence levels

### HCS-SXC-Plugins

This project contains a series of plugins to be used in conjunction with the hcs sxc core component, at the time of writing, the following plug-ins are available.
The choice of a plug-in architecture is to enable additional plugins to be developed without needing to change the projects that may later depend on them and so to offer extensibility with a choice of options.

* hcs-sxc-plugins-mirror-direct - plugin to enable the `hcs-sxc-core` to subscribe to mirror notifications directly
* hcs-sxc-plugins-mirror-queue-artemis - plugin to enable the `hcs-sxc-core` to subscribe to mirror notifications via an Artemis Message Queue (which receives messages via the `hcs-sxc-relay` component)
* hcs-sxc-plugins-persistence-in-h2 - plug in to provide data persistence in a database (H2)
* hcs-sxc-plugins-persistence-in-memory - plug in to provide data persistence in memory

### HCS-SXC Proto

Defines the protobuf messages used within `hcs-sxc-core`.

### Artemis Message Queue

This is not a java project, but a component which is started by way of a docker image if necessary.

## Choosing which plugins to use

To choose whether to use the `direct` or `hcs-sxc-relay+activeMQ` subscription method, include either the first or second dependency below in your application's `pom.xml`

for direct

```
<groupId>com.hedera</groupId>
<artifactId>hcs-sxc-plugins-mirror-direct</artifactId>
<version>0.0.3-SNAPSHOT</version>
```

for Artemis Message Queue

```
<groupId>com.hedera</groupId>
<artifactId>hcs-sxc-plugins-mirror-queue-artemis</artifactId>
<version>0.0.3-SNAPSHOT</version>
```

If you choose Artemis Message Queue, you must run a `hcs-sxc-relay` to ensure the queue is given messages to persist on behalf of `AppNet` participants.

To choose whether to use the `in memory` or `in database` persistence plug in, include either the first or second dependency below in your application's `pom.xml`

for in memory

```
<groupId>com.hedera</groupId>
<artifactId>hcs-sxc-plugins-persistence-in-memory</artifactId>
<version>0.0.3-SNAPSHOT</version>
```

for in database

```
<groupId>com.hedera</groupId>
<artifactId>hcs-sxc-plugins-persistence-in-h2</artifactId>
<version>0.0.3-SNAPSHOT</version>
```

*Note 1: Version numbers may change over time.*

*Note 2: Class loading should happen from the class path if the correct jar is found, it may therefore not be absolutely necessary to declare dependencies here since loading of a class matching the appropriate interface (`MirrorSubscriptionInterface` or `SxcMessagePersistence`) will happen dynamically when the application starts.*

## Configuration files

A number of configuration files are necessary in order to provide the components the necessary information such as which TopicId(s) to use or subscribe to, etc... These files are listed and explained below.

### HCS-sxc-relay

The `relay-config.yaml` file contains the necessary configuration for the `hcs-sxc-relay` component and is found in `/src/main/resources` of the corresponding java project. A sample file is provided as a starting point.

*Note: If a `relay-config.yaml` file is found in the root of the project, it will override the file from `src/main/resources`*

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
  tcpConnectionFactory: "tcp://hcs-sxc-queue:61616?jms.redeliveryPolicy.initialRedeliveryDelay=0&jms.redeliveryPolicy.backOffMultiplier=1&jms.redeliveryPolicy.maximumRedeliveries=5&jms.redeliveryPolicy.redeliveryDelay=500&jms.redeliveryPolicy.useExponentialBackOff=false"
```

### hcs-sxc-plugins-mirror-queue-artemis

The `queue-config.yaml` file contains the necessary configuration for the `hcs-sxc-plugins-mirror-queue-artemis` component and is found in the `/src/main/resources` of the corresponding java project. A sample file is provided as a starting point.

*Note: If a `queue-config.yaml` file is found in the root of the project, it will override the file from `src/main/resources`*

```
queue:
  tcpConnectionFactory: "tcp://hcs-sxc-queue:61616?jms.redeliveryPolicy.initialRedeliveryDelay=0&jms.redeliveryPolicy.backOffMultiplier=1&jms.redeliveryPolicy.maximumRedeliveries=5&jms.redeliveryPolicy.redeliveryDelay=500&jms.redeliveryPolicy.useExponentialBackOff=false"
  initialContextFactory: "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory"
```

### Applications

Applications will vary in use cases, however the `hcs-sxc-core` expects the application to provide a number of configurable parameters, these are defined in the `config.yaml` file which resides in the `/src/main/resources` folder of the application's project.

*Note: If a `config.yaml` file is found in the root of the project, it will override the file from `src/main/resources`*

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
#  - address: 1.testnet.hedera.com:50211
#    account: 0.0.4
#  - address: 2.testnet.hedera.com:50211
#    account: 0.0.5
#  - address: 3.testnet.hedera.com:50211
#    account: 0.0.6
```

In addition to the `config.yaml` file, a `.env` file may be provided (or environment variables set) for the application to be able to submit transactions to Hedera. Again, a sample file is provided with the examples (`dotenv.sample`).

```
OPERATOR_KEY=
OPERATOR_ID=0.0.2
# APP Net
APP_ID=0
```

The `OPERATOR_KEY` is the private key of the account identified by `OPERATOR_ID`.

*Note: When running in your java IDE or standalone in a command line, the host's environment variables take precedence over those in the `.env` file.*

## Docker

Docker is a convenient way of starting a number of individual components, we use it extensively in the `hcs-sxc-settlement-demo` example, but also to start up the `hcs-sxc-relay` and `Artemis MQ` components when necessary. Below is an example `docker-compose.yml` file for this purpose. You may wish to extend it with your own application-specific images if necessary.

```
version: '3.3'
services:
  hcs-sxc-queue:
    container_name: hcs-sxc-queue
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

  hcs-sxc-relay:
    container_name: hcs-sxc-relay
    depends_on:
      - hcs-sxc-queue
    image: hederahashgraph/hcs-sxc-relay:latest
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

### Sending a HCS message via the core component

```java
    TransactionId transactionId = new OutboundHCSMessage(appData.getHCSCore())
            .sendMessage(appData.getTopicIndex(), myMessage.toByteArray());
```

*Note 1: `myMessage` may be larger than 4k, in which case the hcs sxc core will take care of breaking it up into multiple transactions, and recombining the contents of each transaction post-consensus to rebuild the message.*

*Note 2: If the `hcs-sxc-core` is setup to encrypt, sign, key-rotate (subject to availability), this will all happen automatically, the application developer need not worry about it*

### Susbcribing to a topic via `hcs-sxc-core`

```java
    public HCSIntegration() throws Exception {
        this.appData = new AppData();
        // create a callback object to receive the message
        OnHCSMessageCallback onHCSMessageCallback = new OnHCSMessageCallback(appData.getHCSCore());
        onHCSMessageCallback.addObserver(hcsMessage -> {
            processHCSMessage(hcsMessage);
        });
    }
```

### Handling a notification from `hcs-sxc-core`

```
    public void processHCSMessage(HCSResponse hcsResponse) {
        try {
            ApplicationMessage applicationMessage = ApplicationMessage.parseFrom(hcsResponse.getMessage());
            ...
            ...
        }
    }
```

## Compiling the project

*Note: The project uses [lombok](https://projectlombok.org/) which is "a java library that automatically plugs into your editor and build tools, spicing up your java". Some IDEs require that a plug in is installed in order for lombok to work.*

### Pre-requisites

- The project is built on java 10.
- Docker and Docker-Compose

### Compilation steps

- Ensure the necessary configuration files are complete and accurate
    - hcs-sxc-relay/src/main/resources/relay-config.yaml (use relay-config.yaml.sample as a starting point)
    - hcs-sxc-plugins/hcs-sxc-plugins-mirror-queue-artemis/src/main/resources/queue-config.yaml (use queue-config.yaml.sample as a starting point)
    - hcs-sxc-examples/hcs-sxc-settlement-demo/src/main/resources/.env (use dotenv.sample as a starting point)
    - hcs-sxc-examples/hcs-sxc-settlement-demo/src/main/resources/.config.yaml (use config.yaml.sample as a starting point)
    - hcs-sxc-examples/hcs-sxc-settlement-demo/src/main/resources/docker-compose.yml
    - hcs-sxc-examples/hcs-sxc-simple-message-demo/src/main/resources/apps.yaml (use apps.yaml.sample as a starting point)
    - hcs-sxc-examples/hcs-sxc-simple-message-demo/src/main/resources/config.yaml (use config.yaml.sample as a starting point)
    - hcs-sxc-examples/hcs-sxc-simple-message-demo/docker-compose.yml

#### Compile docker images

From the top of the project, issue the following command to compile docker images

```shell
mvn clean install -Dcom.hedera.hashgraph.sdk.experimental=true -Pdocker
```

*Note: a `mvnw` executable is provided in the project in the event you don't have maven installed*

#### Compile "fat" jars

From the top of the project, issue the following command to create fat jars

```shell
mvn clean install -Dcom.hedera.hashgraph.sdk.experimental=true -Pfatjar
```

*Note: a `mvnw` executable is provided in the project in the event you don't have maven installed*

## Running the project in your IDE

Note that you need to add the following to your VM Arguments `-Dcom.hedera.hashgraph.sdk.experimental=true`.

You may also need to setup environment variables to match those in the `.env` and `docker-compose.yml` files.

## Examples

The project comes with two examples to get you started, these are fully functional examples. The first `hcs-sxc-simple-message-demo` is a simple command line example where running two instances of the application side by side, you can witness that a message sent from one app is reflected in the other. The first app sends the message to Hedera and the second receives it via a subscription to a mirror node. The opposite also works. The second example `hcs-sxc-settlement-demo` is a more complex application which is based on spring boot with a web UI. Each instance of the application represents a participant in a settlement use case where participants can issue credit notes to each other, approve them, group them to reach a settlement amount, employ a third party to effect the payment and finally both original parties confirm the payment was completed. In addition to this, an audit log is provided so that the full history of messages between participants can be consulted.

### hcs-sxc-simple-message-demo

This is a simple messaging demo between two participants. All messages sent from one participant are pushed to the Hedera HCS service and each participant subscribes to a mirror node to receive the consensus messages.

To run the demo, first create a new HCS topic using the SDK and edit the `src/main/resources/config.yaml` file to reflect the new topic id. This is to ensure that when you run the demo, you don't receive messages from someone else who you may be sharing a topic id with - although that could be fun.
Also check other details such as the mirror node, hedera network, etc... are correct.

You will also need to ensure the same topic id is reflected in `hcs-sxc-relay/src/main/resources/relay-config.yaml`

Also create a `.env` file with the following information

```
OPERATOR_KEY=
OPERATOR_ID=0.0.2
```

This demo uses the queue and relay components. For the apps to connect to the queue, an entry in your hosts file needs to be added as follows:

```text
127.0.0.1       hcs-sxc-queue
```

Compile the project (see above) and open three console terminals and switch to the folder/directory containing the `hcs-sxc-simple-message-demo` example on your computer.

In the first, run the docker images for the queue and relay.

```shell
docker-compose up
```

once the components are up and running

```shell
hcs-sxc-relay_1  | 2020-01-07 13:07:22 [Thread-1] INFO  MirrorTopicSubscriber:131 - Sleeping 30s
hcs-sxc-relay_1  | 2020-01-07 13:07:52 [Thread-1] INFO  MirrorTopicSubscriber:131 - Sleeping 30s
```

switch to the second terminal window and type

linux/mac: `./runapp.sh 0`, windows: `runapp.cmd 0`

in the third terminal window, type

linux/mac: `./runapp.sh 1`, windows: `runapp.cmd 1`

both windows should show a prompt

```
****************************************
** Welcome to a simple HCS demo
** I am app: Player 1
****************************************
```

typing text and pressing `[RETURN]` should result in the message appearing in the other application's window after a short consensus delay.

Both applications see the sent message, this is because both applications subscribe to mirror node notifications on topic and the sender essentially receives its own messages as well as those from others.

### hcs-sxc-settlement-demo

This is a more complex application which is based on spring boot with a web UI. Each instance of the application represents a participant in a settlement use case where participants can issue credit notes to each other, approve them, group them to reach a settlement amount, employ a third party to effect the payment and finally both original parties confirm the payment was completed. In addition to this, an audit log is provided so that the full history of messages between participants can be consulted.

To run the demo, first create a new HCS topic using the SDK and edit the `src/main/resources/config.yaml` file to reflect the new topic id. This is to ensure that when you run the demo, you don't receive messages from someone else who you may be sharing a topic id with - although that could be fun.
Also check other details such as the mirror node, hedera network, etc... are correct.

Also create a `.env` file with the following information

```
OPERATOR_KEY=
OPERATOR_ID=0.0.2
# APP Net
APP_ID=0
```

This demo does not use the queue and relay components, although it's possible to enable them by modifying the `pom.xml` file of the `hcs-sxc-settlement-demo` project to include them, they will also need to run as docker containers.

Compile the project (see above) and open a console terminal and switch to the folder/directory containing the `hcs-sxc-settlement-demo` example on your computer.

Then switch to `src/main/resources` and run the docker images as follows

```shell
docker-compose -f docker-compose-direct.yml up --remove-orphans
```

once the components are up and running (this may take a while), you can navigate to the UIs of the respective application users. Note: An instance of the `hcs-sxc-settlement-demo` application is run for each of the users and offered up on a separate http port.

You can see all the participants by navigating to one of the application's landing page

http://localhost:8081/landing.html

And from there, open a new page for each of the participants

* Alice http://localhost:8081
* Bob http://localhost:8082
* Carlos http://localhost:8083
* Worldpay http://localhost:8084
* Erica http://localhost:8085
* Farouk http://localhost:8086
* Grace http://localhost:8087
* Stripe http://localhost:8088

Whenever a participant performs and action in the UI, this results in a HCS transaction containing an `application-message` which itself contains a `business-message` containing the user's intent. Once the transaction has reached consensus, it's broadcast to all participants since they all subscribe to the same topic on a mirror node.

## Contributing

Contributions are welcome. Please see the [contributing](CONTRIBUTING.md) guide to see how you can get
involved.

## Code of Conduct

This project is governed by the [Contributor Covenant Code of Conduct](CODE_OF_CONDUCT.md). By participating, you are
expected to uphold this code of conduct. Please report unacceptable behavior to [oss@hedera.com](mailto:oss@hedera.com)

## License

[Apache License 2.0](LICENSE)
