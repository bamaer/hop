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

@GraphDatabaseMetaPlugin(
        type = "NONEGRAPH",
        typeDescription = "No graph connection type",
        documentationUrl = ""
)
public class NoneGraphDatabaseMeta extends BaseGraphDatabaseMeta implements IGraphDatabase {

    @Override
    public GraphDatabaseTestResults testConnectionSuccess(IVariables variables) throws HopConfigException, HopException {
        GraphDatabaseTestResults testResults = new GraphDatabaseTestResults();
        testResults.setSuccess(true);
        testResults.setMessage("nothing to test");
        return testResults;
    }
}