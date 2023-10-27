/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hop.graphdatabases.arangodb;

import com.arangodb.ArangoDB;
import org.apache.hop.core.exception.HopConfigException;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.graph.BaseGraphDatabaseMeta;
import org.apache.hop.core.graph.GraphDatabaseMetaPlugin;
import org.apache.hop.core.graph.GraphDatabaseTestResults;
import org.apache.hop.core.graph.IGraphDatabase;
import org.apache.hop.core.gui.plugin.GuiPlugin;
import org.apache.hop.core.variables.IVariables;

import java.util.List;
import java.util.Map;

@GraphDatabaseMetaPlugin(
    type = "ARANGODB",
    typeDescription = "ArangoDB",
    documentationUrl = "",
    classLoaderGroup = "arangodb"
)
@GuiPlugin(id = "GUI-ArangoGraphDatabaseMeta")
public class ArangoDbGraphDatabaseMeta extends BaseGraphDatabaseMeta implements IGraphDatabase {

    @Override
    public String getServerTestQuery() {
        return null;
    }

    @Override
    public String getServerInfo() {
        return null;
    }

    @Override
    public String getDriverClass(){
        return "com.arangodb.ArangoDB";
    }

    @Override
    public GraphDatabaseTestResults testConnectionSuccess(IVariables variables) throws HopConfigException, HopException {
        GraphDatabaseTestResults testResults = new GraphDatabaseTestResults();
        try{
            ArangoDB arangoDB = new ArangoDB.Builder()
                    .host(this.getHostname(), Integer.valueOf(this.getPort()))
                    .user(this.getUsername())
                    .password(this.getPassword())
                    .build();
            testResults.setMessage(arangoDB.getEngine().getName().toString());
            testResults.setSuccess(true);
        }catch(Exception e){
            testResults.setMessage("Error connecting to ArangoDB. ");
            testResults.setSuccess(false);
            e.printStackTrace();
        }
        return testResults;
    }

    @Override
    public String getCreateIndexStatement(List<String> labels, String property) {
        return null;
    }

    @Override
    public void runStatement(IVariables variables, String statement) {
    }

    @Override
    public void writeData(IVariables variables, String query, Map<String, Object> properties) {
    }

}
