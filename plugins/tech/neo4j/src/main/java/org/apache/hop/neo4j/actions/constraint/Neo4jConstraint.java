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

package org.apache.hop.neo4j.actions.constraint;

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
    id = "NEO4J_CONSTRAINT",
    name = "Neo4j constraint",
    description = "Create or delete constraints in a Neo4j database",
    image = "neo4j_constraint.svg",
    categoryDescription = "i18n:org.apache.hop.workflow:ActionCategory.Category.Scripting",
    keywords = "i18n::Neo4jConstraint.keyword",
    documentationUrl = "/workflow/actions/neo4j-constraint.html")
public class Neo4jConstraint extends ActionBase implements IAction {

  /** The name of the Neo4j or Bolt graph database connection. */
  @HopMetadataProperty(key = "connection")
  private String connectionName;

  private NamedGraphConnection connection;

  @HopMetadataProperty(groupKey = "updates", key = "update")
  private List<ConstraintUpdate> constraintUpdates;

  public Neo4jConstraint() {
    this("", "");
  }

  public Neo4jConstraint(String name) {
    this(name, "");
  }

  public Neo4jConstraint(String name, String description) {
    super(name, description);
    constraintUpdates = new ArrayList<>();
  }

  @Override
  public Result execute(Result result, int nr) throws HopException {

    connection =
        NeoConnectionUtils.findGraphConnection(getMetadataProvider(), resolve(connectionName));

    if (connection == null) {
      result.setResult(false);
      result.increaseErrors(1L);
      throw new HopException("Please specify a Neo4j connection to use");
    }

    // Loop over the constraint updates to see which need deleting...
    //
    for (ConstraintUpdate constraintUpdate : constraintUpdates) {
      if (constraintUpdate.getUpdateType() == null) {
        throw new HopException("Please make sure to always specify a constraint update type");
      }
      switch (constraintUpdate.getUpdateType()) {
        case DROP:
          dropConstraint(constraintUpdate);
          break;
        default:
          break;
      }
    }

    // Create the constraints if needed
    //
    for (ConstraintUpdate constraintUpdate : constraintUpdates) {
      switch (constraintUpdate.getUpdateType()) {
        case CREATE:
          createConstraint(constraintUpdate);
          break;
        default:
          break;
      }
    }

    return result;
  }

  /**
   * Generate preview Cypher for dropping a constraint (without executing it)
   *
   * @param constraintUpdate The constraint update configuration
   * @return The generated Cypher statement
   * @throws HopException If constraint name is missing for relationship constraints
   */
  /**
   * Generate the Cypher to drop a constraint in the given dialect.
   *
   * @throws HopException if the database doesn't support it or information is missing
   */
  public static String generateDropConstraintCypher(
      ConstraintUpdate constraintUpdate, CypherDialect dialect) throws HopException {
    validateConstraintSupport(constraintUpdate, dialect);
    if (dialect == CypherDialect.MEMGRAPH) {
      return "DROP " + getMemgraphConstraintClause(constraintUpdate);
    }
    return generateDropConstraintCypher(constraintUpdate);
  }

  /**
   * Generate the Cypher to create a constraint in the given dialect.
   *
   * @throws HopException if the database doesn't support it or information is missing
   */
  public static String generateCreateConstraintCypher(
      ConstraintUpdate constraintUpdate, CypherDialect dialect) throws HopException {
    validateConstraintSupport(constraintUpdate, dialect);
    if (dialect == CypherDialect.MEMGRAPH) {
      return "CREATE " + getMemgraphConstraintClause(constraintUpdate);
    }
    return generateCreateConstraintCypher(constraintUpdate);
  }

  private static void validateConstraintSupport(
      ConstraintUpdate constraintUpdate, CypherDialect dialect) throws HopException {
    java.util.Set<ConstraintType> supported =
        constraintUpdate.getObjectType() == ObjectType.RELATIONSHIP
            ? dialect.getRelationshipConstraintTypes()
            : dialect.getNodeConstraintTypes();
    if (!supported.contains(constraintUpdate.getConstraintType())) {
      throw new HopException(
          constraintUpdate.getConstraintType()
              + " constraints on "
              + constraintUpdate.getObjectType()
              + " are not supported by "
              + dialect
              + ", for "
              + constraintUpdate.getObjectName());
    }
  }

  /** Memgraph constraints have no name: CONSTRAINT ON (n:Label) ASSERT ... */
  private static String getMemgraphConstraintClause(ConstraintUpdate constraintUpdate)
      throws HopException {
    if (StringUtils.isEmpty(constraintUpdate.getObjectName())
        || StringUtils.isEmpty(constraintUpdate.getObjectProperties())) {
      throw new HopException(
          "Memgraph constraints are identified by label and properties, please specify both."
              + " Constraint: "
              + constraintUpdate.getConstraintName());
    }
    String[] properties = constraintUpdate.getObjectProperties().split(",");
    StringBuilder clause =
        new StringBuilder("CONSTRAINT ON (n:" + constraintUpdate.getObjectName() + ") ASSERT ");
    if (constraintUpdate.getConstraintType() == ConstraintType.NOT_NULL) {
      if (properties.length > 1) {
        throw new HopException(
            "Memgraph existence constraints are on a single property, not on "
                + constraintUpdate.getObjectProperties());
      }
      clause.append("EXISTS (n.").append(Const.trim(properties[0])).append(")");
    } else {
      for (int i = 0; i < properties.length; i++) {
        if (i > 0) {
          clause.append(", ");
        }
        clause.append("n.").append(Const.trim(properties[i]));
      }
      clause.append(" IS UNIQUE");
    }
    return clause.toString();
  }

  public static String generateDropConstraintCypher(ConstraintUpdate constraintUpdate)
      throws HopException {
    String cypher = "DROP CONSTRAINT ";

    if (StringUtils.isNotEmpty(constraintUpdate.getConstraintName())) {
      cypher += constraintUpdate.getConstraintName();
    } else {
      throw new HopException(
          "Please drop constraint on relationship properties with the name of the constraint. This was for label: "
              + constraintUpdate.getObjectName()
              + ", properties: "
              + constraintUpdate.getObjectProperties());
    }
    cypher += " IF EXISTS ";
    return cypher;
  }

  private void dropConstraint(final ConstraintUpdate constraintUpdate) throws HopException {
    String cypher = generateDropConstraintCypher(constraintUpdate, connection.getDialect());

    // Run this cypher statement...
    //
    NeoConnectionUtils.runSchemaStatement(
        connection, getLogChannel(), this, cypher, "Dropping constraint");
  }

  /**
   * Generate preview Cypher for creating a constraint (without executing it)
   *
   * @param constraintUpdate The constraint update configuration
   * @return The generated Cypher statement
   * @throws HopException If configuration is invalid
   */
  public static String generateCreateConstraintCypher(ConstraintUpdate constraintUpdate)
      throws HopException {
    String cypher = "CREATE CONSTRAINT ";

    if (StringUtils.isNotEmpty(constraintUpdate.getConstraintName())) {
      cypher += constraintUpdate.getConstraintName();
    } else {
      throw new HopException(
          "Please create constraints on relationship properties with a name for the constraint. This was for label: "
              + constraintUpdate.getObjectName()
              + ", properties: "
              + constraintUpdate.getObjectProperties());
    }

    cypher += " IF NOT EXISTS FOR ";

    if (constraintUpdate.getObjectType() == ObjectType.NODE) {
      // Constraint on a node
      //
      cypher += "(n:" + constraintUpdate.getObjectName() + ") ";
      cypher += "REQUIRE ";
      switch (constraintUpdate.getConstraintType()) {
        case UNIQUE:
          cypher += " n." + constraintUpdate.getObjectProperties() + " IS UNIQUE ";
          break;
        case NOT_NULL:
          cypher += " n." + constraintUpdate.getObjectProperties() + " IS NOT NULL ";
          break;
        case NODE_KEY:
          // NODE_KEY requires multiple properties (comma-separated)
          String properties = constraintUpdate.getObjectProperties();
          if (StringUtils.isEmpty(properties)) {
            throw new HopException(
                "NODE_KEY constraint requires at least one property. Properties: " + properties);
          }
          String[] props = properties.split(",");
          if (props.length < 1) {
            throw new HopException(
                "NODE_KEY constraint requires at least one property. Properties: " + properties);
          }
          // Format as (n.prop1, n.prop2, ...) IS NODE KEY
          StringBuilder propsList = new StringBuilder("(");
          for (int i = 0; i < props.length; i++) {
            if (i > 0) {
              propsList.append(", ");
            }
            propsList.append("n.").append(props[i].trim());
          }
          propsList.append(")");
          cypher += propsList.toString() + " IS NODE KEY ";
          break;
        default:
          throw new HopException(
              "Unsupported constraint type: " + constraintUpdate.getConstraintType());
      }

    } else {
      // constraint on a relationship
      //
      cypher += "()-[r:" + constraintUpdate.getObjectName() + "]-() ";
      cypher += "REQUIRE ";
      switch (constraintUpdate.getConstraintType()) {
        case UNIQUE:
          cypher += " r." + constraintUpdate.getObjectProperties() + " IS UNIQUE ";
          break;
        case NOT_NULL:
          cypher += " r." + constraintUpdate.getObjectProperties() + " IS NOT NULL ";
          break;
        case NODE_KEY:
          throw new HopException(
              "NODE_KEY constraint type is only supported for nodes, not relationships");
        default:
          throw new HopException(
              "Unsupported constraint type: " + constraintUpdate.getConstraintType());
      }
    }

    return cypher;
  }

  private void createConstraint(ConstraintUpdate constraintUpdate) throws HopException {
    String cypher = generateCreateConstraintCypher(constraintUpdate, connection.getDialect());

    // Run this cypher statement...
    //
    NeoConnectionUtils.runSchemaStatement(
        connection, getLogChannel(), this, cypher, "Creating constraint");
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
   * Gets constraintUpdates
   *
   * @return value of constraintUpdates
   */
  public List<ConstraintUpdate> getConstraintUpdates() {
    return constraintUpdates;
  }

  /**
   * @param constraintUpdates The constraintUpdates to set
   */
  public void setConstraintUpdates(List<ConstraintUpdate> constraintUpdates) {
    this.constraintUpdates = constraintUpdates;
  }
}
