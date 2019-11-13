package com.hedera.hcslib.utils;

import com.google.protobuf.ByteString;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ByteUtilTest {
    public ByteUtilTest() {
    }

    /**
     * Test of merge method, of class ByteUtil.
     */
    @Test
    public void testMerge_byteArrArr() {
        String random1 = RandomStringUtils.random(50, true, true);
        String random2 = RandomStringUtils.random(10, true, true);
        byte[] expResult = (random1+random2).getBytes();
        byte[] result = ByteUtil.merge(random1.getBytes(), random2.getBytes());
        Assertions.assertArrayEquals(expResult, result);
    }

    @Test
    public void testMerge_identity() {
        String random1 = RandomStringUtils.random(50, true, true);
        byte[] expResult = (random1).getBytes();
        byte[] result = ByteUtil.merge(random1.getBytes());
        Assertions.assertArrayEquals(expResult, result);
    }
    
    @Test
    public void testMerge_rightEmpty() {
        String random1 = RandomStringUtils.random(50, true, true);
        byte[] expResult = (random1).getBytes();
        byte[] result = ByteUtil.merge(random1.getBytes(), new byte[0]);
        Assertions.assertArrayEquals(expResult, result);
    }
    
    @Test
    public void testMerge_leftEmpty() {
        String random1 = RandomStringUtils.random(50, true, true);
        byte[] expResult = (random1).getBytes();
        byte[] result = ByteUtil.merge(new byte[0], random1.getBytes());
        Assertions.assertArrayEquals(expResult, result);
    }
    
    
    /**
     * Test of merge method, of class ByteUtil.
     */
    @Test
    public void testMerge_ByteStringArr() {
        String random1 = RandomStringUtils.random(50, true, true);
        String random2 = RandomStringUtils.random(10, true, true);
        ByteString bRandom1 =  ByteString.copyFrom(random1.getBytes());
        ByteString bRandom2 =  ByteString.copyFrom(random2.getBytes());
        
        ByteString expResult = bRandom1.concat(bRandom2);
        ByteString result = ByteUtil.merge(bRandom1, bRandom2);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }
    
}
