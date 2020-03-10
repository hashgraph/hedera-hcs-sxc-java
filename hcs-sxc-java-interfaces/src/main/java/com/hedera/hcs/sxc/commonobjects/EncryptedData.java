package com.hedera.hcs.sxc.commonobjects;

import lombok.Data;

@Data
public final class EncryptedData {
    private byte[] encryptedData;
    private byte[] random;
}
