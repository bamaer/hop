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
import org.apache.hop.ui.core.ConstUi;
import org.apache.hop.ui.core.PropsUi;
import org.apache.hop.ui.core.dialog.BaseDialog;
import org.apache.hop.ui.core.dialog.ErrorDialog;
import org.apache.hop.ui.core.gui.GuiResource;
import org.apache.hop.ui.core.gui.WindowProperty;
import org.apache.hop.ui.core.widget.TextVar;
import org.apache.hop.ui.hopgui.HopGui;
import org.apache.hop.ui.pipeline.dialog.PipelineExecutionConfigurationDialog;
import org.apache.hop.ui.pipeline.transform.BaseTransformDialog;
import org.apache.hop.workflow.WorkflowMeta;
import org.apache.hop.workflow.config.WorkflowRunConfiguration;
import org.apache.hop.workflow.engines.local.LocalWorkflowRunConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class EmptyProjectDialog extends Dialog {
  private static final Class<?> PKG = EmptyProjectDialog.class;

  private String returnValue;
  private Shell shell;
  private final PropsUi props;
  private final IVariables variables;
  private final Runnable addEnvironmentCallback;

  private Text wName;
  private TextVar wHome;
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

    Composite comp = new Composite(shell, SWT.NONE);
    comp.setLayout(new FormLayout());
    PropsUi.setLook(comp);

    FormData fdComp = new FormData();
    fdComp.left = new FormAttachment(0, 0);
    fdComp.right = new FormAttachment(100, 0);
    fdComp.top = new FormAttachment(0, 0);
    fdComp.bottom = new FormAttachment(wOk, -margin);
    comp.setLayoutData(fdComp);

    Control lastControl = null;

    // Project name
    Label wlName = new Label(comp, SWT.RIGHT);
    wlName.setText(BaseMessages.getString(PKG, "EmptyProjectDialog.Label.ProjectName"));
    FormData fdlName = new FormData();
    fdlName.left = new FormAttachment(0, 0);
    fdlName.right = new FormAttachment(middle, 0);
    fdlName.top = new FormAttachment(0, margin * 2);
    wlName.setLayoutData(fdlName);
    wName = new Text(comp, SWT.SINGLE | SWT.BORDER | SWT.LEFT);
    FormData fdName = new FormData();
    fdName.left = new FormAttachment(middle, margin);
    fdName.right = new FormAttachment(99, 0);
    fdName.top = new FormAttachment(wlName, 0, SWT.CENTER);
    wName.setLayoutData(fdName);
    lastControl = wName;

    // Home folder
    Label wlHome = new Label(comp, SWT.RIGHT);
    wlHome.setText(BaseMessages.getString(PKG, "EmptyProjectDialog.Label.HomeFolder"));
    FormData fdlHome = new FormData();
    fdlHome.left = new FormAttachment(0, 0);
    fdlHome.right = new FormAttachment(middle, 0);
    fdlHome.top = new FormAttachment(lastControl, margin);
    wlHome.setLayoutData(fdlHome);
    Button wbHome = new Button(comp, SWT.PUSH);
    wbHome.setText(BaseMessages.getString(PKG, "ProjectDialog.Button.Browse"));
    FormData fdbHome = new FormData();
    fdbHome.right = new FormAttachment(99, 0);
    fdbHome.top = new FormAttachment(wlHome, 0, SWT.CENTER);
    wbHome.setLayoutData(fdbHome);
    wbHome.addListener(SWT.Selection, e -> browseHome());
    wHome = new TextVar(variables, comp, SWT.SINGLE | SWT.BORDER | SWT.LEFT);
    FormData fdHome = new FormData();
    fdHome.left = new FormAttachment(middle, margin);
    fdHome.right = new FormAttachment(wbHome, -margin);
    fdHome.top = new FormAttachment(wlHome, 0, SWT.CENTER);
    wHome.setLayoutData(fdHome);
    lastControl = wHome;

    // Create pipeline
    wCreatePipeline = new Button(comp, SWT.CHECK);
    wCreatePipeline.setText(BaseMessages.getString(PKG, "EmptyProjectDialog.Label.CreatePipeline"));
    FormData fdCreatePipeline = new FormData();
    fdCreatePipeline.left = new FormAttachment(0, 0);
    fdCreatePipeline.right = new FormAttachment(99, 0);
    fdCreatePipeline.top = new FormAttachment(lastControl, margin * 2);
    wCreatePipeline.setLayoutData(fdCreatePipeline);
    wCreatePipeline.addListener(SWT.Selection, e -> updatePipelineNameState());
    lastControl = wCreatePipeline;

    wPipelineName = new Text(comp, SWT.SINGLE | SWT.BORDER | SWT.LEFT);
    FormData fdPipelineName = new FormData();
    fdPipelineName.left = new FormAttachment(middle, margin);
    fdPipelineName.right = new FormAttachment(99, 0);
    fdPipelineName.top = new FormAttachment(lastControl, margin);
    wPipelineName.setLayoutData(fdPipelineName);
    lastControl = wPipelineName;

    // Create workflow
    wCreateWorkflow = new Button(comp, SWT.CHECK);
    wCreateWorkflow.setText(BaseMessages.getString(PKG, "EmptyProjectDialog.Label.CreateWorkflow"));
    FormData fdCreateWorkflow = new FormData();
    fdCreateWorkflow.left = new FormAttachment(0, 0);
    fdCreateWorkflow.right = new FormAttachment(99, 0);
    fdCreateWorkflow.top = new FormAttachment(lastControl, margin * 2);
    wCreateWorkflow.setLayoutData(fdCreateWorkflow);
    wCreateWorkflow.addListener(SWT.Selection, e -> updateWorkflowNameState());
    lastControl = wCreateWorkflow;

    wWorkflowName = new Text(comp, SWT.SINGLE | SWT.BORDER | SWT.LEFT);
    FormData fdWorkflowName = new FormData();
    fdWorkflowName.left = new FormAttachment(middle, margin);
    fdWorkflowName.right = new FormAttachment(99, 0);
    fdWorkflowName.top = new FormAttachment(lastControl, margin);
    wWorkflowName.setLayoutData(fdWorkflowName);
    lastControl = wWorkflowName;

    // Create run configs
    wCreateRunConfigs = new Button(comp, SWT.CHECK);
    wCreateRunConfigs.setText(
        BaseMessages.getString(PKG, "EmptyProjectDialog.Label.CreateRunConfigs"));
    FormData fdCreateRunConfigs = new FormData();
    fdCreateRunConfigs.left = new FormAttachment(0, 0);
    fdCreateRunConfigs.right = new FormAttachment(99, 0);
    fdCreateRunConfigs.top = new FormAttachment(lastControl, margin * 2);
    wCreateRunConfigs.setLayoutData(fdCreateRunConfigs);
    lastControl = wCreateRunConfigs;

    // Create environment
    wCreateEnvironment = new Button(comp, SWT.CHECK);
    wCreateEnvironment.setText(
        BaseMessages.getString(PKG, "EmptyProjectDialog.Label.CreateEnvironment"));
    FormData fdCreateEnvironment = new FormData();
    fdCreateEnvironment.left = new FormAttachment(0, 0);
    fdCreateEnvironment.right = new FormAttachment(99, 0);
    fdCreateEnvironment.top = new FormAttachment(lastControl, margin * 2);
    wCreateEnvironment.setLayoutData(fdCreateEnvironment);

    getData();
    updatePipelineNameState();
    updateWorkflowNameState();

    shell.setDefaultButton(wOk);
    wName.setFocus();
    BaseDialog.defaultShellHandling(shell, c -> ok(), c -> cancel());

    return returnValue;
  }

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
        // ignore
      }
    }
  }

  private void getData() {
    ProjectsConfig config = ProjectsConfigSingleton.getConfig();
    String standardFolder = variables.resolve(config.getStandardProjectsFolder());
    wHome.setText(Const.NVL(standardFolder, ""));
    wPipelineName.setText(BaseMessages.getString(PKG, "EmptyProjectDialog.Default.PipelineName"));
    wWorkflowName.setText(BaseMessages.getString(PKG, "EmptyProjectDialog.Default.WorkflowName"));
    wCreatePipeline.setSelection(true);
    wCreateWorkflow.setSelection(true);
    wCreateRunConfigs.setSelection(true);
    wCreateEnvironment.setSelection(false);
  }

  private void ok() {
    try {
      String projectName = wName.getText();
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

      String defaultConfigFile = variables.resolve(config.getDefaultProjectConfigFile());
      ProjectConfig projectConfig = new ProjectConfig(projectName, homeFolder, defaultConfigFile);
      Project project = new Project();
      project.setParentProjectName(config.getStandardParentProject());

      // Create project folder and config
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

      // Create pipeline if requested
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

      // Create workflow if requested
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

      // Create run configs if requested
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

      // Create environment if requested
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
