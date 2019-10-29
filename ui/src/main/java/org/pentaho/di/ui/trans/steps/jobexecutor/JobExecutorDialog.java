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

package org.pentaho.di.ui.trans.steps.jobexecutor;

import org.apache.commons.vfs2.FileObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryObject;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.jobexecutor.JobExecutorMeta;
import org.pentaho.di.trans.steps.jobexecutor.JobExecutorParameters;
import org.pentaho.di.ui.core.ConstUI;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.ColumnsResizer;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.util.DialogHelper;
import org.pentaho.di.ui.util.DialogUtils;
import org.pentaho.di.ui.util.ParameterTableHelper;
import org.pentaho.di.ui.util.SwtSvgImageUtil;
import org.pentaho.vfs.ui.VfsFileChooserDialog;

import java.io.IOException;
import java.util.Arrays;

public class JobExecutorDialog extends BaseStepDialog implements StepDialogInterface {
  private static Class<?> PKG = JobExecutorMeta.class; // for i18n purposes, needed by Translator2!!

  private static int FIELD_DESCRIPTION = 1;
  private static int FIELD_NAME = 2;

  private ParameterTableHelper parameterTableHelper = new ParameterTableHelper();

  private JobExecutorMeta jobExecutorMeta;

  private Label wlPath;
  private TextVar wPath;

  private Button wbBrowse;

  private CTabFolder wTabFolder;

  private JobMeta executorJobMeta = null;

  protected boolean jobModified;

  private ModifyListener lsMod;
  private ModifyListener lsModParams;

  private Button wInheritAll;

  private TableView wJobExecutorParameters;

  private Label wlGroupSize;
  private TextVar wGroupSize;
  private Label wlGroupField;
  private CCombo wGroupField;
  private Label wlGroupTime;
  private TextVar wGroupTime;

  private Label wlExecutionResultTarget;
  private CCombo wExecutionResultTarget;
  private TableItem tiExecutionTimeField;
  private TableItem tiExecutionResultField;
  private TableItem tiExecutionNrErrorsField;
  private TableItem tiExecutionLinesReadField;
  private TableItem tiExecutionLinesWrittenField;
  private TableItem tiExecutionLinesInputField;
  private TableItem tiExecutionLinesOutputField;
  private TableItem tiExecutionLinesRejectedField;
  private TableItem tiExecutionLinesUpdatedField;
  private TableItem tiExecutionLinesDeletedField;
  private TableItem tiExecutionFilesRetrievedField;
  private TableItem tiExecutionExitStatusField;
  private TableItem tiExecutionLogTextField;
  private TableItem tiExecutionLogChannelIdField;

  private ObjectId referenceObjectId;
  private ObjectLocationSpecificationMethod specificationMethod;

  private ColumnInfo[] parameterColumns;

  private Label wlResultFilesTarget;

  private CCombo wResultFilesTarget;

  private Label wlResultFileNameField;

  private TextVar wResultFileNameField;

  private Label wlResultRowsTarget;

  private CCombo wResultRowsTarget;

  private Label wlResultFields;

  private TableView wResultRowsFields;

  private Button wGetParameters;

  public JobExecutorDialog( Shell parent, Object in, TransMeta tr, String sname ) {
    super( parent, (BaseStepMeta) in, tr, sname );
    jobExecutorMeta = (JobExecutorMeta) in;
    jobModified = false;
  }

  public String open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX );
    props.setLook( shell );
    setShellImage( shell, jobExecutorMeta );

    lsMod = modifyEvent -> doMod();

    // Extended Modify Listener for Params Table to enable/disable fields according to disable listeners
    lsModParams = modifyEvent -> {
      parameterTableHelper.checkTableOnMod( modifyEvent );
      doMod();
    };

    changed = jobExecutorMeta.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = 15;
    formLayout.marginHeight = 15;

    shell.setLayout( formLayout );
    shell.setText( BaseMessages.getString( PKG, "JobExecutorDialog.Shell.Title" ) );

    Label wicon = new Label( shell, SWT.RIGHT );
    wicon.setImage( getImage() );
    FormData fdlicon = new FormData();
    fdlicon.top = new FormAttachment( 0, 0 );
    fdlicon.right = new FormAttachment( 100, 0 );
    wicon.setLayoutData( fdlicon );
    props.setLook( wicon );

    // Stepname line
    wlStepname = new Label( shell, SWT.RIGHT );
    wlStepname.setText( BaseMessages.getString( PKG, "JobExecutorDialog.Stepname.Label" ) );
    props.setLook( wlStepname );
    fdlStepname = new FormData();
    fdlStepname.left = new FormAttachment( 0, 0 );
    fdlStepname.top = new FormAttachment( 0, 0 );
    wlStepname.setLayoutData( fdlStepname );

    wStepname = new Text( shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wStepname.setText( stepname );
    props.setLook( wStepname );
    wStepname.addModifyListener( lsMod );
    fdStepname = new FormData();
    fdStepname.width = 250;
    fdStepname.left = new FormAttachment( 0, 0 );
    fdStepname.top = new FormAttachment( wlStepname, 5 );
    wStepname.setLayoutData( fdStepname );

    Label spacer = new Label( shell, SWT.HORIZONTAL | SWT.SEPARATOR );
    FormData fdSpacer = new FormData();
    fdSpacer.left = new FormAttachment( 0, 0 );
    fdSpacer.top = new FormAttachment( wStepname, 15 );
    fdSpacer.right = new FormAttachment( 100, 0 );
    spacer.setLayoutData( fdSpacer );

    wlPath = new Label( shell, SWT.LEFT );
    props.setLook( wlPath );
    wlPath.setText( BaseMessages.getString( PKG, "JobExecutorDialog.Job.Label" ) );
    FormData fdlJobformation = new FormData();
    fdlJobformation.left = new FormAttachment( 0, 0 );
    fdlJobformation.top = new FormAttachment( spacer, 20 );
    fdlJobformation.right = new FormAttachment( 50, 0 );
    wlPath.setLayoutData( fdlJobformation );

    wPath = new TextVar( transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wPath );
    FormData fdJobformation = new FormData();
    fdJobformation.left = new FormAttachment( 0, 0 );
    fdJobformation.top = new FormAttachment( wlPath, 5 );
    fdJobformation.width = 350;
    wPath.setLayoutData( fdJobformation );

    wbBrowse = new Button( shell, SWT.PUSH );
    props.setLook( wbBrowse );
    wbBrowse.setText( BaseMessages.getString( PKG, "JobExecutorDialog.Browse.Label" ) );
    FormData fdBrowse = new FormData();
    fdBrowse.left = new FormAttachment( wPath, 5 );
    fdBrowse.top = new FormAttachment( wlPath, Const.isOSX() ? 0 : 5 );
    wbBrowse.setLayoutData( fdBrowse );

    wbBrowse.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        if ( repository != null ) {
          selectRepositoryJob();
        } else {
          selectFileJob();
        }
      }
    } );

    //
    // Add a tab folder for the parameters and various input and output
    // streams
    //
    wTabFolder = new CTabFolder( shell, SWT.BORDER );
    props.setLook( wTabFolder, Props.WIDGET_STYLE_TAB );
    wTabFolder.setSimple( false );
    wTabFolder.setUnselectedCloseVisible( true );

    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );
    FormData fdCancel = new FormData();
    fdCancel.right = new FormAttachment( 100, 0 );
    fdCancel.bottom = new FormAttachment( 100, 0 );
    wCancel.setLayoutData( fdCancel );

    // Some buttons
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

    // Add the tabs...
    //
    addParametersTab();
    addExecutionResultTab();
    addRowGroupTab();
    addResultRowsTab();
    addResultFilesTab();

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

    wCancel.addListener( SWT.Selection, lsCancel );
    wOK.addListener( SWT.Selection, lsOK );

    lsDef = new SelectionAdapter() {
      public void widgetDefaultSelected( SelectionEvent e ) {
        ok();
      }
    };

    wStepname.addSelectionListener( lsDef );
    wPath.addSelectionListener( lsDef );
    wResultFileNameField.addSelectionListener( lsDef );

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    // Set the shell size, based upon previous time...
    setSize( shell, 620, 675 );

    getData();
    jobExecutorMeta.setChanged( changed );
    wTabFolder.setSelection( 0 );

    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return stepname;
  }

  protected Image getImage() {
    return SwtSvgImageUtil
      .getImage( shell.getDisplay(), getClass().getClassLoader(), "JOBEx.svg", ConstUI.LARGE_ICON_SIZE,
        ConstUI.LARGE_ICON_SIZE );
  }

  private void selectRepositoryJob() {
    RepositoryObject repositoryObject = DialogHelper.selectRepositoryObject( "*.kjb", log );

    try {
      if ( repositoryObject != null ) {
        loadRepositoryJob( repositoryObject.getName(), repositoryObject.getRepositoryDirectory() );
        String path = DialogUtils
          .getPath( transMeta.getRepositoryDirectory().getPath(), executorJobMeta.getRepositoryDirectory().getPath() );
        String fullPath = ( path.equals( "/" ) ? "/" : path + "/" ) + executorJobMeta.getName();
        wPath.setText( fullPath );
        specificationMethod = ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME;
      }
    } catch ( KettleException ke ) {
      new ErrorDialog( shell,
        BaseMessages.getString( PKG, "SingleThreaderDialog.ErrorSelectingObject.DialogTitle" ),
        BaseMessages.getString( PKG, "SingleThreaderDialog.ErrorSelectingObject.DialogMessage" ), ke );
    }
  }

  private void loadRepositoryJob( String transName, RepositoryDirectoryInterface repdir ) throws KettleException {
    // Read the transformation...
    //
    executorJobMeta = repository.loadJob( transMeta.environmentSubstitute( transName ), repdir, null, null ); // reads
    // last
    // version
    executorJobMeta.clearChanged();
  }

  private void selectFileJob() {
    String curFile = transMeta.environmentSubstitute( wPath.getText() );

    FileObject root = null;

    String parentFolder = null;
    try {
      parentFolder =
        KettleVFS.getFileObject( transMeta.environmentSubstitute( transMeta.getFilename() ) ).getParent().toString();
    } catch ( Exception e ) {
      // Take no action
    }

    try {
      root = KettleVFS.getFileObject( curFile != null ? curFile : Const.getUserHomeDirectory() );

      VfsFileChooserDialog vfsFileChooser = Spoon.getInstance().getVfsFileChooserDialog( root.getParent(), root );
      FileObject file =
        vfsFileChooser.open(
          shell, null, Const.STRING_JOB_FILTER_EXT, Const.getJobFilterNames(),
          VfsFileChooserDialog.VFS_DIALOG_OPEN_FILE );
      if ( file == null ) {
        return;
      }
      String fileName = file.getName().toString();
      if ( fileName != null ) {
        loadFileJob( fileName );
        if ( parentFolder != null && fileName.startsWith( parentFolder ) ) {
          fileName = fileName.replace( parentFolder, "${" + Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY + "}" );
        }
        wPath.setText( fileName );
        specificationMethod = ObjectLocationSpecificationMethod.FILENAME;
      }
    } catch ( IOException | KettleException e ) {
      new ErrorDialog( shell,
        BaseMessages.getString( PKG, "JobExecutorDialog.ErrorLoadingJobformation.DialogTitle" ),
        BaseMessages.getString( PKG, "JobExecutorDialog.ErrorLoadingJobformation.DialogMessage" ), e );
    }
  }

  private void loadFileJob( String fname ) throws KettleException {
    executorJobMeta = new JobMeta( transMeta.environmentSubstitute( fname ), repository );
    executorJobMeta.clearChanged();
  }

  private void loadJob() throws KettleException {
    String filename = wPath.getText();
    if ( repository != null ) {
      specificationMethod = ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME;
    } else {
      specificationMethod = ObjectLocationSpecificationMethod.FILENAME;
    }
    switch ( specificationMethod ) {
      case FILENAME:
        if ( Utils.isEmpty( filename ) ) {
          return;
        }
        if ( !filename.endsWith( ".kjb" ) ) {
          filename = filename + ".kjb";
          wPath.setText( filename );
        }
        loadFileJob( filename );
        break;
      case REPOSITORY_BY_NAME:
        if ( Utils.isEmpty( filename ) ) {
          return;
        }
        String transPath = transMeta.environmentSubstitute( filename );
        String realJobname = transPath;
        String realDirectory = "";
        int index = transPath.lastIndexOf( "/" );
        if ( index != -1 ) {
          realJobname = transPath.substring( index + 1 );
          realDirectory = transPath.substring( 0, index );
        }

        if ( Utils.isEmpty( realDirectory ) || Utils.isEmpty( realJobname ) ) {
          throw new KettleException(
            BaseMessages.getString( PKG, "JobExecutorDialog.Exception.NoValidMappingDetailsFound" ) );
        }
        RepositoryDirectoryInterface repdir = repository.findDirectory( realDirectory );
        if ( repdir == null ) {
          throw new KettleException( BaseMessages.getString(
            PKG, "JobExecutorDialog.Exception.UnableToFindRepositoryDirectory" ) );
        }
        loadRepositoryJob( realJobname, repdir );
        break;
      default:
        break;
    }
  }


  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void getData() {
    specificationMethod = jobExecutorMeta.getSpecificationMethod();
    switch ( specificationMethod ) {
      case FILENAME:
        wPath.setText( Const.NVL( jobExecutorMeta.getFileName(), "" ) );
        break;
      case REPOSITORY_BY_NAME:
        String fullPath = Const.NVL( jobExecutorMeta.getDirectoryPath(), "" ) + "/" + Const
          .NVL( jobExecutorMeta.getJobName(), "" );
        wPath.setText( fullPath );
        break;
      case REPOSITORY_BY_REFERENCE:
        referenceObjectId = jobExecutorMeta.getJobObjectId();
        getByReferenceData( referenceObjectId );
        break;
      default:
        break;
    }

    // TODO: throw in a separate thread.
    //
    try {
      String[] prevSteps = transMeta.getStepNames();
      Arrays.sort( prevSteps );
      wExecutionResultTarget.setItems( prevSteps );
      wResultFilesTarget.setItems( prevSteps );
      wResultRowsTarget.setItems( prevSteps );

      String[] inputFields = transMeta.getPrevStepFields( stepMeta ).getFieldNames();
      parameterColumns[ 1 ].setComboValues( inputFields );
      wGroupField.setItems( inputFields );
    } catch ( Exception e ) {
      log.logError( "couldn't get previous step list", e );
    }

    wGroupSize.setText( Const.NVL( jobExecutorMeta.getGroupSize(), "" ) );
    wGroupTime.setText( Const.NVL( jobExecutorMeta.getGroupTime(), "" ) );
    wGroupField.setText( Const.NVL( jobExecutorMeta.getGroupField(), "" ) );

    wExecutionResultTarget.setText( jobExecutorMeta.getExecutionResultTargetStepMeta() == null
      ? "" : jobExecutorMeta.getExecutionResultTargetStepMeta().getName() );
    tiExecutionTimeField.setText( FIELD_NAME, Const.NVL( jobExecutorMeta.getExecutionTimeField(), "" ) );
    tiExecutionResultField.setText( FIELD_NAME, Const.NVL( jobExecutorMeta.getExecutionResultField(), "" ) );
    tiExecutionNrErrorsField.setText( FIELD_NAME, Const.NVL( jobExecutorMeta.getExecutionNrErrorsField(), "" ) );
    tiExecutionLinesReadField.setText( FIELD_NAME, Const.NVL( jobExecutorMeta.getExecutionLinesReadField(), "" ) );
    tiExecutionLinesWrittenField
      .setText( FIELD_NAME, Const.NVL( jobExecutorMeta.getExecutionLinesWrittenField(), "" ) );
    tiExecutionLinesInputField.setText( FIELD_NAME, Const.NVL( jobExecutorMeta.getExecutionLinesInputField(), "" ) );
    tiExecutionLinesOutputField
      .setText( FIELD_NAME, Const.NVL( jobExecutorMeta.getExecutionLinesOutputField(), "" ) );
    tiExecutionLinesRejectedField
      .setText( FIELD_NAME, Const.NVL( jobExecutorMeta.getExecutionLinesRejectedField(), "" ) );
    tiExecutionLinesUpdatedField
      .setText( FIELD_NAME, Const.NVL( jobExecutorMeta.getExecutionLinesUpdatedField(), "" ) );
    tiExecutionLinesDeletedField
      .setText( FIELD_NAME, Const.NVL( jobExecutorMeta.getExecutionLinesDeletedField(), "" ) );
    tiExecutionFilesRetrievedField
      .setText( FIELD_NAME, Const.NVL( jobExecutorMeta.getExecutionFilesRetrievedField(), "" ) );
    tiExecutionExitStatusField.setText( FIELD_NAME, Const.NVL( jobExecutorMeta.getExecutionExitStatusField(), "" ) );
    tiExecutionLogTextField.setText( FIELD_NAME, Const.NVL( jobExecutorMeta.getExecutionLogTextField(), "" ) );
    tiExecutionLogChannelIdField
      .setText( FIELD_NAME, Const.NVL( jobExecutorMeta.getExecutionLogChannelIdField(), "" ) );

    // result files
    //
    wResultFilesTarget.setText( jobExecutorMeta.getResultFilesTargetStepMeta() == null ? "" : jobExecutorMeta
      .getResultFilesTargetStepMeta().getName() );
    wResultFileNameField.setText( Const.NVL( jobExecutorMeta.getResultFilesFileNameField(), "" ) );

    // Result rows
    //
    wResultRowsTarget.setText( jobExecutorMeta.getResultRowsTargetStepMeta() == null ? "" : jobExecutorMeta
      .getResultRowsTargetStepMeta().getName() );
    for ( int i = 0; i < jobExecutorMeta.getResultRowsField().length; i++ ) {
      TableItem item = new TableItem( wResultRowsFields.table, SWT.NONE );
      item.setText( 1, Const.NVL( jobExecutorMeta.getResultRowsField()[ i ], "" ) );
      item.setText( 2, ValueMetaFactory.getValueMetaName( jobExecutorMeta.getResultRowsType()[ i ] ) );
      int length = jobExecutorMeta.getResultRowsLength()[ i ];
      item.setText( 3, length < 0 ? "" : Integer.toString( length ) );
      int precision = jobExecutorMeta.getResultRowsPrecision()[ i ];
      item.setText( 4, precision < 0 ? "" : Integer.toString( precision ) );
    }
    wResultRowsFields.removeEmptyRows();
    wResultRowsFields.setRowNums();
    wResultRowsFields.optWidth( true );

    wTabFolder.setSelection( 0 );

    try {
      loadJob();
    } catch ( Throwable t ) {
      // Ignore errors
    }

    setFlags();

    wStepname.selectAll();
    wStepname.setFocus();
  }

  private void getByReferenceData( ObjectId jobObjectId ) {
    try {
      RepositoryObject jobInf = repository.getObjectInformation( jobObjectId, RepositoryObjectType.JOB );
      String path =
        DialogUtils.getPath( transMeta.getRepositoryDirectory().getPath(), jobInf.getRepositoryDirectory().getPath() );
      String fullPath =
        Const.NVL( path, "" ) + "/" + Const.NVL( jobInf.getName(), "" );
      wPath.setText( fullPath );
    } catch ( KettleException e ) {
      new ErrorDialog( shell,
        BaseMessages.getString( PKG, "JobEntryJobDialog.Exception.UnableToReferenceObjectId.Title" ),
        BaseMessages.getString( PKG, "JobEntryJobDialog.Exception.UnableToReferenceObjectId.Message" ), e );
    }
  }

  private void addParametersTab() {
    CTabItem wParametersTab = new CTabItem( wTabFolder, SWT.NONE );
    wParametersTab.setText( BaseMessages.getString( PKG, "JobExecutorDialog.Parameters.Title" ) );
    wParametersTab.setToolTipText( BaseMessages.getString( PKG, "JobExecutorDialog.Parameters.Tooltip" ) );

    Composite wParametersComposite = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wParametersComposite );

    FormLayout parameterTabLayout = new FormLayout();
    parameterTabLayout.marginWidth = 15;
    parameterTabLayout.marginHeight = 15;
    wParametersComposite.setLayout( parameterTabLayout );

    // Add a button: get parameters
    //
    wGetParameters = new Button( wParametersComposite, SWT.PUSH );
    wGetParameters.setText( BaseMessages.getString( PKG, "JobExecutorDialog.Parameters.GetParameters" ) );
    props.setLook( wGetParameters );
    FormData fdGetParameters = new FormData();
    fdGetParameters.bottom = new FormAttachment( 100, 0 );
    fdGetParameters.right = new FormAttachment( 100, 0 );
    wGetParameters.setLayoutData( fdGetParameters );
    wGetParameters.setSelection( jobExecutorMeta.getParameters().isInheritingAllVariables() );
    wGetParameters.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        getParametersFromJob( null ); // null : reload file
      }
    } );

    // Now add a table view with the 3 columns to specify: variable name, input field & optional static input
    //
    parameterColumns =
      new ColumnInfo[] {
        new ColumnInfo(
          BaseMessages.getString( PKG, "JobExecutorDialog.Parameters.column.Variable" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "JobExecutorDialog.Parameters.column.Field" ),
          ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] {}, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "JobExecutorDialog.Parameters.column.Input" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false, false ), };
    parameterColumns[ 1 ].setUsingVariables( true );

    JobExecutorParameters parameters = jobExecutorMeta.getParameters();
    wJobExecutorParameters =
      new TableView(
        transMeta, wParametersComposite, SWT.FULL_SELECTION | SWT.SINGLE | SWT.BORDER, parameterColumns,
        parameters.getVariable().length, lsModParams, props );
    props.setLook( wJobExecutorParameters );
    FormData fdJobExecutors = new FormData();
    fdJobExecutors.left = new FormAttachment( 0, 0 );
    fdJobExecutors.right = new FormAttachment( 100, 0 );
    fdJobExecutors.top = new FormAttachment( 0, 0 );
    fdJobExecutors.bottom = new FormAttachment( wGetParameters, -10 );
    wJobExecutorParameters.setLayoutData( fdJobExecutors );
    wJobExecutorParameters.getTable().addListener( SWT.Resize, new ColumnsResizer( 0, 33, 33, 33 ) );

    parameterTableHelper.setParameterTableView( wJobExecutorParameters );
    parameterTableHelper.setUpDisabledListeners();
    // Add disabled listeners to columns
    parameterColumns[0].setDisabledListener( parameterTableHelper.getVarDisabledListener() );
    parameterColumns[1].setDisabledListener( parameterTableHelper.getFieldDisabledListener() );
    parameterColumns[2].setDisabledListener( parameterTableHelper.getInputDisabledListener() );

    for ( int i = 0; i < parameters.getVariable().length; i++ ) {
      TableItem tableItem = wJobExecutorParameters.table.getItem( i );
      tableItem.setText( 1, Const.NVL( parameters.getVariable()[ i ], "" ) );
      tableItem.setText( 2, Const.NVL( parameters.getField()[ i ], "" ) );
      tableItem.setText( 3, Const.NVL( parameters.getInput()[ i ], "" ) );
      // Check disable listeners to shade fields gray
      parameterTableHelper.checkTableOnOpen( tableItem, i );
    }
    wJobExecutorParameters.setRowNums();
    wJobExecutorParameters.optWidth( true );

    // Add a checkbox: inherit all variables...
    //
    wInheritAll = new Button( wParametersComposite, SWT.CHECK );
    wInheritAll.setText( BaseMessages.getString( PKG, "JobExecutorDialog.Parameters.InheritAll" ) );
    props.setLook( wInheritAll );
    FormData fdInheritAll = new FormData();
    fdInheritAll.left = new FormAttachment( 0, 0 );
    fdInheritAll.top = new FormAttachment( wJobExecutorParameters, 15 );
    wInheritAll.setLayoutData( fdInheritAll );
    wInheritAll.setSelection( jobExecutorMeta.getParameters().isInheritingAllVariables() );

    FormData fdParametersComposite = new FormData();
    fdParametersComposite.left = new FormAttachment( 0, 0 );
    fdParametersComposite.top = new FormAttachment( 0, 0 );
    fdParametersComposite.right = new FormAttachment( 100, 0 );
    fdParametersComposite.bottom = new FormAttachment( 100, 0 );
    wParametersComposite.setLayoutData( fdParametersComposite );

    wParametersComposite.layout();
    wParametersTab.setControl( wParametersComposite );
  }

  private void doMod() {
    jobExecutorMeta.setChanged();
    setFlags();
  }

  protected void getParametersFromJob( JobMeta inputJobMeta ) {
    try {
      // Load the job in executorJobMeta
      //
      if ( inputJobMeta == null ) {
        loadJob();
        inputJobMeta = executorJobMeta;
      }

      String[] parameters = inputJobMeta.listParameters();
      for ( int i = 0; i < parameters.length; i++ ) {
        String name = parameters[ i ];
        String desc = inputJobMeta.getParameterDescription( name );

        TableItem item = new TableItem( wJobExecutorParameters.table, SWT.NONE );
        item.setText( 1, Const.NVL( name, "" ) );
        item.setText( 3, Const.NVL( desc, "" ) );
      }
      wJobExecutorParameters.removeEmptyRows();
      wJobExecutorParameters.setRowNums();
      wJobExecutorParameters.optWidth( true );

    } catch ( Exception e ) {
      new ErrorDialog(
        shell, BaseMessages.getString( PKG, "JobExecutorDialog.ErrorLoadingSpecifiedJob.Title" ), BaseMessages
        .getString( PKG, "JobExecutorDialog.ErrorLoadingSpecifiedJob.Message" ), e );
    }

  }

  private void addRowGroupTab() {

    final CTabItem wTab = new CTabItem( wTabFolder, SWT.NONE );
    wTab.setText( BaseMessages.getString( PKG, "JobExecutorDialog.RowGroup.Title" ) );
    wTab.setToolTipText( BaseMessages.getString( PKG, "JobExecutorDialog.RowGroup.Tooltip" ) );

    Composite wInputComposite = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wInputComposite );

    FormLayout tabLayout = new FormLayout();
    tabLayout.marginWidth = 15;
    tabLayout.marginHeight = 15;
    wInputComposite.setLayout( tabLayout );

    // Group size
    //
    wlGroupSize = new Label( wInputComposite, SWT.RIGHT );
    props.setLook( wlGroupSize );
    wlGroupSize.setText( BaseMessages.getString( PKG, "JobExecutorDialog.GroupSize.Label" ) );
    FormData fdlGroupSize = new FormData();
    fdlGroupSize.top = new FormAttachment( 0, 0 );
    fdlGroupSize.left = new FormAttachment( 0, 0 );
    wlGroupSize.setLayoutData( fdlGroupSize );

    wGroupSize = new TextVar( transMeta, wInputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wGroupSize );
    wGroupSize.addModifyListener( lsMod );
    FormData fdGroupSize = new FormData();
    fdGroupSize.width = 250;
    fdGroupSize.top = new FormAttachment( wlGroupSize, 5 );
    fdGroupSize.left = new FormAttachment( 0, 0 );
    wGroupSize.setLayoutData( fdGroupSize );

    // Group field
    //
    wlGroupField = new Label( wInputComposite, SWT.RIGHT );
    props.setLook( wlGroupField );
    wlGroupField.setText( BaseMessages.getString( PKG, "JobExecutorDialog.GroupField.Label" ) );
    FormData fdlGroupField = new FormData();
    fdlGroupField.top = new FormAttachment( wGroupSize, 10 );
    fdlGroupField.left = new FormAttachment( 0, 0 );
    wlGroupField.setLayoutData( fdlGroupField );

    wGroupField = new CCombo( wInputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wGroupField );
    wGroupField.addModifyListener( lsMod );
    FormData fdGroupField = new FormData();
    fdGroupField.width = 250;
    fdGroupField.top = new FormAttachment( wlGroupField, 5 );
    fdGroupField.left = new FormAttachment( 0, 0 );
    wGroupField.setLayoutData( fdGroupField );

    // Group time
    //
    wlGroupTime = new Label( wInputComposite, SWT.RIGHT );
    props.setLook( wlGroupTime );
    wlGroupTime.setText( BaseMessages.getString( PKG, "JobExecutorDialog.GroupTime.Label" ) );
    FormData fdlGroupTime = new FormData();
    fdlGroupTime.top = new FormAttachment( wGroupField, 10 );
    fdlGroupTime.left = new FormAttachment( 0, 0 ); // First one in the left
    wlGroupTime.setLayoutData( fdlGroupTime );

    wGroupTime = new TextVar( transMeta, wInputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wGroupTime );
    wGroupTime.addModifyListener( lsMod );
    FormData fdGroupTime = new FormData();
    fdGroupTime.width = 250;
    fdGroupTime.top = new FormAttachment( wlGroupTime, 5 );
    fdGroupTime.left = new FormAttachment( 0, 0 );
    wGroupTime.setLayoutData( fdGroupTime );

    wTab.setControl( wInputComposite );
    wTabFolder.setSelection( wTab );
  }

  private void addExecutionResultTab() {

    final CTabItem wTab = new CTabItem( wTabFolder, SWT.NONE );
    wTab.setText( BaseMessages.getString( PKG, "JobExecutorDialog.ExecutionResults.Title" ) );
    wTab.setToolTipText( BaseMessages.getString( PKG, "JobExecutorDialog.ExecutionResults.Tooltip" ) );

    ScrolledComposite scrolledComposite = new ScrolledComposite( wTabFolder, SWT.V_SCROLL | SWT.H_SCROLL );
    scrolledComposite.setLayout( new FillLayout() );

    Composite wInputComposite = new Composite( scrolledComposite, SWT.NONE );
    props.setLook( wInputComposite );

    FormLayout tabLayout = new FormLayout();
    tabLayout.marginWidth = 15;
    tabLayout.marginHeight = 15;
    wInputComposite.setLayout( tabLayout );

    wlExecutionResultTarget = new Label( wInputComposite, SWT.RIGHT );
    props.setLook( wlExecutionResultTarget );
    wlExecutionResultTarget
      .setText( BaseMessages.getString( PKG, "JobExecutorDialog.ExecutionResultTarget.Label" ) );
    FormData fdlExecutionResultTarget = new FormData();
    fdlExecutionResultTarget.top = new FormAttachment( 0, 0 );
    fdlExecutionResultTarget.left = new FormAttachment( 0, 0 ); // First one in the left
    wlExecutionResultTarget.setLayoutData( fdlExecutionResultTarget );

    wExecutionResultTarget = new CCombo( wInputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wExecutionResultTarget );
    wExecutionResultTarget.addModifyListener( lsMod );
    FormData fdExecutionResultTarget = new FormData();
    fdExecutionResultTarget.width = 250;
    fdExecutionResultTarget.top = new FormAttachment( wlExecutionResultTarget, 5 );
    fdExecutionResultTarget.left = new FormAttachment( 0, 0 ); // To the right
    wExecutionResultTarget.setLayoutData( fdExecutionResultTarget );

    ColumnInfo[] executionResultColumns =
      new ColumnInfo[] {
        new ColumnInfo( BaseMessages.getString( PKG, "JobExecutorMeta.ExecutionResults.FieldDescription.Label" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false, true ),
        new ColumnInfo( BaseMessages.getString( PKG, "JobExecutorMeta.ExecutionResults.FieldName.Label" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false, false )
      };
    executionResultColumns[ 1 ].setUsingVariables( true );

    TableView wExectionResults =
      new TableView( transMeta, wInputComposite, SWT.FULL_SELECTION | SWT.SINGLE | SWT.BORDER, executionResultColumns,
        14, false, lsMod, props, false );
    props.setLook( wExectionResults );
    FormData fdExecutionResults = new FormData();
    fdExecutionResults.left = new FormAttachment( 0 );
    fdExecutionResults.right = new FormAttachment( 100 );
    fdExecutionResults.top = new FormAttachment( wExecutionResultTarget, 10 );
    fdExecutionResults.bottom = new FormAttachment( 100 );
    wExectionResults.setLayoutData( fdExecutionResults );
    wExectionResults.getTable().addListener( SWT.Resize, new ColumnsResizer( 0, 50, 50 ) );

    int index = 0;
    tiExecutionTimeField = wExectionResults.table.getItem( index++ );
    tiExecutionResultField = wExectionResults.table.getItem( index++ );
    tiExecutionNrErrorsField = wExectionResults.table.getItem( index++ );
    tiExecutionLinesReadField = wExectionResults.table.getItem( index++ );
    tiExecutionLinesWrittenField = wExectionResults.table.getItem( index++ );
    tiExecutionLinesInputField = wExectionResults.table.getItem( index++ );
    tiExecutionLinesOutputField = wExectionResults.table.getItem( index++ );
    tiExecutionLinesRejectedField = wExectionResults.table.getItem( index++ );
    tiExecutionLinesUpdatedField = wExectionResults.table.getItem( index++ );
    tiExecutionLinesDeletedField = wExectionResults.table.getItem( index++ );
    tiExecutionFilesRetrievedField = wExectionResults.table.getItem( index++ );
    tiExecutionExitStatusField = wExectionResults.table.getItem( index++ );
    tiExecutionLogTextField = wExectionResults.table.getItem( index++ );
    tiExecutionLogChannelIdField = wExectionResults.table.getItem( index++ );

    tiExecutionTimeField
      .setText( FIELD_DESCRIPTION, BaseMessages.getString( PKG, "JobExecutorDialog.ExecutionTimeField.Label" ) );
    tiExecutionResultField
      .setText( FIELD_DESCRIPTION, BaseMessages.getString( PKG, "JobExecutorDialog.ExecutionResultField.Label" ) );
    tiExecutionNrErrorsField
      .setText( FIELD_DESCRIPTION, BaseMessages.getString( PKG, "JobExecutorDialog.ExecutionNrErrorsField.Label" ) );
    tiExecutionLinesReadField
      .setText( FIELD_DESCRIPTION, BaseMessages.getString( PKG, "JobExecutorDialog.ExecutionLinesReadField.Label" ) );
    tiExecutionLinesWrittenField.setText( FIELD_DESCRIPTION,
      BaseMessages.getString( PKG, "JobExecutorDialog.ExecutionLinesWrittenField.Label" ) );
    tiExecutionLinesInputField.setText( FIELD_DESCRIPTION,
      BaseMessages.getString( PKG, "JobExecutorDialog.ExecutionLinesInputField.Label" ) );
    tiExecutionLinesOutputField.setText( FIELD_DESCRIPTION,
      BaseMessages.getString( PKG, "JobExecutorDialog.ExecutionLinesOutputField.Label" ) );
    tiExecutionLinesRejectedField.setText( FIELD_DESCRIPTION,
      BaseMessages.getString( PKG, "JobExecutorDialog.ExecutionLinesRejectedField.Label" ) );
    tiExecutionLinesUpdatedField.setText( FIELD_DESCRIPTION,
      BaseMessages.getString( PKG, "JobExecutorDialog.ExecutionLinesUpdatedField.Label" ) );
    tiExecutionLinesDeletedField.setText( FIELD_DESCRIPTION,
      BaseMessages.getString( PKG, "JobExecutorDialog.ExecutionLinesDeletedField.Label" ) );
    tiExecutionFilesRetrievedField.setText( FIELD_DESCRIPTION,
      BaseMessages.getString( PKG, "JobExecutorDialog.ExecutionFilesRetrievedField.Label" ) );
    tiExecutionExitStatusField.setText( FIELD_DESCRIPTION,
      BaseMessages.getString( PKG, "JobExecutorDialog.ExecutionExitStatusField.Label" ) );
    tiExecutionLogTextField
      .setText( FIELD_DESCRIPTION, BaseMessages.getString( PKG, "JobExecutorDialog.ExecutionLogTextField.Label" ) );
    tiExecutionLogChannelIdField.setText( FIELD_DESCRIPTION,
      BaseMessages.getString( PKG, "JobExecutorDialog.ExecutionLogChannelIdField.Label" ) );

    wJobExecutorParameters.setRowNums();
    wJobExecutorParameters.optWidth( true );

    wInputComposite.pack();
    Rectangle bounds = wInputComposite.getBounds();

    scrolledComposite.setContent( wInputComposite );
    scrolledComposite.setExpandHorizontal( true );
    scrolledComposite.setExpandVertical( true );
    scrolledComposite.setMinWidth( bounds.width );
    scrolledComposite.setMinHeight( bounds.height );

    wTab.setControl( scrolledComposite );
    wTabFolder.setSelection( wTab );
  }

  private void addResultFilesTab() {
    final CTabItem wTab = new CTabItem( wTabFolder, SWT.NONE );
    wTab.setText( BaseMessages.getString( PKG, "JobExecutorDialog.ResultFiles.Title" ) );
    wTab.setToolTipText( BaseMessages.getString( PKG, "JobExecutorDialog.ResultFiles.Tooltip" ) );

    ScrolledComposite scrolledComposite = new ScrolledComposite( wTabFolder, SWT.V_SCROLL | SWT.H_SCROLL );
    scrolledComposite.setLayout( new FillLayout() );

    Composite wInputComposite = new Composite( scrolledComposite, SWT.NONE );
    props.setLook( wInputComposite );

    FormLayout tabLayout = new FormLayout();
    tabLayout.marginWidth = 15;
    tabLayout.marginHeight = 15;
    wInputComposite.setLayout( tabLayout );

    wlResultFilesTarget = new Label( wInputComposite, SWT.RIGHT );
    props.setLook( wlResultFilesTarget );
    wlResultFilesTarget.setText( BaseMessages.getString( PKG, "JobExecutorDialog.ResultFilesTarget.Label" ) );
    FormData fdlResultFilesTarget = new FormData();
    fdlResultFilesTarget.top = new FormAttachment( 0, 0 );
    fdlResultFilesTarget.left = new FormAttachment( 0, 0 ); // First one in the left
    wlResultFilesTarget.setLayoutData( fdlResultFilesTarget );

    wResultFilesTarget = new CCombo( wInputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wResultFilesTarget );
    wResultFilesTarget.addModifyListener( lsMod );
    FormData fdResultFilesTarget = new FormData();
    fdResultFilesTarget.width = 250;
    fdResultFilesTarget.top = new FormAttachment( wlResultFilesTarget, 5 );
    fdResultFilesTarget.left = new FormAttachment( 0, 0 ); // To the right
    wResultFilesTarget.setLayoutData( fdResultFilesTarget );

    // ResultFileNameField
    //
    wlResultFileNameField = new Label( wInputComposite, SWT.RIGHT );
    props.setLook( wlResultFileNameField );
    wlResultFileNameField.setText( BaseMessages.getString( PKG, "JobExecutorDialog.ResultFileNameField.Label" ) );
    FormData fdlResultFileNameField = new FormData();
    fdlResultFileNameField.top = new FormAttachment( wResultFilesTarget, 10 );
    fdlResultFileNameField.left = new FormAttachment( 0, 0 ); // First one in the left
    wlResultFileNameField.setLayoutData( fdlResultFileNameField );

    wResultFileNameField = new TextVar( transMeta, wInputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wResultFileNameField );
    wResultFileNameField.addModifyListener( lsMod );
    FormData fdResultFileNameField = new FormData();
    fdResultFileNameField.width = 250;
    fdResultFileNameField.top = new FormAttachment( wlResultFileNameField, 5 );
    fdResultFileNameField.left = new FormAttachment( 0, 0 ); // To the right
    wResultFileNameField.setLayoutData( fdResultFileNameField );

    wInputComposite.pack();
    Rectangle bounds = wInputComposite.getBounds();

    scrolledComposite.setContent( wInputComposite );
    scrolledComposite.setExpandHorizontal( true );
    scrolledComposite.setExpandVertical( true );
    scrolledComposite.setMinWidth( bounds.width );
    scrolledComposite.setMinHeight( bounds.height );

    wTab.setControl( scrolledComposite );
    wTabFolder.setSelection( wTab );
  }

  private void addResultRowsTab() {

    final CTabItem wTab = new CTabItem( wTabFolder, SWT.NONE );
    wTab.setText( BaseMessages.getString( PKG, "JobExecutorDialog.ResultRows.Title" ) );
    wTab.setToolTipText( BaseMessages.getString( PKG, "JobExecutorDialog.ResultRows.Tooltip" ) );

    ScrolledComposite scrolledComposite = new ScrolledComposite( wTabFolder, SWT.V_SCROLL | SWT.H_SCROLL );
    scrolledComposite.setLayout( new FillLayout() );

    Composite wInputComposite = new Composite( scrolledComposite, SWT.NONE );
    props.setLook( wInputComposite );

    FormLayout tabLayout = new FormLayout();
    tabLayout.marginWidth = 15;
    tabLayout.marginHeight = 15;
    wInputComposite.setLayout( tabLayout );

    wlResultRowsTarget = new Label( wInputComposite, SWT.RIGHT );
    props.setLook( wlResultRowsTarget );
    wlResultRowsTarget.setText( BaseMessages.getString( PKG, "JobExecutorDialog.ResultRowsTarget.Label" ) );
    FormData fdlResultRowsTarget = new FormData();
    fdlResultRowsTarget.top = new FormAttachment( 0, 0 );
    fdlResultRowsTarget.left = new FormAttachment( 0, 0 ); // First one in the left
    wlResultRowsTarget.setLayoutData( fdlResultRowsTarget );

    wResultRowsTarget = new CCombo( wInputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wResultRowsTarget );
    wResultRowsTarget.addModifyListener( lsMod );
    FormData fdResultRowsTarget = new FormData();
    fdResultRowsTarget.width = 250;
    fdResultRowsTarget.top = new FormAttachment( wlResultRowsTarget, 5 );
    fdResultRowsTarget.left = new FormAttachment( 0, 0 ); // To the right
    wResultRowsTarget.setLayoutData( fdResultRowsTarget );

    wlResultFields = new Label( wInputComposite, SWT.NONE );
    wlResultFields.setText( BaseMessages.getString( PKG, "JobExecutorDialog.ResultFields.Label" ) );
    props.setLook( wlResultFields );
    FormData fdlResultFields = new FormData();
    fdlResultFields.left = new FormAttachment( 0, 0 );
    fdlResultFields.top = new FormAttachment( wResultRowsTarget, 10 );
    wlResultFields.setLayoutData( fdlResultFields );

    int nrRows = ( jobExecutorMeta.getResultRowsField() != null ? jobExecutorMeta.getResultRowsField().length : 1 );

    ColumnInfo[] ciResultFields =
      new ColumnInfo[] {
        new ColumnInfo( BaseMessages.getString( PKG, "JobExecutorDialog.ColumnInfo.Field" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false, false ),
        new ColumnInfo( BaseMessages.getString( PKG, "JobExecutorDialog.ColumnInfo.Type" ),
          ColumnInfo.COLUMN_TYPE_CCOMBO, ValueMetaFactory.getValueMetaNames() ),
        new ColumnInfo( BaseMessages.getString( PKG, "JobExecutorDialog.ColumnInfo.Length" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false ),
        new ColumnInfo( BaseMessages.getString( PKG, "JobExecutorDialog.ColumnInfo.Precision" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false ), };

    wResultRowsFields =
      new TableView( transMeta, wInputComposite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL
        | SWT.H_SCROLL, ciResultFields, nrRows, false, lsMod, props, false );

    FormData fdResultFields = new FormData();
    fdResultFields.left = new FormAttachment( 0, 0 );
    fdResultFields.top = new FormAttachment( wlResultFields, 5 );
    fdResultFields.right = new FormAttachment( 100, 0 );
    fdResultFields.bottom = new FormAttachment( 100, 0 );
    wResultRowsFields.setLayoutData( fdResultFields );
    wResultRowsFields.getTable().addListener( SWT.Resize, new ColumnsResizer( 0, 25, 25, 25, 25 ) );

    wInputComposite.pack();
    Rectangle bounds = wInputComposite.getBounds();

    scrolledComposite.setContent( wInputComposite );
    scrolledComposite.setExpandHorizontal( true );
    scrolledComposite.setExpandVertical( true );
    scrolledComposite.setMinWidth( bounds.width );
    scrolledComposite.setMinHeight( bounds.height );

    wTab.setControl( scrolledComposite );
    wTabFolder.setSelection( wTab );
  }

  private void setFlags() {
    // Enable/disable fields...
    //
    if ( wlGroupSize == null
      || wlGroupSize == null || wlGroupField == null || wGroupField == null || wlGroupTime == null
      || wGroupTime == null ) {
      return;
    }
    boolean enableSize = Const.toInt( transMeta.environmentSubstitute( wGroupSize.getText() ), -1 ) >= 0;
    boolean enableField = !Utils.isEmpty( wGroupField.getText() );
    // boolean enableTime = Const.toInt(transMeta.environmentSubstitute(wGroupTime.getText()), -1)>0;

    wlGroupSize.setEnabled( true );
    wGroupSize.setEnabled( true );
    wlGroupField.setEnabled( !enableSize );
    wGroupField.setEnabled( !enableSize );
    wlGroupTime.setEnabled( !enableSize && !enableField );
    wGroupTime.setEnabled( !enableSize && !enableField );
  }

  private void cancel() {
    stepname = null;
    jobExecutorMeta.setChanged( changed );
    dispose();
  }

  private void ok() {
    if ( Utils.isEmpty( wStepname.getText() ) ) {
      return;
    }

    // Check if all parameters have names. If so, continue on.
    if ( parameterTableHelper.checkParams( shell ) ) {
      return;
    }

    stepname = wStepname.getText(); // return value

    try {
      loadJob();
    } catch ( KettleException e ) {
      new ErrorDialog(
        shell, BaseMessages.getString( PKG, "JobExecutorDialog.ErrorLoadingSpecifiedJob.Title" ), BaseMessages
        .getString( PKG, "JobExecutorDialog.ErrorLoadingSpecifiedJob.Message" ), e );
    }

    jobExecutorMeta.setSpecificationMethod( specificationMethod );
    switch ( specificationMethod ) {
      case FILENAME:
        jobExecutorMeta.setFileName( wPath.getText() );
        jobExecutorMeta.setDirectoryPath( null );
        jobExecutorMeta.setJobName( null );
        jobExecutorMeta.setJobObjectId( null );
        break;
      case REPOSITORY_BY_NAME:
        String transPath = wPath.getText();
        String transName = transPath;
        String directory = "";
        int index = transPath.lastIndexOf( "/" );
        if ( index != -1 ) {
          transName = transPath.substring( index + 1 );
          directory = transPath.substring( 0, index );
        }
        jobExecutorMeta.setDirectoryPath( directory );
        jobExecutorMeta.setJobName( transName );
        jobExecutorMeta.setFileName( null );
        jobExecutorMeta.setJobObjectId( null );
        break;
      default:
        break;
    }

    // Load the information on the tabs, optionally do some
    // verifications...
    //
    collectInformation();

    // Set the input steps for input mappings
    jobExecutorMeta.searchInfoAndTargetSteps( transMeta.getSteps() );

    jobExecutorMeta.setChanged( true );

    dispose();
  }

  private void collectInformation() {
    // The parameters...
    //
    JobExecutorParameters parameters = jobExecutorMeta.getParameters();

    int nrLines = wJobExecutorParameters.nrNonEmpty();
    String[] variables = new String[ nrLines ];
    String[] fields = new String[ nrLines ];
    String[] input = new String[ nrLines ];
    parameters.setVariable( variables );
    parameters.setField( fields );
    parameters.setInput( input );
    for ( int i = 0; i < nrLines; i++ ) {
      TableItem item = wJobExecutorParameters.getNonEmpty( i );
      variables[ i ] = item.getText( 1 );
      fields[ i ] = item.getText( 2 );
      input[ i ] = item.getText( 3 );
    }
    parameters.setInheritingAllVariables( wInheritAll.getSelection() );

    // The group definition
    //
    jobExecutorMeta.setGroupSize( wGroupSize.getText() );
    jobExecutorMeta.setGroupField( wGroupField.getText() );
    jobExecutorMeta.setGroupTime( wGroupTime.getText() );

    jobExecutorMeta.setExecutionResultTargetStep( wExecutionResultTarget.getText() );
    jobExecutorMeta.setExecutionResultTargetStepMeta( transMeta.findStep( wExecutionResultTarget.getText() ) );
    jobExecutorMeta.setExecutionTimeField( tiExecutionTimeField.getText( FIELD_NAME ) );
    jobExecutorMeta.setExecutionResultField( tiExecutionResultField.getText( FIELD_NAME ) );
    jobExecutorMeta.setExecutionNrErrorsField( tiExecutionNrErrorsField.getText( FIELD_NAME ) );
    jobExecutorMeta.setExecutionLinesReadField( tiExecutionLinesReadField.getText( FIELD_NAME ) );
    jobExecutorMeta.setExecutionLinesWrittenField( tiExecutionLinesWrittenField.getText( FIELD_NAME ) );
    jobExecutorMeta.setExecutionLinesInputField( tiExecutionLinesInputField.getText( FIELD_NAME ) );
    jobExecutorMeta.setExecutionLinesOutputField( tiExecutionLinesOutputField.getText( FIELD_NAME ) );
    jobExecutorMeta.setExecutionLinesRejectedField( tiExecutionLinesRejectedField.getText( FIELD_NAME ) );
    jobExecutorMeta.setExecutionLinesUpdatedField( tiExecutionLinesUpdatedField.getText( FIELD_NAME ) );
    jobExecutorMeta.setExecutionLinesDeletedField( tiExecutionLinesDeletedField.getText( FIELD_NAME ) );
    jobExecutorMeta.setExecutionFilesRetrievedField( tiExecutionFilesRetrievedField.getText( FIELD_NAME ) );
    jobExecutorMeta.setExecutionExitStatusField( tiExecutionExitStatusField.getText( FIELD_NAME ) );
    jobExecutorMeta.setExecutionLogTextField( tiExecutionLogTextField.getText( FIELD_NAME ) );
    jobExecutorMeta.setExecutionLogChannelIdField( tiExecutionLogChannelIdField.getText( FIELD_NAME ) );

    jobExecutorMeta.setResultFilesTargetStep( wResultFilesTarget.getText() );
    jobExecutorMeta.setResultFilesTargetStepMeta( transMeta.findStep( wResultFilesTarget.getText() ) );

    jobExecutorMeta.setResultFilesFileNameField( wResultFileNameField.getText() );

    // Result row info
    //
    jobExecutorMeta.setResultRowsTargetStep( wResultRowsTarget.getText() );
    jobExecutorMeta.setResultRowsTargetStepMeta( transMeta.findStep( wResultRowsTarget.getText() ) );
    int nrFields = wResultRowsFields.nrNonEmpty();
    jobExecutorMeta.setResultRowsField( new String[ nrFields ] );
    jobExecutorMeta.setResultRowsType( new int[ nrFields ] );
    jobExecutorMeta.setResultRowsLength( new int[ nrFields ] );
    jobExecutorMeta.setResultRowsPrecision( new int[ nrFields ] );

    //CHECKSTYLE:Indentation:OFF
    for ( int i = 0; i < nrFields; i++ ) {
      TableItem item = wResultRowsFields.getNonEmpty( i );
      jobExecutorMeta.getResultRowsField()[ i ] = item.getText( 1 );
      jobExecutorMeta.getResultRowsType()[ i ] = ValueMetaFactory.getIdForValueMeta( item.getText( 2 ) );
      jobExecutorMeta.getResultRowsLength()[ i ] = Const.toInt( item.getText( 3 ), -1 );
      jobExecutorMeta.getResultRowsPrecision()[ i ] = Const.toInt( item.getText( 4 ), -1 );
    }

  }
}


