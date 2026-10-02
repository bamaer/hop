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

import java.util.EnumSet;
import java.util.Set;
import org.apache.hop.neo4j.actions.constraint.ConstraintType;

/**
 * The Cypher dialect of a Bolt graph database: which index and constraint statements it supports.
 * Statements the transforms and actions generate otherwise are the same for all Bolt databases.
 */
public enum BoltDialect {
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

  BoltDialect(
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
