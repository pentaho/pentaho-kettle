/* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

package org.pentaho.di.ui.core.dialog;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Rectangle;
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
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

/**
 * Displays an ArrayList of rows in a TableView.
 * 
 * @author Matt
 * @since 19-06-2003
 */
public class PreviewRowsDialog {
  private static Class<?> PKG = PreviewRowsDialog.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

  public static final int MAX_BINARY_STRING_PREVIEW_SIZE = 1000000;

  private String stepname;

  private Label wlFields;

  private TableView wFields;

  private FormData fdlFields, fdFields;

  private Button wClose;

  private Button wStop;

  private Button wNext;

  private Button wLog;

  private Shell shell;

  private List<Object[]> buffer;

  private PropsUI props;

  private String title, message;

  private Rectangle bounds;

  private int hscroll, vscroll;

  private int hmax, vmax;

  private String loggingText;

  private boolean proposingToGetMoreRows;

  private boolean proposingToStop;

  private boolean askingForMoreRows;

  private boolean askingToStop;

  private RowMetaInterface rowMeta;

  private VariableSpace variables;

  private LogChannelInterface log;

  private boolean dynamic;

  private boolean waitingForRows;

  protected int lineNr;

  private int style = SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN;

  private Shell parentShell;


  public PreviewRowsDialog(Shell parent, VariableSpace space, int style, String stepName, RowMetaInterface rowMeta,
      List<Object[]> rowBuffer) {
    this(parent, space, style, stepName, rowMeta, rowBuffer, null);
  }

  public PreviewRowsDialog(Shell parent, VariableSpace space, int style, String stepName, RowMetaInterface rowMeta,
      List<Object[]> rowBuffer, String loggingText) {
    this.stepname = stepName;
    this.buffer = rowBuffer;
    this.loggingText = loggingText;
    this.rowMeta = rowMeta;
    this.variables = space;
    this.parentShell = parent;
    this.style = (style != SWT.None) ? style : this.style;

    props = PropsUI.getInstance();
    bounds = null;
    hscroll = -1;
    vscroll = -1;
    title = null;
    message = null;

    this.log = new LogChannel("Row Preview");
  }

  public void setTitleMessage(String title, String message) {
    this.title = title;
    this.message = message;
  }

  public void open() {
    shell = new Shell(parentShell, style);
    props.setLook(shell);
    shell.setImage(GUIResource.getInstance().getImageSpoon());

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    if (title == null)
      title = BaseMessages.getString(PKG, "PreviewRowsDialog.Title");
    if (message == null)
      message = BaseMessages.getString(PKG, "PreviewRowsDialog.Header", stepname);

    if (buffer != null)
      message += " " + BaseMessages.getString(PKG, "PreviewRowsDialog.NrRows", "" + buffer.size());

    shell.setLayout(formLayout);
    shell.setText(title);

    if (addFields()) {
      return;
    }

    List<Button> buttons = new ArrayList<Button>();

    wClose = new Button(shell, SWT.PUSH);
    wClose.setText(BaseMessages.getString(PKG, "System.Button.Close"));
    wClose.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event e) {
        close();
      }
    });
    buttons.add(wClose);

    if (!Const.isEmpty(loggingText)) {
      wLog = new Button(shell, SWT.PUSH);
      wLog.setText(BaseMessages.getString(PKG, "PreviewRowsDialog.Button.ShowLog"));
      wLog.addListener(SWT.Selection, new Listener() {
        public void handleEvent(Event e) {
          log();
        }
      });
      buttons.add(wLog);
    }

    if (proposingToStop) {
      wStop = new Button(shell, SWT.PUSH);
      wStop.setText(BaseMessages.getString(PKG, "PreviewRowsDialog.Button.Stop.Label"));
      wStop.setToolTipText(BaseMessages.getString(PKG, "PreviewRowsDialog.Button.Stop.ToolTip"));
      wStop.addListener(SWT.Selection, new Listener() {
        public void handleEvent(Event e) {
          askingToStop = true;
          close();
        }
      });
      buttons.add(wStop);
    }

    if (proposingToGetMoreRows) {
      wNext = new Button(shell, SWT.PUSH);
      wNext.setText(BaseMessages.getString(PKG, "PreviewRowsDialog.Button.Next.Label"));
      wNext.setToolTipText(BaseMessages.getString(PKG, "PreviewRowsDialog.Button.Next.ToolTip"));
      wNext.addListener(SWT.Selection, new Listener() {
        public void handleEvent(Event e) {
          askingForMoreRows = true;
          close();
        }
      });
      buttons.add(wNext);
    }

    if (proposingToGetMoreRows || proposingToStop) {
      wClose.setText(BaseMessages.getString(PKG, "PreviewRowsDialog.Button.Close.Label"));
      wClose.setToolTipText(BaseMessages.getString(PKG, "PreviewRowsDialog.Button.Close.ToolTip"));
    }

    // Position the buttons...
    //
    BaseStepDialog.positionBottomButtons(shell, buttons.toArray(new Button[buttons.size()]), Const.MARGIN, null);

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener(new ShellAdapter() {
      public void shellClosed(ShellEvent e) {
        close();
      }
    });

    getData();

    BaseStepDialog.setSize(shell);

    shell.open();
  }

  private boolean addFields() {
    // int middle = props.getMiddlePct();
    int margin = Const.MARGIN;

    if (wlFields == null) {
      wlFields = new Label(shell, SWT.LEFT);
      wlFields.setText(message);
      props.setLook(wlFields);
      fdlFields = new FormData();
      fdlFields.left = new FormAttachment(0, 0);
      fdlFields.right = new FormAttachment(100, 0);
      fdlFields.top = new FormAttachment(0, margin);
      wlFields.setLayoutData(fdlFields);
    } else {
      wFields.dispose();
    }

    if (dynamic && rowMeta == null) {
      rowMeta = new RowMeta();
      rowMeta.addValueMeta(new ValueMeta("<waiting for rows>", ValueMetaInterface.TYPE_STRING));
      waitingForRows = true;
    }
    if (!dynamic) {
      // Mmm, if we don't get any rows in the buffer: show a dialog box.
      if (buffer == null || buffer.size() == 0) {
        ShowMessageDialog dialog = new ShowMessageDialog(shell, SWT.OK | SWT.ICON_WARNING, BaseMessages.getString(PKG,
            "PreviewRowsDialog.NoRows.Text"), BaseMessages.getString(PKG, "PreviewRowsDialog.NoRows.Message"));
        dialog.open();
        shell.dispose();
        return true;
      }
    }

    ColumnInfo[] colinf = new ColumnInfo[rowMeta.size()];
    for (int i = 0; i < rowMeta.size(); i++) {
      ValueMetaInterface v = rowMeta.getValueMeta(i);
      colinf[i] = new ColumnInfo(v.getName(), ColumnInfo.COLUMN_TYPE_TEXT, v.isNumeric());
      colinf[i].setToolTip(v.toStringMeta());
      colinf[i].setValueMeta(v);
    }

    wFields = new TableView(variables, shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf, 0, null, props);

    fdFields = new FormData();
    fdFields.left = new FormAttachment(0, 0);
    fdFields.top = new FormAttachment(wlFields, margin);
    fdFields.right = new FormAttachment(100, 0);
    fdFields.bottom = new FormAttachment(100, -50);
    wFields.setLayoutData(fdFields);

    if (dynamic) {
      shell.layout(true, true);
    }

    return false;
  }

  public void dispose() {
    props.setScreen(new WindowProperty(shell));
    bounds = shell.getBounds();
    hscroll = wFields.getHorizontalBar().getSelection();
    vscroll = wFields.getVerticalBar().getSelection();
    shell.dispose();
  }

  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  private void getData() {
    shell.getDisplay().asyncExec(new Runnable() {
      public void run() {
        int nrErrors = 0;
        lineNr = 0;
        for (int i = 0; i < buffer.size(); i++) {
          TableItem item;
          if (i == 0)
            item = wFields.table.getItem(i);
          else
            item = new TableItem(wFields.table, SWT.NONE);

          Object[] row = (Object[]) buffer.get(i);

          nrErrors += getDataForRow(item, row);
        }
        if (!wFields.isDisposed())
          wFields.optWidth(true, 200);
      }
    });
  }

  protected int getDataForRow(TableItem item, Object[] row) {
    int nrErrors = 0;

    // Display the correct line item...
    //
    String strNr;
    lineNr++;
    try {
      strNr = wFields.getNumberColumn().getValueMeta().getString(new Long(lineNr));
    } catch (Exception e) {
      strNr = Integer.toString(lineNr);
    }
    item.setText(0, strNr);

    for (int c = 0; c < rowMeta.size(); c++) {
      ValueMetaInterface v = rowMeta.getValueMeta(c);
      String show;
      try {
        show = v.getString(row[c]);
        if (v.isBinary() && show != null && show.length() > MAX_BINARY_STRING_PREVIEW_SIZE) {
          // We want to limit the size of the strings during preview to keep all SWT widgets happy.
          //
          show = show.substring(0, MAX_BINARY_STRING_PREVIEW_SIZE);
        }
      } catch (KettleValueException e) {
        nrErrors++;
        if (nrErrors < 25) {
          log.logError(Const.getStackTracker(e));
        }
        show = null;
      } catch (ArrayIndexOutOfBoundsException e) {
        nrErrors++;
        if (nrErrors < 25) {
          log.logError(Const.getStackTracker(e));
        }
        show = null;
      }

      if (show != null) {
        item.setText(c + 1, show);
      }
    }

    return nrErrors;

  }

  private void close() {
    stepname = null;
    dispose();
  }

  /**
   * Show the logging of the preview (in case errors occurred
   */
  private void log() {
    if (loggingText != null) {
      EnterTextDialog etd = new EnterTextDialog(shell, BaseMessages.getString(PKG,
          "PreviewRowsDialog.ShowLogging.Title"), BaseMessages.getString(PKG, "PreviewRowsDialog.ShowLogging.Message"),
          loggingText);
      etd.open();
    }
  };

  public boolean isDisposed() {
    return shell.isDisposed();
  }

  public Rectangle getBounds() {
    return bounds;
  }

  public void setBounds(Rectangle b) {
    bounds = b;
  }

  public int getHScroll() {
    return hscroll;
  }

  public void setHScroll(int s) {
    hscroll = s;
  }

  public int getVScroll() {
    return vscroll;
  }

  public void setVScroll(int s) {
    vscroll = s;
  }

  public int getHMax() {
    return hmax;
  }

  public void setHMax(int m) {
    hmax = m;
  }

  public int getVMax() {
    return vmax;
  }

  public void setVMax(int m) {
    vmax = m;
  }

  /**
   * @return true if the user is asking to grab the next rows with preview
   */
  public boolean isAskingForMoreRows() {
    return askingForMoreRows;
  }

  /**
   * @return true if the dialog is proposing to ask for more rows
   */
  public boolean isProposingToGetMoreRows() {
    return proposingToGetMoreRows;
  }

  /**
   * @param proposingToGetMoreRows Set to true if you want to display a button asking for more preview rows.
   */
  public void setProposingToGetMoreRows(boolean proposingToGetMoreRows) {
    this.proposingToGetMoreRows = proposingToGetMoreRows;
  }

  /**
   * @return the askingToStop
   */
  public boolean isAskingToStop() {
    return askingToStop;
  }

  /**
   * @return the proposingToStop
   */
  public boolean isProposingToStop() {
    return proposingToStop;
  }

  /**
   * @param proposingToStop the proposingToStop to set
   */
  public void setProposingToStop(boolean proposingToStop) {
    this.proposingToStop = proposingToStop;
  }

  public void setDynamic(boolean dynamic) {
    this.dynamic = dynamic;
  }

  public synchronized void addDataRow(final RowMetaInterface rowMeta, final Object[] rowData) {

    if (shell == null || shell.isDisposed())
      return;

    Display.getDefault().syncExec(new Runnable() {

      public void run() {

        if (wFields.isDisposed())
          return;

        if (waitingForRows) {
          PreviewRowsDialog.this.rowMeta = rowMeta;
          addFields();
        }

        TableItem item = new TableItem(wFields.table, SWT.NONE);
        getDataForRow(item, rowData);
        if (waitingForRows) {
          waitingForRows = false;
          wFields.removeEmptyRows();
          PreviewRowsDialog.this.rowMeta = rowMeta;
          if (wFields.table.getItemCount() < 10) {
            wFields.optWidth(true);
          }
        }

        if (wFields.table.getItemCount() > props.getDefaultPreviewSize()) {
          wFields.table.remove(0);
        }

        // wFields.table.setSelection(new TableItem[] { item, });
        wFields.table.setTopIndex(wFields.table.getItemCount() - 1);
      }
    });
  }
}
