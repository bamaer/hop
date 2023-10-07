package org.apache.hop.core.graph;

import org.apache.commons.lang.StringUtils;
import org.apache.hop.core.Const;
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
//     * @param protocol The graph database connection protocol to use
//     * @param host The hostname or IP address
//     * @param db The graph database name
//     * @param port The port on which the graph database listens
     * @param user The username
     * @param pass The password
     */
    public GraphDatabaseMeta(
            String name,
            String type,
//            String protocol,
//            String host,
//            String db,
//            String port,
            String user,
            String pass){
//        setValues(name, type, protocol, host, db, port, user, pass);
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
//        iGraphDatabase.addDefaultOptions();
//        setSupportsBooleanDataType(true);
//        setSupportsTimestampDataType(true);
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
//                graphDatabaseMeta.getProtocol(),
//                graphDatabaseMeta.getServer(),
//                graphDatabaseMeta.getDatabaseName(),
//                graphDatabaseMeta.getBoltPort(),
                graphDatabaseMeta.getUsername(),
                graphDatabaseMeta.getPassword());
//        this.setServer(graphDatabaseMeta.getServer());

        this.iGraphDatabase = (IGraphDatabase) graphDatabaseMeta.iGraphDatabase.clone();

        this.setChanged();
    }

    public void setValues(
            String name,
            String type,
//            String protocol,
//            String host,
//            String db,
//            String port,
            String user,
            String pass) {
        try {
            iGraphDatabase = getIGraphDatabase(type);
        } catch (HopGraphDatabaseException kde) {
            throw new RuntimeException("Graph database type not found!", kde);
        }

        setName(name);
        setGraphDatabaseType(type);
//        setServer(host);
//        setDatabaseName(db);
//        setBoltPort(port);
        setUsername(user);
        setPassword(pass);
//        setServer(null);
//        setChanged(false);
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

//        setServer(oldInterface.getHostname());
//        setDatabaseName(oldInterface.getDatabaseName());
//        setProtocol(oldInterface.getProtocol());
//        setBoltPort(oldInterface.getBoltPort());
//        setServer(oldInterface.getHostname());
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

/*
    public String getBoltPort() {
        return iGraphDatabase.getBoltPort();
    }

    public void setBoltPort(String boltPort) {
        iGraphDatabase.setBoltPort(boltPort);
    }

    public String getBrowserPort() {
        return iGraphDatabase.getBrowserPort();
    }

    public void setBrowserPort(String browserPort) {
        iGraphDatabase.setBrowserPort(browserPort);
    }

    public boolean isRouting() {
        return iGraphDatabase.isRouting();
    }

    public void setRouting(boolean routing) {
        iGraphDatabase.setRouting(routing);
    }

    public boolean getRoutingVariable() {
        return iGraphDatabase.isRoutingVariable();
    }

    public void setRoutingVariable(boolean routingVariable) {
        iGraphDatabase.setRoutingVariable(routingVariable);
    }

    public String getRoutingPolicy() {
        return iGraphDatabase.getRoutingPolicy();
    }

    public void setRoutingPolicy(String routingPolicy) {
        iGraphDatabase.setRoutingPolicy(routingPolicy);
    }
*/

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

/*
    public boolean isUsingEncryption() {
        return iGraphDatabase.isUsingEncryption();
    }

    public void setUsingEncryption(boolean usingEncryption) {
        iGraphDatabase.setUsingEncryption(usingEncryption);
    }

    public String getUsingEncryptionVariable() {
        return iGraphDatabase.getUsingEncryptionVariable();
    }

    public void setUsingEncryptionVariable(String usingEncryptionVariable) {
        iGraphDatabase.setUsingEncryptionVariable(usingEncryptionVariable);
    }

    public boolean isTrustAllCertificates() {
        return iGraphDatabase.isTrustAllCertificates();
    }

    public void setTrustAllCertificates(boolean trustAllCertificates) {
        iGraphDatabase.setTrustAllCertificates(trustAllCertificates);
    }

    public String getTrustAllCertificatesVariable() {
        return iGraphDatabase.getTrustAllCertificatesVariable();
    }

    public void setTrustAllCertificatesVariable(String trustAllCertificatesVariable) {
        iGraphDatabase.setTrustAllCertificatesVariable(trustAllCertificatesVariable);
    }

    public String getConnectionLivenessCheckTimeout() {
        return iGraphDatabase.getConnectionLivenessCheckTimeout();
    }

    public void setConnectionLivenessCheckTimeout(String connectionLivenessCheckTimeout) {
        iGraphDatabase.setConnectionLivenessCheckTimeout(connectionLivenessCheckTimeout);
    }

    public String getMaxConnectionLifetime() {
        return iGraphDatabase.getMaxConnectionLifetime();
    }

    public void setMaxConnectionLifetime(String maxConnectionLifetime) {
        iGraphDatabase.setMaxConnectionLifetime(maxConnectionLifetime);
    }

    public String getMaxConnectionPoolSize() {
        return iGraphDatabase.getMaxConnectionPoolSize();
    }

    public void setMaxConnectionPoolSize(String maxConnectionPoolSize) {
        iGraphDatabase.setMaxConnectionPoolSize(maxConnectionPoolSize);
    }

    public String getConnectionAcquisitionTimeout() {
        return iGraphDatabase.getConnectionAcquisitionTimeout();
    }

    public void setConnectionAcquisitionTimeout(String connectionAcquisitionTimeout) {
        iGraphDatabase.setConnectionAcquisitionTimeout(connectionAcquisitionTimeout);
    }

    public String getConnectionTimeout() {
        return iGraphDatabase.getConnectionTimeout();
    }

    public void setConnectionTimeout(String connectionTimeout) {
        iGraphDatabase.setConnectionTimeout(connectionTimeout);
    }

    public String getMaxTransactionRetryTime() {
        return iGraphDatabase.getMaxTransactionRetryTime();
    }

    public void setMaxTransactionRetryTime(String maxTransactionRetryTime) {
        iGraphDatabase.setMaxTransactionRetryTime(maxTransactionRetryTime);
    }

    public boolean isVersion4() {
        return iGraphDatabase.isVersion4();
    }

    public void setVersion4(boolean version4) {
        iGraphDatabase.setVersion4(version4);
    }

    public String getVersion4Variable() {
        return iGraphDatabase.getVersion4Variable();
    }

    public void setVersion4Variable(String version4Variable) {
        iGraphDatabase.setVersion4Variable(version4Variable);
    }

    public boolean isAutomatic() {
        return iGraphDatabase.isAutomatic();
    }

    public void setAutomatic(boolean automatic) {
        iGraphDatabase.setAutomatic(automatic);
    }

    public String getAutomaticVariable() {
        return iGraphDatabase.getAutomaticVariable();
    }

    public void setAutomaticVariable(String automaticVariable) {
        iGraphDatabase.setAutomaticVariable(automaticVariable);
    }

    public String getProtocol() {
        return iGraphDatabase.getProtocol();
    }

    public void setProtocol(String protocol) {
        iGraphDatabase.setProtocol(protocol);
    }
*/

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

/*
    */
/** @return true if the database supports a boolean, bit, logical, ... datatype *//*

    public boolean supportsBooleanDataType() {
        return iGraphDatabase.isSupportsBooleanDataType();
    }

    */
/** @param b Set to true if the database supports a boolean, bit, logical, ... datatype *//*

    public void setSupportsBooleanDataType(boolean b) {
        iGraphDatabase.setSupportsBooleanDataType(b);
    }

    */
/**
     * @return true if the database supports the Timestamp data type (nanosecond precision and all)
     *//*

    public boolean supportsTimestampDataType() {
        return iGraphDatabase.isSupportsTimestampDataType();
    }

    */
/**
     * @param b Set to true if the database supports the Timestamp data type (nanosecond precision and
     *     all)
     *//*

    public void setSupportsTimestampDataType(boolean b) {
        iGraphDatabase.setSupportsTimestampDataType(b);
    }

    */
/** @return A manually entered URL which will be used over the internally generated one *//*

    public List<String> getManualUrls() {
        return iGraphDatabase.getManualUrls();
    }

    */
/**
     * @param manualUrls A manually entered URL which will be used over the internally generated one
     *//*

    public void setManualUrls(List<String> manualUrls) {
        iGraphDatabase.setManualUrls(manualUrls);
    }
*/


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

//    @Override
/*
    public GraphDatabaseTestResults testConnectionSuccess(IVariables variables){
        StringBuilder report = new StringBuilder();
        GraphDatabaseTestResults testResults = new GraphDatabaseTestResults();

        // If the plugin needs to provide connection information, we ask the IGraphDatabase.
//        try{
//            IGraphDatabaseFactory factory = getGraphDatabaseFactory();
//            testResults = factory.getConnectionTestResults(variables, this);
//            testResults = testC
        }catch(ClassNotFoundException e){
            report.append(
                    BaseMessages.getString(PKG, "BaseGraphDatabaseMeta.TestConnectionReportNotImplemented.Message")).append(Const.CR);
            report.append(BaseMessages.getString(PKG, "GraphDatabaseMeta.report.ConnectionError", getName()) + e.toString() + Const.CR);
            report.append(Const.getStackTracker(e) + Const.CR);
            testResults.setMessage(report.toString());
            testResults.setSuccess(false);
        }catch(Exception e){
            report.append(
                    BaseMessages.getString(PKG, "GraphDatabaseMeta.report.ConnectionError", getName())
                    + e.toString()
                    + Const.CR);
            report.append(Const.getStackTracker(e) + Const.CR);
            testResults.setMessage(report.toString());
            testResults.setSuccess(false);
        }
        return testResults;
    }
*/

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
