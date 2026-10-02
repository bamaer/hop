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

package org.apache.hop.neo4j.bolt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.abort;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.apache.hop.core.HopClientEnvironment;
import org.apache.hop.core.graph.GraphDatabaseMeta;
import org.apache.hop.core.graph.GraphDatabasePlugin;
import org.apache.hop.core.graph.GraphDatabasePluginType;
import org.apache.hop.core.graph.IGraphConnection;
import org.apache.hop.core.logging.LogChannel;
import org.apache.hop.core.plugins.PluginRegistry;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.core.variables.Variables;
import org.apache.hop.neo4j.actions.constraint.ConstraintType;
import org.apache.hop.neo4j.actions.constraint.ConstraintUpdate;
import org.apache.hop.neo4j.actions.constraint.Neo4jConstraint;
import org.apache.hop.neo4j.actions.index.IndexUpdate;
import org.apache.hop.neo4j.actions.index.Neo4jIndex;
import org.apache.hop.neo4j.actions.index.ObjectType;
import org.apache.hop.neo4j.actions.index.UpdateType;
import org.apache.hop.neo4j.shared.NeoConnection;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

/**
 * Connects to a real Memgraph server with a Memgraph graph database connection, both through the
 * generic graph connection and through the Neo4j connection form the Neo4j transforms use.
 *
 * <p>Skipped when Docker is unavailable or the container cannot start in time.
 */
class MemgraphBoltIT {
  private static GenericContainer<?> memgraph;
  private static GraphDatabaseMeta graphDatabaseMeta;
  private static final IVariables variables = Variables.getADefaultVariableSpace();

  @BeforeAll
  static void setUp() throws Exception {
    assumeTrue(
        DockerClientFactory.instance().isDockerAvailable(),
        "Docker is required for MemgraphBoltIT");
    memgraph =
        new GenericContainer<>(DockerImageName.parse("memgraph/memgraph:3.6.0"))
            .withExposedPorts(7687)
            .waitingFor(Wait.forListeningPort())
            .withStartupTimeout(Duration.ofMinutes(3));
    try {
      memgraph.start();
    } catch (Exception e) {
      abort("Memgraph container did not become ready: " + e.getMessage());
    }

    HopClientEnvironment.init();
    PluginRegistry.getInstance()
        .registerPluginClass(
            MemgraphGraphDatabase.class.getName(),
            GraphDatabasePluginType.class,
            GraphDatabasePlugin.class);

    MemgraphGraphDatabase type =
        (MemgraphGraphDatabase) GraphDatabaseMeta.createGraphDatabase("MEMGRAPH");
    type.setServer(memgraph.getHost());
    type.setBoltPort(Integer.toString(memgraph.getMappedPort(7687)));
    type.setUsername("memgraph");
    type.setPassword("memgraph");
    graphDatabaseMeta = new GraphDatabaseMeta("memgraph", type);
  }

  @AfterAll
  static void tearDown() {
    if (memgraph != null) {
      memgraph.stop();
    }
  }

  @Test
  void testConnection() throws Exception {
    String url = graphDatabaseMeta.test(variables);
    assertTrue(url.startsWith("bolt://"), url);
  }

  @Test
  void testExecute() throws Exception {
    try (IGraphConnection connection = graphDatabaseMeta.connect(LogChannel.GENERAL, variables)) {
      connection.execute("CREATE (:BoltIT { name : $name })", Map.of("name", "graph-connection"));
      List<Map<String, Object>> rows =
          connection.execute(
              "MATCH (n:BoltIT { name : $name }) RETURN n.name AS name",
              Map.of("name", "graph-connection"));
      assertEquals(1, rows.size());
      assertEquals("graph-connection", rows.get(0).get("name"));
    }
  }

  /** The Neo4j transforms get the connection as a Neo4j connection: check that path too. */
  @Test
  void testNeoConnectionForm() throws Exception {
    NeoConnection neo =
        ((BoltGraphDatabase) graphDatabaseMeta.getGraphDatabase()).toNeoConnection("memgraph");
    try (Driver driver = neo.getDriver(LogChannel.GENERAL, variables);
        Session session = neo.getSession(LogChannel.GENERAL, driver, variables)) {
      long count =
          session.executeWrite(
              tx -> {
                tx.run("UNWIND range(1, 10) AS i CREATE (:BoltITBatch { id : i })");
                return tx.run("MATCH (n:BoltITBatch) RETURN count(n) AS c")
                    .single()
                    .get("c")
                    .asLong();
              });
      assertEquals(10L, count);
    }
  }

  /** The index and constraint statements of the Memgraph dialect are accepted by Memgraph. */
  @Test
  void testDialectStatements() throws Exception {
    org.apache.hop.neo4j.actions.constraint.ObjectType node =
        org.apache.hop.neo4j.actions.constraint.ObjectType.NODE;
    List<String> statements =
        List.of(
            Neo4jIndex.generateCreateIndexCypher(
                new IndexUpdate(UpdateType.CREATE, ObjectType.NODE, null, "BoltITSchema", "a, b"),
                BoltDialect.MEMGRAPH),
            Neo4jIndex.generateCreateIndexCypher(
                new IndexUpdate(UpdateType.CREATE, ObjectType.RELATIONSHIP, null, "LINK", "w"),
                BoltDialect.MEMGRAPH),
            Neo4jConstraint.generateCreateConstraintCypher(
                new ConstraintUpdate(
                    org.apache.hop.neo4j.actions.constraint.UpdateType.CREATE,
                    node,
                    ConstraintType.UNIQUE,
                    null,
                    "BoltITSchema",
                    "id"),
                BoltDialect.MEMGRAPH),
            Neo4jConstraint.generateCreateConstraintCypher(
                new ConstraintUpdate(
                    org.apache.hop.neo4j.actions.constraint.UpdateType.CREATE,
                    node,
                    ConstraintType.NOT_NULL,
                    null,
                    "BoltITSchema",
                    "id"),
                BoltDialect.MEMGRAPH),
            Neo4jConstraint.generateDropConstraintCypher(
                new ConstraintUpdate(
                    org.apache.hop.neo4j.actions.constraint.UpdateType.DROP,
                    node,
                    ConstraintType.NOT_NULL,
                    null,
                    "BoltITSchema",
                    "id"),
                BoltDialect.MEMGRAPH),
            Neo4jIndex.generateDropIndexCypher(
                new IndexUpdate(UpdateType.DROP, ObjectType.NODE, null, "BoltITSchema", "a, b"),
                BoltDialect.MEMGRAPH));
    // Memgraph only accepts index and constraint changes in auto-commit transactions
    //
    try (IGraphConnection connection = graphDatabaseMeta.connect(LogChannel.GENERAL, variables)) {
      for (String statement : statements) {
        connection.execute(statement, Map.of());
      }
      List<Map<String, Object>> constraints = connection.execute("SHOW CONSTRAINT INFO", Map.of());
      assertEquals(1, constraints.size(), constraints.toString());
    }
  }
}
