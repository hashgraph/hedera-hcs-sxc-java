package com.hedera.hcslib.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import com.fasterxml.jackson.databind.ObjectMapper;

public class StringUtils {
    

    public static String byteArrayToString(byte[] byteArray){
        return new String(byteArray, StandardCharsets.ISO_8859_1);
    }
    
    public static  byte[] stringToByteArray(String s){
       return s.getBytes(StandardCharsets.ISO_8859_1);
    }
     
    public static String byteArrayToPrintableArrayString(byte[] byteArray){
            return Arrays.toString(byteArray);
    }
    
    public static byte[] printableArrayStringToByteArray(String s){
      byte[] b=null;
        try {
            b = new ObjectMapper().readValue(s, byte[].class);
        } catch (IOException ex) {
            Logger.getLogger(StringUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
      return  b;        
    }
    
    public static String byteArrayToHexString(byte[] byteArray){
        return   Hex.encodeHexString(byteArray);
    }
    
    public static byte[] hexStringToByteArray(String s){
        byte[] b = null;
        try {
            b = Hex.decodeHex(s.toCharArray());
        } catch (DecoderException ex) {
            Logger.getLogger(StringUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return b;
    }
 }
