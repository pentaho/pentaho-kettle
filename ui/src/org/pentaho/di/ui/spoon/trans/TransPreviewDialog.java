/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.ui.spoon.trans;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.debug.StepDebugMeta;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.dialog.DialogClosedListener;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.spoon.SpoonUiExtenderPluginInterface;
import org.pentaho.di.ui.spoon.SpoonUiExtenderPluginType;

public class TransPreviewDialog {
  private static Class<?> PKG = PreviewRowsDialog.class; // for i18n purposes, needed by Translator2!!

  public static final String TRANS_PREVIEW_DIALOG = "TRANS_PREVIEW_DIALOG";

  public static final String TRANS_PREVIEW_DIALOG_SET_DATA = "TRANS_PREVIEW_DIALOG_SET_DATA";

  public static final int MAX_BINARY_STRING_PREVIEW_SIZE = 1000000;

  private Label wlFields;

  private TableView wFields;

  private CTabFolder tabFolder;

  private CTabItem rowsCtabItem;

  private Composite rowsComposite;

  private Shell shell;

  private Button wNext;

  private PropsUI props;

  private Rectangle bounds;

  private int hscroll, vscroll;

  private int hmax, vmax;

  private String loggingText;

  private VariableSpace variables;

  private LogChannelInterface log;

  private boolean waitingForRows;

  private int style = SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN;

  private Shell parentShell;

  private List<DialogClosedListener> dialogClosedListeners;

  private final Runnable moreRows;

  private final Runnable stop;

  public TransPreviewDialog( Shell parent, VariableSpace space, int style, Runnable moreRows, Runnable stop ) {
    this( parent, space, style, null, moreRows, stop );
  }

  public TransPreviewDialog( Shell parent, VariableSpace space, int style, String loggingText, Runnable moreRows,
      Runnable stop ) {
    this.loggingText = loggingText;
    this.variables = space;
    this.parentShell = parent;
    this.style = ( style != SWT.None ) ? style : this.style;
    this.dialogClosedListeners = new ArrayList<DialogClosedListener>();
    this.moreRows = moreRows;
    this.stop = stop;

    props = PropsUI.getInstance();
    bounds = null;
    hscroll = -1;
    vscroll = -1;

    this.log = new LogChannel( "Row Preview" );
  }

  public void open() {
    shell = new Shell( parentShell, style );
    props.setLook( shell );
    shell.setImage( GUIResource.getInstance().getImageSpoon() );

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout( formLayout );
    shell.setText( BaseMessages.getString( PKG, "PreviewRowsDialog.Title" ) );

    List<Button> buttons = new ArrayList<Button>();

    Button wClose = new Button( shell, SWT.PUSH );
    wClose.setText( BaseMessages.getString( PKG, "System.Button.Close" ) );
    wClose.addListener( SWT.Selection, new Listener() {
      public void handleEvent( Event e ) {
        close();
      }
    } );
    buttons.add( wClose );

    if ( !Const.isEmpty( loggingText ) ) {
      Button wLog = new Button( shell, SWT.PUSH );
      wLog.setText( BaseMessages.getString( PKG, "PreviewRowsDialog.Button.ShowLog" ) );
      wLog.addListener( SWT.Selection, new Listener() {
        public void handleEvent( Event e ) {
          log();
        }
      } );
      buttons.add( wLog );
    }

    Button wStop = new Button( shell, SWT.PUSH );
    wStop.setText( BaseMessages.getString( PKG, "PreviewRowsDialog.Button.Stop.Label" ) );
    wStop.setToolTipText( BaseMessages.getString( PKG, "PreviewRowsDialog.Button.Stop.ToolTip" ) );
    wStop.addListener( SWT.Selection, new Listener() {
      public void handleEvent( Event e ) {
        stop.run();
        close();
      }
    } );
    buttons.add( wStop );

    wNext = new Button( shell, SWT.PUSH );
    wNext.setText( BaseMessages.getString( PKG, "PreviewRowsDialog.Button.Next.Label" ) );
    wNext.setToolTipText( BaseMessages.getString( PKG, "PreviewRowsDialog.Button.Next.ToolTip" ) );
    wNext.addListener( SWT.Selection, new Listener() {
      public void handleEvent( Event e ) {
        wNext.setEnabled( false );
        moreRows.run();
      }
    } );
    wNext.setEnabled( false );
    buttons.add( wNext );

    wClose.setText( BaseMessages.getString( PKG, "PreviewRowsDialog.Button.Close.Label" ) );
    wClose.setToolTipText( BaseMessages.getString( PKG, "PreviewRowsDialog.Button.Close.ToolTip" ) );

    // Position the buttons...
    //
    BaseStepDialog.positionBottomButtons( shell, buttons.toArray( new Button[buttons.size()] ), Const.MARGIN, null );

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      public void shellClosed( ShellEvent e ) {
        close();
      }
    } );

    BaseStepDialog.setSize( shell );
    int margin = Const.MARGIN;

    wlFields = new Label( shell, SWT.LEFT );
    wlFields.setText( BaseMessages.getString( PKG, "PreviewRowsDialog.Header", "" ) );
    props.setLook( wlFields );
    FormData fdlFields = new FormData();
    fdlFields.left = new FormAttachment( 0, 0 );
    fdlFields.right = new FormAttachment( 100, 0 );
    fdlFields.top = new FormAttachment( 0, margin );
    wlFields.setLayoutData( fdlFields );

    tabFolder = new CTabFolder( shell, SWT.MULTI );
    FormData fdFields = new FormData();
    fdFields.left = new FormAttachment( 0, 0 );
    fdFields.top = new FormAttachment( wlFields, margin );
    fdFields.right = new FormAttachment( 100, 0 );
    fdFields.bottom = new FormAttachment( 100, -50 );
    props.setLook( tabFolder );
    tabFolder.setLayoutData( fdFields );

    rowsCtabItem = new CTabItem( tabFolder, SWT.NONE );
    rowsCtabItem.setText( BaseMessages.getString( PKG, "PreviewRowsDialog.TabHeader" ) );

    rowsComposite = new Composite( tabFolder, SWT.NONE );
    fdFields = new FormData();
    fdFields.left = new FormAttachment( 0, 0 );
    fdFields.top = new FormAttachment( wlFields, margin );
    fdFields.right = new FormAttachment( 100, 0 );
    fdFields.bottom = new FormAttachment( 100, 0 );
    rowsComposite.setLayoutData( fdFields );
    rowsComposite.setLayout( new FormLayout() );
    props.setLook( rowsComposite );
    rowsCtabItem.setControl( rowsComposite );

    tabFolder.setSelection( 0 );
    List<SpoonUiExtenderPluginInterface> relevantExtenders =
        SpoonUiExtenderPluginType.getInstance().getRelevantExtenders( TransPreviewDialog.class, TRANS_PREVIEW_DIALOG );
    for ( SpoonUiExtenderPluginInterface relevantExtender : relevantExtenders ) {
      relevantExtender.uiEvent( this, TRANS_PREVIEW_DIALOG );
    }
    shell.open();

    if ( !waitingForRows ) {
      while ( !shell.isDisposed() ) {
        if ( !shell.getDisplay().readAndDispatch() ) {
          shell.getDisplay().sleep();
        }
      }
    }
  }

  public CTabFolder getTabFolder() {
    return tabFolder;
  }

  public Label getWlFields() {
    return wlFields;
  }

  public void dispose() {
    props.setScreen( new WindowProperty( shell ) );
    bounds = shell.getBounds();
    if ( wFields != null ) {
      ScrollBar hScrollBar = wFields.getHorizontalBar();
      if ( hScrollBar != null ) {
        hscroll = hScrollBar.getSelection();
      }
      ScrollBar vScrollBar = wFields.getVerticalBar();
      if ( vScrollBar != null ) {
        vscroll = vScrollBar.getSelection();
      }
    }
    shell.dispose();
  }

  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void setData( final StepDebugMeta stepDebugMeta, final RowMetaInterface rowMeta, final List<Object[]> buffer ) {
    shell.getDisplay().asyncExec( new Runnable() {
      public void run() {
        List<SpoonUiExtenderPluginInterface> relevantExtenders =
            SpoonUiExtenderPluginType.getInstance().getRelevantExtenders( TransPreviewDialogSetDataWrapper.class,
                TRANS_PREVIEW_DIALOG_SET_DATA );
        TransPreviewDialogSetDataWrapper transDebugMetaWrapper =
            new TransPreviewDialogSetDataWrapper( TransPreviewDialog.this, stepDebugMeta );
        for ( SpoonUiExtenderPluginInterface relevantExtender : relevantExtenders ) {
          relevantExtender.uiEvent( transDebugMetaWrapper, TRANS_PREVIEW_DIALOG_SET_DATA );
        }
        // int middle = props.getMiddlePct();
        String stepName = stepDebugMeta.getStepMeta().getName();
        shell.setText( stepName );
        wlFields.setText( BaseMessages.getString( PKG, "PreviewRowsDialog.Header", stepName ) );
        int margin = Const.MARGIN;

        // ColumnInfo[] colinf = new ColumnInfo[rowMeta==null ? 0 : rowMeta.size()];
        ColumnInfo[] colinf = new ColumnInfo[rowMeta.size()];
        for ( int i = 0; i < rowMeta.size(); i++ ) {
          ValueMetaInterface v = rowMeta.getValueMeta( i );
          colinf[i] = new ColumnInfo( v.getName(), ColumnInfo.COLUMN_TYPE_TEXT, v.isNumeric() );
          colinf[i].setToolTip( v.toStringMeta() );
          colinf[i].setValueMeta( v );
        }

        if ( wFields != null ) {
          wFields.dispose();
        }
        wFields =
            new TableView( variables, rowsComposite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf, 0, null,
                props );
        FormData fdFields = new FormData();
        fdFields.left = new FormAttachment( 0, 0 );
        fdFields.top = new FormAttachment( wlFields, margin );
        fdFields.right = new FormAttachment( 100, 0 );
        fdFields.bottom = new FormAttachment( 100, 0 );
        wFields.setLayoutData( fdFields );
        wFields.setShowingBlueNullValues( true );

        shell.layout( true, true );

        int i = 0;
        for ( Object[] row : buffer ) {
          TableItem item;
          if ( i == 0 ) {
            item = wFields.table.getItem( i );
          } else {
            item = new TableItem( wFields.table, SWT.NONE );
          }

          int nrErrors = 0;

          // Display the correct line item...
          //
          String strNr;
          int lineNr = i + 1;
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
          i++;
        }
        if ( !wFields.isDisposed() ) {
          wFields.optWidth( true, 200 );
        }
        wNext.setEnabled( true );
      }
    } );
  }

  private void close() {
    dispose();
  }

  /**
   * Show the logging of the preview (in case errors occurred
   */
  private void log() {
    if ( loggingText != null ) {
      EnterTextDialog etd =
          new EnterTextDialog( shell, BaseMessages.getString( PKG, "PreviewRowsDialog.ShowLogging.Title" ),
              BaseMessages.getString( PKG, "PreviewRowsDialog.ShowLogging.Message" ), loggingText );
      etd.open();
    }
  }

  public boolean isDisposed() {
    return shell.isDisposed();
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

  public void addDialogClosedListener( DialogClosedListener listener ) {
    dialogClosedListeners.add( listener );
  }

  public class TransPreviewDialogSetDataWrapper {
    private final TransPreviewDialog transPreviewDialog;
    private final StepDebugMeta stepDebugMeta;

    public TransPreviewDialogSetDataWrapper( TransPreviewDialog transPreviewDialog, StepDebugMeta stepDebugMeta ) {
      super();
      this.transPreviewDialog = transPreviewDialog;
      this.stepDebugMeta = stepDebugMeta;
    }

    public TransPreviewDialog getTransPreviewDialog() {
      return transPreviewDialog;
    }

    public StepDebugMeta getStepDebugMeta() {
      return stepDebugMeta;
    }
  }
}
