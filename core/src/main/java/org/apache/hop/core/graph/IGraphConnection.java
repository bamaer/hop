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

package org.apache.hop.core.graph;

import java.util.List;
import java.util.Map;
import org.apache.hop.core.exception.HopException;

/** An open connection to a graph database. Not thread-safe: use one per thread. */
public interface IGraphConnection extends AutoCloseable {

  /**
   * Execute a statement.
   *
   * @param statement The statement in the query language of the database
   * @param parameters The statement parameters, may be empty
   * @return The result rows, column name to value
   * @throws HopException In case the statement failed
   */
  List<Map<String, Object>> execute(String statement, Map<String, Object> parameters)
      throws HopException;

  /**
   * Begin an explicit transaction. Databases without multi-statement transactions execute each
   * statement on its own and ignore commit and rollback.
   */
  IGraphTransaction beginTransaction() throws HopException;

  /**
   * Run work in a write transaction, committed when the work returns. Where the database supports
   * it, the work is retried on transient errors.
   */
  <T> T executeWrite(IGraphTransactionWork<T> work) throws HopException;

  /**
   * @return The Cypher dialect of the database, for example NEO4J or MEMGRAPH. The plugins
   *     generating statements use it to decide which syntax to use.
   */
  default String getDialect() {
    return "NEO4J";
  }

  /**
   * @return True if a transaction groups several statements atomically. False if every statement is
   *     executed and committed on its own, whatever transaction it runs in.
   */
  default boolean isSupportingTransactions() {
    return true;
  }

  /**
   * @return True if this connection writes nodes and relationships with {@link #upsert} instead of
   *     with statements, as for databases which don't speak Cypher.
   */
  default boolean isSupportingUpserts() {
    return false;
  }

  /**
   * Create or update nodes and then relationships, in this order.
   *
   * @param nodes The nodes to upsert
   * @param relationships The relationships to upsert between nodes, which are upserted first
   */
  default void upsert(List<GraphUpsertNode> nodes, List<GraphUpsertRelationship> relationships)
      throws HopException {
    throw new HopException("This graph database connection doesn't support upserts");
  }

  @Override
  void close() throws HopException;
}
