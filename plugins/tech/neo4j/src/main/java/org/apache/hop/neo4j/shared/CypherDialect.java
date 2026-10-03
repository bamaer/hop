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

package org.apache.hop.neo4j.shared;

import java.util.EnumSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.hop.neo4j.actions.constraint.ConstraintType;

/**
 * The Cypher dialect of a Bolt graph database: which index and constraint statements it supports.
 * Statements the transforms and actions generate otherwise are the same for all Bolt databases.
 */
public enum CypherDialect {
  /** Neo4j 5: named indexes and constraints, IF [NOT] EXISTS. */
  NEO4J(
      true,
      true,
      true,
      EnumSet.allOf(ConstraintType.class),
      EnumSet.of(ConstraintType.UNIQUE, ConstraintType.NOT_NULL)),

  /** Memgraph: unnamed label-property and edge-type-property indexes, node constraints only. */
  MEMGRAPH(
      true,
      true,
      false,
      EnumSet.of(ConstraintType.UNIQUE, ConstraintType.NOT_NULL),
      EnumSet.noneOf(ConstraintType.class)),

  /**
   * FalkorDB: unnamed indexes in Cypher, constraints only through its own commands. Every statement
   * is a transaction of its own.
   */
  FALKORDB(
      true, true, true, EnumSet.noneOf(ConstraintType.class), EnumSet.noneOf(ConstraintType.class)),

  /** Apache AGE: indexes and constraints are PostgreSQL ones, not Cypher. */
  AGE(
      false,
      false,
      true,
      EnumSet.noneOf(ConstraintType.class),
      EnumSet.noneOf(ConstraintType.class)),

  /**
   * Gremlin servers don't speak Cypher: Graph Output writes to them with upserts, the Cypher
   * transform and script run Gremlin scripts. No index or constraint statements.
   */
  GREMLIN(
      false,
      false,
      true,
      EnumSet.noneOf(ConstraintType.class),
      EnumSet.noneOf(ConstraintType.class)),

  /** Amazon Neptune manages its indexes itself and has no constraints. */
  NEPTUNE(
      false,
      false,
      true,
      EnumSet.noneOf(ConstraintType.class),
      EnumSet.noneOf(ConstraintType.class));

  private final boolean supportingNodeIndexes;
  private final boolean supportingRelationshipIndexes;
  private final boolean supportingSchemaChangesInTransactions;
  private final Set<ConstraintType> nodeConstraintTypes;
  private final Set<ConstraintType> relationshipConstraintTypes;

  CypherDialect(
      boolean supportingNodeIndexes,
      boolean supportingRelationshipIndexes,
      boolean supportingSchemaChangesInTransactions,
      Set<ConstraintType> nodeConstraintTypes,
      Set<ConstraintType> relationshipConstraintTypes) {
    this.supportingNodeIndexes = supportingNodeIndexes;
    this.supportingRelationshipIndexes = supportingRelationshipIndexes;
    this.supportingSchemaChangesInTransactions = supportingSchemaChangesInTransactions;
    this.nodeConstraintTypes = nodeConstraintTypes;
    this.relationshipConstraintTypes = relationshipConstraintTypes;
  }

  /**
   * @return False for databases which don't speak Cypher, like Gremlin servers
   */
  /**
   * The statements Memgraph doesn't run in an explicit transaction: information queries like SHOW
   * INDEX INFO, index and constraint changes, and a few administrative statements.
   */
  private static final Pattern MEMGRAPH_AUTO_COMMIT_STATEMENT =
      Pattern.compile(
          "^\\s*(SHOW\\b|DROP\\s+ALL\\b|ANALYZE\\s+GRAPH\\b|FREE\\s+MEMORY\\b|STORAGE\\s+MODE\\b"
              + "|(CREATE|DROP)\\s+(\\w+\\s+)?(INDEX|CONSTRAINT)\\b)",
          Pattern.CASE_INSENSITIVE);

  /**
   * @return True if the database refuses to run this statement in an explicit transaction, so it
   *     has to run on its own in an auto-commit transaction
   */
  public boolean isRequiringAutoCommit(String statement) {
    return this == MEMGRAPH
        && statement != null
        && MEMGRAPH_AUTO_COMMIT_STATEMENT.matcher(stripLeadingComments(statement)).find();
  }

  /** The statement without the whitespace and comments it starts with. */
  static String stripLeadingComments(String statement) {
    String rest = statement.stripLeading();
    while (true) {
      if (rest.startsWith("//")) {
        int newline = rest.indexOf('\n');
        rest = newline < 0 ? "" : rest.substring(newline + 1).stripLeading();
      } else if (rest.startsWith("/*")) {
        int end = rest.indexOf("*/");
        rest = end < 0 ? "" : rest.substring(end + 2).stripLeading();
      } else {
        return rest;
      }
    }
  }

  public boolean isCypher() {
    return this != GREMLIN;
  }

  /**
   * The Cypher expression that stores a vector parameter in a property. FalkorDB only indexes
   * vectors created with {@code vecf32()}; the other databases store the list of numbers as is.
   *
   * @param parameterExpression The parameter holding the vector, like {@code $param1}
   */
  public String vectorValue(String parameterExpression) {
    return this == FALKORDB ? "vecf32(" + parameterExpression + ")" : parameterExpression;
  }

  /** The dialect with the given name, Neo4j for anything unknown. */
  public static CypherDialect fromId(String id) {
    for (CypherDialect dialect : values()) {
      if (dialect.name().equalsIgnoreCase(id)) {
        return dialect;
      }
    }
    return NEO4J;
  }

  public boolean isSupportingNodeIndexes() {
    return supportingNodeIndexes;
  }

  public boolean isSupportingRelationshipIndexes() {
    return supportingRelationshipIndexes;
  }

  /**
   * @return false if index and constraint changes have to run in auto-commit transactions, as on
   *     Memgraph
   */
  public boolean isSupportingSchemaChangesInTransactions() {
    return supportingSchemaChangesInTransactions;
  }

  public boolean isSupportingIndexes() {
    return supportingNodeIndexes || supportingRelationshipIndexes;
  }

  /** The constraint types this database supports on nodes. */
  public Set<ConstraintType> getNodeConstraintTypes() {
    return nodeConstraintTypes;
  }

  /** The constraint types this database supports on relationships. */
  public Set<ConstraintType> getRelationshipConstraintTypes() {
    return relationshipConstraintTypes;
  }

  public boolean isSupportingConstraints() {
    return !nodeConstraintTypes.isEmpty() || !relationshipConstraintTypes.isEmpty();
  }
}
