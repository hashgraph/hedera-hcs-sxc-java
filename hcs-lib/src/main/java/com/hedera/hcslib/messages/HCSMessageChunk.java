package com.hedera.hcslib.messages;
import com.google.common.base.Preconditions;
import com.hedera.hcslib.utils.ByteUtil;
import com.hedera.hcslib.utils.StringUtils;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import javax.annotation.Nonnull;

/**
 * 
 * A message chunk is a part of a #{@link HCSMessage}
 * A list of such chunks is kept in the message object
 */
public  class HCSMessageChunk implements Serializable{
        //header
        public @Nonnull String header; 
        public @Nonnull int topic;
        public @Nonnull int messageNo;
        public @Nonnull byte partNo;
        public @Nonnull byte noOfParts;
        public @Nonnull long timestampSeconds;
        //body
        public @Nonnull String payload;
        
        public byte[] serialized;// buffer that hold above fields
        
        /**
         * Construct a chunk, when message is received, by parsing a byte array. The constructed chunk has
         * all non-null fields initialized. Use this constructor {@link  #onMessage}
         * of your Queue Or Topic message call back and add each constructed chunk
         * to an HCSMessage using {@link HCSMessage#addPart(com.hedera.hcslib.messages.HCSMessageChunk) }
         * to construct an HCSMessage incrementally while parts become available.
         * @param serialized 
         */
        public HCSMessageChunk(byte[] serialized) {
            Preconditions.checkArgument(serialized.length < 100, "buffler length: %d too long", serialized.length); 
            ByteBuffer wrap = ByteBuffer.wrap(serialized);
            this.serialized = serialized;
            byte[] _3ByteString = ByteBuffer.allocate(3).array();
            wrap.get(_3ByteString);
            this.header = new String(_3ByteString);
            byte[] _2ByteInt = ByteBuffer.allocate(2).array();
            wrap.get(_2ByteInt);
            this.topic = ((_2ByteInt[1] & 0xff) << 8) | (_2ByteInt[0] & 0xff);
            this.messageNo =  wrap.getInt();
            this.partNo = wrap.get();
            this.noOfParts = wrap.get();
            byte[] _86ByteString = ByteBuffer.allocate(86).array();
            wrap.get(_86ByteString);
            this.payload = StringUtils.byteArrayToString(trim(_86ByteString));
        }
        
        /**
         * Construct a chunk when message is to be disassembled.
         * @param topic
         * @param messageNo
         * @param chunkNo
         * @param totalChunks
         * @param payload
         * @throws UnsupportedEncodingException 
         */
        HCSMessageChunk (int topic, int messageNo, byte chunkNo, byte totalChunks, String payload) throws UnsupportedEncodingException{
            this.header = "HCS";
            this.topic  = topic;
            this.messageNo = messageNo;
            this.partNo = chunkNo;
            this.noOfParts = totalChunks;
            this.payload = payload;
            
            serialized  = ByteUtil.merge(
                ByteBuffer.allocate(3).put(header.getBytes(StandardCharsets.ISO_8859_1)).array(),
                ByteBuffer.allocate(2).put(BigInteger.valueOf(topic).toByteArray()).array(), // Topic,  1 - 65535
                ByteBuffer.allocate(4).putInt(messageNo).array(), // Topic,  1 - 65535
                ByteBuffer.allocate(1).put(chunkNo).array(),
                ByteBuffer.allocate(1).put(totalChunks).array(),
                ByteBuffer.allocate(86).put(payload.getBytes(StandardCharsets.ISO_8859_1)).array()
            );
        }

        @Override
        public int hashCode() {
            int hash = 5;
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
            final HCSMessageChunk other = (HCSMessageChunk) obj;
            if (this.topic != other.topic) {
                return false;
            }
            if (this.messageNo != other.messageNo) {
                return false;
            }
            if (this.partNo != other.partNo) {
                return false;
            }
            if (this.noOfParts != other.noOfParts) {
                return false;
            }
            if (!Objects.equals(this.header, other.header)) {
                return false;
            }
            if (!Arrays.equals(this.serialized, other.serialized)) {
                return false;
            }
            if (!Arrays.equals(this.payload.getBytes(), other.payload.getBytes())) {
                return false;
            }
            if (!Objects.equals(this.payload, other.payload)) {
                return false;
            }
            
            return true;
        }
        
        //TODO exhaustive test regarding EOF character
        private static byte[] trim(byte[] bytes) {
            int i = bytes.length - 1;
            while (i >= 0 && bytes[i] == 0)
            {
                --i;
            }
            return Arrays.copyOf(bytes, i + 1);
        }
        
    }
