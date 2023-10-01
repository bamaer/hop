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

    /** @return the username to log onto the database */
    String getUsername();

    /** @param username Sets the username to log onto the database with. */
    void setUsername(String username);

    /** @return Returns the password. */
    String getPassword();

    /** @param password The password to set. */
    void setPassword(String password);

    /**
     * @return true if the database supports the Timestamp data type (nanosecond precision and all)
     */
    boolean isSupportsTimestampDataType();

    /**
     * @param b Set to true if the database supports the Timestamp data type (nanosecond precision and
     *     all)
     */

    void setSupportsTimestampDataType(boolean b);

    /**
     * Clone this graph database interface: copy all info to a new object
     *
     * @return the cloned Graph Database Interface object.
     */
    Object clone();



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
