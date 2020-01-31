package com.hedera.hcs.sxc.plugin.persistence.hibernate;

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

import java.util.Map;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {
    private static StandardServiceRegistry registry;

    public static Session getHibernateSession(Map<String, String> hibernateProperties) {
        // get configuration from config
        Configuration configuration = new Configuration();
        hibernateProperties.forEach((key,value) -> configuration.setProperty(key, value)); 

        // Create registry
        registry = new StandardServiceRegistryBuilder()
                .configure()
                .applySettings(configuration.getProperties())
                .build();
        
        // Create MetadataSources
        MetadataSources sources = new MetadataSources(registry);
        
        // Create Metadata
        Metadata metadata = sources.getMetadataBuilder().build();
        
        // Create SessionFactory
        final SessionFactory sessionFactory = metadata.getSessionFactoryBuilder().build();
        final Session session = sessionFactory.openSession();
        
        return session;
    }

    public static void shutdown() {
        if (registry != null) {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }
}
