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

package org.pentaho.di.ui.trans.steps.mapping;

import org.apache.commons.vfs2.FileObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.SourceToTargetMapping;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
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
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.mapping.MappingIODefinition;
import org.pentaho.di.trans.steps.mapping.MappingMeta;
import org.pentaho.di.trans.steps.mapping.MappingParameters;
import org.pentaho.di.trans.steps.mapping.MappingValueRename;
import org.pentaho.di.ui.core.ConstUI;
import org.pentaho.di.ui.core.dialog.EnterMappingDialog;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.dialog.WarningDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.ColumnsResizer;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.util.DialogHelper;
import org.pentaho.di.ui.util.DialogUtils;
import org.pentaho.di.ui.util.MappingUtil;
import org.pentaho.di.ui.util.SwtSvgImageUtil;
import org.pentaho.vfs.ui.VfsFileChooserDialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MappingDialog extends BaseStepDialog implements StepDialogInterface {
  private static Class<?> PKG = MappingMeta.class; // for i18n purposes, needed by Translator2!!

  private MappingMeta mappingMeta;

  private Label wlPath;
  private TextVar wPath;

  private Button wbBrowse;

  private CTabFolder wTabFolder;

  private TransMeta mappingTransMeta = null;

  protected boolean transModified;

  private ModifyListener lsMod;

  private MappingParameters mappingParameters;

  private List<MappingIODefinition> inputMappings;

  private List<MappingIODefinition> outputMappings;

  private ObjectId referenceObjectId;
  private ObjectLocationSpecificationMethod specificationMethod;

  private interface ApplyChanges {
    void applyChanges();
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

    private Text wInputStep;

    private Text wOutputStep;

    private Button wMainPath;

    private Text wDescription;

    private TableView wFieldMappings;

    public MappingDefinitionTab( MappingIODefinition definition, Text inputStep, Text outputStep, Button mainPath,
                                 Text description, TableView fieldMappings ) {
      super();
      this.definition = definition;
      wInputStep = inputStep;
      wOutputStep = outputStep;
      wMainPath = mainPath;
      wDescription = description;
      wFieldMappings = fieldMappings;
    }

    public void applyChanges() {

      // The input step
      definition.setInputStepname( wInputStep.getText() );

      // The output step
      definition.setOutputStepname( wOutputStep.getText() );

      // The description
      definition.setDescription( wDescription.getText() );

      // The main path flag
      definition.setMainDataPath( wMainPath.getSelection() );

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

  private ApplyChanges parameterChanges;
  private ApplyChanges tabChanges;

  public MappingDialog( Shell parent, Object in, TransMeta tr, String sname ) {
    super( parent, (BaseStepMeta) in, tr, sname );
    mappingMeta = (MappingMeta) in;
    transModified = false;

    // Make a copy for our own purposes...
    // This allows us to change everything directly in the classes with
    // listeners.
    // Later we need to copy it to the input class on ok()
    //
    mappingParameters = (MappingParameters) mappingMeta.getMappingParameters().clone();
    inputMappings = new ArrayList<MappingIODefinition>();
    outputMappings = new ArrayList<MappingIODefinition>();
    for ( int i = 0; i < mappingMeta.getInputMappings().size(); i++ ) {
      inputMappings.add( (MappingIODefinition) mappingMeta.getInputMappings().get( i ).clone() );
    }
    for ( int i = 0; i < mappingMeta.getOutputMappings().size(); i++ ) {
      outputMappings.add( (MappingIODefinition) mappingMeta.getOutputMappings().get( i ).clone() );
    }
  }

  private static int CONST_WIDTH = 200;

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
    formLayout.marginWidth = 15;
    formLayout.marginHeight = 15;

    shell.setLayout( formLayout );
    shell.setText( BaseMessages.getString( PKG, "MappingDialog.Shell.Title" ) );

    Label wicon = new Label( shell, SWT.RIGHT );
    wicon.setImage( getImage() );
    FormData fdlicon = new FormData();
    fdlicon.top = new FormAttachment( 0, 0 );
    fdlicon.right = new FormAttachment( 100, 0 );
    wicon.setLayoutData( fdlicon );
    props.setLook( wicon );

    // Stepname line
    wlStepname = new Label( shell, SWT.RIGHT );
    wlStepname.setText( BaseMessages.getString( PKG, "MappingDialog.Stepname.Label" ) );
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
    wlPath.setText( BaseMessages.getString( PKG, "MappingDialog.Transformation.Label" ) );
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
    wbBrowse.setText( BaseMessages.getString( PKG, "MappingDialog.Browse.Label" ) );
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

    wCancel.addListener( SWT.Selection, lsCancel );
    wOK.addListener( SWT.Selection, lsOK );

    lsDef = new SelectionAdapter() {
      public void widgetDefaultSelected( SelectionEvent e ) {
        ok();
      }
    };

    wStepname.addSelectionListener( lsDef );

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    // Set the shell size, based upon previous time...
    setSize( shell, 670, 690 );

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

  protected Image getImage() {
    return SwtSvgImageUtil
      .getImage( shell.getDisplay(), getClass().getClassLoader(), "MAP.svg", ConstUI.LARGE_ICON_SIZE,
        ConstUI.LARGE_ICON_SIZE );
  }

  private void selectRepositoryTrans() {
    RepositoryObject repositoryObject = DialogHelper.selectRepositoryObject( "*.ktr", log );

    try {
      if ( repositoryObject != null ) {
        loadRepositoryTrans( repositoryObject.getName(), repositoryObject.getRepositoryDirectory() );
        String path = DialogUtils
          .getPath( transMeta.getRepositoryDirectory().getPath(), mappingTransMeta.getRepositoryDirectory().getPath() );
        String fullPath = ( path.equals( "/" ) ? "/" : path + "/" ) + mappingTransMeta.getName();
        wPath.setText( fullPath );
        specificationMethod = ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME;
      }
    } catch ( KettleException ke ) {
      new ErrorDialog( shell,
        BaseMessages.getString( PKG, "MappingDialog.ErrorSelectingObject.DialogTitle" ),
        BaseMessages.getString( PKG, "MappingDialog.ErrorSelectingObject.DialogMessage" ), ke );
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
        BaseMessages.getString( PKG, "MappingDialog.ErrorLoadingTransformation.DialogTitle" ),
        BaseMessages.getString( PKG, "MappingDialog.ErrorLoadingTransformation.DialogMessage" ), e );
    }
  }

  private void loadFileTrans( String fname ) throws KettleException {
    mappingTransMeta = new TransMeta( transMeta.environmentSubstitute( fname ) );
    mappingTransMeta.clearChanged();
  }

  // Method is defined as package-protected in order to be accessible by unit tests
  void loadTransformation() throws KettleException {
    String filename = wPath.getText();
    if ( repository != null ) {
      specificationMethod = ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME;
    } else {
      specificationMethod = ObjectLocationSpecificationMethod.FILENAME;
    }
    switch ( getSpecificationMethod() ) {
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
        if ( filename.endsWith( ".ktr" ) ) {
          filename = filename.replace( ".ktr", "" );
          wPath.setText( filename );
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
            BaseMessages.getString( PKG, "MappingDialog.Exception.NoValidMappingDetailsFound" ) );
        }
        RepositoryDirectoryInterface repdir = repository.findDirectory( realDirectory );
        if ( repdir == null ) {
          throw new KettleException( BaseMessages.getString(
            PKG, "MappingDialog.Exception.UnableToFindRepositoryDirectory" ) );
        }
        loadRepositoryTrans( realTransname, repdir );
        break;
      default:
        break;
    }
  }

  private void getByReferenceData( ObjectId transObjectId ) {
    try {
      if ( repository == null ) {
        throw new KettleException( BaseMessages.getString(
          PKG, "MappingDialog.Exception.NotConnectedToRepository.Message" ) );
      }
      RepositoryObject transInf = repository.getObjectInformation( transObjectId, RepositoryObjectType.TRANSFORMATION );
      String path = DialogUtils
        .getPath( transMeta.getRepositoryDirectory().getPath(), transInf.getRepositoryDirectory().getPath() );
      String fullPath =
        Const.NVL( path, "" ) + "/" + Const.NVL( transInf.getName(), "" );
      wPath.setText( fullPath );
    } catch ( KettleException e ) {
      new ErrorDialog( shell, BaseMessages.getString(
        PKG, "MappingDialog.Exception.UnableToReferenceObjectId.Title" ), BaseMessages.getString(
        PKG, "MappingDialog.Exception.UnableToReferenceObjectId.Message" ), e );
    }
  }

  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void getData() {
    setSpecificationMethod( mappingMeta.getSpecificationMethod() );
    switch ( getSpecificationMethod() ) {
      case FILENAME:
        wPath.setText( Const.NVL( mappingMeta.getFileName(), "" ) );
        break;
      case REPOSITORY_BY_NAME:
        String fullPath = Const.NVL( mappingMeta.getDirectoryPath(), "" ) + "/" + Const
          .NVL( mappingMeta.getTransName(), "" );
        wPath.setText( fullPath );
        break;
      case REPOSITORY_BY_REFERENCE:
        referenceObjectId = mappingMeta.getTransObjectId();
        getByReferenceData( referenceObjectId );
        break;
      default:
        break;
    }

    // Add the parameters tab
    addParametersTab( mappingParameters );
    wTabFolder.setSelection( 0 );

    // Now add the input stream tabs: where is our data coming from?
    addInputMappingDefinitionTab();
    addOutputMappingDefinitionTab();

    try {
      loadTransformation();
    } catch ( Throwable t ) {
      // Ignore errors
    }

    wStepname.selectAll();
    wStepname.setFocus();
  }

  private void addInputMappingDefinitionTab() {
    addMappingDefinitionTab( inputMappings, BaseMessages.getString( PKG, "MappingDialog.InputTab.Title" ),
      BaseMessages.getString( PKG, "MappingDialog.label.AvailableInputs" ),
      BaseMessages.getString( PKG, "MappingDialog.label.AddInput" ),
      BaseMessages.getString( PKG, "MappingDialog.label.RemoveInput" ),
      BaseMessages.getString( PKG, "MappingDialog.InputTab.label.InputSourceStepName" ), BaseMessages.getString(
        PKG, "MappingDialog.InputTab.label.OutputTargetStepName" ), BaseMessages.getString(
        PKG, "MappingDialog.InputTab.label.Description" ), BaseMessages.getString(
        PKG, "MappingDialog.InputTab.column.SourceField" ), BaseMessages.getString(
        PKG, "MappingDialog.InputTab.column.TargetField" ), BaseMessages.getString(
        PKG, "MappingDialog.InputTab.label.NoItems" ), true
    );
  }

  private void addOutputMappingDefinitionTab() {
    addMappingDefinitionTab( outputMappings, BaseMessages.getString( PKG, "MappingDialog.OutputTab.Title" ),
      BaseMessages.getString( PKG, "MappingDialog.label.AvailableOutputs" ),
      BaseMessages.getString( PKG, "MappingDialog.label.AddOutput" ),
      BaseMessages.getString( PKG, "MappingDialog.label.RemoveOutput" ),
      BaseMessages.getString( PKG, "MappingDialog.OutputTab.label.InputSourceStepName" ), BaseMessages.getString(
        PKG, "MappingDialog.OutputTab.label.OutputTargetStepName" ), BaseMessages.getString(
        PKG, "MappingDialog.OutputTab.label.Description" ), BaseMessages.getString(
        PKG, "MappingDialog.OutputTab.column.SourceField" ), BaseMessages.getString(
        PKG, "MappingDialog.OutputTab.column.TargetField" ), BaseMessages.getString(
        PKG, "MappingDialog.OutputTab.label.NoItems" ), false );
  }

  private void addParametersTab( final MappingParameters parameters ) {

    CTabItem wParametersTab = new CTabItem( wTabFolder, SWT.NONE );
    wParametersTab.setText( BaseMessages.getString( PKG, "MappingDialog.Parameters.Title" ) );
    wParametersTab.setToolTipText( BaseMessages.getString( PKG, "MappingDialog.Parameters.Tooltip" ) );

    Composite wParametersComposite = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wParametersComposite );

    FormLayout parameterTabLayout = new FormLayout();
    parameterTabLayout.marginWidth = 15;
    parameterTabLayout.marginHeight = 15;
    wParametersComposite.setLayout( parameterTabLayout );

    // Add a checkbox: inherit all variables...
    //
    Button wInheritAll = new Button( wParametersComposite, SWT.CHECK );
    wInheritAll.setText( BaseMessages.getString( PKG, "MappingDialog.Parameters.InheritAll" ) );
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
          BaseMessages.getString( PKG, "MappingDialog.Parameters.column.Variable" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "MappingDialog.Parameters.column.ValueOrField" ),
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
    fdMappings.bottom = new FormAttachment( wInheritAll, -10 );
    wMappingParameters.setLayoutData( fdMappings );
    wMappingParameters.getTable().addListener( SWT.Resize, new ColumnsResizer( 0, 50, 50 ) );

    for ( int i = 0; i < parameters.getVariable().length; i++ ) {
      TableItem tableItem = wMappingParameters.table.getItem( i );
      tableItem.setText( 1, Const.NVL( parameters.getVariable()[ i ], "" ) );
      tableItem.setText( 2, Const.NVL( parameters.getInputField()[ i ], "" ) );
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

    parameterChanges = new MappingParametersTab( wMappingParameters, wInheritAll, parameters );
  }

  protected String selectTransformationStepname( boolean getTransformationStep, boolean mappingInput ) {
    String dialogTitle;
    String dialogMessage;
    if ( getTransformationStep ) {
      dialogTitle = BaseMessages.getString( PKG, "MappingDialog.SelectTransStep.Title" );
      dialogMessage = BaseMessages.getString( PKG, "MappingDialog.SelectTransStep.Message" );
      String[] stepnames;
      if ( mappingInput ) {
        stepnames = transMeta.getPrevStepNames( stepMeta );
      } else {
        stepnames = transMeta.getNextStepNames( stepMeta );
      }
      EnterSelectionDialog dialog = new EnterSelectionDialog( shell, stepnames, dialogTitle, dialogMessage );
      return dialog.open();
    } else {
      dialogTitle = BaseMessages.getString( PKG, "MappingDialog.SelectMappingStep.Title" );
      dialogMessage = BaseMessages.getString( PKG, "MappingDialog.SelectMappingStep.Message" );

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

  public RowMetaInterface getFieldsFromStep( String stepname, boolean getTransformationStep, boolean mappingInput ) throws KettleException {
    if ( !( mappingInput ^ getTransformationStep ) ) {
      if ( Utils.isEmpty( stepname ) ) {
        // If we don't have a specified stepname we return the input row
        // metadata
        //
        return transMeta.getPrevStepFields( this.stepname );
      } else {
        // OK, a fieldname is specified...
        // See if we can find it...
        StepMeta stepMeta = transMeta.findStep( stepname );
        if ( stepMeta == null ) {
          throw new KettleException( BaseMessages.getString(
            PKG, "MappingDialog.Exception.SpecifiedStepWasNotFound", stepname ) );
        }
        return transMeta.getStepFields( stepMeta );
      }

    } else {
      if ( mappingTransMeta == null ) {
        throw new KettleException( BaseMessages.getString( PKG, "MappingDialog.Exception.NoMappingSpecified" ) );
      }

      if ( Utils.isEmpty( stepname ) ) {
        // If we don't have a specified stepname we select the one and
        // only "mapping input" step.
        //
        String[] stepnames = getMappingSteps( mappingTransMeta, mappingInput );
        if ( stepnames.length > 1 ) {
          throw new KettleException( BaseMessages.getString(
            PKG, "MappingDialog.Exception.OnlyOneMappingInputStepAllowed", "" + stepnames.length ) );
        }
        if ( stepnames.length == 0 ) {
          throw new KettleException( BaseMessages.getString(
            PKG, "MappingDialog.Exception.OneMappingInputStepRequired", "" + stepnames.length ) );
        }
        return mappingTransMeta.getStepFields( stepnames[ 0 ] );
      } else {
        // OK, a fieldname is specified...
        // See if we can find it...
        StepMeta stepMeta = mappingTransMeta.findStep( stepname );
        if ( stepMeta == null ) {
          throw new KettleException( BaseMessages.getString(
            PKG, "MappingDialog.Exception.SpecifiedStepWasNotFound", stepname ) );
        }
        return mappingTransMeta.getStepFields( stepMeta );
      }
    }
  }

  private void addMappingDefinitionTab( List<MappingIODefinition> definitions, final String tabTitle, String listLabel,
                                        String addToolTip, String removeToolTip, String inputStepLabel,
                                        String outputStepLabel, String descriptionLabel, String sourceColumnLabel,
                                        String targetColumnLabel, String noItemsLabel, final boolean input ) {

    final CTabItem wTab = new CTabItem( wTabFolder, SWT.NONE );
    wTab.setText( tabTitle );

    Composite wInputComposite = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wInputComposite );

    FormLayout tabLayout = new FormLayout();
    tabLayout.marginWidth = 15;
    tabLayout.marginHeight = 15;
    wInputComposite.setLayout( tabLayout );

    Label wAvailableInputs = new Label( wInputComposite, SWT.LEFT );
    props.setLook( wAvailableInputs );
    wAvailableInputs.setText( listLabel );
    FormData fdwAvailableInputs = new FormData();
    fdwAvailableInputs.left = new FormAttachment( 0 );
    fdwAvailableInputs.top = new FormAttachment( 0 );

    Label wRemoveButton = new Label( wInputComposite, SWT.NONE );
    wRemoveButton.setImage( GUIResource.getInstance().getImage( "ui/images/generic-delete.svg" ) );
    wRemoveButton.setToolTipText( removeToolTip );
    props.setLook( wRemoveButton );
    FormData fdwAddInputButton = new FormData();
    fdwAddInputButton.top = new FormAttachment( 0 );
    fdwAddInputButton.right = new FormAttachment( 30 );
    wRemoveButton.setLayoutData( fdwAddInputButton );

    Label wAddButton = new Label( wInputComposite, SWT.NONE );
    wAddButton.setImage( GUIResource.getInstance().getImage( "ui/images/Add.svg" ) );
    wAddButton.setToolTipText( addToolTip );
    props.setLook( wAddButton );
    FormData fdwAddButton = new FormData();
    fdwAddButton.top = new FormAttachment( 0 );
    fdwAddButton.right = new FormAttachment( wRemoveButton, -5 );
    wAddButton.setLayoutData( fdwAddButton );

    org.eclipse.swt.widgets.List wInputList = new org.eclipse.swt.widgets.List( wInputComposite, SWT.BORDER );
    FormData fdwInputList = new FormData();
    fdwInputList.left = new FormAttachment( 0 );
    fdwInputList.top = new FormAttachment( wAvailableInputs, 5 );
    fdwInputList.bottom = new FormAttachment( 100 );
    fdwInputList.right = new FormAttachment( 30 );
    wInputList.setLayoutData( fdwInputList );

    for ( int i = 0; i < definitions.size(); i++ ) {
      String label =
        !Utils.isEmpty( definitions.get( i ).getInputStepname() ) ? definitions.get( i ).getInputStepname()
          : tabTitle + ( i > 0 ? String.valueOf( i + 1 ) : "" );
      wInputList.add( label );
    }

    final Label wlNoItems = new Label( wInputComposite, SWT.CENTER );
    wlNoItems.setText( noItemsLabel );
    props.setLook( wlNoItems );
    FormData fdlNoItems = new FormData();
    fdlNoItems.left = new FormAttachment( wInputList, 30 );
    fdlNoItems.right = new FormAttachment( 100 );
    fdlNoItems.top = new FormAttachment( 50 );
    wlNoItems.setLayoutData( fdlNoItems );
    wlNoItems.setVisible( false );

    Composite wFieldsComposite = new Composite( wInputComposite, SWT.NONE );
    props.setLook( wFieldsComposite );
    FormLayout fieldLayout = new FormLayout();
    fieldLayout.marginWidth = 0;
    fieldLayout.marginHeight = 0;
    wFieldsComposite.setLayout( fieldLayout );

    final Button wMainPath = new Button( wFieldsComposite, SWT.CHECK );
    wMainPath.setText( BaseMessages.getString( PKG, "MappingDialog.input.MainDataPath" )  );
    props.setLook( wMainPath );
    FormData fdMainPath = new FormData();
    fdMainPath.top = new FormAttachment( 0 );
    fdMainPath.left = new FormAttachment( 0 );
    wMainPath.setLayoutData( fdMainPath );
    wMainPath.addSelectionListener( new SelectionAdapter() {

      @Override
      public void widgetSelected( SelectionEvent event ) {
        definitions.get( wInputList.getSelectionIndex() )
          .setMainDataPath( !definitions.get( wInputList.getSelectionIndex() ).isMainDataPath() );
      }

    } );

    final Label wlInputStep = new Label( wFieldsComposite, SWT.RIGHT );
    props.setLook( wlInputStep );
    wlInputStep.setText( inputStepLabel );
    FormData fdlInputStep = new FormData();
    fdlInputStep.top = new FormAttachment( wMainPath, 10 );
    fdlInputStep.left = new FormAttachment( 0 );
    wlInputStep.setLayoutData( fdlInputStep );

    // What's the stepname to read from? (empty is OK too)
    //
    final Button wbInputStep = new Button( wFieldsComposite, SWT.PUSH );
    props.setLook( wbInputStep );
    wbInputStep.setText( BaseMessages.getString( PKG, "MappingDialog.button.SourceStepName" ) );
    FormData fdbInputStep = new FormData();
    fdbInputStep.top = new FormAttachment( wlInputStep, 5 );
    fdbInputStep.right = new FormAttachment( 100 ); // First one in the
    // left top corner
    wbInputStep.setLayoutData( fdbInputStep );

    final Text wInputStep = new Text( wFieldsComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wInputStep );
    wInputStep.addModifyListener( lsMod );
    FormData fdInputStep = new FormData();
    fdInputStep.top = new FormAttachment( wlInputStep, 5 );
    fdInputStep.left = new FormAttachment( 0 ); // To the right of
    // the label
    fdInputStep.right = new FormAttachment( wbInputStep, -5 );
    wInputStep.setLayoutData( fdInputStep );
    wInputStep.addFocusListener( new FocusAdapter() {
      @Override
      public void focusLost( FocusEvent event ) {
        definitions.get( wInputList.getSelectionIndex() ).setInputStepname( wInputStep.getText() );
        String label = !Utils.isEmpty( wInputStep.getText() ) ? wInputStep.getText()
          : tabTitle + ( wInputList.getSelectionIndex() > 0 ? String.valueOf( wInputList.getSelectionIndex() + 1 )
          : "" );
        wInputList.setItem( wInputList.getSelectionIndex(), label );
      }
    } );
    wbInputStep.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent event ) {
        String stepName = selectTransformationStepname( input, input );
        if ( stepName != null ) {
          wInputStep.setText( stepName );
          definitions.get( wInputList.getSelectionIndex() ).setInputStepname( stepName );
        }
      }
    } );

    // What's the step name to read from? (empty is OK too)
    //
    final Label wlOutputStep = new Label( wFieldsComposite, SWT.RIGHT );
    props.setLook( wlOutputStep );
    wlOutputStep.setText( outputStepLabel );
    FormData fdlOutputStep = new FormData();
    fdlOutputStep.top = new FormAttachment( wInputStep, 10 );
    fdlOutputStep.left = new FormAttachment( 0 );
    wlOutputStep.setLayoutData( fdlOutputStep );

    final Button wbOutputStep = new Button( wFieldsComposite, SWT.PUSH );
    props.setLook( wbOutputStep );
    wbOutputStep.setText( BaseMessages.getString( PKG, "MappingDialog.button.SourceStepName" ) );
    FormData fdbOutputStep = new FormData();
    fdbOutputStep.top = new FormAttachment( wlOutputStep, 5 );
    fdbOutputStep.right = new FormAttachment( 100 );
    wbOutputStep.setLayoutData( fdbOutputStep );

    final Text wOutputStep = new Text( wFieldsComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wOutputStep );
    wOutputStep.addModifyListener( lsMod );
    FormData fdOutputStep = new FormData();
    fdOutputStep.top = new FormAttachment( wlOutputStep, 5 );
    fdOutputStep.left = new FormAttachment( 0 ); // To the right of
    // the label
    fdOutputStep.right = new FormAttachment( wbOutputStep, -5 );
    wOutputStep.setLayoutData( fdOutputStep );

    // Allow for a small description
    //
    Label wlDescription = new Label( wFieldsComposite, SWT.RIGHT );
    props.setLook( wlDescription );
    wlDescription.setText( descriptionLabel );
    FormData fdlDescription = new FormData();
    fdlDescription.top = new FormAttachment( wOutputStep, 5 );
    fdlDescription.left = new FormAttachment( 0 ); // First one in the left
    wlDescription.setLayoutData( fdlDescription );

    final Text wDescription = new Text( wFieldsComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wDescription );
    wDescription.addModifyListener( lsMod );
    FormData fdDescription = new FormData();
    fdDescription.top = new FormAttachment( wlDescription, 5 );
    fdDescription.left = new FormAttachment( 0 ); // To the right of
    // the label
    fdDescription.right = new FormAttachment( 100 );
    wDescription.setLayoutData( fdDescription );
    wDescription.addFocusListener( new FocusAdapter() {
      @Override
      public void focusLost( FocusEvent event ) {
        definitions.get( wInputList.getSelectionIndex() ).setDescription( wDescription.getText() );
      }
    } );

    final Button wbEnterMapping = new Button( wFieldsComposite, SWT.PUSH );
    props.setLook( wbEnterMapping );
    wbEnterMapping.setText( BaseMessages.getString( PKG, "MappingDialog.button.EnterMapping" ) );
    FormData fdbEnterMapping = new FormData();
    fdbEnterMapping.bottom = new FormAttachment( 100 );
    fdbEnterMapping.right = new FormAttachment( 100 );
    wbEnterMapping.setLayoutData( fdbEnterMapping );
    wbEnterMapping.setEnabled( input );

    ColumnInfo[] colinfo =
      new ColumnInfo[] {
        new ColumnInfo( sourceColumnLabel, ColumnInfo.COLUMN_TYPE_TEXT, false, false ),
        new ColumnInfo( targetColumnLabel, ColumnInfo.COLUMN_TYPE_TEXT, false, false ), };
    final TableView wFieldMappings =
      new TableView( transMeta, wFieldsComposite, SWT.FULL_SELECTION | SWT.SINGLE | SWT.BORDER, colinfo, 1, false, lsMod,
        props, false );
    props.setLook( wFieldMappings );
    FormData fdMappings = new FormData();
    fdMappings.top = new FormAttachment( wDescription, 20 );
    fdMappings.bottom = new FormAttachment( wbEnterMapping, -5 );
    fdMappings.left = new FormAttachment( 0 );
    fdMappings.right = new FormAttachment( 100 );
    wFieldMappings.setLayoutData( fdMappings );
    wFieldMappings.getTable().addListener( SWT.Resize, new ColumnsResizer( 0, 50, 50 ) );

    wbEnterMapping.addSelectionListener( new SelectionAdapter() {

      @Override
      public void widgetSelected( SelectionEvent arg0 ) {
        try {
          RowMetaInterface sourceRowMeta = getFieldsFromStep( wInputStep.getText(), true, input );
          RowMetaInterface targetRowMeta = getFieldsFromStep( wOutputStep.getText(), false, input );
          String[] sourceFields = sourceRowMeta.getFieldNames();
          String[] targetFields = targetRowMeta.getFieldNames();

          //Refresh mappings
          int nrLines = wFieldMappings.nrNonEmpty();
          definitions.get( wInputList.getSelectionIndex() ).getValueRenames().clear();
          for ( int i = 0; i < nrLines; i++ ) {
            TableItem item = wFieldMappings.getNonEmpty( i );
            definitions.get( wInputList.getSelectionIndex() ).getValueRenames().add( new MappingValueRename( item.getText( 1 ), item.getText( 2 ) ) );
          }

          List<MappingValueRename> mappingValue = definitions.get( wInputList.getSelectionIndex() ).getValueRenames();
          List<SourceToTargetMapping> currentMappings = MappingUtil.getCurrentMappings( Arrays.asList( sourceFields ), Arrays.asList( targetFields ), mappingValue );
          EnterMappingDialog dialog = new EnterMappingDialog( shell, sourceFields, targetFields, currentMappings );
          List<SourceToTargetMapping> mappings = dialog.open();
          if ( mappings != null ) {
            // first clear the dialog...
            wFieldMappings.clearAll( false );

            //
            mappingValue.clear();

            // Now add the new values...
            for ( SourceToTargetMapping mapping : mappings ) {
              TableItem item = new TableItem( wFieldMappings.table, SWT.NONE );
              item.setText( 1, mapping.getSourceString( sourceFields ) );
              item.setText( 2, mapping.getTargetString( targetFields ) );

              String source = input ? item.getText( 1 ) : item.getText( 2 );
              String target = input ? item.getText( 2 ) : item.getText( 1 );
              mappingValue.add( new MappingValueRename( source, target ) );
            }
            wFieldMappings.removeEmptyRows();
            wFieldMappings.setRowNums();
            wFieldMappings.optWidth( true );
          }
        } catch ( KettleException e ) {
          Listener ok = new Listener() {
            @Override
            public void handleEvent( final Event event ) { /* do nothing for now */ }
          };
          Map<String, Listener> listenerMap = new LinkedHashMap<>();
          listenerMap.put( BaseMessages.getString( "System.Button.OK" ), ok );
          new WarningDialog( shell, BaseMessages.getString( PKG, "System.Dialog.Error.Title" ), e.getMessage(), listenerMap );
          //new ErrorDialog( shell, BaseMessages.getString( PKG, "System.Dialog.Error.Title" ), BaseMessages.getString(
           // PKG, "MappingDialog.Exception.ErrorGettingMappingSourceAndTargetFields", e.toString() ), e );
        }
      }

    } );

    wOutputStep.addFocusListener( new FocusAdapter() {
      @Override
      public void focusLost( FocusEvent event ) {
        definitions.get( wInputList.getSelectionIndex() ).setOutputStepname( wOutputStep.getText() );
        try {
          enableMappingButton( wbEnterMapping, input, wInputStep.getText(), wOutputStep.getText() );
        } catch ( KettleException e ) {
          // Show the missing/wrong step name error
          //
          Listener ok = new Listener() {
            @Override
            public void handleEvent( final Event event ) { /* do nothing for now */ }
          };
          Map<String, Listener> listenerMap = new LinkedHashMap<>();
          listenerMap.put( BaseMessages.getString( "System.Button.OK" ), ok );
          new WarningDialog( shell, "Error", e.getMessage(), listenerMap ).open();
          //
          //new ErrorDialog( shell, "Error", "Unexpected error", e );
        }
      }
    } );
    wbOutputStep.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent event ) {
        String stepName = selectTransformationStepname( !input, input );
        if ( stepName != null ) {
          wOutputStep.setText( stepName );
          definitions.get( wInputList.getSelectionIndex() ).setOutputStepname( stepName );
          try {
            enableMappingButton( wbEnterMapping, input, wInputStep.getText(), wOutputStep.getText() );
          } catch ( KettleException e ) {
            // Show the missing/wrong stepname error
            new ErrorDialog( shell, "Error", "Unexpected error", e );
          }
        }
      }
    } );

    final Button wRenameOutput;
    if ( input ) {
      // Add a checkbox to indicate that all output mappings need to rename
      // the values back...
      //
      wRenameOutput = new Button( wFieldsComposite, SWT.CHECK );
      wRenameOutput.setText( BaseMessages.getString( PKG, "MappingDialog.input.RenamingOnOutput" ) );
      props.setLook( wRenameOutput );
      FormData fdRenameOutput = new FormData();
      fdRenameOutput.top = new FormAttachment( wFieldMappings, 5 );
      fdRenameOutput.left = new FormAttachment( 0 );
      wRenameOutput.setLayoutData( fdRenameOutput );
      wRenameOutput.addSelectionListener( new SelectionAdapter() {
        @Override
        public void widgetSelected( SelectionEvent event ) {
          definitions.get( wInputList.getSelectionIndex() ).setRenamingOnOutput( !definitions.get( wInputList.getSelectionIndex() ).isRenamingOnOutput() );
        }
      } );
    } else {
      wRenameOutput = null;
    }

    FormData fdInputComposite = new FormData();
    fdInputComposite.left = new FormAttachment( 0 );
    fdInputComposite.top = new FormAttachment( 0 );
    fdInputComposite.right = new FormAttachment( 100 );
    fdInputComposite.bottom = new FormAttachment( 100 );
    wInputComposite.setLayoutData( fdInputComposite );

    FormData fdFieldsComposite = new FormData();
    fdFieldsComposite.left = new FormAttachment( wInputList, 30 );
    fdFieldsComposite.right = new FormAttachment( 100 );
    fdFieldsComposite.bottom = new FormAttachment( 100 );
    fdFieldsComposite.top = new FormAttachment( 0 );
    wFieldsComposite.setLayoutData( fdFieldsComposite );

    wInputComposite.layout();
    wTab.setControl( wInputComposite );

    wMainPath.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent arg0 ) {
        setTabFlags( wMainPath, wlInputStep, wInputStep, wbInputStep, wlOutputStep, wOutputStep,
          wbOutputStep, wlDescription, wDescription );
      }
    } );

    wInputList.addSelectionListener( new SelectionAdapter() {
      @Override public void widgetSelected( SelectionEvent selectionEvent ) {
        updateFields( definitions.get( wInputList.getSelectionIndex() ), input, wMainPath, wlInputStep, wInputStep,
          wbInputStep, wlOutputStep, wOutputStep, wbOutputStep, wlDescription, wDescription, wFieldMappings,
          wRenameOutput );
      }
    } );

    wAddButton.addMouseListener( new MouseAdapter() {
      @Override public void mouseUp( MouseEvent mouseEvent ) {
        MappingIODefinition definition = new MappingIODefinition();
        definition.setMainDataPath( true );
        definitions.add( definition );
        wInputList.add( tabTitle + ( definitions.size() > 1 ? String.valueOf( definitions.size() ) : "" ) );
        wInputList.select( definitions.size() - 1 );
        updateFields( definitions.get( wInputList.getSelectionIndex() ), input, wMainPath, wlInputStep, wInputStep,
          wbInputStep, wlOutputStep, wOutputStep,
          wbOutputStep, wlDescription, wDescription, wFieldMappings, wRenameOutput );
        wlNoItems.setVisible( false );
        wFieldsComposite.setVisible( true );
        wRemoveButton.setEnabled( true );
      }
    } );

    wRemoveButton.addMouseListener( new MouseAdapter() {
      @Override public void mouseUp( MouseEvent mouseEvent ) {
        MessageBox box = new MessageBox( shell, SWT.YES | SWT.NO );
        box.setText( BaseMessages.getString( PKG, "MappingDialog.CloseDefinitionTabAreYouSure.Title" ) );
        box.setMessage( BaseMessages.getString( PKG, "MappingDialog.CloseDefinitionTabAreYouSure.Message" ) );
        int answer = box.open();
        if ( answer != SWT.YES ) {
          return;
        }

        int index = wInputList.getSelectionIndex();
        definitions.remove( index );
        wInputList.removeAll();
        for ( int i = 0; i < definitions.size(); i++ ) {
          String label =
            !Utils.isEmpty( definitions.get( i ).getInputStepname() ) ? definitions.get( i ).getInputStepname()
              : tabTitle + ( i > 0 ? String.valueOf( i + 1 ) : "" );
          wInputList.add( label );
        }
        if ( index > 0 ) {
          wInputList.select( index - 1 );
        } else if ( definitions.size() > 0 ) {
          wInputList.select( index );
        } else {
          index = -1;
        }
        if ( index != -1 ) {
          updateFields( definitions.get( wInputList.getSelectionIndex() ), input, wMainPath, wlInputStep, wInputStep,
            wbInputStep, wlOutputStep, wOutputStep,
            wbOutputStep, wlDescription, wDescription, wFieldMappings, wRenameOutput );
        }
        if ( definitions.size() == 0 ) {
          wlNoItems.setVisible( true );
          wFieldsComposite.setVisible( false );
          wRemoveButton.setEnabled( false );
        }
      }
    } );

    if ( definitions.size() > 0 ) {
      wInputList.select( 0 );
      updateFields( definitions.get( 0 ), input, wMainPath, wlInputStep, wInputStep,
        wbInputStep, wlOutputStep, wOutputStep, wbOutputStep, wlDescription, wDescription, wFieldMappings,
        wRenameOutput );
    } else {
      wlNoItems.setVisible( true );
      wFieldsComposite.setVisible( false );
      wRemoveButton.setEnabled( false );
    }

    setTabFlags( wMainPath, wlInputStep, wInputStep, wbInputStep, wlOutputStep, wOutputStep,
      wbOutputStep, wlDescription, wDescription );

    wTabFolder.setSelection( wTab );

  }

  private void updateFields( MappingIODefinition definition, boolean input, Button wMainPath, Label wlInputStep,
                             Text wInputStep, Button wbInputStep, Label wlOutputStep, Text wOutputStep,
                             Button wbOutputStep, Label wlDescription, Text wDescription, TableView wFieldMappings,
                             Button wRenameOutput ) {
    if ( tabChanges != null ) {
      tabChanges.applyChanges();
    }
    wMainPath.setSelection( definition.isMainDataPath() );
    wInputStep.setText( Const.NVL( definition.getInputStepname(), "" ) );
    wOutputStep.setText( Const.NVL( definition.getOutputStepname(), "" ) );
    wDescription.setText( Const.NVL( definition.getDescription(), "" ) );
    setTabFlags( wMainPath, wlInputStep, wInputStep, wbInputStep, wlOutputStep, wOutputStep, wbOutputStep,
      wlDescription, wDescription );
    wFieldMappings.removeAll();
    for ( MappingValueRename valueRename : definition.getValueRenames() ) {
      TableItem tableItem = new TableItem( wFieldMappings.table, SWT.NONE );
      tableItem.setText( 1, Const.NVL( valueRename.getSourceValueName(), "" ) );
      tableItem.setText( 2, Const.NVL( valueRename.getTargetValueName(), "" ) );
    }
    wFieldMappings.removeEmptyRows();
    wFieldMappings.setRowNums();
    wFieldMappings.optWidth( true );
    if ( input ) {
      wRenameOutput.setSelection( definition.isRenamingOnOutput() );
    }
    tabChanges =
      new MappingDefinitionTab( definition, wInputStep, wOutputStep, wMainPath, wDescription, wFieldMappings );
  }

  private void setTabFlags( Button wMainPath, Label wlInputStep, Text wInputStep, Button wbInputStep,
                            Label wlOutputStep, Text wOutputStep, Button wbOutputStep, Label wlDescription,
                            Text wDescription ) {
    boolean mainPath = wMainPath.getSelection();
    wlInputStep.setEnabled( !mainPath );
    wInputStep.setEnabled( !mainPath );
    wbInputStep.setEnabled( !mainPath );
    wlOutputStep.setEnabled( !mainPath );
    wOutputStep.setEnabled( !mainPath );
    wbOutputStep.setEnabled( !mainPath );
    wlDescription.setEnabled( !mainPath );
    wDescription.setEnabled( !mainPath );
  }

  /**
   * Enables or disables the mapping button. We can only enable it if the target steps allows a mapping to be made
   * against it.
   *
   * @param button         The button to disable or enable
   * @param input          input or output. If it's true, we keep the button enabled all the time.
   * @param sourceStepname The mapping output step
   * @param targetStepname The target step to verify
   * @throws KettleException
   */
  private void enableMappingButton( final Button button, boolean input, String sourceStepname, String targetStepname ) throws KettleException {
    if ( input ) {
      return; // nothing to do
    }

    boolean enabled = false;

    if ( mappingTransMeta != null ) {
      StepMeta mappingInputStep = mappingTransMeta.findMappingInputStep( sourceStepname );
      if ( mappingInputStep != null ) {
        StepMeta mappingOutputStep = transMeta.findMappingOutputStep( targetStepname );
        RowMetaInterface requiredFields = mappingOutputStep.getStepMetaInterface().getRequiredFields( transMeta );
        if ( requiredFields != null && requiredFields.size() > 0 ) {
          enabled = true;
        }
      }
    }

    button.setEnabled( enabled );
  }

  private void cancel() {
    stepname = null;
    mappingMeta.setChanged( changed );
    dispose();
  }

  private void ok() {
    if ( Utils.isEmpty( wStepname.getText() ) ) {
      return;
    }

    stepname = wStepname.getText(); // return value

    try {
      loadTransformation();
    } catch ( KettleException e ) {
      new ErrorDialog( shell, BaseMessages.getString(
        PKG, "MappingDialog.ErrorLoadingSpecifiedTransformation.Title" ), BaseMessages.getString(
        PKG, "MappingDialog.ErrorLoadingSpecifiedTransformation.Message" ), e );
      return;
    }

    mappingMeta.setSpecificationMethod( getSpecificationMethod() );
    switch ( getSpecificationMethod() ) {
      case FILENAME:
        mappingMeta.setFileName( wPath.getText() );
        mappingMeta.setDirectoryPath( null );
        mappingMeta.setTransName( null );
        mappingMeta.setTransObjectId( null );
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
        mappingMeta.setDirectoryPath( directory );
        mappingMeta.setTransName( transName );
        mappingMeta.setFileName( null );
        mappingMeta.setTransObjectId( null );
      default:
        break;
    }

    // Load the information on the tabs, optionally do some
    // verifications...
    //
    collectInformation();

    mappingMeta.setMappingParameters( mappingParameters );
    mappingMeta.setInputMappings( inputMappings );
    // Set the input steps for input mappings
    mappingMeta.searchInfoAndTargetSteps( transMeta.getSteps() );
    mappingMeta.setOutputMappings( outputMappings );

    mappingMeta.setAllowingMultipleInputs( true );
    mappingMeta.setAllowingMultipleOutputs( true );

    mappingMeta.setChanged( true );

    dispose();
  }

  private void collectInformation() {
    parameterChanges.applyChanges();
    tabChanges.applyChanges();
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
