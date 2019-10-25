package com.hedera.hcslib.outbound;

import java.util.HashMap;
import java.util.Map;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.HederaNetworkException;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.account.CryptoTransferTransaction;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hcslib.HCSLib;

public final class OutboundHCSMessage {
    private boolean signMessages = false;
    private boolean encryptMessages = false;
    private boolean rotateKeys = false;
    private int rotationFrequency = 0;
    private Map<AccountId, String> nodeMap = new HashMap<AccountId, String>();
    private AccountId operatorAccountId = new AccountId(0, 0, 0); 
    private Ed25519PrivateKey ed25519PrivateKey; 
    
    public OutboundHCSMessage(HCSLib hcsLib) {
        this.signMessages = hcsLib.getSignMessages();
        this.encryptMessages = hcsLib.getEncryptMessages();
        this.rotateKeys = hcsLib.getRotateKeys();
        this.nodeMap = hcsLib.getNodeMap();
        this.operatorAccountId = hcsLib.getOperatorAccountId();
        this.ed25519PrivateKey = hcsLib.getEd25519PrivateKey();
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
    
    public boolean sendMessage(String message) throws HederaNetworkException, IllegalArgumentException, HederaException {

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

        TransactionId id = new TransactionId(this.operatorAccountId); 
        client.setMaxTransactionFee(100_000_000L);
        
        id = new CryptoTransferTransaction(client)
            .addSender(this.operatorAccountId, 1)
            .addRecipient(AccountId.fromString("0.0.3"), 1)
            .setMemo(message)
            .setTransactionId(id)
            .execute();
        
        return true;
    }

}
