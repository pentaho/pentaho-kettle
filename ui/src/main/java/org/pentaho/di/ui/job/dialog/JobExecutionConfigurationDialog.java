/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.logging.DefaultLogLevel;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobExecutionConfiguration;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.ui.core.dialog.ConfigurationDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.spoon.Spoon;

public class JobExecutionConfigurationDialog extends ConfigurationDialog {
  private static Class<?> PKG = JobExecutionConfigurationDialog.class; // for i18n purposes, needed by Translator2!!

  private Button wExpandRemote;
  private CCombo wStartCopy;

  public JobExecutionConfigurationDialog( Shell parent, JobExecutionConfiguration configuration, JobMeta jobMeta ) {
    super( parent, configuration, jobMeta );
  }

  protected void serverOptionsComposite( Class<?> PKG, String prefix ) {
    wlRemoteHost = new Label( serverOptionsComposite, SWT.NONE );
    props.setLook( wlRemoteHost );
    wlRemoteHost.setText( BaseMessages.getString( PKG, prefix + ".RemoteHost.Label" ) );
    wlRemoteHost.setToolTipText( BaseMessages.getString( PKG, prefix + ".RemoteHost.Tooltip" ) );
    FormData fdlRemoteHost = new FormData();
    fdlRemoteHost.top = new FormAttachment( 0, 10 );
    fdlRemoteHost.left = new FormAttachment( environmentSeparator, 5 );
    wlRemoteHost.setLayoutData( fdlRemoteHost );

    wRemoteHost = new CCombo( serverOptionsComposite, SWT.BORDER );
    wRemoteHost.setToolTipText( BaseMessages.getString( PKG, prefix + ".RemoteHost.Tooltip" ) );
    props.setLook( wRemoteHost );
    FormData fdRemoteHost = new FormData();
    fdRemoteHost.left = new FormAttachment( wlRemoteHost, 0, SWT.LEFT );
    fdRemoteHost.width = 170;
    fdRemoteHost.top = new FormAttachment( wlRemoteHost, 8 );
    wRemoteHost.setLayoutData( fdRemoteHost );
    for ( int i = 0; i < abstractMeta.getSlaveServers().size(); i++ ) {
      SlaveServer slaveServer = abstractMeta.getSlaveServers().get( i );
      wRemoteHost.add( slaveServer.toString() );
    }

    wPassExport = new Button( serverOptionsComposite, SWT.CHECK );
    wPassExport.setText( BaseMessages.getString( PKG, prefix + ".PassExport.Label" ) );
    wPassExport.setToolTipText( BaseMessages.getString( PKG, prefix + ".PassExport.Tooltip" ) );
    props.setLook( wPassExport );
    FormData fdPassExport = new FormData();
    fdPassExport.left = new FormAttachment( wRemoteHost, 0, SWT.LEFT );
    fdPassExport.top = new FormAttachment( wRemoteHost, 8 );
    wPassExport.setLayoutData( fdPassExport );

    wExpandRemote = new Button( serverOptionsComposite, SWT.CHECK );
    wExpandRemote.setText( BaseMessages.getString( PKG, prefix + ".ExpandRemote.Label" ) );
    wExpandRemote
        .setToolTipText( BaseMessages.getString( PKG, "JobExecutionConfigurationDialog.ExpandRemote.Tooltip" ) );
    props.setLook( wExpandRemote );
    FormData fd_expandCheckButton = new FormData();
    fd_expandCheckButton.bottom = new FormAttachment( wPassExport, 0, SWT.BOTTOM );
    fd_expandCheckButton.left = new FormAttachment( wPassExport, 45 );
    wExpandRemote.setLayoutData( fd_expandCheckButton );
  }

  protected void optionsSectionControls() {
    wClearLog = new Button( gDetails, SWT.CHECK );
    wClearLog.setText( BaseMessages.getString( PKG, "JobExecutionConfigurationDialog.ClearLog.Label" ) );
    wClearLog.setToolTipText( BaseMessages.getString( PKG, "JobExecutionConfigurationDialog.ClearLog.Tooltip" ) );
    props.setLook( wClearLog );
    FormData fdClearLog = new FormData();
    fdClearLog.top = new FormAttachment( 0, 10 );
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

    environmentTypeSectionLayout( PKG, "JobExecutionConfigurationDialog" );
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
    wExecLocal.setSelection( configuration.isExecutingLocally() );
    if ( configuration.isExecutingLocally() ) {
      stackedLayout.topControl = localOptionsComposite;
    }
    wExecRemote.setSelection( configuration.isExecutingRemotely() );
    if ( configuration.isExecutingRemotely() ) {
      stackedLayout.topControl = serverOptionsComposite;
    }

    wSafeMode.setSelection( configuration.isSafeModeEnabled() );
    wClearLog.setSelection( configuration.isClearingLog() );
    wRemoteHost.setText( configuration.getRemoteServer() == null ? "" : configuration.getRemoteServer().toString() );
    wPassExport.setSelection( configuration.isPassingExport() );
    wExpandRemote.setSelection( getConfiguration().isExpandingRemoteJob() );
    wLogLevel.select( DefaultLogLevel.getLogLevel().getLevel() );
    wGatherMetrics.setSelection( configuration.isGatheringMetrics() );

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
      configuration.setExecutingLocally( wExecLocal.getSelection() );
      configuration.setExecutingRemotely( wExecRemote.getSelection() );

      // Remote data
      //
      if ( wExecRemote.getSelection() ) {
        String serverName = wRemoteHost.getText();
        configuration.setRemoteServer( abstractMeta.findSlaveServer( serverName ) );
      }
      configuration.setPassingExport( wPassExport.getSelection() );
      getConfiguration().setExpandingRemoteJob( wExpandRemote.getSelection() );

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
}
