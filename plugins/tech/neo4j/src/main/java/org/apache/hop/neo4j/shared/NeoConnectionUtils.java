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

package org.apache.hop.neo4j.shared;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.lang3.StringUtils;
import org.apache.hop.core.Const;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.graph.GraphDatabaseMeta;
import org.apache.hop.core.graph.IGraphConnection;
import org.apache.hop.core.logging.ILogChannel;
import org.apache.hop.metadata.api.IHopMetadataProvider;
import org.apache.hop.metadata.api.IHopMetadataSerializer;
import org.apache.hop.neo4j.bolt.BoltGraphDatabase;
import org.apache.hop.neo4j.bolt.Neo4jGraphDatabase;
import org.neo4j.driver.Session;
import org.neo4j.driver.summary.Notification;
import org.neo4j.driver.summary.ResultSummary;

public class NeoConnectionUtils {
  private static final Class<?> PKG =
      NeoConnectionUtils.class; // for i18n purposes, needed by Translator2!!

  private static final String NEO4J_PLUGIN_ID = "NEO4J";

  /**
   * Log the notifications of a statement. Notifications are advice about a statement which
   * succeeded, never errors: errors are thrown as exceptions. Warnings are logged at the basic
   * level, informational notifications at the detailed level.
   */
  public static void logNotifications(ILogChannel log, ResultSummary summary) {
    for (Notification notification : summary.notifications()) {
      String message =
          notification.rawSeverityLevel().orElse("")
              + " : "
              + notification.title()
              + " : "
              + notification.code()
              + " : "
              + notification.description()
              + ", position "
              + notification.position();
      if (isInformational(notification)) {
        log.logDetailed(message);
      } else {
        log.logBasic(message);
      }
    }
  }

  /**
   * Informational notifications, like Neo4j's INFORMATION or Memgraph's INFO planner hints, are not
   * errors.
   */
  public static boolean isInformational(Notification notification) {
    String severity = notification.rawSeverityLevel().orElse("");
    return "INFORMATION".equalsIgnoreCase(severity) || "INFO".equalsIgnoreCase(severity);
  }

  /**
   * Find a connection by name. A Neo4j connection comes first, so existing projects behave exactly
   * as before. Otherwise a graph database connection of a Bolt type is used, in the form of a Neo4j
   * connection with the same settings.
   *
   * @return The connection or null if there is no Neo4j or Bolt connection with that name
   */
  public static NeoConnection loadConnection(IHopMetadataProvider metadataProvider, String name)
      throws HopException {
    if (metadataProvider == null || StringUtils.isEmpty(name)) {
      return null;
    }
    NeoConnection connection = metadataProvider.getSerializer(NeoConnection.class).load(name);
    if (connection != null) {
      return connection;
    }
    GraphDatabaseMeta graphDatabaseMeta = GraphDatabaseMeta.load(metadataProvider, name);
    if (graphDatabaseMeta != null
        && graphDatabaseMeta.getGraphDatabase() instanceof BoltGraphDatabase bolt) {
      return bolt.toNeoConnection(graphDatabaseMeta.getName());
    }
    return null;
  }

  /**
   * Convert a Neo4j connection into a graph database connection of type Neo4j with the same name
   * and settings. The graph database connection is saved first, then the Neo4j connection is
   * deleted, so the connection can be found by name at any time.
   *
   * @return The new graph database connection
   * @throws HopException if there is no such Neo4j connection or a graph database connection with
   *     that name already exists
   */
  public static GraphDatabaseMeta convertToGraphConnection(
      IHopMetadataProvider metadataProvider, String name) throws HopException {
    IHopMetadataSerializer<NeoConnection> neoSerializer =
        metadataProvider.getSerializer(NeoConnection.class);
    IHopMetadataSerializer<GraphDatabaseMeta> graphSerializer =
        metadataProvider.getSerializer(GraphDatabaseMeta.class);
    NeoConnection neo = neoSerializer.load(name);
    if (neo == null) {
      throw new HopException("Neo4j connection '" + name + "' doesn't exist");
    }
    if (graphSerializer.exists(name)) {
      throw new HopException("A graph database connection named '" + name + "' already exists");
    }
    Neo4jGraphDatabase neo4j =
        (Neo4jGraphDatabase) GraphDatabaseMeta.createGraphDatabase(NEO4J_PLUGIN_ID);
    neo4j.copyFrom(neo);
    GraphDatabaseMeta graphDatabaseMeta = new GraphDatabaseMeta(neo.getName(), neo4j);
    graphDatabaseMeta.setVirtualPath(neo.getVirtualPath());
    graphSerializer.save(graphDatabaseMeta);
    neoSerializer.delete(name);
    return graphDatabaseMeta;
  }

  /**
   * Convert all Neo4j connections into graph database connections of type Neo4j. A Neo4j connection
   * is skipped when a graph database connection with the same name exists already.
   *
   * @return The names of the skipped connections, mapped to the reason. Empty if all were
   *     converted.
   */
  public static java.util.Map<String, String> convertAllToGraphConnections(
      IHopMetadataProvider metadataProvider) throws HopException {
    java.util.Map<String, String> skipped = new java.util.TreeMap<>();
    for (String name : metadataProvider.getSerializer(NeoConnection.class).listObjectNames()) {
      try {
        convertToGraphConnection(metadataProvider, name);
      } catch (HopException e) {
        skipped.put(name, e.getMessage());
      }
    }
    return skipped;
  }

  /**
   * Find a connection by name: a Neo4j connection first, so existing projects behave exactly as
   * before, otherwise a graph database connection of any type.
   *
   * @return The connection or null if there is no connection with that name
   */
  public static NamedGraphConnection findGraphConnection(
      IHopMetadataProvider metadataProvider, String name) throws HopException {
    if (metadataProvider == null || StringUtils.isEmpty(name)) {
      return null;
    }
    NeoConnection connection = metadataProvider.getSerializer(NeoConnection.class).load(name);
    if (connection != null) {
      return new NamedGraphConnection(name, connection, null);
    }
    GraphDatabaseMeta graphDatabaseMeta = GraphDatabaseMeta.load(metadataProvider, name);
    if (graphDatabaseMeta != null) {
      return new NamedGraphConnection(name, null, graphDatabaseMeta);
    }
    return null;
  }

  /** True for Neo4j connections and graph database connections of a Bolt type. */
  public static boolean isBolt(NamedGraphConnection graphConnection) {
    return graphConnection.neoConnection() != null
        || graphConnection.graphDatabaseMeta().getGraphDatabase() instanceof BoltGraphDatabase;
  }

  /**
   * Find a connection by name or fail.
   *
   * @throws HopException when there is no Neo4j or graph database connection with that name
   */
  public static NamedGraphConnection getGraphConnection(
      IHopMetadataProvider metadataProvider, String name) throws HopException {
    if (StringUtils.isEmpty(name)) {
      throw new HopException("Please specify a graph database or Neo4j connection");
    }
    NamedGraphConnection connection = findGraphConnection(metadataProvider, name);
    if (connection == null) {
      throw new HopException("Unable to find graph database or Neo4j connection '" + name + "'");
    }
    return connection;
  }

  /**
   * The sorted names of all Neo4j connections and graph database connections of any type, for the
   * transforms and actions which work through {@link NamedGraphConnection}.
   */
  public static List<String> getAllConnectionNames(IHopMetadataProvider metadataProvider)
      throws HopException {
    Set<String> names =
        new TreeSet<>(metadataProvider.getSerializer(NeoConnection.class).listObjectNames());
    names.addAll(metadataProvider.getSerializer(GraphDatabaseMeta.class).listObjectNames());
    return new ArrayList<>(names);
  }

  /**
   * Create a unique constraint (one key property) or an index (several key properties) on the first
   * label, through a graph connection in its dialect.
   */
  public static void createNodeIndex(
      ILogChannel log, IGraphConnection connection, List<String> labels, List<String> keyProperties)
      throws HopException {
    CypherDialect dialect = CypherDialect.fromId(connection.getDialect());
    String cypher = getCreateNodeIndexCypher(labels, keyProperties, dialect);
    if (cypher == null) {
      if (!labels.isEmpty() && !keyProperties.isEmpty()) {
        log.logBasic(
            dialect
                + " doesn't create indexes in Cypher: not creating an index on "
                + labels.get(0));
      }
      return;
    }
    log.logDetailed("Creating index or constraint : " + cypher);
    try {
      connection.execute(cypher, Map.of());
    } catch (HopException e) {
      if (!isExistingOrMissingIndex(dialect, e)) {
        throw e;
      }
    }
  }

  /**
   * FalkorDB has no IF [NOT] EXISTS: an index which exists already, or doesn't exist when dropping,
   * is not an error, the same as with Neo4j.
   */
  private static boolean isExistingOrMissingIndex(CypherDialect dialect, Exception e) {
    if (dialect != CypherDialect.FALKORDB) {
      return false;
    }
    String message = Const.getSimpleStackTrace(e);
    return message.contains("already indexed") || message.contains("no such index");
  }

  /**
   * Run an index or constraint statement: in a write transaction, or on its own where the database
   * doesn't allow schema changes in a transaction.
   *
   * @param description What the statement does, for the log, for example "Creating index"
   */
  public static void runSchemaStatement(
      NamedGraphConnection graphConnection,
      ILogChannel log,
      org.apache.hop.core.variables.IVariables variables,
      String cypher,
      String description)
      throws HopException {
    try (IGraphConnection connection = graphConnection.connect(log, variables)) {
      log.logDetailed(description + " with cypher: " + cypher);
      if (!graphConnection.getDialect().isSupportingSchemaChangesInTransactions()
          || !connection.isSupportingTransactions()) {
        connection.execute(cypher, Map.of());
      } else {
        connection.executeWrite(
            transaction -> {
              transaction.execute(cypher, Map.of());
              return true;
            });
      }
    } catch (HopException e) {
      if (isExistingOrMissingIndex(graphConnection.getDialect(), e)) {
        log.logDetailed(description + ": nothing to do, " + Const.getSimpleStackTrace(e));
        return;
      }
      throw new HopException(description + " failed with cypher [" + cypher + "]", e);
    }
  }

  /** The statement creating the index of {@link #createNodeIndex}, null if there is none. */
  public static String getCreateNodeIndexCypher(
      List<String> labels, List<String> keyProperties, CypherDialect dialect) {
    if (keyProperties.isEmpty() || labels.isEmpty() || !dialect.isSupportingNodeIndexes()) {
      return null;
    }
    String label = labels.get(0);
    switch (dialect) {
      case MEMGRAPH:
        if (keyProperties.size() == 1) {
          return "CREATE CONSTRAINT ON (n:"
              + label
              + ") ASSERT n."
              + keyProperties.get(0)
              + " IS UNIQUE";
        }
        return "CREATE INDEX ON :" + label + "(" + String.join(", ", keyProperties) + ")";
      case FALKORDB:
        List<String> properties = new ArrayList<>();
        keyProperties.forEach(p -> properties.add("n." + p));
        return "CREATE INDEX FOR (n:" + label + ") ON (" + String.join(", ", properties) + ")";
      default:
        if (keyProperties.size() == 1) {
          return "CREATE CONSTRAINT IF NOT EXISTS FOR (n:"
              + label
              + ") REQUIRE n."
              + keyProperties.get(0)
              + " IS UNIQUE;";
        }
        List<String> neoProperties = new ArrayList<>();
        keyProperties.forEach(p -> neoProperties.add("n." + p));
        return "CREATE INDEX IF NOT EXISTS FOR (n:"
            + label
            + ") ON ("
            + String.join(", ", neoProperties)
            + ")";
    }
  }

  /** The sorted names of all Neo4j connections and graph database connections of a Bolt type. */
  public static List<String> getConnectionNames(IHopMetadataProvider metadataProvider)
      throws HopException {
    Set<String> names =
        new TreeSet<>(metadataProvider.getSerializer(NeoConnection.class).listObjectNames());
    for (GraphDatabaseMeta graphDatabaseMeta :
        metadataProvider.getSerializer(GraphDatabaseMeta.class).loadAll()) {
      if (graphDatabaseMeta.getGraphDatabase() instanceof BoltGraphDatabase) {
        names.add(graphDatabaseMeta.getName());
      }
    }
    return new ArrayList<>(names);
  }

  public static final void createNodeIndex(
      ILogChannel log, Session session, List<String> labels, List<String> keyProperties) {
    createNodeIndex(log, session, labels, keyProperties, CypherDialect.NEO4J);
  }

  /**
   * Create a unique constraint (one key property) or an index (several key properties) on the first
   * label, in the dialect of the database.
   */
  public static final void createNodeIndex(
      ILogChannel log,
      Session session,
      List<String> labels,
      List<String> keyProperties,
      CypherDialect dialect) {
    if (keyProperties.isEmpty() || labels.isEmpty()) {
      return;
    }
    if (!dialect.isSupportingNodeIndexes()) {
      log.logBasic(dialect + " manages its own indexes: not creating an index on " + labels.get(0));
      return;
    }
    if (dialect == CypherDialect.MEMGRAPH) {
      String label = labels.get(0);
      String cypher;
      if (keyProperties.size() == 1) {
        cypher =
            "CREATE CONSTRAINT ON (n:"
                + label
                + ") ASSERT n."
                + keyProperties.get(0)
                + " IS UNIQUE";
      } else {
        cypher = "CREATE INDEX ON :" + label + "(" + String.join(", ", keyProperties) + ")";
      }
      log.logDetailed("Creating index or constraint : " + cypher);
      session.run(cypher).consume();
      return;
    }

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
    String labelsClause = ":" + labels.get(0);

    // CREATE CONSTRAINT FOR (n:NodeLabel) REQUIRE n.property1 IS UNIQUE
    //
    if (keyProperties.size() == 1) {
      String property = keyProperties.get(0);
      String constraintCypher =
          "CREATE CONSTRAINT IF NOT EXISTS FOR (n"
              + labelsClause
              + ") REQUIRE n."
              + property
              + " IS UNIQUE;";

      log.logDetailed("Creating constraint : " + constraintCypher);
      session.run(constraintCypher);

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
      indexCypher += "n." + property;
    }
    indexCypher += ")";

    log.logDetailed("Creating index : " + indexCypher);
    session.run(indexCypher);
  }
}
