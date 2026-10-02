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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.graph.IGraphConnection;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

/** An open Bolt connection: a driver with one session. Closing it closes both. */
@Getter
public class BoltGraphConnection implements IGraphConnection {
  private final Driver driver;
  private final Session session;

  public BoltGraphConnection(Driver driver, Session session) {
    this.driver = driver;
    this.session = session;
  }

  @Override
  public List<Map<String, Object>> execute(String statement, Map<String, Object> parameters)
      throws HopException {
    try {
      Result result = session.run(statement, parameters == null ? Map.of() : parameters);
      List<Map<String, Object>> rows = new ArrayList<>();
      while (result.hasNext()) {
        rows.add(result.next().asMap());
      }
      return rows;
    } catch (Exception e) {
      throw new HopException("Error executing statement: " + statement, e);
    }
  }

  @Override
  public void close() throws HopException {
    try {
      session.close();
    } finally {
      driver.close();
    }
  }
}
