/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.jms.JmsDelegate;
import org.pentaho.di.trans.step.jms.context.JmsProvider;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.widget.PasswordTextVar;
import org.pentaho.di.ui.core.widget.TextVar;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.pentaho.di.i18n.BaseMessages.getString;
import static org.pentaho.di.trans.step.jms.JmsConstants.PKG;
import static org.pentaho.di.ui.trans.step.BaseStreamingDialog.INPUT_WIDTH;

public class ConnectionForm {

  private final Composite parentComponent;
  private final JmsDelegate jmsDelegate;
  private final JmsProvider.ConnectionType currentConnType;

  private PropsUI props;
  private TransMeta transMeta;
  private ModifyListener lsMod;

  private Map<JmsProvider.ConnectionType, Button> typesToButtons = new HashMap<>();
  private Group wConnectionGroup;

  private Label wlIbmUrl;
  private Label wlIbmUser;
  private Label wlIbmPassword;
  private TextVar wIbmUrl;
  private TextVar wIbmUser;
  private TextVar wIbmPassword;

  private Label wlActiveUrl;
  private Label wlActiveUser;
  private Label wlActivePassword;
  private TextVar wActiveUrl;
  private TextVar wActiveUser;
  private TextVar wActivePassword;

  ConnectionForm( Composite parentComponent, PropsUI props, TransMeta transMeta,
                  ModifyListener lsMod, JmsDelegate jmsDelegate ) {
    checkNotNull( parentComponent );
    checkNotNull( props );
    checkNotNull( transMeta );
    checkNotNull( lsMod );

    this.parentComponent = parentComponent;
    this.props = props;
    this.transMeta = transMeta;
    this.lsMod = lsMod;
    this.jmsDelegate = jmsDelegate;
    this.currentConnType = JmsProvider.ConnectionType.valueOf( jmsDelegate.connectionType );
  }


  Group layoutForm() {
    FormLayout setupLayout = new FormLayout();
    setupLayout.marginHeight = 15;
    setupLayout.marginWidth = 15;
    parentComponent.setLayout( setupLayout );

    wConnectionGroup = new Group( parentComponent, SWT.SHADOW_ETCHED_IN );
    wConnectionGroup.setText( getString( PKG, "JmsDialog.Connection" ) );

    FormLayout flConnection = new FormLayout();
    flConnection.marginHeight = 15;
    flConnection.marginWidth = 15;
    wConnectionGroup.setLayout( flConnection );

    FormData fdConnectionGroup = new FormData();
    fdConnectionGroup.left = new FormAttachment( 0, 0 );
    fdConnectionGroup.top = new FormAttachment( 0, 0 );
    fdConnectionGroup.right = new FormAttachment( 100, 0 );
    fdConnectionGroup.width = INPUT_WIDTH;
    wConnectionGroup.setLayoutData( fdConnectionGroup );

    props.setLook( wConnectionGroup );

    displayConnTypes( wConnectionGroup );

    setStartingsVals();

    return wConnectionGroup;
  }

  private void setStartingsVals() {
    wIbmUrl.setText( jmsDelegate.ibmUrl );
    wIbmUser.setText( jmsDelegate.ibmUsername );
    wIbmPassword.setText( jmsDelegate.ibmPassword );

    wActiveUrl.setText( jmsDelegate.amqUrl );
    wActiveUser.setText( jmsDelegate.amqUsername );
    wActivePassword.setText( jmsDelegate.amqPassword );

    JmsProvider.ConnectionType connectionType = JmsProvider.ConnectionType.valueOf( jmsDelegate.connectionType );
    typesToButtons.get( connectionType )
      .setSelection( true );
    toggleVisibility( connectionType );
  }

  private void displayConnTypes( Group wConnectionGroup ) {
    Button previous = null;
    Button widestWidget = null;
    for ( JmsProvider.ConnectionType type : JmsProvider.ConnectionType.values() ) {

      Button connectionButton = new Button( wConnectionGroup, SWT.RADIO );
      connectionButton.setText( type.toString() );

      typesToButtons.put( type, connectionButton );


      FormData fdbConnType = new FormData();
      fdbConnType.left = new FormAttachment( 0, 0 );
      if ( previous == null ) {
        fdbConnType.top = new FormAttachment( 0, 0 );
      } else {
        fdbConnType.top = new FormAttachment( previous, 10 );
      }
      previous = connectionButton;
      connectionButton.setLayoutData( fdbConnType );

      connectionButton.addSelectionListener( new SelectionListener() {
        @Override public void widgetSelected( final SelectionEvent selectionEvent ) {
          lsMod.modifyText( null );
          toggleVisibility( type );
        }

        @Override public void widgetDefaultSelected( final SelectionEvent selectionEvent ) {
          toggleVisibility( type );
        }
      } );
      props.setLook( connectionButton );
      if ( null == widestWidget || previous.getSize().y > widestWidget.getSize().y ) {
        widestWidget = previous;
      }
    }
    Label environmentSeparator = new Label( wConnectionGroup, SWT.SEPARATOR | SWT.VERTICAL );

    FormData fdenvironmentSeparator = new FormData();
    fdenvironmentSeparator.top = new FormAttachment( 0, 0 );
    fdenvironmentSeparator.left = new FormAttachment( widestWidget, 15 );
    fdenvironmentSeparator.bottom = new FormAttachment( 100, 0 );
    environmentSeparator.setLayoutData( fdenvironmentSeparator );

    layoutIbmMqConnectionFields( environmentSeparator );
    layoutActiveMqConnectionFields( environmentSeparator );
  }

  private void layoutIbmMqConnectionFields( Control leftOf ) {
    wlIbmUrl = new Label( wConnectionGroup, SWT.LEFT );
    props.setLook( wlIbmUrl );
    wlIbmUrl.setText( getString( PKG, "JmsDialog.JmsUrl" ) );
    FormData fdlJmsUrl = new FormData();
    fdlJmsUrl.left = new FormAttachment( leftOf, 15 );
    fdlJmsUrl.top = new FormAttachment( 0, 0 );
    wlIbmUrl.setLayoutData( fdlJmsUrl );

    wIbmUrl = new TextVar( transMeta, wConnectionGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wIbmUrl.setToolTipText( JmsProvider.ConnectionType.WEBSPHERE.getUrlHint() );
    props.setLook( wIbmUrl );
    wIbmUrl.addModifyListener( lsMod );
    FormData fdUrl = new FormData();
    fdUrl.left = new FormAttachment( leftOf, 15 );
    fdUrl.top = new FormAttachment( wlIbmUrl, 5 );
    fdUrl.right = new FormAttachment( 100, 0 );
    wIbmUrl.setLayoutData( fdUrl );

    wlIbmUser = new Label( wConnectionGroup, SWT.LEFT );
    props.setLook( wlIbmUser );
    wlIbmUser.setText( getString( PKG, "JmsDialog.JmsUser" ) );
    FormData fdlUser = new FormData();
    fdlUser.left = new FormAttachment( leftOf, 15 );
    fdlUser.top = new FormAttachment( wIbmUrl, 10 );
    wlIbmUser.setLayoutData( fdlUser );

    wIbmUser = new TextVar( transMeta, wConnectionGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wIbmUser );
    wIbmUser.addModifyListener( lsMod );
    FormData fdUser = new FormData();
    fdUser.left = new FormAttachment( leftOf, 15 );
    fdUser.top = new FormAttachment( wlIbmUser, 5 );
    fdUser.right = new FormAttachment( 100, 0 );
    wIbmUser.setLayoutData( fdUser );

    wlIbmPassword = new Label( wConnectionGroup, SWT.LEFT );
    props.setLook( wlIbmPassword );
    wlIbmPassword.setText( getString( PKG, "JmsDialog.JmsPassword" ) );
    FormData fdlPassword = new FormData();
    fdlPassword.left = new FormAttachment( leftOf, 15 );
    fdlPassword.top = new FormAttachment( wIbmUser, 10 );
    wlIbmPassword.setLayoutData( fdlPassword );

    wIbmPassword = new PasswordTextVar( transMeta, wConnectionGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wIbmPassword );
    wIbmPassword.addModifyListener( lsMod );
    FormData fdPassword = new FormData();
    fdPassword.left = new FormAttachment( leftOf, 15 );
    fdPassword.top = new FormAttachment( wlIbmPassword, 5 );
    fdPassword.right = new FormAttachment( 100, 0 );
    wIbmPassword.setLayoutData( fdPassword );
  }

  private void layoutActiveMqConnectionFields( Control leftOf ) {
    wlActiveUrl = new Label( wConnectionGroup, SWT.LEFT );
    props.setLook( wlActiveUrl );
    wlActiveUrl.setText( getString( PKG, "JmsDialog.JmsUrl" ) );
    FormData fdlJmsUrl = new FormData();
    fdlJmsUrl.left = new FormAttachment( leftOf, 15 );
    fdlJmsUrl.top = new FormAttachment( 0, 0 );
    wlActiveUrl.setLayoutData( fdlJmsUrl );

    wActiveUrl = new TextVar( transMeta, wConnectionGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wActiveUrl.setToolTipText( JmsProvider.ConnectionType.ACTIVEMQ.getUrlHint() );
    props.setLook( wActiveUrl );
    wActiveUrl.addModifyListener( lsMod );
    FormData fdJmsUrl = new FormData();
    fdJmsUrl.left = new FormAttachment( leftOf, 15 );
    fdJmsUrl.top = new FormAttachment( wlActiveUrl, 5 );
    fdJmsUrl.right = new FormAttachment( 100, 0 );
    wActiveUrl.setLayoutData( fdJmsUrl );

    wlActiveUser = new Label( wConnectionGroup, SWT.LEFT );
    props.setLook( wlActiveUser );
    wlActiveUser.setText( getString( PKG, "JmsDialog.JmsUser" ) );
    FormData fdlUser = new FormData();
    fdlUser.left = new FormAttachment( leftOf, 15 );
    fdlUser.top = new FormAttachment( wActiveUrl, 10 );
    wlActiveUser.setLayoutData( fdlUser );

    wActiveUser = new TextVar( transMeta, wConnectionGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wActiveUser );
    wActiveUser.addModifyListener( lsMod );
    FormData fdUser = new FormData();
    fdUser.left = new FormAttachment( leftOf, 15 );
    fdUser.top = new FormAttachment( wlActiveUser, 5 );
    fdUser.right = new FormAttachment( 100, 0 );
    wActiveUser.setLayoutData( fdUser );

    wlActivePassword = new Label( wConnectionGroup, SWT.LEFT );
    props.setLook( wlActivePassword );
    wlActivePassword.setText( getString( PKG, "JmsDialog.JmsPassword" ) );
    FormData fdlPassword = new FormData();
    fdlPassword.left = new FormAttachment( leftOf, 15 );
    fdlPassword.top = new FormAttachment( wActiveUser, 10 );
    wlActivePassword.setLayoutData( fdlPassword );

    wActivePassword = new PasswordTextVar( transMeta, wConnectionGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wActivePassword );
    wActivePassword.addModifyListener( lsMod );
    FormData fdPassword = new FormData();
    fdPassword.left = new FormAttachment( leftOf, 15 );
    fdPassword.top = new FormAttachment( wlActivePassword, 5 );
    fdPassword.right = new FormAttachment( 100, 0 );
    wActivePassword.setLayoutData( fdPassword );
  }

  public String getIbmUrl() {
    return wIbmUrl.getText();
  }

  public String getIbmUser() {
    return wIbmUser.getText();
  }

  public String getIbmPassword() {
    return wIbmPassword.getText();
  }

  public String getActiveUrl() {
    return wActiveUrl.getText();
  }

  public String getActiveUser() {
    return wActiveUser.getText();
  }

  public String getActivePassword() {
    return wActivePassword.getText();
  }

  public String getConnectionType() {
    return typesToButtons.entrySet().stream()
      .filter( entry -> entry.getValue().getSelection() )
      .findFirst()
      .orElseThrow( () -> new IllegalStateException( "One type should be selected" ) )
      .getKey().name();
  }

  private void toggleVisibility( JmsProvider.ConnectionType type ) {
    switch ( type ) {
      case WEBSPHERE:
        setActiveMqVisibility( false );
        setIbmMqVisibility( true );
        break;
      case ACTIVEMQ:
        setIbmMqVisibility( false );
        setActiveMqVisibility( true );
        break;
    }
  }

  private void setIbmMqVisibility( boolean isVisible ) {
    wlIbmUrl.setVisible( isVisible );
    wlIbmUser.setVisible( isVisible );
    wlIbmPassword.setVisible( isVisible );
    wIbmUrl.setVisible( isVisible );
    wIbmUser.setVisible( isVisible );
    wIbmPassword.setVisible( isVisible );
  }

  private void setActiveMqVisibility( boolean isVisible ) {
    wlActiveUrl.setVisible( isVisible );
    wlActiveUser.setVisible( isVisible );
    wlActivePassword.setVisible( isVisible );
    wActiveUrl.setVisible( isVisible );
    wActiveUser.setVisible( isVisible );
    wActivePassword.setVisible( isVisible );
  }
}
