package com.hedera.hcs.sxc.plugin.persistence.inmemory;

/*-
 * ‌
 * hcs-sxc-java
 * ​
 * Copyright (C) 2019 - 2020 Hedera Hashgraph, LLC
 * ​
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ‍
 */

import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.consensus.ConsensusMessageSubmitTransaction;
import com.hedera.hcs.sxc.commonobjects.SxcConsensusMessage;
import com.hedera.hcs.sxc.interfaces.MessagePersistenceLevel;
import com.hedera.hcs.sxc.proto.ApplicationMessage;
import com.hedera.hcs.sxc.proto.ApplicationMessageChunk;
import com.hedera.hcs.sxc.proto.ApplicationMessageID;

import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
public class Persist 
        implements com.hedera.hcs.sxc.interfaces.SxcPersistence{
    
    private Map<ApplicationMessageID, List<ApplicationMessageChunk>> partialMessages;
    private Map<String, ConsensusMessageSubmitTransaction> transactions;
    private Map<String, SxcConsensusMessage> mirrorTopicMessages;
    private Map<String, ApplicationMessage> applicationMessages;
    
    private MessagePersistenceLevel persistenceLevel = MessagePersistenceLevel.FULL;
    
    public Persist() throws IOException{
        partialMessages = new HashMap<>();
        transactions = new HashMap<>();
        mirrorTopicMessages = new HashMap<>();
        applicationMessages = new HashMap<>();
    }
    
    public void setPersistenceLevel(MessagePersistenceLevel persistenceLevel) {
        this.persistenceLevel = persistenceLevel;
    }

//    0: none
//    1: timestamp, hash, signature and content for my messages (those I sent or those sent to me)
//    2: 1+ timestamps, hashes and signatures for all messages (regardless of sender/recipient), and content only for my messages
//    3: timestamp, hash, signature and contents for all messages
//  
    // Mirror responses
    @Override
    public void storeMirrorResponse(SxcConsensusMessage consensusMessage) {
        mirrorTopicMessages.put(consensusMessage.consensusTimestamp.toString(), consensusMessage);
        log.debug("storeMirrorResponse " + consensusMessage.consensusTimestamp.toString() + "-" + consensusMessage);
    }
    
    @Override 
    public SxcConsensusMessage getMirrorResponse(String timestamp) {
        return mirrorTopicMessages.get(timestamp);
    }
        
    @Override 
    public Map<String, SxcConsensusMessage> getMirrorResponses() {
        return mirrorTopicMessages;
    }
    
    @Override
    public Map<String, SxcConsensusMessage> getMirrorResponses(String fromTimestamp, String toTimestamp) {
        Map<String, SxcConsensusMessage> response = new HashMap<String, SxcConsensusMessage>();
        
        mirrorTopicMessages.forEach((key,value) -> {
            if ((key.compareTo(fromTimestamp) >= 0) && (key.compareTo(toTimestamp) <= 0) ) {
                response.put(key, value);            
            }
        }); 
        
        return response;
    }
    

    // Transactions
    @Override
    public void storeTransaction(TransactionId transactionId, ConsensusMessageSubmitTransaction submitMessageTransaction) {
        String txId = transactionId.accountId.shard
                + "." + transactionId.accountId.realm
                + "." + transactionId.accountId.account
                + "-" + transactionId.validStart.getEpochSecond()
                + "-" + transactionId.validStart.getNano();
        
        transactions.put(txId, submitMessageTransaction);
        log.debug("storeTransaction " + txId + "-" + submitMessageTransaction);
    }
    
    @Override 
    public ConsensusMessageSubmitTransaction getSubmittedTransaction(String transactionId) {
        return transactions.get(transactionId);
    }

    @Override 
    public Map<String, ConsensusMessageSubmitTransaction> getSubmittedTransactions() {
        return transactions;
    }
    
    @Override
    public List<ApplicationMessageChunk> getParts(ApplicationMessageID applicationMessageId) {
        return this.partialMessages.get(applicationMessageId);
    }

    @Override
    public void storeApplicationMessage(ApplicationMessageID applicationMessageId, ApplicationMessage applicationMessage) {
        String appMessageId = applicationMessageId.getAccountID().getShardNum()
                + "." + applicationMessageId.getAccountID().getRealmNum()
                + "." + applicationMessageId.getAccountID().getAccountNum()
                + "-" + applicationMessageId.getValidStart().getSeconds()
                + "-" + applicationMessageId.getValidStart().getNanos();

        applicationMessages.put(appMessageId, applicationMessage);
        log.debug("storeApplicationMessage " + appMessageId + "-" + applicationMessage);
    }

    @Override
    public ApplicationMessage getApplicationMessage(String applicationMessageId) {
        return applicationMessages.get(applicationMessageId);
    }

    
    @Override
    public Map<String, ApplicationMessage> getApplicationMessages() {
        return applicationMessages;
    }

    @Override
    public void putParts(ApplicationMessageID applicationMessageId, List<ApplicationMessageChunk> l) {
        // always keep data to allow for reassembly of messages,
        // part messages can be deleted once full messages have been reconstituted
        // see removeParts
        this.partialMessages.put(applicationMessageId, l);            
    }

    @Override
    public void removeParts(ApplicationMessageID applicationMessageId) {
        switch (persistenceLevel) {
            case FULL:
                // do not remove stored data
                break;
            case MESSAGE_AND_PARTS:
                // do not remove stored data
                break;
            case MESSAGE_ONLY:
                this.partialMessages.remove(applicationMessageId);
                break;
            case NONE:
                this.partialMessages.remove(applicationMessageId);
                break;
        }
    }
    
    @Override
    public Instant getLastConsensusTimestamp() {

        Instant lastConsensusTimestamp = Instant.EPOCH;
        for (Map.Entry<String, SxcConsensusMessage> mirrorTopicMessage : mirrorTopicMessages.entrySet()) {
            long seconds = mirrorTopicMessage.getValue().consensusTimestamp.getEpochSecond();
            int nanos = mirrorTopicMessage.getValue().consensusTimestamp.getNano();
            
            if (lastConsensusTimestamp.getEpochSecond() < seconds) {
                lastConsensusTimestamp = Instant.ofEpochSecond(seconds, nanos);
            } else if ((lastConsensusTimestamp.getEpochSecond() == seconds) && (lastConsensusTimestamp.getNano() < nanos)) {
                lastConsensusTimestamp = Instant.ofEpochSecond(seconds, nanos);
            }
        }
        return lastConsensusTimestamp;
    }

    @Override
    public void clear() {
        partialMessages = new HashMap<>();
        transactions = new HashMap<>();
        mirrorTopicMessages = new HashMap<>();
        applicationMessages = new HashMap<>();
    }

    @Override
    public void setHibernateProperties(Map<String, String> hibernateProperties) {
        
    }

    @Override
    public void storeSecretKey(byte[] secretKey) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public byte[] getSecretKey() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void storePublicKey(byte[] secretKey) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public byte[] getPublicKey() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

   
}
