package com.hedera.hcslib.consensus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.HederaNetworkException;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.consensus.SubmitMessageTransaction;
import com.hedera.hashgraph.sdk.consensus.TopicId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hcslib.HCSLib;

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
    private List<TopicId> topicIds = new ArrayList<TopicId>();
    
    public OutboundHCSMessage(HCSLib hcsLib) {
        this.signMessages = hcsLib.getSignMessages();
        this.encryptMessages = hcsLib.getEncryptMessages();
        this.rotateKeys = hcsLib.getRotateKeys();
        this.nodeMap = hcsLib.getNodeMap();
        this.operatorAccountId = hcsLib.getOperatorAccountId();
        this.ed25519PrivateKey = hcsLib.getEd25519PrivateKey();
        this.topicIds = hcsLib.getTopicIds();
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
    
    public boolean sendMessage(int topicIndex, String message) throws HederaNetworkException, IllegalArgumentException, HederaException {

        if (signMessages) {
            
        }
        if (encryptMessages) {
            
        }
        if (rotateKeys) {
            int messageCount = 0; //TODO - keep track of messages app-wide, not just here.
            if (messageCount > rotationFrequency) {
            }
        }

        // sends a message to HCS
        Client client = new Client(this.nodeMap);
        client.setOperator(
            this.operatorAccountId
            ,this.ed25519PrivateKey
        );
        client.setMaxTransactionFee(100_000_000L);

        // generate TXId for main and first message
        TransactionId transactionId = new TransactionId(this.operatorAccountId);
        
        // create complete message (from app provided data) in protobuf (CompleteMessage)
            //-> TxId
            //-> Complete App Message (ByteArray) (bytes in protobuf -> ByteString in java)
            
        // convert above protobuf message to byte array
        
        // break up byte array into 3500 bytes parts
        
        // protobuf message part objects (iterate) -> MessagePart
            // include above part(s) into each
            // -> (?) convert to byte array and send to JMS
        
        TransactionReceipt receipt = new SubmitMessageTransaction(client)
                .setMessage(message.getBytes())
                .setTopicId(this.topicIds.get(topicIndex))
                .setTransactionId(transactionId)
                .executeForReceipt();
        log.info(receipt.getTopicSequenceNumber());
                
        return true;
    }
}
