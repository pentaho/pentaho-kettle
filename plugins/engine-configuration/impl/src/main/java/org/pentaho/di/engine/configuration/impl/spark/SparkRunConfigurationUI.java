/*
 * *****************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2017 by Pentaho : http://www.pentaho.com
 *
 *  *******************************************************************************
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 *  this file except in compliance with the License. You may obtain a copy of the
 *  License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * *****************************************************************************
 *
 */

package org.pentaho.di.engine.configuration.impl.spark;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.engine.configuration.api.RunConfigurationDialog;
import org.pentaho.di.engine.configuration.api.RunConfigurationUI;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;

/**
 * Created by bmorrise on 8/22/17.
 */
public class SparkRunConfigurationUI implements RunConfigurationUI {

  private static Class<?> PKG = SparkRunConfigurationUI.class;
  private PropsUI props = PropsUI.getInstance();

  private SparkRunConfiguration sparkRunConfiguration;

  public SparkRunConfigurationUI( SparkRunConfiguration sparkRunConfiguration ) {
    this.sparkRunConfiguration = sparkRunConfiguration;
  }

  @Override public void attach( RunConfigurationDialog runConfigurationDialog ) {
    Label optionLabel = new Label( runConfigurationDialog.getGroup(), SWT.LEFT );
    props.setLook( optionLabel );
    optionLabel.setText( BaseMessages.getString( PKG, "SparkRunConfigurationDialog.Label.URL" ) );
    FormData fdlOption = new FormData();
    fdlOption.left = new FormAttachment( 0 );
    fdlOption.top = new FormAttachment( 0 );
    optionLabel.setLayoutData( fdlOption );

    Text optionText = new Text( runConfigurationDialog.getGroup(), SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( optionText );
    optionText.setText( sparkRunConfiguration.getUrl() );
    FormData fdOption = new FormData();
    fdOption.left = new FormAttachment( 0 );
    fdOption.top = new FormAttachment( optionLabel, 5 );
    fdOption.right = new FormAttachment( 100 );
    optionText.setLayoutData( fdOption );

    optionText.addModifyListener( new ModifyListener() {
      @Override public void modifyText( ModifyEvent modifyEvent ) {
        sparkRunConfiguration.setUrl( optionText.getText() );
      }
    } );
  }
}
