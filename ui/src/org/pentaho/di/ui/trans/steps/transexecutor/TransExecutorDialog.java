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

package org.pentaho.di.ui.trans.steps.transexecutor;

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
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.core.gui.SpoonInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryElementMetaInterface;
import org.pentaho.di.repository.RepositoryObject;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.transexecutor.TransExecutorMeta;
import org.pentaho.di.trans.steps.transexecutor.TransExecutorParameters;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.repository.dialog.SelectObjectDialog;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.trans.dialog.TransDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.vfs.ui.VfsFileChooserDialog;

import java.io.IOException;
import java.util.Arrays;

public class TransExecutorDialog extends BaseStepDialog implements StepDialogInterface {
  private static Class<?> PKG = TransExecutorMeta.class; // for i18n purposes, needed by Translator2!!

  private TransExecutorMeta transExecutorMeta;

  private Group gTransGroup;

  // File
  //
  private Button radioFilename;
  private Button wbbFilename;
  private TextVar wFilename;

  // Repository by name
  //
  private Button radioByName;
  private TextVar wTransname, wDirectory;
  private Button wbTrans;

  // Repository by reference
  //
  private Button radioByReference;
  private Button wbByReference;
  private TextVar wByReference;

  // Edit the TransExecutor transformation in Spoon
  //
  private Button wNewTrans;
  private Button wEditTrans;

  private CTabFolder wTabFolder;

  private TransMeta executorTransMeta = null;

  protected boolean jobModified;

  private ModifyListener lsMod;

  private int middle;

  private int margin;

  private Button wInheritAll;

  private TableView wTransExecutorParameters;

  private Label wlGroupSize;
  private TextVar wGroupSize;
  private Label wlGroupField;
  private CCombo wGroupField;
  private Label wlGroupTime;
  private TextVar wGroupTime;

  private Label wlExecutionResultTarget;
  private Label wlExecutionTimeField;
  private Label wlExecutionResultField;
  private Label wlExecutionNrErrorsField;
  private Label wlExecutionLinesReadField;
  private Label wlExecutionLinesWrittenField;
  private Label wlExecutionLinesInputField;
  private Label wlExecutionLinesOutputField;
  private Label wlExecutionLinesRejectedField;
  private Label wlExecutionLinesUpdatedField;
  private Label wlExecutionLinesDeletedField;
  private Label wlExecutionFilesRetrievedField;
  private Label wlExecutionExitStatusField;
  private Label wlExecutionLogTextField;
  private Label wlExecutionLogChannelIdField;
  private CCombo wExecutionResultTarget;
  private TextVar wExecutionTimeField;
  private TextVar wExecutionResultField;
  private TextVar wExecutionNrErrorsField;
  private TextVar wExecutionLinesReadField;
  private TextVar wExecutionLinesWrittenField;
  private TextVar wExecutionLinesInputField;
  private TextVar wExecutionLinesOutputField;
  private TextVar wExecutionLinesRejectedField;
  private TextVar wExecutionLinesUpdatedField;
  private TextVar wExecutionLinesDeletedField;
  private TextVar wExecutionFilesRetrievedField;
  private TextVar wExecutionExitStatusField;
  private TextVar wExecutionLogTextField;
  private TextVar wExecutionLogChannelIdField;

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

    lsMod = new ModifyListener() {
      public void modifyText( ModifyEvent e ) {
        transExecutorMeta.setChanged();
        setFlags();
      }
    };
    changed = transExecutorMeta.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout( formLayout );
    shell.setText( BaseMessages.getString( PKG, "TransExecutorDialog.Shell.Title" ) );

    middle = props.getMiddlePct();
    margin = Const.MARGIN;

    // Stepname line
    wlStepname = new Label( shell, SWT.RIGHT );
    wlStepname.setText( BaseMessages.getString( PKG, "TransExecutorDialog.Stepname.Label" ) );
    props.setLook( wlStepname );
    fdlStepname = new FormData();
    fdlStepname.left = new FormAttachment( 0, 0 );
    fdlStepname.right = new FormAttachment( middle, -margin );
    fdlStepname.top = new FormAttachment( 0, margin );
    wlStepname.setLayoutData( fdlStepname );
    wStepname = new Text( shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wStepname.setText( stepname );
    props.setLook( wStepname );
    wStepname.addModifyListener( lsMod );
    fdStepname = new FormData();
    fdStepname.left = new FormAttachment( middle, 0 );
    fdStepname.top = new FormAttachment( 0, margin );
    fdStepname.right = new FormAttachment( 100, 0 );
    wStepname.setLayoutData( fdStepname );

    // Show a group with 2 main options: a transformation in the repository
    // or on file
    //

    // //////////////////////////////////////////////////
    // The key creation box
    // //////////////////////////////////////////////////
    //
    gTransGroup = new Group( shell, SWT.SHADOW_ETCHED_IN );
    gTransGroup.setText( BaseMessages.getString( PKG, "TransExecutorDialog.TransGroup.Label" ) );
    gTransGroup.setBackground( shell.getBackground() ); // the default looks
    // ugly
    FormLayout transGroupLayout = new FormLayout();
    transGroupLayout.marginLeft = margin * 2;
    transGroupLayout.marginTop = margin * 2;
    transGroupLayout.marginRight = margin * 2;
    transGroupLayout.marginBottom = margin * 2;
    gTransGroup.setLayout( transGroupLayout );

    // Radio button: The TransExecutor is in a file
    //
    radioFilename = new Button( gTransGroup, SWT.RADIO );
    props.setLook( radioFilename );
    radioFilename.setSelection( false );
    radioFilename.setText( BaseMessages.getString( PKG, "TransExecutorDialog.RadioFile.Label" ) );
    radioFilename.setToolTipText( BaseMessages.getString( PKG, "TransExecutorDialog.RadioFile.Tooltip", Const.CR ) );
    FormData fdFileRadio = new FormData();
    fdFileRadio.left = new FormAttachment( 0, 0 );
    fdFileRadio.right = new FormAttachment( 100, 0 );
    fdFileRadio.top = new FormAttachment( 0, 0 );
    radioFilename.setLayoutData( fdFileRadio );
    radioFilename.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        specificationMethod = ObjectLocationSpecificationMethod.FILENAME;
        setRadioButtons();
      }
    } );

    wbbFilename = new Button( gTransGroup, SWT.PUSH | SWT.CENTER ); // Browse
    props.setLook( wbbFilename );
    wbbFilename.setText( BaseMessages.getString( PKG, "System.Button.Browse" ) );
    wbbFilename.setToolTipText( BaseMessages.getString( PKG, "System.Tooltip.BrowseForFileOrDirAndAdd" ) );
    FormData fdbFilename = new FormData();
    fdbFilename.right = new FormAttachment( 100, 0 );
    fdbFilename.top = new FormAttachment( radioFilename, margin );
    wbbFilename.setLayoutData( fdbFilename );
    wbbFilename.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        selectFileTrans();
      }
    } );

    wFilename = new TextVar( transMeta, gTransGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wFilename );
    wFilename.addModifyListener( lsMod );
    FormData fdFilename = new FormData();
    fdFilename.left = new FormAttachment( 0, 25 );
    fdFilename.right = new FormAttachment( wbbFilename, -margin );
    fdFilename.top = new FormAttachment( wbbFilename, 0, SWT.CENTER );
    wFilename.setLayoutData( fdFilename );
    wFilename.addModifyListener( new ModifyListener() {
      public void modifyText( ModifyEvent e ) {
        specificationMethod = ObjectLocationSpecificationMethod.FILENAME;
        setRadioButtons();
      }
    } );

    // Radio button: The TransExecutor is in the repository
    //
    radioByName = new Button( gTransGroup, SWT.RADIO );
    props.setLook( radioByName );
    radioByName.setSelection( false );
    radioByName.setText( BaseMessages.getString( PKG, "TransExecutorDialog.RadioRep.Label" ) );
    radioByName.setToolTipText( BaseMessages.getString( PKG, "TransExecutorDialog.RadioRep.Tooltip", Const.CR ) );
    FormData fdRepRadio = new FormData();
    fdRepRadio.left = new FormAttachment( 0, 0 );
    fdRepRadio.right = new FormAttachment( 100, 0 );
    fdRepRadio.top = new FormAttachment( wbbFilename, 2 * margin );
    radioByName.setLayoutData( fdRepRadio );
    radioByName.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        specificationMethod = ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME;
        setRadioButtons();
      }
    } );
    wbTrans = new Button( gTransGroup, SWT.PUSH | SWT.CENTER ); // Browse
    props.setLook( wbTrans );
    wbTrans.setText( BaseMessages.getString( PKG, "TransExecutorDialog.Select.Button" ) );
    wbTrans.setToolTipText( BaseMessages.getString( PKG, "System.Tooltip.BrowseForFileOrDirAndAdd" ) );
    FormData fdbTrans = new FormData();
    fdbTrans.right = new FormAttachment( 100, 0 );
    fdbTrans.top = new FormAttachment( radioByName, 2 * margin );
    wbTrans.setLayoutData( fdbTrans );
    wbTrans.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        selectRepositoryTrans();
      }
    } );

    wDirectory = new TextVar( transMeta, gTransGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wDirectory );
    wDirectory.addModifyListener( lsMod );
    FormData fdTransDir = new FormData();
    fdTransDir.left = new FormAttachment( middle + ( 100 - middle ) / 2, 0 );
    fdTransDir.right = new FormAttachment( wbTrans, -margin );
    fdTransDir.top = new FormAttachment( wbTrans, 0, SWT.CENTER );
    wDirectory.setLayoutData( fdTransDir );
    wDirectory.addModifyListener( new ModifyListener() {
      public void modifyText( ModifyEvent e ) {
        specificationMethod = ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME;
        setRadioButtons();
      }
    } );

    wTransname = new TextVar( transMeta, gTransGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wTransname );
    wTransname.addModifyListener( lsMod );
    FormData fdTransName = new FormData();
    fdTransName.left = new FormAttachment( 0, 25 );
    fdTransName.right = new FormAttachment( wDirectory, -margin );
    fdTransName.top = new FormAttachment( wbTrans, 0, SWT.CENTER );
    wTransname.setLayoutData( fdTransName );
    wTransname.addModifyListener( new ModifyListener() {
      public void modifyText( ModifyEvent e ) {
        specificationMethod = ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME;
        setRadioButtons();
      }
    } );

    // Radio button: The TransExecutor is in the repository
    //
    radioByReference = new Button( gTransGroup, SWT.RADIO );
    props.setLook( radioByReference );
    radioByReference.setSelection( false );
    radioByReference.setText( BaseMessages.getString( PKG, "TransExecutorDialog.RadioRepByReference.Label" ) );
    radioByReference.setToolTipText( BaseMessages.getString( PKG, "TransExecutorDialog.RadioRepByReference.Tooltip",
      Const.CR ) );
    FormData fdRadioByReference = new FormData();
    fdRadioByReference.left = new FormAttachment( 0, 0 );
    fdRadioByReference.right = new FormAttachment( 100, 0 );
    fdRadioByReference.top = new FormAttachment( wTransname, 2 * margin );
    radioByReference.setLayoutData( fdRadioByReference );
    radioByReference.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        specificationMethod = ObjectLocationSpecificationMethod.REPOSITORY_BY_REFERENCE;
        setRadioButtons();
      }
    } );

    wbByReference = new Button( gTransGroup, SWT.PUSH | SWT.CENTER );
    props.setLook( wbByReference );
    wbByReference.setImage( GUIResource.getInstance().getImageTransGraph() );
    wbByReference.setToolTipText( BaseMessages.getString( PKG, "TransExecutorDialog.SelectTrans.Tooltip" ) );
    FormData fdbByReference = new FormData();
    fdbByReference.top = new FormAttachment( radioByReference, margin );
    fdbByReference.right = new FormAttachment( 100, 0 );
    wbByReference.setLayoutData( fdbByReference );
    wbByReference.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        selectTransByReference();
      }
    } );

    wByReference = new TextVar( transMeta, gTransGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER | SWT.READ_ONLY );
    props.setLook( wByReference );
    wByReference.addModifyListener( lsMod );
    FormData fdByReference = new FormData();
    fdByReference.top = new FormAttachment( radioByReference, margin );
    fdByReference.left = new FormAttachment( 0, 25 );
    fdByReference.right = new FormAttachment( wbByReference, -margin );
    wByReference.setLayoutData( fdByReference );
    wByReference.addModifyListener( new ModifyListener() {
      public void modifyText( ModifyEvent e ) {
        specificationMethod = ObjectLocationSpecificationMethod.REPOSITORY_BY_REFERENCE;
        setRadioButtons();
      }
    } );

    wNewTrans = new Button( gTransGroup, SWT.PUSH | SWT.CENTER ); // Browse
    props.setLook( wNewTrans );
    wNewTrans.setText( BaseMessages.getString( PKG, "TransExecutorDialog.New.Button" ) );
    FormData fdNewTrans = new FormData();
    fdNewTrans.left = new FormAttachment( 0, 0 );
    fdNewTrans.top = new FormAttachment( wByReference, 3 * margin );
    wNewTrans.setLayoutData( fdNewTrans );
    wNewTrans.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        newTransformation();
      }
    } );

    wEditTrans = new Button( gTransGroup, SWT.PUSH | SWT.CENTER ); // Browse
    props.setLook( wEditTrans );
    wEditTrans.setText( BaseMessages.getString( PKG, "TransExecutorDialog.Edit.Button" ) );
    wEditTrans.setToolTipText( BaseMessages.getString( PKG, "System.Tooltip.BrowseForFileOrDirAndAdd" ) );
    FormData fdEditTrans = new FormData();
    fdEditTrans.left = new FormAttachment( wNewTrans, 2 * margin );
    fdEditTrans.top = new FormAttachment( wByReference, 3 * margin );
    wEditTrans.setLayoutData( fdEditTrans );
    wEditTrans.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        editTrans();
      }
    } );

    FormData fdTransGroup = new FormData();
    fdTransGroup.left = new FormAttachment( 0, 0 );
    fdTransGroup.top = new FormAttachment( wStepname, 2 * margin );
    fdTransGroup.right = new FormAttachment( 100, 0 );
    // fdTransGroup.bottom = new FormAttachment(wStepname, 350);
    gTransGroup.setLayoutData( fdTransGroup );
    Control lastControl = gTransGroup;

    //
    // Add a tab folder for the parameters and various input and output
    // streams
    //
    wTabFolder = new CTabFolder( shell, SWT.BORDER );
    props.setLook( wTabFolder, Props.WIDGET_STYLE_TAB );
    wTabFolder.setSimple( false );
    wTabFolder.setUnselectedCloseVisible( true );

    FormData fdTabFolder = new FormData();
    fdTabFolder.left = new FormAttachment( 0, 0 );
    fdTabFolder.right = new FormAttachment( 100, 0 );
    fdTabFolder.top = new FormAttachment( lastControl, margin * 2 );
    fdTabFolder.bottom = new FormAttachment( 100, -75 );
    wTabFolder.setLayoutData( fdTabFolder );

    // Some buttons
    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );

    setButtonPositions( new Button[] { wOK, wCancel }, margin, null );

    // Add the tabs...
    //
    addParametersTab();
    addRowGroupTab();
    addExecutionResultTab();
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
    wFilename.addSelectionListener( lsDef );
    wTransname.addSelectionListener( lsDef );
    wExecutionTimeField.addSelectionListener( lsDef );
    wExecutionResultField.addSelectionListener( lsDef );
    wExecutionNrErrorsField.addSelectionListener( lsDef );
    wExecutionLinesReadField.addSelectionListener( lsDef );
    wExecutionLinesWrittenField.addSelectionListener( lsDef );
    wExecutionLinesInputField.addSelectionListener( lsDef );
    wExecutionLinesOutputField.addSelectionListener( lsDef );
    wExecutionLinesRejectedField.addSelectionListener( lsDef );
    wExecutionLinesUpdatedField.addSelectionListener( lsDef );
    wExecutionLinesDeletedField.addSelectionListener( lsDef );
    wExecutionFilesRetrievedField.addSelectionListener( lsDef );
    wExecutionExitStatusField.addSelectionListener( lsDef );
    wExecutionLogTextField.addSelectionListener( lsDef );
    wExecutionLogChannelIdField.addSelectionListener( lsDef );
    wResultFileNameField.addSelectionListener( lsDef );

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    // Set the shell size, based upon previous time...
    setSize();

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

  protected void selectTransByReference() {
    if ( repository != null ) {
      SelectObjectDialog sod = getSelectObjectDialog( shell, repository, true, false );
      sod.open();
      RepositoryElementMetaInterface repositoryObject = sod.getRepositoryObject();
      if ( repositoryObject != null ) {
        specificationMethod = ObjectLocationSpecificationMethod.REPOSITORY_BY_REFERENCE;
        updateByReferenceField( repositoryObject );
        referenceObjectId = repositoryObject.getObjectId();
        setRadioButtons();
      }
    }
  }

  void selectRepositoryTrans() {
    try {
      SelectObjectDialog sod = getSelectObjectDialog( shell, repository, true, false );
      String transName = sod.open();
      RepositoryDirectoryInterface repdir = sod.getDirectory();
      if ( transName != null && repdir != null ) {
        loadRepositoryTrans( transName, repdir );
        wTransname.setText( executorTransMeta.getName() );
        wDirectory.setText( executorTransMeta.getRepositoryDirectory().getPath() );
        wFilename.setText( "" );
        radioByName.setSelection( true );
        radioFilename.setSelection( false );
        specificationMethod = ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME;
        setRadioButtons();
      }
    } catch ( KettleException ke ) {
      new ErrorDialog( shell, BaseMessages.getString( PKG, "TransExecutorDialog.ErrorSelectingObject.DialogTitle" ),
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
    String curFile = wFilename.getText();
    FileObject root = null;

    try {
      root = KettleVFS.getFileObject( curFile != null ? curFile : Const.getUserHomeDirectory() );

      VfsFileChooserDialog vfsFileChooser = Spoon.getInstance().getVfsFileChooserDialog( root.getParent(), root );
      FileObject file =
        vfsFileChooser.open( shell, null, Const.STRING_TRANS_FILTER_EXT, Const.getTransformationFilterNames(),
          VfsFileChooserDialog.VFS_DIALOG_OPEN_FILE );
      if ( file == null ) {
        return;
      }
      String fname = null;

      fname = file.getURL().getFile();

      if ( fname != null ) {

        loadFileTrans( fname );
        wFilename.setText( fname );
        wTransname.setText( Const.NVL( executorTransMeta.getName(), "" ) );
        wDirectory.setText( "" );
        specificationMethod = ObjectLocationSpecificationMethod.FILENAME;
        setRadioButtons();
      }
    } catch ( IOException e ) {
      new ErrorDialog( shell, BaseMessages.getString( PKG, "TransExecutorDialog.ErrorLoadingTrans.DialogTitle" ),
        BaseMessages.getString( PKG, "TransExecutorDialog.ErrorLoadingTrans.DialogMessage" ), e );
    } catch ( KettleException e ) {
      new ErrorDialog( shell, BaseMessages.getString( PKG, "TransExecutorDialog.ErrorLoadingTrans.DialogTitle" ),
        BaseMessages.getString( PKG, "TransExecutorDialog.ErrorLoadingTrans.DialogMessage" ), e );
    }
  }

  private void loadFileTrans( String fname ) throws KettleException {
    executorTransMeta = new TransMeta( transMeta.environmentSubstitute( fname ), repository );
    executorTransMeta.clearChanged();
  }

  private void editTrans() {
    // Load the transformation again to make sure it's still there and
    // refreshed
    // It's an extra check to make sure it's still OK...
    //
    try {
      loadTrans();

      // If we're still here, jobExecutorMeta is valid.
      //
      SpoonInterface spoon = SpoonFactory.getInstance();
      if ( spoon != null ) {
        spoon.addTransGraph( executorTransMeta );
      }
    } catch ( KettleException e ) {
      new ErrorDialog( shell, BaseMessages.getString( PKG, "TransExecutorDialog.ErrorShowingTrans.Title" ),
        BaseMessages.getString( PKG, "TransExecutorDialog.ErrorShowingTrans.Message" ), e );
    }
  }

  private void loadTrans() throws KettleException {
    switch ( specificationMethod ) {
      case FILENAME:
        loadFileTrans( wFilename.getText() );
        break;
      case REPOSITORY_BY_NAME:
        String realDirectory = transMeta.environmentSubstitute( wDirectory.getText() );
        String realTransname = transMeta.environmentSubstitute( wTransname.getText() );

        if ( Const.isEmpty( realDirectory ) || Const.isEmpty( realTransname ) ) {
          throw new KettleException( BaseMessages.getString( PKG,
            "TransExecutorDialog.Exception.NoValidTransExecutorDetailsFound" ) );
        }
        RepositoryDirectoryInterface repdir = repository.findDirectory( realDirectory );
        if ( repdir == null ) {
          throw new KettleException( BaseMessages.getString( PKG,
            "TransExecutorDialog.Exception.UnableToFindRepositoryDirectory)" ) );
        }
        loadRepositoryTrans( realTransname, repdir );
        break;
      case REPOSITORY_BY_REFERENCE:
        if ( referenceObjectId == null ) {
          throw new KettleException( BaseMessages.getString( PKG,
            "TransExecutorDialog.Exception.ReferencedTransformationIdIsNull" ) );
        }
        executorTransMeta = repository.loadTransformation( referenceObjectId, null ); // load the last version
        executorTransMeta.clearChanged();
        break;
      default:
        break;
    }
  }

  public void setActive() {
    boolean supportsReferences =
      repository != null && repository.getRepositoryMeta().getRepositoryCapabilities().supportsReferences();

    radioByName.setEnabled( repository != null );
    radioByReference.setEnabled( repository != null && supportsReferences );
    wFilename.setEnabled( radioFilename.getSelection() );
    wbbFilename.setEnabled( radioFilename.getSelection() );
    wTransname.setEnabled( repository != null && radioByName.getSelection() );

    wDirectory.setEnabled( repository != null && radioByName.getSelection() );

    wbTrans.setEnabled( repository != null && radioByName.getSelection() );

    wByReference.setEnabled( repository != null && radioByReference.getSelection() && supportsReferences );
    wbByReference.setEnabled( repository != null && radioByReference.getSelection() && supportsReferences );
  }

  protected void setRadioButtons() {
    radioFilename.setSelection( specificationMethod == ObjectLocationSpecificationMethod.FILENAME );
    radioByName.setSelection( specificationMethod == ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME );
    radioByReference.setSelection( specificationMethod == ObjectLocationSpecificationMethod.REPOSITORY_BY_REFERENCE );
    setActive();
  }

  private void updateByReferenceField( RepositoryElementMetaInterface element ) {
    String path = getPathOf( element );
    if ( path == null ) {
      path = "";
    }
    wByReference.setText( path );
  }

  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void getData() {
    specificationMethod = transExecutorMeta.getSpecificationMethod();
    switch ( specificationMethod ) {
      case FILENAME:
        wFilename.setText( Const.NVL( transExecutorMeta.getFileName(), "" ) );
        break;
      case REPOSITORY_BY_NAME:
        wDirectory.setText( Const.NVL( transExecutorMeta.getDirectoryPath(), "" ) );
        wTransname.setText( Const.NVL( transExecutorMeta.getTransName(), "" ) );
        break;
      case REPOSITORY_BY_REFERENCE:
        wByReference.setText( "" );
        if ( transExecutorMeta.getTransObjectId() != null ) {
          this.referenceObjectId = transExecutorMeta.getTransObjectId();
          getByReferenceData( transExecutorMeta.getTransObjectId() );
        }
        break;
      default:
        break;
    }
    setRadioButtons();

    // TODO: throw in a separate thread.
    //
    try {
      String[] prevSteps = transMeta.getStepNames();
      Arrays.sort( prevSteps );
      wExecutionResultTarget.setItems( prevSteps );
      wResultFilesTarget.setItems( prevSteps );
      wOutputRowsSource.setItems( prevSteps );

      String[] inputFields = transMeta.getPrevStepFields( stepMeta ).getFieldNames();
      parameterColumns[1].setComboValues( inputFields );
      wGroupField.setItems( inputFields );
    } catch ( Exception e ) {
      log.logError( "couldn't get previous step list", e );
    }

    wGroupSize.setText( Const.NVL( transExecutorMeta.getGroupSize(), "" ) );
    wGroupTime.setText( Const.NVL( transExecutorMeta.getGroupTime(), "" ) );
    wGroupField.setText( Const.NVL( transExecutorMeta.getGroupField(), "" ) );

    wExecutionResultTarget.setText( transExecutorMeta.getExecutionResultTargetStepMeta() == null ? ""
      : transExecutorMeta.getExecutionResultTargetStepMeta().getName() );
    wExecutionTimeField.setText( Const.NVL( transExecutorMeta.getExecutionTimeField(), "" ) );
    wExecutionResultField.setText( Const.NVL( transExecutorMeta.getExecutionResultField(), "" ) );
    wExecutionNrErrorsField.setText( Const.NVL( transExecutorMeta.getExecutionNrErrorsField(), "" ) );
    wExecutionLinesReadField.setText( Const.NVL( transExecutorMeta.getExecutionLinesReadField(), "" ) );
    wExecutionLinesWrittenField.setText( Const.NVL( transExecutorMeta.getExecutionLinesWrittenField(), "" ) );
    wExecutionLinesInputField.setText( Const.NVL( transExecutorMeta.getExecutionLinesInputField(), "" ) );
    wExecutionLinesOutputField.setText( Const.NVL( transExecutorMeta.getExecutionLinesOutputField(), "" ) );
    wExecutionLinesRejectedField.setText( Const.NVL( transExecutorMeta.getExecutionLinesRejectedField(), "" ) );
    wExecutionLinesUpdatedField.setText( Const.NVL( transExecutorMeta.getExecutionLinesUpdatedField(), "" ) );
    wExecutionLinesDeletedField.setText( Const.NVL( transExecutorMeta.getExecutionLinesDeletedField(), "" ) );
    wExecutionFilesRetrievedField.setText( Const.NVL( transExecutorMeta.getExecutionFilesRetrievedField(), "" ) );
    wExecutionExitStatusField.setText( Const.NVL( transExecutorMeta.getExecutionExitStatusField(), "" ) );
    wExecutionLogTextField.setText( Const.NVL( transExecutorMeta.getExecutionLogTextField(), "" ) );
    wExecutionLogChannelIdField.setText( Const.NVL( transExecutorMeta.getExecutionLogChannelIdField(), "" ) );

    executorOutputStep = transExecutorMeta.getExecutorsOutputStep();

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
      item.setText( 1, Const.NVL( transExecutorMeta.getOutputRowsField()[i], "" ) );
      item.setText( 2, ValueMeta.getTypeDesc( transExecutorMeta.getOutputRowsType()[i] ) );
      int length = transExecutorMeta.getOutputRowsLength()[i];
      item.setText( 3, length < 0 ? "" : Integer.toString( length ) );
      int precision = transExecutorMeta.getOutputRowsPrecision()[i];
      item.setText( 4, precision < 0 ? "" : Integer.toString( precision ) );
    }
    wOutputFields.removeEmptyRows();
    wOutputFields.setRowNums();
    wOutputFields.optWidth( true );

    wTabFolder.setSelection( 0 );

    try {
      loadTrans();
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
    parameterTabLayout.marginWidth = Const.FORM_MARGIN;
    parameterTabLayout.marginHeight = Const.FORM_MARGIN;
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

    // Add a checkbox: inherit all variables...
    //
    wInheritAll = new Button( wParametersComposite, SWT.CHECK );
    wInheritAll.setText( BaseMessages.getString( PKG, "TransExecutorDialog.Parameters.InheritAll" ) );
    props.setLook( wInheritAll );
    FormData fdInheritAll = new FormData();
    fdInheritAll.bottom = new FormAttachment( 100, 0 );
    fdInheritAll.left = new FormAttachment( 0, 0 );
    fdInheritAll.right = new FormAttachment( wGetParameters, -margin );
    wInheritAll.setLayoutData( fdInheritAll );
    wInheritAll.setSelection( transExecutorMeta.getParameters().isInheritingAllVariables() );

    // Now add a table view with the 3 columns to specify: variable name, input field & optional static input
    //
    parameterColumns =
      new ColumnInfo[] {
        new ColumnInfo( BaseMessages.getString( PKG, "TransExecutorDialog.Parameters.column.Variable" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false, false ),
        new ColumnInfo( BaseMessages.getString( PKG, "TransExecutorDialog.Parameters.column.Field" ),
          ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { }, false ),
        new ColumnInfo( BaseMessages.getString( PKG, "TransExecutorDialog.Parameters.column.Input" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false, false ), };
    parameterColumns[1].setUsingVariables( true );

    TransExecutorParameters parameters = transExecutorMeta.getParameters();
    wTransExecutorParameters =
      new TableView( transMeta, wParametersComposite, SWT.FULL_SELECTION | SWT.SINGLE | SWT.BORDER, parameterColumns,
        parameters.getVariable().length, lsMod, props );
    props.setLook( wTransExecutorParameters );
    FormData fdTransExecutors = new FormData();
    fdTransExecutors.left = new FormAttachment( 0, 0 );
    fdTransExecutors.right = new FormAttachment( 100, 0 );
    fdTransExecutors.top = new FormAttachment( 0, 0 );
    fdTransExecutors.bottom = new FormAttachment( wInheritAll, -margin * 2 );
    wTransExecutorParameters.setLayoutData( fdTransExecutors );

    for ( int i = 0; i < parameters.getVariable().length; i++ ) {
      TableItem tableItem = wTransExecutorParameters.table.getItem( i );
      tableItem.setText( 1, Const.NVL( parameters.getVariable()[i], "" ) );
      tableItem.setText( 2, Const.NVL( parameters.getField()[i], "" ) );
      tableItem.setText( 3, Const.NVL( parameters.getInput()[i], "" ) );
    }
    wTransExecutorParameters.setRowNums();
    wTransExecutorParameters.optWidth( true );

    FormData fdParametersComposite = new FormData();
    fdParametersComposite.left = new FormAttachment( 0, 0 );
    fdParametersComposite.top = new FormAttachment( 0, 0 );
    fdParametersComposite.right = new FormAttachment( 100, 0 );
    fdParametersComposite.bottom = new FormAttachment( 100, 0 );
    wParametersComposite.setLayoutData( fdParametersComposite );

    wParametersComposite.layout();
    wParametersTab.setControl( wParametersComposite );
  }

  protected void getParametersFromTrans( TransMeta inputTransMeta ) {
    try {
      // Load the job in executorTransMeta
      //
      if ( inputTransMeta == null ) {
        loadTrans();
        inputTransMeta = executorTransMeta;
      }

      String[] parameters = inputTransMeta.listParameters();
      for ( int i = 0; i < parameters.length; i++ ) {
        String name = parameters[i];
        String desc = inputTransMeta.getParameterDescription( name );

        TableItem item = new TableItem( wTransExecutorParameters.table, SWT.NONE );
        item.setText( 1, Const.NVL( name, "" ) );
        item.setText( 3, Const.NVL( desc, "" ) );
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
    tabLayout.marginWidth = Const.FORM_MARGIN;
    tabLayout.marginHeight = Const.FORM_MARGIN;
    wInputComposite.setLayout( tabLayout );

    // Group size
    //
    wlGroupSize = new Label( wInputComposite, SWT.RIGHT );
    props.setLook( wlGroupSize );
    wlGroupSize.setText( BaseMessages.getString( PKG, "TransExecutorDialog.GroupSize.Label" ) );
    FormData fdlGroupSize = new FormData();
    fdlGroupSize.top = new FormAttachment( 0, 0 );
    fdlGroupSize.left = new FormAttachment( 0, 0 ); // First one in the left
    fdlGroupSize.right = new FormAttachment( middle, -margin );
    wlGroupSize.setLayoutData( fdlGroupSize );
    wGroupSize = new TextVar( transMeta, wInputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wGroupSize );
    wGroupSize.addModifyListener( lsMod );
    FormData fdGroupSize = new FormData();
    fdGroupSize.top = new FormAttachment( 0, 0 );
    fdGroupSize.left = new FormAttachment( middle, 0 ); // To the right of
    fdGroupSize.right = new FormAttachment( 100, 0 );
    wGroupSize.setLayoutData( fdGroupSize );
    Control lastControl = wGroupSize;

    // Group field
    //
    wlGroupField = new Label( wInputComposite, SWT.RIGHT );
    props.setLook( wlGroupField );
    wlGroupField.setText( BaseMessages.getString( PKG, "TransExecutorDialog.GroupField.Label" ) );
    FormData fdlGroupField = new FormData();
    fdlGroupField.top = new FormAttachment( lastControl, margin );
    fdlGroupField.left = new FormAttachment( 0, 0 ); // First one in the left
    fdlGroupField.right = new FormAttachment( middle, -margin );
    wlGroupField.setLayoutData( fdlGroupField );
    wGroupField = new CCombo( wInputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wGroupField );
    wGroupField.addModifyListener( lsMod );
    FormData fdGroupField = new FormData();
    fdGroupField.top = new FormAttachment( lastControl, margin );
    fdGroupField.left = new FormAttachment( middle, 0 ); // To the right of
    fdGroupField.right = new FormAttachment( 100, 0 );
    wGroupField.setLayoutData( fdGroupField );
    lastControl = wGroupField;

    // Group time
    //
    wlGroupTime = new Label( wInputComposite, SWT.RIGHT );
    props.setLook( wlGroupTime );
    wlGroupTime.setText( BaseMessages.getString( PKG, "TransExecutorDialog.GroupTime.Label" ) );
    FormData fdlGroupTime = new FormData();
    fdlGroupTime.top = new FormAttachment( lastControl, margin );
    fdlGroupTime.left = new FormAttachment( 0, 0 ); // First one in the left
    fdlGroupTime.right = new FormAttachment( middle, -margin );
    wlGroupTime.setLayoutData( fdlGroupTime );
    wGroupTime = new TextVar( transMeta, wInputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wGroupTime );
    wGroupTime.addModifyListener( lsMod );
    FormData fdGroupTime = new FormData();
    fdGroupTime.top = new FormAttachment( lastControl, margin );
    fdGroupTime.left = new FormAttachment( middle, 0 ); // To the right of
    fdGroupTime.right = new FormAttachment( 100, 0 );
    wGroupTime.setLayoutData( fdGroupTime );
    // lastControl = wGroupTime;

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
    tabLayout.marginWidth = Const.FORM_MARGIN;
    tabLayout.marginHeight = Const.FORM_MARGIN;
    wInputComposite.setLayout( tabLayout );

    wlExecutionResultTarget = new Label( wInputComposite, SWT.RIGHT );
    props.setLook( wlExecutionResultTarget );
    wlExecutionResultTarget.setText( BaseMessages.getString( PKG, "TransExecutorDialog.ExecutionResultTarget.Label" ) );
    FormData fdlExecutionResultTarget = new FormData();
    fdlExecutionResultTarget.top = new FormAttachment( 0, 0 );
    fdlExecutionResultTarget.left = new FormAttachment( 0, 0 ); // First one in the left
    fdlExecutionResultTarget.right = new FormAttachment( middle, -margin );
    wlExecutionResultTarget.setLayoutData( fdlExecutionResultTarget );
    wExecutionResultTarget = new CCombo( wInputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wExecutionResultTarget );
    wExecutionResultTarget.addModifyListener( lsMod );
    FormData fdExecutionResultTarget = new FormData();
    fdExecutionResultTarget.top = new FormAttachment( 0, 0 );
    fdExecutionResultTarget.left = new FormAttachment( middle, 0 ); // To the right
    fdExecutionResultTarget.right = new FormAttachment( 100, 0 );
    wExecutionResultTarget.setLayoutData( fdExecutionResultTarget );
    Control lastControl = wExecutionResultTarget;

    // ExecutionTimeField
    //
    wlExecutionTimeField = new Label( wInputComposite, SWT.RIGHT );
    props.setLook( wlExecutionTimeField );
    wlExecutionTimeField.setText( BaseMessages.getString( PKG, "TransExecutorDialog.ExecutionTimeField.Label" ) );
    FormData fdlExecutionTimeField = new FormData();
    fdlExecutionTimeField.top = new FormAttachment( lastControl, margin );
    fdlExecutionTimeField.left = new FormAttachment( 0, 0 ); // First one in the left
    fdlExecutionTimeField.right = new FormAttachment( middle, -margin );
    wlExecutionTimeField.setLayoutData( fdlExecutionTimeField );
    wExecutionTimeField = new TextVar( transMeta, wInputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wExecutionTimeField );
    wExecutionTimeField.addModifyListener( lsMod );
    FormData fdExecutionTimeField = new FormData();
    fdExecutionTimeField.top = new FormAttachment( lastControl, margin );
    fdExecutionTimeField.left = new FormAttachment( middle, 0 ); // To the right
    fdExecutionTimeField.right = new FormAttachment( 100, 0 );
    wExecutionTimeField.setLayoutData( fdExecutionTimeField );
    lastControl = wExecutionTimeField;

    // ExecutionResultField
    //
    wlExecutionResultField = new Label( wInputComposite, SWT.RIGHT );
    props.setLook( wlExecutionResultField );
    wlExecutionResultField.setText( BaseMessages.getString( PKG, "TransExecutorDialog.ExecutionResultField.Label" ) );
    FormData fdlExecutionResultField = new FormData();
    fdlExecutionResultField.top = new FormAttachment( lastControl, margin );
    fdlExecutionResultField.left = new FormAttachment( 0, 0 ); // First one in the left
    fdlExecutionResultField.right = new FormAttachment( middle, -margin );
    wlExecutionResultField.setLayoutData( fdlExecutionResultField );
    wExecutionResultField = new TextVar( transMeta, wInputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wExecutionResultField );
    wExecutionResultField.addModifyListener( lsMod );
    FormData fdExecutionResultField = new FormData();
    fdExecutionResultField.top = new FormAttachment( lastControl, margin );
    fdExecutionResultField.left = new FormAttachment( middle, 0 ); // To the right
    fdExecutionResultField.right = new FormAttachment( 100, 0 );
    wExecutionResultField.setLayoutData( fdExecutionResultField );
    lastControl = wExecutionResultField;

    // ExecutionNrErrorsField
    //
    wlExecutionNrErrorsField = new Label( wInputComposite, SWT.RIGHT );
    props.setLook( wlExecutionNrErrorsField );
    wlExecutionNrErrorsField
      .setText( BaseMessages.getString( PKG, "TransExecutorDialog.ExecutionNrErrorsField.Label" ) );
    FormData fdlExecutionNrErrorsField = new FormData();
    fdlExecutionNrErrorsField.top = new FormAttachment( lastControl, margin );
    fdlExecutionNrErrorsField.left = new FormAttachment( 0, 0 ); // First one in the left
    fdlExecutionNrErrorsField.right = new FormAttachment( middle, -margin );
    wlExecutionNrErrorsField.setLayoutData( fdlExecutionNrErrorsField );
    wExecutionNrErrorsField = new TextVar( transMeta, wInputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wExecutionNrErrorsField );
    wExecutionNrErrorsField.addModifyListener( lsMod );
    FormData fdExecutionNrErrorsField = new FormData();
    fdExecutionNrErrorsField.top = new FormAttachment( lastControl, margin );
    fdExecutionNrErrorsField.left = new FormAttachment( middle, 0 ); // To the right
    fdExecutionNrErrorsField.right = new FormAttachment( 100, 0 );
    wExecutionNrErrorsField.setLayoutData( fdExecutionNrErrorsField );
    lastControl = wExecutionNrErrorsField;

    // ExecutionLinesReadField
    //
    wlExecutionLinesReadField = new Label( wInputComposite, SWT.RIGHT );
    props.setLook( wlExecutionLinesReadField );
    wlExecutionLinesReadField.setText( BaseMessages
      .getString( PKG, "TransExecutorDialog.ExecutionLinesReadField.Label" ) );
    FormData fdlExecutionLinesReadField = new FormData();
    fdlExecutionLinesReadField.top = new FormAttachment( lastControl, margin );
    fdlExecutionLinesReadField.left = new FormAttachment( 0, 0 ); // First one in the left
    fdlExecutionLinesReadField.right = new FormAttachment( middle, -margin );
    wlExecutionLinesReadField.setLayoutData( fdlExecutionLinesReadField );
    wExecutionLinesReadField = new TextVar( transMeta, wInputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wExecutionLinesReadField );
    wExecutionLinesReadField.addModifyListener( lsMod );
    FormData fdExecutionLinesReadField = new FormData();
    fdExecutionLinesReadField.top = new FormAttachment( lastControl, margin );
    fdExecutionLinesReadField.left = new FormAttachment( middle, 0 ); // To the right
    fdExecutionLinesReadField.right = new FormAttachment( 100, 0 );
    wExecutionLinesReadField.setLayoutData( fdExecutionLinesReadField );
    lastControl = wExecutionLinesReadField;

    // ExecutionLinesWrittenField
    //
    wlExecutionLinesWrittenField = new Label( wInputComposite, SWT.RIGHT );
    props.setLook( wlExecutionLinesWrittenField );
    wlExecutionLinesWrittenField.setText( BaseMessages.getString( PKG,
      "TransExecutorDialog.ExecutionLinesWrittenField.Label" ) );
    FormData fdlExecutionLinesWrittenField = new FormData();
    fdlExecutionLinesWrittenField.top = new FormAttachment( lastControl, margin );
    fdlExecutionLinesWrittenField.left = new FormAttachment( 0, 0 ); // First one in the left
    fdlExecutionLinesWrittenField.right = new FormAttachment( middle, -margin );
    wlExecutionLinesWrittenField.setLayoutData( fdlExecutionLinesWrittenField );
    wExecutionLinesWrittenField = new TextVar( transMeta, wInputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wExecutionLinesWrittenField );
    wExecutionLinesWrittenField.addModifyListener( lsMod );
    FormData fdExecutionLinesWrittenField = new FormData();
    fdExecutionLinesWrittenField.top = new FormAttachment( lastControl, margin );
    fdExecutionLinesWrittenField.left = new FormAttachment( middle, 0 ); // To the right
    fdExecutionLinesWrittenField.right = new FormAttachment( 100, 0 );
    wExecutionLinesWrittenField.setLayoutData( fdExecutionLinesWrittenField );
    lastControl = wExecutionLinesWrittenField;

    // ExecutionLinesInputField
    //
    wlExecutionLinesInputField = new Label( wInputComposite, SWT.RIGHT );
    props.setLook( wlExecutionLinesInputField );
    wlExecutionLinesInputField.setText( BaseMessages.getString( PKG,
      "TransExecutorDialog.ExecutionLinesInputField.Label" ) );
    FormData fdlExecutionLinesInputField = new FormData();
    fdlExecutionLinesInputField.top = new FormAttachment( lastControl, margin );
    fdlExecutionLinesInputField.left = new FormAttachment( 0, 0 ); // First one in the left
    fdlExecutionLinesInputField.right = new FormAttachment( middle, -margin );
    wlExecutionLinesInputField.setLayoutData( fdlExecutionLinesInputField );
    wExecutionLinesInputField = new TextVar( transMeta, wInputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wExecutionLinesInputField );
    wExecutionLinesInputField.addModifyListener( lsMod );
    FormData fdExecutionLinesInputField = new FormData();
    fdExecutionLinesInputField.top = new FormAttachment( lastControl, margin );
    fdExecutionLinesInputField.left = new FormAttachment( middle, 0 ); // To the right
    fdExecutionLinesInputField.right = new FormAttachment( 100, 0 );
    wExecutionLinesInputField.setLayoutData( fdExecutionLinesInputField );
    lastControl = wExecutionLinesInputField;

    // ExecutionLinesOutputField
    //
    wlExecutionLinesOutputField = new Label( wInputComposite, SWT.RIGHT );
    props.setLook( wlExecutionLinesOutputField );
    wlExecutionLinesOutputField.setText( BaseMessages.getString( PKG,
      "TransExecutorDialog.ExecutionLinesOutputField.Label" ) );
    FormData fdlExecutionLinesOutputField = new FormData();
    fdlExecutionLinesOutputField.top = new FormAttachment( lastControl, margin );
    fdlExecutionLinesOutputField.left = new FormAttachment( 0, 0 ); // First one in the left
    fdlExecutionLinesOutputField.right = new FormAttachment( middle, -margin );
    wlExecutionLinesOutputField.setLayoutData( fdlExecutionLinesOutputField );
    wExecutionLinesOutputField = new TextVar( transMeta, wInputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wExecutionLinesOutputField );
    wExecutionLinesOutputField.addModifyListener( lsMod );
    FormData fdExecutionLinesOutputField = new FormData();
    fdExecutionLinesOutputField.top = new FormAttachment( lastControl, margin );
    fdExecutionLinesOutputField.left = new FormAttachment( middle, 0 ); // To the right
    fdExecutionLinesOutputField.right = new FormAttachment( 100, 0 );
    wExecutionLinesOutputField.setLayoutData( fdExecutionLinesOutputField );
    lastControl = wExecutionLinesOutputField;

    // ExecutionLinesRejectedField
    //
    wlExecutionLinesRejectedField = new Label( wInputComposite, SWT.RIGHT );
    props.setLook( wlExecutionLinesRejectedField );
    wlExecutionLinesRejectedField.setText( BaseMessages.getString( PKG,
      "TransExecutorDialog.ExecutionLinesRejectedField.Label" ) );
    FormData fdlExecutionLinesRejectedField = new FormData();
    fdlExecutionLinesRejectedField.top = new FormAttachment( lastControl, margin );
    fdlExecutionLinesRejectedField.left = new FormAttachment( 0, 0 ); // First one in the left
    fdlExecutionLinesRejectedField.right = new FormAttachment( middle, -margin );
    wlExecutionLinesRejectedField.setLayoutData( fdlExecutionLinesRejectedField );
    wExecutionLinesRejectedField = new TextVar( transMeta, wInputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wExecutionLinesRejectedField );
    wExecutionLinesRejectedField.addModifyListener( lsMod );
    FormData fdExecutionLinesRejectedField = new FormData();
    fdExecutionLinesRejectedField.top = new FormAttachment( lastControl, margin );
    fdExecutionLinesRejectedField.left = new FormAttachment( middle, 0 ); // To the right
    fdExecutionLinesRejectedField.right = new FormAttachment( 100, 0 );
    wExecutionLinesRejectedField.setLayoutData( fdExecutionLinesRejectedField );
    lastControl = wExecutionLinesRejectedField;

    // ExecutionLinesUpdatedField
    //
    wlExecutionLinesUpdatedField = new Label( wInputComposite, SWT.RIGHT );
    props.setLook( wlExecutionLinesUpdatedField );
    wlExecutionLinesUpdatedField.setText( BaseMessages.getString( PKG,
      "TransExecutorDialog.ExecutionLinesUpdatedField.Label" ) );
    FormData fdlExecutionLinesUpdatedField = new FormData();
    fdlExecutionLinesUpdatedField.top = new FormAttachment( lastControl, margin );
    fdlExecutionLinesUpdatedField.left = new FormAttachment( 0, 0 ); // First one in the left
    fdlExecutionLinesUpdatedField.right = new FormAttachment( middle, -margin );
    wlExecutionLinesUpdatedField.setLayoutData( fdlExecutionLinesUpdatedField );
    wExecutionLinesUpdatedField = new TextVar( transMeta, wInputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wExecutionLinesUpdatedField );
    wExecutionLinesUpdatedField.addModifyListener( lsMod );
    FormData fdExecutionLinesUpdatedField = new FormData();
    fdExecutionLinesUpdatedField.top = new FormAttachment( lastControl, margin );
    fdExecutionLinesUpdatedField.left = new FormAttachment( middle, 0 ); // To the right
    fdExecutionLinesUpdatedField.right = new FormAttachment( 100, 0 );
    wExecutionLinesUpdatedField.setLayoutData( fdExecutionLinesUpdatedField );
    lastControl = wExecutionLinesUpdatedField;

    // ExecutionLinesDeletedField
    //
    wlExecutionLinesDeletedField = new Label( wInputComposite, SWT.RIGHT );
    props.setLook( wlExecutionLinesDeletedField );
    wlExecutionLinesDeletedField.setText( BaseMessages.getString( PKG,
      "TransExecutorDialog.ExecutionLinesDeletedField.Label" ) );
    FormData fdlExecutionLinesDeletedField = new FormData();
    fdlExecutionLinesDeletedField.top = new FormAttachment( lastControl, margin );
    fdlExecutionLinesDeletedField.left = new FormAttachment( 0, 0 ); // First one in the left
    fdlExecutionLinesDeletedField.right = new FormAttachment( middle, -margin );
    wlExecutionLinesDeletedField.setLayoutData( fdlExecutionLinesDeletedField );
    wExecutionLinesDeletedField = new TextVar( transMeta, wInputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wExecutionLinesDeletedField );
    wExecutionLinesDeletedField.addModifyListener( lsMod );
    FormData fdExecutionLinesDeletedField = new FormData();
    fdExecutionLinesDeletedField.top = new FormAttachment( lastControl, margin );
    fdExecutionLinesDeletedField.left = new FormAttachment( middle, 0 ); // To the right
    fdExecutionLinesDeletedField.right = new FormAttachment( 100, 0 );
    wExecutionLinesDeletedField.setLayoutData( fdExecutionLinesDeletedField );
    lastControl = wExecutionLinesDeletedField;

    // ExecutionFilesRetrievedField
    //
    wlExecutionFilesRetrievedField = new Label( wInputComposite, SWT.RIGHT );
    props.setLook( wlExecutionFilesRetrievedField );
    wlExecutionFilesRetrievedField.setText( BaseMessages.getString( PKG,
      "TransExecutorDialog.ExecutionFilesRetrievedField.Label" ) );
    FormData fdlExecutionFilesRetrievedField = new FormData();
    fdlExecutionFilesRetrievedField.top = new FormAttachment( lastControl, margin );
    fdlExecutionFilesRetrievedField.left = new FormAttachment( 0, 0 ); // First one in the left
    fdlExecutionFilesRetrievedField.right = new FormAttachment( middle, -margin );
    wlExecutionFilesRetrievedField.setLayoutData( fdlExecutionFilesRetrievedField );
    wExecutionFilesRetrievedField = new TextVar( transMeta, wInputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wExecutionFilesRetrievedField );
    wExecutionFilesRetrievedField.addModifyListener( lsMod );
    FormData fdExecutionFilesRetrievedField = new FormData();
    fdExecutionFilesRetrievedField.top = new FormAttachment( lastControl, margin );
    fdExecutionFilesRetrievedField.left = new FormAttachment( middle, 0 ); // To the right
    fdExecutionFilesRetrievedField.right = new FormAttachment( 100, 0 );
    wExecutionFilesRetrievedField.setLayoutData( fdExecutionFilesRetrievedField );
    lastControl = wExecutionFilesRetrievedField;

    // ExecutionExitStatusField
    //
    wlExecutionExitStatusField = new Label( wInputComposite, SWT.RIGHT );
    props.setLook( wlExecutionExitStatusField );
    wlExecutionExitStatusField.setText( BaseMessages.getString( PKG,
      "TransExecutorDialog.ExecutionExitStatusField.Label" ) );
    FormData fdlExecutionExitStatusField = new FormData();
    fdlExecutionExitStatusField.top = new FormAttachment( lastControl, margin );
    fdlExecutionExitStatusField.left = new FormAttachment( 0, 0 ); // First one in the left
    fdlExecutionExitStatusField.right = new FormAttachment( middle, -margin );
    wlExecutionExitStatusField.setLayoutData( fdlExecutionExitStatusField );
    wExecutionExitStatusField = new TextVar( transMeta, wInputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wExecutionExitStatusField );
    wExecutionExitStatusField.addModifyListener( lsMod );
    FormData fdExecutionExitStatusField = new FormData();
    fdExecutionExitStatusField.top = new FormAttachment( lastControl, margin );
    fdExecutionExitStatusField.left = new FormAttachment( middle, 0 ); // To the right
    fdExecutionExitStatusField.right = new FormAttachment( 100, 0 );
    wExecutionExitStatusField.setLayoutData( fdExecutionExitStatusField );
    lastControl = wExecutionExitStatusField;

    // ExecutionLogTextField
    //
    wlExecutionLogTextField = new Label( wInputComposite, SWT.RIGHT );
    props.setLook( wlExecutionLogTextField );
    wlExecutionLogTextField.setText( BaseMessages.getString( PKG, "TransExecutorDialog.ExecutionLogTextField.Label" ) );
    FormData fdlExecutionLogTextField = new FormData();
    fdlExecutionLogTextField.top = new FormAttachment( lastControl, margin );
    fdlExecutionLogTextField.left = new FormAttachment( 0, 0 ); // First one in the left
    fdlExecutionLogTextField.right = new FormAttachment( middle, -margin );
    wlExecutionLogTextField.setLayoutData( fdlExecutionLogTextField );
    wExecutionLogTextField = new TextVar( transMeta, wInputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wExecutionLogTextField );
    wExecutionLogTextField.addModifyListener( lsMod );
    FormData fdExecutionLogTextField = new FormData();
    fdExecutionLogTextField.top = new FormAttachment( lastControl, margin );
    fdExecutionLogTextField.left = new FormAttachment( middle, 0 ); // To the right
    fdExecutionLogTextField.right = new FormAttachment( 100, 0 );
    wExecutionLogTextField.setLayoutData( fdExecutionLogTextField );
    lastControl = wExecutionLogTextField;

    // ExecutionLogChannelIdField
    //
    wlExecutionLogChannelIdField = new Label( wInputComposite, SWT.RIGHT );
    props.setLook( wlExecutionLogChannelIdField );
    wlExecutionLogChannelIdField.setText( BaseMessages.getString( PKG,
      "TransExecutorDialog.ExecutionLogChannelIdField.Label" ) );
    FormData fdlExecutionLogChannelIdField = new FormData();
    fdlExecutionLogChannelIdField.top = new FormAttachment( lastControl, margin );
    fdlExecutionLogChannelIdField.left = new FormAttachment( 0, 0 ); // First one in the left
    fdlExecutionLogChannelIdField.right = new FormAttachment( middle, -margin );
    wlExecutionLogChannelIdField.setLayoutData( fdlExecutionLogChannelIdField );
    wExecutionLogChannelIdField = new TextVar( transMeta, wInputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wExecutionLogChannelIdField );
    wExecutionLogChannelIdField.addModifyListener( lsMod );
    FormData fdExecutionLogChannelIdField = new FormData();
    fdExecutionLogChannelIdField.top = new FormAttachment( lastControl, margin );
    fdExecutionLogChannelIdField.left = new FormAttachment( middle, 0 ); // To the right
    fdExecutionLogChannelIdField.right = new FormAttachment( 100, 0 );
    wExecutionLogChannelIdField.setLayoutData( fdExecutionLogChannelIdField );
    lastControl = wExecutionLogChannelIdField;

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
    tabLayout.marginWidth = Const.FORM_MARGIN;
    tabLayout.marginHeight = Const.FORM_MARGIN;
    wInputComposite.setLayout( tabLayout );

    wlResultFilesTarget = new Label( wInputComposite, SWT.RIGHT );
    props.setLook( wlResultFilesTarget );
    wlResultFilesTarget.setText( BaseMessages.getString( PKG, "TransExecutorDialog.ResultFilesTarget.Label" ) );
    FormData fdlResultFilesTarget = new FormData();
    fdlResultFilesTarget.top = new FormAttachment( 0, 0 );
    fdlResultFilesTarget.left = new FormAttachment( 0, 0 ); // First one in the left
    fdlResultFilesTarget.right = new FormAttachment( middle, -margin );
    wlResultFilesTarget.setLayoutData( fdlResultFilesTarget );
    wResultFilesTarget = new CCombo( wInputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wResultFilesTarget );
    wResultFilesTarget.addModifyListener( lsMod );
    FormData fdResultFilesTarget = new FormData();
    fdResultFilesTarget.top = new FormAttachment( 0, 0 );
    fdResultFilesTarget.left = new FormAttachment( middle, 0 ); // To the right
    fdResultFilesTarget.right = new FormAttachment( 100, 0 );
    wResultFilesTarget.setLayoutData( fdResultFilesTarget );
    Control lastControl = wResultFilesTarget;

    // ResultFileNameField
    //
    wlResultFileNameField = new Label( wInputComposite, SWT.RIGHT );
    props.setLook( wlResultFileNameField );
    wlResultFileNameField.setText( BaseMessages.getString( PKG, "TransExecutorDialog.ResultFileNameField.Label" ) );
    FormData fdlResultFileNameField = new FormData();
    fdlResultFileNameField.top = new FormAttachment( lastControl, margin );
    fdlResultFileNameField.left = new FormAttachment( 0, 0 ); // First one in the left
    fdlResultFileNameField.right = new FormAttachment( middle, -margin );
    wlResultFileNameField.setLayoutData( fdlResultFileNameField );
    wResultFileNameField = new TextVar( transMeta, wInputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wResultFileNameField );
    wResultFileNameField.addModifyListener( lsMod );
    FormData fdResultFileNameField = new FormData();
    fdResultFileNameField.top = new FormAttachment( lastControl, margin );
    fdResultFileNameField.left = new FormAttachment( middle, 0 ); // To the right
    fdResultFileNameField.right = new FormAttachment( 100, 0 );
    wResultFileNameField.setLayoutData( fdResultFileNameField );
    lastControl = wResultFileNameField;

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
    tabLayout.marginWidth = Const.FORM_MARGIN;
    tabLayout.marginHeight = Const.FORM_MARGIN;
    wInputComposite.setLayout( tabLayout );

    wlResultRowsTarget = new Label( wInputComposite, SWT.RIGHT );
    props.setLook( wlResultRowsTarget );
    wlResultRowsTarget.setText( BaseMessages.getString( PKG, "TransExecutorDialog.OutputRowsSource.Label" ) );
    FormData fdlResultRowsTarget = new FormData();
    fdlResultRowsTarget.top = new FormAttachment( 0, 0 );
    fdlResultRowsTarget.left = new FormAttachment( 0, 0 ); // First one in the left
    fdlResultRowsTarget.right = new FormAttachment( middle, -margin );
    wlResultRowsTarget.setLayoutData( fdlResultRowsTarget );
    wOutputRowsSource = new CCombo( wInputComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wOutputRowsSource );
    wOutputRowsSource.addModifyListener( lsMod );
    FormData fdResultRowsTarget = new FormData();
    fdResultRowsTarget.top = new FormAttachment( 0, 0 );
    fdResultRowsTarget.left = new FormAttachment( middle, 0 ); // To the right
    fdResultRowsTarget.right = new FormAttachment( 100, 0 );
    wOutputRowsSource.setLayoutData( fdResultRowsTarget );
    Control lastControl = wOutputRowsSource;

    wlOutputFields = new Label( wInputComposite, SWT.NONE );
    wlOutputFields.setText( BaseMessages.getString( PKG, "TransExecutorDialog.ResultFields.Label" ) );
    props.setLook( wlOutputFields );
    FormData fdlResultFields = new FormData();
    fdlResultFields.left = new FormAttachment( 0, 0 );
    fdlResultFields.top = new FormAttachment( lastControl, margin );
    wlOutputFields.setLayoutData( fdlResultFields );

    int nrRows = ( transExecutorMeta.getOutputRowsField() != null ? transExecutorMeta.getOutputRowsField().length : 1 );

    ColumnInfo[] ciResultFields =
      new ColumnInfo[] {
        new ColumnInfo( BaseMessages.getString( PKG, "TransExecutorDialog.ColumnInfo.Field" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false, false ),
        new ColumnInfo( BaseMessages.getString( PKG, "TransExecutorDialog.ColumnInfo.Type" ),
          ColumnInfo.COLUMN_TYPE_CCOMBO, ValueMeta.getTypes() ),
        new ColumnInfo( BaseMessages.getString( PKG, "TransExecutorDialog.ColumnInfo.Length" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false ),
        new ColumnInfo( BaseMessages.getString( PKG, "TransExecutorDialog.ColumnInfo.Precision" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false ), };

    wOutputFields =
      new TableView( transMeta, wInputComposite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL
        | SWT.H_SCROLL, ciResultFields, nrRows, lsMod, props );

    FormData fdResultFields = new FormData();
    fdResultFields.left = new FormAttachment( 0, 0 );
    fdResultFields.top = new FormAttachment( wlOutputFields, margin );
    fdResultFields.right = new FormAttachment( 100, 0 );
    fdResultFields.bottom = new FormAttachment( 100, 0 );
    wOutputFields.setLayoutData( fdResultFields );

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
    boolean enableField = !Const.isEmpty( wGroupField.getText() );
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
    if ( Const.isEmpty( wStepname.getText() ) ) {
      return;
    }

    stepname = wStepname.getText(); // return value

    try {
      loadTrans();
    } catch ( KettleException e ) {
      new ErrorDialog( shell, BaseMessages.getString( PKG, "TransExecutorDialog.ErrorLoadingSpecifiedTrans.Title" ),
        BaseMessages.getString( PKG, "TransExecutorDialog.ErrorLoadingSpecifiedTrans.Message" ), e );
    }

    transExecutorMeta.setSpecificationMethod( specificationMethod );
    switch ( specificationMethod ) {
      case FILENAME:
        transExecutorMeta.setFileName( wFilename.getText() );
        transExecutorMeta.setDirectoryPath( null );
        transExecutorMeta.setTransName( null );
        transExecutorMeta.setTransObjectId( null );
        break;
      case REPOSITORY_BY_NAME:
        transExecutorMeta.setDirectoryPath( wDirectory.getText() );
        transExecutorMeta.setTransName( wTransname.getText() );
        transExecutorMeta.setFileName( null );
        transExecutorMeta.setTransObjectId( null );
        break;
      case REPOSITORY_BY_REFERENCE:
        transExecutorMeta.setFileName( null );
        transExecutorMeta.setDirectoryPath( null );
        transExecutorMeta.setTransName( null );
        transExecutorMeta.setTransObjectId( referenceObjectId );
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
    String[] variables = new String[nrLines];
    String[] fields = new String[nrLines];
    String[] input = new String[nrLines];
    parameters.setVariable( variables );
    parameters.setField( fields );
    parameters.setInput( input );
    for ( int i = 0; i < nrLines; i++ ) {
      TableItem item = wTransExecutorParameters.getNonEmpty( i );
      variables[i] = item.getText( 1 );
      fields[i] = item.getText( 2 );
      input[i] = item.getText( 3 );
    }
    parameters.setInheritingAllVariables( wInheritAll.getSelection() );

    // The group definition
    //
    transExecutorMeta.setGroupSize( wGroupSize.getText() );
    transExecutorMeta.setGroupField( wGroupField.getText() );
    transExecutorMeta.setGroupTime( wGroupTime.getText() );

    transExecutorMeta.setExecutionResultTargetStep( wExecutionResultTarget.getText() );
    transExecutorMeta.setExecutionResultTargetStepMeta( transMeta.findStep( wExecutionResultTarget.getText() ) );
    transExecutorMeta.setExecutionTimeField( wExecutionTimeField.getText() );
    transExecutorMeta.setExecutionResultField( wExecutionResultField.getText() );
    transExecutorMeta.setExecutionNrErrorsField( wExecutionNrErrorsField.getText() );
    transExecutorMeta.setExecutionLinesReadField( wExecutionLinesReadField.getText() );
    transExecutorMeta.setExecutionLinesWrittenField( wExecutionLinesWrittenField.getText() );
    transExecutorMeta.setExecutionLinesInputField( wExecutionLinesInputField.getText() );
    transExecutorMeta.setExecutionLinesOutputField( wExecutionLinesOutputField.getText() );
    transExecutorMeta.setExecutionLinesRejectedField( wExecutionLinesRejectedField.getText() );
    transExecutorMeta.setExecutionLinesUpdatedField( wExecutionLinesUpdatedField.getText() );
    transExecutorMeta.setExecutionLinesDeletedField( wExecutionLinesDeletedField.getText() );
    transExecutorMeta.setExecutionFilesRetrievedField( wExecutionFilesRetrievedField.getText() );
    transExecutorMeta.setExecutionExitStatusField( wExecutionExitStatusField.getText() );
    transExecutorMeta.setExecutionLogTextField( wExecutionLogTextField.getText() );
    transExecutorMeta.setExecutionLogChannelIdField( wExecutionLogChannelIdField.getText() );

    transExecutorMeta.setResultFilesTargetStep( wResultFilesTarget.getText() );
    transExecutorMeta.setResultFilesTargetStepMeta( transMeta.findStep( wResultFilesTarget.getText() ) );
    transExecutorMeta.setResultFilesFileNameField( wResultFileNameField.getText() );

    if ( !Const.isEmpty( executorOutputStep ) ) {
      transExecutorMeta.setExecutorsOutputStep( executorOutputStep );
      transExecutorMeta.setExecutorsOutputStepMeta( transMeta.findStep( executorOutputStep ) );
    }

    // Result row info
    //
    transExecutorMeta.setOutputRowsSourceStep( wOutputRowsSource.getText() );
    transExecutorMeta.setOutputRowsSourceStepMeta( transMeta.findStep( wOutputRowsSource.getText() ) );
    int nrFields = wOutputFields.nrNonEmpty();
    transExecutorMeta.setOutputRowsField( new String[nrFields] );
    transExecutorMeta.setOutputRowsType( new int[nrFields] );
    transExecutorMeta.setOutputRowsLength( new int[nrFields] );
    transExecutorMeta.setOutputRowsPrecision( new int[nrFields] );

    // CHECKSTYLE:Indentation:OFF
    for ( int i = 0; i < nrFields; i++ ) {
      TableItem item = wOutputFields.getNonEmpty( i );
      transExecutorMeta.getOutputRowsField()[i] = item.getText( 1 );
      transExecutorMeta.getOutputRowsType()[i] = ValueMeta.getType( item.getText( 2 ) );
      transExecutorMeta.getOutputRowsLength()[i] = Const.toInt( item.getText( 3 ), -1 );
      transExecutorMeta.getOutputRowsPrecision()[i] = Const.toInt( item.getText( 4 ), -1 );
    }

  }

  /**
   * Ask the user to fill in the details...
   */
  protected void newTransformation() {
    TransMeta newTransMeta = new TransMeta();

    newTransMeta.getDatabases().addAll( transMeta.getDatabases() );
    newTransMeta.setRepository( transMeta.getRepository() );
    newTransMeta.setRepositoryDirectory( transMeta.getRepositoryDirectory() );

    // Pass some interesting settings from the parent transformations...
    //
    newTransMeta.setUsingUniqueConnections( transMeta.isUsingUniqueConnections() );

    TransDialog transDialog = new TransDialog( shell, SWT.NONE, newTransMeta, repository );
    if ( transDialog.open() != null ) {
      Spoon spoon = Spoon.getInstance();
      spoon.addTransGraph( newTransMeta );
      boolean saved = false;
      try {
        if ( repository != null ) {
          if ( !Const.isEmpty( newTransMeta.getName() ) ) {
            wStepname.setText( newTransMeta.getName() );
          }
          saved = spoon.saveToRepository( newTransMeta, false );
          if ( repository.getRepositoryMeta().getRepositoryCapabilities().supportsReferences() ) {
            specificationMethod = ObjectLocationSpecificationMethod.REPOSITORY_BY_REFERENCE;
          } else {
            specificationMethod = ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME;
          }
        } else {
          saved = spoon.saveToFile( newTransMeta );
          specificationMethod = ObjectLocationSpecificationMethod.FILENAME;
        }
      } catch ( Exception e ) {
        new ErrorDialog( shell, "Error", "Error saving new transformation", e );
      }
      if ( saved ) {
        setRadioButtons();
        switch ( specificationMethod ) {
          case FILENAME:
            wFilename.setText( Const.NVL( newTransMeta.getFilename(), "" ) );
            break;
          case REPOSITORY_BY_NAME:
            wTransname.setText( Const.NVL( newTransMeta.getName(), "" ) );
            wDirectory.setText( newTransMeta.getRepositoryDirectory().getPath() );
            break;
          case REPOSITORY_BY_REFERENCE:
            getByReferenceData( newTransMeta.getObjectId() );
            break;
          default:
            break;
        }

        // Grab parameters
        //
        getParametersFromTrans( newTransMeta );
      }
    }
  }

  private void getByReferenceData( ObjectId transObjectId ) {
    try {
      if ( repository == null ) {
        throw new KettleException( BaseMessages.getString( PKG,
          "TransExecutorDialog.Exception.NotConnectedToRepository.Message" ) );
      }
      RepositoryObject transInf = repository.getObjectInformation( transObjectId, RepositoryObjectType.TRANSFORMATION );
      updateByReferenceField( transInf );
    } catch ( KettleException e ) {
      new ErrorDialog( shell, BaseMessages.getString( PKG,
        "TransExecutorDialog.Exception.UnableToReferenceObjectId.Title" ), BaseMessages.getString( PKG,
        "TransExecutorDialog.Exception.UnableToReferenceObjectId.Message" ), e );
    }
  }

  SelectObjectDialog getSelectObjectDialog( Shell parent, Repository rep, boolean showTransformations,
      boolean showJobs ) {
    return new SelectObjectDialog( parent, rep, showTransformations, showJobs );
  }

}
