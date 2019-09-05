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

package org.pentaho.di.ui.trans.steps.uniquerows;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.uniquerows.UniqueRowsMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class UniqueRowsDialog extends BaseStepDialog implements StepDialogInterface {
  private static Class<?> PKG = UniqueRowsMeta.class; // for i18n purposes, needed by Translator2!!

  public static final String STRING_SORT_WARNING_PARAMETER = "UniqueSortWarning";

  private UniqueRowsMeta input;

  private Label wlCount;
  private Button wCount;
  private FormData fdlCount, fdCount;

  private Label wlCountField;
  private Text wCountField;
  private FormData fdlCountField, fdCountField;

  private Label wlFields;
  private TableView wFields;
  private FormData fdlFields, fdFields;

  private ColumnInfo[] colinf;

  private Map<String, Integer> inputFields;

  private Label wlRejectDuplicateRow;
  private Button wRejectDuplicateRow;
  private FormData fdlRejectDuplicateRow, fdRejectDuplicateRow;

  private Label wlErrorDesc;
  private TextVar wErrorDesc;
  private FormData fdlErrorDesc, fdErrorDesc;

  private Group wSettings;
  private FormData fdSettings;

  public UniqueRowsDialog( Shell parent, Object in, TransMeta transMeta, String sname ) {
    super( parent, (BaseStepMeta) in, transMeta, sname );
    input = (UniqueRowsMeta) in;
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
    shell.setText( BaseMessages.getString( PKG, "UniqueRowsDialog.Shell.Title" ) );

    int middle = props.getMiddlePct();
    int margin = Const.MARGIN;

    // Stepname line
    wlStepname = new Label( shell, SWT.RIGHT );
    wlStepname.setText( BaseMessages.getString( PKG, "UniqueRowsDialog.Stepname.Label" ) );
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

    // ///////////////////////////////
    // START OF Settings GROUP //
    // ///////////////////////////////

    wSettings = new Group( shell, SWT.SHADOW_NONE );
    props.setLook( wSettings );
    wSettings.setText( BaseMessages.getString( PKG, "UniqueRowsDialog.Settings.Label" ) );

    FormLayout SettingsgroupLayout = new FormLayout();
    SettingsgroupLayout.marginWidth = 10;
    SettingsgroupLayout.marginHeight = 10;
    wSettings.setLayout( SettingsgroupLayout );

    wlCount = new Label( wSettings, SWT.RIGHT );
    wlCount.setText( BaseMessages.getString( PKG, "UniqueRowsDialog.Count.Label" ) );
    props.setLook( wlCount );
    fdlCount = new FormData();
    fdlCount.left = new FormAttachment( 0, 0 );
    fdlCount.top = new FormAttachment( wStepname, margin );
    fdlCount.right = new FormAttachment( middle, -margin );
    wlCount.setLayoutData( fdlCount );

    wCount = new Button( wSettings, SWT.CHECK );
    props.setLook( wCount );
    wCount.setToolTipText( BaseMessages.getString( PKG, "UniqueRowsDialog.Count.ToolTip", Const.CR ) );
    fdCount = new FormData();
    fdCount.left = new FormAttachment( middle, 0 );
    fdCount.top = new FormAttachment( wStepname, margin );
    wCount.setLayoutData( fdCount );
    wCount.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
        setFlags();
      }
    } );

    wlCountField = new Label( wSettings, SWT.LEFT );
    wlCountField.setText( BaseMessages.getString( PKG, "UniqueRowsDialog.CounterField.Label" ) );
    props.setLook( wlCountField );
    fdlCountField = new FormData();
    fdlCountField.left = new FormAttachment( wCount, margin );
    fdlCountField.top = new FormAttachment( wStepname, margin );
    wlCountField.setLayoutData( fdlCountField );
    wCountField = new Text( wSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wCountField );
    wCountField.addModifyListener( lsMod );
    fdCountField = new FormData();
    fdCountField.left = new FormAttachment( wlCountField, margin );
    fdCountField.top = new FormAttachment( wStepname, margin );
    fdCountField.right = new FormAttachment( 100, 0 );
    wCountField.setLayoutData( fdCountField );

    wlRejectDuplicateRow = new Label( wSettings, SWT.RIGHT );
    wlRejectDuplicateRow.setText( BaseMessages.getString( PKG, "UniqueRowsDialog.RejectDuplicateRow.Label" ) );
    props.setLook( wlRejectDuplicateRow );
    fdlRejectDuplicateRow = new FormData();
    fdlRejectDuplicateRow.left = new FormAttachment( 0, 0 );
    fdlRejectDuplicateRow.top = new FormAttachment( wCountField, margin );
    fdlRejectDuplicateRow.right = new FormAttachment( middle, -margin );
    wlRejectDuplicateRow.setLayoutData( fdlRejectDuplicateRow );

    wRejectDuplicateRow = new Button( wSettings, SWT.CHECK );
    props.setLook( wRejectDuplicateRow );
    wRejectDuplicateRow.setToolTipText( BaseMessages.getString(
      PKG, "UniqueRowsDialog.RejectDuplicateRow.ToolTip", Const.CR ) );
    fdRejectDuplicateRow = new FormData();
    fdRejectDuplicateRow.left = new FormAttachment( middle, margin );
    fdRejectDuplicateRow.top = new FormAttachment( wCountField, margin );
    wRejectDuplicateRow.setLayoutData( fdRejectDuplicateRow );
    wRejectDuplicateRow.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        input.setChanged();
        setErrorDesc();
      }
    } );

    wlErrorDesc = new Label( wSettings, SWT.LEFT );
    wlErrorDesc.setText( BaseMessages.getString( PKG, "UniqueRowsDialog.ErrorDescription.Label" ) );
    props.setLook( wlErrorDesc );
    fdlErrorDesc = new FormData();
    fdlErrorDesc.left = new FormAttachment( wRejectDuplicateRow, margin );
    fdlErrorDesc.top = new FormAttachment( wCountField, margin );
    wlErrorDesc.setLayoutData( fdlErrorDesc );
    wErrorDesc = new TextVar( transMeta, wSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wErrorDesc );
    wErrorDesc.addModifyListener( lsMod );
    fdErrorDesc = new FormData();
    fdErrorDesc.left = new FormAttachment( wlErrorDesc, margin );
    fdErrorDesc.top = new FormAttachment( wCountField, margin );
    fdErrorDesc.right = new FormAttachment( 100, 0 );
    wErrorDesc.setLayoutData( fdErrorDesc );

    fdSettings = new FormData();
    fdSettings.left = new FormAttachment( 0, margin );
    fdSettings.top = new FormAttachment( wStepname, margin );
    fdSettings.right = new FormAttachment( 100, -margin );
    wSettings.setLayoutData( fdSettings );

    // ///////////////////////////////////////////////////////////
    // / END OF Settings GROUP
    // ///////////////////////////////////////////////////////////

    // Some buttons
    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
    wGet = new Button( shell, SWT.PUSH );
    wGet.setText( BaseMessages.getString( PKG, "UniqueRowsDialog.Get.Button" ) );
    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );
    fdOK = new FormData();

    setButtonPositions( new Button[] { wOK, wCancel, wGet }, margin, null );

    wlFields = new Label( shell, SWT.NONE );
    wlFields.setText( BaseMessages.getString( PKG, "UniqueRowsDialog.Fields.Label" ) );
    props.setLook( wlFields );
    fdlFields = new FormData();
    fdlFields.left = new FormAttachment( 0, 0 );
    fdlFields.top = new FormAttachment( wSettings, margin );
    wlFields.setLayoutData( fdlFields );

    final int FieldsRows = input.getCompareFields() == null ? 0 : input.getCompareFields().length;

    colinf =
      new ColumnInfo[] {
        new ColumnInfo(
          BaseMessages.getString( PKG, "UniqueRowsDialog.ColumnInfo.Fieldname" ),
          ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "UniqueRowsDialog.ColumnInfo.IgnoreCase" ), ColumnInfo.COLUMN_TYPE_CCOMBO,
          new String[] { "Y", "N" }, true )
      };

    wFields =
      new TableView(
        transMeta, shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf, FieldsRows, lsMod, props );

    fdFields = new FormData();
    fdFields.left = new FormAttachment( 0, 0 );
    fdFields.top = new FormAttachment( wlFields, margin );
    fdFields.right = new FormAttachment( 100, 0 );
    fdFields.bottom = new FormAttachment( wOK, -2 * margin );
    wFields.setLayoutData( fdFields );

    //
    // Search the fields in the background

    final Runnable runnable = new Runnable() {
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
    wGet.addListener( SWT.Selection, lsGet );
    wOK.addListener( SWT.Selection, lsOK );

    lsDef = new SelectionAdapter() {
      public void widgetDefaultSelected( SelectionEvent e ) {
        ok();
      }
    };

    wStepname.addSelectionListener( lsDef );
    wCountField.addSelectionListener( lsDef );

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

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

  private void setErrorDesc() {
    wlErrorDesc.setEnabled( wRejectDuplicateRow.getSelection() );
    wErrorDesc.setEnabled( wRejectDuplicateRow.getSelection() );
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

  public void setFlags() {
    wlCountField.setEnabled( wCount.getSelection() );
    wCountField.setEnabled( wCount.getSelection() );
  }

  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void getData() {
    wCount.setSelection( input.isCountRows() );
    if ( input.getCountField() != null ) {
      wCountField.setText( input.getCountField() );
    }
    setFlags();
    wRejectDuplicateRow.setSelection( input.isRejectDuplicateRow() );
    if ( input.getErrorDescription() != null ) {
      wErrorDesc.setText( input.getErrorDescription() );
    }
    setErrorDesc();
    for ( int i = 0; i < input.getCompareFields().length; i++ ) {
      TableItem item = wFields.table.getItem( i );
      if ( input.getCompareFields()[i] != null ) {
        item.setText( 1, input.getCompareFields()[i] );
      }
      item.setText( 2, input.getCaseInsensitive()[i] ? "Y" : "N" );
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

    int nrfields = wFields.nrNonEmpty();
    input.allocate( nrfields );

    //CHECKSTYLE:Indentation:OFF
    for ( int i = 0; i < nrfields; i++ ) {
      TableItem item = wFields.getNonEmpty( i );
      input.getCompareFields()[i] = item.getText( 1 );
      input.getCaseInsensitive()[i] = "Y".equalsIgnoreCase( item.getText( 2 ) );
    }

    input.setCountField( wCountField.getText() );
    input.setCountRows( wCount.getSelection() );
    input.setRejectDuplicateRow( wRejectDuplicateRow.getSelection() );
    input.setErrorDescription( wErrorDesc.getText() );
    stepname = wStepname.getText(); // return value

    if ( "Y".equalsIgnoreCase( props.getCustomParameter( STRING_SORT_WARNING_PARAMETER, "Y" ) ) ) {
      MessageDialogWithToggle md = new MessageDialogWithToggle( shell,
        BaseMessages.getString( PKG, "UniqueRowsDialog.InputNeedSort.DialogTitle" ),
        null,
        BaseMessages.getString( PKG, "UniqueRowsDialog.InputNeedSort.DialogMessage", Const.CR ) + Const.CR,
        MessageDialog.WARNING,
        new String[] { BaseMessages.getString( PKG, "UniqueRowsDialog.InputNeedSort.Option1" ) },
        0,
        BaseMessages.getString( PKG, "UniqueRowsDialog.InputNeedSort.Option2" ),
        "N".equalsIgnoreCase( props.getCustomParameter( STRING_SORT_WARNING_PARAMETER, "Y" ) ) );
      MessageDialogWithToggle.setDefaultImage( GUIResource.getInstance().getImageSpoon() );
      md.open();
      props.setCustomParameter( STRING_SORT_WARNING_PARAMETER, md.getToggleState() ? "N" : "Y" );
      props.saveProps();
    }

    // Remove any error hops coming out of UniqueRows when Reject Duplicate Rows checkbox is unselected.
    if ( wRejectDuplicateRow.getSelection() == false ) {
      List<TransHopMeta> hops = this.transMeta.getTransHops();
      IntStream.range( 0, hops.size() )
        .filter( hopInd -> {
          TransHopMeta hop = hops.get( hopInd );
          return (
            hop.isErrorHop()
            && hop.getFromStep().getName().equals( this.input.getParentStepMeta().getName() ) );
        } )
        .forEach( hopInd -> this.transMeta.removeTransHop( hopInd ) );
    }

    dispose();
  }

  private void get() {
    try {
      RowMetaInterface r = transMeta.getPrevStepFields( stepname );
      if ( r != null && !r.isEmpty() ) {
        BaseStepDialog.getFieldsFromPrevious( r, wFields, 1, new int[] { 1 }, new int[] {}, -1, -1, null );
      }
    } catch ( KettleException ke ) {
      new ErrorDialog(
        shell, BaseMessages.getString( PKG, "UniqueRowsDialog.FailedToGetFields.DialogTitle" ), BaseMessages
          .getString( PKG, "UniqueRowsDialog.FailedToGetFields.DialogMessage" ), ke );
    }
  }

}
