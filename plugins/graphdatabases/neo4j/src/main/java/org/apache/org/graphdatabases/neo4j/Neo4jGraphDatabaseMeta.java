package org.apache.org.graphdatabases.neo4j;

import org.apache.hop.core.graph.BaseGraphDatabaseMeta;
import org.apache.hop.core.graph.GraphDatabaseMeta;
import org.apache.hop.core.graph.GraphDatabaseMetaPlugin;
import org.apache.hop.core.graph.IGraphDatabase;
import org.apache.hop.core.gui.plugin.GuiElementType;
import org.apache.hop.core.gui.plugin.GuiPlugin;
import org.apache.hop.core.gui.plugin.GuiWidgetElement;
import org.apache.hop.core.logging.ILogChannel;
import org.apache.hop.metadata.api.IHopMetadataProvider;

import java.util.ArrayList;
import java.util.List;

@GraphDatabaseMetaPlugin(
        type = "NEO4J",
        typeDescription = "Neo4j",
        documentationUrl = ""
)
@GuiPlugin(id = "GUI-Neo4GraphDatabaseMeta")
public class Neo4jGraphDatabaseMeta extends BaseGraphDatabaseMeta implements IGraphDatabase {

    private static final Class<?> PKG = Neo4jGraphDatabaseMeta.class; // For Translator

    @GuiWidgetElement(
        id = "neo4jVersion",
        order = "10",
        parentId = GraphDatabaseMeta.GUI_PLUGIN_ELEMENT_PARENT_ID,
        type = GuiElementType.COMBO,
        variables = false,
        comboValuesMethod = "getNeo4jVersions",
        label = "i18n::Neo4jGraphDatabaseMeta.label.Neo4jVersion"
    )
    private String neo4jVersion = "Neo4j 5";

    public List<String> getNeo4jVersions(ILogChannel log, IHopMetadataProvider metadataProvider){
        List<String> versions = new ArrayList<>();
        versions.add("Neo4j 4");
        versions.add("Neo4j 5");
        return versions;
    }

    @Override
    public boolean isSupportsBTreeIndex(){
        return true;
    }


}
