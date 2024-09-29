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

package org.pentaho.di.ui.trans.steps.constant;

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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.constant.ConstantMeta;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class ConstantDialog extends BaseStepDialog implements StepDialogInterface {
  private static Class<?> PKG = ConstantMeta.class; // for i18n purposes, needed by Translator2!!

  private Label wlFields;
  private TableView wFields;
  private FormData fdlFields, fdFields;

  private ConstantMeta input;

  public ConstantDialog( Shell parent, Object in, TransMeta transMeta, String sname ) {
    super( parent, (BaseStepMeta) in, transMeta, sname );
    input = (ConstantMeta) in;
  }

  @Override
  public String open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );
    props.setLook( shell );
    setShellImage( shell, input );

    ModifyListener lsMod = new ModifyListener() {
      @Override
      public void modifyText( ModifyEvent e ) {
        input.setChanged();
      }
    };
    changed = input.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout( formLayout );
    shell.setText( BaseMessages.getString( PKG, "ConstantDialog.DialogTitle" ) );

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

    wlFields = new Label( shell, SWT.NONE );
    wlFields.setText( BaseMessages.getString( PKG, "ConstantDialog.Fields.Label" ) );
    props.setLook( wlFields );
    fdlFields = new FormData();
    fdlFields.left = new FormAttachment( 0, 0 );
    fdlFields.top = new FormAttachment( wStepname, margin );
    wlFields.setLayoutData( fdlFields );

    final int FieldsCols = 10;
    final int FieldsRows = input.getFieldName().length;

    ColumnInfo[] colinf = new ColumnInfo[FieldsCols];
    colinf[0] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "ConstantDialog.Name.Column" ), ColumnInfo.COLUMN_TYPE_TEXT, false );
    colinf[1] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "ConstantDialog.Type.Column" ), ColumnInfo.COLUMN_TYPE_CCOMBO,
        ValueMetaFactory.getValueMetaNames() );
    colinf[2] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "ConstantDialog.Format.Column" ), ColumnInfo.COLUMN_TYPE_FORMAT, 2 );
    colinf[3] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "ConstantDialog.Length.Column" ), ColumnInfo.COLUMN_TYPE_TEXT, false );
    colinf[4] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "ConstantDialog.Precision.Column" ), ColumnInfo.COLUMN_TYPE_TEXT, false );
    colinf[5] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "ConstantDialog.Currency.Column" ), ColumnInfo.COLUMN_TYPE_TEXT, false );
    colinf[6] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "ConstantDialog.Decimal.Column" ), ColumnInfo.COLUMN_TYPE_TEXT, false );
    colinf[7] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "ConstantDialog.Group.Column" ), ColumnInfo.COLUMN_TYPE_TEXT, false );
    colinf[8] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "ConstantDialog.Value.Column" ), ColumnInfo.COLUMN_TYPE_TEXT, false );
    colinf[9] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "ConstantDialog.Value.SetEmptyString" ),
        ColumnInfo.COLUMN_TYPE_CCOMBO,
        new String[] {
          BaseMessages.getString( PKG, "System.Combo.Yes" ), BaseMessages.getString( PKG, "System.Combo.No" ) } );

    wFields =
      new TableView(
        transMeta, shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf, FieldsRows, lsMod, props );

    fdFields = new FormData();
    fdFields.left = new FormAttachment( 0, 0 );
    fdFields.top = new FormAttachment( wlFields, margin );
    fdFields.right = new FormAttachment( 100, 0 );
    fdFields.bottom = new FormAttachment( 100, -50 );
    wFields.setLayoutData( fdFields );

    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );

    setButtonPositions( new Button[] { wOK, wCancel }, margin, wFields );

    // Add listeners
    lsOK = new Listener() {
      @Override
      public void handleEvent( Event e ) {
        ok();
      }
    };
    lsCancel = new Listener() {
      @Override
      public void handleEvent( Event e ) {
        cancel();
      }
    };

    wOK.addListener( SWT.Selection, lsOK );
    wCancel.addListener( SWT.Selection, lsCancel );

    lsDef = new SelectionAdapter() {
      @Override
      public void widgetDefaultSelected( SelectionEvent e ) {
        ok();
      }
    };

    wStepname.addSelectionListener( lsDef );

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      @Override
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    lsResize = new Listener() {
      @Override
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

  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void getData() {
    int i;
    if ( log.isDebug() ) {
      logDebug( "getting fields info..." );
    }

    for ( i = 0; i < input.getFieldName().length; i++ ) {
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

    int i;
    // Table table = wFields.table;

    int nrfields = wFields.nrNonEmpty();

    input.allocate( nrfields );

    //CHECKSTYLE:Indentation:OFF
    //CHECKSTYLE:LineLength:OFF
    for ( i = 0; i < nrfields; i++ ) {
      TableItem item = wFields.getNonEmpty( i );
      input.getFieldName()[i] = item.getText( 1 );
      input.isSetEmptyString()[i] = BaseMessages.getString( PKG, "System.Combo.Yes" ).equalsIgnoreCase( item.getText( 10 ) );

      input.getFieldType()[i] = input.isSetEmptyString()[i] ? "String" : item.getText( 2 );
      input.getFieldFormat()[i] = item.getText( 3 );
      String slength = item.getText( 4 );
      String sprec = item.getText( 5 );
      input.getCurrency()[i] = item.getText( 6 );
      input.getDecimal()[i] = item.getText( 7 );
      input.getGroup()[i] = item.getText( 8 );
      input.getValue()[i] = input.isSetEmptyString()[i] ? "" : item.getText( 9 );

      try {
        input.getFieldLength()[i] = Integer.parseInt( slength );
      } catch ( Exception e ) {
        input.getFieldLength()[i] = -1;
      }
      try {
        input.getFieldPrecision()[i] = Integer.parseInt( sprec );
      } catch ( Exception e ) {
        input.getFieldPrecision()[i] = -1;
      }

    }

    dispose();
  }
}
