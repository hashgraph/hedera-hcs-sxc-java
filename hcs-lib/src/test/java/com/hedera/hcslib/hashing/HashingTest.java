package com.hedera.hcslib.hashing;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.hedera.hcslib.utils.StringUtils;

public class HashingTest {
    
    public HashingTest() {
    }
    
    final String plaintext = "Welcome to the Hedera Hashgraph Concensus Service";
    final String hexOfHashOfPlainText="f39829c128492f289d15580844141549f395fd90292c708abdd1f786d9ce52fa";

    @Test
    public void testSha() {
        System.out.println("sha");
        byte[] expResult = StringUtils.hexStringToByteArray(hexOfHashOfPlainText);
        byte[] result = Hashing.sha(plaintext);
        assertArrayEquals(expResult, result);
    }

    @Test
    public void testMatchSHA() {
        byte[] sha1 = StringUtils.hexStringToByteArray(hexOfHashOfPlainText);
        byte[] sha2 = Hashing.sha(plaintext);
        boolean expResult = true;
        boolean result = Hashing.matchSHA(sha1, sha2);
        assertEquals(expResult, result);
    }

    @Test
    public void testVerifySHA() {
        byte[] sha = Hashing.sha(plaintext);
        String _plaintext = this.plaintext;
        boolean expResult = true;
        boolean result = Hashing.verifySHA(sha, _plaintext);
        assertEquals(expResult, result);
    }
    
}
