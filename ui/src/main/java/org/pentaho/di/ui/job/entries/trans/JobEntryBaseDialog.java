/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2020 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.job.entries.trans;

import org.apache.commons.vfs2.FileObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPointHandler;
import org.pentaho.di.core.extension.KettleExtensionPoint;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.util.ExecutorUtil;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.trans.JobEntryTrans;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.ColumnsResizer;
import org.pentaho.di.ui.core.widget.ComboVar;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.job.entry.JobEntryDialog;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.util.ParameterTableHelper;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Created by bmorrise on 1/6/17.
 */
public abstract class JobEntryBaseDialog extends JobEntryDialog {

  public static Class<?> PKG = JobEntryTrans.class;
  public static final int IS_PENTAHO = 1;

  protected ParameterTableHelper parameterTableHelper = new ParameterTableHelper();

  protected Label wlPath;
  protected TextVar wPath;

  protected Button wbBrowse;

  protected Group gLogFile;

  protected Composite wOptions;

  protected Label wlName;
  protected Text wName;
  protected FormData fdlName, fdName;

  protected Button wSetLogfile;

  protected Label wlLogfile;
  protected TextVar wLogfile;

  protected Button wbLogFilename;
  protected FormData fdbLogFilename;

  protected Label wlCreateParentFolder;
  protected Button wCreateParentFolder;
  protected FormData fdlCreateParentFolder, fdCreateParentFolder;

  protected Label wlLogext;
  protected TextVar wLogext;

  protected Label wlAddDate;
  protected Button wAddDate;

  protected Label wlAddTime;
  protected Button wAddTime;

  protected Label wlLoglevel;
  protected CCombo wLoglevel;

  protected Label wlPrevious;
  protected Button wPrevious;

  protected Button wPrevToParams;

  protected Button wEveryRow;

  protected Button wClearRows;

  protected Button wClearFiles;

  protected TableView wFields;

  protected TableView wParameters;

  protected Button wWaitingToFinish;

  protected Button wFollowingAbortRemotely;

  protected Group gExecution;

  protected Button wOK, wCancel;

  protected Listener lsOK, lsCancel;

  protected Shell shell;

  protected SelectionAdapter lsDef;

  protected boolean backupChanged;

  protected Button wAppendLogfile;

  protected Button wPassParams;

  protected Button wbGetParams;

  protected Display display;

  protected Text wByReference;

  protected FormData fdgExecution;

  protected ObjectId referenceObjectId;
  protected ObjectLocationSpecificationMethod specificationMethod;

  protected LogChannel log;
  protected ComboVar wRunConfiguration;

  public JobEntryBaseDialog( Shell parent,
                               JobEntryInterface jobEntryInt,
                               Repository rep, JobMeta jobMeta ) {
    super( parent, jobEntryInt, rep, jobMeta );
    log = new LogChannel( jobMeta );
  }

  protected void createElements() {

    ModifyListener lsMod = new ModifyListener() {
      public void modifyText( ModifyEvent e ) {
        getJobEntry().setChanged();
      }
    };

    ModifyListener lsModParams = modifyEvent -> {
      parameterTableHelper.checkTableOnMod( modifyEvent );
      getJobEntry().setChanged();
    };


    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = 15;
    formLayout.marginHeight = 15;

    shell.setLayout( formLayout );

    Label wicon = new Label( shell, SWT.RIGHT );
    wicon.setImage( getImage() );
    FormData fdlicon = new FormData();
    fdlicon.top = new FormAttachment( 0, 0 );
    fdlicon.right = new FormAttachment( 100, 0 );
    wicon.setLayoutData( fdlicon );
    props.setLook( wicon );

    wlName = new Label( shell, SWT.LEFT );
    props.setLook( wlName );
    wlName.setText( BaseMessages.getString( PKG, "JobTrans.JobStep.Label" ) );
    fdlName = new FormData();
    fdlName.left = new FormAttachment( 0, 0 );
    fdlName.top = new FormAttachment( 0, 0 );
    wlName.setLayoutData( fdlName );

    wName = new Text( shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wName );
    fdName = new FormData();
    fdName.width = 250;
    fdName.top = new FormAttachment( wlName, 5 );
    fdName.left = new FormAttachment( 0, 0 );
    wName.setLayoutData( fdName );

    Label spacer = new Label( shell, SWT.HORIZONTAL | SWT.SEPARATOR );
    FormData fdSpacer = new FormData();
    fdSpacer.left = new FormAttachment( 0, 0 );
    fdSpacer.top = new FormAttachment( wName, 15 );
    fdSpacer.right = new FormAttachment( 100, 0 );
    spacer.setLayoutData( fdSpacer );

    wlPath = new Label( shell, SWT.LEFT );
    props.setLook( wlPath );
    FormData fdlTransformation = new FormData();
    fdlTransformation.left = new FormAttachment( 0, 0 );
    fdlTransformation.top = new FormAttachment( spacer, 20 );
    fdlTransformation.right = new FormAttachment( 50, 0 );
    wlPath.setLayoutData( fdlTransformation );

    wPath = new TextVar( jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wPath );
    FormData fdTransformation = new FormData();
    fdTransformation.left = new FormAttachment( 0, 0 );
    fdTransformation.top = new FormAttachment( wlPath, 5 );
    fdTransformation.right = new FormAttachment( 50, 0 );
    wPath.setLayoutData( fdTransformation );

    wbBrowse = new Button( shell, SWT.PUSH );
    props.setLook( wbBrowse );
    wbBrowse.setText( BaseMessages.getString( PKG, "JobTrans.Browse.Label" ) );
    FormData fdBrowse = new FormData();
    fdBrowse.left = new FormAttachment( wPath, 5 );
    fdBrowse.top = new FormAttachment( wlPath, Const.isOSX() ? 0 : 5 );
    wbBrowse.setLayoutData( fdBrowse );

    CTabFolder wTabFolder = new CTabFolder( shell, SWT.BORDER );
    props.setLook( wTabFolder, Props.WIDGET_STYLE_TAB );

    // Options Tab Start
    CTabItem wOptionsTab = new CTabItem( wTabFolder, SWT.NONE );
    wOptionsTab.setText( BaseMessages.getString( PKG, "JobTrans.Options.Group.Label" ) );

    wOptions = new Composite( wTabFolder, SWT.SHADOW_NONE );
    props.setLook( wOptions );

    FormLayout specLayout = new FormLayout();
    specLayout.marginWidth = 15;
    specLayout.marginHeight = 15;
    wOptions.setLayout( specLayout );

    gExecution = new Group( wOptions, SWT.SHADOW_ETCHED_IN );
    props.setLook( gExecution );
    gExecution.setText( BaseMessages.getString( PKG, "JobTrans.Execution.Group.Label" ) );
    FormLayout gExecutionLayout = new FormLayout();
    gExecutionLayout.marginWidth = 15;
    gExecutionLayout.marginHeight = 15;
    gExecution.setLayout( gExecutionLayout );

    fdgExecution = new FormData();
    fdgExecution.top = new FormAttachment( 0, 10 );
    fdgExecution.left = new FormAttachment( 0, 0 );
    fdgExecution.right = new FormAttachment( 100, 0 );
    gExecution.setLayoutData( fdgExecution );

    wEveryRow = new Button( gExecution, SWT.CHECK );
    props.setLook( wEveryRow );
    wEveryRow.setText( BaseMessages.getString( PKG, "JobTrans.ExecForEveryInputRow.Label" ) );
    FormData fdbExecute = new FormData();
    fdbExecute.left = new FormAttachment( 0, 0 );
    fdbExecute.top = new FormAttachment( 0, 0 );
    wEveryRow.setLayoutData( fdbExecute );

    wOptionsTab.setControl( wOptions );

    FormData fdOptions = new FormData();
    fdOptions.left = new FormAttachment( 0, 0 );
    fdOptions.top = new FormAttachment( 0, 0 );
    fdOptions.right = new FormAttachment( 100, 0 );
    fdOptions.bottom = new FormAttachment( 100, 0 );
    wOptions.setLayoutData( fdOptions );
    // Options Tab End

    // Logging Tab Start
    CTabItem wLoggingTab = new CTabItem( wTabFolder, SWT.NONE );
    wLoggingTab.setText( BaseMessages.getString( PKG, "JobTrans.LogSettings.Group.Label" ) );

    Composite wLogging = new Composite( wTabFolder, SWT.SHADOW_NONE );
    props.setLook( wLogging );

    FormLayout loggingLayout = new FormLayout();
    loggingLayout.marginWidth = 15;
    loggingLayout.marginHeight = 15;
    wLogging.setLayout( loggingLayout );

    wSetLogfile = new Button( wLogging, SWT.CHECK );
    props.setLook( wSetLogfile );
    wSetLogfile.setText( BaseMessages.getString( PKG, "JobTrans.Specify.Logfile.Label" ) );
    FormData fdSpecifyLogFile = new FormData();
    fdSpecifyLogFile.left = new FormAttachment( 0, 0 );
    fdSpecifyLogFile.top = new FormAttachment( 0, 0 );
    wSetLogfile.setLayoutData( fdSpecifyLogFile );

    gLogFile = new Group( wLogging, SWT.SHADOW_ETCHED_IN );
    props.setLook( gLogFile );
    gLogFile.setText( BaseMessages.getString( PKG, "JobTrans.Logfile.Group.Label" ) );
    FormLayout gLogFileLayout = new FormLayout();
    gLogFileLayout.marginWidth = 15;
    gLogFileLayout.marginHeight = 15;
    gLogFile.setLayout( gLogFileLayout );

    FormData fdgLogFile = new FormData();
    fdgLogFile.top = new FormAttachment( wSetLogfile, 10 );
    fdgLogFile.left = new FormAttachment( 0, 0 );
    fdgLogFile.right = new FormAttachment( 100, 0 );
    gLogFile.setLayoutData( fdgLogFile );

    wlLogfile = new Label( gLogFile, SWT.LEFT );
    props.setLook( wlLogfile );
    wlLogfile.setText( BaseMessages.getString( PKG, "JobTrans.NameOfLogfile.Label" ) );
    FormData fdlName = new FormData();
    fdlName.left = new FormAttachment( 0, 0 );
    fdlName.top = new FormAttachment( 0, 0 );
    wlLogfile.setLayoutData( fdlName );

    wLogfile = new TextVar( jobMeta, gLogFile, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wLogfile );
    FormData fdName = new FormData();
    fdName.width = 250;
    fdName.left = new FormAttachment( 0, 0 );
    fdName.top = new FormAttachment( wlLogfile, 5 );
    wLogfile.setLayoutData( fdName );

    wbLogFilename = new Button( gLogFile, SWT.PUSH | SWT.CENTER );
    props.setLook( wbLogFilename );
    wbLogFilename.setText( BaseMessages.getString( PKG, "JobTrans.Browse.Label" ) );
    fdbLogFilename = new FormData();
    fdbLogFilename.top = new FormAttachment( wlLogfile, Const.isOSX() ? 0 : 5 );
    fdbLogFilename.left = new FormAttachment( wLogfile, 5 );
    wbLogFilename.setLayoutData( fdbLogFilename );

    wlLogext = new Label( gLogFile, SWT.LEFT );
    props.setLook( wlLogext );
    wlLogext.setText( BaseMessages.getString( PKG, "JobTrans.LogfileExtension.Label" ) );
    FormData fdlExtension = new FormData();
    fdlExtension.left = new FormAttachment( 0, 0 );
    fdlExtension.top = new FormAttachment( wLogfile, 10 );
    wlLogext.setLayoutData( fdlExtension );

    wLogext = new TextVar( jobMeta, gLogFile, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wLogext );
    FormData fdExtension = new FormData();
    fdExtension.width = 250;
    fdExtension.left = new FormAttachment( 0, 0 );
    fdExtension.top = new FormAttachment( wlLogext, 5 );
    wLogext.setLayoutData( fdExtension );

    wlLoglevel = new Label( gLogFile, SWT.LEFT );
    props.setLook( wlLoglevel );
    wlLoglevel.setText( BaseMessages.getString( PKG, "JobTrans.Loglevel.Label" ) );
    FormData fdlLogLevel = new FormData();
    fdlLogLevel.left = new FormAttachment( 0, 0 );
    fdlLogLevel.top = new FormAttachment( wLogext, 10 );
    wlLoglevel.setLayoutData( fdlLogLevel );

    wLoglevel = new CCombo( gLogFile, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wLoglevel.setItems( LogLevel.getLogLevelDescriptions() );
    props.setLook( wLoglevel );
    FormData fdLogLevel = new FormData();
    fdLogLevel.width = 250;
    fdLogLevel.left = new FormAttachment( 0, 0 );
    fdLogLevel.top = new FormAttachment( wlLoglevel, 5 );
    wLoglevel.setLayoutData( fdLogLevel );

    wAppendLogfile = new Button( gLogFile, SWT.CHECK );
    props.setLook( wAppendLogfile );
    wAppendLogfile.setText( BaseMessages.getString( PKG, "JobTrans.Append.Logfile.Label" ) );
    FormData fdLogFile = new FormData();
    fdLogFile.left = new FormAttachment( 0, 0 );
    fdLogFile.top = new FormAttachment( wLoglevel, 10 );
    wAppendLogfile.setLayoutData( fdLogFile );

    wCreateParentFolder = new Button( gLogFile, SWT.CHECK );
    props.setLook( wCreateParentFolder );
    wCreateParentFolder.setText( BaseMessages.getString( PKG, "JobTrans.Logfile.CreateParentFolder.Label" ) );
    FormData fdCreateParent = new FormData();
    fdCreateParent.left = new FormAttachment( 0, 0 );
    fdCreateParent.top = new FormAttachment( wAppendLogfile, 10 );
    wCreateParentFolder.setLayoutData( fdCreateParent );

    wAddDate = new Button( gLogFile, SWT.CHECK );
    props.setLook( wAddDate );
    wAddDate.setText( BaseMessages.getString( PKG, "JobTrans.Logfile.IncludeDate.Label" ) );
    FormData fdIncludeDate = new FormData();
    fdIncludeDate.left = new FormAttachment( 0, 0 );
    fdIncludeDate.top = new FormAttachment( wCreateParentFolder, 10 );
    wAddDate.setLayoutData( fdIncludeDate );

    wAddTime = new Button( gLogFile, SWT.CHECK );
    props.setLook( wAddTime );
    wAddTime.setText( BaseMessages.getString( PKG, "JobTrans.Logfile.IncludeTime.Label" ) );
    FormData fdIncludeTime = new FormData();
    fdIncludeTime.left = new FormAttachment( 0, 0 );
    fdIncludeTime.top = new FormAttachment( wAddDate, 10 );
    wAddTime.setLayoutData( fdIncludeTime );

    wSetLogfile.addSelectionListener( new SelectionAdapter() {
      @Override public void widgetSelected( SelectionEvent selectionEvent ) {
        setActive();
      }
    } );

    wLoggingTab.setControl( wLogging );

    FormData fdLogging = new FormData();
    fdLogging.left = new FormAttachment( 0, 0 );
    fdLogging.top = new FormAttachment( 0, 0 );
    fdLogging.right = new FormAttachment( 100, 0 );
    fdLogging.bottom = new FormAttachment( 100, 0 );
    wOptions.setLayoutData( fdLogging );
    // Logging Tab End

    CTabItem wArgumentTab = new CTabItem( wTabFolder, SWT.NONE );
    wArgumentTab.setText( BaseMessages.getString( PKG, "JobTrans.Fields.Arguments.Label" ) );

    FormLayout fieldLayout = new FormLayout();
    fieldLayout.marginWidth = 15;
    fieldLayout.marginHeight = 15;

    Composite wFieldComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wFieldComp );
    wFieldComp.setLayout( fieldLayout );

    wPrevious = new Button( wFieldComp, SWT.CHECK );
    props.setLook( wPrevious );
    wPrevious.setSelection( getArgFromPrev() );
    wPrevious.setText( BaseMessages.getString( PKG, "JobTrans.Previous.Label" ) );
    FormData fdCopyResults = new FormData();
    fdCopyResults.top = new FormAttachment( 0, 0 );
    fdCopyResults.left = new FormAttachment( 0, 0 );
    wPrevious.setLayoutData( fdCopyResults );
    wPrevious.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        wFields.setEnabled( !getArgFromPrev() );
      }
    } );

    final int FieldsCols = 1;
    int rows = getArguments() == null ? 1 : ( getArguments().length == 0 ? 0 : getArguments().length );
    final int FieldsRows = rows;

    ColumnInfo[] colinf = new ColumnInfo[ FieldsCols ];
    colinf[ 0 ] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "JobTrans.Arguments.Argument.Label" ), ColumnInfo.COLUMN_TYPE_TEXT, false );
    colinf[ 0 ].setUsingVariables( true );

    wFields =
      new TableView( jobMeta, wFieldComp, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.NO_SCROLL | SWT.V_SCROLL,
        colinf, FieldsRows, false, lsMod, props, false );

    FormData fdFields = new FormData();
    fdFields.left = new FormAttachment( 0, 0 );
    fdFields.top = new FormAttachment( wPrevious, 15 );
    fdFields.right = new FormAttachment( 100, 0 );
    fdFields.bottom = new FormAttachment( 100, 0 );
    wFields.setLayoutData( fdFields );
    wFields.getTable().addListener( SWT.Resize, new ColumnsResizer( 0, 100 ) );

    FormData fdFieldsComp = new FormData();
    fdFieldsComp.left = new FormAttachment( 0, 0 );
    fdFieldsComp.top = new FormAttachment( 0, 0 );
    fdFieldsComp.right = new FormAttachment( 100, 0 );
    fdFieldsComp.bottom = new FormAttachment( 100, 0 );
    wFieldComp.setLayoutData( fdFieldsComp );

    wFieldComp.layout();
    wArgumentTab.setControl( wFieldComp );

    CTabItem wParametersTab = new CTabItem( wTabFolder, SWT.NONE );
    wParametersTab.setText( BaseMessages.getString( PKG, "JobTrans.Fields.Parameters.Label" ) );

    fieldLayout = new FormLayout();
    fieldLayout.marginWidth = 15;
    fieldLayout.marginHeight = 15;

    Composite wParameterComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wParameterComp );
    wParameterComp.setLayout( fieldLayout );

    wPrevToParams = new Button( wParameterComp, SWT.CHECK );
    props.setLook( wPrevToParams );
    wPrevToParams.setText( BaseMessages.getString( PKG, "JobTrans.PrevToParams.Label" ) );
    FormData fdCopyResultsParams = new FormData();
    fdCopyResultsParams.left = new FormAttachment( 0, 0 );
    fdCopyResultsParams.top = new FormAttachment( 0, 0 );
    wPrevToParams.setLayoutData( fdCopyResultsParams );
    wPrevToParams.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        getJobEntry().setChanged();
      }
    } );

    wPassParams = new Button( wParameterComp, SWT.CHECK );
    props.setLook( wPassParams );
    FormData fdPassParams = new FormData();
    fdPassParams.left = new FormAttachment( 0, 0 );
    fdPassParams.top = new FormAttachment( wPrevToParams, 10 );
    wPassParams.setLayoutData( fdPassParams );

    wbGetParams = new Button( wParameterComp, SWT.PUSH );
    wbGetParams.setText( BaseMessages.getString( PKG, "JobTrans.GetParameters.Button.Label" ) );
    FormData fdGetParams = new FormData();
    fdGetParams.bottom = new FormAttachment( 100, 0 );
    fdGetParams.right = new FormAttachment( 100, 0 );
    wbGetParams.setLayoutData( fdGetParams );

    final int parameterRows = getParamters() != null ? getParamters().length : 0;

    colinf =
      new ColumnInfo[] {
        new ColumnInfo(
          BaseMessages.getString( PKG, "JobTrans.Parameters.Parameter.Label" ), ColumnInfo.COLUMN_TYPE_TEXT,
          false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "JobTrans.Parameters.ColumnName.Label" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "JobTrans.Parameters.Value.Label" ), ColumnInfo.COLUMN_TYPE_TEXT,
          false ), };
    colinf[ 2 ].setUsingVariables( true );

    wParameters =
      new TableView( jobMeta, wParameterComp, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf, parameterRows, false,
        lsModParams, props, false );
    props.setLook( wParameters );
    FormData fdParameters = new FormData();
    fdParameters.left = new FormAttachment( 0, 0 );
    fdParameters.top = new FormAttachment( wPassParams, 10 );
    fdParameters.right = new FormAttachment( 100 );
    fdParameters.bottom = new FormAttachment( wbGetParams, -10 );
    wParameters.setLayoutData( fdParameters );
    wParameters.getTable().addListener( SWT.Resize, new ColumnsResizer( 0, 33, 33, 33 ) );

    parameterTableHelper.setParameterTableView( wParameters );
    parameterTableHelper.setUpDisabledListeners();
    // Add disabled listeners to columns
    colinf[0].setDisabledListener( parameterTableHelper.getVarDisabledListener() );
    colinf[1].setDisabledListener( parameterTableHelper.getFieldDisabledListener() );
    colinf[2].setDisabledListener( parameterTableHelper.getInputDisabledListener() );

    FormData fdParametersComp = new FormData();
    fdParametersComp.left = new FormAttachment( 0, 0 );
    fdParametersComp.top = new FormAttachment( 0, 0 );
    fdParametersComp.right = new FormAttachment( 100, 0 );
    fdParametersComp.bottom = new FormAttachment( 100, 0 );
    wParameterComp.setLayoutData( fdParametersComp );

    wParameterComp.layout();
    wParametersTab.setControl( wParameterComp );

    wTabFolder.setSelection( 0 );

    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );
    FormData fdCancel = new FormData();
    fdCancel.right = new FormAttachment( 100, 0 );
    fdCancel.bottom = new FormAttachment( 100, 0 );
    wCancel.setLayoutData( fdCancel );

    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
    FormData fdOk = new FormData();
    fdOk.right = new FormAttachment( wCancel, -5 );
    fdOk.bottom = new FormAttachment( 100, 0 );
    wOK.setLayoutData( fdOk );

    Label hSpacer = new Label( shell, SWT.HORIZONTAL | SWT.SEPARATOR );
    FormData fdhSpacer = new FormData();
    fdhSpacer.left = new FormAttachment( 0, 0 );
    fdhSpacer.bottom = new FormAttachment( wCancel, -15 );
    fdhSpacer.right = new FormAttachment( 100, 0 );
    hSpacer.setLayoutData( fdhSpacer );

    FormData fdTabFolder = new FormData();
    fdTabFolder.left = new FormAttachment( 0, 0 );
    fdTabFolder.top = new FormAttachment( wPath, 20 );
    fdTabFolder.right = new FormAttachment( 100, 0 );
    fdTabFolder.bottom = new FormAttachment( hSpacer, -15 );
    wTabFolder.setLayoutData( fdTabFolder );

    // Add listeners
    lsCancel = new Listener() {
      public void handleEvent( Event e ) {
        cancel();
      }
    };
    lsOK = new Listener() {
      public void handleEvent( Event e ) {
        ok();
      }
    };

    wOK.addListener( SWT.Selection, lsOK );
    wCancel.addListener( SWT.Selection, lsCancel );

    lsDef = new SelectionAdapter() {
      public void widgetDefaultSelected( SelectionEvent e ) {
        ok();
      }
    };
    wName.addSelectionListener( lsDef );
    wPath.addSelectionListener( lsDef );
  }

  protected void selectLogFile( String[] filters ) {
    FileDialog dialog = new FileDialog( shell, SWT.SAVE );
    dialog.setFilterExtensions( new String[] { "*.txt", "*.log", "*" } );
    dialog.setFilterNames( filters );

    if ( wLogfile.getText() != null ) {
      dialog.setFileName( jobMeta.environmentSubstitute( wLogfile.getText() ) );
    }

    if ( dialog.open() != null ) {
      wLogfile.setText( dialog.getFilterPath() + Const.FILE_SEPARATOR + dialog.getFileName() );
      String filename = dialog.getFilterPath() + Const.FILE_SEPARATOR + dialog.getFileName();
      FileObject file = null;
      try {
        file = KettleVFS.getFileObject( filename );
        // Set file extension ..
        wLogext.setText( file.getName().getExtension() );
        // Set filename without extension ...
        wLogfile.setText( wLogfile.getText().substring(
          0, wLogfile.getText().length() - wLogext.getText().length() - 1 ) );
      } catch ( Exception ex ) {
        // Ignore
      }
      if ( file != null ) {
        try {
          file.close();
        } catch ( IOException ex ) { /* Ignore */
        }
      }
    }
  }

//  protected void setRadioButtons() {
//    wLocal.setVisible( wbLocal.getSelection() );
//    wServer.setVisible( wbServer.getSelection() );
//  }

  protected void setActive() {

    gLogFile.setEnabled( wSetLogfile.getSelection() );

    wbLogFilename.setEnabled( wSetLogfile.getSelection() );

    wlLogfile.setEnabled( wSetLogfile.getSelection() );
    wLogfile.setEnabled( wSetLogfile.getSelection() );

    wlLogext.setEnabled( wSetLogfile.getSelection() );
    wLogext.setEnabled( wSetLogfile.getSelection() );

    wCreateParentFolder.setEnabled( wSetLogfile.getSelection() );

    wAddDate.setEnabled( wSetLogfile.getSelection() );

    wAddTime.setEnabled( wSetLogfile.getSelection() );

    wlLoglevel.setEnabled( wSetLogfile.getSelection() );
    wLoglevel.setEnabled( wSetLogfile.getSelection() );

    wAppendLogfile.setEnabled( wSetLogfile.getSelection() );
  }

  public class RunConfigurationModifyListener implements ModifyListener {
    @Override
    public void modifyText( ModifyEvent modifyEvent ) {
      ExecutorService executorService = ExecutorUtil.getExecutor();
      final String runConfiguration = jobMeta.environmentSubstitute( wRunConfiguration.getText() );
      executorService.submit( () -> {
        List<Object> items = Arrays.asList( runConfiguration, false );
        try {
          ExtensionPointHandler.callExtensionPoint( Spoon.getInstance().getLog(), KettleExtensionPoint
                  .RunConfigurationSelection.id, items );
        } catch ( KettleException ignored ) {
          // Ignore errors
        }
        display.asyncExec( () -> {
          if ( wWaitingToFinish != null && !wWaitingToFinish.isDisposed() ) {
            if ( (Boolean) items.get( IS_PENTAHO ) ) {
              wWaitingToFinish.setSelection( false );
              wWaitingToFinish.setEnabled( false );
            } else {
              wWaitingToFinish.setEnabled( true );
            }
          }
        } );
      } );
    }
  }

  protected abstract void ok();

  protected abstract void cancel();

  protected abstract JobEntryBase getJobEntry();

  protected abstract Image getImage();

  protected abstract boolean getArgFromPrev();

  protected abstract String[] getArguments();

  protected abstract String[] getParamters();

}
