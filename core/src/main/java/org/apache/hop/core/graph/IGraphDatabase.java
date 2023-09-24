package org.apache.hop.core.graph;

import org.apache.hop.metadata.api.HopMetadataObject;

import java.util.List;
import java.util.Map;

@HopMetadataObject(objectFactory = GraphDatabaseMetaObjectFactory.class)
public interface IGraphDatabase extends Cloneable{

    /** @return the plugin id of this database */
    String getPluginId();

    /** @param pluginId set the plugin id of this plugin (after instantiation) */
    void setPluginId(String pluginId);

    /** @return the plugin name of this database, the same thing as the annotation typeDescription */
    String getPluginName();

    /** @param pluginName set the plugin name of this plugin (after instantiation) */
    void setPluginName(String pluginName);

    /** @return Returns the changed. */
    boolean isChanged();

    /** @param changed The changed to set. */
    void setChanged(boolean changed);

    String getBrowserPort();

    void setBrowserPort(String browserPort);

    /** @return Returns the databaseName. */
    String getDatabaseName();

    /** @param databaseName The databaseName to set. */
    void setDatabaseName(String databaseName);

    /** @return Returns the hostname. */
    String getHostname();

    /** @param hostname The hostname to set. */
    void setHostname(String hostname);

    /** @return the username to log onto the database */
    String getUsername();

    /** @param username Sets the username to log onto the database with. */
    void setUsername(String username);

    /** @return Returns the password. */
    String getPassword();

    /** @param password The password to set. */
    void setPassword(String password);

    /** @return Returns the servername. */
//    String getServername();

    /** @param servername The servername to set. */
//    void setServername(String servername);

    /** Set default options for all graph databases **/
    default void addDefaultOptions(){}

    /** @return true if the database supports a boolean, bit, logical, ... datatype */
    boolean isSupportsBooleanDataType();

    /** @param b Set to true if the database supports a boolean, bit, logical, ... datatype */
    void setSupportsBooleanDataType(boolean b);

    /**
     * @return true if the database supports the Timestamp data type (nanosecond precision and all)
     */
    boolean isSupportsTimestampDataType();

    /**
     * @param b Set to true if the database supports the Timestamp data type (nanosecond precision and
     *     all)
     */
    void setSupportsTimestampDataType(boolean b);

    /** @return A manually entered URL which will be used over the internally generated one */
    List<String> getManualUrls();

    /**
     * @param manualUrl A manually entered URL which will be used over the internally generated one
     */
    void setManualUrls(List<String> manualUrl);

    String getDefaultBoltPort();

    void setDefaultBoltPort(String defaultBoltPort);

    String getBoltPort();

    void setBoltPort(String boltPort);

    boolean isRouting();

    void setRouting(boolean routing);

    boolean getRoutingVariable();

    void setRoutingVariable(boolean routingVariable);

    String getRoutingPolicy();

    void setRoutingPolicy(String routingPolicy);

    boolean isUsingEncryption();

    void setUsingEncryption(boolean usingEncryption);

    String getUsingEncryptionVariable();

    void setUsingEncryptionVariable(String usingEncryptionVariable);

    boolean isTrustAllCertificates();

    void setTrustAllCertificates(boolean trustAllCertificates);

    String getTrustAllCertificatesVariable();

    void setTrustAllCertificatesVariable(String trustAllCertificatesVariable);

    String getConnectionLivenessCheckTimeout();

    void setConnectionLivenessCheckTimeout(String connectionLivenessCheckTimeout);

    String getMaxConnectionLifetime();

    void setMaxConnectionLifetime(String maxconnectionLifetime);

    String getMaxConnectionPoolSize();

    void setMaxConnectionPoolSize(String maxConnectionPoolSize);

    String getConnectionAcquisitionTimeout();

    void setConnectionAcquisitionTimeout(String connectionAcquisitionTimeout);

    String getConnectionTimeout();

    void setConnectionTimeout(String connectionTimeout);

    String getMaxTransactionRetryTime();

    void setMaxTransactionRetryTime(String maxTransactionRetryTime);

    boolean isVersion4();

    void setVersion4(boolean version4);

    String getVersion4Variable();

    void setVersion4Variable(String version4Variable);

    boolean isAutomatic();

    void setAutomatic(boolean automatic);

    String getAutomaticVariable();

    void setAutomaticVariable(String automaticVariable);

    String getProtocol();

    void setProtocol(String protocol);

    Map<String, String> getAttributes();

    void setAttributes(Map<String, String> attributes);


    /**
     * Clone this graph database interface: copy all info to a new object
     *
     * @return the cloned Graph Database Interface object.
     */
    Object clone();

    /**
     * @return true if the database supports range indexes.
     */
    boolean isSupportsRangeIndex();

    /**
     * @return true if the database supports lookup indexes
     */
    boolean isSupportsLookupIndex();

    /**
     * @return true if the database supports text indexes
     */
    boolean isSupportsTextIndex();

    /**
     * @return true if the database supports point indexes
     */
    boolean isSupportsPointIndex();

    /**
     * @return true if the database supports full text indexes
     */
    boolean isSupportsFullTextIndex();

    /**
     * @return true if the database supports BTree indexes
     */
    boolean isSupportsBTreeIndex();


    /**
     * Obtain the name of the driver class that we need to use for this graph database connection!
     *
     * @return the name of the driver class for the specific graph database
     */
    String getDriverClass();

    void setDriverClass(String driverClass);

    /**
     * You can use this method to supply an alternate factory for the test method in the dialogs. This
     * is useful for plugins like SAP/R3 and PALO.
     *
     * @return the name of the graph database test factory to use.
     */
    String getGraphDatabaseFactoryName();

}
