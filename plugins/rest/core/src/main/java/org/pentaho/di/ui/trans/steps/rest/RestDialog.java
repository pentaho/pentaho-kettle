/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2023 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.ui.trans.steps.rest;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.rest.RestMeta;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.events.dialog.FilterType;
import org.pentaho.di.ui.core.events.dialog.SelectionAdapterFileDialogTextVar;
import org.pentaho.di.ui.core.events.dialog.SelectionAdapterOptions;
import org.pentaho.di.ui.core.events.dialog.SelectionOperation;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.ComboVar;
import org.pentaho.di.ui.core.widget.PasswordTextVar;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RestDialog extends BaseStepDialog implements StepDialogInterface {
  private static Class<?> PKG = RestMeta.class; // for i18n purposes, needed by Translator2!!

  private Label wlApplicationType;
  private ComboVar wApplicationType;
  private FormData fdlApplicationType, fdApplicationType;

  private Label wlMethod;
  private ComboVar wMethod;
  private FormData fdlMethod, fdMethod;

  private Label wlUrl;
  private TextVar wUrl;
  private FormData fdlUrl, fdUrl;

  private Label wlResult;
  private TextVar wResult;
  private FormData fdlResult, fdResult;

  private Label wlResultCode;
  private TextVar wResultCode;
  private FormData fdlResultCode, fdResultCode;

  private Label wlFields;
  private TableView wFields;
  private FormData fdlFields, fdFields;

  private Label wlUrlInField;
  private Button wUrlInField;
  private FormData fdlUrlInField, fdUrlInField;

  private Label wlUrlField;
  private ComboVar wUrlField;
  private FormData fdlUrlField, fdUrlField;

  private Label wlMethodInField;
  private Button wMethodInField;
  private FormData fdlMethodInField, fdMethodInField;

  private Label wlPreemptive;
  private Button wPreemptive;
  private FormData fdlPreemptive, fdPreemptive;

  private Label wlMethodField;
  private ComboVar wMethodField;
  private FormData fdlMethodField, fdMethodField;

  private RestMeta input;

  private Map<String, Integer> inputFields;

  private Label wlBody;
  private ComboVar wBody;
  private FormData fdlBody, fdBody;

  private Button wGetHeaders;

  private ColumnInfo[] colinf, colinfoparams;

  private String[] fieldNames;

  private Label wlHttpLogin;
  private TextVar wHttpLogin;

  private Label wlHttpPassword;
  private TextVar wHttpPassword;

  private Label wlProxyHost;
  private TextVar wProxyHost;

  private Label wlProxyPort;
  private TextVar wProxyPort;

  protected CTabFolder wTabFolder;

  private CTabItem wGeneralTab, wAdditionalTab, wParametersTab, wMatrixParametersTab, wAuthTab;
  protected CTabItem wSSLTab;
  private FormData fdTabFolder;

  private Composite wGeneralComp, wAdditionalComp;
  private FormData fdGeneralComp, fdAdditionalComp;

  private Composite wParametersComp, wMatrixParametersComp;
  private FormData fdParametersComp, fdMatrixParametersComp;

  private Composite wAuthComp;
  private FormData fdAuthComp;

  protected Composite wSSLComp;
  private FormData fdSSLComp;

  private Label wlParameters, wlMatrixParameters;
  private TableView wParameters, wMatrixParameters;
  private FormData fdlParameters, fdlMatrixParameters, fdParameters, fdMatrixParameters;

  private Label wlResponseTime;
  private TextVar wResponseTime;
  private FormData fdlResponseTime, fdResponseTime;
  private Label wlResponseHeader;
  private TextVar wResponseHeader;
  private FormData fdlResponseHeader, fdResponseHeader;

  private Label wlTrustStorePassword;
  private TextVar wTrustStorePassword;
  private FormData fdlTrustStorePassword, fdTrustStorePassword;

  private Label wlTrustStoreFile;
  private TextVar wTrustStoreFile;
  private Button wbTrustStoreFile;
  private FormData fdbTrustStoreFile;
  private FormData fdlTrustStoreFile, fdTrustStoreFile;

  private boolean gotPreviousFields = false;

  private Button wMatrixGet;
  private Listener lsMatrixGet;

  protected Group gSSLTrustStore;


  public RestDialog( Shell parent, Object in, TransMeta transMeta, String sname ) {
    super( parent, (BaseStepMeta) in, transMeta, sname );
    input = (RestMeta) in;
    inputFields = new HashMap<String, Integer>();
  }

  public String open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );
    props.setLook( shell );
    setShellImage( shell, input );

    ModifyListener lsMod = new ModifyListener() {
      public void modifyText( ModifyEvent e ) {
        input.setChanged();
      }
    };

    changed = input.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout( formLayout );
    shell.setText( BaseMessages.getString( PKG, "RestDialog.Shell.Title" ) );

    int middle = props.getMiddlePct();
    int margin = Const.MARGIN;

    // Stepname line
    wlStepname = new Label( shell, SWT.RIGHT );
    wlStepname.setText( BaseMessages.getString( PKG, "RestDialog.Stepname.Label" ) );
    props.setLook( wlStepname );
    fdlStepname = new FormData();
    fdlStepname.left = new FormAttachment( 0, 0 );
    fdlStepname.right = new FormAttachment( middle, -margin );
    fdlStepname.top = new FormAttachment( 0, margin );
    wlStepname.setLayoutData( fdlStepname );
    wStepname = new Text( shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wStepname.setText( stepname );
    props.setLook( wStepname );
    wStepname.addModifyListener( lsMod );
    fdStepname = new FormData();
    fdStepname.left = new FormAttachment( middle, 0 );
    fdStepname.top = new FormAttachment( 0, margin );
    fdStepname.right = new FormAttachment( 100, 0 );
    wStepname.setLayoutData( fdStepname );

    wTabFolder = new CTabFolder( shell, SWT.BORDER );
    props.setLook( wTabFolder, PropsUI.WIDGET_STYLE_TAB );

    // ////////////////////////
    // START OF GENERAL TAB ///
    // ////////////////////////
    wGeneralTab = new CTabItem( wTabFolder, SWT.NONE );
    wGeneralTab.setText( BaseMessages.getString( PKG, "RestDialog.GeneralTab.Title" ) );

    wGeneralComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wGeneralComp );

    FormLayout fileLayout = new FormLayout();
    fileLayout.marginWidth = 3;
    fileLayout.marginHeight = 3;
    wGeneralComp.setLayout( fileLayout );

    // ////////////////////////
    // START Settings GROUP

    Group gSettings = new Group( wGeneralComp, SWT.SHADOW_ETCHED_IN );
    gSettings.setText( BaseMessages.getString( PKG, "RestDialog.SettingsGroup.Label" ) );
    FormLayout SettingsLayout = new FormLayout();
    SettingsLayout.marginWidth = 3;
    SettingsLayout.marginHeight = 3;
    gSettings.setLayout( SettingsLayout );
    props.setLook( gSettings );

    wlUrl = new Label( gSettings, SWT.RIGHT );
    wlUrl.setText( BaseMessages.getString( PKG, "RestDialog.URL.Label" ) );
    props.setLook( wlUrl );
    fdlUrl = new FormData();
    fdlUrl.left = new FormAttachment( 0, 0 );
    fdlUrl.right = new FormAttachment( middle, -margin );
    fdlUrl.top = new FormAttachment( wGeneralComp, margin * 2 );
    wlUrl.setLayoutData( fdlUrl );

    wUrl = new TextVar( transMeta, gSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wUrl );
    wUrl.addModifyListener( lsMod );
    fdUrl = new FormData();
    fdUrl.left = new FormAttachment( middle, 0 );
    fdUrl.top = new FormAttachment( wGeneralComp, margin * 2 );
    fdUrl.right = new FormAttachment( 100, 0 );
    wUrl.setLayoutData( fdUrl );

    // UrlInField line
    wlUrlInField = new Label( gSettings, SWT.RIGHT );
    wlUrlInField.setText( BaseMessages.getString( PKG, "RestDialog.UrlInField.Label" ) );
    props.setLook( wlUrlInField );
    fdlUrlInField = new FormData();
    fdlUrlInField.left = new FormAttachment( 0, 0 );
    fdlUrlInField.top = new FormAttachment( wUrl, margin );
    fdlUrlInField.right = new FormAttachment( middle, -margin );
    wlUrlInField.setLayoutData( fdlUrlInField );
    wUrlInField = new Button( gSettings, SWT.CHECK );
    props.setLook( wUrlInField );
    fdUrlInField = new FormData();
    fdUrlInField.left = new FormAttachment( middle, 0 );
    fdUrlInField.top = new FormAttachment( wUrl, margin );
    fdUrlInField.right = new FormAttachment( 100, 0 );
    wUrlInField.setLayoutData( fdUrlInField );
    wUrlInField.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
        activeUrlInfield();
      }
    } );

    // UrlField Line
    wlUrlField = new Label( gSettings, SWT.RIGHT );
    wlUrlField.setText( BaseMessages.getString( PKG, "RestDialog.UrlField.Label" ) );
    props.setLook( wlUrlField );
    fdlUrlField = new FormData();
    fdlUrlField.left = new FormAttachment( 0, 0 );
    fdlUrlField.right = new FormAttachment( middle, -margin );
    fdlUrlField.top = new FormAttachment( wUrlInField, margin );
    wlUrlField.setLayoutData( fdlUrlField );

    wUrlField = new ComboVar( transMeta, gSettings, SWT.BORDER | SWT.READ_ONLY );
    wUrlField.setEditable( true );
    props.setLook( wUrlField );
    wUrlField.addModifyListener( lsMod );
    fdUrlField = new FormData();
    fdUrlField.left = new FormAttachment( middle, 0 );
    fdUrlField.top = new FormAttachment( wUrlInField, margin );
    fdUrlField.right = new FormAttachment( 100, -margin );
    wUrlField.setLayoutData( fdUrlField );
    wUrlField.addFocusListener( new FocusListener() {
      public void focusLost( org.eclipse.swt.events.FocusEvent e ) {
      }

      public void focusGained( org.eclipse.swt.events.FocusEvent e ) {
        Cursor busy = new Cursor( shell.getDisplay(), SWT.CURSOR_WAIT );
        shell.setCursor( busy );
        setStreamFields();
        shell.setCursor( null );
        busy.dispose();
      }
    } );

    // Method Line
    wlMethod = new Label( gSettings, SWT.RIGHT );
    wlMethod.setText( BaseMessages.getString( PKG, "RestDialog.Method.Label" ) );
    props.setLook( wlMethod );
    fdlMethod = new FormData();
    fdlMethod.left = new FormAttachment( 0, 0 );
    fdlMethod.right = new FormAttachment( middle, -margin );
    fdlMethod.top = new FormAttachment( wUrlField, 2 * margin );
    wlMethod.setLayoutData( fdlMethod );

    wMethod = new ComboVar( transMeta, gSettings, SWT.BORDER | SWT.READ_ONLY );
    wMethod.setEditable( true );
    props.setLook( wMethod );
    wMethod.addModifyListener( lsMod );
    fdMethod = new FormData();
    fdMethod.left = new FormAttachment( middle, 0 );
    fdMethod.top = new FormAttachment( wUrlField, 2 * margin );
    fdMethod.right = new FormAttachment( 100, -margin );
    wMethod.setLayoutData( fdMethod );
    wMethod.setItems( RestMeta.HTTP_METHODS );
    wMethod.addSelectionListener( new SelectionAdapter() {

      public void widgetSelected( SelectionEvent e ) {
        setMethod();
      }
    } );

    // MethodInField line
    wlMethodInField = new Label( gSettings, SWT.RIGHT );
    wlMethodInField.setText( BaseMessages.getString( PKG, "RestDialog.MethodInField.Label" ) );
    props.setLook( wlMethodInField );
    fdlMethodInField = new FormData();
    fdlMethodInField.left = new FormAttachment( 0, 0 );
    fdlMethodInField.top = new FormAttachment( wMethod, margin );
    fdlMethodInField.right = new FormAttachment( middle, -margin );
    wlMethodInField.setLayoutData( fdlMethodInField );
    wMethodInField = new Button( gSettings, SWT.CHECK );
    props.setLook( wMethodInField );
    fdMethodInField = new FormData();
    fdMethodInField.left = new FormAttachment( middle, 0 );
    fdMethodInField.top = new FormAttachment( wMethod, margin );
    fdMethodInField.right = new FormAttachment( 100, 0 );
    wMethodInField.setLayoutData( fdMethodInField );
    wMethodInField.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
        activeMethodInfield();
      }
    } );

    // MethodField Line
    wlMethodField = new Label( gSettings, SWT.RIGHT );
    wlMethodField.setText( BaseMessages.getString( PKG, "RestDialog.MethodField.Label" ) );
    props.setLook( wlMethodField );
    fdlMethodField = new FormData();
    fdlMethodField.left = new FormAttachment( 0, 0 );
    fdlMethodField.right = new FormAttachment( middle, -margin );
    fdlMethodField.top = new FormAttachment( wMethodInField, margin );
    wlMethodField.setLayoutData( fdlMethodField );

    wMethodField = new ComboVar( transMeta, gSettings, SWT.BORDER | SWT.READ_ONLY );
    wMethodField.setEditable( true );
    props.setLook( wMethodField );
    wMethodField.addModifyListener( lsMod );
    fdMethodField = new FormData();
    fdMethodField.left = new FormAttachment( middle, 0 );
    fdMethodField.top = new FormAttachment( wMethodInField, margin );
    fdMethodField.right = new FormAttachment( 100, -margin );
    wMethodField.setLayoutData( fdMethodField );
    wMethodField.addFocusListener( new FocusListener() {
      public void focusLost( org.eclipse.swt.events.FocusEvent e ) {
      }

      public void focusGained( org.eclipse.swt.events.FocusEvent e ) {
        Cursor busy = new Cursor( shell.getDisplay(), SWT.CURSOR_WAIT );
        shell.setCursor( busy );
        setStreamFields();
        shell.setCursor( null );
        busy.dispose();
      }
    } );

    // Body Line
    wlBody = new Label( gSettings, SWT.RIGHT );
    wlBody.setText( BaseMessages.getString( PKG, "RestDialog.Body.Label" ) );
    props.setLook( wlBody );
    fdlBody = new FormData();
    fdlBody.left = new FormAttachment( 0, 0 );
    fdlBody.right = new FormAttachment( middle, -margin );
    fdlBody.top = new FormAttachment( wMethodField, 2 * margin );
    wlBody.setLayoutData( fdlBody );

    wBody = new ComboVar( transMeta, gSettings, SWT.BORDER | SWT.READ_ONLY );
    wBody.setEditable( true );
    props.setLook( wBody );
    wBody.addModifyListener( lsMod );
    fdBody = new FormData();
    fdBody.left = new FormAttachment( middle, 0 );
    fdBody.top = new FormAttachment( wMethodField, 2 * margin );
    fdBody.right = new FormAttachment( 100, -margin );
    wBody.setLayoutData( fdBody );
    wBody.addFocusListener( new FocusListener() {
      public void focusLost( org.eclipse.swt.events.FocusEvent e ) {
      }

      public void focusGained( org.eclipse.swt.events.FocusEvent e ) {
        Cursor busy = new Cursor( shell.getDisplay(), SWT.CURSOR_WAIT );
        shell.setCursor( busy );
        setStreamFields();
        shell.setCursor( null );
        busy.dispose();
      }
    } );

    // ApplicationType Line
    wlApplicationType = new Label( gSettings, SWT.RIGHT );
    wlApplicationType.setText( BaseMessages.getString( PKG, "RestDialog.ApplicationType.Label" ) );
    props.setLook( wlApplicationType );
    fdlApplicationType = new FormData();
    fdlApplicationType.left = new FormAttachment( 0, 0 );
    fdlApplicationType.right = new FormAttachment( middle, -margin );
    fdlApplicationType.top = new FormAttachment( wBody, 2 * margin );
    wlApplicationType.setLayoutData( fdlApplicationType );

    wApplicationType = new ComboVar( transMeta, gSettings, SWT.BORDER | SWT.READ_ONLY );
    wApplicationType.setEditable( true );
    props.setLook( wApplicationType );
    wApplicationType.addModifyListener( lsMod );
    fdApplicationType = new FormData();
    fdApplicationType.left = new FormAttachment( middle, 0 );
    fdApplicationType.top = new FormAttachment( wBody, 2 * margin );
    fdApplicationType.right = new FormAttachment( 100, -margin );
    wApplicationType.setLayoutData( fdApplicationType );
    wApplicationType.setItems( RestMeta.APPLICATION_TYPES );
    wApplicationType.addSelectionListener( new SelectionAdapter() {

      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
      }
    } );

    FormData fdSettings = new FormData();
    fdSettings.left = new FormAttachment( 0, 0 );
    fdSettings.right = new FormAttachment( 100, 0 );
    fdSettings.top = new FormAttachment( wStepname, margin );
    gSettings.setLayoutData( fdSettings );

    // END Output Settings GROUP
    // ////////////////////////

    // ////////////////////////
    // START Output Fields GROUP

    Group gOutputFields = new Group( wGeneralComp, SWT.SHADOW_ETCHED_IN );
    gOutputFields.setText( BaseMessages.getString( PKG, "RestDialog.OutputFieldsGroup.Label" ) );
    FormLayout OutputFieldsLayout = new FormLayout();
    OutputFieldsLayout.marginWidth = 3;
    OutputFieldsLayout.marginHeight = 3;
    gOutputFields.setLayout( OutputFieldsLayout );
    props.setLook( gOutputFields );

    // Result line...
    wlResult = new Label( gOutputFields, SWT.RIGHT );
    wlResult.setText( BaseMessages.getString( PKG, "RestDialog.Result.Label" ) );
    props.setLook( wlResult );
    fdlResult = new FormData();
    fdlResult.left = new FormAttachment( 0, 0 );
    fdlResult.right = new FormAttachment( middle, -margin );
    fdlResult.top = new FormAttachment( gSettings, margin );
    wlResult.setLayoutData( fdlResult );
    wResult = new TextVar( transMeta, gOutputFields, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wResult );
    wResult.addModifyListener( lsMod );
    fdResult = new FormData();
    fdResult.left = new FormAttachment( middle, 0 );
    fdResult.top = new FormAttachment( gSettings, margin * 2 );
    fdResult.right = new FormAttachment( 100, -margin );
    wResult.setLayoutData( fdResult );

    // Resultcode line...
    wlResultCode = new Label( gOutputFields, SWT.RIGHT );
    wlResultCode.setText( BaseMessages.getString( PKG, "RestDialog.ResultCode.Label" ) );
    props.setLook( wlResultCode );
    fdlResultCode = new FormData();
    fdlResultCode.left = new FormAttachment( 0, 0 );
    fdlResultCode.right = new FormAttachment( middle, -margin );
    fdlResultCode.top = new FormAttachment( wResult, margin );
    wlResultCode.setLayoutData( fdlResultCode );
    wResultCode = new TextVar( transMeta, gOutputFields, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wResultCode );
    wResultCode.addModifyListener( lsMod );
    fdResultCode = new FormData();
    fdResultCode.left = new FormAttachment( middle, 0 );
    fdResultCode.top = new FormAttachment( wResult, margin );
    fdResultCode.right = new FormAttachment( 100, -margin );
    wResultCode.setLayoutData( fdResultCode );

    // Response time line...
    wlResponseTime = new Label( gOutputFields, SWT.RIGHT );
    wlResponseTime.setText( BaseMessages.getString( PKG, "RestDialog.ResponseTime.Label" ) );
    props.setLook( wlResponseTime );
    fdlResponseTime = new FormData();
    fdlResponseTime.left = new FormAttachment( 0, 0 );
    fdlResponseTime.right = new FormAttachment( middle, -margin );
    fdlResponseTime.top = new FormAttachment( wResultCode, margin );
    wlResponseTime.setLayoutData( fdlResponseTime );
    wResponseTime = new TextVar( transMeta, gOutputFields, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wResponseTime );
    wResponseTime.addModifyListener( lsMod );
    fdResponseTime = new FormData();
    fdResponseTime.left = new FormAttachment( middle, 0 );
    fdResponseTime.top = new FormAttachment( wResultCode, margin );
    fdResponseTime.right = new FormAttachment( 100, 0 );
    wResponseTime.setLayoutData( fdResponseTime );
    // Response header line...
    wlResponseHeader = new Label( gOutputFields, SWT.RIGHT );
    wlResponseHeader.setText( BaseMessages.getString( PKG, "RestDialog.ResponseHeader.Label" ) );
    props.setLook( wlResponseHeader );
    fdlResponseHeader = new FormData();
    fdlResponseHeader.left = new FormAttachment( 0, 0 );
    fdlResponseHeader.right = new FormAttachment( middle, -margin );
    fdlResponseHeader.top = new FormAttachment( wResponseTime, margin );
    wlResponseHeader.setLayoutData( fdlResponseHeader );
    wResponseHeader = new TextVar( transMeta, gOutputFields, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wResponseHeader );
    wResponseHeader.addModifyListener( lsMod );
    fdResponseHeader = new FormData();
    fdResponseHeader.left = new FormAttachment( middle, 0 );
    fdResponseHeader.top = new FormAttachment( wResponseTime, margin );
    fdResponseHeader.right = new FormAttachment( 100, 0 );
    wResponseHeader.setLayoutData( fdResponseHeader );

    FormData fdOutputFields = new FormData();
    fdOutputFields.left = new FormAttachment( 0, 0 );
    fdOutputFields.right = new FormAttachment( 100, 0 );
    fdOutputFields.top = new FormAttachment( gSettings, margin );
    gOutputFields.setLayoutData( fdOutputFields );

    // END Output Fields GROUP
    // ////////////////////////

    fdGeneralComp = new FormData();
    fdGeneralComp.left = new FormAttachment( 0, 0 );
    fdGeneralComp.top = new FormAttachment( wStepname, margin );
    fdGeneralComp.right = new FormAttachment( 100, 0 );
    fdGeneralComp.bottom = new FormAttachment( 100, 0 );
    wGeneralComp.setLayoutData( fdGeneralComp );

    wGeneralComp.layout();
    wGeneralTab.setControl( wGeneralComp );

    // ///////////////////////////////////////////////////////////
    // / END OF GENERAL TAB
    // ///////////////////////////////////////////////////////////

    // Auth tab...
    //
    wAuthTab = new CTabItem( wTabFolder, SWT.NONE );
    wAuthTab.setText( BaseMessages.getString( PKG, "RestDialog.Auth.Title" ) );

    FormLayout alayout = new FormLayout();
    alayout.marginWidth = Const.FORM_MARGIN;
    alayout.marginHeight = Const.FORM_MARGIN;

    wAuthComp = new Composite( wTabFolder, SWT.NONE );
    wAuthComp.setLayout( alayout );
    props.setLook( wAuthComp );

    // ////////////////////////
    // START HTTP AUTH GROUP

    Group gHttpAuth = new Group( wAuthComp, SWT.SHADOW_ETCHED_IN );
    gHttpAuth.setText( BaseMessages.getString( PKG, "RestDialog.HttpAuthGroup.Label" ) );
    FormLayout httpAuthLayout = new FormLayout();
    httpAuthLayout.marginWidth = 3;
    httpAuthLayout.marginHeight = 3;
    gHttpAuth.setLayout( httpAuthLayout );
    props.setLook( gHttpAuth );

    // HTTP Login
    wlHttpLogin = new Label( gHttpAuth, SWT.RIGHT );
    wlHttpLogin.setText( BaseMessages.getString( PKG, "RestDialog.HttpLogin.Label" ) );
    props.setLook( wlHttpLogin );
    FormData fdlHttpLogin = new FormData();
    fdlHttpLogin.top = new FormAttachment( 0, margin );
    fdlHttpLogin.left = new FormAttachment( 0, 0 );
    fdlHttpLogin.right = new FormAttachment( middle, -margin );
    wlHttpLogin.setLayoutData( fdlHttpLogin );
    wHttpLogin = new TextVar( transMeta, gHttpAuth, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wHttpLogin.addModifyListener( lsMod );
    wHttpLogin.setToolTipText( BaseMessages.getString( PKG, "RestDialog.HttpLogin.Tooltip" ) );
    props.setLook( wHttpLogin );
    FormData fdHttpLogin = new FormData();
    fdHttpLogin.top = new FormAttachment( 0, margin );
    fdHttpLogin.left = new FormAttachment( middle, 0 );
    fdHttpLogin.right = new FormAttachment( 100, 0 );
    wHttpLogin.setLayoutData( fdHttpLogin );

    // HTTP Password
    wlHttpPassword = new Label( gHttpAuth, SWT.RIGHT );
    wlHttpPassword.setText( BaseMessages.getString( PKG, "RestDialog.HttpPassword.Label" ) );
    props.setLook( wlHttpPassword );
    FormData fdlHttpPassword = new FormData();
    fdlHttpPassword.top = new FormAttachment( wHttpLogin, margin );
    fdlHttpPassword.left = new FormAttachment( 0, 0 );
    fdlHttpPassword.right = new FormAttachment( middle, -margin );
    wlHttpPassword.setLayoutData( fdlHttpPassword );
    wHttpPassword = new PasswordTextVar( transMeta, gHttpAuth, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wHttpPassword.addModifyListener( lsMod );
    wHttpPassword.setToolTipText( BaseMessages.getString( PKG, "RestDialog.HttpPassword.Tooltip" ) );
    props.setLook( wHttpPassword );
    FormData fdHttpPassword = new FormData();
    fdHttpPassword.top = new FormAttachment( wHttpLogin, margin );
    fdHttpPassword.left = new FormAttachment( middle, 0 );
    fdHttpPassword.right = new FormAttachment( 100, 0 );
    wHttpPassword.setLayoutData( fdHttpPassword );

    // Preemptive line
    wlPreemptive = new Label( gHttpAuth, SWT.RIGHT );
    wlPreemptive.setText( BaseMessages.getString( PKG, "RestDialog.Preemptive.Label" ) );
    props.setLook( wlPreemptive );
    fdlPreemptive = new FormData();
    fdlPreemptive.left = new FormAttachment( 0, 0 );
    fdlPreemptive.top = new FormAttachment( wHttpPassword, margin );
    fdlPreemptive.right = new FormAttachment( middle, -margin );
    wlPreemptive.setLayoutData( fdlPreemptive );
    wPreemptive = new Button( gHttpAuth, SWT.CHECK );
    props.setLook( wPreemptive );
    fdPreemptive = new FormData();
    fdPreemptive.left = new FormAttachment( middle, 0 );
    fdPreemptive.top = new FormAttachment( wHttpPassword, margin );
    fdPreemptive.right = new FormAttachment( 100, 0 );
    wPreemptive.setLayoutData( fdPreemptive );
    wPreemptive.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
      }
    } );

    FormData fdHttpAuth = new FormData();
    fdHttpAuth.left = new FormAttachment( 0, 0 );
    fdHttpAuth.right = new FormAttachment( 100, 0 );
    fdHttpAuth.top = new FormAttachment( gOutputFields, margin );
    gHttpAuth.setLayoutData( fdHttpAuth );

    // END HTTP AUTH GROUP
    // ////////////////////////

    // ////////////////////////
    // START PROXY GROUP

    Group gProxy = new Group( wAuthComp, SWT.SHADOW_ETCHED_IN );
    gProxy.setText( BaseMessages.getString( PKG, "RestDialog.ProxyGroup.Label" ) );
    FormLayout proxyLayout = new FormLayout();
    proxyLayout.marginWidth = 3;
    proxyLayout.marginHeight = 3;
    gProxy.setLayout( proxyLayout );
    props.setLook( gProxy );

    // HTTP Login
    wlProxyHost = new Label( gProxy, SWT.RIGHT );
    wlProxyHost.setText( BaseMessages.getString( PKG, "RestDialog.ProxyHost.Label" ) );
    props.setLook( wlProxyHost );
    FormData fdlProxyHost = new FormData();
    fdlProxyHost.top = new FormAttachment( 0, margin );
    fdlProxyHost.left = new FormAttachment( 0, 0 );
    fdlProxyHost.right = new FormAttachment( middle, -margin );
    wlProxyHost.setLayoutData( fdlProxyHost );
    wProxyHost = new TextVar( transMeta, gProxy, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wProxyHost.addModifyListener( lsMod );
    wProxyHost.setToolTipText( BaseMessages.getString( PKG, "RestDialog.ProxyHost.Tooltip" ) );
    props.setLook( wProxyHost );
    FormData fdProxyHost = new FormData();
    fdProxyHost.top = new FormAttachment( 0, margin );
    fdProxyHost.left = new FormAttachment( middle, 0 );
    fdProxyHost.right = new FormAttachment( 100, 0 );
    wProxyHost.setLayoutData( fdProxyHost );

    // HTTP Password
    wlProxyPort = new Label( gProxy, SWT.RIGHT );
    wlProxyPort.setText( BaseMessages.getString( PKG, "RestDialog.ProxyPort.Label" ) );
    props.setLook( wlProxyPort );
    FormData fdlProxyPort = new FormData();
    fdlProxyPort.top = new FormAttachment( wProxyHost, margin );
    fdlProxyPort.left = new FormAttachment( 0, 0 );
    fdlProxyPort.right = new FormAttachment( middle, -margin );
    wlProxyPort.setLayoutData( fdlProxyPort );
    wProxyPort = new TextVar( transMeta, gProxy, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wProxyPort.addModifyListener( lsMod );
    wProxyPort.setToolTipText( BaseMessages.getString( PKG, "RestDialog.ProxyPort.Tooltip" ) );
    props.setLook( wProxyPort );
    FormData fdProxyPort = new FormData();
    fdProxyPort.top = new FormAttachment( wProxyHost, margin );
    fdProxyPort.left = new FormAttachment( middle, 0 );
    fdProxyPort.right = new FormAttachment( 100, 0 );
    wProxyPort.setLayoutData( fdProxyPort );

    FormData fdProxy = new FormData();
    fdProxy.left = new FormAttachment( 0, 0 );
    fdProxy.right = new FormAttachment( 100, 0 );
    fdProxy.top = new FormAttachment( gHttpAuth, margin );
    gProxy.setLayoutData( fdProxy );

    // END HTTP AUTH GROUP
    // ////////////////////////

    fdAuthComp = new FormData();
    fdAuthComp.left = new FormAttachment( 0, 0 );
    fdAuthComp.top = new FormAttachment( wStepname, margin );
    fdAuthComp.right = new FormAttachment( 100, 0 );
    fdAuthComp.bottom = new FormAttachment( 100, 0 );
    wAuthComp.setLayoutData( fdAuthComp );

    wAuthComp.layout();
    wAuthTab.setControl( wAuthComp );
    // ////// END of Auth Tab

    // SSL tab...
    //
    wSSLTab = new CTabItem( wTabFolder, SWT.NONE );
    wSSLTab.setText( BaseMessages.getString( PKG, "RestDialog.SSL.Title" ) );

    FormLayout ssll = new FormLayout();
    ssll.marginWidth = Const.FORM_MARGIN;
    ssll.marginHeight = Const.FORM_MARGIN;

    wSSLComp = new Composite( wTabFolder, SWT.NONE );
    wSSLComp.setLayout( ssll );
    props.setLook( wSSLComp );

    // ////////////////////////
    // START SSLTrustStore GROUP

    gSSLTrustStore = new Group( wSSLComp, SWT.SHADOW_ETCHED_IN );
    gSSLTrustStore.setText( BaseMessages.getString( PKG, "RestDialog.SSLTrustStoreGroup.Label" ) );
    FormLayout SSLTrustStoreLayout = new FormLayout();
    SSLTrustStoreLayout.marginWidth = 3;
    SSLTrustStoreLayout.marginHeight = 3;
    gSSLTrustStore.setLayout( SSLTrustStoreLayout );
    props.setLook( gSSLTrustStore );

    // TrustStoreFile line
    wlTrustStoreFile = new Label( gSSLTrustStore, SWT.RIGHT );
    wlTrustStoreFile.setText( BaseMessages.getString( PKG, "RestDialog.TrustStoreFile.Label" ) );
    props.setLook( wlTrustStoreFile );
    fdlTrustStoreFile = new FormData();
    fdlTrustStoreFile.left = new FormAttachment( 0, 0 );
    fdlTrustStoreFile.top = new FormAttachment( 0, margin );
    fdlTrustStoreFile.right = new FormAttachment( middle, -margin );
    wlTrustStoreFile.setLayoutData( fdlTrustStoreFile );

    wbTrustStoreFile = new Button( gSSLTrustStore, SWT.PUSH | SWT.CENTER );
    props.setLook( wbTrustStoreFile );
    wbTrustStoreFile.setText( BaseMessages.getString( PKG, "System.Button.Browse" ) );
    fdbTrustStoreFile = new FormData();
    fdbTrustStoreFile.right = new FormAttachment( 100, 0 );
    fdbTrustStoreFile.top = new FormAttachment( 0, 0 );
    wbTrustStoreFile.setLayoutData( fdbTrustStoreFile );


    wTrustStoreFile = new TextVar( transMeta, gSSLTrustStore, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wTrustStoreFile );
    wTrustStoreFile.addModifyListener( lsMod );
    fdTrustStoreFile = new FormData();
    fdTrustStoreFile.left = new FormAttachment( middle, 0 );
    fdTrustStoreFile.top = new FormAttachment( 0, margin );
    fdTrustStoreFile.right = new FormAttachment( wbTrustStoreFile, -margin );
    wTrustStoreFile.setLayoutData( fdTrustStoreFile );

    wbTrustStoreFile.addSelectionListener( new SelectionAdapterFileDialogTextVar( log, wTrustStoreFile, transMeta,
      new SelectionAdapterOptions( SelectionOperation.FILE,
        new FilterType[] { FilterType.ALL }, FilterType.ALL ) ) );

    // TrustStorePassword line
    wlTrustStorePassword = new Label( gSSLTrustStore, SWT.RIGHT );
    wlTrustStorePassword.setText( BaseMessages.getString( PKG, "RestDialog.TrustStorePassword.Label" ) );
    props.setLook( wlTrustStorePassword );
    fdlTrustStorePassword = new FormData();
    fdlTrustStorePassword.left = new FormAttachment( 0, 0 );
    fdlTrustStorePassword.top = new FormAttachment( wbTrustStoreFile, margin );
    fdlTrustStorePassword.right = new FormAttachment( middle, -margin );
    wlTrustStorePassword.setLayoutData( fdlTrustStorePassword );
    wTrustStorePassword =
      new PasswordTextVar( transMeta, gSSLTrustStore, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wTrustStorePassword );
    wTrustStorePassword.addModifyListener( lsMod );
    fdTrustStorePassword = new FormData();
    fdTrustStorePassword.left = new FormAttachment( middle, 0 );
    fdTrustStorePassword.top = new FormAttachment( wbTrustStoreFile, margin );
    fdTrustStorePassword.right = new FormAttachment( 100, 0 );
    wTrustStorePassword.setLayoutData( fdTrustStorePassword );

    FormData fdSSLTrustStore = new FormData();
    fdSSLTrustStore.left = new FormAttachment( 0, 0 );
    fdSSLTrustStore.right = new FormAttachment( 100, 0 );
    fdSSLTrustStore.top = new FormAttachment( gHttpAuth, margin );
    gSSLTrustStore.setLayoutData( fdSSLTrustStore );

    // END HTTP AUTH GROUP
    // ////////////////////////

    // TODO
    additionalSSLUiChanges();

    fdSSLComp = new FormData();
    fdSSLComp.left = new FormAttachment( 0, 0 );
    fdSSLComp.top = new FormAttachment( wStepname, margin );
    fdSSLComp.right = new FormAttachment( 100, 0 );
    fdSSLComp.bottom = new FormAttachment( 100, 0 );
    wSSLComp.setLayoutData( fdSSLComp );

    wSSLComp.layout();
    wSSLTab.setControl( wSSLComp );
    // ////// END of SSL Tab

    // Additional tab...
    //
    wAdditionalTab = new CTabItem( wTabFolder, SWT.NONE );
    wAdditionalTab.setText( BaseMessages.getString( PKG, "RestDialog.Headers.Title" ) );

    FormLayout addLayout = new FormLayout();
    addLayout.marginWidth = Const.FORM_MARGIN;
    addLayout.marginHeight = Const.FORM_MARGIN;

    wAdditionalComp = new Composite( wTabFolder, SWT.NONE );
    wAdditionalComp.setLayout( addLayout );
    props.setLook( wAdditionalComp );

    wlFields = new Label( wAdditionalComp, SWT.NONE );
    wlFields.setText( BaseMessages.getString( PKG, "RestDialog.Headers.Label" ) );
    props.setLook( wlFields );
    fdlFields = new FormData();
    fdlFields.left = new FormAttachment( 0, 0 );
    fdlFields.top = new FormAttachment( wStepname, margin );
    wlFields.setLayoutData( fdlFields );

    wGetHeaders = new Button( wAdditionalComp, SWT.PUSH );
    wGetHeaders.setText( BaseMessages.getString( PKG, "RestDialog.GetHeaders.Button" ) );
    FormData fdGetHeaders = new FormData();
    fdGetHeaders.top = new FormAttachment( wlFields, margin );
    fdGetHeaders.right = new FormAttachment( 100, 0 );
    wGetHeaders.setLayoutData( fdGetHeaders );

    final int FieldsRows = input.getHeaderName().length;

    colinf =
      new ColumnInfo[] {
        new ColumnInfo(
          BaseMessages.getString( PKG, "RestDialog.ColumnInfo.Field" ), ColumnInfo.COLUMN_TYPE_CCOMBO,
          new String[] { "" }, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "RestDialog.ColumnInfo.Name" ), ColumnInfo.COLUMN_TYPE_TEXT, false ) };

    colinf[ 1 ].setUsingVariables( true );
    wFields =
      new TableView(
        transMeta, wAdditionalComp, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf, FieldsRows, lsMod,
        props );

    fdFields = new FormData();
    fdFields.left = new FormAttachment( 0, 0 );
    fdFields.top = new FormAttachment( wlFields, margin );
    fdFields.right = new FormAttachment( wGetHeaders, -margin );
    fdFields.bottom = new FormAttachment( 100, -margin );
    wFields.setLayoutData( fdFields );

    fdAdditionalComp = new FormData();
    fdAdditionalComp.left = new FormAttachment( 0, 0 );
    fdAdditionalComp.top = new FormAttachment( wStepname, margin );
    fdAdditionalComp.right = new FormAttachment( 100, -margin );
    fdAdditionalComp.bottom = new FormAttachment( 100, 0 );
    wAdditionalComp.setLayoutData( fdAdditionalComp );

    wAdditionalComp.layout();
    wAdditionalTab.setControl( wAdditionalComp );
    // ////// END of Additional Tab

    // Query Parameters tab...
    //
    wParametersTab = new CTabItem( wTabFolder, SWT.NONE );
    wParametersTab.setText( BaseMessages.getString( PKG, "RestDialog.Parameters.Title" ) );

    FormLayout playout = new FormLayout();
    playout.marginWidth = Const.FORM_MARGIN;
    playout.marginHeight = Const.FORM_MARGIN;

    wParametersComp = new Composite( wTabFolder, SWT.NONE );
    wParametersComp.setLayout( playout );
    props.setLook( wParametersComp );

    wlParameters = new Label( wParametersComp, SWT.NONE );
    wlParameters.setText( BaseMessages.getString( PKG, "RestDialog.Parameters.Label" ) );
    props.setLook( wlParameters );
    fdlParameters = new FormData();
    fdlParameters.left = new FormAttachment( 0, 0 );
    fdlParameters.top = new FormAttachment( wStepname, margin );
    wlParameters.setLayoutData( fdlParameters );

    wGet = new Button( wParametersComp, SWT.PUSH );
    wGet.setText( BaseMessages.getString( PKG, "RestDialog.GetParameters.Button" ) );
    FormData fdGet = new FormData();
    fdGet.top = new FormAttachment( wlParameters, margin );
    fdGet.right = new FormAttachment( 100, 0 );
    wGet.setLayoutData( fdGet );

    final int ParametersRows = input.getParameterField().length;

    colinfoparams =
      new ColumnInfo[] {
        new ColumnInfo(
          BaseMessages.getString( PKG, "RestDialog.ColumnInfo.ParameterField" ),
          ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "RestDialog.ColumnInfo.ParameterName" ), ColumnInfo.COLUMN_TYPE_TEXT,
          false ), };

    wParameters =
      new TableView(
        transMeta, wParametersComp, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinfoparams,
        ParametersRows, lsMod, props );

    fdParameters = new FormData();
    fdParameters.left = new FormAttachment( 0, 0 );
    fdParameters.top = new FormAttachment( wlParameters, margin );
    fdParameters.right = new FormAttachment( wGet, -margin );
    fdParameters.bottom = new FormAttachment( 100, -margin );
    wParameters.setLayoutData( fdParameters );

    fdParametersComp = new FormData();
    fdParametersComp.left = new FormAttachment( 0, 0 );
    fdParametersComp.top = new FormAttachment( wStepname, margin );
    fdParametersComp.right = new FormAttachment( 100, 0 );
    fdParametersComp.bottom = new FormAttachment( 100, 0 );
    wParametersComp.setLayoutData( fdParametersComp );

    wParametersComp.layout();
    wParametersTab.setControl( wParametersComp );
    // ////// END of Query Parameters Tab

    // Matrix Parameters tab
    wMatrixParametersTab = new CTabItem( wTabFolder, SWT.NONE );
    wMatrixParametersTab.setText( BaseMessages.getString( PKG, "RestDialog.MatrixParameters.Title" ) );

    FormLayout pl = new FormLayout();
    pl.marginWidth = Const.FORM_MARGIN;
    pl.marginHeight = Const.FORM_MARGIN;

    wMatrixParametersComp = new Composite( wTabFolder, SWT.NONE );
    wMatrixParametersComp.setLayout( pl );
    props.setLook( wMatrixParametersComp );

    wlMatrixParameters = new Label( wMatrixParametersComp, SWT.NONE );
    wlMatrixParameters.setText( BaseMessages.getString( PKG, "RestDialog.Parameters.Label" ) );
    props.setLook( wlMatrixParameters );
    fdlMatrixParameters = new FormData();
    fdlMatrixParameters.left = new FormAttachment( 0, 0 );
    fdlMatrixParameters.top = new FormAttachment( wStepname, margin );
    wlMatrixParameters.setLayoutData( fdlMatrixParameters );

    wMatrixGet = new Button( wMatrixParametersComp, SWT.PUSH );
    wMatrixGet.setText( BaseMessages.getString( PKG, "RestDialog.GetParameters.Button" ) );
    FormData fdMatrixGet = new FormData();
    fdMatrixGet.top = new FormAttachment( wlMatrixParameters, margin );
    fdMatrixGet.right = new FormAttachment( 100, 0 );
    wMatrixGet.setLayoutData( fdMatrixGet );

    int matrixParametersRows = input.getMatrixParameterField().length;

    colinfoparams =
      new ColumnInfo[] {
        new ColumnInfo(
          BaseMessages.getString( PKG, "RestDialog.ColumnInfo.ParameterField" ),
          ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "RestDialog.ColumnInfo.ParameterName" ), ColumnInfo.COLUMN_TYPE_TEXT,
          false ), };

    wMatrixParameters =
      new TableView(
        transMeta, wMatrixParametersComp, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinfoparams,
        matrixParametersRows, lsMod, props );

    fdMatrixParameters = new FormData();
    fdMatrixParameters.left = new FormAttachment( 0, 0 );
    fdMatrixParameters.top = new FormAttachment( wlMatrixParameters, margin );
    fdMatrixParameters.right = new FormAttachment( wMatrixGet, -margin );
    fdMatrixParameters.bottom = new FormAttachment( 100, -margin );
    wMatrixParameters.setLayoutData( fdMatrixParameters );

    fdMatrixParametersComp = new FormData();
    fdMatrixParametersComp.left = new FormAttachment( 0, 0 );
    fdMatrixParametersComp.top = new FormAttachment( wStepname, margin );
    fdMatrixParametersComp.right = new FormAttachment( 100, 0 );
    fdMatrixParametersComp.bottom = new FormAttachment( 100, 0 );
    wMatrixParametersComp.setLayoutData( fdMatrixParametersComp );

    wMatrixParametersComp.layout();
    wMatrixParametersTab.setControl( wMatrixParametersComp );
    //END of Matrix Parameters Tab

    fdTabFolder = new FormData();
    fdTabFolder.left = new FormAttachment( 0, 0 );
    fdTabFolder.top = new FormAttachment( wStepname, margin );
    fdTabFolder.right = new FormAttachment( 100, 0 );
    fdTabFolder.bottom = new FormAttachment( 100, -50 );
    wTabFolder.setLayoutData( fdTabFolder );

    //
    // Search the fields in the background
    //

    final Runnable runnable = new Runnable() {
      public void run() {
        StepMeta stepMeta = transMeta.findStep( stepname );
        if ( stepMeta != null ) {
          try {
            RowMetaInterface row = transMeta.getPrevStepFields( stepMeta );

            // Remember these fields...
            for ( int i = 0; i < row.size(); i++ ) {
              inputFields.put( row.getValueMeta( i ).getName(), Integer.valueOf( i ) );
            }

            setComboBoxes();
          } catch ( KettleException e ) {
            log.logError( toString(), BaseMessages.getString( PKG, "System.Dialog.GetFieldsFailed.Message" ) );
          }
        }
      }
    };
    new Thread( runnable ).start();

    // THE BUTTONS
    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );

    setButtonPositions( new Button[] { wOK, wCancel }, margin, wTabFolder );

    // Add listeners
    lsOK = new Listener() {
      public void handleEvent( Event e ) {
        ok();
      }
    };
    lsCancel = new Listener() {
      public void handleEvent( Event e ) {
        cancel();
      }
    };
    lsGet = new Listener() {
      public void handleEvent( Event e ) {
        getParametersFields( wParameters );
      }
    };
    lsMatrixGet = new Listener() {
      public void handleEvent( Event e ) {
        getParametersFields( wMatrixParameters );
      }
    };
    Listener lsGetHeaders = new Listener() {
      public void handleEvent( Event e ) {
        getHeaders();
      }
    };

    wOK.addListener( SWT.Selection, lsOK );
    wGet.addListener( SWT.Selection, lsGet );
    wMatrixGet.addListener( SWT.Selection, lsMatrixGet );
    wGetHeaders.addListener( SWT.Selection, lsGetHeaders );
    wCancel.addListener( SWT.Selection, lsCancel );

    lsDef = new SelectionAdapter() {
      public void widgetDefaultSelected( SelectionEvent e ) {
        ok();
      }
    };

    wStepname.addSelectionListener( lsDef );
    wUrl.addSelectionListener( lsDef );
    wResult.addSelectionListener( lsDef );
    wResultCode.addSelectionListener( lsDef );
    wResponseTime.addSelectionListener( lsDef );

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    lsResize = new Listener() {
      public void handleEvent( Event event ) {
        Point size = shell.getSize();
        wFields.setSize( size.x - 10, size.y - 50 );
        wFields.table.setSize( size.x - 10, size.y - 50 );
        wFields.redraw();
      }
    };
    shell.addListener( SWT.Resize, lsResize );


    // Set the shell size, based upon previous time...
    setSize();
    wTabFolder.setSelection( 0 );
    getData();
    activeUrlInfield();
    activeMethodInfield();
    setMethod();
    input.setChanged( changed );

    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return stepname;
  }

  protected void setMethod() {
    boolean activateBody = RestMeta.isActiveBody( wMethod.getText() );
    boolean activateParams = RestMeta.isActiveParameters( wMethod.getText() );

    wlBody.setEnabled( activateBody );
    wBody.setEnabled( activateBody );
    wApplicationType.setEnabled( activateBody );

    wlParameters.setEnabled( activateParams );
    wParameters.setEnabled( activateParams );
    wGet.setEnabled( activateParams );
    wlMatrixParameters.setEnabled( activateParams );
    wMatrixParameters.setEnabled( activateParams );
    wMatrixGet.setEnabled( activateParams );
  }

  protected void setComboBoxes() {
    // Something was changed in the row.
    //
    final Map<String, Integer> fields = new HashMap<String, Integer>();

    // Add the currentMeta fields...
    fields.putAll( inputFields );

    Set<String> keySet = fields.keySet();
    List<String> entries = new ArrayList<String>( keySet );

    fieldNames = entries.toArray( new String[ entries.size() ] );

    Const.sortStrings( fieldNames );
    colinfoparams[ 0 ].setComboValues( fieldNames );
    colinf[ 0 ].setComboValues( fieldNames );
  }

  private void setStreamFields() {
    if ( !gotPreviousFields ) {
      String urlfield = wUrlField.getText();
      String body = wBody.getText();
      String method = wMethodField.getText();

      wUrlField.removeAll();
      wBody.removeAll();
      wMethodField.removeAll();

      try {
        if ( fieldNames != null ) {
          wUrlField.setItems( fieldNames );
          wBody.setItems( fieldNames );
          wMethodField.setItems( fieldNames );
        }
      } finally {
        if ( urlfield != null ) {
          wUrlField.setText( urlfield );
        }
        if ( body != null ) {
          wBody.setText( body );
        }
        if ( method != null ) {
          wMethodField.setText( method );
        }
      }
      gotPreviousFields = true;
    }
  }

  private void activeUrlInfield() {
    wlUrlField.setEnabled( wUrlInField.getSelection() );
    wUrlField.setEnabled( wUrlInField.getSelection() );
    wlUrl.setEnabled( !wUrlInField.getSelection() );
    wUrl.setEnabled( !wUrlInField.getSelection() );
  }

  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void getData() {
    if ( isDebug() ) {
      logDebug( BaseMessages.getString( PKG, "RestDialog.Log.GettingKeyInfo" ) );
    }

    if ( input.getHeaderName() != null ) {
      for ( int i = 0; i < input.getHeaderName().length; i++ ) {
        TableItem item = wFields.table.getItem( i );
        if ( input.getHeaderField()[ i ] != null ) {
          item.setText( 1, input.getHeaderField()[ i ] );
        }
        if ( input.getHeaderName()[ i ] != null ) {
          item.setText( 2, input.getHeaderName()[ i ] );
        }
      }
    }

    if ( input.getParameterField() != null ) {
      for ( int i = 0; i < input.getParameterField().length; i++ ) {
        TableItem item = wParameters.table.getItem( i );
        if ( input.getParameterField()[ i ] != null ) {
          item.setText( 1, input.getParameterField()[ i ] );
        }
        if ( input.getParameterName()[ i ] != null ) {
          item.setText( 2, input.getParameterName()[ i ] );
        }
      }
    }

    if ( input.getMatrixParameterField() != null ) {
      for ( int i = 0; i < input.getMatrixParameterField().length; i++ ) {
        TableItem item = wMatrixParameters.table.getItem( i );
        if ( input.getMatrixParameterField()[ i ] != null ) {
          item.setText( 1, input.getMatrixParameterField()[ i ] );
        }
        if ( input.getMatrixParameterField()[ i ] != null ) {
          item.setText( 2, input.getMatrixParameterField()[ i ] );
        }
      }
    }

    wMethod.setText( Const.NVL( input.getMethod(), RestMeta.HTTP_METHOD_GET ) );
    wMethodInField.setSelection( input.isDynamicMethod() );
    if ( input.getBodyField() != null ) {
      wBody.setText( input.getBodyField() );
    }
    if ( input.getMethodFieldName() != null ) {
      wMethodField.setText( input.getMethodFieldName() );
    }
    if ( input.getUrl() != null ) {
      wUrl.setText( input.getUrl() );
    }
    wUrlInField.setSelection( input.isUrlInField() );
    if ( input.getUrlField() != null ) {
      wUrlField.setText( input.getUrlField() );
    }
    if ( input.getFieldName() != null ) {
      wResult.setText( input.getFieldName() );
    }
    if ( input.getResultCodeFieldName() != null ) {
      wResultCode.setText( input.getResultCodeFieldName() );
    }
    if ( input.getResponseTimeFieldName() != null ) {
      wResponseTime.setText( input.getResponseTimeFieldName() );
    }

    if ( input.getHttpLogin() != null ) {
      wHttpLogin.setText( input.getHttpLogin() );
    }
    if ( input.getHttpPassword() != null ) {
      wHttpPassword.setText( input.getHttpPassword() );
    }
    if ( input.getProxyHost() != null ) {
      wProxyHost.setText( input.getProxyHost() );
    }
    if ( input.getProxyPort() != null ) {
      wProxyPort.setText( input.getProxyPort() );
    }
    wPreemptive.setSelection( input.isPreemptive() );

    if ( input.getTrustStoreFile() != null ) {
      wTrustStoreFile.setText( input.getTrustStoreFile() );
    }
    if ( input.getTrustStorePassword() != null ) {
      wTrustStorePassword.setText( input.getTrustStorePassword() );
    }
    if ( input.getResponseHeaderFieldName() != null ) {
      wResponseHeader.setText( input.getResponseHeaderFieldName() );
    }

    wApplicationType.setText( Const.NVL( input.getApplicationType(), "" ) );

    wFields.setRowNums();
    wFields.optWidth( true );

    wStepname.selectAll();
    wStepname.setFocus();
  }

  private void cancel() {
    stepname = null;
    input.setChanged( changed );
    dispose();
  }

  private void ok() {
    if ( Utils.isEmpty( wStepname.getText() ) ) {
      return;
    }

    int nrheaders = wFields.nrNonEmpty();
    int nrparams = wParameters.nrNonEmpty();
    int nrmatrixparams = wMatrixParameters.nrNonEmpty();
    input.allocate( nrheaders, nrparams, nrmatrixparams );

    if ( isDebug() ) {
      logDebug( BaseMessages.getString( PKG, "RestDialog.Log.FoundArguments", String.valueOf( nrheaders ) ) );
    }
    for ( int i = 0; i < nrheaders; i++ ) {
      TableItem item = wFields.getNonEmpty( i );
      input.getHeaderField()[ i ] = item.getText( 1 );
      input.getHeaderName()[ i ] = item.getText( 2 );
    }
    for ( int i = 0; i < nrparams; i++ ) {
      TableItem item = wParameters.getNonEmpty( i );
      input.getParameterField()[ i ] = item.getText( 1 );
      input.getParameterName()[ i ] = item.getText( 2 );
    }

    for ( int i = 0; i < nrmatrixparams; i++ ) {
      TableItem item = wMatrixParameters.getNonEmpty( i );
      input.getMatrixParameterField()[ i ] = item.getText( 1 );
      input.getMatrixParameterName()[ i ] = item.getText( 2 );
    }

    input.setDynamicMethod( wMethodInField.getSelection() );
    input.setMethodFieldName( wMethodField.getText() );
    input.setMethod( wMethod.getText() );
    input.setUrl( wUrl.getText() );
    input.setUrlField( wUrlField.getText() );
    input.setUrlInField( wUrlInField.getSelection() );
    input.setBodyField( wBody.getText() );
    input.setFieldName( wResult.getText() );
    input.setResultCodeFieldName( wResultCode.getText() );
    input.setResponseTimeFieldName( wResponseTime.getText() );
    input.setResponseHeaderFieldName( wResponseHeader.getText() );

    input.setHttpLogin( wHttpLogin.getText() );
    input.setHttpPassword( wHttpPassword.getText() );
    input.setProxyHost( wProxyHost.getText() );
    input.setProxyPort( wProxyPort.getText() );
    input.setPreemptive( wPreemptive.getSelection() );

    input.setTrustStoreFile( wTrustStoreFile.getText() );
    input.setTrustStorePassword( wTrustStorePassword.getText() );
    input.setApplicationType( wApplicationType.getText() );
    stepname = wStepname.getText(); // return value

    setAdditionalFieldsInMeta();
    dispose();
  }

  private void getParametersFields( TableView tView ) {
    try {
      RowMetaInterface r = transMeta.getPrevStepFields( stepname );
      if ( r != null && !r.isEmpty() ) {
        BaseStepDialog
          .getFieldsFromPrevious( r, tView, 1, new int[] { 1, 2 }, new int[] { 3 }, -1, -1, null );
      }
    } catch ( KettleException ke ) {
      new ErrorDialog(
        shell, BaseMessages.getString( PKG, "RestDialog.FailedToGetFields.DialogTitle" ), BaseMessages
        .getString( PKG, "RestDialog.FailedToGetFields.DialogMessage" ), ke );
    }

  }

  private void getHeaders() {
    try {
      RowMetaInterface r = transMeta.getPrevStepFields( stepname );
      if ( r != null && !r.isEmpty() ) {
        BaseStepDialog.getFieldsFromPrevious( r, wFields, 1, new int[] { 1, 2 }, new int[] { 3 }, -1, -1, null );
      }
    } catch ( KettleException ke ) {
      new ErrorDialog(
        shell, BaseMessages.getString( PKG, "RestDialog.FailedToGetHeaders.DialogTitle" ), BaseMessages
        .getString( PKG, "RestDialog.FailedToGetHeaders.DialogMessage" ), ke );
    }

  }

  private void activeMethodInfield() {
    wlMethod.setEnabled( !wMethodInField.getSelection() );
    wMethod.setEnabled( !wMethodInField.getSelection() );
    wlMethodField.setEnabled( wMethodInField.getSelection() );
    wMethodField.setEnabled( wMethodInField.getSelection() );
  }

  protected void additionalSSLUiChanges() {
    // None
  }

  protected void setAdditionalFieldsInMeta() {
    //Noop. The class extending this class can set additional fields
  }
}
