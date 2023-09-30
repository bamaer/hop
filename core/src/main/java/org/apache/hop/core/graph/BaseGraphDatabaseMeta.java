package org.apache.hop.core.graph;

import org.apache.hop.core.database.DatabaseFactory;
import org.apache.hop.core.database.DatabaseMeta;
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

    private String driverClass = "org.neo4j.driver.Driver";

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
            id = "hostname",
            order = "03",
            label = "i18n:org.apache.hop.ui.core.graph:GraphDatabaseDialog.ServerHostname.Label",
            type = GuiElementType.TEXT,
            variables = true,
            parentId = GraphDatabaseMeta.GUI_PLUGIN_ELEMENT_PARENT_ID)
    protected String hostname;

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
    @GuiWidgetElement(
            id = "databaseName",
            order = "05",
            label = "i18n:org.apache.hop.ui.core.graph:GraphDatabaseDialog.DatabaseName.Label",
            type = GuiElementType.TEXT,
            variables = true,
            parentId = GraphDatabaseMeta.GUI_PLUGIN_ELEMENT_PARENT_ID)
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
            parentId = GraphDatabaseMeta.GUI_PLUGIN_ELEMENT_PARENT_ID
    )
    protected String password;

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
    private boolean routingVariable;

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


    private boolean changed;

    @HopMetadataProperty protected Map<String, String> attributes;

    @HopMetadataProperty protected String pluginId;
    @HopMetadataProperty protected String pluginName;

    public BaseGraphDatabaseMeta(){
        attributes = Collections.synchronizedMap(new HashMap<>());
        manualUrls = new ArrayList<>();
        changed = false;
        defaultBoltPort = "666";
        boltPort = "7687";
        automatic = true;
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
    public boolean isRoutingVariable() {
        return routingVariable;
    }

    @Override
    public void setRoutingVariable(boolean routingVariable) {
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
    public boolean isAutomatic() {
        return automatic;
    }

    @Override
    public void setAutomatic(boolean automatic) {
        this.automatic = automatic;
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
        return DatabaseFactory.class.getName();
    }

    public List<String> getSupportedProtocols(ILogChannel log, IHopMetadataProvider metadataProvider){
        List<String> protocols = new ArrayList<>();
        protocols.add("neo4j");
        protocols.add("bolt");
        return protocols;
    }
}
