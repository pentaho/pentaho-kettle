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

package org.pentaho.di.ui.trans.step;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.extension.ExtensionPointHandler;
import org.pentaho.di.core.extension.KettleExtensionPoint;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.shared.SharedObjects;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.recordsfromstream.RecordsFromStreamMeta;
import org.pentaho.di.trans.streaming.common.BaseStreamStepMeta;
import org.pentaho.di.ui.core.ConstUI;
import org.pentaho.di.ui.core.FormDataBuilder;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.ComboVar;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.core.widget.VFSFileSelection;
import org.pentaho.di.ui.spoon.MainSpoonPerspective;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.dialog.NewSubtransDialog;
import org.pentaho.xul.swt.tab.TabItem;
import org.pentaho.xul.swt.tab.TabSet;

import java.util.Arrays;

import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;
import static org.pentaho.di.trans.StepWithMappingMeta.loadMappingMeta;

@SuppressWarnings ( { "FieldCanBeLocal", "unused", "WeakerAccess" } )
public abstract class BaseStreamingDialog extends BaseStepDialog implements StepDialogInterface {

  public static final int INPUT_WIDTH = 350;
  private static final Class<?> PKG = BaseStreamingDialog.class;
  // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

  protected BaseStreamStepMeta meta;
  private Spoon spoonInstance;

  protected Label wlTransPath;
  protected VFSFileSelection wFileSection;
  protected Button wbCreateSubtrans;

  protected Label wlSubStep;
  protected ComboVar wSubStep;

  protected ObjectId referenceObjectId;
  protected ObjectLocationSpecificationMethod specificationMethod;

  protected ModifyListener lsMod;
  protected Label wlBatchSize;
  protected TextVar wBatchSize;
  protected TextVar wPrefetchCount;
  protected Label wlBatchDuration;
  protected TextVar wBatchDuration;
  protected TextVar wParallelism;

  protected CTabFolder wTabFolder;
  protected CTabItem wSetupTab;
  protected CTabItem wBatchTab;
  protected CTabItem wResultsTab;

  protected Composite wSetupComp;
  protected Composite wBatchComp;
  protected Composite wResultsComp;

  protected TableView fieldsTable;

  public BaseStreamingDialog( Shell parent, Object in, TransMeta tr, String sname ) {
    super( parent, (BaseStepMeta) in, tr, sname );
    meta = (BaseStreamStepMeta) in;
    spoonInstance = Spoon.getInstance();
  }

  public String open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.MIN | SWT.MAX | SWT.RESIZE );
    props.setLook( shell );
    setShellImage( shell, meta );
    shell.setMinimumSize( 527, 622 );

    lsMod = e -> meta.setChanged();
    changed = meta.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = 15;
    formLayout.marginHeight = 15;

    shell.setLayout( formLayout );
    shell.setText( getDialogTitle() );

    Label wicon = new Label( shell, SWT.RIGHT );
    wicon.setImage( getImage() );
    FormData fdlicon = new FormData();
    fdlicon.top = new FormAttachment( 0, 0 );
    fdlicon.right = new FormAttachment( 100, 0 );
    wicon.setLayoutData( fdlicon );
    props.setLook( wicon );

    wlStepname = new Label( shell, SWT.RIGHT );
    wlStepname.setText( BaseMessages.getString( PKG, "BaseStreamingDialog.Stepname.Label" ) );
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
    props.setLook( spacer );
    FormData fdSpacer = new FormData();
    fdSpacer.height = 2;
    fdSpacer.left = new FormAttachment( 0, 0 );
    fdSpacer.top = new FormAttachment( wStepname, 15 );
    fdSpacer.right = new FormAttachment( 100, 0 );
    fdSpacer.width = 497;
    spacer.setLayoutData( fdSpacer );

    wlTransPath = new Label( shell, SWT.LEFT );
    props.setLook( wlTransPath );
    wlTransPath.setText( BaseMessages.getString( PKG, "BaseStreamingDialog.Transformation" ) );
    FormData fdlTransPath = new FormData();
    fdlTransPath.left = new FormAttachment( 0, 0 );
    fdlTransPath.top = new FormAttachment( spacer, 15 );
    fdlTransPath.right = new FormAttachment( 50, 0 );
    wlTransPath.setLayoutData( fdlTransPath );

    wFileSection = new VFSFileSelection( shell, SWT.SINGLE, Const.STRING_TRANS_FILTER_EXT, Const.getTransformationFilterNames(), transMeta, repository  );
    props.setLook( wFileSection );
    FormData fdFileSelection = new FormData();
    fdFileSelection.left = new FormAttachment( 0, 0 );
    fdFileSelection.top = new FormAttachment( wlTransPath, 5 );
    wFileSection.setLayoutData( fdFileSelection );

    wbCreateSubtrans = new Button( shell, SWT.PUSH );
    props.setLook( wbCreateSubtrans );
    wbCreateSubtrans.setText( BaseMessages.getString( PKG, "BaseStreaming.Dialog.Transformation.CreateSubtrans" ) );
    FormData fdCreateSubtrans = new FormData();
    fdCreateSubtrans.left = new FormAttachment( wFileSection, 5 );
    fdCreateSubtrans.top = new FormAttachment( wFileSection, 0, SWT.TOP );
    wbCreateSubtrans.setLayoutData( fdCreateSubtrans );

    wbCreateSubtrans.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        createNewSubtrans();
      }
    } );



    // Start of tabbed display
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
    props.setLook( hSpacer );
    FormData fdhSpacer = new FormData();
    fdhSpacer.height = 2;
    fdhSpacer.left = new FormAttachment( 0, 0 );
    fdhSpacer.bottom = new FormAttachment( wCancel, -15 );
    fdhSpacer.right = new FormAttachment( 100, 0 );
    hSpacer.setLayoutData( fdhSpacer );

    FormData fdTabFolder = new FormData();
    fdTabFolder.left = new FormAttachment( 0, 0 );
    fdTabFolder.top = new FormAttachment( wFileSection, 15 );
    fdTabFolder.bottom = new FormAttachment( hSpacer, -15 );
    fdTabFolder.right = new FormAttachment( 100, 0 );
    wTabFolder.setLayoutData( fdTabFolder );

    buildSetupTab();
    buildBatchTab();
    buildResultsTab();
    createAdditionalTabs();

    lsCancel = e -> cancel();
    lsOK = e -> ok();

    wOK.addListener( SWT.Selection, lsOK );
    wCancel.addListener( SWT.Selection, lsCancel );

    lsDef = new SelectionAdapter() {
      @Override
      public void widgetDefaultSelected( SelectionEvent e ) {
        ok();
      }
    };
    wStepname.addSelectionListener( lsDef );

    shell.addShellListener( new ShellAdapter() {
      @Override
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    getData();
    setSize();

    wTabFolder.setSelection( 0 );

    wStepname.selectAll();
    wStepname.setFocus();

    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return stepname;
  }

  protected abstract String getDialogTitle();

  private void buildSetupTab() {
    wSetupTab = new CTabItem( wTabFolder, SWT.NONE );
    wSetupTab.setText( BaseMessages.getString( PKG, "BaseStreamingDialog.SetupTab" ) );

    wSetupComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wSetupComp );
    FormLayout setupLayout = new FormLayout();
    setupLayout.marginHeight = 15;
    setupLayout.marginWidth = 15;
    wSetupComp.setLayout( setupLayout );

    buildSetup( wSetupComp );

    FormData fdSetupComp = new FormData();
    fdSetupComp.left = new FormAttachment( 0, 0 );
    fdSetupComp.top = new FormAttachment( 0, 0 );
    fdSetupComp.right = new FormAttachment( 100, 0 );
    fdSetupComp.bottom = new FormAttachment( 100, 0 );
    wSetupComp.setLayoutData( fdSetupComp );
    wSetupComp.layout();
    wSetupTab.setControl( wSetupComp );
  }

  protected abstract void buildSetup( Composite wSetupComp );

  protected void createAdditionalTabs() {
  }

  protected void createNewSubtrans() {
    TransMeta newSubTransMeta = createSubTransMeta();

    boolean saved = false;
    String path = null;
    if ( spoonInstance.getRepository() != null ) {
      try {
        saved = spoonInstance.saveToRepository( newSubTransMeta );
        path = getRepositoryRelativePath( newSubTransMeta.getPathAndName() );
      } catch ( KettleException e ) {
        new ErrorDialog( shell, BaseMessages.getString( PKG, "BaseStreamingDialog.File.Save.Fail.Title" ), BaseMessages.getString(
          PKG, "BaseStreamingDialog.File.Save.Fail.Message" ), e );
      }
    } else {
      saved = spoonInstance.saveXMLFile( newSubTransMeta, false );
      if ( saved ) {
        try {
          path = getRelativePath( KettleVFS.getFileObject( newSubTransMeta.getFilename() ).toString() );
        } catch ( KettleFileException e ) {
          new ErrorDialog( shell, BaseMessages.getString( PKG, "BaseStreamingDialog.File.Save.Fail.Title" ),
            BaseMessages.getString(
              PKG, "BaseStreamingDialog.File.Save.Fail.Message" ), e );
        }
      }
    }

    if ( saved && null != path ) {
      wFileSection.wFileName.setText( path );
      createSubtrans( newSubTransMeta );

      if ( props.showNewSubtransPopup() ) {
        NewSubtransDialog newSubtransDialog = new NewSubtransDialog( shell, SWT.NONE );
        props.setShowNewSubtransPopup( !newSubtransDialog.open() );
      }
    }
  }

  protected TransMeta createSubTransMeta() {
    RecordsFromStreamMeta rm = new RecordsFromStreamMeta();
    String[] fieldNames = getFieldNames();
    int[] empty = new int[ fieldNames.length ];
    Arrays.fill( empty, -1 );
    rm.setFieldname( fieldNames );
    rm.setType( getFieldTypes() );
    rm.setLength( empty );
    rm.setPrecision( empty );

    StepMeta recsFromStream = new StepMeta( "RecordsFromStream", "Get records from stream", rm );
    recsFromStream.setLocation( new Point( 100, 100 ) );
    recsFromStream.setDraw( true );

    TransMeta transMeta = new TransMeta();
    transMeta.addStep( recsFromStream );
    transMeta.setFilename( "" );

    return transMeta;
  }

  protected TableView getFieldsTable() {
    return fieldsTable;
  }

  protected String[] getFieldNames() {
    return stream( getFieldsTable().getTable().getItems() ).map( row -> row.getText( 2 ) ).toArray( String[]::new );
  }

  protected int[] getFieldTypes() {
    return stream( getFieldsTable().getTable().getItems() )
      .mapToInt( row -> ValueMetaFactory.getIdForValueMeta( row.getText( 3 ) ) ).toArray();
  }

  private void createSubtrans( TransMeta newTransMeta ) {
    TabItem tabItem =  spoonInstance.getTabSet().getSelected(); // remember current tab

    newTransMeta.setMetaStore( spoonInstance.getMetaStore() );
    try {
      SharedObjects sharedObjects = newTransMeta.readSharedObjects();
      newTransMeta.setSharedObjects( sharedObjects );
      newTransMeta.importFromMetaStore();
      newTransMeta.clearChanged();
    } catch ( Exception e ) {
      log.logError( "Failed to retrieve shared objects", e );
    }

    spoonInstance.delegates.tabs.makeTabName( newTransMeta, false );
    spoonInstance.addTransGraph( newTransMeta );
    spoonInstance.applyVariables();
    if ( spoonInstance.setDesignMode() ) {
      // No refresh done yet, do so
      spoonInstance.refreshTree();
    }
    spoonInstance.loadPerspective( MainSpoonPerspective.ID );
    try {
      ExtensionPointHandler.callExtensionPoint( log, KettleExtensionPoint.TransformationCreateNew.id, newTransMeta );
    } catch ( KettleException e ) {
      log.logError( "Failed to call extension point", e );
    }

    // go back to inital tab
    TabSet ts = spoonInstance.getTabSet();
    ts.setSelected( tabItem );
  }

  private void buildBatchTab() {
    wBatchTab = new CTabItem( wTabFolder, SWT.NONE );
    wBatchTab.setText( BaseMessages.getString( PKG, "BaseStreamingDialog.BatchTab" ) );

    wBatchComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wBatchComp );
    FormLayout batchLayout = new FormLayout();
    batchLayout.marginHeight = 15;
    batchLayout.marginWidth = 15;
    wBatchComp.setLayout( batchLayout );

    FormData fdBatchComp = new FormData();
    fdBatchComp.left = new FormAttachment( 0, 0 );
    fdBatchComp.top = new FormAttachment( 0, 0 );
    fdBatchComp.right = new FormAttachment( 100, 0 );
    fdBatchComp.bottom = new FormAttachment( 100, 0 );
    wBatchComp.setLayoutData( fdBatchComp );

    wlBatchDuration = new Label( wBatchComp, SWT.LEFT );
    props.setLook( wlBatchDuration );
    wlBatchDuration.setText( BaseMessages.getString( PKG, "BaseStreamingDialog.BatchDuration" ) );
    FormData fdlBatchDuration = new FormData();
    fdlBatchDuration.left = new FormAttachment( 0, 0 );
    fdlBatchDuration.top = new FormAttachment( 0, 0 );
    fdlBatchDuration.right = new FormAttachment( 50, 0 );
    wlBatchDuration.setLayoutData( fdlBatchDuration );

    wBatchDuration = new TextVar( transMeta, wBatchComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wBatchDuration );
    wBatchDuration.addModifyListener( lsMod );
    FormData fdBatchDuration = new FormData();
    fdBatchDuration.left = new FormAttachment( 0, 0 );
    fdBatchDuration.top = new FormAttachment( wlBatchDuration, 5 );
    fdBatchDuration.width = 75;
    wBatchDuration.setLayoutData( fdBatchDuration );

    wlBatchSize = new Label( wBatchComp, SWT.LEFT );
    props.setLook( wlBatchSize );
    wlBatchSize.setText( BaseMessages.getString( PKG, "BaseStreamingDialog.BatchSize" ) );
    FormData fdlBatchSize = new FormData();
    fdlBatchSize.left = new FormAttachment( 0, 0 );
    fdlBatchSize.top = new FormAttachment( wBatchDuration, 10 );
    fdlBatchSize.right = new FormAttachment( 50, 0 );
    wlBatchSize.setLayoutData( fdlBatchSize );

    wBatchSize = new TextVar( transMeta, wBatchComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wBatchSize );
    wBatchSize.addModifyListener( lsMod );
    FormData fdBatchSize = new FormData();
    fdBatchSize.left = new FormAttachment( 0, 0 );
    fdBatchSize.top = new FormAttachment( wlBatchSize, 5 );
    fdBatchSize.width = 75;
    wBatchSize.setLayoutData( fdBatchSize );

    Label wlParallelism = new Label( wBatchComp, SWT.LEFT );
    props.setLook( wlParallelism );
    wlParallelism.setText( BaseMessages.getString( PKG, "BaseStreamingDialog.Parallelism" ) );
    wlParallelism.setLayoutData( new FormDataBuilder().left().top( wBatchSize, 10 ).right( 50, 0 ).result() );

    wParallelism = new TextVar( transMeta, wBatchComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wParallelism );
    wParallelism.addModifyListener( lsMod );
    wParallelism.setLayoutData( new FormDataBuilder().left().top( wlParallelism, 5 ).width( 75 ).result() );

    Label wlPrefetchCount = new Label( wBatchComp, SWT.LEFT );
    props.setLook( wlPrefetchCount );
    wlPrefetchCount.setText( BaseMessages.getString( PKG, "BaseStreamingDialog.PrefetchCount" ) );
    wlPrefetchCount.setLayoutData( new FormDataBuilder().left().top( wParallelism, 10 ).right( 50, 0 ).result() );

    wPrefetchCount = new TextVar( transMeta, wBatchComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wPrefetchCount );
    wPrefetchCount.addModifyListener( lsMod );
    wPrefetchCount.setLayoutData( new FormDataBuilder().left().top( wlPrefetchCount, 5 ).width( 125 ).result() );

    wBatchComp.layout();
    wBatchTab.setControl( wBatchComp );
  }

  private void buildResultsTab() {
    wResultsTab = new CTabItem( wTabFolder, SWT.NONE );
    wResultsTab.setText( BaseMessages.getString( PKG, "BaseStreamingDialog.ResultsTab" ) );

    wResultsComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wResultsComp );
    FormLayout resultsLayout = new FormLayout();
    resultsLayout.marginHeight = 15;
    resultsLayout.marginWidth = 15;
    wResultsComp.setLayout( resultsLayout );

    FormData fdResultsComp = new FormData();
    fdResultsComp.left = new FormAttachment( 0, 0 );
    fdResultsComp.top = new FormAttachment( 0, 0 );
    fdResultsComp.right = new FormAttachment( 100, 0 );
    fdResultsComp.bottom = new FormAttachment( 100, 0 );
    wResultsComp.setLayoutData( fdResultsComp );

    wlSubStep = new Label( wResultsComp, SWT.LEFT );
    props.setLook( wlSubStep );
    FormData fdlSubTrans = new FormData();
    fdlSubTrans.left = new FormAttachment( 0, 0 );
    fdlSubTrans.top = new FormAttachment( 0, 0 );
    wlSubStep.setLayoutData( fdlSubTrans );
    wlSubStep.setText( BaseMessages.getString( PKG, "BaseStreaming.Dialog.Transformation.SubTransStep" ) );

    wSubStep = new ComboVar( transMeta, wResultsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wSubStep );
    FormData fdSubStep = new FormData();
    fdSubStep.left = new FormAttachment( 0, 0 );
    fdSubStep.top = new FormAttachment( wlSubStep, 5 );
    fdSubStep.width = 250;
    wSubStep.setLayoutData( fdSubStep );
    wSubStep.getCComboWidget().addListener( SWT.FocusIn, this::populateSubSteps );


    wResultsComp.layout();
    wResultsTab.setControl( wResultsComp );
  }

  protected void getData() {
    if ( meta.getTransformationPath() != null ) {
      wFileSection.wFileName.setText( meta.getTransformationPath() );
    }
    if ( meta.getBatchSize() != null ) {
      wBatchSize.setText( meta.getBatchSize() );
    }
    if ( meta.getBatchDuration() != null ) {
      wBatchDuration.setText( meta.getBatchDuration() );
    }
    if ( this.meta.getParallelism() != null ) {
      wParallelism.setText( meta.getParallelism() );
    }
    if ( meta.getPrefetchCount() != null ) {
      wPrefetchCount.setText( meta.getPrefetchCount() );
    }
    if ( this.meta.getSubStep() != null ) {
      wSubStep.setText( this.meta.getSubStep() );
    }
    specificationMethod = meta.getSpecificationMethod();
  }

  @SuppressWarnings( "squid:S1172" )
  protected void populateSubSteps( Event event ) {
    try {
      String current = wSubStep.getText();
      wSubStep.removeAll();

      ofNullable( getMappingMeta() )
        .ifPresent( transMeta ->
          transMeta
            .getSteps()
            .stream()
            .map( StepMeta::getName )
            .sorted()
            .forEach( wSubStep::add ) );

      //I don't know why but just calling setText does not work when the text is not one of the items in the list.
      //Instead the first item in the list is selected.  asyncExec solves it.  If you have a better solution, by all
      //means go ahead and implement
      Display.getDefault().asyncExec( () -> wSubStep.setText( current ) );
    } catch ( KettleException e ) {
      logDebug( e.getMessage(), e );
    }
  }

  private TransMeta getMappingMeta() throws KettleException {
    BaseStreamStepMeta baseMeta = (BaseStreamStepMeta) meta.clone();
    updateMeta( baseMeta );
    return  loadMappingMeta( baseMeta, getRepository(), getMetaStore(), transMeta );
  }

  private Image getImage() {
    PluginInterface plugin =
      PluginRegistry.getInstance().getPlugin( StepPluginType.class, stepMeta.getStepMetaInterface() );
    String id = plugin.getIds()[ 0 ];
    if ( id != null ) {
      return GUIResource.getInstance().getImagesSteps().get( id ).getAsBitmapForSize( shell.getDisplay(),
        ConstUI.LARGE_ICON_SIZE, ConstUI.LARGE_ICON_SIZE );
    }
    return null;
  }

  private void cancel() {
    meta.setChanged( false );
    dispose();
  }

  private void ok() {
    stepname = wStepname.getText();
    updateMeta( meta );
    dispose();
  }

  /**
   * populates streamMeta based on current values of form
   */
  private void updateMeta( BaseStreamStepMeta streamMeta ) {
    streamMeta.setTransformationPath( wFileSection.wFileName.getText() );
    streamMeta.setBatchSize( wBatchSize.getText() );
    streamMeta.setBatchDuration( wBatchDuration.getText() );
    streamMeta.setParallelism( wParallelism.getText() );
    streamMeta.setPrefetchCount( wPrefetchCount.getText() );
    streamMeta.setSpecificationMethod( specificationMethod );
    streamMeta.setSubStep( wSubStep.getText() );
    switch ( specificationMethod ) {
      case FILENAME:
        streamMeta.setFileName( wFileSection.wFileName.getText() );
        streamMeta.setDirectoryPath( null );
        streamMeta.setTransName( null );
        streamMeta.setTransObjectId( null );
        break;
      case REPOSITORY_BY_NAME:
        String transPath = wFileSection.wFileName.getText();
        String transName = transPath;
        String directory = "";
        int index = transPath.lastIndexOf( '/' );
        if ( index != -1 ) {
          transName = transPath.substring( index + 1 );
          directory = transPath.substring( 0, index );
        }
        streamMeta.setDirectoryPath( directory );
        streamMeta.setTransName( transName );
        streamMeta.setFileName( null );
        streamMeta.setTransObjectId( null );
        break;
      default:
        break;
    }
    additionalOks( streamMeta );
  }

  protected void additionalOks( BaseStreamStepMeta meta ) {

  }

  protected String getRepositoryRelativePath( String path ) {
    String parentPath = this.transMeta.getRepositoryDirectory().getPath();
    if ( path.startsWith( parentPath ) ) {
      path = path.replace( parentPath, "${" + Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY + "}" );
    }
    return path;
  }

  protected String getRelativePath( String filePath ) {
    String parentFolder = null;
    try {
      parentFolder =
        KettleVFS.getFileObject( transMeta.environmentSubstitute( transMeta.getFilename() ) ).getParent().toString();
    } catch ( Exception e ) {
      // Take no action
    }

    if ( filePath != null && parentFolder != null && filePath.startsWith( parentFolder ) ) {
      filePath = filePath.replace( parentFolder, "${" + Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY + "}" );
    }

    return filePath;
  }
}

