package org.apache.hop.graphdatabases.neptune;


import org.apache.hop.core.graph.BaseBoltGraphDatabaseMeta;
import org.apache.hop.core.graph.GraphDatabaseMetaPlugin;
import org.apache.hop.core.graph.IBoltGraphDatabase;
import org.apache.hop.core.gui.plugin.GuiPlugin;

@GraphDatabaseMetaPlugin(
        type = "NEPTUNE",
        typeDescription = "AWS Neptune",
        documentationUrl = ""
)
@GuiPlugin(id = "GUI-NeptuneGraphDatabaseMeta")
public class NeptuneGraphDatabaseMeta extends BaseBoltGraphDatabaseMeta implements IBoltGraphDatabase {

/*
    @Override
    public String getBoltPort(){
        return "8183";
    }
*/
}
