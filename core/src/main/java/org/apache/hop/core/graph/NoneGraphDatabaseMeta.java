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


import org.apache.hop.core.exception.HopConfigException;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.variables.IVariables;

import java.util.List;
import java.util.Map;

@GraphDatabaseMetaPlugin(
        type = "NONEGRAPH",
        typeDescription = "No graph connection type",
        documentationUrl = ""
)
public class NoneGraphDatabaseMeta extends BaseGraphDatabaseMeta implements IGraphDatabase {

    private static final String serverTestQuery = "no test query";
    private static final String serverInfoQuery = "no version query";


    @Override
    public GraphDatabaseTestResults testConnectionSuccess(IVariables variables) throws HopConfigException, HopException {
        GraphDatabaseTestResults testResults = new GraphDatabaseTestResults();
        testResults.setSuccess(true);
        testResults.setMessage("nothing to test");
        return testResults;
    }

    @Override
    public String getServerTestQuery() {
        return null;
    }

    @Override
    public String getServerInfo() {
        return null;
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