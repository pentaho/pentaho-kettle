/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.job.dialog;

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
import org.pentaho.di.core.logging.DefaultLogLevel;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobExecutionConfiguration;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.ui.core.dialog.ConfigurationDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.spoon.Spoon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class JobExecutionConfigurationDialog extends ConfigurationDialog {
  private static Class<?> PKG = JobExecutionConfigurationDialog.class; // for i18n purposes, needed by Translator2!!

  private Button wExpandRemote;
  private CCombo wStartCopy;

  public JobExecutionConfigurationDialog( Shell parent, JobExecutionConfiguration configuration, JobMeta jobMeta ) {
    super( parent, configuration, jobMeta );
  }

  protected void optionsSectionControls() {

    wExpandRemote = new Button( gDetails, SWT.CHECK );
    wExpandRemote.setText( BaseMessages.getString( PKG, "JobExecutionConfigurationDialog.ExpandRemote.Label" ) );
    wExpandRemote
      .setToolTipText( BaseMessages.getString( PKG, "JobExecutionConfigurationDialog.ExpandRemote.Tooltip" ) );
    props.setLook( wExpandRemote );
    FormData fd_expandCheckButton = new FormData();
    fd_expandCheckButton.top = new FormAttachment( 0, 10 );
    fd_expandCheckButton.left = new FormAttachment( 0, 10 );
    wExpandRemote.setLayoutData( fd_expandCheckButton );
    addRunConfigurationListenerForExpandRemoteOption();

    wClearLog = new Button( gDetails, SWT.CHECK );
    wClearLog.setText( BaseMessages.getString( PKG, "JobExecutionConfigurationDialog.ClearLog.Label" ) );
    wClearLog.setToolTipText( BaseMessages.getString( PKG, "JobExecutionConfigurationDialog.ClearLog.Tooltip" ) );
    props.setLook( wClearLog );
    FormData fdClearLog = new FormData();
    fdClearLog.top = new FormAttachment( wExpandRemote, 10 );
    fdClearLog.left = new FormAttachment( 0, 10 );
    wClearLog.setLayoutData( fdClearLog );

    wSafeMode = new Button( gDetails, SWT.CHECK );
    wSafeMode.setText( BaseMessages.getString( PKG, "JobExecutionConfigurationDialog.SafeMode.Label" ) );
    wSafeMode.setToolTipText( BaseMessages.getString( PKG, "JobExecutionConfigurationDialog.SafeMode.Tooltip" ) );
    props.setLook( wSafeMode );
    FormData fdSafeMode = new FormData();
    fdSafeMode.top = new FormAttachment( wClearLog, 7 );
    fdSafeMode.left = new FormAttachment( 0, 10 );
    wSafeMode.setLayoutData( fdSafeMode );

    wGatherMetrics = new Button( gDetails, SWT.CHECK );
    wGatherMetrics.setText( BaseMessages.getString( PKG, "JobExecutionConfigurationDialog.GatherMetrics.Label" ) );
    wGatherMetrics.setToolTipText( BaseMessages
        .getString( PKG, "JobExecutionConfigurationDialog.GatherMetrics.Tooltip" ) );
    props.setLook( wGatherMetrics );
    FormData fdGatherMetrics = new FormData();
    fdGatherMetrics.top = new FormAttachment( wSafeMode, 7 );
    fdGatherMetrics.left = new FormAttachment( 0, 10 );
    fdGatherMetrics.bottom = new FormAttachment( 100, -10 );
    wGatherMetrics.setLayoutData( fdGatherMetrics );

    wlLogLevel = new Label( gDetails, SWT.RIGHT );
    wlLogLevel.setText( BaseMessages.getString( PKG, "JobExecutionConfigurationDialog.LogLevel.Label" ) );
    wlLogLevel.setToolTipText( BaseMessages.getString( PKG, "JobExecutionConfigurationDialog.LogLevel.Tooltip" ) );
    props.setLook( wlLogLevel );
    FormData fdlLogLevel = new FormData();
    fdlLogLevel.top = new FormAttachment( 0, 10 );
    fdlLogLevel.left = new FormAttachment( 45, 0 );
    wlLogLevel.setLayoutData( fdlLogLevel );

    wLogLevel = new CCombo( gDetails, SWT.READ_ONLY | SWT.BORDER );
    wLogLevel.setToolTipText( BaseMessages.getString( PKG, "JobExecutionConfigurationDialog.LogLevel.Tooltip" ) );
    props.setLook( wLogLevel );
    FormData fdLogLevel = new FormData();
    fdLogLevel.top = new FormAttachment( wlLogLevel, -2, SWT.TOP );
    fdLogLevel.width = 180;
    fdLogLevel.left = new FormAttachment( wlLogLevel, 6 );
    wLogLevel.setLayoutData( fdLogLevel );
    wLogLevel.setItems( LogLevel.getLogLevelDescriptions() );

    Label lblStartJob = new Label( gDetails, SWT.RIGHT );
    lblStartJob.setText( BaseMessages.getString( PKG, "JobExecutionConfigurationDialog.StartCopy.Label" ) );
    lblStartJob.setToolTipText( BaseMessages.getString( PKG, "JobExecutionConfigurationDialog.StartCopy.Tooltip" ) );
    props.setLook( lblStartJob );
    FormData fd_lblStartJob = new FormData();
    fd_lblStartJob.top = new FormAttachment( wlLogLevel, 18 );
    fd_lblStartJob.right = new FormAttachment( wlLogLevel, 0, SWT.RIGHT );
    lblStartJob.setLayoutData( fd_lblStartJob );

    wStartCopy = new CCombo( gDetails, SWT.READ_ONLY | SWT.BORDER );
    wStartCopy.setToolTipText( BaseMessages.getString( PKG, "JobExecutionConfigurationDialog.StartCopy.Tooltip" ) );
    props.setLook( wStartCopy );
    FormData fd_startJobCombo = new FormData();
    fd_startJobCombo.top = new FormAttachment( lblStartJob, -2, SWT.TOP );
    fd_startJobCombo.width = 180;
    fd_startJobCombo.left = new FormAttachment( lblStartJob, 6 );
    wStartCopy.setLayoutData( fd_startJobCombo );

    JobMeta jobMeta = (JobMeta) super.abstractMeta;

    String[] names = new String[jobMeta.getJobCopies().size()];
    for ( int i = 0; i < names.length; i++ ) {
      JobEntryCopy copy = jobMeta.getJobCopies().get( i );
      names[i] = getJobEntryCopyName( copy );
    }
    wStartCopy.setItems( names );
  }

  public boolean open() {

    mainLayout( PKG, "JobExecutionConfigurationDialog", GUIResource.getInstance().getImageJobGraph() );
    runConfigurationSectionLayout( PKG, "TransExecutionConfigurationDialog" );
    optionsSectionLayout( PKG, "JobExecutionConfigurationDialog" );
    parametersSectionLayout( PKG, "JobExecutionConfigurationDialog" );

    String docUrl =
        Const.getDocUrl( BaseMessages.getString( Spoon.class, "Spoon.JobExecutionConfigurationDialog.Help" ) );
    String docTitle = BaseMessages.getString( PKG, "JobExecutionConfigurationDialog.docTitle" );
    String docHeader = BaseMessages.getString( PKG, "JobExecutionConfigurationDialog.docHeader" );
    buttonsSectionLayout( PKG, "JobExecutionConfigurationDialog", docTitle, docUrl, docHeader );

    getData();
    openDialog();
    return retval;
  }

  private String getJobEntryCopyName( JobEntryCopy copy ) {
    return copy.getName() + ( copy.getNr() > 0 ? copy.getNr() : "" );
  }

  private void getVariablesData() {
    wVariables.clearAll( false );
    List<String> variableNames = new ArrayList<String>( configuration.getVariables().keySet() );
    Collections.sort( variableNames );

    List<String> paramNames = new ArrayList<String>( configuration.getParams().keySet() );

    for ( int i = 0; i < variableNames.size(); i++ ) {
      String variableName = variableNames.get( i );
      String variableValue = configuration.getVariables().get( variableName );

      if ( !paramNames.contains( variableName ) ) {
        //
        // Do not put the parameters among the variables.
        //
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
    wExpandRemote.setSelection( getConfiguration().isExpandingRemoteJob() );
    wLogLevel.select( DefaultLogLevel.getLogLevel().getLevel() );
    wGatherMetrics.setSelection( configuration.isGatheringMetrics() );

    List<String> runConfigurations = new ArrayList<>();
    try {
      ExtensionPointHandler
        .callExtensionPoint( Spoon.getInstance().getLog(), KettleExtensionPoint.SpoonRunConfiguration.id,
          new Object[] { runConfigurations, JobMeta.XML_TAG } );
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

    wExpandRemote.setEnabled( getConfiguration().isExecutingRemotely() );

    String startCopy = "";
    if ( !Utils.isEmpty( getConfiguration().getStartCopyName() ) ) {
      JobEntryCopy copy =
          ( (JobMeta) abstractMeta ).findJobEntry( getConfiguration().getStartCopyName(), getConfiguration()
              .getStartCopyNr(), false );
      if ( copy != null ) {
        startCopy = getJobEntryCopyName( copy );
      }
    }
    wStartCopy.setText( startCopy );

    getParamsData();
    getVariablesData();
  }

  public void getInfo() {
    try {
      getConfiguration().setExpandingRemoteJob( wExpandRemote.getSelection() );
      getConfiguration().setRunConfiguration( wRunConfiguration.getText() );

      // various settings
      //
      configuration.setReplayDate( null );
      configuration.setSafeModeEnabled( wSafeMode.getSelection() );
      configuration.setClearingLog( wClearLog.getSelection() );
      configuration.setLogLevel( LogLevel.values()[wLogLevel.getSelectionIndex()] );

      String startCopyName = null;
      int startCopyNr = 0;
      if ( !Utils.isEmpty( wStartCopy.getText() ) ) {
        if ( wStartCopy.getSelectionIndex() >= 0 ) {
          JobEntryCopy copy = ( (JobMeta) abstractMeta ).getJobCopies().get( wStartCopy.getSelectionIndex() );
          startCopyName = copy.getName();
          startCopyNr = copy.getNr();
        }
      }
      getConfiguration().setStartCopyName( startCopyName );
      getConfiguration().setStartCopyNr( startCopyNr );

      // The lower part of the dialog...
      getInfoParameters();
      getInfoVariables();

      // Metrics
      configuration.setGatheringMetrics( wGatherMetrics.getSelection() );
    } catch ( Exception e ) {
      new ErrorDialog( shell, "Error in settings", "There is an error in the dialog settings", e );
    }
  }

  /**
   * @return the configuration
   */
  public JobExecutionConfiguration getConfiguration() {
    return (JobExecutionConfiguration) configuration;
  }

  public void addRunConfigurationListenerForExpandRemoteOption() {
    wRunConfiguration.addModifyListener( modifyEvent -> {
      List<Object> items = Arrays.asList( wRunConfiguration.getText(), true );
      try {
        ExtensionPointHandler.callExtensionPoint( Spoon.getInstance().getLog(), KettleExtensionPoint
          .RunConfigurationIsRemote.id, items );
      } catch ( KettleException ignored ) {
        // Ignore errors - keep old behavior - expand remote job always enabled
      }
      Boolean isRemote = (Boolean) items.get( 1 );
      getConfiguration().setRunConfiguration( wRunConfiguration.getText() );
      getConfiguration().setExecutingRemotely( isRemote );
      getConfiguration().setExecutingLocally( !isRemote );
      wExpandRemote.setEnabled( isRemote );
      wExpandRemote.setSelection( wExpandRemote.isEnabled() && wExpandRemote.getSelection() );
    } );
  }
}
