package com.hedera.hcs.sxc.plugin.encryption.diffiehellman;

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

    public static String stringToByteArray(byte[] originalMessage) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
 }
