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

package org.apache.hop.neo4j.actions.index;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.hop.core.Const;
import org.apache.hop.core.Result;
import org.apache.hop.core.annotations.Action;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.metadata.api.HopMetadataProperty;
import org.apache.hop.neo4j.shared.CypherDialect;
import org.apache.hop.neo4j.shared.NamedGraphConnection;
import org.apache.hop.neo4j.shared.NeoConnectionUtils;
import org.apache.hop.workflow.action.ActionBase;
import org.apache.hop.workflow.action.IAction;

@Action(
    id = "NEO4J_INDEX",
    name = "Graph index",
    description = "Create or delete indexes in a graph database",
    image = "graph_index.svg",
    categoryDescription = "i18n:org.apache.hop.workflow:ActionCategory.Category.Scripting",
    keywords = "i18n::Neo4jIndex.keyword",
    documentationUrl = "/workflow/actions/graph-index.html")
public class Neo4jIndex extends ActionBase implements IAction {

  /** The name of the Neo4j or Bolt graph database connection. */
  @HopMetadataProperty(key = "connection")
  private String connectionName;

  private NamedGraphConnection connection;

  @HopMetadataProperty(groupKey = "updates", key = "update")
  private List<IndexUpdate> indexUpdates;

  public Neo4jIndex() {
    this("", "");
  }

  public Neo4jIndex(String name) {
    this(name, "");
  }

  public Neo4jIndex(String name, String description) {
    super(name, description);
    indexUpdates = new ArrayList<>();
  }

  @Override
  public Result execute(Result result, int nr) throws HopException {
    // Success unless something goes wrong, whatever the result of the previous action
    result.setResult(true);

    connection =
        NeoConnectionUtils.findGraphConnection(getMetadataProvider(), resolve(connectionName));

    if (connection == null) {
      result.setResult(false);
      result.increaseErrors(1L);
      throw new HopException("Please specify a Neo4j connection to use");
    }

    // Loop over the index updates to see which need deleting...
    //
    for (IndexUpdate indexUpdate : indexUpdates) {
      if (indexUpdate.getType() == null) {
        throw new HopException("Please make sure to always specify an index update type");
      }
      switch (indexUpdate.getType()) {
        case DROP:
          dropIndex(indexUpdate);
          break;
        default:
          break;
      }
    }

    // Create the indexes if needed
    //
    for (IndexUpdate indexUpdate : indexUpdates) {
      switch (indexUpdate.getType()) {
        case CREATE:
          createIndex(indexUpdate);
          break;
        default:
          break;
      }
    }

    return result;
  }

  /**
   * Generate preview Cypher for dropping an index (without executing it)
   *
   * @param indexUpdate The index update configuration
   * @return The generated Cypher statement
   * @throws HopException If configuration is invalid
   */
  /**
   * Generate the Cypher to drop an index in the given dialect.
   *
   * @throws HopException if the database doesn't support it or information is missing
   */
  public static String generateDropIndexCypher(IndexUpdate indexUpdate, CypherDialect dialect)
      throws HopException {
    if (dialect == CypherDialect.MEMGRAPH) {
      return "DROP " + getMemgraphIndexClause(indexUpdate);
    }
    if (dialect == CypherDialect.FALKORDB) {
      return "DROP " + getFalkorDbIndexClause(indexUpdate);
    }
    validateIndexSupport(indexUpdate, dialect);
    return generateDropIndexCypher(indexUpdate);
  }

  public static String generateDropIndexCypher(IndexUpdate indexUpdate) throws HopException {
    String cypher = "DROP INDEX ";

    if (StringUtils.isEmpty(indexUpdate.getIndexName())) {
      throw new HopException(
          "Please drop indexes with the name of the index. Object: "
              + indexUpdate.getObjectName()
              + ", properties: "
              + indexUpdate.getObjectProperties());
    }
    cypher += indexUpdate.getIndexName();
    cypher += " IF EXISTS";
    return cypher;
  }

  private void dropIndex(final IndexUpdate indexUpdate) throws HopException {
    String cypher = generateDropIndexCypher(indexUpdate, connection.getDialect());

    // Run this cypher statement...
    //
    NeoConnectionUtils.runSchemaStatement(
        connection, getLogChannel(), this, cypher, "Dropping index");
  }

  /**
   * Generate preview Cypher for creating an index (without executing it)
   *
   * @param indexUpdate The index update configuration
   * @return The generated Cypher statement
   */
  /**
   * Generate the Cypher to create an index in the given dialect.
   *
   * @throws HopException if the database doesn't support it or information is missing
   */
  public static String generateCreateIndexCypher(IndexUpdate indexUpdate, CypherDialect dialect)
      throws HopException {
    if (dialect == CypherDialect.MEMGRAPH) {
      return "CREATE " + getMemgraphIndexClause(indexUpdate);
    }
    if (dialect == CypherDialect.FALKORDB) {
      return "CREATE " + getFalkorDbIndexClause(indexUpdate);
    }
    validateIndexSupport(indexUpdate, dialect);
    return generateCreateIndexCypher(indexUpdate);
  }

  private static void validateIndexSupport(IndexUpdate indexUpdate, CypherDialect dialect)
      throws HopException {
    boolean supported =
        indexUpdate.getObjectType() == ObjectType.RELATIONSHIP
            ? dialect.isSupportingRelationshipIndexes()
            : dialect.isSupportingNodeIndexes();
    if (!supported) {
      throw new HopException(
          "Index updates on "
              + indexUpdate.getObjectType()
              + " are not supported by "
              + dialect
              + " for index on "
              + indexUpdate.getObjectName());
    }
  }

  /** FalkorDB indexes have no name: INDEX FOR (n:Label) ON (n.property, ...) */
  private static String getFalkorDbIndexClause(IndexUpdate indexUpdate) throws HopException {
    if (StringUtils.isEmpty(indexUpdate.getObjectName())
        || StringUtils.isEmpty(indexUpdate.getObjectProperties())) {
      throw new HopException(
          "FalkorDB indexes are identified by label and properties, please specify both. Index: "
              + indexUpdate.getIndexName());
    }
    StringBuilder clause = new StringBuilder("INDEX FOR ");
    if (indexUpdate.getObjectType() == ObjectType.RELATIONSHIP) {
      clause.append("()-[n:").append(indexUpdate.getObjectName()).append("]-()");
    } else {
      clause.append("(n:").append(indexUpdate.getObjectName()).append(")");
    }
    clause.append(" ON (");
    String[] properties = indexUpdate.getObjectProperties().split(",");
    for (int i = 0; i < properties.length; i++) {
      if (i > 0) {
        clause.append(", ");
      }
      clause.append("n.").append(Const.trim(properties[i]));
    }
    return clause.append(")").toString();
  }

  /** Memgraph indexes have no name: [EDGE] INDEX ON :Label(property, ...) */
  private static String getMemgraphIndexClause(IndexUpdate indexUpdate) throws HopException {
    if (StringUtils.isEmpty(indexUpdate.getObjectName())
        || StringUtils.isEmpty(indexUpdate.getObjectProperties())) {
      throw new HopException(
          "Memgraph indexes are identified by label and properties, please specify both. Index: "
              + indexUpdate.getIndexName());
    }
    String[] properties = indexUpdate.getObjectProperties().split(",");
    StringBuilder clause = new StringBuilder();
    if (indexUpdate.getObjectType() == ObjectType.RELATIONSHIP) {
      if (properties.length > 1) {
        throw new HopException(
            "Memgraph edge indexes are on a single property, not on "
                + indexUpdate.getObjectProperties());
      }
      clause.append("EDGE ");
    }
    clause.append("INDEX ON :").append(indexUpdate.getObjectName()).append("(");
    for (int i = 0; i < properties.length; i++) {
      if (i > 0) {
        clause.append(", ");
      }
      clause.append(Const.trim(properties[i]));
    }
    clause.append(")");
    return clause.toString();
  }

  public static String generateCreateIndexCypher(IndexUpdate indexUpdate) {
    String cypher = "CREATE INDEX ";

    if (StringUtils.isNotEmpty(indexUpdate.getIndexName())) {
      cypher += indexUpdate.getIndexName();
    }

    cypher += " IF NOT EXISTS";

    String[] properties = indexUpdate.getObjectProperties().split(",");

    cypher += " FOR ";
    switch (indexUpdate.getObjectType()) {
      case NODE:
        cypher += "(n:" + indexUpdate.getObjectName() + ") ";
        break;
      case RELATIONSHIP:
        cypher += "()-[n:" + indexUpdate.getObjectName() + "]-() ";
        break;
    }

    // Add the properties to index:
    //
    cypher += "ON (";
    for (int i = 0; i < properties.length; i++) {
      String property = properties[i];
      if (i > 0) {
        cypher += ", ";
      }
      cypher += "n." + Const.trim(property);
    }
    cypher += ")";

    return cypher;
  }

  private void createIndex(IndexUpdate indexUpdate) throws HopException {
    String cypher = generateCreateIndexCypher(indexUpdate, connection.getDialect());

    // Run this cypher statement...
    //
    NeoConnectionUtils.runSchemaStatement(
        connection, getLogChannel(), this, cypher, "Creating index");
  }

  @Override
  public boolean isEvaluation() {
    return true;
  }

  @Override
  public boolean isUnconditional() {
    return false;
  }

  /**
   * Gets the name of the connection
   *
   * @return value of connectionName
   */
  public String getConnectionName() {
    return connectionName;
  }

  /**
   * @param connectionName The name of the Neo4j or Bolt graph database connection to use
   */
  public void setConnectionName(String connectionName) {
    this.connectionName = connectionName;
  }

  /**
   * Gets indexUpdates
   *
   * @return value of indexUpdates
   */
  public List<IndexUpdate> getIndexUpdates() {
    return indexUpdates;
  }

  /**
   * @param indexUpdates The indexUpdates to set
   */
  public void setIndexUpdates(List<IndexUpdate> indexUpdates) {
    this.indexUpdates = indexUpdates;
  }
}
