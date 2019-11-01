package com.hedera.hcslib.messages;

import com.google.common.base.Preconditions;
import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.HederaNetworkException;
import com.hedera.hashgraph.sdk.account.CryptoTransferTransaction;
import com.hedera.hcslib.cryptography.Cryptography;
import com.hedera.hcslib.cryptography.KeyRotation;
import com.hedera.hcslib.utils.StringUtils;
import com.hedera.hcslib.hashing.Hashing;
import com.hedera.hcslib.signing.Signing;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

import java.security.PrivateKey;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/**
 * An HCSMessageOld is 
 * 
 */
public class HCSMessageOld implements Serializable {

    private static final Logger LOG = Logger.getLogger(HCSMessageOld.class.getName());
    
    public @Nullable String from; // set this when you know who this message is from
    public @Nullable int receivedPartsSize; // used when not all parts available and message constructed incrementaly. 
    public HCSMessagePayloadOld payload; // this is where the cleartext or encrypted message resides. 
    public HCSMessageChunkOld[] parts;
    public int topic;
    public int messageNo; //FIXME: Needs to be a unique number. Use epoch or UUID.most significant bits
    public @Nullable byte noOfParts;  
    public boolean isComplete; // is true if all parts available
    public long timestamp;  // last/max chronological timestamp received - 
    // extra business logic fields
    public @Nullable String extStatus; // optional status flag to record business status flags
    public @Nullable Integer threadNo;
    public @Nullable String messageType;
    public @Nullable String[] arguments;
    
    
    /**
     * Creates an encrypted HCSMessage ready to be sent to the HH Network.
     * <p>Use {@link #getParts()} to retrieve all message parts {@link HCSMessageChunkOld} 
     * of the constructed message. Under the hood, the method:</p>
     * <ol>
     * <li>  encrypts {@param message} using {@param sharedSecret} generated 
     * in {@link KeyRotation} </li>
     * <li>  places  the encrypted {@param message} into a {@link HCSMessagePayloadOld}</li>
     * <li>  hashes {@param message}  and puts hash into the {@link HCSMessagePayloadOld}</li>
     * <li>  signs encrypted in (1) message using {@param privateKey}  and puts 
     * signature into the {@link HCSMessagePayloadOld}</li>
     * <li>  and finally breaks the concatenated contents of {@link HCSMessagePayloadOld}
     *    into a list of {@link HCSMessagePart} </li>
     * </ol>
     * @param sharedSecret the secret used to decrypt
     * @param topic 
     * @param messageNo a unique identifier 
     * @param message the cleartext message
     * @param privateKey the key used to sign the message and to identify recipient
     * @return the encrypted HCSMessageOld object containing a list of MessageParts
     */
    public static HCSMessageOld prepareMessage(byte[] sharedSecret, int topic, int messageNo, String message, PrivateKey  privateKey){
        return new HCSMessageOld(sharedSecret, topic, messageNo, message, privateKey);
    }
    
    /**
     * Similar to {@link #prepareMessage(byte[], int, int, java.lang.String, java.security.PrivateKey)}
     * but prepares an un-encrypted message
     * @param topic
     * @param messageNo
     * @param message
     * @param privateKey
     * @return 
     */
    public static HCSMessageOld prepareCleartext(int topic, int messageNo, String message, PrivateKey  privateKey){
        return new HCSMessageOld(null, topic, messageNo, message, privateKey);
    }
    
    /**
     * Constructs and decrypts an HCSMessageOld when all encrypted parts
 {@link HCSMessagePart } are available. The decrypted messages is accessible
     * from the {@link HCSMessagePayloadOld} object; access via {@link #this.payload.message}
     * Only essential fields of the object are initialised after construction.
     * @param sharedSecret
     * @param parts
     * @return the decrypted HCSMessageOld where the cleartext is under {@link #this.payload.message}
     */
    public static HCSMessageOld processMessage(byte[] sharedSecret,  List<HCSMessageChunkOld> parts ){
        return new HCSMessageOld(sharedSecret, parts);
    }
    
    /**
     * Assemble a message and don't attempt decrypting. Use this when message 
     * was not encrypted and generated  by {@link #prepareCleartext(int, int, java.lang.String, java.security.PrivateKey) }
     * @param sharedSecret
     * @param parts
     * @return 
     */
    public static HCSMessageOld processMessageSkipDecrypt(byte[] sharedSecret,  List<HCSMessageChunkOld> parts ){
        return new HCSMessageOld(null, parts);
    }
    
    
    public  void sendMessage(CryptoTransferTransaction t){
        Arrays.asList(this.parts).forEach(chunk->{
            t.setMemo(chunk.payload);
            try {
                t.execute();
            } catch (HederaException ex) {
                LOG.log(Level.SEVERE, null, ex);
            } catch (HederaNetworkException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
            
        });
    }
    
    
    //construct a message from encrypted parts solutionSDK.createMessage solutionSDK.decryptMessage
    private HCSMessageOld(byte[] sharedSecret, List<HCSMessageChunkOld> parts){
        Preconditions.checkArgument(
                parts.size() > 0
                , "There needs to be a list one part for a valid message"); 
        Preconditions.checkArgument(
                parts.stream().map(m-> m.topic).distinct().count() == 1
                , "All parts must have the same topic number"); 
        Preconditions.checkArgument(
                parts.stream().map(m-> m.messageNo).distinct().count() == 1
                , "All parts must have the same message number"); 
        Preconditions.checkArgument(
                parts.stream().map(m-> m.noOfParts).distinct().count() == 1
                , "All parts must have the same noOfParts mumber"); 
        
        
        this.topic = parts.get(0).topic;
        this.messageNo = parts.get(0).messageNo;
        this.noOfParts = parts.get(0).noOfParts;
        
        Preconditions.checkArgument(
                parts.size() == noOfParts
                , "Cannot construct message due to missing parts");
      
        this.receivedPartsSize = noOfParts;
        this.parts = new HCSMessageChunkOld[this.noOfParts];
        parts.forEach(p -> this.parts[p.partNo - 1] = p); // sort the parts
        StringBuilder b =  new StringBuilder();
        //solutionSDK.extractMessageContent - has not its own SDK call but is part of 
        Arrays.asList(this.parts).forEach(o -> {
             b.append(
                      o.payload
             );
        });
        
        this.payload = new HCSMessagePayloadOld(b.toString());
        
        
        if (sharedSecret!=null){
            try {
                //solutionSDK.decryptMessage
                this.payload.message = StringUtils.byteArrayToString(
                        Cryptography.decrypt(
                                sharedSecret, 
                                StringUtils.stringToByteArray(
                                        this.payload.message
                                )
                        )
                );
                //solutionSDK.checkHashMatch   
                Preconditions.checkArgument(
                    Arrays.equals(this.payload.hashOfUnencryptedMessage, Hashing.sha(this.payload.message) )
                    , "The hash of the unecrupted message must match"); 

       
            } catch (Exception ex) {
                Logger.getLogger(HCSMessageOld.class.getName()).log(Level.SEVERE, null, ex);
            } 
        
        } else {
            //this.payload.message = b.toString();
        }
    
        this.isComplete = true;
        
    }
    
    
    //solutionSDK.createMessage solutionSDK.encryptMessage solutionSDK.signMessage solutionSDK.hashMessage
    private HCSMessageOld(byte[] sharedSecret, int topic, int messageNo, String message, PrivateKey  privateKey){
        try {
            this.topic = topic;
            this.messageNo = messageNo;
            this.payload = new HCSMessagePayloadOld();
            this.payload.message = message;
       
            
            // payload can not be 86 because AES is padding things for each 16 byte blocks. 
            // we use messages up to lenght 79 which give up to 80 byte encrypted results. 
            // It leaves us with 20 byte headers.  
            
            //solutionSDK.hashMessage
            this.payload.hashOfUnencryptedMessage =  Hashing.sha(this.payload.message);
            //solutionSDK.signMessage
            this.payload.signatureOfEncryptedMessage = Signing.sign(this.payload.message.getBytes(StandardCharsets.ISO_8859_1), privateKey);
            
            
            if(sharedSecret!=null){
                //solutionSDK.encryptMessage
                byte[] encrypt = Cryptography.encrypt(sharedSecret, this.payload.message);
                this.payload.message = StringUtils.byteArrayToString(encrypt);
                //solutionSDK.signMessage  (think it's redundant)
                this.payload.signatureOfEncryptedMessage = Signing.sign(this.payload.message.getBytes(StandardCharsets.ISO_8859_1), privateKey);
            }
    
            String embeddable = this.payload.getEmbeddable();
    
            this.noOfParts = (byte)((embeddable.length() / 79 ) + (embeddable.length() % 79 ==0?0:1));
            this.parts = new HCSMessageChunkOld[this.noOfParts];

            for (byte part = 0;  part < this.noOfParts; part++){
                int beginIndex = part * 79;
                int endIndex = beginIndex + 79;

                String bodyPart = embeddable.substring(beginIndex, (endIndex>embeddable.length()?embeddable.length():endIndex));
                parts[part] = (new HCSMessageChunkOld(topic, messageNo, (byte)(part+1) , this.noOfParts, bodyPart));
            }
            this.receivedPartsSize = this.noOfParts;
            this.isComplete = true;
        } catch (Exception ex) {
            Logger.getLogger(HCSMessageOld.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    
    

   //solutionSDK.createMessage - use this when you want add parts and decrypt later
    public HCSMessageOld(int topic){
        this.isComplete = false;
        this.topic = topic;
        this.receivedPartsSize = 0;
    }
    
    public boolean addPart(HCSMessageChunkOld p ){
        Preconditions.checkArgument(
                p.topic == this.topic
                , "Message is for a different topic");
        
        if(this.parts == null){
            this.parts = new HCSMessageChunkOld[p.noOfParts];
            this.messageNo = p.messageNo;
            this.noOfParts = p.noOfParts;
        }
        
        Preconditions.checkArgument(
                p.messageNo == this.messageNo
                , "MessageNo needs to be the same");
        Preconditions.checkArgument(
                p.noOfParts == this.noOfParts
                , "Number of parts need to be the same");
        
        
        this.parts[p.partNo - 1] = p;
        this.receivedPartsSize +=1;
        
        if (this.receivedPartsSize == this.noOfParts) {
            this.isComplete = true;
            this.timestamp = p.timestampSeconds;
            StringBuilder b =  new StringBuilder();
            Arrays.asList(this.parts).forEach(o -> {
                b.append(o.payload);
            });
            this.payload = new HCSMessagePayloadOld (b.toString());
            return this.isComplete;
        } else {
            return this.isComplete;
        }
    }

    
    public boolean decrypt (byte[] sharedSecret){
        StringBuilder b =  new StringBuilder();
        Arrays.asList(this.parts).forEach(o -> {
                b.append(o.payload);
        });
        this.payload = new HCSMessagePayloadOld (b.toString());
        try {
                this.payload.message = StringUtils.byteArrayToString(
                        Cryptography.decrypt(
                                sharedSecret, 
                                StringUtils.stringToByteArray(
                                        this.payload.message
                                )
                        )
                );
            } catch (Exception ex) {
                //Logger.getLogger(HCSMessageOld.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            } 
        return true;
    }
    
    // Getters Setters
    public List<HCSMessageChunkOld> getParts(){
        Preconditions.checkState(isComplete, "The message is partial");
        
        return Arrays.asList(this.parts);
    }

    public int getMessageNo() {
        return messageNo;
    }

    public String getFrom() {
        return from;
    }

    public HCSMessagePayloadOld getPayload() {
        return payload;
    }

    public String getExtStatus() {
        return extStatus;
    }
    
    public boolean isIsComplete() {
        return isComplete;
    }

    public byte getNoOfParts() {
        return noOfParts;
    }

    public int getReceivedPartsSize() {
        return receivedPartsSize;
    }

    public int getTopic() {
        return topic;
    }

    public long getTimestamp() {
        return timestamp;
    }


    
    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }

    
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final HCSMessageOld other = (HCSMessageOld) obj;
        if (this.receivedPartsSize != other.receivedPartsSize) {
            return false;
        }
        if (this.topic != other.topic) {
            return false;
        }
        if (this.messageNo != other.messageNo) {
            return false;
        }
        if (this.noOfParts != other.noOfParts) {
            return false;
        }
        if (this.isComplete != other.isComplete) {
            return false;
        }
        if (this.timestamp != other.timestamp) {
            return false;
        }
        if (!Objects.equals(this.payload, other.payload)) {
            return false;
        }
        
        // don't compare the parts.
        
        return true;
    }
}
