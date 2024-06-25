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

package org.pentaho.di.ui.trans.steps.checksum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.annotations.PluginDialog;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.checksum.CheckSumMeta;
import org.pentaho.di.ui.core.FormDataBuilder;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.trans.step.TableItemInsertListener;

@PluginDialog( id = "CheckSum", image = "CSM.svg", pluginType = PluginDialog.PluginType.STEP,
    documentationUrl = "mk-95pdia003/pdi-transformation-steps/add-a-checksum" )
public class CheckSumDialog extends BaseStepDialog implements StepDialogInterface {
  private static Class<?> PKG = CheckSumDialog.class; // for i18n purposes, needed by Translator2!!

  private CheckSumMeta input;

  private Label wlType;
  private CCombo wType;

  private Label wlFields;
  private TableView wFields;

  private Label wlResult;
  private Text wResult;

  private Label wlCompatibility;
  private Button wCompatibility;

  private Label wlEvaluationMethod;
  private CCombo wEvaluationMethod;

  private Label wlFieldSeparatorString;
  private Text wFieldSeparatorString;

  private ColumnInfo[] colinf;

  private Map<String, Integer> inputFields;

  private Label wlResultType;
  private CCombo wResultType;

  public CheckSumDialog( Shell parent, Object in, TransMeta tr, String sname ) {
    super( parent, (BaseStepMeta) in, tr, sname );
    input = (CheckSumMeta) in;
    inputFields = new HashMap<>();
  }

  @Override
  public String open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX );
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
    shell.setText( BaseMessages.getString( PKG, "CheckSumDialog.Shell.Title" ) );

    int middle = props.getMiddlePct();
    int margin = Const.MARGIN;

    // Stepname line
    wlStepname = new Label( shell, SWT.RIGHT );
    wlStepname.setText( BaseMessages.getString( PKG, "CheckSumDialog.Stepname.Label" ) );
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
    fdStepname.right = new FormAttachment( 100, 0 );
    fdStepname.top = new FormAttachment( 0, margin );
    wStepname.setLayoutData( fdStepname );

    // Type
    wlType = new Label( shell, SWT.RIGHT );
    wlType.setText( BaseMessages.getString( PKG, "CheckSumDialog.Type.Label" ) );
    props.setLook( wlType );
    FormData fdlType = new FormData();
    fdlType.left = new FormAttachment( 0, 0 );
    fdlType.right = new FormAttachment( middle, -margin );
    fdlType.top = new FormAttachment( wStepname, margin );
    wlType.setLayoutData( fdlType );
    wType = new CCombo( shell, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER );
    for ( int i = 0; i < input.getChecksumtypeDescs().length; i++ ) {
      wType.add( input.getChecksumtypeDescs()[i] );
    }
    wType.select( 0 );
    props.setLook( wType );
    FormData fdType = new FormData();
    fdType.left = new FormAttachment( middle, 0 );
    fdType.right = new FormAttachment( 100, 0 );
    fdType.top = new FormAttachment( wStepname, margin );
    wType.setLayoutData( fdType );
    wType.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
        activeResultType();
      }
    } );

    // ResultType
    wlResultType = new Label( shell, SWT.RIGHT );
    wlResultType.setText( BaseMessages.getString( PKG, "CheckSumDialog.ResultType.Label" ) );
    props.setLook( wlResultType );
    FormData fdlResultType = new FormData();
    fdlResultType.left = new FormAttachment( 0, 0 );
    fdlResultType.right = new FormAttachment( middle, -margin );
    fdlResultType.top = new FormAttachment( wType, 2 * margin );
    wlResultType.setLayoutData( fdlResultType );
    wResultType = new CCombo( shell, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER );
    wResultType.setItems( input.getResultTypeDescs() );
    wResultType.select( 0 );
    props.setLook( wResultType );
    FormData fdResultType = new FormData();
    fdResultType.left = new FormAttachment( middle, 0 );
    fdResultType.right = new FormAttachment( 100, 0 );
    fdResultType.top = new FormAttachment( wType, 2 * margin );
    wResultType.setLayoutData( fdResultType );
    wResultType.addSelectionListener( new SelectionAdapter() {

      @Override
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
        activeHexa();
      }
    } );

    // Result line...
    wlResult = new Label( shell, SWT.RIGHT );
    wlResult.setText( BaseMessages.getString( PKG, "CheckSumDialog.Result.Label" ) );
    props.setLook( wlResult );
    FormData fdlResult = new FormData();
    fdlResult.left = new FormAttachment( 0, 0 );
    fdlResult.right = new FormAttachment( middle, -margin );
    fdlResult.top = new FormAttachment( wResultType, 2 * margin );
    wlResult.setLayoutData( fdlResult );
    wResult = new Text( shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wResult );
    wResult.addModifyListener( lsMod );
    FormData fdResult = new FormData();
    fdResult.left = new FormAttachment( middle, 0 );
    fdResult.right = new FormAttachment( 100, 0 );
    fdResult.top = new FormAttachment( wResultType, 2 * margin );
    wResult.setLayoutData( fdResult );

    wlFieldSeparatorString = new Label( shell, SWT.RIGHT );
    wlFieldSeparatorString.setText( BaseMessages.getString( PKG, "CheckSumDialog.FieldSeparatorString.Label" ) );
    props.setLook( wlFieldSeparatorString );
    wlFieldSeparatorString.setLayoutData(
      new FormDataBuilder().left( 0, 0 ).right( middle, -margin ).top( wResult, 2 * margin ).result() );
    wFieldSeparatorString = new Text( shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wFieldSeparatorString );
    wFieldSeparatorString.addModifyListener( lsMod );
    wFieldSeparatorString.setLayoutData(
      new FormDataBuilder().left( middle, 0 ).right( 100, 0 ).top( wResult, 2 * margin ).result() );

    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
    wGet = new Button( shell, SWT.PUSH );
    wGet.setText( BaseMessages.getString( PKG, "System.Button.GetFields" ) );
    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );

    setButtonPositions( new Button[] { wOK, wGet, wCancel }, margin, null );

    // Are we operating in compatibility mode?
    wlCompatibility = new Label( shell, SWT.RIGHT );
    wlCompatibility.setText( BaseMessages.getString( PKG, "CheckSumDialog.CompatibilityMode.Label" ) );
    props.setLook( wlCompatibility );
    FormData fdlCompatibility = new FormData();
    fdlCompatibility.left = new FormAttachment( 0, 0 );
    fdlCompatibility.right = new FormAttachment( middle, -margin );
    fdlCompatibility.top = new FormAttachment( wFieldSeparatorString, margin );
    wlCompatibility.setLayoutData( fdlCompatibility );
    wCompatibility = new Button( shell, SWT.CHECK );
    wCompatibility.setToolTipText( BaseMessages.getString( PKG, "CheckSumDialog.CompatibilityMode.Tooltip" ) );
    props.setLook( wCompatibility );
    FormData fdCompatibility = new FormData();
    fdCompatibility.left = new FormAttachment( middle, 0 );
    fdCompatibility.right = new FormAttachment( 100, 0 );
    fdCompatibility.top = new FormAttachment( wFieldSeparatorString, margin );
    wCompatibility.setLayoutData( fdCompatibility );
    SelectionAdapter lsSelR = new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent arg0 ) {
        input.setChanged();
      }
    };
    wCompatibility.addSelectionListener( lsSelR );

    // EvaluationMethod
    // The label
    wlEvaluationMethod = new Label( shell, SWT.RIGHT );
    wlEvaluationMethod.setText( BaseMessages.getString( PKG, "CheckSumDialog.EvaluationMethod.Label" ) );
    props.setLook( wlEvaluationMethod );
    FormData wlEvaluationMethodFD = new FormData();
    wlEvaluationMethodFD.left = new FormAttachment( 0, 0 );
    wlEvaluationMethodFD.right = new FormAttachment( middle, -margin );
    wlEvaluationMethodFD.top = new FormAttachment( wCompatibility, margin );
    wlEvaluationMethod.setLayoutData( wlEvaluationMethodFD );
    // The control
    wEvaluationMethod = new CCombo( shell, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER );
    wEvaluationMethod.setToolTipText( BaseMessages.getString( PKG, "CheckSumDialog.EvaluationMethod.Tooltip" ) );
    wEvaluationMethod.setItems( CheckSumMeta.getEvaluationMethodDescs() );
    wEvaluationMethod.select( 0 );
    props.setLook( wEvaluationMethod );
    FormData wEvaluationMethodFD = new FormData();
    wEvaluationMethodFD.left = new FormAttachment( middle, 0 );
    wEvaluationMethodFD.right = new FormAttachment( 100, 0 );
    wEvaluationMethodFD.top = new FormAttachment( wCompatibility, margin );
    wEvaluationMethod.setLayoutData( wEvaluationMethodFD );
    wEvaluationMethod.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
      }
    } );

    // Table with fields
    wlFields = new Label( shell, SWT.NONE );
    wlFields.setText( BaseMessages.getString( PKG, "CheckSumDialog.Fields.Label" ) );
    props.setLook( wlFields );
    FormData fdlFields = new FormData();
    fdlFields.left = new FormAttachment( 0, 0 );
    fdlFields.right = new FormAttachment( middle, -margin );
    fdlFields.top = new FormAttachment( wEvaluationMethod, 2 * margin );
    wlFields.setLayoutData( fdlFields );

    final int FieldsCols = 1;
    final int FieldsRows = input.getFieldName().length;

    colinf = new ColumnInfo[FieldsCols];
    colinf[0] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "CheckSumDialog.Fieldname.Column" ), ColumnInfo.COLUMN_TYPE_CCOMBO,
        new String[] { "" }, false );
    wFields =
      new TableView(
        transMeta, shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf, FieldsRows, lsMod, props );

    FormData fdFields = new FormData();
    fdFields.left = new FormAttachment( 0, 0 );
    fdFields.right = new FormAttachment( 100, 0 );
    fdFields.top = new FormAttachment( wlFields, margin );
    fdFields.bottom = new FormAttachment( wOK, -2 * margin );
    wFields.setLayoutData( fdFields );

    //
    // Search the fields in the background

    final Runnable runnable = new Runnable() {
      @Override
      public void run() {
        StepMeta stepMeta = transMeta.findStep( stepname );
        if ( stepMeta != null ) {
          try {
            RowMetaInterface row = transMeta.getPrevStepFields( stepMeta );

            // Remember these fields...
            for ( int i = 0; i < row.size(); i++ ) {
              inputFields.put( row.getValueMeta( i ).getName(), Integer.valueOf( i ) );
            }

            setComboBoxes();
          } catch ( KettleException e ) {
            logError( BaseMessages.getString( PKG, "System.Dialog.GetFieldsFailed.Message" ) );
          }
        }
      }
    };
    new Thread( runnable ).start();

    // Add listeners
    lsCancel = new Listener() {
      @Override
      public void handleEvent( Event e ) {
        cancel();
      }
    };
    lsGet = new Listener() {
      @Override
      public void handleEvent( Event e ) {
        get();
      }
    };
    lsOK = new Listener() {
      @Override
      public void handleEvent( Event e ) {
        ok();
      }
    };

    wCancel.addListener( SWT.Selection, lsCancel );
    wOK.addListener( SWT.Selection, lsOK );
    wGet.addListener( SWT.Selection, lsGet );

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

    // Set the shell size, based upon previous time...
    setSize();

    getData();
    activeHexa();
    activeResultType();
    input.setChanged( changed );

    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return stepname;
  }

  private void activeResultType() {
    int currentType = wType.getSelectionIndex();
    boolean active = currentType == 2 || currentType == 3 || currentType == 4;
    wlResultType.setEnabled( active );
    wResultType.setEnabled( active );
  }

  protected void setComboBoxes() {
    // Something was changed in the row.
    //
    final Map<String, Integer> fields = new HashMap<>();

    // Add the currentMeta fields...
    fields.putAll( inputFields );

    Set<String> keySet = fields.keySet();
    List<String> entries = new ArrayList<>( keySet );

    String[] fieldNames = entries.toArray( new String[entries.size()] );

    Const.sortStrings( fieldNames );
    colinf[0].setComboValues( fieldNames );
  }

  private void get() {
    try {
      RowMetaInterface r = transMeta.getPrevStepFields( stepname );
      if ( r != null ) {
        TableItemInsertListener insertListener = new TableItemInsertListener() {
          @Override
          public boolean tableItemInserted( TableItem tableItem, ValueMetaInterface v ) {
            tableItem.setText( 2, BaseMessages.getString( PKG, "System.Combo.Yes" ) );
            return true;
          }
        };
        BaseStepDialog
          .getFieldsFromPrevious( r, wFields, 1, new int[] { 1 }, new int[] {}, -1, -1, insertListener );
      }
    } catch ( KettleException ke ) {
      new ErrorDialog( shell, BaseMessages.getString( PKG, "System.Dialog.GetFieldsFailed.Title" ), BaseMessages
        .getString( PKG, "System.Dialog.GetFieldsFailed.Message" ), ke );
    }
  }

  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void getData() {
    wType.select( input.getTypeByDesc() );
    if ( input.getResultFieldName() != null ) {
      wResult.setText( input.getResultFieldName() );
    }
    if ( input.getFieldSeparatorString() != null ) {
      wFieldSeparatorString.setText( input.getFieldSeparatorString() );
    }
    wResultType.setText( input.getResultTypeDesc( input.getResultType() ) );
    wCompatibility.setSelection( input.isCompatibilityMode() );
    wEvaluationMethod.select( input.getEvaluationMethod() );

    Table table = wFields.table;
    if ( input.getFieldName().length > 0 ) {
      table.removeAll();
    }
    for ( int i = 0; i < input.getFieldName().length; i++ ) {
      TableItem ti = new TableItem( table, SWT.NONE );
      ti.setText( 0, "" + ( i + 1 ) );
      ti.setText( 1, input.getFieldName()[i] );
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

    if ( wType.getSelectionIndex() < 0 ) {
      input.setCheckSumType( 0 );
    } else {
      input.setCheckSumType( wType.getSelectionIndex() );
    }

    input.setResultFieldName( wResult.getText() );
    input.setFieldSeparatorString( wFieldSeparatorString.getText() );
    input.setResultType( input.getResultTypeByDesc( wResultType.getText() ) );

    input.setCompatibilityMode( wCompatibility.getSelection() );
    input.setEvaluationMethod( wEvaluationMethod.getSelectionIndex() );

    int nrfields = wFields.nrNonEmpty();
    input.allocate( nrfields );
    for ( int i = 0; i < nrfields; i++ ) {
      TableItem ti = wFields.getNonEmpty( i );
      input.getFieldName()[ i ] = ti.getText( 1 );
    }
    dispose();
  }

  private void activeHexa() {
    boolean activate =
      ( input.getResultTypeByDesc( wResultType.getText() ) == CheckSumMeta.result_TYPE_HEXADECIMAL );
    wlCompatibility.setEnabled( activate );
    wCompatibility.setEnabled( activate );
  }
}
