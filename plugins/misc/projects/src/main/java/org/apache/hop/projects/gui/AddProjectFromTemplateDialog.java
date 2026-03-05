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
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.hop.core.Const;
import org.apache.hop.core.config.HopConfig;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.core.variables.Variables;
import org.apache.hop.core.vfs.HopVfs;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.projects.config.ProjectsConfig;
import org.apache.hop.projects.config.ProjectsConfigSingleton;
import org.apache.hop.projects.project.Project;
import org.apache.hop.projects.project.ProjectConfig;
import org.apache.hop.projects.util.GitCloneHelper;
import org.apache.hop.ui.core.ConstUi;
import org.apache.hop.ui.core.PropsUi;
import org.apache.hop.ui.core.dialog.BaseDialog;
import org.apache.hop.ui.core.dialog.ErrorDialog;
import org.apache.hop.ui.core.gui.GuiResource;
import org.apache.hop.ui.core.gui.WindowProperty;
import org.apache.hop.ui.core.widget.TextVar;
import org.apache.hop.ui.pipeline.transform.BaseTransformDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class AddProjectFromTemplateDialog extends Dialog {
  private static final Class<?> PKG = AddProjectFromTemplateDialog.class;

  private static final String TEMP_CLONE_PREFIX = "hop-template-clone-";

  private String returnValue;
  private Shell shell;
  private final PropsUi props;
  private final IVariables variables;

  private Combo wSource;
  private Text wGitUrl;
  private Text wToken;
  private Button wShallowClone;
  private Text wShallowDepth;
  private Label wlShallow;
  private Label wlCommits;
  private Label wlLocal;
  private Button wbLocal;
  private TextVar wLocalFolder;
  private Text wProjectName;
  private TextVar wDestination;
  private Composite comp;
  private Label wlUrl;
  private Label wlToken;
  private Label wlName;
  private FormData fdlNameTop;
  private int margin;
  private int middle;

  public AddProjectFromTemplateDialog(Shell parent, IVariables variables) {
    super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);
    this.variables = new Variables();
    this.variables.initializeFrom(variables);
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

    margin = PropsUi.getMargin() + 2;
    middle = props.getMiddlePct();

    shell.setLayout(new FormLayout());
    shell.setText(BaseMessages.getString(PKG, "AddProjectFromTemplateDialog.Shell.Name"));

    Button wOk = new Button(shell, SWT.PUSH);
    wOk.setText(BaseMessages.getString(PKG, "System.Button.OK"));
    wOk.addListener(SWT.Selection, event -> ok());
    Button wCancel = new Button(shell, SWT.PUSH);
    wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));
    wCancel.addListener(SWT.Selection, event -> cancel());
    BaseTransformDialog.positionBottomButtons(shell, new Button[] {wOk, wCancel}, margin * 3, null);

    comp = new Composite(shell, SWT.NONE);
    comp.setLayout(new FormLayout());
    PropsUi.setLook(comp);

    FormData fdComp = new FormData();
    fdComp.left = new FormAttachment(0, 0);
    fdComp.right = new FormAttachment(100, 0);
    fdComp.top = new FormAttachment(0, 0);
    fdComp.bottom = new FormAttachment(wOk, -margin);
    comp.setLayoutData(fdComp);

    Control lastControl = null;

    // Template source (dropdown: Template from Git / Local template)
    Label wlSource = new Label(comp, SWT.RIGHT);
    wlSource.setText(
        BaseMessages.getString(PKG, "AddProjectFromTemplateDialog.Label.TemplateSource"));
    FormData fdlSource = new FormData();
    fdlSource.left = new FormAttachment(0, 0);
    fdlSource.right = new FormAttachment(middle, 0);
    fdlSource.top = new FormAttachment(0, margin * 2);
    wlSource.setLayoutData(fdlSource);
    wSource = new Combo(comp, SWT.DROP_DOWN | SWT.READ_ONLY);
    wSource.setItems(
        new String[] {
          BaseMessages.getString(PKG, "AddProjectFromTemplateDialog.Label.FromGit"),
          BaseMessages.getString(PKG, "AddProjectFromTemplateDialog.Label.FromLocal")
        });
    wSource.select(0);
    wSource.addListener(SWT.Selection, e -> updateSourceState());
    FormData fdSource = new FormData();
    fdSource.left = new FormAttachment(middle, margin);
    fdSource.right = new FormAttachment(99, 0);
    fdSource.top = new FormAttachment(wlSource, 0, SWT.CENTER);
    wSource.setLayoutData(fdSource);
    lastControl = wSource;

    // Git options
    wlUrl = new Label(comp, SWT.RIGHT);
    wlUrl.setText(BaseMessages.getString(PKG, "AddProjectFromTemplateDialog.Label.GitUrl"));
    FormData fdlUrl = new FormData();
    fdlUrl.left = new FormAttachment(0, 0);
    fdlUrl.right = new FormAttachment(middle, 0);
    fdlUrl.top = new FormAttachment(lastControl, margin);
    wlUrl.setLayoutData(fdlUrl);
    wGitUrl = new Text(comp, SWT.SINGLE | SWT.BORDER | SWT.LEFT);
    wGitUrl.addListener(SWT.Modify, e -> updateProjectNameFromUrl());
    FormData fdGitUrl = new FormData();
    fdGitUrl.left = new FormAttachment(middle, margin);
    fdGitUrl.right = new FormAttachment(99, 0);
    fdGitUrl.top = new FormAttachment(wlUrl, 0, SWT.CENTER);
    wGitUrl.setLayoutData(fdGitUrl);

    wlToken = new Label(comp, SWT.RIGHT);
    wlToken.setText(BaseMessages.getString(PKG, "AddProjectFromTemplateDialog.Label.Token"));
    FormData fdlToken = new FormData();
    fdlToken.left = new FormAttachment(0, 0);
    fdlToken.right = new FormAttachment(middle, 0);
    fdlToken.top = new FormAttachment(wGitUrl, margin);
    wlToken.setLayoutData(fdlToken);
    wToken = new Text(comp, SWT.SINGLE | SWT.BORDER | SWT.LEFT | SWT.PASSWORD);
    PropsUi.setLook(wToken);
    FormData fdToken = new FormData();
    fdToken.left = new FormAttachment(middle, margin);
    fdToken.right = new FormAttachment(99, 0);
    fdToken.top = new FormAttachment(wlToken, 0, SWT.CENTER);
    wToken.setLayoutData(fdToken);
    wToken.setToolTipText(
        BaseMessages.getString(PKG, "AddProjectFromTemplateDialog.Label.Token.Tooltip"));

    wlShallow = new Label(comp, SWT.RIGHT);
    wlShallow.setText(
        BaseMessages.getString(PKG, "AddProjectFromTemplateDialog.Label.ShallowClone"));
    FormData fdlShallow = new FormData();
    fdlShallow.left = new FormAttachment(0, 0);
    fdlShallow.right = new FormAttachment(middle, 0);
    fdlShallow.top = new FormAttachment(wToken, margin);
    wlShallow.setLayoutData(fdlShallow);
    wShallowClone = new Button(comp, SWT.CHECK);
    FormData fdShallow = new FormData();
    fdShallow.left = new FormAttachment(middle, margin);
    fdShallow.top = new FormAttachment(wlShallow, 0, SWT.CENTER);
    wShallowClone.setLayoutData(fdShallow);
    wShallowClone.addListener(SWT.Selection, e -> updateShallowDepthState());
    wShallowDepth = new Text(comp, SWT.SINGLE | SWT.BORDER | SWT.LEFT);
    PropsUi.setLook(wShallowDepth);
    wShallowDepth.setText("1");
    FormData fdDepth = new FormData();
    fdDepth.left = new FormAttachment(wShallowClone, margin);
    fdDepth.top = new FormAttachment(wlShallow, 0, SWT.CENTER);
    fdDepth.width = 60;
    wShallowDepth.setLayoutData(fdDepth);
    wlCommits = new Label(comp, SWT.LEFT);
    wlCommits.setText(
        " "
            + BaseMessages.getString(
                PKG, "AddProjectFromTemplateDialog.Label.ShallowClone.Commits"));
    FormData fdlCommits = new FormData();
    fdlCommits.left = new FormAttachment(wShallowDepth, margin);
    fdlCommits.top = new FormAttachment(wlShallow, 0, SWT.CENTER);
    wlCommits.setLayoutData(fdlCommits);

    // Local options
    wlLocal = new Label(comp, SWT.RIGHT);
    wlLocal.setText(BaseMessages.getString(PKG, "AddProjectFromTemplateDialog.Label.LocalFolder"));
    FormData fdlLocal = new FormData();
    fdlLocal.left = new FormAttachment(0, 0);
    fdlLocal.right = new FormAttachment(middle, 0);
    fdlLocal.top = new FormAttachment(lastControl, margin);
    wlLocal.setLayoutData(fdlLocal);
    wbLocal = new Button(comp, SWT.PUSH);
    wbLocal.setText(BaseMessages.getString(PKG, "AddProjectFromTemplateDialog.Button.Browse"));
    FormData fdbLocal = new FormData();
    fdbLocal.right = new FormAttachment(99, 0);
    fdbLocal.top = new FormAttachment(wlLocal, 0, SWT.CENTER);
    wbLocal.setLayoutData(fdbLocal);
    wbLocal.addListener(SWT.Selection, e -> browseLocalFolder());
    wLocalFolder = new TextVar(variables, comp, SWT.SINGLE | SWT.BORDER | SWT.LEFT);
    FormData fdLocal = new FormData();
    fdLocal.left = new FormAttachment(middle, margin);
    fdLocal.right = new FormAttachment(wbLocal, -margin);
    fdLocal.top = new FormAttachment(wlLocal, 0, SWT.CENTER);
    wLocalFolder.setLayoutData(fdLocal);

    // Project name
    wlName = new Label(comp, SWT.RIGHT);
    wlName.setText(BaseMessages.getString(PKG, "AddProjectFromTemplateDialog.Label.ProjectName"));
    fdlNameTop = new FormData();
    fdlNameTop.left = new FormAttachment(0, 0);
    fdlNameTop.right = new FormAttachment(middle, 0);
    fdlNameTop.top = new FormAttachment(wlCommits, margin);
    wlName.setLayoutData(fdlNameTop);
    wProjectName = new Text(comp, SWT.SINGLE | SWT.BORDER | SWT.LEFT);
    FormData fdName = new FormData();
    fdName.left = new FormAttachment(middle, margin);
    fdName.right = new FormAttachment(99, 0);
    fdName.top = new FormAttachment(wlName, 0, SWT.CENTER);
    wProjectName.setLayoutData(fdName);

    // Destination folder
    Label wlDest = new Label(comp, SWT.RIGHT);
    wlDest.setText(
        BaseMessages.getString(PKG, "AddProjectFromTemplateDialog.Label.DestinationFolder"));
    FormData fdlDest = new FormData();
    fdlDest.left = new FormAttachment(0, 0);
    fdlDest.right = new FormAttachment(middle, 0);
    fdlDest.top = new FormAttachment(wProjectName, margin);
    wlDest.setLayoutData(fdlDest);
    Button wbDest = new Button(comp, SWT.PUSH);
    wbDest.setText(BaseMessages.getString(PKG, "AddProjectFromTemplateDialog.Button.Browse"));
    FormData fdbDest = new FormData();
    fdbDest.right = new FormAttachment(99, 0);
    fdbDest.top = new FormAttachment(wlDest, 0, SWT.CENTER);
    wbDest.setLayoutData(fdbDest);
    wbDest.addListener(SWT.Selection, e -> browseDestination());
    wDestination = new TextVar(variables, comp, SWT.SINGLE | SWT.BORDER | SWT.LEFT);
    FormData fdDest = new FormData();
    fdDest.left = new FormAttachment(middle, margin);
    fdDest.right = new FormAttachment(wbDest, -margin);
    fdDest.top = new FormAttachment(wlDest, 0, SWT.CENTER);
    wDestination.setLayoutData(fdDest);

    getData();
    updateSourceState();
    updateShallowDepthState();

    shell.setDefaultButton(wOk);
    wGitUrl.setFocus();
    BaseDialog.defaultShellHandling(shell, c -> ok(), c -> cancel());

    return returnValue;
  }

  private void updateSourceState() {
    boolean fromGit = wSource.getSelectionIndex() == 0;

    // Git options
    wlUrl.setVisible(fromGit);
    wGitUrl.setVisible(fromGit);
    wlToken.setVisible(fromGit);
    wToken.setVisible(fromGit);
    wlShallow.setVisible(fromGit);
    wShallowClone.setVisible(fromGit);
    wShallowDepth.setVisible(fromGit);
    wlCommits.setVisible(fromGit);

    // Local options
    wlLocal.setVisible(!fromGit);
    wbLocal.setVisible(!fromGit);
    wLocalFolder.setVisible(!fromGit);

    // Update Project name section top attachment
    Control topAttachment = fromGit ? wlCommits : wLocalFolder;
    fdlNameTop.top = new FormAttachment(topAttachment, margin);

    comp.layout(true, true);
  }

  private void updateShallowDepthState() {
    wShallowDepth.setEnabled(wShallowClone.getSelection());
  }

  private void updateProjectNameFromUrl() {
    if (wSource.getSelectionIndex() == 0 && StringUtils.isEmpty(wProjectName.getText())) {
      String url = wGitUrl.getText();
      if (StringUtils.isNotEmpty(url)) {
        String baseName = FilenameUtils.getBaseName(url.replaceFirst("\\.git$", ""));
        if (StringUtils.isNotEmpty(baseName)) {
          wProjectName.setText(baseName);
        }
      }
    }
  }

  private void browseLocalFolder() {
    BaseDialog.presentDirectoryDialog(shell, wLocalFolder, variables);
  }

  private void browseDestination() {
    BaseDialog.presentDirectoryDialog(shell, wDestination, variables);
  }

  private void getData() {
    ProjectsConfig config = ProjectsConfigSingleton.getConfig();
    String standardFolder = variables.resolve(config.getStandardProjectsFolder());
    wDestination.setText(Const.NVL(standardFolder, ""));
  }

  private void ok() {
    try {
      String projectName = wProjectName.getText().trim();
      if (StringUtils.isEmpty(projectName)) {
        throw new HopException("Please specify a project name");
      }
      String destination = variables.resolve(wDestination.getText());
      if (StringUtils.isEmpty(destination)) {
        throw new HopException("Please specify the destination folder");
      }

      ProjectsConfig config = ProjectsConfigSingleton.getConfig();
      if (config.findProjectConfig(projectName) != null) {
        throw new HopException("Project '" + projectName + "' already exists");
      }

      String projectPath = destination + File.separator + projectName;
      File projectDir = new File(projectPath);
      if (projectDir.exists()) {
        throw new HopException("Directory '" + projectPath + "' already exists");
      }

      Path templatePath;
      if (wSource.getSelectionIndex() == 0) {
        String url = wGitUrl.getText().trim();
        if (StringUtils.isEmpty(url)) {
          throw new HopException("Please specify the Git repository URL");
        }
        int depth = 0;
        if (wShallowClone.getSelection()) {
          try {
            depth = Integer.parseInt(wShallowDepth.getText().trim());
            if (depth < 1) {
              depth = 1;
            }
          } catch (NumberFormatException e) {
            depth = 1;
          }
        }
        Path tempDir = Files.createTempDirectory(TEMP_CLONE_PREFIX);
        try {
          if (!GitCloneHelper.cloneRepo(tempDir.toString(), url, wToken.getText(), depth)) {
            return;
          }
          templatePath = tempDir;
          copyTemplateToDestination(templatePath, projectDir.toPath());
        } finally {
          org.apache.commons.io.FileUtils.deleteQuietly(tempDir.toFile());
        }
      } else {
        String localFolder = variables.resolve(wLocalFolder.getText());
        if (StringUtils.isEmpty(localFolder)) {
          throw new HopException("Please specify the template folder");
        }
        File localDir = new File(localFolder);
        if (!localDir.exists() || !localDir.isDirectory()) {
          throw new HopException("Template folder '" + localFolder + "' does not exist");
        }
        templatePath = localDir.toPath();
        copyTemplateToDestination(templatePath, projectDir.toPath());
      }

      String defaultConfigFile = variables.resolve(config.getDefaultProjectConfigFile());
      ProjectConfig projectConfig = new ProjectConfig(projectName, projectPath, defaultConfigFile);
      Project project = new Project();
      project.setParentProjectName(config.getStandardParentProject());

      String configFilename = projectConfig.getActualProjectConfigFilename(variables);
      FileObject configFile = HopVfs.getFileObject(configFilename);
      if (configFile.exists()) {
        project.setConfigFilename(configFilename);
        project.readFromFile();
      } else {
        project.setConfigFilename(configFilename);
        if (!configFile.getParent().exists()) {
          configFile.getParent().createFolder();
        }
        project.saveToFile();
      }

      config.addProjectConfig(projectConfig);
      HopConfig.getInstance().saveToFile();

      ProjectsGuiPlugin.updateProjectToolItem(projectName);
      ProjectsGuiPlugin.enableHopGuiProject(projectName, project, null);

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

  private void copyTemplateToDestination(Path source, Path destination) throws Exception {
    Files.walkFileTree(
        source,
        new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
              throws java.io.IOException {
            if (dir.equals(source)) {
              Files.createDirectories(destination);
              return FileVisitResult.CONTINUE;
            }
            String name = dir.getFileName().toString();
            if (".git".equals(name)) {
              return FileVisitResult.SKIP_SUBTREE;
            }
            Path targetDir = destination.resolve(source.relativize(dir));
            Files.createDirectories(targetDir);
            return FileVisitResult.CONTINUE;
          }

          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
              throws java.io.IOException {
            Path target = destination.resolve(source.relativize(file));
            Files.copy(file, target);
            return FileVisitResult.CONTINUE;
          }
        });
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
