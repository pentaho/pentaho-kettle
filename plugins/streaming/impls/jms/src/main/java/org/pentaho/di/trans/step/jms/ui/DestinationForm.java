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

import static org.pentaho.di.i18n.BaseMessages.getString;

public class DestinationForm {
  private static Class<?> PKG = DestinationForm.class;

  private final Composite parentComponent;
  private final PropsUI props;
  private final TransMeta transMeta;
  private final ModifyListener lsMod;
  private final Composite aboveComposite;
  ComboVar destinationType;
  TextVar destinationName;


  DestinationForm( Composite parentComponent, Composite above, PropsUI props, TransMeta transMeta,
                   ModifyListener lsMod ) {
    this.parentComponent = parentComponent;
    this.props = props;
    this.transMeta = transMeta;
    this.lsMod = lsMod;
    this.aboveComposite = above;
  }

  public Composite layoutForm() {

    Label lbDestType = new Label( parentComponent, SWT.LEFT );
    props.setLook( lbDestType );
    lbDestType.setText( getString( PKG, "JmsDialog.DestinationType" ) );

    FormData fdDest = new FormData();
    fdDest.left = new FormAttachment( 0, 0 );
    fdDest.top = new FormAttachment( aboveComposite, 20 );
    fdDest.right = new FormAttachment( 100, 0 );
    lbDestType.setLayoutData( fdDest );

    destinationType = new ComboVar( transMeta, parentComponent, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( destinationType );
    destinationType.addModifyListener( lsMod );
    FormData fdDestType = new FormData();
    fdDestType.left = new FormAttachment( 0, 0 );
    fdDestType.top = new FormAttachment( lbDestType, 10 );
    //fdDestType.bottom = new FormAttachment( 100, 0 );
    fdDestType.width = 135;
    destinationType.setLayoutData( fdDestType );
    destinationType.add( getString( PKG, "JmsDialog.Dest.Topic" ) );
    destinationType.add( getString( PKG, "JmsDialog.Dest.Queue" ) );

    //JmsDialog.Dest.Name

    return destinationType.getCComboWidget();


  }

}
