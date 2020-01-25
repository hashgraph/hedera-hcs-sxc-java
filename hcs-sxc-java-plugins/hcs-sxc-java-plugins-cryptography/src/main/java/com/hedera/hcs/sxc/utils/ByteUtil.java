package com.hedera.hcs.sxc.utils;

import com.google.protobuf.ByteString;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import lombok.extern.log4j.Log4j2;

@Log4j2
public final class ByteUtil {
    
    public static byte[] merge(byte[] ... byteArrays){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        try {
            for(byte[] a: byteArrays ){
                outputStream.write( a );
            }
        } catch (IOException e){
            log.debug("Failed to merge");
        }
        return outputStream.toByteArray( );
    }
    
    public static ByteString merge(ByteString ... byteStrings){
        ByteString a = ByteString.EMPTY;
       
            for(ByteString s: byteStrings ){
                a = a.concat(s);
            }
        
        return a;
    }
 }
