package org.apache.hop.core.graph;

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
import org.neo4j.driver.Session;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
}
