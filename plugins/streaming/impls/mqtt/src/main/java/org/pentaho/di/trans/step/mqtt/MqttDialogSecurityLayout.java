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

package org.pentaho.di.trans.step.mqtt;

import com.google.common.collect.Lists;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.PasswordTextVar;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Strings.nullToEmpty;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyMap;
import static java.util.Collections.sort;
import static org.pentaho.di.ui.trans.step.BaseStreamingDialog.INPUT_WIDTH;

/**
 * Common SWT layout for both the consumer and producer
 */
class MqttDialogSecurityLayout {

  private static Class<?> PKG = MqttDialogSecurityLayout.class;

  private final PropsUI props;
  private final CTabFolder wTabFolder;
  private final ModifyListener lsMod;
  private final TransMeta transMeta;
  private final String password;
  private final Map<String, String> sslConfig;
  private final boolean sslEnabled;

  private TableView sslTable;
  private Button wUseSSL;
  private TextVar wUsername;
  private PasswordTextVar wPassword;
  private String username;

  MqttDialogSecurityLayout(
    PropsUI props, CTabFolder wTabFolder, String username,
    String password, ModifyListener lsMod, TransMeta transMeta,
    Map<String, String> sslConfig, boolean sslEnabled ) {
    checkNotNull( props );
    checkNotNull( wTabFolder );
    checkNotNull( lsMod );
    checkNotNull( transMeta );

    this.props = props;
    this.wTabFolder = wTabFolder;
    this.lsMod = lsMod;
    this.transMeta = transMeta;
    this.sslEnabled = sslEnabled;
    this.sslConfig = Optional.ofNullable( sslConfig ).orElse( emptyMap() );
    this.username = nullToEmpty( username );
    this.password = nullToEmpty( password );
  }

  String username() {
    return wUsername.getText();
  }

  String password() {
    return wPassword.getText();
  }

  Map<String, String> sslConfig() {
    return tableToMap( sslTable );
  }

  boolean useSsl() {
    return wUseSSL.getSelection();
  }

  void buildSecurityTab() {
    CTabItem wSecurityTab = new CTabItem( wTabFolder, SWT.NONE, 1 );
    wSecurityTab.setText( BaseMessages.getString( PKG, "MQTTDialog.Security.Tab" ) );

    Composite wSecurityComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wSecurityComp );
    FormLayout securityLayout = new FormLayout();
    securityLayout.marginHeight = 15;
    securityLayout.marginWidth = 15;
    wSecurityComp.setLayout( securityLayout );

    // Authentication group
    Group wAuthenticationGroup = new Group( wSecurityComp, SWT.SHADOW_ETCHED_IN );
    props.setLook( wAuthenticationGroup );
    wAuthenticationGroup.setText( BaseMessages.getString( PKG, "MQTTDialog.Security.Authentication" ) );
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

    Label wlUsername = new Label( wAuthenticationGroup, SWT.LEFT );
    props.setLook( wlUsername );
    wlUsername.setText( BaseMessages.getString( PKG, "MQTTDialog.Security.Username" ) );
    FormData fdlUsername = new FormData();
    fdlUsername.left = new FormAttachment( 0, 0 );
    fdlUsername.top = new FormAttachment( 0, 0 );
    fdlUsername.right = new FormAttachment( 0, INPUT_WIDTH );
    wlUsername.setLayoutData( fdlUsername );

    wUsername = new TextVar( transMeta, wAuthenticationGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );

    props.setLook( wUsername );
    wUsername.addModifyListener( lsMod );
    FormData fdUsername = new FormData();
    fdUsername.left = new FormAttachment( 0, 0 );
    fdUsername.top = new FormAttachment( wlUsername, 5 );
    fdUsername.right = new FormAttachment( 0, INPUT_WIDTH );
    wUsername.setLayoutData( fdUsername );

    Label wlPassword = new Label( wAuthenticationGroup, SWT.LEFT );
    props.setLook( wlPassword );
    wlPassword.setText( BaseMessages.getString( PKG, "MQTTDialog.Security.Password" ) );
    FormData fdlPassword = new FormData();
    fdlPassword.left = new FormAttachment( 0, 0 );
    fdlPassword.top = new FormAttachment( wUsername, 10 );
    fdlPassword.right = new FormAttachment( 0, INPUT_WIDTH );
    wlPassword.setLayoutData( fdlPassword );

    wPassword = new PasswordTextVar( transMeta, wAuthenticationGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wPassword );
    wPassword.addModifyListener( lsMod );
    FormData fdPassword = new FormData();
    fdPassword.left = new FormAttachment( 0, 0 );
    fdPassword.top = new FormAttachment( wlPassword, 5 );
    fdPassword.right = new FormAttachment( 0, INPUT_WIDTH );
    wPassword.setLayoutData( fdPassword );

    wUseSSL = new Button( wSecurityComp, SWT.CHECK );
    wUseSSL.setText( BaseMessages.getString( PKG, "MQTTDialog.Security.UseSSL" ) );
    props.setLook( wUseSSL );
    FormData fdUseSSL = new FormData();
    fdUseSSL.top = new FormAttachment( wAuthenticationGroup, 15 );
    fdUseSSL.left = new FormAttachment( 0, 0 );
    wUseSSL.setLayoutData( fdUseSSL );
    wUseSSL.addSelectionListener( new SelectionListener() {
      @Override public void widgetSelected( SelectionEvent selectionEvent ) {
        boolean selection = ( (Button) selectionEvent.getSource() ).getSelection();
        sslTable.setEnabled( selection );
        sslTable.table.setEnabled( selection );
      }

      @Override public void widgetDefaultSelected( SelectionEvent selectionEvent ) {
        boolean selection = ( (Button) selectionEvent.getSource() ).getSelection();
        sslTable.setEnabled( selection );
        sslTable.table.setEnabled( selection );
      }
    } );

    Label wlSSLProperties = new Label( wSecurityComp, SWT.LEFT );
    wlSSLProperties.setText( BaseMessages.getString( PKG, "MQTTDialog.Security.SSLProperties" ) );
    props.setLook( wlSSLProperties );
    FormData fdlSSLProperties = new FormData();
    fdlSSLProperties.top = new FormAttachment( wUseSSL, 10 );
    fdlSSLProperties.left = new FormAttachment( 0, 0 );
    wlSSLProperties.setLayoutData( fdlSSLProperties );

    FormData fdSecurityComp = new FormData();
    fdSecurityComp.left = new FormAttachment( 0, 0 );
    fdSecurityComp.top = new FormAttachment( 0, 0 );
    fdSecurityComp.right = new FormAttachment( 100, 0 );
    fdSecurityComp.bottom = new FormAttachment( 100, 0 );
    wSecurityComp.setLayoutData( fdSecurityComp );

    buildSSLTable( wSecurityComp, wlSSLProperties );

    wSecurityComp.layout();
    wSecurityTab.setControl( wSecurityComp );
  }

  void setUIText() {
    wUseSSL.setSelection( sslEnabled );
    sslTable.setEnabled( sslEnabled );
    sslTable.table.setEnabled( sslEnabled );

    sslTable.table.select( 0 );
    sslTable.table.showSelection();

    wUsername.setText( username );
    wPassword.setText( password );
  }

  private void buildSSLTable( Composite parentWidget, Control relativePosition ) {
    ColumnInfo[] columns = getSSLColumns();

    sslTable = new TableView(
      transMeta,
      parentWidget,
      SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI,
      columns,
      0,  // num of starting rows (will be added later)
      false,
      lsMod,
      props,
      false
    );

    sslTable.setSortable( false );
    sslTable.getTable().addListener( SWT.Resize, event -> {
      Table table = (Table) event.widget;
      table.getColumn( 1 ).setWidth( 200 );
      table.getColumn( 2 ).setWidth( 200 );
    } );

    populateSSLData();

    FormData fdData = new FormData();
    fdData.left = new FormAttachment( 0, 0 );
    fdData.top = new FormAttachment( relativePosition, 5 );
    fdData.bottom = new FormAttachment( 100, 0 );
    fdData.width = INPUT_WIDTH + 80;

    // resize the columns to fit the data in them
    stream( sslTable.getTable().getColumns() ).forEach( column -> {
      if ( column.getWidth() > 0 ) {
        // don't pack anything with a 0 width, it will resize it to make it visible (like the index column)
        column.setWidth( 200 );
      }
    } );

    sslTable.setLayoutData( fdData );
  }

  private ColumnInfo[] getSSLColumns() {
    ColumnInfo optionName = new ColumnInfo( BaseMessages.getString( PKG, "MQTTDialog.Security.SSL.Column.Name" ),
      ColumnInfo.COLUMN_TYPE_TEXT, false, false );

    ColumnInfo value = new ColumnInfo( BaseMessages.getString( PKG, "MQTTDialog.Security.SSL.Column.Value" ),
      ColumnInfo.COLUMN_TYPE_TEXT, false, false, 200 );
    value.setUsingVariables( true );

    return new ColumnInfo[] { optionName, value };
  }

  private void populateSSLData() {
    sslTable.getTable().removeAll();
    new TableItem( sslTable.getTable(), SWT.NONE );

    checkNotNull( sslTable.getItem( 0 ) );
    checkState( sslTable.getItem( 0 ).length == 2 );

    List<String> keys = Lists.newArrayList( sslConfig.keySet() );
    sort( keys );

    String firstKey = keys.remove( 0 );
    sslTable.getTable().getItem( 0 ).setText( 1, firstKey );
    sslTable.getTable().getItem( 0 ).setText( 2, sslConfig.get( firstKey ) );

    keys.stream()
      .forEach( key -> sslTable.add( key, sslConfig.get( key ) ) );
  }

  private Map<String, String> tableToMap( TableView table ) {
    return IntStream.range( 0, table.getItemCount() )
      .mapToObj( table::getItem )
      .collect( Collectors.toMap( strArray -> strArray[ 0 ], strArray -> strArray[ 1 ] ) );
  }
}
