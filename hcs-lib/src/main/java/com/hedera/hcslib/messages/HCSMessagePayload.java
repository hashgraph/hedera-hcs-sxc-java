/*
 *  Copyirght hash-hash.info
 */
package com.hedera.hcslib.messages;

import com.google.common.base.Preconditions;
import com.hedera.hcslib.utils.StringUtils;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;


public class HCSMessagePayload implements Serializable {

    static int headerSize = 32 + 256;
    public String message = null;
    public byte[] hashOfUnencryptedMessage = null;
    public byte[] signatureOfEncryptedMessage = null;

    public HCSMessagePayload() {
    }

    public HCSMessagePayload(String embeddable) {
        Preconditions.checkArgument(
                embeddable.length() > 32 + 256,
                 "The embedable minimum size is not met");
        this.hashOfUnencryptedMessage = StringUtils.stringToByteArray(embeddable.substring(0, 32));
        this.signatureOfEncryptedMessage = StringUtils.stringToByteArray(embeddable.substring(32, 32 + 256));
        this.message = embeddable.substring(32 + 256, embeddable.length());
    }

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
        final HCSMessagePayload other = (HCSMessagePayload) obj;
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
