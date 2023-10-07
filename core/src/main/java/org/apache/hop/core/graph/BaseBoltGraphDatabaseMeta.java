package org.apache.hop.core.graph;

import org.apache.commons.lang.StringUtils;
import org.apache.hop.core.Const;
import org.apache.hop.core.encryption.Encr;
import org.apache.hop.core.exception.HopConfigException;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.gui.plugin.GuiElementType;
import org.apache.hop.core.gui.plugin.GuiWidgetElement;
import org.apache.hop.core.logging.ILogChannel;
import org.apache.hop.core.logging.LogChannel;
import org.apache.hop.core.row.value.ValueMetaBase;
import org.apache.hop.core.util.Utils;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.metadata.api.HopMetadataProperty;
import org.apache.hop.metadata.api.IHopMetadataProvider;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Config;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class BaseBoltGraphDatabaseMeta extends BaseGraphDatabaseMeta implements IBoltGraphDatabase{

    @GuiWidgetElement(
            id = "neo4jVersion",
            order = "10",
            parentId = GraphDatabaseMeta.GUI_PLUGIN_ELEMENT_PARENT_ID,
            type = GuiElementType.COMBO,
            variables = false,
            comboValuesMethod = "getNeo4jVersions",
            label = "i18n:org.apache.hop.ui.core.graph:Neo4jGraphDatabaseMeta.Neo4jVersion.Label"
    )
    @HopMetadataProperty(key = "neo4jVersion")
    private String neo4jVersion = "Neo4j 5";

    @HopMetadataProperty
    @GuiWidgetElement(
            id = "automatic",
            order = "01",
            label = "i18n:org.apache.hop.ui.core.graph:GraphDatabaseDialog.Automatic.Label",
            type = GuiElementType.CHECKBOX,
            variables = true,
            parentId = GraphDatabaseMeta.GUI_PLUGIN_ELEMENT_PARENT_ID
    )
    private boolean automatic;

    @HopMetadataProperty
    @GuiWidgetElement(
            id = "protocol",
            order = "02",
            label = "i18n:org.apache.hop.ui.core.graph:GraphDatabaseDialog.Protocol.Label",
            type = GuiElementType.COMBO,
            variables = true,
            comboValuesMethod = "getSupportedProtocols",
            parentId = GraphDatabaseMeta.GUI_PLUGIN_ELEMENT_PARENT_ID
    )
    private String protocol;

    @HopMetadataProperty
    @GuiWidgetElement(
            id = "boltPort",
            order = "04",
            label = "i18n:org.apache.hop.ui.core.graph:GraphDatabaseDialog.BoltPort.Label",
            type = GuiElementType.TEXT,
            variables = true,
            parentId = GraphDatabaseMeta.GUI_PLUGIN_ELEMENT_PARENT_ID)
    protected String boltPort;

    @HopMetadataProperty
    private String defaultBoltPort;

    @HopMetadataProperty
/*
    @GuiWidgetElement(
            id = "routing",
            order = "",
            label = "i18n:org.apache.hop.ui.core.graphdatabase:GraphDatabaseDialog.label.Routing",
            type = GuiElementType.TEXT,
            variables = true,
            parentId = GraphDatabaseMeta.GUI_PLUGIN_ELEMENT_PARENT_ID
    )
*/
    private boolean routing;

    @HopMetadataProperty
    private String browserPort;

    @HopMetadataProperty
    private String routingVariable;

    @HopMetadataProperty
    private String routingPolicy;

    @HopMetadataProperty private boolean usingEncryption;

    @HopMetadataProperty private String usingEncryptionVariable;

    @HopMetadataProperty private boolean trustAllCertificates;

    @HopMetadataProperty private String trustAllCertificatesVariable;

    @HopMetadataProperty private List<String> manualUrls;

    @HopMetadataProperty private String connectionLivenessCheckTimeout;

    @HopMetadataProperty private String maxConnectionLifetime;

    @HopMetadataProperty private String maxConnectionPoolSize;

    @HopMetadataProperty private String connectionAcquisitionTimeout;

    @HopMetadataProperty private String connectionTimeout;

    @HopMetadataProperty private String maxTransactionRetryTime;

    @HopMetadataProperty private boolean version4;

    @HopMetadataProperty private String version4Variable;

    @HopMetadataProperty private String automaticVariable;


    public BaseBoltGraphDatabaseMeta(){
        super();
        manualUrls = new ArrayList<>();
        defaultBoltPort = "666";
        boltPort = "7687";
        automatic = true;
    }

    public List<String> getNeo4jVersions(ILogChannel log, IHopMetadataProvider metadataProvider){
        List<String> versions = new ArrayList<>();
        versions.add("Neo4j 4");
        versions.add("Neo4j 5");
        return versions;
    }

    public String getNeo4jVersion(){
        return neo4jVersion;
    }

    public void setNeo4jVersion(String neo4jVersion){
        this.neo4jVersion = neo4jVersion;
    }

    @Override
    public boolean isAutomatic() {
        return automatic;
    }

    @Override
    public void setAutomatic(boolean automatic) {
        this.automatic = automatic;
    }

    @Override
    public String getBrowserPort() {
        return browserPort;
    }

    @Override
    public void setBrowserPort(String browserPort) {
        this.browserPort = browserPort;
    }

    @Override
    public String getDatabaseName() {
        return databaseName;
    }

    @Override
    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    @Override
    public String getHostname() {
        return hostname;
    }

    @Override
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    @Override
    public boolean isSupportsBooleanDataType() {
        return true;
    }

    @Override
    public void setSupportsBooleanDataType(boolean b) {
    }

    @Override
    public boolean isSupportsTimestampDataType() {
        return true;
    }

    @Override
    public void setSupportsTimestampDataType(boolean b) {

    }

    @Override
    public List<String> getManualUrls() {
        return manualUrls;
    }

    @Override
    public void setManualUrls(List<String> manualUrls) {
        this.manualUrls = manualUrls;
    }

    @Override
    public String getDefaultBoltPort() {
        return defaultBoltPort;
    }

    @Override
    public void setDefaultBoltPort(String defaultBoltPort){
        this.defaultBoltPort = defaultBoltPort;
    }

    @Override
    public void setBoltPort(String boltPort) {
        this.boltPort = boltPort;
    }

    @Override
    public String getBoltPort(){ return boltPort; }

    @Override
    public boolean isRouting() {
        return routing;
    }

    @Override
    public void setRouting(boolean routing) {
        this.routing = routing;
    }

    @Override
    public String isRoutingVariable() {
        return routingVariable;
    }

    @Override
    public void setRoutingVariable(String routingVariable) {
        this.routingVariable = routingVariable;
    }

    @Override
    public String getRoutingPolicy() {
        return routingPolicy;
    }

    @Override
    public void setRoutingPolicy(String routingPolicy) {
        this.routingPolicy = routingPolicy;
    }

    @Override
    public boolean isUsingEncryption() {
        return usingEncryption;
    }

    @Override
    public void setUsingEncryption(boolean usingEncryption) {
        this.usingEncryption = usingEncryption;
    }

    @Override
    public String getUsingEncryptionVariable() {
        return usingEncryptionVariable;
    }

    @Override
    public void setUsingEncryptionVariable(String usingEncryptionVariable) {
        this.usingEncryptionVariable = usingEncryptionVariable;
    }

    @Override
    public boolean isTrustAllCertificates() {
        return trustAllCertificates;
    }

    @Override
    public void setTrustAllCertificates(boolean trustAllCertificates) {
        this.trustAllCertificates = trustAllCertificates;
    }

    @Override
    public String getTrustAllCertificatesVariable() {
        return trustAllCertificatesVariable;
    }

    @Override
    public void setTrustAllCertificatesVariable(String trustAllCertificatesVariable) {
        this.trustAllCertificatesVariable = trustAllCertificatesVariable;
    }

    @Override
    public String getConnectionLivenessCheckTimeout() {
        return connectionLivenessCheckTimeout;
    }

    @Override
    public void setConnectionLivenessCheckTimeout(String connectionLivenessCheckTimeout) {
        this.connectionLivenessCheckTimeout = connectionLivenessCheckTimeout;
    }

    @Override
    public String getMaxConnectionLifetime() {
        return maxConnectionLifetime;
    }

    @Override
    public void setMaxConnectionLifetime(String maxConnectionLifetime) {
        this.maxConnectionLifetime = maxConnectionLifetime;
    }

    @Override
    public String getMaxConnectionPoolSize() {
        return maxConnectionPoolSize;
    }

    @Override
    public void setMaxConnectionPoolSize(String maxConnectionPoolSize) {
        this.maxConnectionPoolSize = maxConnectionPoolSize;
    }

    @Override
    public String getConnectionAcquisitionTimeout() {
        return connectionAcquisitionTimeout;
    }

    @Override
    public void setConnectionAcquisitionTimeout(String connectionAcquisitionTimeout) {
        this.connectionAcquisitionTimeout = connectionAcquisitionTimeout;
    }

    @Override
    public String getConnectionTimeout() {
        return connectionTimeout;
    }

    @Override
    public void setConnectionTimeout(String connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    @Override
    public String getMaxTransactionRetryTime() {
        return maxTransactionRetryTime;
    }

    @Override
    public void setMaxTransactionRetryTime(String maxTransactionRetryTime) {
        this.maxTransactionRetryTime = maxTransactionRetryTime;
    }

    @Override
    public boolean isVersion4() {
        return version4;
    }

    @Override
    public void setVersion4(boolean version4) {
        this.version4 = version4;
    }

    @Override
    public String getVersion4Variable() {
        return version4Variable;
    }

    @Override
    public void setVersion4Variable(String version4Variable) {
        this.version4Variable = version4Variable;
    }

    @Override
    public String getAutomaticVariable() {
        return automaticVariable;
    }

    @Override
    public void setAutomaticVariable(String automaticVariable) {
        this.automaticVariable = automaticVariable;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public void setProtocol(String protocol) {
        this.protocol = protocol;
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
    public boolean isSupportsRangeIndex() {
        return false;
    }

    @Override
    public boolean isSupportsLookupIndex() {
        return false;
    }

    @Override
    public boolean isSupportsTextIndex() {
        return false;
    }

    @Override
    public boolean isSupportsPointIndex() {
        return false;
    }

    @Override
    public boolean isSupportsFullTextIndex() {
        return false;
    }

    @Override
    public boolean isSupportsBTreeIndex() {
        return false;
    }

    @Override
    public GraphDatabaseTestResults testConnectionSuccess(IVariables variables) throws HopConfigException, HopException{
        GraphDatabaseTestResults testResults = new GraphDatabaseTestResults();
        boolean success = true;
        String message = "";
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
                message += "connection to " + databaseName + " tested successfully";
            } catch (Exception e) {
                message += "error connecting to " + databaseName + Const.CR;
                message += Const.getStackTracker(e) + Const.CR;
                success = false;
                throw new HopException(
                        "Unable to connect to database '" + databaseName + "' : " + e.getMessage(), e);
            }
        }catch(HopConfigException e){
            message += "error getting driver for " + databaseName + Const.CR;
            message += Const.getStackTracker(e) + Const.CR;
            success = false;
            throw new HopConfigException("Unable to get driver for " + databaseName + ". " + e.getMessage(), e);
        }
        testResults.setMessage(message);
        testResults.setSuccess(success);
        return testResults;
    }

    public Driver getDriver(ILogChannel log, IVariables variables) throws HopConfigException {

        try {
            List<URI> uris = getURIs(variables);

            String realUsername = variables.resolve(username);
            String realPassword = Encr.decryptPasswordOptionallyEncrypted(variables.resolve(password));
            Config.ConfigBuilder configBuilder;

            if (!isAutomatic()) {
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

            Driver driver;
            if (isUsingRouting(variables)) {
                driver =
                        org.neo4j.driver.GraphDatabase.routingDriver(uris, AuthTokens.basic(realUsername, realPassword), config);
            } else {
                driver =
                        GraphDatabase.driver(uris.get(0), AuthTokens.basic(realUsername, realPassword), config);
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
            String serversString = variables.resolve(hostname);
            if (!isAutomatic() && isUsingRouting(variables)) {
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
            if (isAutomatic() || isUsingRouting(variables)) {
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
        if (!isAutomatic()
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

    public boolean isUsingRouting(IVariables variables) {
        if (!Utils.isEmpty(routingVariable)) {
            String value = variables.resolve(routingVariable);
            if (!Utils.isEmpty(value)) {
                return ValueMetaBase.convertStringToBoolean(value);
            }
        }
        return routing;
    }
}
