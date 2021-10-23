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

package org.pentaho.di.ui.trans.steps.stringoperations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.pentaho.di.trans.steps.stringoperations.StringOperationsMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.trans.step.TableItemInsertListener;

/**
 * Dialog class for the StringOperations step.
 *
 * @author Samatar Hassan
 * @since 02 April 2009
 */
public class StringOperationsDialog extends BaseStepDialog implements StepDialogInterface {

  private static Class<?> PKG = StringOperationsMeta.class; // for i18n purposes, needed by Translator2!!

  private Label wlKey;

  private TableView wFields;

  private FormData fdlKey, fdKey;

  private StringOperationsMeta input;

  // holds the names of the fields entering this step
  private Map<String, Integer> inputFields;

  private ColumnInfo[] ciKey;

  public StringOperationsDialog( Shell parent, Object in, TransMeta tr, String sname ) {
    super( parent, (BaseStepMeta) in, tr, sname );
    input = (StringOperationsMeta) in;
    inputFields = new HashMap<String, Integer>();
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
    shell.setText( BaseMessages.getString( PKG, "StringOperationsDialog.Shell.Title" ) );

    int middle = props.getMiddlePct();
    int margin = Const.MARGIN;

    // Stepname line
    wlStepname = new Label( shell, SWT.RIGHT );
    wlStepname.setText( BaseMessages.getString( PKG, "StringOperationsDialog.Stepname.Label" ) );
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

    wlKey = new Label( shell, SWT.NONE );
    wlKey.setText( BaseMessages.getString( PKG, "StringOperationsDialog.Fields.Label" ) );
    props.setLook( wlKey );
    fdlKey = new FormData();
    fdlKey.left = new FormAttachment( 0, 0 );
    fdlKey.top = new FormAttachment( wStepname, 2 * margin );
    wlKey.setLayoutData( fdlKey );

    int nrFieldCols = 11;
    int nrFieldRows = ( input.getFieldInStream() != null ? input.getFieldInStream().length : 1 );

    ciKey = new ColumnInfo[nrFieldCols];
    ciKey[0] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "StringOperationsDialog.ColumnInfo.InStreamField" ),
        ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false );
    ciKey[1] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "StringOperationsDialog.ColumnInfo.OutStreamField" ),
        ColumnInfo.COLUMN_TYPE_TEXT, false );
    ciKey[2] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "StringOperationsDialog.ColumnInfo.Trim" ),
        ColumnInfo.COLUMN_TYPE_CCOMBO, StringOperationsMeta.trimTypeDesc, true );
    ciKey[3] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "StringOperationsDialog.ColumnInfo.LowerUpper" ),
        ColumnInfo.COLUMN_TYPE_CCOMBO, StringOperationsMeta.lowerUpperDesc, true );
    ciKey[4] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "StringOperationsDialog.ColumnInfo.Padding" ),
        ColumnInfo.COLUMN_TYPE_CCOMBO, StringOperationsMeta.paddingDesc, true );
    ciKey[5] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "StringOperationsDialog.ColumnInfo.CharPad" ),
        ColumnInfo.COLUMN_TYPE_TEXT, false );
    ciKey[6] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "StringOperationsDialog.ColumnInfo.LenPad" ),
        ColumnInfo.COLUMN_TYPE_TEXT, false );
    ciKey[7] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "StringOperationsDialog.ColumnInfo.InitCap" ),
        ColumnInfo.COLUMN_TYPE_CCOMBO, StringOperationsMeta.initCapDesc );
    ciKey[8] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "StringOperationsDialog.ColumnInfo.MaskXML" ),
        ColumnInfo.COLUMN_TYPE_CCOMBO, StringOperationsMeta.maskXMLDesc );
    ciKey[9] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "StringOperationsDialog.ColumnInfo.Digits" ),
        ColumnInfo.COLUMN_TYPE_CCOMBO, StringOperationsMeta.digitsDesc );
    ciKey[10] =
      new ColumnInfo(
        BaseMessages.getString( PKG, "StringOperationsDialog.ColumnInfo.RemoveSpecialCharacters" ),
        ColumnInfo.COLUMN_TYPE_CCOMBO, StringOperationsMeta.removeSpecialCharactersDesc );

    ciKey[1]
      .setToolTip( BaseMessages.getString( PKG, "StringOperationsDialog.ColumnInfo.OutStreamField.Tooltip" ) );
    ciKey[1].setUsingVariables( true );
    ciKey[4].setUsingVariables( true );
    ciKey[5].setUsingVariables( true );
    ciKey[6].setUsingVariables( true );
    ciKey[7].setUsingVariables( true );

    wFields =
      new TableView(
        transMeta, shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL, ciKey,
        nrFieldRows, lsMod, props );

    fdKey = new FormData();
    fdKey.left = new FormAttachment( 0, 0 );
    fdKey.top = new FormAttachment( wlKey, margin );
    fdKey.right = new FormAttachment( 100, -margin );
    fdKey.bottom = new FormAttachment( 100, -30 );
    wFields.setLayoutData( fdKey );

    // THE BUTTONS
    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );

    wGet = new Button( shell, SWT.PUSH );
    wGet.setText( BaseMessages.getString( PKG, "StringOperationsDialog.GetFields.Button" ) );
    fdGet = new FormData();
    fdGet.right = new FormAttachment( 100, 0 );
    fdGet.top = new FormAttachment( wStepname, 3 * middle );
    wGet.setLayoutData( fdGet );

    setButtonPositions( new Button[] { wOK, wGet, wCancel }, margin, null );

    // Add listeners
    lsOK = new Listener() {
      public void handleEvent( Event e ) {
        ok();
      }
    };
    lsGet = new Listener() {
      public void handleEvent( Event e ) {
        get();
      }
    };
    lsCancel = new Listener() {
      public void handleEvent( Event e ) {
        cancel();
      }
    };

    wOK.addListener( SWT.Selection, lsOK );
    wGet.addListener( SWT.Selection, lsGet );
    wCancel.addListener( SWT.Selection, lsCancel );

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
            logError( "Error getting fields from incoming stream!", e );
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
    Set<String> keySet = inputFields.keySet();
    List<String> entries = new ArrayList<String>( keySet );
    String[] fieldNames = entries.toArray( new String[entries.size()] );
    Const.sortStrings( fieldNames );
    ciKey[0].setComboValues( fieldNames );

  }

  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void getData() {
    if ( input.getFieldInStream() != null ) {
      for ( int i = 0; i < input.getFieldInStream().length; i++ ) {
        TableItem item = wFields.table.getItem( i );
        if ( input.getFieldInStream()[i] != null ) {
          item.setText( 1, input.getFieldInStream()[i] );
        }
        if ( input.getFieldOutStream()[i] != null ) {
          item.setText( 2, input.getFieldOutStream()[i] );
        }
        item.setText( 3, input.getTrimType()[i] );
        item.setText( 4, input.getLowerUpper()[i] );
        item.setText( 5, input.getPaddingType()[i] );
        if ( input.getPadChar()[i] != null ) {
          item.setText( 6, input.getPadChar()[i] );
        }
        if ( input.getPadLen()[i] != null ) {
          item.setText( 7, input.getPadLen()[i] );
        }
        item.setText( 8, input.getInitCap()[i] );
        item.setText( 9, input.getMaskXML()[i] );
        item.setText( 10, input.getDigits()[i] );
        item.setText( 11, input.getRemoveSpecialCharacters()[i] );
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

  private void getInfo( StringOperationsMeta inf ) {
    int nrkeys = wFields.nrNonEmpty();

    inf.allocate( nrkeys );
    if ( isDebug() ) {
      logDebug( BaseMessages.getString( PKG, "StringOperationsDialog.Log.FoundFields", String.valueOf( nrkeys ) ) );
    }
    //CHECKSTYLE:Indentation:OFF
    for ( int i = 0; i < nrkeys; i++ ) {
      TableItem item = wFields.getNonEmpty( i );
      inf.getFieldInStream()[i] = item.getText( 1 );
      inf.getFieldOutStream()[i] = item.getText( 2 );
      inf.getTrimType()[i] = item.getText( 3 );
      inf.getLowerUpper()[i] = item.getText( 4 );
      inf.getPaddingType()[i] = item.getText( 5 );
      inf.getPadChar()[i] = item.getText( 6 );
      inf.getPadLen()[i] = item.getText( 7 );
      inf.getInitCap()[i] = item.getText( 8 );
      inf.getMaskXML()[i] = item.getText( 9 );
      inf.getDigits()[i] = item.getText( 10 );
      inf.getRemoveSpecialCharacters()[i] = item.getText( 11 );
    }

    stepname = wStepname.getText(); // return value
  }

  private void ok() {
    if ( Utils.isEmpty( wStepname.getText() ) ) {
      return;
    }

    // Get the information for the dialog into the input structure.
    getInfo( input );

    dispose();
  }

  private void get() {
    try {
      RowMetaInterface r = transMeta.getPrevStepFields( stepname );
      if ( r != null ) {
        TableItemInsertListener listener = new TableItemInsertListener() {
          public boolean tableItemInserted( TableItem tableItem, ValueMetaInterface v ) {
            if ( v.getType() == ValueMetaInterface.TYPE_STRING ) {
              // Only process strings
              tableItem.setText( 3, BaseMessages.getString( PKG, "StringOperationsMeta.TrimType.None" ) );
              tableItem.setText( 4, BaseMessages.getString( PKG, "StringOperationsMeta.LowerUpper.None" ) );
              tableItem.setText( 5, BaseMessages.getString( PKG, "StringOperationsMeta.Padding.None" ) );
              tableItem.setText( 8, BaseMessages.getString( PKG, "System.Combo.No" ) );
              tableItem.setText( 9, BaseMessages.getString( PKG, "StringOperationsMeta.MaskXML.None" ) );
              tableItem.setText( 10, BaseMessages.getString( PKG, "StringOperationsMeta.Digits.None" ) );
              tableItem.setText( 11, BaseMessages.getString(
                PKG, "StringOperationsMeta.RemoveSpecialCharacters.None" ) );
              return true;
            } else {
              return false;
            }
          }
        };

        BaseStepDialog.getFieldsFromPrevious( r, wFields, 1, new int[] { 1 }, new int[] {}, -1, -1, listener );

      }
    } catch ( KettleException ke ) {
      new ErrorDialog(
        shell, BaseMessages.getString( PKG, "StringOperationsDialog.FailedToGetFields.DialogTitle" ),
        BaseMessages.getString( PKG, "StringOperationsDialog.FailedToGetFields.DialogMessage" ), ke );
    }
  }
}
