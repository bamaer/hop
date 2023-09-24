package org.apache.hop.core.graph;

import org.apache.hop.core.Const;
import org.apache.hop.core.exception.HopConfigException;
import org.apache.hop.core.logging.ILoggingObject;
import org.apache.hop.core.logging.LogChannel;
import org.apache.hop.core.logging.LoggingObjectType;
import org.apache.hop.core.logging.SimpleLoggingObject;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.i18n.BaseMessages;
import org.neo4j.driver.Driver;

public class GraphDatabaseFactory implements IGraphDatabaseFactory {

    private static final Class<?> PKG = GraphDatabase.class;
    private boolean success;

    public static final ILoggingObject logginObject =
        new SimpleLoggingObject("Graph DatabaseFactor", LoggingObjectType.GENERAL, null);


    public GraphDatabaseFactory(){}

    @Override
    public String getConnectionTestReport(IVariables variables, GraphDatabaseMeta graphDatabaseMeta) throws HopGraphDatabaseException {
        success = true;

        StringBuilder report = new StringBuilder();

        GraphDatabase graphDatabase = new GraphDatabase(logginObject, variables, graphDatabaseMeta);

        try(Driver driver = graphDatabase.getDriver(LogChannel.GENERAL, variables)){
            report.append(BaseMessages.getString(PKG, "GraphDatabaseMeta.report.ConnectionOK", graphDatabaseMeta.getName()) + Const.CR);
        } catch (HopConfigException e) {
            report.append(BaseMessages.getString(PKG, "GraphDatabaseMeta.report.ConnectionError", graphDatabaseMeta.getName()) + Const.CR);
            report.append(Const.getStackTracker(e) + Const.CR);
            success = false;
        } finally {
            graphDatabase.disconnect();
        }

        return null;
    }

    @Override
    public GraphDatabaseTestResults getConnectionTestResults(IVariables variables, GraphDatabaseMeta graphDatabaseMeta) throws HopGraphDatabaseException {
        GraphDatabaseTestResults testResults = new GraphDatabaseTestResults();
        String message = getConnectionTestReport(variables, graphDatabaseMeta);
        testResults.setMessage(message);
        testResults.setSuccess(success);
        return testResults;
    }

}
