/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.ui.trans.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPointHandler;
import org.pentaho.di.core.extension.KettleExtensionPoint;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.dialog.ConfigurationDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.spoon.Spoon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TransExecutionConfigurationDialog extends ConfigurationDialog {
  private static Class<?> PKG = TransExecutionConfigurationDialog.class; // for i18n purposes, needed by Translator2!!

  private CCombo wRunConfiguration;

  public TransExecutionConfigurationDialog( Shell parent, TransExecutionConfiguration configuration,
    TransMeta transMeta ) {
    super( parent, configuration, transMeta );
  }

  protected void serverOptionsComposite( Class<?> PKG, String prefix ) {

  }

  protected void optionsSectionControls() {
    wClearLog = new Button( gDetails, SWT.CHECK );
    wClearLog.setText( BaseMessages.getString( PKG, "TransExecutionConfigurationDialog.ClearLog.Label" ) );
    wClearLog.setToolTipText( BaseMessages.getString( PKG, "TransExecutionConfigurationDialog.ClearLog.Tooltip" ) );
    props.setLook( wClearLog );
    FormData fdClearLog = new FormData();
    fdClearLog.top = new FormAttachment( 0, 10 );
    fdClearLog.left = new FormAttachment( 0, 10 );
    wClearLog.setLayoutData( fdClearLog );

    wSafeMode = new Button( gDetails, SWT.CHECK );
    wSafeMode.setText( BaseMessages.getString( PKG, "TransExecutionConfigurationDialog.SafeMode.Label" ) );
    wSafeMode.setToolTipText( BaseMessages.getString( PKG, "TransExecutionConfigurationDialog.SafeMode.Tooltip" ) );
    props.setLook( wSafeMode );
    FormData fdSafeMode = new FormData();
    fdSafeMode.top = new FormAttachment( wClearLog, 7 );
    fdSafeMode.left = new FormAttachment( 0, 10 );
    wSafeMode.setLayoutData( fdSafeMode );

    wGatherMetrics = new Button( gDetails, SWT.CHECK );
    wGatherMetrics.setText( BaseMessages.getString( PKG, "TransExecutionConfigurationDialog.GatherMetrics.Label" ) );
    wGatherMetrics.setToolTipText( BaseMessages.getString( PKG,
        "TransExecutionConfigurationDialog.GatherMetrics.Tooltip" ) );
    props.setLook( wGatherMetrics );
    FormData fdGatherMetrics = new FormData();
    fdGatherMetrics.top = new FormAttachment( wSafeMode, 7 );
    fdGatherMetrics.left = new FormAttachment( 0, 10 );
    fdGatherMetrics.bottom = new FormAttachment( 100, -10 );
    wGatherMetrics.setLayoutData( fdGatherMetrics );

    wlLogLevel = new Label( gDetails, SWT.NONE );
    props.setLook( wlLogLevel );
    wlLogLevel.setText( BaseMessages.getString( PKG, "TransExecutionConfigurationDialog.LogLevel.Label" ) );
    wlLogLevel.setToolTipText( BaseMessages.getString( PKG, "TransExecutionConfigurationDialog.LogLevel.Tooltip" ) );
    FormData fdlLogLevel = new FormData();
    fdlLogLevel.top = new FormAttachment( 0, 10 );
    fdlLogLevel.left = new FormAttachment( 45, 0 );
    wlLogLevel.setLayoutData( fdlLogLevel );

    wLogLevel = new CCombo( gDetails, SWT.READ_ONLY | SWT.BORDER );
    wLogLevel.setToolTipText( BaseMessages.getString( PKG, "TransExecutionConfigurationDialog.LogLevel.Tooltip" ) );
    props.setLook( wLogLevel );
    FormData fdLogLevel = new FormData();
    fdLogLevel.top = new FormAttachment( wlLogLevel, -2, SWT.TOP );
    fdLogLevel.width = 180;
    fdLogLevel.left = new FormAttachment( wlLogLevel, 6 );
    wLogLevel.setLayoutData( fdLogLevel );
    wLogLevel.setItems( LogLevel.getLogLevelDescriptions() );
  }

  public boolean open() {

    mainLayout( PKG, "TransExecutionConfigurationDialog", GUIResource.getInstance().getImageTransGraph() );

    Composite cRunConfiguration = new Composite( shell, SWT.NONE );
    cRunConfiguration.setLayout( new FormLayout() );
    props.setLook( cRunConfiguration );
    FormData fdLocal = new FormData();
    fdLocal.top = new FormAttachment( 0, 15 );
    fdLocal.right = new FormAttachment( 100, -15 );
    fdLocal.left = new FormAttachment( 0, 15 );

    cRunConfiguration.setBackground( shell.getBackground() ); // the default looks ugly
    cRunConfiguration.setLayoutData( fdLocal );

    Label wlRunConfiguration = new Label( cRunConfiguration, SWT.LEFT );
    props.setLook( wlRunConfiguration );
    wlRunConfiguration.setText( "Run configuration:" );
    FormData fdlRunConfiguration = new FormData();
    fdlRunConfiguration.top = new FormAttachment( 0 );
    fdlRunConfiguration.left = new FormAttachment( 0 );
    wlRunConfiguration.setLayoutData( fdlRunConfiguration );

    wRunConfiguration = new CCombo( cRunConfiguration, SWT.BORDER );
    props.setLook( wRunConfiguration );
    FormData fdRunConfiguration = new FormData();
    fdRunConfiguration.width = 200;
    fdRunConfiguration.top = new FormAttachment( wlRunConfiguration, 5 );
    fdRunConfiguration.left = new FormAttachment( 0 );
    wRunConfiguration.setLayoutData( fdRunConfiguration );

    optionsSectionLayout( PKG, "TransExecutionConfigurationDialog" );
    parametersSectionLayout( PKG, "TransExecutionConfigurationDialog" );

    fdDetails.top = new FormAttachment( cRunConfiguration, 15 );

    String docUrl =
        Const.getDocUrl( BaseMessages.getString( Spoon.class, "Spoon.TransExecutionConfigurationDialog.Help" ) );
    String docTitle = BaseMessages.getString( PKG, "TransExecutionConfigurationDialog.docTitle" );
    String docHeader = BaseMessages.getString( PKG, "TransExecutionConfigurationDialog.docHeader" );
    buttonsSectionLayout( PKG, "TransExecutionConfigurationDialog", docTitle, docUrl, docHeader );

    getData();
    openDialog();
    return retval;
  }

  private void getVariablesData() {
    wVariables.clearAll( false );
    List<String> variableNames = new ArrayList<String>( configuration.getVariables().keySet() );
    Collections.sort( variableNames );

    for ( int i = 0; i < variableNames.size(); i++ ) {
      String variableName = variableNames.get( i );
      String variableValue = configuration.getVariables().get( variableName );

      if ( Const.indexOfString( variableName, abstractMeta.listParameters() ) < 0 ) {

        TableItem tableItem = new TableItem( wVariables.table, SWT.NONE );
        tableItem.setText( 1, variableName );
        tableItem.setText( 2, Const.NVL( variableValue, "" ) );
      }
    }
    wVariables.removeEmptyRows();
    wVariables.setRowNums();
    wVariables.optWidth( true );
  }

  public void getData() {
    wSafeMode.setSelection( configuration.isSafeModeEnabled() );
    wClearLog.setSelection( configuration.isClearingLog() );
    wGatherMetrics.setSelection( configuration.isGatheringMetrics() );

    List<String> runConfigurations = new ArrayList<>();
    try {
      ExtensionPointHandler
        .callExtensionPoint( Spoon.getInstance().getLog(), KettleExtensionPoint.SpoonRunConfiguration.id,
          runConfigurations );
    } catch ( KettleException e ) {
      // Ignore errors
    }

    wRunConfiguration.setItems( runConfigurations.toArray( new String[ 0 ] ) );
    if ( !runConfigurations.contains( getConfiguration().getRunConfiguration() ) ) {
      getConfiguration().setRunConfiguration( null );
    }
    if ( Utils.isEmpty( getConfiguration().getRunConfiguration() ) ) {
      wRunConfiguration.select( 0 );
    } else {
      wRunConfiguration.setText( getConfiguration().getRunConfiguration() );
    }

    wLogLevel.select( configuration.getLogLevel().getLevel() );
    getParamsData();
    getVariablesData();
  }

  public void getInfo() {
    try {
      configuration.setReplayDate( null ); // removed from new execution dialog.
      getConfiguration().setRunConfiguration( wRunConfiguration.getText() );

      configuration.setSafeModeEnabled( wSafeMode.getSelection() );
      configuration.setClearingLog( wClearLog.getSelection() );
      configuration.setLogLevel( LogLevel.values()[wLogLevel.getSelectionIndex()] );
      configuration.setGatheringMetrics( wGatherMetrics.getSelection() );

      // The lower part of the dialog...
      getInfoParameters();
      getInfoVariables();
    } catch ( Exception e ) {
      new ErrorDialog( shell, "Error in settings", "There is an error in the dialog settings", e );
    }
  }

  /**
   * @return the configuration
   */
  public TransExecutionConfiguration getConfiguration() {
    return (TransExecutionConfiguration) configuration;
  }
}
