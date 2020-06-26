# hcs-sxc-java-simple-message-demo

This is an  messaging demo between three participants, with app-id`Player-0, Player-1, and Player-2` that demonstrates

+ communication between multiple participants
+ with and without encryption 
+ with and without key rotation
+ simple cross-app state management
+ verification of an encrypted message without disseminating encryption keys. 

All messages sent from one participant are sent to the Hedera HCS service and each participant subscribes to a mirror node to receive the consensus messages. This demo implements concepts described in the [main tutorial](../../)

## Pre-requisites

- maven. A  maven executable is in included with the project if you don't have it installed
- Java 10
- docker and docker-compose installed (optional)

The simple message demo is a submodule - we assume the root project has been built with 

```
mvn -pl -hcs-sxc-java-examples install
```

which builds plugins and the core components used in the examples.

## Setting up configuration files

Rename `./config/donenv.sample` to `./config/.env` and fill out the details. You will need to do the same with `./config/config.yaml.sample ` and rename to  `./config/config.yaml`. If you don't have a `topic-id` yet then you can create one by running 

on windows

```
mvn.cmd exec:java -D"exec.mainClass"="com.hedera.hcsapp.CreateTopic"
```

and on Linux

```
mvn exec:java -Dexec.mainClass="com.hedera.hcsapp.CreateTopic"
```

The new topic number will appear in your console output; edit  `config.yaml`  to reflect the new topic id. This is to ensure that when you run the demo, you don't receive messages from someone else who you may be sharing a topic id with - although that could be fun. Also check other details such as the mirror node, hedera network, etc... are correct.

We will use the files you just created as templates to generate `.env` files for each Player. To do so run

windows

```bash
 mvn.cmd exec:java -D"exec.mainClass"="com.hedera.hcsapp.GenerateConfigurationFiles"
```

Linux

```powershell
mvn exec:java -Dexec.mainClass="com.hedera.hcsapp.GenerateConfigurationFiles"
```

This will take the operator key and id from the `.env` file and create corresponding environment files for each of the three players in the `config` folder (`.envPlayer-0,` `.envPlayer-1` and `.envPlayer-2`).

If you open any of the generated `envPlayer-N` files you will notice that a unique  `SIGNING_KEY` has been appended to each new .envs. This is a unique private key - it's corresponding public key will be held in the address book of the other participants.  

You will also notice that  `./config/contact-list.yaml`  is generated and this file will be consulted by each player to load into his own buddy list, that is, public signing keys of other players and players with which keys are shared for pairwise encryption.  This is a single file but each participant will consult the part that is relevant to him/her. 

## Running the programs

You can run the demo with or without encryption. If the demo is run without encryption, all communications are in clear text and all participants see the messages. If the demo is run with encryption, then only the participants who share a key with the sender can read and decrypt the messages. In the samples generated in  `./config/contact-list.yaml` , player 0 can communicate with players 1 and 2, but Player 1 and 2 don't communicate with each other.

### Messaging without encryption and maintaining state

Ensure `./config/config.yaml` has its `encryptMessages` property set to `false` and the signing property is set to `true`.  Compile the project with `mvn clean install`.

Open up to three terminal windows and in each type:

  - terminal 1: linux/mac: `./runapp.sh Player-0`, windows: `runapp.cmd Player-0`
  - terminal 2: linux/mac: `./runapp.sh Player-1`, windows: `runapp.cmd Player-1`
  - terminal 3: linux/mac: `./runapp.sh Player-2`, windows: `runapp.cmd Player-2`

All terminals should now be waiting for input, after a few seconds, the message should be reflected in all three terminals.

```
****************************************
** Welcome to a simple HCS demo
** I am app: Player-0
** My private signing key is: 302e...6fdc
** My public signing key is:302a...5227
** My buddies are: 
    Player-1={
        sharedSymmetricEncryptionKey=817c...2449,
        theirEd25519PubKeyForSigning=302a...4396
    },
    Player-2={
        sharedSymmetricEncryptionKey=c415...a91a,
        theirEd25519PubKeyForSigning=302a...633a
    }
****************************************
Input these commands to interact with the application:
new thread_name to create a new thread (note doesn't change current thread)
select thread_name to create a new thread (note doesn't change current thread)
list to show a list of threads
show to list all messages for the current thread
prove  app_id applicationMessage_id  public_key  to prove message after the fact; you can generate a message first and copy its resulting applicationMessage_id.
help to print this help
exit to quit


>
```

To start sending messages  that all other players will receive you must create a new *conversation thread*. Type `new myThread`  in any of the three participants and wait for a confirmation message to appear.  You can observe that the thread creation request has been propagated to all participants. This means, that adll **participants share a common state**. You can go to any participant an type `select myThread`  and then type any message to send it across the appnet.  You should expect to see verbose output in all terminals. 

Note, the message being sent with the Business Process Message (see main documentation) is not a simple cleartext message but rather an protobuf message. 

### Messaging with pairwise encryption

Ensure `./config/config.yaml` has its `encryptMessages` property set to `true`. Open up to three terminal windows as was done in the unencrypted case. If you observe the buddy list in the welcome screen of each player then you can see that messages from  Player 0 are expected to be decrypted and picket up by Player 1's and 2's terminal.  You can also see the same information in the generated `contact-list.yaml` 

Player-0 communicates with Player 1 and Player 2

```yaml
Player-0 :
  Player-1 :
    sharedSymmetricEncryptionKey: 817c2d3fc ...
    theirEd25519PubKeyForSigning: 302a30050 ... 4396
  Player-2 :
    sharedSymmetricEncryptionKey: c41569190 ...
    theirEd25519PubKeyForSigning: 302a30050 ... 633a

```

Player 1 can talk to Player 0 only

```yaml
Player-1 :
  Player-0 :
    sharedSymmetricEncryptionKey: 817c2d3fc ...
    theirEd25519PubKeyForSigning: 302a30050 ... 5227
```

player 2 can talk to Player 0 only

```yaml
Player-2 :
  Player-0 :
    sharedSymmetricEncryptionKey: c4156919e ...  
    theirEd25519PubKeyForSigning: 302a30050 ... 5227

```

When a message is received it is displayed on the same output and extra details are shown such as consensus information and whether the message was from one self. For instance, when player 0 creates a new thread then **two messages are sent,** one to player 1 and one to player 2.  Player 0 **receives his own message back twice**; such a message would be marked as a self message or echo:

```
    applicationMessageId: 0.0.95518-1585056287-851629200
    last chrono chunk consensus sequenceNum: 614
    last chrono chunk consensus running hash: 565b24...b298
    ApplicationMessage:
        Id: 0.0.95518-1585056287-851629200
        Hash of unencrypted message: 4382f8...efb6
        Signature on hash above: 085f8...be06
        Encryption random: 9791722faf59ca86cc0226f5e7179fea
        Is this a self message?: true
```

### Verifying a message

To test **proof after the fact** you can send a single message where the shared key of Player-0 + Player-1 is used. That is, the demo will send only one message as it wont create a message with Player 2's shared key.

From` Player-0`'s terminal type:

```
send-restricted Player-1 HelloFuture
```

Spaces aren't allowed in these messages; wait until a single echo message arrives then <u>copy the generated application message id</u> in your clipboard.  Observe Player-1's terminal to check if the message arrived.

This message exchange is between Player-0 and Player-1, thus <u>Player-2 has no way of decrypting</u> the contents; however, <u>Player-2 has a copy of the encrypted message</u>. 

`Player-0` can now ask from `Player-2` to verify the message

```
prove Player-2 0.0.95518-1585566306-230801600 302a30...633a
```

where the second argument is the copied application id and the third argument is Player-0's public signing key 

A single validation request is sent (encrypted) to Player-2. If all works as expected then Player-2 should automatically validate the message and Player-0's prompt should show a success message. 

```
    ...
    applicationMessageId: 0.0.95518-1585056383-216111500
    last chrono chunk consensus sequenceNum: 620
    last chrono chunk consensus running hash: 017b080...28c8
    ApplicationMessage:
        Id: 0.0.95518-1585056383-216111500
        Hash of unencrypted message: af0c3e9...bb64
        Signature on hash above: 1f8659...7303
        Message verification result: VALIDATION_OK  
        Encryption random:  
        Is this a self message?: false
```

Note that for this to work, Player-2 must be up and running. 

### Key Rotation

Ensure `./config/config.yaml` has its `keyrotation` property set to `true` and use a rotation frequency greater than zero. You can keep sending messages as usually but you will now notice keyrotation feedback in the terminal.  Key rotation messages are sent and handled automatically in the background but whenever one is sent you should expect to see echo-feedback as well, which might make it a bit difficult to monitor the flow of messages.   

## Setting up a relay and a queue

The demo is preconfigured to run with a direct mirror connections. What this means is that HCS SXC connects a listener to a mirror GRPC endpoint and listens for incoming topic messages. Alternatively, it is possible to connect indirectly where mirror messages end up first on a MQ (Artemis Message Queue) and the core then picks up the messages from the queue instead of the mirror. 

The reason you want to do this is to have multiple app participants listen to the queue in a pub-sub fashion and also send *out of band* messages between them that don't need to go via HCS. 

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
