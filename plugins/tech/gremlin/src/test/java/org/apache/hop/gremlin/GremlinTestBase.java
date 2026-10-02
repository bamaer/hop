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

package org.apache.hop.gremlin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.abort;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.hop.core.HopClientEnvironment;
import org.apache.hop.core.graph.GraphUpsertNode;
import org.apache.hop.core.graph.GraphUpsertRelationship;
import org.apache.hop.core.graph.IGraphConnection;
import org.apache.hop.core.logging.LogChannel;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.core.variables.Variables;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;

/**
 * Runs a Gremlin graph database connection against a Gremlin server in a container. Subclasses pick
 * the server.
 */
abstract class GremlinTestBase {
  protected static GenericContainer<?> server;
  protected static GremlinGraphDatabase graphDatabase;
  private static final IVariables variables = Variables.getADefaultVariableSpace();

  /** Start the server and point the graph database at it. Skips without Docker. */
  protected static void start(GenericContainer<?> container) throws Exception {
    assumeTrue(
        DockerClientFactory.instance().isDockerAvailable(), "Docker is required for this test");
    server = container;
    try {
      server.start();
    } catch (Exception e) {
      abort("Gremlin server container did not become ready: " + e.getMessage());
    }
    HopClientEnvironment.init();
    graphDatabase = new GremlinGraphDatabase();
    graphDatabase.setHostnames(server.getHost());
    graphDatabase.setPort(Integer.toString(server.getMappedPort(8182)));
  }

  @AfterAll
  static void tearDown() {
    if (server != null) {
      server.stop();
    }
  }

  @Test
  void testConnection() throws Exception {
    assertFalse(graphDatabase.test(variables, "gremlin").isEmpty());
  }

  @Test
  void testScript() throws Exception {
    try (IGraphConnection connection = graphDatabase.connect(LogChannel.GENERAL, variables, "it")) {
      connection.execute("g.addV('Script').property('id', scriptId)", Map.of("scriptId", 7L));
      List<Map<String, Object>> rows =
          connection.execute("g.V().hasLabel('Script').elementMap()", Map.of());
      assertEquals(1, rows.size());
      assertEquals("Script", rows.get(0).get("label"));
      assertEquals(7L, rows.get(0).get("id"));
      assertEquals(
          List.of(Map.of("result", 1L)),
          connection.execute("g.V().hasLabel('Script').count()", Map.of()));
    }
  }

  /** What Graph Output does: upsert nodes and relationships, twice without duplicates. */
  @Test
  void testUpsert() throws Exception {
    try (IGraphConnection connection = graphDatabase.connect(LogChannel.GENERAL, variables, "it")) {
      assertTrue(connection.isSupportingUpserts());
      for (int run = 0; run < 2; run++) {
        List<GraphUpsertNode> nodes = new ArrayList<>();
        for (long id = 1; id <= 3; id++) {
          Map<String, Object> properties = new LinkedHashMap<>();
          properties.put("name", "Person " + id + " run " + run);
          properties.put("born", LocalDate.of(2000, 1, (int) id));
          properties.put("nothing", null);
          nodes.add(new GraphUpsertNode("Person", Map.of("pid", id), properties));
        }
        nodes.add(new GraphUpsertNode("Company", Map.of("code", "HOP"), Map.of()));
        List<GraphUpsertRelationship> relationships =
            List.of(
                new GraphUpsertRelationship(
                    "KNOWS", nodes.get(0), nodes.get(1), Map.of("since", 2020L + run)),
                new GraphUpsertRelationship("WORKS_AT", nodes.get(0), nodes.get(3), Map.of()),
                new GraphUpsertRelationship("WORKS_AT", nodes.get(1), nodes.get(3), Map.of()));
        connection.upsert(nodes, relationships);
      }
      assertEquals(
          List.of(Map.of("result", 3L)),
          connection.execute("g.V().hasLabel('Person').count()", Map.of()));
      assertEquals(
          List.of(Map.of("result", 1L)),
          connection.execute("g.V().hasLabel('Company').count()", Map.of()));
      assertEquals(
          List.of(Map.of("result", 1L)),
          connection.execute("g.E().hasLabel('KNOWS').count()", Map.of()));
      assertEquals(
          List.of(Map.of("result", 2L)),
          connection.execute("g.E().hasLabel('WORKS_AT').count()", Map.of()));
      Map<String, Object> person =
          connection.execute("g.V().has('Person', 'pid', 1L).elementMap()", Map.of()).get(0);
      assertEquals("Person 1 run 1", person.get("name"));
      assertTrue(person.get("born") instanceof Date, String.valueOf(person.get("born")));
      assertFalse(person.containsKey("nothing"));
      assertEquals(
          List.of(Map.of("result", 2021L)),
          connection.execute("g.E().hasLabel('KNOWS').values('since')", Map.of()));

      // Whole elements come back as maps with id, label and properties
      Map<?, ?> edge =
          (Map<?, ?>) connection.execute("g.E().hasLabel('KNOWS')", Map.of()).get(0).get("result");
      assertEquals("KNOWS", edge.get("label"));
      assertEquals(2021L, ((Map<?, ?>) edge.get("properties")).get("since"));
    }
  }
}
