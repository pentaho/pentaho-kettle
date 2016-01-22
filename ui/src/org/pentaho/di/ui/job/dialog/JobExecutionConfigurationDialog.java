/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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
import org.pentaho.di.core.logging.DefaultLogLevel;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobExecutionConfiguration;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.ui.core.dialog.ConfigurationDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;

public class JobExecutionConfigurationDialog extends ConfigurationDialog {
  private static Class<?> PKG = JobExecutionConfigurationDialog.class; // for i18n purposes, needed by Translator2!!

  //private Button wExpandRemote;
  //private CCombo wStartCopy;

  public JobExecutionConfigurationDialog( Shell parent, JobExecutionConfiguration configuration, JobMeta jobMeta ) {
    super( parent, configuration, jobMeta );
  }

  protected void localOptionsComposite( Class<?> PKG, String prefix ) {

    Label localDescriptionLabel = new Label( localOptionsComposite, SWT.NONE );
    props.setLook( localDescriptionLabel );
    localDescriptionLabel.setText( BaseMessages.getString( PKG, prefix + ".LocalHost.Label" ) );
    FormData fd_localDescriptionLabel = new FormData();
    fd_localDescriptionLabel.left = new FormAttachment( environmentSeparator, 5 );
    fd_localDescriptionLabel.top = new FormAttachment( 0, 12 );
    localDescriptionLabel.setLayoutData( fd_localDescriptionLabel );
  }

  protected void serverOptionsComposite( Class<?> PKG, String prefix ) {

    wlRemoteHost = new Label( serverOptionsComposite, SWT.NONE );
    props.setLook( wlRemoteHost );
    wlRemoteHost.setText( BaseMessages.getString( PKG, prefix + ".RemoteHost.Label" ) );
    wlRemoteHost.setToolTipText( BaseMessages.getString( PKG, prefix + ".RemoteHost.Tooltip" ) );
    FormData fdlRemoteHost = new FormData();
    fdlRemoteHost.top = new FormAttachment( 0, 12 );
    fdlRemoteHost.left = new FormAttachment( environmentSeparator, 5 );
    wlRemoteHost.setLayoutData( fdlRemoteHost );

    wRemoteHost = new CCombo( serverOptionsComposite, SWT.BORDER );
    wRemoteHost.setToolTipText( BaseMessages.getString( PKG, prefix + ".RemoteHost.Tooltip" ) );
    props.setLook( wRemoteHost );
    FormData fdRemoteHost = new FormData();
    fdRemoteHost.left = new FormAttachment( wlRemoteHost, 0, SWT.LEFT );
    fdRemoteHost.right = new FormAttachment( 100, -293 );
    fdRemoteHost.top = new FormAttachment( wlRemoteHost, 10 );
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
    fdPassExport.top = new FormAttachment( wRemoteHost, 10 );
    wPassExport.setLayoutData( fdPassExport );

  }

  protected void optionsSectionControls() {

    wSafeMode = new Button( gDetails, SWT.CHECK );
    wSafeMode.setText( BaseMessages.getString( PKG, "JobExecutionConfigurationDialog.SafeMode.Label" ) );
    wSafeMode.setToolTipText( BaseMessages.getString( PKG, "JobExecutionConfigurationDialog.SafeMode.Tooltip" ) );
    props.setLook( wSafeMode );
    FormData fdSafeMode = new FormData();
    fdSafeMode.right = new FormAttachment( 0, 186 );
    fdSafeMode.top = new FormAttachment( 0, 40 );
    fdSafeMode.left = new FormAttachment( 0, 10 );
    wSafeMode.setLayoutData( fdSafeMode );

    wGatherMetrics = new Button( gDetails, SWT.CHECK );
    wGatherMetrics.setText( BaseMessages.getString( PKG, "JobExecutionConfigurationDialog.GatherMetrics.Label" ) );
    wGatherMetrics.setToolTipText( BaseMessages.getString( PKG,
        "JobExecutionConfigurationDialog.GatherMetrics.Tooltip" ) );
    props.setLook( wGatherMetrics );
    FormData fdGatherMetrics = new FormData();
    fdGatherMetrics.right = new FormAttachment( 0, 186 );
    fdGatherMetrics.top = new FormAttachment( 0, 65 );
    fdGatherMetrics.left = new FormAttachment( 0, 10 );
    wGatherMetrics.setLayoutData( fdGatherMetrics );

    wClearLog = new Button( gDetails, SWT.CHECK );
    wClearLog.setText( BaseMessages.getString( PKG, "JobExecutionConfigurationDialog.ClearLog.Label" ) );
    wClearLog.setToolTipText( BaseMessages.getString( PKG, "JobExecutionConfigurationDialog.ClearLog.Tooltip" ) );
    props.setLook( wClearLog );
    FormData fdClearLog = new FormData();
    fdClearLog.right = new FormAttachment( 0, 171 );
    fdClearLog.top = new FormAttachment( 0, 15 );
    fdClearLog.left = new FormAttachment( 0, 10 );
    wClearLog.setLayoutData( fdClearLog );

    wlLogLevel = new Label( gDetails, SWT.RIGHT );
    props.setLook( wlLogLevel );
    wlLogLevel.setText( BaseMessages.getString( PKG, "JobExecutionConfigurationDialog.LogLevel.Label" ) );
    wlLogLevel.setToolTipText( BaseMessages.getString( PKG, "JobExecutionConfigurationDialog.LogLevel.Tooltip" ) );
    FormData fdlLogLevel = new FormData();
    fdlLogLevel.right = new FormAttachment( 0, 290 );
    fdlLogLevel.top = new FormAttachment( 0, 17 );
    fdlLogLevel.left = new FormAttachment( 0, 240 );
    wlLogLevel.setLayoutData( fdlLogLevel );

    wLogLevel = new CCombo( gDetails, SWT.READ_ONLY | SWT.BORDER );
    wLogLevel.setToolTipText( BaseMessages.getString( PKG, "JobExecutionConfigurationDialog.LogLevel.Tooltip" ) );
    props.setLook( wLogLevel );
    FormData fdLogLevel = new FormData();
    fdLogLevel.left = new FormAttachment( wlLogLevel, 6 );
    fdLogLevel.width = 200;
    fdLogLevel.top = new FormAttachment( wClearLog, -2, SWT.TOP );
    fdLogLevel.right = new FormAttachment( 0, 446 );
    wLogLevel.setLayoutData( fdLogLevel );
    wLogLevel.setItems( LogLevel.getLogLevelDescriptions() );
  }

  public boolean open() {

    mainLayout( PKG, "JobExecutionConfigurationDialog" );

    environmentTypeSectionLayout( PKG, "JobExecutionConfigurationDialog" );
    optionsSectionLayout( PKG, "JobExecutionConfigurationDialog" );
    parametersSectionLayout( PKG, "JobExecutionConfigurationDialog" );
    buttonsSectionLayout( PKG, "JobExecutionConfigurationDialog" );

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
    wExecRemote.setSelection( configuration.isExecutingRemotely() );
    wSafeMode.setSelection( configuration.isSafeModeEnabled() );
    wClearLog.setSelection( configuration.isClearingLog() );
    wRemoteHost.setText( configuration.getRemoteServer() == null ? "" : configuration.getRemoteServer().toString() );
    wPassExport.setSelection( configuration.isPassingExport() );
    //wExpandRemote.setSelection( getConfiguration().isExpandingRemoteJob() );
    wLogLevel.select( DefaultLogLevel.getLogLevel().getLevel() );
    wGatherMetrics.setSelection( configuration.isGatheringMetrics() );

    String startCopy = "";
    if ( !Const.isEmpty( getConfiguration().getStartCopyName() ) ) {
      JobEntryCopy copy =
          ( (JobMeta) abstractMeta ).findJobEntry( getConfiguration().getStartCopyName(), getConfiguration()
              .getStartCopyNr(), false );
      if ( copy != null ) {
        startCopy = getJobEntryCopyName( copy );
      }
    }
    //wStartCopy.setText( startCopy );

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
      //getConfiguration().setExpandingRemoteJob( wExpandRemote.getSelection() );

      // various settings
      //
      configuration.setReplayDate( null );
      configuration.setSafeModeEnabled( wSafeMode.getSelection() );
      configuration.setClearingLog( wClearLog.getSelection() );
      configuration.setLogLevel( LogLevel.values()[wLogLevel.getSelectionIndex()] );

      /*String startCopyName = null;
      int startCopyNr = 0;
      if ( !Const.isEmpty( wStartCopy.getText() ) ) {
        if ( wStartCopy.getSelectionIndex() >= 0 ) {
          JobEntryCopy copy = ( (JobMeta) abstractMeta ).getJobCopies().get( wStartCopy.getSelectionIndex() );
          startCopyName = copy.getName();
          startCopyNr = copy.getNr();
        }
      }
      getConfiguration().setStartCopyName( startCopyName );
      getConfiguration().setStartCopyNr( startCopyNr );*/

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
