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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.hop.core.exception.HopException;
import org.apache.hop.neo4j.actions.constraint.ConstraintType;
import org.apache.hop.neo4j.actions.constraint.ConstraintUpdate;
import org.apache.hop.neo4j.actions.constraint.Neo4jConstraint;
import org.apache.hop.neo4j.actions.index.IndexUpdate;
import org.apache.hop.neo4j.actions.index.Neo4jIndex;
import org.apache.hop.neo4j.actions.index.ObjectType;
import org.apache.hop.neo4j.actions.index.UpdateType;
import org.junit.jupiter.api.Test;

/** The statements were checked against Memgraph 3.6, see MemgraphBoltIT. */
class BoltDialectTest {

  private static IndexUpdate index(ObjectType objectType, String name, String properties) {
    return new IndexUpdate(UpdateType.CREATE, objectType, "idx", name, properties);
  }

  private static ConstraintUpdate constraint(
      org.apache.hop.neo4j.actions.constraint.ObjectType objectType,
      ConstraintType type,
      String name,
      String properties) {
    return new ConstraintUpdate(
        org.apache.hop.neo4j.actions.constraint.UpdateType.CREATE,
        objectType,
        type,
        "c",
        name,
        properties);
  }

  /** Neo4j keeps generating exactly what it did before. */
  @Test
  void testNeo4jUnchanged() throws Exception {
    IndexUpdate update = index(ObjectType.NODE, "Person", "name, age");
    assertEquals(
        Neo4jIndex.generateCreateIndexCypher(update),
        Neo4jIndex.generateCreateIndexCypher(update, BoltDialect.NEO4J));
    assertEquals(
        Neo4jIndex.generateDropIndexCypher(update),
        Neo4jIndex.generateDropIndexCypher(update, BoltDialect.NEO4J));
    ConstraintUpdate unique =
        constraint(
            org.apache.hop.neo4j.actions.constraint.ObjectType.NODE,
            ConstraintType.UNIQUE,
            "Person",
            "id");
    assertEquals(
        Neo4jConstraint.generateCreateConstraintCypher(unique),
        Neo4jConstraint.generateCreateConstraintCypher(unique, BoltDialect.NEO4J));
  }

  @Test
  void testMemgraphIndexes() throws Exception {
    assertEquals(
        "CREATE INDEX ON :Person(name, age)",
        Neo4jIndex.generateCreateIndexCypher(
            index(ObjectType.NODE, "Person", "name, age"), BoltDialect.MEMGRAPH));
    assertEquals(
        "DROP INDEX ON :Person(name)",
        Neo4jIndex.generateDropIndexCypher(
            index(ObjectType.NODE, "Person", "name"), BoltDialect.MEMGRAPH));
    assertEquals(
        "CREATE EDGE INDEX ON :KNOWS(since)",
        Neo4jIndex.generateCreateIndexCypher(
            index(ObjectType.RELATIONSHIP, "KNOWS", "since"), BoltDialect.MEMGRAPH));
    assertThrows(
        HopException.class,
        () ->
            Neo4jIndex.generateCreateIndexCypher(
                index(ObjectType.RELATIONSHIP, "KNOWS", "a, b"), BoltDialect.MEMGRAPH));
  }

  @Test
  void testMemgraphConstraints() throws Exception {
    org.apache.hop.neo4j.actions.constraint.ObjectType node =
        org.apache.hop.neo4j.actions.constraint.ObjectType.NODE;
    assertEquals(
        "CREATE CONSTRAINT ON (n:Person) ASSERT n.a, n.b IS UNIQUE",
        Neo4jConstraint.generateCreateConstraintCypher(
            constraint(node, ConstraintType.UNIQUE, "Person", "a, b"), BoltDialect.MEMGRAPH));
    assertEquals(
        "DROP CONSTRAINT ON (n:Person) ASSERT EXISTS (n.id)",
        Neo4jConstraint.generateDropConstraintCypher(
            constraint(node, ConstraintType.NOT_NULL, "Person", "id"), BoltDialect.MEMGRAPH));
    assertThrows(
        HopException.class,
        () ->
            Neo4jConstraint.generateCreateConstraintCypher(
                constraint(node, ConstraintType.NODE_KEY, "Person", "id"), BoltDialect.MEMGRAPH));
    assertThrows(
        HopException.class,
        () ->
            Neo4jConstraint.generateCreateConstraintCypher(
                constraint(
                    org.apache.hop.neo4j.actions.constraint.ObjectType.RELATIONSHIP,
                    ConstraintType.UNIQUE,
                    "KNOWS",
                    "id"),
                BoltDialect.MEMGRAPH));
  }

  @Test
  void testNeptuneHasNoIndexesOrConstraints() {
    assertFalse(BoltDialect.NEPTUNE.isSupportingIndexes());
    assertFalse(BoltDialect.NEPTUNE.isSupportingConstraints());
    assertThrows(
        HopException.class,
        () ->
            Neo4jIndex.generateCreateIndexCypher(
                index(ObjectType.NODE, "Person", "name"), BoltDialect.NEPTUNE));
  }

  @Test
  void testSchemaChangesInTransactions() {
    assertTrue(BoltDialect.NEO4J.isSupportingSchemaChangesInTransactions());
    assertFalse(BoltDialect.MEMGRAPH.isSupportingSchemaChangesInTransactions());
  }
}
