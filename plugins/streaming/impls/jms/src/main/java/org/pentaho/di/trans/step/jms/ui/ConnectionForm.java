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
import org.pentaho.di.trans.step.jms.JmsConsumerMeta;
import org.pentaho.di.trans.step.jms.JmsMeta;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.widget.TextVar;

import java.util.List;

import static com.google.common.collect.ImmutableList.of;
import static org.pentaho.di.i18n.BaseMessages.getString;
import static org.pentaho.di.ui.trans.step.BaseStreamingDialog.INPUT_WIDTH;

public class ConnectionForm {
  private static Class<?> PKG = ConnectionForm.class;

  private final Composite parentComponent;
  JmsConsumerMeta jmsMeta;

  private TextVar wJmsUrl;
  private TextVar wJmsUser;
  private Group wConnectionGroup;

  private PropsUI props;
  private TransMeta transMeta;
  private ModifyListener lsMod;
  private JmsMeta meta;

  List<ConnectionType> connTypes = of(
    new ConnectionType( getString( PKG, "JmsDialog.ActiveMQ" ) ),
    new ConnectionType( getString( PKG, "JmsDialog.IBMMQ" ) ),
    new ConnectionType( getString( PKG, "JmsDialog.Generic" ) ) );

  ConnectionForm( Composite parentComponent, PropsUI props, TransMeta transMeta,
                  ModifyListener lsMod ) {
    this.parentComponent = parentComponent;
    this.props = props;
    this.transMeta = transMeta;
    this.lsMod = lsMod;
  }

  public String getUrl() {
    return wJmsUrl.getText();
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

    return wConnectionGroup;
  }

  private void displayConnTypes( Group wConnectionGroup ) {
    Button previous = null;
    for ( ConnectionType type : connTypes ) {

      Button connectionButton = new Button( wConnectionGroup, SWT.RADIO );
      connectionButton.setText( type.name );

      FormData fdbConnType = new FormData();
      fdbConnType.left = new FormAttachment( 0, 0 );
      if ( type == null ) {
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
    }
    layoutConnectionFields( previous );
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
    fdlIbmJmsUrl.width = 300;
    wlJmsUrl.setLayoutData( fdlIbmJmsUrl );

    wJmsUrl = new TextVar( transMeta, wConnectionGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wJmsUrl );
    wJmsUrl.addModifyListener( lsMod );
    FormData fdJmsUrl = new FormData();
    //fdJmsUrl.left = new FormAttachment( wlJmsUrl, 0, SWT.LEFT );
    fdJmsUrl.left = new FormAttachment( environmentSeparator, 15 );
    fdJmsUrl.top = new FormAttachment( wlJmsUrl, 5 );
    fdJmsUrl.width = 300;
    wJmsUrl.setLayoutData( fdJmsUrl );

    Label wlIbmUser = new Label( wConnectionGroup, SWT.LEFT );
    props.setLook( wlIbmUser );
    wlIbmUser.setText( getString( PKG, "JmsDialog.JmsUser" ) );
    FormData fdlIbmUser = new FormData();
    fdlIbmUser.left = new FormAttachment( environmentSeparator, 15 );
    fdlIbmUser.top = new FormAttachment( wJmsUrl, 10 );
    fdlIbmUser.width = 300;
    wlIbmUser.setLayoutData( fdlIbmUser );

    wJmsUser = new TextVar( transMeta, wConnectionGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wJmsUrl );
    wJmsUser.addModifyListener( lsMod );
    FormData fdIbmJmsUser = new FormData();
    fdIbmJmsUser.left = new FormAttachment( environmentSeparator, 15 );
    fdIbmJmsUser.top = new FormAttachment( wlIbmUser, 5 );
    fdIbmJmsUser.width = 300;
    wJmsUser.setLayoutData( fdIbmJmsUser );
  }

  //  getString( PKG, "JmsDialog.URL" ),
  //  getString( PKG, "JmsDialog.Username" ),
  //  getString( PKG, "JmsDialog.Password" )

  static class ConnectionType {
    String name;
    List<Control> visibleControls = of(
    );

    ConnectionType( String n ) {
      name = n;
    }

    ConnectionType( String n, List<Control> visibleFields ) {
      this.visibleControls = visibleFields;
    }
  }
}
