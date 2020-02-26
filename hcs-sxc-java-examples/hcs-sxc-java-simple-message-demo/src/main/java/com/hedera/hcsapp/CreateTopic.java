package main.java.com.hedera.hcsapp;

import com.hedera.hashgraph.sdk.account.AccountId;

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
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hcs.sxc.HCSCore;
import com.hedera.hcs.sxc.consensus.CreateHCSTopic;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class CreateTopic {

    public static void main(String[] args) throws Exception {
        Dotenv dotEnv = Dotenv.load();
        Ed25519PrivateKey operatorKey = Ed25519PrivateKey.fromString(dotEnv.get("OPERATOR_KEY"));
        AccountId operatorId = AccountId.fromString(dotEnv.get("OPERATOR_ID"));

        HCSCore hcsCore = HCSCore.INSTANCE.singletonInstance("0")
                .withOperatorAccountId(operatorId)
                .withOperatorKey(operatorKey);
        
        // create topics on HCS
        CreateHCSTopic createHCSTopic = new CreateHCSTopic(hcsCore);
        ConsensusTopicId topicId = createHCSTopic.execute();
        log.debug(topicId.topic);
    }

}
