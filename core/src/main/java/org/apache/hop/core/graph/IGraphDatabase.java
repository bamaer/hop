/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hop.core.graph;

import org.apache.hop.core.exception.HopConfigException;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.graph.model.GraphNode;
import org.apache.hop.core.graph.model.GraphRelationship;
import org.apache.hop.core.variables.IVariables;
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

    /** @return Returns the hostname. */
    String getHostname();

    /** @param hostname The hostname to set. */
    void setHostname(String hostname);

    String getPort();
    void setPort(String port);
    /**
     * @return Returns the databaseName.
     *
     */
    String getDatabaseName();

    /** @param databaseName The databaseName to set. */
    void setDatabaseName(String databaseName);

    /**
     * @return true if the database supports the Timestamp data type (nanosecond precision and all)
     */
    boolean isSupportsTimestampDataType();

    /**
     * @param b Set to true if the database supports the Timestamp data type (nanosecond precision and
     *     all)
     */

    void setSupportsTimestampDataType(boolean b);

    Map<String, String> getAttributes();

    void setAttributes(Map<String, String> attributes);

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

    String getServerTestQuery();

    String getServerInfo();

    GraphDatabaseTestResults testConnectionSuccess(IVariables variables) throws HopConfigException, HopException;

    String getCreateIndexStatement(List<String> labels, String property);

    void runStatement(IVariables variables, String statement);

    void writeData(IVariables variables, String query, Map<String, Object> properties);

    void writeToGraph(
            List<GraphNode> fromNodes,
            List<GraphNode> toNodes,
            List<Object[]> fromNodeProperties,
            List<Object[]> toNodeProperties,
            List<GraphRelationship> graphRelationships,
            List<Object[]> relationshipProperties
    );

    void writeNode(
            List<GraphNode> nodes,
            List<Object[]> nodeProperties
    );
}
