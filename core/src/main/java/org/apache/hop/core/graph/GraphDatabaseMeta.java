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

import org.apache.commons.lang.StringUtils;
import org.apache.hop.core.exception.HopPluginException;
import org.apache.hop.core.exception.HopXmlException;
import org.apache.hop.core.logging.ILogChannel;
import org.apache.hop.core.logging.LogChannel;
import org.apache.hop.core.plugins.IPlugin;
import org.apache.hop.core.plugins.IPluginTypeListener;
import org.apache.hop.core.plugins.PluginRegistry;
import org.apache.hop.core.util.ExecutorUtil;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.metadata.api.HopMetadata;
import org.apache.hop.metadata.api.HopMetadataBase;
import org.apache.hop.metadata.api.HopMetadataProperty;
import org.apache.hop.metadata.api.IHopMetadata;
import org.apache.hop.metadata.api.IHopMetadataProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

@HopMetadata(
        key = "graphdb",
        name = "Graph Database Connection",
        description = "This contains all the metadata needed to connect to an OpenCypher and Bolt compatible graph database.",
        image = "ui/images/graph-database.svg",
        documentationUrl = "/metadata-types/graph-connection.html")
public class GraphDatabaseMeta extends HopMetadataBase implements Cloneable, IHopMetadata {

    private static final Class<?> PKG = GraphDatabase.class;
    public static final String XML_TAG = "graph_connection";

    public static final String GUI_PLUGIN_ELEMENT_PARENT_ID = "GraphDatabaseMeta-PluginSpecific-Options";

    public static final Comparator<GraphDatabaseMeta> comparator =
            (GraphDatabaseMeta gdbm1, GraphDatabaseMeta gbbm2) -> gdbm1.getName().compareToIgnoreCase(gbbm2.getName());


    @HopMetadataProperty(key = "graphdb")
    private IGraphDatabase iGraphDatabase;

    private static volatile Future<Map<String, IGraphDatabase>> allGraphDatabaseInterfaces;

    static {
        init();
    }

    public static void init(){
        PluginRegistry.getInstance()
                .addPluginListener(GraphDatabasePluginType.class,
                        new IPluginTypeListener() {
                            @Override
                            public void pluginAdded(Object serviceObject) {
                                clearGraphDatabaseInterfacesMap();
                            }

                            @Override
                            public void pluginRemoved(Object serviceObject) {
                                clearGraphDatabaseInterfacesMap();
                            }

                            @Override
                            public void pluginChanged(Object serviceObject) {
                                clearGraphDatabaseInterfacesMap();
                            }
                        });
    }

    private boolean readOnly = false;

    /** Connect natively through Bolt driver to the database **/
    public static final int TYPE_ACCESS_NATIVE = 0;

    /** Short description of the access type, used in serialization **/
    public static final String[] graphDbAccessTypeCode = {"Bolt"};

    /** Longer description for user interaction **/
    public static final String[] graphDbAccessTypeDesc = {"Native (Bolt)"};

    /**
     * Use this length in a String value to indication that you want to use a CLOB instead of a normal text field.
    **/
    public static final int CLOB_LENGTH = 9999999;

    /**
     * The value to store in the attribute so that an empty value doesn't get lost...
     */
    public static final String EMPTY_OPTIONS_STRING = "><EMPTY><";

    /**
     * Construct a new graph database connection. Note that all these parameters are not always mandatory.
     *
     * @param name The graph database name
     * @param type The graph database type
     * @param user The username
     * @param pass The password
     */
    public GraphDatabaseMeta(
            String name,
            String type,
            String user,
            String pass){
        setValues(name, type, user, pass);
        addOptions();
    }

    public GraphDatabaseMeta(){
        setDefault();
        addOptions();
    }

    public static GraphDatabaseMeta loadGraphDatabase(
        IHopMetadataProvider metadataProvider, String connectionName
    ) throws HopXmlException{
        if(metadataProvider == null || StringUtils.isEmpty(connectionName)){
            return null; // Nothing to find or load;
        }
        try{
            return metadataProvider.getSerializer(GraphDatabaseMeta.class).load(connectionName);
        }catch(Exception e){
            throw new HopXmlException(
                    "Unable to load graph database connection '" + connectionName + "'", e);
        }
    }

    public void setDefault(){
//        setValues("", "NONE", "bolt", "", "", "", "", "");
        setValues("", "NONEGRAPH", "", "");
    }

    public void addOptions(){
    }

    public GraphDatabaseMeta(GraphDatabaseMeta graphDatabaseMeta){
        this();
        replaceMeta(graphDatabaseMeta);
    }

    /** @return the system dependent graph database interface for this graph database metadata definition */
    public IGraphDatabase getIGraphDatabase(){
        return iGraphDatabase;
    }

    /**
     * Set the system dependent graph database interface for this graph database metadata definition
     *
     * @param iGraphDatabase the system dependent database interface
     */
    public void setIGraphDatabase(IGraphDatabase iGraphDatabase){
        this.iGraphDatabase = iGraphDatabase;
    }

    /**
     * Search for the right type of IGraphDatabase object and clone it.
     *
     * @param graphDatabaseType the type of IGraphDatabase to look for (description)
     * @return The requested IGraphDatabase
     * @throws HopGraphDatabaseException when the type could not be found or referenced.
     */
    public static final IGraphDatabase getIGraphDatabase(String graphDatabaseType) throws HopGraphDatabaseException {
        IGraphDatabase igd = findIGraphDatabase(graphDatabaseType);
        if(igd == null){
            throw new HopGraphDatabaseException(
                    BaseMessages.getString(
                            PKG, "GraphDatabaseMeta.Error.GraphDatabaseInterfaceNotFound", graphDatabaseType));
        }
        return (IGraphDatabase)igd.clone();
    }

    /**
     * Search for the right type of IGraphDatabase object and return it.
     *
     * @param graphDatabaseTypeDesc the type of IGraphDatabase to look for (id or description)
     * @return The requested IGraphDatabase
     * @throws HopGraphDatabaseException when the type could not be found or referenced.
     */
    private static final IGraphDatabase findIGraphDatabase(String graphDatabaseTypeDesc)
            throws HopGraphDatabaseException {
        PluginRegistry registry = PluginRegistry.getInstance();
        IPlugin plugin = registry.getPlugin(GraphDatabasePluginType.class, graphDatabaseTypeDesc);
        if (plugin == null) {
            plugin = registry.findPluginWithName(GraphDatabasePluginType.class, graphDatabaseTypeDesc);
        }

        if (plugin == null) {
            throw new HopGraphDatabaseException(
                    "database type with plugin id [" + graphDatabaseTypeDesc + "] couldn't be found!");
        }
        return getIGraphDatabaseMap().get(plugin.getIds()[0]);
    }

    @Override
    public Object clone() {
        return new GraphDatabaseMeta(this);
    }

    public void replaceMeta(GraphDatabaseMeta graphDatabaseMeta) {
        this.setValues(
                graphDatabaseMeta.getName(),
                graphDatabaseMeta.getPluginId(),
                graphDatabaseMeta.getUsername(),
                graphDatabaseMeta.getPassword());

        this.iGraphDatabase = (IGraphDatabase) graphDatabaseMeta.iGraphDatabase.clone();

        this.setChanged();
    }

    public void setValues(
            String name,
            String type,
            String user,
            String pass) {
        try {
            iGraphDatabase = getIGraphDatabase(type);
        } catch (HopGraphDatabaseException kde) {
            throw new RuntimeException("Graph database type not found!", kde);
        }
        setName(name);
        setGraphDatabaseType(type);
        setUsername(user);
        setPassword(pass);
    }


    public void setGraphDatabaseType(String type) {
        IGraphDatabase oldInterface = iGraphDatabase;

        try {
            iGraphDatabase = getIGraphDatabase(type);
        } catch (HopGraphDatabaseException kde) {
            throw new RuntimeException("Database type [" + type + "] not found!", kde);
        }
        setHostname(oldInterface.getHostname());
        setUsername(oldInterface.getUsername());
        setPassword(oldInterface.getPassword());
        setDatabaseName(oldInterface.getDatabaseName());
        setChanged(oldInterface.isChanged());
    }

    public void setValues(GraphDatabaseMeta info) {
        iGraphDatabase = (IGraphDatabase) info.iGraphDatabase.clone();
    }

    /** @return The plugin ID of the database interface */
    public String getPluginId() {
        return iGraphDatabase.getPluginId();
    }

    public void setPluginId(String pluginId){
        iGraphDatabase.setPluginId(pluginId);
    }

    /** @return The name of the database plugin type */
    public String getPluginName() {
        return iGraphDatabase.getPluginName();
    }

    public void setPluginName(String pluginName){ iGraphDatabase.setPluginName(pluginName);}

    public String getHostname() {
        return iGraphDatabase.getHostname();
    }

    public void setHostname(String hostname) {
        iGraphDatabase.setHostname(hostname);
    }

    public String getDatabaseName() {
        return iGraphDatabase.getDatabaseName();
    }

    public void setDatabaseName(String databaseName) {
        iGraphDatabase.setDatabaseName(databaseName);
    }

    public String getUsername() {
        return iGraphDatabase.getUsername();
    }

    public void setUsername(String username) {
        iGraphDatabase.setUsername(username);
    }

    public String getPassword() {
        return iGraphDatabase.getPassword();
    }

    public void setPassword(String password) {
        iGraphDatabase.setPassword(password);
    }

    public void setChanged() {
        setChanged(true);
    }

    public void setChanged(boolean ch) {
        iGraphDatabase.setChanged(ch);
    }

    public boolean hasChanged() {
        return iGraphDatabase.isChanged();
    }

    public void clearChanged() {
        iGraphDatabase.setChanged(false);
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public int hashCode() {
        return getName().hashCode(); // name of connection is unique!
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof GraphDatabaseMeta && getName().equals(((GraphDatabaseMeta) obj).getName());
    }

    public static final IGraphDatabase[] getGraphDatabaseInterfaces() {
        List<IGraphDatabase> list = new ArrayList<>(getIGraphDatabaseMap().values());
        return list.toArray(new IGraphDatabase[list.size()]);
    }

    /**
     * Clear the database interfaces map. The map is cached by getDatabaseInterfacesMap(), but in some
     * instances it may need to be reloaded (such as adding/updating Database plugins). After calling
     * clearDatabaseInterfacesMap(), the next call to getDatabaseInterfacesMap() will reload the map.
     */    public static final void clearGraphDatabaseInterfacesMap(){
        allGraphDatabaseInterfaces = null;
    }

    public static final IGraphDatabase[] getDatabaseInterfaces() {
        List<IGraphDatabase> list = new ArrayList<>(getIGraphDatabaseMap().values());
        return list.toArray(new IGraphDatabase[list.size()]);
    }

    private static final Future<Map<String, IGraphDatabase>> createGraphDatabaseInterfacesMap() {
        return ExecutorUtil.getExecutor()
                .submit(
                        new Callable<Map<String, IGraphDatabase>>() {
                            private Map<String, IGraphDatabase> doCreate() {
                                ILogChannel log = LogChannel.GENERAL;
                                PluginRegistry registry = PluginRegistry.getInstance();

                                List<IPlugin> plugins = registry.getPlugins(GraphDatabasePluginType.class);
                                HashMap<String, IGraphDatabase> tmpAllDatabaseInterfaces = new HashMap<>();
                                for (IPlugin plugin : plugins) {
                                    try {
                                        IGraphDatabase iGraphDatabase = (IGraphDatabase) registry.loadClass(plugin);
                                        iGraphDatabase.setPluginId(plugin.getIds()[0]);
                                        iGraphDatabase.setPluginName(plugin.getName());
                                        tmpAllDatabaseInterfaces.put(plugin.getIds()[0], iGraphDatabase);
                                    } catch (HopPluginException cnfe) {
                                        log.logError(
                                                "Could not create connection entry for "
                                                        + plugin.getName()
                                                        + ".  "
                                                        + cnfe.getCause().getClass().getName());
                                        if (log.isDebug()) {
                                            log.logDebug("Debug-Error loading plugin: " + plugin, cnfe);
                                        }
                                    } catch (Exception e) {
                                        log.logError("Error loading plugin: " + plugin, e);
                                    }
                                }
                                return Collections.unmodifiableMap(tmpAllDatabaseInterfaces);
                            }

                            @Override
                            public Map<String, IGraphDatabase> call() throws Exception {
                                return doCreate();
                            }
                        });
    }


    /**
     * Clear the database interfaces map. The map is cached by getDatabaseInterfacesMap(), but in some
     * instances it may need to be reloaded (such as adding/updating Database plugins). After calling
     * clearDatabaseInterfacesMap(), the next call to getDatabaseInterfacesMap() will reload the map.
     */
    public static final void clearDatabaseInterfacesMap() {
        allGraphDatabaseInterfaces = null;
    }

    private static final Future<Map<String, IGraphDatabase>> createDatabaseInterfacesMap() {
        return ExecutorUtil.getExecutor()
                .submit(
                        new Callable<Map<String, IGraphDatabase>>() {
                            private Map<String, IGraphDatabase> doCreate() {
                                ILogChannel log = LogChannel.GENERAL;
                                PluginRegistry registry = PluginRegistry.getInstance();

                                List<IPlugin> plugins = registry.getPlugins(GraphDatabasePluginType.class);
                                HashMap<String, IGraphDatabase> tmpAllDatabaseInterfaces = new HashMap<>();
                                for (IPlugin plugin : plugins) {
                                    try {
                                        IGraphDatabase iGraphDatabase = (IGraphDatabase) registry.loadClass(plugin);
                                        iGraphDatabase.setPluginId(plugin.getIds()[0]);
                                        iGraphDatabase.setPluginName(plugin.getName());
                                        tmpAllDatabaseInterfaces.put(plugin.getIds()[0], iGraphDatabase);
                                    } catch (HopPluginException cnfe) {
                                        log.logError(
                                                "Could not create connection entry for "
                                                        + plugin.getName()
                                                        + ".  "
                                                        + cnfe.getCause().getClass().getName());
                                        if (log.isDebug()) {
                                            log.logDebug("Debug-Error loading plugin: " + plugin, cnfe);
                                        }
                                    } catch (Exception e) {
                                        log.logError("Error loading plugin: " + plugin, e);
                                    }
                                }
                                return Collections.unmodifiableMap(tmpAllDatabaseInterfaces);
                            }

                            @Override
                            public Map<String, IGraphDatabase> call() throws Exception {
                                return doCreate();
                            }
                        });
    }

    public static final Map<String, IGraphDatabase> getIGraphDatabaseMap() {
        Future<Map<String, IGraphDatabase>> allDatabaseInterfaces = GraphDatabaseMeta.allGraphDatabaseInterfaces;
        while (allDatabaseInterfaces == null) {
            GraphDatabaseMeta.allGraphDatabaseInterfaces = createGraphDatabaseInterfacesMap();
            allDatabaseInterfaces = GraphDatabaseMeta.allGraphDatabaseInterfaces;
        }
        try {
            return allDatabaseInterfaces.get();
        } catch (Exception e) {
            clearDatabaseInterfacesMap();
            // doCreate() above doesn't declare any exceptions so anything that comes out SHOULD be a
            // runtime exception
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    public String[] checkParameters(){
        ArrayList<String> remarks = new ArrayList<>();

        if(getIGraphDatabase() == null){
            remarks.add(BaseMessages.getString(PKG, "GraphDatabaseMeta.BadInterface"));
        }

        if(getName() == null || getName().length() == 0){
            remarks.add(BaseMessages.getString(PKG, "GraphDatabaseMeta.BadConnectionName"));
        }

        return remarks.toArray(new String[0]);
    }

    public IGraphDatabaseFactory getGraphDatabaseFactory() throws Exception {
        PluginRegistry registry = PluginRegistry.getInstance();
        IPlugin plugin = registry.getPlugin(GraphDatabasePluginType.class, iGraphDatabase.getPluginId());
        if(plugin == null){
            throw new HopGraphDatabaseException(
                    "graph database with plugin id [" + iGraphDatabase.getPluginId() + "] couldn't be found!");
        }

        ClassLoader loader = registry.getClassLoader(plugin);

        Class<?> clazz = Class.forName(iGraphDatabase.getGraphDatabaseFactoryName(), true, loader);
        return(IGraphDatabaseFactory) clazz.getDeclaredConstructor().newInstance();
    }

    public String getDriverClass(IVariables variables){
        return variables.resolve(iGraphDatabase.getDriverClass());
    }

}
