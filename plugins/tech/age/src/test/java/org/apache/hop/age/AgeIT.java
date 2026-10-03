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

package org.apache.hop.age;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.abort;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.hop.core.HopClientEnvironment;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.graph.GraphIndex;
import org.apache.hop.core.graph.GraphNodeValue;
import org.apache.hop.core.graph.GraphRelationshipValue;
import org.apache.hop.core.graph.IGraphConnection;
import org.apache.hop.core.graph.IGraphTransaction;
import org.apache.hop.core.logging.LogChannel;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.core.variables.Variables;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

/** Runs an Apache AGE graph database connection against PostgreSQL with AGE. */
class AgeIT {
  private static GenericContainer<?> age;
  private static AgeGraphDatabase graphDatabase;
  private static final IVariables variables = Variables.getADefaultVariableSpace();

  @BeforeAll
  static void setUp() throws Exception {
    assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker is required for AgeIT");
    age =
        new GenericContainer<>(DockerImageName.parse("apache/age:release_PG18_1.8.0"))
            .withExposedPorts(5432)
            .withEnv("POSTGRES_USER", "hop")
            .withEnv("POSTGRES_PASSWORD", "hop")
            .withEnv("POSTGRES_DB", "hop")
            .waitingFor(Wait.forLogMessage(".*database system is ready to accept connections.*", 2))
            .withStartupTimeout(Duration.ofMinutes(3));
    try {
      age.start();
    } catch (Exception e) {
      abort("Apache AGE container did not become ready: " + e.getMessage());
    }
    HopClientEnvironment.init();
    graphDatabase = new AgeGraphDatabase();
    graphDatabase.setHostname(age.getHost());
    graphDatabase.setPort(Integer.toString(age.getMappedPort(5432)));
    graphDatabase.setDatabaseName("hop");
    graphDatabase.setUsername("hop");
    graphDatabase.setPassword("hop");
    graphDatabase.setGraphName("hop_it");
  }

  @AfterAll
  static void tearDown() {
    if (age != null) {
      age.stop();
    }
  }

  @Test
  void testConnection() throws Exception {
    assertFalse(graphDatabase.test(variables, "age").isEmpty());
  }

  /** The way Neo4j Output writes: UNWIND over a list of maps, in a transaction. */
  @Test
  void testUnwindWriteAndRead() throws Exception {
    try (IGraphConnection connection = graphDatabase.connect(LogChannel.GENERAL, variables, "it")) {
      List<Map<String, Object>> props = new ArrayList<>();
      for (long id = 1; id <= 3; id++) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", id);
        row.put("name", "Person's " + id);
        props.add(row);
      }
      connection.executeWrite(
          tx -> {
            tx.execute(
                "UNWIND $props AS pr MERGE (n:Person {id: pr.id}) SET n.name = pr.name",
                Map.of("props", props));
            return null;
          });
      connection.execute(
          "MATCH (a:Person {id: 1}), (b:Person {id: 2}) MERGE (a)-[:KNOWS {since: $since}]->(b)",
          Map.of("since", 2020L));

      List<Map<String, Object>> rows =
          connection.execute(
              "MATCH (n:Person) RETURN n.id AS id, n.name AS name ORDER BY n.id", Map.of());
      assertEquals(3, rows.size());
      assertEquals(1L, rows.get(0).get("id"));
      assertEquals("Person's 1", rows.get(0).get("name"));

      Map<String, Object> graph =
          connection
              .execute("MATCH (a)-[r:KNOWS]->(b) RETURN a, r, labels(a) AS l", Map.of())
              .get(0);
      GraphNodeValue a = (GraphNodeValue) graph.get("a");
      assertEquals(List.of("Person"), a.labels());
      GraphRelationshipValue r = (GraphRelationshipValue) graph.get("r");
      assertEquals(2020L, r.properties().get("since"));
      assertEquals(a.id(), r.startNodeId());
      assertEquals(List.of("Person"), graph.get("l"));
    }
  }

  @Test
  void testRollback() throws Exception {
    try (IGraphConnection connection = graphDatabase.connect(LogChannel.GENERAL, variables, "it")) {
      try (IGraphTransaction transaction = connection.beginTransaction()) {
        transaction.execute("CREATE (:Rollback {id: 1})", Map.of());
        transaction.rollback();
      }
      assertEquals(
          0L,
          connection.execute("MATCH (n:Rollback) RETURN count(n) AS c", Map.of()).get(0).get("c"));
      assertThrows(
          HopException.class,
          () ->
              connection.executeWrite(
                  tx -> {
                    tx.execute("CREATE (:Rollback {id: 2})", Map.of());
                    throw new HopException("Roll back");
                  }));
      assertEquals(
          0L,
          connection.execute("MATCH (n:Rollback) RETURN count(n) AS c", Map.of()).get(0).get("c"));
    }
  }

  /** Property indexes are PostgreSQL indexes on the label tables. */
  @Test
  void testIndexes() throws Exception {
    try (IGraphConnection connection = graphDatabase.connect(LogChannel.GENERAL, variables, "it")) {
      connection.execute(
          "CREATE (:Indexed {k: 1, a: 2})-[:INDEXED_REL {w: 3}]->(:Indexed {k: 2})", Map.of());
      String url = "jdbc:postgresql://" + age.getHost() + ":" + age.getMappedPort(5432) + "/hop";
      try (Connection jdbc = DriverManager.getConnection(url, "hop", "hop");
          Statement statement = jdbc.createStatement()) {
        statement.execute(
            "CREATE UNIQUE INDEX indexed_k ON hop_it.\"Indexed\" (ag_catalog.agtype_access_operator("
                + "VARIADIC ARRAY[properties, '\"k\"'::ag_catalog.agtype]))");
        statement.execute(
            "CREATE INDEX indexed_rel_all ON hop_it.\"INDEXED_REL\" USING gin (properties)");
      }
      List<GraphIndex> indexes = connection.getIndexes();
      GraphIndex unique =
          indexes.stream().filter(i -> i.name().equals("indexed_k")).findFirst().orElseThrow();
      assertEquals(List.of("Indexed"), unique.labelsOrTypes());
      assertEquals(List.of("k"), unique.properties());
      assertTrue(unique.unique());
      assertFalse(unique.relationship());
      GraphIndex all =
          indexes.stream()
              .filter(i -> i.name().equals("indexed_rel_all"))
              .findFirst()
              .orElseThrow();
      assertTrue(all.relationship());
      assertTrue(all.covers("INDEXED_REL", "w"));
      // Only property indexes: not the indexes on ids which AGE creates
      assertTrue(indexes.stream().noneMatch(i -> i.name().endsWith("_pkey")));
    }
  }
}
