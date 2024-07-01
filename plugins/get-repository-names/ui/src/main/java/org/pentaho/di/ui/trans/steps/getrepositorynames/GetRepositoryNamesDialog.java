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

package org.pentaho.di.ui.trans.steps.getrepositorynames;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
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
import org.pentaho.di.core.annotations.PluginDialog;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.getrepositorynames.GetRepositoryNamesMeta;
import org.pentaho.di.trans.steps.getrepositorynames.ObjectTypeSelection;
import org.pentaho.di.ui.core.dialog.EnterNumberDialog;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.repository.dialog.SelectDirectoryDialog;
import org.pentaho.di.ui.trans.dialog.TransPreviewProgressDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.util.HelpUtils;

@PluginDialog(id = "GetRepositoryNames",image = "GRN.svg", pluginType = PluginDialog.PluginType.STEP,
        documentationUrl = "http://wiki.pentaho.com/display/EAI/Get+repository+names")
public class GetRepositoryNamesDialog extends BaseStepDialog implements StepDialogInterface {
  private static Class<?> PKG = GetRepositoryNamesMeta.class; // i18n

  private static final String[] YES_NO_COMBO = new String[] {
    BaseMessages.getString( PKG, "System.Combo.No" ), BaseMessages.getString( PKG, "System.Combo.Yes" ), };

  private Composite comp;
  private ScrolledComposite sComp;

  private Label wlDirectory;
  private Button wbbDirectory; // browse
  private Button wbdDirectory; // Delete
  private Button wbeDirectory; // Edit
  private Button wbaDirectory; // Add

  private TextVar wDirectory;

  private Label wlDirectoryList;
  private TableView wDirectoryList;

  private Label wlExcludeNameMask;
  private TextVar wExcludeFilemask;

  private Label wlNameMask;
  private TextVar wNameMask;

  private Label wlObjectTypeSelection;
  private CCombo wObjectTypeSelection;

  private GetRepositoryNamesMeta input;

  private int middle, margin;

  private ModifyListener lsMod;

  private Label wlInclRownum;
  private Button wInclRownum;

  private Label wlInclRownumField;
  private TextVar wInclRownumField;

  private Composite helpComp;

  public GetRepositoryNamesDialog( Shell parent, Object in, TransMeta transMeta, String sname ) {
    super( parent, (BaseStepMeta) in, transMeta, sname );
    input = (GetRepositoryNamesMeta) in;
  }

  public String open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );
    props.setLook( shell );

    lsMod = new ModifyListener() {
      public void modifyText( ModifyEvent e ) {
        input.setChanged();
      }
    };
    changed = input.hasChanged();

    GridLayout shellLayout = new GridLayout();
    shellLayout.numColumns = 1;
    shell.setLayout( shellLayout );
    shell.setText( BaseMessages.getString( PKG, "GetRepositoryNamesDialog.DialogTitle" ) );

    middle = props.getMiddlePct();
    margin = Const.MARGIN;

    Composite sCompParent = new Composite( shell, SWT.NONE );
    sCompParent.setLayout( new FillLayout( SWT.VERTICAL ) );
    GridData sCompGridData = new GridData( GridData.FILL_BOTH );
    sCompGridData.grabExcessHorizontalSpace = true;
    sCompGridData.grabExcessVerticalSpace = true;
    sCompParent.setLayoutData( sCompGridData );

    sComp = new ScrolledComposite( sCompParent, SWT.V_SCROLL | SWT.H_SCROLL );
    sComp.setLayout( new FormLayout() );
    sComp.setExpandHorizontal( true );
    sComp.setExpandVertical( true );

    helpComp = new Composite( shell, SWT.NONE );
    helpComp.setLayout( new FormLayout() );
    GridData helpCompData = new GridData();
    helpCompData.grabExcessHorizontalSpace = true;
    helpCompData.grabExcessVerticalSpace = false;
    helpComp.setLayoutData( helpCompData );
    setShellImage( shell, input );

    comp = new Composite( sComp, SWT.NONE );
    props.setLook( comp );

    FormLayout fileLayout = new FormLayout();
    fileLayout.marginWidth = 3;
    fileLayout.marginHeight = 3;
    comp.setLayout( fileLayout );

    // Stepname line
    wlStepname = new Label( comp, SWT.RIGHT );
    wlStepname.setText( BaseMessages.getString( PKG, "System.Label.StepName" ) );
    props.setLook( wlStepname );
    fdlStepname = new FormData();
    fdlStepname.left = new FormAttachment( 0, 0 );
    fdlStepname.top = new FormAttachment( 0, margin );
    fdlStepname.right = new FormAttachment( middle, -margin );
    wlStepname.setLayoutData( fdlStepname );
    wStepname = new Text( comp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wStepname.setText( stepname );
    props.setLook( wStepname );
    wStepname.addModifyListener( lsMod );
    fdStepname = new FormData();
    fdStepname.left = new FormAttachment( middle, 0 );
    fdStepname.top = new FormAttachment( 0, margin );
    fdStepname.right = new FormAttachment( 100, 0 );
    wStepname.setLayoutData( fdStepname );

    // Filename line
    wlDirectory = new Label( comp, SWT.RIGHT );
    wlDirectory.setText( BaseMessages.getString( PKG, "GetRepositoryNamesDialog.Directory.Label" ) );
    props.setLook( wlDirectory );
    FormData fdlDirectory = new FormData();
    fdlDirectory.left = new FormAttachment( 0, 0 );
    fdlDirectory.top = new FormAttachment( wStepname, margin );
    fdlDirectory.right = new FormAttachment( middle, -margin );
    wlDirectory.setLayoutData( fdlDirectory );

    wbbDirectory = new Button( comp, SWT.PUSH | SWT.CENTER );
    props.setLook( wbbDirectory );
    wbbDirectory.setText( BaseMessages.getString( PKG, "System.Button.Browse" ) );
    wbbDirectory.setToolTipText( BaseMessages.getString( PKG, "System.Tooltip.BrowseForRepositoryDirectory" ) );
    FormData fdbDirectory = new FormData();
    fdbDirectory.right = new FormAttachment( 100, 0 );
    fdbDirectory.top = new FormAttachment( wStepname, margin );
    wbbDirectory.setLayoutData( fdbDirectory );

    wbaDirectory = new Button( comp, SWT.PUSH | SWT.CENTER );
    props.setLook( wbaDirectory );
    wbaDirectory.setText( BaseMessages.getString( PKG, "GetRepositoryNamesDialog.DirectoryAdd.Button" ) );
    wbaDirectory.setToolTipText( BaseMessages.getString( PKG, "GetRepositoryNamesDialog.DirectoryAdd.Tooltip" ) );
    FormData fdbaDirectory = new FormData();
    fdbaDirectory.right = new FormAttachment( wbbDirectory, -margin );
    fdbaDirectory.top = new FormAttachment( wStepname, margin );
    wbaDirectory.setLayoutData( fdbaDirectory );

    wDirectory = new TextVar( transMeta, comp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wDirectory );
    wDirectory.addModifyListener( lsMod );
    FormData fdDirectory = new FormData();
    fdDirectory.left = new FormAttachment( middle, 0 );
    fdDirectory.right = new FormAttachment( wbaDirectory, -margin );
    fdDirectory.top = new FormAttachment( wStepname, margin );
    wDirectory.setLayoutData( fdDirectory );

    wlNameMask = new Label( comp, SWT.RIGHT );
    wlNameMask.setText( BaseMessages.getString( PKG, "GetRepositoryNamesDialog.NameMask.Label" ) );
    props.setLook( wlNameMask );
    FormData fdlNameMask = new FormData();
    fdlNameMask.left = new FormAttachment( 0, 0 );
    fdlNameMask.top = new FormAttachment( wDirectory, margin );
    fdlNameMask.right = new FormAttachment( middle, -margin );
    wlNameMask.setLayoutData( fdlNameMask );
    wNameMask = new TextVar( transMeta, comp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wNameMask );
    wNameMask.addModifyListener( lsMod );
    FormData fdNameMask = new FormData();
    fdNameMask.left = new FormAttachment( middle, 0 );
    fdNameMask.top = new FormAttachment( wDirectory, margin );
    fdNameMask.right = new FormAttachment( wDirectory, 0, SWT.RIGHT );
    wNameMask.setLayoutData( fdNameMask );

    wlExcludeNameMask = new Label( comp, SWT.RIGHT );
    wlExcludeNameMask.setText( BaseMessages.getString( PKG, "GetRepositoryNamesDialog.ExcludeNameMask.Label" ) );
    props.setLook( wlExcludeNameMask );
    FormData fdlExcludeNameMask = new FormData();
    fdlExcludeNameMask.left = new FormAttachment( 0, 0 );
    fdlExcludeNameMask.top = new FormAttachment( wNameMask, margin );
    fdlExcludeNameMask.right = new FormAttachment( middle, -margin );
    wlExcludeNameMask.setLayoutData( fdlExcludeNameMask );
    wExcludeFilemask = new TextVar( transMeta, comp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wExcludeFilemask );
    wExcludeFilemask.addModifyListener( lsMod );
    FormData fdExcludeNameMask = new FormData();
    fdExcludeNameMask.left = new FormAttachment( middle, 0 );
    fdExcludeNameMask.top = new FormAttachment( wNameMask, margin );
    fdExcludeNameMask.right = new FormAttachment( wDirectory, 0, SWT.RIGHT );
    wExcludeFilemask.setLayoutData( fdExcludeNameMask );

    // Directory list line
    wlDirectoryList = new Label( comp, SWT.RIGHT );
    wlDirectoryList.setText( BaseMessages.getString( PKG, "GetRepositoryNamesDialog.DirectoryList.Label" ) );
    props.setLook( wlDirectoryList );
    FormData fdlDirectoryList = new FormData();
    fdlDirectoryList.left = new FormAttachment( 0, 0 );
    fdlDirectoryList.top = new FormAttachment( wExcludeFilemask, margin );
    fdlDirectoryList.right = new FormAttachment( middle, -margin );
    wlDirectoryList.setLayoutData( fdlDirectoryList );

    // Buttons to the right of the screen...
    wbdDirectory = new Button( comp, SWT.PUSH | SWT.CENTER );
    props.setLook( wbdDirectory );
    wbdDirectory.setText( BaseMessages.getString( PKG, "GetRepositoryNamesDialog.DirectoryDelete.Button" ) );
    wbdDirectory
      .setToolTipText( BaseMessages.getString( PKG, "GetRepositoryNamesDialog.DirectoryDelete.Tooltip" ) );
    FormData fdbdDirectory = new FormData();
    fdbdDirectory.right = new FormAttachment( 100, 0 );
    fdbdDirectory.top = new FormAttachment( wExcludeFilemask, 40 );
    wbdDirectory.setLayoutData( fdbdDirectory );

    wbeDirectory = new Button( comp, SWT.PUSH | SWT.CENTER );
    props.setLook( wbeDirectory );
    wbeDirectory.setText( BaseMessages.getString( PKG, "GetRepositoryNamesDialog.DirectoryEdit.Button" ) );
    wbeDirectory.setToolTipText( BaseMessages.getString( PKG, "GetRepositoryNamesDialog.DirectoryEdit.Tooltip" ) );
    FormData fdbeDirectory = new FormData();
    fdbeDirectory.right = new FormAttachment( 100, 0 );
    fdbeDirectory.left = new FormAttachment( wbdDirectory, 0, SWT.LEFT );
    fdbeDirectory.top = new FormAttachment( wbdDirectory, margin );
    wbeDirectory.setLayoutData( fdbeDirectory );

    ColumnInfo[] colinfo =
      new ColumnInfo[] {
        new ColumnInfo(
          BaseMessages.getString( PKG, "GetRepositoryNamesDialog.Directory.Column" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "GetRepositoryNamesDialog.NameWildcard.Column" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "GetRepositoryNamesDialog.ExcludeNameWildcard.Column" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "GetRepositoryNamesDialog.IncludeSubDirs.Column" ),
          ColumnInfo.COLUMN_TYPE_CCOMBO, YES_NO_COMBO ) };

    colinfo[0].setUsingVariables( true );
    colinfo[1].setUsingVariables( true );
    colinfo[1].setToolTip( BaseMessages.getString( PKG, "GetRepositoryNamesDialog.RegExpColumn.Column" ) );
    colinfo[2].setUsingVariables( true );
    colinfo[2].setToolTip( BaseMessages.getString( PKG, "GetRepositoryNamesDialog.ExcludeRegExpColumn.Column" ) );
    colinfo[3].setToolTip( BaseMessages.getString( PKG, "GetRepositoryNamesDialog.IncludeSubDirs.ToolTip" ) );

    wDirectoryList =
      new TableView(
        transMeta, comp, SWT.FULL_SELECTION | SWT.SINGLE | SWT.BORDER, colinfo, colinfo.length, lsMod, props );
    props.setLook( wDirectoryList );
    FormData fdDirectoryList = new FormData();
    fdDirectoryList.left = new FormAttachment( middle, 0 );
    fdDirectoryList.right = new FormAttachment( wbdDirectory, -margin );
    fdDirectoryList.top = new FormAttachment( wExcludeFilemask, margin );
    fdDirectoryList.bottom = new FormAttachment( wExcludeFilemask, 200 );
    wDirectoryList.setLayoutData( fdDirectoryList );

    // Filter File Type
    wlObjectTypeSelection = new Label( comp, SWT.RIGHT );
    wlObjectTypeSelection.setText( BaseMessages.getString(
      PKG, "GetRepositoryNamesDialog.ObjectTypeSelection.Label" ) );
    props.setLook( wlObjectTypeSelection );
    FormData fdlFilterFileType = new FormData();
    fdlFilterFileType.left = new FormAttachment( 0, 0 );
    fdlFilterFileType.right = new FormAttachment( middle, 0 );
    fdlFilterFileType.top = new FormAttachment( wDirectoryList, 3 * margin );
    wlObjectTypeSelection.setLayoutData( fdlFilterFileType );
    wObjectTypeSelection = new CCombo( comp, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER );
    wObjectTypeSelection.add( ObjectTypeSelection.Jobs.getDescription() );
    wObjectTypeSelection.add( ObjectTypeSelection.Transformations.getDescription() );
    wObjectTypeSelection.add( ObjectTypeSelection.All.getDescription() );
    props.setLook( wObjectTypeSelection );
    FormData fdFilterFileType = new FormData();
    fdFilterFileType.left = new FormAttachment( middle, 0 );
    fdFilterFileType.top = new FormAttachment( wDirectoryList, 3 * margin );
    fdFilterFileType.right = new FormAttachment( 100, 0 );
    wObjectTypeSelection.setLayoutData( fdFilterFileType );

    wlInclRownum = new Label( comp, SWT.RIGHT );
    wlInclRownum.setText( BaseMessages.getString( PKG, "GetRepositoryNamesDialog.InclRownum.Label" ) );
    props.setLook( wlInclRownum );
    FormData fdlInclRownum = new FormData();
    fdlInclRownum.left = new FormAttachment( 0, 0 );
    fdlInclRownum.top = new FormAttachment( wObjectTypeSelection, 2 * margin );
    fdlInclRownum.right = new FormAttachment( middle, -margin );
    wlInclRownum.setLayoutData( fdlInclRownum );
    wInclRownum = new Button( comp, SWT.CHECK );
    props.setLook( wInclRownum );
    wInclRownum.setToolTipText( BaseMessages.getString( PKG, "GetRepositoryNamesDialog.InclRownum.Tooltip" ) );
    FormData fdRownum = new FormData();
    fdRownum.left = new FormAttachment( middle, 0 );
    fdRownum.top = new FormAttachment( wObjectTypeSelection, 2 * margin );
    wInclRownum.setLayoutData( fdRownum );

    wlInclRownumField = new Label( comp, SWT.RIGHT );
    wlInclRownumField.setText( BaseMessages.getString( PKG, "GetRepositoryNamesDialog.InclRownumField.Label" ) );
    props.setLook( wlInclRownumField );
    FormData fdlInclRownumField = new FormData();
    fdlInclRownumField.left = new FormAttachment( wInclRownum, margin );
    fdlInclRownumField.top = new FormAttachment( wObjectTypeSelection, 2 * margin );
    wlInclRownumField.setLayoutData( fdlInclRownumField );
    wInclRownumField = new TextVar( transMeta, comp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wInclRownumField );
    wInclRownumField.addModifyListener( lsMod );
    FormData fdInclRownumField = new FormData();
    fdInclRownumField.left = new FormAttachment( wlInclRownumField, margin );
    fdInclRownumField.top = new FormAttachment( wObjectTypeSelection, 2 * margin );
    fdInclRownumField.right = new FormAttachment( 100, 0 );
    wInclRownumField.setLayoutData( fdInclRownumField );

    // ///////////////////////////////////////////////////////////
    // / END OF FILE Filter TAB
    // ///////////////////////////////////////////////////////////

    wOK = new Button( comp, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );

    wPreview = new Button( comp, SWT.PUSH );
    wPreview.setText( BaseMessages.getString( PKG, "GetRepositoryNamesDialog.Preview.Button" ) );

    wCancel = new Button( comp, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );

    setButtonPositions( new Button[] { wOK, wPreview, wCancel }, margin, wInclRownumField );

    FormData fdComp = new FormData();
    fdComp.left = new FormAttachment( 0, 0 );
    fdComp.top = new FormAttachment( 0, 0 );
    fdComp.right = new FormAttachment( 100, 0 );
    fdComp.bottom = new FormAttachment( 100, 0 );
    comp.setLayoutData( fdComp );

    comp.pack();
    Rectangle bounds = comp.getBounds();

    sComp.setContent( comp );
    sComp.setExpandHorizontal( true );
    sComp.setExpandVertical( true );
    sComp.setMinWidth( bounds.width );
    sComp.setMinHeight( bounds.height );

    // Add listeners
    lsOK = new Listener() {
      public void handleEvent( Event e ) {
        ok();
      }
    };
    lsPreview = new Listener() {
      public void handleEvent( Event e ) {
        preview();
      }
    };
    lsCancel = new Listener() {
      public void handleEvent( Event e ) {
        cancel();
      }
    };

    wOK.addListener( SWT.Selection, lsOK );
    wPreview.addListener( SWT.Selection, lsPreview );
    wCancel.addListener( SWT.Selection, lsCancel );

    lsDef = new SelectionAdapter() {
      public void widgetDefaultSelected( SelectionEvent e ) {
        ok();
      }
    };

    wStepname.addSelectionListener( lsDef );

    // Add the file to the list of files...
    SelectionAdapter selA = new SelectionAdapter() {
      public void widgetSelected( SelectionEvent arg0 ) {
        wDirectoryList.add( new String[] {
          wDirectory.getText(), wNameMask.getText(), wExcludeFilemask.getText(), "Y", } );
        wDirectory.setText( "" );
        wNameMask.setText( "" );
        wDirectoryList.removeEmptyRows();
        wDirectoryList.setRowNums();
        wDirectoryList.optWidth( true );
      }
    };
    wbaDirectory.addSelectionListener( selA );
    wDirectory.addSelectionListener( selA );

    // Delete files from the list of files...
    wbdDirectory.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent arg0 ) {
        int[] idx = wDirectoryList.getSelectionIndices();
        wDirectoryList.remove( idx );
        wDirectoryList.removeEmptyRows();
        wDirectoryList.setRowNums();
      }
    } );

    // Edit the selected file & remove from the list...
    wbeDirectory.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent arg0 ) {
        int idx = wDirectoryList.getSelectionIndex();
        if ( idx >= 0 ) {
          String[] string = wDirectoryList.getItem( idx );
          wDirectory.setText( string[0] );
          wNameMask.setText( string[1] );
          wExcludeFilemask.setText( string[2] );
          wDirectoryList.remove( idx );
        }
        wDirectoryList.removeEmptyRows();
        wDirectoryList.setRowNums();
      }
    } );

    // Listen to the Browse... button
    wbbDirectory.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        displaydirectoryList();
      }
    } );

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    // Set the shell size, based upon previous time...
    getData( input );
    setSize();

    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return stepname;
  }

  @Override
  protected Button createHelpButton( Shell shell, StepMeta stepMeta, PluginInterface plugin ) {
    return HelpUtils.createHelpButton( helpComp, HelpUtils.getHelpDialogTitle( plugin ), plugin );
  }

  /**
   * Read the data from the TextFileInputMeta object and show it in this dialog.
   *
   * @param meta
   *          The TextFileInputMeta object to obtain the data from.
   */
  public void getData( GetRepositoryNamesMeta meta ) {
    wStepname.setText( stepname );

    for ( int i = 0; i < meta.getDirectory().length; i++ ) {
      TableItem item = new TableItem( wDirectoryList.table, SWT.NONE );
      int col = 1;
      item.setText( col++, Const.NVL( meta.getDirectory()[i], "" ) );
      item.setText( col++, Const.NVL( meta.getNameMask()[i], "" ) );
      item.setText( col++, Const.NVL( meta.getExcludeNameMask()[i], "" ) );
      item.setText( col++, meta.getIncludeSubFolders()[i] ? YES_NO_COMBO[1] : YES_NO_COMBO[0] );
    }
    wDirectoryList.removeEmptyRows();
    wDirectoryList.setRowNums();
    wDirectoryList.optWidth( true );

    wObjectTypeSelection.setText( meta.getObjectTypeSelection().getDescription() );

    wInclRownum.setSelection( meta.isIncludeRowNumber() );
    wInclRownumField.setText( Const.NVL( meta.getRowNumberField(), "" ) );

    wStepname.selectAll();
    wStepname.setFocus();
  }

  private void cancel() {
    stepname = null;
    input.setChanged( changed );
    dispose();
  }

  private void ok() {
    if ( Utils.isEmpty( wStepname.getText() ) ) {
      return;
    }

    getInfo( input );
    dispose();
  }

  private void getInfo( GetRepositoryNamesMeta meta ) {
    stepname = wStepname.getText(); // return value

    int nrfiles = wDirectoryList.nrNonEmpty();
    meta.allocate( nrfiles );
    //CHECKSTYLE:Indentation:OFF
    for ( int i = 0; i < nrfiles; i++ ) {
      TableItem item = wDirectoryList.getNonEmpty( i );
      int col = 1;
      meta.getDirectory()[i] = item.getText( col++ );
      meta.getNameMask()[i] = item.getText( col++ );
      meta.getExcludeNameMask()[i] = item.getText( col++ );
      meta.getIncludeSubFolders()[i] = YES_NO_COMBO[1].equals( item.getText( col++ ) );
    }

    meta.setObjectTypeSelection( ObjectTypeSelection.getObjectTypeSelectionByDescription( wObjectTypeSelection
      .getText() ) );
    meta.setIncludeRowNumber( wInclRownum.getSelection() );
    meta.setRowNumberField( wInclRownumField.getText() );
  }

  // Preview the data
  private void preview() {
    // Create the XML input step
    GetRepositoryNamesMeta oneMeta = new GetRepositoryNamesMeta();
    getInfo( oneMeta );

    TransMeta previewMeta =
      TransPreviewFactory.generatePreviewTransformation( transMeta, oneMeta, wStepname.getText() );
    previewMeta.setRepository( repository );

    EnterNumberDialog numberDialog =
      new EnterNumberDialog( shell, props.getDefaultPreviewSize(), BaseMessages.getString(
        PKG, "GetRepositoryNamesDialog.PreviewSize.DialogTitle" ), BaseMessages.getString(
        PKG, "GetRepositoryNamesDialog.PreviewSize.DialogMessage" ) );
    int previewSize = numberDialog.open();
    if ( previewSize > 0 ) {
      TransPreviewProgressDialog progressDialog =
        new TransPreviewProgressDialog(
          shell, previewMeta, new String[] { wStepname.getText() }, new int[] { previewSize } );
      progressDialog.open();

      if ( !progressDialog.isCancelled() ) {
        Trans trans = progressDialog.getTrans();
        String loggingText = progressDialog.getLoggingText();

        if ( trans.getResult() != null && trans.getResult().getNrErrors() > 0 ) {
          EnterTextDialog etd =
            new EnterTextDialog( shell, BaseMessages.getString( PKG, "System.Dialog.Error.Title" ), BaseMessages
              .getString( PKG, "GetRepositoryNamesDialog.ErrorInPreview.DialogMessage" ), loggingText, true );
          etd.setReadOnly();
          etd.open();
        }

        PreviewRowsDialog prd =
          new PreviewRowsDialog(
            shell, transMeta, SWT.NONE, wStepname.getText(), progressDialog.getPreviewRowsMeta( wStepname
              .getText() ), progressDialog.getPreviewRows( wStepname.getText() ), loggingText );
        prd.open();
      }
    }
  }

  private void displaydirectoryList() {

    try {

      if ( repository == null ) {
        throw new KettleException( BaseMessages.getString(
          PKG, "GetRepositoryNames.Exception.NotConnectedToRepository" ) );
      }

      SelectDirectoryDialog sdd = new SelectDirectoryDialog( shell, SWT.NONE, repository );
      RepositoryDirectoryInterface rd = sdd.open();
      if ( rd != null ) {
        wDirectory.setText( rd.getPath() );
      }

    } catch ( Exception e ) {
      new ErrorDialog( shell, BaseMessages.getString( PKG, "System.Dialog.Error.Title" ), BaseMessages.getString(
        PKG, "GetRepositoryNames.ErrorGettingFolderds.DialogMessage" ), e );
    }
  }
}
