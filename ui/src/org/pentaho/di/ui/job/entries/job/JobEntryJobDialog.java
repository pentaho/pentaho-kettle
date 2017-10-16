/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
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
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entries.trans.JobEntryBaseDialog;
import org.pentaho.di.ui.repository.dialog.SelectObjectDialog;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.util.SwtSvgImageUtil;

import java.io.File;

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
    wlDescription.setText( BaseMessages.getString( PKG, "JobJob.Local.Label" ) );
    wPassParams.setText( BaseMessages.getString( PKG, "JobJob.PassAllParameters.Label" ) );

    // Start Server Section
    wWaitingToFinish = new Button( wServer, SWT.CHECK );
    props.setLook( wWaitingToFinish );
    wWaitingToFinish.setText( BaseMessages.getString( PKG, "JobJob.WaitToFinish.Label" ) );
    FormData fdWait = new FormData();
    fdWait.top = new FormAttachment( wSlaveServer, 10 );
    fdWait.left = new FormAttachment( 0, 0 );
    wWaitingToFinish.setLayoutData( fdWait );

    wPassExport = new Button( wServer, SWT.CHECK );
    wPassExport.setText( BaseMessages.getString( PKG, "JobJob.PassExportToSlave.Label" ) );
    props.setLook( wPassExport );
    FormData fdPassExport = new FormData();
    fdPassExport.left = new FormAttachment( 0, 0 );
    fdPassExport.top = new FormAttachment( wWaitingToFinish, 10 );
    fdPassExport.right = new FormAttachment( 100, 0 );
    wPassExport.setLayoutData( fdPassExport );

    wExpandRemote = new Button( wServer, SWT.CHECK );
    wExpandRemote.setText( BaseMessages.getString( PKG, "JobEntryJobDialog.ExpandRemoteOnSlave.Label" ) );
    props.setLook( wExpandRemote );
    FormData fdExpandRemote = new FormData();
    fdExpandRemote.top = new FormAttachment( wPassExport, 10 );
    fdExpandRemote.left = new FormAttachment( 0, 0 );
    wExpandRemote.setLayoutData( fdExpandRemote );

    wFollowingAbortRemotely = new Button( wServer, SWT.CHECK );
    props.setLook( wFollowingAbortRemotely );
    wFollowingAbortRemotely.setText( BaseMessages.getString( PKG, "JobJob.AbortRemote.Label" ) );
    FormData fdFollow = new FormData();
    fdFollow.top = new FormAttachment( wExpandRemote, 10 );
    fdFollow.left = new FormAttachment( 0, 0 );
    wFollowingAbortRemotely.setLayoutData( fdFollow );
    // End Server Section

    wbGetParams.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent arg0 ) {
        getParameters( null ); // force reload from file specification
      }
    } );

    wbBrowse.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        if ( rep != null ) {
          selectJob();
        } else {
          pickFileVFS();
        }
      }
    } );

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
      .getImage( shell.getDisplay(), getClass().getClassLoader(), "JOB.svg", ConstUI.ICON_SIZE,
        ConstUI.ICON_SIZE );
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

  protected void getParameters( JobMeta inputJobMeta ) {
    try {
      if ( inputJobMeta == null ) {
        JobEntryJob jej = new JobEntryJob();
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

  protected void selectJob() {
    if ( rep != null ) {
      SelectObjectDialog sod = new SelectObjectDialog( shell, rep, false, true );
      String jobname = sod.open();
      if ( jobname != null ) {
        String path = getPath( sod.getDirectory().getPath() );
        String fullPath = path + "/" + jobname;
        wPath.setText( fullPath );
        specificationMethod = ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME;
      }
    }
  }

  private void updateByReferenceField( RepositoryElementMetaInterface element ) {
    String path = getPathOf( element );
    if ( path == null ) {
      path = "";
    }
    wByReference.setText( path );
  }

  protected void pickFileVFS() {
    FileDialog dialog = new FileDialog( shell, SWT.OPEN );
    dialog.setFilterExtensions( Const.STRING_JOB_FILTER_EXT );
    dialog.setFilterNames( Const.getJobFilterNames() );
    String prevName = jobMeta.environmentSubstitute( wPath.getText() );
    String parentFolder = null;
    try {
      parentFolder =
        KettleVFS.getFilename( KettleVFS
          .getFileObject( jobMeta.environmentSubstitute( jobMeta.getFilename() ) ).getParent() );
    } catch ( Exception e ) {
      // not that important
    }
    if ( !Utils.isEmpty( prevName ) ) {
      try {
        if ( KettleVFS.fileExists( prevName ) ) {
          dialog.setFilterPath( KettleVFS.getFilename( KettleVFS.getFileObject( prevName ).getParent() ) );
        } else {

          if ( !prevName.endsWith( ".kjb" ) ) {
            prevName = getEntryName( Const.trim( wPath.getText() ) + ".kjb" );
          }
          if ( KettleVFS.fileExists( prevName ) ) {
            wPath.setText( prevName );
            specificationMethod = ObjectLocationSpecificationMethod.FILENAME;
            setRadioButtons();
            return;
          } else {
            // File specified doesn't exist. Ask if we should create the file...
            //
            MessageBox mb = new MessageBox( shell, SWT.YES | SWT.NO | SWT.ICON_QUESTION );
            mb.setMessage( BaseMessages.getString( PKG, "JobJob.Dialog.CreateJobQuestion.Message" ) );
            mb.setText( BaseMessages.getString( PKG, "JobJob.Dialog.CreateJobQuestion.Title" ) ); // Sorry!
            int answer = mb.open();
            if ( answer == SWT.YES ) {

              Spoon spoon = Spoon.getInstance();
              spoon.newJobFile();
              JobMeta newJobMeta = spoon.getActiveJob();
              newJobMeta.initializeVariablesFrom( jobEntry );
              newJobMeta.setFilename( jobMeta.environmentSubstitute( prevName ) );
              wPath.setText( prevName );
              specificationMethod = ObjectLocationSpecificationMethod.FILENAME;
              setRadioButtons();
              spoon.saveFile();
              return;
            }
          }
        }
      } catch ( Exception e ) {
        dialog.setFilterPath( parentFolder );
      }
    } else if ( !Utils.isEmpty( parentFolder ) ) {
      dialog.setFilterPath( parentFolder );
    }

    String fname = dialog.open();
    if ( fname != null ) {
      File file = new File( fname );
      String name = file.getName();
      String parentFolderSelection = file.getParentFile().toString();

      if ( !Utils.isEmpty( parentFolder ) && parentFolder.equals( parentFolderSelection ) ) {
        wPath.setText( getEntryName( name ) );
      } else {
        wPath.setText( fname );
      }

    }
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
    wLocal.setVisible( !wbServer.getSelection() );
  }

  public void getData() {
    wName.setText( Const.NVL( jobEntry.getName(), "" ) );

    specificationMethod = jobEntry.getSpecificationMethod();
    switch ( specificationMethod ) {
      case FILENAME:
        wPath.setText( Const.NVL( jobEntry.getFilename(), "" ) );
        break;
      case REPOSITORY_BY_NAME:
        String fullPath = Const.NVL( jobEntry.getDirectory(), "" ) + "/" + Const.NVL( jobEntry.getJobName(), "" );
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
    setRadioButtons();

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
    if ( jobEntry.getRemoteSlaveServerName() != null && !Utils.isEmpty( jobEntry.getRemoteSlaveServerName() ) ) {
      wbServer.setSelection( true );
    } else {
      wbLocal.setSelection( true );
    }
    if ( jobEntry.getRemoteSlaveServerName() != null ) {
      wSlaveServer.setText( jobEntry.getRemoteSlaveServerName() );
    }
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

    wName.selectAll();
    wName.setFocus();
  }

  private void getByReferenceData( ObjectId referenceObjectId ) {
    try {
      RepositoryObject jobInf = rep.getObjectInformation( referenceObjectId, RepositoryObjectType.JOB );
      String path = getPath( jobInf.getRepositoryDirectory().getPath() );
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

  private void getInfo( JobEntryJob jej ) {
    jej.setName( wName.getText() );
    if ( rep != null ) {
      specificationMethod = ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME;
    } else {
      specificationMethod = ObjectLocationSpecificationMethod.FILENAME;
    }
    jej.setSpecificationMethod( specificationMethod );
    switch ( specificationMethod ) {
      case FILENAME:
        jej.setFileName( wPath.getText() );
        jej.setDirectory( null );
        jej.setJobName( null );
        jej.setJobObjectId( null );
        break;
      case REPOSITORY_BY_NAME:
        String jobPath = wPath.getText();
        String jobName = jobPath;
        String directory = "";
        int index = jobPath.lastIndexOf( "/" );
        if ( index != -1 ) {
          jobName = jobPath.substring( index + 1 );
          directory = jobPath.substring( 0, index );
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
    if ( this.wbLocal.getSelection() ) {
      jej.setRemoteSlaveServerName( null );
    } else {
      jej.setRemoteSlaveServerName( wSlaveServer.getText() );
    }
    jej.setPassingExport( wPassExport.getSelection() );
    jej.setAppendLogfile = wAppendLogfile.getSelection();
    jej.setWaitingToFinish( wWaitingToFinish.getSelection() );
    jej.createParentFolder = wCreateParentFolder.getSelection();
    jej.setFollowingAbortRemotely( wFollowingAbortRemotely.getSelection() );
    jej.setExpandingRemoteJob( wExpandRemote.getSelection() );
  }

  public void ok() {
    if ( Utils.isEmpty( wName.getText() ) ) {
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
      mb.setText( BaseMessages.getString( PKG, "System.StepJobEntryNameMissing.Title" ) );
      mb.setMessage( BaseMessages.getString( PKG, "System.JobEntryNameMissing.Msg" ) );
      mb.open();
      return;
    }
    getInfo( jobEntry );
    dispose();
  }
}
