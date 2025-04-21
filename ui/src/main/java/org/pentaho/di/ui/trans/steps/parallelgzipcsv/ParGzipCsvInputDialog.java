/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.ui.trans.steps.parallelgzipcsv;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.provider.local.LocalFile;
import org.eclipse.swt.SWT;
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
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
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
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.parallelgzipcsv.ParGzipCsvInputMeta;
import org.pentaho.di.trans.steps.textfileinput.EncodingType;
import org.pentaho.di.trans.steps.textfileinput.TextFileInput;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputField;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputMeta;
import org.pentaho.di.ui.core.dialog.EnterNumberDialog;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.di.ui.core.events.dialog.FilterType;
import org.pentaho.di.ui.core.events.dialog.SelectionOperation;

import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.ComboValuesSelectionListener;
import org.pentaho.di.ui.core.widget.ComboVar;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.dialog.TransPreviewProgressDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.trans.steps.textfileinput.TextFileCSVImportProgressDialog;
import org.pentaho.di.ui.util.DialogHelper;

public class ParGzipCsvInputDialog extends BaseStepDialog implements StepDialogInterface {
  private static Class<?> PKG = ParGzipCsvInputMeta.class; // for i18n purposes, needed by Translator2!!

  private ParGzipCsvInputMeta inputMeta;

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
  private ComboVar wEncoding;

  private boolean gotEncodings = false;

  public ParGzipCsvInputDialog( Shell parent, Object in, TransMeta tr, String sname ) {
    super( parent, (BaseStepMeta) in, tr, sname );
    inputMeta = (ParGzipCsvInputMeta) in;
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

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout( formLayout );
    shell.setText( BaseMessages.getString( PKG, "ParGzipCsvInputDialog.Shell.Title" ) );

    int middle = props.getMiddlePct();
    int margin = Const.MARGIN;

    // Step name line
    //
    wlStepname = new Label( shell, SWT.RIGHT );
    wlStepname.setText( BaseMessages.getString( PKG, "ParGzipCsvInputDialog.Stepname.Label" ) );
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

    // See if the step receives input. If so, we don't ask for the filename, but for the filename field.
    //
    isReceivingInput = transMeta.findNrPrevSteps( stepMeta ) > 0;
    if ( isReceivingInput ) {

      RowMetaInterface previousFields;
      try {
        previousFields = transMeta.getPrevStepFields( stepMeta );
      } catch ( KettleStepException e ) {
        new ErrorDialog( shell,
          BaseMessages.getString( PKG, "ParGzipCsvInputDialog.ErrorDialog.UnableToGetInputFields.Title" ),
          BaseMessages.getString( PKG, "ParGzipCsvInputDialog.ErrorDialog.UnableToGetInputFields.Message" ), e );
        previousFields = new RowMeta();
      }

      // The filename field ...
      //
      Label wlFilename = new Label( shell, SWT.RIGHT );
      wlFilename.setText( BaseMessages.getString( PKG, "ParGzipCsvInputDialog.FilenameField.Label" ) );
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
      wlIncludeFilename
        .setText( BaseMessages.getString( PKG, "ParGzipCsvInputDialog.IncludeFilenameField.Label" ) );
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
      wlFilename.setText( BaseMessages.getString( PKG, "ParGzipCsvInputDialog.Filename.Label" ) );
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
      lastControl = wFilename;
    }

    // delimiter
    Label wlDelimiter = new Label( shell, SWT.RIGHT );
    wlDelimiter.setText( BaseMessages.getString( PKG, "ParGzipCsvInputDialog.Delimiter.Label" ) );
    props.setLook( wlDelimiter );
    FormData fdlDelimiter = new FormData();
    fdlDelimiter.top = new FormAttachment( lastControl, margin );
    fdlDelimiter.left = new FormAttachment( 0, 0 );
    fdlDelimiter.right = new FormAttachment( middle, -margin );
    wlDelimiter.setLayoutData( fdlDelimiter );
    wbDelimiter = new Button( shell, SWT.PUSH | SWT.CENTER );
    props.setLook( wbDelimiter );
    wbDelimiter.setText( BaseMessages.getString( PKG, "ParGzipCsvInputDialog.Delimiter.Button" ) );
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
    lastControl = wDelimiter;

    // enclosure
    Label wlEnclosure = new Label( shell, SWT.RIGHT );
    wlEnclosure.setText( BaseMessages.getString( PKG, "ParGzipCsvInputDialog.Enclosure.Label" ) );
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
    lastControl = wEnclosure;

    // bufferSize
    //
    Label wlBufferSize = new Label( shell, SWT.RIGHT );
    wlBufferSize.setText( BaseMessages.getString( PKG, "ParGzipCsvInputDialog.BufferSize.Label" ) );
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
    wlLazyConversion.setText( BaseMessages.getString( PKG, "ParGzipCsvInputDialog.LazyConversion.Label" ) );
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
    wlHeaderPresent.setText( BaseMessages.getString( PKG, "ParGzipCsvInputDialog.HeaderPresent.Label" ) );
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
    lastControl = wHeaderPresent;

    wlAddResult = new Label( shell, SWT.RIGHT );
    wlAddResult.setText( BaseMessages.getString( PKG, "ParGzipCsvInputDialog.AddResult.Label" ) );
    props.setLook( wlAddResult );
    fdlAddResult = new FormData();
    fdlAddResult.left = new FormAttachment( 0, 0 );
    fdlAddResult.top = new FormAttachment( wHeaderPresent, margin );
    fdlAddResult.right = new FormAttachment( middle, -margin );
    wlAddResult.setLayoutData( fdlAddResult );
    wAddResult = new Button( shell, SWT.CHECK );
    props.setLook( wAddResult );
    wAddResult.setToolTipText( BaseMessages.getString( PKG, "ParGzipCsvInputDialog.AddResult.Tooltip" ) );
    fdAddResult = new FormData();
    fdAddResult.left = new FormAttachment( middle, 0 );
    fdAddResult.top = new FormAttachment( wHeaderPresent, margin );
    wAddResult.setLayoutData( fdAddResult );
    lastControl = wAddResult;

    // The field itself...
    //
    Label wlRowNumField = new Label( shell, SWT.RIGHT );
    wlRowNumField.setText( BaseMessages.getString( PKG, "ParGzipCsvInputDialog.RowNumField.Label" ) );
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
    Label wlRunningInParallel = new Label( shell, SWT.RIGHT );
    wlRunningInParallel.setText( BaseMessages.getString( PKG, "ParGzipCsvInputDialog.RunningInParallel.Label" ) );
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

    // Encoding
    Label wlEncoding = new Label( shell, SWT.RIGHT );
    wlEncoding.setText( BaseMessages.getString( PKG, "ParGzipCsvInputDialog.Encoding.Label" ) );
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

    setButtonPositions( new Button[] { wOK, wPreview, wGet, wCancel }, margin, null );

    // Fields
    ColumnInfo[] colinf =
      new ColumnInfo[] {
        new ColumnInfo(
          BaseMessages.getString( PKG, "ParGzipCsvInputDialog.NameColumn.Column" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "ParGzipCsvInputDialog.TypeColumn.Column" ),
          ColumnInfo.COLUMN_TYPE_CCOMBO, ValueMetaFactory.getValueMetaNames(), true ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "ParGzipCsvInputDialog.FormatColumn.Column" ),
          ColumnInfo.COLUMN_TYPE_FORMAT, 2 ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "ParGzipCsvInputDialog.LengthColumn.Column" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "ParGzipCsvInputDialog.PrecisionColumn.Column" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "ParGzipCsvInputDialog.CurrencyColumn.Column" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "ParGzipCsvInputDialog.DecimalColumn.Column" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "ParGzipCsvInputDialog.GroupColumn.Column" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "ParGzipCsvInputDialog.TrimTypeColumn.Column" ),
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
        getCSV();
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
      wbbFilename.addSelectionListener( DialogHelper.constructSelectionAdapterFileDialogTextVarForUserFile( log
        , wFilename, transMeta, SelectionOperation.FILE, new FilterType[] { FilterType.GZ
          , FilterType.ALL }, FilterType.GZ ) );
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

    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return stepname;
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

  public void getData() {
    getData( inputMeta );
  }

  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void getData( ParGzipCsvInputMeta inputMeta ) {
    wStepname.setText( stepname );
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
    wRowNumField.setText( Const.NVL( inputMeta.getRowNumField(), "" ) );
    wAddResult.setSelection( inputMeta.isAddResultFile() );
    wEncoding.setText( Const.NVL( inputMeta.getEncoding(), "" ) );

    for ( int i = 0; i < inputMeta.getInputFields().length; i++ ) {
      TextFileInputField field = inputMeta.getInputFields()[i];

      TableItem item = new TableItem( wFields.table, SWT.NONE );
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

    wStepname.selectAll();
    wStepname.setFocus();
  }

  private void cancel() {
    stepname = null;
    inputMeta.setChanged( changed );
    dispose();
  }

  private void getInfo( ParGzipCsvInputMeta inputMeta ) {

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

  // Get the data layout
  private void getCSV() {
    InputStream inputStream = null;
    try {
      ParGzipCsvInputMeta meta = new ParGzipCsvInputMeta();
      getInfo( meta );

      String filename = transMeta.environmentSubstitute( meta.getFilename() );

      FileObject fileObject = KettleVFS.getInstance( transMeta.getBowl() ).getFileObject( filename );
      if ( !( fileObject instanceof LocalFile ) ) {
        // We can only use NIO on local files at the moment, so that's what we limit ourselves to.
        //
        throw new KettleException( BaseMessages.getString( PKG, "ParGzipCsvInput.Log.OnlyLocalFilesAreSupported" ) );
      }

      wFields.table.removeAll();

      inputStream = new GZIPInputStream( KettleVFS.getInputStream( fileObject ) );
      InputStreamReader reader = new InputStreamReader( inputStream );
      EncodingType encodingType = EncodingType.guessEncodingType( reader.getEncoding() );

      // Read a line of data to determine the number of rows...
      //
      String line =
        TextFileInput.getLine(
          log, reader, encodingType, TextFileInputMeta.FILE_FORMAT_MIXED, new StringBuilder( 1000 ) );

      // Split the string, header or data into parts...
      //
      String[] fieldNames = Const.splitString( line, meta.getDelimiter() );

      if ( !meta.isHeaderPresent() ) {
        // Don't use field names from the header...
        // Generate field names F1 ... F10
        //
        DecimalFormat df = new DecimalFormat( "000" );
        for ( int i = 0; i < fieldNames.length; i++ ) {
          fieldNames[i] = "Field_" + df.format( i );
        }
      } else {
        if ( !Utils.isEmpty( meta.getEnclosure() ) ) {
          for ( int i = 0; i < fieldNames.length; i++ ) {
            if ( fieldNames[i].startsWith( meta.getEnclosure() )
              && fieldNames[i].endsWith( meta.getEnclosure() ) && fieldNames[i].length() > 1 ) {
              fieldNames[i] = fieldNames[i].substring( 1, fieldNames[i].length() - 1 );
            }
          }
        }
      }

      // Trim the names to make sure...
      //
      for ( int i = 0; i < fieldNames.length; i++ ) {
        fieldNames[i] = Const.trim( fieldNames[i] );
      }

      // Update the GUI
      //
      for ( int i = 0; i < fieldNames.length; i++ ) {
        TableItem item = new TableItem( wFields.table, SWT.NONE );
        item.setText( 1, fieldNames[i] );
        item.setText( 2, ValueMetaFactory.getValueMetaName( ValueMetaInterface.TYPE_STRING ) );
      }
      wFields.removeEmptyRows();
      wFields.setRowNums();
      wFields.optWidth( true );

      // Now we can continue reading the rows of data and we can guess the
      // Sample a few lines to determine the correct type of the fields...
      //
      String shellText = BaseMessages.getString( PKG, "ParGzipCsvInputDialog.LinesToSample.DialogTitle" );
      String lineText = BaseMessages.getString( PKG, "ParGzipCsvInputDialog.LinesToSample.DialogMessage" );
      EnterNumberDialog end = new EnterNumberDialog( shell, 100, shellText, lineText );
      int samples = end.open();
      if ( samples >= 0 ) {
        getInfo( meta );

        TextFileCSVImportProgressDialog pd =
          new TextFileCSVImportProgressDialog( shell, meta, transMeta, reader, samples, true );
        String message = pd.open();
        if ( message != null ) {
          wFields.removeAll();

          // OK, what's the result of our search?
          getData( meta );
          wFields.removeEmptyRows();
          wFields.setRowNums();
          wFields.optWidth( true );

          EnterTextDialog etd = new EnterTextDialog( shell,
            BaseMessages.getString( PKG, "ParGzipCsvInputDialog.ScanResults.DialogTitle" ),
            BaseMessages.getString( PKG, "ParGzipCsvInputDialog.ScanResults.DialogMessage" ), message, true );
          etd.setReadOnly();
          etd.open();
        }
      }
    } catch ( IOException e ) {
      new ErrorDialog(
        shell, BaseMessages.getString( PKG, "ParGzipCsvInputDialog.IOError.DialogTitle" ), BaseMessages
          .getString( PKG, "ParGzipCsvInputDialog.IOError.DialogMessage" ), e );
    } catch ( KettleException e ) {
      new ErrorDialog( shell, BaseMessages.getString( PKG, "System.Dialog.Error.Title" ), BaseMessages.getString(
        PKG, "ParGzipCsvInputDialog.ErrorGettingFileDesc.DialogMessage" ), e );
    } finally {
      try {
        inputStream.close();
      } catch ( Exception e ) {
        // Ignore errors
      }
    }
  }

  // Preview the data
  private void preview() {
    // Create the XML input step
    ParGzipCsvInputMeta oneMeta = new ParGzipCsvInputMeta();
    getInfo( oneMeta );

    TransMeta previewMeta =
      TransPreviewFactory.generatePreviewTransformation( transMeta, oneMeta, wStepname.getText() );

    EnterNumberDialog numberDialog =
      new EnterNumberDialog( shell, props.getDefaultPreviewSize(), BaseMessages.getString(
        PKG, "ParGzipCsvInputDialog.PreviewSize.DialogTitle" ), BaseMessages.getString(
        PKG, "ParGzipCsvInputDialog.PreviewSize.DialogMessage" ) );
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

}
