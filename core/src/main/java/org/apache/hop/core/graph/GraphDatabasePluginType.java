/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
