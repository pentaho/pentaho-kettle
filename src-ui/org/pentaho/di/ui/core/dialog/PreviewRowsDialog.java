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
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.pentaho.di.core.Const;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.ShowMessageDialog;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.dialog.Messages;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;


/**
 * Displays an ArrayList of rows in a TableView.
 * 
 * @author Matt
 * @since 19-06-2003
 */
public class PreviewRowsDialog extends Dialog
{
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

    public PreviewRowsDialog(Shell parent, VariableSpace space, int style, String stepName, RowMetaInterface rowMeta, List<Object[]> rowBuffer)
    {
        this(parent, space, style, stepName, rowMeta, rowBuffer, null);
    }

    public PreviewRowsDialog(Shell parent, VariableSpace space, int style, String stepName, RowMetaInterface rowMeta, List<Object[]> rowBuffer, String loggingText)
    {
        super(parent, style);
        this.stepname = stepName;
        this.buffer = rowBuffer;
        this.loggingText = loggingText;
        this.rowMeta = rowMeta;
        this.variables = space;

        props = PropsUI.getInstance();
        bounds = null;
        hscroll = -1;
        vscroll = -1;
        title = null;
        message = null;
    }

    public void setTitleMessage(String title, String message)
    {
        this.title = title;
        this.message = message;
    }

    public void open()
    {
        Shell parent = getParent();
        Display display = parent.getDisplay();

        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX);
        props.setLook(shell);
		shell.setImage(GUIResource.getInstance().getImageConnection());

        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;

        if (title == null)
            title = Messages.getString("PreviewRowsDialog.Title");
        if (message == null)
            message = Messages.getString("PreviewRowsDialog.Header", stepname);

        shell.setLayout(formLayout);
        shell.setText(title);

        // int middle = props.getMiddlePct();
        int margin = Const.MARGIN;

        wlFields = new Label(shell, SWT.LEFT);
        wlFields.setText(message);
        props.setLook(wlFields);
        fdlFields = new FormData();
        fdlFields.left = new FormAttachment(0, 0);
        fdlFields.right = new FormAttachment(100, 0);
        fdlFields.top = new FormAttachment(0, margin);
        wlFields.setLayoutData(fdlFields);

        // Mmm, if we don't get any rows in the buffer: show a dialog box.
        if (buffer == null || buffer.size() == 0)
        {
            ShowMessageDialog dialog = new ShowMessageDialog(shell, SWT.OK | SWT.ICON_WARNING, Messages.getString("PreviewRowsDialog.NoRows.Text"), Messages.getString("PreviewRowsDialog.NoRows.Message"));
            dialog.open();
            shell.dispose();
            return;
        }

        ColumnInfo[] colinf = new ColumnInfo[rowMeta.size()];
        for (int i = 0; i < rowMeta.size(); i++)
        {
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
        
        List<Button> buttons = new ArrayList<Button>();
        
        wClose = new Button(shell, SWT.PUSH);
        wClose.setText(Messages.getString("System.Button.Close"));
        wClose.addListener(SWT.Selection, new Listener() { public void handleEvent(Event e) { close(); } });
        buttons.add(wClose);
        
        if (!Const.isEmpty(loggingText)) {
	        wLog = new Button(shell, SWT.PUSH);
	        wLog.setText(Messages.getString("PreviewRowsDialog.Button.ShowLog"));
	        wLog.addListener(SWT.Selection, new Listener() { public void handleEvent(Event e) { log(); } });
	        buttons.add(wLog);
        }

        if (proposingToStop) {
	        wStop = new Button(shell, SWT.PUSH);
	        wStop.setText(Messages.getString("PreviewRowsDialog.Button.Stop.Label"));
	        wStop.setToolTipText(Messages.getString("PreviewRowsDialog.Button.Stop.ToolTip"));
	        wStop.addListener(SWT.Selection, new Listener() { public void handleEvent(Event e) { askingToStop=true; close(); } });
	        buttons.add(wStop);
        }

        if (proposingToGetMoreRows) {
	        wNext = new Button(shell, SWT.PUSH);
	        wNext.setText(Messages.getString("PreviewRowsDialog.Button.Next.Label"));
	        wNext.setToolTipText(Messages.getString("PreviewRowsDialog.Button.Next.ToolTip"));
	        wNext.addListener(SWT.Selection, new Listener() { public void handleEvent(Event e) { askingForMoreRows=true; close(); } });
	        buttons.add(wNext);
        }
        
        if (proposingToGetMoreRows || proposingToStop) {
	        wClose.setText(Messages.getString("PreviewRowsDialog.Button.Close.Label"));
	        wClose.setToolTipText(Messages.getString("PreviewRowsDialog.Button.Close.ToolTip"));
        }

        // Position the buttons...
        //
        BaseStepDialog.positionBottomButtons(shell, buttons.toArray(new Button[buttons.size()]), margin, null);

        // Detect X or ALT-F4 or something that kills this window...
        shell.addShellListener( new ShellAdapter() { public void shellClosed(ShellEvent e) { close(); } } );

        getData();

        BaseStepDialog.setSize(shell);

        shell.open();

        while (!shell.isDisposed())
        {
            if (!display.readAndDispatch()) display.sleep();
        }
    }

    public void dispose()
    {
        props.setScreen(new WindowProperty(shell));
        bounds = shell.getBounds();
        hscroll = wFields.getHorizontalBar().getSelection();
        vscroll = wFields.getVerticalBar().getSelection();
        shell.dispose();
    }

    /**
     * Copy information from the meta-data input to the dialog fields.
     */
    private void getData()
    {
        shell.getDisplay().asyncExec(new Runnable()
        {
            public void run()
            {
            	int nrErrors = 0;
                for (int i = 0; i < buffer.size(); i++)
                {
                    TableItem item;
                    if (i==0) item = wFields.table.getItem(i);
                    else item = new TableItem(wFields.table, SWT.NONE);
                    
                    // Display the correct line item...
                    //
                    String strNr;
                    try {
						strNr = wFields.getNumberColumn().getValueMeta().getString(new Long(i+1));
					} catch (Exception e) {
						strNr = Integer.toString(i+1);
					}
					item.setText(0, strNr);
                    
                    Object[] row = (Object[]) buffer.get(i);

                    for (int c = 0; c < rowMeta.size(); c++)
                    {
                        ValueMetaInterface v = rowMeta.getValueMeta(c);
                        String show;
                        try
                        {
                            show = v.getString(row[c]);
                            if (v.isBinary() && show!=null && show.length()>MAX_BINARY_STRING_PREVIEW_SIZE)
                            {
                            	// We want to limit the size of the strings during preview to keep all SWT widgets happy.
                            	//
                            	show = show.substring(0, MAX_BINARY_STRING_PREVIEW_SIZE);
                            }
                        }
                        catch (KettleValueException e)
                        {
                        	nrErrors++;
                        	if (nrErrors<25)
                        	{
	                            LogWriter.getInstance().logError(toString(), Const.getStackTracker(e));
                        	}
                            show=null;
                        }
                        catch (ArrayIndexOutOfBoundsException e)
                        {
                        	nrErrors++;
                        	if (nrErrors<25)
                        	{
                        		LogWriter.getInstance().logError(toString(), Const.getStackTracker(e));
                        	}
                            show=null;
                        }

                        if (show != null)
                        {
                            item.setText(c + 1, show);
                        }
                    }
                }
                if (!wFields.isDisposed()) wFields.optWidth(true, 200);
            }
        });
    }

    private void close()
    {
        stepname = null;
        dispose();
    }

    /**
     * Show the logging of the preview (in case errors occurred
     */
    private void log()
    {
        if (loggingText != null)
        {
            EnterTextDialog etd = new EnterTextDialog(shell, Messages.getString("PreviewRowsDialog.ShowLogging.Title"), Messages.getString("PreviewRowsDialog.ShowLogging.Message"), loggingText);
            etd.open();
        }
    };

    public boolean isDisposed()
    {
        return shell.isDisposed();
    }

    public Rectangle getBounds()
    {
        return bounds;
    }

    public void setBounds(Rectangle b)
    {
        bounds = b;
    }

    public int getHScroll()
    {
        return hscroll;
    }

    public void setHScroll(int s)
    {
        hscroll = s;
    }

    public int getVScroll()
    {
        return vscroll;
    }

    public void setVScroll(int s)
    {
        vscroll = s;
    }

    public int getHMax()
    {
        return hmax;
    }

    public void setHMax(int m)
    {
        hmax = m;
    }

    public int getVMax()
    {
        return vmax;
    }

    public void setVMax(int m)
    {
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
}
