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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.hop.core.exception.HopException;
import org.junit.jupiter.api.Test;

class AgeCypherTest {

  @Test
  void testReturnColumns() throws Exception {
    assertEquals(List.of(), AgeCypher.getReturnColumns("CREATE (n:Person {id: $id})"));
    assertEquals(
        List.of("id", "name"),
        AgeCypher.getReturnColumns("MATCH (n:Person) RETURN n.id AS id, n.name AS name"));
    assertEquals(
        List.of("n", "count(*)"),
        AgeCypher.getReturnColumns("MATCH (n) return n, count(*) ORDER BY n.id LIMIT 3"));
    assertEquals(
        List.of("my name"),
        AgeCypher.getReturnColumns("MATCH (n) RETURN DISTINCT n.name AS `my name`;"));
    // Commas and keywords in maps, lists, strings and subqueries don't count
    assertEquals(
        List.of("m", "l"),
        AgeCypher.getReturnColumns(
            "MATCH (n) WHERE n.x = 'RETURN a, b' "
                + "CALL { WITH n RETURN n.y AS y } "
                + "RETURN {a: 1, b: 2} AS m, [1, 2] AS l"));
    assertThrows(HopException.class, () -> AgeCypher.getReturnColumns("MATCH (n) RETURN *"));
  }

  @Test
  void testSql() {
    assertEquals(
        "SELECT * FROM ag_catalog.cypher('hop''s', $hop$ MATCH (n) RETURN n $hop$, ?) AS (c1 agtype)",
        AgeCypher.toSql("hop's", "MATCH (n) RETURN n", List.of("n"), true));
    assertEquals(
        "SELECT * FROM ag_catalog.cypher('g', $hop1$ RETURN '$hop$' $hop1$) AS (v agtype)",
        AgeCypher.toSql("g", "RETURN '$hop$'", List.of(), false));
    assertEquals(
        "SELECT * FROM ag_catalog.cypher('g', $hop$ CREATE (n) $hop$) AS (v agtype)",
        AgeCypher.toSql("g", " CREATE (n) ;\n", List.of(), false));
  }

  @Test
  void testParameters() throws Exception {
    Map<String, Object> row = new LinkedHashMap<>();
    row.put("id", 1L);
    row.put("name", "Ann's");
    row.put("day", LocalDate.of(2024, 1, 2));
    row.put("none", null);
    assertEquals(
        "{\"props\":[{\"id\":1,\"name\":\"Ann's\",\"day\":\"2024-01-02\",\"none\":null}]}",
        AgeCypher.toParameterJson(Map.of("props", List.of(row))));
  }

  @Test
  void testValues() throws Exception {
    assertEquals(1L, AgeCypher.toValue("1"));
    assertEquals(1.5d, AgeCypher.toValue("1.5"));
    assertEquals("x::y", AgeCypher.toValue("\"x::y\""));
    Map<?, ?> vertex =
        (Map<?, ?>)
            AgeCypher.toValue(
                "{\"id\": 844424930131969, \"label\": \"Person\", \"properties\": {\"name\": \"A::B\"}}::vertex");
    assertEquals("Person", vertex.get("label"));
    assertEquals(Map.of("name", "A::B"), vertex.get("properties"));
    List<?> path =
        (List<?>)
            AgeCypher.toValue(
                "[{\"id\": 1, \"label\": \"A\", \"properties\": {}}::vertex, "
                    + "{\"id\": 2, \"label\": \"R\", \"end_id\": 3, \"start_id\": 1, \"properties\": {}}::edge, "
                    + "{\"id\": 3, \"label\": \"A\", \"properties\": {}}::vertex]::path");
    assertEquals(3, path.size());
  }
}
