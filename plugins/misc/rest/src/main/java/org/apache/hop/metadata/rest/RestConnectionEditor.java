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

package org.apache.hop.metadata.rest;

import java.util.Arrays;
import org.apache.commons.lang.StringUtils;
import org.apache.hop.core.Const;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.ui.core.PropsUi;
import org.apache.hop.ui.core.dialog.ErrorDialog;
import org.apache.hop.ui.core.dialog.MessageBox;
import org.apache.hop.ui.core.metadata.MetadataEditor;
import org.apache.hop.ui.core.metadata.MetadataManager;
import org.apache.hop.ui.core.widget.ComboVar;
import org.apache.hop.ui.core.widget.PasswordTextVar;
import org.apache.hop.ui.core.widget.TextVar;
import org.apache.hop.ui.hopgui.HopGui;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class RestConnectionEditor extends MetadataEditor<RestConnection> {
  private static final Class<?> PKG = RestConnectionEditor.class;

  private Text wName;

  private TextVar wBaseUrl;
  private TextVar wTestUrl;
  private ComboVar wAuthType;
  private static String[] authTypes = new String[] {"No Auth", "API Key", "Basic", "Bearer"};

  private Composite wAuthComp;

  // Bearer token
  private PasswordTextVar wBearerValue;

  // Basic auth token
  private TextVar wUsername;
  private TextVar wPassword;

  // API Key
  private TextVar wAuthorizationName;
  private TextVar wAuthorizationPrefix;
  private PasswordTextVar wAuthorizationValue;

  private PropsUi props;
  private int middle;
  private int margin;
  private IVariables variables;
  Control lastControl;

  public RestConnectionEditor(
      HopGui hopGui, MetadataManager<RestConnection> manager, RestConnection restConnection) {
    super(hopGui, manager, restConnection);
    props = PropsUi.getInstance();

    middle = props.getMiddlePct();
    margin = props.getMargin();
  }

  @Override
  public void createControl(Composite composite) {

    variables = hopGui.getVariables();

    // The name
    Label wlName = new Label(composite, SWT.RIGHT);
    PropsUi.setLook(wlName);
    wlName.setText(BaseMessages.getString(PKG, "RestConnectionEditor.Name"));
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
    lastControl = wName;

    Label wlBaseUrl = new Label(composite, SWT.RIGHT);
    PropsUi.setLook(wlBaseUrl);
    wlBaseUrl.setText(BaseMessages.getString(PKG, "RestConnectionEditor.BaseUrl"));
    FormData fdlBaseUrl = new FormData();
    fdlBaseUrl.top = new FormAttachment(lastControl, margin);
    fdlBaseUrl.left = new FormAttachment(0, 0);
    fdlBaseUrl.right = new FormAttachment(middle, -margin);
    wlBaseUrl.setLayoutData(fdlBaseUrl);
    wBaseUrl = new TextVar(variables, composite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    PropsUi.setLook(wBaseUrl);
    FormData fdBaseUrl = new FormData();
    fdBaseUrl.top = new FormAttachment(wlBaseUrl, 0, SWT.CENTER);
    fdBaseUrl.left = new FormAttachment(middle, 0);
    fdBaseUrl.right = new FormAttachment(95, 0);
    wBaseUrl.setLayoutData(fdBaseUrl);
    lastControl = wBaseUrl;

    Label wlTestUrl = new Label(composite, SWT.RIGHT);
    PropsUi.setLook(wlTestUrl);
    wlTestUrl.setText(BaseMessages.getString(PKG, "RestConnectionEditor.TestUrl"));
    FormData fdlTestUrl = new FormData();
    fdlTestUrl.top = new FormAttachment(lastControl, margin);
    fdlTestUrl.left = new FormAttachment(0, 0);
    fdlTestUrl.right = new FormAttachment(middle, -margin);
    wlTestUrl.setLayoutData(fdlTestUrl);
    wTestUrl = new TextVar(variables, composite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    PropsUi.setLook(wTestUrl);
    FormData fdTestUrl = new FormData();
    fdTestUrl.top = new FormAttachment(wlTestUrl, 0, SWT.CENTER);
    fdTestUrl.left = new FormAttachment(middle, 0);
    fdTestUrl.right = new FormAttachment(95, 0);
    wTestUrl.setLayoutData(fdTestUrl);
    lastControl = wTestUrl;

    Label wlAuthType = new Label(composite, SWT.RIGHT);
    PropsUi.setLook(wlAuthType);
    wlAuthType.setText(BaseMessages.getString(PKG, "RestConnectionEditor.AuthType"));
    FormData fdlAuthType = new FormData();
    fdlAuthType.top = new FormAttachment(lastControl, margin);
    fdlAuthType.left = new FormAttachment(0, 0);
    fdlAuthType.right = new FormAttachment(middle, -margin);
    wlAuthType.setLayoutData(fdlAuthType);
    wAuthType = new ComboVar(variables, composite, SWT.READ_ONLY | SWT.BORDER);
    PropsUi.setLook(wAuthType);
    FormData fdAuthType = new FormData();
    fdAuthType.top = new FormAttachment(wlAuthType, 0, SWT.CENTER);
    fdAuthType.left = new FormAttachment(middle, 0);
    fdAuthType.right = new FormAttachment(95, 0);
    wAuthType.setLayoutData(fdAuthType);
    lastControl = wAuthType;

    wAuthType.setItems(authTypes);
    wAuthType.addListener(
        SWT.Selection,
        e -> {
          if (wAuthType.getText().equals("No Auth")) {
            addNoAuthFields();
          } else if (wAuthType.getText().equals("API Key")) {
            addApiKeyFields();
          } else if (wAuthType.getText().equals("Basic")) {
            addBasicAuthFields();
          } else if (wAuthType.getText().equals("Bearer")) {
            addBearerFields();
          }
        });

    ScrolledComposite wsAuthComp = new ScrolledComposite(composite, SWT.V_SCROLL | SWT.H_SCROLL);
    props.setLook(wsAuthComp);
    FormData fdAuthSComp = new FormData();
    fdAuthSComp.top = new FormAttachment(lastControl, margin);
    fdAuthSComp.left = new FormAttachment(0, 0);
    fdAuthSComp.right = new FormAttachment(95, 0);
    fdAuthSComp.bottom = new FormAttachment(95, 0);
    wsAuthComp.setLayoutData(fdAuthSComp);

    wAuthComp = new Composite(wsAuthComp, SWT.BACKGROUND);
    PropsUi.setLook(wAuthComp);
    wAuthComp.setLayout(new FormLayout());
    FormData fdAuthComp = new FormData();
    fdAuthComp.left = new FormAttachment(0, 0);
    fdAuthComp.right = new FormAttachment(0, 0);
    fdAuthComp.top = new FormAttachment(95, 0);
    fdAuthComp.bottom = new FormAttachment(95, 0);
    wAuthComp.setLayoutData(fdAuthComp);
    wAuthComp.pack();

    wsAuthComp.setContent(wAuthComp);

    wAuthType.select(0);

    wAuthComp.layout();

    setWidgetsContent();

    wsAuthComp.setExpandHorizontal(true);
    wsAuthComp.setExpandVertical(true);
    Rectangle authCompBounds = wAuthComp.getBounds();
    wsAuthComp.setMinSize(authCompBounds.width, authCompBounds.height);

    Control[] controls = {wName, wBaseUrl, wTestUrl, wAuthComp, wAuthType};
    enableControls(controls);
  }

  private void enableControls(Control[] controls) {
    for (Control control : controls) {
      control.addListener(SWT.Modify, e -> setChanged());
      control.addListener(SWT.Selection, e -> setChanged());
    }
  }

  private void clearAuthComp() {
    for (Control child : wAuthComp.getChildren()) {
      child.dispose();
    }
  }

  private void addNoAuthFields() {
    clearAuthComp();
    wAuthComp.pack();
    wAuthComp.redraw();
  }

  private void addBasicAuthFields() {
    clearAuthComp();

    Label wlUsername = new Label(wAuthComp, SWT.RIGHT);
    props.setLook(wlUsername);
    wlUsername.setText(BaseMessages.getString(PKG, "RestConnectionEditor.Basic.Username"));
    FormData fdlUsername = new FormData();
    fdlUsername.top = new FormAttachment(0, margin);
    fdlUsername.left = new FormAttachment(0, 0);
    fdlUsername.right = new FormAttachment(middle, -margin);
    wlUsername.setLayoutData(fdlUsername);

    wUsername = new TextVar(variables, wAuthComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    props.setLook(wUsername);
    FormData fdUsername = new FormData();
    fdUsername.top = new FormAttachment(wlUsername, 0, SWT.CENTER);
    fdUsername.left = new FormAttachment(middle, 0);
    fdUsername.right = new FormAttachment(95, margin);
    wUsername.setLayoutData(fdUsername);
    lastControl = wlUsername;

    Label wlPassword = new Label(wAuthComp, SWT.RIGHT);
    wlPassword.setText(BaseMessages.getString(PKG, "RestConnectionEditor.Basic.Password"));
    FormData fdlPassword = new FormData();
    fdlPassword.top = new FormAttachment(lastControl, margin);
    fdlPassword.left = new FormAttachment(0, 0);
    fdlPassword.right = new FormAttachment(middle, -margin);
    wlPassword.setLayoutData(fdlPassword);

    wPassword = new PasswordTextVar(variables, wAuthComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    props.setLook(wPassword);
    FormData fdPassword = new FormData();
    fdPassword.top = new FormAttachment(wlPassword, 0, SWT.CENTER);
    fdPassword.left = new FormAttachment(middle, 0);
    fdPassword.right = new FormAttachment(95, 0);
    wPassword.setLayoutData(fdPassword);

    wAuthComp.pack();
    wAuthComp.redraw();

    Control[] controls = {wUsername, wPassword};
    enableControls(controls);
  }

  private void addBearerFields() {
    clearAuthComp();

    Label wlBearer = new Label(wAuthComp, SWT.RIGHT);
    PropsUi.setLook(wlBearer);
    wlBearer.setText(BaseMessages.getString(PKG, "RestConnectionEditor.Bearer.Token"));
    FormData fdlBearer = new FormData();
    fdlBearer.top = new FormAttachment(0, margin);
    fdlBearer.left = new FormAttachment(0, 0);
    fdlBearer.right = new FormAttachment(middle, -margin);
    wlBearer.setLayoutData(fdlBearer);

    wBearerValue = new PasswordTextVar(variables, wAuthComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    PropsUi.setLook(wBearerValue);
    FormData fdBearer = new FormData();
    fdBearer.top = new FormAttachment(wlBearer, 0, SWT.CENTER);
    fdBearer.left = new FormAttachment(middle, 0);
    fdBearer.right = new FormAttachment(95, 0);
    wBearerValue.setLayoutData(fdBearer);

    wAuthComp.pack();

    Control[] controls = {wBearerValue};
    enableControls(controls);
  }

  private void addApiKeyFields() {
    clearAuthComp();

    Label wlAuthorizationName = new Label(wAuthComp, SWT.RIGHT);
    PropsUi.setLook(wlAuthorizationName);
    wlAuthorizationName.setText(
        BaseMessages.getString(PKG, "RestConnectionEditor.API.AuthorizationName"));
    FormData fdlAuthorizationName = new FormData();
    fdlAuthorizationName.top = new FormAttachment(0, margin);
    fdlAuthorizationName.left = new FormAttachment(0, 0);
    fdlAuthorizationName.right = new FormAttachment(middle, -margin);
    wlAuthorizationName.setLayoutData(fdlAuthorizationName);

    wAuthorizationName = new TextVar(variables, wAuthComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    PropsUi.setLook(wAuthorizationName);
    FormData fdAuthorizationName = new FormData();
    fdAuthorizationName.top = new FormAttachment(wlAuthorizationName, 0, SWT.CENTER);
    fdAuthorizationName.left = new FormAttachment(middle, 0);
    fdAuthorizationName.right = new FormAttachment(95, 0);
    wAuthorizationName.setLayoutData(fdAuthorizationName);
    lastControl = wAuthorizationName;

    Label wlAuthorizationPrefix = new Label(wAuthComp, SWT.RIGHT);
    PropsUi.setLook(wlAuthorizationPrefix);
    wlAuthorizationPrefix.setText(
        BaseMessages.getString(PKG, "RestConnectionEditor.API.AuthorizationPrefix"));
    FormData fdlAuthorizationPrefix = new FormData();
    fdlAuthorizationPrefix.top = new FormAttachment(lastControl, margin);
    fdlAuthorizationPrefix.left = new FormAttachment(0, 0);
    fdlAuthorizationPrefix.right = new FormAttachment(middle, -margin);
    wlAuthorizationPrefix.setLayoutData(fdlAuthorizationPrefix);

    wAuthorizationPrefix = new TextVar(variables, wAuthComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    PropsUi.setLook(wAuthorizationPrefix);
    FormData fdAuthorizationPrefix = new FormData();
    fdAuthorizationPrefix.top = new FormAttachment(wlAuthorizationPrefix, 0, SWT.CENTER);
    fdAuthorizationPrefix.left = new FormAttachment(middle, 0);
    fdAuthorizationPrefix.right = new FormAttachment(95, 0);
    wAuthorizationPrefix.setLayoutData(fdAuthorizationPrefix);
    lastControl = wAuthorizationPrefix;

    Label wlAuthorizationValue = new Label(wAuthComp, SWT.RIGHT);
    PropsUi.setLook(wlAuthorizationValue);
    wlAuthorizationValue.setText(
        BaseMessages.getString(PKG, "RestConnectionEditor.API.AuthorizationValue"));
    FormData fdlAuthorizationValue = new FormData();
    fdlAuthorizationValue.top = new FormAttachment(lastControl, margin);
    fdlAuthorizationValue.left = new FormAttachment(0, 0);
    fdlAuthorizationValue.right = new FormAttachment(middle, -margin);
    wlAuthorizationValue.setLayoutData(fdlAuthorizationValue);
    wAuthorizationValue =
        new PasswordTextVar(variables, wAuthComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    PropsUi.setLook(wAuthorizationValue);
    FormData fdAuthorizationValue = new FormData();
    fdAuthorizationValue.top = new FormAttachment(wlAuthorizationValue, 0, SWT.CENTER);
    fdAuthorizationValue.left = new FormAttachment(middle, 0);
    fdAuthorizationValue.right = new FormAttachment(95, 0);
    wAuthorizationValue.setLayoutData(fdAuthorizationValue);

    wAuthComp.pack();

    Control[] controls = {wAuthorizationName, wAuthorizationPrefix, wAuthorizationValue};
    enableControls(controls);
  }

  @Override
  public Button[] createButtonsForButtonBar(Composite composite) {
    Button wTest = new Button(composite, SWT.PUSH);
    wTest.setText(BaseMessages.getString(PKG, "System.Button.Test"));
    wTest.addListener(SWT.Selection, e -> test());

    return new Button[] {wTest};
  }

  private void test() {
    IVariables variables = hopGui.getVariables();
    RestConnection restConnection = new RestConnection(variables);
    restConnection.setName(wName.getText());
    if (StringUtils.isEmpty(wTestUrl.getText())) {
      restConnection.setTestUrl(wBaseUrl.getText());
    }
    restConnection.setBaseUrl(wBaseUrl.getText());
    restConnection.setTestUrl(wTestUrl.getText());
    restConnection.setAuthType(wAuthType.getText());
    if (wAuthType.getText().equals("No Auth")) {
      // nothing required
    } else if (wAuthType.getText().equals("Basic")) {
      restConnection.setUsername(wUsername.getText());
      restConnection.setPassword(wPassword.getText());
    } else if (wAuthType.getText().equals("Bearer")) {
      restConnection.setBearerToken(wBearerValue.getText());
    } else if (wAuthType.getText().equals("API Key")) {
      restConnection.setAuthorizationHeaderName(wAuthorizationName.getText());
      restConnection.setAuthorizationPrefix(wAuthorizationPrefix.getText());
      restConnection.setAuthorizationHeaderValue(wAuthorizationValue.getText());
    }
    try {
      restConnection.testConnection();
      MessageBox box = new MessageBox(hopGui.getShell(), SWT.OK);
      box.setText("OK");
      String message =
          BaseMessages.getString(PKG, "RestConnectionEditor.ConnectionTestSuccess") + Const.CR;
      message += Const.CR;
      message += "URL : " + wTestUrl.getText();
      box.setMessage(message);
      box.open();
    } catch (Exception e) {
      new ErrorDialog(
          hopGui.getShell(), "Error", "Error connecting to REST URL : " + wTestUrl.getText(), e);
    }
  }

  @Override
  public void dispose() {}

  @Override
  public void setWidgetsContent() {
    // backwards compatibility: if we have authorization header values but no Auth Type,
    // consider this to be an API Key auth
    if (!StringUtils.isEmpty(metadata.getAuthorizationHeaderName())
        && !StringUtils.isEmpty(metadata.getAuthorizationHeaderValue())
        && StringUtils.isEmpty(metadata.getAuthType())) {
      metadata.setAuthType("API Key");
    }

    wName.setText(Const.NVL(metadata.getName(), ""));
    wBaseUrl.setText(Const.NVL(metadata.getBaseUrl(), ""));
    wTestUrl.setText(Const.NVL(metadata.getTestUrl(), ""));
    // wAuthType.setText(Const.NVL(metadata.getAuthType(), ""));
    if (StringUtils.isEmpty(metadata.getAuthType())) {
      metadata.setAuthType("No Auth");
      wAuthType.select(0);
    } else {
      wAuthType.select(Arrays.asList(authTypes).indexOf(metadata.getAuthType()));
    }
    if (metadata.getAuthType().equals("Basic")) {
      addBasicAuthFields();
      wUsername.setText(Const.NVL(metadata.getUsername(), ""));
      wPassword.setText(Const.NVL(metadata.getPassword(), ""));
    } else if (metadata.getAuthType().equals("Bearer")) {
      addBearerFields();
      wBearerValue.setText(metadata.getBearerToken());
    } else if (metadata.getAuthType().equals("API Key")) {
      addApiKeyFields();
      wAuthorizationName.setText(Const.NVL(metadata.getAuthorizationHeaderName(), ""));
      wAuthorizationPrefix.setText(Const.NVL(metadata.getAuthorizationPrefix(), ""));
      wAuthorizationValue.setText(Const.NVL(metadata.getAuthorizationHeaderValue(), ""));
    }
  }

  @Override
  public void getWidgetsContent(RestConnection connection) {
    connection.setName(wName.getText());
    connection.setBaseUrl(wBaseUrl.getText());
    connection.setTestUrl(wTestUrl.getText());
    connection.setAuthType(wAuthType.getText());
    if (wAuthType.getText().equals("Basic")) {
      connection.setUsername(wUsername.getText());
      connection.setPassword(wPassword.getText());
    } else if (wAuthType.getText().equals("Bearer")) {
      connection.setBearerToken(wBearerValue.getText());
    } else if (wAuthType.getText().equals("API Key")) {
      connection.setAuthorizationHeaderName(wAuthorizationName.getText());
      connection.setAuthorizationPrefix(wAuthorizationPrefix.getText());
      connection.setAuthorizationHeaderValue(wAuthorizationValue.getText());
    }
  }

  @Override
  public boolean setFocus() {
    if (wName == null || wName.isDisposed()) {
      return false;
    }
    return wName.setFocus();
  }
}
