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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
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
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

/**
 * Displays a TableView with the possibility to see different data sets per subject.
 *
 * @author Matt
 * @since 26-02-2013
 */
public class SubjectDataBrowserDialog {
  private static Class<?> PKG = SubjectDataBrowserDialog.class; // for i18n purposes, needed by Translator2!!

  public static final int MAX_BINARY_STRING_PREVIEW_SIZE = 1000000;

  private Label wlSubjectMessage;
  private CCombo wSubject;
  private TableView wFields;

  private Button wClose;

  private Shell shell;

  private Map<String, List<Object[]>> dataMap;
  private Map<String, RowMetaInterface> metaMap;

  private PropsUI props;

  private String dialogTitle, subjectMessage;

  private LogChannelInterface log;

  private Shell parentShell;

  private String selectedSubject;

  private String[] subjects;

  public SubjectDataBrowserDialog( Shell parent, Map<String, RowMetaInterface> metaMap,
    Map<String, List<Object[]>> dataMap, String dialogTitle, String subjectMessage ) {
    this.parentShell = parent;
    this.metaMap = metaMap;
    this.dataMap = dataMap;
    this.dialogTitle = dialogTitle;
    this.subjectMessage = subjectMessage;

    props = PropsUI.getInstance();

    subjects = metaMap.keySet().toArray( new String[metaMap.size()] );
    Arrays.sort( subjects );

    selectedSubject = "";
    if ( !metaMap.isEmpty() ) {
      selectedSubject = subjects[0];
    }

    this.log = new LogChannel( "Subject Data Browser Dialog" );
  }

  public void open() {
    shell = new Shell( parentShell, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );
    props.setLook( shell );
    shell.setImage( GUIResource.getInstance().getImageSpoon() );

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout( formLayout );
    shell.setText( dialogTitle );

    if ( addFields() ) {
      return;
    }

    wClose = new Button( shell, SWT.PUSH );
    wClose.setText( BaseMessages.getString( PKG, "System.Button.Close" ) );
    wClose.addListener( SWT.Selection, new Listener() {
      public void handleEvent( Event e ) {
        close();
      }
    } );

    // Position the buttons...
    //
    BaseStepDialog.positionBottomButtons( shell, new Button[] { wClose, }, Const.MARGIN, null );

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      public void shellClosed( ShellEvent e ) {
        close();
      }
    } );

    BaseStepDialog.setSize( shell );

    shell.open();

    while ( !shell.isDisposed() ) {
      if ( !shell.getDisplay().readAndDispatch() ) {
        shell.getDisplay().sleep();
      }
    }
  }

  private boolean addFields() {
    // int middle = props.getMiddlePct();
    int margin = Const.MARGIN;

    if ( wlSubjectMessage == null ) {
      wlSubjectMessage = new Label( shell, SWT.LEFT );
      wlSubjectMessage.setText( subjectMessage );
      props.setLook( wlSubjectMessage );
      FormData fdlFields = new FormData();
      fdlFields.left = new FormAttachment( 0, 0 );
      fdlFields.top = new FormAttachment( 0, margin );
      wlSubjectMessage.setLayoutData( fdlFields );

      wSubject = new CCombo( shell, SWT.LEFT | SWT.READ_ONLY | SWT.BORDER );
      wSubject.setItems( subjects );
      wSubject.setText( selectedSubject );
      props.setLook( wSubject );
      FormData fdlSubject = new FormData();
      fdlSubject.left = new FormAttachment( wlSubjectMessage, margin );
      // fdlSubject.right = new FormAttachment(100, 0);
      fdlSubject.top = new FormAttachment( wlSubjectMessage, 0, SWT.CENTER );
      wSubject.setLayoutData( fdlSubject );

      wSubject.addSelectionListener( new SelectionAdapter() {
        @Override
        public void widgetSelected( SelectionEvent arg0 ) {
          selectedSubject = wSubject.getText();
          addFields(); // Refresh
        }
      } );

    } else {
      wFields.dispose();
    }

    RowMetaInterface rowMeta = metaMap.get( selectedSubject );
    List<Object[]> buffer = dataMap.get( selectedSubject );

    // Mmm, if we don't get any rows in the buffer: show a dialog box.
    //
    if ( buffer == null ) {
      buffer = new ArrayList<Object[]>();
    }

    // ColumnInfo[] colinf = new ColumnInfo[rowMeta==null ? 0 : rowMeta.size()];
    ColumnInfo[] colinf = new ColumnInfo[rowMeta.size()];
    for ( int i = 0; i < rowMeta.size(); i++ ) {
      ValueMetaInterface v = rowMeta.getValueMeta( i );
      colinf[i] = new ColumnInfo( v.getName(), ColumnInfo.COLUMN_TYPE_TEXT, v.isNumeric() );
      colinf[i].setToolTip( v.toStringMeta() );
      colinf[i].setValueMeta( v );
    }

    wFields =
      new TableView( new Variables(), shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf, 0, null, props );
    wFields.setShowingBlueNullValues( true );

    FormData fdFields = new FormData();
    fdFields.left = new FormAttachment( 0, 0 );
    fdFields.top = new FormAttachment( wSubject, margin );
    fdFields.right = new FormAttachment( 100, 0 );
    fdFields.bottom = new FormAttachment( 100, -50 );
    wFields.setLayoutData( fdFields );

    // Add the data rows...
    //
    for ( int i = 0; i < buffer.size(); i++ ) {
      TableItem item;
      if ( i == 0 ) {
        item = wFields.table.getItem( i );
      } else {
        item = new TableItem( wFields.table, SWT.NONE );
      }

      Object[] rowData = buffer.get( i );

      getDataForRow( item, rowMeta, rowData, i + 1 );
    }
    if ( !wFields.isDisposed() ) {
      wFields.optWidth( true, 200 );
    }

    shell.layout( true, true );

    return false;
  }

  public void dispose() {
    props.setScreen( new WindowProperty( shell ) );
    shell.dispose();
  }

  protected int getDataForRow( TableItem item, RowMetaInterface rowMeta, Object[] row, int lineNr ) {
    int nrErrors = 0;

    // Display the correct line item...
    //
    String strNr;
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

  private void close() {
    dispose();
  }

  public boolean isDisposed() {
    return shell.isDisposed();
  }
}
