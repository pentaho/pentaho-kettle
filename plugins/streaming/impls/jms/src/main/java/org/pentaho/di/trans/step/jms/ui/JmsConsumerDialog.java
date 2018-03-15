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
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.jms.JmsConsumerMeta;
import org.pentaho.di.trans.streaming.common.BaseStreamStepMeta;
import org.pentaho.di.ui.trans.step.BaseStreamingDialog;

import static org.pentaho.di.trans.step.jms.JmsConstants.PKG;

public class JmsConsumerDialog extends BaseStreamingDialog {
  JmsConsumerMeta jmsMeta;
  private DestinationForm destinationForm;
  private ConnectionForm connectionForm;

  public JmsConsumerDialog( Shell parent, Object in, TransMeta tr, String sname ) {
    super( parent, in, tr, sname );
    jmsMeta = (JmsConsumerMeta) in;
  }

  @Override protected String getDialogTitle() {
    return BaseMessages.getString( PKG, "JMSConsumerDialog.Shell.Title" );
  }

  @Override protected void buildSetup( Composite wSetupComp ) {
    Composite frame = new Composite( wSetupComp, SWT.NONE );
    props.setLook( frame );
    FormLayout setupLayout = new FormLayout();
    setupLayout.marginHeight = 15;
    setupLayout.marginWidth = 15;
    wSetupComp.setLayout( setupLayout );
    connectionForm = new ConnectionForm( frame, props, transMeta, lsMod );
    Group group = connectionForm.layoutForm();

    destinationForm = new DestinationForm( frame, group, props, transMeta, lsMod );
    destinationForm.layoutForm();

  }

  @Override protected void additionalOks( BaseStreamStepMeta meta ) {
    jmsMeta.url = connectionForm.getUrl();
    // TODO all other props
  }

  @Override protected int[] getFieldTypes() {
    return new int[ 0 ];
  }

  @Override protected String[] getFieldNames() {
    return new String[ 0 ];
  }
}
