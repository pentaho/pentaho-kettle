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
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.engine.configuration.api.RunConfigurationDialog;
import org.pentaho.di.engine.configuration.api.RunConfigurationUI;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;

import java.util.ArrayList;
import java.util.List;

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

    // Attach Schema Label & Combo
    List<String> schemas = new ArrayList<>();
    schemas.add( "http://" );
    schemas.add( "https://" );

    Label schemaLabel = new Label( runConfigurationDialog.getGroup(), SWT.LEFT );
    props.setLook( schemaLabel );
    schemaLabel.setText( BaseMessages.getString( PKG, "SparkRunConfigurationDialog.Label.Schema" ) );
    FormData schemaLabelForm = new FormData();
    schemaLabelForm.left = new FormAttachment( 0 );
    schemaLabelForm.top = new FormAttachment( 0 );
    schemaLabel.setLayoutData( schemaLabelForm );

    CCombo schemaCombo = new CCombo( runConfigurationDialog.getGroup(), SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER );
    for ( String schema : schemas ) {
      schemaCombo.add( schema );
    }
    schemaCombo.addSelectionListener( new SelectionAdapter() {
      @Override public void widgetSelected( SelectionEvent selectionEvent ) {
        String schema = schemaCombo.getText();
        sparkRunConfiguration.setSchema( schema );
      }
    } );

    int selected = schemas.indexOf( sparkRunConfiguration.getSchema() );
    schemaCombo.select( selected );
    props.setLook( schemaCombo );
    FormData schemaComboForm = new FormData();
    schemaComboForm.width = 65;
    schemaComboForm.left = new FormAttachment( 0, 0 );
    schemaComboForm.top = new FormAttachment( schemaLabel, 5 );
    schemaCombo.setLayoutData( schemaComboForm );

    // Attach URL Label & Combo
    Label optionLabel = new Label( runConfigurationDialog.getGroup(), SWT.LEFT );
    props.setLook( optionLabel );
    optionLabel.setText( BaseMessages.getString( PKG, "SparkRunConfigurationDialog.Label.URL" ) );
    FormData fdlOption = new FormData();
    fdlOption.left = new FormAttachment( schemaLabel, 25 );
    fdlOption.top = new FormAttachment( 0 );
    optionLabel.setLayoutData( fdlOption );

    Text optionText = new Text( runConfigurationDialog.getGroup(), SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( optionText );
    optionText.setText( sparkRunConfiguration.getUrl() );
    FormData fdOption = new FormData();
    fdOption.left = new FormAttachment( schemaCombo, 15 );
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
