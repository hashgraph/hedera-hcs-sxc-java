package com.hedera.hcs.sxc.consensus;

import com.google.protobuf.ByteString;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.HederaNetworkException;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.consensus.ConsensusMessageSubmitTransaction;
import com.hedera.hashgraph.sdk.consensus.ConsensusTopicId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hcs.sxc.HCSCore;
import com.hedera.hcs.sxc.config.Topic;
import com.hedera.hcs.sxc.interfaces.SxcMessagePersistence;
import com.hedera.hcs.sxc.plugins.Plugins;
import com.hedera.hcs.sxc.proto.AccountID;
import com.hedera.hcs.sxc.proto.ApplicationMessage;
import com.hedera.hcs.sxc.proto.ApplicationMessageChunk;
import com.hedera.hcs.sxc.proto.ApplicationMessageId;
import com.hedera.hcs.sxc.proto.Timestamp;

import java.util.Arrays;

import lombok.extern.log4j.Log4j2;

@Log4j2
public final class OutboundHCSMessage {

    private boolean signMessages = false;
    private boolean encryptMessages = false;
    private boolean rotateKeys = false;
    private int rotationFrequency = 0;
    private Map<AccountId, String> nodeMap = new HashMap<AccountId, String>();
    private AccountId operatorAccountId = new AccountId(0, 0, 0);
    private Ed25519PrivateKey ed25519PrivateKey;
    private List<Topic> topics = new ArrayList<Topic>();
    private long hcsTransactionFee = 0L;
    private TransactionId transactionId = null;
    private SxcMessagePersistence persistence;

    public OutboundHCSMessage(HCSCore hcsCore) throws Exception {
        this.signMessages = hcsCore.getSignMessages();
        this.encryptMessages = hcsCore.getEncryptMessages();
        this.rotateKeys = hcsCore.getRotateKeys();
        this.nodeMap = hcsCore.getNodeMap();
        this.operatorAccountId = hcsCore.getOperatorAccountId();
        this.ed25519PrivateKey = hcsCore.getEd25519PrivateKey();
        this.topics = hcsCore.getTopics();
        this.hcsTransactionFee = hcsCore.getMaxTransactionFee();

        // load persistence implementation at runtime
        Class<?> persistenceClass = Plugins.find("com.hedera.hcs.sxc.plugin.persistence.*", "com.hedera.hcs.sxc.interfaces.SxcMessagePersistence", true);
        this.persistence = (SxcMessagePersistence)persistenceClass.newInstance();
    }

    public OutboundHCSMessage overrideMessageSignature(boolean signMessages) {
        this.signMessages = signMessages;
        return this;
    }

    public OutboundHCSMessage overrideEncryptedMessages(boolean encryptMessages) {
        this.encryptMessages = encryptMessages;
        return this;
    }

    public OutboundHCSMessage overrideKeyRotation(boolean keyRotation, int frequency) {
        this.rotateKeys = keyRotation;
        this.rotationFrequency = frequency;
        return this;
    }

    public OutboundHCSMessage overrideNodeMap(Map<AccountId, String> nodeMap) {
        this.nodeMap = nodeMap;
        return this;
    }

    public OutboundHCSMessage overrideOperatorAccountId(AccountId operatorAccountId) {
        this.operatorAccountId = operatorAccountId;
        return this;
    }

    public OutboundHCSMessage overrideOperatorKey(Ed25519PrivateKey ed25519PrivateKey) {
        this.ed25519PrivateKey = ed25519PrivateKey;
        return this;
    }

    public OutboundHCSMessage withFirstTransactionId(TransactionId transactionId) {
        this.transactionId = transactionId;
        return this;
    }

    /**
     * Sends a single cleartext message
     *
     * @param topicIndex the index reference in one of {@link #topics}
     * @param message
     * @throws HederaNetworkException
     * @throws IllegalArgumentException
     * @throws Exception
     */
    public TransactionId sendMessage(int topicIndex, byte[] message) throws Exception {

        if (signMessages) {

        }
        if (encryptMessages) {

        }
        if (rotateKeys) {
            int messageCount = 0; //TODO - keep track of messages app-wide, not just here. ( per topic )
            if (messageCount > rotationFrequency) {
            }
        }

        // generate TXId for main and first message it not already set by caller
        TransactionId firstTransactionId = (this.transactionId == null) ? new TransactionId(this.operatorAccountId) : this.transactionId;

        //break up
        List<ApplicationMessageChunk> parts = chunk(firstTransactionId, message);
        // send each part to the network

        try (Client client = new Client(this.nodeMap)) {
            client.setOperator(
                    this.operatorAccountId,
                     this.ed25519PrivateKey
            );

            client.setMaxTransactionFee(this.hcsTransactionFee);

            TransactionId transactionId = firstTransactionId;
            int count = 1;
            for (ApplicationMessageChunk messageChunk : parts) {
                log.info("Sending message part " + count + " of " + parts.size() + " to topic " + this.topics.get(topicIndex));
                count++;
                ConsensusMessageSubmitTransaction tx = new ConsensusMessageSubmitTransaction()
                    .setMessage(messageChunk.toByteArray())
                    .setTopicId(this.topics.get(topicIndex).getConsensusTopicId())
                    .setTransactionId(transactionId);

                if ((this.topics.get(topicIndex).getSubmitKey() != null) && (! this.topics.get(topicIndex).getSubmitKey().isEmpty())) {
                    // sign if we have a submit key
                    tx.build(client).sign(Ed25519PrivateKey.fromString(this.topics.get(topicIndex).getSubmitKey()));
                }

                // persist the transaction
                this.persistence.storeTransaction(transactionId, tx);

                log.info("Executing transaction");
                TransactionId txId = tx.execute(client);

                TransactionReceipt receipt = txId.getReceipt(client, Duration.ofSeconds(30));

                transactionId = new TransactionId(this.operatorAccountId);

                log.info("status is {} "
                        + "sequence no is {}"
                        ,receipt.status
                        ,receipt.getConsensusTopicSequenceNumber()
                );

            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (TimeoutException e) {
            // do nothing
        } catch (Exception e) {
            log.error(e);
            throw e;
        }

        return firstTransactionId;
    }

    public static  List<ApplicationMessageChunk> chunk(TransactionId transactionId,  byte[] message) {
        
        ApplicationMessageId transactionID = ApplicationMessageId.newBuilder()
                .setAccountID(AccountID.newBuilder()
                        .setShardNum(transactionId.accountId.shard)
                        .setRealmNum(transactionId.accountId.realm)
                        .setAccountNum(transactionId.accountId.account)
                        .build()
                )
                .setValidStart(Timestamp.newBuilder()
                        .setSeconds(transactionId.validStart.getEpochSecond())
                        .setNanos(transactionId.validStart.getNano())
                        .build()
                ).build();

        byte[] originalMessage = Arrays.copyOf(message, message.length);

        ApplicationMessage applicationMessage = ApplicationMessage
                .newBuilder()
                .setApplicationMessageId(transactionID)
                .setBusinessProcessMessage(ByteString.copyFrom(originalMessage))
                .build();

        List<ApplicationMessageChunk> parts = new ArrayList<>();

        //TransactionID transactionID = messageEnvelope.getMessageEnvelopeId();
        byte[] amByteArray = applicationMessage.toByteArray();
        final int amByteArrayLength = amByteArray.length;
        // break up byte array into 3500 bytes parts
        final int chunkSize = 3500; // the hcs tx limit is 4k - there are header bytes that will be added to that
        int totalParts = (int) Math.ceil((double) amByteArrayLength / chunkSize);
        // chunk and send to network
        for (int i = 0, partId = 1; i < amByteArrayLength; i += chunkSize, partId++) {

            byte[] amMessageChunk = Arrays.copyOfRange(
                    amByteArray,
                    i,
                    Math.min(amByteArrayLength, i + chunkSize)
            );

            ApplicationMessageChunk applicationMessageChunk = ApplicationMessageChunk.newBuilder()
                    .setApplicationMessageId(transactionID)
                    .setChunkIndex(partId)
                    .setChunksCount(totalParts)
                    .setMessageChunk(ByteString.copyFrom(amMessageChunk))
                    .build();

            parts.add(applicationMessageChunk);
        }
        return parts;
    }
}
