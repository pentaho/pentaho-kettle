/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2023 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.metainject;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
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
import org.pentaho.di.core.annotations.PluginDialog;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.injection.bean.BeanInjectionInfo;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.util.Utils;
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
import org.pentaho.di.ui.core.ConstUI;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.events.dialog.FilterType;
import org.pentaho.di.ui.core.events.dialog.SelectionOperation;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.ColumnsResizer;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.util.DialogHelper;
import org.pentaho.di.ui.util.DialogUtils;
import org.pentaho.di.ui.util.SwtSvgImageUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@PluginDialog(
    id = "MetaInject",
    image = "org/pentaho/di/trans/steps/metainject/img/GenericTransform.svg",
    pluginType = PluginDialog.PluginType.STEP,
    documentationUrl = "mk-95pdia003/pdi-transformation-steps/etl-metadata-injection"
)
public class MetaInjectDialog extends BaseStepDialog implements StepDialogInterface {

  public static final String CONST_VALUE = "<const>";
  private static Class<?> PKG = MetaInjectMeta.class; // for i18n purposes, needed by Translator2!!

  private MetaInjectMeta metaInjectMeta;

  private Label wlPath;
  private TextVar wPath;

  private Button wbBrowse;

  private CTabFolder wTabFolder;

  private CTabItem wOptionsTab;
  private ScrolledComposite wOptionsSComp;
  private Composite wOptionsComp;

  private CTabItem wInjectTab;
  private ScrolledComposite wInjectSComp;
  private Composite wInjectComp;

  private TransMeta injectTransMeta = null;

  protected boolean transModified;

  private ModifyListener lsMod;

  private ObjectId referenceObjectId;
  private ObjectLocationSpecificationMethod specificationMethod;

  // the source step
  //
  private CCombo wSourceStep;

  // The source step output fields...
  //
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

    targetSourceMapping = new HashMap<>();
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
    formLayout.marginWidth = 15;
    formLayout.marginHeight = 15;

    shell.setLayout( formLayout );
    shell.setText( BaseMessages.getString( PKG, "MetaInjectDialog.Shell.Title" ) );

    Label wicon = new Label( shell, SWT.RIGHT );
    wicon.setImage( getImage() );
    FormData fdlicon = new FormData();
    fdlicon.top = new FormAttachment( 0, 0 );
    fdlicon.right = new FormAttachment( 100, 0 );
    wicon.setLayoutData( fdlicon );
    props.setLook( wicon );

    // Stepname line
    wlStepname = new Label( shell, SWT.RIGHT );
    wlStepname.setText( BaseMessages.getString( PKG, "MetaInjectDialog.Stepname.Label" ) );
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
    wlPath.setText( BaseMessages.getString( PKG, "MetaInjectDialog.Transformation.Label" ) );
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
    wPath.addFocusListener( new FocusAdapter() {
      @Override public void focusLost( FocusEvent focusEvent ) {
        refreshTree();
      }
    } );

    wbBrowse = new Button( shell, SWT.PUSH );
    props.setLook( wbBrowse );
    wbBrowse.setText( BaseMessages.getString( PKG, "MetaInjectDialog.Browse.Label" ) );
    FormData fdBrowse = new FormData();
    fdBrowse.left = new FormAttachment( wPath, 5 );
    fdBrowse.top = new FormAttachment( wlPath, Const.isOSX() ? 0 : 5 );
    wbBrowse.setLayoutData( fdBrowse );
    wbBrowse.addSelectionListener( DialogHelper.constructSelectionAdapterFileDialogTextVarForKettleFile( log, wPath, transMeta,
        SelectionOperation.FILE_OR_FOLDER, FilterType.KETTLE_TRANS, repository ) );

    wTabFolder = new CTabFolder( shell, SWT.BORDER );
    props.setLook( wTabFolder, Props.WIDGET_STYLE_TAB );
    wTabFolder.setSimple( false );

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

    addInjectTab();
    addOptionsTab();

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

    wPath.addSelectionListener( lsDef );
    wStepname.addSelectionListener( lsDef );

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

  private Image getImage() {
    return SwtSvgImageUtil
      .getImage( shell.getDisplay(), getClass().getClassLoader(), "GenericTransform.svg", ConstUI.LARGE_ICON_SIZE,
        ConstUI.LARGE_ICON_SIZE );
  }

  private void checkInvalidMapping() {
    if ( injectTransMeta == null ) {
      try {
        if ( !loadTransformation() ) {
          return;
        }
      } catch ( KettleException e ) {
        showErrorOnLoadTransformationDialog( e );
        return;
      }
    }
    Set<SourceStepField> unavailableSourceSteps =
        MetaInject.getUnavailableSourceSteps( targetSourceMapping, transMeta, stepMeta );
    Set<TargetStepAttribute> unavailableTargetSteps =
        MetaInject.getUnavailableTargetSteps( targetSourceMapping, injectTransMeta );
    Set<TargetStepAttribute> missingTargetKeys =
        MetaInject.getUnavailableTargetKeys( targetSourceMapping, injectTransMeta, unavailableTargetSteps );
    if ( unavailableSourceSteps.isEmpty() && unavailableTargetSteps.isEmpty() && missingTargetKeys.isEmpty() ) {
      return;
    }
    showInvalidMappingDialog( unavailableSourceSteps, unavailableTargetSteps, missingTargetKeys );
  }

  private void showInvalidMappingDialog( Set<SourceStepField> unavailableSourceSteps,
      Set<TargetStepAttribute> unavailableTargetSteps, Set<TargetStepAttribute> missingTargetKeys ) {
    MessageBox mb = new MessageBox( shell, SWT.YES | SWT.NO | SWT.ICON_QUESTION );
    mb.setMessage( BaseMessages.getString( PKG, "MetaInjectDialog.InvalidMapping.Question" ) );
    mb.setText( BaseMessages.getString( PKG, "MetaInjectDialog.InvalidMapping.Title" ) );
    int id = mb.open();
    if ( id == SWT.YES ) {
      MetaInject.removeUnavailableStepsFromMapping( targetSourceMapping, unavailableSourceSteps,
          unavailableTargetSteps );
      for ( TargetStepAttribute target : missingTargetKeys ) {
        targetSourceMapping.remove( target );
      }
    }
  }

  private void showErrorOnLoadTransformationDialog( KettleException e ) {
    new ErrorDialog( shell, BaseMessages.getString( PKG, "MetaInjectDialog.ErrorLoadingSpecifiedTransformation.Title" ),
        BaseMessages.getString( PKG, "MetaInjectDialog.ErrorLoadingSpecifiedTransformation.Message" ), e );
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
    fileLayout.marginWidth = 15;
    fileLayout.marginHeight = 15;
    wOptionsComp.setLayout( fileLayout );

    Label wlSourceStep = new Label( wOptionsComp, SWT.RIGHT );
    wlSourceStep.setText( BaseMessages.getString( PKG, "MetaInjectDialog.SourceStep.Label" ) );
    props.setLook( wlSourceStep );
    FormData fdlSourceStep = new FormData();
    fdlSourceStep.left = new FormAttachment( 0, 0 );
    fdlSourceStep.top = new FormAttachment( 0, 0 );
    wlSourceStep.setLayoutData( fdlSourceStep );

    wSourceStep = new CCombo( wOptionsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wSourceStep );
    wSourceStep.addModifyListener( lsMod );
    FormData fdSourceStep = new FormData();
    fdSourceStep.width = 300;
    fdSourceStep.left = new FormAttachment( 0, 0 );
    fdSourceStep.top = new FormAttachment( wlSourceStep, 5 );
    wSourceStep.setLayoutData( fdSourceStep );
    wSourceStep.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent arg0 ) {
        setActive();
      }
    } );

    final int fieldRows = metaInjectMeta.getSourceOutputFields().size();

    ColumnInfo[] colinf =
      new ColumnInfo[] {
        new ColumnInfo(
          BaseMessages.getString( PKG, "MetaInjectDialog.ColumnInfo.Fieldname" ), ColumnInfo.COLUMN_TYPE_TEXT,
          false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "MetaInjectDialog.ColumnInfo.Type" ), ColumnInfo.COLUMN_TYPE_CCOMBO,
          ValueMetaFactory.getAllValueMetaNames() ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "MetaInjectDialog.ColumnInfo.Length" ), ColumnInfo.COLUMN_TYPE_TEXT,
          false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "MetaInjectDialog.ColumnInfo.Precision" ), ColumnInfo.COLUMN_TYPE_TEXT,
          false ), };

    wSourceFields =
      new TableView( transMeta, wOptionsComp, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf, fieldRows, false,
        lsMod, props, false );

    FormData fdFields = new FormData();
    fdFields.height = 150;
    fdFields.left = new FormAttachment( 0, 0 );
    fdFields.top = new FormAttachment( wSourceStep, 10 );
    fdFields.right = new FormAttachment( 100, 0 );
    wSourceFields.setLayoutData( fdFields );
    wSourceFields.getTable().addListener( SWT.Resize, new ColumnsResizer( 0, 25, 25, 25, 25 ) );

    Label wlTargetFile = new Label( wOptionsComp, SWT.RIGHT );
    wlTargetFile.setText( BaseMessages.getString( PKG, "MetaInjectDialog.TargetFile.Label" ) );
    props.setLook( wlTargetFile );
    FormData fdlTargetFile = new FormData();
    fdlTargetFile.left = new FormAttachment( 0, 0 );
    fdlTargetFile.top = new FormAttachment( wSourceFields, 10 );
    wlTargetFile.setLayoutData( fdlTargetFile );

    wTargetFile = new TextVar( transMeta, wOptionsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wTargetFile );
    wTargetFile.addModifyListener( lsMod );
    FormData fdTargetFile = new FormData();
    fdTargetFile.width = 300;
    fdTargetFile.left = new FormAttachment( 0, 0 );
    fdTargetFile.top = new FormAttachment( wlTargetFile, 5 );
    wTargetFile.setLayoutData( fdTargetFile );

    wlStreamingSourceStep = new Label( wOptionsComp, SWT.RIGHT );
    wlStreamingSourceStep.setText( BaseMessages.getString( PKG, "MetaInjectDialog.StreamingSourceStep.Label" ) );
    props.setLook( wlStreamingSourceStep );
    FormData fdlStreamingSourceStep = new FormData();
    fdlStreamingSourceStep.left = new FormAttachment( 0, 0 );
    fdlStreamingSourceStep.top = new FormAttachment( wTargetFile, 10 );
    wlStreamingSourceStep.setLayoutData( fdlStreamingSourceStep );

    wStreamingSourceStep = new CCombo( wOptionsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wStreamingSourceStep );
    FormData fdStreamingSourceStep = new FormData();
    fdStreamingSourceStep.width = 300;
    fdStreamingSourceStep.left = new FormAttachment( 0, 0 );
    fdStreamingSourceStep.top = new FormAttachment( wlStreamingSourceStep, 5 );
    wStreamingSourceStep.setLayoutData( fdStreamingSourceStep );
    wStreamingSourceStep.setItems( transMeta.getStepNames() );
    wStreamingSourceStep.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent arg0 ) {
        setActive();
      }
    } );

    wlStreamingTargetStep = new Label( wOptionsComp, SWT.RIGHT );
    wlStreamingTargetStep.setText( BaseMessages.getString( PKG, "MetaInjectDialog.StreamingTargetStep.Label" ) );
    props.setLook( wlStreamingTargetStep );
    FormData fdlStreamingTargetStep = new FormData();
    fdlStreamingTargetStep.left = new FormAttachment( 0, 0 );
    fdlStreamingTargetStep.top = new FormAttachment( wStreamingSourceStep, 10 );
    wlStreamingTargetStep.setLayoutData( fdlStreamingTargetStep );

    wStreamingTargetStep = new CCombo( wOptionsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wStreamingTargetStep );
    FormData fdStreamingTargetStep = new FormData();
    fdStreamingTargetStep.width = 300;
    fdStreamingTargetStep.left = new FormAttachment( 0, 0 );
    fdStreamingTargetStep.top = new FormAttachment( wlStreamingTargetStep, 5 );
    wStreamingTargetStep.setLayoutData( fdStreamingTargetStep );

    wNoExecution = new Button( wOptionsComp, SWT.CHECK );
    wNoExecution.setText( BaseMessages.getString( PKG, "MetaInjectDialog.NoExecution.Label" ) );
    props.setLook( wNoExecution );
    FormData fdNoExecution = new FormData();
    fdNoExecution.width = 350;
    fdNoExecution.left = new FormAttachment( 0, 0 );
    fdNoExecution.top = new FormAttachment( wStreamingTargetStep, 10 );
    wNoExecution.setLayoutData( fdNoExecution );

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
    fileLayout.marginWidth = 15;
    fileLayout.marginHeight = 15;
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
          BaseMessages.getString( PKG, "MetaInjectDialog.Column.RequiredField" ),
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

              Map<String, SourceStepField> fieldMap = new HashMap<>();
              for ( String prevStepName : prevStepNames ) {
                RowMetaInterface fields = transMeta.getStepFields( prevStepName );
                for ( ValueMetaInterface field : fields.getValueMetaList() ) {
                  String key = buildStepFieldKey( prevStepName, field.getName() );
                  fieldMap.put( key, new SourceStepField( prevStepName, field.getName() ) );
                }
              }
              String[] sourceFields = fieldMap.keySet().toArray( new String[fieldMap.size()] );
              Arrays.sort( sourceFields );

              String constant = source != null && source.getStepname() == null ? source.getField() : "";
              EnterSelectionDialog selectSourceField = new EnterSelectionDialog( shell, sourceFields,
                BaseMessages.getString( PKG, "MetaInjectDialog.SourceFieldDialog.Title" ),
                BaseMessages.getString( PKG, "MetaInjectDialog.SourceFieldDialog.Label" ), constant, transMeta );
              if ( source != null ) {
                if ( source.getStepname() != null && !Utils.isEmpty( source.getStepname() ) ) {
                  String key = buildStepFieldKey( source.getStepname(), source.getField() );
                  selectSourceField.setCurrentValue( key );
                  int index = Const.indexOfString( key, sourceFields );
                  if ( index >= 0 ) {
                    selectSourceField.setSelectedNrs( new int[] { index, } );
                  }
                } else {
                  selectSourceField.setCurrentValue( source.getField() );
                }
              }
              String selectedStepField = selectSourceField.open();
              if ( selectedStepField != null ) {
                SourceStepField newSource = fieldMap.get( selectedStepField );
                if ( newSource == null ) {
                  newSource = new SourceStepField( null, selectedStepField );
                  item.setText( 3, CONST_VALUE );
                  item.setText( 4, selectedStepField );
                } else {
                  item.setText( 3, newSource.getStepname() );
                  item.setText( 4, newSource.getField() );
                }
                targetSourceMapping.put( target, newSource );
              } else {
                item.setText( 3, "" );
                item.setText( 4, "" );
                targetSourceMapping.remove( target );
              }

              /*
               * EnterSelectionDialog selectStep = new EnterSelectionDialog(shell, prevStepNames, "Select source step",
               * "Select the source step"); if (source!=null && !Utils.isEmpty(source.getStepname())) { int index =
               * Const.indexOfString(source.getStepname(), prevStepNames); if (index>=0) { selectStep.setSelectedNrs(new
               * int[] {index,}); } } String prevStep = selectStep.open(); if (prevStep!=null) { // OK, now we list the
               * fields from that step... // RowMetaInterface fields = transMeta.getStepFields(prevStep); String[]
               * fieldNames = fields.getFieldNames(); Arrays.sort(fieldNames); EnterSelectionDialog selectField = new
               * EnterSelectionDialog(shell, fieldNames, "Select field", "Select the source field"); if (source!=null &&
               * !Utils.isEmpty(source.getField())) { int index = Const.indexOfString(source.getField(), fieldNames); if
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

  private void loadRepositoryTrans( String transName, RepositoryDirectoryInterface repdir ) throws KettleException {
    // Read the transformation...
    //
    injectTransMeta =
      repository.loadTransformation( transMeta.environmentSubstitute( transName ), repdir, null, true, null );
    injectTransMeta.clearChanged();
  }
  private void loadFileTrans( String fname ) throws KettleException {
    injectTransMeta = new TransMeta( transMeta.environmentSubstitute( fname ) );
    injectTransMeta.clearChanged();
  }

  private boolean loadTransformation() throws KettleException {
    String filename = wPath.getText();
    boolean isEmptyFilename = Utils.isEmpty( filename );
    if ( repository != null ) {
      specificationMethod = ( isEmptyFilename && referenceObjectId != null )
        ? ObjectLocationSpecificationMethod.REPOSITORY_BY_REFERENCE
        : ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME;
    } else {
      specificationMethod = ObjectLocationSpecificationMethod.FILENAME;
    }
    switch ( specificationMethod ) {
      case FILENAME:
        if ( isEmptyFilename ) {
          return false;
        }
        if ( !filename.endsWith( ".ktr" ) ) {
          filename = filename + ".ktr";
          wPath.setText( filename );
        }
        loadFileTrans( filename );
        break;
      case REPOSITORY_BY_NAME:
        if ( isEmptyFilename ) {
          return false;
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
            BaseMessages.getString( PKG, "SingleThreaderDialog.Exception.NoValidMappingDetailsFound" ) );
        }
        RepositoryDirectoryInterface repdir = repository.findDirectory( realDirectory );
        if ( repdir == null ) {
          throw new KettleException( BaseMessages.getString(
            PKG, "SingleThreaderDialog.Exception.UnableToFindRepositoryDirectory)" ) );
        }
        loadRepositoryTrans( realTransname, repdir );
        break;
      case REPOSITORY_BY_REFERENCE:
        if ( referenceObjectId == null ) {
          return false;
        }
        injectTransMeta = repository.loadTransformation( referenceObjectId, null ); // load the last version
        injectTransMeta.clearChanged();
        break;
      default:
        break;
    }
    return true;
  }

  public void setActive() {
    boolean outputCapture = !Utils.isEmpty( wSourceStep.getText() );
    wSourceFields.setEnabled( outputCapture );

    boolean streaming = !Utils.isEmpty( wStreamingSourceStep.getText() );
    wStreamingTargetStep.setEnabled( streaming );
    wlStreamingTargetStep.setEnabled( streaming );
  }

  private void getByReferenceData( RepositoryElementMetaInterface transInf  ) {
    String path =
      DialogUtils.getPath( transMeta.getRepositoryDirectory().getPath(), transInf.getRepositoryDirectory().getPath() );
    String fullPath =
      Const.NVL( path, "" ) + "/" + Const.NVL( transInf.getName(), "" );
    wPath.setText( fullPath );
  }

  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void getData() {
    specificationMethod = metaInjectMeta.getSpecificationMethod();
    switch ( specificationMethod ) {
      case FILENAME:
        wPath.setText( Const.NVL( metaInjectMeta.getFileName(), "" ) );
        break;
      case REPOSITORY_BY_NAME:
        String fullPath = Const.NVL( metaInjectMeta.getDirectoryPath(), "" ) + "/" + Const
          .NVL( metaInjectMeta.getTransName(), "" );
        wPath.setText( fullPath );
        break;
      case REPOSITORY_BY_REFERENCE:
        referenceObjectId = metaInjectMeta.getTransObjectId();
        getByReferenceData( referenceObjectId );
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
    wNoExecution.setSelection( !metaInjectMeta.isNoExecution() );

    wStreamingSourceStep.setText( Const.NVL(
      metaInjectMeta.getStreamSourceStep() == null ? null : metaInjectMeta.getStreamSourceStep().getName(), "" ) );
    wStreamingTargetStep.setText( Const.NVL( metaInjectMeta.getStreamTargetStepname(), "" ) );

    setActive();
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

      treeItemTargetMap = new HashMap<>();

      wTree.removeAll();

      TreeItem transItem = new TreeItem( wTree, SWT.NONE );
      transItem.setExpanded( true );
      transItem.setText( injectTransMeta.getName() );
      List<StepMeta> injectSteps = new ArrayList<>();
      for ( StepMeta stepMeta : injectTransMeta.getUsedSteps() ) {
        StepMetaInterface meta = stepMeta.getStepMetaInterface();
        if ( meta.getStepMetaInjectionInterface() != null || BeanInjectionInfo
          .isInjectionSupported( meta.getClass() ) ) {
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
          entryItem.setText( 3, Const.NVL( source.getStepname(), "" ) );
          entryItem.setText( 4, Const.NVL( source.getField(), "" ) );
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
            treeItem.setText( 3, Const.NVL( source.getStepname(), "" ) );
            treeItem.setText( 4, Const.NVL( source.getField(), "" ) );
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
        treeItem.setText( 2, stepInjectionInfo.getProperties().get( property.getName() ).isRequire() ? "Y" : "" );
        TargetStepAttribute target = new TargetStepAttribute( stepMeta.getName(), property.getName(), !rootGroup );
        treeItemTargetMap.put( treeItem, target );

        SourceStepField source = targetSourceMapping.get( target );
        if ( source != null ) {
          treeItem.setText( 3, Const.NVL( source.getStepname() == null ? CONST_VALUE : source.getStepname(), "" ) );
          treeItem.setText( 4, Const.NVL( source.getField(), "" ) );
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
    if ( Utils.isEmpty( wStepname.getText() ) ) {
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

    if ( repository != null ) {
      specificationMethod = ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME;
    } else {
      specificationMethod = ObjectLocationSpecificationMethod.FILENAME;
    }
    metaInjectMeta.setSpecificationMethod( specificationMethod );
    switch ( specificationMethod ) {
      case FILENAME:
        metaInjectMeta.setFileName( wPath.getText() );
        metaInjectMeta.setDirectoryPath( null );
        metaInjectMeta.setTransName( null );
        metaInjectMeta.setTransObjectId( null );
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
        metaInjectMeta.setDirectoryPath( directory );
        metaInjectMeta.setTransName( transName );
        metaInjectMeta.setFileName( null );
        metaInjectMeta.setTransObjectId( null );
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
    metaInjectMeta.setNoExecution( !wNoExecution.getSelection() );

    final StepMeta streamSourceStep = transMeta.findStep( wStreamingSourceStep.getText() );
    metaInjectMeta.setStreamSourceStep( streamSourceStep );
    // PDI-15989 Save streamSourceStepname to find streamSourceStep when loading
    metaInjectMeta.setStreamSourceStepname( streamSourceStep != null ? streamSourceStep.getName() : "" );
    metaInjectMeta.setStreamTargetStepname( wStreamingTargetStep.getText() );

    metaInjectMeta.setTargetSourceMapping( targetSourceMapping );
    metaInjectMeta.setChanged( true );

    dispose();
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
