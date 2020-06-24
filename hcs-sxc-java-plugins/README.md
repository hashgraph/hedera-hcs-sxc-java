# Plugin architecture

HCS SXC allows you to plug your own implementations fin or persistence, cryptography, and mirror feedback handling. This will allow you do use your own database vendor or write your own encryption algorithms.   Plugins are maven artefacts,  which you include in your project's `pom.xml`.  These plugins implement common `interfaces` that are found in the `hcs-sxc-java-interfaces` module. 

## Choosing which plugins to use

### Persistence

To choose whether to use the `in memory` or `in database` persistence plug in, include either the first or second dependency below in your application's `pom.xml`

for in memory

```
<groupId>com.hedera</groupId>
<artifactId>hcs-sxc-java-plugins-persistence-in-memory</artifactId>
<version>0.0.3-SNAPSHOT</version>
```

for in database

```
<groupId>com.hedera</groupId>
<artifactId>hcs-sxc-java-plugins-persistence-hibernate</artifactId>
<version>0.0.3-SNAPSHOT</version>
```

To use this plugin you must add the following section to your `<poject-root-folder>/.config/.congig.yaml>` to define the database you want to connect to. 

```yaml
coreHibernate:
  # these values will be loaded as a map, add or remove properties as necessary
  # {appId} will be replaced with the application instance id autmatically 
  hibernate.connection.driver_class: "org.h2.Driver"
  hibernate.connection.url: "jdbc:h2:./h2data/libdb-{appid}"
  hibernate.connection.username: "admin"
  hibernate.connection.password: ""
  hibernate.default_schema: "PUBLIC"
  hibernate.connection.pool_size: 5
  hibernate.dialect.H2Dialect: "org.hibernate.dialect.H2Dialect"
  hibernate.cache.provider_class: "org.hibernate.cache.internal.NoCacheProvider"
  hibernate.show_sql: "false"
  hibernate.hbm2ddl.auto: "update"
```

Modify this example to use your connection values, however keep the `{appid}`  string unmodified.

The list of configuration entries is variable, you may add or remove entries as necessary for your particular database. Also, if `{appid}` is found in any of the values, it will be swapped at run time for the id of the instance of the application being run.

Further, to ensure the appropriate database vendors' dependencies are available when compiling, the `hcs-sxc-java-plugins-persistence-hibernate` project makes use of profiles in its `pom.xml`. The default profile is `h2` which also downloads the `h2` database server - in any other setting you would need to load and run the respective database server and amend the configuration entries in the yaml above. 

For example:

```xml
   <profiles>
        <profile>
            <id>mysql</id>                
            <dependencies>
                <dependency>
                    <groupId>mysql</groupId>
                    <artifactId>mysql-connector-java</artifactId>
                    <version>5.1.9</version>
                </dependency>
            </dependencies>
        </profile>
        <profile>
            <id>postgres</id>
            <dependencies>
                <dependency>
                    <groupId>mysql</groupId>
                    <artifactId>postgres-connector-java</artifactId>
                    <version>5.1.9</version>
                </dependency>
            </dependencies>
        </profile>
        <profile>
            <id>h2</id>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
            <dependencies>
                <dependency>
                <groupId>com.h2database</groupId>
                <artifactId>h2</artifactId>
                <version>1.4.200</version>
                </dependency>
            </dependencies>
        </profile>
    </profiles>
```

to build this hibernate component with the appropriate vendor's dependencies, add `-P profileName` to your maven install command.

Example:

```xml
mvnw clean install -Pdocker -Ppostgres
```

### Mirror subscription method

To choose whether to use the `direct` or `hcs-sxc-java-relay+activeMQ` subscription method, include either the first or second dependency below in your application's `pom.xml`

for **direct subscriptions** where the app downloads directly from a mirror

```
<groupId>com.hedera</groupId>
<artifactId>hcs-sxc-java-plugins-mirror-direct</artifactId>
<version>0.0.3-SNAPSHOT</version>
```

for **Artemis Message Queue** where mirror messages are downloaded to a message queue first and are then relied to the main program 

```
<groupId>com.hedera</groupId>
<artifactId>hcs-sxc-java-plugins-mirror-queue-artemis</artifactId>
<version>0.0.3-SNAPSHOT</version>
```

If you choose Artemis Message Queue, you must run both a

+ `message queue`: run Artemis MQ or use our [artemis from docker instructions](../hcs-sxc-java-queue/README.md)

+ `hcs-sxc-java-relay` :  We need the relay to ensure the queue is given messages to persist on behalf of `AppNet` participants. Run `Launch.java` from the [relay project](../hcs-sxc-java-relay)  to start the relay. The relay must be up and running before the hcs core component is instantiated.  Also you must add `<plugin-folder>\.config\.queue-config.yaml` to your config folder of the queue plugin and modify appropriately. 

```yaml
# Connection details to the Artemis MQ component
queue:
  initialContextFactory: "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory"
  tcpConnectionFactory: "tcp://hcs-sxc-java-queue:61616?jms.redeliveryPolicy.initialRedeliveryDelay=0&jms.redeliveryPolicy.backOffMultiplier=1&jms.redeliveryPolicy.maximumRedeliveries=5&jms.redeliveryPolicy.redeliveryDelay=500&jms.redeliveryPolicy.useExponentialBackOff=false"

```

### Encryption and key rotation

Encryption and key rotation is optional. You can implement own encryption mechanism and provide your own Diffie Hellman key rotation implementation. To use the supplied implementation use. 

```yaml
<groupId>com.hedera</groupId>
<artifactId>hcs-sxc-java-plugins-encryption-diffie-hellman</artifactId>
<version>0.0.3-SNAPSHOT</version>    
```

Consult the [encryption howto section in the main README](../README.md####sending-and-receiving-encrypted-messages)  on how to configure it and send encrypted messages. 

*Note 1: Version numbers may change over time.*

