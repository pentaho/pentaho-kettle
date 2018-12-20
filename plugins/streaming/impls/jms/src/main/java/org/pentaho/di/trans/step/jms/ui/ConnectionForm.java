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
import org.pentaho.di.ui.core.widget.TextVar;

import java.util.EnumMap;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.pentaho.di.i18n.BaseMessages.getString;
import static org.pentaho.di.trans.step.jms.JmsConstants.PKG;
import static org.pentaho.di.ui.trans.step.BaseStreamingDialog.INPUT_WIDTH;

public class ConnectionForm {

  private final Composite parentComponent;
  private final JmsDelegate jmsDelegate;
  private final JmsDialogSecurityLayout jmsDialogSecurityLayout;

  private PropsUI props;
  private TransMeta transMeta;
  private ModifyListener lsMod;

  private EnumMap<JmsProvider.ConnectionType, Button> typesToButtons
    = new EnumMap<>( JmsProvider.ConnectionType.class );
  private Group wConnectionGroup;

  private Label wlIbmUrl;
  private TextVar wIbmUrl;

  private Label wlActiveUrl;
  private TextVar wActiveUrl;

  ConnectionForm( Composite parentComponent, PropsUI props, TransMeta transMeta,
                  ModifyListener lsMod, JmsDelegate jmsDelegate, JmsDialogSecurityLayout jmsDialogSecurityLayout ) {
    checkNotNull( parentComponent );
    checkNotNull( props );
    checkNotNull( transMeta );
    checkNotNull( lsMod );

    this.parentComponent = parentComponent;
    this.props = props;
    this.transMeta = transMeta;
    this.lsMod = lsMod;
    this.jmsDelegate = jmsDelegate;
    this.jmsDialogSecurityLayout = jmsDialogSecurityLayout;
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
    wActiveUrl.setText( jmsDelegate.amqUrl );

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
  }

  public String getIbmUrl() {
    return wIbmUrl.getText();
  }

  public String getActiveUrl() {
    return wActiveUrl.getText();
  }

  public String getConnectionType() {
    return typesToButtons.entrySet().stream()
      .filter( entry -> entry.getValue().getSelection() )
      .findFirst()
      .orElseThrow( () -> new IllegalStateException( "One type should be selected" ) )
      .getKey().name();
  }

  private void toggleVisibility( JmsProvider.ConnectionType type ) {
    if ( type == JmsProvider.ConnectionType.WEBSPHERE ) {
      jmsDialogSecurityLayout.toggleVisibility( type );
      setIbmMqVisibility( true );
      setActiveMqVisibility( false );
    } else if ( type == JmsProvider.ConnectionType.ACTIVEMQ ) {
      jmsDialogSecurityLayout.toggleVisibility( type );
      setIbmMqVisibility( false );
      setActiveMqVisibility( true );

    }
  }

  private void setIbmMqVisibility( boolean isVisible ) {
    wlIbmUrl.setVisible( isVisible );
    wIbmUrl.setVisible( isVisible );
  }

  private void setActiveMqVisibility( boolean isVisible ) {
    wlActiveUrl.setVisible( isVisible );
    wActiveUrl.setVisible( isVisible );
  }
}
