 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/

 
/*
 * Created on 19-jun-2003
 *
 */

package be.ibridge.kettle.core.dialog;
import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;

import be.ibridge.kettle.core.ColumnInfo;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.WindowProperty;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.core.widget.TableView;
import be.ibridge.kettle.trans.step.BaseStepDialog;


/**
 * Shows a dialog that allows you to enter values for a number of strings.
 *  
 * @author Matt
 *
 */
public class EnterStringsDialog extends Dialog
{
    private Label        wlFields;
    private TableView    wFields;
    private FormData     fdlFields, fdFields;
		
	private Button   wOK, wCancel;
	private Listener lsOK, lsCancel;

	private Shell         shell;
	private Row           strings;
	private Props 		  props;

    /**
     * Constructs a new dialog
     * @param parent The parent shell to link to
     * @param style The style in which we want to draw this shell.
     * @param strings The list of rows to change.
     */
	public EnterStringsDialog(Shell parent, int style, Row strings)
	{
			super(parent, style);
			this.strings=strings;
			props=Props.getInstance();
	}

	public Row open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX);
 		props.setLook(shell);

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText("Enter string values");
		
		int margin = Const.MARGIN;

		// Filename line
		wlFields=new Label(shell, SWT.NONE);
		wlFields.setText("Enter values for the Strings specified below : ");
 		props.setLook(wlFields);
		fdlFields=new FormData();
		fdlFields.left = new FormAttachment(0, 0);
		fdlFields.top  = new FormAttachment(0, margin);
		wlFields.setLayoutData(fdlFields);

        int FieldsRows=strings.size();
        
        ColumnInfo[] colinf=new ColumnInfo[]
            {
                new ColumnInfo("String name",  ColumnInfo.COLUMN_TYPE_TEXT, false, true),
                new ColumnInfo("String value", ColumnInfo.COLUMN_TYPE_TEXT, false, false)                
            };
        
        wFields=new TableView(shell, 
                              SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, 
                              colinf, 
                              FieldsRows,  
                              null,
                              props
                              );

        fdFields=new FormData();
        fdFields.left   = new FormAttachment(0, 0);
        fdFields.top    = new FormAttachment(wlFields, 30);
        fdFields.right  = new FormAttachment(100, 0);
        fdFields.bottom = new FormAttachment(100, -50);
        wFields.setLayoutData(fdFields);
        
        
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(" &OK ");

		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(" &Cancel ");

        BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, wFields);

		// Add listeners
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };

		wOK.addListener (SWT.Selection, lsOK    );
		wCancel.addListener(SWT.Selection, lsCancel    );
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		getData();

		WindowProperty winprop = props.getScreen(shell.getText());
		if (winprop!=null) winprop.setShell(shell); else shell.pack();
		
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return strings;
	}

	public void dispose()
	{
		props.setScreen(new WindowProperty(shell));
		shell.dispose();
	}
	
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
        if (strings!=null)
        {
            for (int i=0;i<strings.size();i++)
            {
                Value value = strings.getValue(i);
                TableItem item = wFields.table.getItem(i);
                item.setText(1, value.getName());
                if (value.getString()!=null && !value.isNull()) item.setText(2, value.getString());
            }
        }
        wFields.sortTable(1);
        wFields.setRowNums();
        wFields.optWidth(true);
	}

	private void cancel()
	{
        strings=null;
		dispose();
	}
	
	private void ok()
	{
        // Loop over the input rows and find the new values...
        for (int i=0;i<wFields.nrNonEmpty();i++)
        {
            TableItem item = wFields.getNonEmpty(i);
            String name = item.getText(1);
            for (int j=0;j<strings.size();j++)
            {
                Value value = strings.getValue(j);
                if (value.getName().equalsIgnoreCase(name))
                {
                    String stringValue = item.getText(2);
                    value.setValue(stringValue);
                }
            }
        }
        dispose();
	}
}
