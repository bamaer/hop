package org.apache.hop.graphdatabases.nebula;

import org.apache.hop.core.graph.BaseGraphDatabaseMeta;
import org.apache.hop.core.graph.GraphDatabaseMetaPlugin;
import org.apache.hop.core.graph.IGraphDatabase;
import org.apache.hop.core.gui.plugin.GuiPlugin;

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
    public boolean isSupportsTimestampDataType() {
        return false;
    }

    @Override
    public void setSupportsTimestampDataType(boolean b) {

    }
}
