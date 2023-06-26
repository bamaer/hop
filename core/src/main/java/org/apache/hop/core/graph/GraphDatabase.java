package org.apache.hop.core.graph;

import org.apache.hop.core.exception.HopValueException;
import org.apache.hop.core.logging.ILoggingObject;
import org.apache.hop.core.logging.LogLevel;
import org.apache.hop.core.logging.LoggingObjectType;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.variables.IVariables;

import java.util.Date;
import java.util.Map;

/**
 * Graph Database handles the process of connecting to, reading from, writing to and updating graph databases.
 * The graph database specific parameters are defined in DatabaseInfo.
 */
public class GraphDatabase implements IVariables, ILoggingObject, AutoCloseable {

    private static final Class<?> PKG = GraphDatabase.class;

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
}
