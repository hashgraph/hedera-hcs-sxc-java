[![CircleCI](https://circleci.com/gh/hashgraph/hedera-hcs-sxc-java.svg?style=svg&circle-token=4f151711fb43e44d3d469cc1fbeaa17de4ab0c23)](https://circleci.com/gh/hashgraph/hedera-hcs-sxc)
[![codecov](https://codecov.io/gh/hashgraph/hedera-hcs-sxc-java/branch/master/graph/badge.svg)](https://codecov.io/gh/hashgraph/hedera-hcs-sxc-java)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](https://opensource.org/licenses/Apache-2.0)

# HCS-SXC-Java
     
The HCS SXC Java project (SDK eXtension Components) is a set of pre-built components that aim to provide additional functionality over and above the java SDK for HCS to make it easier and quicker to develop applications.

These components use the Hedera Java SDK to communicate with Hedera's HCS service and add a number of features (Italicised still in development/planning) as follows:

- Sending messages to Hedera's HCS service
    - *With optional message encryption* (subject to plug in development)
    - *With optional key rotation* (subject to plug in development)
    - *With message signature*
    - Optionally across multiple topics
- Chunking and re-assembly of large messages (Hedera transactions are limited to 4k)
- Pairwise symmetric key encryption between participants
- Protobuf application message structure
- Persistence of transactions sent and messages sent/received
    - In memory or in database via plugins. All messages are automatically persisted and made available to the application through an interface such that it can perform audit for example.
- Mirror node topic subscription
    - Via relay
    - Direct to mirror node
- Example applications
    - Simple console-based messaging app between two participants
    - Settlement use case demonstration with web UI

## Terms and conditions

Please review the [Hedera Terms and Conditions](https://www.hedera.com/terms) prior to using this software.

This repository is only intended for demo purposes and not production applications.

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

Cryptographic signing-keys and encryption-keys are used at various levels throughout HCS SXC. At the lowest level we have a HCS message, which is constructed by the current Java SDK: as any other Hedera Hashgraph message, these messages are signed with private ed25519 keys. In HCS SXC, we call that key an OPERATOR_KEY and it is set up either in .env fires or is passed as a command line parameter. The OPERATOR_KEY is accompanied by the OPERATOR_ID which is a Hedera account number  - you may notice that the library doesn't ask you to specify the public key counterpart because the Hedera network's address-book has a record of the public key and thus can verify signatures for messages it receives.

HCS-SCX messages are higher level messages where each such message is broken up into smaller chunks which are then sent to the network as low level Hedera Hashgraph HCS messages. These messages are called ApplicationMessages where each message allows, optionally, to be signed and encrypted. HCS-SCX also uses ed25519 keys to sign such messages. Such signing-keys are private keys and the library provides various methods to set them up: the simplest setup is via an .env file where this private ed25519 key is identified as a SIGNING_KEY.

HCS-SCX signing keys are used to identify message-origin and this is done in conjunction of an address-book (contact-list.yaml) that lists all ed25519 public keys of participants an APP is communicating with. Such public ed25519 keys are identified as `theirPublicSigningKey'.  It is assumed that each participant has a unique signing key and it is not shared across participants.

HCS-SCX ApplicationMessages can optionally be encrypted: pairwise symmetric key encryption is used, that is, pairs of communicating entities share a common encryption key. You may notice that .env files don't specify encryption keys and this is because an App needs a different encryption key for each other entity it is communicating with. Such encryption keys are identified as sharedSymmetricEncryptionKey and are located in the contact-list.yaml along with theirPublicSigningKey. The type of the secret key is AppNet specific, however it is recommended to use a minimum of 256 bit secrets. The example plugin provides generateSecretKey() to generate 256 bit AES secrets

The encryption scheme that HCS SCX uses is plugin based and users can define their own by implementing the SxcMessageEncryption interface. The example Cryptopgraphy plugin implements AES/GCM/NoPadding encryption and uses 32 byte private keys. Encryption can  either be static where the same key is used throughout the life time of a communicating pair or rotating; optionally, the application can use a plugin based key rotation scheme and the example plugin implements Diffie Hellmann key exchange (DHKE). Notice that when key rotation is enabled then the initial secret generated by generateSecretKey() is  replaced by the shared secret that is generated by the key rotation implementation; however,  encryption remains to be AES. Thus, the  symmetric key is used until rotation is triggered. This is permissible because the AES standard does not specify a structure on the key used, but it is important too ensure that the size of the key generated they key rotation scheme is the same as the size required by the encryption plugin.


### Persistence

The HCS SXC Core component provides some level of persistence, however this is not meant to implement application-specific persistence. The persistence afforded by the HCS SXC Core component provides transaction and message level persistence.

It is fully expected that an application would need to persist some data itself, data from messages exchanges with HCS for example, or data created as a result of HCS transactions (e.g. Bob bought this token from Alice). This application-level persisted data would constitute the application's state and will depend on each and every application rather than being common to all.

*Note: The settlement demo makes use of application level persistence, however it has been implemented such that a transaction sent to HCS is not considered part of state (it remains pending) until the transaction has been confirmed by mirror node at which point the state transition is confirmed.
This is to ensure application state does not end up out of sync with other applications in the event of a transaction processing failure for example.*

## Components

Looking through the java project, we have the following Maven components/artifacts.

* hcs-sxc-java
    * hcs-sxc-java-core
    * hcs-sxc-java-coverage
    * hcs-sxc-java-relay
    * hcs-sxc-java-interfaces
    * hcs-sxc-java-examples
        * hcs-sxc-java-simple-message-demo
        * hcs-sxc-java-settlement-demo
    * hcs-sxc-java-proto
    * hcs-sxc-java-plugins
        * hcs-sxc-java-plugins-encryption-diffie-hellman
        * hcs-sxc-java-plugins-persistence-in-memory
        * hcs-sxc-java-plugins-persistence-hibernate
        * hcs-sxc-java-plugins-mirror-direct
        * hcs-sxc-java-plugins-mirror-queue-artemis

### hcs-sxc-java-core

This component does the bulk of the work and is imported into a project (see example applications).

### hcs-sxc-java-coverage

Test coverage aggregator, no code here.

### hcs-sxc-java-relay

This component subscribes to topic(s) from a mirror node and forwards messages to a message queue. `AppNet` participants subscribe to the queue to receive messages.

### hcs-sxc-java-interfaces

A set of standard interfaces or structures for the various components to communicate with each other. Listed below are those that are used in the context of plug-ins which have to satisfy particular interface requirements.

* HCSCallBackFromMirror - so that an app can register with the `hcs-sxc-java-core` for callbacks
* HCSCallBackToAppInterface - so that the `hcs-sxc-java-core` can call back to an app
* MirrorSubscriptionInterface - so that plugins can be made to subscribe to mirror notifications
* SxcPersistence - so that plugins can be used to persist data

Defined in the `hcs-sxc-java-Interfaces` project, these are data structures that are shared between components.

* HCSRelayMessage - a message from the `hcs-sxc-java-relay` components
* HCSResponse - a application message id and message
* SxcConsensusMessage - a (temporary) POJO for consensus messages (until these can be serialized)
* MessagePersistenceLevel - a list of pre-defined persistence levels

### hcs-sxc-java-plugins

This project contains a series of plugins to be used in conjunction with the hcs sxc core component, at the time of writing, the following plug-ins are available.
The choice of a plug-in architecture is to enable additional plugins to be developed without needing to change the projects that may later depend on them and so to offer extensibility with a choice of options.

* hcs-sxc-java-plugins-encryption-diffie-hellman - plugin to encrypt messages and manage key rotation using Diffie Hellman
* hcs-sxc-java-plugins-mirror-direct - plugin to enable the `hcs-sxc-java-core` to subscribe to mirror notifications directly
* hcs-sxc-java-plugins-mirror-queue-artemis - plugin to enable the `hcs-sxc-java-core` to subscribe to mirror notifications via an Artemis Message Queue (which receives messages via the `hcs-sxc-java-relay` component)
* hcs-sxc-java-plugins-persistence-hibernate - plug in to provide data persistence in a database through hibernate
* hcs-sxc-java-plugins-persistence-in-memory - plug in to provide data persistence in memory

#### hcs-sxc-java-plugins-encryption-diffie-hellman

Manages message encryption and decryption while also optionally dealing with key rotation using Diffie Hellman.

#### hcs-sxc-java-plugins-mirror-direct

This plugin provides a direct subscription to a mirror node which implements the same subscription API as the [open source mirror node project provided by Hedera](https://github.com/hashgraph/hedera-mirror-node). Other mirror node projects may have different subscription APIs in which case new plug ins conforming to those mirror nodes' subscription APIs would be required.

When in use, the plug in uses the configuration parameters from a `config.yaml` file to identify the IP address and port of the mirror node to subscribe to.

#### hcs-sxc-java-plugins-mirror-queue-artemis

This plugin provides a subscription to an Artemis Active MQ from which it will receive notifications from a mirror node. Note: An Active MQ service must be available and be populated with data from a mirror node via the `hcs-sxc-java-relay`.

#### hcs-sxc-java-plugins-persistence-hibernate

This plugin provides persistence of HCS transactions and mirror notifications into a database via hibernate. The default project configuration uses H2 as a database, but others can easily be implemented.

To configure a different database, edit the `coreHibernate` section of the `config.yaml` file of the application.

Example configuration for h2:

```
coreHibernate:
  # these values will be loaded as a map, add or remove properties as necessary
  # {appId} will be replaced with the application instance id
  hibernate.connection.driver_class: "org.h2.Driver"
  hibernate.connection.url: "jdbc:h2:./h2data/libdb-{appid}"
  hibernate.connection.username: "admin"
  hibernate.connection.password: ""
  hibernate.default_schema: "PUBLIC"
  hibernate.connection.pool_size: 5
  hibernate.dialect.H2Dialect: "org.hibernate.dialect.H2Dialect"
  hibernate.cache.provider_class: "org.hibernate.cache.internal.NoCacheProvider"
  hibernate.show_sql: "false"
  hibernate.hbm2ddl.auto: "update"
```

The list of configuration entries is variable, you may add or remove entries as necessary for your particular database.
Also, if `{appid}` is found in any of the values, it will be swapped at run time for the id of the instance of the application being run.

Further, to ensure the appropriate database vendors' dependencies are available when compiling, the `hcs-sxc-java-plugins-persistence-hibernate` project makes use of profiles in its `pom.xml`. 

For example:

```
   <profiles>
        <profile>
            <id>mysql</id>                
            <dependencies>
                <dependency>
                    <groupId>mysql</groupId>
                    <artifactId>mysql-connector-java</artifactId>
                    <version>5.1.9</version>
                </dependency>
            </dependencies>
        </profile>
        <profile>
            <id>postgres</id>
            <dependencies>
                <dependency>
                    <groupId>mysql</groupId>
                    <artifactId>mysql-connector-java</artifactId>
                    <version>5.1.9</version>
                </dependency>
            </dependencies>
        </profile>
        <profile>
            <id>h2</id>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
            <dependencies>
                <dependency>
                <groupId>com.h2database</groupId>
                <artifactId>h2</artifactId>
                <version>1.4.200</version>
                </dependency>
            </dependencies>
        </profile>
    </profiles>
```

to build this hibernate component with the appropriate vendor's dependencies, add `-P profileName` to your maven install command.

Example:
```
mvnw clean install -Pdocker -Ppostgres
```

the `h2` profile is the default profile

*Note: Repeated compilations with the `docker` profile may lead to a large number of images being created in the docker repository. Be sure to remove them from time to time `docker image prune -a`.*

#### hcs-sxc-java-plugins-persistence-in-memory

### hcs-sxc-java proto

Defines the protobuf messages used within `hcs-sxc-java-core`.

### Artemis Message Queue

This is not a java project, but a component which is started by way of a docker image if necessary.

## Choosing which plugins to use

To choose whether to use the `direct` or `hcs-sxc-java-relay+activeMQ` subscription method, include either the first or second dependency below in your application's `pom.xml`

for direct

```
<groupId>com.hedera</groupId>
<artifactId>hcs-sxc-java-plugins-mirror-direct</artifactId>
<version>0.0.3-SNAPSHOT</version>
```

for Artemis Message Queue

```
<groupId>com.hedera</groupId>
<artifactId>hcs-sxc-java-plugins-mirror-queue-artemis</artifactId>
<version>0.0.3-SNAPSHOT</version>
```

If you choose Artemis Message Queue, you must run a `hcs-sxc-java-relay` to ensure the queue is given messages to persist on behalf of `AppNet` participants.

To choose whether to use the `in memory` or `in database` persistence plug in, include either the first or second dependency below in your application's `pom.xml`

for in memory

```
<groupId>com.hedera</groupId>
<artifactId>hcs-sxc-java-plugins-persistence-in-memory</artifactId>
<version>0.0.3-SNAPSHOT</version>
```

for in database

```
<groupId>com.hedera</groupId>
<artifactId>hcs-sxc-java-plugins-persistence-hibernate</artifactId>
<version>0.0.3-SNAPSHOT</version>
```

*Note 1: Version numbers may change over time.*

*Note 2: Class loading should happen from the class path if the correct jar is found, it may therefore not be absolutely necessary to declare dependencies here since loading of a class matching the appropriate interface (`MirrorSubscriptionInterface` or `SxcPersistence`) will happen dynamically when the application starts.*

## Configuration files

A number of configuration files are necessary in order to provide the components the necessary information such as which TopicId(s) to use or subscribe to, etc... These files are listed and explained below.

Sample configuration files are located in the `./config` folder of each project where a configuration file may be necessary.

### Order of precedence

Some configuration file data may be overridden with environment variables and/or command line parameters. The location of a configuration file may also vary depending on use cases. The order of precedence is below for all components:

- command line parameters
- host environment variables
- environment variables found in `./config/.env` file
- `./config` folder for other configuration files

Component logs will generally indicate where the configuration was obtained.

### hcs-sxc-java-relay

The `relay-config.yaml` file contains the necessary configuration for the `hcs-sxc-java-relay` component.

```
# Address of the mirror node's subscription end point
mirrorAddress: "hcs.testnet.mirrornode.hedera.com:5600"

# The topic IDs to subscribe to
topics:
  - topic: "0.0.999"

# Should relay catch up with message history on startup
catchupHistory: true

# Keeps the consensus time of the last received message in this file
lastConsensusTimeFile: "./lastConsensusTime.txt"

# Connection details to the Artemis MQ component
queue:
  initialContextFactory: "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory"
  tcpConnectionFactory: "tcp://hcs-sxc-java-queue:61616?jms.redeliveryPolicy.initialRedeliveryDelay=0&jms.redeliveryPolicy.backOffMultiplier=1&jms.redeliveryPolicy.maximumRedeliveries=5&jms.redeliveryPolicy.redeliveryDelay=500&jms.redeliveryPolicy.useExponentialBackOff=false"
```

### hcs-sxc-java-plugins-mirror-queue-artemis

The `queue-config.yaml` file contains the necessary configuration for the `hcs-sxc-java-plugins-mirror-queue-artemis` component

```
queue:
  tcpConnectionFactory: "tcp://hcs-sxc-java-queue:61616?jms.redeliveryPolicy.initialRedeliveryDelay=0&jms.redeliveryPolicy.backOffMultiplier=1&jms.redeliveryPolicy.maximumRedeliveries=5&jms.redeliveryPolicy.redeliveryDelay=500&jms.redeliveryPolicy.useExponentialBackOff=false"
  initialContextFactory: "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory"
```

### Applications

Applications will vary in use cases, however the `hcs-sxc-java-core` expects the application to provide a number of configurable parameters, these are defined in the `config.yaml`.

Example `config.yaml`

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
    - topic: 0.0.999
  # Which level of persistence should be used
  persistenceLevel: "FULL"
  # Should history of messages be caught up
  catchupHistory: true

coreHibernate:
  # these values will be loaded as a map, add or remove properties as necessary
  # {appId} will be replaced with the application instance id
  hibernate.connection.driver_class: "org.h2.Driver"
  hibernate.connection.url: "jdbc:h2:./h2data/libdb-{appid}"
  hibernate.connection.username: "admin"
  hibernate.connection.password: ""
  hibernate.default_schema: "PUBLIC"
  hibernate.connection.pool_size: 5
  hibernate.dialect.H2Dialect: "org.hibernate.dialect.H2Dialect"
  hibernate.cache.provider_class: "org.hibernate.cache.internal.NoCacheProvider"
  hibernate.show_sql: "false"
  hibernate.hbm2ddl.auto: "update"

# Default HCS transaction fee in tinybar
HCSTransactionFee: 100000000

mirrorNode:
  # Address of the mirror node's subscription end point
  address: "hcs.testnet.mirrornode.hedera.com:5600"

# List of Hedera Nodes to send HCS messages to, if more than one is specified, the SDK will randomly choose a node with each transaction
nodes:
  - address: 0.testnet.hedera.com:50211
    account: 0.0.3
  - address: 1.testnet.hedera.com:50211
    account: 0.0.4
  - address: 2.testnet.hedera.com:50211
    account: 0.0.5
  - address: 3.testnet.hedera.com:50211
    account: 0.0.6
```

In addition to the `config.yaml` file, a `.env` file may be provided (or environment variables set) for the application to be able to submit transactions to Hedera. Again, a sample file is provided with the examples (`dotenv.sample`).

```
OPERATOR_KEY=
OPERATOR_ID=0.0.xxxx
# APP Net
APP_ID="Alice"
# Message encryption key (HEX)
ENCRYPTION_KEY=308204be02...
```

The `OPERATOR_KEY` is the HH private key of the account identified by `OPERATOR_ID`.
The `ENCRYPTION_KEY` is used only if message encryption is enabled in in `config.yaml`

*Note: When running in your java IDE or standalone in a command line, the host's environment variables take precedence over those in the `.env` file.*

## Docker

Docker is a convenient way of starting a number of individual components, we use it extensively in the `hcs-sxc-java-settlement-demo` example, but also to start up the `hcs-sxc-java-relay` and `Artemis MQ` components when necessary. Below is an example `docker-compose.yml` file for this purpose. You may wish to extend it with your own application-specific images if necessary.

```
version: '3.3'
services:
  hcs-sxc-java-queue:
    container_name: hcs-sxc-java-queue
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
      DISABLE_SECURITY: "true"
      ARTEMIS_USERNAME: hcsdemo
      ARTEMIS_PASSWORD: hcsdemo
      RESTORE_CONFIGURATION: "true"

  hcs-sxc-java-relay:
    container_name: hcs-sxc-java-relay
    depends_on:
      - hcs-sxc-java-queue
    image: hederahashgraph/hcs-sxc-java-relay:latest
    restart: on-failure
    networks:
      - backing-services
    volumes:
      - ./config:/config

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

*Note 2: If the `hcs-sxc-java-core` is setup to encrypt, sign, key-rotate (subject to availability), this will all happen automatically, the application developer need not worry about it*

### Susbcribing to a topic via `hcs-sxc-java-core`

```java
    public HCSIntegration() throws Exception {
        // create a callback object to receive the message
        OnHCSMessageCallback onHCSMessageCallback = new OnHCSMessageCallback(Statics.getAppData().getHCSCore());
        onHCSMessageCallback.addObserver(hcsMessage -> {
            processHCSMessage(hcsMessage);
        });
    }
```

### Handling a notification from `hcs-sxc-java-core`

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
- [Protobuf compiler](https://github.com/protocolbuffers/protobuf) version 3.11.2. (check with `protoc --version` in a command prompt).
- Docker and Docker-Compose

### Compilation steps

- Ensure the necessary configuration files are complete and accurate (use provided samples as starting points)
    - hcs-sxc-java-examples/hcs-sxc-java-settlement-demo/config/.env
    - hcs-sxc-java-examples/hcs-sxc-java-settlement-demo/config/.config.yaml
    - hcs-sxc-java-examples/hcs-sxc-java-settlement-demo/config/docker-compose.yml

    - hcs-sxc-java-examples/hcs-sxc-java-simple-message-demo/config/apps.yaml
    - hcs-sxc-java-examples/hcs-sxc-java-simple-message-demo/config/config.yaml

    - hcs-sxc-java-examples/hcs-sxc-java-simple-message-demo/docker-compose.yml (only if you want to try simple demo through relay and queue)
    - hcs-sxc-java-examples/hcs-sxc-java-simple-message-demo/config/queue-config.yaml (only if you want to try simple demo through relay and queue)
    - hcs-sxc-java-examples/hcs-sxc-java-simple-message-demo/config/relay-config.yaml (only if you want to try simple demo through relay and queue)

#### Compile docker images

From the top of the project, issue the following command to compile docker images

```shell
mvnw clean install -Pdocker
```

*Note: a `mvnw` executable is provided in the project in the event you don't have maven installed*

*Note: Repeated compilations with the `docker` profile may lead to a large number of images being created in the docker repository. Be sure to remove them from time to time `docker image prune -a`.

#### Compile "fat" jars

From the top of the project, issue the following command to create fat jars

```shell
mvnw clean install -Pfatjar
```

*Note: a `mvnw` executable is provided in the project in the event you don't have maven installed*

## Running the project in your IDE

You may need to setup environment variables to match those in the `.env` and `docker-compose.yml` files.

## Examples

The project comes with two examples to get you started, these are fully functional examples. The first `hcs-sxc-java-simple-message-demo` is a simple command line example where running two instances of the application side by side, you can witness that a message sent from one app is reflected in the other. The first app sends the message to Hedera and the second receives it via a subscription to a mirror node. The opposite also works. The second example `hcs-sxc-java-settlement-demo` is a more complex application which is based on spring boot with a web UI. Each instance of the application represents a participant in a settlement use case where participants can issue credit notes to each other, approve them, group them to reach a settlement amount, employ a third party to effect the payment and finally both original parties confirm the payment was completed. In addition to this, an audit log is provided so that the full history of messages between participants can be consulted.

### hcs-sxc-java-simple-message-demo

This is a simple messaging demo between three participants. All messages sent from one participant are sent to the Hedera HCS service and each participant subscribes to a mirror node to receive the consensus messages.

You can run the demo with or without encryption. If the demo is run without encryption, all communications are in clear text and all participants see the messages.
If the demo is run with encryption, then only the participants who share a key with the sender can read and decrypt the messages. In the samples provided, player 1 can communicate with players 2 and 3, but 2 and 3 don't communicate with each other.

To run the demo, first create a new HCS topic using the CreateTopic class in the examples and edit the `config.yaml` file to reflect the new topic id. This is to ensure that when you run the demo, you don't receive messages from someone else who you may be sharing a topic id with - although that could be fun.
Also check other details such as the mirror node, hedera network, etc... are correct.

#### without relay and queue

This is the simplest way to get the demo running.

ensure that the following files are present and configured properly, `apps.yaml` should just be a copy of `apps.yaml.sample`, no change required. `config.yaml` should be a copy of `config.yaml.sample` with the `topic` property set to your topic id.

  - hcs-sxc-java-examples/hcs-sxc-java-simple-message-demo/config/apps.yaml
  - hcs-sxc-java-examples/hcs-sxc-java-simple-message-demo/config/config.yaml
  - hcs-sxc-java-examples/hcs-sxc-java-simple-message-demo/config/.env (copy of `dotenv.sample` with your own account and private key)

Once you have these files, run `java -cp hcs-sxc-java-simple-message-demo-0.0.3-SNAPSHOT-shaded.jar com.hedera.hcsapp.GenerateConfigurationFiles`

This will take the operator key and id from the .env file and create corresponding environment files for each of the three players in the `config` folder (.envPlayer-0, .envPlayer-1 and .envPlayer-2).
It will also generate a `./config/contact-list.yaml` file but this is only necessary for encrypted demos.

* Without encryption

ensure `./config/config.yaml` has its `encryptMessages` property set to `false`.

Open up to three terminal windows and in each type:

  - terminal 1: linux/mac: `./runapp.sh Player-0`, windows: `runapp.cmd Player-0`
  - terminal 2: linux/mac: `./runapp.sh Player-1`, windows: `runapp.cmd Player-1`
  - terminal 3: linux/mac: `./runapp.sh Player-2`, windows: `runapp.cmd Player-2`

All terminals should now be waiting for input, after a few seconds, the message should be reflected in all three terminals.

```
****************************************
** Welcome to a simple HCS demo
** I am app: Player 1
****************************************
Input these commands to interact with the application:
new `thread_name` to create a new thread (note doesn't change current thread)
select `thread_name` to switch to `thread_name`
list to show a list of threads (current thread is highlighted in bold or **)
show to list all messages for the current thread
help to print this help
exit to quit

>
```

* With pair-wise encryption

ensure `./config/config.yaml` has its `encryptMessages` property set to `true`.

Open up to three terminal windows and in each type:

  - terminal 1: linux/mac: `./runapp.sh Player-0`, windows: `runapp.cmd Player-0`
  - terminal 2: linux/mac: `./runapp.sh Player-1`, windows: `runapp.cmd Player-1`
  - terminal 3: linux/mac: `./runapp.sh Player-2`, windows: `runapp.cmd Player-2`

All terminals should now be waiting for input, enter text in one terminal and press [return], after a few seconds, the message should be reflected in all or some of the terminals.

Messages sent by player 1 are echoed on Player 2 and 3, player 1 gets a copy of each message sent
Messages sent by players 2 and 3 are only echoed on player 1

```
****************************************
** Welcome to a simple HCS demo
** I am app: Player 1
****************************************
Input these commands to interact with the application:
new `thread_name` to create a new thread (note doesn't change current thread)
select `thread_name` to switch to `thread_name`
list to show a list of threads (current thread is highlighted in bold or **)
show to list all messages for the current thread
help to print this help
exit to quit

>
```

#### with relay and queue

This setup is slightly more complex and showcases the use of a relay to catch notifications from mirror which are then forwarded to a queue (Active MQ) for consumption by clients.

In addition to the files above (without relay and queue), ensure the following files are present and properly configured

  - hcs-sxc-java-examples/hcs-sxc-java-simple-message-demo/docker-compose.yml (no changes required here, just copy sample)
  - hcs-sxc-java-examples/hcs-sxc-java-simple-message-demo/config/queue-config.yaml (no changes required here, just copy sample)
  - hcs-sxc-java-examples/hcs-sxc-java-simple-message-demo/config/relay-config.yaml (update the topic id)

This demo uses the queue and relay components. For the apps to connect to the queue, an entry in your hosts file needs to be added as follows:

```text
127.0.0.1       hcs-sxc-java-queue
```

Compile the project 

`mvnw clean install -Pqueue` This will invoke the `queue` profile which switches dependencies to include the queue instead of mirror for subscriptions.

(see above) and open three console terminals and switch to the folder/directory containing the `hcs-sxc-java-simple-message-demo` example on your computer.

Open up to three terminal windows and in each type:

In the first, run the docker images for the queue and relay.

```shell
docker-compose up
```

once the components are up and running

```shell
hcs-sxc-java-relay_1  | 2020-01-07 13:07:22 [Thread-1] INFO  MirrorTopicSubscriber:131 - Sleeping 30s
hcs-sxc-java-relay_1  | 2020-01-07 13:07:52 [Thread-1] INFO  MirrorTopicSubscriber:131 - Sleeping 30s
```

Now refer to the `without relay and queue` instructions for running the player apps with or without encryption.

### hcs-sxc-java-settlement-demo

This is a more complex application which is based on spring boot with a web UI. Each instance of the application represents a participant in a settlement use case where participants can issue credit notes to each other, approve them, group them to reach a settlement amount, employ a third party to effect the payment and finally both original parties confirm the payment was completed. In addition to this, an audit log is provided so that the full history of messages between participants can be consulted.

To run the demo, first create a new HCS topic using the SDK `CreateTopic.java` and edit the `config.yaml` file to reflect the new topic id. This is to ensure that when you run the demo, you don't receive messages from someone else who you may be sharing a topic id with - although that could be fun.
Also check other details such as the mirror node, hedera network, etc... are correct.

In order to reduce the load on your computer, you may want to comment out some sections of the `docker-compose.yml` file too. Comment out the containers for Erica, Farouk, Grace and Henry, they're not strictly necessary to run the example.
The `docker-compose.yml` also specifies App environment specific settings such as color code of each participant's interface as well as  public and private signing-keys.

Also create a `.env` file with the following information

```
OPERATOR_KEY=
OPERATOR_ID=0.0.xxxx
# APP Net
APP_ID="Alice"
```


If you want to use encryption then enable the relevant setting in `config.yaml`. The demo uses pairwise encryption and only parties that share a key can see each other's messages. The file  `contact-list.yaml` contains a sample configuration to kick start things: it has a list of participants and all parties with whom each of them can communicate. You may notice that the file does not specify the public key that corresponds to the private signing key. The public key resides in the `docker-compose.yml` file. If you want to specify custom communication relationships between participants then edit  the `contact-list.template.yaml` file  (see instruction in the file) and run `GenerateConfigurationFiles.java` to automatically generate both a `docker-compose.yml` and a `contact-list.yaml` file with all keys set. 


This demo does not use the queue and relay components, although it's possible to enable them by modifying the `pom.xml` file of the `hcs-sxc-java-settlement-demo` project to include them, they will also need to run as docker containers.

Compile the project (see above) and open a console terminal and switch to the folder/directory containing the `hcs-sxc-java-settlement-demo` example on your computer.

Then switch to `./config` folder and run the docker images as follows

```shell
docker-compose up --remove-orphans
```

once the components are up and running (this may take a while), you can navigate to the UIs of the respective application users. Note: An instance of the `hcs-sxc-java-settlement-demo` application is run for each of the users and offered up on a separate http port.

You can see all the participants by navigating to one of the application's landing page

http://localhost:8081/landing.html

And from there, open a new page for each of the participants

* Alice http://localhost:8081
* Bob http://localhost:8082
* Carlos http://localhost:8083
* Diana http://localhost:8084
* Erica http://localhost:8085 (if enabled in `docker-compose.yml`)
* Farouk http://localhost:8086 (if enabled in `docker-compose.yml`)
* Grace http://localhost:8087 (if enabled in `docker-compose.yml`)
* Henry http://localhost:8088 (if enabled in `docker-compose.yml`)

Whenever a participant performs and action in the UI, this results in a HCS transaction containing an `application-message` which itself contains a `business-message` containing the user's intent. Once the transaction has reached consensus, it's broadcast to all participants since they all subscribe to the same topic on a mirror node.

#### Without docker (useful when debugging)

To run the examples outside of docker and override `.env` variables run:

```
mvnw exec:java -Dexec.mainClass="com.hedera.hcsapp.Application"  -Pfatjar  -DAPP_ID="Alice" -DOPERATOR_ID=0.0.1010 -DOPERATOR_KEY=302e0208...94329fb
```
If you want to run multiple clients from the command line simultaneously then make sure the server ports are not occupied.

Note that the  `docker-compose.yaml` file is consulted even when running from then command line. If you specify the `-DAPP_ID`  argument then the port mapping is selected from the `yaml` file. 
You can override the port by setting:

```-Dserver.port=8081```

The demo provides helper functions to delete save and restore the local demo database however, these have undefined behavior when `hcs-sxc-plugins-persistence-in-memory` is chosen.

You can also specify these `-D` input values in your IDE so that you can run several instances of the application in the IDE, this can help when debugging. To enable the debugger use the `-Djpda.listen=maven` flag.

## Contributing

Contributions are welcome. Please see the [contributing](CONTRIBUTING.md) guide to see how you can get
involved.

## Code of Conduct

This project is governed by the [Contributor Covenant Code of Conduct](CODE_OF_CONDUCT.md). By participating, you are
expected to uphold this code of conduct. Please report unacceptable behavior to [oss@hedera.com](mailto:oss@hedera.com)

## License

[Apache License 2.0](LICENSE)
