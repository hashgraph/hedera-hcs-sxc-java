package com.hedera.hcslib.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class StringUtils {
    

    public static String byteArrayToString(byte[] byteArray){
        return new String(byteArray, StandardCharsets.UTF_8);
    }
    
    public static  byte[] stringToByteArray(String s){
       return s.getBytes(StandardCharsets.UTF_8);
    }
     
    public static String byteArrayToPrintableArrayString(byte[] byteArray){
            return Arrays.toString(byteArray);
    }
    
    public static byte[] printableArrayStringToByteArray(String s){
      byte[] b=null;
        try {
            b = new ObjectMapper().readValue(s, byte[].class);
        } catch (IOException ex) {
            log.error(ex);
        }
      return  b;        
    }
    
    public static String byteArrayToHexString(byte[] byteArray){
        return Hex.encodeHexString(byteArray);
    }
    
    public static byte[] hexStringToByteArray(String s){
        byte[] b = null;
        try {
            b = Hex.decodeHex(s.toCharArray());
        } catch (DecoderException ex) {
            log.error(ex);
        }
        return b;
    }
 }
