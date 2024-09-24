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

package org.pentaho.di.ui.trans.steps.fieldschangesequence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.swt.SWT;
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
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.fieldschangesequence.FieldsChangeSequenceMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.trans.step.TableItemInsertListener;

public class FieldsChangeSequenceDialog extends BaseStepDialog implements StepDialogInterface {
  private static Class<?> PKG = FieldsChangeSequenceMeta.class; // for i18n purposes, needed by Translator2!!

  private FieldsChangeSequenceMeta input;

  private Label wlStart;
  private TextVar wStart;
  private FormData fdlStart, fdStart;

  private Label wlIncrement;
  private TextVar wIncrement;
  private FormData fdlIncrement, fdIncrement;

  private Label wlFields;
  private TableView wFields;
  private FormData fdlFields, fdFields;

  private Label wlResult;
  private Text wResult;
  private FormData fdlResult, fdResult;

  private Map<String, Integer> inputFields;

  private ColumnInfo[] colinf;

  public static final String STRING_CHANGE_SEQUENCE_WARNING_PARAMETER = "ChangeSequenceSortWarning";

  public FieldsChangeSequenceDialog( Shell parent, Object in, TransMeta tr, String sname ) {
    super( parent, (BaseStepMeta) in, tr, sname );
    input = (FieldsChangeSequenceMeta) in;
    inputFields = new HashMap<String, Integer>();
  }

  public String open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX );
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
    shell.setText( BaseMessages.getString( PKG, "FieldsChangeSequenceDialog.Shell.Title" ) );

    int middle = props.getMiddlePct();
    int margin = Const.MARGIN;

    // Stepname line
    wlStepname = new Label( shell, SWT.RIGHT );
    wlStepname.setText( BaseMessages.getString( PKG, "FieldsChangeSequenceDialog.Stepname.Label" ) );
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

    // Result line...
    wlResult = new Label( shell, SWT.RIGHT );
    wlResult.setText( BaseMessages.getString( PKG, "FieldsChangeSequenceDialog.Result.Label" ) );
    props.setLook( wlResult );
    fdlResult = new FormData();
    fdlResult.left = new FormAttachment( 0, 0 );
    fdlResult.right = new FormAttachment( middle, -margin );
    fdlResult.top = new FormAttachment( wStepname, 2 * margin );
    wlResult.setLayoutData( fdlResult );
    wResult = new Text( shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wResult );
    wResult.addModifyListener( lsMod );
    fdResult = new FormData();
    fdResult.left = new FormAttachment( middle, 0 );
    fdResult.top = new FormAttachment( wStepname, 2 * margin );
    fdResult.right = new FormAttachment( 100, 0 );
    wResult.setLayoutData( fdResult );

    // Start
    wlStart = new Label( shell, SWT.RIGHT );
    wlStart.setText( BaseMessages.getString( PKG, "FieldsChangeSequenceDialog.Start.Label" ) );
    props.setLook( wlStart );
    fdlStart = new FormData();
    fdlStart.left = new FormAttachment( 0, 0 );
    fdlStart.right = new FormAttachment( middle, -margin );
    fdlStart.top = new FormAttachment( wResult, margin );
    wlStart.setLayoutData( fdlStart );
    wStart = new TextVar( transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wStart );
    fdStart = new FormData();
    fdStart.left = new FormAttachment( middle, 0 );
    fdStart.top = new FormAttachment( wResult, margin );
    fdStart.right = new FormAttachment( 100, 0 );
    wStart.setLayoutData( fdStart );

    // Increment
    wlIncrement = new Label( shell, SWT.RIGHT );
    wlIncrement.setText( BaseMessages.getString( PKG, "FieldsChangeSequenceDialog.Increment.Label" ) );
    props.setLook( wlIncrement );
    fdlIncrement = new FormData();
    fdlIncrement.left = new FormAttachment( 0, 0 );
    fdlIncrement.right = new FormAttachment( middle, -margin );
    fdlIncrement.top = new FormAttachment( wStart, margin );
    wlIncrement.setLayoutData( fdlIncrement );
    wIncrement = new TextVar( transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wIncrement );
    fdIncrement = new FormData();
    fdIncrement.left = new FormAttachment( middle, 0 );
    fdIncrement.top = new FormAttachment( wStart, margin );
    fdIncrement.right = new FormAttachment( 100, 0 );
    wIncrement.setLayoutData( fdIncrement );

    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
    wGet = new Button( shell, SWT.PUSH );
    wGet.setText( BaseMessages.getString( PKG, "System.Button.GetFields" ) );
    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );

    setButtonPositions( new Button[] { wOK, wGet, wCancel }, margin, null );

    // Table with fields
    wlFields = new Label( shell, SWT.NONE );
    wlFields.setText( BaseMessages.getString( PKG, "FieldsChangeSequenceDialog.Fields.Label" ) );
    props.setLook( wlFields );
    fdlFields = new FormData();
    fdlFields.left = new FormAttachment( 0, 0 );
    fdlFields.top = new FormAttachment( wIncrement, margin );
    wlFields.setLayoutData( fdlFields );

    final int FieldsCols = 1;
    final int FieldsRows = input.getFieldName().length;

    colinf = new ColumnInfo[FieldsCols];
    colinf[0] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "FieldsChangeSequenceDialog.Fieldname.Column" ),
        ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false );
    wFields =
      new TableView(
        transMeta, shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf, FieldsRows, lsMod, props );

    fdFields = new FormData();
    fdFields.left = new FormAttachment( 0, 0 );
    fdFields.top = new FormAttachment( wlFields, margin );
    fdFields.right = new FormAttachment( 100, 0 );
    fdFields.bottom = new FormAttachment( wOK, -2 * margin );
    wFields.setLayoutData( fdFields );

    // Add listeners
    lsCancel = new Listener() {
      public void handleEvent( Event e ) {
        cancel();
      }
    };
    lsGet = new Listener() {
      public void handleEvent( Event e ) {
        get();
      }
    };
    lsOK = new Listener() {
      public void handleEvent( Event e ) {
        ok();
      }
    };

    wCancel.addListener( SWT.Selection, lsCancel );
    wOK.addListener( SWT.Selection, lsOK );
    wGet.addListener( SWT.Selection, lsGet );

    lsDef = new SelectionAdapter() {
      public void widgetDefaultSelected( SelectionEvent e ) {
        ok();
      }
    };

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

    //
    // Search the fields in the background
    //

    final Runnable runnable = new Runnable() {
      public void run() {
        StepMeta stepMeta = transMeta.findStep( stepname );
        if ( stepMeta != null ) {
          try {
            RowMetaInterface row = transMeta.getPrevStepFields( stepMeta );
            if ( row != null ) {
              // Remember these fields...
              for ( int i = 0; i < row.size(); i++ ) {
                inputFields.put( row.getValueMeta( i ).getName(), new Integer( i ) );
              }

              setComboBoxes();
            }

            // Dislay in red missing field names
            Runnable asyncExecRunnable = new Runnable() {
              public void run() {
                if ( !wFields.isDisposed() ) {
                  for ( int i = 0; i < wFields.table.getItemCount(); i++ ) {
                    TableItem it = wFields.table.getItem( i );
                    if ( !Utils.isEmpty( it.getText( 1 ) ) ) {
                      if ( !inputFields.containsKey( it.getText( 1 ) ) ) {
                        it.setBackground( GUIResource.getInstance().getColorRed() );
                      }
                    }
                  }
                }
              }
            };
            if ( Const.isRunningOnWebspoonMode() ) {
              display.asyncExec( asyncExecRunnable );
            } else {
              Display.getDefault().asyncExec( asyncExecRunnable );
            }

          } catch ( KettleException e ) {
            logError( BaseMessages.getString( PKG, "FieldsChangeSequenceDialog.ErrorGettingPreviousFields", e
              .getMessage() ) );
          }
        }
      }
    };
    new Thread( runnable ).start();

    input.setChanged( changed );

    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return stepname;
  }

  protected void setComboBoxes() {
    // Something was changed in the row.
    //
    final Map<String, Integer> fields = new HashMap<String, Integer>();

    // Add the currentMeta fields...
    fields.putAll( inputFields );

    Set<String> keySet = fields.keySet();
    List<String> entries = new ArrayList<String>( keySet );

    String[] fieldNames = entries.toArray( new String[entries.size()] );

    Const.sortStrings( fieldNames );
    colinf[0].setComboValues( fieldNames );
  }

  private void get() {
    try {
      RowMetaInterface r = transMeta.getPrevStepFields( stepname );
      if ( r != null ) {
        TableItemInsertListener insertListener = new TableItemInsertListener() {
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
    wStart.setText( Const.NVL( input.getStart(), "1" ) );
    wIncrement.setText( Const.NVL( input.getIncrement(), "1" ) );
    wResult.setText( Const.NVL( input.getResultFieldName(), "result" ) );

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

    input.setStart( wStart.getText() );
    input.setIncrement( wIncrement.getText() );
    input.setResultFieldName( wResult.getText() );

    int nrfields = wFields.nrNonEmpty();
    input.allocate( nrfields );
    for ( int i = 0; i < nrfields; i++ ) {
      TableItem ti = wFields.getNonEmpty( i );
      //CHECKSTYLE:Indentation:OFF
      input.getFieldName()[i] = ti.getText( 1 );
    }

    if ( "Y".equalsIgnoreCase( props.getCustomParameter( STRING_CHANGE_SEQUENCE_WARNING_PARAMETER, "Y" ) ) ) {
      MessageDialogWithToggle md =
        new MessageDialogWithToggle( shell,
          BaseMessages.getString( PKG, "FieldsChangeSequenceDialog.InputNeedSort.DialogTitle" ),
          null,
          BaseMessages.getString( PKG, "FieldsChangeSequenceDialog.InputNeedSort.DialogMessage", Const.CR ) + Const.CR,
          MessageDialog.WARNING,
          new String[] { BaseMessages.getString( PKG, "FieldsChangeSequenceDialog.InputNeedSort.Option1" ) },
          0,
          BaseMessages.getString( PKG, "FieldsChangeSequenceDialog.InputNeedSort.Option2" ), "N".equalsIgnoreCase(
            props.getCustomParameter( STRING_CHANGE_SEQUENCE_WARNING_PARAMETER, "Y" ) ) );
      MessageDialogWithToggle.setDefaultImage( GUIResource.getInstance().getImageSpoon() );
      md.open();
      props.setCustomParameter( STRING_CHANGE_SEQUENCE_WARNING_PARAMETER, md.getToggleState() ? "N" : "Y" );
      props.saveProps();
    }

    dispose();
  }
}
