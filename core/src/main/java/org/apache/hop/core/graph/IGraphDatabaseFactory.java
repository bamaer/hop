package org.apache.hop.core.graph;

import org.apache.hop.core.variables.IVariables;

public interface IGraphDatabaseFactory {

    String getConnectionTestReport(IVariables variables, GraphDatabaseMeta graphDatabaseMeta)
        throws HopGraphDatabaseException;

    GraphDatabaseTestResults getConnectionTestResults(IVariables variables, GraphDatabaseMeta graphDatabaseMeta)
        throws HopGraphDatabaseException;

}
