package org.apache.hop.core.graph;

import org.apache.hop.core.database.DatabaseMetaPlugin;
import org.apache.hop.core.plugins.BasePluginType;

import java.util.Map;

public class GraphDatabasePluginType extends BasePluginType<GraphDatabaseMetaPlugin> {

    private static GraphDatabasePluginType pluginType;

    private GraphDatabasePluginType(){
        super(GraphDatabaseMetaPlugin.class, "GRAPH_DATABASE", "Graph Database");
    }

    public static GraphDatabasePluginType getInstance() {
        if(pluginType == null){
            pluginType = new GraphDatabasePluginType();
        }
        return pluginType;
    }

    public String[] getNaturalCategoriesOrder() { return new String[0];}

    @Override
    protected String extractCategory(GraphDatabaseMetaPlugin annotation){ return "";}

    @Override
    protected String extractDesc(GraphDatabaseMetaPlugin annotation) {
        return annotation.typeDescription();
    }

    @Override
    protected String extractID(GraphDatabaseMetaPlugin annotation) {
        return annotation.type();
    }

    @Override
    protected String extractName(GraphDatabaseMetaPlugin annotation) {
        return annotation.typeDescription();
    }

    @Override
    protected String extractImageFile(GraphDatabaseMetaPlugin annotation) {
        return null;
    }

    @Override
    protected boolean extractSeparateClassLoader(GraphDatabaseMetaPlugin annotation) {
        return false;
    }

    @Override
    protected void addExtraClasses(
            Map<Class<?>, String> classMap, Class<?> clazz, GraphDatabaseMetaPlugin annotation) {}

    @Override
    protected String extractDocumentationUrl(GraphDatabaseMetaPlugin annotation) {
        return annotation.documentationUrl();
    }

    @Override
    protected String extractCasesUrl(GraphDatabaseMetaPlugin annotation) {
        return null;
    }

    @Override
    protected String extractForumUrl(GraphDatabaseMetaPlugin annotation) {
        return null;
    }

    @Override
    protected String extractSuggestion(GraphDatabaseMetaPlugin annotation) {
        return null;
    }

    @Override
    protected String extractClassLoaderGroup(GraphDatabaseMetaPlugin annotation) {
        return annotation.classLoaderGroup();
    }


}
