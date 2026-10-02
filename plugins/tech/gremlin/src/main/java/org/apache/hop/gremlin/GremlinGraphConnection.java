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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.graph.GraphUpsertNode;
import org.apache.hop.core.graph.GraphUpsertRelationship;
import org.apache.hop.core.graph.IGraphConnection;
import org.apache.hop.core.graph.IGraphTransaction;
import org.apache.hop.core.graph.IGraphTransactionWork;
import org.apache.hop.core.logging.ILogChannel;
import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;

/**
 * A connection to a Gremlin server. Statements are Gremlin scripts, with their parameters as
 * bindings. Nodes and relationships are upserted with traversals: no scripts, so this also works on
 * servers which don't run scripts with bindings. Every request is a transaction of its own.
 */
public class GremlinGraphConnection implements IGraphConnection {
  /** How many nodes or relationships go into one upsert traversal. */
  private static final int UPSERT_CHUNK_SIZE = 100;

  private final Cluster cluster;
  private final Client client;
  private final GraphTraversalSource g;
  private final ILogChannel log;

  public GremlinGraphConnection(
      Cluster cluster, Client client, GraphTraversalSource g, ILogChannel log) {
    this.cluster = cluster;
    this.client = client;
    this.g = g;
    this.log = log;
  }

  @Override
  public List<Map<String, Object>> execute(String statement, Map<String, Object> parameters)
      throws HopException {
    try {
      List<Result> results =
          client.submit(statement, parameters == null ? Map.of() : parameters).all().get();
      List<Map<String, Object>> rows = new ArrayList<>();
      for (Result result : results) {
        rows.add(GremlinValues.toRow(result.getObject()));
      }
      return rows;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new HopException("Interrupted executing Gremlin script: " + statement, e);
    } catch (Exception e) {
      throw new HopException("Error executing Gremlin script: " + statement, e);
    }
  }

  @Override
  public boolean isSupportingUpserts() {
    return true;
  }

  @Override
  public void upsert(List<GraphUpsertNode> nodes, List<GraphUpsertRelationship> relationships)
      throws HopException {
    try {
      for (int start = 0; start < nodes.size(); start += UPSERT_CHUNK_SIZE) {
        upsertNodes(nodes.subList(start, Math.min(nodes.size(), start + UPSERT_CHUNK_SIZE)));
      }
      for (int start = 0; start < relationships.size(); start += UPSERT_CHUNK_SIZE) {
        upsertRelationships(
            relationships.subList(
                start, Math.min(relationships.size(), start + UPSERT_CHUNK_SIZE)));
      }
    } catch (Exception e) {
      throw new HopException("Error writing nodes and relationships to the Gremlin server", e);
    }
  }

  /**
   * One traversal per chunk: for every node V().has(keys).fold().coalesce(unfold(),
   * addV(label).property(keys)) followed by the other properties.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  private void upsertNodes(List<GraphUpsertNode> nodes) {
    GraphTraversal traversal = null;
    for (GraphUpsertNode node : nodes) {
      GraphTraversal match = traversal == null ? g.V() : traversal.V();
      match = hasKeys(match, node);
      GraphTraversal create = __.addV(node.label());
      for (Map.Entry<String, Object> key : node.keys().entrySet()) {
        create = create.property(key.getKey(), GremlinValues.toPropertyValue(key.getValue()));
      }
      traversal = match.fold().coalesce(__.unfold(), create);
      traversal = setProperties(traversal, node.properties());
    }
    if (traversal != null) {
      traversal.iterate();
    }
  }

  /**
   * One traversal per chunk: for every relationship the existing edge between the two nodes, or a
   * new one, followed by its properties. The nodes are looked up by their keys.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  private void upsertRelationships(List<GraphUpsertRelationship> relationships) {
    GraphTraversal traversal = null;
    for (GraphUpsertRelationship relationship : relationships) {
      GraphTraversal existing =
          hasKeys(__.V(), relationship.source())
              .outE(relationship.label())
              .where(hasKeys(__.inV(), relationship.target()));
      GraphTraversal create =
          __.addE(relationship.label())
              .from(hasKeys(__.V(), relationship.source()))
              .to(hasKeys(__.V(), relationship.target()));
      GraphTraversal start = traversal == null ? g.inject(0) : traversal;
      traversal = setProperties(start.coalesce(existing, create), relationship.properties());
    }
    if (traversal != null) {
      traversal.iterate();
    }
  }

  @SuppressWarnings("rawtypes")
  private static GraphTraversal hasKeys(GraphTraversal traversal, GraphUpsertNode node) {
    GraphTraversal result = traversal.hasLabel(node.label());
    for (Map.Entry<String, Object> key : node.keys().entrySet()) {
      result = result.has(key.getKey(), GremlinValues.toPropertyValue(key.getValue()));
    }
    return result;
  }

  /** Set the properties. Null values are skipped: Gremlin properties can't be null. */
  @SuppressWarnings("rawtypes")
  private static GraphTraversal setProperties(
      GraphTraversal traversal, Map<String, Object> properties) {
    GraphTraversal result = traversal;
    for (Map.Entry<String, Object> property : properties.entrySet()) {
      if (property.getValue() != null) {
        result =
            result.property(property.getKey(), GremlinValues.toPropertyValue(property.getValue()));
      }
    }
    return result;
  }

  @Override
  public IGraphTransaction beginTransaction() {
    return new IGraphTransaction() {
      @Override
      public List<Map<String, Object>> execute(String statement, Map<String, Object> parameters)
          throws HopException {
        return GremlinGraphConnection.this.execute(statement, parameters);
      }

      @Override
      public void commit() {
        // Every request is committed on its own
      }

      @Override
      public void rollback() {
        // Every request is committed on its own
      }

      @Override
      public void close() {
        // Nothing to close
      }
    };
  }

  @Override
  public <T> T executeWrite(IGraphTransactionWork<T> work) throws HopException {
    return work.execute(beginTransaction());
  }

  @Override
  public String getDialect() {
    return GremlinGraphDatabase.DIALECT;
  }

  @Override
  public boolean isSupportingTransactions() {
    return false;
  }

  @Override
  public void close() throws HopException {
    try {
      g.close();
    } catch (Exception e) {
      if (log != null) {
        log.logDetailed("Error closing the Gremlin traversal source: " + e.getMessage());
      }
    } finally {
      client.close();
      cluster.close();
    }
  }
}
