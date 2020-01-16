package com.hedera.hcs.sxc.plugin.persistence.db;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

public class HibernateUtil {
    private static StandardServiceRegistry registry;

    public static Session getHibernateSession() {
      // Create registry
      registry = new StandardServiceRegistryBuilder().configure().build();

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