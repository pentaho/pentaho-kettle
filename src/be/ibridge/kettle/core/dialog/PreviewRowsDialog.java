 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** It belongs to, is maintained by and is copyright 1999-2005 by     **
 **                                                                   **
 **      i-Bridge bvba                                                **
 **      Fonteinstraat 70                                             **
 **      9400 OKEGEM                                                  **
 **      Belgium                                                      **
 **      http://www.kettle.be                                         **
 **      info@kettle.be                                               **
 **                                                                   **
 **********************************************************************/
 

package be.ibridge.kettle.core.dialog;

import java.util.ArrayList;

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

import be.ibridge.kettle.core.ColumnInfo;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.WindowProperty;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.core.widget.TableView;


/**
 * Displays an ArrayList of rows in a TableView.
 * 
 * @author Matt
 * @since 19-06-2003
 */
public class PreviewRowsDialog extends Dialog
{
	private String       stepname;
		
	private Label        wlFields;
	private TableView    wFields;
	private FormData     fdlFields, fdFields;

	private Button wClose;
	private FormData fdClose;
	private Listener lsClose;

	private Shell         shell;
	private ArrayList     buffer;
	private Props         props;
	private String        title, message;
	
	private Rectangle     bounds;
	private int           hscroll, vscroll;
	private int           hmax, vmax;
	
    /** @deprecated */
    public PreviewRowsDialog(Shell parent, int style, LogWriter l, Props pr, String nam, ArrayList buf)
    {
        this(parent, style, nam, buf);
    }

	public PreviewRowsDialog(Shell parent, int style, String nam, ArrayList buf)
	{
		super(parent, style);
		stepname=nam;
		buffer=buf;
		props=Props.getInstance();
		bounds=null;
		hscroll=-1;
		vscroll=-1;
		title=null;
		message=null;
	}
	
	public void setTitleMessage(String title, String message)
	{
		this.title=title;
		this.message=message;
	}

	public Object open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX);
 		props.setLook(shell);

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		if (title==null)   title="Examine preview data";
		if (message==null) message="Rows of step: "+stepname;

		shell.setLayout(formLayout);
		shell.setText(title);
		
		// int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		wlFields=new Label(shell, SWT.LEFT);
		wlFields.setText(message);
 		props.setLook(wlFields);
		fdlFields=new FormData();
		fdlFields.left = new FormAttachment(0, 0);
		fdlFields.right= new FormAttachment(100, 0);
		fdlFields.top  = new FormAttachment(0, margin);
		wlFields.setLayoutData(fdlFields);
		
		Row row = (Row)buffer.get(0);
		
		int FieldsRows=buffer.size();
		
		ColumnInfo[] colinf=new ColumnInfo[row.size()];
		for (int i=0;i<row.size();i++)
		{
			Value v=row.getValue(i);
			colinf[i]=new ColumnInfo(v.getName(),  ColumnInfo.COLUMN_TYPE_TEXT,   "", false);
			colinf[i].setToolTip(v.toStringMeta());
		}
		
		wFields=new TableView(shell, 
						      SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, 
						      colinf, 
						      FieldsRows,  
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
		fdClose=new FormData();
		fdClose.left=new FormAttachment(wFields, 0, SWT.CENTER);
		fdClose.top =new FormAttachment(wFields, 30);
		wClose.setLayoutData(fdClose);

		// Add listeners
		lsClose = new Listener() { public void handleEvent(Event e) { close(); } };
		wClose.addListener(SWT.Selection, lsClose    );
		
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
		int i, c;
		
		for (i=0;i<buffer.size();i++)
		{
			Row row = (Row)buffer.get(i);
			
			for (c=0;c<row.size();c++)
			{
				Value v=row.getValue(c);
				String show;
				if (v.isNumeric()) show = v.toString(true);
				else               show = v.toString(false);
				if (show!=null) wFields.table.getItem(i).setText(c+1, show);
			}
		}
		wFields.optWidth(true);
	}
	
	private void close()
	{
		stepname=null;
		dispose();
	}
	
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
		hscroll=s;
	}

	public int getVScroll()
	{
		return vscroll;
	}

	public void setVScroll(int s)
	{
		vscroll=s;
	}

	public int getHMax()
	{
		return hmax;
	}
	
	public void setHMax(int m)
	{
		hmax=m;
	}

	public int getVMax()
	{
		return vmax;
	}

	public void setVMax(int m)
	{
		vmax=m;
	}
}
