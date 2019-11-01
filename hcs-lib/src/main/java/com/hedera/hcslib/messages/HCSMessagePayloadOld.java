/*
 *  Copyirght hash-hash.info
 */
package com.hedera.hcslib.messages;

import com.google.common.base.Preconditions;
import com.hedera.hcslib.utils.StringUtils;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

/**
 * Payload object which is composed in an HCSMessage. The object
 * is used internally only. TODO: add as a private static inner class. 
 * to HCSMessage. 
 * 
 */
public class HCSMessagePayloadOld implements Serializable {

    static int headerSize = 32 + 256;
    public String message = null; // this it the message. it may be encrypted or decrypted.
    public byte[] hashOfUnencryptedMessage = null;
    public byte[] signatureOfEncryptedMessage = null;

    public HCSMessagePayloadOld() {
    }

    /**
     * Construct payload from a serialised string. The object has all it's fields
     * initialised after construction. 
     * @param embeddable 
     */
    public HCSMessagePayloadOld(String embeddable) {
        Preconditions.checkArgument(
                embeddable.length() > headerSize,
                 "The embedable minimum size is not met");
        this.hashOfUnencryptedMessage = StringUtils.stringToByteArray(embeddable.substring(0, 32));
        this.signatureOfEncryptedMessage = StringUtils.stringToByteArray(embeddable.substring(32, 32 + 256));
        this.message = embeddable.substring(headerSize, embeddable.length());
    }

    /**
     * Serialize the objects fields into an embeddeable string. It is this string
     * that is broken up in and used in chunks {@link HCSMessageChunk}
     * @return 
     */
    public String getEmbeddable() {
        return StringUtils.byteArrayToString(this.hashOfUnencryptedMessage)
                + StringUtils.byteArrayToString(this.signatureOfEncryptedMessage)
                + this.message;
    }

    public String getMessage() {
        return message;
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
        final HCSMessagePayloadOld other = (HCSMessagePayloadOld) obj;
        if (!Objects.equals(this.message, other.message)) {
            return false;
        }
        if (!Arrays.equals(this.hashOfUnencryptedMessage, other.hashOfUnencryptedMessage)) {
            return false;
        }
        if (!Arrays.equals(this.signatureOfEncryptedMessage, other.signatureOfEncryptedMessage)) {
            return false;
        }
        return true;
    }

}
