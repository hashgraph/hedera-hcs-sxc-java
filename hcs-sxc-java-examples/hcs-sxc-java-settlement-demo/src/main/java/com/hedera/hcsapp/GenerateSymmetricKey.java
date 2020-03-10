package com.hedera.hcsapp;

import com.hedera.hcs.sxc.plugin.encryption.diffiehellman.Encryption;
import com.hedera.hcs.sxc.plugin.encryption.diffiehellman.StringUtils;

public class GenerateSymmetricKey {

    public static void main(String[] args) throws Exception {
        byte[] secretKey = new Encryption().generateSecretKey();
        System.out.println("Private key : " + StringUtils.byteArrayToHexString(secretKey));
    }

}
