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
 *
 */

package org.apache.hop.neo4j.execution.path.base;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.hop.core.Const;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.graph.GraphNodeValue;
import org.apache.hop.core.graph.GraphPathValue;
import org.apache.hop.core.graph.IGraphConnection;
import org.apache.hop.execution.Execution;
import org.apache.hop.execution.ExecutionInfoLocation;
import org.apache.hop.execution.ExecutionState;
import org.apache.hop.execution.ExecutionType;
import org.apache.hop.execution.IExecutionInfoLocation;
import org.apache.hop.neo4j.execution.NeoExecutionInfoLocation;
import org.apache.hop.neo4j.execution.path.PathResult;
import org.apache.hop.neo4j.logging.util.LoggingCore;
import org.apache.hop.neo4j.perspective.HopNeo4jPerspective;
import org.apache.hop.neo4j.shared.CypherDialect;
import org.apache.hop.ui.core.ConstUi;
import org.apache.hop.ui.core.PropsUi;
import org.apache.hop.ui.core.dialog.ErrorDialog;
import org.apache.hop.ui.hopgui.perspective.execution.ExecutionPerspective;
import org.apache.hop.ui.hopgui.shared.BaseExecutionViewer;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public abstract class NeoExecutionViewerTabBase {
  public static final Class<?> PKG = HopNeo4jPerspective.class;

  protected final BaseExecutionViewer viewer;
  protected final PropsUi props;
  protected final ClassLoader classLoader;
  protected final int iconSize;

  public NeoExecutionViewerTabBase(BaseExecutionViewer viewer) {
    this.viewer = viewer;
    this.props = PropsUi.getInstance();
    this.classLoader = this.getClass().getClassLoader();
    this.iconSize = ConstUi.SMALL_ICON_SIZE;
  }

  protected static ExecutionInfoLocation getExecutionInfoLocation(BaseExecutionViewer viewer) {
    String locationName = viewer.getLocationName();
    ExecutionPerspective perspective = viewer.getPerspective();
    return perspective.getLocationMap().get(locationName);
  }

  protected String getActiveLogChannelId() {
    return viewer.getActiveId();
  }

  protected IGraphConnection getConnection() {
    ExecutionInfoLocation location = getExecutionInfoLocation(viewer);
    return ((NeoExecutionInfoLocation) location.getExecutionInfoLocation()).getConnection();
  }

  protected CypherDialect getDialect() {
    IGraphConnection connection = getConnection();
    return connection == null ? CypherDialect.NEO4J : CypherDialect.fromId(connection.getDialect());
  }

  protected String getPathToRootCypher() {
    return buildPathToRootCypher(
        StringUtils.isNotEmpty(viewer.getExecution().getParentId()), getDialect());
  }

  /**
   * The paths of executions a statement returns as "p", each from its last execution to its first.
   */
  protected List<List<PathResult>> readPaths(String cypher, Map<String, Object> parameters)
      throws HopException {
    List<List<PathResult>> paths = new ArrayList<>();
    for (Map<String, Object> row :
        getConnection().executeRead(transaction -> transaction.execute(cypher, parameters))) {
      if (!(row.get("p") instanceof GraphPathValue path)) {
        continue;
      }
      List<PathResult> pathResults = new ArrayList<>();
      for (GraphNodeValue node : path.nodes()) {
        Map<String, Object> properties = node.properties();
        PathResult nodeResult = new PathResult();
        nodeResult.setId(LoggingCore.getStringValue(properties, "id"));
        nodeResult.setName(LoggingCore.getStringValue(properties, "name"));
        nodeResult.setType(LoggingCore.getStringValue(properties, "executionType"));
        nodeResult.setFailed(LoggingCore.getBooleanValue(properties, "failed"));
        nodeResult.setRegistrationDate(
            NeoExecutionInfoLocation.toDate(properties.get("registrationDate")));
        nodeResult.setCopy(LoggingCore.getStringValue(properties, "copyNr"));
        pathResults.add(0, nodeResult);
      }
      paths.add(pathResults);
    }
    return paths;
  }

  /**
   * Cypher that walks from a child execution to the root parent using directed EXECUTES
   * relationships. The cartesian {@code MATCH (top:Execution)} form is avoided because it does not
   * scale on a busy logging graph and is a common timeout on Neo4j 5.
   */
  public static String buildPathToRootCypher(boolean hasParent) {
    return buildPathToRootCypher(hasParent, CypherDialect.NEO4J);
  }

  /**
   * The path to the root in the dialect of the database. Without Neo4j's shortestPath(): the
   * executions form a tree, so there is only one path down from the root.
   */
  public static String buildPathToRootCypher(boolean hasParent, CypherDialect dialect) {
    if (hasParent && dialect != CypherDialect.NEO4J) {
      return "MATCH p = (top:Execution)-[:EXECUTES*]->(child:Execution {id: $executionId }) "
          + Const.CR
          + "WHERE top.parentId IS NULL "
          + Const.CR
          + "RETURN p "
          + Const.CR
          + "LIMIT 10 "
          + Const.CR;
    }
    if (!hasParent) {
      return "MATCH(e:Execution {id: $executionId }) " + Const.CR + "RETURN e " + Const.CR;
    }
    return "MATCH (child:Execution {id: $executionId }) "
        + Const.CR
        + "MATCH p = shortestPath((top:Execution)-[:EXECUTES*]->(child)) "
        + Const.CR
        + "WHERE top.parentId IS NULL "
        + Const.CR
        + "RETURN p "
        + Const.CR
        + "ORDER BY size(RELATIONSHIPS(p)) DESC "
        + Const.CR
        + "LIMIT 10 "
        + Const.CR;
  }

  protected String getPathToFailedCypher() {
    return buildPathToFailedCypher(getDialect());
  }

  /** The paths to failed leaf executions in the dialect of the database. */
  public static String buildPathToFailedCypher(CypherDialect dialect) {
    if (dialect == CypherDialect.NEO4J) {
      return buildPathToFailedCypher();
    }
    return "MATCH p = (top:Execution {id: $executionId })-[:EXECUTES*]->(child:Execution) "
        + Const.CR
        + "WHERE child.failed = true "
        + Const.CR
        + "AND   child.id <> $executionId "
        + Const.CR
        + "OPTIONAL MATCH (child)-[grandChild:EXECUTES]->() "
        + Const.CR
        + "WITH p, count(grandChild) AS grandChildren "
        + Const.CR
        + "WHERE grandChildren = 0 "
        + Const.CR
        + "RETURN p "
        + Const.CR
        + "ORDER BY length(p) "
        + Const.CR
        + "LIMIT 10 "
        + Const.CR;
  }

  /**
   * Cypher that walks from the current execution to failed leaf executions. The leaf predicate uses
   * a pattern predicate rather than {@code size((n)-[:EXECUTES]->())}, which Neo4j 5 removed.
   */
  public static String buildPathToFailedCypher() {
    return "MATCH (top:Execution {id: $executionId }) "
        + Const.CR
        + "MATCH p = shortestPath((top)-[:EXECUTES*]->(child:Execution)) "
        + Const.CR
        + "WHERE child.failed = true "
        + Const.CR
        + "AND   child.id <> $executionId "
        + Const.CR
        + "AND   NOT (child)-[:EXECUTES]->() "
        + Const.CR
        + "RETURN p "
        + Const.CR
        + "ORDER BY size(RELATIONSHIPS(p)) "
        + Const.CR
        + "LIMIT 10 "
        + Const.CR;
  }

  public void openItem(Tree tree) {
    if (tree.getSelectionCount() <= 0) {
      return;
    }
    TreeItem treeItem = tree.getSelection()[0];

    String childId = null;
    String id = treeItem.getText(1);
    String name = treeItem.getText(2);
    String type = treeItem.getText(3);

    ExecutionInfoLocation location = getExecutionInfoLocation(viewer);
    IExecutionInfoLocation iLocation = location.getExecutionInfoLocation();

    try {

      if (ExecutionType.Transform.name().equals(type)) {
        // Find the parent of this execution
        Execution transformExecution = iLocation.getExecution(id);
        childId = id;
        id = transformExecution.getParentId();
      } else if (ExecutionType.Action.name().equals(type)) {
        // Find the parent of this execution
        Execution actionExecution = iLocation.getExecution(id);
        childId = id;
        id = actionExecution.getParentId();
      }
      // Get the state
      //
      Execution execution = iLocation.getExecution(id);
      ExecutionState executionState = iLocation.getExecutionState(id);

      // Open the execution in a new viewer
      //
      viewer.getPerspective().createExecutionViewer(location.getName(), execution, executionState);
    } catch (Exception e) {
      new ErrorDialog(viewer.getShell(), "Error", "Error opening lineage item", e);
    }
  }

  /**
   * Gets viewer
   *
   * @return value of viewer
   */
  public BaseExecutionViewer getViewer() {
    return viewer;
  }

  /**
   * Gets props
   *
   * @return value of props
   */
  public PropsUi getProps() {
    return props;
  }

  /**
   * Gets classLoader
   *
   * @return value of classLoader
   */
  public ClassLoader getClassLoader() {
    return classLoader;
  }

  /**
   * Gets iconSize
   *
   * @return value of iconSize
   */
  public int getIconSize() {
    return iconSize;
  }
}
