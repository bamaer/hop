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
import org.apache.hop.core.Const;
import org.apache.hop.core.encryption.Encr;
import org.apache.hop.core.exception.HopConfigException;
import org.apache.hop.core.exception.HopValueException;
import org.apache.hop.core.extension.ExtensionPointHandler;
import org.apache.hop.core.extension.HopExtensionPoint;
import org.apache.hop.core.logging.DefaultLogLevel;
import org.apache.hop.core.logging.ILogChannel;
import org.apache.hop.core.logging.ILoggingObject;
import org.apache.hop.core.logging.LogChannel;
import org.apache.hop.core.logging.LogLevel;
import org.apache.hop.core.logging.LoggingObjectType;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.IValueMeta;
import org.apache.hop.core.row.value.ValueMetaFactory;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.core.variables.Variables;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Config;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Logging;
import org.neo4j.driver.Session;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Graph Database handles the process of connecting to, reading from, writing to and updating graph databases.
 * The graph database specific parameters are defined in DatabaseInfo.
 */
public class GraphDatabase implements IVariables, ILoggingObject, AutoCloseable {

    private static final Class<?> PKG = GraphDatabase.class;

    private ILogChannel log;
    private ILoggingObject parentLoggingObject;
    private IVariables variables = new Variables();
    private LogLevel logLevel = DefaultLogLevel.getLogLevel();
    private static final Map<String, Set<String>> registeredDrivers = new HashMap<>();
    private String containerObjectId;
    private int nrExecutedCommits;
    private static List<IValueMeta> valueMetaPluginClasses;

    private Session session;

    static {
        try {
            valueMetaPluginClasses = ValueMetaFactory.getValueMetaPluginClasses();
            Collections.sort(
                    valueMetaPluginClasses,
                    (o1, o2) ->
                            // Reverse the sort list
                            (Integer.valueOf(o1.getType()).compareTo(Integer.valueOf(o2.getType()))) * -1);
        }catch(Exception e){
            throw new RuntimeException("Unable to get list of instantiated value meta plugin classes", e);
        }
    }

    private final GraphDatabaseMeta graphDatabaseMeta;

    public GraphDatabase(ILoggingObject parentObject, IVariables variables, GraphDatabaseMeta graphDatabaseMeta){
        this.parentLoggingObject = parentObject;
        this.variables = variables;
        this.graphDatabaseMeta = graphDatabaseMeta;

        log = new LogChannel(this, parentObject);
        this.containerObjectId = log.getContainerObjectId();
        this.logLevel = log.getLogLevel();
        if(parentObject != null){
            log.setGatheringMetrics(parentObject.isGatheringMetrics());
        }

        try{
            ExtensionPointHandler.callExtensionPoint(
                    log, variables, HopExtensionPoint.GraphDatabaseCreated.id, this);
        }catch(Exception e){
            throw new RuntimeException("Error calling extension point while creating graph database connection", e);
        }

        if(log.isDetailed()){
            log.logDetailed("New graph database connection defined");
        }
    }

    @Override
    public boolean equals(Object obj){
        GraphDatabase other = (GraphDatabase) obj;
        if(other == null){
            return false;
        }else{
            return this.graphDatabaseMeta.equals(other.graphDatabaseMeta);
        }
    }

    @Override
    public void close() throws Exception {

    }

    @Override
    public String getObjectName() {
        return null;
    }

    @Override
    public String getFilename() {
        return null;
    }

    @Override
    public String getLogChannelId() {
        return null;
    }

    @Override
    public ILoggingObject getParent() {
        return null;
    }

    @Override
    public LoggingObjectType getObjectType() {
        return null;
    }

    @Override
    public String getObjectCopy() {
        return null;
    }

    @Override
    public LogLevel getLogLevel() {
        return null;
    }

    @Override
    public String getContainerId() {
        return null;
    }

    @Override
    public Date getRegistrationDate() {
        return null;
    }

    @Override
    public boolean isGatheringMetrics() {
        return false;
    }

    @Override
    public void setGatheringMetrics(boolean gatheringMetrics) {

    }

    @Override
    public void setForcingSeparateLogging(boolean forcingSeparateLogging) {

    }

    @Override
    public boolean isForcingSeparateLogging() {
        return false;
    }

    @Override
    public void initializeFrom(IVariables parent) {

    }

    @Override
    public void copyFrom(IVariables variables) {

    }

    @Override
    public void shareWith(IVariables variables) {

    }

    @Override
    public IVariables getParentVariables() {
        return null;
    }

    @Override
    public void setParentVariables(IVariables parent) {

    }

    @Override
    public void setVariable(String variableName, String variableValue) {

    }

    @Override
    public String getVariable(String variableName, String defaultValue) {
        return null;
    }

    @Override
    public String getVariable(String variableName) {
        return null;
    }

    @Override
    public boolean getVariableBoolean(String variableName, boolean defaultValue) {
        return false;
    }

    @Override
    public String[] getVariableNames() {
        return new String[0];
    }

    @Override
    public String resolve(String aString) {
        return null;
    }

    @Override
    public String[] resolve(String[] string) {
        return new String[0];
    }

    @Override
    public void setVariables(Map<String, String> map) {

    }

    @Override
    public String resolve(String aString, IRowMeta rowMeta, Object[] rowData) throws HopValueException {
        return null;
    }

    public Session getSession(){
        return session;
    }

    public void setSession(Session session){
        this.session = session;
    }

    public Driver getDriver(ILogChannel log, IVariables variables) throws HopConfigException {

        try {
            List<URI> uris = getURIs(variables);

            String realUsername = variables.resolve(graphDatabaseMeta.getUsername());
            String realPassword = Encr.decryptPasswordOptionallyEncrypted(variables.resolve(graphDatabaseMeta.getPassword()));
            Config.ConfigBuilder configBuilder;

/*
            if (!graphDatabaseMeta.isAutomatic()) {
                if (!StringUtils.isEmpty(graphDatabaseMeta.getUsingEncryptionVariable()) || graphDatabaseMeta.isUsingEncryption()) {
                    configBuilder = Config.builder().withEncryption();
                    if (!StringUtils.isEmpty(graphDatabaseMeta.getTrustAllCertificatesVariable()) || graphDatabaseMeta.isTrustAllCertificates()) {
                        configBuilder =
                                configBuilder.withTrustStrategy(Config.TrustStrategy.trustAllCertificates());
                    }
                } else {
                    configBuilder = Config.builder().withoutEncryption();
                }
            } else {
                configBuilder = Config.builder();
            }
            if (StringUtils.isNotEmpty(graphDatabaseMeta.getConnectionLivenessCheckTimeout())) {
                long seconds = Const.toLong(variables.resolve(graphDatabaseMeta.getConnectionLivenessCheckTimeout()), -1L);
                if (seconds > 0) {
                    configBuilder =
                            configBuilder.withConnectionLivenessCheckTimeout(seconds, TimeUnit.MILLISECONDS);
                }
            }
            if (StringUtils.isNotEmpty(graphDatabaseMeta.getMaxConnectionLifetime())) {
                long seconds = Const.toLong(variables.resolve(graphDatabaseMeta.getMaxConnectionLifetime()), -1L);
                if (seconds > 0) {
                    configBuilder = configBuilder.withMaxConnectionLifetime(seconds, TimeUnit.MILLISECONDS);
                }
            }
            if (StringUtils.isNotEmpty(graphDatabaseMeta.getMaxConnectionPoolSize())) {
                int size = Const.toInt(variables.resolve(graphDatabaseMeta.getMaxConnectionPoolSize()), -1);
                if (size > 0) {
                    configBuilder = configBuilder.withMaxConnectionPoolSize(size);
                }
            }
            if (StringUtils.isNotEmpty(graphDatabaseMeta.getConnectionAcquisitionTimeout())) {
                long seconds = Const.toLong(variables.resolve(graphDatabaseMeta.getConnectionAcquisitionTimeout()), -1L);
                if (seconds > 0) {
                    configBuilder =
                            configBuilder.withConnectionAcquisitionTimeout(seconds, TimeUnit.MILLISECONDS);
                }
            }
            if (StringUtils.isNotEmpty(graphDatabaseMeta.getConnectionTimeout())) {
                long seconds = Const.toLong(variables.resolve(graphDatabaseMeta.getConnectionTimeout()), -1L);
                if (seconds > 0) {
                    configBuilder = configBuilder.withConnectionTimeout(seconds, TimeUnit.MILLISECONDS);
                }
            }
            if (StringUtils.isNotEmpty(graphDatabaseMeta.getMaxTransactionRetryTime())) {
                long seconds = Const.toLong(variables.resolve(graphDatabaseMeta.getMaxTransactionRetryTime()), -1L);
                if (seconds >= 0) {
                    configBuilder = configBuilder.withMaxTransactionRetryTime(seconds, TimeUnit.MILLISECONDS);
                }
            }
*/

            // Disable info messages: only warnings and above...
            //
            configBuilder = Config.builder();
            configBuilder = configBuilder.withLogging(Logging.javaUtilLogging(Level.WARNING));

            Config config = configBuilder.build();

            org.neo4j.driver.Driver driver;
/*
            if (graphDatabaseMeta.isRouting()) {
                driver =
                        org.neo4j.driver.GraphDatabase.routingDriver(uris, AuthTokens.basic(realUsername, realPassword), config);
            } else {
                driver =
                        org.neo4j.driver.GraphDatabase.driver(uris.get(0), AuthTokens.basic(realUsername, realPassword), config);
            }
*/
            driver = org.neo4j.driver.GraphDatabase.driver(uris.get(0), AuthTokens.basic(realUsername, realPassword), config);

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

/*
        if (graphDatabaseMeta.getManualUrls() != null && !graphDatabaseMeta.getManualUrls().isEmpty()) {
            // A manual URL is specified
            //
            for (String manualUrl : graphDatabaseMeta.getManualUrls()) {
                uris.add(new URI(manualUrl));
            }
        } else {
            // Construct the URIs from the entered values
            //
            List<String> serverStrings = new ArrayList<>();
            String serversString = variables.resolve(graphDatabaseMeta.getServer());
            if (!graphDatabaseMeta.isAutomatic() && graphDatabaseMeta.isRouting()) {
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
*/

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
/*
        if (StringUtils.isEmpty(graphDatabaseMeta.getProtocol())) {
            if (graphDatabaseMeta.isAutomatic() || graphDatabaseMeta.isRouting()) {
                url += "neo4j";
            } else {
                url += "bolt";
            }
        } else {
            url += variables.resolve(graphDatabaseMeta.getProtocol());
        }
*/
        url += "://";

        // Hostname
        //
        url += hostname;

        // Port
        //
/*
        if (StringUtils.isNotEmpty(graphDatabaseMeta.getBoltPort()) && hostname != null && !hostname.contains(":")) {
            url += ":" + variables.resolve(graphDatabaseMeta.getBoltPort());
        }

        String routingPolicyString = variables.resolve(graphDatabaseMeta.getRoutingPolicy());

        // We don't add these options if the automatic flag is set
        //
        if (!graphDatabaseMeta.isAutomatic()
                && graphDatabaseMeta.isRouting()
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
*/

        return url;
    }

    public void disconnect(){
        // clean up and close the session.
    }
}
