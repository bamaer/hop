package org.apache.hop.core.graph;

import java.util.List;
import java.util.Map;

public interface IBoltGraphDatabase extends IGraphDatabase{

    String getBrowserPort();

    void setBrowserPort(String browserPort);


    /**
     * @return Returns the databaseName.
     *
     */

    String getDatabaseName();

    /** @param databaseName The databaseName to set. */
    void setDatabaseName(String databaseName);

    /** @return Returns the hostname. */
    String getHostname();

    /** @param hostname The hostname to set. */
    void setHostname(String hostname);

    /** @return Returns the servername. */
    String getServername();

    /** @param servername The servername to set. */
    void setServername(String servername);

    /** Set default options for all graph databases **/
    default void addDefaultOptions(){}

    /** @return true if the database supports a boolean, bit, logical, ... datatype */
    boolean isSupportsBooleanDataType();

    /** @param b Set to true if the database supports a boolean, bit, logical, ... datatype */
    void setSupportsBooleanDataType(boolean b);

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

    boolean isRoutingVariable();

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


}
