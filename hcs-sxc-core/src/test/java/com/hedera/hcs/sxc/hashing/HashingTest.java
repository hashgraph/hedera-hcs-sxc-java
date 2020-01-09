package com.hedera.hcs.sxc.hashing;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.hedera.hcs.sxc.hashing.Hashing;
import com.hedera.hcs.sxc.utils.StringUtils;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class HashingTest {
    
    public HashingTest() {
    }
    
    final String plaintext = "Welcome to the Hedera Hashgraph Concensus Service";
    final String hexOfHashOfPlainText="f39829c128492f289d15580844141549f395fd90292c708abdd1f786d9ce52fa";

    @Test
    public void Sha() {
        log.info("sha");
        byte[] expResult = StringUtils.hexStringToByteArray(hexOfHashOfPlainText);
        byte[] result = Hashing.sha(plaintext);
        assertArrayEquals(expResult, result);
    }

    @Test
    public void matchSHA() {
        byte[] sha1 = StringUtils.hexStringToByteArray(hexOfHashOfPlainText);
        byte[] sha2 = Hashing.sha(plaintext);
        boolean expResult = true;
        boolean result = Hashing.matchSHA(sha1, sha2);
        assertEquals(expResult, result);
    }

    @Test
    public void verifySHA() {
        byte[] sha = Hashing.sha(plaintext);
        String _plaintext = this.plaintext;
        boolean expResult = true;
        boolean result = Hashing.verifySHA(sha, _plaintext);
        assertEquals(expResult, result);
    }
    
}
