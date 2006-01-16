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

 
package be.ibridge.kettle.core.dialog;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Color;
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

import be.ibridge.kettle.core.CheckResult;
import be.ibridge.kettle.core.ColumnInfo;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.WindowProperty;
import be.ibridge.kettle.core.widget.TableView;
import be.ibridge.kettle.trans.step.BaseStepDialog;
import be.ibridge.kettle.trans.step.StepMeta;


/**
 * Dialog to display the results of a verify operation.
 * 
 * @author Matt
 * @since 19-06-2003
 *
 */

public class CheckResultDialog extends Dialog
{
	private ArrayList    remarks;
		
	private Label        wlFields;
	private TableView    wFields;
	private FormData     fdlFields, fdFields;

	private Button wClose, wView, wEdit;
	private Listener lsClose, lsView, lsEdit;

	private Shell    shell;
	private Props    props;
	
	private Color    red, green, yellow;
	
	private String stepname;
	
    /** @deprecated */
    public CheckResultDialog(Shell parent, int style, LogWriter l, Props pr, ArrayList rem)
    {
        this(parent, style, rem);
    }

	public CheckResultDialog(Shell parent, int style, ArrayList rem)
	{
			super(parent, style);
			remarks=rem;
			props=Props.getInstance();
			stepname=null;
	}

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();
		
		red    = display.getSystemColor(SWT.COLOR_RED);
		green  = display.getSystemColor(SWT.COLOR_GREEN);
		yellow = display.getSystemColor(SWT.COLOR_YELLOW);

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX);
 		props.setLook(shell);

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText("Results of transformation checks");
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		wlFields=new Label(shell, SWT.RIGHT);
		wlFields.setText("Remarks: ");
 		props.setLook(wlFields);
		fdlFields=new FormData();
		fdlFields.left = new FormAttachment(0, 0);
		fdlFields.right= new FormAttachment(middle, -margin);
		fdlFields.top  = new FormAttachment(0, margin);
		wlFields.setLayoutData(fdlFields);
		
		int FieldsCols=3;
		int FieldsRows=remarks.size();
		
		ColumnInfo[] colinf=new ColumnInfo[FieldsCols];
		colinf[0]=new ColumnInfo("Stepname", ColumnInfo.COLUMN_TYPE_TEXT,   false, true);
		colinf[1]=new ColumnInfo("Result",   ColumnInfo.COLUMN_TYPE_TEXT,   false, true);
		colinf[2]=new ColumnInfo("Remark",   ColumnInfo.COLUMN_TYPE_TEXT,   false, true);
		
		wFields=new TableView(shell, 
						      SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, 
						      colinf, 
						      FieldsRows,  
							  true, // read-only
						      null,
							  props
						      );
		
		fdFields=new FormData();
		fdFields.left   = new FormAttachment(0, 0);
		fdFields.top    = new FormAttachment(wlFields, margin);
		fdFields.right  = new FormAttachment(100, 0);
		fdFields.bottom = new FormAttachment(100, -50);
		wFields.setLayoutData(fdFields);

		wClose=new Button(shell, SWT.PUSH);
		wClose.setText(" &Close ");

		wView=new Button(shell, SWT.PUSH);
		wView.setText(" &View message");

		wEdit=new Button(shell, SWT.PUSH);
		wEdit.setText(" &Edit origin step");

		BaseStepDialog.positionBottomButtons(shell, new Button[] { wClose, wView, wEdit  }, margin, null);

		// Add listeners
		lsClose = new Listener() { public void handleEvent(Event e) { close(); } };
		lsView  = new Listener() { public void handleEvent(Event e) { view(); } };
		lsEdit  = new Listener() { public void handleEvent(Event e) { edit(); } };

		wClose.addListener(SWT.Selection, lsClose    );
		wView .addListener(SWT.Selection, lsView     );
		wEdit .addListener(SWT.Selection, lsEdit     );
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { close(); } } );


		getData();
		
		WindowProperty winprop = props.getScreen(shell.getText());
		if (winprop!=null) winprop.setShell(shell); else shell.pack();
		
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
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
		for (int i=0;i<remarks.size();i++)
		{
			CheckResult cr = (CheckResult)remarks.get(i);
			TableItem ti = wFields.table.getItem(i); 

			StepMeta stepMeta = cr.getStepInfo();
			if (stepMeta!=null) ti.setText(1, stepMeta.getName());
			else          ti.setText(1, "<global>");
			ti.setText(2, cr.getType()+" - "+cr.getTypeDesc());
			ti.setText(3, cr.getText());

			Color col = ti.getBackground();
			switch(cr.getType())
			{
				case CheckResult.TYPE_RESULT_OK:      col=green;  break;
				case CheckResult.TYPE_RESULT_ERROR:   col=red;    break;
				case CheckResult.TYPE_RESULT_WARNING: col=yellow; break;
				case CheckResult.TYPE_RESULT_COMMENT: 
				default:break;
			}
			ti.setBackground(col);
		}
		wFields.setRowNums();
		wFields.optWidth(true);
	}
	
	// View message:
	private void view()
	{
		String message="";
		
		TableItem item[] = wFields.table.getSelection();
		
		// None selected: don't waste users time: select them all!
		if (item.length==0) item=wFields.table.getItems();
		
		for (int i=0;i<item.length;i++)
		{
			if (i>0) message+="_______________________________________________________________________________"+Const.CR+Const.CR;
			message+="["+item[i].getText(2)+"] "+item[i].getText(1)+Const.CR;
			message+="  "+item[i].getText(3)+Const.CR+Const.CR;
		}
		
		EnterTextDialog etd = new EnterTextDialog(shell, "View message", "Message"+(item.length!=1?"s":"")+" : ", message);
		etd.setReadOnly();
		etd.open();
	}
	
	private void edit()
	{
		int idx=wFields.table.getSelectionIndex();
		if (idx>=0)
		{
			stepname = wFields.table.getItem(idx).getText(1);
			dispose();
		}	
	}
	
	private void close()
	{
		dispose();
	}
}
