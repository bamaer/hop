package org.apache.hop.core.graph;

import org.apache.hop.core.gui.plugin.GuiElementType;
import org.apache.hop.core.gui.plugin.GuiWidgetElement;
import org.apache.hop.metadata.api.HopMetadataProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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


    public BaseBoltGraphDatabaseMeta(){
        super();
        manualUrls = new ArrayList<>();
        defaultBoltPort = "666";
        boltPort = "7687";
        automatic = true;
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
    public String getServername() {
        return null;
    }

    @Override
    public void setServername(String servername) {

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
}
