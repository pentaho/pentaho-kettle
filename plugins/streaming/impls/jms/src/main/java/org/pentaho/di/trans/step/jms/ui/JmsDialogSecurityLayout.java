/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.step.jms.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.jms.JmsDelegate;
import org.pentaho.di.trans.step.jms.context.JmsProvider;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.widget.CheckBoxTableCombo;
import org.pentaho.di.ui.core.widget.PasswordTextVar;
import org.pentaho.di.ui.core.widget.TextVar;

import java.util.HashMap;
import java.util.Map;

import static org.pentaho.di.i18n.BaseMessages.getString;
import static org.pentaho.di.trans.step.jms.JmsConstants.PKG;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.pentaho.di.ui.trans.step.BaseStreamingDialog.INPUT_WIDTH;


/**
 * Common SWT layout for both the consumer and producer
 */
class JmsDialogSecurityLayout {

  private final PropsUI props;
  private final CTabFolder wTabFolder;
  private final ModifyListener lsMod;
  private final TransMeta transMeta;
  private Map<String, String> sslConfig;
  private final JmsDelegate jmsDelegate;

  private final String SSL_TRUST_STORE_TYPE;
  private final String SSL_TRUST_STORE_PATH;
  private final String SSL_TRUST_STORE_PASSWORD;
  private final String SSL_KEY_STORE_TYPE;
  private final String SSL_KEY_STORE_PATH;
  private final String SSL_KEY_STORE_PASS;
  private final String SSL_CONTEXT_ALGORITHM;
  private final String SSL_IBM_CIPHER_SUITE;
  private final String SSL_IBM_FIPS_REQUIRED;

  private CheckBoxTableCombo checkBoxTableCombo;
  private Group wAuthenticationGroup;
  private Composite wSslGroup;

  private Label wlIbmUser;
  private Label wlIbmPassword;
  private TextVar wIbmUser;
  private TextVar wIbmPassword;

  private Label wlActiveUser;
  private Label wlActivePassword;
  private TextVar wActiveUser;
  private TextVar wActivePassword;

  JmsDialogSecurityLayout(
    PropsUI props, CTabFolder wTabFolder, ModifyListener lsMod, TransMeta transMeta,
    boolean sslEnabled, JmsDelegate jmsDelegate ) {
    checkNotNull( props );
    checkNotNull( wTabFolder );
    checkNotNull( lsMod );
    checkNotNull( transMeta );

    this.props = props;
    this.wTabFolder = wTabFolder;
    this.lsMod = lsMod;
    this.transMeta = transMeta;
    this.jmsDelegate = jmsDelegate;
    this.jmsDelegate.sslEnabled = sslEnabled;
    this.sslConfig = new HashMap<>();

    SSL_TRUST_STORE_TYPE = getString( PKG, "JmsDialog.Security.SSL_TRUST_STORE_TYPE" );
    SSL_TRUST_STORE_PATH = getString( PKG, "JmsDialog.Security.SSL_TRUST_STORE_PATH" );
    SSL_TRUST_STORE_PASSWORD = getString( PKG, "JmsDialog.Security.SSL_TRUST_STORE_PASSWORD" );
    SSL_KEY_STORE_TYPE = getString( PKG, "JmsDialog.Security.SSL_KEY_STORE_TYPE" );
    SSL_KEY_STORE_PATH = getString( PKG, "JmsDialog.Security.SSL_KEY_STORE_PATH" );
    SSL_KEY_STORE_PASS = getString( PKG, "JmsDialog.Security.SSL_KEY_STORE_PASS" );
    SSL_CONTEXT_ALGORITHM = getString( PKG, "JmsDialog.Security.SSL_CONTEXT_ALGORITHM" );
    SSL_IBM_CIPHER_SUITE = getString( PKG, "JmsDialog.Security.SSL_IBM_CIPHER_SUITE" );
    SSL_IBM_FIPS_REQUIRED = getString( PKG, "JmsDialog.Security.SSL_IBM_FIPS_REQUIRED" );
  }

  public String getIbmUser() {
    return wIbmUser.getText();
  }

  public String getIbmPassword() {
    return wIbmPassword.getText();
  }

  public String getActiveUser() {
    return wActiveUser.getText();
  }

  public String getActivePassword() {
    return wActivePassword.getText();
  }

  protected void buildSecurityTab() {
    CTabItem wSecurityTab = new CTabItem( wTabFolder, SWT.NONE, 1 );
    wSecurityTab.setText( BaseMessages.getString( PKG, "JmsDialog.Security.Tab" ) );

    Composite wSecurityComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wSecurityComp );
    FormLayout securityLayout = new FormLayout();
    securityLayout.marginHeight = 15;
    securityLayout.marginWidth = 15;
    wSecurityComp.setLayout( securityLayout );

    wAuthenticationGroup = new Group( wSecurityComp, SWT.SHADOW_ETCHED_IN );
    wAuthenticationGroup.setText( getString( PKG, "JmsDialog.Authentication" ) );

    FormLayout flAuthentication = new FormLayout();
    flAuthentication.marginHeight = 15;
    flAuthentication.marginWidth = 15;
    wAuthenticationGroup.setLayout( flAuthentication );

    FormData fdAuthenticationGroup = new FormData();
    fdAuthenticationGroup.left = new FormAttachment( 0, 0 );
    fdAuthenticationGroup.top = new FormAttachment( 0, 0 );
    fdAuthenticationGroup.right = new FormAttachment( 100, 0 );
    fdAuthenticationGroup.width = INPUT_WIDTH;
    wAuthenticationGroup.setLayoutData( fdAuthenticationGroup );

    props.setLook( wAuthenticationGroup );

    wSslGroup = new Composite( wSecurityComp, SWT.NONE );

    FormLayout flSsl = new FormLayout();
    flSsl.marginHeight = 0;
    flSsl.marginWidth = 0;
    wSslGroup.setLayout( flSsl );

    FormData fdSslGroup = new FormData();
    fdSslGroup.left = new FormAttachment( 0, 0 );
    fdSslGroup.top = new FormAttachment( wAuthenticationGroup, 15 );
    fdSslGroup.right = new FormAttachment( 100, 0 );
    fdSslGroup.bottom = new FormAttachment( 100, 0 );
    fdSslGroup.width = INPUT_WIDTH;
    wSslGroup.setLayoutData( fdSslGroup );

    props.setLook( wSslGroup );

    checkBoxTableCombo = new CheckBoxTableCombo(
      wSslGroup,
      props,
      lsMod,
      transMeta,
      sslConfig,
      BaseMessages.getString( PKG, "JmsDialog.Security.SSLButton" ),
      BaseMessages.getString( PKG, "JmsDialog.Security.SSLTable" ),
      BaseMessages.getString( PKG, "JmsDialog.Security.Column.Name" ),
      BaseMessages.getString( PKG, "JmsDialog.Security.Column.Value" ),
      jmsDelegate.sslEnabled );

    FormData fdSecurityComp = new FormData();
    wSecurityComp.setLayoutData( fdSecurityComp );

    wSecurityTab.setControl( wSecurityComp );
    layoutIbmMqConnectionFields();
    layoutActiveMqConnectionFields();

    wIbmUser.setText( jmsDelegate.ibmUsername );
    wIbmPassword.setText( jmsDelegate.ibmPassword );

    wActiveUser.setText( jmsDelegate.amqUsername );
    wActivePassword.setText( jmsDelegate.amqPassword );

    toggleVisibility( JmsProvider.ConnectionType.valueOf( this.jmsDelegate.connectionType ) );
  }

  protected void saveTableValues() {
    Map<String, String> tableValues = checkBoxTableCombo.getPropertiesData();
    jmsDelegate.sslEnabled = checkBoxTableCombo.getIsEnabled();
    jmsDelegate.sslTruststoreType = tableValues.get( SSL_TRUST_STORE_TYPE );
    jmsDelegate.sslTruststorePath = tableValues.get( SSL_TRUST_STORE_PATH );
    jmsDelegate.sslTruststorePassword = tableValues.get( SSL_TRUST_STORE_PASSWORD );
    jmsDelegate.sslKeystoreType = tableValues.get( SSL_KEY_STORE_TYPE );
    jmsDelegate.sslKeystorePath = tableValues.get( SSL_KEY_STORE_PATH );
    jmsDelegate.sslKeystorePassword = tableValues.get( SSL_KEY_STORE_PASS );
    jmsDelegate.sslContextAlgorithm = tableValues.get( SSL_CONTEXT_ALGORITHM );
    jmsDelegate.ibmSslCipherSuite = tableValues.get( SSL_IBM_CIPHER_SUITE );
    jmsDelegate.ibmSslFipsRequired = tableValues.get( SSL_IBM_FIPS_REQUIRED );
  }

  protected void populateTableIbm() {
    this.sslConfig = new HashMap<>();

    this.sslConfig.put( SSL_TRUST_STORE_TYPE, this.jmsDelegate.sslTruststoreType );
    this.sslConfig.put( SSL_TRUST_STORE_PATH, this.jmsDelegate.sslTruststorePath );
    this.sslConfig.put( SSL_TRUST_STORE_PASSWORD, this.jmsDelegate.sslTruststorePassword );
    this.sslConfig.put( SSL_KEY_STORE_TYPE, this.jmsDelegate.sslKeystoreType );
    this.sslConfig.put( SSL_KEY_STORE_PATH, this.jmsDelegate.sslKeystorePath );
    this.sslConfig.put( SSL_KEY_STORE_PASS, this.jmsDelegate.sslKeystorePassword );
    this.sslConfig.put( SSL_CONTEXT_ALGORITHM, this.jmsDelegate.sslContextAlgorithm );
    this.sslConfig.put( SSL_IBM_CIPHER_SUITE, this.jmsDelegate.ibmSslCipherSuite );
    this.sslConfig.put( SSL_IBM_FIPS_REQUIRED, this.jmsDelegate.ibmSslFipsRequired );

    this.checkBoxTableCombo.updateDataMap( this.sslConfig );
  }

  protected void populateTableActiveMq() {
    this.sslConfig = new HashMap<>();

    this.sslConfig.put( SSL_TRUST_STORE_TYPE, this.jmsDelegate.sslTruststoreType );
    this.sslConfig.put( SSL_TRUST_STORE_PATH, this.jmsDelegate.sslTruststorePath );
    this.sslConfig.put( SSL_TRUST_STORE_PASSWORD, this.jmsDelegate.sslTruststorePassword );
    this.sslConfig.put( SSL_KEY_STORE_TYPE, this.jmsDelegate.sslKeystoreType );
    this.sslConfig.put( SSL_KEY_STORE_PATH, this.jmsDelegate.sslKeystorePath );
    this.sslConfig.put( SSL_KEY_STORE_PASS, this.jmsDelegate.sslKeystorePassword );
    this.sslConfig.put( SSL_CONTEXT_ALGORITHM, this.jmsDelegate.sslContextAlgorithm );

    this.checkBoxTableCombo.updateDataMap( this.sslConfig );
  }

  private void layoutIbmMqConnectionFields( ) {
    wlIbmUser = new Label( wAuthenticationGroup, SWT.LEFT );
    props.setLook( wlIbmUser );
    wlIbmUser.setText( getString( PKG, "JmsDialog.JmsUser" ) );
    FormData fdlUser = new FormData();
    fdlUser.left = new FormAttachment( 0, 0 );
    fdlUser.top = new FormAttachment( 0, 0 );
    wlIbmUser.setLayoutData( fdlUser );

    wIbmUser = new TextVar( transMeta, wAuthenticationGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wIbmUser );
    wIbmUser.addModifyListener( lsMod );
    FormData fdUser = new FormData();
    fdUser.left = new FormAttachment( 0, 0 );
    fdUser.top = new FormAttachment( wlIbmUser, 5 );
    fdUser.right = new FormAttachment( 0, INPUT_WIDTH );
    wIbmUser.setLayoutData( fdUser );

    wlIbmPassword = new Label( wAuthenticationGroup, SWT.LEFT );
    props.setLook( wlIbmPassword );
    wlIbmPassword.setText( getString( PKG, "JmsDialog.JmsPassword" ) );
    FormData fdlPassword = new FormData();
    fdlPassword.left = new FormAttachment( 0, 0 );
    fdlPassword.top = new FormAttachment( wIbmUser, 10 );
    wlIbmPassword.setLayoutData( fdlPassword );

    wIbmPassword = new PasswordTextVar( transMeta, wAuthenticationGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wIbmPassword );
    wIbmPassword.addModifyListener( lsMod );
    FormData fdPassword = new FormData();
    fdPassword.left = new FormAttachment( 0, 0 );
    fdPassword.top = new FormAttachment( wlIbmPassword, 5 );
    fdPassword.right = new FormAttachment( 0, INPUT_WIDTH );
    wIbmPassword.setLayoutData( fdPassword );
  }

  private void layoutActiveMqConnectionFields(  ) {
    wlActiveUser = new Label( wAuthenticationGroup, SWT.LEFT );
    props.setLook( wlActiveUser );
    wlActiveUser.setText( getString( PKG, "JmsDialog.JmsUser" ) );
    FormData fdlUser = new FormData();
    fdlUser.left = new FormAttachment( 0, 0 );
    fdlUser.top = new FormAttachment( 0, 0 );
    wlActiveUser.setLayoutData( fdlUser );

    wActiveUser = new TextVar( transMeta, wAuthenticationGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wActiveUser );
    wActiveUser.addModifyListener( lsMod );
    FormData fdUser = new FormData();
    fdUser.left = new FormAttachment( 0, 0 );
    fdUser.top = new FormAttachment( wlActiveUser, 5 );
    fdUser.right = new FormAttachment( 0, INPUT_WIDTH );
    wActiveUser.setLayoutData( fdUser );

    wlActivePassword = new Label( wAuthenticationGroup, SWT.LEFT );
    props.setLook( wlActivePassword );
    wlActivePassword.setText( getString( PKG, "JmsDialog.JmsPassword" ) );
    FormData fdlPassword = new FormData();
    fdlPassword.left = new FormAttachment( 0, 0 );
    fdlPassword.top = new FormAttachment( wActiveUser, 10 );
    wlActivePassword.setLayoutData( fdlPassword );

    wActivePassword = new PasswordTextVar( transMeta, wAuthenticationGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wActivePassword );
    wActivePassword.addModifyListener( lsMod );
    FormData fdPassword = new FormData();
    fdPassword.left = new FormAttachment( 0, 0 );
    fdPassword.top = new FormAttachment( wlActivePassword, 5 );
    fdPassword.right = new FormAttachment( 0, INPUT_WIDTH );
    wActivePassword.setLayoutData( fdPassword );
  }

  protected void toggleVisibility( JmsProvider.ConnectionType type ) {
    switch ( type ) {
      case WEBSPHERE:
        setActiveMqVisibility( false );
        setIbmMqVisibility( true );
        populateTableIbm();
        break;
      case ACTIVEMQ:
        setIbmMqVisibility( false );
        setActiveMqVisibility( true );
        populateTableActiveMq();
        break;
    }
  }

  private void setIbmMqVisibility( boolean isVisible ) {

    wlIbmUser.setVisible( isVisible );
    wlIbmPassword.setVisible( isVisible );

    wIbmUser.setVisible( isVisible );
    wIbmPassword.setVisible( isVisible );
  }

  private void setActiveMqVisibility( boolean isVisible ) {

    wlActiveUser.setVisible( isVisible );
    wlActivePassword.setVisible( isVisible );

    wActiveUser.setVisible( isVisible );
    wActivePassword.setVisible( isVisible );
  }
}
