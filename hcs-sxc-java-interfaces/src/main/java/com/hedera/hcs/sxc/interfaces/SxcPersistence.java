package com.hedera.hcs.sxc.interfaces;

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

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.consensus.ConsensusMessageSubmitTransaction;
import com.hedera.hcs.sxc.commonobjects.SxcConsensusMessage;
import com.hedera.hcs.sxc.proto.ApplicationMessage;
import com.hedera.hcs.sxc.proto.ApplicationMessageChunk;
import com.hedera.hcs.sxc.proto.ApplicationMessageID;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public interface SxcPersistence {
    // Hibernate properties
    public void setHibernateProperties(Map<String, String> hibernateProperties);
    
    // message chunking persistence
    public List<ApplicationMessageChunk> getParts(ApplicationMessageID applicationMessageId);
    public void putParts(ApplicationMessageID applicationMessageId, List<ApplicationMessageChunk> l);
    public void removeParts(ApplicationMessageID messageEnvelopeId);
    
    // mirror message persistence
    void storeMirrorResponse(SxcConsensusMessage mirrorTopicMessageResponse);
    public SxcConsensusMessage getMirrorResponse(String timestamp);
    public Map<String, SxcConsensusMessage> getMirrorResponses(String fromTimestamp, String toTimestamp);
    public Map<String, SxcConsensusMessage> getMirrorResponses();

    // HCS transaction persistence
    void storeTransaction(TransactionId transactionId, ConsensusMessageSubmitTransaction submitMessageTransaction);
    public ConsensusMessageSubmitTransaction getSubmittedTransaction(String transactionId);
    public Map<String, ConsensusMessageSubmitTransaction> getSubmittedTransactions();
    
   // Application message persistence
  
   public void storeApplicationMessage(
        ApplicationMessage applicationMessage, // the message
        Instant lastChronoPartConsensusTimestamp, // consensus data if available
        String lastChronoPartRunningHashHEX, // consensus data if available
        long lastChronoPartSequenceNum // consensus data if available
   );
   
   public Map<String, ApplicationMessage> getApplicationMessages();
   public List<? extends SxcApplicationMessageInterface> getSXCApplicationMessages(); 
    
   public SxcApplicationMessageInterface getApplicationMessageEntity(String applicationMessageId);
   public ApplicationMessage getApplicationMessage(String applicationMessageId) throws InvalidProtocolBufferException;
   
    
    // secret key  and keySpec holder for key rotation
    public void storeSecretKey(byte[] secretKey);
    public byte[] getSecretKey();

    public void storePublicKey(byte[] secretKey);
    public byte[] getPublicKey();

    // consensus timestamp
    public Instant getLastConsensusTimestamp();

    // persistence level
    public void setPersistenceLevel(MessagePersistenceLevel persistenceLevel);

    // clear all data
    public void clear();

    // AddressBook Persistence
    public Map<String,Map<String,String>> getAddressList();
    public void addOrUpdateAppParticipant(String appId, String theirEd25519PubKeyForSigning, String sharedSymmetricEncryptionKey);
    public void removeAppParticipant(String appId);
    
    //extract primary key
    public static String extractApplicationMessageStringId(ApplicationMessageID applicationMessageID){
         {
        // need to know if message was sent by me. I have to lookup
        // in db to see if I placed it into it when I sent with OutboutHCS...
        String applicationMessageId =
                applicationMessageID.getAccountID().getShardNum()
                + "." + applicationMessageID.getAccountID().getRealmNum()
                + "." + applicationMessageID.getAccountID().getAccountNum()
                + "-" + applicationMessageID.getValidStart().getSeconds()
                + "-" + applicationMessageID.getValidStart().getNanos();
        return applicationMessageId;
    }

    }
}
    