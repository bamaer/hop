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

import java.util.List;
import org.apache.hop.core.Const;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.graph.GraphDatabaseMeta;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.metadata.api.IHopMetadataProvider;
import org.apache.hop.ui.core.metadata.MetadataManager;
import org.apache.hop.ui.core.widget.MetaSelectionLine;
import org.eclipse.swt.widgets.Composite;

/**
 * Selects a connection for the Neo4j transforms and actions: a graph database connection of a Bolt
 * type or a Neo4j connection. New connections are graph database connections. Existing Neo4j
 * connections open in their own editor.
 */
public class NeoConnectionSelectionLine extends MetaSelectionLine<GraphDatabaseMeta> {

  public NeoConnectionSelectionLine(
      IVariables variables,
      IHopMetadataProvider metadataProvider,
      Composite parentComposite,
      int flags,
      String labelText,
      String toolTipText) {
    super(
        variables,
        metadataProvider,
        GraphDatabaseMeta.class,
        parentComposite,
        flags,
        labelText,
        toolTipText);
  }

  @Override
  public void fillItems() throws HopException {
    String previous = getText();
    if (getMetadataProvider() == null) {
      return;
    }
    List<String> names = NeoConnectionUtils.getConnectionNames(getMetadataProvider());
    setItems(names.toArray(new String[0]));
    setText(Const.NVL(previous, ""));
  }

  @Override
  protected boolean editMetadata() {
    String name = getText();
    try {
      if (isNeo4jConnection(name)) {
        MetadataManager<NeoConnection> neoManager =
            new MetadataManager<>(
                getVariables(), getMetadataProvider(), NeoConnection.class, getShell());
        return neoManager.editMetadata(name);
      }
    } catch (HopException e) {
      // Fall back to the graph database connection editor
    }
    return super.editMetadata();
  }

  private boolean isNeo4jConnection(String name) throws HopException {
    return getMetadataProvider().getSerializer(NeoConnection.class).exists(name)
        && !getMetadataProvider().getSerializer(GraphDatabaseMeta.class).exists(name);
  }
}
