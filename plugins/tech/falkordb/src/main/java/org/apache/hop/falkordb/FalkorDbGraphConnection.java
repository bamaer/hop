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

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.output.NestedMultiOutput;
import io.lettuce.core.protocol.CommandArgs;
import io.lettuce.core.protocol.ProtocolKeyword;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.graph.IGraphConnection;
import org.apache.hop.core.graph.IGraphTransaction;
import org.apache.hop.core.graph.IGraphTransactionWork;
import org.apache.hop.core.logging.ILogChannel;

/**
 * A connection to one FalkorDB graph. FalkorDB runs every query as a transaction of its own: there
 * are no multi-statement transactions, so a transaction here executes its statements right away.
 */
public class FalkorDbGraphConnection implements IGraphConnection {

  /** The GRAPH.QUERY command of FalkorDB. */
  private enum GraphCommand implements ProtocolKeyword {
    QUERY("GRAPH.QUERY");

    private final byte[] bytes;

    GraphCommand(String name) {
      bytes = name.getBytes(StandardCharsets.US_ASCII);
    }

    @Override
    public byte[] getBytes() {
      return bytes;
    }
  }

  private final RedisClient client;
  private final StatefulRedisConnection<String, String> connection;
  private final String graphName;
  private final ILogChannel log;

  public FalkorDbGraphConnection(
      RedisClient client,
      StatefulRedisConnection<String, String> connection,
      String graphName,
      ILogChannel log) {
    this.client = client;
    this.connection = connection;
    this.graphName = graphName;
    this.log = log;
  }

  @Override
  public List<Map<String, Object>> execute(String statement, Map<String, Object> parameters)
      throws HopException {
    String query = FalkorDbCypher.withParameters(statement, parameters);
    try {
      List<Object> reply = query(query, true);
      if (log != null && log.isDebug()) {
        log.logDebug("FalkorDB statistics: " + reply.get(reply.size() - 1));
      }
      return FalkorDbCypher.toRows(reply, names);
    } catch (Exception e) {
      throw new HopException(
          "Error executing statement on FalkorDB graph " + graphName + ": " + statement, e);
    }
  }

  private List<Object> query(String query, boolean compact) {
    CommandArgs<String, String> args =
        new CommandArgs<>(StringCodec.UTF8).addKey(graphName).add(query);
    if (compact) {
      args.add("--compact");
    }
    return connection
        .sync()
        .dispatch(GraphCommand.QUERY, new NestedMultiOutput<>(StringCodec.UTF8), args);
  }

  /**
   * The names of labels, relationship types and property keys by id, read with the db.labels(),
   * db.relationshipTypes() and db.propertyKeys() procedures. They are read again when an id isn't
   * known yet: new ones are added at the end.
   */
  private final FalkorDbCypher.NameResolver names =
      new FalkorDbCypher.NameResolver() {
        private final List<String> labels = new ArrayList<>();
        private final List<String> relationshipTypes = new ArrayList<>();
        private final List<String> propertyKeys = new ArrayList<>();

        @Override
        public String label(int id) {
          return resolve(labels, id, "CALL db.labels()");
        }

        @Override
        public String relationshipType(int id) {
          return resolve(relationshipTypes, id, "CALL db.relationshipTypes()");
        }

        @Override
        public String propertyKey(int id) {
          return resolve(propertyKeys, id, "CALL db.propertyKeys()");
        }

        private String resolve(List<String> cache, int id, String procedure) {
          if (id >= cache.size()) {
            cache.clear();
            List<Object> reply = query(procedure, false);
            if (reply.size() >= 3) {
              for (Object row : (List<?>) reply.get(1)) {
                cache.add(String.valueOf(((List<?>) row).get(0)));
              }
            }
          }
          return id < cache.size() ? cache.get(id) : Integer.toString(id);
        }
      };

  @Override
  public IGraphTransaction beginTransaction() {
    return new IGraphTransaction() {
      @Override
      public List<Map<String, Object>> execute(String statement, Map<String, Object> parameters)
          throws HopException {
        return FalkorDbGraphConnection.this.execute(statement, parameters);
      }

      @Override
      public void commit() {
        // Every statement is committed on its own
      }

      @Override
      public void rollback() {
        // Every statement is committed on its own
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
    return FalkorDbGraphDatabase.DIALECT;
  }

  @Override
  public boolean isSupportingTransactions() {
    return false;
  }

  @Override
  public void close() {
    try {
      connection.close();
    } finally {
      client.shutdown();
    }
  }
}
