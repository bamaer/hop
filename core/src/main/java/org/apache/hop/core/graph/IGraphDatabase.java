package org.apache.hop.core.graph;

import org.apache.hop.metadata.api.HopMetadataObject;

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

    String getPort();

    void setPort(String port);

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
    String getServername();

    /** @param servername The servername to set. */
    void setServername(String servername);

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
    String getManualUrl();

    /**
     * @param manualUrl A manually entered URL which will be used over the internally generated one
     */
    void setManualUrl(String manualUrl);

    /**
     * Clone this graph database interface: copy all info to a new object
     *
     * @return the cloned Graph Database Interface object.
     */
    Object clone();

}
