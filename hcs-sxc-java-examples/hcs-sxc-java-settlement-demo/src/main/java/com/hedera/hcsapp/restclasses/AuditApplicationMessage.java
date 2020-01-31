package com.hedera.hcsapp.restclasses;

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

import com.hedera.hcsapp.AppData;

import lombok.Data;

@Data
public class AuditApplicationMessage {
    private String applicationMessageId;
    private String message;
    private String topicId;
    
    public AuditApplicationMessage(AppData appData) {
        this.topicId = appData.getHCSCore().getTopics().get(appData.getTopicIndex()).getTopic();
    }
}
