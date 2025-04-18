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

package org.apache.hop.pipeline.transforms.pipelineexecutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.hop.core.Const;
import org.apache.hop.core.Props;
import org.apache.hop.core.SourceToTargetMapping;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.extension.ExtensionPointHandler;
import org.apache.hop.core.extension.HopExtensionPoint;
import org.apache.hop.core.logging.LogChannel;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.value.ValueMetaFactory;
import org.apache.hop.core.util.Utils;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.core.vfs.HopVfs;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.config.PipelineRunConfiguration;
import org.apache.hop.ui.core.ConstUi;
import org.apache.hop.ui.core.PropsUi;
import org.apache.hop.ui.core.dialog.BaseDialog;
import org.apache.hop.ui.core.dialog.EnterMappingDialog;
import org.apache.hop.ui.core.dialog.ErrorDialog;
import org.apache.hop.ui.core.dialog.MessageBox;
import org.apache.hop.ui.core.gui.GuiResource;
import org.apache.hop.ui.core.widget.ColumnInfo;
import org.apache.hop.ui.core.widget.ColumnsResizer;
import org.apache.hop.ui.core.widget.ComboVar;
import org.apache.hop.ui.core.widget.TableView;
import org.apache.hop.ui.core.widget.TextVar;
import org.apache.hop.ui.hopgui.HopGui;
import org.apache.hop.ui.hopgui.file.pipeline.HopPipelineFileType;
import org.apache.hop.ui.pipeline.transform.BaseTransformDialog;
import org.apache.hop.ui.util.SwtSvgImageUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class PipelineExecutorDialog extends BaseTransformDialog {
  private static final Class<?> PKG = PipelineExecutorDialog.class;

  private static final int FIELD_DESCRIPTION = 1;
  private static final int FIELD_NAME = 2;

  private PipelineExecutorMeta pipelineExecutorMeta;

  private Label wlPath;
  private TextVar wPath;

  protected Label wlRunConfiguration;
  protected ComboVar wRunConfiguration;

  private Button wbPipelineNameInField;

  private Label wlPipelineNameField;
  private ComboVar wPipelineNameField;

  private CTabFolder wTabFolder;

  private PipelineMeta executorPipelineMeta = null;

  private Button wInheritAll;

  private TableView wPipelineExecutorParameters;

  private Label wlGroupSize;
  private TextVar wGroupSize;
  private Label wlGroupField;
  private CCombo wGroupField;
  private Label wlGroupTime;
  private TextVar wGroupTime;

  private CCombo wExecutionResultTarget;
  private TableItem tiExecutionTimeField;
  private TableItem tiExecutionResultField;
  private TableItem tiExecutionNrErrorsField;
  private TableItem tiExecutionLinesReadField;
  private TableItem tiExecutionLinesWrittenField;
  private TableItem tiExecutionLinesInputField;
  private TableItem tiExecutionLinesOutputField;
  private TableItem tiExecutionLinesRejectedField;
  private TableItem tiExecutionLinesUpdatedField;
  private TableItem tiExecutionLinesDeletedField;
  private TableItem tiExecutionFilesRetrievedField;
  private TableItem tiExecutionExitStatusField;
  private TableItem tiExecutionLogTextField;
  private TableItem tiExecutionLogChannelIdField;

  private String executorOutputTransform;

  private ColumnInfo[] parameterColumns;

  private CCombo wResultFilesTarget;

  private TextVar wResultFileNameField;

  private CCombo wOutputRowsSource;

  private TableView wOutputFields;

  private boolean gotPreviousFields = false;

  private final int middle = props.getMiddlePct();
  private final int margin = PropsUi.getMargin();

  public PipelineExecutorDialog(
      Shell parent,
      IVariables variables,
      PipelineExecutorMeta transformMeta,
      PipelineMeta pipelineMeta) {
    super(parent, variables, transformMeta, pipelineMeta);
    pipelineExecutorMeta = transformMeta;
  }

  @Override
  public String open() {
    Shell parent = getParent();

    shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
    PropsUi.setLook(shell);
    setShellImage(shell, pipelineExecutorMeta);

    changed = pipelineExecutorMeta.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = 15;
    formLayout.marginHeight = 15;

    shell.setLayout(formLayout);
    shell.setText(BaseMessages.getString(PKG, "PipelineExecutorDialog.Shell.Title"));

    ModifyListener lsMod = e -> pipelineExecutorMeta.setChanged();

    Label wicon = new Label(shell, SWT.RIGHT);
    wicon.setImage(getImage());
    FormData fdlicon = new FormData();
    fdlicon.top = new FormAttachment(0, 0);
    fdlicon.right = new FormAttachment(100, 0);
    wicon.setLayoutData(fdlicon);
    PropsUi.setLook(wicon);

    // Some buttons
    wCancel = new Button(shell, SWT.PUSH);
    wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));
    wCancel.addListener(SWT.Selection, e -> cancel());
    wOk = new Button(shell, SWT.PUSH);
    wOk.setText(BaseMessages.getString(PKG, "System.Button.OK"));
    wOk.addListener(SWT.Selection, e -> ok());
    positionBottomButtons(shell, new Button[] {wOk, wCancel}, PropsUi.getMargin(), null);

    // TransformName line
    wlTransformName = new Label(shell, SWT.RIGHT);
    wlTransformName.setText(
        BaseMessages.getString(PKG, "PipelineExecutorDialog.TransformName.Label"));
    PropsUi.setLook(wlTransformName);
    fdlTransformName = new FormData();
    fdlTransformName.left = new FormAttachment(0, 0);
    fdlTransformName.top = new FormAttachment(wicon, 0, SWT.CENTER);
    fdlTransformName.right = new FormAttachment(middle, -margin);
    wlTransformName.setLayoutData(fdlTransformName);

    wTransformName = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    wTransformName.setText(transformName);
    PropsUi.setLook(wTransformName);
    fdTransformName = new FormData();
    fdTransformName.right = new FormAttachment(wicon, 0);
    fdTransformName.left = new FormAttachment(middle, 0);
    fdTransformName.top = new FormAttachment(wlTransformName, 0, SWT.CENTER);
    wTransformName.setLayoutData(fdTransformName);

    Label spacer = new Label(shell, SWT.HORIZONTAL | SWT.SEPARATOR);
    FormData fdSpacer = new FormData();
    fdSpacer.left = new FormAttachment(0, 0);
    fdSpacer.top = new FormAttachment(wicon, 0);
    fdSpacer.right = new FormAttachment(100, 0);
    spacer.setLayoutData(fdSpacer);

    wlPath = new Label(shell, SWT.RIGHT);
    PropsUi.setLook(wlPath);
    wlPath.setText(BaseMessages.getString(PKG, "PipelineExecutorDialog.Pipeline.Label"));
    FormData fdlTransformation = new FormData();
    fdlTransformation.left = new FormAttachment(0, 0);
    fdlTransformation.top = new FormAttachment(spacer, 20);
    fdlTransformation.right = new FormAttachment(middle, -margin);
    wlPath.setLayoutData(fdlTransformation);

    Button wbBrowse = new Button(shell, SWT.PUSH);
    PropsUi.setLook(wbBrowse);
    wbBrowse.setText(BaseMessages.getString(PKG, "PipelineExecutorDialog.Browse.Label"));
    FormData fdBrowse = new FormData();
    fdBrowse.right = new FormAttachment(100, 0);
    fdBrowse.top = new FormAttachment(wlPath, 0, SWT.CENTER);
    wbBrowse.setLayoutData(fdBrowse);
    wbBrowse.addListener(SWT.Selection, e -> selectPipelineFile());

    wPath = new TextVar(variables, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    PropsUi.setLook(wPath);
    FormData fdTransformation = new FormData();
    fdTransformation.left = new FormAttachment(middle, 0);
    fdTransformation.top = new FormAttachment(wlPath, 0, SWT.CENTER);
    fdTransformation.right = new FormAttachment(wbBrowse, -margin);
    wPath.setLayoutData(fdTransformation);

    wbPipelineNameInField = new Button(shell, SWT.CHECK);
    PropsUi.setLook(wbPipelineNameInField);
    wbPipelineNameInField.setText(
        BaseMessages.getString(PKG, "PipelineExecutorDialog.PipelineNameInField.Label"));
    FormData fdPipelineNameInField = new FormData();
    fdPipelineNameInField.left = new FormAttachment(middle, 0);
    fdPipelineNameInField.top = new FormAttachment(wPath, margin);
    wbPipelineNameInField.setLayoutData(fdPipelineNameInField);
    wbPipelineNameInField.addSelectionListener(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            pipelineExecutorMeta.setChanged();
            activePipelineNameField();
          }
        });

    // FileNameField Line
    wlPipelineNameField = new Label(shell, SWT.RIGHT);
    wlPipelineNameField.setText(
        BaseMessages.getString(PKG, "PipelineExecutorDialog.PipelineNameField.Label"));
    PropsUi.setLook(wlPipelineNameField);
    FormData fdlPipelineNameField = new FormData();
    fdlPipelineNameField.left = new FormAttachment(0, 0);
    fdlPipelineNameField.right = new FormAttachment(middle, -margin);
    fdlPipelineNameField.top = new FormAttachment(wbPipelineNameInField, margin);
    wlPipelineNameField.setLayoutData(fdlPipelineNameField);

    wPipelineNameField = new ComboVar(variables, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    PropsUi.setLook(wPipelineNameField);
    wPipelineNameField.addModifyListener(lsMod);
    FormData fdPipelineNameField = new FormData();
    fdPipelineNameField.left = new FormAttachment(middle, 0);
    fdPipelineNameField.top = new FormAttachment(wlPipelineNameField, 0, SWT.CENTER);
    fdPipelineNameField.right = new FormAttachment(100, 0);
    wPipelineNameField.setLayoutData(fdPipelineNameField);
    wPipelineNameField.setEnabled(false);
    wPipelineNameField.addFocusListener(
        new FocusListener() {
          @Override
          public void focusLost(FocusEvent e) {
            // Do nothing
          }

          @Override
          public void focusGained(FocusEvent e) {
            Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
            shell.setCursor(busy);
            getFields();
            shell.setCursor(null);
            busy.dispose();
          }
        });
    /* End */

    wlRunConfiguration = new Label(shell, SWT.RIGHT);
    wlRunConfiguration.setText(
        BaseMessages.getString(PKG, "PipelineExecutorDialog.RunConfiguration.Label"));
    PropsUi.setLook(wlRunConfiguration);
    FormData fdlRunConfiguration = new FormData();
    fdlRunConfiguration.left = new FormAttachment(0, 0);
    fdlRunConfiguration.top = new FormAttachment(wPipelineNameField, margin);
    fdlRunConfiguration.right = new FormAttachment(middle, -margin);
    wlRunConfiguration.setLayoutData(fdlRunConfiguration);

    wRunConfiguration = new ComboVar(variables, shell, SWT.LEFT | SWT.BORDER);
    PropsUi.setLook(wlRunConfiguration);
    FormData fdRunConfiguration = new FormData();
    fdRunConfiguration.left = new FormAttachment(middle, 0);
    fdRunConfiguration.top = new FormAttachment(wlRunConfiguration, 0, SWT.CENTER);
    fdRunConfiguration.right = new FormAttachment(100, 0);
    wRunConfiguration.setLayoutData(fdRunConfiguration);
    PropsUi.setLook(wRunConfiguration);

    //
    // Add a tab folder for the parameters and various input and output
    // streams
    //
    wTabFolder = new CTabFolder(shell, SWT.BORDER);
    PropsUi.setLook(wTabFolder, Props.WIDGET_STYLE_TAB);
    wTabFolder.setUnselectedCloseVisible(true);

    Label hSpacer = new Label(shell, SWT.HORIZONTAL | SWT.SEPARATOR);
    FormData fdhSpacer = new FormData();
    fdhSpacer.left = new FormAttachment(0, 0);
    fdhSpacer.bottom = new FormAttachment(wCancel, -15);
    fdhSpacer.right = new FormAttachment(100, 0);
    hSpacer.setLayoutData(fdhSpacer);

    FormData fdTabFolder = new FormData();
    fdTabFolder.left = new FormAttachment(0, 0);
    fdTabFolder.top = new FormAttachment(wRunConfiguration, 20);
    fdTabFolder.right = new FormAttachment(100, 0);
    fdTabFolder.bottom = new FormAttachment(hSpacer, -15);
    wTabFolder.setLayoutData(fdTabFolder);

    // Add the tabs...
    //
    addParametersTab();
    addExecutionResultTab();
    addRowGroupTab();
    addResultRowsTab();
    addResultFilesTab();

    getData();
    pipelineExecutorMeta.setChanged(changed);
    wTabFolder.setSelection(0);

    BaseDialog.defaultShellHandling(shell, c -> ok(), c -> cancel());

    return transformName;
  }

  private void getFields() {
    if (!gotPreviousFields) {
      try {
        String field = wPipelineNameField.getText();
        IRowMeta r = pipelineMeta.getPrevTransformFields(variables, transformName);
        if (r != null) {
          wPipelineNameField.setItems(r.getFieldNames());
        }
        if (field != null) {
          wPipelineNameField.setText(field);
        }
      } catch (HopException ke) {
        new ErrorDialog(
            shell,
            BaseMessages.getString(PKG, "TextFileOutputDialog.FailedToGetFields.DialogTitle"),
            BaseMessages.getString(PKG, "TextFileOutputDialog.FailedToGetFields.DialogMessage"),
            ke);
      }
      gotPreviousFields = true;
    }
  }

  protected Image getImage() {
    return SwtSvgImageUtil.getImage(
        shell.getDisplay(),
        getClass().getClassLoader(),
        "ui/images/pipelineexecutor.svg",
        ConstUi.LARGE_ICON_SIZE,
        ConstUi.LARGE_ICON_SIZE);
  }

  private void selectPipelineFile() {
    String parentFolder = null;
    try {
      parentFolder =
          HopVfs.getFileObject(variables.resolve(pipelineMeta.getFilename()), variables)
              .getParent()
              .toString();
    } catch (Exception e) {
      // Take no action
    }

    try {
      HopPipelineFileType<PipelineMeta> fileType =
          HopGui.getDataOrchestrationPerspective().getPipelineFileType();
      String filename =
          BaseDialog.presentFileDialog(
              shell,
              wPath,
              variables,
              fileType.getFilterExtensions(),
              fileType.getFilterNames(),
              true);
      if (filename != null) {
        loadPipelineFile(filename);
        if (parentFolder != null && filename.startsWith(parentFolder)) {
          filename =
              filename.replace(
                  parentFolder, "${" + Const.INTERNAL_VARIABLE_ENTRY_CURRENT_FOLDER + "}");
        }
        wPath.setText(filename);
      }
      if (filename != null) {
        replaceNameWithBaseFilename(filename);
      }
    } catch (HopException e) {
      new ErrorDialog(
          shell,
          BaseMessages.getString(PKG, "PipelineExecutorDialog.ErrorLoadingPipeline.DialogTitle"),
          BaseMessages.getString(PKG, "PipelineExecutorDialog.ErrorLoadingPipeline.DialogMessage"),
          e);
    }
  }

  private void loadPipelineFile(String fname) throws HopException {
    String filename = variables.resolve(fname);
    executorPipelineMeta = new PipelineMeta(filename, metadataProvider, variables);
    executorPipelineMeta.clearChanged();
  }

  // Method is defined as package-protected in order to be accessible by unit tests
  void loadPipeline() throws HopException {
    String filename = wPath.getText();
    if (Utils.isEmpty(filename)) {
      return;
    }
    if (!filename.endsWith(".hpl")) {
      filename = filename + ".hpl";
      wPath.setText(filename);
    }
    loadPipelineFile(filename);
  }

  /** Copy information from the meta-data input to the dialog fields. */
  public void getData() {
    wPath.setText(Const.NVL(pipelineExecutorMeta.getFilename(), ""));

    try {
      List<String> runConfigurations =
          metadataProvider.getSerializer(PipelineRunConfiguration.class).listObjectNames();

      try {
        ExtensionPointHandler.callExtensionPoint(
            HopGui.getInstance().getLog(),
            variables,
            HopExtensionPoint.HopGuiRunConfiguration.id,
            new Object[] {runConfigurations, PipelineMeta.XML_TAG});
      } catch (HopException e) {
        // Ignore errors
      }

      wRunConfiguration.setItems(runConfigurations.toArray(new String[0]));
      wRunConfiguration.setText(Const.NVL(pipelineExecutorMeta.getRunConfigurationName(), ""));

      wbPipelineNameInField.setSelection(pipelineExecutorMeta.isFilenameInField());
      if (pipelineExecutorMeta.getFilenameField() != null) {
        wPipelineNameField.setText(pipelineExecutorMeta.getFilenameField());
      }

      activePipelineNameField();

      if (Utils.isEmpty(pipelineExecutorMeta.getRunConfigurationName())) {
        wRunConfiguration.select(0);
      } else {
        wRunConfiguration.setText(pipelineExecutorMeta.getRunConfigurationName());
      }
    } catch (Exception e) {
      LogChannel.UI.logError("Error getting pipeline run configurations", e);
    }

    // TODO: throw in a separate thread.
    //
    try {
      String[] prevTransforms = pipelineMeta.getTransformNames();
      Arrays.sort(prevTransforms);
      wExecutionResultTarget.setItems(prevTransforms);
      wResultFilesTarget.setItems(prevTransforms);
      wOutputRowsSource.setItems(prevTransforms);

      String[] inputFields =
          pipelineMeta.getPrevTransformFields(variables, transformMeta).getFieldNames();
      parameterColumns[1].setComboValues(inputFields);
      wGroupField.setItems(inputFields);
    } catch (Exception e) {
      log.logError("couldn't get previous transform list", e);
    }

    wGroupSize.setText(Const.NVL(pipelineExecutorMeta.getGroupSize(), ""));
    wGroupTime.setText(Const.NVL(pipelineExecutorMeta.getGroupTime(), ""));
    wGroupField.setText(Const.NVL(pipelineExecutorMeta.getGroupField(), ""));

    wExecutionResultTarget.setText(
        pipelineExecutorMeta.getExecutionResultTargetTransformMeta() == null
            ? ""
            : pipelineExecutorMeta.getExecutionResultTargetTransformMeta().getName());
    tiExecutionTimeField.setText(
        FIELD_NAME, Const.NVL(pipelineExecutorMeta.getExecutionTimeField(), ""));
    tiExecutionResultField.setText(
        FIELD_NAME, Const.NVL(pipelineExecutorMeta.getExecutionResultField(), ""));
    tiExecutionNrErrorsField.setText(
        FIELD_NAME, Const.NVL(pipelineExecutorMeta.getExecutionNrErrorsField(), ""));
    tiExecutionLinesReadField.setText(
        FIELD_NAME, Const.NVL(pipelineExecutorMeta.getExecutionLinesReadField(), ""));
    tiExecutionLinesWrittenField.setText(
        FIELD_NAME, Const.NVL(pipelineExecutorMeta.getExecutionLinesWrittenField(), ""));
    tiExecutionLinesInputField.setText(
        FIELD_NAME, Const.NVL(pipelineExecutorMeta.getExecutionLinesInputField(), ""));
    tiExecutionLinesOutputField.setText(
        FIELD_NAME, Const.NVL(pipelineExecutorMeta.getExecutionLinesOutputField(), ""));
    tiExecutionLinesRejectedField.setText(
        FIELD_NAME, Const.NVL(pipelineExecutorMeta.getExecutionLinesRejectedField(), ""));
    tiExecutionLinesUpdatedField.setText(
        FIELD_NAME, Const.NVL(pipelineExecutorMeta.getExecutionLinesUpdatedField(), ""));
    tiExecutionLinesDeletedField.setText(
        FIELD_NAME, Const.NVL(pipelineExecutorMeta.getExecutionLinesDeletedField(), ""));
    tiExecutionFilesRetrievedField.setText(
        FIELD_NAME, Const.NVL(pipelineExecutorMeta.getExecutionFilesRetrievedField(), ""));
    tiExecutionExitStatusField.setText(
        FIELD_NAME, Const.NVL(pipelineExecutorMeta.getExecutionExitStatusField(), ""));
    tiExecutionLogTextField.setText(
        FIELD_NAME, Const.NVL(pipelineExecutorMeta.getExecutionLogTextField(), ""));
    tiExecutionLogChannelIdField.setText(
        FIELD_NAME, Const.NVL(pipelineExecutorMeta.getExecutionLogChannelIdField(), ""));

    if (pipelineExecutorMeta.getExecutorsOutputTransformMeta() != null) {
      executorOutputTransform = pipelineExecutorMeta.getExecutorsOutputTransformMeta().getName();
    }

    // result files
    //
    wResultFilesTarget.setText(
        pipelineExecutorMeta.getResultFilesTargetTransformMeta() == null
            ? ""
            : pipelineExecutorMeta.getResultFilesTargetTransformMeta().getName());
    wResultFileNameField.setText(Const.NVL(pipelineExecutorMeta.getResultFilesFileNameField(), ""));

    // Result rows
    //
    wOutputRowsSource.setText(
        pipelineExecutorMeta.getOutputRowsSourceTransformMeta() == null
            ? ""
            : pipelineExecutorMeta.getOutputRowsSourceTransformMeta().getName());
    for (int i = 0; i < pipelineExecutorMeta.getResultRows().size(); i++) {
      TableItem item = new TableItem(wOutputFields.table, SWT.NONE);
      item.setText(1, Const.NVL(pipelineExecutorMeta.getResultRows().get(i).getName(), ""));
      item.setText(2, pipelineExecutorMeta.getResultRows().get(i).getType());
      int length = pipelineExecutorMeta.getResultRows().get(i).getLength();
      item.setText(3, length < 0 ? "" : Integer.toString(length));
      int precision = pipelineExecutorMeta.getResultRows().get(i).getPrecision();
      item.setText(4, precision < 0 ? "" : Integer.toString(precision));
    }
    wOutputFields.removeEmptyRows();
    wOutputFields.setRowNums();
    wOutputFields.optWidth(true);

    wTabFolder.setSelection(0);

    try {
      loadPipeline();
    } catch (Throwable t) {
      // Ignore errors
    }

    setFlags();

    wTransformName.selectAll();
    wTransformName.setFocus();
  }

  private void addParametersTab() {
    CTabItem wParametersTab = new CTabItem(wTabFolder, SWT.NONE);
    wParametersTab.setFont(GuiResource.getInstance().getFontDefault());
    wParametersTab.setText(BaseMessages.getString(PKG, "PipelineExecutorDialog.Parameters.Title"));
    wParametersTab.setToolTipText(
        BaseMessages.getString(PKG, "PipelineExecutorDialog.Parameters.Tooltip"));

    Composite wParametersComposite = new Composite(wTabFolder, SWT.NONE);
    PropsUi.setLook(wParametersComposite);

    FormLayout parameterTabLayout = new FormLayout();
    parameterTabLayout.marginWidth = 15;
    parameterTabLayout.marginHeight = 15;
    wParametersComposite.setLayout(parameterTabLayout);

    // Add a button: get parameters
    //
    Button wGetParameters = new Button(wParametersComposite, SWT.PUSH);
    wGetParameters.setText(
        BaseMessages.getString(PKG, "PipelineExecutorDialog.Parameters.GetParameters"));
    PropsUi.setLook(wGetParameters);
    FormData fdGetParameters = new FormData();
    fdGetParameters.bottom = new FormAttachment(100, 0);
    fdGetParameters.right = new FormAttachment(100, 0);
    wGetParameters.setLayoutData(fdGetParameters);
    wGetParameters.setSelection(pipelineExecutorMeta.isInheritingAllVariables());
    wGetParameters.addListener(SWT.Selection, e -> getParametersFromPipeline());

    // Add a button: get parameters
    //
    Button wMapParameters = new Button(wParametersComposite, SWT.PUSH);
    wMapParameters.setText(
        BaseMessages.getString(PKG, "PipelineExecutorDialog.Parameters.MapParameters"));
    PropsUi.setLook(wMapParameters);
    FormData fdMapParameters = new FormData();
    fdMapParameters.bottom = new FormAttachment(100, 0);
    fdMapParameters.right = new FormAttachment(wGetParameters, -PropsUi.getMargin());
    wMapParameters.setLayoutData(fdMapParameters);
    wMapParameters.setSelection(pipelineExecutorMeta.isInheritingAllVariables());
    wMapParameters.addListener(SWT.Selection, e -> mapFieldsToPipelineParameters());

    // Now add a table view with the 3 columns to specify: variable name, input field & optional
    // static input
    //
    parameterColumns =
        new ColumnInfo[] {
          new ColumnInfo(
              BaseMessages.getString(PKG, "PipelineExecutorDialog.Parameters.column.Variable"),
              ColumnInfo.COLUMN_TYPE_TEXT,
              false,
              false),
          new ColumnInfo(
              BaseMessages.getString(PKG, "PipelineExecutorDialog.Parameters.column.Field"),
              ColumnInfo.COLUMN_TYPE_CCOMBO,
              new String[] {},
              false),
          new ColumnInfo(
              BaseMessages.getString(PKG, "PipelineExecutorDialog.Parameters.column.Input"),
              ColumnInfo.COLUMN_TYPE_TEXT,
              false,
              false),
        };
    parameterColumns[1].setUsingVariables(true);

    List<PipelineExecutorParameters> parameters = pipelineExecutorMeta.getParameters();
    wPipelineExecutorParameters =
        new TableView(
            variables,
            wParametersComposite,
            SWT.FULL_SELECTION | SWT.MULTI | SWT.BORDER,
            parameterColumns,
            parameters.size(),
            false,
            null,
            props,
            false);
    PropsUi.setLook(wPipelineExecutorParameters);
    FormData fdPipelineExecutors = new FormData();
    fdPipelineExecutors.left = new FormAttachment(0, 0);
    fdPipelineExecutors.right = new FormAttachment(100, 0);
    fdPipelineExecutors.top = new FormAttachment(0, 0);
    fdPipelineExecutors.bottom = new FormAttachment(wGetParameters, -10);
    wPipelineExecutorParameters.setLayoutData(fdPipelineExecutors);

    for (int i = 0; i < parameters.size(); i++) {
      TableItem tableItem = wPipelineExecutorParameters.table.getItem(i);
      tableItem.setText(1, Const.NVL(parameters.get(i).getVariable(), ""));
      tableItem.setText(2, Const.NVL(parameters.get(i).getField(), ""));
      tableItem.setText(3, Const.NVL(parameters.get(i).getInput(), ""));
    }
    wPipelineExecutorParameters.setRowNums();
    wPipelineExecutorParameters.optWidth(true);

    // Add a checkbox: inherit all variables...
    //
    wInheritAll = new Button(wParametersComposite, SWT.CHECK);
    wInheritAll.setText(
        BaseMessages.getString(PKG, "PipelineExecutorDialog.Parameters.InheritAll"));
    PropsUi.setLook(wInheritAll);
    FormData fdInheritAll = new FormData();
    fdInheritAll.top = new FormAttachment(wPipelineExecutorParameters, 15);
    fdInheritAll.left = new FormAttachment(0, 0);
    wInheritAll.setLayoutData(fdInheritAll);
    wInheritAll.setSelection(pipelineExecutorMeta.isInheritingAllVariables());

    FormData fdParametersComposite = new FormData();
    fdParametersComposite.left = new FormAttachment(0, 0);
    fdParametersComposite.top = new FormAttachment(0, 0);
    fdParametersComposite.right = new FormAttachment(100, 0);
    fdParametersComposite.bottom = new FormAttachment(100, 0);
    wParametersComposite.setLayoutData(fdParametersComposite);

    wParametersComposite.layout();
    wParametersTab.setControl(wParametersComposite);
  }

  /** Get parameters from the specified pipeline */
  protected void getParametersFromPipeline() {
    try {
      // Load the specified pipeline metadata in executorPipelineMeta
      //
      loadPipeline();
      String[] parameters = executorPipelineMeta.listParameters();
      for (String name : parameters) {
        TableItem item = new TableItem(wPipelineExecutorParameters.table, SWT.NONE);
        item.setText(1, Const.NVL(name, ""));
      }
      wPipelineExecutorParameters.removeEmptyRows();
      wPipelineExecutorParameters.setRowNums();
      wPipelineExecutorParameters.optWidth(true);

    } catch (Exception e) {
      new ErrorDialog(
          shell,
          BaseMessages.getString(PKG, "PipelineExecutorDialog.ErrorLoadingSpecifiedPipeline.Title"),
          BaseMessages.getString(
              PKG, "PipelineExecutorDialog.ErrorLoadingSpecifiedPipeline.Message"),
          e);
    }
  }

  protected void mapFieldsToPipelineParameters() {
    try {
      // The field names
      //
      IRowMeta inputFields = pipelineMeta.getPrevTransformFields(variables, transformMeta);
      String[] inputFieldNames = inputFields.getFieldNames();

      loadPipeline();
      String[] parameters = executorPipelineMeta.listParameters();

      // Get the current mapping...
      //
      List<SourceToTargetMapping> mappings = new ArrayList<>();
      for (TableItem item : wPipelineExecutorParameters.getNonEmptyItems()) {
        int sourceIndex = Const.indexOfString(item.getText(1), parameters);
        int targetIndex = Const.indexOfString(item.getText(2), inputFieldNames);
        if (sourceIndex >= 0 && targetIndex >= 0) {
          SourceToTargetMapping mapping = new SourceToTargetMapping(sourceIndex, targetIndex);
          mappings.add(mapping);
        }
      }

      // Now we can ask for the mapping...
      //
      EnterMappingDialog enterMappingDialog =
          new EnterMappingDialog(shell, inputFieldNames, parameters, mappings);
      mappings = enterMappingDialog.open();
      if (mappings != null) {
        wPipelineExecutorParameters.removeAll();

        for (SourceToTargetMapping mapping : mappings) {
          TableItem item = new TableItem(wPipelineExecutorParameters.table, SWT.NONE);
          item.setText(1, Const.NVL(mapping.getTargetString(parameters), ""));
          item.setText(2, Const.NVL(mapping.getSourceString(inputFieldNames), ""));
          item.setText(3, "");
        }

        wPipelineExecutorParameters.removeEmptyRows();
        wPipelineExecutorParameters.setRowNums();
        wPipelineExecutorParameters.optWidth(true);
      }
    } catch (Exception e) {
      new ErrorDialog(
          shell,
          BaseMessages.getString(PKG, "WorkflowExecutorDialog.ErrorLoadingSpecifiedJob.Title"),
          BaseMessages.getString(PKG, "WorkflowExecutorDialog.ErrorLoadingSpecifiedJob.Message"),
          e);
    }
  }

  private void activePipelineNameField() {
    wlPipelineNameField.setEnabled(wbPipelineNameInField.getSelection());
    wPipelineNameField.setEnabled(wbPipelineNameInField.getSelection());
    wPath.setEnabled(!wbPipelineNameInField.getSelection());
    wlPath.setEnabled(!wbPipelineNameInField.getSelection());
    if (wbPipelineNameInField.getSelection()) {
      wPath.setText("");
    } else {
      wPipelineNameField.setText("");
    }
  }

  private void addRowGroupTab() {

    final CTabItem wTab = new CTabItem(wTabFolder, SWT.NONE);
    wTab.setFont(GuiResource.getInstance().getFontDefault());
    wTab.setText(BaseMessages.getString(PKG, "PipelineExecutorDialog.RowGroup.Title"));
    wTab.setToolTipText(BaseMessages.getString(PKG, "PipelineExecutorDialog.RowGroup.Tooltip"));

    Composite wInputComposite = new Composite(wTabFolder, SWT.NONE);
    PropsUi.setLook(wInputComposite);

    FormLayout tabLayout = new FormLayout();
    tabLayout.marginWidth = 15;
    tabLayout.marginHeight = 15;
    wInputComposite.setLayout(tabLayout);

    // Group size
    //
    wlGroupSize = new Label(wInputComposite, SWT.RIGHT);
    PropsUi.setLook(wlGroupSize);
    wlGroupSize.setText(BaseMessages.getString(PKG, "PipelineExecutorDialog.GroupSize.Label"));
    wlGroupSize.setToolTipText(
        BaseMessages.getString(PKG, "PipelineExecutorDialog.GroupSize.Tooltip"));
    FormData fdlGroupSize = new FormData();
    fdlGroupSize.top = new FormAttachment(0, 0);
    fdlGroupSize.left = new FormAttachment(0, 0);
    fdlGroupSize.right = new FormAttachment(middle, -margin);
    wlGroupSize.setLayoutData(fdlGroupSize);

    wGroupSize = new TextVar(variables, wInputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    PropsUi.setLook(wGroupSize);
    FormData fdGroupSize = new FormData();
    fdGroupSize.right = new FormAttachment(100);
    fdGroupSize.top = new FormAttachment(wlGroupSize, 0, SWT.CENTER);
    fdGroupSize.left = new FormAttachment(middle, 0);
    wGroupSize.setLayoutData(fdGroupSize);
    // Enable/Disable fields base on the size of the group
    wGroupSize.addListener(SWT.Modify, e -> setFlags());

    // Group field
    //
    wlGroupField = new Label(wInputComposite, SWT.RIGHT);
    PropsUi.setLook(wlGroupField);
    wlGroupField.setText(BaseMessages.getString(PKG, "PipelineExecutorDialog.GroupField.Label"));
    FormData fdlGroupField = new FormData();
    fdlGroupField.top = new FormAttachment(wGroupSize, 10);
    fdlGroupField.left = new FormAttachment(0, 0);
    fdlGroupField.right = new FormAttachment(middle, -margin);
    wlGroupField.setLayoutData(fdlGroupField);

    wGroupField = new CCombo(wInputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    PropsUi.setLook(wGroupField);
    FormData fdGroupField = new FormData();
    fdGroupField.right = new FormAttachment(100);
    fdGroupField.top = new FormAttachment(wlGroupField, 0, SWT.CENTER);
    fdGroupField.left = new FormAttachment(middle, 0);
    wGroupField.setLayoutData(fdGroupField);
    // Enable/Disable widgets when this field changes
    wGroupField.addListener(SWT.Modify, e -> setFlags());

    // Group time
    //
    wlGroupTime = new Label(wInputComposite, SWT.RIGHT);
    PropsUi.setLook(wlGroupTime);
    wlGroupTime.setText(BaseMessages.getString(PKG, "PipelineExecutorDialog.GroupTime.Label"));
    FormData fdlGroupTime = new FormData();
    fdlGroupTime.top = new FormAttachment(wGroupField, 10);
    fdlGroupTime.left = new FormAttachment(0, 0);
    fdlGroupTime.right = new FormAttachment(middle, -margin);
    wlGroupTime.setLayoutData(fdlGroupTime);

    wGroupTime = new TextVar(variables, wInputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    PropsUi.setLook(wGroupTime);
    FormData fdGroupTime = new FormData();
    fdGroupTime.right = new FormAttachment(100);
    fdGroupTime.top = new FormAttachment(wlGroupTime, 0, SWT.CENTER);
    fdGroupTime.left = new FormAttachment(middle, 0);
    wGroupTime.setLayoutData(fdGroupTime);

    wTab.setControl(wInputComposite);
    wTabFolder.setSelection(wTab);
  }

  private void addExecutionResultTab() {

    final CTabItem wTab = new CTabItem(wTabFolder, SWT.NONE);
    wTab.setFont(GuiResource.getInstance().getFontDefault());
    wTab.setText(BaseMessages.getString(PKG, "PipelineExecutorDialog.ExecutionResults.Title"));
    wTab.setToolTipText(
        BaseMessages.getString(PKG, "PipelineExecutorDialog.ExecutionResults.Tooltip"));

    ScrolledComposite scrolledComposite =
        new ScrolledComposite(wTabFolder, SWT.V_SCROLL | SWT.H_SCROLL);
    scrolledComposite.setLayout(new FillLayout());

    Composite wInputComposite = new Composite(scrolledComposite, SWT.NONE);
    PropsUi.setLook(wInputComposite);

    FormLayout tabLayout = new FormLayout();
    tabLayout.marginWidth = 15;
    tabLayout.marginHeight = 15;
    wInputComposite.setLayout(tabLayout);

    Label wlExecutionResultTarget = new Label(wInputComposite, SWT.RIGHT);
    PropsUi.setLook(wlExecutionResultTarget);
    wlExecutionResultTarget.setText(
        BaseMessages.getString(PKG, "PipelineExecutorDialog.ExecutionResultTarget.Label"));
    FormData fdlExecutionResultTarget = new FormData();
    fdlExecutionResultTarget.top = new FormAttachment(0, 0);
    fdlExecutionResultTarget.right = new FormAttachment(middle, -margin);
    wlExecutionResultTarget.setLayoutData(fdlExecutionResultTarget);

    wExecutionResultTarget = new CCombo(wInputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    PropsUi.setLook(wExecutionResultTarget);
    FormData fdExecutionResultTarget = new FormData();
    fdExecutionResultTarget.right = new FormAttachment(100);
    fdExecutionResultTarget.top = new FormAttachment(wlExecutionResultTarget, 0, SWT.CENTER);
    fdExecutionResultTarget.left = new FormAttachment(middle, 0);
    wExecutionResultTarget.setLayoutData(fdExecutionResultTarget);

    ColumnInfo[] executionResultColumns =
        new ColumnInfo[] {
          new ColumnInfo(
              BaseMessages.getString(
                  PKG, "PipelineExecutorMeta.ExecutionResults.FieldDescription.Label"),
              ColumnInfo.COLUMN_TYPE_TEXT,
              false,
              true),
          new ColumnInfo(
              BaseMessages.getString(PKG, "PipelineExecutorMeta.ExecutionResults.FieldName.Label"),
              ColumnInfo.COLUMN_TYPE_TEXT,
              false,
              false)
        };
    executionResultColumns[1].setUsingVariables(true);

    TableView wExectionResults =
        new TableView(
            variables,
            wInputComposite,
            SWT.FULL_SELECTION | SWT.SINGLE | SWT.BORDER,
            executionResultColumns,
            14,
            false,
            null,
            props,
            false);
    PropsUi.setLook(wExectionResults);
    FormData fdExecutionResults = new FormData();
    fdExecutionResults.left = new FormAttachment(0);
    fdExecutionResults.right = new FormAttachment(100);
    fdExecutionResults.top = new FormAttachment(wExecutionResultTarget, 10);
    fdExecutionResults.bottom = new FormAttachment(100);
    wExectionResults.setLayoutData(fdExecutionResults);
    wExectionResults.getTable().addListener(SWT.Resize, new ColumnsResizer(0, 50, 50));

    int index = 0;
    tiExecutionTimeField = wExectionResults.table.getItem(index++);
    tiExecutionResultField = wExectionResults.table.getItem(index++);
    tiExecutionNrErrorsField = wExectionResults.table.getItem(index++);
    tiExecutionLinesReadField = wExectionResults.table.getItem(index++);
    tiExecutionLinesWrittenField = wExectionResults.table.getItem(index++);
    tiExecutionLinesInputField = wExectionResults.table.getItem(index++);
    tiExecutionLinesOutputField = wExectionResults.table.getItem(index++);
    tiExecutionLinesRejectedField = wExectionResults.table.getItem(index++);
    tiExecutionLinesUpdatedField = wExectionResults.table.getItem(index++);
    tiExecutionLinesDeletedField = wExectionResults.table.getItem(index++);
    tiExecutionFilesRetrievedField = wExectionResults.table.getItem(index++);
    tiExecutionExitStatusField = wExectionResults.table.getItem(index++);
    tiExecutionLogTextField = wExectionResults.table.getItem(index++);
    tiExecutionLogChannelIdField = wExectionResults.table.getItem(index);

    tiExecutionTimeField.setText(
        FIELD_DESCRIPTION,
        BaseMessages.getString(PKG, "PipelineExecutorDialog.ExecutionTimeField.Label"));
    tiExecutionResultField.setText(
        FIELD_DESCRIPTION,
        BaseMessages.getString(PKG, "PipelineExecutorDialog.ExecutionResultField.Label"));
    tiExecutionNrErrorsField.setText(
        FIELD_DESCRIPTION,
        BaseMessages.getString(PKG, "PipelineExecutorDialog.ExecutionNrErrorsField.Label"));
    tiExecutionLinesReadField.setText(
        FIELD_DESCRIPTION,
        BaseMessages.getString(PKG, "PipelineExecutorDialog.ExecutionLinesReadField.Label"));
    tiExecutionLinesWrittenField.setText(
        FIELD_DESCRIPTION,
        BaseMessages.getString(PKG, "PipelineExecutorDialog.ExecutionLinesWrittenField.Label"));
    tiExecutionLinesInputField.setText(
        FIELD_DESCRIPTION,
        BaseMessages.getString(PKG, "PipelineExecutorDialog.ExecutionLinesInputField.Label"));
    tiExecutionLinesOutputField.setText(
        FIELD_DESCRIPTION,
        BaseMessages.getString(PKG, "PipelineExecutorDialog.ExecutionLinesOutputField.Label"));
    tiExecutionLinesRejectedField.setText(
        FIELD_DESCRIPTION,
        BaseMessages.getString(PKG, "PipelineExecutorDialog.ExecutionLinesRejectedField.Label"));
    tiExecutionLinesUpdatedField.setText(
        FIELD_DESCRIPTION,
        BaseMessages.getString(PKG, "PipelineExecutorDialog.ExecutionLinesUpdatedField.Label"));
    tiExecutionLinesDeletedField.setText(
        FIELD_DESCRIPTION,
        BaseMessages.getString(PKG, "PipelineExecutorDialog.ExecutionLinesDeletedField.Label"));
    tiExecutionFilesRetrievedField.setText(
        FIELD_DESCRIPTION,
        BaseMessages.getString(PKG, "PipelineExecutorDialog.ExecutionFilesRetrievedField.Label"));
    tiExecutionExitStatusField.setText(
        FIELD_DESCRIPTION,
        BaseMessages.getString(PKG, "PipelineExecutorDialog.ExecutionExitStatusField.Label"));
    tiExecutionLogTextField.setText(
        FIELD_DESCRIPTION,
        BaseMessages.getString(PKG, "PipelineExecutorDialog.ExecutionLogTextField.Label"));
    tiExecutionLogChannelIdField.setText(
        FIELD_DESCRIPTION,
        BaseMessages.getString(PKG, "PipelineExecutorDialog.ExecutionLogChannelIdField.Label"));

    wPipelineExecutorParameters.setRowNums();
    wPipelineExecutorParameters.optWidth(true);

    wInputComposite.pack();
    Rectangle bounds = wInputComposite.getBounds();

    scrolledComposite.setContent(wInputComposite);
    scrolledComposite.setExpandHorizontal(true);
    scrolledComposite.setExpandVertical(true);
    scrolledComposite.setMinWidth(bounds.width);
    scrolledComposite.setMinHeight(bounds.height);

    wTab.setControl(scrolledComposite);
    wTabFolder.setSelection(wTab);
  }

  private void addResultFilesTab() {

    final CTabItem wTab = new CTabItem(wTabFolder, SWT.NONE);
    wTab.setFont(GuiResource.getInstance().getFontDefault());
    wTab.setText(BaseMessages.getString(PKG, "PipelineExecutorDialog.ResultFiles.Title"));
    wTab.setToolTipText(BaseMessages.getString(PKG, "PipelineExecutorDialog.ResultFiles.Tooltip"));

    ScrolledComposite scrolledComposite =
        new ScrolledComposite(wTabFolder, SWT.V_SCROLL | SWT.H_SCROLL);
    scrolledComposite.setLayout(new FillLayout());

    Composite wInputComposite = new Composite(scrolledComposite, SWT.NONE);
    PropsUi.setLook(wInputComposite);

    FormLayout tabLayout = new FormLayout();
    tabLayout.marginWidth = 15;
    tabLayout.marginHeight = 15;
    wInputComposite.setLayout(tabLayout);

    Label wlResultFilesTarget = new Label(wInputComposite, SWT.RIGHT);
    PropsUi.setLook(wlResultFilesTarget);
    wlResultFilesTarget.setText(
        BaseMessages.getString(PKG, "PipelineExecutorDialog.ResultFilesTarget.Label"));
    FormData fdlResultFilesTarget = new FormData();
    fdlResultFilesTarget.top = new FormAttachment(0, 0);
    fdlResultFilesTarget.left = new FormAttachment(0, 0);
    fdlResultFilesTarget.right = new FormAttachment(middle, -margin);
    wlResultFilesTarget.setLayoutData(fdlResultFilesTarget);

    wResultFilesTarget = new CCombo(wInputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    PropsUi.setLook(wResultFilesTarget);
    FormData fdResultFilesTarget = new FormData();
    fdResultFilesTarget.right = new FormAttachment(100);
    fdResultFilesTarget.top = new FormAttachment(wlResultFilesTarget, 0, SWT.CENTER);
    fdResultFilesTarget.left = new FormAttachment(middle, 0);
    wResultFilesTarget.setLayoutData(fdResultFilesTarget);

    // ResultFileNameField
    //
    Label wlResultFileNameField = new Label(wInputComposite, SWT.RIGHT);
    PropsUi.setLook(wlResultFileNameField);
    wlResultFileNameField.setText(
        BaseMessages.getString(PKG, "PipelineExecutorDialog.ResultFileNameField.Label"));
    FormData fdlResultFileNameField = new FormData();
    fdlResultFileNameField.top = new FormAttachment(wResultFilesTarget, margin * 2);
    fdlResultFileNameField.left = new FormAttachment(0, 0);
    fdlResultFileNameField.right = new FormAttachment(middle, -margin);
    wlResultFileNameField.setLayoutData(fdlResultFileNameField);

    wResultFileNameField =
        new TextVar(variables, wInputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    PropsUi.setLook(wResultFileNameField);
    FormData fdResultFileNameField = new FormData();
    fdResultFileNameField.width = 250;
    fdResultFileNameField.top = new FormAttachment(wlResultFileNameField, 0, SWT.CENTER);
    fdResultFileNameField.left = new FormAttachment(middle, 0);
    fdResultFileNameField.right = new FormAttachment(100);
    wResultFileNameField.setLayoutData(fdResultFileNameField);

    wInputComposite.pack();
    Rectangle bounds = wInputComposite.getBounds();

    scrolledComposite.setContent(wInputComposite);
    scrolledComposite.setExpandHorizontal(true);
    scrolledComposite.setExpandVertical(true);
    scrolledComposite.setMinWidth(bounds.width);
    scrolledComposite.setMinHeight(bounds.height);

    wTab.setControl(scrolledComposite);
    wTabFolder.setSelection(wTab);
  }

  private void addResultRowsTab() {

    final CTabItem wTab = new CTabItem(wTabFolder, SWT.NONE);
    wTab.setFont(GuiResource.getInstance().getFontDefault());
    wTab.setText(BaseMessages.getString(PKG, "PipelineExecutorDialog.ResultRows.Title"));
    wTab.setToolTipText(BaseMessages.getString(PKG, "PipelineExecutorDialog.ResultRows.Tooltip"));

    ScrolledComposite scrolledComposite =
        new ScrolledComposite(wTabFolder, SWT.V_SCROLL | SWT.H_SCROLL);
    scrolledComposite.setLayout(new FillLayout());

    Composite wInputComposite = new Composite(scrolledComposite, SWT.NONE);
    PropsUi.setLook(wInputComposite);

    FormLayout tabLayout = new FormLayout();
    tabLayout.marginWidth = 15;
    tabLayout.marginHeight = 15;
    wInputComposite.setLayout(tabLayout);

    Label wlResultRowsTarget = new Label(wInputComposite, SWT.RIGHT);
    PropsUi.setLook(wlResultRowsTarget);
    wlResultRowsTarget.setText(
        BaseMessages.getString(PKG, "PipelineExecutorDialog.OutputRowsSource.Label"));
    FormData fdlResultRowsTarget = new FormData();
    fdlResultRowsTarget.top = new FormAttachment(0, 0);
    fdlResultRowsTarget.left = new FormAttachment(0, 0);
    fdlResultRowsTarget.right = new FormAttachment(middle, -margin);
    wlResultRowsTarget.setLayoutData(fdlResultRowsTarget);

    wOutputRowsSource = new CCombo(wInputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    PropsUi.setLook(wOutputRowsSource);
    FormData fdResultRowsTarget = new FormData();
    fdResultRowsTarget.right = new FormAttachment(100);
    fdResultRowsTarget.top = new FormAttachment(wlResultRowsTarget, 0, SWT.CENTER);
    fdResultRowsTarget.left = new FormAttachment(middle, 0);
    wOutputRowsSource.setLayoutData(fdResultRowsTarget);

    Label wlOutputFields = new Label(wInputComposite, SWT.NONE);
    wlOutputFields.setText(
        BaseMessages.getString(PKG, "PipelineExecutorDialog.ResultFields.Label"));
    PropsUi.setLook(wlOutputFields);
    FormData fdlResultFields = new FormData();
    fdlResultFields.left = new FormAttachment(0, 0);
    fdlResultFields.top = new FormAttachment(wOutputRowsSource, 10);
    wlOutputFields.setLayoutData(fdlResultFields);

    int nrRows =
        (pipelineExecutorMeta.getResultRows() != null
            ? pipelineExecutorMeta.getResultRows().size()
            : 1);

    ColumnInfo[] ciResultFields =
        new ColumnInfo[] {
          new ColumnInfo(
              BaseMessages.getString(PKG, "PipelineExecutorDialog.ColumnInfo.Field"),
              ColumnInfo.COLUMN_TYPE_TEXT,
              false,
              false),
          new ColumnInfo(
              BaseMessages.getString(PKG, "PipelineExecutorDialog.ColumnInfo.Type"),
              ColumnInfo.COLUMN_TYPE_CCOMBO,
              ValueMetaFactory.getValueMetaNames()),
          new ColumnInfo(
              BaseMessages.getString(PKG, "PipelineExecutorDialog.ColumnInfo.Length"),
              ColumnInfo.COLUMN_TYPE_TEXT,
              false),
          new ColumnInfo(
              BaseMessages.getString(PKG, "PipelineExecutorDialog.ColumnInfo.Precision"),
              ColumnInfo.COLUMN_TYPE_TEXT,
              false),
        };

    wOutputFields =
        new TableView(
            variables,
            wInputComposite,
            SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL,
            ciResultFields,
            nrRows,
            false,
            null,
            props,
            false);

    FormData fdResultFields = new FormData();
    fdResultFields.left = new FormAttachment(0, 0);
    fdResultFields.top = new FormAttachment(wlOutputFields, 5);
    fdResultFields.right = new FormAttachment(100, 0);
    fdResultFields.bottom = new FormAttachment(100, 0);
    wOutputFields.setLayoutData(fdResultFields);
    wOutputFields.getTable().addListener(SWT.Resize, new ColumnsResizer(0, 25, 25, 25, 25));

    wInputComposite.pack();
    Rectangle bounds = wInputComposite.getBounds();

    scrolledComposite.setContent(wInputComposite);
    scrolledComposite.setExpandHorizontal(true);
    scrolledComposite.setExpandVertical(true);
    scrolledComposite.setMinWidth(bounds.width);
    scrolledComposite.setMinHeight(bounds.height);

    wTab.setControl(scrolledComposite);
    wTabFolder.setSelection(wTab);
  }

  private void setFlags() {
    // Enable/disable fields...
    //
    if (wlGroupSize == null
        || wlGroupField == null
        || wGroupField == null
        || wlGroupTime == null
        || wGroupTime == null) {
      return;
    }
    boolean enableSize = Const.toInt(variables.resolve(wGroupSize.getText()), -1) >= 0;
    boolean enableField = !Utils.isEmpty(wGroupField.getText());

    wlGroupSize.setEnabled(true);
    wGroupSize.setEnabled(true);
    wlGroupField.setEnabled(!enableSize);
    wGroupField.setEnabled(!enableSize);
    wlGroupTime.setEnabled(!enableSize && !enableField);
    wGroupTime.setEnabled(!enableSize && !enableField);
  }

  private void cancel() {
    transformName = null;
    pipelineExecutorMeta.setChanged(changed);
    dispose();
  }

  private void ok() {
    if (Utils.isEmpty(wTransformName.getText())) {
      return;
    }

    transformName = wTransformName.getText(); // return value

    // No check if the pipeline to be executed comes from the stream field.
    if (!wbPipelineNameInField.getSelection()) {

      if (Utils.isEmpty(wPath.getText())) {
        MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
        mb.setText(BaseMessages.getString(PKG, "PipelineExecutorDialog.FilenameMissing.Header"));
        mb.setMessage(
            BaseMessages.getString(PKG, "PipelineExecutorDialog.FilenameMissing.Message"));
        mb.open();
        return;
      }

      if (isSelfReferencing()) {
        MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
        mb.setText(BaseMessages.getString(PKG, "PipelineExecutorDialog.SelfReference.Header"));
        mb.setMessage(BaseMessages.getString(PKG, "PipelineExecutorDialog.SelfReference.Message"));
        mb.open();
        return;
      }

      try {
        loadPipeline();
      } catch (HopException e) {
        new ErrorDialog(
            shell,
            BaseMessages.getString(
                PKG, "PipelineExecutorDialog.ErrorLoadingSpecifiedPipeline.Title"),
            BaseMessages.getString(
                PKG, "PipelineExecutorDialog.ErrorLoadingSpecifiedPipeline.Message"),
            e);
      }
    }

    pipelineExecutorMeta.setFilename(wPath.getText());
    pipelineExecutorMeta.setFilenameInField(wbPipelineNameInField.getSelection());
    pipelineExecutorMeta.setFilenameField(wPipelineNameField.getText());
    pipelineExecutorMeta.setRunConfigurationName(wRunConfiguration.getText());

    // Load the information on the tabs, optionally do some
    // verifications...
    //
    collectInformation();

    // Set the input transforms for input mappings
    pipelineExecutorMeta.searchInfoAndTargetTransforms(pipelineMeta.getTransforms());

    pipelineExecutorMeta.setChanged(true);

    dispose();
  }

  private void collectInformation() {
    // The parameters...
    //
    List<PipelineExecutorParameters> parameters = new ArrayList<>();

    int nrLines = wPipelineExecutorParameters.nrNonEmpty();
    for (int i = 0; i < nrLines; i++) {
      PipelineExecutorParameters params = new PipelineExecutorParameters();
      TableItem item = wPipelineExecutorParameters.getNonEmpty(i);
      params.setVariable(item.getText(1));
      params.setField(item.getText(2));
      params.setInput(item.getText(3));
      parameters.add(params);
    }
    pipelineExecutorMeta.setParameters(parameters);
    pipelineExecutorMeta.setInheritingAllVariables(wInheritAll.getSelection());

    // The group definition
    //
    pipelineExecutorMeta.setGroupSize(wGroupSize.getText());
    pipelineExecutorMeta.setGroupField(wGroupField.getText());
    pipelineExecutorMeta.setGroupTime(wGroupTime.getText());

    pipelineExecutorMeta.setExecutionResultTargetTransform(wExecutionResultTarget.getText());
    pipelineExecutorMeta.setExecutionResultTargetTransformMeta(
        pipelineMeta.findTransform(wExecutionResultTarget.getText()));
    pipelineExecutorMeta.setExecutionTimeField(tiExecutionTimeField.getText(FIELD_NAME));
    pipelineExecutorMeta.setExecutionResultField(tiExecutionResultField.getText(FIELD_NAME));
    pipelineExecutorMeta.setExecutionNrErrorsField(tiExecutionNrErrorsField.getText(FIELD_NAME));
    pipelineExecutorMeta.setExecutionLinesReadField(tiExecutionLinesReadField.getText(FIELD_NAME));
    pipelineExecutorMeta.setExecutionLinesWrittenField(
        tiExecutionLinesWrittenField.getText(FIELD_NAME));
    pipelineExecutorMeta.setExecutionLinesInputField(
        tiExecutionLinesInputField.getText(FIELD_NAME));
    pipelineExecutorMeta.setExecutionLinesOutputField(
        tiExecutionLinesOutputField.getText(FIELD_NAME));
    pipelineExecutorMeta.setExecutionLinesRejectedField(
        tiExecutionLinesRejectedField.getText(FIELD_NAME));
    pipelineExecutorMeta.setExecutionLinesUpdatedField(
        tiExecutionLinesUpdatedField.getText(FIELD_NAME));
    pipelineExecutorMeta.setExecutionLinesDeletedField(
        tiExecutionLinesDeletedField.getText(FIELD_NAME));
    pipelineExecutorMeta.setExecutionFilesRetrievedField(
        tiExecutionFilesRetrievedField.getText(FIELD_NAME));
    pipelineExecutorMeta.setExecutionExitStatusField(
        tiExecutionExitStatusField.getText(FIELD_NAME));
    pipelineExecutorMeta.setExecutionLogTextField(tiExecutionLogTextField.getText(FIELD_NAME));
    pipelineExecutorMeta.setExecutionLogChannelIdField(
        tiExecutionLogChannelIdField.getText(FIELD_NAME));

    pipelineExecutorMeta.setResultFilesTargetTransform(wResultFilesTarget.getText());
    pipelineExecutorMeta.setResultFilesTargetTransformMeta(
        pipelineMeta.findTransform(wResultFilesTarget.getText()));
    pipelineExecutorMeta.setResultFilesFileNameField(wResultFileNameField.getText());

    if (!Utils.isEmpty(executorOutputTransform)) {
      pipelineExecutorMeta.setExecutorsOutputTransform(executorOutputTransform);
      pipelineExecutorMeta.setExecutorsOutputTransformMeta(
          pipelineMeta.findTransform(executorOutputTransform));
    }

    // Result row info
    //
    pipelineExecutorMeta.setOutputRowsSourceTransform(wOutputRowsSource.getText());
    pipelineExecutorMeta.setOutputRowsSourceTransformMeta(
        pipelineMeta.findTransform(wOutputRowsSource.getText()));
    int nrFields = wOutputFields.nrNonEmpty();
    List<PipelineExecutorResultRows> resultRows = new ArrayList<>();
    for (int i = 0; i < nrFields; i++) {
      TableItem item = wOutputFields.getNonEmpty(i);
      PipelineExecutorResultRows pipelineExecutorResultRows = new PipelineExecutorResultRows();
      pipelineExecutorResultRows.setName(item.getText(1));
      pipelineExecutorResultRows.setType(item.getText(2));
      pipelineExecutorResultRows.setLength(Const.toInt(item.getText(3), -1));
      pipelineExecutorResultRows.setPrecision(Const.toInt(item.getText(4), -1));
      resultRows.add(pipelineExecutorResultRows);
    }
    pipelineExecutorMeta.setResultRows(resultRows);
  }

  private boolean isSelfReferencing() {
    return variables.resolve(wPath.getText()).equals(variables.resolve(pipelineMeta.getFilename()));
  }
}
