package org.apache.hop.ui.core.graph;

import org.apache.hop.core.Const;
import org.apache.hop.core.Props;
import org.apache.hop.core.database.BaseDatabaseMeta;
import org.apache.hop.core.database.DatabaseMeta;
import org.apache.hop.core.encryption.Encr;
import org.apache.hop.core.exception.HopConfigException;
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
import org.apache.hop.ui.core.dialog.ErrorDialog;
import org.apache.hop.ui.core.dialog.MessageBox;
import org.apache.hop.ui.core.dialog.ShowMessageDialog;
import org.apache.hop.ui.core.gui.GuiCompositeWidgets;
import org.apache.hop.ui.core.gui.GuiCompositeWidgetsAdapter;
import org.apache.hop.ui.core.gui.GuiResource;
import org.apache.hop.ui.core.metadata.MetadataEditor;
import org.apache.hop.ui.core.metadata.MetadataManager;
import org.apache.hop.ui.core.widget.CheckBoxVar;
import org.apache.hop.ui.core.widget.ColumnInfo;
import org.apache.hop.ui.core.widget.PasswordTextVar;
import org.apache.hop.ui.core.widget.TableView;
import org.apache.hop.ui.core.widget.TextVar;
import org.apache.hop.ui.hopgui.HopGui;
import org.apache.hop.ui.hopgui.perspective.metadata.MetadataPerspective;
import org.apache.hop.ui.util.HelpUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
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
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.neo4j.driver.Config;
import org.neo4j.driver.Driver;

import java.net.URI;
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
    private Label wlAutomatic;
    private CheckBoxVar wAutomatic;
    private TextVar wProtocol;
    private Label wlServer;
    private TextVar wServer;
    private Label wlDatabaseName;
    private TextVar wDatabaseName;
    private Label wlDatabasePort;
    private TextVar wDatabasePort;
    private TextVar wUsername;
    private TextVar wPassword;

    // Advanced
    //
    private Label wlVersion4;
    private CheckBoxVar wVersion4;
    private TextVar wBrowserPort;
    private Label wlPolicy;
    private TextVar wPolicy;
    private Label wlRouting;
    private CheckBoxVar wRouting;
    private Label wlEncryption;
    private CheckBoxVar wEncryption;
    private Label wlTrustAllCertificates;
    private CheckBoxVar wTrustAllCertificates;

    private TextVar wConnectionLivenessCheckTimeout;
    private TextVar wMaxConnectionLifetime;
    private TextVar wMaxConnectionPoolSize;
    private TextVar wConnectionAcquisitionTimeout;
    private TextVar wConnectionTimeout;
    private TextVar wMaxTransactionRetryTime;
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
                if(graphDatabase.getDefaultBoltPort() == null){
                    graphDatabase.setDefaultBoltPort("7687");
                }
                if (Integer.valueOf(graphDatabase.getDefaultBoltPort()) > 0) {
                    graphDatabase.setBoltPort(graphDatabase.getDefaultBoltPort());
                }
                graphDatabase.setPluginId(plugin.getIds()[0]);
                graphDatabase.setPluginName(plugin.getName());
                graphDatabase.addDefaultOptions();

                metaMap.put(graphDatabase.getClass(), graphDatabase);
            } catch (Exception e) {
                HopGui.getInstance().getLog().logError("Error instantiating graph database metadata", e);
            }
        }
        return metaMap;

    }

    @Override
    public void createControl(Composite composite) {
//        PropsUi props = PropsUi.getInstance();

        middle = props.getMiddlePct();
        margin = PropsUi.getMargin() + 2;

        IVariables variables = getHopGui().getVariables();

        // The name
        Label wlName = new Label(composite, SWT.RIGHT);
        PropsUi.setLook(wlName);
        wlName.setText(BaseMessages.getString(PKG, "GraphConnectionEditor.Name.Label"));
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
//                wAutomatic,
//                wProtocol,
//                wServer,
//                wDatabaseName,
//                wVersion4,
//                wDatabasePort,
//                wBrowserPort,
//                wPolicy,
//                wUsername,
//                wPassword,
//                wRouting,
//                wEncryption,
//                wTrustAllCertificates,
//                wConnectionLivenessCheckTimeout,
//                wMaxConnectionLifetime,
//                wMaxConnectionPoolSize,
//                wConnectionAcquisitionTimeout,
//                wConnectionTimeout,
//                wMaxTransactionRetryTime
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
        wlGraphConnectionType.setText(BaseMessages.getString(PKG, "GraphConnectionEditor.label.ConnectionType"));
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
        wlDriverInfo.setText(BaseMessages.getString(PKG, "GraphConnectionEditor.label.InstalledDriver"));
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

        // Automatic?
//        wlAutomatic = new Label(wBasicComp, SWT.RIGHT);
//        wlAutomatic.setText(BaseMessages.getString(PKG, "GraphConnectionEditor.Automatic.Label"));
//        wlAutomatic.setToolTipText(
//                BaseMessages.getString(PKG, "GraphConnectionEditor.Automatic.Tooltip"));
//        PropsUi.setLook(wlAutomatic);
//        FormData fdlAutomatic = new FormData();
//        fdlAutomatic.top = new FormAttachment(lastControl, margin*2);
//        fdlAutomatic.left = new FormAttachment(0, 0);
//        fdlAutomatic.right = new FormAttachment(middle, -margin);
//        wlAutomatic.setLayoutData(fdlAutomatic);
//        wAutomatic = new CheckBoxVar(variables, wBasicComp, SWT.CHECK);
//        wAutomatic.setToolTipText(BaseMessages.getString(PKG, "GraphConnectionEditor.Automatic.Tooltip"));
//        PropsUi.setLook(wAutomatic);
//        FormData fdAutomatic = new FormData();
//        fdAutomatic.top = new FormAttachment(wlAutomatic, 0, SWT.CENTER);
//        fdAutomatic.left = new FormAttachment(middle, 0);
//        fdAutomatic.right = new FormAttachment(95, 0);
//        wAutomatic.setLayoutData(fdAutomatic);
//        wAutomatic.addListener(SWT.Selection, e -> enableFields());
//        lastControl = wAutomatic;
//
//        // Protocol
//        Label wlProtocol = new Label(wBasicComp, SWT.RIGHT);
//        wlProtocol.setText(BaseMessages.getString(PKG, "GraphConnectionEditor.Protocol.Label"));
//        wlProtocol.setToolTipText(BaseMessages.getString(PKG, "GraphConnectionEditor.Protocol.Tooltip"));
//        PropsUi.setLook(wlProtocol);
//        FormData fdlProtocol = new FormData();
//        fdlProtocol.top = new FormAttachment(lastControl, margin);
//        fdlProtocol.left = new FormAttachment(0, 0);
//        fdlProtocol.right = new FormAttachment(middle, -margin);
//        wlProtocol.setLayoutData(fdlProtocol);
//        wProtocol = new TextVar(variables, wBasicComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
//        wProtocol.setToolTipText(BaseMessages.getString(PKG, "GraphConnectionEditor.Protocol.Tooltip"));
//        PropsUi.setLook(wProtocol);
//        FormData fdProtocol = new FormData();
//        fdProtocol.top = new FormAttachment(wlProtocol, 0, SWT.CENTER);
//        fdProtocol.left = new FormAttachment(middle, 0);
//        fdProtocol.right = new FormAttachment(95, 0);
//        wProtocol.setLayoutData(fdProtocol);
//        lastControl = wProtocol;
//
//        // The server
//        wlServer = new Label(wBasicComp, SWT.RIGHT);
//        PropsUi.setLook(wlServer);
//        wlServer.setText(BaseMessages.getString(PKG, "GraphConnectionEditor.Server.Label"));
//        FormData fdlServer = new FormData();
//        fdlServer.top = new FormAttachment(lastControl, margin);
//        fdlServer.left = new FormAttachment(0, 0); // First one in the left top corner
//        fdlServer.right = new FormAttachment(middle, -margin);
//        wlServer.setLayoutData(fdlServer);
//        wServer = new TextVar(variables, wBasicComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
//        PropsUi.setLook(wServer);
//        FormData fdServer = new FormData();
//        fdServer.top = new FormAttachment(wlServer, 0, SWT.CENTER);
//        fdServer.left = new FormAttachment(middle, 0); // To the right of the label
//        fdServer.right = new FormAttachment(95, 0);
//        wServer.setLayoutData(fdServer);
//        lastControl = wServer;
//
//        // The DatabaseName
//        wlDatabaseName = new Label(wBasicComp, SWT.RIGHT);
//        PropsUi.setLook(wlDatabaseName);
//        wlDatabaseName.setText(BaseMessages.getString(PKG, "GraphConnectionEditor.DatabaseName.Label"));
//        FormData fdlDatabaseName = new FormData();
//        fdlDatabaseName.top = new FormAttachment(lastControl, margin);
//        fdlDatabaseName.left = new FormAttachment(0, 0); // First one in the left top corner
//        fdlDatabaseName.right = new FormAttachment(middle, -margin);
//        wlDatabaseName.setLayoutData(fdlDatabaseName);
//        wDatabaseName = new TextVar(variables, wBasicComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
//        PropsUi.setLook(wDatabaseName);
//        FormData fdDatabaseName = new FormData();
//        fdDatabaseName.top = new FormAttachment(wlDatabaseName, 0, SWT.CENTER);
//        fdDatabaseName.left = new FormAttachment(middle, 0); // To the right of the label
//        fdDatabaseName.right = new FormAttachment(95, 0);
//        wDatabaseName.setLayoutData(fdDatabaseName);
//        lastControl = wDatabaseName;
//
//        // Database port?
//        wlDatabasePort = new Label(wBasicComp, SWT.RIGHT);
//        PropsUi.setLook(wlDatabasePort);
//        wlDatabasePort.setText(BaseMessages.getString(PKG, "GraphConnectionEditor.DatabasePort.Label"));
//        FormData fdlDatabasePort = new FormData();
//        fdlDatabasePort.top = new FormAttachment(lastControl, margin);
//        fdlDatabasePort.left = new FormAttachment(0, 0); // First one in the left top corner
//        fdlDatabasePort.right = new FormAttachment(middle, -margin);
//        wlDatabasePort.setLayoutData(fdlDatabasePort);
//        wDatabasePort = new TextVar(variables, wBasicComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
//        PropsUi.setLook(wDatabasePort);
//        FormData fdDatabasePort = new FormData();
//        fdDatabasePort.top = new FormAttachment(wlDatabasePort, 0, SWT.CENTER);
//        fdDatabasePort.left = new FormAttachment(middle, 0); // To the right of the label
//        fdDatabasePort.right = new FormAttachment(95, 0);
//        wDatabasePort.setLayoutData(fdDatabasePort);
//        lastControl = wDatabasePort;
//
//        // Username
//        wlUsername = new Label(wBasicComp, SWT.RIGHT);
//        wlUsername.setText(BaseMessages.getString(PKG, "GraphConnectionEditor.UserName.Label"));
//        PropsUi.setLook(wlUsername);
//        FormData fdlUsername = new FormData();
//        fdlUsername.top = new FormAttachment(lastControl, margin);
//        fdlUsername.left = new FormAttachment(0, 0);
//        fdlUsername.right = new FormAttachment(middle, -margin);
//        wlUsername.setLayoutData(fdlUsername);
//        wUsername = new TextVar(variables, wBasicComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
//        PropsUi.setLook(wUsername);
//        FormData fdUsername = new FormData();
//        fdUsername.top = new FormAttachment(wlUsername, 0, SWT.CENTER);
//        fdUsername.left = new FormAttachment(middle, 0);
//        fdUsername.right = new FormAttachment(95, 0);
//        wUsername.setLayoutData(fdUsername);
//        lastControl = wUsername;
//
//        // Password
//        wlPassword = new Label(wBasicComp, SWT.RIGHT);
//        wlPassword.setText(BaseMessages.getString(PKG, "GraphConnectionEditor.Password.Label"));
//        PropsUi.setLook(wlPassword);
//        FormData fdlPassword = new FormData();
//        fdlPassword.top = new FormAttachment(lastControl, margin);
//        fdlPassword.left = new FormAttachment(0, 0);
//        fdlPassword.right = new FormAttachment(middle, -margin);
//        wlPassword.setLayoutData(fdlPassword);
//        wPassword = new PasswordTextVar(variables, wBasicComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
//        PropsUi.setLook(wPassword);
//        FormData fdPassword = new FormData();
//        fdPassword.top = new FormAttachment(wlPassword, 0, SWT.CENTER);
//        fdPassword.left = new FormAttachment(middle, 0);
//        fdPassword.right = new FormAttachment(95, 0);
//        wPassword.setLayoutData(fdPassword);
//        lastControl = wPassword;

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
     *  Update JDBC driver information and version
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

        // Version4?
        wlVersion4 = new Label(wProtocolComp, SWT.RIGHT);
        wlVersion4.setText(BaseMessages.getString(PKG, "GraphConnectionEditor.Version4.Label"));
        PropsUi.setLook(wlVersion4);
        FormData fdlVersion4 = new FormData();
        fdlVersion4.top = new FormAttachment(0, margin);
        fdlVersion4.left = new FormAttachment(0, 0);
        fdlVersion4.right = new FormAttachment(middle, -margin);
        wlVersion4.setLayoutData(fdlVersion4);
        wVersion4 = new CheckBoxVar(variables, wProtocolComp, SWT.CHECK);
        PropsUi.setLook(wVersion4);
        FormData fdVersion4 = new FormData();
        fdVersion4.top = new FormAttachment(wlVersion4, 0, SWT.CENTER);
        fdVersion4.left = new FormAttachment(middle, 0);
        fdVersion4.right = new FormAttachment(95, 0);
        wVersion4.setLayoutData(fdVersion4);
        Control lastControl = wVersion4;

        // Browser port?
        Label wlBrowserPort = new Label(wProtocolComp, SWT.RIGHT);
        PropsUi.setLook(wlBrowserPort);
        wlBrowserPort.setText(BaseMessages.getString(PKG, "GraphConnectionEditor.BrowserPort.Label"));
        FormData fdlBrowserPort = new FormData();
        fdlBrowserPort.top = new FormAttachment(lastControl, margin);
        fdlBrowserPort.left = new FormAttachment(0, 0); // First one in the left top corner
        fdlBrowserPort.right = new FormAttachment(middle, -margin);
        wlBrowserPort.setLayoutData(fdlBrowserPort);
        wBrowserPort = new TextVar(variables, wProtocolComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        PropsUi.setLook(wBrowserPort);
        FormData fdBrowserPort = new FormData();
        fdBrowserPort.top = new FormAttachment(wlBrowserPort, 0, SWT.CENTER);
        fdBrowserPort.left = new FormAttachment(middle, 0); // To the right of the label
        fdBrowserPort.right = new FormAttachment(95, 0);
        wBrowserPort.setLayoutData(fdBrowserPort);
        lastControl = wBrowserPort;

        // Routing
        wlRouting = new Label(wProtocolComp, SWT.RIGHT);
        wlRouting.setText(BaseMessages.getString(PKG, "GraphConnectionEditor.Routing.Label"));
        PropsUi.setLook(wlRouting);
        FormData fdlRouting = new FormData();
        fdlRouting.top = new FormAttachment(lastControl, margin);
        fdlRouting.left = new FormAttachment(0, 0);
        fdlRouting.right = new FormAttachment(middle, -margin);
        wlRouting.setLayoutData(fdlRouting);
        wRouting = new CheckBoxVar(variables, wProtocolComp, SWT.CHECK);
        PropsUi.setLook(wRouting);
        FormData fdRouting = new FormData();
        fdRouting.top = new FormAttachment(wlRouting, 0, SWT.CENTER);
        fdRouting.left = new FormAttachment(middle, 0);
        fdRouting.right = new FormAttachment(95, 0);
        wRouting.setLayoutData(fdRouting);
        wRouting.addListener(SWT.Selection, e -> enableFields());
        lastControl = wRouting;

        // Policy
        wlPolicy = new Label(wProtocolComp, SWT.RIGHT);
        wlPolicy.setText(BaseMessages.getString(PKG, "GraphConnectionEditor.Policy.Label"));
        PropsUi.setLook(wlPolicy);
        FormData fdlPolicy = new FormData();
        fdlPolicy.top = new FormAttachment(lastControl, margin);
        fdlPolicy.left = new FormAttachment(0, 0);
        fdlPolicy.right = new FormAttachment(middle, -margin);
        wlPolicy.setLayoutData(fdlPolicy);
        wPolicy = new TextVar(variables, wProtocolComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        PropsUi.setLook(wPolicy);
        FormData fdPolicy = new FormData();
        fdPolicy.top = new FormAttachment(wlPolicy, 0, SWT.CENTER);
        fdPolicy.left = new FormAttachment(middle, 0);
        fdPolicy.right = new FormAttachment(95, 0);
        wPolicy.setLayoutData(fdPolicy);
        lastControl = wPolicy;

        // Encryption?
        wlEncryption = new Label(wProtocolComp, SWT.RIGHT);
        wlEncryption.setText(BaseMessages.getString(PKG, "GraphConnectionEditor.Encryption.Label"));
        PropsUi.setLook(wlEncryption);
        FormData fdlEncryption = new FormData();
        fdlEncryption.top = new FormAttachment(lastControl, margin);
        fdlEncryption.left = new FormAttachment(0, 0);
        fdlEncryption.right = new FormAttachment(middle, -margin);
        wlEncryption.setLayoutData(fdlEncryption);
        wEncryption = new CheckBoxVar(variables, wProtocolComp, SWT.CHECK);
        PropsUi.setLook(wEncryption);
        FormData fdEncryption = new FormData();
        fdEncryption.top = new FormAttachment(wlEncryption, 0, SWT.CENTER);
        fdEncryption.left = new FormAttachment(middle, 0);
        fdEncryption.right = new FormAttachment(95, 0);
        wEncryption.setLayoutData(fdEncryption);
        wEncryption.addListener(SWT.Selection, e -> enableFields());
        lastControl = wEncryption;

        // Trust Level?
        wlTrustAllCertificates = new Label(wProtocolComp, SWT.RIGHT);
        wlTrustAllCertificates.setText(
                BaseMessages.getString(PKG, "GraphConnectionEditor.TrustAllCertificates.Label"));
        PropsUi.setLook(wlTrustAllCertificates);
        FormData fdlTrustAllCertificates = new FormData();
        fdlTrustAllCertificates.top = new FormAttachment(lastControl, margin);
        fdlTrustAllCertificates.left = new FormAttachment(0, 0);
        fdlTrustAllCertificates.right = new FormAttachment(middle, -margin);
        wlTrustAllCertificates.setLayoutData(fdlTrustAllCertificates);
        wTrustAllCertificates = new CheckBoxVar(variables, wProtocolComp, SWT.CHECK);
        PropsUi.setLook(wEncryption);
        FormData fdTrustAllCertificates = new FormData();
        fdTrustAllCertificates.top = new FormAttachment(wlTrustAllCertificates, 0, SWT.CENTER);
        fdTrustAllCertificates.left = new FormAttachment(middle, 0);
        fdTrustAllCertificates.right = new FormAttachment(95, 0);
        wTrustAllCertificates.setLayoutData(fdTrustAllCertificates);
        lastControl = wTrustAllCertificates;

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

        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = 3;
        formLayout.marginHeight = 3;
        wUrlsComp.setLayout(formLayout);

        // URLs
        wUrls =
                new TableView(
                        variables,
                        wUrlsComp,
                        SWT.NONE,
                        new ColumnInfo[] {
                                new ColumnInfo(
                                        BaseMessages.getString(PKG, "GraphConnectionEditor.URLColumn.Label"),
                                        ColumnInfo.COLUMN_TYPE_TEXT)
                        },
                        getMetadata().getManualUrls().size(),
                        e -> { setChanged(); enableFields(); },
                        props);

        FormData fdUrls = new FormData();
        fdUrls.top = new FormAttachment(0, 0);
        fdUrls.left = new FormAttachment(0, 0);
        fdUrls.right = new FormAttachment(100, 0);
        fdUrls.bottom = new FormAttachment(100, 0);
        wUrls.setLayoutData(fdUrls);

        // End of the basic tab...
        //
        wUrlsComp.pack();

        Rectangle bounds = wUrlsComp.getBounds();

        wUrlsSComp.setContent(wUrlsComp);
        wUrlsSComp.setExpandHorizontal(true);
        wUrlsSComp.setExpandVertical(true);
        wUrlsSComp.setMinWidth(bounds.width);
        wUrlsSComp.setMinHeight(bounds.height);

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

        // ConnectionLivenessCheckTimeout
        Label wlConnectionLivenessCheckTimeout = new Label(wAdvancedComp, SWT.RIGHT);
        wlConnectionLivenessCheckTimeout.setText(
                BaseMessages.getString(PKG, "GraphConnectionEditor.ConnectionLivenessCheckTimeout.Label"));
        PropsUi.setLook(wlConnectionLivenessCheckTimeout);
        FormData fdlConnectionLivenessCheckTimeout = new FormData();
        fdlConnectionLivenessCheckTimeout.top = new FormAttachment(0, 0);
        fdlConnectionLivenessCheckTimeout.left = new FormAttachment(0, 0);
        fdlConnectionLivenessCheckTimeout.right = new FormAttachment(middle, -margin);
        wlConnectionLivenessCheckTimeout.setLayoutData(fdlConnectionLivenessCheckTimeout);
        wConnectionLivenessCheckTimeout =
                new TextVar(variables, wAdvancedComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        PropsUi.setLook(wConnectionLivenessCheckTimeout);
        FormData fdConnectionLivenessCheckTimeout = new FormData();
        fdConnectionLivenessCheckTimeout.top =
                new FormAttachment(wlConnectionLivenessCheckTimeout, 0, SWT.CENTER);
        fdConnectionLivenessCheckTimeout.left = new FormAttachment(middle, 0);
        fdConnectionLivenessCheckTimeout.right = new FormAttachment(95, 0);
        wConnectionLivenessCheckTimeout.setLayoutData(fdConnectionLivenessCheckTimeout);
        Control lastGroupControl = wConnectionLivenessCheckTimeout;

        // MaxConnectionLifetime
        Label wlMaxConnectionLifetime = new Label(wAdvancedComp, SWT.RIGHT);
        wlMaxConnectionLifetime.setText(
                BaseMessages.getString(PKG, "GraphConnectionEditor.MaxConnectionLifetime.Label"));
        PropsUi.setLook(wlMaxConnectionLifetime);
        FormData fdlMaxConnectionLifetime = new FormData();
        fdlMaxConnectionLifetime.top = new FormAttachment(lastGroupControl, margin);
        fdlMaxConnectionLifetime.left = new FormAttachment(0, 0);
        fdlMaxConnectionLifetime.right = new FormAttachment(middle, -margin);
        wlMaxConnectionLifetime.setLayoutData(fdlMaxConnectionLifetime);
        wMaxConnectionLifetime =
                new TextVar(variables, wAdvancedComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        PropsUi.setLook(wMaxConnectionLifetime);
        FormData fdMaxConnectionLifetime = new FormData();
        fdMaxConnectionLifetime.top = new FormAttachment(wlMaxConnectionLifetime, 0, SWT.CENTER);
        fdMaxConnectionLifetime.left = new FormAttachment(middle, 0);
        fdMaxConnectionLifetime.right = new FormAttachment(95, 0);
        wMaxConnectionLifetime.setLayoutData(fdMaxConnectionLifetime);
        lastGroupControl = wMaxConnectionLifetime;

        // MaxConnectionPoolSize
        Label wlMaxConnectionPoolSize = new Label(wAdvancedComp, SWT.RIGHT);
        wlMaxConnectionPoolSize.setText(
                BaseMessages.getString(PKG, "GraphConnectionEditor.MaxConnectionPoolSize.Label"));
        PropsUi.setLook(wlMaxConnectionPoolSize);
        FormData fdlMaxConnectionPoolSize = new FormData();
        fdlMaxConnectionPoolSize.top = new FormAttachment(lastGroupControl, margin);
        fdlMaxConnectionPoolSize.left = new FormAttachment(0, 0);
        fdlMaxConnectionPoolSize.right = new FormAttachment(middle, -margin);
        wlMaxConnectionPoolSize.setLayoutData(fdlMaxConnectionPoolSize);
        wMaxConnectionPoolSize =
                new TextVar(variables, wAdvancedComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        PropsUi.setLook(wMaxConnectionPoolSize);
        FormData fdMaxConnectionPoolSize = new FormData();
        fdMaxConnectionPoolSize.top = new FormAttachment(wlMaxConnectionPoolSize, 0, SWT.CENTER);
        fdMaxConnectionPoolSize.left = new FormAttachment(middle, 0);
        fdMaxConnectionPoolSize.right = new FormAttachment(95, 0);
        wMaxConnectionPoolSize.setLayoutData(fdMaxConnectionPoolSize);
        lastGroupControl = wMaxConnectionPoolSize;

        // ConnectionAcquisitionTimeout
        Label wlConnectionAcquisitionTimeout = new Label(wAdvancedComp, SWT.RIGHT);
        wlConnectionAcquisitionTimeout.setText(
                BaseMessages.getString(PKG, "GraphConnectionEditor.ConnectionAcquisitionTimeout.Label"));
        PropsUi.setLook(wlConnectionAcquisitionTimeout);
        FormData fdlConnectionAcquisitionTimeout = new FormData();
        fdlConnectionAcquisitionTimeout.top = new FormAttachment(lastGroupControl, margin);
        fdlConnectionAcquisitionTimeout.left = new FormAttachment(0, 0);
        fdlConnectionAcquisitionTimeout.right = new FormAttachment(middle, -margin);
        wlConnectionAcquisitionTimeout.setLayoutData(fdlConnectionAcquisitionTimeout);
        wConnectionAcquisitionTimeout =
                new TextVar(variables, wAdvancedComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        PropsUi.setLook(wConnectionAcquisitionTimeout);
        FormData fdConnectionAcquisitionTimeout = new FormData();
        fdConnectionAcquisitionTimeout.top =
                new FormAttachment(wlConnectionAcquisitionTimeout, 0, SWT.CENTER);
        fdConnectionAcquisitionTimeout.left = new FormAttachment(middle, 0);
        fdConnectionAcquisitionTimeout.right = new FormAttachment(95, 0);
        wConnectionAcquisitionTimeout.setLayoutData(fdConnectionAcquisitionTimeout);
        lastGroupControl = wConnectionAcquisitionTimeout;

        // ConnectionTimeout
        Label wlConnectionTimeout = new Label(wAdvancedComp, SWT.RIGHT);
        wlConnectionTimeout.setText(
                BaseMessages.getString(PKG, "GraphConnectionEditor.ConnectionTimeout.Label"));
        PropsUi.setLook(wlConnectionTimeout);
        FormData fdlConnectionTimeout = new FormData();
        fdlConnectionTimeout.top = new FormAttachment(lastGroupControl, margin);
        fdlConnectionTimeout.left = new FormAttachment(0, 0);
        fdlConnectionTimeout.right = new FormAttachment(middle, -margin);
        wlConnectionTimeout.setLayoutData(fdlConnectionTimeout);
        wConnectionTimeout = new TextVar(variables, wAdvancedComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        PropsUi.setLook(wConnectionTimeout);
        FormData fdConnectionTimeout = new FormData();
        fdConnectionTimeout.top = new FormAttachment(wlConnectionTimeout, 0, SWT.CENTER);
        fdConnectionTimeout.left = new FormAttachment(middle, 0);
        fdConnectionTimeout.right = new FormAttachment(95, 0);
        wConnectionTimeout.setLayoutData(fdConnectionTimeout);
        lastGroupControl = wConnectionTimeout;

        // MaxTransactionRetryTime
        Label wlMaxTransactionRetryTime = new Label(wAdvancedComp, SWT.RIGHT);
        wlMaxTransactionRetryTime.setText(
                BaseMessages.getString(PKG, "GraphConnectionEditor.MaxTransactionRetryTime.Label"));
        PropsUi.setLook(wlMaxTransactionRetryTime);
        FormData fdlMaxTransactionRetryTime = new FormData();
        fdlMaxTransactionRetryTime.top = new FormAttachment(lastGroupControl, margin);
        fdlMaxTransactionRetryTime.left = new FormAttachment(0, 0);
        fdlMaxTransactionRetryTime.right = new FormAttachment(middle, -margin);
        wlMaxTransactionRetryTime.setLayoutData(fdlMaxTransactionRetryTime);
        wMaxTransactionRetryTime =
                new TextVar(variables, wAdvancedComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        PropsUi.setLook(wMaxTransactionRetryTime);
        FormData fdMaxTransactionRetryTime = new FormData();
        fdMaxTransactionRetryTime.top = new FormAttachment(wlMaxTransactionRetryTime, 0, SWT.CENTER);
        fdMaxTransactionRetryTime.left = new FormAttachment(middle, 0);
        fdMaxTransactionRetryTime.right = new FormAttachment(95, 0);
        wMaxTransactionRetryTime.setLayoutData(fdMaxTransactionRetryTime);

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
        boolean hasNoUrls = wUrls.nrNonEmpty() == 0;
        for (Control control :
                new Control[] {
//                        wlUsername,
//                        wlPassword,
//                        wUsername,
//                        wPassword,
//                        wlServer,
//                        wServer,
//                        wlDatabaseName,
//                        wDatabaseName,
//                        wlDatabasePort,
//                        wDatabasePort,
//                        wlRouting,
//                        wRouting,
//                        wlPolicy,
//                        wPolicy,
//                        wlEncryption,
//                        wEncryption,
                }) {
            control.setEnabled(true);
        }

        // For the normal scenarios without manual URLs we consider the automatic flag.
        // If things are configured automatically a number of flags are no longer applicable:
        // Version 4, routing, routing policy, encryption & trust all certificates.
        //
        GraphDatabaseMeta graphDatabaseMeta = new GraphDatabaseMeta();
        getWidgetsContent(graphDatabaseMeta);

//        boolean automatic = graphDatabaseMeta.isAutomatic();
//        boolean routing = graphDatabaseMeta.isRouting();
//        boolean encryption = graphDatabaseMeta.isUsingEncryption();

//        wlVersion4.setEnabled(!automatic);
//        wVersion4.setEnabled(!automatic);
//        wRouting.setEnabled(!automatic);
//        wlRouting.setEnabled(!automatic);
//        wRouting.setEnabled(!automatic);
//        wlEncryption.setEnabled(!automatic);
//        wEncryption.setEnabled(!automatic);
//
//        wlPolicy.setEnabled(!automatic && routing);
//        wPolicy.setEnabled(!automatic && routing);
//
//        wlTrustAllCertificates.setEnabled(!automatic && encryption);
//        wTrustAllCertificates.setEnabled(!automatic && encryption);
//        wTrustAllCertificates.getTextVar().setEnabled(!automatic && encryption);

        // Also enable/disable the custom native fields
        //
        guiCompositeWidgets.enableWidgets(
                getMetadata().getIGraphDatabase(), DatabaseMeta.GUI_PLUGIN_ELEMENT_PARENT_ID, true);
    }

    @Override
    public void setWidgetsContent() {

        GraphDatabaseMeta graphDatabaseMeta = this.getMetadata();

        wName.setText(Const.NVL(graphDatabaseMeta.getName(), ""));
//        wAutomatic.setSelection(graphDatabaseMeta.isAutomatic());
//        wAutomatic.setVariableName(Const.NVL(graphDatabaseMeta.getAutomaticVariable(), ""));
//        wProtocol.setText(Const.NVL(graphDatabaseMeta.getProtocol(), ""));
//        wServer.setText(Const.NVL(graphDatabaseMeta.getServer(), ""));
//        wDatabaseName.setText(Const.NVL(graphDatabaseMeta.getDatabaseName(), ""));
//        wVersion4.setSelection(graphDatabaseMeta.isVersion4());
//        wVersion4.setVariableName(Const.NVL(graphDatabaseMeta.getVersion4Variable(), ""));
//        wDatabasePort.setText(Const.NVL(graphDatabaseMeta.getBoltPort(), ""));
//        wBrowserPort.setText(Const.NVL(graphDatabaseMeta.getBrowserPort(), ""));
//        wRouting.setSelection(graphDatabaseMeta.isRouting());
//        wRoutingVariable.setVariableName(Const.NVL(metadata.getRoutingVariable(), ""));
//        wPolicy.setText(Const.NVL(graphDatabaseMeta.getRoutingPolicy(), ""));
//        wUsername.setText(Const.NVL(graphDatabaseMeta.getUsername(), ""));
//        wPassword.setText(Const.NVL(graphDatabaseMeta.getPassword(), ""));

//        wEncryption.setSelection(graphDatabaseMeta.isUsingEncryption());
//        wEncryption.setVariableName(Const.NVL(graphDatabaseMeta.getUsingEncryptionVariable(), ""));
//        wTrustAllCertificates.setSelection(graphDatabaseMeta.isTrustAllCertificates());
//        wTrustAllCertificates.setVariableName(
//                Const.NVL(graphDatabaseMeta.getTrustAllCertificatesVariable(), ""));
//        wConnectionLivenessCheckTimeout.setText(
//                Const.NVL(graphDatabaseMeta.getConnectionLivenessCheckTimeout(), ""));
//        wMaxConnectionLifetime.setText(Const.NVL(graphDatabaseMeta.getMaxConnectionLifetime(), ""));
//        wMaxConnectionPoolSize.setText(Const.NVL(graphDatabaseMeta.getMaxConnectionPoolSize(), ""));
//        wConnectionAcquisitionTimeout.setText(
//                Const.NVL(graphDatabaseMeta.getConnectionAcquisitionTimeout(), ""));
//        wConnectionTimeout.setText(Const.NVL(graphDatabaseMeta.getConnectionTimeout(), ""));
//        wMaxTransactionRetryTime.setText(Const.NVL(graphDatabaseMeta.getMaxTransactionRetryTime(), ""));
//        for (int i = 0; i < graphDatabaseMeta.getManualUrls().size(); i++) {
//            TableItem item = wUrls.table.getItem(i);
//            item.setText(1, Const.NVL(graphDatabaseMeta.getManualUrls().get(i), ""));
//        }
//        wUrls.setRowNums();
//        wUrls.optWidth(true);
//
//        guiCompositeWidgets.setWidgetsContents(
//                graphDatabaseMeta.getIGraphDatabase(),
//                wGraphDatabaseSpecificComp,
//                GraphDatabaseMeta.GUI_PLUGIN_ELEMENT_PARENT_ID
//        );

        updateDriverInfo();
        enableFields();
        wName.setFocus();
    }

    @Override
    public void getWidgetsContent(GraphDatabaseMeta graphDatabaseMeta) {
        graphDatabaseMeta.setName(wName.getText());

//        graphDatabaseMeta.setAutomatic(wAutomatic.getSelection());
//        graphDatabaseMeta.setAutomaticVariable(wAutomatic.getVariableName());
//        graphDatabaseMeta.setProtocol(wProtocol.getText());
//        graphDatabaseMeta.setServer(wServer.getText());
//        graphDatabaseMeta.setDatabaseName(wDatabaseName.getText());
//        graphDatabaseMeta.setVersion4(wVersion4.getSelection());
//        graphDatabaseMeta.setVersion4Variable(wVersion4.getVariableName());
//        graphDatabaseMeta.setBoltPort(wDatabasePort.getText());
//        graphDatabaseMeta.setBrowserPort(wBrowserPort.getText());
//        graphDatabaseMeta.setRouting(wRouting.getSelection());
//        graphDatabaseMeta.setRoutingVariable(wRouting.getVariableName());
//        graphDatabaseMeta.setRoutingPolicy(wPolicy.getText());
//        graphDatabaseMeta.setUsername(wUsername.getText());
//        graphDatabaseMeta.setPassword(wPassword.getText());

//        graphDatabaseMeta.setConnectionLivenessCheckTimeout(wConnectionLivenessCheckTimeout.getText());
//        graphDatabaseMeta.setMaxConnectionLifetime(wMaxConnectionLifetime.getText());
//        graphDatabaseMeta.setMaxConnectionPoolSize(wMaxConnectionPoolSize.getText());
//        graphDatabaseMeta.setConnectionAcquisitionTimeout(wConnectionAcquisitionTimeout.getText());
//        graphDatabaseMeta.setConnectionTimeout(wConnectionTimeout.getText());
//        graphDatabaseMeta.setMaxTransactionRetryTime(wMaxTransactionRetryTime.getText());
//
//        graphDatabaseMeta.getManualUrls().clear();
//        for (int i = 0; i < wUrls.nrNonEmpty(); i++) {
//            TableItem item = wUrls.getNonEmpty(i);
//            graphDatabaseMeta.getManualUrls().add(item.getText(1));
//        }
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
            GraphDatabaseTestResults testResults = graphDatabaseMeta.testConnectionSuccess(variables);
            String message = testResults.getMessage();
            boolean success = testResults.isSuccess();
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
            mb.open();        }
    }

/*
    public void test() {
        IVariables variables = manager.getVariables();
        GraphDatabaseMeta graphDatabaseMeta = new GraphDatabaseMeta(metadata);
        try {
            getWidgetsContent(graphDatabaseMeta);

            MessageBox box = new MessageBox(hopGui.getShell(), SWT.OK);
            box.setText("OK");
            String message = "Connection successful!" + Const.CR;
            message += Const.CR;
            message += "URL : " + graphDatabaseMeta.getUrl(variables);
            box.setMessage(message);
            box.open();
        } catch (Exception e) {
            new ErrorDialog(
                    hopGui.getShell(),
                    "Error",
                    "Error connecting to Neo with URL : " + graphDatabaseMeta.getUrl(variables),
                    e);
        }
    }
*/

/*
    public static final void testConnection(Shell shell, IVariables variables, GraphDatabaseMeta graphDatabaseMeta){

    }

    public Driver getDriver(ILogChannel log, IVariables variables) throws HopConfigException {

        try {
            List<URI> uris = getURIs(variables);

            String realUsername = variables.resolve(username);
            String realPassword = Encr.decryptPasswordOptionallyEncrypted(variables.resolve(password));
            Config.ConfigBuilder configBuilder;

            if (!isAutomatic(variables)) {
                if (encryptionVariableSet(variables) || usingEncryption) {
                    configBuilder = Config.builder().withEncryption();
                    if (trustAllCertificatesVariableSet(variables) || trustAllCertificates) {
                        configBuilder =
                                configBuilder.withTrustStrategy(Config.TrustStrategy.trustAllCertificates());
                    }
                } else {
                    configBuilder = Config.builder().withoutEncryption();
                }
            } else {
                configBuilder = Config.builder();
            }
            if (StringUtils.isNotEmpty(connectionLivenessCheckTimeout)) {
                long seconds = Const.toLong(variables.resolve(connectionLivenessCheckTimeout), -1L);
                if (seconds > 0) {
                    configBuilder =
                            configBuilder.withConnectionLivenessCheckTimeout(seconds, TimeUnit.MILLISECONDS);
                }
            }
            if (StringUtils.isNotEmpty(maxConnectionLifetime)) {
                long seconds = Const.toLong(variables.resolve(maxConnectionLifetime), -1L);
                if (seconds > 0) {
                    configBuilder = configBuilder.withMaxConnectionLifetime(seconds, TimeUnit.MILLISECONDS);
                }
            }
            if (StringUtils.isNotEmpty(maxConnectionPoolSize)) {
                int size = Const.toInt(variables.resolve(maxConnectionPoolSize), -1);
                if (size > 0) {
                    configBuilder = configBuilder.withMaxConnectionPoolSize(size);
                }
            }
            if (StringUtils.isNotEmpty(connectionAcquisitionTimeout)) {
                long seconds = Const.toLong(variables.resolve(connectionAcquisitionTimeout), -1L);
                if (seconds > 0) {
                    configBuilder =
                            configBuilder.withConnectionAcquisitionTimeout(seconds, TimeUnit.MILLISECONDS);
                }
            }
            if (StringUtils.isNotEmpty(connectionTimeout)) {
                long seconds = Const.toLong(variables.resolve(connectionTimeout), -1L);
                if (seconds > 0) {
                    configBuilder = configBuilder.withConnectionTimeout(seconds, TimeUnit.MILLISECONDS);
                }
            }
            if (StringUtils.isNotEmpty(maxTransactionRetryTime)) {
                long seconds = Const.toLong(variables.resolve(maxTransactionRetryTime), -1L);
                if (seconds >= 0) {
                    configBuilder = configBuilder.withMaxTransactionRetryTime(seconds, TimeUnit.MILLISECONDS);
                }
            }

            // Disable info messages: only warnings and above...
            //
            configBuilder = configBuilder.withLogging(Logging.javaUtilLogging(Level.WARNING));

            Config config = configBuilder.build();

            org.neo4j.driver.Driver driver;
            if (isUsingRouting(variables)) {
                driver =
                        org.neo4j.driver.GraphDatabase.routingDriver(uris, AuthTokens.basic(realUsername, realPassword), config);
            } else {
                driver =
                        org.neo4j.driver.GraphDatabase.driver(uris.get(0), AuthTokens.basic(realUsername, realPassword), config);
            }

            // Verify connectivity at this point to ensure we're not being dishonest when testing
            //
            driver.verifyConnectivity();

            return driver;
        } catch (URISyntaxException e) {
            throw new HopConfigException(
                    "URI syntax problem, check your settings, hostnames especially.  For routing use comma separated server values.",
                    e);
        } catch (Exception e) {
            throw new HopConfigException("Error obtaining driver for a Neo4j connection", e);
        }
    }

    public List<URI> getURIs(IVariables variables) throws URISyntaxException {

        List<URI> uris = new ArrayList<>();

        if (manualUrls != null && !manualUrls.isEmpty()) {
            // A manual URL is specified
            //
            for (String manualUrl : manualUrls) {
                uris.add(new URI(manualUrl));
            }
        } else {
            // Construct the URIs from the entered values
            //
            List<String> serverStrings = new ArrayList<>();
            String serversString = variables.resolve(server);
            if (!isAutomatic(variables) && isUsingRouting(variables)) {
                Collections.addAll(serverStrings, serversString.split(","));
            } else {
                serverStrings.add(serversString);
            }

            for (String serverString : serverStrings) {
                // Trim excess spaces from server name
                //
                String url = getUrl(Const.trim(serverString), variables);
                uris.add(new URI(url));
            }
        }

        return uris;
    }
*/


/*
    public void test(IVariables variables) throws HopException {

        try (Driver driver = getDriver(LogChannel.GENERAL, variables)) {
            SessionConfig.Builder builder = SessionConfig.builder();
            if (StringUtils.isNotEmpty(databaseName)) {
                builder = builder.withDatabase(variables.resolve(databaseName));
            }
            try (Session session = driver.session(builder.build())) {
                // Do something with the session otherwise it doesn't test the connection
                //
                Result result = session.run("RETURN 0");
                Record record = result.next();
                Value value = record.get(0);
                int zero = value.asInt();
                assert (zero == 0);
            } catch (Exception e) {
                throw new HopException(
                        "Unable to connect to database '" + name + "' : " + e.getMessage(), e);
            }
        }
    }
*/

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
