/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.ui.trans.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
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
    runConfigurationSectionLayout( PKG, "TransExecutionConfigurationDialog" );
    optionsSectionLayout( PKG, "TransExecutionConfigurationDialog" );
    parametersSectionLayout( PKG, "TransExecutionConfigurationDialog" );

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
          new Object[] { runConfigurations, TransMeta.XML_TAG } );
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
