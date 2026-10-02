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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.graph.IGraphConnection;
import org.apache.hop.core.graph.IGraphTransaction;
import org.apache.hop.core.graph.IGraphTransactionWork;
import org.apache.hop.core.logging.ILogChannel;

/** A JDBC connection to PostgreSQL with Apache AGE, working with one graph. */
public class AgeGraphConnection implements IGraphConnection {
  private final Connection connection;
  private final String graphName;
  private final ILogChannel log;

  public AgeGraphConnection(Connection connection, String graphName, ILogChannel log) {
    this.connection = connection;
    this.graphName = graphName;
    this.log = log;
  }

  @Override
  public List<Map<String, Object>> execute(String statement, Map<String, Object> parameters)
      throws HopException {
    List<String> columns = AgeCypher.getReturnColumns(statement);
    boolean withParameters = parameters != null && !parameters.isEmpty();
    String sql = AgeCypher.toSql(graphName, statement, columns, withParameters);
    if (log != null && log.isDebug()) {
      log.logDebug("Apache AGE query: " + sql);
    }
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      if (withParameters) {
        ps.setObject(1, AgeCypher.toParameterJson(parameters), Types.OTHER);
      }
      List<Map<String, Object>> rows = new ArrayList<>();
      if (ps.execute()) {
        try (ResultSet resultSet = ps.getResultSet()) {
          while (resultSet.next()) {
            if (columns.isEmpty()) {
              continue;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 0; i < columns.size(); i++) {
              row.put(columns.get(i), AgeCypher.toValue(resultSet.getString(i + 1)));
            }
            rows.add(row);
          }
        }
      }
      return rows;
    } catch (SQLException e) {
      throw new HopException(
          "Error executing statement on Apache AGE graph " + graphName + ": " + statement, e);
    }
  }

  @Override
  public IGraphTransaction beginTransaction() throws HopException {
    try {
      connection.setAutoCommit(false);
    } catch (SQLException e) {
      throw new HopException("Unable to start a transaction", e);
    }
    return new IGraphTransaction() {
      private boolean finished;

      @Override
      public List<Map<String, Object>> execute(String statement, Map<String, Object> parameters)
          throws HopException {
        return AgeGraphConnection.this.execute(statement, parameters);
      }

      @Override
      public void commit() throws HopException {
        try {
          connection.commit();
          finished = true;
          connection.setAutoCommit(true);
        } catch (SQLException e) {
          throw new HopException("Unable to commit the transaction", e);
        }
      }

      @Override
      public void rollback() throws HopException {
        try {
          connection.rollback();
          finished = true;
          connection.setAutoCommit(true);
        } catch (SQLException e) {
          throw new HopException("Unable to roll back the transaction", e);
        }
      }

      @Override
      public void close() throws HopException {
        if (!finished) {
          rollback();
        }
      }
    };
  }

  @Override
  public <T> T executeWrite(IGraphTransactionWork<T> work) throws HopException {
    IGraphTransaction transaction = beginTransaction();
    try {
      T result = work.execute(transaction);
      transaction.commit();
      return result;
    } finally {
      transaction.close();
    }
  }

  @Override
  public String getDialect() {
    return AgeGraphDatabase.DIALECT;
  }

  @Override
  public void close() throws HopException {
    try {
      connection.close();
    } catch (SQLException e) {
      throw new HopException("Unable to close the Apache AGE connection", e);
    }
  }
}
