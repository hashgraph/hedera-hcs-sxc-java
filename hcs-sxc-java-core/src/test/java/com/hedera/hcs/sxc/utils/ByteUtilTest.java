package com.hedera.hcs.sxc.utils;

/*-
 * ‌
 * hcs-sxc-java
 * ​
 * Copyright (C) 2019 - 2020 Hedera Hashgraph, LLC
 * ​
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ‍
 */

import com.google.protobuf.ByteString;
import com.hedera.hcs.sxc.utils.ByteUtil;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ByteUtilTest {

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
