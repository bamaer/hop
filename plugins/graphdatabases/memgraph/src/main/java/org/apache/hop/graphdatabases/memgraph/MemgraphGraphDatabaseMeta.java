package org.apache.hop.graphdatabases.memgraph;

import org.apache.hop.core.graph.BaseBoltGraphDatabaseMeta;
import org.apache.hop.core.graph.BaseGraphDatabaseMeta;
import org.apache.hop.core.graph.GraphDatabaseMetaPlugin;
import org.apache.hop.core.graph.IBoltGraphDatabase;
import org.apache.hop.core.graph.IGraphDatabase;
import org.apache.hop.core.gui.plugin.GuiPlugin;

@GraphDatabaseMetaPlugin(
        type = "MEMGRAPH",
        typeDescription = "Memgraph",
        documentationUrl = ""
)
@GuiPlugin(id = "GUI-MemgraphGraphDatabaseMeta")
public class MemgraphGraphDatabaseMeta  extends BaseBoltGraphDatabaseMeta implements IBoltGraphDatabase {

}
