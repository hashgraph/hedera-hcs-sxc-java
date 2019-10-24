package com.hedera.hcslib.cryptography;

import java.util.Arrays;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;


public class KeyRotationTest {
    
    public KeyRotationTest() {
    }

    @Test
    public void testCreateCommonSecret() {
        KeyRotation keyRotation = new KeyRotation();
        byte[] alicePublic = keyRotation.aliceFirst();
        Pair<byte[], byte[]> bobPubSecret = KeyRotation.bobGenFromAlice(alicePublic);
        byte[] aliceSharedSecret = keyRotation.aliceFinish(bobPubSecret.getLeft());
        byte[] bobSharedSecret = bobPubSecret.getRight();
        Assert.assertTrue(Arrays.equals(aliceSharedSecret, bobSharedSecret));
    }
}
