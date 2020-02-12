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
import org.pentaho.di.trans.steps.mapping.MappingParameters;
import org.pentaho.di.trans.steps.mapping.MappingValueRename;
import org.pentaho.di.trans.steps.simplemapping.SimpleMappingMeta;
import org.pentaho.di.ui.core.ConstUI;
import org.pentaho.di.ui.core.dialog.EnterMappingDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
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
import java.util.List;

public class SimpleMappingDialog extends BaseStepDialog implements StepDialogInterface {
  private static Class<?> PKG = SimpleMappingMeta.class; // for i18n purposes, needed by Translator2!!

  private SimpleMappingMeta mappingMeta;

  private Label wlPath;
  private TextVar wPath;

  private Button wbBrowse;

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
    formLayout.marginWidth = 15;
    formLayout.marginHeight = 15;

    shell.setLayout( formLayout );
    shell.setText( BaseMessages.getString( PKG, "SimpleMappingDialog.Shell.Title" ) );

    Label wicon = new Label( shell, SWT.RIGHT );
    wicon.setImage( getImage() );
    FormData fdlicon = new FormData();
    fdlicon.top = new FormAttachment( 0, 0 );
    fdlicon.right = new FormAttachment( 100, 0 );
    wicon.setLayoutData( fdlicon );
    props.setLook( wicon );

    // Stepname line
    wlStepname = new Label( shell, SWT.RIGHT );
    wlStepname.setText( BaseMessages.getString( PKG, "SimpleMappingDialog.Stepname.Label" ) );
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
    wlPath.setText( BaseMessages.getString( PKG, "SimpleMappingDialog.Transformation.Label" ) );
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
    wbBrowse.setText( BaseMessages.getString( PKG, "SimpleMappingDialog.Browse.Label" ) );
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
    wPath.addSelectionListener( lsDef );

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
      .getImage( shell.getDisplay(), getClass().getClassLoader(), "MAP.svg", ConstUI.ICON_SIZE,
        ConstUI.ICON_SIZE );
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
        BaseMessages.getString( PKG, "SimpleMappingDialog.ErrorSelectingObject.DialogTitle" ),
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
        BaseMessages.getString( PKG, "SimpleMappingDialog.ErrorLoadingTransformation.DialogTitle" ),
        BaseMessages.getString( PKG, "SimpleMappingDialog.ErrorLoadingTransformation.DialogMessage" ), e );
    }
  }

  private void loadFileTrans( String fname ) throws KettleException {
    mappingTransMeta = new TransMeta( transMeta.environmentSubstitute( fname ) );
    mappingTransMeta.clearChanged();
  }

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
            BaseMessages.getString( PKG, "SimpleMappingDialog.Exception.NoValidMappingDetailsFound" ) );
        }
        RepositoryDirectoryInterface repdir = repository.findDirectory( realDirectory );
        if ( repdir == null ) {
          throw new KettleException( BaseMessages.getString(
            PKG, "SimpleMappingDialog.Exception.UnableToFindRepositoryDirectory" ) );
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
          PKG, "SimpleMappingDialog.Exception.NotConnectedToRepository.Message" ) );
      }
      RepositoryObject transInf = repository.getObjectInformation( transObjectId, RepositoryObjectType.TRANSFORMATION );
      String path = DialogUtils
        .getPath( transMeta.getRepositoryDirectory().getPath(), transInf.getRepositoryDirectory().getPath() );
      String fullPath =
        Const.NVL( path, "" ) + "/" + Const.NVL( transInf.getName(), "" );
      wPath.setText( fullPath );
    } catch ( KettleException e ) {
      new ErrorDialog( shell, BaseMessages.getString(
        PKG, "SimpleMappingDialog.Exception.UnableToReferenceObjectId.Title" ), BaseMessages.getString(
        PKG, "SimpleMappingDialog.Exception.UnableToReferenceObjectId.Message" ), e );
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

    addParametersTab( mappingParameters );
    wTabFolder.setSelection( 0 );

    addInputMappingDefinitionTab( inputMapping, 0 );
    addOutputMappingDefinitionTab( outputMapping, 1 );

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
    parameterTabLayout.marginWidth = 15;
    parameterTabLayout.marginHeight = 15;
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
        .getVariable().length, false, lsMod, props, false
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
    tabLayout.marginWidth = 15;
    tabLayout.marginHeight = 15;
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
    fdbEnterMapping.bottom = new FormAttachment( 100 );
    fdbEnterMapping.right = new FormAttachment( 100 );
    wbEnterMapping.setLayoutData( fdbEnterMapping );

    ColumnInfo[] colinfo =
      new ColumnInfo[] {
        new ColumnInfo( sourceColumnLabel, ColumnInfo.COLUMN_TYPE_TEXT, false, false ),
        new ColumnInfo( targetColumnLabel, ColumnInfo.COLUMN_TYPE_TEXT, false, false ), };
    final TableView wFieldMappings =
      new TableView(
        transMeta, wInputComposite, SWT.FULL_SELECTION | SWT.SINGLE | SWT.BORDER, colinfo, 1, false, lsMod, props,
        false );
    props.setLook( wFieldMappings );
    FormData fdMappings = new FormData();
    fdMappings.left = new FormAttachment( 0 );
    fdMappings.right = new FormAttachment( 100 );
    fdMappings.top = new FormAttachment( 0 );
    fdMappings.bottom = new FormAttachment( wbEnterMapping, -10 );
    wFieldMappings.setLayoutData( fdMappings );
    wFieldMappings.getTable().addListener( SWT.Resize, new ColumnsResizer( 0, 50, 50 ) );

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

            //refresh mappings
            int nrLines = wFieldMappings.nrNonEmpty();
            definition.getValueRenames().clear();
            for ( int i = 0; i < nrLines; i++ ) {
              TableItem item = wFieldMappings.getNonEmpty( i );
              definition.getValueRenames().add( new MappingValueRename( item.getText( 1 ), item.getText( 2 ) ) );
            }

            List<MappingValueRename> mappingValue = definition.getValueRenames();
            List<SourceToTargetMapping> currentMappings = MappingUtil.getCurrentMappings( Arrays.asList( sourceFields ), Arrays.asList( targetFields ), mappingValue );
            EnterMappingDialog dialog = new EnterMappingDialog( shell, sourceFields, targetFields, currentMappings );
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
      fdRenameOutput.top = new FormAttachment( wFieldMappings, 10 );
      fdRenameOutput.left = new FormAttachment( 0, 0 );
      wRenameOutput.setLayoutData( fdRenameOutput );

      wRenameOutput.setSelection( definition.isRenamingOnOutput() );
      wRenameOutput.addSelectionListener( new SelectionAdapter() {
        @Override
        public void widgetSelected( SelectionEvent event ) {
          // flip the switch
          definition.setRenamingOnOutput( !definition.isRenamingOnOutput() );
        }
      } );
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

    if ( Utils.isEmpty( stepname ) ) {
      wTab.setText( tabTitle );
    } else {
      wTab.setText( tabTitle + " : " + stepname );
    }
    String tooltip = tabTooltip;
    if ( !Utils.isEmpty( stepname ) ) {
      tooltip += Const.CR + Const.CR + stepname;
    }
    if ( !Utils.isEmpty( description ) ) {
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
    if ( Utils.isEmpty( wStepname.getText() ) ) {
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
