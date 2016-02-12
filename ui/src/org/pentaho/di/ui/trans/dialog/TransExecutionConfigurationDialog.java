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

package org.pentaho.di.ui.trans.dialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.dialog.ConfigurationDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.spoon.Spoon;

public class TransExecutionConfigurationDialog extends ConfigurationDialog {
  private static Class<?> PKG = TransExecutionConfigurationDialog.class; // for i18n purposes, needed by Translator2!!

  private Button wExecCluster;
  private FormData fdExecCluster;
  private FormData fdExecClusterComposite;
  private Composite clusteredOptionsComposite;
  private Composite execClusterComposite;
  private Button showDialogRunCheckbox;

  public TransExecutionConfigurationDialog( Shell parent, TransExecutionConfiguration configuration,
      TransMeta transMeta ) {
    super( parent, configuration, transMeta );
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

  protected void clusteredOptionsComposite() {

    Label clusterDescriptionLabel = new Label( clusteredOptionsComposite, SWT.NONE );
    props.setLook( clusterDescriptionLabel );
    clusterDescriptionLabel.setText( BaseMessages.getString( PKG,
        "TransExecutionConfigurationDialog.ClusterDescription.Label" ) );
    FormData fd_clusterDescriptionLabel = new FormData();
    fd_clusterDescriptionLabel.top = new FormAttachment( 0, 12 );
    fd_clusterDescriptionLabel.left = new FormAttachment( environmentSeparator, 5 );
    clusterDescriptionLabel.setLayoutData( fd_clusterDescriptionLabel );

    showDialogRunCheckbox = new Button( clusteredOptionsComposite, SWT.CHECK );
    props.setLook( showDialogRunCheckbox );
    FormData fd_resroucesCheckBox = new FormData();
    fd_resroucesCheckBox.top = new FormAttachment( clusterDescriptionLabel, 10 );
    fd_resroucesCheckBox.left = new FormAttachment( clusterDescriptionLabel, 0, SWT.LEFT );
    showDialogRunCheckbox.setLayoutData( fd_resroucesCheckBox );
    showDialogRunCheckbox.setText( BaseMessages.getString( PKG,
        "TransExecutionConfigurationDialog.ShowTransformations.Label" ) );
  }

  protected void optionsSectionControls() {

    wSafeMode = new Button( gDetails, SWT.CHECK );
    wSafeMode.setText( BaseMessages.getString( PKG, "TransExecutionConfigurationDialog.SafeMode.Label" ) );
    wSafeMode.setToolTipText( BaseMessages.getString( PKG, "TransExecutionConfigurationDialog.SafeMode.Tooltip" ) );
    props.setLook( wSafeMode );
    FormData fdSafeMode = new FormData();
    fdSafeMode.right = new FormAttachment( 0, 186 );
    fdSafeMode.top = new FormAttachment( 0, 30 );
    fdSafeMode.left = new FormAttachment( 0, 10 );
    wSafeMode.setLayoutData( fdSafeMode );

    wGatherMetrics = new Button( gDetails, SWT.CHECK );
    wGatherMetrics.setText( BaseMessages.getString( PKG, "TransExecutionConfigurationDialog.GatherMetrics.Label" ) );
    wGatherMetrics.setToolTipText( BaseMessages.getString( PKG,
        "TransExecutionConfigurationDialog.GatherMetrics.Tooltip" ) );
    props.setLook( wGatherMetrics );
    FormData fdGatherMetrics = new FormData();
    fdGatherMetrics.right = new FormAttachment( 0, 230 );
    fdGatherMetrics.top = new FormAttachment( 0, 55 );
    fdGatherMetrics.left = new FormAttachment( 0, 10 );
    wGatherMetrics.setLayoutData( fdGatherMetrics );

    wClearLog = new Button( gDetails, SWT.CHECK );
    wClearLog.setText( BaseMessages.getString( PKG, "TransExecutionConfigurationDialog.ClearLog.Label" ) );
    wClearLog.setToolTipText( BaseMessages.getString( PKG, "TransExecutionConfigurationDialog.ClearLog.Tooltip" ) );
    props.setLook( wClearLog );
    FormData fdClearLog = new FormData();
    fdClearLog.right = new FormAttachment( 0, 200 );
    fdClearLog.top = new FormAttachment( 0, 5 );
    fdClearLog.left = new FormAttachment( 0, 10 );
    wClearLog.setLayoutData( fdClearLog );

    wlLogLevel = new Label( gDetails, SWT.RIGHT );
    props.setLook( wlLogLevel );
    wlLogLevel.setText( BaseMessages.getString( PKG, "TransExecutionConfigurationDialog.LogLevel.Label" ) );
    wlLogLevel.setToolTipText( BaseMessages.getString( PKG, "TransExecutionConfigurationDialog.LogLevel.Tooltip" ) );
    FormData fdlLogLevel = new FormData();
    fdlLogLevel.right = new FormAttachment( 0, 333 );
    fdlLogLevel.top = new FormAttachment( 0, 7 );
    fdlLogLevel.left = new FormAttachment( 0, 260 );
    wlLogLevel.setLayoutData( fdlLogLevel );

    wLogLevel = new CCombo( gDetails, SWT.READ_ONLY | SWT.BORDER );
    wLogLevel.setToolTipText( BaseMessages.getString( PKG, "TransExecutionConfigurationDialog.LogLevel.Tooltip" ) );
    props.setLook( wLogLevel );
    FormData fdLogLevel = new FormData();
    fdLogLevel.left = new FormAttachment( wlLogLevel, 6 );
    fdLogLevel.width = 200;
    if ( Const.isOSX() ) {
      fdLogLevel.top = new FormAttachment( wClearLog, 2, SWT.TOP );
    } else {
      fdLogLevel.top = new FormAttachment( wClearLog, -2, SWT.TOP );
    }
    fdLogLevel.right = new FormAttachment( 0, 500 );
    wLogLevel.setLayoutData( fdLogLevel );
    wLogLevel.setItems( LogLevel.getLogLevelDescriptions() );
  }

  public boolean open() {

    mainLayout( PKG, "TransExecutionConfigurationDialog", GUIResource.getInstance().getImageTransGraph() );
    environmentTypeSectionLayout( PKG, "TransExecutionConfigurationDialog" );

    TransMeta transMeta = (TransMeta) abstractMeta;

    // Check for cluster environment to enable/disable button
    if ( transMeta.getClusterSchemas() == null || transMeta.getClusterSchemas().size() == 0 ) {
      execClusterComposite = new Composite( gLocal, SWT.NONE );
      execClusterComposite.setLayout( new FormLayout() );
      execClusterComposite.setToolTipText( BaseMessages.getString( PKG,
          "TransExecutionConfigurationDialog.ExecCluster.DisabledTooltip" ) );
      props.setLook( execClusterComposite );
      fdExecClusterComposite = new FormData();
      fdExecClusterComposite.left = new FormAttachment( wExecLocal, 0, SWT.LEFT );
      if ( abstractMeta.getSlaveServers() == null || abstractMeta.getSlaveServers().size() == 0 ) {
        fdExecClusterComposite.top = new FormAttachment( composite, 7 );
      } else {
        fdExecClusterComposite.top = new FormAttachment( wExecRemote, 7 );
      }
      execClusterComposite.setLayoutData( fdExecClusterComposite );

      wExecCluster = new Button( execClusterComposite, SWT.RADIO );
      wExecCluster.setText( BaseMessages.getString( PKG, "TransExecutionConfigurationDialog.ExecCluster.Label" ) );
      props.setLook( wExecCluster );
      wExecCluster.setEnabled( false );
      fdExecCluster = new FormData();
      fdExecCluster.top = new FormAttachment( 0 );
      wExecCluster.setLayoutData( fdExecCluster );
    } else {
      wExecCluster = new Button( gLocal, SWT.RADIO );
      wExecCluster.setText( BaseMessages.getString( PKG, "TransExecutionConfigurationDialog.ExecCluster.Label" ) );
      wExecCluster.setToolTipText( BaseMessages.getString( PKG,
          "TransExecutionConfigurationDialog.ExecCluster.Tooltip" ) );
      props.setLook( wExecCluster );
      fdExecCluster = new FormData();
      fdExecCluster.left = new FormAttachment( 0, 10 );
      if ( abstractMeta.getSlaveServers() == null || abstractMeta.getSlaveServers().size() == 0 ) {
        fdExecCluster.top = new FormAttachment( composite, 7 );
      } else {
        fdExecCluster.top = new FormAttachment( wExecRemote, 7 );
      }
      wExecCluster.setLayoutData( fdExecCluster );
      wExecCluster.addSelectionListener( new SelectionAdapter() {
        public void widgetSelected( SelectionEvent e ) {
          stackedLayout.topControl = clusteredOptionsComposite;
          stackedLayoutComposite.layout();
        }
      } );
    }

    clusteredOptionsComposite = new Composite( stackedLayoutComposite, SWT.NONE );
    clusteredOptionsComposite.setLayout( new FormLayout() );
    props.setLook( clusteredOptionsComposite );

    clusteredOptionsComposite();

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

    wExecLocal.setSelection( configuration.isExecutingLocally() );
    if ( configuration.isExecutingLocally() ) {
      stackedLayout.topControl = localOptionsComposite;
    }
    wExecRemote.setSelection( configuration.isExecutingRemotely() );
    if ( configuration.isExecutingRemotely() ) {
      stackedLayout.topControl = serverOptionsComposite;
    }
    wExecCluster.setSelection( getConfiguration().isExecutingClustered() );
    if ( getConfiguration().isExecutingClustered() ) {
      stackedLayout.topControl = clusteredOptionsComposite;
    }

    wSafeMode.setSelection( configuration.isSafeModeEnabled() );
    wClearLog.setSelection( configuration.isClearingLog() );
    wRemoteHost.setText( configuration.getRemoteServer() == null ? "" : configuration.getRemoteServer().toString() );
    wPassExport.setSelection( configuration.isPassingExport() );
    wGatherMetrics.setSelection( configuration.isGatheringMetrics() );
    showDialogRunCheckbox.setSelection( getConfiguration().isClusterShowingTransformation() );

    wLogLevel.select( configuration.getLogLevel().getLevel() );
    getParamsData();
    getVariablesData();
  }

  public void getInfo() {
    try {
      configuration.setReplayDate( null ); // removed from new execution dialog.
      configuration.setExecutingLocally( wExecLocal.getSelection() );
      configuration.setExecutingRemotely( wExecRemote.getSelection() );
      getConfiguration().setExecutingClustered( wExecCluster.getSelection() );

      // Local data
      // --> preview handled in debug transformation meta dialog

      // Remote data
      if ( wExecRemote.getSelection() ) {
        String serverName = wRemoteHost.getText();
        configuration.setRemoteServer( abstractMeta.findSlaveServer( serverName ) );
        configuration.setPassingExport( wPassExport.getSelection() );
      }
      if ( wExecCluster.getSelection() ) {
        getConfiguration().setClusterShowingTransformation( showDialogRunCheckbox.getSelection() );
      }

      // Clustering data
      getConfiguration().setClusterPosting( wExecCluster.getSelection() ? true : false );
      getConfiguration().setClusterPreparing( wExecCluster.getSelection() ? true : false );
      getConfiguration().setClusterStarting( wExecCluster.getSelection() ? true : false );
      getConfiguration().setClusterShowingTransformation( wExecCluster.getSelection() ? true : false );

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
