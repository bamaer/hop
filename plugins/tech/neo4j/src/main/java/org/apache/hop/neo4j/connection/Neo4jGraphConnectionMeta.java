package org.apache.hop.neo4j.connection;

import org.apache.hop.core.graph.BaseGraphDatabaseMeta;
import org.apache.hop.core.graph.GraphDatabaseMetaPlugin;
import org.apache.hop.core.graph.IGraphDatabase;
import org.apache.hop.core.gui.plugin.GuiPlugin;

@GraphDatabaseMetaPlugin(
        type = "NEO4J",
        typeDescription = "Neo4J",
        classLoaderGroup = "neo4j-db"
)
@GuiPlugin(
        id = "GUI-Neo4jGraphDatabaseMeta"
)
public class Neo4jGraphConnectionMeta extends BaseGraphDatabaseMeta implements IGraphDatabase {


}
