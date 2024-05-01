//CHECKSTYLE:FileLength:OFF
/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.trans.steps.fileinput.text;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Collectors;

import org.apache.commons.vfs2.FileObject;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Cursor;
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
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.compress.CompressionInputStream;
import org.pentaho.di.core.compress.CompressionProvider;
import org.pentaho.di.core.compress.CompressionProviderFactory;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.gui.TextFileInputFieldInterface;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.file.BaseFileField;
import org.pentaho.di.trans.steps.fileinput.text.BufferedInputStreamReader;
import org.pentaho.di.trans.steps.fileinput.text.EncodingType;
import org.pentaho.di.trans.steps.fileinput.text.TextFileFilter;
import org.pentaho.di.trans.steps.fileinput.text.TextFileInputMeta;
import org.pentaho.di.trans.steps.fileinput.text.TextFileInputUtils;
import org.pentaho.di.ui.core.dialog.EnterNumberDialog;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.di.ui.core.events.dialog.FilterType;
import org.pentaho.di.ui.core.events.dialog.SelectionAdapterFileDialogTextVar;
import org.pentaho.di.ui.core.events.dialog.SelectionAdapterOptions;
import org.pentaho.di.ui.core.events.dialog.SelectionOperation;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.dialog.TransPreviewProgressDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.trans.steps.common.CsvInputAwareMeta;
import org.pentaho.di.ui.trans.step.common.CsvInputAwareImportProgressDialog;
import org.pentaho.di.ui.trans.step.common.CsvInputAwareStepDialog;
import org.pentaho.di.ui.trans.step.common.GetFieldsCapableStepDialog;

public class TextFileInputDialog extends BaseStepDialog implements StepDialogInterface,
  GetFieldsCapableStepDialog<TextFileInputMeta>, CsvInputAwareStepDialog {
  private static Class<?> PKG = TextFileInputMeta.class; // for i18n purposes, needed by Translator2!!

  private static final String[] YES_NO_COMBO =
      new String[] { BaseMessages.getString( PKG, "System.Combo.No" ), BaseMessages.getString( PKG,
          "System.Combo.Yes" ) };

  private CTabFolder wTabFolder;
  private FormData fdTabFolder;

  private CTabItem wFileTab;
  private CTabItem wContentTab;
  private CTabItem wErrorTab;
  private CTabItem wFilterTab;
  private CTabItem wFieldsTab;

  private ScrolledComposite wFileSComp;
  private ScrolledComposite wContentSComp;
  private ScrolledComposite wErrorSComp;

  private Composite wFileComp;
  private Composite wContentComp;
  private Composite wErrorComp;
  private Composite wFilterComp;
  private Composite wFieldsComp;

  private FormData fdFileComp;
  private FormData fdContentComp;
  private FormData fdErrorComp;
  private FormData fdFilterComp;
  private FormData fdFieldsComp;

  private Group gAccepting;
  private FormData fdAccepting;

  private Label wlExcludeFilemask;
  private TextVar wExcludeFilemask;
  private FormData fdlExcludeFilemask, fdExcludeFilemask;

  private Label wlAccFilenames;
  private Button wAccFilenames;
  private FormData fdlAccFilenames, fdAccFilenames;

  private Label wlPassThruFields;
  private Button wPassThruFields;
  private FormData fdlPassThruFields, fdPassThruFields;

  private Label wlAccField;
  private Text wAccField;
  private FormData fdlAccField, fdAccField;

  private Label wlAccStep;
  private CCombo wAccStep;
  private FormData fdlAccStep, fdAccStep;

  private Label wlFilename;
  private Button wbbFilename; // Browse: add file or directory
  private Button wbdFilename; // Delete
  private Button wbeFilename; // Edit
  private Button wbaFilename; // Add or change
  private TextVar wFilename;
  private FormData fdlFilename, fdbFilename, fdbdFilename, fdbeFilename, fdbaFilename, fdFilename;

  private Label wlFilenameList;
  private TableView wFilenameList;
  private FormData fdlFilenameList, fdFilenameList;

  private Label wlFilemask;
  // PDI-8664
  private TextVar wFilemask;
  private FormData fdlFilemask, fdFilemask;

  private Button wbShowFiles;
  private FormData fdbShowFiles;

  private Button wFirst;
  private FormData fdFirst;
  private Listener lsFirst;

  private Button wFirstHeader;
  private FormData fdFirstHeader;
  private Listener lsFirstHeader;

  private Label wlFiletype;
  private CCombo wFiletype;
  private FormData fdlFiletype, fdFiletype;

  private Label wlSeparator;
  private Button wbSeparator;
  private TextVar wSeparator;
  private FormData fdlSeparator, fdbSeparator, fdSeparator;

  private Label wlEnclosure;
  private Text wEnclosure;
  private FormData fdlEnclosure, fdEnclosure;

  private Label wlEscape;
  private Text wEscape;
  private FormData fdlEscape, fdEscape;

  private Label wlHeader;
  private Button wHeader;
  private FormData fdlHeader, fdHeader;

  private Label wlNrHeader;
  private Text wNrHeader;
  private FormData fdlNrHeader, fdNrHeader;

  private Label wlFooter;
  private Button wFooter;
  private FormData fdlFooter, fdFooter;

  private Label wlNrFooter;
  private Text wNrFooter;
  private FormData fdlNrFooter, fdNrFooter;

  private Label wlWraps;
  private Button wWraps;
  private FormData fdlWraps, fdWraps;

  private Label wlNrWraps;
  private Text wNrWraps;
  private FormData fdlNrWraps, fdNrWraps;

  private Label wlLayoutPaged;
  private Button wLayoutPaged;
  private FormData fdlLayoutPaged, fdLayoutPaged;

  private Label wlNrLinesPerPage;
  private Text wNrLinesPerPage;
  private FormData fdlNrLinesPerPage, fdNrLinesPerPage;

  private Label wlNrLinesDocHeader;
  private Text wNrLinesDocHeader;
  private FormData fdlNrLinesDocHeader, fdNrLinesDocHeader;

  private Label wlCompression;
  private CCombo wCompression;
  private FormData fdlCompression, fdCompression;

  private Label wlNoempty;
  private Button wNoempty;
  private FormData fdlNoempty, fdNoempty;

  private Label wlInclFilename;
  private Button wInclFilename;
  private FormData fdlInclFilename, fdInclFilename;

  private Label wlInclFilenameField;
  private Text wInclFilenameField;
  private FormData fdlInclFilenameField, fdInclFilenameField;

  private Label wlInclRownum;
  private Button wInclRownum;
  private FormData fdlInclRownum, fdRownum;

  private Label wlRownumByFileField;
  private Button wRownumByFile;
  private FormData fdlRownumByFile, fdRownumByFile;

  private Label wlInclRownumField;
  private Text wInclRownumField;
  private FormData fdlInclRownumField, fdInclRownumField;

  private Label wlFormat;
  private CCombo wFormat;
  private FormData fdlFormat, fdFormat;

  private Label wlEncoding;
  private CCombo wEncoding;
  private FormData fdlEncoding, fdEncoding;

  private Label wlLength;
  private CCombo wLength;
  private FormData fdlLength, fdLength;

  private Label wlLimit;
  private Text wLimit;
  private FormData fdlLimit, fdLimit;

  private Label wlDateLenient;
  private Button wDateLenient;
  private FormData fdlDateLenient, fdDateLenient;

  private Label wlDateLocale;
  private CCombo wDateLocale;
  private FormData fdlDateLocale, fdDateLocale;

  // ERROR HANDLING...
  private Label wlErrorIgnored;
  private Button wErrorIgnored;
  private FormData fdlErrorIgnored, fdErrorIgnored;

  private Label wlSkipBadFiles;
  private Button wSkipBadFiles;
  private FormData fdlSkipBadFiles, fdSkipBadFiles;

  private Label wlSkipErrorLines;
  private Button wSkipErrorLines;
  private FormData fdlSkipErrorLines, fdSkipErrorLines;

  private Label wlErrorCount;
  private Text wErrorCount;
  private FormData fdlErrorCount, fdErrorCount;

  private Label wlErrorFields;
  private Text wErrorFields;
  private FormData fdlErrorFields, fdErrorFields;

  private Label wlErrorText;
  private Text wErrorText;
  private FormData fdlErrorText, fdErrorText;

  // New entries for intelligent error handling AKA replay functionality
  // Bad files destination directory
  private Label wlWarnDestDir;
  private Button wbbWarnDestDir; // Browse: add file or directory
  private TextVar wWarnDestDir;
  private FormData fdlWarnDestDir, fdbBadDestDir, fdBadDestDir;
  private Label wlWarnExt;
  private Text wWarnExt;
  private FormData fdlWarnDestExt, fdWarnDestExt;

  // Error messages files destination directory
  private Label wlErrorDestDir;
  private Button wbbErrorDestDir; // Browse: add file or directory
  private TextVar wErrorDestDir;
  private FormData fdlErrorDestDir, fdbErrorDestDir, fdErrorDestDir;
  private Label wlErrorExt;
  private Text wErrorExt;
  private FormData fdlErrorDestExt, fdErrorDestExt;

  // Line numbers files destination directory
  private Label wlLineNrDestDir;
  private Button wbbLineNrDestDir; // Browse: add file or directory
  private TextVar wLineNrDestDir;
  private FormData fdlLineNrDestDir, fdbLineNrDestDir, fdLineNrDestDir;
  private Label wlLineNrExt;
  private Text wLineNrExt;
  private FormData fdlLineNrDestExt, fdLineNrDestExt;

  private TableView wFilter;
  private FormData fdFilter;

  private TableView wFields;
  private FormData fdFields;

  private FormData fdlAddResult, fdAddFileResult, fdAddResult;

  private Group wAddFileResult;

  private Label wlAddResult;

  private Button wAddResult;

  private TextFileInputMeta input;

  private Button wMinWidth;
  private Listener lsMinWidth;

  // Wizard info...
  private Vector<TextFileInputFieldInterface> fields;

  private String[] dateLocale;

  private int middle, margin;
  private ModifyListener lsMod;

  public static final int[] dateLengths = new int[] { 23, 19, 14, 10, 10, 10, 10, 8, 8, 8, 8, 6, 6 };

  private boolean gotEncodings = false;

  protected boolean firstClickOnDateLocale;

  private CTabItem wAdditionalFieldsTab;
  private Composite wAdditionalFieldsComp;
  private FormData fdAdditionalFieldsComp;

  private Label wlShortFileFieldName;
  private FormData fdlShortFileFieldName;
  private TextVar wShortFileFieldName;
  private FormData fdShortFileFieldName;
  private Label wlPathFieldName;
  private FormData fdlPathFieldName;
  private TextVar wPathFieldName;
  private FormData fdPathFieldName;

  private Label wlIsHiddenName;
  private FormData fdlIsHiddenName;
  private TextVar wIsHiddenName;
  private FormData fdIsHiddenName;
  private Label wlLastModificationTimeName;
  private FormData fdlLastModificationTimeName;
  private TextVar wLastModificationTimeName;
  private FormData fdLastModificationTimeName;
  private Label wlUriName;
  private FormData fdlUriName;
  private TextVar wUriName;
  private FormData fdUriName;
  private Label wlRootUriName;
  private FormData fdlRootUriName;
  private TextVar wRootUriName;
  private FormData fdRootUriName;
  private Label wlExtensionFieldName;
  private FormData fdlExtensionFieldName;
  private TextVar wExtensionFieldName;
  private FormData fdExtensionFieldName;
  private Label wlSizeFieldName;
  private FormData fdlSizeFieldName;
  private TextVar wSizeFieldName;
  private FormData fdSizeFieldName;

  private Label wlBadFileField;

  private FormData fdlBadFileField;

  private Text wBadFileField;

  private FormData fdBadFileField;

  private Label wlBadFileMessageField;

  private FormData fdlBadFileMessageField;

  private Text wBadFileMessageField;

  private FormData fdBadFileMessageField;

  public TextFileInputDialog( Shell parent, Object in, TransMeta transMeta, String sname ) {
    super( parent, (BaseStepMeta) in, transMeta, sname );
    input = (TextFileInputMeta) in;
    firstClickOnDateLocale = true;
  }

  public String open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );
    props.setLook( shell );
    setShellImage( shell, input );

    lsMod = new ModifyListener() {
      public void modifyText( ModifyEvent e ) {
        input.setChanged();
      }
    };
    changed = input.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout( formLayout );
    shell.setText( BaseMessages.getString( PKG, "TextFileInputDialog.DialogTitle" ) );

    middle = props.getMiddlePct();
    margin = Const.MARGIN;

    // Stepname line
    wlStepname = new Label( shell, SWT.RIGHT );
    wlStepname.setText( BaseMessages.getString( PKG, "System.Label.StepName" ) );
    props.setLook( wlStepname );
    fdlStepname = new FormData();
    fdlStepname.left = new FormAttachment( 0, 0 );
    fdlStepname.top = new FormAttachment( 0, margin );
    fdlStepname.right = new FormAttachment( middle, -margin );
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

    addFilesTab();
    addContentTab();
    addErrorTab();
    addFiltersTabs();
    addFieldsTabs();
    addAdditionalFieldsTab();

    fdTabFolder = new FormData();
    fdTabFolder.left = new FormAttachment( 0, 0 );
    fdTabFolder.top = new FormAttachment( wStepname, margin );
    fdTabFolder.right = new FormAttachment( 100, 0 );
    fdTabFolder.bottom = new FormAttachment( 100, -50 );
    wTabFolder.setLayoutData( fdTabFolder );

    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );

    wPreview = new Button( shell, SWT.PUSH );
    wPreview.setText( BaseMessages.getString( PKG, "TextFileInputDialog.Preview.Button" ) );

    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );

    setButtonPositions( new Button[] { wOK, wPreview, wCancel }, margin, wTabFolder );

    // Add listeners
    lsOK = new Listener() {
      public void handleEvent( Event e ) {
        ok();
      }
    };
    lsFirst = new Listener() {
      public void handleEvent( Event e ) {
        first( false );
      }
    };
    lsFirstHeader = new Listener() {
      public void handleEvent( Event e ) {
        first( true );
      }
    };
    lsGet = new Listener() {
      public void handleEvent( Event e ) {
        get();
      }
    };
    lsPreview = new Listener() {
      public void handleEvent( Event e ) {
        preview();
      }
    };
    lsMinWidth = new Listener() {
      public void handleEvent( Event e ) {
        setMinimalWidth();
      }
    };
    lsCancel = new Listener() {
      public void handleEvent( Event e ) {
        cancel();
      }
    };

    wOK.addListener( SWT.Selection, lsOK );
    wFirst.addListener( SWT.Selection, lsFirst );
    wFirstHeader.addListener( SWT.Selection, lsFirstHeader );
    wGet.addListener( SWT.Selection, lsGet );
    wMinWidth.addListener( SWT.Selection, lsMinWidth );
    wPreview.addListener( SWT.Selection, lsPreview );
    wCancel.addListener( SWT.Selection, lsCancel );

    lsDef = new SelectionAdapter() {
      public void widgetDefaultSelected( SelectionEvent e ) {
        ok();
      }
    };

    wAccFilenames.addSelectionListener( lsDef );
    wStepname.addSelectionListener( lsDef );
    // wFilename.addSelectionListener( lsDef );
    wSeparator.addSelectionListener( lsDef );
    wLimit.addSelectionListener( lsDef );
    wInclRownumField.addSelectionListener( lsDef );
    wInclFilenameField.addSelectionListener( lsDef );
    wNrHeader.addSelectionListener( lsDef );
    wNrFooter.addSelectionListener( lsDef );
    wNrWraps.addSelectionListener( lsDef );
    wWarnDestDir.addSelectionListener( lsDef );
    wWarnExt.addSelectionListener( lsDef );
    wErrorDestDir.addSelectionListener( lsDef );
    wErrorExt.addSelectionListener( lsDef );
    wLineNrDestDir.addSelectionListener( lsDef );
    wLineNrExt.addSelectionListener( lsDef );
    wAccField.addSelectionListener( lsDef );

    // Add the file to the list of files...
    SelectionAdapter selA = new SelectionAdapter() {
      public void widgetSelected( SelectionEvent arg0 ) {
        wFilenameList.add( wFilename.getText(), wFilemask.getText(), wExcludeFilemask.getText(),
            TextFileInputMeta.RequiredFilesCode[0], TextFileInputMeta.RequiredFilesCode[0] );
        wFilename.setText( "" );
        wFilemask.setText( "" );
        wExcludeFilemask.setText( "" );
        wFilenameList.removeEmptyRows();
        wFilenameList.setRowNums();
        wFilenameList.optWidth( true );
      }
    };
    wbaFilename.addSelectionListener( selA );
    wFilename.addSelectionListener( selA );

    // Delete files from the list of files...
    wbdFilename.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent arg0 ) {
        int[] idx = wFilenameList.getSelectionIndices();
        wFilenameList.remove( idx );
        wFilenameList.removeEmptyRows();
        wFilenameList.setRowNums();
      }
    } );

    // Edit the selected file & remove from the list...
    wbeFilename.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent arg0 ) {
        int idx = wFilenameList.getSelectionIndex();
        if ( idx >= 0 ) {
          String[] string = wFilenameList.getItem( idx );
          wFilename.setText( string[0] );
          wFilemask.setText( string[1] );
          wExcludeFilemask.setText( string[2] );
          wFilenameList.remove( idx );
        }
        wFilenameList.removeEmptyRows();
        wFilenameList.setRowNums();
      }
    } );

    // Show the files that are selected at this time...
    wbShowFiles.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        showFiles();
      }
    } );

    // Allow the insertion of tabs as separator...
    wbSeparator.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent se ) {
        wSeparator.getTextWidget().insert( "\t" );
      }
    } );

    SelectionAdapter lsFlags = new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        setFlags();
      }
    };

    // Enable/disable the right fields...
    wInclFilename.addSelectionListener( lsFlags );
    wInclRownum.addSelectionListener( lsFlags );
    wRownumByFile.addSelectionListener( lsFlags );
    wErrorIgnored.addSelectionListener( lsFlags );
    wSkipBadFiles.addSelectionListener( lsFlags );
    wHeader.addSelectionListener( lsFlags );
    wFooter.addSelectionListener( lsFlags );
    wWraps.addSelectionListener( lsFlags );
    wLayoutPaged.addSelectionListener( lsFlags );
    wAccFilenames.addSelectionListener( lsFlags );

    // Listen to the Browse... button
    wbbFilename.addSelectionListener( new SelectionAdapterFileDialogTextVar( log, wFilename, transMeta,
      new SelectionAdapterOptions( SelectionOperation.FILE_OR_FOLDER,
        new FilterType[] { FilterType.TXT, FilterType.CSV, FilterType.ALL }, FilterType.TXT ) ) );

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    wTabFolder.setSelection( 0 );

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

  private void showFiles() {
    TextFileInputMeta tfii = new TextFileInputMeta();
    getInfo( tfii, true );
    String[] files =
        FileInputList.createFilePathList( transMeta.getBowl(), transMeta, tfii.inputFiles.fileName,
            tfii.inputFiles.fileMask, tfii.inputFiles.excludeFileMask, tfii.inputFiles.fileRequired,
            tfii.inputFiles.includeSubFolderBoolean() );

    if ( files != null && files.length > 0 ) {
      EnterSelectionDialog esd = new EnterSelectionDialog( shell, files, "Files read", "Files read:" );
      esd.setViewOnly();
      esd.open();
    } else {
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
      mb.setMessage( BaseMessages.getString( PKG, "TextFileInputDialog.NoFilesFound.DialogMessage" ) );
      mb.setText( BaseMessages.getString( PKG, "System.Dialog.Error.Title" ) );
      mb.open();
    }
  }

  private void addFilesTab() {
    // ////////////////////////
    // START OF FILE TAB ///
    // ////////////////////////

    wFileTab = new CTabItem( wTabFolder, SWT.NONE );
    wFileTab.setText( BaseMessages.getString( PKG, "TextFileInputDialog.FileTab.TabTitle" ) );

    wFileSComp = new ScrolledComposite( wTabFolder, SWT.V_SCROLL | SWT.H_SCROLL );
    wFileSComp.setLayout( new FillLayout() );

    wFileComp = new Composite( wFileSComp, SWT.NONE );
    props.setLook( wFileComp );

    FormLayout fileLayout = new FormLayout();
    fileLayout.marginWidth = 3;
    fileLayout.marginHeight = 3;
    wFileComp.setLayout( fileLayout );

    // Filename line
    wlFilename = new Label( wFileComp, SWT.RIGHT );
    wlFilename.setText( BaseMessages.getString( PKG, "TextFileInputDialog.Filename.Label" ) );
    props.setLook( wlFilename );
    fdlFilename = new FormData();
    fdlFilename.left = new FormAttachment( 0, 0 );
    fdlFilename.top = new FormAttachment( 0, 0 );
    fdlFilename.right = new FormAttachment( middle, -margin );
    wlFilename.setLayoutData( fdlFilename );

    wbbFilename = new Button( wFileComp, SWT.PUSH | SWT.CENTER );
    props.setLook( wbbFilename );
    wbbFilename.setText( BaseMessages.getString( PKG, "System.Button.Browse" ) );
    wbbFilename.setToolTipText( BaseMessages.getString( PKG, "System.Tooltip.BrowseForFileOrDirAndAdd" ) );
    fdbFilename = new FormData();
    fdbFilename.right = new FormAttachment( 100, 0 );
    fdbFilename.top = new FormAttachment( 0, 0 );
    wbbFilename.setLayoutData( fdbFilename );

    wbaFilename = new Button( wFileComp, SWT.PUSH | SWT.CENTER );
    props.setLook( wbaFilename );
    wbaFilename.setText( BaseMessages.getString( PKG, "TextFileInputDialog.FilenameAdd.Button" ) );
    wbaFilename.setToolTipText( BaseMessages.getString( PKG, "TextFileInputDialog.FilenameAdd.Tooltip" ) );
    fdbaFilename = new FormData();
    fdbaFilename.right = new FormAttachment( wbbFilename, -margin );
    fdbaFilename.top = new FormAttachment( 0, 0 );
    wbaFilename.setLayoutData( fdbaFilename );

    wFilename = new TextVar( transMeta, wFileComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wFilename );
    wFilename.addModifyListener( lsMod );
    fdFilename = new FormData();
    fdFilename.left = new FormAttachment( middle, 0 );
    fdFilename.right = new FormAttachment( wbaFilename, -margin );
    fdFilename.top = new FormAttachment( 0, 0 );
    wFilename.setLayoutData( fdFilename );

    wlFilemask = new Label( wFileComp, SWT.RIGHT );
    wlFilemask.setText( BaseMessages.getString( PKG, "TextFileInputDialog.Filemask.Label" ) );
    props.setLook( wlFilemask );
    fdlFilemask = new FormData();
    fdlFilemask.left = new FormAttachment( 0, 0 );
    fdlFilemask.top = new FormAttachment( wFilename, margin );
    fdlFilemask.right = new FormAttachment( middle, -margin );
    wlFilemask.setLayoutData( fdlFilemask );

    // PDI-8664
    wFilemask = new TextVar( transMeta, wFileComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );

    props.setLook( wFilemask );
    wFilemask.addModifyListener( lsMod );
    fdFilemask = new FormData();
    fdFilemask.left = new FormAttachment( middle, 0 );
    fdFilemask.top = new FormAttachment( wFilename, margin );
    fdFilemask.right = new FormAttachment( wbaFilename, -margin );
    wFilemask.setLayoutData( fdFilemask );

    wlExcludeFilemask = new Label( wFileComp, SWT.RIGHT );
    wlExcludeFilemask.setText( BaseMessages.getString( PKG, "TextFileInputDialog.ExcludeFilemask.Label" ) );
    props.setLook( wlExcludeFilemask );
    fdlExcludeFilemask = new FormData();
    fdlExcludeFilemask.left = new FormAttachment( 0, 0 );
    fdlExcludeFilemask.top = new FormAttachment( wFilemask, margin );
    fdlExcludeFilemask.right = new FormAttachment( middle, -margin );
    wlExcludeFilemask.setLayoutData( fdlExcludeFilemask );
    wExcludeFilemask = new TextVar( transMeta, wFileComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wExcludeFilemask );
    wExcludeFilemask.addModifyListener( lsMod );
    fdExcludeFilemask = new FormData();
    fdExcludeFilemask.left = new FormAttachment( middle, 0 );
    fdExcludeFilemask.top = new FormAttachment( wFilemask, margin );
    fdExcludeFilemask.right = new FormAttachment( wFilename, 0, SWT.RIGHT );
    wExcludeFilemask.setLayoutData( fdExcludeFilemask );

    // Filename list line
    wlFilenameList = new Label( wFileComp, SWT.RIGHT );
    wlFilenameList.setText( BaseMessages.getString( PKG, "TextFileInputDialog.FilenameList.Label" ) );
    props.setLook( wlFilenameList );
    fdlFilenameList = new FormData();
    fdlFilenameList.left = new FormAttachment( 0, 0 );
    fdlFilenameList.top = new FormAttachment( wExcludeFilemask, margin );
    fdlFilenameList.right = new FormAttachment( middle, -margin );
    wlFilenameList.setLayoutData( fdlFilenameList );

    // Buttons to the right of the screen...
    wbdFilename = new Button( wFileComp, SWT.PUSH | SWT.CENTER );
    props.setLook( wbdFilename );
    wbdFilename.setText( BaseMessages.getString( PKG, "TextFileInputDialog.FilenameDelete.Button" ) );
    wbdFilename.setToolTipText( BaseMessages.getString( PKG, "TextFileInputDialog.FilenameDelete.Tooltip" ) );
    fdbdFilename = new FormData();
    fdbdFilename.right = new FormAttachment( 100, 0 );
    fdbdFilename.top = new FormAttachment( wExcludeFilemask, 40 );
    wbdFilename.setLayoutData( fdbdFilename );

    wbeFilename = new Button( wFileComp, SWT.PUSH | SWT.CENTER );
    props.setLook( wbeFilename );
    wbeFilename.setText( BaseMessages.getString( PKG, "TextFileInputDialog.FilenameEdit.Button" ) );
    wbeFilename.setToolTipText( BaseMessages.getString( PKG, "TextFileInputDialog.FilenameEdit.Tooltip" ) );
    fdbeFilename = new FormData();
    fdbeFilename.right = new FormAttachment( 100, 0 );
    fdbeFilename.left = new FormAttachment( wbdFilename, 0, SWT.LEFT );
    fdbeFilename.top = new FormAttachment( wbdFilename, margin );
    wbeFilename.setLayoutData( fdbeFilename );

    wbShowFiles = new Button( wFileComp, SWT.PUSH | SWT.CENTER );
    props.setLook( wbShowFiles );
    wbShowFiles.setText( BaseMessages.getString( PKG, "TextFileInputDialog.ShowFiles.Button" ) );
    fdbShowFiles = new FormData();
    fdbShowFiles.left = new FormAttachment( middle, 0 );
    fdbShowFiles.bottom = new FormAttachment( 100, 0 );
    wbShowFiles.setLayoutData( fdbShowFiles );

    wFirst = new Button( wFileComp, SWT.PUSH );
    wFirst.setText( BaseMessages.getString( PKG, "TextFileInputDialog.First.Button" ) );
    fdFirst = new FormData();
    fdFirst.left = new FormAttachment( wbShowFiles, margin * 2 );
    fdFirst.bottom = new FormAttachment( 100, 0 );
    wFirst.setLayoutData( fdFirst );

    wFirstHeader = new Button( wFileComp, SWT.PUSH );
    wFirstHeader.setText( BaseMessages.getString( PKG, "TextFileInputDialog.FirstHeader.Button" ) );
    fdFirstHeader = new FormData();
    fdFirstHeader.left = new FormAttachment( wFirst, margin * 2 );
    fdFirstHeader.bottom = new FormAttachment( 100, 0 );
    wFirstHeader.setLayoutData( fdFirstHeader );

    // Accepting filenames group
    //

    gAccepting = new Group( wFileComp, SWT.SHADOW_ETCHED_IN );
    gAccepting.setText( BaseMessages.getString( PKG, "TextFileInputDialog.AcceptingGroup.Label" ) );
    FormLayout acceptingLayout = new FormLayout();
    acceptingLayout.marginWidth = 3;
    acceptingLayout.marginHeight = 3;
    gAccepting.setLayout( acceptingLayout );
    props.setLook( gAccepting );

    // Accept filenames from previous steps?
    //
    wlAccFilenames = new Label( gAccepting, SWT.RIGHT );
    wlAccFilenames.setText( BaseMessages.getString( PKG, "TextFileInputDialog.AcceptFilenames.Label" ) );
    props.setLook( wlAccFilenames );
    fdlAccFilenames = new FormData();
    fdlAccFilenames.top = new FormAttachment( 0, margin );
    fdlAccFilenames.left = new FormAttachment( 0, 0 );
    fdlAccFilenames.right = new FormAttachment( middle, -margin );
    wlAccFilenames.setLayoutData( fdlAccFilenames );
    wAccFilenames = new Button( gAccepting, SWT.CHECK );
    wAccFilenames.setToolTipText( BaseMessages.getString( PKG, "TextFileInputDialog.AcceptFilenames.Tooltip" ) );
    props.setLook( wAccFilenames );
    fdAccFilenames = new FormData();
    fdAccFilenames.top = new FormAttachment( 0, margin );
    fdAccFilenames.left = new FormAttachment( middle, 0 );
    fdAccFilenames.right = new FormAttachment( 100, 0 );
    wAccFilenames.setLayoutData( fdAccFilenames );

    // Accept filenames from previous steps?
    //
    wlPassThruFields = new Label( gAccepting, SWT.RIGHT );
    wlPassThruFields.setText( BaseMessages.getString( PKG, "TextFileInputDialog.PassThruFields.Label" ) );
    props.setLook( wlPassThruFields );
    fdlPassThruFields = new FormData();
    fdlPassThruFields.top = new FormAttachment( wAccFilenames, margin );
    fdlPassThruFields.left = new FormAttachment( 0, 0 );
    fdlPassThruFields.right = new FormAttachment( middle, -margin );
    wlPassThruFields.setLayoutData( fdlPassThruFields );
    wPassThruFields = new Button( gAccepting, SWT.CHECK );
    wPassThruFields.setToolTipText( BaseMessages.getString( PKG, "TextFileInputDialog.PassThruFields.Tooltip" ) );
    props.setLook( wPassThruFields );
    fdPassThruFields = new FormData();
    fdPassThruFields.top = new FormAttachment( wAccFilenames, margin );
    fdPassThruFields.left = new FormAttachment( middle, 0 );
    fdPassThruFields.right = new FormAttachment( 100, 0 );
    wPassThruFields.setLayoutData( fdPassThruFields );

    // Which step to read from?
    wlAccStep = new Label( gAccepting, SWT.RIGHT );
    wlAccStep.setText( BaseMessages.getString( PKG, "TextFileInputDialog.AcceptStep.Label" ) );
    props.setLook( wlAccStep );
    fdlAccStep = new FormData();
    fdlAccStep.top = new FormAttachment( wPassThruFields, margin );
    fdlAccStep.left = new FormAttachment( 0, 0 );
    fdlAccStep.right = new FormAttachment( middle, -margin );
    wlAccStep.setLayoutData( fdlAccStep );
    wAccStep = new CCombo( gAccepting, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wAccStep.setToolTipText( BaseMessages.getString( PKG, "TextFileInputDialog.AcceptStep.Tooltip" ) );
    props.setLook( wAccStep );
    fdAccStep = new FormData();
    fdAccStep.top = new FormAttachment( wPassThruFields, margin );
    fdAccStep.left = new FormAttachment( middle, 0 );
    fdAccStep.right = new FormAttachment( 100, 0 );
    wAccStep.setLayoutData( fdAccStep );

    // Which field?
    //
    wlAccField = new Label( gAccepting, SWT.RIGHT );
    wlAccField.setText( BaseMessages.getString( PKG, "TextFileInputDialog.AcceptField.Label" ) );
    props.setLook( wlAccField );
    fdlAccField = new FormData();
    fdlAccField.top = new FormAttachment( wAccStep, margin );
    fdlAccField.left = new FormAttachment( 0, 0 );
    fdlAccField.right = new FormAttachment( middle, -margin );
    wlAccField.setLayoutData( fdlAccField );
    wAccField = new Text( gAccepting, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wAccField.setToolTipText( BaseMessages.getString( PKG, "TextFileInputDialog.AcceptField.Tooltip" ) );
    props.setLook( wAccField );
    fdAccField = new FormData();
    fdAccField.top = new FormAttachment( wAccStep, margin );
    fdAccField.left = new FormAttachment( middle, 0 );
    fdAccField.right = new FormAttachment( 100, 0 );
    wAccField.setLayoutData( fdAccField );

    // Fill in the source steps...
    List<StepMeta> prevSteps = transMeta.findPreviousSteps( transMeta.findStep( stepname ) );
    for ( StepMeta prevStep : prevSteps ) {
      wAccStep.add( prevStep.getName() );
    }

    fdAccepting = new FormData();
    fdAccepting.left = new FormAttachment( 0, 0 );
    fdAccepting.right = new FormAttachment( 100, 0 );
    fdAccepting.bottom = new FormAttachment( wFirstHeader, -margin * 2 );
    gAccepting.setLayoutData( fdAccepting );

    ColumnInfo[] colinfo =
        new ColumnInfo[] { new ColumnInfo( BaseMessages.getString( PKG, "TextFileInputDialog.FileDirColumn.Column" ),
            ColumnInfo.COLUMN_TYPE_TEXT, false ), new ColumnInfo( BaseMessages.getString( PKG,
                "TextFileInputDialog.WildcardColumn.Column" ), ColumnInfo.COLUMN_TYPE_TEXT, false ), new ColumnInfo(
                    BaseMessages.getString( PKG, "TextFileInputDialog.Files.ExcludeWildcard.Column" ),
                    ColumnInfo.COLUMN_TYPE_TEXT, false ),
          new ColumnInfo( BaseMessages.getString( PKG, "TextFileInputDialog.RequiredColumn.Column" ),
            ColumnInfo.COLUMN_TYPE_CCOMBO, YES_NO_COMBO ), new ColumnInfo( BaseMessages.getString( PKG,
                "TextFileInputDialog.IncludeSubDirs.Column" ), ColumnInfo.COLUMN_TYPE_CCOMBO, YES_NO_COMBO ) };

    colinfo[0].setUsingVariables( true );
    colinfo[1].setToolTip( BaseMessages.getString( PKG, "TextFileInputDialog.RegExpColumn.Column" ) );

    // PDI-8664
    colinfo[1].setUsingVariables( true );

    colinfo[2].setUsingVariables( true );
    colinfo[2].setToolTip( BaseMessages.getString( PKG, "TextFileInputDialog.Files.ExcludeWildcard.Tooltip" ) );
    colinfo[3].setToolTip( BaseMessages.getString( PKG, "TextFileInputDialog.RequiredColumn.Tooltip" ) );
    colinfo[4].setToolTip( BaseMessages.getString( PKG, "TextFileInputDialog.IncludeSubDirs.Tooltip" ) );

    wFilenameList =
        new TableView( transMeta, wFileComp, SWT.FULL_SELECTION | SWT.SINGLE | SWT.BORDER, colinfo, 4, lsMod, props );
    props.setLook( wFilenameList );
    fdFilenameList = new FormData();
    fdFilenameList.left = new FormAttachment( middle, 0 );
    fdFilenameList.right = new FormAttachment( wbdFilename, -margin );
    fdFilenameList.top = new FormAttachment( wExcludeFilemask, margin );
    fdFilenameList.bottom = new FormAttachment( gAccepting, -margin );
    wFilenameList.setLayoutData( fdFilenameList );

    fdFileComp = new FormData();
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

  private void addContentTab() {
    // ////////////////////////
    // START OF CONTENT TAB///
    // /
    wContentTab = new CTabItem( wTabFolder, SWT.NONE );
    wContentTab.setText( BaseMessages.getString( PKG, "TextFileInputDialog.ContentTab.TabTitle" ) );

    FormLayout contentLayout = new FormLayout();
    contentLayout.marginWidth = 3;
    contentLayout.marginHeight = 3;

    wContentSComp = new ScrolledComposite( wTabFolder, SWT.V_SCROLL | SWT.H_SCROLL );
    wContentSComp.setLayout( new FillLayout() );

    wContentComp = new Composite( wContentSComp, SWT.NONE );
    props.setLook( wContentComp );
    wContentComp.setLayout( contentLayout );

    // Filetype line
    wlFiletype = new Label( wContentComp, SWT.RIGHT );
    wlFiletype.setText( BaseMessages.getString( PKG, "TextFileInputDialog.Filetype.Label" ) );
    props.setLook( wlFiletype );
    fdlFiletype = new FormData();
    fdlFiletype.left = new FormAttachment( 0, 0 );
    fdlFiletype.top = new FormAttachment( 0, 0 );
    fdlFiletype.right = new FormAttachment( middle, -margin );
    wlFiletype.setLayoutData( fdlFiletype );
    wFiletype = new CCombo( wContentComp, SWT.BORDER | SWT.READ_ONLY );
    wFiletype.setText( BaseMessages.getString( PKG, "TextFileInputDialog.Filetype.Label" ) );
    props.setLook( wFiletype );
    wFiletype.add( "CSV" );
    wFiletype.add( "Fixed" );
    wFiletype.select( 0 );
    wFiletype.addModifyListener( lsMod );
    fdFiletype = new FormData();
    fdFiletype.left = new FormAttachment( middle, 0 );
    fdFiletype.top = new FormAttachment( 0, 0 );
    fdFiletype.right = new FormAttachment( 100, 0 );
    wFiletype.setLayoutData( fdFiletype );

    wlSeparator = new Label( wContentComp, SWT.RIGHT );
    wlSeparator.setText( BaseMessages.getString( PKG, "TextFileInputDialog.Separator.Label" ) );
    props.setLook( wlSeparator );
    fdlSeparator = new FormData();
    fdlSeparator.left = new FormAttachment( 0, 0 );
    fdlSeparator.top = new FormAttachment( wFiletype, margin );
    fdlSeparator.right = new FormAttachment( middle, -margin );
    wlSeparator.setLayoutData( fdlSeparator );

    wbSeparator = new Button( wContentComp, SWT.PUSH | SWT.CENTER );
    wbSeparator.setText( BaseMessages.getString( PKG, "TextFileInputDialog.Delimiter.Button" ) );
    props.setLook( wbSeparator );
    fdbSeparator = new FormData();
    fdbSeparator.right = new FormAttachment( 100, 0 );
    fdbSeparator.top = new FormAttachment( wFiletype, 0 );
    wbSeparator.setLayoutData( fdbSeparator );
    wSeparator = new TextVar( transMeta, wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wSeparator );
    wSeparator.addModifyListener( lsMod );
    fdSeparator = new FormData();
    fdSeparator.top = new FormAttachment( wFiletype, margin );
    fdSeparator.left = new FormAttachment( middle, 0 );
    fdSeparator.right = new FormAttachment( wbSeparator, -margin );
    wSeparator.setLayoutData( fdSeparator );

    // Enclosure
    wlEnclosure = new Label( wContentComp, SWT.RIGHT );
    wlEnclosure.setText( BaseMessages.getString( PKG, "TextFileInputDialog.Enclosure.Label" ) );
    props.setLook( wlEnclosure );
    fdlEnclosure = new FormData();
    fdlEnclosure.left = new FormAttachment( 0, 0 );
    fdlEnclosure.top = new FormAttachment( wSeparator, margin );
    fdlEnclosure.right = new FormAttachment( middle, -margin );
    wlEnclosure.setLayoutData( fdlEnclosure );
    wEnclosure = new Text( wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wEnclosure );
    wEnclosure.addModifyListener( lsMod );
    fdEnclosure = new FormData();
    fdEnclosure.left = new FormAttachment( middle, 0 );
    fdEnclosure.top = new FormAttachment( wSeparator, margin );
    fdEnclosure.right = new FormAttachment( 100, 0 );
    wEnclosure.setLayoutData( fdEnclosure );

    // Escape
    wlEscape = new Label( wContentComp, SWT.RIGHT );
    wlEscape.setText( BaseMessages.getString( PKG, "TextFileInputDialog.Escape.Label" ) );
    props.setLook( wlEscape );
    fdlEscape = new FormData();
    fdlEscape.left = new FormAttachment( 0, 0 );
    fdlEscape.top = new FormAttachment( wEnclosure, margin );
    fdlEscape.right = new FormAttachment( middle, -margin );
    wlEscape.setLayoutData( fdlEscape );
    wEscape = new Text( wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wEscape );
    wEscape.addModifyListener( lsMod );
    fdEscape = new FormData();
    fdEscape.left = new FormAttachment( middle, 0 );
    fdEscape.top = new FormAttachment( wEnclosure, margin );
    fdEscape.right = new FormAttachment( 100, 0 );
    wEscape.setLayoutData( fdEscape );

    // Header checkbox
    wlHeader = new Label( wContentComp, SWT.RIGHT );
    wlHeader.setText( BaseMessages.getString( PKG, "TextFileInputDialog.Header.Label" ) );
    props.setLook( wlHeader );
    fdlHeader = new FormData();
    fdlHeader.left = new FormAttachment( 0, 0 );
    fdlHeader.top = new FormAttachment( wEscape, margin );
    fdlHeader.right = new FormAttachment( middle, -margin );
    wlHeader.setLayoutData( fdlHeader );
    wHeader = new Button( wContentComp, SWT.CHECK );
    props.setLook( wHeader );
    fdHeader = new FormData();
    fdHeader.left = new FormAttachment( middle, 0 );
    fdHeader.top = new FormAttachment( wEscape, margin );
    wHeader.setLayoutData( fdHeader );

    // NrHeader
    wlNrHeader = new Label( wContentComp, SWT.RIGHT );
    wlNrHeader.setText( BaseMessages.getString( PKG, "TextFileInputDialog.NrHeader.Label" ) );
    props.setLook( wlNrHeader );
    fdlNrHeader = new FormData();
    fdlNrHeader.left = new FormAttachment( wHeader, margin );
    fdlNrHeader.top = new FormAttachment( wEscape, margin );
    wlNrHeader.setLayoutData( fdlNrHeader );
    wNrHeader = new Text( wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wNrHeader.setTextLimit( 3 );
    props.setLook( wNrHeader );
    wNrHeader.addModifyListener( lsMod );
    fdNrHeader = new FormData();
    fdNrHeader.left = new FormAttachment( wlNrHeader, margin );
    fdNrHeader.top = new FormAttachment( wEscape, margin );
    fdNrHeader.right = new FormAttachment( 100, 0 );
    wNrHeader.setLayoutData( fdNrHeader );

    wlFooter = new Label( wContentComp, SWT.RIGHT );
    wlFooter.setText( BaseMessages.getString( PKG, "TextFileInputDialog.Footer.Label" ) );
    props.setLook( wlFooter );
    fdlFooter = new FormData();
    fdlFooter.left = new FormAttachment( 0, 0 );
    fdlFooter.top = new FormAttachment( wHeader, margin );
    fdlFooter.right = new FormAttachment( middle, -margin );
    wlFooter.setLayoutData( fdlFooter );
    wFooter = new Button( wContentComp, SWT.CHECK );
    props.setLook( wFooter );
    fdFooter = new FormData();
    fdFooter.left = new FormAttachment( middle, 0 );
    fdFooter.top = new FormAttachment( wHeader, margin );
    wFooter.setLayoutData( fdFooter );

    // NrFooter
    wlNrFooter = new Label( wContentComp, SWT.RIGHT );
    wlNrFooter.setText( BaseMessages.getString( PKG, "TextFileInputDialog.NrFooter.Label" ) );
    props.setLook( wlNrFooter );
    fdlNrFooter = new FormData();
    fdlNrFooter.left = new FormAttachment( wFooter, margin );
    fdlNrFooter.top = new FormAttachment( wHeader, margin );
    wlNrFooter.setLayoutData( fdlNrFooter );
    wNrFooter = new Text( wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wNrFooter.setTextLimit( 3 );
    props.setLook( wNrFooter );
    wNrFooter.addModifyListener( lsMod );
    fdNrFooter = new FormData();
    fdNrFooter.left = new FormAttachment( wlNrFooter, margin );
    fdNrFooter.top = new FormAttachment( wHeader, margin );
    fdNrFooter.right = new FormAttachment( 100, 0 );
    wNrFooter.setLayoutData( fdNrFooter );

    // Wraps
    wlWraps = new Label( wContentComp, SWT.RIGHT );
    wlWraps.setText( BaseMessages.getString( PKG, "TextFileInputDialog.Wraps.Label" ) );
    props.setLook( wlWraps );
    fdlWraps = new FormData();
    fdlWraps.left = new FormAttachment( 0, 0 );
    fdlWraps.top = new FormAttachment( wFooter, margin );
    fdlWraps.right = new FormAttachment( middle, -margin );
    wlWraps.setLayoutData( fdlWraps );
    wWraps = new Button( wContentComp, SWT.CHECK );
    props.setLook( wWraps );
    fdWraps = new FormData();
    fdWraps.left = new FormAttachment( middle, 0 );
    fdWraps.top = new FormAttachment( wFooter, margin );
    wWraps.setLayoutData( fdWraps );

    // NrWraps
    wlNrWraps = new Label( wContentComp, SWT.RIGHT );
    wlNrWraps.setText( BaseMessages.getString( PKG, "TextFileInputDialog.NrWraps.Label" ) );
    props.setLook( wlNrWraps );
    fdlNrWraps = new FormData();
    fdlNrWraps.left = new FormAttachment( wWraps, margin );
    fdlNrWraps.top = new FormAttachment( wFooter, margin );
    wlNrWraps.setLayoutData( fdlNrWraps );
    wNrWraps = new Text( wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wNrWraps.setTextLimit( 3 );
    props.setLook( wNrWraps );
    wNrWraps.addModifyListener( lsMod );
    fdNrWraps = new FormData();
    fdNrWraps.left = new FormAttachment( wlNrWraps, margin );
    fdNrWraps.top = new FormAttachment( wFooter, margin );
    fdNrWraps.right = new FormAttachment( 100, 0 );
    wNrWraps.setLayoutData( fdNrWraps );

    // Pages
    wlLayoutPaged = new Label( wContentComp, SWT.RIGHT );
    wlLayoutPaged.setText( BaseMessages.getString( PKG, "TextFileInputDialog.LayoutPaged.Label" ) );
    props.setLook( wlLayoutPaged );
    fdlLayoutPaged = new FormData();
    fdlLayoutPaged.left = new FormAttachment( 0, 0 );
    fdlLayoutPaged.top = new FormAttachment( wWraps, margin );
    fdlLayoutPaged.right = new FormAttachment( middle, -margin );
    wlLayoutPaged.setLayoutData( fdlLayoutPaged );
    wLayoutPaged = new Button( wContentComp, SWT.CHECK );
    props.setLook( wLayoutPaged );
    fdLayoutPaged = new FormData();
    fdLayoutPaged.left = new FormAttachment( middle, 0 );
    fdLayoutPaged.top = new FormAttachment( wWraps, margin );
    wLayoutPaged.setLayoutData( fdLayoutPaged );

    // Nr of lines per page
    wlNrLinesPerPage = new Label( wContentComp, SWT.RIGHT );
    wlNrLinesPerPage.setText( BaseMessages.getString( PKG, "TextFileInputDialog.NrLinesPerPage.Label" ) );
    props.setLook( wlNrLinesPerPage );
    fdlNrLinesPerPage = new FormData();
    fdlNrLinesPerPage.left = new FormAttachment( wLayoutPaged, margin );
    fdlNrLinesPerPage.top = new FormAttachment( wWraps, margin );
    wlNrLinesPerPage.setLayoutData( fdlNrLinesPerPage );
    wNrLinesPerPage = new Text( wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wNrLinesPerPage.setTextLimit( 3 );
    props.setLook( wNrLinesPerPage );
    wNrLinesPerPage.addModifyListener( lsMod );
    fdNrLinesPerPage = new FormData();
    fdNrLinesPerPage.left = new FormAttachment( wlNrLinesPerPage, margin );
    fdNrLinesPerPage.top = new FormAttachment( wWraps, margin );
    fdNrLinesPerPage.right = new FormAttachment( 100, 0 );
    wNrLinesPerPage.setLayoutData( fdNrLinesPerPage );

    // NrPages
    wlNrLinesDocHeader = new Label( wContentComp, SWT.RIGHT );
    wlNrLinesDocHeader.setText( BaseMessages.getString( PKG, "TextFileInputDialog.NrLinesDocHeader.Label" ) );
    props.setLook( wlNrLinesDocHeader );
    fdlNrLinesDocHeader = new FormData();
    fdlNrLinesDocHeader.left = new FormAttachment( wLayoutPaged, margin );
    fdlNrLinesDocHeader.top = new FormAttachment( wNrLinesPerPage, margin );
    wlNrLinesDocHeader.setLayoutData( fdlNrLinesDocHeader );
    wNrLinesDocHeader = new Text( wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wNrLinesDocHeader.setTextLimit( 3 );
    props.setLook( wNrLinesDocHeader );
    wNrLinesDocHeader.addModifyListener( lsMod );
    fdNrLinesDocHeader = new FormData();

    fdNrLinesDocHeader.left = new FormAttachment( wlNrLinesPerPage, margin );
    fdNrLinesDocHeader.top = new FormAttachment( wNrLinesPerPage, margin );
    fdNrLinesDocHeader.right = new FormAttachment( 100, 0 );
    wNrLinesDocHeader.setLayoutData( fdNrLinesDocHeader );

    // Compression type (None, Zip or GZip
    wlCompression = new Label( wContentComp, SWT.RIGHT );
    wlCompression.setText( BaseMessages.getString( PKG, "TextFileInputDialog.Compression.Label" ) );
    props.setLook( wlCompression );
    fdlCompression = new FormData();
    fdlCompression.left = new FormAttachment( 0, 0 );
    fdlCompression.top = new FormAttachment( wNrLinesDocHeader, margin );
    fdlCompression.right = new FormAttachment( middle, -margin );
    wlCompression.setLayoutData( fdlCompression );
    wCompression = new CCombo( wContentComp, SWT.BORDER | SWT.READ_ONLY );
    wCompression.setText( BaseMessages.getString( PKG, "TextFileInputDialog.Compression.Label" ) );
    wCompression.setToolTipText( BaseMessages.getString( PKG, "TextFileInputDialog.Compression.Tooltip" ) );
    props.setLook( wCompression );
    wCompression.setItems( CompressionProviderFactory.getInstance().getCompressionProviderNames() );

    wCompression.addModifyListener( lsMod );
    fdCompression = new FormData();
    fdCompression.left = new FormAttachment( middle, 0 );
    fdCompression.top = new FormAttachment( wNrLinesDocHeader, margin );
    fdCompression.right = new FormAttachment( 100, 0 );
    wCompression.setLayoutData( fdCompression );

    wlNoempty = new Label( wContentComp, SWT.RIGHT );
    wlNoempty.setText( BaseMessages.getString( PKG, "TextFileInputDialog.NoEmpty.Label" ) );
    props.setLook( wlNoempty );
    fdlNoempty = new FormData();
    fdlNoempty.left = new FormAttachment( 0, 0 );
    fdlNoempty.top = new FormAttachment( wCompression, margin );
    fdlNoempty.right = new FormAttachment( middle, -margin );
    wlNoempty.setLayoutData( fdlNoempty );
    wNoempty = new Button( wContentComp, SWT.CHECK );
    props.setLook( wNoempty );
    wNoempty.setToolTipText( BaseMessages.getString( PKG, "TextFileInputDialog.NoEmpty.Tooltip" ) );
    fdNoempty = new FormData();
    fdNoempty.left = new FormAttachment( middle, 0 );
    fdNoempty.top = new FormAttachment( wCompression, margin );
    fdNoempty.right = new FormAttachment( 100, 0 );
    wNoempty.setLayoutData( fdNoempty );

    wlInclFilename = new Label( wContentComp, SWT.RIGHT );
    wlInclFilename.setText( BaseMessages.getString( PKG, "TextFileInputDialog.InclFilename.Label" ) );
    props.setLook( wlInclFilename );
    fdlInclFilename = new FormData();
    fdlInclFilename.left = new FormAttachment( 0, 0 );
    fdlInclFilename.top = new FormAttachment( wNoempty, margin );
    fdlInclFilename.right = new FormAttachment( middle, -margin );
    wlInclFilename.setLayoutData( fdlInclFilename );
    wInclFilename = new Button( wContentComp, SWT.CHECK );
    props.setLook( wInclFilename );
    wInclFilename.setToolTipText( BaseMessages.getString( PKG, "TextFileInputDialog.InclFilename.Tooltip" ) );
    fdInclFilename = new FormData();
    fdInclFilename.left = new FormAttachment( middle, 0 );
    fdInclFilename.top = new FormAttachment( wNoempty, margin );
    wInclFilename.setLayoutData( fdInclFilename );

    wlInclFilenameField = new Label( wContentComp, SWT.LEFT );
    wlInclFilenameField.setText( BaseMessages.getString( PKG, "TextFileInputDialog.InclFilenameField.Label" ) );
    props.setLook( wlInclFilenameField );
    fdlInclFilenameField = new FormData();
    fdlInclFilenameField.left = new FormAttachment( wInclFilename, margin );
    fdlInclFilenameField.top = new FormAttachment( wNoempty, margin );
    wlInclFilenameField.setLayoutData( fdlInclFilenameField );
    wInclFilenameField = new Text( wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wInclFilenameField );
    wInclFilenameField.addModifyListener( lsMod );
    fdInclFilenameField = new FormData();
    fdInclFilenameField.left = new FormAttachment( wlInclFilenameField, margin );
    fdInclFilenameField.top = new FormAttachment( wNoempty, margin );
    fdInclFilenameField.right = new FormAttachment( 100, 0 );
    wInclFilenameField.setLayoutData( fdInclFilenameField );

    wlInclRownum = new Label( wContentComp, SWT.RIGHT );
    wlInclRownum.setText( BaseMessages.getString( PKG, "TextFileInputDialog.InclRownum.Label" ) );
    props.setLook( wlInclRownum );
    fdlInclRownum = new FormData();
    fdlInclRownum.left = new FormAttachment( 0, 0 );
    fdlInclRownum.top = new FormAttachment( wInclFilenameField, margin );
    fdlInclRownum.right = new FormAttachment( middle, -margin );
    wlInclRownum.setLayoutData( fdlInclRownum );
    wInclRownum = new Button( wContentComp, SWT.CHECK );
    props.setLook( wInclRownum );
    wInclRownum.setToolTipText( BaseMessages.getString( PKG, "TextFileInputDialog.InclRownum.Tooltip" ) );
    fdRownum = new FormData();
    fdRownum.left = new FormAttachment( middle, 0 );
    fdRownum.top = new FormAttachment( wInclFilenameField, margin );
    wInclRownum.setLayoutData( fdRownum );

    wlInclRownumField = new Label( wContentComp, SWT.RIGHT );
    wlInclRownumField.setText( BaseMessages.getString( PKG, "TextFileInputDialog.InclRownumField.Label" ) );
    props.setLook( wlInclRownumField );
    fdlInclRownumField = new FormData();
    fdlInclRownumField.left = new FormAttachment( wInclRownum, margin );
    fdlInclRownumField.top = new FormAttachment( wInclFilenameField, margin );
    wlInclRownumField.setLayoutData( fdlInclRownumField );
    wInclRownumField = new Text( wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wInclRownumField );
    wInclRownumField.addModifyListener( lsMod );
    fdInclRownumField = new FormData();
    fdInclRownumField.left = new FormAttachment( wlInclRownumField, margin );
    fdInclRownumField.top = new FormAttachment( wInclFilenameField, margin );
    fdInclRownumField.right = new FormAttachment( 100, 0 );
    wInclRownumField.setLayoutData( fdInclRownumField );

    wlRownumByFileField = new Label( wContentComp, SWT.RIGHT );
    wlRownumByFileField.setText( BaseMessages.getString( PKG, "TextFileInputDialog.RownumByFile.Label" ) );
    props.setLook( wlRownumByFileField );
    fdlRownumByFile = new FormData();
    fdlRownumByFile.left = new FormAttachment( wInclRownum, margin );
    fdlRownumByFile.top = new FormAttachment( wInclRownumField, margin );
    wlRownumByFileField.setLayoutData( fdlRownumByFile );
    wRownumByFile = new Button( wContentComp, SWT.CHECK );
    props.setLook( wRownumByFile );
    wRownumByFile.setToolTipText( BaseMessages.getString( PKG, "TextFileInputDialog.RownumByFile.Tooltip" ) );
    fdRownumByFile = new FormData();
    fdRownumByFile.left = new FormAttachment( wlRownumByFileField, margin );
    fdRownumByFile.top = new FormAttachment( wInclRownumField, margin );
    wRownumByFile.setLayoutData( fdRownumByFile );

    wlFormat = new Label( wContentComp, SWT.RIGHT );
    wlFormat.setText( BaseMessages.getString( PKG, "TextFileInputDialog.Format.Label" ) );
    props.setLook( wlFormat );
    fdlFormat = new FormData();
    fdlFormat.left = new FormAttachment( 0, 0 );
    fdlFormat.top = new FormAttachment( wRownumByFile, margin * 2 );
    fdlFormat.right = new FormAttachment( middle, -margin );
    wlFormat.setLayoutData( fdlFormat );
    wFormat = new CCombo( wContentComp, SWT.BORDER | SWT.READ_ONLY );
    wFormat.setText( BaseMessages.getString( PKG, "TextFileInputDialog.Format.Label" ) );
    props.setLook( wFormat );
    wFormat.add( "DOS" );
    wFormat.add( "Unix" );
    wFormat.add( "mixed" );
    wFormat.select( 0 );
    wFormat.addModifyListener( lsMod );
    fdFormat = new FormData();
    fdFormat.left = new FormAttachment( middle, 0 );
    fdFormat.top = new FormAttachment( wRownumByFile, margin * 2 );
    fdFormat.right = new FormAttachment( 100, 0 );
    wFormat.setLayoutData( fdFormat );

    wlEncoding = new Label( wContentComp, SWT.RIGHT );
    wlEncoding.setText( BaseMessages.getString( PKG, "TextFileInputDialog.Encoding.Label" ) );
    props.setLook( wlEncoding );
    fdlEncoding = new FormData();
    fdlEncoding.left = new FormAttachment( 0, 0 );
    fdlEncoding.top = new FormAttachment( wFormat, margin );
    fdlEncoding.right = new FormAttachment( middle, -margin );
    wlEncoding.setLayoutData( fdlEncoding );
    wEncoding = new CCombo( wContentComp, SWT.BORDER | SWT.READ_ONLY );
    wEncoding.setEditable( true );
    props.setLook( wEncoding );
    wEncoding.addModifyListener( lsMod );
    fdEncoding = new FormData();
    fdEncoding.left = new FormAttachment( middle, 0 );
    fdEncoding.top = new FormAttachment( wFormat, margin );
    fdEncoding.right = new FormAttachment( 100, 0 );
    wEncoding.setLayoutData( fdEncoding );
    wEncoding.addFocusListener( new FocusListener() {
      public void focusLost( org.eclipse.swt.events.FocusEvent e ) {
      }

      public void focusGained( org.eclipse.swt.events.FocusEvent e ) {
        Cursor busy = new Cursor( shell.getDisplay(), SWT.CURSOR_WAIT );
        shell.setCursor( busy );
        setEncodings();
        shell.setCursor( null );
        busy.dispose();
      }
    } );


    wlLength = new Label( wContentComp, SWT.RIGHT );
    wlLength.setText( BaseMessages.getString( PKG, "TextFileInputDialog.Length.Label" ) );
    props.setLook( wlLength );
    fdlLength = new FormData();
    fdlLength.left = new FormAttachment( 0, 0 );
    fdlLength.top = new FormAttachment( wEncoding, margin );
    fdlLength.right = new FormAttachment( middle, -margin );
    wlLength.setLayoutData( fdlLength );
    wLength = new CCombo( wContentComp, SWT.BORDER | SWT.READ_ONLY );
    wLength.setText( BaseMessages.getString( PKG, "TextFileInputDialog.Length.Label" ) );
    props.setLook( wLength );
    wLength.add( "Characters" );
    wLength.add( "Bytes" );
    wLength.select( 0 );
    wLength.addModifyListener( lsMod );
    fdLength = new FormData();
    fdLength.left = new FormAttachment( middle, 0 );
    fdLength.top = new FormAttachment( wEncoding, margin );
    fdLength.right = new FormAttachment( 100, 0 );
    wLength.setLayoutData( fdLength );

    wlLimit = new Label( wContentComp, SWT.RIGHT );
    wlLimit.setText( BaseMessages.getString( PKG, "TextFileInputDialog.Limit.Label" ) );
    props.setLook( wlLimit );
    fdlLimit = new FormData();
    fdlLimit.left = new FormAttachment( 0, 0 );
    fdlLimit.top = new FormAttachment( wLength, margin );
    fdlLimit.right = new FormAttachment( middle, -margin );
    wlLimit.setLayoutData( fdlLimit );
    wLimit = new Text( wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wLimit );
    wLimit.addModifyListener( lsMod );
    fdLimit = new FormData();
    fdLimit.left = new FormAttachment( middle, 0 );
    fdLimit.top = new FormAttachment( wLength, margin );
    fdLimit.right = new FormAttachment( 100, 0 );
    wLimit.setLayoutData( fdLimit );

    // Date Lenient checkbox
    wlDateLenient = new Label( wContentComp, SWT.RIGHT );
    wlDateLenient.setText( BaseMessages.getString( PKG, "TextFileInputDialog.DateLenient.Label" ) );
    props.setLook( wlDateLenient );
    fdlDateLenient = new FormData();
    fdlDateLenient.left = new FormAttachment( 0, 0 );
    fdlDateLenient.top = new FormAttachment( wLimit, margin );
    fdlDateLenient.right = new FormAttachment( middle, -margin );
    wlDateLenient.setLayoutData( fdlDateLenient );
    wDateLenient = new Button( wContentComp, SWT.CHECK );
    wDateLenient.setToolTipText( BaseMessages.getString( PKG, "TextFileInputDialog.DateLenient.Tooltip" ) );
    props.setLook( wDateLenient );
    fdDateLenient = new FormData();
    fdDateLenient.left = new FormAttachment( middle, 0 );
    fdDateLenient.top = new FormAttachment( wLimit, margin );
    wDateLenient.setLayoutData( fdDateLenient );

    wlDateLocale = new Label( wContentComp, SWT.RIGHT );
    wlDateLocale.setText( BaseMessages.getString( PKG, "TextFileInputDialog.DateLocale.Label" ) );
    props.setLook( wlDateLocale );
    fdlDateLocale = new FormData();
    fdlDateLocale.left = new FormAttachment( 0, 0 );
    fdlDateLocale.top = new FormAttachment( wDateLenient, margin );
    fdlDateLocale.right = new FormAttachment( middle, -margin );
    wlDateLocale.setLayoutData( fdlDateLocale );
    wDateLocale = new CCombo( wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wDateLocale.setToolTipText( BaseMessages.getString( PKG, "TextFileInputDialog.DateLocale.Tooltip" ) );
    props.setLook( wDateLocale );
    wDateLocale.addModifyListener( lsMod );
    fdDateLocale = new FormData();
    fdDateLocale.left = new FormAttachment( middle, 0 );
    fdDateLocale.top = new FormAttachment( wDateLenient, margin );
    fdDateLocale.right = new FormAttachment( 100, 0 );
    wDateLocale.setLayoutData( fdDateLocale );
    wDateLocale.addFocusListener( new FocusListener() {
      public void focusLost( org.eclipse.swt.events.FocusEvent e ) {
      }

      public void focusGained( org.eclipse.swt.events.FocusEvent e ) {
        Cursor busy = new Cursor( shell.getDisplay(), SWT.CURSOR_WAIT );
        shell.setCursor( busy );
        setLocales();
        shell.setCursor( null );
        busy.dispose();
      }
    } );

    // ///////////////////////////////
    // START OF AddFileResult GROUP //
    // ///////////////////////////////

    wAddFileResult = new Group( wContentComp, SWT.SHADOW_NONE );
    props.setLook( wAddFileResult );
    wAddFileResult.setText( BaseMessages.getString( PKG, "TextFileInputDialog.wAddFileResult.Label" ) );

    FormLayout AddFileResultgroupLayout = new FormLayout();
    AddFileResultgroupLayout.marginWidth = 10;
    AddFileResultgroupLayout.marginHeight = 10;
    wAddFileResult.setLayout( AddFileResultgroupLayout );

    wlAddResult = new Label( wAddFileResult, SWT.RIGHT );
    wlAddResult.setText( BaseMessages.getString( PKG, "TextFileInputDialog.AddResult.Label" ) );
    props.setLook( wlAddResult );
    fdlAddResult = new FormData();
    fdlAddResult.left = new FormAttachment( 0, 0 );
    fdlAddResult.top = new FormAttachment( wDateLocale, margin );
    fdlAddResult.right = new FormAttachment( middle, -margin );
    wlAddResult.setLayoutData( fdlAddResult );
    wAddResult = new Button( wAddFileResult, SWT.CHECK );
    props.setLook( wAddResult );
    wAddResult.setToolTipText( BaseMessages.getString( PKG, "TextFileInputDialog.AddResult.Tooltip" ) );
    fdAddResult = new FormData();
    fdAddResult.left = new FormAttachment( middle, 0 );
    fdAddResult.top = new FormAttachment( wDateLocale, margin );
    wAddResult.setLayoutData( fdAddResult );

    fdAddFileResult = new FormData();
    fdAddFileResult.left = new FormAttachment( 0, margin );
    fdAddFileResult.top = new FormAttachment( wDateLocale, margin );
    fdAddFileResult.right = new FormAttachment( 100, -margin );
    wAddFileResult.setLayoutData( fdAddFileResult );

    // ///////////////////////////////////////////////////////////
    // / END OF AddFileResult GROUP
    // ///////////////////////////////////////////////////////////

    wContentComp.pack();
    // What's the size:
    Rectangle bounds = wContentComp.getBounds();

    wContentSComp.setContent( wContentComp );
    wContentSComp.setExpandHorizontal( true );
    wContentSComp.setExpandVertical( true );
    wContentSComp.setMinWidth( bounds.width );
    wContentSComp.setMinHeight( bounds.height );

    fdContentComp = new FormData();
    fdContentComp.left = new FormAttachment( 0, 0 );
    fdContentComp.top = new FormAttachment( 0, 0 );
    fdContentComp.right = new FormAttachment( 100, 0 );
    fdContentComp.bottom = new FormAttachment( 100, 0 );
    wContentComp.setLayoutData( fdContentComp );

    wContentTab.setControl( wContentSComp );

    // ///////////////////////////////////////////////////////////
    // / END OF CONTENT TAB
    // ///////////////////////////////////////////////////////////

  }

  protected void setLocales() {
    Locale[] locale = Locale.getAvailableLocales();
    dateLocale = new String[locale.length];
    for ( int i = 0; i < locale.length; i++ ) {
      dateLocale[i] = locale[i].toString();
    }
    if ( dateLocale != null ) {
      wDateLocale.setItems( dateLocale );
    }
  }

  private void addErrorTab() {
    // ////////////////////////
    // START OF ERROR TAB ///
    // /
    wErrorTab = new CTabItem( wTabFolder, SWT.NONE );
    wErrorTab.setText( BaseMessages.getString( PKG, "TextFileInputDialog.ErrorTab.TabTitle" ) );

    wErrorSComp = new ScrolledComposite( wTabFolder, SWT.V_SCROLL | SWT.H_SCROLL );
    wErrorSComp.setLayout( new FillLayout() );

    FormLayout errorLayout = new FormLayout();
    errorLayout.marginWidth = 3;
    errorLayout.marginHeight = 3;

    wErrorComp = new Composite( wErrorSComp, SWT.NONE );
    props.setLook( wErrorComp );
    wErrorComp.setLayout( errorLayout );

    // ERROR HANDLING...
    // ErrorIgnored?
    wlErrorIgnored = new Label( wErrorComp, SWT.RIGHT );
    wlErrorIgnored.setText( BaseMessages.getString( PKG, "TextFileInputDialog.ErrorIgnored.Label" ) );
    props.setLook( wlErrorIgnored );
    fdlErrorIgnored = new FormData();
    fdlErrorIgnored.left = new FormAttachment( 0, 0 );
    fdlErrorIgnored.top = new FormAttachment( 0, margin );
    fdlErrorIgnored.right = new FormAttachment( middle, -margin );
    wlErrorIgnored.setLayoutData( fdlErrorIgnored );
    wErrorIgnored = new Button( wErrorComp, SWT.CHECK );
    props.setLook( wErrorIgnored );
    wErrorIgnored.setToolTipText( BaseMessages.getString( PKG, "TextFileInputDialog.ErrorIgnored.Tooltip" ) );
    fdErrorIgnored = new FormData();
    fdErrorIgnored.left = new FormAttachment( middle, 0 );
    fdErrorIgnored.top = new FormAttachment( 0, margin );
    wErrorIgnored.setLayoutData( fdErrorIgnored );

    // Skip bad files?
    wlSkipBadFiles = new Label( wErrorComp, SWT.RIGHT );
    wlSkipBadFiles.setText( BaseMessages.getString( PKG, "TextFileInputDialog.SkipBadFiles.Label" ) );
    props.setLook( wlSkipBadFiles );
    fdlSkipBadFiles = new FormData();
    fdlSkipBadFiles.left = new FormAttachment( 0, 0 );
    fdlSkipBadFiles.top = new FormAttachment( wErrorIgnored, margin );
    fdlSkipBadFiles.right = new FormAttachment( middle, -margin );
    wlSkipBadFiles.setLayoutData( fdlSkipBadFiles );
    wSkipBadFiles = new Button( wErrorComp, SWT.CHECK );
    props.setLook( wSkipBadFiles );
    wSkipBadFiles.setToolTipText( BaseMessages.getString( PKG, "TextFileInputDialog.SkipBadFiles.Tooltip" ) );
    fdSkipBadFiles = new FormData();
    fdSkipBadFiles.left = new FormAttachment( middle, 0 );
    fdSkipBadFiles.top = new FormAttachment( wErrorIgnored, margin );
    wSkipBadFiles.setLayoutData( fdSkipBadFiles );

    // field for rejected file
    wlBadFileField = new Label( wErrorComp, SWT.RIGHT );
    wlBadFileField.setText( BaseMessages.getString( PKG, "TextFileInputDialog.BadFileField.Label" ) );
    props.setLook( wlBadFileField );
    fdlBadFileField = new FormData();
    fdlBadFileField.left = new FormAttachment( 0, 0 );
    fdlBadFileField.top = new FormAttachment( wSkipBadFiles, margin );
    fdlBadFileField.right = new FormAttachment( middle, -margin );
    wlBadFileField.setLayoutData( fdlBadFileField );
    wBadFileField = new Text( wErrorComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wBadFileField );
    wBadFileField.addModifyListener( lsMod );
    fdBadFileField = new FormData();
    fdBadFileField.left = new FormAttachment( middle, 0 );
    fdBadFileField.top = new FormAttachment( wSkipBadFiles, margin );
    fdBadFileField.right = new FormAttachment( 100, 0 );
    wBadFileField.setLayoutData( fdBadFileField );

    // field for file error messsage
    wlBadFileMessageField = new Label( wErrorComp, SWT.RIGHT );
    wlBadFileMessageField.setText( BaseMessages.getString( PKG, "TextFileInputDialog.BadFileMessageField.Label" ) );
    props.setLook( wlBadFileMessageField );
    fdlBadFileMessageField = new FormData();
    fdlBadFileMessageField.left = new FormAttachment( 0, 0 );
    fdlBadFileMessageField.top = new FormAttachment( wBadFileField, margin );
    fdlBadFileMessageField.right = new FormAttachment( middle, -margin );
    wlBadFileMessageField.setLayoutData( fdlBadFileMessageField );
    wBadFileMessageField = new Text( wErrorComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wBadFileMessageField );
    wBadFileMessageField.addModifyListener( lsMod );
    fdBadFileMessageField = new FormData();
    fdBadFileMessageField.left = new FormAttachment( middle, 0 );
    fdBadFileMessageField.top = new FormAttachment( wBadFileField, margin );
    fdBadFileMessageField.right = new FormAttachment( 100, 0 );
    wBadFileMessageField.setLayoutData( fdBadFileMessageField );

    // Skip error lines?
    wlSkipErrorLines = new Label( wErrorComp, SWT.RIGHT );
    wlSkipErrorLines.setText( BaseMessages.getString( PKG, "TextFileInputDialog.SkipErrorLines.Label" ) );
    props.setLook( wlSkipErrorLines );
    fdlSkipErrorLines = new FormData();
    fdlSkipErrorLines.left = new FormAttachment( 0, 0 );
    fdlSkipErrorLines.top = new FormAttachment( wBadFileMessageField, margin );
    fdlSkipErrorLines.right = new FormAttachment( middle, -margin );
    wlSkipErrorLines.setLayoutData( fdlSkipErrorLines );
    wSkipErrorLines = new Button( wErrorComp, SWT.CHECK );
    props.setLook( wSkipErrorLines );
    wSkipErrorLines.setToolTipText( BaseMessages.getString( PKG, "TextFileInputDialog.SkipErrorLines.Tooltip" ) );
    fdSkipErrorLines = new FormData();
    fdSkipErrorLines.left = new FormAttachment( middle, 0 );
    fdSkipErrorLines.top = new FormAttachment( wBadFileMessageField, margin );
    wSkipErrorLines.setLayoutData( fdSkipErrorLines );

    wlErrorCount = new Label( wErrorComp, SWT.RIGHT );
    wlErrorCount.setText( BaseMessages.getString( PKG, "TextFileInputDialog.ErrorCount.Label" ) );
    props.setLook( wlErrorCount );
    fdlErrorCount = new FormData();
    fdlErrorCount.left = new FormAttachment( 0, 0 );
    fdlErrorCount.top = new FormAttachment( wSkipErrorLines, margin );
    fdlErrorCount.right = new FormAttachment( middle, -margin );
    wlErrorCount.setLayoutData( fdlErrorCount );
    wErrorCount = new Text( wErrorComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wErrorCount );
    wErrorCount.addModifyListener( lsMod );
    fdErrorCount = new FormData();
    fdErrorCount.left = new FormAttachment( middle, 0 );
    fdErrorCount.top = new FormAttachment( wSkipErrorLines, margin );
    fdErrorCount.right = new FormAttachment( 100, 0 );
    wErrorCount.setLayoutData( fdErrorCount );

    wlErrorFields = new Label( wErrorComp, SWT.RIGHT );
    wlErrorFields.setText( BaseMessages.getString( PKG, "TextFileInputDialog.ErrorFields.Label" ) );
    props.setLook( wlErrorFields );
    fdlErrorFields = new FormData();
    fdlErrorFields.left = new FormAttachment( 0, 0 );
    fdlErrorFields.top = new FormAttachment( wErrorCount, margin );
    fdlErrorFields.right = new FormAttachment( middle, -margin );
    wlErrorFields.setLayoutData( fdlErrorFields );
    wErrorFields = new Text( wErrorComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wErrorFields );
    wErrorFields.addModifyListener( lsMod );
    fdErrorFields = new FormData();
    fdErrorFields.left = new FormAttachment( middle, 0 );
    fdErrorFields.top = new FormAttachment( wErrorCount, margin );
    fdErrorFields.right = new FormAttachment( 100, 0 );
    wErrorFields.setLayoutData( fdErrorFields );

    wlErrorText = new Label( wErrorComp, SWT.RIGHT );
    wlErrorText.setText( BaseMessages.getString( PKG, "TextFileInputDialog.ErrorText.Label" ) );
    props.setLook( wlErrorText );
    fdlErrorText = new FormData();
    fdlErrorText.left = new FormAttachment( 0, 0 );
    fdlErrorText.top = new FormAttachment( wErrorFields, margin );
    fdlErrorText.right = new FormAttachment( middle, -margin );
    wlErrorText.setLayoutData( fdlErrorText );
    wErrorText = new Text( wErrorComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wErrorText );
    wErrorText.addModifyListener( lsMod );
    fdErrorText = new FormData();
    fdErrorText.left = new FormAttachment( middle, 0 );
    fdErrorText.top = new FormAttachment( wErrorFields, margin );
    fdErrorText.right = new FormAttachment( 100, 0 );
    wErrorText.setLayoutData( fdErrorText );

    // Bad lines files directory + extension
    Control previous = wErrorText;

    // BadDestDir line
    wlWarnDestDir = new Label( wErrorComp, SWT.RIGHT );
    wlWarnDestDir.setText( BaseMessages.getString( PKG, "TextFileInputDialog.WarnDestDir.Label" ) );
    props.setLook( wlWarnDestDir );
    fdlWarnDestDir = new FormData();
    fdlWarnDestDir.left = new FormAttachment( 0, 0 );
    fdlWarnDestDir.top = new FormAttachment( previous, margin * 4 );
    fdlWarnDestDir.right = new FormAttachment( middle, -margin );
    wlWarnDestDir.setLayoutData( fdlWarnDestDir );

    wbbWarnDestDir = new Button( wErrorComp, SWT.PUSH | SWT.CENTER );
    props.setLook( wbbWarnDestDir );
    wbbWarnDestDir.setText( BaseMessages.getString( PKG, "System.Button.Browse" ) );
    wbbWarnDestDir.setToolTipText( BaseMessages.getString( PKG, "System.Tooltip.BrowseForDir" ) );
    fdbBadDestDir = new FormData();
    fdbBadDestDir.right = new FormAttachment( 100, 0 );
    fdbBadDestDir.top = new FormAttachment( previous, margin * 4 );
    wbbWarnDestDir.setLayoutData( fdbBadDestDir );

    wWarnExt = new Text( wErrorComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wWarnExt );
    wWarnExt.addModifyListener( lsMod );
    fdWarnDestExt = new FormData();
    fdWarnDestExt.left = new FormAttachment( wbbWarnDestDir, -150 );
    fdWarnDestExt.right = new FormAttachment( wbbWarnDestDir, -margin );
    fdWarnDestExt.top = new FormAttachment( previous, margin * 4 );
    wWarnExt.setLayoutData( fdWarnDestExt );

    wlWarnExt = new Label( wErrorComp, SWT.RIGHT );
    wlWarnExt.setText( BaseMessages.getString( PKG, "System.Label.Extension" ) );
    props.setLook( wlWarnExt );
    fdlWarnDestExt = new FormData();
    fdlWarnDestExt.top = new FormAttachment( previous, margin * 4 );
    fdlWarnDestExt.right = new FormAttachment( wWarnExt, -margin );
    wlWarnExt.setLayoutData( fdlWarnDestExt );

    wWarnDestDir = new TextVar( transMeta, wErrorComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wWarnDestDir );
    wWarnDestDir.addModifyListener( lsMod );
    fdBadDestDir = new FormData();
    fdBadDestDir.left = new FormAttachment( middle, 0 );
    fdBadDestDir.right = new FormAttachment( wlWarnExt, -margin );
    fdBadDestDir.top = new FormAttachment( previous, margin * 4 );
    wWarnDestDir.setLayoutData( fdBadDestDir );

    // Listen to the Browse... button
    wbbWarnDestDir.addSelectionListener( DirectoryDialogButtonListenerFactory.getSelectionAdapter( shell, wWarnDestDir
        .getTextWidget() ) );

    // Whenever something changes, set the tooltip to the expanded version of the directory:
    wWarnDestDir.addModifyListener( getModifyListenerTooltipText( wWarnDestDir.getTextWidget() ) );

    // Error lines files directory + extension
    previous = wWarnDestDir;

    // ErrorDestDir line
    wlErrorDestDir = new Label( wErrorComp, SWT.RIGHT );
    wlErrorDestDir.setText( BaseMessages.getString( PKG, "TextFileInputDialog.ErrorDestDir.Label" ) );
    props.setLook( wlErrorDestDir );
    fdlErrorDestDir = new FormData();
    fdlErrorDestDir.left = new FormAttachment( 0, 0 );
    fdlErrorDestDir.top = new FormAttachment( previous, margin );
    fdlErrorDestDir.right = new FormAttachment( middle, -margin );
    wlErrorDestDir.setLayoutData( fdlErrorDestDir );

    wbbErrorDestDir = new Button( wErrorComp, SWT.PUSH | SWT.CENTER );
    props.setLook( wbbErrorDestDir );
    wbbErrorDestDir.setText( BaseMessages.getString( PKG, "System.Button.Browse" ) );
    wbbErrorDestDir.setToolTipText( BaseMessages.getString( PKG, "System.Tooltip.BrowseForDir" ) );
    fdbErrorDestDir = new FormData();
    fdbErrorDestDir.right = new FormAttachment( 100, 0 );
    fdbErrorDestDir.top = new FormAttachment( previous, margin );
    wbbErrorDestDir.setLayoutData( fdbErrorDestDir );

    wErrorExt = new Text( wErrorComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wErrorExt );
    wErrorExt.addModifyListener( lsMod );
    fdErrorDestExt = new FormData();
    fdErrorDestExt.left = new FormAttachment( wWarnExt, 0, SWT.LEFT );
    fdErrorDestExt.right = new FormAttachment( wWarnExt, 0, SWT.RIGHT );
    fdErrorDestExt.top = new FormAttachment( previous, margin );
    wErrorExt.setLayoutData( fdErrorDestExt );

    wlErrorExt = new Label( wErrorComp, SWT.RIGHT );
    wlErrorExt.setText( BaseMessages.getString( PKG, "System.Label.Extension" ) );
    props.setLook( wlErrorExt );
    fdlErrorDestExt = new FormData();
    fdlErrorDestExt.top = new FormAttachment( previous, margin );
    fdlErrorDestExt.right = new FormAttachment( wErrorExt, -margin );
    wlErrorExt.setLayoutData( fdlErrorDestExt );

    wErrorDestDir = new TextVar( transMeta, wErrorComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wErrorDestDir );
    wErrorDestDir.addModifyListener( lsMod );
    fdErrorDestDir = new FormData();
    fdErrorDestDir.left = new FormAttachment( middle, 0 );
    fdErrorDestDir.right = new FormAttachment( wlErrorExt, -margin );
    fdErrorDestDir.top = new FormAttachment( previous, margin );
    wErrorDestDir.setLayoutData( fdErrorDestDir );

    // Listen to the Browse... button
    wbbErrorDestDir.addSelectionListener( DirectoryDialogButtonListenerFactory.getSelectionAdapter( shell, wErrorDestDir
        .getTextWidget() ) );

    // Whenever something changes, set the tooltip to the expanded version of the directory:
    wErrorDestDir.addModifyListener( getModifyListenerTooltipText( wErrorDestDir.getTextWidget() ) );

    // Data Error lines files directory + extension
    previous = wErrorDestDir;

    // LineNrDestDir line
    wlLineNrDestDir = new Label( wErrorComp, SWT.RIGHT );
    wlLineNrDestDir.setText( BaseMessages.getString( PKG, "TextFileInputDialog.LineNrDestDir.Label" ) );
    props.setLook( wlLineNrDestDir );
    fdlLineNrDestDir = new FormData();
    fdlLineNrDestDir.left = new FormAttachment( 0, 0 );
    fdlLineNrDestDir.top = new FormAttachment( previous, margin );
    fdlLineNrDestDir.right = new FormAttachment( middle, -margin );
    wlLineNrDestDir.setLayoutData( fdlLineNrDestDir );

    wbbLineNrDestDir = new Button( wErrorComp, SWT.PUSH | SWT.CENTER );
    props.setLook( wbbLineNrDestDir );
    wbbLineNrDestDir.setText( BaseMessages.getString( PKG, "System.Button.Browse" ) );
    wbbLineNrDestDir.setToolTipText( BaseMessages.getString( PKG, "System.Tooltip.Browse" ) );
    fdbLineNrDestDir = new FormData();
    fdbLineNrDestDir.right = new FormAttachment( 100, 0 );
    fdbLineNrDestDir.top = new FormAttachment( previous, margin );
    wbbLineNrDestDir.setLayoutData( fdbLineNrDestDir );

    wLineNrExt = new Text( wErrorComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wLineNrExt );
    wLineNrExt.addModifyListener( lsMod );
    fdLineNrDestExt = new FormData();
    fdLineNrDestExt.left = new FormAttachment( wErrorExt, 0, SWT.LEFT );
    fdLineNrDestExt.right = new FormAttachment( wErrorExt, 0, SWT.RIGHT );
    fdLineNrDestExt.top = new FormAttachment( previous, margin );
    wLineNrExt.setLayoutData( fdLineNrDestExt );

    wlLineNrExt = new Label( wErrorComp, SWT.RIGHT );
    wlLineNrExt.setText( BaseMessages.getString( PKG, "System.Label.Extension" ) );
    props.setLook( wlLineNrExt );
    fdlLineNrDestExt = new FormData();
    fdlLineNrDestExt.top = new FormAttachment( previous, margin );
    fdlLineNrDestExt.right = new FormAttachment( wLineNrExt, -margin );
    wlLineNrExt.setLayoutData( fdlLineNrDestExt );

    wLineNrDestDir = new TextVar( transMeta, wErrorComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wLineNrDestDir );
    wLineNrDestDir.addModifyListener( lsMod );
    fdLineNrDestDir = new FormData();
    fdLineNrDestDir.left = new FormAttachment( middle, 0 );
    fdLineNrDestDir.right = new FormAttachment( wlLineNrExt, -margin );
    fdLineNrDestDir.top = new FormAttachment( previous, margin );
    wLineNrDestDir.setLayoutData( fdLineNrDestDir );

    // Listen to the Browse... button
    wbbLineNrDestDir.addSelectionListener( DirectoryDialogButtonListenerFactory.getSelectionAdapter( shell,
        wLineNrDestDir.getTextWidget() ) );

    // Whenever something changes, set the tooltip to the expanded version of the directory:
    wLineNrDestDir.addModifyListener( getModifyListenerTooltipText( wLineNrDestDir.getTextWidget() ) );

    fdErrorComp = new FormData();
    fdErrorComp.left = new FormAttachment( 0, 0 );
    fdErrorComp.top = new FormAttachment( 0, 0 );
    fdErrorComp.right = new FormAttachment( 100, 0 );
    fdErrorComp.bottom = new FormAttachment( 100, 0 );
    wErrorComp.setLayoutData( fdErrorComp );

    wErrorComp.pack();
    // What's the size:
    Rectangle bounds = wErrorComp.getBounds();

    wErrorSComp.setContent( wErrorComp );
    wErrorSComp.setExpandHorizontal( true );
    wErrorSComp.setExpandVertical( true );
    wErrorSComp.setMinWidth( bounds.width );
    wErrorSComp.setMinHeight( bounds.height );

    wErrorTab.setControl( wErrorSComp );

    // ///////////////////////////////////////////////////////////
    // / END OF CONTENT TAB
    // ///////////////////////////////////////////////////////////

  }

  private void addFiltersTabs() {
    // Filters tab...
    //
    wFilterTab = new CTabItem( wTabFolder, SWT.NONE );
    wFilterTab.setText( BaseMessages.getString( PKG, "TextFileInputDialog.FilterTab.TabTitle" ) );

    FormLayout FilterLayout = new FormLayout();
    FilterLayout.marginWidth = Const.FORM_MARGIN;
    FilterLayout.marginHeight = Const.FORM_MARGIN;

    wFilterComp = new Composite( wTabFolder, SWT.NONE );
    wFilterComp.setLayout( FilterLayout );
    props.setLook( wFilterComp );

    final int FilterRows = input.getFilter().length;

    ColumnInfo[] colinf =
        new ColumnInfo[] { new ColumnInfo( BaseMessages.getString( PKG,
            "TextFileInputDialog.FilterStringColumn.Column" ), ColumnInfo.COLUMN_TYPE_TEXT, false ), new ColumnInfo(
                BaseMessages.getString( PKG, "TextFileInputDialog.FilterPositionColumn.Column" ),
                ColumnInfo.COLUMN_TYPE_TEXT, false ), new ColumnInfo( BaseMessages.getString( PKG,
                    "TextFileInputDialog.StopOnFilterColumn.Column" ), ColumnInfo.COLUMN_TYPE_CCOMBO, YES_NO_COMBO ),
          new ColumnInfo( BaseMessages.getString( PKG, "TextFileInputDialog.FilterPositiveColumn.Column" ),
              ColumnInfo.COLUMN_TYPE_CCOMBO, YES_NO_COMBO ) };

    colinf[2].setToolTip( BaseMessages.getString( PKG, "TextFileInputDialog.StopOnFilterColumn.Tooltip" ) );
    colinf[3].setToolTip( BaseMessages.getString( PKG, "TextFileInputDialog.FilterPositiveColumn.Tooltip" ) );

    wFilter = new TableView( transMeta, wFilterComp, SWT.FULL_SELECTION | SWT.MULTI, colinf, FilterRows, lsMod, props );

    fdFilter = new FormData();
    fdFilter.left = new FormAttachment( 0, 0 );
    fdFilter.top = new FormAttachment( 0, 0 );
    fdFilter.right = new FormAttachment( 100, 0 );
    fdFilter.bottom = new FormAttachment( 100, 0 );
    wFilter.setLayoutData( fdFilter );

    fdFilterComp = new FormData();
    fdFilterComp.left = new FormAttachment( 0, 0 );
    fdFilterComp.top = new FormAttachment( 0, 0 );
    fdFilterComp.right = new FormAttachment( 100, 0 );
    fdFilterComp.bottom = new FormAttachment( 100, 0 );
    wFilterComp.setLayoutData( fdFilterComp );

    wFilterComp.layout();
    wFilterTab.setControl( wFilterComp );
  }

  private void addFieldsTabs() {
    // Fields tab...
    //
    wFieldsTab = new CTabItem( wTabFolder, SWT.NONE );
    wFieldsTab.setText( BaseMessages.getString( PKG, "TextFileInputDialog.FieldsTab.TabTitle" ) );

    FormLayout fieldsLayout = new FormLayout();
    fieldsLayout.marginWidth = Const.FORM_MARGIN;
    fieldsLayout.marginHeight = Const.FORM_MARGIN;

    wFieldsComp = new Composite( wTabFolder, SWT.NONE );
    wFieldsComp.setLayout( fieldsLayout );
    props.setLook( wFieldsComp );

    wGet = new Button( wFieldsComp, SWT.PUSH );
    wGet.setText( BaseMessages.getString( PKG, "System.Button.GetFields" ) );
    fdGet = new FormData();
    fdGet.left = new FormAttachment( 50, 0 );
    fdGet.bottom = new FormAttachment( 100, 0 );
    wGet.setLayoutData( fdGet );

    wMinWidth = new Button( wFieldsComp, SWT.PUSH );
    wMinWidth.setText( BaseMessages.getString( PKG, "TextFileInputDialog.MinWidth.Button" ) );
    wMinWidth.setToolTipText( BaseMessages.getString( PKG, "TextFileInputDialog.MinWidth.Tooltip" ) );
    wMinWidth.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
      }
    } );
    setButtonPositions( new Button[] { wGet, wMinWidth }, margin, null );

    final int FieldsRows = input.inputFields.length;

    ColumnInfo[] colinf =
        new ColumnInfo[] { new ColumnInfo( BaseMessages.getString( PKG, "TextFileInputDialog.NameColumn.Column" ),
            ColumnInfo.COLUMN_TYPE_TEXT, false ), new ColumnInfo( BaseMessages.getString( PKG,
                "TextFileInputDialog.TypeColumn.Column" ), ColumnInfo.COLUMN_TYPE_CCOMBO, ValueMetaFactory.getValueMetaNames(), true ),
          new ColumnInfo( BaseMessages.getString( PKG, "TextFileInputDialog.FormatColumn.Column" ),
              ColumnInfo.COLUMN_TYPE_FORMAT, 2 ), new ColumnInfo( BaseMessages.getString( PKG,
                  "TextFileInputDialog.PositionColumn.Column" ), ColumnInfo.COLUMN_TYPE_TEXT, false ), new ColumnInfo(
                      BaseMessages.getString( PKG, "TextFileInputDialog.LengthColumn.Column" ),
                      ColumnInfo.COLUMN_TYPE_TEXT, false ), new ColumnInfo( BaseMessages.getString( PKG,
                          "TextFileInputDialog.PrecisionColumn.Column" ), ColumnInfo.COLUMN_TYPE_TEXT, false ),
          new ColumnInfo( BaseMessages.getString( PKG, "TextFileInputDialog.CurrencyColumn.Column" ),
              ColumnInfo.COLUMN_TYPE_TEXT, false ), new ColumnInfo( BaseMessages.getString( PKG,
                  "TextFileInputDialog.DecimalColumn.Column" ), ColumnInfo.COLUMN_TYPE_TEXT, false ), new ColumnInfo(
                      BaseMessages.getString( PKG, "TextFileInputDialog.GroupColumn.Column" ),
                      ColumnInfo.COLUMN_TYPE_TEXT, false ), new ColumnInfo( BaseMessages.getString( PKG,
                          "TextFileInputDialog.NullIfColumn.Column" ), ColumnInfo.COLUMN_TYPE_TEXT, false ),
          new ColumnInfo( BaseMessages.getString( PKG, "TextFileInputDialog.IfNullColumn.Column" ),
              ColumnInfo.COLUMN_TYPE_TEXT, false ), new ColumnInfo( BaseMessages.getString( PKG,
                  "TextFileInputDialog.TrimTypeColumn.Column" ), ColumnInfo.COLUMN_TYPE_CCOMBO, ValueMetaString.trimTypeDesc,
                  true ), new ColumnInfo( BaseMessages.getString( PKG, "TextFileInputDialog.RepeatColumn.Column" ),
                      ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { BaseMessages.getString( PKG, "System.Combo.Yes" ),
                        BaseMessages.getString( PKG, "System.Combo.No" ) }, true ) };

    colinf[12].setToolTip( BaseMessages.getString( PKG, "TextFileInputDialog.RepeatColumn.Tooltip" ) );

    wFields = new TableView( transMeta, wFieldsComp, SWT.FULL_SELECTION | SWT.MULTI, colinf, FieldsRows, lsMod, props );

    fdFields = new FormData();
    fdFields.left = new FormAttachment( 0, 0 );
    fdFields.top = new FormAttachment( 0, 0 );
    fdFields.right = new FormAttachment( 100, 0 );
    fdFields.bottom = new FormAttachment( wGet, -margin );
    wFields.setLayoutData( fdFields );

    fdFieldsComp = new FormData();
    fdFieldsComp.left = new FormAttachment( 0, 0 );
    fdFieldsComp.top = new FormAttachment( 0, 0 );
    fdFieldsComp.right = new FormAttachment( 100, 0 );
    fdFieldsComp.bottom = new FormAttachment( 100, 0 );
    wFieldsComp.setLayoutData( fdFieldsComp );

    wFieldsComp.layout();
    wFieldsTab.setControl( wFieldsComp );
  }

  public void setFlags() {
    boolean accept = wAccFilenames.getSelection();
    wlPassThruFields.setEnabled( accept );
    wPassThruFields.setEnabled( accept );
    if ( !wAccFilenames.getSelection() ) {
      wPassThruFields.setSelection( false );
    }
    wlAccField.setEnabled( accept );
    wAccField.setEnabled( accept );
    wlAccStep.setEnabled( accept );
    wAccStep.setEnabled( accept );

    wlFilename.setEnabled( !accept );
    wbbFilename.setEnabled( !accept ); // Browse: add file or directory
    wbdFilename.setEnabled( !accept ); // Delete
    wbeFilename.setEnabled( !accept ); // Edit
    wbaFilename.setEnabled( !accept ); // Add or change
    wFilename.setEnabled( !accept );
    wlFilenameList.setEnabled( !accept );
    wFilenameList.setEnabled( !accept );
    wlFilemask.setEnabled( !accept );
    wFilemask.setEnabled( !accept );
    wbShowFiles.setEnabled( !accept );

    // Keep this one active: use the sample in the file list
    // wPreview.setEnabled(!accept);

    wFirst.setEnabled( !accept );
    wFirstHeader.setEnabled( !accept );

    wlInclFilenameField.setEnabled( wInclFilename.getSelection() );
    wInclFilenameField.setEnabled( wInclFilename.getSelection() );

    wlInclRownumField.setEnabled( wInclRownum.getSelection() );
    wInclRownumField.setEnabled( wInclRownum.getSelection() );
    wlRownumByFileField.setEnabled( wInclRownum.getSelection() );
    wRownumByFile.setEnabled( wInclRownum.getSelection() );

    // Error handling tab...
    wlSkipErrorLines.setEnabled( wErrorIgnored.getSelection() );
    wSkipBadFiles.setEnabled( wErrorIgnored.getSelection() );
    wBadFileField.setEnabled( wErrorIgnored.getSelection() && wSkipBadFiles.getSelection() );
    wBadFileMessageField.setEnabled( wErrorIgnored.getSelection() && wSkipBadFiles.getSelection() );
    wSkipErrorLines.setEnabled( wErrorIgnored.getSelection() );
    wlErrorCount.setEnabled( wErrorIgnored.getSelection() );
    wErrorCount.setEnabled( wErrorIgnored.getSelection() );
    wlErrorFields.setEnabled( wErrorIgnored.getSelection() );
    wErrorFields.setEnabled( wErrorIgnored.getSelection() );
    wlErrorText.setEnabled( wErrorIgnored.getSelection() );
    wErrorText.setEnabled( wErrorIgnored.getSelection() );

    wlWarnDestDir.setEnabled( wErrorIgnored.getSelection() );
    wWarnDestDir.setEnabled( wErrorIgnored.getSelection() );
    wlWarnExt.setEnabled( wErrorIgnored.getSelection() );
    wWarnExt.setEnabled( wErrorIgnored.getSelection() );
    wbbWarnDestDir.setEnabled( wErrorIgnored.getSelection() );

    wlErrorDestDir.setEnabled( wErrorIgnored.getSelection() );
    wErrorDestDir.setEnabled( wErrorIgnored.getSelection() );
    wlErrorExt.setEnabled( wErrorIgnored.getSelection() );
    wErrorExt.setEnabled( wErrorIgnored.getSelection() );
    wbbErrorDestDir.setEnabled( wErrorIgnored.getSelection() );

    wlLineNrDestDir.setEnabled( wErrorIgnored.getSelection() );
    wLineNrDestDir.setEnabled( wErrorIgnored.getSelection() );
    wlLineNrExt.setEnabled( wErrorIgnored.getSelection() );
    wLineNrExt.setEnabled( wErrorIgnored.getSelection() );
    wbbLineNrDestDir.setEnabled( wErrorIgnored.getSelection() );

    wlNrHeader.setEnabled( wHeader.getSelection() );
    wNrHeader.setEnabled( wHeader.getSelection() );
    wlNrFooter.setEnabled( wFooter.getSelection() );
    wNrFooter.setEnabled( wFooter.getSelection() );
    wlNrWraps.setEnabled( wWraps.getSelection() );
    wNrWraps.setEnabled( wWraps.getSelection() );

    wlNrLinesPerPage.setEnabled( wLayoutPaged.getSelection() );
    wNrLinesPerPage.setEnabled( wLayoutPaged.getSelection() );
    wlNrLinesDocHeader.setEnabled( wLayoutPaged.getSelection() );
    wNrLinesDocHeader.setEnabled( wLayoutPaged.getSelection() );
  }

  /**
   * Read the data from the TextFileInputMeta object and show it in this dialog.
   *
   * @param meta
   *          The TextFileInputMeta object to obtain the data from.
   */
  public void getData( TextFileInputMeta meta ) {
    getData( meta, true, true, null );
  }

  @Override
  public void getData( final TextFileInputMeta meta, final boolean copyStepname, final boolean reloadAllFields,
                       final Set<String> newFieldNames ) {
    if ( copyStepname ) {
      wStepname.setText( stepname );
    }

    wAccFilenames.setSelection( meta.inputFiles.acceptingFilenames );
    wPassThruFields.setSelection( meta.inputFiles.passingThruFields );
    if ( meta.inputFiles.acceptingField != null ) {
      wAccField.setText( meta.inputFiles.acceptingField );
    }
    if ( meta.getAcceptingStep() != null ) {
      wAccStep.setText( meta.getAcceptingStep().getName() );
    }

    if ( meta.getFileName() != null ) {
      wFilenameList.removeAll();

      for ( int i = 0; i < meta.getFileName().length; i++ ) {
        wFilenameList.add( meta.getFileName()[i], meta.inputFiles.fileMask[i], meta.inputFiles.excludeFileMask[i], meta
            .getRequiredFilesDesc( meta.inputFiles.fileRequired[i] ), meta.getRequiredFilesDesc(
                meta.inputFiles.includeSubFolders[i] ) );
      }
      wFilenameList.removeEmptyRows();
      wFilenameList.setRowNums();
      wFilenameList.optWidth( true );
    }
    if ( meta.content.fileType != null ) {
      wFiletype.setText( meta.content.fileType );
    }
    if ( meta.content.separator != null ) {
      wSeparator.setText( meta.content.separator );
    }
    if ( meta.content.enclosure != null ) {
      wEnclosure.setText( meta.content.enclosure );
    }
    if ( meta.content.escapeCharacter != null ) {
      wEscape.setText( meta.content.escapeCharacter );
    }
    wHeader.setSelection( meta.content.header );
    wNrHeader.setText( "" + meta.content.nrHeaderLines );
    wFooter.setSelection( meta.content.footer );
    wNrFooter.setText( "" + meta.content.nrFooterLines );
    wWraps.setSelection( meta.content.lineWrapped );
    wNrWraps.setText( "" + meta.content.nrWraps );
    wLayoutPaged.setSelection( meta.content.layoutPaged );
    wNrLinesPerPage.setText( "" + meta.content.nrLinesPerPage );
    wNrLinesDocHeader.setText( "" + meta.content.nrLinesDocHeader );
    if ( meta.content.fileCompression != null ) {
      wCompression.setText( meta.content.fileCompression );
    }
    wNoempty.setSelection( meta.content.noEmptyLines );
    wInclFilename.setSelection( meta.content.includeFilename );
    wInclRownum.setSelection( meta.content.includeRowNumber );
    wRownumByFile.setSelection( meta.content.rowNumberByFile );
    wDateLenient.setSelection( meta.content.dateFormatLenient );
    wAddResult.setSelection( meta.inputFiles.isaddresult );

    if ( meta.content.filenameField != null ) {
      wInclFilenameField.setText( meta.content.filenameField );
    }
    if ( meta.content.rowNumberField != null ) {
      wInclRownumField.setText( meta.content.rowNumberField );
    }
    if ( meta.content.fileFormat != null ) {
      wFormat.setText( meta.content.fileFormat );
    }

    if ( meta.content.length != null ) {
      wLength.setText( meta.content.length );
    }

    wLimit.setText( "" + meta.content.rowLimit );

    logDebug( "getting fields info..." );
    getFieldsData( meta, false, reloadAllFields, newFieldNames );

    if ( meta.getEncoding() != null ) {
      wEncoding.setText( meta.getEncoding() );
    }

    // Error handling fields...
    wErrorIgnored.setSelection( meta.errorHandling.errorIgnored );
    wSkipBadFiles.setSelection( meta.errorHandling.skipBadFiles );
    wSkipErrorLines.setSelection( meta.isErrorLineSkipped() );

    if ( meta.errorHandling.fileErrorField != null ) {
      wBadFileField.setText( meta.errorHandling.fileErrorField );
    }
    if ( meta.errorHandling.fileErrorMessageField != null ) {
      wBadFileMessageField.setText( meta.errorHandling.fileErrorMessageField );
    }

    if ( meta.getErrorCountField() != null ) {
      wErrorCount.setText( meta.getErrorCountField() );
    }
    if ( meta.getErrorFieldsField() != null ) {
      wErrorFields.setText( meta.getErrorFieldsField() );
    }
    if ( meta.getErrorTextField() != null ) {
      wErrorText.setText( meta.getErrorTextField() );
    }

    if ( meta.errorHandling.warningFilesDestinationDirectory != null ) {
      wWarnDestDir.setText( meta.errorHandling.warningFilesDestinationDirectory );
    }
    if ( meta.errorHandling.warningFilesExtension != null ) {
      wWarnExt.setText( meta.errorHandling.warningFilesExtension );
    }

    if ( meta.errorHandling.errorFilesDestinationDirectory != null ) {
      wErrorDestDir.setText( meta.errorHandling.errorFilesDestinationDirectory );
    }
    if ( meta.errorHandling.errorFilesExtension != null ) {
      wErrorExt.setText( meta.errorHandling.errorFilesExtension );
    }

    if ( meta.errorHandling.lineNumberFilesDestinationDirectory != null ) {
      wLineNrDestDir.setText( meta.errorHandling.lineNumberFilesDestinationDirectory );
    }
    if ( meta.errorHandling.lineNumberFilesExtension != null ) {
      wLineNrExt.setText( meta.errorHandling.lineNumberFilesExtension );
    }

    for ( int i = 0; i < meta.getFilter().length; i++ ) {
      TableItem item = wFilter.table.getItem( i );

      TextFileFilter filter = meta.getFilter()[i];
      if ( filter.getFilterString() != null ) {
        item.setText( 1, filter.getFilterString() );
      }
      if ( filter.getFilterPosition() >= 0 ) {
        item.setText( 2, "" + filter.getFilterPosition() );
      }
      item.setText( 3, filter.isFilterLastLine() ? BaseMessages.getString( PKG, "System.Combo.Yes" ) : BaseMessages
          .getString( PKG, "System.Combo.No" ) );
      item.setText( 4, filter.isFilterPositive() ? BaseMessages.getString( PKG, "System.Combo.Yes" ) : BaseMessages
          .getString( PKG, "System.Combo.No" ) );
    }

    // Date locale
    wDateLocale.setText( meta.content.dateFormatLocale.toString() );

    wFields.removeEmptyRows();
    wFields.setRowNums();
    wFields.optWidth( true );

    wFilter.removeEmptyRows();
    wFilter.setRowNums();
    wFilter.optWidth( true );

    if ( meta.additionalOutputFields.shortFilenameField != null ) {
      wShortFileFieldName.setText( meta.additionalOutputFields.shortFilenameField );
    }
    if ( meta.additionalOutputFields.pathField != null ) {
      wPathFieldName.setText( meta.additionalOutputFields.pathField );
    }
    if ( meta.additionalOutputFields.hiddenField != null ) {
      wIsHiddenName.setText( meta.additionalOutputFields.hiddenField );
    }
    if ( meta.additionalOutputFields.lastModificationField != null ) {
      wLastModificationTimeName.setText( meta.additionalOutputFields.lastModificationField );
    }
    if ( meta.additionalOutputFields.uriField != null ) {
      wUriName.setText( meta.additionalOutputFields.uriField );
    }
    if ( meta.additionalOutputFields.rootUriField != null ) {
      wRootUriName.setText( meta.additionalOutputFields.rootUriField );
    }
    if ( meta.additionalOutputFields.extensionField != null ) {
      wExtensionFieldName.setText( meta.additionalOutputFields.extensionField );
    }
    if ( meta.additionalOutputFields.sizeField != null ) {
      wSizeFieldName.setText( meta.additionalOutputFields.sizeField );
    }

    setFlags();

    wStepname.selectAll();
    wStepname.setFocus();
  }

  private void getFieldsData( TextFileInputMeta in, boolean insertAtTop, final boolean reloadAllFields,
                              final Set<String> newFieldNames ) {
    final List<String> lowerCaseNewFieldNames = newFieldNames == null ? new ArrayList()
      : newFieldNames.stream().map( String::toLowerCase ).collect( Collectors.toList() );
    for ( int i = 0; i < in.inputFields.length; i++ ) {
      BaseFileField field = in.inputFields[i];

      TableItem item;

      if ( insertAtTop ) {
        item = new TableItem( wFields.table, SWT.NONE, i );
      } else {
        item = getTableItem( field.getName(), reloadAllFields );
      }
      if ( !reloadAllFields && !lowerCaseNewFieldNames.contains( field.getName().toLowerCase() ) ) {
        continue;
      }

      item.setText( 1, Const.NVL( field.getName(), "" ) );
      String type = field.getTypeDesc();
      String format = field.getFormat();
      String position = "" + field.getPosition();
      String length = "" + field.getLength();
      String prec = "" + field.getPrecision();
      String curr = field.getCurrencySymbol();
      String group = field.getGroupSymbol();
      String decim = field.getDecimalSymbol();
      String def = field.getNullString();
      String ifNull = field.getIfNullValue();
      String trim = field.getTrimTypeDesc();
      String rep =
          field.isRepeated() ? BaseMessages.getString( PKG, "System.Combo.Yes" ) : BaseMessages.getString( PKG,
              "System.Combo.No" );

      if ( type != null ) {
        item.setText( 2, type );
      }
      if ( format != null ) {
        item.setText( 3, format );
      }
      if ( position != null && !"-1".equals( position ) ) {
        item.setText( 4, position );
      }
      if ( length != null && !"-1".equals( length ) ) {
        item.setText( 5, length );
      }
      if ( prec != null && !"-1".equals( prec ) ) {
        item.setText( 6, prec );
      }
      if ( curr != null ) {
        item.setText( 7, curr );
      }
      if ( decim != null ) {
        item.setText( 8, decim );
      }
      if ( group != null ) {
        item.setText( 9, group );
      }
      if ( def != null ) {
        item.setText( 10, def );
      }
      if ( ifNull != null ) {
        item.setText( 11, ifNull );
      }
      if ( trim != null ) {
        item.setText( 12, trim );
      }
      if ( rep != null ) {
        item.setText( 13, rep );
      }
    }

  }

  private void setEncodings() {
    // Encoding of the text file:
    if ( !gotEncodings ) {
      gotEncodings = true;

      wEncoding.removeAll();
      List<Charset> values = new ArrayList<>( Charset.availableCharsets().values() );
      for ( Charset charSet : values ) {
        wEncoding.add( charSet.displayName() );
      }

      // Now select the default!
      String defEncoding = Const.getEnvironmentVariable( "file.encoding", "UTF-8" );
      int idx = Const.indexOfString( defEncoding, wEncoding.getItems() );
      if ( idx >= 0 ) {
        wEncoding.select( idx );
      }
    }
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

    getInfo( input, false );
    dispose();
  }

  /**
   * Fill meta object from UI options.
   *
   * @param meta
   *          meta object
   * @param preview
   *          flag for preview or real options should be used. Currently, only one option is differ for preview - EOL
   *          chars. It uses as "mixed" for be able to preview any file.
   */
  private void getInfo( TextFileInputMeta meta, boolean preview ) {
    stepname = wStepname.getText(); // return value

    // copy info to TextFileInputMeta class (input)
    meta.inputFiles.acceptingFilenames = wAccFilenames.getSelection();
    meta.inputFiles.passingThruFields = wPassThruFields.getSelection();
    meta.inputFiles.acceptingField = wAccField.getText();
    meta.inputFiles.acceptingStepName = wAccStep.getText();
    meta.setAcceptingStep( transMeta.findStep( wAccStep.getText() ) );

    meta.content.fileType = wFiletype.getText();
    if ( preview ) {
      // mixed type for preview, for be able to eat any EOL chars
      meta.content.fileFormat = "mixed";
    } else {
      meta.content.fileFormat = wFormat.getText();
    }
    meta.content.separator = wSeparator.getText();
    meta.content.enclosure = wEnclosure.getText();
    meta.content.escapeCharacter = wEscape.getText();
    meta.content.rowLimit = Const.toLong( wLimit.getText(), 0L );
    meta.content.filenameField = wInclFilenameField.getText();
    meta.content.rowNumberField = wInclRownumField.getText();
    meta.inputFiles.isaddresult = wAddResult.getSelection();

    meta.content.includeFilename = wInclFilename.getSelection();
    meta.content.includeRowNumber = wInclRownum.getSelection();
    meta.content.rowNumberByFile = wRownumByFile.getSelection();
    meta.content.header = wHeader.getSelection();
    meta.content.nrHeaderLines = Const.toInt( wNrHeader.getText(), 1 );
    meta.content.footer = wFooter.getSelection();
    meta.content.nrFooterLines = Const.toInt( wNrFooter.getText(), 1 );
    meta.content.lineWrapped = wWraps.getSelection();
    meta.content.nrWraps = Const.toInt( wNrWraps.getText(), 1 );
    meta.content.layoutPaged = wLayoutPaged.getSelection();
    meta.content.nrLinesPerPage = Const.toInt( wNrLinesPerPage.getText(), 80 );
    meta.content.nrLinesDocHeader = Const.toInt( wNrLinesDocHeader.getText(), 0 );
    meta.content.fileCompression = wCompression.getText();
    meta.content.dateFormatLenient = wDateLenient.getSelection();
    meta.content.noEmptyLines = wNoempty.getSelection();
    meta.content.encoding = wEncoding.getText();
    meta.content.length = wLength.getText();

    int nrfiles = wFilenameList.getItemCount();
    int nrfields = wFields.nrNonEmpty();
    int nrfilters = wFilter.nrNonEmpty();
    meta.allocate( nrfiles, nrfields, nrfilters );

    meta.setFileName( wFilenameList.getItems( 0 ) );
    meta.inputFiles.fileMask = wFilenameList.getItems( 1 );
    meta.inputFiles.excludeFileMask = wFilenameList.getItems( 2 );
    meta.inputFiles_fileRequired( wFilenameList.getItems( 3 ) );
    meta.inputFiles_includeSubFolders( wFilenameList.getItems( 4 ) );

    for ( int i = 0; i < nrfields; i++ ) {
      BaseFileField field = new BaseFileField();

      TableItem item = wFields.getNonEmpty( i );
      field.setName( item.getText( 1 ) );
      field.setType( ValueMetaFactory.getIdForValueMeta( item.getText( 2 ) ) );
      field.setFormat( item.getText( 3 ) );
      field.setPosition( Const.toInt( item.getText( 4 ), -1 ) );
      field.setLength( Const.toInt( item.getText( 5 ), -1 ) );
      field.setPrecision( Const.toInt( item.getText( 6 ), -1 ) );
      field.setCurrencySymbol( item.getText( 7 ) );
      field.setDecimalSymbol( item.getText( 8 ) );
      field.setGroupSymbol( item.getText( 9 ) );
      field.setNullString( item.getText( 10 ) );
      field.setIfNullValue( item.getText( 11 ) );
      field.setTrimType( ValueMetaString.getTrimTypeByDesc( item.getText( 12 ) ) );
      field.setRepeated( BaseMessages.getString( PKG, "System.Combo.Yes" ).equalsIgnoreCase( item.getText( 13 ) ) );

      // CHECKSTYLE:Indentation:OFF
      meta.inputFields[i] = field;
    }

    for ( int i = 0; i < nrfilters; i++ ) {
      TableItem item = wFilter.getNonEmpty( i );
      TextFileFilter filter = new TextFileFilter();
      // CHECKSTYLE:Indentation:OFF
      meta.getFilter()[i] = filter;

      filter.setFilterString( item.getText( 1 ) );
      filter.setFilterPosition( Const.toInt( item.getText( 2 ), -1 ) );
      filter.setFilterLastLine( BaseMessages.getString( PKG, "System.Combo.Yes" ).equalsIgnoreCase( item.getText(
          3 ) ) );
      filter.setFilterPositive( BaseMessages.getString( PKG, "System.Combo.Yes" ).equalsIgnoreCase( item.getText(
          4 ) ) );
    }
    // Error handling fields...
    meta.errorHandling.errorIgnored = wErrorIgnored.getSelection();
    meta.errorHandling.skipBadFiles = wSkipBadFiles.getSelection();
    meta.errorHandling.fileErrorField = wBadFileField.getText();
    meta.errorHandling.fileErrorMessageField = wBadFileMessageField.getText();
    meta.setErrorLineSkipped( wSkipErrorLines.getSelection() );
    meta.setErrorCountField( wErrorCount.getText() );
    meta.setErrorFieldsField( wErrorFields.getText() );
    meta.setErrorTextField( wErrorText.getText() );

    meta.errorHandling.warningFilesDestinationDirectory = wWarnDestDir.getText();
    meta.errorHandling.warningFilesExtension = wWarnExt.getText();
    meta.errorHandling.errorFilesDestinationDirectory = wErrorDestDir.getText();
    meta.errorHandling.errorFilesExtension = wErrorExt.getText();
    meta.errorHandling.lineNumberFilesDestinationDirectory = wLineNrDestDir.getText();
    meta.errorHandling.lineNumberFilesExtension = wLineNrExt.getText();

    // Date format Locale
    Locale locale = EnvUtil.createLocale( wDateLocale.getText() );
    if ( !locale.equals( Locale.getDefault() ) ) {
      meta.content.dateFormatLocale = locale;
    } else {
      meta.content.dateFormatLocale = Locale.getDefault();
    }

    meta.additionalOutputFields.shortFilenameField = wShortFileFieldName.getText();
    meta.additionalOutputFields.pathField = wPathFieldName.getText();
    meta.additionalOutputFields.hiddenField = wIsHiddenName.getText();
    meta.additionalOutputFields.lastModificationField = wLastModificationTimeName.getText();
    meta.additionalOutputFields.uriField = wUriName.getText();
    meta.additionalOutputFields.rootUriField = wRootUriName.getText();
    meta.additionalOutputFields.extensionField = wExtensionFieldName.getText();
    meta.additionalOutputFields.sizeField = wSizeFieldName.getText();
  }

  private void get() {
    if ( wFiletype.getText().equalsIgnoreCase( "CSV" ) ) {
      getFields();
    } else {
      getFixed();
    }
  }

  @Override
  public String loadFieldsImpl( final TextFileInputMeta meta, final int samples ) {
    return loadFieldsImpl( (CsvInputAwareMeta) meta, samples );
  }

  @Override
  public String massageFieldName( final String fieldName ) {
    // Replace all spaces and hyphens (-) with underscores (_)
    String massagedFieldName = fieldName;
    massagedFieldName = Const.replace( massagedFieldName, " ", "_" );
    massagedFieldName = Const.replace( massagedFieldName, "-", "_" );
    return massagedFieldName;
  }

  @Override
  public String[] getFieldNames( final TextFileInputMeta meta ) {
    return getFieldNames( (CsvInputAwareMeta) meta );
  }

  public static int guessPrecision( double d ) {
    // Round numbers
    long frac = Math.round( ( d - Math.floor( d ) ) * 1E10 ); // max precision : 10
    int precision = 10;

    // 0,34 --> 3400000000
    // 0 to the right --> precision -1!
    // 0 to the right means frac%10 == 0

    while ( precision >= 0 && ( frac % 10 ) == 0 ) {
      frac /= 10;
      precision--;
    }
    precision++;

    return precision;
  }

  public static int guessIntLength( double d ) {
    double flr = Math.floor( d );
    int len = 1;

    while ( flr > 9 ) {
      flr /= 10;
      flr = Math.floor( flr );
      len++;
    }

    return len;
  }

  // Preview the data
  private void preview() {
    // Create the XML input step
    TextFileInputMeta oneMeta = new TextFileInputMeta();
    getInfo( oneMeta, true );

    if ( oneMeta.inputFiles.acceptingFilenames ) {
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_INFORMATION );
      mb.setMessage( BaseMessages.getString( PKG, "TextFileInputDialog.Dialog.SpecifyASampleFile.Message" ) );
      mb.setText( BaseMessages.getString( PKG, "TextFileInputDialog.Dialog.SpecifyASampleFile.Title" ) );
      mb.open();
      return;
    }

    TransMeta previewMeta =
        TransPreviewFactory.generatePreviewTransformation( transMeta, oneMeta, wStepname.getText() );

    EnterNumberDialog numberDialog =
        new EnterNumberDialog( shell, props.getDefaultPreviewSize(), BaseMessages.getString( PKG,
            "TextFileInputDialog.PreviewSize.DialogTitle" ), BaseMessages.getString( PKG,
                "TextFileInputDialog.PreviewSize.DialogMessage" ) );
    int previewSize = numberDialog.open();
    if ( previewSize > 0 ) {
      TransPreviewProgressDialog progressDialog =
          new TransPreviewProgressDialog( shell, previewMeta, new String[] { wStepname.getText() }, new int[] {
            previewSize } );
      progressDialog.open();

      Trans trans = progressDialog.getTrans();
      String loggingText = progressDialog.getLoggingText();

      if ( !progressDialog.isCancelled() ) {
        if ( trans.getResult() != null && trans.getResult().getNrErrors() > 0 ) {
          EnterTextDialog etd =
              new EnterTextDialog( shell, BaseMessages.getString( PKG, "System.Dialog.PreviewError.Title" ),
                  BaseMessages.getString( PKG, "System.Dialog.PreviewError.Message" ), loggingText, true );
          etd.setReadOnly();
          etd.open();
        }
      }

      PreviewRowsDialog prd =
          new PreviewRowsDialog( shell, transMeta, SWT.NONE, wStepname.getText(), progressDialog.getPreviewRowsMeta(
              wStepname.getText() ), progressDialog.getPreviewRows( wStepname.getText() ), loggingText );
      prd.open();
    }
  }

  // Get the first x lines
  private void first( boolean skipHeaders ) {
    TextFileInputMeta info = new TextFileInputMeta();
    getInfo( info, true );

    try {
      if ( info.getFileInputList( transMeta.getBowl(), transMeta ).nrOfFiles() > 0 ) {
        String shellText = BaseMessages.getString( PKG, "TextFileInputDialog.LinesToView.DialogTitle" );
        String lineText = BaseMessages.getString( PKG, "TextFileInputDialog.LinesToView.DialogMessage" );
        EnterNumberDialog end = new EnterNumberDialog( shell, 100, shellText, lineText );
        int nrLines = end.open();
        if ( nrLines >= 0 ) {
          List<String> linesList = getFirst( nrLines, skipHeaders );
          if ( linesList != null && linesList.size() > 0 ) {
            String firstlines = "";
            for ( String aLinesList : linesList ) {
              firstlines += aLinesList + Const.CR;
            }
            EnterTextDialog etd =
                new EnterTextDialog( shell, BaseMessages.getString( PKG,
                    "TextFileInputDialog.ContentOfFirstFile.DialogTitle" ), ( nrLines == 0 ? BaseMessages.getString(
                        PKG, "TextFileInputDialog.ContentOfFirstFile.AllLines.DialogMessage" ) : BaseMessages.getString(
                            PKG, "TextFileInputDialog.ContentOfFirstFile.NLines.DialogMessage", "" + nrLines ) ),
                    firstlines, true );
            etd.setReadOnly();
            etd.open();
          } else {
            MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
            mb.setMessage( BaseMessages.getString( PKG, "TextFileInputDialog.UnableToReadLines.DialogMessage" ) );
            mb.setText( BaseMessages.getString( PKG, "TextFileInputDialog.UnableToReadLines.DialogTitle" ) );
            mb.open();
          }
        }
      } else {
        MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
        mb.setMessage( BaseMessages.getString( PKG, "TextFileInputDialog.NoValidFile.DialogMessage" ) );
        mb.setText( BaseMessages.getString( PKG, "System.Dialog.Error.Title" ) );
        mb.open();
      }
    } catch ( KettleException e ) {
      new ErrorDialog( shell, BaseMessages.getString( PKG, "System.Dialog.Error.Title" ), BaseMessages.getString( PKG,
          "TextFileInputDialog.ErrorGettingData.DialogMessage" ), e );
    }
  }

  // Get the first x lines
  private List<String> getFirst( int nrlines, boolean skipHeaders ) throws KettleException {
    TextFileInputMeta meta = new TextFileInputMeta();
    getInfo( meta, true );
    FileInputList textFileList = meta.getFileInputList( transMeta.getBowl(), transMeta );

    InputStream fi;
    CompressionInputStream f = null;
    StringBuilder lineStringBuilder = new StringBuilder( 256 );
    int fileFormatType = meta.getFileFormatTypeNr();

    List<String> retval = new ArrayList<>();

    if ( textFileList.nrOfFiles() > 0 ) {
      FileObject file = textFileList.getFile( 0 );
      try {
        fi = KettleVFS.getInputStream( file );

        CompressionProvider provider =
            CompressionProviderFactory.getInstance().createCompressionProviderInstance( meta.content.fileCompression );
        f = provider.createInputStream( fi );

        BufferedInputStreamReader reader;
        if ( meta.getEncoding() != null && meta.getEncoding().length() > 0 ) {
          reader = new BufferedInputStreamReader( new InputStreamReader( f, meta.getEncoding() ) );
        } else {
          reader = new BufferedInputStreamReader( new InputStreamReader( f ) );
        }
        EncodingType encodingType = EncodingType.guessEncodingType( reader.getEncoding() );

        int linenr = 0;
        int maxnr = nrlines + ( meta.content.header ? meta.content.nrHeaderLines : 0 );

        if ( skipHeaders ) {
          // Skip the header lines first if more then one, it helps us position
          if ( meta.content.layoutPaged && meta.content.nrLinesDocHeader > 0 ) {
            TextFileInputUtils.skipLines( log, reader, encodingType, fileFormatType,
              lineStringBuilder,  meta.content.nrLinesDocHeader - 1, meta.getEnclosure(), meta.getEscapeCharacter(), 0 );
          }

          // Skip the header lines first if more then one, it helps us position
          if ( meta.content.header && meta.content.nrHeaderLines > 0 ) {
            TextFileInputUtils.skipLines( log, reader, encodingType, fileFormatType,
              lineStringBuilder,  meta.content.nrHeaderLines - 1, meta.getEnclosure(), meta.getEscapeCharacter(), 0 );
          }
        }

        String line = TextFileInputUtils.getLine( log, reader, encodingType, fileFormatType, lineStringBuilder, meta.getEnclosure(), meta.getEscapeCharacter() );
        while ( line != null && ( linenr < maxnr || nrlines == 0 ) ) {
          retval.add( line );
          linenr++;
          line = TextFileInputUtils.getLine( log, reader, encodingType, fileFormatType, lineStringBuilder, meta.getEnclosure(), meta.getEscapeCharacter() );
        }
      } catch ( Exception e ) {
        throw new KettleException( BaseMessages.getString( PKG, "TextFileInputDialog.Exception.ErrorGettingFirstLines",
            "" + nrlines, file.getName().getURI() ), e );
      } finally {
        try {
          if ( f != null ) {
            f.close();
          }
        } catch ( Exception e ) {
          // Ignore errors
        }
      }
    }

    return retval;
  }

  private void getFixed() {
    TextFileInputMeta info = new TextFileInputMeta();
    getInfo( info, true );

    Shell sh = new Shell( shell, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );

    try {
      List<String> rows = getFirst( 50, false );
      fields = getFields( info, rows );

      final TextFileImportWizardPage1 page1 = new TextFileImportWizardPage1( "1", props, rows, fields );
      page1.createControl( sh );
      final TextFileImportWizardPage2 page2 = new TextFileImportWizardPage2( "2", props, rows, fields );
      page2.createControl( sh );

      Wizard wizard = new Wizard() {
        public boolean performFinish() {
          wFields.clearAll( false );

          for ( TextFileInputFieldInterface field1 : fields ) {
            BaseFileField field = (BaseFileField) field1;
            if ( !field.isIgnored() && field.getLength() > 0 ) {
              TableItem item = new TableItem( wFields.table, SWT.NONE );
              item.setText( 1, field.getName() );
              item.setText( 2, "" + field.getTypeDesc() );
              item.setText( 3, "" + field.getFormat() );
              item.setText( 4, "" + field.getPosition() );
              item.setText( 5, field.getLength() < 0 ? "" : "" + field.getLength() );
              item.setText( 6, field.getPrecision() < 0 ? "" : "" + field.getPrecision() );
              item.setText( 7, "" + field.getCurrencySymbol() );
              item.setText( 8, "" + field.getDecimalSymbol() );
              item.setText( 9, "" + field.getGroupSymbol() );
              item.setText( 10, "" + field.getNullString() );
              item.setText( 11, "" + field.getIfNullValue() );
              item.setText( 12, "" + field.getTrimTypeDesc() );
              item.setText( 13, field.isRepeated() ? BaseMessages.getString( PKG, "System.Combo.Yes" ) : BaseMessages
                  .getString( PKG, "System.Combo.No" ) );
            }

          }
          int size = wFields.table.getItemCount();
          if ( size == 0 ) {
            new TableItem( wFields.table, SWT.NONE );
          }

          wFields.removeEmptyRows();
          wFields.setRowNums();
          wFields.optWidth( true );

          input.setChanged();

          return true;
        }
      };

      wizard.addPage( page1 );
      wizard.addPage( page2 );

      WizardDialog wd = new WizardDialog( shell, wizard );
      WizardDialog.setDefaultImage( GUIResource.getInstance().getImageWizard() );
      wd.setMinimumPageSize( 700, 375 );
      wd.updateSize();
      wd.open();
    } catch ( Exception e ) {
      new ErrorDialog( shell, BaseMessages.getString( PKG, "TextFileInputDialog.ErrorShowingFixedWizard.DialogTitle" ),
          BaseMessages.getString( PKG, "TextFileInputDialog.ErrorShowingFixedWizard.DialogMessage" ), e );
    }
  }

  private Vector<TextFileInputFieldInterface> getFields( TextFileInputMeta info, List<String> rows ) {
    Vector<TextFileInputFieldInterface> fields = new Vector<>();

    int maxsize = 0;
    for ( String row : rows ) {
      int len = row.length();
      if ( len > maxsize ) {
        maxsize = len;
      }
    }

    int prevEnd = 0;
    int dummynr = 1;

    for ( int i = 0; i < info.inputFields.length; i++ ) {
      BaseFileField f = info.inputFields[i];

      // See if positions are skipped, if this is the case, add dummy fields...
      if ( f.getPosition() != prevEnd ) { // gap

        BaseFileField field = new BaseFileField( "Dummy" + dummynr, prevEnd, f.getPosition() - prevEnd );
        field.setIgnored( true ); // don't include in result by default.
        fields.add( field );
        dummynr++;
      }

      BaseFileField field = new BaseFileField( f.getName(), f.getPosition(), f.getLength() );
      field.setType( f.getType() );
      field.setIgnored( false );
      field.setFormat( f.getFormat() );
      field.setPrecision( f.getPrecision() );
      field.setTrimType( f.getTrimType() );
      field.setDecimalSymbol( f.getDecimalSymbol() );
      field.setGroupSymbol( f.getGroupSymbol() );
      field.setCurrencySymbol( f.getCurrencySymbol() );
      field.setRepeated( f.isRepeated() );
      field.setNullString( f.getNullString() );

      fields.add( field );

      prevEnd = field.getPosition() + field.getLength();
    }

    if ( info.inputFields.length == 0 ) {
      BaseFileField field = new BaseFileField( "Field1", 0, maxsize );
      fields.add( field );
    } else {
      // Take the last field and see if it reached until the maximum...
      BaseFileField f = info.inputFields[info.inputFields.length - 1];

      int pos = f.getPosition();
      int len = f.getLength();
      if ( pos + len < maxsize ) {
        // If not, add an extra trailing field!
        BaseFileField field = new BaseFileField( "Dummy" + dummynr, pos + len, maxsize - pos - len );
        field.setIgnored( true ); // don't include in result by default.
        fields.add( field );
      }
    }

    Collections.sort( fields );

    return fields;
  }

  /**
   * Sets the input width to minimal width...
   *
   */
  public void setMinimalWidth() {
    int nrNonEmptyFields = wFields.nrNonEmpty();
    for ( int i = 0; i < nrNonEmptyFields; i++ ) {
      TableItem item = wFields.getNonEmpty( i );

      item.setText( 5, "" );
      item.setText( 6, "" );
      item.setText( 12, ValueMetaString.getTrimTypeDesc( ValueMetaInterface.TRIM_TYPE_BOTH ) );

      int type = ValueMetaFactory.getIdForValueMeta( item.getText( 2 ) );
      switch ( type ) {
        case ValueMetaInterface.TYPE_STRING:
          item.setText( 3, "" );
          break;
        case ValueMetaInterface.TYPE_INTEGER:
          item.setText( 3, "0" );
          break;
        case ValueMetaInterface.TYPE_NUMBER:
          item.setText( 3, "0.#####" );
          break;
        case ValueMetaInterface.TYPE_DATE:
          break;
        default:
          break;
      }
    }

    for ( int i = 0; i < input.inputFields.length; i++ ) {
      input.inputFields[i].setTrimType( ValueMetaInterface.TRIM_TYPE_BOTH );
    }

    wFields.optWidth( true );
  }

  /**
   * Overloading setMinimalWidth() in order to test trim functionality
   * @param wFields mocked TableView to avoid wFields.nrNonEmpty() from throwing NullPointerException
   */
  public void setMinimalWidth( TableView wFields ) {
    this.wFields = wFields;
    this.setMinimalWidth();
  }

  private void addAdditionalFieldsTab() {
    // ////////////////////////
    // START OF ADDITIONAL FIELDS TAB ///
    // ////////////////////////
    wAdditionalFieldsTab = new CTabItem( wTabFolder, SWT.NONE );
    wAdditionalFieldsTab.setText( BaseMessages.getString( PKG, "TextFileInputDialog.AdditionalFieldsTab.TabTitle" ) );

    wAdditionalFieldsComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wAdditionalFieldsComp );

    FormLayout fieldsLayout = new FormLayout();
    fieldsLayout.marginWidth = 3;
    fieldsLayout.marginHeight = 3;
    wAdditionalFieldsComp.setLayout( fieldsLayout );

    // ShortFileFieldName line
    wlShortFileFieldName = new Label( wAdditionalFieldsComp, SWT.RIGHT );
    wlShortFileFieldName.setText( BaseMessages.getString( PKG, "TextFileInputDialog.ShortFileFieldName.Label" ) );
    props.setLook( wlShortFileFieldName );
    fdlShortFileFieldName = new FormData();
    fdlShortFileFieldName.left = new FormAttachment( 0, 0 );
    fdlShortFileFieldName.top = new FormAttachment( margin, margin );
    fdlShortFileFieldName.right = new FormAttachment( middle, -margin );
    wlShortFileFieldName.setLayoutData( fdlShortFileFieldName );

    wShortFileFieldName = new TextVar( transMeta, wAdditionalFieldsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wShortFileFieldName );
    wShortFileFieldName.addModifyListener( lsMod );
    fdShortFileFieldName = new FormData();
    fdShortFileFieldName.left = new FormAttachment( middle, 0 );
    fdShortFileFieldName.right = new FormAttachment( 100, -margin );
    fdShortFileFieldName.top = new FormAttachment( margin, margin );
    wShortFileFieldName.setLayoutData( fdShortFileFieldName );

    // ExtensionFieldName line
    wlExtensionFieldName = new Label( wAdditionalFieldsComp, SWT.RIGHT );
    wlExtensionFieldName.setText( BaseMessages.getString( PKG, "TextFileInputDialog.ExtensionFieldName.Label" ) );
    props.setLook( wlExtensionFieldName );
    fdlExtensionFieldName = new FormData();
    fdlExtensionFieldName.left = new FormAttachment( 0, 0 );
    fdlExtensionFieldName.top = new FormAttachment( wShortFileFieldName, margin );
    fdlExtensionFieldName.right = new FormAttachment( middle, -margin );
    wlExtensionFieldName.setLayoutData( fdlExtensionFieldName );

    wExtensionFieldName = new TextVar( transMeta, wAdditionalFieldsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wExtensionFieldName );
    wExtensionFieldName.addModifyListener( lsMod );
    fdExtensionFieldName = new FormData();
    fdExtensionFieldName.left = new FormAttachment( middle, 0 );
    fdExtensionFieldName.right = new FormAttachment( 100, -margin );
    fdExtensionFieldName.top = new FormAttachment( wShortFileFieldName, margin );
    wExtensionFieldName.setLayoutData( fdExtensionFieldName );

    // PathFieldName line
    wlPathFieldName = new Label( wAdditionalFieldsComp, SWT.RIGHT );
    wlPathFieldName.setText( BaseMessages.getString( PKG, "TextFileInputDialog.PathFieldName.Label" ) );
    props.setLook( wlPathFieldName );
    fdlPathFieldName = new FormData();
    fdlPathFieldName.left = new FormAttachment( 0, 0 );
    fdlPathFieldName.top = new FormAttachment( wExtensionFieldName, margin );
    fdlPathFieldName.right = new FormAttachment( middle, -margin );
    wlPathFieldName.setLayoutData( fdlPathFieldName );

    wPathFieldName = new TextVar( transMeta, wAdditionalFieldsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wPathFieldName );
    wPathFieldName.addModifyListener( lsMod );
    fdPathFieldName = new FormData();
    fdPathFieldName.left = new FormAttachment( middle, 0 );
    fdPathFieldName.right = new FormAttachment( 100, -margin );
    fdPathFieldName.top = new FormAttachment( wExtensionFieldName, margin );
    wPathFieldName.setLayoutData( fdPathFieldName );

    // SizeFieldName line
    wlSizeFieldName = new Label( wAdditionalFieldsComp, SWT.RIGHT );
    wlSizeFieldName.setText( BaseMessages.getString( PKG, "TextFileInputDialog.SizeFieldName.Label" ) );
    props.setLook( wlSizeFieldName );
    fdlSizeFieldName = new FormData();
    fdlSizeFieldName.left = new FormAttachment( 0, 0 );
    fdlSizeFieldName.top = new FormAttachment( wPathFieldName, margin );
    fdlSizeFieldName.right = new FormAttachment( middle, -margin );
    wlSizeFieldName.setLayoutData( fdlSizeFieldName );

    wSizeFieldName = new TextVar( transMeta, wAdditionalFieldsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wSizeFieldName );
    wSizeFieldName.addModifyListener( lsMod );
    fdSizeFieldName = new FormData();
    fdSizeFieldName.left = new FormAttachment( middle, 0 );
    fdSizeFieldName.right = new FormAttachment( 100, -margin );
    fdSizeFieldName.top = new FormAttachment( wPathFieldName, margin );
    wSizeFieldName.setLayoutData( fdSizeFieldName );

    // IsHiddenName line
    wlIsHiddenName = new Label( wAdditionalFieldsComp, SWT.RIGHT );
    wlIsHiddenName.setText( BaseMessages.getString( PKG, "TextFileInputDialog.IsHiddenName.Label" ) );
    props.setLook( wlIsHiddenName );
    fdlIsHiddenName = new FormData();
    fdlIsHiddenName.left = new FormAttachment( 0, 0 );
    fdlIsHiddenName.top = new FormAttachment( wSizeFieldName, margin );
    fdlIsHiddenName.right = new FormAttachment( middle, -margin );
    wlIsHiddenName.setLayoutData( fdlIsHiddenName );

    wIsHiddenName = new TextVar( transMeta, wAdditionalFieldsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wIsHiddenName );
    wIsHiddenName.addModifyListener( lsMod );
    fdIsHiddenName = new FormData();
    fdIsHiddenName.left = new FormAttachment( middle, 0 );
    fdIsHiddenName.right = new FormAttachment( 100, -margin );
    fdIsHiddenName.top = new FormAttachment( wSizeFieldName, margin );
    wIsHiddenName.setLayoutData( fdIsHiddenName );

    // LastModificationTimeName line
    wlLastModificationTimeName = new Label( wAdditionalFieldsComp, SWT.RIGHT );
    wlLastModificationTimeName.setText( BaseMessages.getString( PKG,
        "TextFileInputDialog.LastModificationTimeName.Label" ) );
    props.setLook( wlLastModificationTimeName );
    fdlLastModificationTimeName = new FormData();
    fdlLastModificationTimeName.left = new FormAttachment( 0, 0 );
    fdlLastModificationTimeName.top = new FormAttachment( wIsHiddenName, margin );
    fdlLastModificationTimeName.right = new FormAttachment( middle, -margin );
    wlLastModificationTimeName.setLayoutData( fdlLastModificationTimeName );

    wLastModificationTimeName = new TextVar( transMeta, wAdditionalFieldsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wLastModificationTimeName );
    wLastModificationTimeName.addModifyListener( lsMod );
    fdLastModificationTimeName = new FormData();
    fdLastModificationTimeName.left = new FormAttachment( middle, 0 );
    fdLastModificationTimeName.right = new FormAttachment( 100, -margin );
    fdLastModificationTimeName.top = new FormAttachment( wIsHiddenName, margin );
    wLastModificationTimeName.setLayoutData( fdLastModificationTimeName );

    // UriName line
    wlUriName = new Label( wAdditionalFieldsComp, SWT.RIGHT );
    wlUriName.setText( BaseMessages.getString( PKG, "TextFileInputDialog.UriName.Label" ) );
    props.setLook( wlUriName );
    fdlUriName = new FormData();
    fdlUriName.left = new FormAttachment( 0, 0 );
    fdlUriName.top = new FormAttachment( wLastModificationTimeName, margin );
    fdlUriName.right = new FormAttachment( middle, -margin );
    wlUriName.setLayoutData( fdlUriName );

    wUriName = new TextVar( transMeta, wAdditionalFieldsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wUriName );
    wUriName.addModifyListener( lsMod );
    fdUriName = new FormData();
    fdUriName.left = new FormAttachment( middle, 0 );
    fdUriName.right = new FormAttachment( 100, -margin );
    fdUriName.top = new FormAttachment( wLastModificationTimeName, margin );
    wUriName.setLayoutData( fdUriName );

    // RootUriName line
    wlRootUriName = new Label( wAdditionalFieldsComp, SWT.RIGHT );
    wlRootUriName.setText( BaseMessages.getString( PKG, "TextFileInputDialog.RootUriName.Label" ) );
    props.setLook( wlRootUriName );
    fdlRootUriName = new FormData();
    fdlRootUriName.left = new FormAttachment( 0, 0 );
    fdlRootUriName.top = new FormAttachment( wUriName, margin );
    fdlRootUriName.right = new FormAttachment( middle, -margin );
    wlRootUriName.setLayoutData( fdlRootUriName );

    wRootUriName = new TextVar( transMeta, wAdditionalFieldsComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wRootUriName );
    wRootUriName.addModifyListener( lsMod );
    fdRootUriName = new FormData();
    fdRootUriName.left = new FormAttachment( middle, 0 );
    fdRootUriName.right = new FormAttachment( 100, -margin );
    fdRootUriName.top = new FormAttachment( wUriName, margin );
    wRootUriName.setLayoutData( fdRootUriName );

    fdAdditionalFieldsComp = new FormData();
    fdAdditionalFieldsComp.left = new FormAttachment( 0, 0 );
    fdAdditionalFieldsComp.top = new FormAttachment( 0, 0 );
    fdAdditionalFieldsComp.right = new FormAttachment( 100, 0 );
    fdAdditionalFieldsComp.bottom = new FormAttachment( 100, 0 );
    wAdditionalFieldsComp.setLayoutData( fdAdditionalFieldsComp );

    wAdditionalFieldsComp.layout();
    wAdditionalFieldsTab.setControl( wAdditionalFieldsComp );

    // ///////////////////////////////////////////////////////////
    // / END OF ADDITIONAL FIELDS TAB
    // ///////////////////////////////////////////////////////////

  }

  @Override
  public TableView getFieldsTable() {
    return this.wFields;
  }

  @Override
  public Shell getShell() {
    return this.shell;
  }

  @Override
  public TextFileInputMeta getNewMetaInstance() {
    return new TextFileInputMeta();
  }

  @Override
  public void populateMeta( TextFileInputMeta inputMeta ) {
    getInfo( inputMeta, false );
  }

  @Override
  public CsvInputAwareImportProgressDialog getCsvImportProgressDialog(
    final CsvInputAwareMeta meta, final int samples, final InputStreamReader reader ) {
    return new TextFileCSVImportProgressDialog( getShell(), (TextFileInputMeta) meta, transMeta, reader, samples, true );
  }

  @Override
  public LogChannel getLogChannel() {
    return log;
  }

  @Override
  public TransMeta getTransMeta() {
    return transMeta;
  }

  @Override
  public InputStream getInputStream( final CsvInputAwareMeta meta ) {
    InputStream fileInputStream;
    CompressionInputStream inputStream = null;
    try {
      FileObject fileObject = meta.getHeaderFileObject( getTransMeta() );
      fileInputStream = KettleVFS.getInputStream( fileObject );
      CompressionProvider provider = CompressionProviderFactory.getInstance().createCompressionProviderInstance(
        ( (TextFileInputMeta) meta ).content.fileCompression );
      inputStream = provider.createInputStream( fileInputStream );
    } catch ( final Exception e ) {
      logError( BaseMessages.getString( "FileInputDialog.ErrorGettingFileDesc.DialogMessage" ), e );
    }
    return inputStream;
  }
}
