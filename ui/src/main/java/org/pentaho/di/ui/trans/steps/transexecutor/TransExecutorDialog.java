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

package org.pentaho.di.ui.trans.steps.transexecutor;

import org.apache.commons.vfs2.FileObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
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
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryObject;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.transexecutor.TransExecutorMeta;
import org.pentaho.di.trans.steps.transexecutor.TransExecutorParameters;
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

public class TransExecutorDialog extends BaseStepDialog implements StepDialogInterface {
  private static Class<?> PKG = TransExecutorMeta.class; // for i18n purposes, needed by Translator2!!

  private static int FIELD_DESCRIPTION = 1;
  private static int FIELD_NAME = 2;

  private ParameterTableHelper parameterTableHelper = new ParameterTableHelper();

  private TransExecutorMeta transExecutorMeta;

  private Label wlPath;
  private TextVar wPath;

  private Button wbBrowse;

  private CTabFolder wTabFolder;

  private TransMeta executorTransMeta = null;

  protected boolean jobModified;

  private ModifyListener lsMod;
  private ModifyListener lsModParams;

  private Button wInheritAll;

  private TableView wTransExecutorParameters;

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

  private String executorOutputStep;

  private ObjectId referenceObjectId;
  private ObjectLocationSpecificationMethod specificationMethod;

  private ColumnInfo[] parameterColumns;

  private Label wlResultFilesTarget;

  private CCombo wResultFilesTarget;

  private Label wlResultFileNameField;

  private TextVar wResultFileNameField;

  private Label wlResultRowsTarget;

  private CCombo wOutputRowsSource;

  private Label wlOutputFields;

  private TableView wOutputFields;

  private Button wGetParameters;

  public TransExecutorDialog( Shell parent, Object in, TransMeta tr, String sname ) {
    super( parent, (BaseStepMeta) in, tr, sname );
    transExecutorMeta = (TransExecutorMeta) in;
    jobModified = false;
  }

  public String open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX );
    props.setLook( shell );
    setShellImage( shell, transExecutorMeta );

    lsMod = modifyEvent -> doMod();

    // Extended Modify Listener for Params Table to enable/disable fields according to disable listeners
    lsModParams = modifyEvent -> {
      parameterTableHelper.checkTableOnMod( modifyEvent );
      doMod();
    };

    changed = transExecutorMeta.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = 15;
    formLayout.marginHeight = 15;

    shell.setLayout( formLayout );
    shell.setText( BaseMessages.getString( PKG, "TransExecutorDialog.Shell.Title" ) );

    Label wicon = new Label( shell, SWT.RIGHT );
    wicon.setImage( getImage() );
    FormData fdlicon = new FormData();
    fdlicon.top = new FormAttachment( 0, 0 );
    fdlicon.right = new FormAttachment( 100, 0 );
    wicon.setLayoutData( fdlicon );
    props.setLook( wicon );

    // Stepname line
    wlStepname = new Label( shell, SWT.RIGHT );
    wlStepname.setText( BaseMessages.getString( PKG, "TransExecutorDialog.Stepname.Label" ) );
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
    wlPath.setText( BaseMessages.getString( PKG, "TransExecutorDialog.Transformation.Label" ) );
    FormData fdlTransformation = new FormData();
    fdlTransformation.left = new FormAttachment( 0, 0 );
    fdlTransformation.top = new FormAttachment( spacer, 20 );
    fdlTransformation.right = new FormAttachment( 50, 0 );
    wlPath.setLayoutData( fdlTransformation );

    wPath = new TextVar( transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wPath );
    FormData fdTransformation = new FormData();
    fdTransformation.left = new FormAttachment( 0, 0 );
    fdTransformation.top = new FormAttachment( wlPath, 5 );
    fdTransformation.width = 350;
    wPath.setLayoutData( fdTransformation );

    wbBrowse = new Button( shell, SWT.PUSH );
    props.setLook( wbBrowse );
    wbBrowse.setText( BaseMessages.getString( PKG, "TransExecutorDialog.Browse.Label" ) );
    FormData fdBrowse = new FormData();
    fdBrowse.left = new FormAttachment( wPath, 5 );
    fdBrowse.top = new FormAttachment( wlPath, Const.isOSX() ? 0 : 5 );
    wbBrowse.setLayoutData( fdBrowse );

    wbBrowse.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        if ( repository != null ) {
          selectRepositoryTrans();
        } else {
          selectFileTrans();
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
    transExecutorMeta.setChanged( changed );
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
      .getImage( shell.getDisplay(), getClass().getClassLoader(), "TRNEx.svg", ConstUI.LARGE_ICON_SIZE,
        ConstUI.LARGE_ICON_SIZE );
  }

  private void selectRepositoryTrans() {
    RepositoryObject repositoryObject = DialogHelper.selectRepositoryObject( "*.ktr", log );

    try {
      if ( repositoryObject != null ) {
        loadRepositoryTrans( repositoryObject.getName(), repositoryObject.getRepositoryDirectory() );
        String path = DialogUtils.getPath( transMeta.getRepositoryDirectory().getPath(),
          executorTransMeta.getRepositoryDirectory().getPath() );
        String fullPath = ( path.equals( "/" ) ? "/" : path + "/" ) + executorTransMeta.getName();
        wPath.setText( fullPath );
        specificationMethod = ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME;
      }
    } catch ( KettleException ke ) {
      new ErrorDialog( shell,
        BaseMessages.getString( PKG, "TransExecutorDialog.ErrorSelectingObject.DialogTitle" ),
        BaseMessages.getString( PKG, "TransExecutorDialog.ErrorSelectingObject.DialogMessage" ), ke );
    }
  }

  private void loadRepositoryTrans( String transName, RepositoryDirectoryInterface repdir ) throws KettleException {
    // Read the transformation...
    //
    executorTransMeta =
      repository.loadTransformation( transMeta.environmentSubstitute( transName ), repdir, null, false, null );
    executorTransMeta.clearChanged();
  }

  private void selectFileTrans() {
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
          shell, null, Const.STRING_TRANS_FILTER_EXT, Const.getTransformationFilterNames(),
          VfsFileChooserDialog.VFS_DIALOG_OPEN_FILE );
      if ( file == null ) {
        return;
      }
      String fileName = file.getName().toString();
      if ( fileName != null ) {
        loadFileTrans( fileName );
        if ( parentFolder != null && fileName.startsWith( parentFolder ) ) {
          fileName = fileName.replace( parentFolder, "${" + Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY + "}" );
        }
        wPath.setText( fileName );
        specificationMethod = ObjectLocationSpecificationMethod.FILENAME;
      }
    } catch ( IOException | KettleException e ) {
      new ErrorDialog( shell,
        BaseMessages.getString( PKG, "TransExecutorDialog.ErrorLoadingTransformation.DialogTitle" ),
        BaseMessages.getString( PKG, "TransExecutorDialog.ErrorLoadingTransformation.DialogMessage" ), e );
    }
  }

  private void loadFileTrans( String fname ) throws KettleException {
    executorTransMeta = new TransMeta( transMeta.environmentSubstitute( fname ), repository );
    executorTransMeta.clearChanged();
  }

  // Method is defined as package-protected in order to be accessible by unit tests
  void loadTransformation() throws KettleException {
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
        if ( !filename.endsWith( ".ktr" ) ) {
          filename = filename + ".ktr";
          wPath.setText( filename );
        }
        loadFileTrans( filename );
        break;
      case REPOSITORY_BY_NAME:
        if ( Utils.isEmpty( filename ) ) {
          return;
        }
        String transPath = transMeta.environmentSubstitute( filename );
        String realTransname = transPath;
        String realDirectory = "";
        int index = transPath.lastIndexOf( "/" );
        if ( index != -1 ) {
          realTransname = transPath.substring( index + 1 );
          realDirectory = transPath.substring( 0, index );
        }

        if ( Utils.isEmpty( realDirectory ) || Utils.isEmpty( realTransname ) ) {
          throw new KettleException(
            BaseMessages.getString( PKG, "TransExecutorDialog.Exception.NoValidMappingDetailsFound" ) );
        }
        RepositoryDirectoryInterface repdir = repository.findDirectory( realDirectory );
        if ( repdir == null ) {
          throw new KettleException( BaseMessages.getString(
            PKG, "TransExecutorDialog.Exception.UnableToFindRepositoryDirectory" ) );
        }
        loadRepositoryTrans( realTransname, repdir );
        break;
      default:
        break;
    }
  }

  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void getData() {
    specificationMethod = transExecutorMeta.getSpecificationMethod();
    switch ( specificationMethod ) {
      case FILENAME:
        wPath.setText( Const.NVL( transExecutorMeta.getFileName(), "" ) );
        break;
      case REPOSITORY_BY_NAME:
        String transname = transMeta.environmentSubstitute( Const.NVL( transExecutorMeta.getTransName(), "" ) );
        String directoryPath = transMeta.environmentSubstitute( Const.NVL( transExecutorMeta.getDirectoryPath(), "" ) );
        String fullPath = directoryPath.isEmpty() && !transname.isEmpty()
          ? Const.NVL( transExecutorMeta.getTransName(), "" )
          : Const.NVL( transExecutorMeta.getDirectoryPath(), "" ) + "/" + Const.NVL( transExecutorMeta.getTransName(), "" );
        wPath.setText( fullPath );
        break;
      case REPOSITORY_BY_REFERENCE:
        referenceObjectId = transExecutorMeta.getTransObjectId();
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
      wOutputRowsSource.setItems( prevSteps );

      String[] inputFields = transMeta.getPrevStepFields( stepMeta ).getFieldNames();
      parameterColumns[ 1 ].setComboValues( inputFields );
      wGroupField.setItems( inputFields );
    } catch ( Exception e ) {
      log.logError( "couldn't get previous step list", e );
    }

    wGroupSize.setText( Const.NVL( transExecutorMeta.getGroupSize(), "" ) );
    wGroupTime.setText( Const.NVL( transExecutorMeta.getGroupTime(), "" ) );
    wGroupField.setText( Const.NVL( transExecutorMeta.getGroupField(), "" ) );

    wExecutionResultTarget.setText( transExecutorMeta.getExecutionResultTargetStepMeta() == null ? ""
      : transExecutorMeta.getExecutionResultTargetStepMeta().getName() );
    tiExecutionTimeField.setText( FIELD_NAME, Const.NVL( transExecutorMeta.getExecutionTimeField(), "" ) );
    tiExecutionResultField.setText( FIELD_NAME, Const.NVL( transExecutorMeta.getExecutionResultField(), "" ) );
    tiExecutionNrErrorsField.setText( FIELD_NAME, Const.NVL( transExecutorMeta.getExecutionNrErrorsField(), "" ) );
    tiExecutionLinesReadField.setText( FIELD_NAME, Const.NVL( transExecutorMeta.getExecutionLinesReadField(), "" ) );
    tiExecutionLinesWrittenField
      .setText( FIELD_NAME, Const.NVL( transExecutorMeta.getExecutionLinesWrittenField(), "" ) );
    tiExecutionLinesInputField.setText( FIELD_NAME, Const.NVL( transExecutorMeta.getExecutionLinesInputField(), "" ) );
    tiExecutionLinesOutputField
      .setText( FIELD_NAME, Const.NVL( transExecutorMeta.getExecutionLinesOutputField(), "" ) );
    tiExecutionLinesRejectedField
      .setText( FIELD_NAME, Const.NVL( transExecutorMeta.getExecutionLinesRejectedField(), "" ) );
    tiExecutionLinesUpdatedField
      .setText( FIELD_NAME, Const.NVL( transExecutorMeta.getExecutionLinesUpdatedField(), "" ) );
    tiExecutionLinesDeletedField
      .setText( FIELD_NAME, Const.NVL( transExecutorMeta.getExecutionLinesDeletedField(), "" ) );
    tiExecutionFilesRetrievedField
      .setText( FIELD_NAME, Const.NVL( transExecutorMeta.getExecutionFilesRetrievedField(), "" ) );
    tiExecutionExitStatusField.setText( FIELD_NAME, Const.NVL( transExecutorMeta.getExecutionExitStatusField(), "" ) );
    tiExecutionLogTextField.setText( FIELD_NAME, Const.NVL( transExecutorMeta.getExecutionLogTextField(), "" ) );
    tiExecutionLogChannelIdField
      .setText( FIELD_NAME, Const.NVL( transExecutorMeta.getExecutionLogChannelIdField(), "" ) );

    if ( transExecutorMeta.getExecutorsOutputStepMeta() != null ) {
      executorOutputStep = transExecutorMeta.getExecutorsOutputStepMeta().getName();
    }

    // result files
    //
    wResultFilesTarget.setText( transExecutorMeta.getResultFilesTargetStepMeta() == null ? "" : transExecutorMeta
      .getResultFilesTargetStepMeta().getName() );
    wResultFileNameField.setText( Const.NVL( transExecutorMeta.getResultFilesFileNameField(), "" ) );

    // Result rows
    //
    wOutputRowsSource.setText( transExecutorMeta.getOutputRowsSourceStepMeta() == null ? "" : transExecutorMeta
      .getOutputRowsSourceStepMeta().getName() );
    for ( int i = 0; i < transExecutorMeta.getOutputRowsField().length; i++ ) {
      TableItem item = new TableItem( wOutputFields.table, SWT.NONE );
      item.setText( 1, Const.NVL( transExecutorMeta.getOutputRowsField()[ i ], "" ) );
      item.setText( 2, ValueMetaFactory.getValueMetaName( transExecutorMeta.getOutputRowsType()[ i ] ) );
      int length = transExecutorMeta.getOutputRowsLength()[ i ];
      item.setText( 3, length < 0 ? "" : Integer.toString( length ) );
      int precision = transExecutorMeta.getOutputRowsPrecision()[ i ];
      item.setText( 4, precision < 0 ? "" : Integer.toString( precision ) );
    }
    wOutputFields.removeEmptyRows();
    wOutputFields.setRowNums();
    wOutputFields.optWidth( true );

    wTabFolder.setSelection( 0 );

    try {
      loadTransformation();
    } catch ( Throwable t ) {
      // Ignore errors
    }

    setFlags();

    wStepname.selectAll();
    wStepname.setFocus();
  }

  private void addParametersTab() {
    CTabItem wParametersTab = new CTabItem( wTabFolder, SWT.NONE );
    wParametersTab.setText( BaseMessages.getString( PKG, "TransExecutorDialog.Parameters.Title" ) );
    wParametersTab.setToolTipText( BaseMessages.getString( PKG, "TransExecutorDialog.Parameters.Tooltip" ) );

    Composite wParametersComposite = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wParametersComposite );

    FormLayout parameterTabLayout = new FormLayout();
    parameterTabLayout.marginWidth = 15;
    parameterTabLayout.marginHeight = 15;
    wParametersComposite.setLayout( parameterTabLayout );

    // Add a button: get parameters
    //
    wGetParameters = new Button( wParametersComposite, SWT.PUSH );
    wGetParameters.setText( BaseMessages.getString( PKG, "TransExecutorDialog.Parameters.GetParameters" ) );
    props.setLook( wGetParameters );
    FormData fdGetParameters = new FormData();
    fdGetParameters.bottom = new FormAttachment( 100, 0 );
    fdGetParameters.right = new FormAttachment( 100, 0 );
    wGetParameters.setLayoutData( fdGetParameters );
    wGetParameters.setSelection( transExecutorMeta.getParameters().isInheritingAllVariables() );
    wGetParameters.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        getParametersFromTrans( null ); // null = force reload of data on disk
      }
    } );

    // Now add a table view with the 3 columns to specify: variable name, input field & optional static input
    //
    parameterColumns =
      new ColumnInfo[] {
        new ColumnInfo( BaseMessages.getString( PKG, "TransExecutorDialog.Parameters.column.Variable" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false, false ),
        new ColumnInfo( BaseMessages.getString( PKG, "TransExecutorDialog.Parameters.column.Field" ),
          ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] {}, false ),
        new ColumnInfo( BaseMessages.getString( PKG, "TransExecutorDialog.Parameters.column.Input" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false, false ), };
    parameterColumns[ 1 ].setUsingVariables( true );

    TransExecutorParameters parameters = transExecutorMeta.getParameters();
    wTransExecutorParameters =
      new TableView( transMeta, wParametersComposite, SWT.FULL_SELECTION | SWT.SINGLE | SWT.BORDER, parameterColumns,
        parameters.getVariable().length, false, lsModParams, props, false );
    props.setLook( wTransExecutorParameters );
    FormData fdTransExecutors = new FormData();
    fdTransExecutors.left = new FormAttachment( 0, 0 );
    fdTransExecutors.right = new FormAttachment( 100, 0 );
    fdTransExecutors.top = new FormAttachment( 0, 0 );
    fdTransExecutors.bottom = new FormAttachment( wGetParameters, -10 );
    wTransExecutorParameters.setLayoutData( fdTransExecutors );
    wTransExecutorParameters.getTable().addListener( SWT.Resize, new ColumnsResizer( 0, 33, 33, 33 ) );

    parameterTableHelper.setParameterTableView( wTransExecutorParameters );
    parameterTableHelper.setUpDisabledListeners();
    // Add disabled listeners to columns
    parameterColumns[0].setDisabledListener( parameterTableHelper.getVarDisabledListener() );
    parameterColumns[1].setDisabledListener( parameterTableHelper.getFieldDisabledListener() );
    parameterColumns[2].setDisabledListener( parameterTableHelper.getInputDisabledListener() );

    for ( int i = 0; i < parameters.getVariable().length; i++ ) {
      TableItem tableItem = wTransExecutorParameters.table.getItem( i );
      tableItem.setText( 1, Const.NVL( parameters.getVariable()[ i ], "" ) );
      tableItem.setText( 2, Const.NVL( parameters.getField()[ i ], "" ) );
      tableItem.setText( 3, Const.NVL( parameters.getInput()[ i ], "" ) );
      // Check disable listeners to shade fields gray
      parameterTableHelper.checkTableOnOpen( tableItem, i );
    }
    wTransExecutorParameters.setRowNums();
    wTransExecutorParameters.optWidth( true );

    // Add a checkbox: inherit all variables...
    //
    wInheritAll = new Button( wParametersComposite, SWT.CHECK );
    wInheritAll.setText( BaseMessages.getString( PKG, "TransExecutorDialog.Parameters.InheritAll" ) );
    props.setLook( wInheritAll );
    FormData fdInheritAll = new FormData();
    fdInheritAll.top = new FormAttachment( wTransExecutorParameters, 15 );
    fdInheritAll.left = new FormAttachment( 0, 0 );
    wInheritAll.setLayoutData( fdInheritAll );
    wInheritAll.setSelection( transExecutorMeta.getParameters().isInheritingAllVariables() );

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
    transExecutorMeta.setChanged();
    setFlags();
  }

  protected void getParametersFromTrans( TransMeta inputTransMeta ) {
    try {
      // Load the job in executorTransMeta
      //
      if ( inputTransMeta == null ) {
        loadTransformation();
        inputTransMeta = executorTransMeta;
      }

      String[] parameters = inputTransMeta.listParameters();
      for ( int i = 0; i < parameters.length; i++ ) {
        String name = parameters[ i ];
        String desc = inputTransMeta.getParameterDescription( name );

        TableItem item = new TableItem( wTransExecutorParameters.table, SWT.NONE );
        item.setText( 1, Const.NVL( name, "" ) );
        String str = inputTransMeta.getParameterDefault( name );
        str = ( str != null ? str : ( desc != null ? desc : "" ) );
        item.setText( 3, Const.NVL( str, "" ) );
      }
      wTransExecutorParameters.removeEmptyRows();
      wTransExecutorParameters.setRowNums();
      wTransExecutorParameters.optWidth( true );

    } catch ( Exception e ) {
      new ErrorDialog( shell, BaseMessages.getString( PKG, "TransExecutorDialog.ErrorLoadingSpecifiedTrans.Title" ),
        BaseMessages.getString( PKG, "TransExecutorDialog.ErrorLoadingSpecifiedTrans.Message" ), e );
    }

  }

  private void addRowGroupTab() {

    final CTabItem wTab = new CTabItem( wTabFolder, SWT.NONE );
    wTab.setText( BaseMessages.getString( PKG, "TransExecutorDialog.RowGroup.Title" ) );
    wTab.setToolTipText( BaseMessages.getString( PKG, "TransExecutorDialog.RowGroup.Tooltip" ) );

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
    wlGroupSize.setText( BaseMessages.getString( PKG, "TransExecutorDialog.GroupSize.Label" ) );
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
    wlGroupField.setText( BaseMessages.getString( PKG, "TransExecutorDialog.GroupField.Label" ) );
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
    wlGroupTime.setText( BaseMessages.getString( PKG, "TransExecutorDialog.GroupTime.Label" ) );
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
    wTab.setText( BaseMessages.getString( PKG, "TransExecutorDialog.ExecutionResults.Title" ) );
    wTab.setToolTipText( BaseMessages.getString( PKG, "TransExecutorDialog.ExecutionResults.Tooltip" ) );

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
    wlExecutionResultTarget.setText( BaseMessages.getString( PKG, "TransExecutorDialog.ExecutionResultTarget.Label" ) );
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
        new ColumnInfo( BaseMessages.getString( PKG, "TransExecutorMeta.ExecutionResults.FieldDescription.Label" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false, true ),
        new ColumnInfo( BaseMessages.getString( PKG, "TransExecutorMeta.ExecutionResults.FieldName.Label" ),
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
      .setText( FIELD_DESCRIPTION, BaseMessages.getString( PKG, "TransExecutorDialog.ExecutionTimeField.Label" ) );
    tiExecutionResultField
      .setText( FIELD_DESCRIPTION, BaseMessages.getString( PKG, "TransExecutorDialog.ExecutionResultField.Label" ) );
    tiExecutionNrErrorsField
      .setText( FIELD_DESCRIPTION, BaseMessages.getString( PKG, "TransExecutorDialog.ExecutionNrErrorsField.Label" ) );
    tiExecutionLinesReadField
      .setText( FIELD_DESCRIPTION, BaseMessages.getString( PKG, "TransExecutorDialog.ExecutionLinesReadField.Label" ) );
    tiExecutionLinesWrittenField.setText( FIELD_DESCRIPTION,
      BaseMessages.getString( PKG, "TransExecutorDialog.ExecutionLinesWrittenField.Label" ) );
    tiExecutionLinesInputField.setText( FIELD_DESCRIPTION,
      BaseMessages.getString( PKG, "TransExecutorDialog.ExecutionLinesInputField.Label" ) );
    tiExecutionLinesOutputField.setText( FIELD_DESCRIPTION,
      BaseMessages.getString( PKG, "TransExecutorDialog.ExecutionLinesOutputField.Label" ) );
    tiExecutionLinesRejectedField.setText( FIELD_DESCRIPTION,
      BaseMessages.getString( PKG, "TransExecutorDialog.ExecutionLinesRejectedField.Label" ) );
    tiExecutionLinesUpdatedField.setText( FIELD_DESCRIPTION,
      BaseMessages.getString( PKG, "TransExecutorDialog.ExecutionLinesUpdatedField.Label" ) );
    tiExecutionLinesDeletedField.setText( FIELD_DESCRIPTION,
      BaseMessages.getString( PKG, "TransExecutorDialog.ExecutionLinesDeletedField.Label" ) );
    tiExecutionFilesRetrievedField.setText( FIELD_DESCRIPTION,
      BaseMessages.getString( PKG, "TransExecutorDialog.ExecutionFilesRetrievedField.Label" ) );
    tiExecutionExitStatusField.setText( FIELD_DESCRIPTION,
      BaseMessages.getString( PKG, "TransExecutorDialog.ExecutionExitStatusField.Label" ) );
    tiExecutionLogTextField
      .setText( FIELD_DESCRIPTION, BaseMessages.getString( PKG, "TransExecutorDialog.ExecutionLogTextField.Label" ) );
    tiExecutionLogChannelIdField.setText( FIELD_DESCRIPTION,
      BaseMessages.getString( PKG, "TransExecutorDialog.ExecutionLogChannelIdField.Label" ) );

    wTransExecutorParameters.setRowNums();
    wTransExecutorParameters.optWidth( true );

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
    wTab.setText( BaseMessages.getString( PKG, "TransExecutorDialog.ResultFiles.Title" ) );
    wTab.setToolTipText( BaseMessages.getString( PKG, "TransExecutorDialog.ResultFiles.Tooltip" ) );

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
    wlResultFilesTarget.setText( BaseMessages.getString( PKG, "TransExecutorDialog.ResultFilesTarget.Label" ) );
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
    wlResultFileNameField.setText( BaseMessages.getString( PKG, "TransExecutorDialog.ResultFileNameField.Label" ) );
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
    wTab.setText( BaseMessages.getString( PKG, "TransExecutorDialog.ResultRows.Title" ) );
    wTab.setToolTipText( BaseMessages.getString( PKG, "TransExecutorDialog.ResultRows.Tooltip" ) );

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
    wlResultRowsTarget.setText( BaseMessages.getString( PKG, "TransExecutorDialog.OutputRowsSource.Label" ) );
    FormData fdlResultRowsTarget = new FormData();
    fdlResultRowsTarget.top = new FormAttachment( 0, 0 );
    fdlResultRowsTarget.left = new FormAttachment( 0, 0 ); // First one in the left
    wlResultRowsTarget.setLayoutData( fdlResultRowsTarget );

    wOutputRowsSource = new CCombo( wInputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wOutputRowsSource );
    wOutputRowsSource.addModifyListener( lsMod );
    FormData fdResultRowsTarget = new FormData();
    fdResultRowsTarget.width = 250;
    fdResultRowsTarget.top = new FormAttachment( wlResultRowsTarget, 5 );
    fdResultRowsTarget.left = new FormAttachment( 0, 0 ); // To the right
    wOutputRowsSource.setLayoutData( fdResultRowsTarget );

    wlOutputFields = new Label( wInputComposite, SWT.NONE );
    wlOutputFields.setText( BaseMessages.getString( PKG, "TransExecutorDialog.ResultFields.Label" ) );
    props.setLook( wlOutputFields );
    FormData fdlResultFields = new FormData();
    fdlResultFields.left = new FormAttachment( 0, 0 );
    fdlResultFields.top = new FormAttachment( wOutputRowsSource, 10 );
    wlOutputFields.setLayoutData( fdlResultFields );

    int nrRows = ( transExecutorMeta.getOutputRowsField() != null ? transExecutorMeta.getOutputRowsField().length : 1 );

    ColumnInfo[] ciResultFields =
      new ColumnInfo[] {
        new ColumnInfo( BaseMessages.getString( PKG, "TransExecutorDialog.ColumnInfo.Field" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false, false ),
        new ColumnInfo( BaseMessages.getString( PKG, "TransExecutorDialog.ColumnInfo.Type" ),
          ColumnInfo.COLUMN_TYPE_CCOMBO, ValueMetaFactory.getValueMetaNames() ),
        new ColumnInfo( BaseMessages.getString( PKG, "TransExecutorDialog.ColumnInfo.Length" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false ),
        new ColumnInfo( BaseMessages.getString( PKG, "TransExecutorDialog.ColumnInfo.Precision" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false ), };

    wOutputFields =
      new TableView( transMeta, wInputComposite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL
        | SWT.H_SCROLL, ciResultFields, nrRows, false, lsMod, props, false );

    FormData fdResultFields = new FormData();
    fdResultFields.left = new FormAttachment( 0, 0 );
    fdResultFields.top = new FormAttachment( wlOutputFields, 5 );
    fdResultFields.right = new FormAttachment( 100, 0 );
    fdResultFields.bottom = new FormAttachment( 100, 0 );
    wOutputFields.setLayoutData( fdResultFields );
    wOutputFields.getTable().addListener( SWT.Resize, new ColumnsResizer( 0, 25, 25, 25, 25 ) );

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
    if ( wlGroupSize == null || wlGroupSize == null || wlGroupField == null || wGroupField == null
      || wlGroupTime == null || wGroupTime == null ) {
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
    transExecutorMeta.setChanged( changed );
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
      loadTransformation();
    } catch ( KettleException e ) {
      new ErrorDialog( shell, BaseMessages.getString( PKG, "TransExecutorDialog.ErrorLoadingSpecifiedTrans.Title" ),
        BaseMessages.getString( PKG, "TransExecutorDialog.ErrorLoadingSpecifiedTrans.Message" ), e );
    }

    transExecutorMeta.setSpecificationMethod( specificationMethod );
    switch ( specificationMethod ) {
      case FILENAME:
        transExecutorMeta.setFileName( wPath.getText() );
        transExecutorMeta.setDirectoryPath( null );
        transExecutorMeta.setTransName( null );
        transExecutorMeta.setTransObjectId( null );
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
        transExecutorMeta.setDirectoryPath( directory );
        transExecutorMeta.setTransName( transName );
        transExecutorMeta.setFileName( null );
        transExecutorMeta.setTransObjectId( null );
        break;
      default:
        break;
    }

    // Load the information on the tabs, optionally do some
    // verifications...
    //
    collectInformation();

    // Set the input steps for input mappings
    transExecutorMeta.searchInfoAndTargetSteps( transMeta.getSteps() );

    transExecutorMeta.setChanged( true );

    dispose();
  }

  private void collectInformation() {
    // The parameters...
    //
    TransExecutorParameters parameters = transExecutorMeta.getParameters();

    int nrLines = wTransExecutorParameters.nrNonEmpty();
    String[] variables = new String[ nrLines ];
    String[] fields = new String[ nrLines ];
    String[] input = new String[ nrLines ];
    parameters.setVariable( variables );
    parameters.setField( fields );
    parameters.setInput( input );
    for ( int i = 0; i < nrLines; i++ ) {
      TableItem item = wTransExecutorParameters.getNonEmpty( i );
      variables[ i ] = item.getText( 1 );
      fields[ i ] = item.getText( 2 );
      input[ i ] = item.getText( 3 );
    }
    parameters.setInheritingAllVariables( wInheritAll.getSelection() );

    // The group definition
    //
    transExecutorMeta.setGroupSize( wGroupSize.getText() );
    transExecutorMeta.setGroupField( wGroupField.getText() );
    transExecutorMeta.setGroupTime( wGroupTime.getText() );

    transExecutorMeta.setExecutionResultTargetStep( wExecutionResultTarget.getText() );
    transExecutorMeta.setExecutionResultTargetStepMeta( transMeta.findStep( wExecutionResultTarget.getText() ) );
    transExecutorMeta.setExecutionTimeField( tiExecutionTimeField.getText( FIELD_NAME ) );
    transExecutorMeta.setExecutionResultField( tiExecutionResultField.getText( FIELD_NAME ) );
    transExecutorMeta.setExecutionNrErrorsField( tiExecutionNrErrorsField.getText( FIELD_NAME ) );
    transExecutorMeta.setExecutionLinesReadField( tiExecutionLinesReadField.getText( FIELD_NAME ) );
    transExecutorMeta.setExecutionLinesWrittenField( tiExecutionLinesWrittenField.getText( FIELD_NAME ) );
    transExecutorMeta.setExecutionLinesInputField( tiExecutionLinesInputField.getText( FIELD_NAME ) );
    transExecutorMeta.setExecutionLinesOutputField( tiExecutionLinesOutputField.getText( FIELD_NAME ) );
    transExecutorMeta.setExecutionLinesRejectedField( tiExecutionLinesRejectedField.getText( FIELD_NAME ) );
    transExecutorMeta.setExecutionLinesUpdatedField( tiExecutionLinesUpdatedField.getText( FIELD_NAME ) );
    transExecutorMeta.setExecutionLinesDeletedField( tiExecutionLinesDeletedField.getText( FIELD_NAME ) );
    transExecutorMeta.setExecutionFilesRetrievedField( tiExecutionFilesRetrievedField.getText( FIELD_NAME ) );
    transExecutorMeta.setExecutionExitStatusField( tiExecutionExitStatusField.getText( FIELD_NAME ) );
    transExecutorMeta.setExecutionLogTextField( tiExecutionLogTextField.getText( FIELD_NAME ) );
    transExecutorMeta.setExecutionLogChannelIdField( tiExecutionLogChannelIdField.getText( FIELD_NAME ) );

    transExecutorMeta.setResultFilesTargetStep( wResultFilesTarget.getText() );
    transExecutorMeta.setResultFilesTargetStepMeta( transMeta.findStep( wResultFilesTarget.getText() ) );
    transExecutorMeta.setResultFilesFileNameField( wResultFileNameField.getText() );

    if ( !Utils.isEmpty( executorOutputStep ) ) {
      transExecutorMeta.setExecutorsOutputStep( executorOutputStep );
      transExecutorMeta.setExecutorsOutputStepMeta( transMeta.findStep( executorOutputStep ) );
    }

    // Result row info
    //
    transExecutorMeta.setOutputRowsSourceStep( wOutputRowsSource.getText() );
    transExecutorMeta.setOutputRowsSourceStepMeta( transMeta.findStep( wOutputRowsSource.getText() ) );
    int nrFields = wOutputFields.nrNonEmpty();
    transExecutorMeta.setOutputRowsField( new String[ nrFields ] );
    transExecutorMeta.setOutputRowsType( new int[ nrFields ] );
    transExecutorMeta.setOutputRowsLength( new int[ nrFields ] );
    transExecutorMeta.setOutputRowsPrecision( new int[ nrFields ] );

    // CHECKSTYLE:Indentation:OFF
    for ( int i = 0; i < nrFields; i++ ) {
      TableItem item = wOutputFields.getNonEmpty( i );
      transExecutorMeta.getOutputRowsField()[ i ] = item.getText( 1 );
      transExecutorMeta.getOutputRowsType()[ i ] = ValueMetaFactory.getIdForValueMeta( item.getText( 2 ) );
      transExecutorMeta.getOutputRowsLength()[ i ] = Const.toInt( item.getText( 3 ), -1 );
      transExecutorMeta.getOutputRowsPrecision()[ i ] = Const.toInt( item.getText( 4 ), -1 );
    }

  }

  private void getByReferenceData( ObjectId transObjectId ) {
    try {
      RepositoryObject transInf = repository.getObjectInformation( transObjectId, RepositoryObjectType.TRANSFORMATION );
      String path = DialogUtils
        .getPath( transMeta.getRepositoryDirectory().getPath(), transInf.getRepositoryDirectory().getPath() );
      String fullPath =
        Const.NVL( path, "" ) + "/" + Const.NVL( transInf.getName(), "" );
      wPath.setText( fullPath );
    } catch ( KettleException e ) {
      new ErrorDialog( shell,
        BaseMessages.getString( PKG, "JobEntryTransDialog.Exception.UnableToReferenceObjectId.Title" ),
        BaseMessages.getString( PKG, "JobEntryTransDialog.Exception.UnableToReferenceObjectId.Message" ), e );
    }
  }


}
