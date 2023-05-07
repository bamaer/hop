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

package org.apache.pipeline.transform.jdbcmetadata;

import org.apache.hop.core.database.Database;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.pipeline.transform.BaseTransformData;
import org.apache.hop.pipeline.transform.ITransformData;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

public class JdbcMetadataData extends BaseTransformData implements ITransformData {

  public IRowMeta outputRowMeta;
  // used to store the named connection
  public Database database;
  // used to store the actual jdbc connection
  public Connection connection;
  // used to store the DatabaseMetaData method that generates the data
  public Method method;
  // use to store the arguments to the method
  public List<String> arguments;
  // named kettle connection cache. Used when connection is a named connection specified by input
  // fields
  public Map<String, Database> databases = null;
  // connection cache. Used when connection is a jdbc connection specified by input fields
  public Map<String[], Connection> connections = null;
  // key to the connection cache.
  public String[] connectionKey = null;
  // field index for named kettle connection
  public int connectionField = -1;
  // field indices for jdbc connections
  public int jdbcDriverField = -1;
  public int jdbcUrlField = -1;
  public int jdbcUserField = -1;
  public int jdbcPasswordField = -1;
  // indices for fields used to specify method arguments
  public int[] argumentFieldIndices;
  // the offset in the output row from where we can add our metadata fields.
  // (we need this in case we're required to remove arguments fields from the input)
  public int outputRowOffset = -1;
  //
  public int[] inputFieldsToCopy;
  // the indices of the columns in the resultset
  public int[] resultSetIndices;

  public JdbcMetadataData() {
    super();
  }
}
