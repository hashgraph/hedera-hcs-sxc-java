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
