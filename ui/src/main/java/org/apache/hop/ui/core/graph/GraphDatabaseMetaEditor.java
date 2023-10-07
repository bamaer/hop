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

package org.apache.hop.ui.core.graph;

import org.apache.commons.lang.StringUtils;
import org.apache.hop.core.Const;
import org.apache.hop.core.Props;
import org.apache.hop.core.database.DatabaseMeta;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.graph.BaseGraphDatabaseMeta;
import org.apache.hop.core.graph.GraphDatabaseMeta;
import org.apache.hop.core.graph.GraphDatabasePluginType;
import org.apache.hop.core.graph.GraphDatabaseTestResults;
import org.apache.hop.core.graph.IGraphDatabase;
import org.apache.hop.core.gui.plugin.GuiPlugin;
import org.apache.hop.core.plugins.IPlugin;
import org.apache.hop.core.plugins.PluginRegistry;
import org.apache.hop.core.util.Utils;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.ui.core.PropsUi;
import org.apache.hop.ui.core.dialog.MessageBox;
import org.apache.hop.ui.core.dialog.ShowMessageDialog;
import org.apache.hop.ui.core.gui.GuiCompositeWidgets;
import org.apache.hop.ui.core.gui.GuiCompositeWidgetsAdapter;
import org.apache.hop.ui.core.gui.GuiResource;
import org.apache.hop.ui.core.metadata.MetadataEditor;
import org.apache.hop.ui.core.metadata.MetadataManager;
import org.apache.hop.ui.core.widget.TableView;
import org.apache.hop.ui.core.widget.TextVar;
import org.apache.hop.ui.hopgui.HopGui;
import org.apache.hop.ui.hopgui.perspective.metadata.MetadataPerspective;
import org.apache.hop.ui.util.HelpUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/** Dialog that allows you to edit the settings of a Neo4j connection */
@GuiPlugin(description = "This is the editor for graph database connection metadata")
public class GraphDatabaseMetaEditor extends MetadataEditor<GraphDatabaseMeta> {
    private static final Class<?> PKG = GraphDatabaseMetaEditor.class; // for Translator2

    private CTabFolder wTabFolder;
    private Composite wBasicComp;
    private Composite wGraphDatabaseSpecificComp;
    private GuiCompositeWidgets guiCompositeWidgets;
    private Label wDriverInfo;
    private Combo wConnectionType;

    // Connection properties
    //
    private Label wlUsername;
    private Label wlPassword;
    private Text wName;
    private TextVar wUsername;
    private TextVar wPassword;

    private Label wlHostname;
    private TextVar wHostname;
    private Label wlDatabaseName;
    private TextVar wDatabaseName;
    private TableView wUrls;
    private PropsUi props;
    private int middle;
    private int margin;
    private Map<Class<? extends IGraphDatabase>, IGraphDatabase> metaMap;

    public GraphDatabaseMetaEditor(
            HopGui hopGui, MetadataManager<GraphDatabaseMeta> manager, GraphDatabaseMeta graphDatabaseMeta) {
        super(hopGui, manager, graphDatabaseMeta);
        props = PropsUi.getInstance();
        metaMap = populateMetaMap();
        metaMap.put(graphDatabaseMeta.getIGraphDatabase().getClass(), graphDatabaseMeta.getIGraphDatabase());
    }

    private Map<Class<? extends IGraphDatabase>, IGraphDatabase> populateMetaMap(){
        metaMap = new HashMap<>();
        List<IPlugin> plugins = PluginRegistry.getInstance().getPlugins(GraphDatabasePluginType.class);
        for (IPlugin plugin : plugins) {
            try {
                IGraphDatabase graphDatabase = (IGraphDatabase) PluginRegistry.getInstance().loadClass(plugin);
                graphDatabase.setPluginId(plugin.getIds()[0]);
                graphDatabase.setPluginName(plugin.getName());
                metaMap.put(graphDatabase.getClass(), graphDatabase);
            } catch (Exception e) {
                HopGui.getInstance().getLog().logError("Error instantiating graph database metadata", e);
            }
        }
        return metaMap;

    }

    @Override
    public void createControl(Composite composite) {

        middle = props.getMiddlePct();
        margin = PropsUi.getMargin() + 2;

        IVariables variables = getHopGui().getVariables();

        // The name
        Label wlName = new Label(composite, SWT.RIGHT);
        PropsUi.setLook(wlName);
        wlName.setText(BaseMessages.getString(PKG, "GraphConnectionDialog.Name.Label"));
        FormData fdlName = new FormData();
        fdlName.top = new FormAttachment(0, margin);
        fdlName.left = new FormAttachment(0, 0); // First one in the left top corner
        fdlName.right = new FormAttachment(middle, -margin);
        wlName.setLayoutData(fdlName);
        wName = new Text(composite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        PropsUi.setLook(wName);
        FormData fdName = new FormData();
        fdName.top = new FormAttachment(wlName, 0, SWT.CENTER);
        fdName.left = new FormAttachment(middle, 0); // To the right of the label
        fdName.right = new FormAttachment(95, 0);
        wName.setLayoutData(fdName);

        wTabFolder = new CTabFolder(composite, SWT.BORDER);
        FormData fdTabFolder = new FormData();
        fdTabFolder.left = new FormAttachment(0, 0);
        fdTabFolder.right = new FormAttachment(100, 0);
        fdTabFolder.top = new FormAttachment(wName, 2 * margin);
        fdTabFolder.bottom = new FormAttachment(100, -2 * margin);
        wTabFolder.setLayoutData(fdTabFolder);

        addBasicTab(props, variables, middle, margin);
        addProtocolTab(props, variables, middle, margin);
        addAdvancedTab(props, variables, middle, margin);
        addUrlsTab(props, variables);

        // Always select the basic tab
        wTabFolder.setSelection(0);

        setWidgetsContent();
        enableFields();

        clearChanged();

        Listener modifyListener = event -> {
            setChanged();
            MetadataPerspective.getInstance().updateEditor(this);
        };

        // Add modify listeners to all controls.
        // This will inform the Metadata perspective in the Hop GUI that this object was modified and
        // needs to be saved.
        //
        Control[] controls = {
                wName,
        };
        for (Control control : controls) {
            control.addListener(SWT.Modify, e -> setChanged());
            control.addListener(SWT.Selection, e -> setChanged());
        }
        wConnectionType.addListener(SWT.Modify, event -> changeConnectionType());
    }

    private void addBasicTab(PropsUi props, IVariables variables, int middle, int margin) {
        CTabItem wModelTab = new CTabItem(wTabFolder, SWT.NONE);
        wModelTab.setFont(GuiResource.getInstance().getFontDefault());
        wModelTab.setText("Basic   ");
        ScrolledComposite wBasicSComp = new ScrolledComposite(wTabFolder, SWT.V_SCROLL | SWT.H_SCROLL);
        wBasicSComp.setLayout(new FillLayout());

        wBasicComp = new Composite(wBasicSComp, SWT.NONE);
        PropsUi.setLook(wBasicComp);

        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = 3;
        formLayout.marginHeight = 3;
        wBasicComp.setLayout(formLayout);

        // What graph database type are we dealing with?
        Label wlGraphConnectionType = new Label(wBasicComp, SWT.RIGHT);
        PropsUi.setLook(wlGraphConnectionType);
        wlGraphConnectionType.setText(BaseMessages.getString(PKG, "GraphConnectionDialog.label.ConnectionType"));
        FormData fdlGraphConnectionType = new FormData();
        fdlGraphConnectionType.top = new FormAttachment(0, margin);
        fdlGraphConnectionType.left = new FormAttachment(0, 0);
        fdlGraphConnectionType.right = new FormAttachment(middle, -margin);
        wlGraphConnectionType.setLayoutData(fdlGraphConnectionType);

        ToolBar wToolbar = new ToolBar(wBasicComp, SWT.FLAT | SWT.HORIZONTAL);
        FormData fdToolbar = new FormData();
        fdToolbar.right = new FormAttachment(100, 0);
        fdToolbar.top = new FormAttachment(0, 0);
        wToolbar.setLayoutData(fdToolbar);
        PropsUi.setLook(wToolbar, Props.WIDGET_STYLE_DEFAULT);

        ToolItem item = new ToolItem(wToolbar, SWT.PUSH);
        item.setImage(GuiResource.getInstance().getImageHelpWeb());
        item.setToolTipText(BaseMessages.getString(PKG, "System.Tooltip.Help"));
        item.addListener(SWT.Selection, e -> onHelpDatabaseType());

        wConnectionType = new Combo(wBasicComp, SWT.SINGLE|SWT.LEFT|SWT.BORDER);
        wConnectionType.setItems(getGraphConnectionTypes());
        PropsUi.setLook(wConnectionType);
        FormData fdConnectionType = new FormData();
        fdConnectionType.top = new FormAttachment(wlGraphConnectionType, 0, SWT.CENTER);
        fdConnectionType.left = new FormAttachment(middle, 0); // To the right of the label
        fdConnectionType.right = new FormAttachment(wToolbar, -margin);
        wConnectionType.setLayoutData(fdConnectionType);
        Control lastControl = wConnectionType;

        Label wlDriverInfo = new Label(wBasicComp, SWT.RIGHT);
        PropsUi.setLook(wlDriverInfo);
        wlDriverInfo.setText(BaseMessages.getString(PKG, "GraphConnectionDialog.InstalledDriver.Label"));
        FormData fdlDriverInfo = new FormData();
        fdlDriverInfo.top = new FormAttachment(lastControl, margin*2);
        fdlDriverInfo.left = new FormAttachment(0, 0);
        fdlDriverInfo.right = new FormAttachment(middle, -margin);
        wlDriverInfo.setLayoutData(fdlDriverInfo);

        wDriverInfo = new Label(wBasicComp, SWT.LEFT);
        wDriverInfo.setEnabled(false);
        PropsUi.setLook(wDriverInfo);
        FormData fdDriverInfo = new FormData();
        fdDriverInfo.top = new FormAttachment(wlDriverInfo, 0, SWT.CENTER);
        fdDriverInfo.left = new FormAttachment(middle, 0);
        fdDriverInfo.right = new FormAttachment(100, 0);
        wDriverInfo.setLayoutData(fdDriverInfo);
        lastControl = wDriverInfo;

        // Add a composite area
        wGraphDatabaseSpecificComp = new Composite(wBasicComp, SWT.BACKGROUND);
        wGraphDatabaseSpecificComp.setLayout(new FormLayout());
        FormData fdGraphDatabaseSpecificComp = new FormData();
        fdGraphDatabaseSpecificComp.left = new FormAttachment(0,0);
        fdGraphDatabaseSpecificComp.right = new FormAttachment(100, 0);
        fdGraphDatabaseSpecificComp.top = new FormAttachment(lastControl, margin);
        wGraphDatabaseSpecificComp.setLayoutData(fdGraphDatabaseSpecificComp);
        PropsUi.setLook(wGraphDatabaseSpecificComp);
        lastControl = wGraphDatabaseSpecificComp;

        // Add the Graph connection specific widgets
        guiCompositeWidgets = new GuiCompositeWidgets(manager.getVariables());
        guiCompositeWidgets.createCompositeWidgets(
                getMetadata().getIGraphDatabase(),
                null,
                wGraphDatabaseSpecificComp,
                GraphDatabaseMeta.GUI_PLUGIN_ELEMENT_PARENT_ID,
                null);

        // Add listener to detect change
        guiCompositeWidgets.setWidgetsListener(
                new GuiCompositeWidgetsAdapter() {
                    @Override
                    public void widgetModified(GuiCompositeWidgets compositeWidgets, Control changedWidget, String widgetId){
                        setChanged();
                        updateDriverInfo();
                    }
                }
        );

        // End of the basic tab...
        //
        wBasicComp.pack();

        Rectangle bounds = wBasicComp.getBounds();

        wBasicSComp.setContent(wBasicComp);
        wBasicSComp.setExpandHorizontal(true);
        wBasicSComp.setExpandVertical(true);
        wBasicSComp.setMinWidth(bounds.width);
        wBasicSComp.setMinHeight(bounds.height);

        wModelTab.setControl(wBasicSComp);
    }

    /**
     *  Update driver information and version
     */
    protected void updateDriverInfo() {
        try {
            GraphDatabaseMeta graphDatabaseMeta = new GraphDatabaseMeta();
            this.getWidgetsContent(graphDatabaseMeta);

            wDriverInfo.setText("");
            String driverName = graphDatabaseMeta.getDriverClass(getVariables());
            if ( !Utils.isEmpty(driverName) ) {
                ClassLoader classLoader = graphDatabaseMeta.getIGraphDatabase().getClass().getClassLoader();
                Class<?> driver = classLoader.loadClass(driverName);

                if ( driver.getPackage().getImplementationVersion()!=null ) {
                    driverName = driverName+" ("+driver.getPackage().getImplementationVersion()+")";
                }

                wDriverInfo.setText(driverName);
            }
        } catch (Exception e) {
            wDriverInfo.setText("No driver installed");
        }
    }


    private void addProtocolTab(PropsUi props, IVariables variables, int middle, int margin) {
        CTabItem wProtocolTab = new CTabItem(wTabFolder, SWT.NONE);
        wProtocolTab.setFont(GuiResource.getInstance().getFontDefault());
        wProtocolTab.setText("Protocol   ");
        ScrolledComposite wProtocolSComp =
                new ScrolledComposite(wTabFolder, SWT.V_SCROLL | SWT.H_SCROLL);
        wProtocolSComp.setLayout(new FillLayout());

        Composite wProtocolComp = new Composite(wProtocolSComp, SWT.NONE);
        PropsUi.setLook(wProtocolComp);

        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = 3;
        formLayout.marginHeight = 3;
        wProtocolComp.setLayout(formLayout);

        // End of the basic tab...
        //
        wProtocolComp.pack();

        Rectangle bounds = wProtocolComp.getBounds();

        wProtocolSComp.setContent(wProtocolComp);
        wProtocolSComp.setExpandHorizontal(true);
        wProtocolSComp.setExpandVertical(true);
        wProtocolSComp.setMinWidth(bounds.width);
        wProtocolSComp.setMinHeight(bounds.height);

        wProtocolTab.setControl(wProtocolSComp);
    }

    private void addUrlsTab(PropsUi props, IVariables variables) {
        CTabItem wUrlsTab = new CTabItem(wTabFolder, SWT.NONE);
        wUrlsTab.setFont(GuiResource.getInstance().getFontDefault());
        wUrlsTab.setText("Manual URLs");
        ScrolledComposite wUrlsSComp = new ScrolledComposite(wTabFolder, SWT.V_SCROLL | SWT.H_SCROLL);
        wUrlsSComp.setLayout(new FillLayout());

        Composite wUrlsComp = new Composite(wUrlsSComp, SWT.NONE);
        PropsUi.setLook(wUrlsComp);

        wUrlsTab.setControl(wUrlsSComp);
    }

    private void addAdvancedTab(PropsUi props, IVariables variables, int middle, int margin) {
        CTabItem wAdvancedTab = new CTabItem(wTabFolder, SWT.NONE);
        wAdvancedTab.setFont(GuiResource.getInstance().getFontDefault());
        wAdvancedTab.setText("Advanced  ");
        ScrolledComposite wAdvancedSComp =
                new ScrolledComposite(wTabFolder, SWT.V_SCROLL | SWT.H_SCROLL);
        wAdvancedSComp.setLayout(new FillLayout());

        Composite wAdvancedComp = new Composite(wAdvancedSComp, SWT.NONE);
        PropsUi.setLook(wAdvancedComp);

        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = 3;
        formLayout.marginHeight = 3;
        wAdvancedComp.setLayout(formLayout);

        // End of the basic tab...
        //
        wAdvancedComp.pack();

        Rectangle bounds = wAdvancedComp.getBounds();

        wAdvancedSComp.setContent(wAdvancedComp);
        wAdvancedSComp.setExpandHorizontal(true);
        wAdvancedSComp.setExpandVertical(true);
        wAdvancedSComp.setMinWidth(bounds.width);
        wAdvancedSComp.setMinHeight(bounds.height);

        wAdvancedTab.setControl(wAdvancedSComp);
    }

    private void enableFields() {

        // If you specify URLs manually a lot of things are no longer available...
        //
        GraphDatabaseMeta graphDatabaseMeta = new GraphDatabaseMeta();
        getWidgetsContent(graphDatabaseMeta);

        // Also enable/disable the custom native fields
        //
        guiCompositeWidgets.enableWidgets(
                getMetadata().getIGraphDatabase(), DatabaseMeta.GUI_PLUGIN_ELEMENT_PARENT_ID, true);
    }

    @Override
    public void setWidgetsContent() {

        GraphDatabaseMeta graphDatabaseMeta = this.getMetadata();

        wConnectionType.setText(Const.NVL(graphDatabaseMeta.getPluginName(), ""));
        wName.setText(Const.NVL(graphDatabaseMeta.getName(), ""));

        guiCompositeWidgets.setWidgetsContents(
                graphDatabaseMeta.getIGraphDatabase(),
                wGraphDatabaseSpecificComp,
                GraphDatabaseMeta.GUI_PLUGIN_ELEMENT_PARENT_ID
        );

        updateDriverInfo();
        enableFields();
        wName.setFocus();
    }

    @Override
    public void getWidgetsContent(GraphDatabaseMeta graphDatabaseMeta) {
        if(!StringUtils.isEmpty(wName.getText())){
            graphDatabaseMeta.setName(wName.getText());
        }
        if(!StringUtils.isEmpty(wConnectionType.getText())){
            graphDatabaseMeta.setGraphDatabaseType(wConnectionType.getText());
        }

        guiCompositeWidgets.getWidgetsContents(
                graphDatabaseMeta.getIGraphDatabase(), GraphDatabaseMeta.GUI_PLUGIN_ELEMENT_PARENT_ID);

    }

    private void test(){
        GraphDatabaseMeta graphDatabaseMeta = new GraphDatabaseMeta();
        getWidgetsContent(graphDatabaseMeta);
        testConnection(getShell(), manager.getVariables(), graphDatabaseMeta);
    }

    public static final void testConnection(
            Shell shell, IVariables variables, GraphDatabaseMeta graphDatabaseMeta){
        String[] remarks = graphDatabaseMeta.checkParameters();
        if(remarks.length == 0){
            String message = "";
            boolean success = true;

            IGraphDatabase graphDatabase = graphDatabaseMeta.getIGraphDatabase();
            GraphDatabaseTestResults testResults = null;
            try{
                testResults = graphDatabase.testConnectionSuccess(variables);
                message = testResults.getMessage();
                success = testResults.isSuccess();
            }catch(HopException e){
                success = false;
                message = "failed to connect to graph database " + graphDatabaseMeta.getName() + Const.CR;
                message += testResults.getMessage() + Const.CR;
            }
            String title =
                    success
                            ? BaseMessages.getString(PKG, "GraphDatabaseDialog.DatabaseConnectionTestSuccess.title")
                            : BaseMessages.getString(PKG, "GraphDatabaseDialog.DatabaseConnectionTest.title");
            if (success && message.contains(Const.CR)) {
                message =
                        message.substring(0, message.indexOf(Const.CR))
                                + Const.CR
                                + message.substring(message.indexOf(Const.CR));
                message = message.substring(0, message.lastIndexOf(Const.CR));
            }
            ShowMessageDialog msgDialog =
                    new ShowMessageDialog(
                            shell, SWT.ICON_INFORMATION | SWT.OK, title, message, message.length() > 300);
            msgDialog.setType(
                    success
                            ? Const.SHOW_MESSAGE_DIALOG_DB_TEST_SUCCESS
                            : Const.SHOW_MESSAGE_DIALOG_DB_TEST_DEFAULT);
            msgDialog.open();
        } else {
            String message = "";
            for (int i = 0; i < remarks.length; i++) {
                message += "    * " + remarks[i] + Const.CR;
            }

            MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
            mb.setText(BaseMessages.getString(PKG, "GraphDatabaseDialog.ErrorParameters2.title"));
            mb.setMessage(
                    BaseMessages.getString(PKG, "GraphDatabaseDialog.ErrorParameters2.description", message));
            mb.open();
        }
    }

    @Override
    public Button[] createButtonsForButtonBar(Composite composite) {
        Button wTest = new Button(composite, SWT.PUSH);
        wTest.setText(BaseMessages.getString(PKG, "System.Button.Test"));
        wTest.addListener(SWT.Selection, e -> test());

        return new Button[] {wTest};
    }

    @Override
    public boolean setFocus() {
        if (wName == null || wName.isDisposed()) {
            return false;
        }
        return wName.setFocus();
    }

    public void clearChanged() {
        resetChanged();
        MetadataPerspective.getInstance().updateEditor(this);
    }

    private void onHelpDatabaseType() {
        PluginRegistry registry = PluginRegistry.getInstance();
        String name = wConnectionType.getText();
        for (IPlugin plugin : registry.getPlugins(GraphDatabasePluginType.class)) {
            if (plugin.getName().equals(name)) {
                HelpUtils.openHelp(getShell(), plugin);
                break;
            }
        }
    }

    private String[] getGraphConnectionTypes() {
        PluginRegistry registry = PluginRegistry.getInstance();
        List<IPlugin> plugins = registry.getPlugins(GraphDatabasePluginType.class);
        String[] types = new String[plugins.size()];
        for (int i = 0; i < types.length; i++) {
            types[i] = plugins.get(i).getName();
        }
        Arrays.sort(types, String.CASE_INSENSITIVE_ORDER);
        return types;
    }

    private AtomicBoolean busyChangingConnectionType = new AtomicBoolean(false);

    private void changeConnectionType() {

        if (busyChangingConnectionType.get()) {
            return;
        }
        busyChangingConnectionType.set(true);

        GraphDatabaseMeta graphDatabaseMeta = this.getMetadata();

        // Keep track of the old database type since this changes when getting the content
        //
        Class<? extends IGraphDatabase> oldClass = graphDatabaseMeta.getIGraphDatabase().getClass();
        String oldTypeName = graphDatabaseMeta.getPluginName();
        String newTypeName = wConnectionType.getText();
        wConnectionType.setText(graphDatabaseMeta.getPluginName());

        // Capture any information on the widgets
        //
        this.getWidgetsContent(graphDatabaseMeta);

        // Save the state of this type, so we can switch back and forth
        //
        metaMap.put(oldClass, graphDatabaseMeta.getIGraphDatabase());

        // Now change the data type
        //
        wConnectionType.setText(newTypeName);
        graphDatabaseMeta.setGraphDatabaseType(newTypeName);

        // Get possible information from the metadata map (from previous work)
        //
        graphDatabaseMeta.setIGraphDatabase(metaMap.get(graphDatabaseMeta.getIGraphDatabase().getClass()));

        // Remove existing children
        //
        for (Control child : wGraphDatabaseSpecificComp.getChildren()) {
            child.dispose();
        }

        // Re-add the widgets
        //
        guiCompositeWidgets = new GuiCompositeWidgets(manager.getVariables());
        guiCompositeWidgets.createCompositeWidgets(
                graphDatabaseMeta.getIGraphDatabase(),
                null,
                wGraphDatabaseSpecificComp,
                GraphDatabaseMeta.GUI_PLUGIN_ELEMENT_PARENT_ID,
                null);
        guiCompositeWidgets.setWidgetsListener(
                new GuiCompositeWidgetsAdapter() {
                    @Override
                    public void widgetModified(
                            GuiCompositeWidgets compositeWidgets, Control changedWidget, String widgetId) {
                        setChanged();
                        updateDriverInfo();
                    }
                });
        addCompositeWidgetsUsernamePassword();

        // Put the data back
        //
        setWidgetsContent();

        wBasicComp.layout(true, true);

        busyChangingConnectionType.set(false);
    }

    private void addCompositeWidgetsUsernamePassword() {
        // Add username and password to the mix so folks can enable/disable those
        //
        guiCompositeWidgets.getWidgetsMap().put(BaseGraphDatabaseMeta.ID_USERNAME_LABEL, wlUsername);
        guiCompositeWidgets.getWidgetsMap().put(BaseGraphDatabaseMeta.ID_USERNAME_WIDGET, wUsername);
        guiCompositeWidgets.getWidgetsMap().put(BaseGraphDatabaseMeta.ID_PASSWORD_LABEL, wlPassword);
        guiCompositeWidgets.getWidgetsMap().put(BaseGraphDatabaseMeta.ID_PASSWORD_WIDGET, wPassword);
    }

}
