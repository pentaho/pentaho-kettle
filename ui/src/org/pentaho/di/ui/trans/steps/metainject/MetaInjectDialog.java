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

package org.pentaho.di.ui.trans.steps.metainject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
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
import org.eclipse.swt.graphics.Point;
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
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.core.gui.SpoonInterface;
import org.pentaho.di.core.injection.bean.BeanInjectionInfo;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryElementMetaInterface;
import org.pentaho.di.repository.RepositoryObject;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepInjectionMetaEntry;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInjectionInterface;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.metainject.MetaInject;
import org.pentaho.di.trans.steps.metainject.MetaInjectMeta;
import org.pentaho.di.trans.steps.metainject.MetaInjectOutputField;
import org.pentaho.di.trans.steps.metainject.SourceStepField;
import org.pentaho.di.trans.steps.metainject.TargetStepAttribute;
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

public class MetaInjectDialog extends BaseStepDialog implements StepDialogInterface {
  // private static final String STRING_TREE_NAME = "META_INJECT_TREE";

  private static Class<?> PKG = MetaInjectMeta.class; // for i18n purposes, needed by Translator2!!

  private MetaInjectMeta metaInjectMeta;

  private CTabFolder wTabFolder;
  private FormData fdTabFolder;

  private CTabItem wFileTab;
  private ScrolledComposite wFileSComp;
  private Composite wFileComp;

  private CTabItem wOptionsTab;
  private ScrolledComposite wOptionsSComp;
  private Composite wOptionsComp;

  private CTabItem wInjectTab;
  private ScrolledComposite wInjectSComp;
  private Composite wInjectComp;

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
  private Button wValidateTrans;
  private Button wEditTrans;
  private Button wNewTrans;

  private TransMeta injectTransMeta = null;

  protected boolean transModified;

  private ModifyListener lsMod;

  private int middle;

  private int margin;

  private ObjectId referenceObjectId;
  private ObjectLocationSpecificationMethod specificationMethod;

  // the source step
  //
  private CCombo wSourceStep;

  // The source step output fields...
  //
  private Label wlSourceFields;
  private TableView wSourceFields;

  // the target file
  //
  private TextVar wTargetFile;

  // don't execute the transformation
  //
  private Button wNoExecution;

  // the streaming source step
  //
  private Label wlStreamingSourceStep;
  private CCombo wStreamingSourceStep;

  // the streaming target step
  //
  private Label wlStreamingTargetStep;
  private CCombo wStreamingTargetStep;

  // The tree object to show the options...
  //
  private Tree wTree;

  private Map<TreeItem, TargetStepAttribute> treeItemTargetMap;

  private Map<TargetStepAttribute, SourceStepField> targetSourceMapping;

  public MetaInjectDialog( Shell parent, Object in, TransMeta tr, String sname ) {
    super( parent, (BaseStepMeta) in, tr, sname );
    metaInjectMeta = (MetaInjectMeta) in;
    transModified = false;

    targetSourceMapping = new HashMap<TargetStepAttribute, SourceStepField>();
    targetSourceMapping.putAll( metaInjectMeta.getTargetSourceMapping() );
  }

  public String open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX );
    props.setLook( shell );
    setShellImage( shell, metaInjectMeta );

    lsMod = new ModifyListener() {
      public void modifyText( ModifyEvent e ) {
        metaInjectMeta.setChanged();
      }
    };
    changed = metaInjectMeta.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout( formLayout );
    shell.setText( BaseMessages.getString( PKG, "MetaInjectDialog.Shell.Title" ) );

    middle = props.getMiddlePct();
    margin = Const.MARGIN;

    // Stepname line
    wlStepname = new Label( shell, SWT.RIGHT );
    wlStepname.setText( BaseMessages.getString( PKG, "MetaInjectDialog.Stepname.Label" ) );
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

    wTabFolder = new CTabFolder( shell, SWT.BORDER );
    props.setLook( wTabFolder, Props.WIDGET_STYLE_TAB );
    wTabFolder.setSimple( false );

    // Some buttons
    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );
    setButtonPositions( new Button[] { wOK, wCancel }, margin, null );

    fdTabFolder = new FormData();
    fdTabFolder.left = new FormAttachment( 0, 0 );
    fdTabFolder.top = new FormAttachment( wStepname, margin );
    fdTabFolder.right = new FormAttachment( 100, 0 );
    fdTabFolder.bottom = new FormAttachment( wOK, -margin * 2 );
    wTabFolder.setLayoutData( fdTabFolder );

    addFileTab();
    addOptionsTab();
    addInjectTab();

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
    metaInjectMeta.setChanged( changed );

    shell.open();

    checkInvalidMapping();

    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return stepname;
  }

  private void checkInvalidMapping() {
    if ( injectTransMeta == null ) {
      try {
        loadTransformation();
      } catch ( KettleException e ) {
        showErrorOnLoadTransformationDialog( e );
        return;
      }
    }
    Set<SourceStepField> unavailableSourceSteps =
        MetaInject.getUnavailableSourceSteps( targetSourceMapping, transMeta, stepMeta );
    Set<TargetStepAttribute> unavailableTargetSteps =
        MetaInject.getUnavailableTargetSteps( targetSourceMapping, injectTransMeta );
    if ( unavailableSourceSteps.isEmpty() && unavailableTargetSteps.isEmpty() ) {
      return;
    }
    showInvalidMappingDialog( unavailableSourceSteps, unavailableTargetSteps );
  }

  private void showInvalidMappingDialog( Set<SourceStepField> unavailableSourceSteps,
      Set<TargetStepAttribute> unavailableTargetSteps ) {
    MessageBox mb = new MessageBox( shell, SWT.YES | SWT.NO | SWT.ICON_QUESTION );
    mb.setMessage( BaseMessages.getString( PKG, "MetaInjectDialog.InvalidMapping.Question" ) );
    mb.setText( BaseMessages.getString( PKG, "MetaInjectDialog.InvalidMapping.Title" ) );
    int id = mb.open();
    if ( id == SWT.YES ) {
      MetaInject.removeUnavailableStepsFromMapping( targetSourceMapping, unavailableSourceSteps,
          unavailableTargetSteps );
    }
  }

  private void showErrorOnLoadTransformationDialog( KettleException e ) {
    new ErrorDialog( shell, BaseMessages.getString( PKG, "MetaInjectDialog.ErrorLoadingSpecifiedTransformation.Title" ),
        BaseMessages.getString( PKG, "MetaInjectDialog.ErrorLoadingSpecifiedTransformation.Message" ), e );
  }

  private void addFileTab() {
    // ////////////////////////
    // START OF FILE TAB ///
    // ////////////////////////

    wFileTab = new CTabItem( wTabFolder, SWT.NONE );
    wFileTab.setText( BaseMessages.getString( PKG, "MetaInjectDialog.FileTab.TabTitle" ) );

    wFileSComp = new ScrolledComposite( wTabFolder, SWT.V_SCROLL | SWT.H_SCROLL );
    wFileSComp.setLayout( new FillLayout() );

    wFileComp = new Composite( wFileSComp, SWT.NONE );
    props.setLook( wFileComp );

    FormLayout fileLayout = new FormLayout();
    fileLayout.marginWidth = 3;
    fileLayout.marginHeight = 3;
    wFileComp.setLayout( fileLayout );

    // //////////////////////////////////////////////////
    // The transformation template box
    // //////////////////////////////////////////////////
    //
    gTransGroup = new Group( wFileComp, SWT.SHADOW_ETCHED_IN );
    gTransGroup.setText( BaseMessages.getString( PKG, "MetaInjectDialog.TransGroup.Label" ) );
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
    radioFilename.setText( BaseMessages.getString( PKG, "MetaInjectDialog.RadioFile.Label" ) );
    radioFilename.setToolTipText( BaseMessages.getString( PKG, "MetaInjectDialog.RadioFile.Tooltip", Const.CR ) );
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
    wbbFilename.setToolTipText( BaseMessages.getString( PKG, "System.Tooltip.Browse" ) );
    FormData fdbFilename = new FormData();
    fdbFilename.right = new FormAttachment( 100, 0 );
    fdbFilename.top = new FormAttachment( radioFilename, margin );
    wbbFilename.setLayoutData( fdbFilename );
    wbbFilename.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        selectFileTrans( true );
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

    // Radio button: The mapping is in the repository
    //
    radioByName = new Button( gTransGroup, SWT.RADIO );
    props.setLook( radioByName );
    radioByName.setSelection( false );
    radioByName.setText( BaseMessages.getString( PKG, "MetaInjectDialog.RadioRep.Label" ) );
    radioByName.setToolTipText( BaseMessages.getString( PKG, "MetaInjectDialog.RadioRep.Tooltip", Const.CR ) );
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
    wbTrans.setText( BaseMessages.getString( PKG, "MetaInjectDialog.Select.Button" ) );
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

    // Radio button: The mapping is in the repository
    //
    radioByReference = new Button( gTransGroup, SWT.RADIO );
    props.setLook( radioByReference );
    radioByReference.setSelection( false );
    radioByReference.setText( BaseMessages.getString( PKG, "MetaInjectDialog.RadioRepByReference.Label" ) );
    radioByReference.setToolTipText( BaseMessages.getString(
      PKG, "MetaInjectDialog.RadioRepByReference.Tooltip", Const.CR ) );
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
    wbByReference.setToolTipText( BaseMessages.getString( PKG, "MetaInjectDialog.SelectTrans.Tooltip" ) );
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
        specificationMethod = ObjectLocationSpecificationMethod.REPOSITORY_BY_REFERENCE;
        setRadioButtons();
      }
    } );

    wNewTrans = new Button( gTransGroup, SWT.PUSH | SWT.CENTER ); // Browse
    props.setLook( wNewTrans );
    wNewTrans.setText( BaseMessages.getString( PKG, "MetaInjectDialog.New.Button" ) );
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
    wEditTrans.setText( BaseMessages.getString( PKG, "MetaInjectDialog.Edit.Button" ) );
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

    wValidateTrans = new Button( gTransGroup, SWT.PUSH | SWT.CENTER ); // Browse
    props.setLook( wValidateTrans );
    wValidateTrans.setText( BaseMessages.getString( PKG, "MetaInjectDialog.Validate.Button" ) );
    wValidateTrans.setToolTipText( BaseMessages.getString( PKG, "MetaInjectDialog.Validate.Tooltip" ) );
    FormData fdValidateTrans = new FormData();
    fdValidateTrans.left = new FormAttachment( wEditTrans, 2 * margin );
    fdValidateTrans.top = new FormAttachment( wByReference, 3 * margin );
    wValidateTrans.setLayoutData( fdValidateTrans );
    wValidateTrans.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        validateTrans();
      }
    } );

    FormData fdTransGroup = new FormData();
    fdTransGroup.left = new FormAttachment( 0, 0 );
    fdTransGroup.top = new FormAttachment( wStepname, 2 * margin );
    fdTransGroup.right = new FormAttachment( 100, 0 );
    // fdTransGroup.bottom = new FormAttachment(wStepname, 350);
    gTransGroup.setLayoutData( fdTransGroup );

    FormData fdFileComp = new FormData();
    fdFileComp.left = new FormAttachment( 0, 0 );
    fdFileComp.top = new FormAttachment( 0, 0 );
    fdFileComp.right = new FormAttachment( 100, 0 );
    fdFileComp.bottom = new FormAttachment( 100, 0 );
    wFileComp.setLayoutData( fdFileComp );

    wFileComp.pack();
    Rectangle bounds = wFileComp.getBounds();

    wFileSComp.setContent( wFileComp );
    wFileSComp.setExpandHorizontal( true );
    wFileSComp.setExpandVertical( true );
    wFileSComp.setMinWidth( bounds.width );
    wFileSComp.setMinHeight( bounds.height );

    wFileTab.setControl( wFileSComp );

    // ///////////////////////////////////////////////////////////
    // / END OF FILE TAB
    // ///////////////////////////////////////////////////////////
  }

  private void addOptionsTab() {
    // ////////////////////////
    // START OF OPTIONS TAB ///
    // ////////////////////////

    wOptionsTab = new CTabItem( wTabFolder, SWT.NONE );
    wOptionsTab.setText( BaseMessages.getString( PKG, "MetaInjectDialog.OptionsTab.TabTitle" ) );

    wOptionsSComp = new ScrolledComposite( wTabFolder, SWT.V_SCROLL | SWT.H_SCROLL );
    wOptionsSComp.setLayout( new FillLayout() );

    wOptionsComp = new Composite( wOptionsSComp, SWT.NONE );
    props.setLook( wOptionsComp );

    FormLayout fileLayout = new FormLayout();
    fileLayout.marginWidth = 3;
    fileLayout.marginHeight = 3;
    wOptionsComp.setLayout( fileLayout );

    Label wlSourceStep = new Label( wOptionsComp, SWT.RIGHT );
    wlSourceStep.setText( BaseMessages.getString( PKG, "MetaInjectDialog.SourceStep.Label" ) );
    props.setLook( wlSourceStep );
    FormData fdlSourceStep = new FormData();
    fdlSourceStep.left = new FormAttachment( 0, 0 );
    fdlSourceStep.right = new FormAttachment( middle, 0 );
    fdlSourceStep.top = new FormAttachment( 0, 0 );
    wlSourceStep.setLayoutData( fdlSourceStep );
    wSourceStep = new CCombo( wOptionsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wSourceStep );
    wSourceStep.addModifyListener( lsMod );
    FormData fdSourceStep = new FormData();
    fdSourceStep.left = new FormAttachment( middle, margin );
    fdSourceStep.top = new FormAttachment( 0, 0 );
    fdSourceStep.right = new FormAttachment( 100, 0 );
    wSourceStep.setLayoutData( fdSourceStep );
    wSourceStep.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent arg0 ) {
        setActive();
      }
    } );
    Control lastControl = wSourceStep;

    wlSourceFields = new Label( wOptionsComp, SWT.RIGHT );
    wlSourceFields.setText( BaseMessages.getString( PKG, "MetaInjectDialog.Fields.Label" ) );
    props.setLook( wlSourceFields );
    FormData fdlFields = new FormData();
    fdlFields.left = new FormAttachment( 0, 0 );
    fdlFields.right = new FormAttachment( middle, 0 );
    fdlFields.top = new FormAttachment( lastControl, margin );
    wlSourceFields.setLayoutData( fdlFields );

    final int FieldsRows = metaInjectMeta.getSourceOutputFields().size();

    ColumnInfo[] colinf =
      new ColumnInfo[] {
        new ColumnInfo(
          BaseMessages.getString( PKG, "MetaInjectDialog.ColumnInfo.Fieldname" ), ColumnInfo.COLUMN_TYPE_TEXT,
          false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "MetaInjectDialog.ColumnInfo.Type" ), ColumnInfo.COLUMN_TYPE_CCOMBO,
          ValueMeta.getAllTypes() ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "MetaInjectDialog.ColumnInfo.Length" ), ColumnInfo.COLUMN_TYPE_TEXT,
          false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "MetaInjectDialog.ColumnInfo.Precision" ), ColumnInfo.COLUMN_TYPE_TEXT,
          false ), };

    wSourceFields = new TableView( transMeta, wOptionsComp,
      SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf, FieldsRows, lsMod, props );

    FormData fdFields = new FormData();
    fdFields.left = new FormAttachment( middle, margin );
    fdFields.top = new FormAttachment( lastControl, margin );
    fdFields.right = new FormAttachment( 100, 0 );
    fdFields.bottom = new FormAttachment( lastControl, margin + 300 );
    wSourceFields.setLayoutData( fdFields );
    lastControl = wSourceFields;

    Label wlTargetFile = new Label( wOptionsComp, SWT.RIGHT );
    wlTargetFile.setText( BaseMessages.getString( PKG, "MetaInjectDialog.TargetFile.Label" ) );
    props.setLook( wlTargetFile );
    FormData fdlTargetFile = new FormData();
    fdlTargetFile.left = new FormAttachment( 0, 0 );
    fdlTargetFile.right = new FormAttachment( middle, 0 );
    fdlTargetFile.top = new FormAttachment( lastControl, margin );
    wlTargetFile.setLayoutData( fdlTargetFile );
    wTargetFile = new TextVar( transMeta, wOptionsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wTargetFile );
    wTargetFile.addModifyListener( lsMod );
    FormData fdTargetFile = new FormData();
    fdTargetFile.left = new FormAttachment( middle, margin );
    fdTargetFile.top = new FormAttachment( lastControl, margin );
    fdTargetFile.right = new FormAttachment( 100, 0 );
    wTargetFile.setLayoutData( fdTargetFile );
    lastControl = wTargetFile;

    Label wlNoExecution = new Label( wOptionsComp, SWT.RIGHT );
    wlNoExecution.setText( BaseMessages.getString( PKG, "MetaInjectDialog.NoExecution.Label" ) );
    props.setLook( wlNoExecution );
    FormData fdlNoExecution = new FormData();
    fdlNoExecution.left = new FormAttachment( 0, 0 );
    fdlNoExecution.right = new FormAttachment( middle, 0 );
    fdlNoExecution.top = new FormAttachment( lastControl, margin );
    wlNoExecution.setLayoutData( fdlNoExecution );
    wNoExecution = new Button( wOptionsComp, SWT.CHECK );
    props.setLook( wNoExecution );
    FormData fdNoExecution = new FormData();
    fdNoExecution.left = new FormAttachment( middle, margin );
    fdNoExecution.top = new FormAttachment( lastControl, margin );
    fdNoExecution.right = new FormAttachment( 100, 0 );
    wNoExecution.setLayoutData( fdNoExecution );
    lastControl = wNoExecution;

    wlStreamingSourceStep = new Label( wOptionsComp, SWT.RIGHT );
    wlStreamingSourceStep.setText( BaseMessages.getString( PKG, "MetaInjectDialog.StreamingSourceStep.Label" ) );
    props.setLook( wlStreamingSourceStep );
    FormData fdlStreamingSourceStep = new FormData();
    fdlStreamingSourceStep.left = new FormAttachment( 0, 0 );
    fdlStreamingSourceStep.right = new FormAttachment( middle, 0 );
    fdlStreamingSourceStep.top = new FormAttachment( lastControl, margin );
    wlStreamingSourceStep.setLayoutData( fdlStreamingSourceStep );
    wStreamingSourceStep = new CCombo( wOptionsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wStreamingSourceStep );
    FormData fdStreamingSourceStep = new FormData();
    fdStreamingSourceStep.left = new FormAttachment( middle, margin );
    fdStreamingSourceStep.top = new FormAttachment( lastControl, margin );
    fdStreamingSourceStep.right = new FormAttachment( 100, 0 );
    wStreamingSourceStep.setLayoutData( fdStreamingSourceStep );
    wStreamingSourceStep.setItems( transMeta.getStepNames() );
    wStreamingSourceStep.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent arg0 ) {
        setActive();
      }
    } );
    lastControl = wStreamingSourceStep;

    wlStreamingTargetStep = new Label( wOptionsComp, SWT.RIGHT );
    wlStreamingTargetStep.setText( BaseMessages.getString( PKG, "MetaInjectDialog.StreamingTargetStep.Label" ) );
    props.setLook( wlStreamingTargetStep );
    FormData fdlStreamingTargetStep = new FormData();
    fdlStreamingTargetStep.left = new FormAttachment( 0, 0 );
    fdlStreamingTargetStep.right = new FormAttachment( middle, 0 );
    fdlStreamingTargetStep.top = new FormAttachment( lastControl, margin );
    wlStreamingTargetStep.setLayoutData( fdlStreamingTargetStep );
    wStreamingTargetStep = new CCombo( wOptionsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wStreamingTargetStep );
    FormData fdStreamingTargetStep = new FormData();
    fdStreamingTargetStep.left = new FormAttachment( middle, margin );
    fdStreamingTargetStep.top = new FormAttachment( lastControl, margin );
    fdStreamingTargetStep.right = new FormAttachment( 100, 0 );
    wStreamingTargetStep.setLayoutData( fdStreamingTargetStep );
    lastControl = wStreamingTargetStep;

    FormData fdOptionsComp = new FormData();
    fdOptionsComp.left = new FormAttachment( 0, 0 );
    fdOptionsComp.top = new FormAttachment( 0, 0 );
    fdOptionsComp.right = new FormAttachment( 100, 0 );
    fdOptionsComp.bottom = new FormAttachment( 100, 0 );
    wOptionsComp.setLayoutData( fdOptionsComp );

    wOptionsComp.pack();
    Rectangle bounds = wOptionsComp.getBounds();

    wOptionsSComp.setContent( wOptionsComp );
    wOptionsSComp.setExpandHorizontal( true );
    wOptionsSComp.setExpandVertical( true );
    wOptionsSComp.setMinWidth( bounds.width );
    wOptionsSComp.setMinHeight( bounds.height );

    wOptionsTab.setControl( wOptionsSComp );

    // ///////////////////////////////////////////////////////////
    // / END OF OPTIONS TAB
    // ///////////////////////////////////////////////////////////
  }

  private void addInjectTab() {
    // ////////////////////////
    // START OF INJECT TAB ///
    // ////////////////////////

    wInjectTab = new CTabItem( wTabFolder, SWT.NONE );
    wInjectTab.setText( BaseMessages.getString( PKG, "MetaInjectDialog.InjectTab.TabTitle" ) );

    wInjectSComp = new ScrolledComposite( wTabFolder, SWT.V_SCROLL | SWT.H_SCROLL );
    wInjectSComp.setLayout( new FillLayout() );

    wInjectComp = new Composite( wInjectSComp, SWT.NONE );
    props.setLook( wInjectComp );

    FormLayout fileLayout = new FormLayout();
    fileLayout.marginWidth = 3;
    fileLayout.marginHeight = 3;
    wInjectComp.setLayout( fileLayout );

    wTree = new Tree( wInjectComp, SWT.SINGLE | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER );
    FormData fdTree = new FormData();
    fdTree.left = new FormAttachment( 0, 0 );
    fdTree.top = new FormAttachment( 0, 0 );
    fdTree.right = new FormAttachment( 100, 0 );
    fdTree.bottom = new FormAttachment( 100, 0 );
    wTree.setLayoutData( fdTree );

    ColumnInfo[] colinf =
      new ColumnInfo[] {
        new ColumnInfo(
          BaseMessages.getString( PKG, "MetaInjectDialog.Column.TargetStep" ), ColumnInfo.COLUMN_TYPE_TEXT,
          false, true ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "MetaInjectDialog.Column.TargetDescription" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false, true ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "MetaInjectDialog.Column.SourceStep" ),
          ColumnInfo.COLUMN_TYPE_CCOMBO, false, true ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "MetaInjectDialog.Column.SourceField" ),
          ColumnInfo.COLUMN_TYPE_CCOMBO, false, true ), };

    wTree.setHeaderVisible( true );
    for ( int i = 0; i < colinf.length; i++ ) {
      ColumnInfo columnInfo = colinf[i];
      TreeColumn treeColumn = new TreeColumn( wTree, columnInfo.getAllignement() );
      treeColumn.setText( columnInfo.getName() );
      treeColumn.setWidth( 200 );
    }

    wTree.addListener( SWT.MouseDown, new Listener() {
      public void handleEvent( Event event ) {
        try {
          Point point = new Point( event.x, event.y );
          TreeItem item = wTree.getItem( point );
          if ( item != null ) {
            TargetStepAttribute target = treeItemTargetMap.get( item );
            if ( target != null ) {
              SourceStepField source = targetSourceMapping.get( target );

              String[] prevStepNames = transMeta.getPrevStepNames( stepMeta );
              Arrays.sort( prevStepNames );

              Map<String, SourceStepField> fieldMap = new HashMap<String, SourceStepField>();
              for ( String prevStepName : prevStepNames ) {
                RowMetaInterface fields = transMeta.getStepFields( prevStepName );
                for ( ValueMetaInterface field : fields.getValueMetaList() ) {
                  String key = buildStepFieldKey( prevStepName, field.getName() );
                  fieldMap.put( key, new SourceStepField( prevStepName, field.getName() ) );
                }
              }
              String[] sourceFields = fieldMap.keySet().toArray( new String[fieldMap.size()] );
              Arrays.sort( sourceFields );

              EnterSelectionDialog selectSourceField =
                new EnterSelectionDialog(
                  shell, sourceFields, "Select source field", "Select the source field (cancel=clear)" );
              if ( source != null && !Const.isEmpty( source.getStepname() ) ) {
                String key = buildStepFieldKey( source.getStepname(), source.getField() );
                int index = Const.indexOfString( key, sourceFields );
                if ( index >= 0 ) {
                  selectSourceField.setSelectedNrs( new int[] { index, } );
                }
              }
              String selectedStepField = selectSourceField.open();
              if ( selectedStepField != null ) {
                SourceStepField newSource = fieldMap.get( selectedStepField );
                item.setText( 2, newSource.getStepname() );
                item.setText( 3, newSource.getField() );
                targetSourceMapping.put( target, newSource );
              } else {
                item.setText( 2, "" );
                item.setText( 3, "" );
                targetSourceMapping.remove( target );
              }

              /*
               * EnterSelectionDialog selectStep = new EnterSelectionDialog(shell, prevStepNames, "Select source step",
               * "Select the source step"); if (source!=null && !Const.isEmpty(source.getStepname())) { int index =
               * Const.indexOfString(source.getStepname(), prevStepNames); if (index>=0) { selectStep.setSelectedNrs(new
               * int[] {index,}); } } String prevStep = selectStep.open(); if (prevStep!=null) { // OK, now we list the
               * fields from that step... // RowMetaInterface fields = transMeta.getStepFields(prevStep); String[]
               * fieldNames = fields.getFieldNames(); Arrays.sort(fieldNames); EnterSelectionDialog selectField = new
               * EnterSelectionDialog(shell, fieldNames, "Select field", "Select the source field"); if (source!=null &&
               * !Const.isEmpty(source.getField())) { int index = Const.indexOfString(source.getField(), fieldNames); if
               * (index>=0) { selectField.setSelectedNrs(new int[] {index,}); } } String fieldName = selectField.open();
               * if (fieldName!=null) { // Store the selection, update the UI... // item.setText(2, prevStep);
               * item.setText(3, fieldName); source = new SourceStepField(prevStep, fieldName);
               * targetSourceMapping.put(target, source); } } else { item.setText(2, ""); item.setText(3, "");
               * targetSourceMapping.remove(target); }
               */
            }

          }
        } catch ( Exception e ) {
          new ErrorDialog( shell, "Oops", "Unexpected Error", e );
        }
      }
    } );

    FormData fdInjectComp = new FormData();
    fdInjectComp.left = new FormAttachment( 0, 0 );
    fdInjectComp.top = new FormAttachment( 0, 0 );
    fdInjectComp.right = new FormAttachment( 100, 0 );
    fdInjectComp.bottom = new FormAttachment( 100, 0 );
    wInjectComp.setLayoutData( fdInjectComp );

    wInjectComp.pack();
    Rectangle bounds = wInjectComp.getBounds();

    wInjectSComp.setContent( wInjectComp );
    wInjectSComp.setExpandHorizontal( true );
    wInjectSComp.setExpandVertical( true );
    wInjectSComp.setMinWidth( bounds.width );
    wInjectSComp.setMinHeight( bounds.height );

    wInjectTab.setControl( wInjectSComp );

    // ///////////////////////////////////////////////////////////
    // / END OF INJECT TAB
    // ///////////////////////////////////////////////////////////
  }

  protected void selectTransformationByReference() {
    if ( repository != null ) {
      SelectObjectDialog sod = new SelectObjectDialog( shell, repository, true, false );
      sod.open();
      RepositoryElementMetaInterface repositoryObject = sod.getRepositoryObject();
      if ( repositoryObject != null ) {
        specificationMethod = ObjectLocationSpecificationMethod.REPOSITORY_BY_REFERENCE;
        getByReferenceData( repositoryObject );
        referenceObjectId = repositoryObject.getObjectId();
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
        wFilename.setText( "" );
        wTransname.setText( injectTransMeta.getName() );
        wDirectory.setText( injectTransMeta.getRepositoryDirectory().getPath() );
        radioByName.setSelection( true );
        radioFilename.setSelection( false );
        validateTrans();
      }
    } catch ( KettleException ke ) {
      new ErrorDialog(
        shell, BaseMessages.getString( PKG, "MetaInjectDialog.ErrorSelectingObject.DialogTitle" ), BaseMessages
          .getString( PKG, "MetaInjectDialog.ErrorSelectingObject.DialogMessage" ), ke );
    }
  }

  private void loadRepositoryTrans( String transName, RepositoryDirectoryInterface repdir ) throws KettleException {
    // Read the transformation...
    //
    injectTransMeta = repository.loadTransformation(
      transMeta.environmentSubstitute( transName ), repdir, null, true, null );
    injectTransMeta.clearChanged();
  }

  private void selectFileTrans( boolean useVfs ) {
    String curFile = wFilename.getText();

    if ( useVfs ) {
      FileObject root = null;

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
        String fname = null;

        fname = file.getURL().getFile();

        if ( fname != null ) {

          loadFileTrans( fname );
          wFilename.setText( injectTransMeta.getFilename() );
          wTransname.setText( Const.NVL( injectTransMeta.getName(), "" ) );
          wDirectory.setText( "" );
          specificationMethod = ObjectLocationSpecificationMethod.FILENAME;
          setRadioButtons();
        }
      } catch ( IOException e ) {
        new ErrorDialog( shell,
          BaseMessages.getString( PKG, "MetaInjectDialog.ErrorLoadingTransformation.DialogTitle" ),
          BaseMessages.getString( PKG, "MetaInjectDialog.ErrorLoadingTransformation.DialogMessage" ), e );
      } catch ( KettleException e ) {
        new ErrorDialog( shell,
          BaseMessages.getString( PKG, "MetaInjectDialog.ErrorLoadingTransformation.DialogTitle" ),
          BaseMessages.getString( PKG, "MetaInjectDialog.ErrorLoadingTransformation.DialogMessage" ), e );
      }
    }

    // else: Local file open dialog, ask for .ktr & xml files...

  }

  private void loadFileTrans( String fname ) throws KettleException {
    injectTransMeta = new TransMeta( transMeta.environmentSubstitute( fname ) );
    injectTransMeta.clearChanged();
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
        spoon.addTransGraph( injectTransMeta );
      }
    } catch ( KettleException e ) {
      new ErrorDialog(
        shell, BaseMessages.getString( PKG, "MetaInjectDialog.ErrorShowingTransformation.Title" ), BaseMessages
          .getString( PKG, "MetaInjectDialog.ErrorShowingTransformation.Message" ), e );
    }
  }

  /**
   * validate the transformation specified and refresh UI information
   */
  private void validateTrans() {
    try {
      loadTransformation();
      refreshTree();
    } catch ( KettleException e ) {
      new ErrorDialog(
        shell, BaseMessages.getString( PKG, "MetaInjectDialog.ErrorValidatingTransformation.Title" ),
        BaseMessages.getString( PKG, "MetaInjectDialog.ErrorValidatingTransformation.Message" ), e );
    }
  }

  private void loadTransformation() throws KettleException {
    switch ( specificationMethod ) {
      case FILENAME:
        loadFileTrans( wFilename.getText() );
        break;
      case REPOSITORY_BY_NAME:
        String realDirectory = transMeta.environmentSubstitute( wDirectory.getText() );
        String realTransname = transMeta.environmentSubstitute( wTransname.getText() );

        if ( Const.isEmpty( realDirectory ) || Const.isEmpty( realTransname ) ) {
          throw new KettleException( BaseMessages.getString(
            PKG, "MetaInjectDialog.Exception.NoValidMappingDetailsFound" ) );
        }
        RepositoryDirectoryInterface repdir = repository.findDirectory( realDirectory );
        if ( repdir == null ) {
          throw new KettleException( BaseMessages.getString(
            PKG, "MetaInjectDialog.Exception.UnableToFindRepositoryDirectory)" ) );
        }
        loadRepositoryTrans( realTransname, repdir );
        break;
      case REPOSITORY_BY_REFERENCE:
        injectTransMeta = repository.loadTransformation( referenceObjectId, null ); // load the last version
        injectTransMeta.clearChanged();
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
    wTransname.setEnabled( repository != null && radioByName.getSelection() );

    wDirectory.setEnabled( repository != null && radioByName.getSelection() );

    wbTrans.setEnabled( repository != null && radioByName.getSelection() );

    wByReference.setEnabled( repository != null && radioByReference.getSelection() && supportsReferences );
    wbByReference.setEnabled( repository != null && radioByReference.getSelection() && supportsReferences );

    boolean outputCapture = !Const.isEmpty( wSourceStep.getText() );
    wlSourceFields.setEnabled( outputCapture );
    wSourceFields.setEnabled( outputCapture );

    boolean streaming = !Const.isEmpty( wStreamingSourceStep.getText() );
    wStreamingTargetStep.setEnabled( streaming );
    wlStreamingTargetStep.setEnabled( streaming );
  }

  protected void setRadioButtons() {
    radioFilename.setSelection( specificationMethod == ObjectLocationSpecificationMethod.FILENAME );
    radioByName.setSelection( specificationMethod == ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME );
    radioByReference
      .setSelection( specificationMethod == ObjectLocationSpecificationMethod.REPOSITORY_BY_REFERENCE );
    setActive();
  }

  private void getByReferenceData( RepositoryElementMetaInterface transInf ) {
    String path = transInf.getRepositoryDirectory().getPath();
    if ( !path.endsWith( "/" ) ) {
      path += "/";
    }
    path += transInf.getName();
    wByReference.setText( path );
  }

  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void getData() {
    specificationMethod = metaInjectMeta.getSpecificationMethod();
    switch ( specificationMethod ) {
      case FILENAME:
        wFilename.setText( Const.NVL( metaInjectMeta.getFileName(), "" ) );
        break;
      case REPOSITORY_BY_NAME:
        wDirectory.setText( Const.NVL( metaInjectMeta.getDirectoryPath(), "" ) );
        wTransname.setText( Const.NVL( metaInjectMeta.getTransName(), "" ) );
        break;
      case REPOSITORY_BY_REFERENCE:
        referenceObjectId = metaInjectMeta.getTransObjectId();
        wByReference.setText( "" );
        try {
          RepositoryObject transInf =
            repository.getObjectInformation(
              metaInjectMeta.getTransObjectId(), RepositoryObjectType.TRANSFORMATION );
          if ( transInf != null ) {
            getByReferenceData( transInf );
          }
        } catch ( KettleException e ) {
          new ErrorDialog( shell,
            BaseMessages.getString( PKG, "MetaInjectDialog.Exception.UnableToReferenceObjectId.Title" ),
            BaseMessages.getString( PKG, "MetaInjectDialog.Exception.UnableToReferenceObjectId.Message" ), e );
        }
        break;
      default:
        break;
    }

    wSourceStep.setText( Const.NVL( metaInjectMeta.getSourceStepName(), "" ) );
    int rownr = 0;
    for ( MetaInjectOutputField field : metaInjectMeta.getSourceOutputFields() ) {
      int colnr = 1;
      wSourceFields.setText( field.getName(), colnr++, rownr );
      wSourceFields.setText( field.getTypeDescription(), colnr++, rownr );
      wSourceFields.setText( field.getLength() < 0 ? "" : Integer.toString( field.getLength() ), colnr++, rownr );
      wSourceFields.setText( field.getPrecision() < 0 ? "" : Integer.toString( field.getPrecision() ), colnr++, rownr );
      rownr++;
    }

    wTargetFile.setText( Const.NVL( metaInjectMeta.getTargetFile(), "" ) );
    wNoExecution.setSelection( metaInjectMeta.isNoExecution() );

    wStreamingSourceStep.setText( Const.NVL(
      metaInjectMeta.getStreamSourceStep() == null ? null : metaInjectMeta.getStreamSourceStep().getName(), "" ) );
    wStreamingTargetStep.setText( Const.NVL( metaInjectMeta.getStreamTargetStepname(), "" ) );

    setRadioButtons();

    refreshTree();

    wTabFolder.setSelection( 0 );

    wStepname.selectAll();
    wStepname.setFocus();
  }

  protected String buildStepFieldKey( String stepname, String field ) {
    return stepname + " : " + field;
  }

  private void refreshTree() {
    try {
      loadTransformation();

      treeItemTargetMap = new HashMap<TreeItem, TargetStepAttribute>();

      wTree.removeAll();

      TreeItem transItem = new TreeItem( wTree, SWT.NONE );
      transItem.setExpanded( true );
      transItem.setText( injectTransMeta.getName() );
      List<StepMeta> injectSteps = new ArrayList<StepMeta>();
      for ( StepMeta stepMeta : injectTransMeta.getUsedSteps() ) {
        StepMetaInterface meta = stepMeta.getStepMetaInterface();
        if ( meta.getStepMetaInjectionInterface() != null || BeanInjectionInfo.isInjectionSupported( meta.getClass() ) ) {
          injectSteps.add( stepMeta );
        }
      }
      Collections.sort( injectSteps );

      for ( StepMeta stepMeta : injectSteps ) {
        TreeItem stepItem = new TreeItem( transItem, SWT.NONE );
        stepItem.setText( stepMeta.getName() );
        stepItem.setExpanded( true );

        // For each step, add the keys
        //
        StepMetaInterface metaInterface = stepMeta.getStepMetaInterface();
        if ( BeanInjectionInfo.isInjectionSupported( metaInterface.getClass() ) ) {
          processNewMDIDescription( stepMeta, stepItem, metaInterface );
        } else {
          processOldMDIDescription( stepMeta, stepItem, metaInterface.getStepMetaInjectionInterface() );
        }
      }

    } catch ( Throwable t ) {
      // Ignore errors
    }

    for ( TreeItem item : wTree.getItems() ) {
      expandItemAndChildren( item );
    }

    // Also set the source step combo values
    //
    if ( injectTransMeta != null ) {
      String[] sourceSteps = injectTransMeta.getStepNames();
      Arrays.sort( sourceSteps );
      wSourceStep.setItems( sourceSteps );
      wStreamingTargetStep.setItems( sourceSteps );
    }
  }

  private void processOldMDIDescription( StepMeta stepMeta, TreeItem stepItem, StepMetaInjectionInterface injection )
    throws KettleException {
    List<StepInjectionMetaEntry> entries = injection.getStepInjectionMetadataEntries();
    for ( final StepInjectionMetaEntry entry : entries ) {
      if ( entry.getValueType() != ValueMetaInterface.TYPE_NONE ) {
        TreeItem entryItem = new TreeItem( stepItem, SWT.NONE );
        entryItem.setText( entry.getKey() );
        entryItem.setText( 1, entry.getDescription() );
        TargetStepAttribute target = new TargetStepAttribute( stepMeta.getName(), entry.getKey(), false );
        treeItemTargetMap.put( entryItem, target );

        SourceStepField source = targetSourceMapping.get( target );
        if ( source != null ) {
          entryItem.setText( 2, Const.NVL( source.getStepname(), "" ) );
          entryItem.setText( 3, Const.NVL( source.getField(), "" ) );
        }
      } else {
        // Fields...
        //
        TreeItem listsItem = new TreeItem( stepItem, SWT.NONE );
        listsItem.setText( entry.getKey() );
        listsItem.setText( 1, entry.getDescription() );
        StepInjectionMetaEntry listEntry = entry.getDetails().get( 0 );
        listsItem.addListener( SWT.Selection, new Listener() {

          @Override
          public void handleEvent( Event arg0 ) {
            System.out.println( entry.getKey() + " - " + entry.getDescription() );
          }
        } );

        /*
         * // Field... // TreeItem listItem = new TreeItem(listsItem, SWT.NONE);
         * listItem.setText(listEntry.getKey()); listItem.setText(1, listEntry.getDescription());
         */

        for ( StepInjectionMetaEntry me : listEntry.getDetails() ) {
          TreeItem treeItem = new TreeItem( listsItem, SWT.NONE );
          treeItem.setText( me.getKey() );
          treeItem.setText( 1, me.getDescription() );

          TargetStepAttribute target = new TargetStepAttribute( stepMeta.getName(), me.getKey(), true );
          treeItemTargetMap.put( treeItem, target );

          SourceStepField source = targetSourceMapping.get( target );
          if ( source != null ) {
            treeItem.setText( 2, Const.NVL( source.getStepname(), "" ) );
            treeItem.setText( 3, Const.NVL( source.getField(), "" ) );
          }
        }
      }
    }
  }

  private void processNewMDIDescription( StepMeta stepMeta, TreeItem stepItem, StepMetaInterface metaInterface ) {
    BeanInjectionInfo stepInjectionInfo = new BeanInjectionInfo( metaInterface.getClass() );

    for ( BeanInjectionInfo.Group gr : stepInjectionInfo.getGroups() ) {
      boolean rootGroup = StringUtils.isEmpty( gr.getName() );
      TreeItem groupItem;
      if ( !rootGroup ) {
        groupItem = new TreeItem( stepItem, SWT.NONE );
        groupItem.setText( gr.getName() );
        groupItem.setText( 1, gr.getDescription() );
      } else {
        groupItem = null;
      }
      for ( BeanInjectionInfo.Property property : gr.getGroupProperties() ) {
        TreeItem treeItem = new TreeItem( rootGroup ? stepItem : groupItem, SWT.NONE );
        treeItem.setText( property.getName() );
        treeItem.setText( 1, property.getDescription() );

        TargetStepAttribute target = new TargetStepAttribute( stepMeta.getName(), property.getName(), !rootGroup );
        treeItemTargetMap.put( treeItem, target );

        SourceStepField source = targetSourceMapping.get( target );
        if ( source != null ) {
          treeItem.setText( 2, Const.NVL( source.getStepname(), "" ) );
          treeItem.setText( 3, Const.NVL( source.getField(), "" ) );
        }
      }
    }
  }

  private void expandItemAndChildren( TreeItem item ) {
    item.setExpanded( true );
    for ( TreeItem item2 : item.getItems() ) {
      expandItemAndChildren( item2 );
    }

  }

  private void cancel() {
    stepname = null;
    metaInjectMeta.setChanged( changed );
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
      new ErrorDialog( shell,
        BaseMessages.getString( PKG, "MetaInjectDialog.ErrorLoadingSpecifiedTransformation.Title" ),
        BaseMessages.getString( PKG, "MetaInjectDialog.ErrorLoadingSpecifiedTransformation.Message" ), e );
    }

    metaInjectMeta.setSpecificationMethod( specificationMethod );
    switch ( specificationMethod ) {
      case FILENAME:
        metaInjectMeta.setFileName( wFilename.getText() );
        metaInjectMeta.setDirectoryPath( null );
        metaInjectMeta.setTransName( null );
        metaInjectMeta.setTransObjectId( null );
        break;
      case REPOSITORY_BY_NAME:
        metaInjectMeta.setDirectoryPath( wDirectory.getText() );
        metaInjectMeta.setTransName( wTransname.getText() );
        metaInjectMeta.setFileName( null );
        metaInjectMeta.setTransObjectId( null );
        break;
      case REPOSITORY_BY_REFERENCE:
        metaInjectMeta.setFileName( null );
        metaInjectMeta.setDirectoryPath( null );
        metaInjectMeta.setTransName( null );
        metaInjectMeta.setTransObjectId( referenceObjectId );
        break;
      default:
        break;
    }

    metaInjectMeta.setSourceStepName( wSourceStep.getText() );
    metaInjectMeta.setSourceOutputFields( new ArrayList<MetaInjectOutputField>() );
    for ( int i = 0; i < wSourceFields.nrNonEmpty(); i++ ) {
      TableItem item = wSourceFields.getNonEmpty( i );
      int colIndex = 1;
      String name = item.getText( colIndex++ );
      int type = ValueMetaFactory.getIdForValueMeta( item.getText( colIndex++ ) );
      int length = Const.toInt( item.getText( colIndex++ ), -1 );
      int precision = Const.toInt( item.getText( colIndex++ ), -1 );
      metaInjectMeta.getSourceOutputFields().add( new MetaInjectOutputField( name, type, length, precision ) );
    }

    metaInjectMeta.setTargetFile( wTargetFile.getText() );
    metaInjectMeta.setNoExecution( wNoExecution.getSelection() );

    metaInjectMeta.setStreamSourceStep( transMeta.findStep( wStreamingSourceStep.getText() ) );
    metaInjectMeta.setStreamTargetStepname( wStreamingTargetStep.getText() );

    metaInjectMeta.setTargetSourceMapping( targetSourceMapping );
    metaInjectMeta.setChanged( true );

    dispose();
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
      }
    }
  }

  private void getByReferenceData( ObjectId transObjectId ) {
    try {
      if ( repository == null ) {
        throw new KettleException( BaseMessages.getString(
          PKG, "MappingDialog.Exception.NotConnectedToRepository.Message" ) );
      }
      RepositoryObject transInf = repository.getObjectInformation( transObjectId, RepositoryObjectType.JOB );
      if ( transInf != null ) {
        getByReferenceData( transInf );
      }
    } catch ( KettleException e ) {
      new ErrorDialog( shell,
        BaseMessages.getString( PKG, "MappingDialog.Exception.UnableToReferenceObjectId.Title" ),
        BaseMessages.getString( PKG, "MappingDialog.Exception.UnableToReferenceObjectId.Message" ), e );
    }
  }
}
