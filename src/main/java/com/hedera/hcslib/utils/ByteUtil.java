package com.hedera.hcslib.utils;

import lombok.extern.log4j.Log4j2;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


@Log4j2
public class ByteUtil {
    
    public static byte[] merge(byte[] ... byteArrays){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        try {
            for(byte[] a: byteArrays ){
                outputStream.write( a );
            }
        } catch (IOException e){
            LOG.log(Level.ALL, "Failed to merge");
        }
        return outputStream.toByteArray( );
    }
    
    private static final Logger LOG = Logger.getLogger(ByteUtil.class.getName());
    
 }
