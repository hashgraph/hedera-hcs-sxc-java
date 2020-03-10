package com.hedera.hcs.sxc.plugin.persistence.entities;

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


import com.hedera.hcs.sxc.interfaces.SxcApplicationMessageInterface;
import java.io.Serializable;
import java.time.Instant;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import lombok.Data;
import javax.persistence.Column;

@Entity
@Data
@Table(name = "ApplicationMessages")
public class HCSApplicationMessage implements  SxcApplicationMessageInterface, Serializable{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    @Id
    private String applicationMessageId;
    @Lob
    private byte[] applicationMessage;
    
    @Column(columnDefinition = "TIMESTAMP(9)")
    private Instant lastChronoPartConsensusTimestamp;
//    private long lastChronoPartShardNum;
//    private long lastChronoPartRealmNum;
//    private long lastChronoPartRealmTopicNum;
    private long lastChronoPartSequenceNum;
    private String lastChronoPartRunningHashHEX;
    
    @Override
    public String getApplicationMessageId(){
        return this.applicationMessageId;
    }
    
    @Override
    public byte[] getApplicationMessage(){
        return this.applicationMessage;
    }
    
    @Override
    public Instant getLastChronoPartConsensusTimestamp () {
        return this.lastChronoPartConsensusTimestamp;
    }
    
//    @Override
//    public long getLastChronoPartShardNum () {
//        return this.lastChronoPartShardNum;
//    }
//    @Override
//    public long getLastChronoPartRealmNum(){
//        return this.lastChronoPartRealmNum;
//    }
    @Override
    public long getLastChronoPartSequenceNum(){
        return this.lastChronoPartSequenceNum;
    }
    @Override
    public String getLastChronoPartRunningHashHEX(){
        return this.lastChronoPartRunningHashHEX;
    }
}
