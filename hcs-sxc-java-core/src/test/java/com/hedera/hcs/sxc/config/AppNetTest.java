package com.hedera.hcs.sxc.config;

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
