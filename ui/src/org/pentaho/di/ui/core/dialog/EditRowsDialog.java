/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

import java.util.ArrayList;
import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

/**
 * Allows the user to edit a list of rows in a TableView.
 *
 * @author Matt
 * @since 19-03-2014
 */
public class EditRowsDialog {
  private static Class<?> PKG = EditRowsDialog.class; // for i18n purposes, needed by Translator2!!

  public static final int MAX_BINARY_STRING_PREVIEW_SIZE = 1000000;

  private Label wlMessage;

  private TableView wFields;

  private FormData fdlFields, fdFields;

  private Button wOK;

  private Button wCancel;

  private Shell shell;

  private List<Object[]> rowBuffer;

  private PropsUI props;

  private String title, message;

  private Rectangle bounds;

  private int hscroll, vscroll;

  private int hmax, vmax;

  private RowMetaInterface rowMeta;

  private LogChannelInterface log;

  protected int lineNr;

  private int style = SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN;

  private Shell parentShell;

  private List<Object[]> outputList;

  private RowMetaInterface stringRowMeta;

  public EditRowsDialog( Shell parent, int style,
    String title, String message, RowMetaInterface rowMeta, List<Object[]> rowBuffer ) {
    this.title = title;
    this.message = message;
    this.rowBuffer = rowBuffer;
    this.rowMeta = rowMeta;
    this.parentShell = parent;
    this.style = ( style != SWT.None ) ? style : this.style;

    props = PropsUI.getInstance();
    bounds = null;
    hscroll = -1;
    vscroll = -1;
    title = null;
    message = null;

    this.log = LogChannel.GENERAL;
  }

  public void setTitleMessage( String title, String message ) {
    this.title = title;
    this.message = message;
  }

  public List<Object[]> open() {
    shell = new Shell( parentShell, style );
    props.setLook( shell );
    shell.setImage( GUIResource.getInstance().getImageSpoon() );

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout( formLayout );
    shell.setText( title );

    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
    wOK.addListener( SWT.Selection, new Listener() {
      public void handleEvent( Event e ) {
        ok();
      }
    } );

    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );
    wCancel.addListener( SWT.Selection, new Listener() {
      public void handleEvent( Event e ) {
        cancel();
      }
    } );

    // Position the buttons...
    //
    BaseStepDialog.positionBottomButtons( shell, new Button[] { wOK, wCancel, }, Const.MARGIN, null );

    if ( addFields() ) {
      return null;
    }

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    getData();

    BaseStepDialog.setSize( shell );

    shell.open();

    while ( !shell.isDisposed() ) {
      if ( !shell.getDisplay().readAndDispatch() ) {
        shell.getDisplay().sleep();
      }
    }

    return outputList;

  }

  private boolean addFields() {
    // int middle = props.getMiddlePct();
    int margin = Const.MARGIN;

    if ( wlMessage == null ) {
      wlMessage = new Label( shell, SWT.LEFT );
      wlMessage.setText( message );
      props.setLook( wlMessage );
      fdlFields = new FormData();
      fdlFields.left = new FormAttachment( 0, 0 );
      fdlFields.right = new FormAttachment( 100, 0 );
      fdlFields.top = new FormAttachment( 0, margin );
      wlMessage.setLayoutData( fdlFields );
    } else {
      wFields.dispose();
    }

    // Mmm, if we don't get any row metadata: show a dialog box.
    if ( rowMeta == null || rowMeta.size() == 0 ) {
      ShowMessageDialog dialog = new ShowMessageDialog( shell, SWT.OK | SWT.ICON_WARNING,
        BaseMessages.getString( PKG, "EditRowsDialog.NoRowMeta.Text" ),
        BaseMessages.getString( PKG, "EditRowsDialog.NoRowMeta.Message" ) );
      dialog.open();
      shell.dispose();
      return true;
    }

    // ColumnInfo[] colinf = new ColumnInfo[rowMeta==null ? 0 : rowMeta.size()];
    ColumnInfo[] colinf = new ColumnInfo[rowMeta.size()];
    for ( int i = 0; i < rowMeta.size(); i++ ) {
      ValueMetaInterface v = rowMeta.getValueMeta( i );
      colinf[i] = new ColumnInfo( v.getName(), ColumnInfo.COLUMN_TYPE_TEXT, v.isNumeric() );
      colinf[i].setToolTip( v.toStringMeta() );
      colinf[i].setValueMeta( v );
    }

    wFields = new TableView(
      new Variables(),
      shell,
      SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI,
      colinf,
      rowBuffer.size(),
      null,
      props );
    wFields.setShowingBlueNullValues( true );

    fdFields = new FormData();
    fdFields.left = new FormAttachment( 0, 0 );
    fdFields.top = new FormAttachment( wlMessage, margin );
    fdFields.right = new FormAttachment( 100, 0 );
    fdFields.bottom = new FormAttachment( 100, -50 );
    wFields.setLayoutData( fdFields );

    shell.layout( true, true );

    return false;
  }

  public void dispose() {
    props.setScreen( new WindowProperty( shell ) );
    bounds = shell.getBounds();
    hscroll = wFields.getHorizontalBar().getSelection();
    vscroll = wFields.getVerticalBar().getSelection();
    shell.dispose();
  }

  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  private void getData() {
    shell.getDisplay().asyncExec( new Runnable() {
      public void run() {
        lineNr = 0;
        for ( int i = 0; i < rowBuffer.size(); i++ ) {
          TableItem item = wFields.table.getItem( i );
          Object[] row = rowBuffer.get( i );
          getDataForRow( item, row );
        }
        wFields.optWidth( true, 200 );
      }
    } );
  }

  protected int getDataForRow( TableItem item, Object[] row ) {
    int nrErrors = 0;

    // Display the correct line item...
    //
    String strNr;
    lineNr++;
    try {
      strNr = wFields.getNumberColumn().getValueMeta().getString( new Long( lineNr ) );
    } catch ( Exception e ) {
      strNr = Integer.toString( lineNr );
    }
    item.setText( 0, strNr );

    for ( int c = 0; c < rowMeta.size(); c++ ) {
      ValueMetaInterface v = rowMeta.getValueMeta( c );
      String show;
      try {
        show = v.getString( row[c] );
        if ( v.isBinary() && show != null && show.length() > MAX_BINARY_STRING_PREVIEW_SIZE ) {
          // We want to limit the size of the strings during preview to keep all SWT widgets happy.
          //
          show = show.substring( 0, MAX_BINARY_STRING_PREVIEW_SIZE );
        }
      } catch ( KettleValueException e ) {
        nrErrors++;
        if ( nrErrors < 25 ) {
          log.logError( Const.getStackTracker( e ) );
        }
        show = null;
      } catch ( ArrayIndexOutOfBoundsException e ) {
        nrErrors++;
        if ( nrErrors < 25 ) {
          log.logError( Const.getStackTracker( e ) );
        }
        show = null;
      }

      if ( show != null ) {
        item.setText( c + 1, show );
        item.setForeground( c + 1, GUIResource.getInstance().getColorBlack() );
      } else {
        // Set null value
        item.setText( c + 1, "<null>" );
        item.setForeground( c + 1, GUIResource.getInstance().getColorBlue() );
      }
    }

    return nrErrors;

  }

  @VisibleForTesting
  Object[] getRowForData( TableItem item, int rowNr ) throws KettleException {
    try {
      Object[] row = RowDataUtil.allocateRowData( rowMeta.size() );
      for ( int i = 0; i < rowMeta.size(); i++ ) {
        ValueMetaInterface valueMeta = rowMeta.getValueMeta( i );
        ValueMetaInterface stringValueMeta = stringRowMeta.getValueMeta( i );

        int colnr = i + 1;
        if ( isDisplayingNullValue( item, colnr ) ) {
          row[i] = null; // <null> value
        } else {
          String string = item.getText( colnr );
          if ( stringValueMeta.isNull( string ) ) {
            string = null;
          }
          row[i] = valueMeta.convertDataFromString( string, stringValueMeta,
            null, null, ValueMetaInterface.TRIM_TYPE_NONE );
        }
      }
      return row;
    } catch ( KettleException e ) {
      throw new KettleException( BaseMessages.getString( PKG, "EditRowsDialog.Error.ErrorGettingRowForData",
        Integer.toString( rowNr ) ), e );
    }
  }

  @VisibleForTesting
  boolean isDisplayingNullValue( TableItem item, int column ) throws KettleException {
    return GUIResource.getInstance().getColorBlue().equals( item.getForeground( column ) );
  }

  private void ok() {

    try {
      stringRowMeta = new RowMeta();
      for ( ValueMetaInterface valueMeta : rowMeta.getValueMetaList() ) {
        ValueMetaInterface stringValueMeta = ValueMetaFactory.cloneValueMeta( valueMeta,
          ValueMetaInterface.TYPE_STRING );
        stringRowMeta.addValueMeta( stringValueMeta );
      }

      List<Object[]> list = new ArrayList<Object[]>();

      // Now read all the rows in the dialog, including the empty rows...
      //
      for ( int i = 0; i < wFields.getItemCount(); i++ ) {
        TableItem item = wFields.getTable().getItem( i );
        Object[] row = getRowForData( item, i + 1 );
        list.add( row );
      }

      outputList = list;
      dispose();

    } catch ( Exception e ) {
      new ErrorDialog( shell, "Error", BaseMessages.getString( PKG, "EditRowsDialog.ErrorConvertingData" ), e );
    }
  }

  private void cancel() {
    outputList = null;
    dispose();
  }

  public Rectangle getBounds() {
    return bounds;
  }

  public void setBounds( Rectangle b ) {
    bounds = b;
  }

  public int getHScroll() {
    return hscroll;
  }

  public void setHScroll( int s ) {
    hscroll = s;
  }

  public int getVScroll() {
    return vscroll;
  }

  public void setVScroll( int s ) {
    vscroll = s;
  }

  public int getHMax() {
    return hmax;
  }

  public void setHMax( int m ) {
    hmax = m;
  }

  public int getVMax() {
    return vmax;
  }

  public void setVMax( int m ) {
    vmax = m;
  }


  @VisibleForTesting
  void setRowMeta( RowMetaInterface rowMeta ) {
    this.rowMeta = rowMeta;
  }

  @VisibleForTesting
  void setStringRowMeta( RowMetaInterface stringRowMeta ) {
    this.stringRowMeta = stringRowMeta;
  }
}
