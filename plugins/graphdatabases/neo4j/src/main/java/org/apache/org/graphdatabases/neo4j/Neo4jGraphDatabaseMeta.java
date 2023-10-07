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


//    @Override
//    public boolean isSupportsBTreeIndex(){
//        return true;
//    }


}
