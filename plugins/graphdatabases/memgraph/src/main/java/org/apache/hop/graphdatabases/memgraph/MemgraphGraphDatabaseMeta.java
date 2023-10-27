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

package org.apache.hop.graphdatabases.memgraph;

import org.apache.hop.core.graph.BaseBoltGraphDatabaseMeta;
import org.apache.hop.core.graph.GraphDatabaseMetaPlugin;
import org.apache.hop.core.graph.IBoltGraphDatabase;
import org.apache.hop.core.gui.plugin.GuiPlugin;
import org.apache.hop.core.variables.IVariables;

import java.util.List;
import java.util.Map;

@GraphDatabaseMetaPlugin(
        type = "MEMGRAPH",
        typeDescription = "Memgraph",
        documentationUrl = ""
)
@GuiPlugin(id = "GUI-MemgraphGraphDatabaseMeta")
public class MemgraphGraphDatabaseMeta  extends BaseBoltGraphDatabaseMeta implements IBoltGraphDatabase {

    private static final String serverInfoQuery = "SHOW VERSION";
    @Override
    public String getServerInfo(){
        return serverInfoQuery;
    }

    @Override
    public String getCreateIndexStatement(List<String> labels, String property) {
        return null;
    }

    @Override
    public void runStatement(IVariables variables, String statement){
    }

    @Override
    public void writeData(IVariables variables, String query, Map<String, Object> properties) {
    }
}
