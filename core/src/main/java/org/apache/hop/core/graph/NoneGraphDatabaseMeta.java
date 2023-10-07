package org.apache.hop.core.graph;


import org.apache.hop.core.exception.HopConfigException;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.variables.IVariables;

@GraphDatabaseMetaPlugin(
        type = "NONEGRAPH",
        typeDescription = "No graph connection type",
        documentationUrl = ""
)
public class NoneGraphDatabaseMeta extends BaseGraphDatabaseMeta implements IGraphDatabase {

    @Override
    public GraphDatabaseTestResults testConnectionSuccess(IVariables variables) throws HopConfigException, HopException {
        GraphDatabaseTestResults testResults = new GraphDatabaseTestResults();
        testResults.setSuccess(true);
        testResults.setMessage("nothing to test");
        return testResults;
    }
}