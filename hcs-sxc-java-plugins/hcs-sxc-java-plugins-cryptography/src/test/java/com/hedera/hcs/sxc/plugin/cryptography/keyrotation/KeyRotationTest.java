package com.hedera.hcs.sxc.plugin.cryptography.keyrotation;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import javax.crypto.KeyAgreement;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;


public class KeyRotationTest {
    
    public KeyRotationTest() {
    }

    @Test
    public void testCreateCommonSecret() throws Exception {
        KeyRotation keyRotation = new KeyRotation();
        Pair<KeyAgreement, byte[]> initiate = keyRotation.initiate();
        KeyAgreement keyAgreement = initiate.getLeft();
        Pair<byte[], byte[]> bobPubSecret = keyRotation.respond(initiate.getRight());
        byte[] aliceSharedSecret = keyRotation.finalise(bobPubSecret.getLeft(), keyAgreement);
        byte[] bobSharedSecret = bobPubSecret.getRight();
        assertTrue(Arrays.equals(aliceSharedSecret, bobSharedSecret));
    }
}
