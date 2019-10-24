package com.hedera.hcslib;

import java.util.HashMap;
import java.util.Map;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.HederaNetworkException;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.account.CryptoTransferTransaction;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;

public final class HCSLib {

    private boolean signMessages = false;
    private boolean encryptMessages = false;
    private boolean rotateKeys = false;
    private int rotationFrequency = 0;
    private String kmsSolution = "";
    private String queueProtocol = "";
    private Map<AccountId, String> nodeMap = new HashMap<AccountId, String>();
    private AccountId operatorAccountId = new AccountId(0, 0, 0); 
    private Ed25519PrivateKey ed25519PrivateKey; 
    
    public HCSLib withMessageSignature(boolean signMessages) {
        this.signMessages = signMessages;
        return this;
    }
    public HCSLib withEncryptedMessages(boolean encryptMessages) {
        this.encryptMessages = encryptMessages;
        return this;
    }
    public HCSLib withKeyRotation(boolean keyRotation, int frequency) {
        this.rotateKeys = keyRotation;
        this.rotationFrequency = frequency;
        return this;
    }
    public HCSLib withKmsSolution(String kmsSolution) {
        this.kmsSolution = kmsSolution;
        return this;
    }
    public HCSLib withQueueProtocol(String queueProtocol) {
        this.queueProtocol = queueProtocol;
        return this;
    }
    public HCSLib withNodeMap(Map<AccountId, String> nodeMap) {
        this.nodeMap = nodeMap;
        return this;
    }
    public HCSLib withOperatorAccountId(AccountId operatorAccountId) {
        this.operatorAccountId = operatorAccountId;
        return this;
    }
    public HCSLib withOperatorKey(Ed25519PrivateKey ed25519PrivateKey) {
        this.ed25519PrivateKey = ed25519PrivateKey;
        return this;
    }
    public boolean sendMessage(String message) throws HederaNetworkException, IllegalArgumentException, HederaException {
        //TODO: 
//        private boolean signMessages = false;
//        private boolean encryptMessages = false;
//        private boolean rotateKeys = false;
//        private int rotationFrequency = 0;
//        private String kmsSolution = "";

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
