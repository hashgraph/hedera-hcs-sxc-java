package com.hedera.hcslib.cryptography;
import java.util.Arrays;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class) // don't boot server for tests
@ActiveProfiles("test")
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
