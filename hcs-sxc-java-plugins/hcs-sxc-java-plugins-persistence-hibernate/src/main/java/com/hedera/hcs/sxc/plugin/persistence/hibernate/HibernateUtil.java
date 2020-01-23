package com.hedera.hcs.sxc.plugin.persistence.hibernate;

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