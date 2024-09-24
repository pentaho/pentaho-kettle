/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2022 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.job.entries.job;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPointHandler;
import org.pentaho.di.core.extension.KettleExtensionPoint;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobExecutionConfiguration;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.job.JobEntryJob;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryElementMetaInterface;
import org.pentaho.di.repository.RepositoryObject;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.ui.core.ConstUI;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.events.dialog.FilterType;
import org.pentaho.di.ui.core.events.dialog.SelectionOperation;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.ComboVar;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entries.trans.JobEntryBaseDialog;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.util.DialogHelper;
import org.pentaho.di.ui.util.DialogUtils;
import org.pentaho.di.ui.util.SwtSvgImageUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * This dialog allows you to edit the job job entry (JobEntryJob)
 *
 * @author Matt
 * @since 19-06-2003
 */
public class JobEntryJobDialog extends JobEntryBaseDialog implements JobEntryDialogInterface {
  private static Class<?> PKG = JobEntryJob.class; // for i18n purposes, needed by Translator2!!

  protected JobEntryJob jobEntry;

  protected Button wPassExport;

  protected Button wExpandRemote;

  private static final String[] FILE_FILTERLOGNAMES = new String[] {
    BaseMessages.getString( PKG, "JobJob.Fileformat.TXT" ),
    BaseMessages.getString( PKG, "JobJob.Fileformat.LOG" ),
    BaseMessages.getString( PKG, "JobJob.Fileformat.All" ) };

  public JobEntryJobDialog( Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta ) {
    super( parent, jobEntryInt, rep, jobMeta );
    jobEntry = (JobEntryJob) jobEntryInt;
  }

  public JobEntryInterface open() {
    Shell parent = getParent();
    display = parent.getDisplay();

    shell = new Shell( parent, props.getJobsDialogStyle() );
    props.setLook( shell );
    JobDialog.setShellImage( shell, jobEntry );

    backupChanged = jobEntry.hasChanged();

    createElements();

    // Detect [X] or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    getData();
    setActive();

    BaseStepDialog.setSize( shell );

    int width = 750;
    int height = Const.isWindows() ? 730 : 718;

    shell.setSize( width, height );
    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return jobEntry;
  }

  protected void createElements() {
    super.createElements();
    shell.setText( BaseMessages.getString( PKG, "JobJob.Header" ) );

    wlPath.setText( BaseMessages.getString( PKG, "JobJob.JobStep.Job.Label" ) );
    //wlDescription.setText( BaseMessages.getString( PKG, "JobJob.Local.Label" ) );
    wPassParams.setText( BaseMessages.getString( PKG, "JobJob.PassAllParameters.Label" ) );

    // Start Server Section
    wPassExport = new Button( gExecution, SWT.CHECK );
    wPassExport.setText( BaseMessages.getString( PKG, "JobJob.PassExportToSlave.Label" ) );
    props.setLook( wPassExport );
    FormData fdPassExport = new FormData();
    fdPassExport.left = new FormAttachment( 0, 0 );
    fdPassExport.top = new FormAttachment( wEveryRow, 10 );
    fdPassExport.right = new FormAttachment( 100, 0 );
    wPassExport.setLayoutData( fdPassExport );

    wExpandRemote = new Button( gExecution, SWT.CHECK );
    wExpandRemote.setText( BaseMessages.getString( PKG, "JobEntryJobDialog.ExpandRemoteOnSlave.Label" ) );
    props.setLook( wExpandRemote );
    FormData fdExpandRemote = new FormData();
    fdExpandRemote.top = new FormAttachment( wPassExport, 10 );
    fdExpandRemote.left = new FormAttachment( 0, 0 );
    wExpandRemote.setLayoutData( fdExpandRemote );

    wWaitingToFinish = new Button( gExecution, SWT.CHECK );
    props.setLook( wWaitingToFinish );
    wWaitingToFinish.setText( BaseMessages.getString( PKG, "JobJob.WaitToFinish.Label" ) );
    FormData fdWait = new FormData();
    fdWait.top = new FormAttachment( wExpandRemote, 10 );
    fdWait.left = new FormAttachment( 0, 0 );
    wWaitingToFinish.setLayoutData( fdWait );

    wFollowingAbortRemotely = new Button( gExecution, SWT.CHECK );
    props.setLook( wFollowingAbortRemotely );
    wFollowingAbortRemotely.setText( BaseMessages.getString( PKG, "JobJob.AbortRemote.Label" ) );
    FormData fdFollow = new FormData();
    fdFollow.top = new FormAttachment( wWaitingToFinish, 10 );
    fdFollow.left = new FormAttachment( 0, 0 );
    wFollowingAbortRemotely.setLayoutData( fdFollow );
    // End Server Section

    Composite cRunConfiguration = new Composite( wOptions, SWT.NONE );
    cRunConfiguration.setLayout( new FormLayout() );
    props.setLook( cRunConfiguration );
    FormData fdLocal = new FormData();
    fdLocal.top = new FormAttachment( 0 );
    fdLocal.right = new FormAttachment( 100 );
    fdLocal.left = new FormAttachment( 0 );

    cRunConfiguration.setBackground( shell.getBackground() ); // the default looks ugly
    cRunConfiguration.setLayoutData( fdLocal );

    Label wlRunConfiguration = new Label( cRunConfiguration, SWT.LEFT );
    props.setLook( wlRunConfiguration );
    wlRunConfiguration.setText( "Run configuration:" );
    FormData fdlRunConfiguration = new FormData();
    fdlRunConfiguration.top = new FormAttachment( 0 );
    fdlRunConfiguration.left = new FormAttachment( 0 );
    wlRunConfiguration.setLayoutData( fdlRunConfiguration );

    wRunConfiguration = new ComboVar( jobMeta, cRunConfiguration, SWT.BORDER );
    props.setLook( wRunConfiguration );
    FormData fdRunConfiguration = new FormData();
    fdRunConfiguration.width = 200;
    fdRunConfiguration.top = new FormAttachment( wlRunConfiguration, 5 );
    fdRunConfiguration.left = new FormAttachment( 0 );
    wRunConfiguration.setLayoutData( fdRunConfiguration );
    wRunConfiguration.addModifyListener( new RunConfigurationModifyListener() );

    fdgExecution.top = new FormAttachment( cRunConfiguration, 10 );

    wbGetParams.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent arg0 ) {
        getParameters( null ); // force reload from file specification
      }
    } );

    wbBrowse.addSelectionListener(DialogHelper.constructSelectionAdapterFileDialogTextVarForKettleFile(log, wPath, jobMeta
        ,SelectionOperation.FILE, FilterType.KJB, rep ) );

    wbLogFilename.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        selectLogFile( FILE_FILTERLOGNAMES );
      }
    } );
  }

  protected JobEntryBase getJobEntry() {
    return jobEntry;
  }

  protected Image getImage() {
    return SwtSvgImageUtil
      .getImage( shell.getDisplay(), getClass().getClassLoader(), "JOB.svg", ConstUI.LARGE_ICON_SIZE,
        ConstUI.LARGE_ICON_SIZE );
  }

  protected boolean getArgFromPrev() {
    return jobEntry.argFromPrevious;
  }

  protected String[] getArguments() {
    return jobEntry.arguments;
  }

  protected String[] getParamters() {
    return jobEntry.parameters;
  }

  @VisibleForTesting
  protected JobEntryJob newJobEntryJob() {
    return new JobEntryJob();
  }

  protected void getParameters( JobMeta inputJobMeta ) {
    try {
      if ( inputJobMeta == null ) {
        JobEntryJob jej = newJobEntryJob();
        getSpecificationPath( jej );
        getInfo( jej );
        inputJobMeta = jej.getJobMeta( rep, metaStore, jobMeta );
      }
      String[] parameters = inputJobMeta.listParameters();

      String[] existing = wParameters.getItems( 1 );

      for ( int i = 0; i < parameters.length; i++ ) {
        if ( Const.indexOfString( parameters[i], existing ) < 0 ) {
          TableItem item = new TableItem( wParameters.table, SWT.NONE );
          item.setText( 1, parameters[i] );
        }
      }
      wParameters.removeEmptyRows();
      wParameters.setRowNums();
      wParameters.optWidth( true );
    } catch ( Exception e ) {
      new ErrorDialog(
        shell, BaseMessages.getString( PKG, "JobEntryJobDialog.Exception.UnableToLoadJob.Title" ), BaseMessages
        .getString( PKG, "JobEntryJobDialog.Exception.UnableToLoadJob.Message" ), e );
    }
  }


  private void updateByReferenceField( RepositoryElementMetaInterface element ) {
    String path = getPathOf( element );
    if ( path == null ) {
      path = "";
    }
    wByReference.setText( path );
  }

  String getEntryName( String name ) {
    return "${"
      + Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY + "}/" + name;
  }

  public void dispose() {
    WindowProperty winprop = new WindowProperty( shell );
    props.setScreen( winprop );
    shell.dispose();
  }

  public void setActive() {
    super.setActive();
  }

  public void getData() {
    wName.setText( Const.NVL( jobEntry.getName(), "" ) );

    specificationMethod = jobEntry.getSpecificationMethod();
    switch ( specificationMethod ) {
      case FILENAME:
        wPath.setText( Const.NVL( jobEntry.getFilename(), "" ) );
        break;
      case REPOSITORY_BY_NAME:
        String dirPath = Const.NVL( jobEntry.getDirectory(), "" );
        String transPath = Const.NVL( jobEntry.getJobName(), "" );
        String fullPath = ( StringUtils.isBlank( dirPath ) ? "" : dirPath + "/" ) + transPath;
        wPath.setText( fullPath );
        break;
      case REPOSITORY_BY_REFERENCE:
        referenceObjectId = jobEntry.getJobObjectId();
        if ( rep != null ) {
          getByReferenceData( referenceObjectId );
        }
        break;
      default:
        break;
    }

    // Arguments
    if ( jobEntry.arguments != null ) {
      for ( int i = 0; i < jobEntry.arguments.length; i++ ) {
        TableItem ti = wFields.table.getItem( i );
        if ( jobEntry.arguments[i] != null ) {
          ti.setText( 1, jobEntry.arguments[i] );
        }
      }
      wFields.setRowNums();
      wFields.optWidth( true );
    }

    // Parameters
    if ( jobEntry.parameters != null ) {
      for ( int i = 0; i < jobEntry.parameters.length; i++ ) {
        TableItem ti = wParameters.table.getItem( i );
        if ( !Utils.isEmpty( jobEntry.parameters[i] ) ) {
          ti.setText( 1, Const.NVL( jobEntry.parameters[i], "" ) );
          ti.setText( 2, Const.NVL( jobEntry.parameterFieldNames[i], "" ) );
          ti.setText( 3, Const.NVL( jobEntry.parameterValues[i], "" ) );
          // Check disable listeners to shade fields gray
          parameterTableHelper.checkTableOnOpen( ti, i );
        }
      }
      wParameters.setRowNums();
      wParameters.optWidth( true );
    }

    wPassParams.setSelection( jobEntry.isPassingAllParameters() );

    wPrevious.setSelection( jobEntry.argFromPrevious );
    wPrevToParams.setSelection( jobEntry.paramsFromPrevious );
    wEveryRow.setSelection( jobEntry.execPerRow );
    wSetLogfile.setSelection( jobEntry.setLogfile );
    if ( jobEntry.logfile != null ) {
      wLogfile.setText( jobEntry.logfile );
    }
    if ( jobEntry.logext != null ) {
      wLogext.setText( jobEntry.logext );
    }
    wAddDate.setSelection( jobEntry.addDate );
    wAddTime.setSelection( jobEntry.addTime );
    wPassExport.setSelection( jobEntry.isPassingExport() );

    if ( jobEntry.logFileLevel != null ) {
      wLoglevel.select( jobEntry.logFileLevel.getLevel() );
    } else {
      // Set the default log level
      wLoglevel.select( JobEntryJob.DEFAULT_LOG_LEVEL.getLevel() );
    }
    wAppendLogfile.setSelection( jobEntry.setAppendLogfile );
    wCreateParentFolder.setSelection( jobEntry.createParentFolder );
    wWaitingToFinish.setSelection( jobEntry.isWaitingToFinish() );
    wFollowingAbortRemotely.setSelection( jobEntry.isFollowingAbortRemotely() );
    wExpandRemote.setSelection( jobEntry.isExpandingRemoteJob() );

    List<String> runConfigurations = new ArrayList<>();
    try {
      ExtensionPointHandler
        .callExtensionPoint( Spoon.getInstance().getLog(), KettleExtensionPoint.SpoonRunConfiguration.id,
          new Object[] { runConfigurations, JobMeta.XML_TAG } );
    } catch ( KettleException e ) {
      // Ignore errors
    }

    wRunConfiguration.setItems( runConfigurations.toArray( new String[ 0 ] ) );
    if ( Utils.isEmpty( jobEntry.getRunConfiguration() ) ) {
      wRunConfiguration.select( 0 );
    } else {
      wRunConfiguration.setText( jobEntry.getRunConfiguration() );
    }

    wName.selectAll();
    wName.setFocus();
  }

  private void getByReferenceData( ObjectId referenceObjectId ) {
    try {
      RepositoryObject jobInf = rep.getObjectInformation( referenceObjectId, RepositoryObjectType.JOB );
      String path =
        DialogUtils.getPath( jobMeta.getRepositoryDirectory().getPath(), jobInf.getRepositoryDirectory().getPath() );
      String fullPath =
        Const.NVL( path, "" ) + "/" + Const.NVL( jobInf.getName(), "" );
      wPath.setText( fullPath );
    } catch ( KettleException e ) {
      new ErrorDialog( shell,
        BaseMessages.getString( PKG, "JobEntryJobDialog.Exception.UnableToReferenceObjectId.Title" ),
        BaseMessages.getString( PKG, "JobEntryJobDialog.Exception.UnableToReferenceObjectId.Message" ), e );
    }
  }

  protected void cancel() {
    jobEntry.setChanged( backupChanged );

    jobEntry = null;
    dispose();
  }

  @VisibleForTesting
  protected void getInfo( JobEntryJob jej ) {
    String jobPath = getPath();
    jej.setName( getName() );
    switch ( specificationMethod ) {
      case FILENAME:
        jej.setFileName( jobPath );
        jej.setDirectory( null );
        jej.setJobName( null );
        jej.setJobObjectId( null );
        break;
      case REPOSITORY_BY_NAME:
        jobPath = getPath();
        String jobName = jobPath;
        String directory = "";
        int index = jobPath.lastIndexOf( "/" );
        if ( index != -1 ) {
          jobName = jobPath.substring( index + 1 );
          directory = index == 0 ? "/" : jobPath.substring( 0, index );
        }
        jej.setDirectory( directory );
        jej.setJobName( jobName );
        jej.setFileName( null );
        jej.setJobObjectId( null );
        break;
      default:
        break;
    }

    // Do the arguments
    int nritems = wFields.nrNonEmpty();
    int nr = 0;
    for ( int i = 0; i < nritems; i++ ) {
      String arg = wFields.getNonEmpty( i ).getText( 1 );
      if ( arg != null && arg.length() != 0 ) {
        nr++;
      }
    }
    jej.arguments = new String[nr];
    nr = 0;
    for ( int i = 0; i < nritems; i++ ) {
      String arg = wFields.getNonEmpty( i ).getText( 1 );
      if ( arg != null && arg.length() != 0 ) {
        jej.arguments[nr] = arg;
        nr++;
      }
    }

    // Do the parameters
    nritems = wParameters.nrNonEmpty();
    nr = 0;
    for ( int i = 0; i < nritems; i++ ) {
      String param = wParameters.getNonEmpty( i ).getText( 1 );
      if ( param != null && param.length() != 0 ) {
        nr++;
      }
    }
    jej.parameters = new String[nr];
    jej.parameterFieldNames = new String[nr];
    jej.parameterValues = new String[nr];
    nr = 0;
    for ( int i = 0; i < nritems; i++ ) {
      String param = wParameters.getNonEmpty( i ).getText( 1 );
      String fieldName = wParameters.getNonEmpty( i ).getText( 2 );
      String value = wParameters.getNonEmpty( i ).getText( 3 );

      jej.parameters[nr] = param;

      if ( !Utils.isEmpty( Const.trim( fieldName ) ) ) {
        jej.parameterFieldNames[nr] = fieldName;
      } else {
        jej.parameterFieldNames[nr] = "";
      }

      if ( !Utils.isEmpty( Const.trim( value ) ) ) {
        jej.parameterValues[nr] = value;
      } else {
        jej.parameterValues[nr] = "";
      }

      nr++;
    }
    jej.setPassingAllParameters( wPassParams.getSelection() );

    jej.setLogfile = wSetLogfile.getSelection();
    jej.addDate = wAddDate.getSelection();
    jej.addTime = wAddTime.getSelection();
    jej.logfile = wLogfile.getText();
    jej.logext = wLogext.getText();
    if ( wLoglevel.getSelectionIndex() >= 0 ) {
      jej.logFileLevel = LogLevel.values()[wLoglevel.getSelectionIndex()];
    } else {
      jej.logFileLevel = LogLevel.BASIC;
    }
    jej.argFromPrevious = wPrevious.getSelection();
    jej.paramsFromPrevious = wPrevToParams.getSelection();
    jej.execPerRow = wEveryRow.getSelection();
    jej.setPassingExport( wPassExport.getSelection() );
    jej.setAppendLogfile = wAppendLogfile.getSelection();
    jej.setWaitingToFinish( wWaitingToFinish.getSelection() );
    jej.createParentFolder = wCreateParentFolder.getSelection();
    jej.setFollowingAbortRemotely( wFollowingAbortRemotely.getSelection() );
    jej.setExpandingRemoteJob( wExpandRemote.getSelection() );
    jej.setRunConfiguration( wRunConfiguration.getText() );

    JobExecutionConfiguration executionConfiguration = new JobExecutionConfiguration();
    executionConfiguration.setRunConfiguration( jej.getRunConfiguration() );
    try {
      ExtensionPointHandler.callExtensionPoint( jobEntry.getLogChannel(), KettleExtensionPoint.SpoonTransBeforeStart.id,
        new Object[] { executionConfiguration, jobMeta, jobMeta, null } );
    } catch ( KettleException e ) {
      // Ignore errors
    }

    try {
      ExtensionPointHandler.callExtensionPoint( jobEntry.getLogChannel(), KettleExtensionPoint.JobEntryTransSave.id,
        new Object[] { jobMeta, jej.getRunConfiguration() } );
    } catch ( KettleException e ) {
      // Ignore errors
    }

    if ( executionConfiguration.getRemoteServer() != null ) {
      jej.setRemoteSlaveServerName( executionConfiguration.getRemoteServer().getName() );
    }
  }

  public void ok() {
    if ( Utils.isEmpty( getName() ) ) {
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
      mb.setText( BaseMessages.getString( PKG, "System.StepJobEntryNameMissing.Title" ) );
      mb.setMessage( BaseMessages.getString( PKG, "System.JobEntryNameMissing.Msg" ) );
      mb.open();
      return;
    }
    // Check if all parameters have names. If so, continue on.
    if ( parameterTableHelper.checkParams( shell ) ) {
      return;
    }
    getSpecificationPath( jobEntry );
    getInfo( jobEntry );
    jobEntry.setChanged();
    dispose();
  }

  @VisibleForTesting
  protected String getName() {
    return wName.getText();
  }

  @VisibleForTesting
  protected String getPath() {
    return wPath.getText();
  }

  @VisibleForTesting
  protected void getSpecificationPath( JobEntryJob jej ) {
    String jobPath = getPath();
    if ( rep == null || jobPath.startsWith( "file://" ) || jobPath.startsWith( "zip:file://" ) || jobPath.startsWith( "hdfs://" ) ) {
      specificationMethod = ObjectLocationSpecificationMethod.FILENAME;
    } else {
      specificationMethod = ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME;
    }
    jej.setSpecificationMethod( specificationMethod );
  }
}
