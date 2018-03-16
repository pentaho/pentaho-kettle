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

  private TextVar wUrl;
  private TextVar wUser;
  private TextVar wPassword;
  private Group wConnectionGroup;

  private PropsUI props;
  private TransMeta transMeta;
  private ModifyListener lsMod;

  private Map<JmsProvider.ConnectionType, Button> typesToButtons = new HashMap<>();

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
    wUrl.setText( jmsDelegate.url );
    wUser.setText( jmsDelegate.username );
    wPassword.setText( jmsDelegate.password );

    typesToButtons.get( JmsProvider.ConnectionType.valueOf( jmsDelegate.connectionType ) )
      .setSelection( true );
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
          //toggleVisibility( true );
        }

        @Override public void widgetDefaultSelected( final SelectionEvent selectionEvent ) {
          //        toggleVisibility( true );
        }
      } );
      props.setLook( connectionButton );
      if ( null == widestWidget || previous.getSize().y > widestWidget.getSize().y ) {
        widestWidget = previous;
      }
    }
    layoutConnectionFields( widestWidget );
  }

  private void layoutConnectionFields( Control leftOf ) {
    Label environmentSeparator = new Label( wConnectionGroup, SWT.SEPARATOR | SWT.VERTICAL );

    FormData fdenvironmentSeparator = new FormData();
    fdenvironmentSeparator.top = new FormAttachment( 0, 0 );
    fdenvironmentSeparator.left = new FormAttachment( leftOf, 15 );
    fdenvironmentSeparator.bottom = new FormAttachment( 100, 0 );
    environmentSeparator.setLayoutData( fdenvironmentSeparator );

    Label wlJmsUrl = new Label( wConnectionGroup, SWT.LEFT );
    props.setLook( wlJmsUrl );
    wlJmsUrl.setText( getString( PKG, "JmsDialog.JmsUrl" ) );
    FormData fdlIbmJmsUrl = new FormData();
    fdlIbmJmsUrl.left = new FormAttachment( environmentSeparator, 15 );
    fdlIbmJmsUrl.top = new FormAttachment( 0, 0 );
    wlJmsUrl.setLayoutData( fdlIbmJmsUrl );

    wUrl = new TextVar( transMeta, wConnectionGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wUrl );
    wUrl.addModifyListener( lsMod );
    FormData fdJmsUrl = new FormData();
    fdJmsUrl.left = new FormAttachment( environmentSeparator, 15 );
    fdJmsUrl.top = new FormAttachment( wlJmsUrl, 5 );
    fdJmsUrl.right = new FormAttachment( 100, 0 );
    wUrl.setLayoutData( fdJmsUrl );

    Label wlIbmUser = new Label( wConnectionGroup, SWT.LEFT );
    props.setLook( wlIbmUser );
    wlIbmUser.setText( getString( PKG, "JmsDialog.JmsUser" ) );
    FormData fdlIbmUser = new FormData();
    fdlIbmUser.left = new FormAttachment( environmentSeparator, 15 );
    fdlIbmUser.top = new FormAttachment( wUrl, 10 );
    wlIbmUser.setLayoutData( fdlIbmUser );

    wUser = new TextVar( transMeta, wConnectionGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wUser );
    wUser.addModifyListener( lsMod );
    FormData fdIbmUser = new FormData();
    fdIbmUser.left = new FormAttachment( environmentSeparator, 15 );
    fdIbmUser.top = new FormAttachment( wlIbmUser, 5 );
    fdIbmUser.right = new FormAttachment( 100, 0 );
    wUser.setLayoutData( fdIbmUser );

    Label wlIbmPassword = new Label( wConnectionGroup, SWT.LEFT );
    props.setLook( wlIbmPassword );
    wlIbmPassword.setText( getString( PKG, "JmsDialog.JmsPassword" ) );
    FormData fdlIbmPassword = new FormData();
    fdlIbmPassword.left = new FormAttachment( environmentSeparator, 15 );
    fdlIbmPassword.top = new FormAttachment( wUser, 10 );
    wlIbmPassword.setLayoutData( fdlIbmPassword );

    wPassword = new PasswordTextVar( transMeta, wConnectionGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wPassword );
    wPassword.addModifyListener( lsMod );
    FormData fdIbmPassword = new FormData();
    fdIbmPassword.left = new FormAttachment( environmentSeparator, 15 );
    fdIbmPassword.top = new FormAttachment( wlIbmPassword, 5 );
    fdIbmPassword.right = new FormAttachment( 100, 0 );
    wPassword.setLayoutData( fdIbmPassword );
  }


  public String getUrl() {
    return wUrl.getText();
  }

  public String getUser() {
    return wUser.getText();
  }

  public String getPassword() {
    return wPassword.getText();
  }


}
