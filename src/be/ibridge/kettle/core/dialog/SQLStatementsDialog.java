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
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;

import be.ibridge.kettle.core.ColumnInfo;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.SQLStatement;
import be.ibridge.kettle.core.WindowProperty;
import be.ibridge.kettle.core.database.Database;
import be.ibridge.kettle.core.database.DatabaseMeta;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.widget.TableView;


/**
 * Dialog to display the results of an SQL generation operation.
 * 
 * @author Matt
 * @since 19-06-2003
 *
 */

public class SQLStatementsDialog extends Dialog
{
	private ArrayList    stats;
		
	private TableView    wFields;
	private FormData     fdFields;

	private Button wClose, wView, wEdit, wExec;
	private FormData fdClose, fdView, fdEdit, fdExec;
	private Listener lsClose, lsView, lsEdit, lsExec;

	private Shell    shell;
	private Props    props;
	
	private Color    red;
	
	private String stepname;
	
    /**
     * @deprecated
     */
	public SQLStatementsDialog(Shell parent, int style, LogWriter log, Props props, ArrayList stats)
	{
			super(parent, style);
			this.stats=stats;
			this.props=props;
			
			this.stepname = null;
	}
    
    public SQLStatementsDialog(Shell parent, int style, ArrayList stats)
    {
            super(parent, style);
            this.stats=stats;
            this.props=Props.getInstance();
            
            this.stepname = null;
    }

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();
		
		red    = display.getSystemColor(SWT.COLOR_RED);

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX);
 		props.setLook(shell);

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText("List of SQL statements to execute");
		
		int margin = Const.MARGIN;
		
		int FieldsCols=4;
		int FieldsRows=stats.size();
		
		ColumnInfo[] colinf=new ColumnInfo[FieldsCols];
		colinf[0]=new ColumnInfo("Stepname",     ColumnInfo.COLUMN_TYPE_TEXT,   false, true);
		colinf[1]=new ColumnInfo("Connection",   ColumnInfo.COLUMN_TYPE_TEXT,   false, true);
		colinf[2]=new ColumnInfo("SQL",          ColumnInfo.COLUMN_TYPE_TEXT,   false, true);
		colinf[3]=new ColumnInfo("Error",        ColumnInfo.COLUMN_TYPE_TEXT,   false, true);
		
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
		fdFields.top    = new FormAttachment(0, 0);
		fdFields.right  = new FormAttachment(100, 0);
		fdFields.bottom = new FormAttachment(100, -50);
		wFields.setLayoutData(fdFields);

		wClose=new Button(shell, SWT.PUSH);
		wClose.setText(" &Close ");
		fdClose=new FormData();
		fdClose.left   =new FormAttachment(25, 0);
		fdClose.bottom =new FormAttachment(100, 0);
		wClose.setLayoutData(fdClose);

		wView=new Button(shell, SWT.PUSH);
		wView.setText(" &View SQL");
		fdView=new FormData();
		fdView.left=new FormAttachment(wClose, margin);
		fdView.bottom =new FormAttachment(100, 0);
		wView.setLayoutData(fdView);

		wExec=new Button(shell, SWT.PUSH);
		wExec.setText(" E&xecute SQL");
		fdExec=new FormData();
		fdExec.left=new FormAttachment(wView, margin);
		fdExec.bottom =new FormAttachment(100, 0);
		wExec.setLayoutData(fdExec);

		wEdit=new Button(shell, SWT.PUSH);
		wEdit.setText(" &Edit origin step");
		fdEdit=new FormData();
		fdEdit.left=new FormAttachment(wExec, margin);
		fdEdit.bottom =new FormAttachment(100, 0);
		wEdit.setLayoutData(fdEdit);

		// Add listeners
		lsClose = new Listener() { public void handleEvent(Event e) { close(); } };
		lsView  = new Listener() { public void handleEvent(Event e) { view(); } };
		lsExec  = new Listener() { public void handleEvent(Event e) { exec(); } };
		lsEdit  = new Listener() { public void handleEvent(Event e) { edit(); } };

		wClose.addListener(SWT.Selection, lsClose    );
		wView .addListener(SWT.Selection, lsView     );
		wExec .addListener(SWT.Selection, lsExec     );
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
		for (int i=0;i<stats.size();i++)
		{
			SQLStatement stat = (SQLStatement)stats.get(i);
			TableItem ti = wFields.table.getItem(i); 

			String name         = stat.getStepname();
			DatabaseMeta dbinfo = stat.getDatabase();
			String sql          = stat.getSQL();
			String error        = stat.getError();
			
			if (name!=null)   ti.setText(1, name);
			if (dbinfo!=null) ti.setText(2, dbinfo.getName() );
			if (sql!=null)    ti.setText(3, sql);
			if (error!=null)  ti.setText(4, error);

			Color col = ti.getBackground();
			if (stat.hasError()) col=red;
			ti.setBackground(col);
		}
		wFields.setRowNums();
		wFields.optWidth(true);
	}
	
	private String getSQL()
	{
		String sql="";
		
		int idx[] = wFields.table.getSelectionIndices();
		
		// None selected: don't waste users time: select them all!
		if (idx.length==0) 
		{
			idx=new int[stats.size()];
			for (int i=0;i<stats.size();i++) idx[i]=i;
		}
		
		for (int i=0;i<idx.length;i++)
		{
			SQLStatement stat = (SQLStatement)stats.get(idx[i]);
			DatabaseMeta di = stat.getDatabase();
			if (i>0) sql+="-------------------------------------------------------------------------------------------"+Const.CR;
			sql+="-- Step                : "+stat.getStepname()+Const.CR;
			sql+="-- Database Connection : "+(di!=null?di.getName():"<not defined>")+Const.CR;
			if (stat.hasSQL())
			{
				sql+="-- SQL                 : "+Const.CR+Const.CR;
				sql+=stat.getSQL()+Const.CR;
			}
			if (stat.hasError())
			{
				sql+="-- Error message       : "+stat.getError()+Const.CR;
			}
		}

		return sql;
	}
	
	// View SQL statement:
	private void view()
	{
		String sql = getSQL();
		EnterTextDialog etd = new EnterTextDialog(shell, "View SQL statements", "Statements:", sql, true);
		etd.setReadOnly();
		etd.open();
	}
	
	private void exec()
	{
		int idx[] = wFields.table.getSelectionIndices();
		
		// None selected: don't waste users time: select them all!
		if (idx.length==0) 
		{
			idx=new int[stats.size()];
			for (int i=0;i<stats.size();i++) idx[i]=i;
		}
		
		int errors = 0;
		for (int i=0;i<idx.length;i++)
		{
			SQLStatement stat = (SQLStatement)stats.get(idx[i]);
			if (stat.hasError()) errors++;
		}
		
		if (errors==0)
		{
			for (int i=0;i<idx.length;i++)
			{
				SQLStatement stat = (SQLStatement)stats.get(idx[i]);
				DatabaseMeta di = stat.getDatabase();
				if (di!=null && !stat.hasError())
				{
					Database db = new Database(di);
					try
					{
						db.connect();
						try
						{
							db.execStatements(stat.getSQL());
						}
						catch(KettleDatabaseException dbe)
						{
							errors++;
							new ErrorDialog(shell, props, "Error", "The following statement could not be executed: "+Const.CR+stat.getSQL(), dbe);
						}
					}
					catch(KettleDatabaseException dbe)
					{
						new ErrorDialog(shell, props, "Error", "I was unable to connect to database connection ["+(di==null?"":di.getName())+"]", dbe);
					}
					finally
					{
						db.disconnect();
					}
				}
			}
			if (errors==0)
			{
				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION );
				mb.setMessage("All "+idx.length+" SQL statements were executed.");
				mb.setText("OK!");
				mb.open();
			}
		}
		else
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage("We can't execute the selected statetements as there are "+errors+" with errors.");
			mb.setText("ERROR");
			mb.open();
		}
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
