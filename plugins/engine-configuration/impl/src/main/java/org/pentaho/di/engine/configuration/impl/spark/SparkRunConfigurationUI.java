/*
 * *****************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2017-2020 by Hitachi Vantara : http://www.pentaho.com
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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.engine.configuration.api.RunConfigurationDialog;
import org.pentaho.di.engine.configuration.api.RunConfigurationUI;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.widget.ComboVar;
import org.pentaho.di.ui.core.widget.TextVar;

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

    GridData protocolLabelData = new GridData( SWT.NONE, SWT.FILL, false, false );
    GridData urlLabelData = new GridData( SWT.FILL, SWT.FILL, true, false );

    GridLayout gridLayout = new GridLayout( 2, false );
    runConfigurationDialog.getGroup().setLayout( gridLayout );

    Label schemaLabel = new Label( runConfigurationDialog.getGroup(), SWT.LEFT );
    props.setLook( schemaLabel );
    schemaLabel.setText( BaseMessages.getString( PKG, "SparkRunConfigurationDialog.Label.Schema" ) );
    schemaLabel.setLayoutData( protocolLabelData );

    Label optionLabel = new Label( runConfigurationDialog.getGroup(), SWT.LEFT );
    props.setLook( optionLabel );
    optionLabel.setText( BaseMessages.getString( PKG, "SparkRunConfigurationDialog.Label.URL" ) );
    optionLabel.setLayoutData( urlLabelData );

    VariableSpace variableSpace = new Variables();
    variableSpace.initializeVariablesFrom( null );

    ComboVar schemaCombo =
            new ComboVar( variableSpace, runConfigurationDialog.getGroup(), SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER );
    schemaCombo.setEditable( true );
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

    GridData protocolData = new GridData( SWT.NONE, SWT.FILL, false, false );
    schemaCombo.setLayoutData( protocolData );

    TextVar urlText =
            new TextVar( variableSpace, runConfigurationDialog.getGroup(), SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( urlText );
    urlText.setText( sparkRunConfiguration.getUrl() );

    GridData urlData = new GridData( SWT.FILL, SWT.FILL, true, false );
    urlText.setLayoutData( urlData );

    urlText.addModifyListener(
            modifyEvent -> {
              sparkRunConfiguration.setUrl( urlText.getText() );
              Button okButton = runConfigurationDialog.getOKButton();
              okButton.setEnabled( !Utils.isEmpty( sparkRunConfiguration.getUrl() ) );
            } );
  }

}
