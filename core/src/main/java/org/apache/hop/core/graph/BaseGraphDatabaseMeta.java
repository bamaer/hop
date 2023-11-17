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

import org.apache.hop.core.graph.model.GraphNode;
import org.apache.hop.core.graph.model.GraphRelationship;
import org.apache.hop.core.gui.plugin.GuiElementType;
import org.apache.hop.core.gui.plugin.GuiWidgetElement;
import org.apache.hop.core.logging.ILogChannel;
import org.apache.hop.metadata.api.HopMetadataProperty;
import org.apache.hop.metadata.api.IHopMetadataProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseGraphDatabaseMeta implements Cloneable, IGraphDatabase{

    public static final String ID_USERNAME_LABEL = "username-label";
    public static final String ID_USERNAME_WIDGET = "username-widget";
    public static final String ID_PASSWORD_LABEL = "password-label";
    public static final String ID_PASSWORD_WIDGET = "password-widget";

    private String driverClass = "no driver";


    @HopMetadataProperty
    @GuiWidgetElement(
            id = "hostname",
            order = "03",
            label = "i18n:org.apache.hop.ui.core.graph:GraphDatabaseDialog.ServerHostname.Label",
            type = GuiElementType.TEXT,
            variables = true,
            parentId = GraphDatabaseMeta.GUI_PLUGIN_ELEMENT_PARENT_ID
    )
    protected String hostname;

    @HopMetadataProperty
    @GuiWidgetElement(
            id = "port",
            order = "04",
            label = "port",
            type = GuiElementType.TEXT,
            variables = true,
            parentId = GraphDatabaseMeta.GUI_PLUGIN_ELEMENT_PARENT_ID
    )
    protected String port;

    @HopMetadataProperty
    @GuiWidgetElement(
            id = "databaseName",
            order = "05",
            label = "i18n:org.apache.hop.ui.core.graph:GraphDatabaseDialog.DatabaseName.Label",
            type = GuiElementType.TEXT,
            variables = true,
            parentId = GraphDatabaseMeta.GUI_PLUGIN_ELEMENT_PARENT_ID
    )
    protected String databaseName;

    @HopMetadataProperty
    @GuiWidgetElement(
            id = "username",
            order = "06",
            label = "i18n:org.apache.hop.ui.core.graph:GraphDatabaseDialog.Username.Label",
            type = GuiElementType.TEXT,
            variables = true,
            parentId = GraphDatabaseMeta.GUI_PLUGIN_ELEMENT_PARENT_ID
    )
    protected String username;

    @HopMetadataProperty(password = true)
    @GuiWidgetElement(
            id = "password",
            order = "07",
            label = "i18n:org.apache.hop.ui.core.graph:GraphDatabaseDialog.Password.Label",
            type = GuiElementType.TEXT,
            variables = true,
            password = true,
            parentId = GraphDatabaseMeta.GUI_PLUGIN_ELEMENT_PARENT_ID
    )
    protected String password;


    private boolean changed;

    @HopMetadataProperty protected Map<String, String> attributes;

    @HopMetadataProperty protected String pluginId;
    @HopMetadataProperty protected String pluginName;

    public BaseGraphDatabaseMeta(){
        attributes = Collections.synchronizedMap(new HashMap<>());
        changed = false;
    }

    @Override
    public String getPluginId() {
        return pluginId;
    }

    @Override
    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }

    @Override
    public String getPluginName() {
        return pluginName;
    }

    @Override
    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

    @Override
    public boolean isChanged() {
        return changed;
    }

    @Override
    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getHostname(){
        return hostname;
    }

    @Override
    public void setHostname(String hostname){
        this.hostname = hostname;
    }

    @Override
    public String getPort(){ return port; }

    @Override
    public void setPort(String port){ this.port = port; }
    @Override
    public String getDatabaseName() {
        return databaseName;
    }

    @Override
    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    @Override
    public boolean isSupportsTimestampDataType() {
        return false;
    }

    @Override
    public void setSupportsTimestampDataType(boolean b) {
    }

    @Override
    public Map<String, String> getAttributes() {
        return attributes;
    }

    @Override
    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    @Override
    public Object clone() {
        BaseGraphDatabaseMeta retval = null;
        try {
            retval = (BaseGraphDatabaseMeta)super.clone();
            retval.attributes = Collections.synchronizedMap(new HashMap<>());
            for(String key : attributes.keySet()){
                retval.attributes.put(key, attributes.get(key));
            }
        }catch(CloneNotSupportedException e){
            throw new RuntimeException(e);
        }
        return retval;
    }

    @Override
    public String getDriverClass(){
        return driverClass;
    }

    @Override
    public void setDriverClass(String driverClass){
        this.driverClass = driverClass;
    }

    /**
     * You can use this method to supply an alternate factory for the test method in the dialogs.
     *
     * @return the name of the database test factory to use.
     */
    @Override
    public String getGraphDatabaseFactoryName() {
        return GraphDatabaseFactory.class.getName();
    }

    public List<String> getSupportedProtocols(ILogChannel log, IHopMetadataProvider metadataProvider){
        List<String> protocols = new ArrayList<>();
        protocols.add("neo4j");
        protocols.add("bolt");
        return protocols;
    }

    @Override
    public void writeToGraph(
            List<GraphNode> fromNodes,
            List<GraphNode> toNodes,
            List<Object[]> fromNodeProperties,
            List<Object[]> toNodeProperties,
            List<GraphRelationship> graphRelationships,
            List<Object[]> relationshipProperties
    ){}

    @Override
    public void writeNode(
            List<GraphNode> nodes,
            List<Object[]> nodeProperties
    ){
    }

}
