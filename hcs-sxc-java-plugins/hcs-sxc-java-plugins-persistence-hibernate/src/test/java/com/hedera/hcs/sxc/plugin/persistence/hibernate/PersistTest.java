package com.hedera.hcs.sxc.plugin.persistence.hibernate;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import com.google.protobuf.ByteString;
import com.hedera.hashgraph.proto.Timestamp;
import com.hedera.hashgraph.proto.mirror.ConsensusTopicResponse;
import com.hedera.hashgraph.sdk.consensus.ConsensusTopicId;
import com.hedera.hcs.sxc.commonobjects.SxcConsensusMessage;
import com.hedera.hcs.sxc.interfaces.MessagePersistenceLevel;
import com.hedera.hcs.sxc.interfaces.SxcApplicationMessageInterface;
import com.hedera.hcs.sxc.proto.AccountID;
import com.hedera.hcs.sxc.proto.ApplicationMessage;
import com.hedera.hcs.sxc.proto.ApplicationMessageChunk;
import com.hedera.hcs.sxc.proto.ApplicationMessageID;


public class PersistTest {
    
    private Map<String, String> getHibernateProperties(String dbName) {
        Map<String, String> hibernateProperties = new HashMap<String, String>();
        hibernateProperties.put("hibernate.connection.driver_class", "org.h2.Driver");
        hibernateProperties.put("hibernate.connection.url", "jdbc:h2:mem:" +  dbName);
        hibernateProperties.put("hibernate.connection.username", "admin");
        hibernateProperties.put("hibernate.connection.password", "");
        hibernateProperties.put("hibernate.default_schema", "PUBLIC");
        hibernateProperties.put("hibernate.connection.pool_size", "5");

        hibernateProperties.put("hibernate.dialect.H2Dialect", "org.hibernate.dialect.H2Dialect");
        hibernateProperties.put("hibernate.cache.provider_class", "org.hibernate.cache.internal.NoCacheProvider");
        hibernateProperties.put("hibernate.show_sql", "true");
        hibernateProperties.put("hibernate.hbm2ddl.auto", "update");

        return hibernateProperties;
    }
    
    @Test
    public void testMirrorResponses() throws Exception {
        Persist persist = new Persist();
        persist.setHibernateProperties(getHibernateProperties("testMirrorResponses"));
        persist.setPersistenceLevel(MessagePersistenceLevel.FULL);
        
        ConsensusTopicResponse consensusTopicResponse = ConsensusTopicResponse.newBuilder()
                .setConsensusTimestamp(Timestamp.newBuilder().setSeconds(10).build())
                .setMessage(ByteString.copyFromUtf8("message"))
                .setRunningHash(ByteString.copyFromUtf8("runninghash"))
                .setSequenceNumber(10)
                .build();
        
        SxcConsensusMessage consensusMessage = new SxcConsensusMessage(ConsensusTopicId.fromString("1.2.3"), consensusTopicResponse);
        persist.storeMirrorResponse(consensusMessage);

        consensusTopicResponse = ConsensusTopicResponse.newBuilder()
                .setConsensusTimestamp(Timestamp.newBuilder().setSeconds(20).build())
                .setMessage(ByteString.copyFromUtf8("message2"))
                .setRunningHash(ByteString.copyFromUtf8("runninghash2"))
                .setSequenceNumber(11)
                .build();
        
        consensusMessage = new SxcConsensusMessage(ConsensusTopicId.fromString("4.5.6"), consensusTopicResponse);
        persist.storeMirrorResponse(consensusMessage);
        
        SxcConsensusMessage response = persist.getMirrorResponse("1970-01-01T00:00:10Z");
        assertNotNull(response);
        assertEquals(10, response.consensusTimestamp.getEpochSecond());
        assertEquals(0, response.consensusTimestamp.getNano());
        assertArrayEquals("message".getBytes(), response.message);
        assertArrayEquals("runninghash".getBytes(), response.runningHash);
        assertEquals(10, response.sequenceNumber);
        assertEquals(1, response.topicId.shard);
        assertEquals(2, response.topicId.realm);
        assertEquals(3, response.topicId.topic);
        
        Map<String, SxcConsensusMessage> responses = persist.getMirrorResponses();
        assertEquals(2, responses.size());
        
        consensusTopicResponse = ConsensusTopicResponse.newBuilder()
                .setConsensusTimestamp(Timestamp.newBuilder().setSeconds(30).build())
                .setMessage(ByteString.copyFromUtf8("message2"))
                .setRunningHash(ByteString.copyFromUtf8("runninghash2"))
                .setSequenceNumber(11)
                .build();
        
        consensusMessage = new SxcConsensusMessage(ConsensusTopicId.fromString("0.0.1"), consensusTopicResponse);
        persist.storeMirrorResponse(consensusMessage);
        
        responses = persist.getMirrorResponses("1970-01-01T00:00:10Z", "1970-01-01T00:00:20Z");
        assertEquals(2, responses.size());
        
        persist.clear();
        responses = persist.getMirrorResponses();
        assertEquals(0, responses.size());
        
    }

    @Test
    public void testTransactions() throws Exception {
//        Persist persist = new Persist();
//        persist.setHibernateProperties(getHibernateProperties("testTransactions"));
//        
//        TransactionId transactionId = TransactionId.withValidStart(AccountId.fromString("1.2.3"), Instant.ofEpochSecond(100, 10));
//        ConsensusMessageSubmitTransaction submitMessageTransaction = new ConsensusMessageSubmitTransaction();
//        submitMessageTransaction.setMaxTransactionFee(1000);
//        submitMessageTransaction.setMessage("message");
//        submitMessageTransaction.setNodeAccountId(AccountId.fromString("4.5.6"));
//        submitMessageTransaction.setTopicId(ConsensusTopicId.fromString("7.8.9"));
//        submitMessageTransaction.setTransactionId(transactionId);
//        submitMessageTransaction.setTransactionMemo("memo");
//        submitMessageTransaction.setTransactionValidDuration(Duration.ofSeconds(200, 20));
//        persist.storeTransaction(transactionId, submitMessageTransaction);
//        
//        TransactionId transactionId2 = TransactionId.withValidStart(AccountId.fromString("10.20.30"), Instant.ofEpochSecond(100, 10));
//        ConsensusMessageSubmitTransaction submitMessageTransaction2 = new ConsensusMessageSubmitTransaction();
//        submitMessageTransaction2.setMaxTransactionFee(10000);
//        submitMessageTransaction2.setMessage("message2");
//        submitMessageTransaction2.setNodeAccountId(AccountId.fromString("40.50.60"));
//        submitMessageTransaction2.setTopicId(ConsensusTopicId.fromString("70.80.90"));
//        submitMessageTransaction2.setTransactionId(transactionId2);
//        submitMessageTransaction2.setTransactionMemo("memo2");
//        submitMessageTransaction2.setTransactionValidDuration(Duration.ofSeconds(400, 40));
//        persist.storeTransaction(transactionId2, submitMessageTransaction2);
//        
//        String txId = transactionId.accountId.shard
//                + "." + transactionId.accountId.realm
//                + "." + transactionId.accountId.account
//                + "-" + transactionId.validStart.getEpochSecond()
//                + "-" + transactionId.validStart.getNano();        
//        
//        ConsensusMessageSubmitTransaction cmsTransaction = persist.getSubmittedTransaction(txId);
//        assertNotNull(cmsTransaction);
//        assertTrue(cmsTransaction.equals(submitMessageTransaction));
//        
//        
//        Map<String, ConsensusMessageSubmitTransaction> cmsTransactions = persist.getSubmittedTransactions();
//        assertEquals(2, cmsTransactions.size());
//
//        persist.clear();
//        cmsTransactions = persist.getSubmittedTransactions();
//        assertEquals(0, cmsTransactions.size());
    }
    
    @Test
    public void testApplicationMessages() throws Exception {
        Persist persist = new Persist();
        persist.setHibernateProperties(getHibernateProperties("testApplicationMessages"));

        ApplicationMessageID applicationMessageID = ApplicationMessageID.newBuilder()
                .setAccountID(AccountID.newBuilder().setAccountNum(3).setRealmNum(2).setShardNum(1).build())
                .setValidStart(com.hedera.hcs.sxc.proto.Timestamp.newBuilder().setSeconds(10))
                .build();
        ApplicationMessage applicationMessage = ApplicationMessage.newBuilder()
                .setApplicationMessageId(applicationMessageID)
                .setUnencryptedBusinessProcessMessageHash(ByteString.copyFromUtf8("businessProcessHash"))
                .setBusinessProcessMessage(ByteString.copyFromUtf8("businessProcessMessage"))
                .setBusinessProcessSignatureOnHash(ByteString.copyFromUtf8("businessProcessSignature"))
                .build();
        
        Instant lastChronoPartConsensusTimestamp = Instant.now();
        String lastChronoPartRunningHashHEX = "lastChronoPartRunningHashHEX";
        long lastChronoPartSequenceNum = 10;
        
        persist.storeApplicationMessage(applicationMessage, lastChronoPartConsensusTimestamp, lastChronoPartRunningHashHEX, lastChronoPartSequenceNum);

        ApplicationMessageID applicationMessageID2 = ApplicationMessageID.newBuilder()
                .setAccountID(AccountID.newBuilder().setAccountNum(4).setRealmNum(5).setShardNum(6).build())
                .setValidStart(com.hedera.hcs.sxc.proto.Timestamp.newBuilder().setSeconds(20))
                .build();
        ApplicationMessage applicationMessage2 = ApplicationMessage.newBuilder()
                .setApplicationMessageId(applicationMessageID2)
                .setBusinessProcessMessage(ByteString.copyFromUtf8("businessProcessMessage2"))
                .setUnencryptedBusinessProcessMessageHash(ByteString.copyFromUtf8("businessProcessHash2"))
                .setBusinessProcessSignatureOnHash(ByteString.copyFromUtf8("businessProcessSignature2"))
                .build();
        
        Instant lastChronoPartConsensusTimestamp2 = Instant.now();
        String lastChronoPartRunningHashHEX2 = "lastChronoPartRunningHashHEX";
        long lastChronoPartSequenceNum2 = 10;
        
        persist.storeApplicationMessage(applicationMessage2, lastChronoPartConsensusTimestamp2, lastChronoPartRunningHashHEX2, lastChronoPartSequenceNum2);
        
        String appMessageId = applicationMessageID.getAccountID().getShardNum()
                + "." + applicationMessageID.getAccountID().getRealmNum()
                + "." + applicationMessageID.getAccountID().getAccountNum()
                + "-" + applicationMessageID.getValidStart().getSeconds()
                + "-" + applicationMessageID.getValidStart().getNanos();
        
        ApplicationMessage getApplicationMessage = persist.getApplicationMessage(appMessageId);
        assertNotNull(getApplicationMessage);
        assertEquals(applicationMessageID, getApplicationMessage.getApplicationMessageId());
        assertEquals("businessProcessHash", getApplicationMessage.getUnencryptedBusinessProcessMessageHash().toStringUtf8());
        assertEquals("businessProcessMessage", getApplicationMessage.getBusinessProcessMessage().toStringUtf8());
        assertEquals("businessProcessSignature", getApplicationMessage.getBusinessProcessSignatureOnHash().toStringUtf8());

        Map<String, ApplicationMessage> getApplicationMessages = persist.getApplicationMessages();
        assertEquals(2, getApplicationMessages.size());
        
        @SuppressWarnings("unused")
        List<? extends SxcApplicationMessageInterface> getSXCApplicationMessages = persist.getSXCApplicationMessages();
        assertEquals(2, getApplicationMessages.size());
        
        SxcApplicationMessageInterface getSXCApplicationMessage = persist.getApplicationMessageEntity(appMessageId);
        assertNotNull(getSXCApplicationMessage);
        assertEquals(appMessageId, getSXCApplicationMessage.getApplicationMessageId());
        assertArrayEquals(applicationMessage.toByteArray(), getSXCApplicationMessage.getApplicationMessage());
        assertTrue(lastChronoPartConsensusTimestamp.equals(getSXCApplicationMessage.getLastChronoPartConsensusTimestamp()));
        assertEquals(lastChronoPartRunningHashHEX, getSXCApplicationMessage.getLastChronoPartRunningHashHEX());
        assertEquals(lastChronoPartSequenceNum, getSXCApplicationMessage.getLastChronoPartSequenceNum());

        persist.clear();
        getApplicationMessages = persist.getApplicationMessages();
        assertEquals(0, getApplicationMessages.size());
    }
    
    @Test
    public void testLastConsensus() throws Exception {
        Persist persist = new Persist();
        persist.setHibernateProperties(getHibernateProperties("testLastConsensus"));
        
        ConsensusTopicResponse consensusTopicResponse = ConsensusTopicResponse.newBuilder()
                .setConsensusTimestamp(Timestamp.newBuilder().setSeconds(10).build())
                .setMessage(ByteString.copyFromUtf8("message"))
                .setRunningHash(ByteString.copyFromUtf8("runninghash"))
                .setSequenceNumber(10)
                .build();
        
        SxcConsensusMessage consensusMessage = new SxcConsensusMessage(ConsensusTopicId.fromString("1.2.3"), consensusTopicResponse);
        persist.storeMirrorResponse(consensusMessage);

        ConsensusTopicResponse consensusTopicResponse2 = ConsensusTopicResponse.newBuilder()
                .setConsensusTimestamp(Timestamp.newBuilder().setSeconds(20).setNanos(12).build())
                .setMessage(ByteString.copyFromUtf8("message2"))
                .setRunningHash(ByteString.copyFromUtf8("runninghash2"))
                .setSequenceNumber(11)
                .build();
        
        SxcConsensusMessage consensusMessage2 = new SxcConsensusMessage(ConsensusTopicId.fromString("4.5.6"), consensusTopicResponse2);
        persist.storeMirrorResponse(consensusMessage2);
        
        consensusTopicResponse2 = ConsensusTopicResponse.newBuilder()
                .setConsensusTimestamp(Timestamp.newBuilder().setSeconds(20).setNanos(21).build())
                .setMessage(ByteString.copyFromUtf8("message2"))
                .setRunningHash(ByteString.copyFromUtf8("runninghash2"))
                .setSequenceNumber(12)
                .build();
        
        consensusMessage2 = new SxcConsensusMessage(ConsensusTopicId.fromString("7.8.9"), consensusTopicResponse2);
        persist.storeMirrorResponse(consensusMessage2);

        consensusTopicResponse2 = ConsensusTopicResponse.newBuilder()
                .setConsensusTimestamp(Timestamp.newBuilder().setSeconds(20).setNanos(9).build())
                .setMessage(ByteString.copyFromUtf8("message2"))
                .setRunningHash(ByteString.copyFromUtf8("runninghash2"))
                .setSequenceNumber(9)
                .build();
        
        consensusMessage2 = new SxcConsensusMessage(ConsensusTopicId.fromString("7.8.9"), consensusTopicResponse2);
        persist.storeMirrorResponse(consensusMessage2);

        Instant getLastConsensusTimestamp = persist.getLastConsensusTimestamp();
        assertNotNull(getLastConsensusTimestamp);
        assertEquals(20, getLastConsensusTimestamp.getEpochSecond());
        assertEquals(21, getLastConsensusTimestamp.getNano());
    }
    
    @Test
    public void testParts() throws Exception {
        Persist persist = new Persist();
        persist.setHibernateProperties(getHibernateProperties("testParts"));

        ApplicationMessageID applicationMessageID = ApplicationMessageID.newBuilder()
                .setAccountID(AccountID.newBuilder().setAccountNum(3).setRealmNum(2).setShardNum(1).build())
                .setValidStart(com.hedera.hcs.sxc.proto.Timestamp.newBuilder().setSeconds(50))
                .build();
        
        List<ApplicationMessageChunk> chunks = makeApplicationMessageChunks(applicationMessageID);
        persist.putParts(applicationMessageID, chunks);
        
        List<ApplicationMessageChunk> getParts = persist.getParts(applicationMessageID);
        
        assertEquals(3, getParts.size());

        persist.setPersistenceLevel(MessagePersistenceLevel.FULL);
        ApplicationMessageID applicationMessageID2 = ApplicationMessageID.newBuilder()
                .setAccountID(AccountID.newBuilder().setAccountNum(3).setRealmNum(2).setShardNum(1).build())
                .setValidStart(com.hedera.hcs.sxc.proto.Timestamp.newBuilder().setSeconds(60))
                .build();
        
        List<ApplicationMessageChunk> chunks2 = makeApplicationMessageChunks(applicationMessageID);
        persist.putParts(applicationMessageID2, chunks2);
        
        persist.removeParts(applicationMessageID);
        assertEquals(3, persist.getParts(applicationMessageID).size());
        assertEquals(3, persist.getParts(applicationMessageID2).size());
        
        persist.setPersistenceLevel(MessagePersistenceLevel.MESSAGE_AND_PARTS);
        persist.removeParts(applicationMessageID);
        assertEquals(3, persist.getParts(applicationMessageID).size());
        assertEquals(3, persist.getParts(applicationMessageID2).size());

        persist.setPersistenceLevel(MessagePersistenceLevel.MESSAGE_ONLY);
        persist.removeParts(applicationMessageID);
        assertNull(persist.getParts(applicationMessageID));
        assertEquals(3, persist.getParts(applicationMessageID2).size());

        persist.setPersistenceLevel(MessagePersistenceLevel.NONE);
        persist.putParts(applicationMessageID, chunks);
        persist.removeParts(applicationMessageID);
        assertNull(persist.getParts(applicationMessageID));
        assertEquals(3, persist.getParts(applicationMessageID2).size());

        persist.clear();
        assertNull(persist.getParts(applicationMessageID));
        assertNull(persist.getParts(applicationMessageID2));
    }
    
    @Test
    public void testAddressList() throws Exception {
        Persist persist = new Persist();
        persist.setHibernateProperties(getHibernateProperties("testAddressList"));
        
        assertDoesNotThrow( () -> {persist.addOrUpdateAppParticipant("0", "theirEd25519PubKeyForSigning0", "sharedSymmetricEncryptionKey0");});
        assertDoesNotThrow( () -> {persist.addOrUpdateAppParticipant("1", "theirEd25519PubKeyForSigning1", "sharedSymmetricEncryptionKey1");});
        
        Map<String, Map<String, String>> gotAddressList = persist.getAddressList();
        
        assertNotNull(gotAddressList.size());
        assertEquals(2, gotAddressList.size());
        assertEquals("theirEd25519PubKeyForSigning0", gotAddressList.get("0").get("theirEd25519PubKeyForSigning"));
        assertEquals("sharedSymmetricEncryptionKey0", gotAddressList.get("0").get("sharedSymmetricEncryptionKey"));
        assertEquals("theirEd25519PubKeyForSigning1", gotAddressList.get("1").get("theirEd25519PubKeyForSigning"));
        assertEquals("sharedSymmetricEncryptionKey1", gotAddressList.get("1").get("sharedSymmetricEncryptionKey"));
    }

    
    @Test
    public void testStoreRetrieveSecretKey() throws Exception {
        Persist persist = new Persist();
        persist.setHibernateProperties(getHibernateProperties("testAddressList"));
        byte[] secretkey = "1234".getBytes();
        persist.storeSecretKey(secretkey);
        byte[] secretkeyFromDb = persist.getSecretKey();
        
        assertArrayEquals(secretkey, secretkeyFromDb);
        // do one more time to test update
        secretkey = "9876".getBytes();
        persist.storeSecretKey(secretkey);
        secretkeyFromDb = persist.getSecretKey();
        assertArrayEquals(secretkey, secretkeyFromDb);
        
    }

    
    private List<ApplicationMessageChunk> makeApplicationMessageChunks(ApplicationMessageID applicationMessageID) {
        List<ApplicationMessageChunk> chunks = new ArrayList<ApplicationMessageChunk>();
        
        ApplicationMessageChunk applicationMessageChunk = ApplicationMessageChunk.newBuilder()
                .setApplicationMessageId(applicationMessageID)
                .setChunkIndex(0)
                .setChunksCount(3)
                .setMessageChunk(ByteString.copyFromUtf8("chunk1"))
                .build();
        chunks.add(applicationMessageChunk);

        applicationMessageChunk = ApplicationMessageChunk.newBuilder()
                .setApplicationMessageId(applicationMessageID)
                .setChunkIndex(1)
                .setChunksCount(3)
                .setMessageChunk(ByteString.copyFromUtf8("chunk2"))
                .build();
        chunks.add(applicationMessageChunk);
        
        applicationMessageChunk = ApplicationMessageChunk.newBuilder()
                .setApplicationMessageId(applicationMessageID)
                .setChunkIndex(2)
                .setChunksCount(3)
                .setMessageChunk(ByteString.copyFromUtf8("chunk3"))
                .build();
        chunks.add(applicationMessageChunk);
        
        return chunks;
    }
}
