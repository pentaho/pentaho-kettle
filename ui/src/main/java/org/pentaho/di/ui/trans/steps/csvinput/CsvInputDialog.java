/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.trans.steps.csvinput;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.provider.local.LocalFile;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LoggingRegistry;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.RowAdapter;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.steps.common.CsvInputAwareMeta;
import org.pentaho.di.trans.steps.csvinput.CsvInput;
import org.pentaho.di.trans.steps.csvinput.CsvInputMeta;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputField;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.dialog.EnterNumberDialog;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.di.ui.core.events.dialog.SelectionOperation;
import org.pentaho.di.ui.core.events.dialog.SelectionAdapterFileDialogTextVar;
import org.pentaho.di.ui.core.events.dialog.SelectionAdapterOptions;
import org.pentaho.di.ui.core.events.dialog.ProviderFilterType;
import org.pentaho.di.ui.core.events.dialog.FilterType;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.ComboValuesSelectionListener;
import org.pentaho.di.ui.core.widget.ComboVar;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.trans.TransGraph;
import org.pentaho.di.ui.trans.dialog.TransPreviewProgressDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.trans.step.common.CsvInputAwareImportProgressDialog;
import org.pentaho.di.ui.trans.step.common.CsvInputAwareStepDialog;
import org.pentaho.di.ui.trans.step.common.GetFieldsCapableStepDialog;
import org.pentaho.di.ui.trans.steps.textfileinput.TextFileCSVImportProgressDialog;

public class CsvInputDialog extends BaseStepDialog implements StepDialogInterface,
  GetFieldsCapableStepDialog<CsvInputMeta>, CsvInputAwareStepDialog {
  private static Class<?> PKG = CsvInput.class; // for i18n purposes, needed by Translator2!!

  private CsvInputMeta inputMeta;

  private TextVar wFilename;
  private CCombo wFilenameField;
  private Button wbbFilename; // Browse for a file
  private Button wIncludeFilename;
  private TextVar wRowNumField;
  private Button wbDelimiter;
  private TextVar wDelimiter;
  private TextVar wEnclosure;
  private TextVar wBufferSize;
  private Button wLazyConversion;
  private Button wHeaderPresent;
  private FormData fdAddResult;
  private FormData fdlAddResult;
  private TableView wFields;
  private Label wlAddResult;
  private Button wAddResult;
  private boolean isReceivingInput;
  private Button wRunningInParallel;
  private Button wNewlinePossible;
  private ComboVar wEncoding;
  private CCombo wFormat;

  private boolean gotEncodings = false;

  private Label wlRunningInParallel;

  private boolean initializing;

  private AtomicBoolean previewBusy;

  public CsvInputDialog( Shell parent, Object in, TransMeta tr, String sname ) {
    super( parent, (BaseStepMeta) in, tr, sname );
    inputMeta = (CsvInputMeta) in;
  }

  public String open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX );
    props.setLook( shell );
    setShellImage( shell, inputMeta );

    ModifyListener lsMod = new ModifyListener() {
      public void modifyText( ModifyEvent e ) {
        inputMeta.setChanged();
      }
    };
    changed = inputMeta.hasChanged();

    ModifyListener lsContent = new ModifyListener() {
      @Override
      public void modifyText( ModifyEvent arg0 ) {
        // asyncUpdatePreview();
      }
    };
    initializing = true;
    previewBusy = new AtomicBoolean( false );

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout( formLayout );
    shell.setText( BaseMessages.getString( PKG, "CsvInputDialog.Shell.Title" ) );

    int middle = props.getMiddlePct();
    int margin = Const.MARGIN;

    // Step name line
    //
    wlStepname = new Label( shell, SWT.RIGHT );
    wlStepname.setText( BaseMessages.getString( PKG, "CsvInputDialog.Stepname.Label" ) );
    props.setLook( wlStepname );
    fdlStepname = new FormData();
    fdlStepname.left = new FormAttachment( 0, 0 );
    fdlStepname.right = new FormAttachment( middle, -margin );
    fdlStepname.top = new FormAttachment( 0, margin );
    wlStepname.setLayoutData( fdlStepname );
    wStepname = new Text( shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wStepname );
    wStepname.addModifyListener( lsMod );
    fdStepname = new FormData();
    fdStepname.left = new FormAttachment( middle, 0 );
    fdStepname.top = new FormAttachment( 0, margin );
    fdStepname.right = new FormAttachment( 100, 0 );
    wStepname.setLayoutData( fdStepname );
    Control lastControl = wStepname;

    // See if the step receives input. If so, we don't ask for the filename, but
    // for the filename field.
    //
    isReceivingInput = transMeta.findNrPrevSteps( stepMeta ) > 0;
    if ( isReceivingInput ) {

      RowMetaInterface previousFields;
      try {
        previousFields = transMeta.getPrevStepFields( stepMeta );
      } catch ( KettleStepException e ) {
        new ErrorDialog( shell,
          BaseMessages.getString( PKG, "CsvInputDialog.ErrorDialog.UnableToGetInputFields.Title" ),
          BaseMessages.getString( PKG, "CsvInputDialog.ErrorDialog.UnableToGetInputFields.Message" ), e );
        previousFields = new RowMeta();
      }

      // The filename field ...
      //
      Label wlFilename = new Label( shell, SWT.RIGHT );
      wlFilename.setText( BaseMessages.getString( PKG, inputMeta.getDescription( "FILENAME_FIELD" ) ) );
      props.setLook( wlFilename );
      FormData fdlFilename = new FormData();
      fdlFilename.top = new FormAttachment( lastControl, margin );
      fdlFilename.left = new FormAttachment( 0, 0 );
      fdlFilename.right = new FormAttachment( middle, -margin );
      wlFilename.setLayoutData( fdlFilename );
      wFilenameField = new CCombo( shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
      wFilenameField.setItems( previousFields.getFieldNames() );
      props.setLook( wFilenameField );
      wFilenameField.addModifyListener( lsMod );
      FormData fdFilename = new FormData();
      fdFilename.top = new FormAttachment( lastControl, margin );
      fdFilename.left = new FormAttachment( middle, 0 );
      fdFilename.right = new FormAttachment( 100, 0 );
      wFilenameField.setLayoutData( fdFilename );
      lastControl = wFilenameField;

      // Checkbox to include the filename in the output...
      //
      Label wlIncludeFilename = new Label( shell, SWT.RIGHT );
      wlIncludeFilename.setText( BaseMessages.getString( PKG, inputMeta.getDescription( "INCLUDE_FILENAME" ) ) );
      props.setLook( wlIncludeFilename );
      FormData fdlIncludeFilename = new FormData();
      fdlIncludeFilename.top = new FormAttachment( lastControl, margin );
      fdlIncludeFilename.left = new FormAttachment( 0, 0 );
      fdlIncludeFilename.right = new FormAttachment( middle, -margin );
      wlIncludeFilename.setLayoutData( fdlIncludeFilename );
      wIncludeFilename = new Button( shell, SWT.CHECK );
      props.setLook( wIncludeFilename );
      wFilenameField.addModifyListener( lsMod );
      FormData fdIncludeFilename = new FormData();
      fdIncludeFilename.top = new FormAttachment( lastControl, margin );
      fdIncludeFilename.left = new FormAttachment( middle, 0 );
      fdIncludeFilename.right = new FormAttachment( 100, 0 );
      wIncludeFilename.setLayoutData( fdIncludeFilename );
      lastControl = wIncludeFilename;
    } else {

      // Filename...
      //
      // The filename browse button
      //
      wbbFilename = new Button( shell, SWT.PUSH | SWT.CENTER );
      props.setLook( wbbFilename );
      wbbFilename.setText( BaseMessages.getString( PKG, "System.Button.Browse" ) );
      wbbFilename.setToolTipText( BaseMessages.getString( PKG, "System.Tooltip.BrowseForFileOrDirAndAdd" ) );
      FormData fdbFilename = new FormData();
      fdbFilename.top = new FormAttachment( lastControl, margin );
      fdbFilename.right = new FormAttachment( 100, 0 );
      wbbFilename.setLayoutData( fdbFilename );

      // The field itself...
      //
      Label wlFilename = new Label( shell, SWT.RIGHT );
      wlFilename.setText( BaseMessages.getString( PKG, inputMeta.getDescription( "FILENAME" ) ) );
      props.setLook( wlFilename );
      FormData fdlFilename = new FormData();
      fdlFilename.top = new FormAttachment( lastControl, margin );
      fdlFilename.left = new FormAttachment( 0, 0 );
      fdlFilename.right = new FormAttachment( middle, -margin );
      wlFilename.setLayoutData( fdlFilename );
      wFilename = new TextVar( transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
      props.setLook( wFilename );
      wFilename.addModifyListener( lsMod );
      FormData fdFilename = new FormData();
      fdFilename.top = new FormAttachment( lastControl, margin );
      fdFilename.left = new FormAttachment( middle, 0 );
      fdFilename.right = new FormAttachment( wbbFilename, -margin );
      wFilename.setLayoutData( fdFilename );
      /*
       * wFilename.addFocusListener(new FocusAdapter() {
       *
       * @Override public void focusLost(FocusEvent arg0) { asyncUpdatePreview(); } });
       */
      lastControl = wFilename;
    }

    // delimiter
    Label wlDelimiter = new Label( shell, SWT.RIGHT );
    wlDelimiter.setText( BaseMessages.getString( PKG, inputMeta.getDescription( "DELIMITER" ) ) );
    props.setLook( wlDelimiter );
    FormData fdlDelimiter = new FormData();
    fdlDelimiter.top = new FormAttachment( lastControl, margin );
    fdlDelimiter.left = new FormAttachment( 0, 0 );
    fdlDelimiter.right = new FormAttachment( middle, -margin );
    wlDelimiter.setLayoutData( fdlDelimiter );
    wbDelimiter = new Button( shell, SWT.PUSH | SWT.CENTER );
    props.setLook( wbDelimiter );
    wbDelimiter.setText( BaseMessages.getString( PKG, "CsvInputDialog.Delimiter.Button" ) );
    FormData fdbDelimiter = new FormData();
    fdbDelimiter.top = new FormAttachment( lastControl, margin );
    fdbDelimiter.right = new FormAttachment( 100, 0 );
    wbDelimiter.setLayoutData( fdbDelimiter );
    wDelimiter = new TextVar( transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wDelimiter );
    wDelimiter.addModifyListener( lsMod );
    FormData fdDelimiter = new FormData();
    fdDelimiter.top = new FormAttachment( lastControl, margin );
    fdDelimiter.left = new FormAttachment( middle, 0 );
    fdDelimiter.right = new FormAttachment( wbDelimiter, -margin );
    wDelimiter.setLayoutData( fdDelimiter );
    wDelimiter.addModifyListener( lsContent );
    lastControl = wDelimiter;

    // enclosure
    Label wlEnclosure = new Label( shell, SWT.RIGHT );
    wlEnclosure.setText( BaseMessages.getString( PKG, inputMeta.getDescription( "ENCLOSURE" ) ) );
    props.setLook( wlEnclosure );
    FormData fdlEnclosure = new FormData();
    fdlEnclosure.top = new FormAttachment( lastControl, margin );
    fdlEnclosure.left = new FormAttachment( 0, 0 );
    fdlEnclosure.right = new FormAttachment( middle, -margin );
    wlEnclosure.setLayoutData( fdlEnclosure );
    wEnclosure = new TextVar( transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wEnclosure );
    wEnclosure.addModifyListener( lsMod );
    FormData fdEnclosure = new FormData();
    fdEnclosure.top = new FormAttachment( lastControl, margin );
    fdEnclosure.left = new FormAttachment( middle, 0 );
    fdEnclosure.right = new FormAttachment( 100, 0 );
    wEnclosure.setLayoutData( fdEnclosure );
    wEnclosure.addModifyListener( lsContent );
    lastControl = wEnclosure;

    // bufferSize
    //
    Label wlBufferSize = new Label( shell, SWT.RIGHT );
    wlBufferSize.setText( BaseMessages.getString( PKG, inputMeta.getDescription( "BUFFERSIZE" ) ) );
    props.setLook( wlBufferSize );
    FormData fdlBufferSize = new FormData();
    fdlBufferSize.top = new FormAttachment( lastControl, margin );
    fdlBufferSize.left = new FormAttachment( 0, 0 );
    fdlBufferSize.right = new FormAttachment( middle, -margin );
    wlBufferSize.setLayoutData( fdlBufferSize );
    wBufferSize = new TextVar( transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wBufferSize );
    wBufferSize.addModifyListener( lsMod );
    FormData fdBufferSize = new FormData();
    fdBufferSize.top = new FormAttachment( lastControl, margin );
    fdBufferSize.left = new FormAttachment( middle, 0 );
    fdBufferSize.right = new FormAttachment( 100, 0 );
    wBufferSize.setLayoutData( fdBufferSize );
    lastControl = wBufferSize;

    // performingLazyConversion?
    //
    Label wlLazyConversion = new Label( shell, SWT.RIGHT );
    wlLazyConversion.setText( BaseMessages.getString( PKG, inputMeta.getDescription( "LAZY_CONVERSION" ) ) );
    props.setLook( wlLazyConversion );
    FormData fdlLazyConversion = new FormData();
    fdlLazyConversion.top = new FormAttachment( lastControl, margin );
    fdlLazyConversion.left = new FormAttachment( 0, 0 );
    fdlLazyConversion.right = new FormAttachment( middle, -margin );
    wlLazyConversion.setLayoutData( fdlLazyConversion );
    wLazyConversion = new Button( shell, SWT.CHECK );
    props.setLook( wLazyConversion );
    FormData fdLazyConversion = new FormData();
    fdLazyConversion.top = new FormAttachment( lastControl, margin );
    fdLazyConversion.left = new FormAttachment( middle, 0 );
    fdLazyConversion.right = new FormAttachment( 100, 0 );
    wLazyConversion.setLayoutData( fdLazyConversion );
    lastControl = wLazyConversion;

    // header row?
    //
    Label wlHeaderPresent = new Label( shell, SWT.RIGHT );
    wlHeaderPresent.setText( BaseMessages.getString( PKG, inputMeta.getDescription( "HEADER_PRESENT" ) ) );
    props.setLook( wlHeaderPresent );
    FormData fdlHeaderPresent = new FormData();
    fdlHeaderPresent.top = new FormAttachment( lastControl, margin );
    fdlHeaderPresent.left = new FormAttachment( 0, 0 );
    fdlHeaderPresent.right = new FormAttachment( middle, -margin );
    wlHeaderPresent.setLayoutData( fdlHeaderPresent );
    wHeaderPresent = new Button( shell, SWT.CHECK );
    props.setLook( wHeaderPresent );
    FormData fdHeaderPresent = new FormData();
    fdHeaderPresent.top = new FormAttachment( lastControl, margin );
    fdHeaderPresent.left = new FormAttachment( middle, 0 );
    fdHeaderPresent.right = new FormAttachment( 100, 0 );
    wHeaderPresent.setLayoutData( fdHeaderPresent );
    /*
     * wHeaderPresent.addSelectionListener(new SelectionAdapter() {
     *
     * @Override public void widgetSelected(SelectionEvent arg0) { asyncUpdatePreview(); } });
     */
    lastControl = wHeaderPresent;

    wlAddResult = new Label( shell, SWT.RIGHT );
    wlAddResult.setText( BaseMessages.getString( PKG, inputMeta.getDescription( "ADD_FILENAME_RESULT" ) ) );
    props.setLook( wlAddResult );
    fdlAddResult = new FormData();
    fdlAddResult.left = new FormAttachment( 0, 0 );
    fdlAddResult.top = new FormAttachment( wHeaderPresent, margin );
    fdlAddResult.right = new FormAttachment( middle, -margin );
    wlAddResult.setLayoutData( fdlAddResult );
    wAddResult = new Button( shell, SWT.CHECK );
    props.setLook( wAddResult );
    wAddResult.setToolTipText( BaseMessages.getString( PKG, inputMeta.getTooltip( "ADD_FILENAME_RESULT" ) ) );
    fdAddResult = new FormData();
    fdAddResult.left = new FormAttachment( middle, 0 );
    fdAddResult.top = new FormAttachment( wHeaderPresent, margin );
    wAddResult.setLayoutData( fdAddResult );
    lastControl = wAddResult;

    // The field itself...
    //
    Label wlRowNumField = new Label( shell, SWT.RIGHT );
    wlRowNumField.setText( BaseMessages.getString( PKG, inputMeta.getDescription( "ROW_NUM_FIELD" ) ) );
    props.setLook( wlRowNumField );
    FormData fdlRowNumField = new FormData();
    fdlRowNumField.top = new FormAttachment( lastControl, margin );
    fdlRowNumField.left = new FormAttachment( 0, 0 );
    fdlRowNumField.right = new FormAttachment( middle, -margin );
    wlRowNumField.setLayoutData( fdlRowNumField );
    wRowNumField = new TextVar( transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wRowNumField );
    wRowNumField.addModifyListener( lsMod );
    FormData fdRowNumField = new FormData();
    fdRowNumField.top = new FormAttachment( lastControl, margin );
    fdRowNumField.left = new FormAttachment( middle, 0 );
    fdRowNumField.right = new FormAttachment( 100, 0 );
    wRowNumField.setLayoutData( fdRowNumField );
    lastControl = wRowNumField;

    // running in parallel?
    //
    wlRunningInParallel = new Label( shell, SWT.RIGHT );
    wlRunningInParallel.setText( BaseMessages.getString( PKG, inputMeta.getDescription( "PARALLEL" ) ) );
    props.setLook( wlRunningInParallel );
    FormData fdlRunningInParallel = new FormData();
    fdlRunningInParallel.top = new FormAttachment( lastControl, margin );
    fdlRunningInParallel.left = new FormAttachment( 0, 0 );
    fdlRunningInParallel.right = new FormAttachment( middle, -margin );
    wlRunningInParallel.setLayoutData( fdlRunningInParallel );
    wRunningInParallel = new Button( shell, SWT.CHECK );
    props.setLook( wRunningInParallel );
    FormData fdRunningInParallel = new FormData();
    fdRunningInParallel.top = new FormAttachment( lastControl, margin );
    fdRunningInParallel.left = new FormAttachment( middle, 0 );
    wRunningInParallel.setLayoutData( fdRunningInParallel );
    lastControl = wRunningInParallel;

    // Is a new line possible in a field?
    //
    Label wlNewlinePossible = new Label( shell, SWT.RIGHT );
    wlNewlinePossible.setText( BaseMessages.getString( PKG, inputMeta.getDescription( "NEWLINE_POSSIBLE" ) ) );
    props.setLook( wlNewlinePossible );
    FormData fdlNewlinePossible = new FormData();
    fdlNewlinePossible.top = new FormAttachment( lastControl, margin );
    fdlNewlinePossible.left = new FormAttachment( 0, 0 );
    fdlNewlinePossible.right = new FormAttachment( middle, -margin );
    wlNewlinePossible.setLayoutData( fdlNewlinePossible );
    wNewlinePossible = new Button( shell, SWT.CHECK );
    props.setLook( wNewlinePossible );
    FormData fdNewlinePossible = new FormData();
    fdNewlinePossible.top = new FormAttachment( lastControl, margin );
    fdNewlinePossible.left = new FormAttachment( middle, 0 );
    wNewlinePossible.setLayoutData( fdNewlinePossible );
    wNewlinePossible.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent event ) {
        setFlags();
      }
    } );
    wNewlinePossible.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent arg0 ) {
        asyncUpdatePreview();
      }
    } );
    lastControl = wNewlinePossible;

    // Format
    Label wlFormat = new Label( shell, SWT.RIGHT );
    wlFormat.setText( BaseMessages.getString( PKG, inputMeta.getDescription( "FORMAT" ) ) );
    props.setLook( wlFormat );
    FormData fdlFormat = new FormData();
    fdlFormat.top = new FormAttachment( lastControl, margin );
    fdlFormat.left = new FormAttachment( 0, 0 );
    fdlFormat.right = new FormAttachment( middle, -margin );
    wlFormat.setLayoutData( fdlFormat );
    wFormat = new CCombo( shell, SWT.BORDER | SWT.READ_ONLY );
    wFormat.setText( BaseMessages.getString( PKG, inputMeta.getDescription( "FORMAT" ) ) );
    props.setLook( wFormat );
    wFormat.add( "DOS" );
    wFormat.add( "Unix" );
    wFormat.add( "mixed" );
    wFormat.select( 2 );
    wFormat.addModifyListener( lsMod );
    FormData fdFormat = new FormData();
    fdFormat.top = new FormAttachment( lastControl, margin );
    fdFormat.left = new FormAttachment( middle, 0 );
    fdFormat.right = new FormAttachment( 100, 0 );
    wFormat.setLayoutData( fdFormat );
    lastControl = wFormat;

    // Encoding
    Label wlEncoding = new Label( shell, SWT.RIGHT );
    wlEncoding.setText( BaseMessages.getString( PKG, inputMeta.getDescription( "ENCODING" ) ) );
    props.setLook( wlEncoding );
    FormData fdlEncoding = new FormData();
    fdlEncoding.top = new FormAttachment( lastControl, margin );
    fdlEncoding.left = new FormAttachment( 0, 0 );
    fdlEncoding.right = new FormAttachment( middle, -margin );
    wlEncoding.setLayoutData( fdlEncoding );
    wEncoding = new ComboVar( transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wEncoding );
    wEncoding.addModifyListener( lsMod );
    FormData fdEncoding = new FormData();
    fdEncoding.top = new FormAttachment( lastControl, margin );
    fdEncoding.left = new FormAttachment( middle, 0 );
    fdEncoding.right = new FormAttachment( 100, 0 );
    wEncoding.setLayoutData( fdEncoding );
    wEncoding.addModifyListener( lsContent );
    lastControl = wEncoding;

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

    // Some buttons first, so that the dialog scales nicely...
    //
    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );
    wPreview = new Button( shell, SWT.PUSH );
    wPreview.setText( BaseMessages.getString( PKG, "System.Button.Preview" ) );
    wPreview.setEnabled( !isReceivingInput );
    wGet = new Button( shell, SWT.PUSH );
    wGet.setText( BaseMessages.getString( PKG, "System.Button.GetFields" ) );
    wGet.setEnabled( !isReceivingInput );

    setButtonPositions( new Button[] { wOK, wGet, wPreview, wCancel }, margin, null );

    // Fields
    ColumnInfo[] colinf =
      new ColumnInfo[] {
        new ColumnInfo(
          BaseMessages.getString( PKG, inputMeta.getDescription( "FIELD_NAME" ) ),
          ColumnInfo.COLUMN_TYPE_TEXT, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, inputMeta.getDescription( "FIELD_TYPE" ) ),
          ColumnInfo.COLUMN_TYPE_CCOMBO, ValueMetaFactory.getValueMetaNames(), true ),
        new ColumnInfo(
          BaseMessages.getString( PKG, inputMeta.getDescription( "FIELD_FORMAT" ) ),
          ColumnInfo.COLUMN_TYPE_FORMAT, 2 ),
        new ColumnInfo(
          BaseMessages.getString( PKG, inputMeta.getDescription( "FIELD_LENGTH" ) ),
          ColumnInfo.COLUMN_TYPE_TEXT, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, inputMeta.getDescription( "FIELD_PRECISION" ) ),
          ColumnInfo.COLUMN_TYPE_TEXT, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, inputMeta.getDescription( "FIELD_CURRENCY" ) ),
          ColumnInfo.COLUMN_TYPE_TEXT, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, inputMeta.getDescription( "FIELD_DECIMAL" ) ),
          ColumnInfo.COLUMN_TYPE_TEXT, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, inputMeta.getDescription( "FIELD_GROUP" ) ),
          ColumnInfo.COLUMN_TYPE_TEXT, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, inputMeta.getDescription( "FIELD_TRIM_TYPE" ) ),
          ColumnInfo.COLUMN_TYPE_CCOMBO, ValueMetaString.trimTypeDesc ), };

    colinf[2].setComboValuesSelectionListener( new ComboValuesSelectionListener() {

      public String[] getComboValues( TableItem tableItem, int rowNr, int colNr ) {
        String[] comboValues = new String[] {};
        int type = ValueMetaFactory.getIdForValueMeta( tableItem.getText( colNr - 1 ) );
        switch ( type ) {
          case ValueMetaInterface.TYPE_DATE:
            comboValues = Const.getDateFormats();
            break;
          case ValueMetaInterface.TYPE_INTEGER:
          case ValueMetaInterface.TYPE_BIGNUMBER:
          case ValueMetaInterface.TYPE_NUMBER:
            comboValues = Const.getNumberFormats();
            break;
          default:
            break;
        }
        return comboValues;
      }

    } );

    wFields = new TableView( transMeta, shell, SWT.FULL_SELECTION | SWT.MULTI, colinf, 1, lsMod, props );

    FormData fdFields = new FormData();
    fdFields.top = new FormAttachment( lastControl, margin * 2 );
    fdFields.bottom = new FormAttachment( wOK, -margin * 2 );
    fdFields.left = new FormAttachment( 0, 0 );
    fdFields.right = new FormAttachment( 100, 0 );
    wFields.setLayoutData( fdFields );
    wFields.setContentListener( lsContent );

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
    lsPreview = new Listener() {
      public void handleEvent( Event e ) {
        preview();
      }
    };
    lsGet = new Listener() {
      public void handleEvent( Event e ) {
        getFields();
      }
    };

    wCancel.addListener( SWT.Selection, lsCancel );
    wOK.addListener( SWT.Selection, lsOK );
    wPreview.addListener( SWT.Selection, lsPreview );
    wGet.addListener( SWT.Selection, lsGet );

    lsDef = new SelectionAdapter() {
      public void widgetDefaultSelected( SelectionEvent e ) {
        ok();
      }
    };

    wStepname.addSelectionListener( lsDef );
    if ( wFilename != null ) {
      wFilename.addSelectionListener( lsDef );
    }
    if ( wFilenameField != null ) {
      wFilenameField.addSelectionListener( lsDef );
    }
    wDelimiter.addSelectionListener( lsDef );
    wEnclosure.addSelectionListener( lsDef );
    wBufferSize.addSelectionListener( lsDef );
    wRowNumField.addSelectionListener( lsDef );

    // Allow the insertion of tabs as separator...
    wbDelimiter.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent se ) {
        Text t = wDelimiter.getTextWidget();
        if ( t != null ) {
          t.insert( "\t" );
        }
      }
    } );

    if ( wbbFilename != null ) {
      // Listen to the browse button next to the file name
      wbbFilename.addSelectionListener( new SelectionAdapterFileDialogTextVar( log, wFilename, transMeta,
        new SelectionAdapterOptions( SelectionOperation.FILE,
          new String[] { FilterType.CSV_TXT.toString(), FilterType.CSV.toString(), FilterType.TXT.toString(),
            FilterType.ALL.toString() }, FilterType.CSV_TXT.toString(),
          new String[] { ProviderFilterType.LOCAL.toString() }, false ) ) );
    }

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    // Set the shell size, based upon previous time...
    setSize();

    getData();

    inputMeta.setChanged( changed );
    initializing = false;

    // Update the preview window.
    //
    // asyncUpdatePreview();

    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return stepname;
  }

  protected void setFlags() {
    // In case there are newlines in fields, we can't load data in parallel
    //
    boolean parallelPossible = !wNewlinePossible.getSelection();
    wlRunningInParallel.setEnabled( parallelPossible );
    wRunningInParallel.setEnabled( parallelPossible );
    if ( !parallelPossible ) {
      wRunningInParallel.setSelection( false );
    }
  }

  private void setEncodings() {
    // Encoding of the text file:
    if ( !gotEncodings ) {
      gotEncodings = true;

      wEncoding.removeAll();
      List<Charset> values = new ArrayList<Charset>( Charset.availableCharsets().values() );
      for ( int i = 0; i < values.size(); i++ ) {
        Charset charSet = values.get( i );
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

  @Override
  public CsvInputMeta getNewMetaInstance() {
    return new CsvInputMeta();
  }

  @Override
  public void populateMeta( CsvInputMeta inputMeta ) {
    getInfo( inputMeta );
  }

  public void getData() {
    getData( inputMeta, true );
  }

  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void getData( CsvInputMeta inputMeta, boolean copyStepname ) {
    getData( inputMeta, copyStepname, true, null );
  }

  @Override
  public void getData( final CsvInputMeta inputMeta, final boolean copyStepname, final boolean reloadAllFields,
                       final Set<String> newFieldNames ) {
    if ( copyStepname ) {
      wStepname.setText( stepname );
    }
    if ( isReceivingInput ) {
      wFilenameField.setText( Const.NVL( inputMeta.getFilenameField(), "" ) );
      wIncludeFilename.setSelection( inputMeta.isIncludingFilename() );
    } else {
      wFilename.setText( Const.NVL( inputMeta.getFilename(), "" ) );
    }
    wDelimiter.setText( Const.NVL( inputMeta.getDelimiter(), "" ) );
    wEnclosure.setText( Const.NVL( inputMeta.getEnclosure(), "" ) );
    wBufferSize.setText( Const.NVL( inputMeta.getBufferSize(), "" ) );
    wLazyConversion.setSelection( inputMeta.isLazyConversionActive() );
    wHeaderPresent.setSelection( inputMeta.isHeaderPresent() );
    wRunningInParallel.setSelection( inputMeta.isRunningInParallel() );
    wNewlinePossible.setSelection( inputMeta.isNewlinePossibleInFields() );
    wRowNumField.setText( Const.NVL( inputMeta.getRowNumField(), "" ) );
    wAddResult.setSelection( inputMeta.isAddResultFile() );
    wFormat.setText( Const.NVL( inputMeta.getFileFormat(), "" ) );
    wEncoding.setText( Const.NVL( inputMeta.getEncoding(), "" ) );

    final List<String> fieldName = newFieldNames == null ? new ArrayList()
      : newFieldNames.stream().map( String::toString ).collect( Collectors.toList() );
    for ( int i = 0; i < inputMeta.getInputFields().length; i++ ) {
      TextFileInputField field = inputMeta.getInputFields()[i];
      final TableItem item = getTableItem( field.getName(), reloadAllFields );
      // update the item only if we are reloading all fields, or the field is new
      if ( !reloadAllFields && !fieldName.contains( field.getName() ) ) {
        continue;
      }
      int colnr = 1;
      item.setText( colnr++, Const.NVL( field.getName(), "" ) );
      item.setText( colnr++, ValueMetaFactory.getValueMetaName( field.getType() ) );
      item.setText( colnr++, Const.NVL( field.getFormat(), "" ) );
      item.setText( colnr++, field.getLength() >= 0 ? Integer.toString( field.getLength() ) : "" );
      item.setText( colnr++, field.getPrecision() >= 0 ? Integer.toString( field.getPrecision() ) : "" );
      item.setText( colnr++, Const.NVL( field.getCurrencySymbol(), "" ) );
      item.setText( colnr++, Const.NVL( field.getDecimalSymbol(), "" ) );
      item.setText( colnr++, Const.NVL( field.getGroupSymbol(), "" ) );
      item.setText( colnr++, Const.NVL( field.getTrimTypeDesc(), "" ) );
    }
    wFields.removeEmptyRows();
    wFields.setRowNums();
    wFields.optWidth( true );

    setFlags();

    wStepname.selectAll();
    wStepname.setFocus();
  }

  private void cancel() {
    stepname = null;
    inputMeta.setChanged( changed );
    dispose();
  }

  private void getInfo( CsvInputMeta inputMeta ) {

    if ( isReceivingInput ) {
      inputMeta.setFilenameField( wFilenameField.getText() );
      inputMeta.setIncludingFilename( wIncludeFilename.getSelection() );
    } else {
      inputMeta.setFilename( wFilename.getText() );
    }

    inputMeta.setDelimiter( wDelimiter.getText() );
    inputMeta.setEnclosure( wEnclosure.getText() );
    inputMeta.setBufferSize( wBufferSize.getText() );
    inputMeta.setLazyConversionActive( wLazyConversion.getSelection() );
    inputMeta.setHeaderPresent( wHeaderPresent.getSelection() );
    inputMeta.setRowNumField( wRowNumField.getText() );
    inputMeta.setAddResultFile( wAddResult.getSelection() );
    inputMeta.setRunningInParallel( wRunningInParallel.getSelection() );
    inputMeta.setNewlinePossibleInFields( wNewlinePossible.getSelection() );
    inputMeta.setFileFormat( wFormat.getText() );
    inputMeta.setEncoding( wEncoding.getText() );

    int nrNonEmptyFields = wFields.nrNonEmpty();
    inputMeta.allocate( nrNonEmptyFields );

    for ( int i = 0; i < nrNonEmptyFields; i++ ) {
      TableItem item = wFields.getNonEmpty( i );
      //CHECKSTYLE:Indentation:OFF
      inputMeta.getInputFields()[i] = new TextFileInputField();

      int colnr = 1;
      inputMeta.getInputFields()[i].setName( item.getText( colnr++ ) );
      inputMeta.getInputFields()[i].setType( ValueMetaFactory.getIdForValueMeta( item.getText( colnr++ ) ) );
      inputMeta.getInputFields()[i].setFormat( item.getText( colnr++ ) );
      inputMeta.getInputFields()[i].setLength( Const.toInt( item.getText( colnr++ ), -1 ) );
      inputMeta.getInputFields()[i].setPrecision( Const.toInt( item.getText( colnr++ ), -1 ) );
      inputMeta.getInputFields()[i].setCurrencySymbol( item.getText( colnr++ ) );
      inputMeta.getInputFields()[i].setDecimalSymbol( item.getText( colnr++ ) );
      inputMeta.getInputFields()[i].setGroupSymbol( item.getText( colnr++ ) );
      inputMeta.getInputFields()[i].setTrimType( ValueMetaString.getTrimTypeByDesc( item.getText( colnr++ ) ) );
    }
    wFields.removeEmptyRows();
    wFields.setRowNums();
    wFields.optWidth( true );

    inputMeta.setChanged();
  }

  private void ok() {
    if ( Utils.isEmpty( wStepname.getText() ) ) {
      return;
    }

    getInfo( inputMeta );
    stepname = wStepname.getText();
    dispose();
  }

  /**'
   * Returns the {@link InputStreamReader} corresponding to the csv file, or null if the file cannot be read.
   * @return the {@link InputStreamReader} corresponding to the csv file, or null if the file cannot be read
   */
  private InputStreamReader getReader( final CsvInputMeta meta, final InputStream inputStream ) {
    InputStreamReader reader = null;
    try {
      String filename = transMeta.environmentSubstitute( meta.getFilename() );

      FileObject fileObject = KettleVFS.getFileObject( filename );
      if ( !( fileObject instanceof LocalFile ) ) {
        // We can only use NIO on local files at the moment, so that's what we
        // limit ourselves to.
        //
        throw new KettleException( BaseMessages.getString( PKG, "CsvInput.Log.OnlyLocalFilesAreSupported" ) );
      }

      String realEncoding = transMeta.environmentSubstitute( meta.getEncoding() );
      if ( Utils.isEmpty( realEncoding ) ) {
        reader = new InputStreamReader( inputStream );
      } else {
        reader = new InputStreamReader( inputStream, realEncoding );
      }
    } catch ( final Exception e ) {
      logError( BaseMessages.getString( PKG, "CsvInputDialog.ErrorGettingFileDesc.DialogMessage" ), e );
    }
    return reader;
  }

  /**'
   * Returns the {@link InputStream} corresponding to the csv file, or null if the file cannot be read.
   * @return the {@link InputStream} corresponding to the csv file, or null if the file cannot be read
   */
  private InputStream getInputStream( final CsvInputMeta meta ) {
    InputStream inputStream = null;
    try {
      final String filename = transMeta.environmentSubstitute( meta.getFilename() );

      final FileObject fileObject = KettleVFS.getFileObject( filename );
      if ( !( fileObject instanceof LocalFile ) ) {
        // We can only use NIO on local files at the moment, so that's what we
        // limit ourselves to.
        //
        throw new KettleException( BaseMessages.getString( PKG, "CsvInput.Log.OnlyLocalFilesAreSupported" ) );
      }

      inputStream = KettleVFS.getInputStream( fileObject );
    } catch ( final Exception e ) {
      logError( BaseMessages.getString( PKG, "CsvInputDialog.ErrorGettingFileDesc.DialogMessage" ), e );
    }
    return inputStream;
  }

  @Override
  public String[] getFieldNames( final CsvInputMeta meta ) {
    return getFieldNames( (CsvInputAwareMeta) meta );
  }

  @Override
  public TableView getFieldsTable() {
    return this.wFields;
  }

  @Override
  public String loadFieldsImpl( final CsvInputMeta meta, final int samples ) {
    return loadFieldsImpl( (CsvInputAwareMeta) meta, samples );
  }

  // Preview the data
  private synchronized void preview() {
    // Create the XML input step
    CsvInputMeta oneMeta = new CsvInputMeta();
    getInfo( oneMeta );

    TransMeta previewMeta =
      TransPreviewFactory.generatePreviewTransformation( transMeta, oneMeta, wStepname.getText() );
    transMeta.getVariable( "Internal.Transformation.Filename.Directory" );
    previewMeta.getVariable( "Internal.Transformation.Filename.Directory" );

    EnterNumberDialog numberDialog =
      new EnterNumberDialog( shell, props.getDefaultPreviewSize(), BaseMessages.getString(
        PKG, "CsvInputDialog.PreviewSize.DialogTitle" ), BaseMessages.getString(
        PKG, "CsvInputDialog.PreviewSize.DialogMessage" ) );
    int previewSize = numberDialog.open();
    if ( previewSize > 0 ) {
      TransPreviewProgressDialog progressDialog =
        new TransPreviewProgressDialog(
          shell, previewMeta, new String[] { wStepname.getText() }, new int[] { previewSize } );
      progressDialog.open();

      Trans trans = progressDialog.getTrans();
      String loggingText = progressDialog.getLoggingText();

      if ( !progressDialog.isCancelled() ) {
        if ( trans.getResult() != null && trans.getResult().getNrErrors() > 0 ) {
          EnterTextDialog etd =
            new EnterTextDialog(
              shell, BaseMessages.getString( PKG, "System.Dialog.PreviewError.Title" ), BaseMessages
                .getString( PKG, "System.Dialog.PreviewError.Message" ), loggingText, true );
          etd.setReadOnly();
          etd.open();
        }
      }

      PreviewRowsDialog prd =
        new PreviewRowsDialog(
          shell, transMeta, SWT.NONE, wStepname.getText(), progressDialog.getPreviewRowsMeta( wStepname
            .getText() ), progressDialog.getPreviewRows( wStepname.getText() ), loggingText );
      prd.open();
    }
  }

  /**
   * Load metadata from step window
   */
  protected void updatePreview() {
    if ( initializing ) {
      return;
    }
    if ( previewBusy.get() ) {
      return;
    }
    try {
      previewBusy.set( true );

      CsvInputMeta meta = new CsvInputMeta();
      getInfo( meta );

      // Validate some basic data...
      //
      if ( Utils.isEmpty( meta.getFilename() ) ) {
        return;
      }
      if ( Utils.isEmpty( meta.getInputFields() ) ) {
        return;
      }

      String stepname = wStepname.getText();

      // StepMeta stepMeta = new StepMeta(stepname, meta);
      StringBuffer buffer = new StringBuffer();
      final List<Object[]> rowsData = new ArrayList<Object[]>();
      final RowMetaInterface rowMeta = new RowMeta();

      try {

        meta.getFields( rowMeta, stepname, null, null, transMeta, repository, metaStore );

        TransMeta previewTransMeta = TransPreviewFactory.generatePreviewTransformation( transMeta, meta, stepname );
        final Trans trans = new Trans( previewTransMeta );
        trans.prepareExecution( null );
        StepInterface step = trans.getRunThread( stepname, 0 );
        step.addRowListener( new RowAdapter() {
          @Override
          public void rowWrittenEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
            rowsData.add( row );

            // If we have enough rows we can stop
            //
            if ( rowsData.size() > PropsUI.getInstance().getDefaultPreviewSize() ) {
              trans.stopAll();
            }
          }
        } );
        trans.startThreads();
        trans.waitUntilFinished();
        if ( trans.getErrors() > 0 ) {
          StringBuffer log = KettleLogStore.getAppender().getBuffer( trans.getLogChannelId(), false );
          buffer.append( log );
        }
        KettleLogStore.discardLines( trans.getLogChannelId(), false );
        LoggingRegistry.getInstance().removeIncludingChildren( trans.getLogChannelId() );

      } catch ( Exception e ) {
        buffer.append( Const.getStackTracker( e ) );
      }

      TransGraph transGraph = Spoon.getInstance().getActiveTransGraph();
      if ( transGraph != null ) {
        if ( !transGraph.isExecutionResultsPaneVisible() ) {
          transGraph.showExecutionResults();
        }

        transGraph.extraViewTabFolder.setSelection( 5 );

        transGraph.transPreviewDelegate.addPreviewData( stepMeta, rowMeta, rowsData, buffer );
        transGraph.transPreviewDelegate.setSelectedStep( stepMeta );
        transGraph.transPreviewDelegate.refreshView();
      }
    } finally {
      previewBusy.set( false );
    }
  }

  protected void asyncUpdatePreview() {

    Runnable update = new Runnable() {

      @Override
      public void run() {
        try {
          updatePreview();
        } catch ( SWTException e ) {
          // Ignore widget disposed errors
        }
      }
    };

    shell.getDisplay().asyncExec( update );
  }

  @Override
  public Shell getShell() {
    return this.shell;
  }

  @Override
  public CsvInputAwareImportProgressDialog getCsvImportProgressDialog(
    final CsvInputAwareMeta meta, final int samples, final InputStreamReader reader ) {
    return new TextFileCSVImportProgressDialog( getShell(), (CsvInputMeta) meta, transMeta, reader, samples, true );
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
    InputStream inputStream = null;
    try {
      FileObject fileObject = meta.getHeaderFileObject( getTransMeta() );
      if ( !( fileObject instanceof LocalFile ) ) {
        // We can only use NIO on local files at the moment, so that's what we limit ourselves to.
        throw new KettleException( BaseMessages.getString( "FileInputDialog.Log.OnlyLocalFilesAreSupported" ) );
      }

      inputStream = KettleVFS.getInputStream( fileObject );
    } catch ( final Exception e ) {
      logError( BaseMessages.getString( "FileInputDialog.ErrorGettingFileDesc.DialogMessage" ), e );
    }
    return inputStream;
  }

}
