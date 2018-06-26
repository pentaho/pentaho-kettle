/*
 * *****************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.engine.configuration.impl.dataflow;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.engine.configuration.api.RunConfigurationDialog;
import org.pentaho.di.engine.configuration.api.RunConfigurationUI;
import org.pentaho.di.ui.core.PropsUI;

/**
 * Created by ccaspanello on 6/13/18.
 */
public class DataflowRunConfigurationUI implements RunConfigurationUI {

  private static Class<?> PKG = DataflowRunConfigurationUI.class;
  private PropsUI props = PropsUI.getInstance();

  private DataflowRunConfiguration dataflowRunConfiguration;

  public DataflowRunConfigurationUI( DataflowRunConfiguration dataflowRunConfiguration ) {
    this.dataflowRunConfiguration = dataflowRunConfiguration;
  }

  @Override
  public void attach( RunConfigurationDialog runConfigurationDialog ) {

    GridData protocolLabelData = new GridData( SWT.NONE, SWT.FILL, false, false );
    GridData urlLabelData = new GridData( SWT.FILL, SWT.FILL, true, false );

    GridLayout gridLayout = new GridLayout( 2, false );
    runConfigurationDialog.getGroup().setLayout( gridLayout );

    Label lblApplication = new Label( runConfigurationDialog.getGroup(), SWT.LEFT );
    props.setLook( lblApplication );
    lblApplication.setText( "Application JAR" );
    lblApplication.setLayoutData( protocolLabelData );

    Label lblRunner = new Label( runConfigurationDialog.getGroup(), SWT.LEFT );
    props.setLook( lblRunner );
    lblRunner.setText( "Runner" );
    lblRunner.setLayoutData( urlLabelData );

    Text txtApplication = new Text( runConfigurationDialog.getGroup(), SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( txtApplication );

    GridData gridApplication = new GridData( SWT.FILL, SWT.FILL, true, false );
    gridApplication.heightHint = 16;
    txtApplication.setText( dataflowRunConfiguration.getApplicationJar() );
    txtApplication.setLayoutData( gridApplication );
    txtApplication.addModifyListener( new ModifyListener() {
      @Override public void modifyText( ModifyEvent modifyEvent ) {
        dataflowRunConfiguration.setApplicationJar( txtApplication.getText() );
      }
    } );

    Text txtRunner = new Text( runConfigurationDialog.getGroup(), SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( txtRunner );

    GridData gridRunner = new GridData( SWT.FILL, SWT.FILL, true, false );
    gridRunner.heightHint = 16;
    txtRunner.setText( dataflowRunConfiguration.getRunner() );
    txtRunner.setLayoutData( gridRunner );
    txtRunner.addModifyListener( new ModifyListener() {
      @Override public void modifyText( ModifyEvent modifyEvent ) {
        dataflowRunConfiguration.setRunner( txtRunner.getText() );
      }
    } );
  }

}
