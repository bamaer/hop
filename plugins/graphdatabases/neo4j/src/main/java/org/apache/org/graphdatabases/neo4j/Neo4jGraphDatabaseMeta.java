package org.apache.org.graphdatabases.neo4j;

import org.apache.hop.core.graph.BaseBoltGraphDatabaseMeta;
import org.apache.hop.core.graph.BaseGraphDatabaseMeta;
import org.apache.hop.core.graph.GraphDatabaseMeta;
import org.apache.hop.core.graph.GraphDatabaseMetaPlugin;
import org.apache.hop.core.graph.IBoltGraphDatabase;
import org.apache.hop.core.graph.IGraphDatabase;
import org.apache.hop.core.gui.plugin.GuiElementType;
import org.apache.hop.core.gui.plugin.GuiPlugin;
import org.apache.hop.core.gui.plugin.GuiWidgetElement;
import org.apache.hop.core.logging.ILogChannel;
import org.apache.hop.metadata.api.HopMetadataProperty;
import org.apache.hop.metadata.api.IHopMetadataProvider;

import java.util.ArrayList;
import java.util.List;

@GraphDatabaseMetaPlugin(
        type = "NEO4J",
        typeDescription = "Neo4j",
        documentationUrl = ""
)
@GuiPlugin(id = "GUI-Neo4GraphDatabaseMeta")
public class Neo4jGraphDatabaseMeta extends BaseBoltGraphDatabaseMeta implements IBoltGraphDatabase {

    private static final Class<?> PKG = Neo4jGraphDatabaseMeta.class; // For Translator

    @Override
    public String getDriverClass(){
        return "Neo4j driver";
    }

    @Override
    public String getServername() {
        return null;
    }

    @Override
    public void setServername(String servername) {

    }

//    @Override
//    public boolean isSupportsBTreeIndex(){
//        return true;
//    }


}
