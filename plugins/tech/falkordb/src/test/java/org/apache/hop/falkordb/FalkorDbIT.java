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

package org.apache.hop.falkordb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assumptions.abort;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.hop.core.HopClientEnvironment;
import org.apache.hop.core.graph.GraphNodeValue;
import org.apache.hop.core.graph.GraphPathValue;
import org.apache.hop.core.graph.GraphRelationshipValue;
import org.apache.hop.core.graph.IGraphConnection;
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

/** Runs a FalkorDB graph database connection against a real FalkorDB server. */
class FalkorDbIT {
  private static GenericContainer<?> falkordb;
  private static FalkorDbGraphDatabase graphDatabase;
  private static final IVariables variables = Variables.getADefaultVariableSpace();

  @BeforeAll
  static void setUp() throws Exception {
    HopClientEnvironment.init();
    assumeTrue(
        DockerClientFactory.instance().isDockerAvailable(), "Docker is required for FalkorDbIT");
    falkordb =
        new GenericContainer<>(DockerImageName.parse("falkordb/falkordb:6.0.1"))
            .withExposedPorts(6379)
            .waitingFor(Wait.forListeningPort())
            .withStartupTimeout(Duration.ofMinutes(3));
    try {
      falkordb.start();
    } catch (Exception e) {
      abort("FalkorDB container did not become ready: " + e.getMessage());
    }
    graphDatabase = new FalkorDbGraphDatabase();
    graphDatabase.setHostname(falkordb.getHost());
    graphDatabase.setPort(Integer.toString(falkordb.getMappedPort(6379)));
    graphDatabase.setGraphName("hop_it");
  }

  @AfterAll
  static void tearDown() {
    if (falkordb != null) {
      falkordb.stop();
    }
  }

  @Test
  void testConnection() throws Exception {
    assertFalse(graphDatabase.test(variables, "falkordb").isEmpty());
  }

  /** The way Neo4j Output writes: UNWIND over a list of maps in one statement. */
  @Test
  void testUnwindWriteAndRead() throws Exception {
    try (IGraphConnection connection = graphDatabase.connect(LogChannel.GENERAL, variables, "it")) {
      List<Map<String, Object>> props = new ArrayList<>();
      for (long id = 1; id <= 3; id++) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", id);
        row.put("name", "Person's " + id);
        row.put("score", id * 1.5);
        props.add(row);
      }
      connection.executeWrite(
          tx -> {
            tx.execute(
                "UNWIND $props AS pr MERGE (n:Person {id: pr.id}) SET n.name = pr.name, n.score = pr.score",
                Map.of("props", props));
            return null;
          });
      connection.execute(
          "MATCH (a:Person {id: 1}), (b:Person {id: 2}) MERGE (a)-[:KNOWS {since: $since}]->(b)",
          Map.of("since", 2020L));

      List<Map<String, Object>> rows =
          connection.execute(
              "MATCH (n:Person) RETURN n.id AS id, n.name AS name, n.score AS score ORDER BY id",
              Map.of());
      assertEquals(3, rows.size());
      assertEquals(1L, rows.get(0).get("id"));
      assertEquals("Person's 1", rows.get(0).get("name"));

      List<Map<String, Object>> graph =
          connection.execute("MATCH (a)-[r:KNOWS]->(b) RETURN a, r, b", Map.of());
      GraphNodeValue a = (GraphNodeValue) graph.get(0).get("a");
      assertEquals(List.of("Person"), a.labels());
      assertEquals("Person's 1", a.properties().get("name"));
      GraphRelationshipValue r = (GraphRelationshipValue) graph.get(0).get("r");
      assertEquals("KNOWS", r.type());
      assertEquals(2020L, r.properties().get("since"));
      assertEquals(a.id(), r.startNodeId());

      // Typed values: lists stay lists, doubles doubles, dates dates
      Map<String, Object> typed =
          connection
              .execute(
                  "MATCH p=(a:Person {id: 1})-[:KNOWS]->(b) RETURN labels(a) AS labels, a.score AS score,"
                      + " date('2024-01-02') AS d, p",
                  Map.of())
              .get(0);
      assertEquals(List.of("Person"), typed.get("labels"));
      assertEquals(1.5d, typed.get("score"));
      assertEquals(java.time.LocalDate.of(2024, 1, 2), typed.get("d"));
      GraphPathValue path = (GraphPathValue) typed.get("p");
      assertEquals(2, path.nodes().size());
      assertEquals(1, path.relationships().size());
    }
  }
}
