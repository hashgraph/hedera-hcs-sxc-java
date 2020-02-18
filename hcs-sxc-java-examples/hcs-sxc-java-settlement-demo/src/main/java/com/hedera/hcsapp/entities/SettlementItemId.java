package com.hedera.hcsapp.entities;

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

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.Data;

@Data
@Embeddable
public class SettlementItemId implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Column(name = "settled_thread_id")
    private String settledThreadId;
 
    @Column(name = "thread_id")
    private String threadId;
 
    public SettlementItemId() {
    }
 
    public SettlementItemId(String settlementThreadId, String threadId) {
        this.settledThreadId = settlementThreadId;
        this.threadId = threadId;
    }
}
