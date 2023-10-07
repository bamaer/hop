package org.apache.hop.graphdatabases.nebula;

import org.apache.hop.core.exception.HopConfigException;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.graph.BaseGraphDatabaseMeta;
import org.apache.hop.core.graph.GraphDatabaseMetaPlugin;
import org.apache.hop.core.graph.GraphDatabaseTestResults;
import org.apache.hop.core.graph.IGraphDatabase;
import org.apache.hop.core.gui.plugin.GuiPlugin;
import org.apache.hop.core.variables.IVariables;

@GraphDatabaseMetaPlugin(
        type = "NEBULA",
        typeDescription = "Nebula Graph",
        documentationUrl = ""
)
@GuiPlugin(
        id = "GUI-NebulaGraphDatabaseMeta"
)
public class NebulaGraphDatabaseMeta extends BaseGraphDatabaseMeta implements IGraphDatabase {

    @Override
    public GraphDatabaseTestResults testConnectionSuccess(IVariables variables) throws HopConfigException, HopException {
        GraphDatabaseTestResults testResults = new GraphDatabaseTestResults();
        testResults.setSuccess(true);
        testResults.setMessage("Nebula Graph test to be implemented");
        return testResults;
    }
}