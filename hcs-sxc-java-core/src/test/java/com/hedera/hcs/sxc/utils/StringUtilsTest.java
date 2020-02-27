package com.hedera.hcs.sxc.utils;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.Arrays;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;

public class StringUtilsTest {
    @Test
    public void testByteArrayToString() {
        String random1 = RandomStringUtils.random(50, true, true);
        byte[] randomBytes = random1.getBytes();
        assertEquals(random1, StringUtils.byteArrayToString(randomBytes));
    }
    @Test
    public void testStringToByteArray() {
        String random1 = RandomStringUtils.random(50, true, true);
        byte[] randomBytes = random1.getBytes();
        assertArrayEquals(randomBytes, StringUtils.stringToByteArray(random1));
    }
    @Test
    public void testByteArrayToPrintableArrayString() {
        String random1 = RandomStringUtils.random(50, true, true);
        byte[] randomBytes = random1.getBytes();
        String printableArray = Arrays.toString(randomBytes);
        assertEquals(printableArray, StringUtils.byteArrayToPrintableArrayString(randomBytes));
    }
    @Test
    public void testPrintableArrayStringToByteArray() {
        String random1 = RandomStringUtils.random(50, true, true);
        byte[] randomBytes = random1.getBytes();
        String printableArray = Arrays.toString(randomBytes);
        assertArrayEquals(randomBytes, StringUtils.printableArrayStringToByteArray(printableArray));
    }
    @Test
    public void testByteArrayToHexString() {
        String random1 = RandomStringUtils.random(50, true, true);
        byte[] randomBytes = random1.getBytes();
        String random1Hex = Hex.encodeHexString(randomBytes);
        assertEquals(random1Hex, StringUtils.byteArrayToHexString(randomBytes));
    }
    @Test
    public void testHexStringToByteArray() {
        String random1 = RandomStringUtils.random(50, true, true);
        byte[] randomBytes = random1.getBytes();
        String random1Hex = Hex.encodeHexString(randomBytes);
        assertArrayEquals(randomBytes, StringUtils.hexStringToByteArray(random1Hex));
    }
}
