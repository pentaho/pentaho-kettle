/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2021 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.trans.steps.rowgenerator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Point;
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
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.rowgenerator.RowGeneratorMeta;
import org.pentaho.di.ui.core.dialog.EnterNumberDialog;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.dialog.TransPreviewProgressDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class RowGeneratorDialog extends BaseStepDialog implements StepDialogInterface {
  private static Class<?> PKG = RowGeneratorMeta.class; // for i18n purposes, needed by Translator2!!

  private Label wlLimit;
  private TextVar wLimit;

  private Label wlNeverEnding;
  private Button wNeverEnding;

  private Label wlInterval;
  private TextVar wInterval;

  private Label wlRowTimeField;
  private TextVar wRowTimeField;

  private Label wlLastTimeField;
  private TextVar wLastTimeField;

  private Label wlFields;
  private TableView wFields;

  private RowGeneratorMeta input;

  public RowGeneratorDialog( Shell parent, Object in, TransMeta transMeta, String sname ) {
    super( parent, (BaseStepMeta) in, transMeta, sname );
    input = (RowGeneratorMeta) in;
  }

  public String open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );
    props.setLook( shell );
    setShellImage( shell, input );

    ModifyListener lsMod = new ModifyListener() {
      public void modifyText( ModifyEvent e ) {
        input.setChanged();
      }
    };
    changed = input.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout( formLayout );
    shell.setText( BaseMessages.getString( PKG, "RowGeneratorDialog.DialogTitle" ) );

    int middle = props.getMiddlePct();
    int margin = Const.MARGIN;

    // Filename line
    wlStepname = new Label( shell, SWT.RIGHT );
    wlStepname.setText( BaseMessages.getString( PKG, "System.Label.StepName" ) );
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
    Control lastControl = wStepname;

    wlLimit = new Label( shell, SWT.RIGHT );
    wlLimit.setText( BaseMessages.getString( PKG, "RowGeneratorDialog.Limit.Label" ) );
    props.setLook( wlLimit );
    FormData fdlLimit = new FormData();
    fdlLimit.left = new FormAttachment( 0, 0 );
    fdlLimit.right = new FormAttachment( middle, -margin );
    fdlLimit.top = new FormAttachment( lastControl, margin );
    wlLimit.setLayoutData( fdlLimit );
    wLimit = new TextVar( transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wLimit );
    wLimit.addModifyListener( lsMod );
    FormData fdLimit = new FormData();
    fdLimit.left = new FormAttachment( middle, 0 );
    fdLimit.top = new FormAttachment( lastControl, margin );
    fdLimit.right = new FormAttachment( 100, 0 );
    wLimit.setLayoutData( fdLimit );
    lastControl = wLimit;

    wlNeverEnding = new Label( shell, SWT.RIGHT );
    wlNeverEnding.setText( BaseMessages.getString( PKG, "RowGeneratorDialog.NeverEnding.Label" ) );
    props.setLook( wlNeverEnding );
    FormData fdlNeverEnding = new FormData();
    fdlNeverEnding.left = new FormAttachment( 0, 0 );
    fdlNeverEnding.right = new FormAttachment( middle, -margin );
    fdlNeverEnding.top = new FormAttachment( lastControl, margin );
    wlNeverEnding.setLayoutData( fdlNeverEnding );
    wNeverEnding = new Button( shell, SWT.CHECK );
    props.setLook( wNeverEnding );
    wNeverEnding.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        setActive();
        input.setChanged();
      }
    } );
    FormData fdNeverEnding = new FormData();
    fdNeverEnding.left = new FormAttachment( middle, 0 );
    fdNeverEnding.top = new FormAttachment( lastControl, margin );
    fdNeverEnding.right = new FormAttachment( 100, 0 );
    wNeverEnding.setLayoutData( fdNeverEnding );
    lastControl = wNeverEnding;

    wlInterval = new Label( shell, SWT.RIGHT );
    wlInterval.setText( BaseMessages.getString( PKG, "RowGeneratorDialog.Interval.Label" ) );
    props.setLook( wlInterval );
    FormData fdlInterval = new FormData();
    fdlInterval.left = new FormAttachment( 0, 0 );
    fdlInterval.right = new FormAttachment( middle, -margin );
    fdlInterval.top = new FormAttachment( lastControl, margin );
    wlInterval.setLayoutData( fdlInterval );
    wInterval = new TextVar( transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wInterval );
    wInterval.addModifyListener( lsMod );
    FormData fdInterval = new FormData();
    fdInterval.left = new FormAttachment( middle, 0 );
    fdInterval.top = new FormAttachment( lastControl, margin );
    fdInterval.right = new FormAttachment( 100, 0 );
    wInterval.setLayoutData( fdInterval );
    lastControl = wInterval;

    wlRowTimeField = new Label( shell, SWT.RIGHT );
    wlRowTimeField.setText( BaseMessages.getString( PKG, "RowGeneratorDialog.RowTimeField.Label" ) );
    props.setLook( wlRowTimeField );
    FormData fdlRowTimeField = new FormData();
    fdlRowTimeField.left = new FormAttachment( 0, 0 );
    fdlRowTimeField.right = new FormAttachment( middle, -margin );
    fdlRowTimeField.top = new FormAttachment( lastControl, margin );
    wlRowTimeField.setLayoutData( fdlRowTimeField );
    wRowTimeField = new TextVar( transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wRowTimeField );
    wRowTimeField.addModifyListener( lsMod );
    FormData fdRowTimeField = new FormData();
    fdRowTimeField.left = new FormAttachment( middle, 0 );
    fdRowTimeField.top = new FormAttachment( lastControl, margin );
    fdRowTimeField.right = new FormAttachment( 100, 0 );
    wRowTimeField.setLayoutData( fdRowTimeField );
    lastControl = wRowTimeField;

    wlLastTimeField = new Label( shell, SWT.RIGHT );
    wlLastTimeField.setText( BaseMessages.getString( PKG, "RowGeneratorDialog.LastTimeField.Label" ) );
    props.setLook( wlLastTimeField );
    FormData fdlLastTimeField = new FormData();
    fdlLastTimeField.left = new FormAttachment( 0, 0 );
    fdlLastTimeField.right = new FormAttachment( middle, -margin );
    fdlLastTimeField.top = new FormAttachment( lastControl, margin );
    wlLastTimeField.setLayoutData( fdlLastTimeField );
    wLastTimeField = new TextVar( transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wLastTimeField );
    wLastTimeField.addModifyListener( lsMod );
    FormData fdLastTimeField = new FormData();
    fdLastTimeField.left = new FormAttachment( middle, 0 );
    fdLastTimeField.top = new FormAttachment( lastControl, margin );
    fdLastTimeField.right = new FormAttachment( 100, 0 );
    wLastTimeField.setLayoutData( fdLastTimeField );
    lastControl = wLastTimeField;

    wlFields = new Label( shell, SWT.NONE );
    wlFields.setText( BaseMessages.getString( PKG, "RowGeneratorDialog.Fields.Label" ) );
    props.setLook( wlFields );
    FormData fdlFields = new FormData();
    fdlFields.left = new FormAttachment( 0, 0 );
    fdlFields.top = new FormAttachment( lastControl, margin );
    wlFields.setLayoutData( fdlFields );
    lastControl = wlFields;

    final int FieldsRows = input.getFieldName().length;

    ColumnInfo[] colinf =
      new ColumnInfo[] {
        new ColumnInfo(
          BaseMessages.getString( PKG, "System.Column.Name" ), ColumnInfo.COLUMN_TYPE_TEXT, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "System.Column.Type" ), ColumnInfo.COLUMN_TYPE_CCOMBO,
          ValueMetaFactory.getValueMetaNames() ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "System.Column.Format" ), ColumnInfo.COLUMN_TYPE_FORMAT, 2 ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "System.Column.Length" ), ColumnInfo.COLUMN_TYPE_TEXT, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "System.Column.Precision" ), ColumnInfo.COLUMN_TYPE_TEXT, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "System.Column.Currency" ), ColumnInfo.COLUMN_TYPE_TEXT, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "System.Column.Decimal" ), ColumnInfo.COLUMN_TYPE_TEXT, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "System.Column.Group" ), ColumnInfo.COLUMN_TYPE_TEXT, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "System.Column.Value" ), ColumnInfo.COLUMN_TYPE_TEXT, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "System.Column.SetEmptyString" ), ColumnInfo.COLUMN_TYPE_CCOMBO,
          new String[] {
            BaseMessages.getString( PKG, "System.Combo.Yes" ),
            BaseMessages.getString( PKG, "System.Combo.No" ) } )

      };

    wFields =
      new TableView(
        transMeta, shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf, FieldsRows, lsMod, props );

    FormData fdFields = new FormData();
    fdFields.left = new FormAttachment( 0, 0 );
    fdFields.top = new FormAttachment( lastControl, margin );
    fdFields.right = new FormAttachment( 100, 0 );
    fdFields.bottom = new FormAttachment( 100, -50 );
    wFields.setLayoutData( fdFields );

    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
    wPreview = new Button( shell, SWT.PUSH );
    wPreview.setText( BaseMessages.getString( PKG, "System.Button.Preview" ) );
    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );

    setButtonPositions( new Button[] { wOK, wPreview, wCancel }, margin, null );

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
    wLimit.addSelectionListener( lsDef );

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    lsResize = new Listener() {
      public void handleEvent( Event event ) {
        Point size = shell.getSize();
        wFields.setSize( size.x - 10, size.y - 50 );
        wFields.table.setSize( size.x - 10, size.y - 50 );
        wFields.redraw();
      }
    };
    if ( !Const.isRunningOnWebspoonMode() ) {
      shell.addListener( SWT.Resize, lsResize );
    }

    // Set the shell size, based upon previous time...
    setSize();

    getData();
    input.setChanged( changed );

    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return stepname;
  }

  protected void setActive() {
    boolean neverEnding = wNeverEnding.getSelection();

    wlLimit.setEnabled( !neverEnding );
    wLimit.setEnabled( !neverEnding );

    wlInterval.setEnabled( neverEnding );
    wInterval.setEnabled( neverEnding );

    wlRowTimeField.setEnabled( neverEnding );
    wRowTimeField.setEnabled( neverEnding );

    wlLastTimeField.setEnabled( neverEnding );
    wLastTimeField.setEnabled( neverEnding );
  }

  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void getData() {
    if ( isDebug() ) {
      logDebug( "getting fields info..." );
    }

    wLimit.setText( input.getRowLimit() );
    wNeverEnding.setSelection( input.isNeverEnding() );
    wInterval.setText( Const.NVL( input.getIntervalInMs(), "" ) );
    wRowTimeField.setText( Const.NVL( input.getRowTimeField(), "" ) );
    wLastTimeField.setText( Const.NVL( input.getLastTimeField(), "" ) );

    for ( int i = 0; i < input.getFieldName().length; i++ ) {
      if ( input.getFieldName()[i] != null ) {
        TableItem item = wFields.table.getItem( i );
        int col = 1;
        item.setText( col++, input.getFieldName()[i] );

        String type = input.getFieldType()[i];
        String format = input.getFieldFormat()[i];
        String length = input.getFieldLength()[i] < 0 ? "" : ( "" + input.getFieldLength()[i] );
        String prec = input.getFieldPrecision()[i] < 0 ? "" : ( "" + input.getFieldPrecision()[i] );

        String curr = input.getCurrency()[i];
        String group = input.getGroup()[i];
        String decim = input.getDecimal()[i];
        String def = input.getValue()[i];

        item.setText( col++, Const.NVL( type, "" ) );
        item.setText( col++, Const.NVL( format, "" ) );
        item.setText( col++, Const.NVL( length, "" ) );
        item.setText( col++, Const.NVL( prec, "" ) );
        item.setText( col++, Const.NVL( curr, "" ) );
        item.setText( col++, Const.NVL( decim, "" ) );
        item.setText( col++, Const.NVL( group, "" ) );
        item.setText( col++, Const.NVL( def, "" ) );
        item
          .setText( col++, input.isSetEmptyString()[i]
            ? BaseMessages.getString( PKG, "System.Combo.Yes" ) : BaseMessages.getString(
              PKG, "System.Combo.No" ) );

      }
    }

    wFields.setRowNums();
    wFields.optWidth( true );

    setActive();

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

    stepname = wStepname.getText(); // return value
    try {
      getInfo( new RowGeneratorMeta() ); // to see if there is an exception
      getInfo( input ); // to put the content on the input structure for real if all is well.
      dispose();
    } catch ( KettleException e ) {
      new ErrorDialog(
        shell, BaseMessages.getString( PKG, "RowGeneratorDialog.Illegal.Dialog.Settings.Title" ), BaseMessages
          .getString( PKG, "RowGeneratorDialog.Illegal.Dialog.Settings.Message" ), e );
    }
  }

  private void getInfo( RowGeneratorMeta meta ) throws KettleException {
    meta.setRowLimit( wLimit.getText() );
    meta.setNeverEnding( wNeverEnding.getSelection() );
    meta.setIntervalInMs( wInterval.getText() );
    meta.setRowTimeField( wRowTimeField.getText() );
    meta.setLastTimeField( wLastTimeField.getText() );

    int nrfields = wFields.nrNonEmpty();

    meta.allocate( nrfields );

    //CHECKSTYLE:Indentation:OFF
    for ( int i = 0; i < nrfields; i++ ) {
      TableItem item = wFields.getNonEmpty( i );

      meta.getFieldName()[i] = item.getText( 1 );

      meta.getFieldFormat()[i] = item.getText( 3 );
      String slength = item.getText( 4 );
      String sprec = item.getText( 5 );
      meta.getCurrency()[i] = item.getText( 6 );
      meta.getDecimal()[i] = item.getText( 7 );
      meta.getGroup()[i] = item.getText( 8 );
      meta.isSetEmptyString()[i] =
        BaseMessages.getString( PKG, "System.Combo.Yes" ).equalsIgnoreCase( item.getText( 10 ) );

      meta.getValue()[i] = meta.isSetEmptyString()[i] ? "" : item.getText( 9 );
      meta.getFieldType()[i] = meta.isSetEmptyString()[i] ? "String" : item.getText( 2 );
      meta.getFieldLength()[i] = Const.toInt( slength, -1 );
      meta.getFieldPrecision()[i] = Const.toInt( sprec, -1 );
    }

    // Performs checks...
    /*
     * Commented out verification : if variables are used, this check is a pain!
     *
     * long longLimit = Const.toLong(transMeta.environmentSubstitute( wLimit.getText()), -1L ); if (longLimit<0) { throw
     * new KettleException( BaseMessages.getString(PKG, "RowGeneratorDialog.Wrong.RowLimit.Number") ); }
     */
  }

  /**
   * Preview the data generated by this step. This generates a transformation using this step & a dummy and previews it.
   *
   */
  private void preview() {
    RowGeneratorMeta oneMeta = new RowGeneratorMeta();
    try {
      getInfo( oneMeta );
    } catch ( KettleException e ) {
      new ErrorDialog(
        shell, BaseMessages.getString( PKG, "RowGeneratorDialog.Illegal.Dialog.Settings.Title" ), BaseMessages
          .getString( PKG, "RowGeneratorDialog.Illegal.Dialog.Settings.Message" ), e );
      return;
    }

    TransMeta previewMeta =
      TransPreviewFactory.generatePreviewTransformation( transMeta, oneMeta, wStepname.getText() );

    EnterNumberDialog numberDialog =
      new EnterNumberDialog( shell, props.getDefaultPreviewSize(), BaseMessages.getString(
        PKG, "System.Dialog.EnterPreviewSize.Title" ), BaseMessages.getString(
        PKG, "System.Dialog.EnterPreviewSize.Message" ) );
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
