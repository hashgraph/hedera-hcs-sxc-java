





[![CircleCI](https://circleci.com/gh/hashgraph/hedera-hcs-sxc-java.svg?style=svg&circle-token=4f151711fb43e44d3d469cc1fbeaa17de4ab0c23)](https://circleci.com/gh/hashgraph/hedera-hcs-sxc)
[![codecov](https://codecov.io/gh/hashgraph/hedera-hcs-sxc-java/branch/master/graph/badge.svg)](https://codecov.io/gh/hashgraph/hedera-hcs-sxc-java)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](https://opensource.org/licenses/Apache-2.0)

# HCS-SXC-Java

The HCS SXC Java project (SDK eXtension Components) is a set of pre-built components that aim to provide *additional functionality* over and above the java SDK for HCS to make it easier and quicker to develop applications.

### Terms and conditions

Please review the [Hedera Terms and Conditions](https://www.hedera.com/terms) prior to using this software.

This repository is only intended for demo purposes and not production applications.

### Feature overview

SXC components use the Hedera Java SDK to communicate with Hedera's HCS service and add a number of features (Italicised still in development/planning) as follows:

- Sending messages to Hedera's HCS service
    - With optional message encryption (subject to plug in development)
    - With optional key rotation and key exchange over non-trusted communication channel (subject to plug in development)
    - *With message signature*
    - Optionally across multiple topics
- Chunking and re-assembly of large messages (Hedera transactions are limited to 4k)
- Pairwise symmetric key encryption between participants
- Key rotation with secure key exchange. 
- Proof after the fact: allow third a third party to verify an encrypted message without sharing encryption keys
- Protobuf application message structure ([see all protobuf definitions](.\hcs-sxc-java-proto\src\main\proto\Messages.proto))
- Persistence of transactions sent and messages sent/received
    - In memory or in database via plugins and JPA compatible. All messages are automatically persisted and made available to the application through an interface such that it can perform audit for example.
- Mirror node topic subscription
    - Via relay
    - Direct to mirror node
- Plugin based architecture ([read more about plugins here](.\hcs-sxc-java-plugins\README.md))
    - hcs-sxc-java-plugins-encryption-diffie-hellman - plugin to encrypt messages and manage key rotation using Diffie Hellman
    - hcs-sxc-java-plugins-mirror-direct - plugin to enable the `hcs-sxc-java-core` to subscribe to mirror notifications directly
    - hcs-sxc-java-plugins-mirror-queue-artemis - plugin to enable the `hcs-sxc-java-core` to subscribe to mirror notifications via an Artemis Message Queue (which receives messages via the `hcs-sxc-java-relay` component)
    - hcs-sxc-java-plugins-persistence-hibernate - plug in to provide data persistence in a database through hibernate. Supports `h2` with no separate database server installation required and has preconfigured `mysql` and `postgres` profiles where a separate database installation is necessary
    - hcs-sxc-java-plugins-persistence-in-memory - plug in to provide data persistence in memory

**Advanced Demos** There are several demo applications included in the [examples folder](./hcs-sxc-java-examples) of this repository. Note that these are child projects and are built automatically when building the HCS-SXC. 

- [Messaging between participants](./hcs-sxc-java-examples/hcs-sxc-java-simple-message-demo)  
  - Java console application
  - Maintaining simple state 
  - Requesting proof after the fact
  - Address book management and pair-wise encryption. 
- [Financial Settlement](./hcs-sxc-java-examples/hcs-sxc-java-settlement-demo) 
  - Advanced UX showcase with reactive web interface
  - Multiple user roles with complex state machine management
  - Building HCS app nets with  REST and WebSocket endpoints
- [Listening Message Queues with logging and  timestamping](./hcs-sxc-java-examples/hcs-sxc-java-mq-consumer)
  - Support for RabbitMQ
  - MQ Instant messenger demo with UI
- [Token demo](./hcs-sxc-java-examples/hcs-sxc-java-token-demo)

## Compiling the project

The project consists of several components each of which is a separate maven project with its own `pom.xml` that you can compile individually.   [Read a brief description of all components](./README-COMPONENTS.md). 

We have bundled all components in a parent `pom.xml` which you can find a the project root folder. Compiling the parent project will **compile all sub-modules** including the [bundled examples](./hcs-sxc-java-examples). For the examples to work you need to make sure that [runtime configuration files](#runtime-configuration-files) of each example are complete and accurate. Configuration files are consulted only at runtime and are available in each example project.

*Note: The project uses [lombok](https://projectlombok.org/) which is "a java library that automatically plugs into your editor and build tools, spicing up your java". Some IDEs require that a plug in is installed in order for lombok to work.*

###### Pre-requisites

- The project is built on java 10.

- [Protobuf compiler](https://github.com/protocolbuffers/protobuf) version 3.11.2. (check with `protoc --version` in a command prompt).

- Some examples require Docker and Docker-Compose

  

At the root of the project issue

```shell
mvnw clean install 
```

If you want to omit compiling the provided examples you can **exclude** them from your build:

```
mvnw -pl -hcs-sxc-java-examples install
```

This will install all modules into your maven repository and you will be able to include the modules as dependencies in your projects. 

###### Compile docker images

If you want to run several apps from within the same machine then you can compile a docker image; from the top of the project, issue the following command to compile docker images

```shell
mvnw clean install -Pdocker
```

*Note: a `mvnw` executable is provided in the project in the event you don't have maven installed*

*Note: Repeated compilations with the `docker` profile may lead to a large number of images being created in the docker repository. Be sure to remove them from time to time `docker image prune -a`.

###### Compile "fat" jars

From the top of the project, issue the following command to create fat jars

```shell
mvnw clean install -Pfatjar
```

*Note: a `mvnw` executable is provided in the project in the event you don't have maven installed* 

## The anatomy of an app 

An HCS SXC **app participant** is part of an **AppNet**, a network of applications, which share the same code. They communicate messages with each other via the Hedera Consensus Service rather than in a peer to peer or other fashion.

An app participant is a Java program that sends, receives, and processes messages. An app loads the HCS SXC Core and passes to it

- APP_ID:  A name that identifies the app in the network
- OPERATOR_KEY: A Hedera Hashgraph *private key* with sufficient balance to sign and submit Hedera Hashgraph transactions. 
- SIGNING_KEY: Optional, an independently generated *private key* to sign Business Process Messages. Receiving parties are assumed to possess the corresponding public key to identify originators of messages.  
-  ADDRESS LIST: Optional, a *buddy list* where each item is a known app participant (APP_ID) along with his public signing key and a shared symmetric private key 
- Configuration parameters that define the TOPIC_ID, node addresses, mirror addresses, how to handle encryption and key rotation and setting up the Java Persistence API parameters for storing messages in an apps internal database. 

An **Application Message** is a message packet sent between AppNet participants using a standard envelope (the message itself may be broken up into several HCS transactions if too large). 

When an participant sends a message then <u>all other participants receive it;</u> SXC uses singing and encryption to *restrict communication* to a subset of participants whenever required.  

Recipients of Application Messages interpret and act upon the contents of the  payload which we call a **Business Process Message**  (BPM)- an AppNet specific message which is sent to other participants inside an Application Message.

On a high level,  the relationship between these two is laid out below. This is a simplified view of the  [detailed protobuf definitions](./hcs-sxc-java-proto/src/main/proto/Messages.proto)

    ApplicationMessage :=  
    	ApplicationMessageId , 
        BusinessProcessMessage ,
        UnencryptedBusinessProcessMessageHash ,  
        BusinessProcessSignatureOnHash ,
        EncryptionRandom
        
    BusinessProcessMessage := AppNetMessageAsBytes | ReservedInstruction
    
    ReservedInstruction := KeyRotationInit | KeyRotationRespond | RequestProof | ConfirmProof

When a BPM is wrapped into an Application Message then the hash of that cleartext BPM is calculated and placed in the relevant field even if the BPM is setup to be sent encrypted. Subsequently,  that hash is signed (when signing is enable) with the operator's *signing key*. Receiving parties are then able to identify whether a message belongs to a *known party* and if so attempt to decrypt it.  This is essentially the mechanism that allows secure  and private communication between designated parties on a non peer to peer network.

The BMP can be an arbitrary byte array or a *reserved instruction*. A reserved instruction is placed in an Application Message by the  SXC core in response to API calls such as requesting validation of a message or in response to conditions that trigger key exchange and rotation.   The user generated BPM can be a plain text message that will be timestamped by the network or could be application code, or op-codes, which is likely to be akin to a state machine which responds to user inputs and generates HCS transactions as a result. 

All things being equal and making sure applications do not behave in an unpredictable way (using random number generators or external data sources independently for example), the state of all applications running in the app-net should be the same at a given HCS message sequence number.

*Note: it is perfectly reasonable for an app to generate a random number and communicate this number to others via a HCS message, or fetch some data from an internet service and share it with others, but it would not be appropriate for each instance of an app to generate its own random number upon receiving a HCS message.

**On keys used in SXC:** Cryptographic signing-keys and encryption-keys are used at various levels throughout HCS SXC. At the lowest level we have a HCS message, which is constructed by the current Java SDK: as any other Hedera Hashgraph message, these messages are signed with private ed25519 keys. In HCS SXC, we call that key an OPERATOR_KEY and is accompanied by the OPERATOR_ID which is a Hedera account number  - you may notice that the library doesn't ask you to specify the public key counterpart because the Hedera network's address-book has a record of the public key and thus can verify signatures for messages it receives. HCS-SCX messages on the other hand are higher level messages where each such message is broken up into smaller chunks which are then sent to the network as low level Hedera Hashgraph HCS messages. 

Application messages can be signed and encrypted. HCS-SCX also uses ed25519 keys to sign such messages. Such signing-keys are private keys and the library provides various methods to set them up: the simplest setup is via an .env file where this private ed25519 key is identified as a SIGNING_KEY.

When an application message is encrypted then it is the `BusinessProcessMessage` field that is encrypted. The `UnencryptedBusinessProcessMessageHash` is used to store the hash of the cleartext message, before encryption was applied. This way, it is possible to verify integrity of messages after decrypting. That hash can further be signed using a private singing key and the signature is then stored in `BusinessProcessSignatureOnHash`.  This mechanism allows the core component to determine origin, without passing recipient information into the envelope, but also apply proof after the fact where a participant can verify a cleartext message against an encrypted application message without possessing the encryption/decryption key. 

## Hello App Participant - your first HCS SXC project

The central piece of HCS-SXC is an `HCSCore` object  which requires a number of parameters to initialise; these parameters can be issued  to `HCSCore` directly or via configuration files or issued via environment variables and command line parameters.  The order of precedence is below for all components:

- command line parameters
- host environment variables
- environment variables found in `./config/.env` file
- `./config` folder for other configuration files

All variables as well as the location of the configuration files can be overridden with API calls in the core component.

**To create your own project** compile the the entire project and omit the examples with `mvnw -pl -hcs-sxc-java-examples install`. This will install the maven artefact to your mvn library.   Then create a new maven project with your `pom.xml` at the root of the project  and create a folder `.config`  and  also place it at the root . Place in the configuration  folder a `.env` file and a `config.yaml` file.

**`.\.config\.env`** 

``` 
OPERATOR_ID=0.0.xxxx          # Hedera Hashgraph payer accound id
OPERATOR_KEY= 302...          # Hedera Hashgraph private payer key
```

The `OPERATOR_KEY` is the HH private key of the account identified by `OPERATOR_ID`.
The `ENCRYPTION_KEY` is used only if message encryption is enabled in in `config.yaml`

**`.\.config\.config.yaml`**  The sample configuration file defines an app participant on the test-net.  Make sure to  update the `topic` id to a topic that you can submit to. Note, in this example we will use a in-memory persistence plugin - if you want to use the supplied database persistence plugin then you must add a `coreHibernate` section as shown in the [persistence plugin documentation](.\hcs-sxc-java-plugins\README.md) and modify your pom.xml  to select the plugin. 

```yaml
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

**`.\pom.xml`**  The library is plugin based  and plugins are selected in the `pom.xml` file. Here we demonstrate a minimal pom.xml where we select an `in-memory-database`  a `direct-mirror-subscription` and will omit encryption.  Consult the [plugin documentation](.\hcs-sxc-java-plugins\README.md) on how to select available plugins.  

Minimal POM

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.hedera</groupId>
    <artifactId>basix-sxc-example</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>jar</packaging>
    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <maven.compiler.source>10</maven.compiler.source>
        <maven.compiler.target>10</maven.compiler.target>
        <hcs-sxc.version>0.0.3-SNAPSHOT</hcs-sxc.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>com.hedera</groupId>
            <artifactId>hcs-sxc-java-core</artifactId>
            <version>${hcs-sxc.version}</version>
        </dependency>
        <!-- PLUGINS -->
        <dependency>
            <groupId>com.hedera</groupId>
            <artifactId>hcs-sxc-java-plugins-mirror-direct</artifactId>
            <version>${hcs-sxc.version}</version>
        </dependency>
        <dependency>
            <groupId>com.hedera</groupId>
            <artifactId>hcs-sxc-java-plugins-persistence-in-memory</artifactId>
            <version>${hcs-sxc.version}</version>
        </dependency>
    </dependencies>
</project>
```

Create a java class with a main method under a package of your choice, eg. `com.mycompany.basic.sxc.example.App`. You can build your project and run the main method  using maven 

on windows

`mvn.cmd exec:java -D"exec.mainClass"="com.mycompany.basic.sxc.example.App"`

and on linux

`mvn.cmd exec:java -Dexec.mainClass="com.mycompany.basic.sxc.example.App"`

#### Initialising the core component

In your main method load the core component with configuration by issuing

``` java
HCSCore hcsCore = new HCSCore().builder(
    "app-0",    // the app name
    "./config/config.yaml", 
    "./config/.env"
);
```

#### Sending a HCS message via the core component

The most basic option for sending messages with HCS SXC Core is to send a non-encrypted message. This however will mean anyone with access to a mirror node will be able to read the messages that are being exchanged between app net participants on a given topic id.

```java
new OutboundHCSMessage(hcsCore)
    .sendMessage(
       0  // select the first topic, from list of topics defined in config.yaml
       , "myMessage".getBytes()
);
```

*Note 1: `myMessage` may be larger than 4k, in which case the hcs sxc core will take care of breaking it up into multiple transactions, and recombining the contents of each transaction post-consensus to rebuild the message.*

*Note 2: If the `hcs-sxc-java-core` is setup to encrypt, sign, key-rotate (subject to availability), this will all happen automatically, the application developer need not worry about it*

#### Subscribing to a topic 

To setup call back in the need to add observers and parse the feedback.  

```java
 // create a callback object to receive the message
OnHCSMessageCallback onHCSMessageCallback = new OnHCSMessageCallback(hcsCore);
onHCSMessageCallback.addObserver(
    (SxcConsensusMessage sxcConsensusMessage, HCSResponse hcsResponse) 
    ->
    {
        // handle notification in mirrorNotification
        mirrorNotification(sxcConsensusMessage, hcsResponse, hcsCore);
    }
);
```

#### Handling the call-back 

To receive the message that was sent

```java
void mirrorNotification(
    SxcConsensusMessage sxcConsensusMessage, 
    HCSResponse hcsResponse, 
    HCSCore hcsCore) {
        // get the message
    	byte[] message = hcsResponse.getMessage(); // the message that was sent
    	...
        ...
}
```

> NOTE:  The call back is **asynchronous**; to prevent your main method thread from terminating prematurely you can ask fort user input at the end of the method, otherwise the tread will terminate before the call-back is handled. 

Sometimes you need to bring low level consensus information to the app level. The core stores all information to the persistence layer which you can access by supplying the application message id.	

```java
void mirrorNotification(
    SxcConsensusMessage sxcConsensusMessage, 
    HCSResponse hcsResponse, 
    HCSCore hcsCore) {
    
    // get the message from persistence
    SxcApplicationMessageInterface applicationMessageEntity =
        hcsCore
        .getPersistence()
        .getApplicationMessageEntity(
        	SxcPersistence.extractApplicationMessageStringId(
            	hcsResponse.getApplicationMessageId()
        	)
    	);
    ...
    ...
    // access application message consensus details
        
    applicationMessageEntity.getLastChronoPartSequenceNum();
    ...
    applicationMessageEntity.getLastChronoPartRunningHashHEX();
    ...
    applicationMessageEntity.getLastChronoPartConsensusTimestamp();

}
```
Note tat an application message can exceed the HCS transaction limit and hence will be chunked into several transactions each of which will have their own timestamp. When the core reassembles messages, it applies an overall timestamp and consensus information to the combined application message; the convention is to use consensus information of the the last part received. 

When you send a message then you expect all other participants to receive it but you should also receive your own message back.

#### Sending and receiving encrypted messages

> WARNING This repository is only intended for demo purposes and not production applications.

In order to provide  privacy, the HCS SXC Core component  implements plugins for message encryption. The encryption scheme that HCS SCX uses is plugin based and users can define their own by implementing the `SxcMessageEncryption` interface. Your own  plugins may be used to provide different encryption methodologies.

The example cryptography plugin implements `AES/GCM/NoPadding` symmetric key encryption and uses 32 byte private keys. 

To enable encryption amend `config.yaml` such that 

```yaml
appNet:
  ...
  # Should messages be encrypted
  encryptMessages: true
  ...
```

and also add the supplied plugin to your `pom.xml`

```xml
 ...
 <!-- PLUGINS -->
 <dependency>
 	...
 </dependency>
 <dependency>
     <groupId>com.hedera</groupId>
     <artifactId>hcs-sxc-java-plugins-encryption-diffie-hellman
     </artifactId>
     <version>${hcs-sxc.version}</version>
</dependency>
```

There are two ways to communicate with encryption enabled. 

- **Universal encryption:** All app participants share the same shared symmetric encryption key
- **Pairwise encryption**: Each app participant shares a unique  symmetric encryption key with every participant he communicates with.  Hey may not share keys with some participants in which case he cannot decrypt their messages. 

To use **universal encryption** add an encryption key to the `.env` file of each participant.

```
OPERATOR_ID=0.0.xxxx         # Hedera Hashgraph payer accound id
OPERATOR_KEY=302...          # Hedera Hashgraph private payer key
# Message encryption key (HEX)
ENCRYPTION_KEY=424..         #shared key used for encrypting a BMP
```

You send messages in the usual way and encryption / decryption is handled in the background. One way to ensure that messages have been encrypted indeed is to check the output in a mirror explorer.  

To use **pairwise encryption** you and all other participants will need unique private signing key.  HCS-SCX signing keys are used to identify message-origin and this is done in conjunction with an address-book that lists all ed25519 public keys of participants an APP is communicating with. Such public ed25519 keys are identified as `theirPublicSigningKey`.  It is assumed that each participant has a unique signing key and it is not shared across participants. In this encryption scheme,  pairs of communicating entities share a common encryption key.  Amend the `.env` file to include

```
APP_ID=Player-0				 # App ID defined at env level
OPERATOR_ID=0.0.xxxx         # Hedera Hashgraph payer accound id
OPERATOR_KEY=302...          # Hedera Hashgraph private payer key
# Message signing key (HEX)
SIGNING_KEY=302..    		 # private signing key. the public key part 
							 # resides in an address book of a communicating
							 # pair.
```

You may notice that `.env` files don't specify encryption keys anymore and this is because an App needs a different encryption key for each other entity it communicates with. For each communicating pair you need to know the pairs 

- application id
- `sharedSymmetricEncryptionKey` 
- `theirPublicSigningKey`

In an app net with three apps you may have the following communicating pairs where app with id Player-0 communicates with Player 1 and Player 2

```yaml
Player-0 :
  Player-1 :
    sharedSymmetricEncryptionKey: 817c2d3fc ...
    theirEd25519PubKeyForSigning: 302a30050 ... f34396
  Player-2 :
    sharedSymmetricEncryptionKey: c41569190 ...
    theirEd25519PubKeyForSigning: 302a30050 ... 4f633a

```

Player 1 can talk to Player 0 only

```yaml
Player-1 :
  Player-0 :
    sharedSymmetricEncryptionKey: 817c2d3fc ...
    theirEd25519PubKeyForSigning: 302a30050 ... f15227
```

player 2 can talk to Player 0 only

```yaml
Player-2 :
  Player-0 :
    sharedSymmetricEncryptionKey: c4156919e ...  
    theirEd25519PubKeyForSigning: 302a30050 ... f15227

```

It is worth studying this example to observe how keys relate and what keys are shared. Each participant needs to supply his communicating parties to his own `hcsCore` component. Player 0 from this example would need to supply two entries, one for Player 1 and one for Player 2.

```java
Map<String,<Map<String,String>>>addressList = ...  // load Player 0 firends into a map
 
addresslist.forEach((k,v)->{
            hcsCore.addOrUpdateAppParticipant(
                k,  // app id
                v.get("theirEd25519PubKeyForSigning"), // friend public sign key
                v.get("sharedSymmetricEncryptionKey"), // friend shared key
                v.get("sharedSymmetricEncryptionKey")  // copy needed for rotation
            );
        });
    }
```

You then send messages as usual and the core will take care of testing for signatures and  decrypting transparently in the background.  Specifically, the core will test first if an incoming messages is from a friends and will discard if otherwise. If the message is from a known party then decryption. Decryption may still fail if the message was not intended for that app participant (which can happen, even if the sender is a know entity).

When Player 0 sends a message then all participants in his list will receive a message encrypted with their shared key. That is, Player 0 has to **send twice**; this is automatically handled by the core component. There are situations where  we wish to encrypt only with one participant. In that case the API provides an override to restrict encryption to a single or a list of participants. For example:

```java
new OutboundHCSMessage(hcsCore)
    .restrictTo("Player-1")  // encrypt only with Player-1's shared key
    .sendMessage(0  , "myMessage".getBytes());
```

In this situation the message is sent **only once**. While Player 2 will still receive a message from the mirror subscription, it will not be possible to decrypt it even though Player 2 shares keys with Player 0. 

You can manually test origin of a message in the message call back handler. To test if a message is an echo message use 

```java
boolean isEcho = Signing.verify(
       appMessage.getUnencryptedBusinessProcessMessageHash().toByteArray()
     , appMessage.getBusinessProcessSignatureOnHash().toByteArray()
     , hcsCore.getMessageSigningKey().publicKey // test against own public key
));
```

#### Proof after the fact and verifying messages

A participant can verify a message originator and timestamp of an encrypted and timestamped message by issuing a verification request to a third party. 

```java
new OutboundHCSMessage(hcsCore)
    .restrictTo("Player-2")
    .requestProof(
    	0,  // topic Index
    	applicationMessageId, // the id of the message to verify  		
    	cleartextBusinessProcessMessage,  // the orignal message
    	publicSigningKeyOfPlayer1 // who is the signer of the original message
	);
```

with

`applicationMessageId` - The application message to be  validated needs to reside in own database either in encrypted or   decrypted form.
`cleartext` - The decrypted cleartext or  business process message
`publicSigningKeyOfPlayer1` - The key of the   signer of the message being validated    

This will send a special message to Player-2 using a reserved instruction in the BPM where Player-2's  core will interpret it automatically and respond back to Player 0 with the result of the verification.  The latter will receive another reserved instruction in the call-back function defined earlier. These instructions are `protobuf` messages and can be parsed as follows: 

```java
Any any = Any.parseFrom(bpmFromCallback);
if (any.is(RequestProof.class)){
	...
} else if (any.is(ConfirmProof.class)){
    ConfirmProof cf = any.unpack(ConfirmProof.class);

    cf.getProofList().forEach(
        verifiedMessage ->{
            System.out.printf("Message verification result: %s \n",
                          verifiedMessage.getVerificationOutcome().name()
         );
    });
} 

```

#### Key rotation

Further, an encrypted message is only truly safe if the key used to decrypt it isn't known to the public, but only to the intended recipient of the message. One solution is to implement key rotation, whereby the keys used to encrypt and decrypt messages are rotated more or less frequently. Assuming rotated keys are discarded, it should not be possible to subsequently decrypt messages. To enable rotation set the necessary parameters in `config.yaml`

```yaml
appNet:
  ...
  # Should messages be encrypted
  encryptMessages: true
  # Should messages be rotated
  rotateKeys: true
  # How often (messages) should keys be rotated
  rotateKeyFrequency: 2
```

Note that encryption must be enabled for rotation to work. 

Encryption can  either be static where the same key is used throughout the life time of a communicating pair or rotating. The encryption plugin loaded earlier implements Diffie Hellmann key exchange (DHKE). Notice that when key rotation is enabled then the initial secret generated is replaced by the shared secret that is generated by the key rotation implementation; however,  encryption remains to be AES. Thus, the  symmetric key is used until rotation is triggered. This is permissible because the AES standard does not specify a structure on the key used, but it is important too ensure that the size of the key generated they key rotation scheme is the same as the size required by the encryption plugin.

Key rotation is a background process and is only enabled when pairwise encryption is used and not universal encryption.  The special instruction in the BPM can be parsed in the call-back as follows.

```java
Any any = Any.parseFrom(bpmFromCallback);
if (any.is(KeyRotationInitialise.class)){
...
} else if (any.is(KeyRotationRespond.class)){
...
} 
...
```

There is very little reason to introspect rotation messages other than for debugging purposes. 



## Contributing

Contributions are welcome. Please see the [contributing](CONTRIBUTING.md) guide to see how you can get
involved.

## Code of Conduct

This project is governed by the [Contributor Covenant Code of Conduct](CODE_OF_CONDUCT.md). By participating, you are
expected to uphold this code of conduct. Please report unacceptable behavior to [oss@hedera.com](mailto:oss@hedera.com)

## License

[Apache License 2.0](LICENSE)
