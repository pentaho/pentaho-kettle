/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018-2019 by Hitachi Vantara : http://www.pentaho.com
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
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.jms.JmsDelegate;
import org.pentaho.di.trans.step.jms.context.JmsProvider;
import org.pentaho.di.ui.core.FormDataBuilder;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.widget.AuthComposite;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.pentaho.di.i18n.BaseMessages.getString;
import static org.pentaho.di.trans.step.jms.JmsConstants.PKG;
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

  private static final String SSL_TRUST_STORE_TYPE = getString( PKG, "JmsDialog.Security.SSL_TRUST_STORE_TYPE" );
  private static final String SSL_TRUST_STORE_PATH = getString( PKG, "JmsDialog.Security.SSL_TRUST_STORE_PATH" );
  private static final String SSL_TRUST_STORE_PASSWORD =
    getString( PKG, "JmsDialog.Security.SSL_TRUST_STORE_PASSWORD" );
  private static final String SSL_KEY_STORE_TYPE = getString( PKG, "JmsDialog.Security.SSL_KEY_STORE_TYPE" );
  private static final String SSL_KEY_STORE_PATH = getString( PKG, "JmsDialog.Security.SSL_KEY_STORE_PATH" );
  private static final String SSL_KEY_STORE_PASS = getString( PKG, "JmsDialog.Security.SSL_KEY_STORE_PASS" );
  private static final String SSL_CONTEXT_ALGORITHM = getString( PKG, "JmsDialog.Security.SSL_CONTEXT_ALGORITHM" );
  private static final String SSL_CIPHER_SUITE = getString( PKG, "JmsDialog.Security.SSL_CIPHER_SUITE" );
  private static final String SSL_IBM_FIPS_REQUIRED = getString( PKG, "JmsDialog.Security.SSL_IBM_FIPS_REQUIRED" );
  private static final String SSL_AMQ_PROVIDER = getString( PKG, "JmsDialog.Security.SSL_AMQ_PROVIDER" );
  private static final String SSL_AMQ_TRUST_ALL = getString( PKG, "JmsDialog.Security.SSL_AMQ_TRUST_ALL" );
  private static final String SSL_AMQ_VERIFY_HOST = getString( PKG, "JmsDialog.Security.SSL_AMQ_VERIFY_HOST" );

  private CheckBoxTableComboDefaultButton checkBoxTableCombo;
  private AuthComposite activeMqAuthComposite;
  private AuthComposite ibmMqAuthComposite;

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
  }

  void buildSecurityTab() {
    CTabItem wSecurityTab = new CTabItem( wTabFolder, SWT.NONE, 1 );
    wSecurityTab.setText( BaseMessages.getString( PKG, "JmsDialog.Security.Tab" ) );

    Composite wSecurityComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wSecurityComp );
    FormLayout securityLayout = new FormLayout();
    securityLayout.marginHeight = 15;
    securityLayout.marginWidth = 15;
    wSecurityComp.setLayout( securityLayout );

    FormData fdAuthComposite = new FormDataBuilder().fullWidth().result();

    ibmMqAuthComposite = new AuthComposite( wSecurityComp, SWT.NONE, props, lsMod, transMeta,
      getString( PKG, "JmsDialog.Authentication" ), getString( PKG, "JmsDialog.JmsUser" ),
      getString( PKG, "JmsDialog.JmsPassword" ) );
    ibmMqAuthComposite.setLayoutData( fdAuthComposite );

    activeMqAuthComposite = new AuthComposite( wSecurityComp, SWT.NONE, props, lsMod, transMeta,
      getString( PKG, "JmsDialog.Authentication" ), getString( PKG, "JmsDialog.JmsUser" ),
      getString( PKG, "JmsDialog.JmsPassword" ) );
    activeMqAuthComposite.setLayoutData( fdAuthComposite );

    Composite wSslGroup = new Composite( wSecurityComp, SWT.NONE );

    FormLayout flSsl = new FormLayout();
    flSsl.marginHeight = 0;
    flSsl.marginWidth = 0;
    wSslGroup.setLayout( flSsl );

    FormData fdSslGroup = new FormData();
    fdSslGroup.left = new FormAttachment( 0, 0 );
    fdSslGroup.top = new FormAttachment( activeMqAuthComposite, 15 );
    fdSslGroup.right = new FormAttachment( 100, 0 );
    fdSslGroup.bottom = new FormAttachment( 100, 0 );
    fdSslGroup.width = INPUT_WIDTH;
    wSslGroup.setLayoutData( fdSslGroup );

    props.setLook( wSslGroup );

    checkBoxTableCombo = new CheckBoxTableComboDefaultButton(
      wSslGroup,
      props,
      lsMod,
      transMeta,
      sslConfig,
      this::toggleVisibility,
      jmsDelegate );

    FormData fdSecurityComp = new FormData();
    wSecurityComp.setLayoutData( fdSecurityComp );

    wSecurityTab.setControl( wSecurityComp );
    activeMqAuthComposite.setUsername( jmsDelegate.amqUsername );
    activeMqAuthComposite.setPassword( jmsDelegate.amqPassword );
    ibmMqAuthComposite.setUsername( jmsDelegate.ibmUsername );
    ibmMqAuthComposite.setPassword( jmsDelegate.ibmPassword );

    toggleVisibility( JmsProvider.ConnectionType.valueOf( this.jmsDelegate.connectionType ) );
  }

  void saveTableValues() {
    Map<String, String> tableValues = checkBoxTableCombo.getPropertiesData();

    jmsDelegate.sslEnabled = checkBoxTableCombo.getIsEnabled();
    jmsDelegate.sslUseDefaultContext = checkBoxTableCombo.getUseDefaultSslContext();

    if ( !checkBoxTableCombo.getUseDefaultSslContext() ) {
      jmsDelegate.sslTruststoreType = tableValues.get( SSL_TRUST_STORE_TYPE );
      jmsDelegate.sslTruststorePath = tableValues.get( SSL_TRUST_STORE_PATH );
      jmsDelegate.sslTruststorePassword = tableValues.get( SSL_TRUST_STORE_PASSWORD );
      jmsDelegate.sslKeystoreType = tableValues.get( SSL_KEY_STORE_TYPE );
      jmsDelegate.sslKeystorePath = tableValues.get( SSL_KEY_STORE_PATH );
      jmsDelegate.sslKeystorePassword = tableValues.get( SSL_KEY_STORE_PASS );
      jmsDelegate.sslContextAlgorithm = tableValues.get( SSL_CONTEXT_ALGORITHM );
      jmsDelegate.amqSslProvider = tableValues.get( SSL_AMQ_PROVIDER );
      jmsDelegate.amqSslTrustAll = tableValues.get( SSL_AMQ_TRUST_ALL );
      jmsDelegate.amqSslVerifyHost = tableValues.get( SSL_AMQ_VERIFY_HOST );
      jmsDelegate.sslCipherSuite = tableValues.get( SSL_CIPHER_SUITE );
    } else {
      if ( checkBoxTableCombo.getSelectedConnectionType().equals( JmsProvider.ConnectionType.WEBSPHERE ) ) {
        jmsDelegate.sslCipherSuite = tableValues.get( SSL_CIPHER_SUITE );
        jmsDelegate.ibmSslFipsRequired = tableValues.get( SSL_IBM_FIPS_REQUIRED );
      }
    }

  }

  void saveAuthentication() {
    jmsDelegate.amqUsername = activeMqAuthComposite.getUsername();
    jmsDelegate.amqPassword = activeMqAuthComposite.getPassword();
    jmsDelegate.ibmUsername = ibmMqAuthComposite.getUsername();
    jmsDelegate.ibmPassword = ibmMqAuthComposite.getPassword();
  }

  private void populateTableIbm() {
    this.sslConfig = new HashMap<>();

    if ( !checkBoxTableCombo.getUseDefaultSslContext() ) {
      this.sslConfig.put( SSL_TRUST_STORE_TYPE, this.jmsDelegate.sslTruststoreType );
      this.sslConfig.put( SSL_TRUST_STORE_PATH, this.jmsDelegate.sslTruststorePath );
      this.sslConfig.put( SSL_TRUST_STORE_PASSWORD, this.jmsDelegate.sslTruststorePassword );
      this.sslConfig.put( SSL_KEY_STORE_TYPE, this.jmsDelegate.sslKeystoreType );
      this.sslConfig.put( SSL_KEY_STORE_PATH, this.jmsDelegate.sslKeystorePath );
      this.sslConfig.put( SSL_KEY_STORE_PASS, this.jmsDelegate.sslKeystorePassword );
      this.sslConfig.put( SSL_CONTEXT_ALGORITHM, this.jmsDelegate.sslContextAlgorithm );
    }
    this.sslConfig.put( SSL_CIPHER_SUITE, this.jmsDelegate.sslCipherSuite );
    this.sslConfig.put( SSL_IBM_FIPS_REQUIRED, this.jmsDelegate.ibmSslFipsRequired );

    this.checkBoxTableCombo.updateDataMap( this.sslConfig );
  }

  private void populateTableActiveMq() {
    this.sslConfig = new HashMap<>();

    if ( !checkBoxTableCombo.getUseDefaultSslContext() ) {
      this.sslConfig.put( SSL_TRUST_STORE_PATH, this.jmsDelegate.sslTruststorePath );
      this.sslConfig.put( SSL_TRUST_STORE_PASSWORD, this.jmsDelegate.sslTruststorePassword );
      this.sslConfig.put( SSL_KEY_STORE_PATH, this.jmsDelegate.sslKeystorePath );
      this.sslConfig.put( SSL_KEY_STORE_PASS, this.jmsDelegate.sslKeystorePassword );
      this.sslConfig.put( SSL_CONTEXT_ALGORITHM, this.jmsDelegate.sslContextAlgorithm );
      this.sslConfig.put( SSL_CIPHER_SUITE, this.jmsDelegate.sslCipherSuite );
      this.sslConfig.put( SSL_AMQ_PROVIDER, this.jmsDelegate.amqSslProvider );
      this.sslConfig.put( SSL_AMQ_TRUST_ALL, this.jmsDelegate.amqSslTrustAll );
      this.sslConfig.put( SSL_AMQ_VERIFY_HOST, this.jmsDelegate.amqSslVerifyHost );
    }

    this.checkBoxTableCombo.updateDataMap( this.sslConfig );
  }

  void toggleVisibility( JmsProvider.ConnectionType type ) {
    checkBoxTableCombo.setSelectedConnectionType( type );
    if ( type == JmsProvider.ConnectionType.WEBSPHERE ) {
      activeMqAuthComposite.setVisible( false );
      ibmMqAuthComposite.setVisible( true );
      populateTableIbm();
    } else if ( type == JmsProvider.ConnectionType.ACTIVEMQ ) {
      ibmMqAuthComposite.setVisible( false );
      activeMqAuthComposite.setVisible( true );
      populateTableActiveMq();
    }
    checkBoxTableCombo.resetPropertyTableVisibility();
  }

  void setConnectionForm( ConnectionForm connectionForm ) {
    checkBoxTableCombo.setConnectionForm( connectionForm );
  }

}
