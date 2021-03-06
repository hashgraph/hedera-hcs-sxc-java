package com.hedera.hcs.sxc.commonobjects;



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

import com.hedera.hcs.sxc.proto.ApplicationMessageID;

public class HCSResponse {
    private ApplicationMessageID applicationMessageId;
    private byte[] message;
    
    public ApplicationMessageID getApplicationMessageId() {
        return this.applicationMessageId;
    }
    public void setApplicationMessageID(ApplicationMessageID applicationMessageId) {
        this.applicationMessageId = applicationMessageId;
    }
    public byte[] getMessage() {
        return this.message;
    }
    public void setMessage(byte[] message) {
        this.message = message.clone();
    }
}
