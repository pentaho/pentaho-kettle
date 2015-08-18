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

package org.pentaho.di.ui.trans.steps.simplemapping;

import org.apache.commons.vfs2.FileObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
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
import org.pentaho.di.core.SourceToTargetMapping;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.core.gui.SpoonInterface;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryElementMetaInterface;
import org.pentaho.di.repository.RepositoryObject;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.mapping.MappingIODefinition;
import org.pentaho.di.trans.steps.mapping.MappingParameters;
import org.pentaho.di.trans.steps.mapping.MappingValueRename;
import org.pentaho.di.trans.steps.mappinginput.MappingInputMeta;
import org.pentaho.di.trans.steps.mappingoutput.MappingOutputMeta;
import org.pentaho.di.trans.steps.simplemapping.SimpleMappingMeta;
import org.pentaho.di.ui.core.dialog.EnterMappingDialog;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
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
import java.util.ArrayList;
import java.util.List;

public class SimpleMappingDialog extends BaseStepDialog implements StepDialogInterface {
  private static Class<?> PKG = SimpleMappingMeta.class; // for i18n purposes, needed by Translator2!!

  private SimpleMappingMeta mappingMeta;

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

  // Edit the mapping transformation in Spoon
  //
  private Button wEditTrans;
  private Button wNewTrans;

  private CTabFolder wTabFolder;

  private TransMeta mappingTransMeta = null;

  protected boolean transModified;

  private ModifyListener lsMod;

  private int middle;

  private int margin;

  private MappingParameters mappingParameters;

  private MappingIODefinition inputMapping;

  private MappingIODefinition outputMapping;

  private ObjectId referenceObjectId;
  private ObjectLocationSpecificationMethod specificationMethod;

  private interface ApplyChanges {
    public void applyChanges();
  }

  private class MappingParametersTab implements ApplyChanges {
    private TableView wMappingParameters;

    private MappingParameters parameters;

    private Button wInheritAll;

    public MappingParametersTab( TableView wMappingParameters, Button wInheritAll, MappingParameters parameters ) {
      this.wMappingParameters = wMappingParameters;
      this.wInheritAll = wInheritAll;
      this.parameters = parameters;
    }

    public void applyChanges() {

      int nrLines = wMappingParameters.nrNonEmpty();
      String[] variables = new String[ nrLines ];
      String[] inputFields = new String[ nrLines ];
      parameters.setVariable( variables );
      parameters.setInputField( inputFields );
      //CHECKSTYLE:Indentation:OFF
      for ( int i = 0; i < nrLines; i++ ) {
        TableItem item = wMappingParameters.getNonEmpty( i );
        parameters.getVariable()[ i ] = item.getText( 1 );
        parameters.getInputField()[ i ] = item.getText( 2 );
      }
      parameters.setInheritingAllVariables( wInheritAll.getSelection() );
    }
  }

  private class MappingDefinitionTab implements ApplyChanges {
    private MappingIODefinition definition;

    private TableView wFieldMappings;

    public MappingDefinitionTab( MappingIODefinition definition, TableView fieldMappings ) {
      super();
      this.definition = definition;
      wFieldMappings = fieldMappings;
    }

    public void applyChanges() {
      // The grid
      //
      int nrLines = wFieldMappings.nrNonEmpty();
      definition.getValueRenames().clear();
      for ( int i = 0; i < nrLines; i++ ) {
        TableItem item = wFieldMappings.getNonEmpty( i );
        definition.getValueRenames().add( new MappingValueRename( item.getText( 1 ), item.getText( 2 ) ) );
      }
    }
  }

  private List<ApplyChanges> changeList;

  public SimpleMappingDialog( Shell parent, Object in, TransMeta tr, String sname ) {
    super( parent, (BaseStepMeta) in, tr, sname );
    mappingMeta = (SimpleMappingMeta) in;
    transModified = false;

    // Make a copy for our own purposes...
    // This allows us to change everything directly in the classes with
    // listeners.
    // Later we need to copy it to the input class on ok()
    //
    mappingParameters = (MappingParameters) mappingMeta.getMappingParameters().clone();
    inputMapping = (MappingIODefinition) mappingMeta.getInputMapping().clone();
    outputMapping = (MappingIODefinition) mappingMeta.getOutputMapping().clone();

    changeList = new ArrayList<ApplyChanges>();
  }

  public String open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX );
    props.setLook( shell );
    setShellImage( shell, mappingMeta );

    lsMod = new ModifyListener() {
      public void modifyText( ModifyEvent e ) {
        mappingMeta.setChanged();
      }
    };
    changed = mappingMeta.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout( formLayout );
    shell.setText( BaseMessages.getString( PKG, "SimpleMappingDialog.Shell.Title" ) );

    middle = props.getMiddlePct();
    margin = Const.MARGIN;

    // Stepname line
    wlStepname = new Label( shell, SWT.RIGHT );
    wlStepname.setText( BaseMessages.getString( PKG, "SimpleMappingDialog.Stepname.Label" ) );
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
    gTransGroup.setText( BaseMessages.getString( PKG, "SimpleMappingDialog.TransGroup.Label" ) );
    gTransGroup.setBackground( shell.getBackground() ); // the default looks
    // ugly
    FormLayout transGroupLayout = new FormLayout();
    transGroupLayout.marginLeft = margin * 2;
    transGroupLayout.marginTop = margin * 2;
    transGroupLayout.marginRight = margin * 2;
    transGroupLayout.marginBottom = margin * 2;
    gTransGroup.setLayout( transGroupLayout );

    // Radio button: The mapping is in a file
    //
    radioFilename = new Button( gTransGroup, SWT.RADIO );
    props.setLook( radioFilename );
    radioFilename.setSelection( false );
    radioFilename.setText( BaseMessages.getString( PKG, "SimpleMappingDialog.RadioFile.Label" ) );
    radioFilename.setToolTipText( BaseMessages.getString( PKG, "SimpleMappingDialog.RadioFile.Tooltip", Const.CR ) );
    FormData fdFileRadio = new FormData();
    fdFileRadio.left = new FormAttachment( 0, 0 );
    fdFileRadio.right = new FormAttachment( 100, 0 );
    fdFileRadio.top = new FormAttachment( 0, 0 );
    radioFilename.setLayoutData( fdFileRadio );
    radioFilename.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        setSpecificationMethod( ObjectLocationSpecificationMethod.FILENAME );
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
        setSpecificationMethod( ObjectLocationSpecificationMethod.FILENAME );
        setRadioButtons();
      }
    } );

    // Radio button: The mapping is in the repository
    //
    radioByName = new Button( gTransGroup, SWT.RADIO );
    props.setLook( radioByName );
    radioByName.setSelection( false );
    radioByName.setText( BaseMessages.getString( PKG, "SimpleMappingDialog.RadioRep.Label" ) );
    radioByName.setToolTipText( BaseMessages.getString( PKG, "SimpleMappingDialog.RadioRep.Tooltip", Const.CR ) );
    FormData fdRepRadio = new FormData();
    fdRepRadio.left = new FormAttachment( 0, 0 );
    fdRepRadio.right = new FormAttachment( 100, 0 );
    fdRepRadio.top = new FormAttachment( wbbFilename, 2 * margin );
    radioByName.setLayoutData( fdRepRadio );
    radioByName.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        setSpecificationMethod( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME );
        setRadioButtons();
      }
    } );
    wbTrans = new Button( gTransGroup, SWT.PUSH | SWT.CENTER ); // Browse
    props.setLook( wbTrans );
    wbTrans.setText( BaseMessages.getString( PKG, "SimpleMappingDialog.Select.Button" ) );
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
        setSpecificationMethod( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME );
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
        setSpecificationMethod( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME );
        setRadioButtons();
      }
    } );

    // Radio button: The mapping is in the repository
    //
    radioByReference = new Button( gTransGroup, SWT.RADIO );
    props.setLook( radioByReference );
    radioByReference.setSelection( false );
    radioByReference.setText( BaseMessages.getString( PKG, "SimpleMappingDialog.RadioRepByReference.Label" ) );
    radioByReference.setToolTipText( BaseMessages.getString( PKG, "SimpleMappingDialog.RadioRepByReference.Tooltip",
      Const.CR ) );
    FormData fdRadioByReference = new FormData();
    fdRadioByReference.left = new FormAttachment( 0, 0 );
    fdRadioByReference.right = new FormAttachment( 100, 0 );
    fdRadioByReference.top = new FormAttachment( wTransname, 2 * margin );
    radioByReference.setLayoutData( fdRadioByReference );
    radioByReference.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        setSpecificationMethod( ObjectLocationSpecificationMethod.REPOSITORY_BY_REFERENCE );
        setRadioButtons();
      }
    } );

    wbByReference = new Button( gTransGroup, SWT.PUSH | SWT.CENTER );
    props.setLook( wbByReference );
    wbByReference.setImage( GUIResource.getInstance().getImageTransGraph() );
    wbByReference.setToolTipText( BaseMessages.getString( PKG, "SimpleMappingDialog.SelectTrans.Tooltip" ) );
    FormData fdbByReference = new FormData();
    fdbByReference.top = new FormAttachment( radioByReference, margin );
    fdbByReference.right = new FormAttachment( 100, 0 );
    wbByReference.setLayoutData( fdbByReference );
    wbByReference.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        selectTransformationByReference();
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
        setSpecificationMethod( ObjectLocationSpecificationMethod.REPOSITORY_BY_REFERENCE );
        setRadioButtons();
      }
    } );

    wNewTrans = new Button( gTransGroup, SWT.PUSH | SWT.CENTER ); // Browse
    props.setLook( wNewTrans );
    wNewTrans.setText( BaseMessages.getString( PKG, "SimpleMappingDialog.New.Button" ) );
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
    wEditTrans.setText( BaseMessages.getString( PKG, "SimpleMappingDialog.Edit.Button" ) );
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

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    // Set the shell size, based upon previous time...
    setSize();

    getData();
    mappingMeta.setChanged( changed );
    wTabFolder.setSelection( 0 );

    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return stepname;
  }

  protected void selectTransformationByReference() {
    if ( repository != null ) {
      SelectObjectDialog sod = new SelectObjectDialog( shell, repository, true, false );
      sod.open();
      RepositoryElementMetaInterface repositoryObject = sod.getRepositoryObject();
      if ( repositoryObject != null ) {
        setSpecificationMethod( ObjectLocationSpecificationMethod.REPOSITORY_BY_REFERENCE );
        updateByReferenceField( repositoryObject );
        setReferenceObjectId( repositoryObject.getObjectId() );
        setRadioButtons();
      }
    }
  }

  private void selectRepositoryTrans() {
    try {
      SelectObjectDialog sod = new SelectObjectDialog( shell, repository );
      String transName = sod.open();
      RepositoryDirectoryInterface repdir = sod.getDirectory();
      if ( transName != null && repdir != null ) {
        loadRepositoryTrans( transName, repdir );
        wTransname.setText( mappingTransMeta.getName() );
        wDirectory.setText( mappingTransMeta.getRepositoryDirectory().getPath() );
        wFilename.setText( "" );
        radioByName.setSelection( true );
        radioFilename.setSelection( false );
        setSpecificationMethod( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME );
        setRadioButtons();
      }
    } catch ( KettleException ke ) {
      new ErrorDialog( shell, BaseMessages.getString( PKG, "SimpleMappingDialog.ErrorSelectingObject.DialogTitle" ),
        BaseMessages.getString( PKG, "SimpleMappingDialog.ErrorSelectingObject.DialogMessage" ), ke );
    }
  }

  private void loadRepositoryTrans( String transName, RepositoryDirectoryInterface repdir ) throws KettleException {
    // Read the transformation...
    //
    mappingTransMeta =
      repository.loadTransformation( transMeta.environmentSubstitute( transName ), repdir, null, true, null );
    mappingTransMeta.clearChanged();
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
        wFilename.setText( mappingTransMeta.getFilename() );
        wTransname.setText( Const.NVL( mappingTransMeta.getName(), "" ) );
        wDirectory.setText( "" );
        setSpecificationMethod( ObjectLocationSpecificationMethod.FILENAME );
        setRadioButtons();
      }
    } catch ( IOException e ) {
      new ErrorDialog( shell,
        BaseMessages.getString( PKG, "SimpleMappingDialog.ErrorLoadingTransformation.DialogTitle" ),
        BaseMessages.getString( PKG, "SimpleMappingDialog.ErrorLoadingTransformation.DialogMessage" ), e );
    } catch ( KettleException e ) {
      new ErrorDialog( shell,
        BaseMessages.getString( PKG, "SimpleMappingDialog.ErrorLoadingTransformation.DialogTitle" ),
        BaseMessages.getString( PKG, "SimpleMappingDialog.ErrorLoadingTransformation.DialogMessage" ), e );
    }
  }

  private void loadFileTrans( String fname ) throws KettleException {
    mappingTransMeta = new TransMeta( transMeta.environmentSubstitute( fname ) );
    mappingTransMeta.clearChanged();
  }

  private void editTrans() {
    // Load the transformation again to make sure it's still there and
    // refreshed
    // It's an extra check to make sure it's still OK...
    //
    try {
      loadTransformation();

      // If we're still here, mappingTransMeta is valid.
      //
      SpoonInterface spoon = SpoonFactory.getInstance();
      if ( spoon != null ) {
        spoon.addTransGraph( mappingTransMeta );
      }
    } catch ( KettleException e ) {
      new ErrorDialog( shell, BaseMessages.getString( PKG, "SimpleMappingDialog.ErrorShowingTransformation.Title" ),
        BaseMessages.getString( PKG, "SimpleMappingDialog.ErrorShowingTransformation.Message" ), e );
    }
  }

  // Method is defined as package-protected in order to be accessible by unit tests
  void loadTransformation() throws KettleException {
    switch( getSpecificationMethod() ) {
      case FILENAME:
        loadFileTrans( wFilename.getText() );
        break;
      case REPOSITORY_BY_NAME:
        String realDirectory = transMeta.environmentSubstitute( wDirectory.getText() );
        String realTransname = transMeta.environmentSubstitute( wTransname.getText() );

        if ( Const.isEmpty( realDirectory ) || Const.isEmpty( realTransname ) ) {
          throw new KettleException( BaseMessages.getString( PKG,
            "SimpleMappingDialog.Exception.NoValidMappingDetailsFound" ) );
        }
        RepositoryDirectoryInterface repdir = repository.findDirectory( realDirectory );
        if ( repdir == null ) {
          throw new KettleException( BaseMessages.getString( PKG,
            "SimpleMappingDialog.Exception.UnableToFindRepositoryDirectory" ) );
        }
        loadRepositoryTrans( realTransname, repdir );
        break;
      case REPOSITORY_BY_REFERENCE:
        if ( getReferenceObjectId() == null ) {
          throw new KettleException( BaseMessages.getString( PKG,
            "SimpleMappingDialog.Exception.ReferencedTransformationIdIsNull" ) );
        }
        mappingTransMeta = repository.loadTransformation( getReferenceObjectId(), null ); // load the last version
        mappingTransMeta.clearChanged();
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
    radioFilename.setSelection( getSpecificationMethod() == ObjectLocationSpecificationMethod.FILENAME );
    radioByName.setSelection( getSpecificationMethod() == ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME );
    radioByReference
      .setSelection( getSpecificationMethod() == ObjectLocationSpecificationMethod.REPOSITORY_BY_REFERENCE );
    setActive();
  }

  /**
   * Ask the user to fill in the details...
   */
  protected void newTransformation() {

    // Get input fields for this step so we can put this metadata in the mapping
    //
    RowMetaInterface inFields = new RowMeta();
    try {
      inFields = transMeta.getPrevStepFields( stepname );
    } catch ( Exception e ) {
      // Just show the error but continue operations.
      //
      new ErrorDialog( shell, "Error", "Unable to get input fields from previous step", e );
    }

    TransMeta newTransMeta = new TransMeta();

    newTransMeta.getDatabases().addAll( transMeta.getDatabases() );
    newTransMeta.setRepository( transMeta.getRepository() );
    newTransMeta.setRepositoryDirectory( transMeta.getRepositoryDirectory() );

    // Pass some interesting settings from the parent transformations...
    //
    newTransMeta.setUsingUniqueConnections( transMeta.isUsingUniqueConnections() );

    // Add MappingInput and MappingOutput steps
    //
    String INPUTSTEP_NAME = "Mapping Input";
    MappingInputMeta inputMeta = new MappingInputMeta();
    inputMeta.allocate( inFields.size() );
    //CHECKSTYLE:Indentation:OFF
    for ( int i = 0; i < inFields.size(); i++ ) {
      ValueMetaInterface valueMeta = inFields.getValueMeta( i );
      inputMeta.getFieldName()[ i ] = valueMeta.getName();
      inputMeta.getFieldType()[ i ] = valueMeta.getType();
      inputMeta.getFieldLength()[ i ] = valueMeta.getLength();
      inputMeta.getFieldPrecision()[ i ] = valueMeta.getPrecision();
    }
    StepMeta inputStep = new StepMeta( INPUTSTEP_NAME, inputMeta );
    inputStep.setLocation( 50, 50 );
    inputStep.setDraw( true );
    newTransMeta.addStep( inputStep );

    String OUTPUTSTEP_NAME = "Mapping Output";
    MappingOutputMeta outputMeta = new MappingOutputMeta();
    outputMeta.allocate( 0 );
    StepMeta outputStep = new StepMeta( OUTPUTSTEP_NAME, outputMeta );
    outputStep.setLocation( 500, 50 );
    outputStep.setDraw( true );
    newTransMeta.addStep( outputStep );
    newTransMeta.addTransHop( new TransHopMeta( inputStep, outputStep ) );

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
            setSpecificationMethod( ObjectLocationSpecificationMethod.REPOSITORY_BY_REFERENCE );
          } else {
            setSpecificationMethod( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME );
          }
        } else {
          saved = spoon.saveToFile( newTransMeta );
          setSpecificationMethod( ObjectLocationSpecificationMethod.FILENAME );
        }
      } catch ( Exception e ) {
        new ErrorDialog( shell, "Error", "Error saving new transformation", e );
      }
      if ( saved ) {
        setRadioButtons();
        switch( getSpecificationMethod() ) {
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
      }
    }
  }

  private void getByReferenceData( ObjectId transObjectId ) {
    try {
      if ( repository == null ) {
        throw new KettleException( BaseMessages.getString(
          PKG, "SimpleMappingDialog.Exception.NotConnectedToRepository.Message" ) );
      }
      RepositoryObject transInf = repository.getObjectInformation( transObjectId, RepositoryObjectType.TRANSFORMATION );
      updateByReferenceField( transInf );
    } catch ( KettleException e ) {
      new ErrorDialog( shell, BaseMessages.getString(
        PKG, "SimpleMappingDialog.Exception.UnableToReferenceObjectId.Title" ), BaseMessages.getString(
        PKG, "SimpleMappingDialog.Exception.UnableToReferenceObjectId.Message" ), e );
    }
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
    setSpecificationMethod( mappingMeta.getSpecificationMethod() );
    switch( getSpecificationMethod() ) {
      case FILENAME:
        wFilename.setText( Const.NVL( mappingMeta.getFileName(), "" ) );
        break;
      case REPOSITORY_BY_NAME:
        wDirectory.setText( Const.NVL( mappingMeta.getDirectoryPath(), "" ) );
        wTransname.setText( Const.NVL( mappingMeta.getTransName(), "" ) );
        break;
      case REPOSITORY_BY_REFERENCE:
        wByReference.setText( "" );
        if ( mappingMeta.getTransObjectId() != null ) {
          setReferenceObjectId( mappingMeta.getTransObjectId() );
          getByReferenceData( getReferenceObjectId() );
        }
        break;
      default:
        break;
    }
    setRadioButtons();

    addInputMappingDefinitionTab( inputMapping, 0 );
    addOutputMappingDefinitionTab( outputMapping, 1 );
    addParametersTab( mappingParameters );
    wTabFolder.setSelection( 0 );

    try {
      loadTransformation();
    } catch ( Throwable t ) {
      // Ignore errors
    }

    wStepname.selectAll();
    wStepname.setFocus();
  }

  private void addOutputMappingDefinitionTab( MappingIODefinition definition, int index ) {
    addMappingDefinitionTab( definition, index + 1, BaseMessages.getString(
      PKG, "SimpleMappingDialog.OutputTab.Title" ), BaseMessages.getString(
      PKG, "SimpleMappingDialog.OutputTab.Tooltip" ), BaseMessages.getString(
      PKG, "SimpleMappingDialog.OutputTab.column.SourceField" ), BaseMessages.getString(
      PKG, "SimpleMappingDialog.OutputTab.column.TargetField" ), false );
  }

  private void addInputMappingDefinitionTab( MappingIODefinition definition, int index ) {
    addMappingDefinitionTab( definition, index + 1, BaseMessages.getString(
      PKG, "SimpleMappingDialog.InputTab.Title" ), BaseMessages.getString(
      PKG, "SimpleMappingDialog.InputTab.Tooltip" ), BaseMessages.getString(
      PKG, "SimpleMappingDialog.InputTab.column.SourceField" ), BaseMessages.getString(
      PKG, "SimpleMappingDialog.InputTab.column.TargetField" ), true );
  }

  private void addParametersTab( final MappingParameters parameters ) {

    CTabItem wParametersTab = new CTabItem( wTabFolder, SWT.NONE );
    wParametersTab.setText( BaseMessages.getString( PKG, "SimpleMappingDialog.Parameters.Title" ) );
    wParametersTab.setToolTipText( BaseMessages.getString( PKG, "SimpleMappingDialog.Parameters.Tooltip" ) );

    Composite wParametersComposite = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wParametersComposite );

    FormLayout parameterTabLayout = new FormLayout();
    parameterTabLayout.marginWidth = Const.FORM_MARGIN;
    parameterTabLayout.marginHeight = Const.FORM_MARGIN;
    wParametersComposite.setLayout( parameterTabLayout );

    // Add a checkbox: inherit all variables...
    //
    Button wInheritAll = new Button( wParametersComposite, SWT.CHECK );
    wInheritAll.setText( BaseMessages.getString( PKG, "SimpleMappingDialog.Parameters.InheritAll" ) );
    props.setLook( wInheritAll );
    FormData fdInheritAll = new FormData();
    fdInheritAll.bottom = new FormAttachment( 100, 0 );
    fdInheritAll.left = new FormAttachment( 0, 0 );
    fdInheritAll.right = new FormAttachment( 100, -30 );
    wInheritAll.setLayoutData( fdInheritAll );
    wInheritAll.setSelection( parameters.isInheritingAllVariables() );

    // Now add a tableview with the 2 columns to specify: input and output
    // fields for the source and target steps.
    //
    ColumnInfo[] colinfo =
      new ColumnInfo[] {
        new ColumnInfo(
          BaseMessages.getString( PKG, "SimpleMappingDialog.Parameters.column.Variable" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "SimpleMappingDialog.Parameters.column.ValueOrField" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false, false ), };
    colinfo[ 1 ].setUsingVariables( true );

    final TableView wMappingParameters =
      new TableView(
        transMeta, wParametersComposite, SWT.FULL_SELECTION | SWT.SINGLE | SWT.BORDER, colinfo, parameters
        .getVariable().length, lsMod, props
      );
    props.setLook( wMappingParameters );
    FormData fdMappings = new FormData();
    fdMappings.left = new FormAttachment( 0, 0 );
    fdMappings.right = new FormAttachment( 100, 0 );
    fdMappings.top = new FormAttachment( 0, 0 );
    fdMappings.bottom = new FormAttachment( wInheritAll, -margin * 2 );
    wMappingParameters.setLayoutData( fdMappings );

    for ( int i = 0; i < parameters.getVariable().length; i++ ) {
      TableItem tableItem = wMappingParameters.table.getItem( i );
      tableItem.setText( 1, parameters.getVariable()[ i ] );
      tableItem.setText( 2, parameters.getInputField()[ i ] );
    }
    wMappingParameters.setRowNums();
    wMappingParameters.optWidth( true );

    FormData fdParametersComposite = new FormData();
    fdParametersComposite.left = new FormAttachment( 0, 0 );
    fdParametersComposite.top = new FormAttachment( 0, 0 );
    fdParametersComposite.right = new FormAttachment( 100, 0 );
    fdParametersComposite.bottom = new FormAttachment( 100, 0 );
    wParametersComposite.setLayoutData( fdParametersComposite );

    wParametersComposite.layout();
    wParametersTab.setControl( wParametersComposite );

    changeList.add( new MappingParametersTab( wMappingParameters, wInheritAll, parameters ) );
  }

  protected String selectTransformationStepname( boolean getTransformationStep, boolean mappingInput ) {
    String dialogTitle = BaseMessages.getString( PKG, "SimpleMappingDialog.SelectTransStep.Title" );
    String dialogMessage = BaseMessages.getString( PKG, "SimpleMappingDialog.SelectTransStep.Message" );
    if ( getTransformationStep ) {
      dialogTitle = BaseMessages.getString( PKG, "SimpleMappingDialog.SelectTransStep.Title" );
      dialogMessage = BaseMessages.getString( PKG, "SimpleMappingDialog.SelectTransStep.Message" );
      String[] stepnames;
      if ( mappingInput ) {
        stepnames = transMeta.getPrevStepNames( stepMeta );
      } else {
        stepnames = transMeta.getNextStepNames( stepMeta );
      }
      EnterSelectionDialog dialog = new EnterSelectionDialog( shell, stepnames, dialogTitle, dialogMessage );
      return dialog.open();
    } else {
      dialogTitle = BaseMessages.getString( PKG, "SimpleMappingDialog.SelectMappingStep.Title" );
      dialogMessage = BaseMessages.getString( PKG, "SimpleMappingDialog.SelectMappingStep.Message" );

      String[] stepnames = getMappingSteps( mappingTransMeta, mappingInput );
      EnterSelectionDialog dialog = new EnterSelectionDialog( shell, stepnames, dialogTitle, dialogMessage );
      return dialog.open();
    }
  }

  public static String[] getMappingSteps( TransMeta mappingTransMeta, boolean mappingInput ) {
    List<StepMeta> steps = new ArrayList<StepMeta>();
    for ( StepMeta stepMeta : mappingTransMeta.getSteps() ) {
      if ( mappingInput && stepMeta.getStepID().equals( "MappingInput" ) ) {
        steps.add( stepMeta );
      }
      if ( !mappingInput && stepMeta.getStepID().equals( "MappingOutput" ) ) {
        steps.add( stepMeta );
      }
    }
    String[] stepnames = new String[ steps.size() ];
    for ( int i = 0; i < stepnames.length; i++ ) {
      stepnames[ i ] = steps.get( i ).getName();
    }

    return stepnames;

  }

  public RowMetaInterface getFieldsFromStep( boolean parent, boolean input ) throws KettleException {
    if ( input ) {
      // INPUT
      //
      if ( parent ) {
        return transMeta.getPrevStepFields( stepMeta );
      } else {
        if ( mappingTransMeta == null ) {
          throw new KettleException( BaseMessages.getString(
            PKG, "SimpleMappingDialog.Exception.NoMappingSpecified" ) );
        }
        StepMeta mappingInputStepMeta = mappingTransMeta.findMappingInputStep( null );
        return mappingTransMeta.getStepFields( mappingInputStepMeta );
      }
    } else {
      // OUTPUT
      //
      StepMeta mappingOutputStepMeta = mappingTransMeta.findMappingOutputStep( null );
      return mappingTransMeta.getStepFields( mappingOutputStepMeta );
    }
  }

  private void addMappingDefinitionTab( final MappingIODefinition definition, int index, final String tabTitle,
                                        final String tabTooltip, String sourceColumnLabel, String targetColumnLabel,
                                        final boolean input ) {

    final CTabItem wTab;
    if ( index >= wTabFolder.getItemCount() ) {
      wTab = new CTabItem( wTabFolder, SWT.CLOSE );
    } else {
      wTab = new CTabItem( wTabFolder, SWT.CLOSE, index );
    }
    setMappingDefinitionTabNameAndToolTip( wTab, tabTitle, tabTooltip, definition, input );

    Composite wInputComposite = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wInputComposite );

    FormLayout tabLayout = new FormLayout();
    tabLayout.marginWidth = Const.FORM_MARGIN;
    tabLayout.marginHeight = Const.FORM_MARGIN;
    wInputComposite.setLayout( tabLayout );

    // Now add a table view with the 2 columns to specify: input and output
    // fields for the source and target steps.
    //
    final Button wbEnterMapping = new Button( wInputComposite, SWT.PUSH );
    props.setLook( wbEnterMapping );
    if ( input ) {
      wbEnterMapping.setText( BaseMessages.getString( PKG, "SimpleMappingDialog.button.EnterMapping" ) );
    } else {
      wbEnterMapping.setText( BaseMessages.getString( PKG, "SimpleMappingDialog.button.GetFields" ) );
    }
    FormData fdbEnterMapping = new FormData();
    fdbEnterMapping.top = new FormAttachment( 0, margin * 2 );
    fdbEnterMapping.right = new FormAttachment( 100, 0 );
    wbEnterMapping.setLayoutData( fdbEnterMapping );

    ColumnInfo[] colinfo =
      new ColumnInfo[] {
        new ColumnInfo( sourceColumnLabel, ColumnInfo.COLUMN_TYPE_TEXT, false, false ),
        new ColumnInfo( targetColumnLabel, ColumnInfo.COLUMN_TYPE_TEXT, false, false ), };
    final TableView wFieldMappings =
      new TableView(
        transMeta, wInputComposite, SWT.FULL_SELECTION | SWT.SINGLE | SWT.BORDER, colinfo, 1, lsMod, props );
    props.setLook( wFieldMappings );
    FormData fdMappings = new FormData();
    fdMappings.left = new FormAttachment( 0, 0 );
    fdMappings.right = new FormAttachment( wbEnterMapping, -margin );
    fdMappings.top = new FormAttachment( 0, margin * 2 );
    fdMappings.bottom = new FormAttachment( 100, -50 );
    wFieldMappings.setLayoutData( fdMappings );
    Control lastControl = wFieldMappings;

    for ( MappingValueRename valueRename : definition.getValueRenames() ) {
      TableItem tableItem = new TableItem( wFieldMappings.table, SWT.NONE );
      tableItem.setText( 1, Const.NVL( valueRename.getSourceValueName(), "" ) );
      tableItem.setText( 2, Const.NVL( valueRename.getTargetValueName(), "" ) );
    }
    wFieldMappings.removeEmptyRows();
    wFieldMappings.setRowNums();
    wFieldMappings.optWidth( true );

    wbEnterMapping.addSelectionListener( new SelectionAdapter() {

      @Override
      public void widgetSelected( SelectionEvent arg0 ) {
        try {
          if ( input ) {
            // INPUT
            //
            RowMetaInterface sourceRowMeta = getFieldsFromStep( true, input );
            RowMetaInterface targetRowMeta = getFieldsFromStep( false, input );
            String[] sourceFields = sourceRowMeta.getFieldNames();
            String[] targetFields = targetRowMeta.getFieldNames();

            EnterMappingDialog dialog = new EnterMappingDialog( shell, sourceFields, targetFields );
            List<SourceToTargetMapping> mappings = dialog.open();
            if ( mappings != null ) {
              // first clear the dialog...
              wFieldMappings.clearAll( false );

              //
              definition.getValueRenames().clear();

              // Now add the new values...
              for ( int i = 0; i < mappings.size(); i++ ) {
                SourceToTargetMapping mapping = mappings.get( i );
                TableItem item = new TableItem( wFieldMappings.table, SWT.NONE );
                item.setText( 1, mapping.getSourceString( sourceFields ) );
                item.setText( 2, mapping.getTargetString( targetFields ) );

                String source = input ? item.getText( 1 ) : item.getText( 2 );
                String target = input ? item.getText( 2 ) : item.getText( 1 );
                definition.getValueRenames().add( new MappingValueRename( source, target ) );
              }
              wFieldMappings.removeEmptyRows();
              wFieldMappings.setRowNums();
              wFieldMappings.optWidth( true );
            }
          } else {
            // OUTPUT
            //
            RowMetaInterface sourceRowMeta = getFieldsFromStep( true, input );
            BaseStepDialog.getFieldsFromPrevious(
              sourceRowMeta, wFieldMappings, 1, new int[] { 1, }, new int[] { }, -1, -1, null );
          }
        } catch ( KettleException e ) {
          new ErrorDialog( shell, BaseMessages.getString( PKG, "System.Dialog.Error.Title" ), BaseMessages
            .getString( PKG, "SimpleMappingDialog.Exception.ErrorGettingMappingSourceAndTargetFields", e
              .toString() ), e );
        }
      }

    } );

    if ( input ) {
      Button wRenameOutput = new Button( wInputComposite, SWT.CHECK );
      props.setLook( wRenameOutput );
      wRenameOutput.setText( BaseMessages.getString( PKG, "SimpleMappingDialog.input.RenamingOnOutput" ) );
      FormData fdRenameOutput = new FormData();
      fdRenameOutput.top = new FormAttachment( lastControl, margin );
      fdRenameOutput.left = new FormAttachment( 0, 0 );
      fdRenameOutput.right = new FormAttachment( 100, 0 );
      wRenameOutput.setLayoutData( fdRenameOutput );

      wRenameOutput.setSelection( definition.isRenamingOnOutput() );
      wRenameOutput.addSelectionListener( new SelectionAdapter() {
        @Override
        public void widgetSelected( SelectionEvent event ) {
          // flip the switch
          definition.setRenamingOnOutput( !definition.isRenamingOnOutput() );
        }
      } );

      lastControl = wRenameOutput;
    }

    FormData fdParametersComposite = new FormData();
    fdParametersComposite.left = new FormAttachment( 0, 0 );
    fdParametersComposite.top = new FormAttachment( 0, 0 );
    fdParametersComposite.right = new FormAttachment( 100, 0 );
    fdParametersComposite.bottom = new FormAttachment( 100, 0 );
    wInputComposite.setLayoutData( fdParametersComposite );

    wInputComposite.layout();
    wTab.setControl( wInputComposite );

    final ApplyChanges applyChanges = new MappingDefinitionTab( definition, wFieldMappings );
    changeList.add( applyChanges );

    wTabFolder.setSelection( wTab );

  }

  private void setMappingDefinitionTabNameAndToolTip( CTabItem wTab, String tabTitle, String tabTooltip,
                                                      MappingIODefinition definition, boolean input ) {

    String stepname;
    if ( input ) {
      stepname = definition.getInputStepname();
    } else {
      stepname = definition.getOutputStepname();
    }
    String description = definition.getDescription();

    if ( Const.isEmpty( stepname ) ) {
      wTab.setText( tabTitle );
    } else {
      wTab.setText( tabTitle + " : " + stepname );
    }
    String tooltip = tabTooltip;
    if ( !Const.isEmpty( stepname ) ) {
      tooltip += Const.CR + Const.CR + stepname;
    }
    if ( !Const.isEmpty( description ) ) {
      tooltip += Const.CR + Const.CR + description;
    }
    wTab.setToolTipText( tooltip );
  }

  private void cancel() {
    stepname = null;
    mappingMeta.setChanged( changed );
    dispose();
  }

  private void ok() {
    if ( Const.isEmpty( wStepname.getText() ) ) {
      return;
    }

    stepname = wStepname.getText(); // return value

    try {
      loadTransformation();
    } catch ( KettleException e ) {
      new ErrorDialog( shell, BaseMessages.getString(
        PKG, "SimpleMappingDialog.ErrorLoadingSpecifiedTransformation.Title" ), BaseMessages.getString(
        PKG, "SimpleMappingDialog.ErrorLoadingSpecifiedTransformation.Message" ), e );
      return;
    }

    mappingMeta.setSpecificationMethod( getSpecificationMethod() );
    switch( getSpecificationMethod() ) {
      case FILENAME:
        mappingMeta.setFileName( wFilename.getText() );
        mappingMeta.setDirectoryPath( null );
        mappingMeta.setTransName( null );
        mappingMeta.setTransObjectId( null );
        break;
      case REPOSITORY_BY_NAME:
        mappingMeta.setDirectoryPath( wDirectory.getText() );
        mappingMeta.setTransName( wTransname.getText() );
        mappingMeta.setFileName( null );
        mappingMeta.setTransObjectId( null );
        break;
      case REPOSITORY_BY_REFERENCE:
        mappingMeta.setFileName( null );
        mappingMeta.setDirectoryPath( null );
        mappingMeta.setTransName( null );
        mappingMeta.setTransObjectId( getReferenceObjectId() );
        break;
      default:
        break;
    }

    // Load the information on the tabs, optionally do some
    // verifications...
    //
    collectInformation();

    mappingMeta.setMappingParameters( mappingParameters );
    mappingMeta.setInputMapping( inputMapping );
    mappingMeta.setOutputMapping( outputMapping );
    mappingMeta.setChanged( true );

    dispose();
  }

  private void collectInformation() {
    for ( ApplyChanges applyChanges : changeList ) {
      applyChanges.applyChanges(); // collect information from all
      // tabs...
    }
  }

  // Method is defined as package-protected in order to be accessible by unit tests
  ObjectId getReferenceObjectId() {
    return referenceObjectId;
  }

  private void setReferenceObjectId( ObjectId referenceObjectId ) {
    this.referenceObjectId = referenceObjectId;
  }

  // Method is defined as package-protected in order to be accessible by unit tests
  ObjectLocationSpecificationMethod getSpecificationMethod() {
    return specificationMethod;
  }

  private void setSpecificationMethod( ObjectLocationSpecificationMethod specificationMethod ) {
    this.specificationMethod = specificationMethod;
  }

}
