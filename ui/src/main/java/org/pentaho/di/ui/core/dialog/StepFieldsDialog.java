/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.core.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

/**
 * Displays the meta-data on the Values in a row as well as the Step origin of the Value.
 *
 * @author Matt
 * @since 19-06-2003
 */
public class StepFieldsDialog extends Dialog {
  private static Class<?> PKG = StepFieldsDialog.class; // for i18n purposes, needed by Translator2!!

  private Label wlStepname;

  private Text wStepname;

  private FormData fdlStepname, fdStepname;

  private Label wlFields;

  private TableView wFields;

  private FormData fdlFields, fdFields;

  private Button wEdit, wCancel, wClose;

  private Listener lsEdit, lsCancel;

  private RowMetaInterface input;

  private Shell shell;

  private PropsUI props;

  private String stepname;

  private SelectionAdapter lsDef;

  private VariableSpace variables;

  private String shellText;

  private String originText;

  private boolean showEditButton = true;

  public StepFieldsDialog( Shell parent, VariableSpace space, int style, String stepname, RowMetaInterface input ) {
    super( parent, style );
    this.stepname = stepname;
    this.input = input;
    this.variables = space;
    props = PropsUI.getInstance();

    shellText = BaseMessages.getString( PKG, "StepFieldsDialog.Title" );
    originText = BaseMessages.getString( PKG, "StepFieldsDialog.Name.Label" );
    showEditButton = true;
  }

  public Object open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );
    props.setLook( shell );

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout( formLayout );
    shell.setImage( GUIResource.getInstance().getImageTransGraph() );
    shell.setText( shellText );

    int margin = Const.MARGIN;

    // Filename line
    wlStepname = new Label( shell, SWT.NONE );
    wlStepname.setText( originText );
    props.setLook( wlStepname );
    fdlStepname = new FormData();
    fdlStepname.left = new FormAttachment( 0, 0 );
    fdlStepname.top = new FormAttachment( 0, margin );
    wlStepname.setLayoutData( fdlStepname );
    wStepname = new Text( shell, SWT.SINGLE | SWT.LEFT | SWT.READ_ONLY );
    wStepname.setText( stepname );
    props.setLook( wStepname );
    fdStepname = new FormData();
    fdStepname.left = new FormAttachment( wlStepname, margin );
    fdStepname.top = new FormAttachment( 0, margin );
    fdStepname.right = new FormAttachment( 100, 0 );
    wStepname.setLayoutData( fdStepname );

    wlFields = new Label( shell, SWT.NONE );
    wlFields.setText( BaseMessages.getString( PKG, "StepFieldsDialog.Fields.Label" ) );
    props.setLook( wlFields );
    fdlFields = new FormData();
    fdlFields.left = new FormAttachment( 0, 0 );
    fdlFields.top = new FormAttachment( wlStepname, margin );
    wlFields.setLayoutData( fdlFields );

    final int FieldsRows = input.size();

    ColumnInfo[] colinf =
      new ColumnInfo[] {
        new ColumnInfo(
          BaseMessages.getString( PKG, "StepFieldsDialog.TableCol.Fieldname" ), ColumnInfo.COLUMN_TYPE_TEXT,
          false, true ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "StepFieldsDialog.TableCol.Type" ), ColumnInfo.COLUMN_TYPE_TEXT,
          false, true ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "StepFieldsDialog.TableCol.Length" ), ColumnInfo.COLUMN_TYPE_TEXT,
          false, true ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "StepFieldsDialog.TableCol.Precision" ), ColumnInfo.COLUMN_TYPE_TEXT,
          false, true ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "StepFieldsDialog.TableCol.Origin" ), ColumnInfo.COLUMN_TYPE_TEXT,
          false, true ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "StepFieldsDialog.TableCol.StorageType" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false, true ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "StepFieldsDialog.TableCol.ConversionMask" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false, true ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "StepFieldsDialog.TableCol.Currency" ), ColumnInfo.COLUMN_TYPE_TEXT,
          false, true ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "StepFieldsDialog.TableCol.Decimal" ), ColumnInfo.COLUMN_TYPE_TEXT,
          false, true ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "StepFieldsDialog.TableCol.Group" ), ColumnInfo.COLUMN_TYPE_TEXT,
          false, true ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "StepFieldsDialog.TableCol.TrimType" ), ColumnInfo.COLUMN_TYPE_TEXT,
          false, true ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "StepFieldsDialog.TableCol.Comments" ), ColumnInfo.COLUMN_TYPE_TEXT,
          false, true ), };

    wFields =
      new TableView( variables, shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf, FieldsRows,
        true, // read-only
        null, props );
    wFields.optWidth( true );

    fdFields = new FormData();
    fdFields.left = new FormAttachment( 0, 0 );
    fdFields.top = new FormAttachment( wlFields, margin );
    fdFields.right = new FormAttachment( 100, 0 );
    fdFields.bottom = new FormAttachment( 100, -50 );
    wFields.setLayoutData( fdFields );

    // Add listeners
    lsCancel = new Listener() {
      @Override
      public void handleEvent( Event e ) {
        cancel();
      }
    };
    lsEdit = new Listener() {
      @Override
      public void handleEvent( Event e ) {
        edit();
      }
    };

    Button[] buttons;
    if ( showEditButton ) {
      wEdit = new Button( shell, SWT.PUSH );
      wEdit.setText( BaseMessages.getString( PKG, "StepFieldsDialog.Buttons.EditOrigin" ) );
      wEdit.addListener( SWT.Selection, lsEdit );
      wCancel = new Button( shell, SWT.PUSH );
      wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );
      wCancel.addListener( SWT.Selection, lsCancel );

      buttons = new Button[] { wEdit, wCancel };
    } else {
      wClose = new Button( shell, SWT.PUSH );
      wClose.setText( BaseMessages.getString( PKG, "System.Button.Close" ) );
      wClose.addListener( SWT.Selection, lsCancel );
      buttons = new Button[] { wClose };
    }

    BaseStepDialog.positionBottomButtons( shell, buttons, margin, wFields );

    lsDef = new SelectionAdapter() {
      @Override
      public void widgetDefaultSelected( SelectionEvent e ) {
        edit();
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

    wFields.table.addMouseListener( new MouseListener() {
      @Override
      public void mouseDoubleClick( MouseEvent arg0 ) {
        edit();
      }

      @Override
      public void mouseDown( MouseEvent arg0 ) {
      }

      @Override
      public void mouseUp( MouseEvent arg0 ) {
      }
    } );

    getData();

    BaseStepDialog.setSize( shell );

    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return stepname;
  }

  public void dispose() {
    props.setScreen( new WindowProperty( shell ) );
    shell.dispose();
  }

  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void getData() {
    int i;

    for ( i = 0; i < input.size(); i++ ) {
      TableItem item = wFields.table.getItem( i );
      ValueMetaInterface v = input.getValueMeta( i );
      int idx = 1;
      if ( v.getName() != null ) {
        item.setText( idx++, v.getName() );
      }
      item.setText( idx++, v.getTypeDesc() );
      item.setText( idx++, v.getLength() < 0 ? "-" : "" + v.getLength() );
      item.setText( idx++, v.getPrecision() < 0 ? "-" : "" + v.getPrecision() );
      item.setText( idx++, Const.NVL( v.getOrigin(), "" ) );
      item.setText( idx++, ValueMetaBase.getStorageTypeCode( v.getStorageType() ) );
      item.setText( idx++, Const.NVL( v.getConversionMask(), "" ) );
      item.setText( idx++, Const.NVL( v.getCurrencySymbol(), "" ) );
      item.setText( idx++, Const.NVL( v.getDecimalSymbol(), "" ) );
      item.setText( idx++, Const.NVL( v.getGroupingSymbol(), "" ) );
      item.setText( idx++, ValueMetaBase.getTrimTypeDesc( v.getTrimType() ) );
      item.setText( idx++, Const.NVL( v.getComments(), "" ) );

    }
    wFields.optWidth( true );
  }

  private void cancel() {
    stepname = null;
    dispose();
  }

  private void edit() {
    int idx = wFields.table.getSelectionIndex();
    if ( idx >= 0 ) {
      stepname = wFields.table.getItem( idx ).getText( 5 );
      dispose();
    } else {
      stepname = null;
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
      mb.setText( BaseMessages.getString( PKG, "StepFieldsDialog.OriginStep.Title" ) );
      mb.setMessage( BaseMessages.getString( PKG, "StepFieldsDialog.OriginStep.Message" ) );
      mb.open();
    }
  }

  public String getShellText() {
    return shellText;
  }

  public void setShellText( String shellText ) {
    this.shellText = shellText;
  }

  public String getOriginText() {
    return originText;
  }

  public void setOriginText( String originText ) {
    this.originText = originText;
  }

  public boolean isShowEditButton() {
    return showEditButton;
  }

  public void setShowEditButton( boolean showEditButton ) {
    this.showEditButton = showEditButton;
  }
}
