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
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.jms.JmsConsumerMeta;
import org.pentaho.di.trans.step.jms.JmsDelegate;
import org.pentaho.di.trans.streaming.common.BaseStreamStepMeta;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStreamingDialog;

import static org.pentaho.di.i18n.BaseMessages.getString;
import static org.pentaho.di.trans.step.jms.JmsConstants.PKG;

public class JmsConsumerDialog extends BaseStreamingDialog {
  private final JmsDelegate jmsDelegate;
  private JmsConsumerMeta jmsMeta;
  private DestinationForm destinationForm;
  private ConnectionForm connectionForm;
  private TextVar wReceiverTimeout;
  private FieldsTab fieldsTab;

  public JmsConsumerDialog( Shell parent, Object in, TransMeta tr, String sname ) {
    super( parent, in, tr, sname );
    jmsMeta = (JmsConsumerMeta) in;
    jmsDelegate = jmsMeta.jmsDelegate;
  }

  @Override protected String getDialogTitle() {
    return getString( PKG, "JMSConsumerDialog.Shell.Title" );
  }

  @Override protected void buildSetup( Composite wSetupComp ) {
    props.setLook( wSetupComp );
    FormLayout setupLayout = new FormLayout();
    setupLayout.marginHeight = 15;
    setupLayout.marginWidth = 15;
    wSetupComp.setLayout( setupLayout );
    connectionForm = new ConnectionForm( wSetupComp, props, transMeta, lsMod, jmsMeta.jmsDelegate );
    Group group = connectionForm.layoutForm();

    destinationForm =
      new DestinationForm( wSetupComp, group, props, transMeta, lsMod, jmsDelegate.destinationType,
        jmsDelegate.destinationName );
    Composite previousComp = destinationForm.layoutForm();

    Label wlReceiveTimeout = new Label( wSetupComp, SWT.LEFT );
    props.setLook( wlReceiveTimeout );
    wlReceiveTimeout.setText( getString( PKG, "JmsDialog.ReceiveTimeout" ) );
    FormData fdlReceiveTimeout = new FormData();
    fdlReceiveTimeout.left = new FormAttachment( 0, 0 );
    fdlReceiveTimeout.top = new FormAttachment( previousComp, 15 );
    wlReceiveTimeout.setLayoutData( fdlReceiveTimeout );

    wReceiverTimeout = new TextVar( transMeta, wSetupComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wReceiverTimeout );
    FormData fdReceiveTimeout = new FormData();
    fdReceiveTimeout.left = new FormAttachment( 0, 0 );
    fdReceiveTimeout.top = new FormAttachment( wlReceiveTimeout, 5 );
    fdReceiveTimeout.width = 140;
    wReceiverTimeout.setLayoutData( fdReceiveTimeout );
    wReceiverTimeout.addModifyListener( lsMod );
    wReceiverTimeout.setText( jmsDelegate.receiveTimeout );

  }

  @Override protected void createAdditionalTabs() {
    fieldsTab = new FieldsTab(
      wTabFolder, props, transMeta, lsMod, jmsDelegate.messageField, jmsDelegate.destinationField );

    fieldsTab.buildFieldsTab();
  }


  @Override protected void additionalOks( BaseStreamStepMeta meta ) {
    jmsDelegate.connectionType = connectionForm.getConnectionType();

    jmsDelegate.ibmUrl = connectionForm.getIbmUrl();
    jmsDelegate.ibmUsername = connectionForm.getIbmUser();
    jmsDelegate.ibmPassword = connectionForm.getIbmPassword();

    jmsDelegate.amqUrl = connectionForm.getActiveUrl();
    jmsDelegate.amqUsername = connectionForm.getActiveUser();
    jmsDelegate.amqPassword = connectionForm.getActivePassword();

    jmsDelegate.connectionType = connectionForm.getConnectionType();

    jmsDelegate.destinationType = destinationForm.getDestinationType();
    jmsDelegate.destinationName = destinationForm.getDestinationName();
    jmsDelegate.messageField = fieldsTab.getFieldNames()[ 0 ];
    jmsDelegate.destinationField = fieldsTab.getFieldNames()[ 1 ];
    jmsDelegate.receiveTimeout = wReceiverTimeout.getText();
  }


  @Override protected int[] getFieldTypes() {
    return fieldsTab.getFieldTypes();
  }

  @Override protected String[] getFieldNames() {
    return fieldsTab.getFieldNames();
  }
}
