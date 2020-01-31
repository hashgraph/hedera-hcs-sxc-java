package com.hedera.hcsapp;

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

import com.hedera.hashgraph.sdk.consensus.ConsensusTopicId;
import com.hedera.hcs.sxc.HCSCore;
import com.hedera.hcs.sxc.consensus.CreateHCSTopic;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class CreateTopic {

    public static void main(String[] args) throws Exception {
        HCSCore hcsCore = HCSCore.INSTANCE.getInstance();
        // create topics on HCS
        ConsensusTopicId topicId = new CreateHCSTopic(hcsCore)
                .execute();
        log.info(topicId.toString());
    }

}
