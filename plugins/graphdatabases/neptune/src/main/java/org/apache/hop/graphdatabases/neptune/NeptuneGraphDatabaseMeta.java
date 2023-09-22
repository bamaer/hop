package org.apache.hop.graphdatabases.neptune;

import org.apache.hop.core.graph.BaseGraphDatabaseMeta;
import org.apache.hop.core.graph.GraphDatabaseMetaPlugin;
import org.apache.hop.core.graph.IGraphDatabase;
import org.apache.hop.core.gui.plugin.GuiPlugin;

@GraphDatabaseMetaPlugin(
        type = "NEPTUNE",
        typeDescription = "AWS Neptune",
        documentationUrl = ""
)
@GuiPlugin(id = "GUI-NeptuneGraphDatabaseMeta")
public class NeptuneGraphDatabaseMeta extends BaseGraphDatabaseMeta implements IGraphDatabase {

    @Override
    public String getBoltPort(){
        return "8183";
    }
}
