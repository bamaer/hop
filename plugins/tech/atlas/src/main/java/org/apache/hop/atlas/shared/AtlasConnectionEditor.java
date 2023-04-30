package org.apache.hop.atlas.shared;

import org.apache.hop.core.Const;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.ui.core.PropsUi;
import org.apache.hop.ui.core.dialog.ErrorDialog;
import org.apache.hop.ui.core.dialog.MessageBox;
import org.apache.hop.ui.core.metadata.MetadataEditor;
import org.apache.hop.ui.core.metadata.MetadataManager;
import org.apache.hop.ui.core.widget.PasswordTextVar;
import org.apache.hop.ui.core.widget.TextVar;
import org.apache.hop.ui.hopgui.HopGui;
import org.apache.hop.ui.hopgui.perspective.metadata.MetadataPerspective;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class AtlasConnectionEditor extends MetadataEditor<AtlasConnection> {

    private Text wName;
    private TextVar wProtocol, wHostname, wPort, wUsername, wPassword;

    private static final Class<?> PKG = AtlasConnectionEditor.class; // for Translator2
    public AtlasConnectionEditor(HopGui hopGui, MetadataManager<AtlasConnection> manager, AtlasConnection metadata) {
        super(hopGui, manager, metadata);
    }

    @Override
    public void createControl(Composite composite) {
        PropsUi props = PropsUi.getInstance();
        int middle = props.getMiddlePct();
        int margin = PropsUi.getMargin()+2;

        IVariables variables = getHopGui().getVariables();;

        Label wlName = new Label(composite, SWT.RIGHT);
        PropsUi.setLook(wlName);
        wlName.setText(BaseMessages.getString(PKG, "AtlasConnectionEditor.Name.label"));
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
        Control lastControl = wName;

        // Protocol
        Label wlProtocol = new Label(composite, SWT.RIGHT);
        wlProtocol.setText(BaseMessages.getString(PKG, "AtlasConnectionEditor.Protocol.Label"));
        wlProtocol.setToolTipText(BaseMessages.getString(PKG, "AtlasConnectionEditor.Protocol.Tooltip"));
        PropsUi.setLook(wlProtocol);
        FormData fdlProtocol = new FormData();
        fdlProtocol.top = new FormAttachment(lastControl, margin);
        fdlProtocol.left = new FormAttachment(0, 0);
        fdlProtocol.right = new FormAttachment(middle, -margin);
        wlProtocol.setLayoutData(fdlProtocol);
        wProtocol = new TextVar(variables, composite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wProtocol.setToolTipText(BaseMessages.getString(PKG, "AtlasConnectionEditor.Protocol.Tooltip"));
        PropsUi.setLook(wProtocol);
        FormData fdProtocol = new FormData();
        fdProtocol.top = new FormAttachment(wlProtocol, 0, SWT.CENTER);
        fdProtocol.left = new FormAttachment(middle, 0);
        fdProtocol.right = new FormAttachment(95, 0);
        wProtocol.setLayoutData(fdProtocol);
        lastControl = wProtocol;

        // The hostname
        Label wlHostname = new Label(composite, SWT.RIGHT);
        PropsUi.setLook(wHostname);
        wlHostname.setText(BaseMessages.getString(PKG, "AtlasConnectionEditor.Hostname.Label"));
        FormData fdlServer = new FormData();
        fdlServer.top = new FormAttachment(lastControl, margin);
        fdlServer.left = new FormAttachment(0, 0); // First one in the left top corner
        fdlServer.right = new FormAttachment(middle, -margin);
        wlHostname.setLayoutData(fdlServer);
        wHostname = new TextVar(variables, composite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        PropsUi.setLook(wHostname);
        FormData fdServer = new FormData();
        fdServer.top = new FormAttachment(wlHostname, 0, SWT.CENTER);
        fdServer.left = new FormAttachment(middle, 0); // To the right of the label
        fdServer.right = new FormAttachment(95, 0);
        wHostname.setLayoutData(fdServer);
        lastControl = wHostname;

        Label wlPort = new Label(composite, SWT.RIGHT);
        PropsUi.setLook(wlPort);
        wlPort.setText(BaseMessages.getString(PKG, "AtlasConnectionEditor.Port.Label"));
        FormData fdlDatabasePort = new FormData();
        fdlDatabasePort.top = new FormAttachment(lastControl, margin);
        fdlDatabasePort.left = new FormAttachment(0, 0); // First one in the left top corner
        fdlDatabasePort.right = new FormAttachment(middle, -margin);
        wlPort.setLayoutData(fdlDatabasePort);
        wPort = new TextVar(variables, composite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        PropsUi.setLook(wPort);
        FormData fdPort = new FormData();
        fdPort.top = new FormAttachment(wlPort, 0, SWT.CENTER);
        fdPort.left = new FormAttachment(middle, 0); // To the right of the label
        fdPort.right = new FormAttachment(95, 0);
        wPort.setLayoutData(fdPort);
        lastControl = wPort;

        // Username
        Label wlUsername = new Label(composite, SWT.RIGHT);
        wlUsername.setText(BaseMessages.getString(PKG, "AtlasConnectionEditor.Username.Label"));
        PropsUi.setLook(wlUsername);
        FormData fdlUsername = new FormData();
        fdlUsername.top = new FormAttachment(lastControl, margin);
        fdlUsername.left = new FormAttachment(0, 0);
        fdlUsername.right = new FormAttachment(middle, -margin);
        wlUsername.setLayoutData(fdlUsername);
        wUsername = new TextVar(variables, composite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        PropsUi.setLook(wUsername);
        FormData fdUsername = new FormData();
        fdUsername.top = new FormAttachment(wlUsername, 0, SWT.CENTER);
        fdUsername.left = new FormAttachment(middle, 0);
        fdUsername.right = new FormAttachment(95, 0);
        wUsername.setLayoutData(fdUsername);
        lastControl = wUsername;

        // Password
        Label wlPassword = new Label(composite, SWT.RIGHT);
        wlPassword.setText(BaseMessages.getString(PKG, "AtlasConnectionEditor.Password.Label"));
        PropsUi.setLook(wlPassword);
        FormData fdlPassword = new FormData();
        fdlPassword.top = new FormAttachment(lastControl, margin);
        fdlPassword.left = new FormAttachment(0, 0);
        fdlPassword.right = new FormAttachment(middle, -margin);
        wlPassword.setLayoutData(fdlPassword);
        wPassword = new PasswordTextVar(variables, composite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        PropsUi.setLook(wPassword);
        FormData fdPassword = new FormData();
        fdPassword.top = new FormAttachment(wlPassword, 0, SWT.CENTER);
        fdPassword.left = new FormAttachment(middle, 0);
        fdPassword.right = new FormAttachment(95, 0);
        wPassword.setLayoutData(fdPassword);

        composite.pack();

        setWidgetsContent();
        clearChanged();

        Control[] controls = {
            wName,
            wProtocol,
            wHostname,
            wPort,
            wUsername,
            wPassword
        };
        for(Control control : controls){
            control.addListener(SWT.Modify, e -> setChanged());
            control.addListener(SWT.Selection, e-> setChanged());
        }
    }

    @Override
    public void setWidgetsContent() {
        wName.setText(Const.NVL(metadata.getName(), ""));
        wProtocol.setText(Const.NVL(metadata.getProtocol(), ""));
        wHostname.setText(Const.NVL(metadata.getHostname(), ""));
        wPort.setText(Const.NVL(metadata.getPort(), ""));
        wUsername.setText(Const.NVL(metadata.getUsername(), ""));
        wPassword.setText(Const.NVL(metadata.getPassword(), ""));
    }

    @Override
    public void getWidgetsContent(AtlasConnection atlasConnection) {
        atlasConnection.setName(wName.getText());
        atlasConnection.setProtocol(wProtocol.getText());
        atlasConnection.setHostname(wHostname.getText());
        atlasConnection.setPort(wPort.getText());
        atlasConnection.setUsername(wUsername.getText());
        atlasConnection.setPassword(wPassword.getText());
    }

    @Override
    public Button[] createButtonsForButtonBar(Composite composite){
        Button wTest = new Button(composite, SWT.PUSH);
        wTest.setText(BaseMessages.getString(PKG, "System.Button.Test"));
        wTest.addListener(SWT.Selection, e -> test());
        return new Button[] {wTest};
    }

    public void test(){
        IVariables variables = manager.getVariables();
        AtlasConnection atlasConnection = new AtlasConnection();
        getWidgetsContent(atlasConnection);
        try{
            metadata.test(variables);
            MessageBox box = new MessageBox(hopGui.getShell(), SWT.OK);
            box.setText("OK");
            String message = "Connection successful" + Const.CR;
            message += Const.CR;
            message += "URL: " + wProtocol.getText() + "://" + wHostname.getText() + ":" + wPort.getText();
            box.setMessage(message);
            box.open();
        }catch(Exception e){
            new ErrorDialog(
                    hopGui.getShell(),
                    "Error",
                    "Error connecting to Apache Atlas with URL: " + wProtocol.getText() + "://" + wHostname.getText() + ":" + wPort.getText(),
                    e);
        }
    }

    public void clearChanged(){
        resetChanged();
        MetadataPerspective.getInstance().updateEditor(this);
    }
}
