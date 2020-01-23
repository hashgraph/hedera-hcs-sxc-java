package com.hedera.hcs.sxc.cryptography;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import com.hedera.hcs.sxc.cryptography.KeyRotation;

public class KeyRotationTest {
    
    public KeyRotationTest() {
    }

    @Test
    public void createCommonSecret() {
        KeyRotation keyRotation = new KeyRotation();
        byte[] alicePublic = keyRotation.aliceFirst();
        Pair<byte[], byte[]> bobPubSecret = KeyRotation.bobGenFromAlice(alicePublic);
        byte[] aliceSharedSecret = keyRotation.aliceFinish(bobPubSecret.getLeft());
        byte[] bobSharedSecret = bobPubSecret.getRight();
        assertTrue(Arrays.equals(aliceSharedSecret, bobSharedSecret));
    }
}
