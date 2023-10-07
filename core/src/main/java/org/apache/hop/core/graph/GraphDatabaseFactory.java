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

import org.apache.hop.core.logging.ILoggingObject;
import org.apache.hop.core.logging.LoggingObjectType;
import org.apache.hop.core.logging.SimpleLoggingObject;
import org.apache.hop.core.variables.IVariables;

public class GraphDatabaseFactory implements IGraphDatabaseFactory {

    private static final Class<?> PKG = GraphDatabase.class;
    private boolean success;

    public static final ILoggingObject logginObject =
        new SimpleLoggingObject("Graph DatabaseFactor", LoggingObjectType.GENERAL, null);


    public GraphDatabaseFactory(){}

    @Override
    public String getConnectionTestReport(IVariables variables, GraphDatabaseMeta graphDatabaseMeta) throws HopGraphDatabaseException {
        success = true;

        StringBuilder report = new StringBuilder();

        GraphDatabase graphDatabase = new GraphDatabase(logginObject, variables, graphDatabaseMeta);

        return report.toString();
    }

    @Override
    public GraphDatabaseTestResults getConnectionTestResults(IVariables variables, GraphDatabaseMeta graphDatabaseMeta) throws HopGraphDatabaseException {
        GraphDatabaseTestResults testResults = new GraphDatabaseTestResults();
        String message = getConnectionTestReport(variables, graphDatabaseMeta);
        testResults.setMessage(message);
        testResults.setSuccess(success);
        return testResults;
    }

}
