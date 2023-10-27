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

package org.apache.hop.graphdatabases.shared;

import org.apache.hop.core.graph.GraphDatabaseMeta;
import org.apache.hop.core.graph.IGraphDatabase;
import org.apache.hop.core.logging.ILogChannel;
import org.apache.hop.core.variables.IVariables;

import java.util.List;
import java.util.Map;

public class GraphConnectionUtils {
  private static final Class<?> PKG =
      GraphConnectionUtils.class; // for i18n purposes, needed by Translator2!!

  public static final void createNodeIndex(
          ILogChannel log, IVariables variables, GraphDatabaseMeta graphDatabaseMeta, List<String> labels, List<String> keyProperties) {

    // If we have no properties or labels, we have nothing to do here
    //
    if (keyProperties.isEmpty()) {
      return;
    }
    if (labels.isEmpty()) {
      return;
    }

    // We only use the first label for index or constraint
    //
//    String labelsClause = ":" + labels.get(0);
    IGraphDatabase graphDatabase = graphDatabaseMeta.getIGraphDatabase();
    String labelsClause = labels.get(0);

    // CREATE CONSTRAINT FOR (n:NodeLabel) REQUIRE n.property1 IS UNIQUE
    //
    if (keyProperties.size() == 1) {
      String property = keyProperties.get(0);
      String constraintStmt = graphDatabase.getCreateIndexStatement(labels, property);
//      String constraintCypher =
//          "CREATE CONSTRAINT IF NOT EXISTS FOR (n"
//              + labelsClause
//              + ") REQUIRE n."
//              + property
//              + " IS UNIQUE;";

      log.logDetailed("Creating constraint : " + constraintStmt);
      graphDatabase.runStatement(variables, constraintStmt);

      // This creates an index, no need to go further here...
      //
      return;
    }

    // Composite index case...
    //
    // CREATE INDEX ON :NodeLabel(property, property2, ...)
    //
    String indexCypher = "CREATE INDEX IF NOT EXISTS FOR (n";

    indexCypher += labelsClause;
    indexCypher += ") ON (";
    boolean firstProperty = true;
    for (String property : keyProperties) {
      if (firstProperty) {
        firstProperty = false;
      } else {
        indexCypher += ", ";
      }
      indexCypher += "n."+property;
    }
    indexCypher += ")";

    log.logDetailed("Creating index : " + indexCypher);
    graphDatabase.runStatement(variables, indexCypher);
  }
}
