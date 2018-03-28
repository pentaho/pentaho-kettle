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
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.widget.ComboVar;
import org.pentaho.di.ui.core.widget.TextVar;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.pentaho.di.i18n.BaseMessages.getString;
import static org.pentaho.di.trans.step.jms.JmsConstants.PKG;

public class DestinationForm {

  private final Composite parentComponent;
  private final PropsUI props;
  private final TransMeta transMeta;
  private final ModifyListener lsMod;
  private final Composite aboveComposite;
  private ComboVar destinationType;
  private TextVar destinationName;
  private String destinationTypeValue;
  private String destinationNameValue;


  DestinationForm( Composite parentComponent, Composite above, PropsUI props, TransMeta transMeta,
                   ModifyListener lsMod, String destinationType, String destinationName ) {
    checkNotNull( parentComponent );
    checkNotNull( above );
    checkNotNull( props );
    checkNotNull( transMeta );
    checkNotNull( lsMod );

    this.parentComponent = parentComponent;
    this.props = props;
    this.transMeta = transMeta;
    this.lsMod = lsMod;
    this.aboveComposite = above;
    destinationTypeValue = destinationType;
    destinationNameValue = destinationName;
  }

  public Composite layoutForm() {
    Label lbDestType = new Label( parentComponent, SWT.LEFT );
    props.setLook( lbDestType );
    lbDestType.setText( getString( PKG, "JmsDialog.DestinationType" ) );
    FormData fdDest = new FormData();
    fdDest.left = new FormAttachment( 0, 0 );
    fdDest.top = new FormAttachment( aboveComposite, 15 );
    fdDest.width = 140;
    lbDestType.setLayoutData( fdDest );

    destinationType = new ComboVar( transMeta, parentComponent, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( destinationType );
    destinationType.addModifyListener( lsMod );
    FormData fdDestType = new FormData();
    fdDestType.left = new FormAttachment( 0, 0 );
    fdDestType.top = new FormAttachment( lbDestType, 5 );
    fdDestType.width = 140;
    destinationType.setLayoutData( fdDestType );
    destinationType.add( getString( PKG, "JmsDialog.Dest.Topic" ) );
    destinationType.add( getString( PKG, "JmsDialog.Dest.Queue" ) );

    destinationType.addModifyListener( lsMod );


    Label lbDestName = new Label( parentComponent, SWT.LEFT );
    props.setLook( lbDestName );
    lbDestName.setText( getString( PKG, "JmsDialog.Dest.Name" ) );
    FormData fdlDestName = new FormData();
    fdlDestName.left = new FormAttachment( lbDestType, 15 );
    fdlDestName.top = new FormAttachment( aboveComposite, 15 );
    fdlDestName.right = new FormAttachment( 100, 0 );
    lbDestName.setLayoutData( fdlDestName );

    destinationName = new TextVar( transMeta, parentComponent, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( destinationName );
    FormData fdDestName = new FormData();
    fdDestName.left = new FormAttachment( destinationType, 15 );
    fdDestName.top = new FormAttachment( lbDestName, 5 );
    fdDestName.right = new FormAttachment( 100, 0 );
    destinationName.setLayoutData( fdDestName );

    destinationName.addModifyListener( lsMod );


    setStartingValues();

    return destinationType;
  }

  private void setStartingValues() {
    destinationName.setText( destinationNameValue );
    destinationType.setText( destinationTypeValue );
    destinationName.setToolTipText( getString( PKG, "JmsProvider.DestinationHint" ) );
  }

  public String getDestinationType() {
    return destinationType.getText();
  }

  public String getDestinationName() {
    return destinationName.getText();
  }
}
