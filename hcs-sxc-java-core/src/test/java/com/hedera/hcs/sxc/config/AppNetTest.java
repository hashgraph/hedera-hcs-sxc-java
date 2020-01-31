package com.hedera.hcs.sxc.config;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.hedera.hcs.sxc.config.AppNet;
import com.hedera.hcs.sxc.config.Topic;

public class AppNetTest extends AbstractConfigTest {

    private  static AppNet appNet = new AppNet();
    
    @Test
    public void gettersAndSetters() throws Exception {
        
        appNet.setEncryptMessages(true);
        assertTrue(appNet.getEncryptMessages());
        appNet.setEncryptMessages(false);
        assertFalse(appNet.getEncryptMessages());
        
        appNet.setSignMessages(true);
        assertTrue(appNet.getSignMessages());
        appNet.setSignMessages(false);
        assertFalse(appNet.getSignMessages());

        appNet.setRotateKeys(true);
        assertTrue(appNet.getRotateKeys());
        appNet.setRotateKeys(false);
        assertFalse(appNet.getRotateKeys());
        
        appNet.setRotateKeyFrequency(2);
        assertEquals(2, appNet.getRotateKeyFrequency());
        appNet.setRotateKeyFrequency(5);
        assertEquals(5, appNet.getRotateKeyFrequency());
        
        List<Topic> topics = new ArrayList<Topic>();
        
        Topic topic = new Topic();
        topic.setTopic("0.0.3");
        Topic topic2 = new Topic();
        topic2.setTopic("0.0.6");
        
        topics.add(topic);
        topics.add(topic2);
        
        appNet.setTopics(topics);
        assertEquals(2, appNet.getTopics().size());
        
//        assertTopicId(0, 0, 3, appNet.getTopicIds().get(0));
//        assertTopicId(0, 0, 6, appNet.getTopicIds().get(1));
    }

}
