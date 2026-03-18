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

package org.apache.hop.projects.gui;

import java.io.File;
import java.nio.charset.StandardCharsets;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.hop.core.Const;
import org.apache.hop.core.config.HopConfig;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.util.Utils;
import org.apache.hop.core.variables.DescribedVariable;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.core.variables.Variables;
import org.apache.hop.core.vfs.HopVfs;
import org.apache.hop.core.xml.XmlHandler;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.metadata.api.IHopMetadataSerializer;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.config.PipelineRunConfiguration;
import org.apache.hop.pipeline.engines.local.LocalPipelineRunConfiguration;
import org.apache.hop.projects.config.ProjectsConfig;
import org.apache.hop.projects.config.ProjectsConfigSingleton;
import org.apache.hop.projects.project.Project;
import org.apache.hop.projects.project.ProjectConfig;
import org.apache.hop.projects.util.ProjectsUtil;
import org.apache.hop.ui.core.ConstUi;
import org.apache.hop.ui.core.PropsUi;
import org.apache.hop.ui.core.dialog.BaseDialog;
import org.apache.hop.ui.core.dialog.ErrorDialog;
import org.apache.hop.ui.core.gui.GuiResource;
import org.apache.hop.ui.core.gui.WindowProperty;
import org.apache.hop.ui.core.widget.ColumnInfo;
import org.apache.hop.ui.core.widget.ComboVar;
import org.apache.hop.ui.core.widget.TableView;
import org.apache.hop.ui.core.widget.TextVar;
import org.apache.hop.ui.hopgui.HopGui;
import org.apache.hop.ui.pipeline.dialog.PipelineExecutionConfigurationDialog;
import org.apache.hop.ui.pipeline.transform.BaseTransformDialog;
import org.apache.hop.workflow.WorkflowMeta;
import org.apache.hop.workflow.config.WorkflowRunConfiguration;
import org.apache.hop.workflow.engines.local.LocalWorkflowRunConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class EmptyProjectDialog extends Dialog {
  private static final Class<?> PKG = EmptyProjectDialog.class;

  private String returnValue;
  private Shell shell;
  private final PropsUi props;
  private final IVariables variables;
  private final Runnable addEnvironmentCallback;

  // Project identification
  private Text wName;
  private TextVar wHome;
  private TextVar wConfigFile;
  private ComboVar wParentProject;

  // Project metadata
  private Text wDescription;
  private Text wCompany;
  private Text wDepartment;

  // Project paths
  private TextVar wMetadataBaseFolder;
  private TextVar wUnitTestsBasePath;
  private TextVar wDataSetCsvFolder;

  // Behaviour
  private Button wEnforceHomeExecution;

  // Variables table
  private TableView wVariables;

  // Scaffold options
  private Button wCreatePipeline;
  private Text wPipelineName;
  private Button wCreateWorkflow;
  private Text wWorkflowName;
  private Button wCreateRunConfigs;
  private Button wCreateEnvironment;

  public EmptyProjectDialog(Shell parent, IVariables variables) {
    this(parent, variables, null);
  }

  public EmptyProjectDialog(Shell parent, IVariables variables, Runnable addEnvironmentCallback) {
    super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);
    this.variables = new Variables();
    this.variables.initializeFrom(variables);
    this.addEnvironmentCallback = addEnvironmentCallback;
    props = PropsUi.getInstance();
  }

  public String open() {
    Shell parent = getParent();
    shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);
    shell.setImage(
        GuiResource.getInstance()
            .getImage(
                "project.svg",
                PKG.getClassLoader(),
                ConstUi.SMALL_ICON_SIZE,
                ConstUi.SMALL_ICON_SIZE));

    PropsUi.setLook(shell);

    int margin = PropsUi.getMargin() + 2;
    int middle = props.getMiddlePct();

    shell.setLayout(new FormLayout());
    shell.setText(BaseMessages.getString(PKG, "EmptyProjectDialog.Shell.Name"));

    Button wOk = new Button(shell, SWT.PUSH);
    wOk.setText(BaseMessages.getString(PKG, "System.Button.OK"));
    wOk.addListener(SWT.Selection, event -> ok());
    Button wCancel = new Button(shell, SWT.PUSH);
    wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));
    wCancel.addListener(SWT.Selection, event -> cancel());
    BaseTransformDialog.positionBottomButtons(shell, new Button[] {wOk, wCancel}, margin * 3, null);

    // Scrollable area so the dialog can grow
    ScrolledComposite scroll = new ScrolledComposite(shell, SWT.V_SCROLL);
    scroll.setLayout(new FillLayout());
    scroll.setExpandHorizontal(true);
    scroll.setExpandVertical(true);
    PropsUi.setLook(scroll);
    FormData fdScroll = new FormData();
    fdScroll.left = new FormAttachment(0, 0);
    fdScroll.right = new FormAttachment(100, 0);
    fdScroll.top = new FormAttachment(0, 0);
    fdScroll.bottom = new FormAttachment(wOk, -margin);
    scroll.setLayoutData(fdScroll);

    Composite comp = new Composite(scroll, SWT.NONE);
    comp.setLayout(new FormLayout());
    PropsUi.setLook(comp);

    Control lastControl = null;

    // ── Project name ──────────────────────────────────────────────────────────
    Label wlName = new Label(comp, SWT.RIGHT);
    wlName.setText(BaseMessages.getString(PKG, "EmptyProjectDialog.Label.ProjectName"));
    PropsUi.setLook(wlName);
    FormData fdlName = new FormData();
    fdlName.left = new FormAttachment(0, 0);
    fdlName.right = new FormAttachment(middle, 0);
    fdlName.top = new FormAttachment(0, margin * 2);
    wlName.setLayoutData(fdlName);
    wName = new Text(comp, SWT.SINGLE | SWT.BORDER | SWT.LEFT);
    PropsUi.setLook(wName);
    FormData fdName = new FormData();
    fdName.left = new FormAttachment(middle, margin);
    fdName.right = new FormAttachment(99, 0);
    fdName.top = new FormAttachment(wlName, 0, SWT.CENTER);
    wName.setLayoutData(fdName);
    lastControl = wName;

    // ── Home folder ───────────────────────────────────────────────────────────
    Label wlHome = new Label(comp, SWT.RIGHT);
    wlHome.setText(BaseMessages.getString(PKG, "EmptyProjectDialog.Label.HomeFolder"));
    PropsUi.setLook(wlHome);
    FormData fdlHome = new FormData();
    fdlHome.left = new FormAttachment(0, 0);
    fdlHome.right = new FormAttachment(middle, 0);
    fdlHome.top = new FormAttachment(lastControl, margin);
    wlHome.setLayoutData(fdlHome);
    Button wbHome = new Button(comp, SWT.PUSH);
    wbHome.setText(BaseMessages.getString(PKG, "EmptyProjectDialog.Button.Browse"));
    PropsUi.setLook(wbHome);
    FormData fdbHome = new FormData();
    fdbHome.right = new FormAttachment(99, 0);
    fdbHome.top = new FormAttachment(wlHome, 0, SWT.CENTER);
    wbHome.setLayoutData(fdbHome);
    wbHome.addListener(SWT.Selection, e -> browseHome());
    wHome = new TextVar(variables, comp, SWT.SINGLE | SWT.BORDER | SWT.LEFT);
    PropsUi.setLook(wHome);
    FormData fdHome = new FormData();
    fdHome.left = new FormAttachment(middle, margin);
    fdHome.right = new FormAttachment(wbHome, -margin);
    fdHome.top = new FormAttachment(wlHome, 0, SWT.CENTER);
    wHome.setLayoutData(fdHome);
    lastControl = wHome;

    // ── Configuration file ────────────────────────────────────────────────────
    Label wlConfigFile = new Label(comp, SWT.RIGHT);
    wlConfigFile.setText(BaseMessages.getString(PKG, "EmptyProjectDialog.Label.ConfigFile"));
    PropsUi.setLook(wlConfigFile);
    FormData fdlConfigFile = new FormData();
    fdlConfigFile.left = new FormAttachment(0, 0);
    fdlConfigFile.right = new FormAttachment(middle, 0);
    fdlConfigFile.top = new FormAttachment(lastControl, margin);
    wlConfigFile.setLayoutData(fdlConfigFile);
    Button wbConfigFile = new Button(comp, SWT.PUSH);
    wbConfigFile.setText(BaseMessages.getString(PKG, "EmptyProjectDialog.Button.Browse"));
    PropsUi.setLook(wbConfigFile);
    FormData fdbConfigFile = new FormData();
    fdbConfigFile.right = new FormAttachment(99, 0);
    fdbConfigFile.top = new FormAttachment(wlConfigFile, 0, SWT.CENTER);
    wbConfigFile.setLayoutData(fdbConfigFile);
    wbConfigFile.addListener(SWT.Selection, e -> browseConfigFile());
    wConfigFile = new TextVar(variables, comp, SWT.SINGLE | SWT.BORDER | SWT.LEFT);
    PropsUi.setLook(wConfigFile);
    wConfigFile.setToolTipText(
        BaseMessages.getString(PKG, "EmptyProjectDialog.Label.ConfigFile.Tooltip"));
    FormData fdConfigFile = new FormData();
    fdConfigFile.left = new FormAttachment(middle, margin);
    fdConfigFile.right = new FormAttachment(wbConfigFile, -margin);
    fdConfigFile.top = new FormAttachment(wlConfigFile, 0, SWT.CENTER);
    wConfigFile.setLayoutData(fdConfigFile);
    lastControl = wConfigFile;

    // ── Parent project ────────────────────────────────────────────────────────
    Label wlParentProject = new Label(comp, SWT.RIGHT);
    wlParentProject.setText(BaseMessages.getString(PKG, "EmptyProjectDialog.Label.ParentProject"));
    PropsUi.setLook(wlParentProject);
    FormData fdlParentProject = new FormData();
    fdlParentProject.left = new FormAttachment(0, 0);
    fdlParentProject.right = new FormAttachment(middle, 0);
    fdlParentProject.top = new FormAttachment(lastControl, margin);
    wlParentProject.setLayoutData(fdlParentProject);
    wParentProject = new ComboVar(variables, comp, SWT.SINGLE | SWT.BORDER | SWT.LEFT);
    PropsUi.setLook(wParentProject);
    FormData fdParentProject = new FormData();
    fdParentProject.left = new FormAttachment(middle, margin);
    fdParentProject.right = new FormAttachment(99, 0);
    fdParentProject.top = new FormAttachment(wlParentProject, 0, SWT.CENTER);
    wParentProject.setLayoutData(fdParentProject);
    lastControl = wParentProject;

    // ── Description ───────────────────────────────────────────────────────────
    Label wlDescription = new Label(comp, SWT.RIGHT);
    wlDescription.setText(BaseMessages.getString(PKG, "EmptyProjectDialog.Label.Description"));
    PropsUi.setLook(wlDescription);
    FormData fdlDescription = new FormData();
    fdlDescription.left = new FormAttachment(0, 0);
    fdlDescription.right = new FormAttachment(middle, 0);
    fdlDescription.top = new FormAttachment(lastControl, margin);
    wlDescription.setLayoutData(fdlDescription);
    wDescription = new Text(comp, SWT.SINGLE | SWT.BORDER | SWT.LEFT);
    PropsUi.setLook(wDescription);
    FormData fdDescription = new FormData();
    fdDescription.left = new FormAttachment(middle, margin);
    fdDescription.right = new FormAttachment(99, 0);
    fdDescription.top = new FormAttachment(wlDescription, 0, SWT.CENTER);
    wDescription.setLayoutData(fdDescription);
    lastControl = wDescription;

    // ── Company ───────────────────────────────────────────────────────────────
    Label wlCompany = new Label(comp, SWT.RIGHT);
    wlCompany.setText(BaseMessages.getString(PKG, "EmptyProjectDialog.Label.Company"));
    PropsUi.setLook(wlCompany);
    FormData fdlCompany = new FormData();
    fdlCompany.left = new FormAttachment(0, 0);
    fdlCompany.right = new FormAttachment(middle, 0);
    fdlCompany.top = new FormAttachment(lastControl, margin);
    wlCompany.setLayoutData(fdlCompany);
    wCompany = new Text(comp, SWT.SINGLE | SWT.BORDER | SWT.LEFT);
    PropsUi.setLook(wCompany);
    FormData fdCompany = new FormData();
    fdCompany.left = new FormAttachment(middle, margin);
    fdCompany.right = new FormAttachment(99, 0);
    fdCompany.top = new FormAttachment(wlCompany, 0, SWT.CENTER);
    wCompany.setLayoutData(fdCompany);
    lastControl = wCompany;

    // ── Department ────────────────────────────────────────────────────────────
    Label wlDepartment = new Label(comp, SWT.RIGHT);
    wlDepartment.setText(BaseMessages.getString(PKG, "EmptyProjectDialog.Label.Department"));
    PropsUi.setLook(wlDepartment);
    FormData fdlDepartment = new FormData();
    fdlDepartment.left = new FormAttachment(0, 0);
    fdlDepartment.right = new FormAttachment(middle, 0);
    fdlDepartment.top = new FormAttachment(lastControl, margin);
    wlDepartment.setLayoutData(fdlDepartment);
    wDepartment = new Text(comp, SWT.SINGLE | SWT.BORDER | SWT.LEFT);
    PropsUi.setLook(wDepartment);
    FormData fdDepartment = new FormData();
    fdDepartment.left = new FormAttachment(middle, margin);
    fdDepartment.right = new FormAttachment(99, 0);
    fdDepartment.top = new FormAttachment(wlDepartment, 0, SWT.CENTER);
    wDepartment.setLayoutData(fdDepartment);
    lastControl = wDepartment;

    // ── Metadata base folder ──────────────────────────────────────────────────
    Label wlMetadataBaseFolder = new Label(comp, SWT.RIGHT);
    wlMetadataBaseFolder.setText(
        BaseMessages.getString(PKG, "EmptyProjectDialog.Label.MetadataBaseFolder"));
    PropsUi.setLook(wlMetadataBaseFolder);
    FormData fdlMetadata = new FormData();
    fdlMetadata.left = new FormAttachment(0, 0);
    fdlMetadata.right = new FormAttachment(middle, 0);
    fdlMetadata.top = new FormAttachment(lastControl, margin);
    wlMetadataBaseFolder.setLayoutData(fdlMetadata);
    wMetadataBaseFolder = new TextVar(variables, comp, SWT.SINGLE | SWT.BORDER | SWT.LEFT);
    PropsUi.setLook(wMetadataBaseFolder);
    FormData fdMetadata = new FormData();
    fdMetadata.left = new FormAttachment(middle, margin);
    fdMetadata.right = new FormAttachment(99, 0);
    fdMetadata.top = new FormAttachment(wlMetadataBaseFolder, 0, SWT.CENTER);
    wMetadataBaseFolder.setLayoutData(fdMetadata);
    lastControl = wMetadataBaseFolder;

    // ── Unit tests base path ──────────────────────────────────────────────────
    Label wlUnitTestsBasePath = new Label(comp, SWT.RIGHT);
    wlUnitTestsBasePath.setText(
        BaseMessages.getString(PKG, "EmptyProjectDialog.Label.UnitTestsBasePath"));
    PropsUi.setLook(wlUnitTestsBasePath);
    FormData fdlUnitTests = new FormData();
    fdlUnitTests.left = new FormAttachment(0, 0);
    fdlUnitTests.right = new FormAttachment(middle, 0);
    fdlUnitTests.top = new FormAttachment(lastControl, margin);
    wlUnitTestsBasePath.setLayoutData(fdlUnitTests);
    wUnitTestsBasePath = new TextVar(variables, comp, SWT.SINGLE | SWT.BORDER | SWT.LEFT);
    PropsUi.setLook(wUnitTestsBasePath);
    FormData fdUnitTests = new FormData();
    fdUnitTests.left = new FormAttachment(middle, margin);
    fdUnitTests.right = new FormAttachment(99, 0);
    fdUnitTests.top = new FormAttachment(wlUnitTestsBasePath, 0, SWT.CENTER);
    wUnitTestsBasePath.setLayoutData(fdUnitTests);
    lastControl = wUnitTestsBasePath;

    // ── Data set CSV folder ───────────────────────────────────────────────────
    Label wlDataSetCsvFolder = new Label(comp, SWT.RIGHT);
    wlDataSetCsvFolder.setText(
        BaseMessages.getString(PKG, "EmptyProjectDialog.Label.DataSetCsvFolder"));
    PropsUi.setLook(wlDataSetCsvFolder);
    FormData fdlDataSet = new FormData();
    fdlDataSet.left = new FormAttachment(0, 0);
    fdlDataSet.right = new FormAttachment(middle, 0);
    fdlDataSet.top = new FormAttachment(lastControl, margin);
    wlDataSetCsvFolder.setLayoutData(fdlDataSet);
    wDataSetCsvFolder = new TextVar(variables, comp, SWT.SINGLE | SWT.BORDER | SWT.LEFT);
    PropsUi.setLook(wDataSetCsvFolder);
    FormData fdDataSet = new FormData();
    fdDataSet.left = new FormAttachment(middle, margin);
    fdDataSet.right = new FormAttachment(99, 0);
    fdDataSet.top = new FormAttachment(wlDataSetCsvFolder, 0, SWT.CENTER);
    wDataSetCsvFolder.setLayoutData(fdDataSet);
    lastControl = wDataSetCsvFolder;

    // ── Enforce home execution ────────────────────────────────────────────────
    Label wlEnforceHome = new Label(comp, SWT.RIGHT);
    wlEnforceHome.setText(
        BaseMessages.getString(PKG, "EmptyProjectDialog.Label.EnforceHomeExecution"));
    PropsUi.setLook(wlEnforceHome);
    FormData fdlEnforceHome = new FormData();
    fdlEnforceHome.left = new FormAttachment(0, 0);
    fdlEnforceHome.right = new FormAttachment(middle, 0);
    fdlEnforceHome.top = new FormAttachment(lastControl, margin);
    wlEnforceHome.setLayoutData(fdlEnforceHome);
    wEnforceHomeExecution = new Button(comp, SWT.CHECK | SWT.LEFT);
    PropsUi.setLook(wEnforceHomeExecution);
    FormData fdEnforceHome = new FormData();
    fdEnforceHome.left = new FormAttachment(middle, margin);
    fdEnforceHome.right = new FormAttachment(99, 0);
    fdEnforceHome.top = new FormAttachment(wlEnforceHome, 0, SWT.CENTER);
    wEnforceHomeExecution.setLayoutData(fdEnforceHome);
    lastControl = wlEnforceHome;

    // ── Project variables ─────────────────────────────────────────────────────
    Label wlVariables = new Label(comp, SWT.LEFT);
    wlVariables.setText(BaseMessages.getString(PKG, "EmptyProjectDialog.Label.ProjectVariables"));
    PropsUi.setLook(wlVariables);
    FormData fdlVariables = new FormData();
    fdlVariables.left = new FormAttachment(1, 0);
    fdlVariables.right = new FormAttachment(99, 0);
    fdlVariables.top = new FormAttachment(lastControl, margin * 2);
    wlVariables.setLayoutData(fdlVariables);

    ColumnInfo[] columnInfo =
        new ColumnInfo[] {
          new ColumnInfo(
              BaseMessages.getString(PKG, "EmptyProjectDialog.Table.Col.VariableName"),
              ColumnInfo.COLUMN_TYPE_TEXT,
              false,
              false),
          new ColumnInfo(
              BaseMessages.getString(PKG, "EmptyProjectDialog.Table.Col.VariableValue"),
              ColumnInfo.COLUMN_TYPE_TEXT,
              false,
              false),
          new ColumnInfo(
              BaseMessages.getString(PKG, "EmptyProjectDialog.Table.Col.VariableDescription"),
              ColumnInfo.COLUMN_TYPE_TEXT,
              false,
              false),
        };
    columnInfo[0].setUsingVariables(true);
    columnInfo[1].setUsingVariables(true);

    wVariables = new TableView(new Variables(), comp, SWT.BORDER, columnInfo, 3, null, props);
    PropsUi.setLook(wVariables);
    FormData fdVariables = new FormData();
    fdVariables.left = new FormAttachment(1, 0);
    fdVariables.right = new FormAttachment(99, 0);
    fdVariables.top = new FormAttachment(wlVariables, margin);
    fdVariables.height = 120;
    wVariables.setLayoutData(fdVariables);
    lastControl = wVariables;

    // ── Separator ─────────────────────────────────────────────────────────────
    Label wlSeparator = new Label(comp, SWT.SEPARATOR | SWT.HORIZONTAL);
    FormData fdlSeparator = new FormData();
    fdlSeparator.left = new FormAttachment(0, 0);
    fdlSeparator.right = new FormAttachment(100, 0);
    fdlSeparator.top = new FormAttachment(lastControl, margin * 2);
    wlSeparator.setLayoutData(fdlSeparator);

    Label wlScaffold = new Label(comp, SWT.LEFT);
    wlScaffold.setText(BaseMessages.getString(PKG, "EmptyProjectDialog.Label.ScaffoldOptions"));
    PropsUi.setLook(wlScaffold);
    FormData fdlScaffold = new FormData();
    fdlScaffold.left = new FormAttachment(1, 0);
    fdlScaffold.right = new FormAttachment(99, 0);
    fdlScaffold.top = new FormAttachment(wlSeparator, margin);
    wlScaffold.setLayoutData(fdlScaffold);
    lastControl = wlScaffold;

    // ── Create pipeline ───────────────────────────────────────────────────────
    wCreatePipeline = new Button(comp, SWT.CHECK);
    wCreatePipeline.setText(BaseMessages.getString(PKG, "EmptyProjectDialog.Label.CreatePipeline"));
    PropsUi.setLook(wCreatePipeline);
    FormData fdCreatePipeline = new FormData();
    fdCreatePipeline.left = new FormAttachment(0, 0);
    fdCreatePipeline.right = new FormAttachment(99, 0);
    fdCreatePipeline.top = new FormAttachment(lastControl, margin * 2);
    wCreatePipeline.setLayoutData(fdCreatePipeline);
    wCreatePipeline.addListener(SWT.Selection, e -> updatePipelineNameState());
    lastControl = wCreatePipeline;

    wPipelineName = new Text(comp, SWT.SINGLE | SWT.BORDER | SWT.LEFT);
    PropsUi.setLook(wPipelineName);
    FormData fdPipelineName = new FormData();
    fdPipelineName.left = new FormAttachment(middle, margin);
    fdPipelineName.right = new FormAttachment(99, 0);
    fdPipelineName.top = new FormAttachment(lastControl, margin);
    wPipelineName.setLayoutData(fdPipelineName);
    lastControl = wPipelineName;

    // ── Create workflow ───────────────────────────────────────────────────────
    wCreateWorkflow = new Button(comp, SWT.CHECK);
    wCreateWorkflow.setText(BaseMessages.getString(PKG, "EmptyProjectDialog.Label.CreateWorkflow"));
    PropsUi.setLook(wCreateWorkflow);
    FormData fdCreateWorkflow = new FormData();
    fdCreateWorkflow.left = new FormAttachment(0, 0);
    fdCreateWorkflow.right = new FormAttachment(99, 0);
    fdCreateWorkflow.top = new FormAttachment(lastControl, margin * 2);
    wCreateWorkflow.setLayoutData(fdCreateWorkflow);
    wCreateWorkflow.addListener(SWT.Selection, e -> updateWorkflowNameState());
    lastControl = wCreateWorkflow;

    wWorkflowName = new Text(comp, SWT.SINGLE | SWT.BORDER | SWT.LEFT);
    PropsUi.setLook(wWorkflowName);
    FormData fdWorkflowName = new FormData();
    fdWorkflowName.left = new FormAttachment(middle, margin);
    fdWorkflowName.right = new FormAttachment(99, 0);
    fdWorkflowName.top = new FormAttachment(lastControl, margin);
    wWorkflowName.setLayoutData(fdWorkflowName);
    lastControl = wWorkflowName;

    // ── Create run configs ────────────────────────────────────────────────────
    wCreateRunConfigs = new Button(comp, SWT.CHECK);
    wCreateRunConfigs.setText(
        BaseMessages.getString(PKG, "EmptyProjectDialog.Label.CreateRunConfigs"));
    PropsUi.setLook(wCreateRunConfigs);
    FormData fdCreateRunConfigs = new FormData();
    fdCreateRunConfigs.left = new FormAttachment(0, 0);
    fdCreateRunConfigs.right = new FormAttachment(99, 0);
    fdCreateRunConfigs.top = new FormAttachment(lastControl, margin * 2);
    wCreateRunConfigs.setLayoutData(fdCreateRunConfigs);
    lastControl = wCreateRunConfigs;

    // ── Create environment ────────────────────────────────────────────────────
    wCreateEnvironment = new Button(comp, SWT.CHECK);
    wCreateEnvironment.setText(
        BaseMessages.getString(PKG, "EmptyProjectDialog.Label.CreateEnvironment"));
    PropsUi.setLook(wCreateEnvironment);
    FormData fdCreateEnvironment = new FormData();
    fdCreateEnvironment.left = new FormAttachment(0, 0);
    fdCreateEnvironment.right = new FormAttachment(99, 0);
    fdCreateEnvironment.top = new FormAttachment(lastControl, margin * 2);
    wCreateEnvironment.setLayoutData(fdCreateEnvironment);

    getData();
    updatePipelineNameState();
    updateWorkflowNameState();

    comp.pack();
    scroll.setContent(comp);
    scroll.setMinSize(comp.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    shell.setMinimumSize(600, 300);
    shell.setDefaultButton(wOk);
    wName.setFocus();
    BaseDialog.defaultShellHandling(shell, c -> ok(), c -> cancel());

    return returnValue;
  }

  // ---------------------------------------------------------------------------
  // UI state helpers
  // ---------------------------------------------------------------------------

  private void updatePipelineNameState() {
    wPipelineName.setEnabled(wCreatePipeline.getSelection());
  }

  private void updateWorkflowNameState() {
    wWorkflowName.setEnabled(wCreateWorkflow.getSelection());
  }

  private void browseHome() {
    String path = BaseDialog.presentDirectoryDialog(shell, wHome, variables);
    if (path != null && StringUtils.isEmpty(wName.getText())) {
      try {
        FileObject file = HopVfs.getFileObject(path);
        wName.setText(Const.NVL(file.getName().getBaseName(), ""));
      } catch (Exception e) {
        // ignore — name suggestion is best-effort
      }
    }
  }

  private void browseConfigFile() {
    String homeFolder = variables.resolve(wHome.getText());
    File configFile =
        new File(
            homeFolder
                + File.separator
                + "config"
                + File.separator
                + ProjectsConfig.DEFAULT_PROJECT_CONFIG_FILENAME);
    wConfigFile.setText(homeFolder);
    if (configFile.exists()) {
      BaseDialog.presentFileDialog(
          shell,
          wConfigFile,
          variables,
          new String[] {"*.json", "*.*"},
          new String[] {"Project config (*.json)", "All files (*.*)"},
          true);
    } else {
      String dir = BaseDialog.presentDirectoryDialog(shell, wConfigFile, variables);
      if (dir != null) {
        wConfigFile.setText(dir + File.separator + ProjectsConfig.DEFAULT_PROJECT_CONFIG_FILENAME);
      }
    }
  }

  // ---------------------------------------------------------------------------
  // Data in / out
  // ---------------------------------------------------------------------------

  private void getData() {
    ProjectsConfig config = ProjectsConfigSingleton.getConfig();
    String standardFolder = variables.resolve(config.getStandardProjectsFolder());
    wHome.setText(Const.NVL(standardFolder, ""));
    wConfigFile.setText(Const.NVL(variables.resolve(config.getDefaultProjectConfigFile()), ""));

    // Populate parent project combo
    try {
      java.util.List<String> names = config.listProjectConfigNames();
      wParentProject.setItems(names.toArray(new String[0]));
      String defaultParent = config.getStandardParentProject();
      wParentProject.setText(Const.NVL(defaultParent, ""));
    } catch (Exception e) {
      // best-effort
    }

    wPipelineName.setText(BaseMessages.getString(PKG, "EmptyProjectDialog.Default.PipelineName"));
    wWorkflowName.setText(BaseMessages.getString(PKG, "EmptyProjectDialog.Default.WorkflowName"));
    wCreatePipeline.setSelection(true);
    wCreateWorkflow.setSelection(true);
    wCreateRunConfigs.setSelection(true);
    wCreateEnvironment.setSelection(false);
  }

  // ---------------------------------------------------------------------------
  // OK / Cancel
  // ---------------------------------------------------------------------------

  private void ok() {
    try {
      String projectName = wName.getText().trim();
      if (StringUtils.isEmpty(projectName)) {
        throw new HopException("Please specify a project name");
      }
      String homeFolder = variables.resolve(wHome.getText());
      if (StringUtils.isEmpty(homeFolder)) {
        throw new HopException("Please specify a home folder");
      }

      ProjectsConfig config = ProjectsConfigSingleton.getConfig();
      if (config.findProjectConfig(projectName) != null) {
        throw new HopException("Project '" + projectName + "' already exists");
      }

      // Validate parent project if specified
      String parentProjectName = wParentProject.getText().trim();
      if (!Utils.isEmpty(parentProjectName)) {
        if (projectName.equals(parentProjectName)) {
          throw new HopException(
              "Project '" + projectName + "' cannot be set as a parent project of itself");
        }
        if (!ProjectsUtil.projectExists(parentProjectName)) {
          throw new HopException("Parent project '" + parentProjectName + "' does not exist");
        }
      }

      // Config file — fall back to system default if empty
      String configFilename = wConfigFile.getText().trim();
      if (StringUtils.isEmpty(configFilename)) {
        configFilename = variables.resolve(config.getDefaultProjectConfigFile());
      }

      ProjectConfig projectConfig = new ProjectConfig(projectName, homeFolder, configFilename);
      Project project = new Project();

      // Set all configuration fields
      project.setParentProjectName(
          StringUtils.isEmpty(parentProjectName) ? null : parentProjectName);
      project.setDescription(wDescription.getText());
      project.setCompany(wCompany.getText());
      project.setDepartment(wDepartment.getText());
      project.setMetadataBaseFolder(wMetadataBaseFolder.getText());
      project.setUnitTestsBasePath(wUnitTestsBasePath.getText());
      project.setDataSetsCsvFolder(wDataSetCsvFolder.getText());
      project.setEnforcingExecutionInHome(wEnforceHomeExecution.getSelection());

      // Variables
      for (int i = 0; i < wVariables.nrNonEmpty(); i++) {
        TableItem item = wVariables.getNonEmpty(i);
        project
            .getDescribedVariables()
            .add(new DescribedVariable(item.getText(1), item.getText(2), item.getText(3)));
      }

      // Create project folder and save config
      FileObject homeDir = HopVfs.getFileObject(homeFolder);
      if (!homeDir.exists()) {
        homeDir.createFolder();
      }

      project.setConfigFilename(projectConfig.getActualProjectConfigFilename(variables));
      project.saveToFile();

      config.addProjectConfig(projectConfig);
      HopConfig.getInstance().saveToFile();

      HopGui hopGui = HopGui.getInstance();
      ProjectsGuiPlugin.updateProjectToolItem(projectName);
      ProjectsGuiPlugin.enableHopGuiProject(projectName, project, null);

      // ── Scaffold: pipeline ─────────────────────────────────────────────────
      if (wCreatePipeline.getSelection()) {
        String pipelineName = wPipelineName.getText();
        if (StringUtils.isEmpty(pipelineName)) {
          pipelineName = BaseMessages.getString(PKG, "EmptyProjectDialog.Default.PipelineName");
        }
        if (!pipelineName.toLowerCase().endsWith(".hpl")) {
          pipelineName += ".hpl";
        }
        String pipelinePath = homeFolder + File.separator + "pipelines";
        FileObject pipelineDir = HopVfs.getFileObject(pipelinePath);
        if (!pipelineDir.exists()) {
          pipelineDir.createFolder();
        }
        String pipelineFile = pipelinePath + File.separator + pipelineName;
        PipelineMeta pipelineMeta = new PipelineMeta();
        pipelineMeta.setName(pipelineName);
        pipelineMeta.setFilename(pipelineFile);
        pipelineMeta.setMetadataProvider(hopGui.getMetadataProvider());
        String xml = pipelineMeta.getXml(variables);
        try (java.io.OutputStream out = HopVfs.getOutputStream(pipelineFile, false)) {
          out.write(XmlHandler.getXmlHeader(Const.XML_ENCODING).getBytes(StandardCharsets.UTF_8));
          out.write(xml.getBytes(StandardCharsets.UTF_8));
        }
      }

      // ── Scaffold: workflow ─────────────────────────────────────────────────
      if (wCreateWorkflow.getSelection()) {
        String workflowName = wWorkflowName.getText();
        if (StringUtils.isEmpty(workflowName)) {
          workflowName = BaseMessages.getString(PKG, "EmptyProjectDialog.Default.WorkflowName");
        }
        if (!workflowName.toLowerCase().endsWith(".hwf")) {
          workflowName += ".hwf";
        }
        String workflowPath = homeFolder + File.separator + "workflows";
        FileObject workflowDir = HopVfs.getFileObject(workflowPath);
        if (!workflowDir.exists()) {
          workflowDir.createFolder();
        }
        String workflowFile = workflowPath + File.separator + workflowName;
        WorkflowMeta workflowMeta = new WorkflowMeta();
        workflowMeta.setName(workflowName);
        workflowMeta.setFilename(workflowFile);
        workflowMeta.setMetadataProvider(hopGui.getMetadataProvider());
        String xml = workflowMeta.getXml(variables);
        try (java.io.OutputStream out = HopVfs.getOutputStream(workflowFile, false)) {
          out.write(XmlHandler.getXmlHeader(Const.XML_ENCODING).getBytes(StandardCharsets.UTF_8));
          out.write(xml.getBytes(StandardCharsets.UTF_8));
        }
      }

      // ── Scaffold: run configs ──────────────────────────────────────────────
      if (wCreateRunConfigs.getSelection()) {
        IHopMetadataSerializer<PipelineRunConfiguration> prcSerializer =
            hopGui.getMetadataProvider().getSerializer(PipelineRunConfiguration.class);
        boolean localFound = false;
        for (PipelineRunConfiguration rc : prcSerializer.loadAll()) {
          if (rc.getEngineRunConfiguration() instanceof LocalPipelineRunConfiguration) {
            localFound = true;
            break;
          }
        }
        if (!localFound) {
          PipelineExecutionConfigurationDialog.createLocalPipelineConfiguration(
              shell, prcSerializer);
        }

        IHopMetadataSerializer<WorkflowRunConfiguration> wrcSerializer =
            hopGui.getMetadataProvider().getSerializer(WorkflowRunConfiguration.class);
        localFound = false;
        for (WorkflowRunConfiguration rc : wrcSerializer.loadAll()) {
          if (rc.getEngineRunConfiguration() instanceof LocalWorkflowRunConfiguration) {
            localFound = true;
            break;
          }
        }
        if (!localFound) {
          LocalWorkflowRunConfiguration localWf = new LocalWorkflowRunConfiguration();
          localWf.setEnginePluginId("Local");
          WorkflowRunConfiguration local =
              new WorkflowRunConfiguration(
                  "local",
                  BaseMessages.getString(
                      ProjectsGuiPlugin.PKG, "ProjectGuiPlugin.LocalWFRunConfigDescription.Text"),
                  null,
                  localWf,
                  true);
          wrcSerializer.save(local);
        }
      }

      // ── Scaffold: environment ──────────────────────────────────────────────
      if (wCreateEnvironment.getSelection() && addEnvironmentCallback != null) {
        addEnvironmentCallback.run();
      }

      returnValue = projectName;
      dispose();
    } catch (Exception e) {
      new ErrorDialog(
          shell,
          BaseMessages.getString(
              ProjectsGuiPlugin.PKG, "ProjectGuiPlugin.AddProject.Error.Dialog.Header"),
          BaseMessages.getString(
              ProjectsGuiPlugin.PKG, "ProjectGuiPlugin.AddProject.Error.Dialog.Message"),
          e);
    }
  }

  private void cancel() {
    returnValue = null;
    dispose();
  }

  private void dispose() {
    props.setScreen(new WindowProperty(shell));
    shell.dispose();
  }
}
