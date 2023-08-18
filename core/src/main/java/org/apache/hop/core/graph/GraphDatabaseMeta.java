package org.apache.hop.core.graph;

import org.apache.commons.lang.StringUtils;
import org.apache.hop.core.Const;
import org.apache.hop.core.encryption.Encr;
import org.apache.hop.core.exception.HopConfigException;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.exception.HopPluginException;
import org.apache.hop.core.exception.HopXmlException;
import org.apache.hop.core.logging.ILogChannel;
import org.apache.hop.core.logging.LogChannel;
import org.apache.hop.core.plugins.IPlugin;
import org.apache.hop.core.plugins.IPluginTypeListener;
import org.apache.hop.core.plugins.PluginRegistry;
import org.apache.hop.core.row.value.ValueMetaBase;
import org.apache.hop.core.util.ExecutorUtil;
import org.apache.hop.core.util.Utils;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.metadata.api.HopMetadata;
import org.apache.hop.metadata.api.HopMetadataBase;
import org.apache.hop.metadata.api.HopMetadataProperty;
import org.apache.hop.metadata.api.IHopMetadata;
import org.apache.hop.metadata.api.IHopMetadataProvider;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Config;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Logging;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;
import org.neo4j.driver.Value;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

@HopMetadata(
        key = "graphdb",
        name = "Graph Database Connection",
        description = "This contains all the metadata needed to connect to an OpenCypher and Bolt compatible graph database.",
        image = "ui/images/graph-database.svg",
        documentationUrl = "/metadata-types/graph-connection.html")
public class GraphDatabaseMeta extends HopMetadataBase implements Cloneable, IHopMetadata {

    private static final Class<?> PKG = GraphDatabase.class;

    public static final String GUI_PLUGIN_ELEMENT_PARENT_ID = "GraphDatabaseMeta-PluginSpecific-Options";

    public static final Comparator<GraphDatabaseMeta> comparator =
            (GraphDatabaseMeta gdbm1, GraphDatabaseMeta gbbm2) -> gdbm1.getName().compareToIgnoreCase(gbbm2.getName());

    @HopMetadataProperty(key = "graphdb")
    private IGraphDatabase iGraphDatabase;

    @HopMetadataProperty
    private List<String> manualUrls;

    @HopMetadataProperty
    private String server;

    @HopMetadataProperty
    private String databaseName;

    @HopMetadataProperty
    private String boltPort;

    @HopMetadataProperty
    private String browserPort;

    @HopMetadataProperty
    private boolean routing;

    @HopMetadataProperty
    private String routingVariable;

    @HopMetadataProperty
    private String routingPolicy;

    @HopMetadataProperty
    private String username;

    @HopMetadataProperty(password = true)
    private String password;

    @HopMetadataProperty
    private boolean usingEncryption;

    @HopMetadataProperty
    private String usingEncryptionVariable;

    @HopMetadataProperty
    private boolean trustAllCertificates;

    @HopMetadataProperty
    private String trustAllCertificatesVariable;

    @HopMetadataProperty
    private String connectionLivenessCheckTimeout;

    @HopMetadataProperty
    private String maxConnectionLifetime;

    @HopMetadataProperty
    private String maxConnectionPoolSize;

    @HopMetadataProperty
    private String connectionAcquisitionTimeout;

    @HopMetadataProperty
    private String connectionTimeout;

    @HopMetadataProperty
    private String maxTransactionRetryTime;

    @HopMetadataProperty
    private boolean version4;

    @HopMetadataProperty
    private String version4Variable;

    @HopMetadataProperty
    private boolean automatic;

    @HopMetadataProperty
    private String automaticVariable;

    @HopMetadataProperty
    private String protocol;

    private static volatile Future<Map<String, IGraphDatabase>> allGraphDatabaseInterfaces;

/*
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
*/

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

    public GraphDatabaseMeta(){
        boltPort = "7687";
        browserPort = "7474";
        protocol = "neo4j";
        manualUrls = new ArrayList<>();
        version4 = true;
        automatic = true;
    }

    public GraphDatabaseMeta(GraphDatabaseMeta source) {
        this.name = source.name;
        this.server = source.server;
        this.boltPort = source.boltPort;
        this.browserPort = source.browserPort;
        this.routing = source.routing;
        this.routingVariable = source.routingVariable;
        this.routingPolicy = source.routingPolicy;
        this.username = source.username;
        this.password = source.password;
        this.usingEncryption = source.usingEncryption;
        this.usingEncryptionVariable = source.usingEncryptionVariable;
        this.trustAllCertificates = source.trustAllCertificates;
        this.trustAllCertificatesVariable = source.trustAllCertificatesVariable;
        this.connectionLivenessCheckTimeout = source.connectionLivenessCheckTimeout;
        this.maxConnectionLifetime = source.maxConnectionLifetime;
        this.maxConnectionPoolSize = source.maxConnectionPoolSize;
        this.connectionAcquisitionTimeout = source.connectionAcquisitionTimeout;
        this.connectionTimeout = source.connectionTimeout;
        this.maxTransactionRetryTime = source.maxTransactionRetryTime;
        this.version4 = source.version4;
        this.version4Variable = source.version4Variable;
        this.automatic = source.automatic;
        this.automaticVariable = source.automaticVariable;
        this.protocol = source.protocol;
        this.manualUrls = new ArrayList<>();
        this.manualUrls.addAll(source.manualUrls);
    }


/*
    */
/**
     * Construct a new graph database connection. Note that all these parameters are not always mandatory.
     *
     * @param name The graph database name
     * @param type The graph database type
     * @param host The hostname or IP address
     * @param db The graph database name
     * @param port The port on which the graph database listens
     * @param user The username
     * @param pass The password
     *//*

    public GraphDatabaseMeta(
            String name,
            String type,
            String host,
            String db,
            String port,
            String user,
            String pass
    ){
        setValues(name, type, host, db, port, user, pass);
        addOptions();
    }
*/

/*
    public GraphDatabaseMeta(){
        setDefault();
        addOptions();
    }
*/

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

/*
    public void setDefault(){
        manualUrls = new ArrayList<>();
        setValues("", "NONE", "", "", "", "", "");
    }
*/

    public void addOptions(){
        iGraphDatabase.addDefaultOptions();
        setSupportsBooleanDataType(true);
        setSupportsTimestampDataType(true);
    }

/*
    public GraphDatabaseMeta(GraphDatabaseMeta graphDatabaseMeta){
        this();
        replaceMeta(graphDatabaseMeta);
    }
*/

    /** @return the system dependent graph database interface for this graph database metadata definition */
    public IGraphDatabase getiGraphDatabase() {
        return iGraphDatabase;
    }

    /**
     * Set the system dependent graph database interface for this graph database metadata definition
     *
     * @param iGraphDatabase the system dependent graph database interface
     */
    public void setIGraphDatabase(IGraphDatabase iGraphDatabase){
        this.iGraphDatabase = iGraphDatabase;
    }

/*
    public static final IGraphDatabase getIGraphDatabase(String graphDatabaseType) throws HopGraphDatabaseException{
        IGraphDatabase graphDb = findIGraphDatabase(graphDatabaseType);
        if(graphDb == null){
            throw new HopGraphDatabaseException(
                    BaseMessages.getString(PKG, "GraphDatabaseMeta.Error.GraphDatabaseInterfaceNotFound", graphDatabaseType)
            );
        }
        return (IGraphDatabase) graphDb.clone();
    }
*/

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

/*
    public void replaceMeta(GraphDatabaseMeta graphDatabaseMeta) {
        this.setValues(
                graphDatabaseMeta.getName(),
                graphDatabaseMeta.getPluginId(),
                graphDatabaseMeta.getServer(),
                graphDatabaseMeta.getDatabaseName(),
                graphDatabaseMeta.getBoltPort(),
                graphDatabaseMeta.getUsername(),
                graphDatabaseMeta.getPassword());
        this.setServer(graphDatabaseMeta.getServer());

        this.iGraphDatabase = (IGraphDatabase) graphDatabaseMeta.iGraphDatabase.clone();

        this.setChanged();
    }
*/

/*
    public void setValues(
            String name,
            String type,
            String host,
            String db,
            String port,
            String user,
            String pass) {
        try {
            iGraphDatabase = getIGraphDatabase(type);
        } catch (HopGraphDatabaseException kde) {
            throw new RuntimeException("Graph database type not found!", kde);
        }

        setName(name);
        setServer(host);
        setDatabaseName(db);
        setBoltPort(port);
        setUsername(user);
        setPassword(pass);
        setServer(null);
        setChanged(false);
    }
*/

/*
    public void setGraphDatabaseType(String type) {
        IGraphDatabase oldInterface = iGraphDatabase;

        try {
            iGraphDatabase = getIGraphDatabase(type);
        } catch (HopGraphDatabaseException kde) {
            throw new RuntimeException("Database type [" + type + "] not found!", kde);
        }

        setServer(oldInterface.getHostname());
        setDatabaseName(oldInterface.getDatabaseName());
        setBoltPort(oldInterface.getPort());
        setUsername(oldInterface.getUsername());
        setPassword(oldInterface.getPassword());
        setServer(oldInterface.getServername());
        setChanged(oldInterface.isChanged());
    }
*/

    public void setValues(GraphDatabaseMeta info) {
        iGraphDatabase = (IGraphDatabase) info.iGraphDatabase.clone();
    }

    /** @return The plugin ID of the database interface */
    public String getPluginId() {
        return iGraphDatabase.getPluginId();
    }

    /** @return The name of the database plugin type */
    public String getPluginName() {
        return iGraphDatabase.getPluginName();
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getBoltPort() {
        return boltPort;
    }

    public void setBoltPort(String boltPort) {
        this.boltPort = boltPort;
    }

    public String getBrowserPort() {
        return browserPort;
    }

    public void setBrowserPort(String browserPort) {
        this.browserPort = browserPort;
    }

    public boolean isRouting() {
        return routing;
    }

    public void setRouting(boolean routing) {
        this.routing = routing;
    }

    public String getRoutingVariable() {
        return routingVariable;
    }

    public void setRoutingVariable(String routingVariable) {
        this.routingVariable = routingVariable;
    }

    public String getRoutingPolicy() {
        return routingPolicy;
    }

    public void setRoutingPolicy(String routingPolicy) {
        this.routingPolicy = routingPolicy;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isUsingEncryption() {
        return usingEncryption;
    }

    public void setUsingEncryption(boolean usingEncryption) {
        this.usingEncryption = usingEncryption;
    }

    public String getUsingEncryptionVariable() {
        return usingEncryptionVariable;
    }

    public void setUsingEncryptionVariable(String usingEncryptionVariable) {
        this.usingEncryptionVariable = usingEncryptionVariable;
    }

    public boolean isTrustAllCertificates() {
        return trustAllCertificates;
    }

    public void setTrustAllCertificates(boolean trustAllCertificates) {
        this.trustAllCertificates = trustAllCertificates;
    }

    public String getTrustAllCertificatesVariable() {
        return trustAllCertificatesVariable;
    }

    public void setTrustAllCertificatesVariable(String trustAllCertificatesVariable) {
        this.trustAllCertificatesVariable = trustAllCertificatesVariable;
    }

    public String getConnectionLivenessCheckTimeout() {
        return connectionLivenessCheckTimeout;
    }

    public void setConnectionLivenessCheckTimeout(String connectionLivenessCheckTimeout) {
        this.connectionLivenessCheckTimeout = connectionLivenessCheckTimeout;
    }

    public String getMaxConnectionLifetime() {
        return maxConnectionLifetime;
    }

    public void setMaxConnectionLifetime(String maxConnectionLifetime) {
        this.maxConnectionLifetime = maxConnectionLifetime;
    }

    public String getMaxConnectionPoolSize() {
        return maxConnectionPoolSize;
    }

    public void setMaxConnectionPoolSize(String maxConnectionPoolSize) {
        this.maxConnectionPoolSize = maxConnectionPoolSize;
    }

    public String getConnectionAcquisitionTimeout() {
        return connectionAcquisitionTimeout;
    }

    public void setConnectionAcquisitionTimeout(String connectionAcquisitionTimeout) {
        this.connectionAcquisitionTimeout = connectionAcquisitionTimeout;
    }

    public String getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(String connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public String getMaxTransactionRetryTime() {
        return maxTransactionRetryTime;
    }

    public void setMaxTransactionRetryTime(String maxTransactionRetryTime) {
        this.maxTransactionRetryTime = maxTransactionRetryTime;
    }

    public boolean isVersion4() {
        return version4;
    }

    public void setVersion4(boolean version4) {
        this.version4 = version4;
    }

    public String getVersion4Variable() {
        return version4Variable;
    }

    public void setVersion4Variable(String version4Variable) {
        this.version4Variable = version4Variable;
    }

    public boolean isAutomatic() {
        return automatic;
    }

    public void setAutomatic(boolean automatic) {
        this.automatic = automatic;
    }

    public String getAutomaticVariable() {
        return automaticVariable;
    }

    public void setAutomaticVariable(String automaticVariable) {
        this.automaticVariable = automaticVariable;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
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

    /** @return true if the database supports a boolean, bit, logical, ... datatype */
    public boolean supportsBooleanDataType() {
        return iGraphDatabase.isSupportsBooleanDataType();
    }

    /** @param b Set to true if the database supports a boolean, bit, logical, ... datatype */
    public void setSupportsBooleanDataType(boolean b) {
        iGraphDatabase.setSupportsBooleanDataType(b);
    }

    /**
     * @return true if the database supports the Timestamp data type (nanosecond precision and all)
     */
    public boolean supportsTimestampDataType() {
        return iGraphDatabase.isSupportsTimestampDataType();
    }

    /**
     * @param b Set to true if the database supports the Timestamp data type (nanosecond precision and
     *     all)
     */
    public void setSupportsTimestampDataType(boolean b) {
        iGraphDatabase.setSupportsTimestampDataType(b);
    }

    /** @return A manually entered URL which will be used over the internally generated one */
    public String getManualUrl() {
        return iGraphDatabase.getManualUrl();
    }

    /**
     * @param manualUrl A manually entered URL which will be used over the internally generated one
     */
    public void setManualUrl(String manualUrl) {
        iGraphDatabase.setManualUrl(manualUrl);
    }

    public List<String> getManualUrls() {
        return manualUrls;
    }

    public void setManualUrls(List<String> manualUrls) {
        this.manualUrls = manualUrls;
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

    /**
     * Test this connection to a bolt/Neo4j compatible graph database
     *
     * @throws HopException In case anything goes wrong
     * @param variables
     */
    public void test(IVariables variables) throws HopException {

        try (Driver driver = getDriver(LogChannel.GENERAL, variables)) {
            SessionConfig.Builder builder = SessionConfig.builder();
            if (StringUtils.isNotEmpty(databaseName)) {
                builder = builder.withDatabase(variables.resolve(databaseName));
            }
            try (Session session = driver.session(builder.build())) {
                // Do something with the session otherwise it doesn't test the connection
                //
                Result result = session.run("RETURN 0");
                Record record = result.next();
                Value value = record.get(0);
                int zero = value.asInt();
                assert (zero == 0);
            } catch (Exception e) {
                throw new HopException(
                        "Unable to connect to database '" + name + "' : " + e.getMessage(), e);
            }
        }
    }

    public org.neo4j.driver.Driver getDriver(ILogChannel log, IVariables variables) throws HopConfigException {

        try {
            List<URI> uris = getURIs(variables);

            String realUsername = variables.resolve(username);
            String realPassword = Encr.decryptPasswordOptionallyEncrypted(variables.resolve(password));
            Config.ConfigBuilder configBuilder;

            if (!isAutomatic(variables)) {
                if (encryptionVariableSet(variables) || usingEncryption) {
                    configBuilder = Config.builder().withEncryption();
                    if (trustAllCertificatesVariableSet(variables) || trustAllCertificates) {
                        configBuilder =
                                configBuilder.withTrustStrategy(Config.TrustStrategy.trustAllCertificates());
                    }
                } else {
                    configBuilder = Config.builder().withoutEncryption();
                }
            } else {
                configBuilder = Config.builder();
            }
            if (StringUtils.isNotEmpty(connectionLivenessCheckTimeout)) {
                long seconds = Const.toLong(variables.resolve(connectionLivenessCheckTimeout), -1L);
                if (seconds > 0) {
                    configBuilder =
                            configBuilder.withConnectionLivenessCheckTimeout(seconds, TimeUnit.MILLISECONDS);
                }
            }
            if (StringUtils.isNotEmpty(maxConnectionLifetime)) {
                long seconds = Const.toLong(variables.resolve(maxConnectionLifetime), -1L);
                if (seconds > 0) {
                    configBuilder = configBuilder.withMaxConnectionLifetime(seconds, TimeUnit.MILLISECONDS);
                }
            }
            if (StringUtils.isNotEmpty(maxConnectionPoolSize)) {
                int size = Const.toInt(variables.resolve(maxConnectionPoolSize), -1);
                if (size > 0) {
                    configBuilder = configBuilder.withMaxConnectionPoolSize(size);
                }
            }
            if (StringUtils.isNotEmpty(connectionAcquisitionTimeout)) {
                long seconds = Const.toLong(variables.resolve(connectionAcquisitionTimeout), -1L);
                if (seconds > 0) {
                    configBuilder =
                            configBuilder.withConnectionAcquisitionTimeout(seconds, TimeUnit.MILLISECONDS);
                }
            }
            if (StringUtils.isNotEmpty(connectionTimeout)) {
                long seconds = Const.toLong(variables.resolve(connectionTimeout), -1L);
                if (seconds > 0) {
                    configBuilder = configBuilder.withConnectionTimeout(seconds, TimeUnit.MILLISECONDS);
                }
            }
            if (StringUtils.isNotEmpty(maxTransactionRetryTime)) {
                long seconds = Const.toLong(variables.resolve(maxTransactionRetryTime), -1L);
                if (seconds >= 0) {
                    configBuilder = configBuilder.withMaxTransactionRetryTime(seconds, TimeUnit.MILLISECONDS);
                }
            }

            // Disable info messages: only warnings and above...
            //
            configBuilder = configBuilder.withLogging(Logging.javaUtilLogging(Level.WARNING));

            Config config = configBuilder.build();

            org.neo4j.driver.Driver driver;
            if (isUsingRouting(variables)) {
                driver =
                        org.neo4j.driver.GraphDatabase.routingDriver(uris, AuthTokens.basic(realUsername, realPassword), config);
            } else {
                driver =
                        org.neo4j.driver.GraphDatabase.driver(uris.get(0), AuthTokens.basic(realUsername, realPassword), config);
            }

            // Verify connectivity at this point to ensure we're not being dishonest when testing
            //
            driver.verifyConnectivity();

            return driver;
        } catch (URISyntaxException e) {
            throw new HopConfigException(
                    "URI syntax problem, check your settings, hostnames especially.  For routing use comma separated server values.",
                    e);
        } catch (Exception e) {
            throw new HopConfigException("Error obtaining driver for a Neo4j connection", e);
        }
    }

    public boolean isUsingRouting(IVariables variables) {
        if (!Utils.isEmpty(routingVariable)) {
            String value = variables.resolve(routingVariable);
            if (!Utils.isEmpty(value)) {
                return ValueMetaBase.convertStringToBoolean(value);
            }
        }
        return routing;
    }

    public List<URI> getURIs(IVariables variables) throws URISyntaxException {

        List<URI> uris = new ArrayList<>();

        if (manualUrls != null && !manualUrls.isEmpty()) {
            // A manual URL is specified
            //
            for (String manualUrl : manualUrls) {
                uris.add(new URI(manualUrl));
            }
        } else {
            // Construct the URIs from the entered values
            //
            List<String> serverStrings = new ArrayList<>();
            String serversString = variables.resolve(server);
            if (!isAutomatic(variables) && isUsingRouting(variables)) {
                Collections.addAll(serverStrings, serversString.split(","));
            } else {
                serverStrings.add(serversString);
            }

            for (String serverString : serverStrings) {
                // Trim excess spaces from server name
                //
                String url = getUrl(Const.trim(serverString), variables);
                uris.add(new URI(url));
            }
        }

        return uris;
    }

    public String getUrl(String hostname, IVariables variables) {

        /*
         * Construct the following URL:
         *
         * neo4://hostname:port
         * bolt://hostname:port
         * bolt+routing://core-server:port/?policy=MyPolicy
         */
        String url = "";
        if (StringUtils.isEmpty(protocol)) {
            if (isAutomatic(variables) || isUsingRouting(variables)) {
                url += "neo4j";
            } else {
                url += "bolt";
            }
        } else {
            url += variables.resolve(protocol);
        }
        url += "://";

        // Hostname
        //
        url += hostname;

        // Port
        //
        if (StringUtils.isNotEmpty(boltPort) && hostname != null && !hostname.contains(":")) {
            url += ":" + variables.resolve(boltPort);
        }

        String routingPolicyString = variables.resolve(routingPolicy);

        // We don't add these options if the automatic flag is set
        //
        if (!isAutomatic(variables)
                && isUsingRouting(variables)
                && StringUtils.isNotEmpty(routingPolicyString)) {
            try {
                url += "?policy=" + URLEncoder.encode(routingPolicyString, "UTF-8");
            } catch (Exception e) {
                LogChannel.GENERAL.logError(
                        "Error encoding routing policy context '" + routingPolicyString + "' in connection URL",
                        e);
                url += "?policy=" + routingPolicyString;
            }
        }

        return url;
    }

    public boolean encryptionVariableSet(IVariables variables) {
        if (!Utils.isEmpty(usingEncryptionVariable)) {
            String value = variables.resolve(usingEncryptionVariable);
            if (!Utils.isEmpty(value)) {
                return ValueMetaBase.convertStringToBoolean(value);
            }
        }
        return false;
    }

    public boolean trustAllCertificatesVariableSet(IVariables variables) {
        if (!Utils.isEmpty(trustAllCertificatesVariable)) {
            String value = variables.resolve(trustAllCertificatesVariable);
            if (!Utils.isEmpty(value)) {
                return ValueMetaBase.convertStringToBoolean(value);
            }
        }
        return false;
    }

    /**
     * Checks both the automaticVariable String and automatic boolean to see if this connection is to
     * be configured automatically.
     *
     * @param variables Used to resolve variable expressions
     * @return True if the connection is to be configured automatically.
     */
    public boolean isAutomatic(IVariables variables) {
        if (StringUtils.isEmpty(automaticVariable)) {
            return isAutomatic();
        } else {
            String automaticString = variables.resolve(automaticVariable);
            Boolean auto = ValueMetaBase.convertStringToBoolean(automaticString);
            return auto != null && auto;
        }
    }

    /**
     * Get a list of all URLs, not just the first in case of routing.
     *
     * @return
     * @param variables
     */
    public String getUrl(IVariables variables) {
        StringBuffer urls = new StringBuffer();
        try {
            for (URI uri : getURIs(variables)) {
                if (urls.length() > 0) {
                    urls.append(",");
                }
                urls.append(uri.toString());
            }
        } catch (Exception e) {
            urls.append("ERROR building URLs: " + e.getMessage());
        }
        return urls.toString();
    }
}
