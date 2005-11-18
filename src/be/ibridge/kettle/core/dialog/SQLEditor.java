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
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
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
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.DBCache;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.WindowProperty;
import be.ibridge.kettle.core.database.Database;
import be.ibridge.kettle.core.database.DatabaseMeta;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.trans.step.BaseStepDialog;


/**
 * Dialog that allows the user to launch SQL statements towards the database.
 * 
 * @author Matt
 * @since 13-10-2003
 * 
 */
public class SQLEditor extends Dialog
{
	private LogWriter    log;
	private Props        props;
		
	private Label        wlScript;
	private Text         wScript;
	private FormData     fdlScript, fdScript;

	private Label        wlPosition;
	private FormData     fdlPosition;

	private Button wExec, wCancel;
	private Listener lsExec, lsCancel;

	private String           input;
	private DatabaseMeta     connection;
	private Shell            shell;
	private DBCache          dbcache;
	
    /** @deprecated */
    public SQLEditor(Shell parent, Props pr, int style, LogWriter l, DatabaseMeta ci, DBCache dbc, String sql)
    {
        this(parent, style, ci, dbc, sql);
    }
    
	public SQLEditor(Shell parent, int style, DatabaseMeta ci, DBCache dbc, String sql)
	{
			super(parent, style);
			props=Props.getInstance();
			log=LogWriter.getInstance();
			input=sql;
			connection=ci;
			dbcache=dbc;
	}

	public void open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
 		props.setLook(shell);

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText("Simple SQL Editor");
		
		int margin = Const.MARGIN;

		// Script line
		wlScript=new Label(shell, SWT.NONE);
		wlScript.setText("SQL statements, separated by ; ");
 		props.setLook(wlScript);

		fdlScript=new FormData();
		fdlScript.left = new FormAttachment(0, 0);
		fdlScript.top  = new FormAttachment(0, 0);
		wlScript.setLayoutData(fdlScript);
		wScript=new Text(shell, SWT.MULTI | SWT.LEFT | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		wScript.setText("");
 		props.setLook(wScript, Props.WIDGET_STYLE_FIXED);
		fdScript=new FormData();
		fdScript.left   = new FormAttachment(0, 0);
		fdScript.top    = new FormAttachment(wlScript, margin);
		fdScript.right  = new FormAttachment(100, -5);
		fdScript.bottom = new FormAttachment(100, -70);
		wScript.setLayoutData(fdScript);

		wlPosition=new Label(shell, SWT.NONE);
		wlPosition.setText("Linenr: 0        ");
 		props.setLook(wlPosition);
		fdlPosition=new FormData();
		fdlPosition.left = new FormAttachment(0, 0);
		fdlPosition.top  = new FormAttachment(wScript, margin);
		wlPosition.setLayoutData(fdlPosition);

		wExec=new Button(shell, SWT.PUSH);
		wExec.setText(" &Execute ");
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(" &Close ");

		BaseStepDialog.positionBottomButtons(shell, new Button[] { wExec, wCancel }, margin, null);
		
		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsExec       = new Listener() { public void handleEvent(Event e) { try { exec(); } catch(Exception ge) {} } };
		
		wCancel.addListener  (SWT.Selection, lsCancel);
		wExec.addListener    (SWT.Selection, lsExec    );
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );


		wScript.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) 
			{
				int linenr = wScript.getCaretLineNumber()+1;
				wlPosition.setText("Linenr: "+linenr+"   ");
			}
		})
		;
		
		WindowProperty winprop = props.getScreen(shell.getText());
		if (winprop!=null) winprop.setShell(shell); else shell.pack();
		
		getData();
		
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
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
		if (input     != null) wScript.setText( input );
		//if (connection!= null) wConnection.setText( connection );
	}
	
	private void cancel()
	{
		dispose();
	}
	
	private void exec()
	{
		DatabaseMeta ci = connection;
		if (ci==null) return;
		
		Database db = new Database(ci);
		try
		{
			db.connect();
			
			// Multiple statements have to be split into parts
			// We use the ";" to separate statements...
			String all = wScript.getText()+Const.CR;
			int from=0;
			int to=0;
			int length = all.length();
			int nrstats = 0;
			
			while (to<length)
			{
				char c = all.charAt(to);
				if (c=='"')
				{
					to++;
					c=' ';
					while (to<length && c!='"') { c=all.charAt(to); to++; }
				}
				else
				if (c=='\'') // skip until next '
				{
					to++;
					c=' ';
					while (to<length && c!='\'') { c=all.charAt(to); to++; }
				}
				if (c==';' || to>=length-1) // end of statement
				{
					if (to>=length-1) to++; // grab last char also!
					
					String stat = all.substring(from, to);
					if (!onlySpaces(stat))
					{
						String sql=Const.trim(stat);
						if (sql.toUpperCase().startsWith("SELECT"))
						{
							// A Query
							log.logDetailed(toString(), "launch SELECT statement: "+Const.CR+sql);
							
							nrstats++;
							try
							{
								ArrayList rows = db.getRows(sql, 1000);
								if (rows.size()>0)
								{
									PreviewRowsDialog prd = new PreviewRowsDialog(shell, SWT.NONE, "SQL statement #"+nrstats, rows);
									prd.open();
								}
								else
								{
									MessageBox mb = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
									mb.setMessage("Couldn't find any rows for statement #"+nrstats+Const.CR+Const.CR+sql);
									mb.setText("Sorry");
									mb.open();
								}
							}
							catch(KettleDatabaseException dbe)
							{
								new ErrorDialog(shell, props, "Error", "I encountered an error executing statement #"+nrstats, dbe);
							}
						}
						else
						{
							log.logDetailed(toString(), "launch DDL statement: "+Const.CR+sql);

							// A DDL statement
							nrstats++;
							try
							{
							    log.logDetailed(toString(), "Executing SQL: "+Const.CR+sql);
								db.execStatement(sql);
								// Clear the database cache, in case we're using one...
								if (dbcache!=null) dbcache.clear(ci.getName());
							}
							catch(Exception dbe)
							{
								new ErrorDialog(shell, props, "Error", "Error executing SQL statement nr "+nrstats+" :", dbe);
							}
						}
					}
					to++;
					from=to;
				}
				else
				{
					to++;
				}
			}
			
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION );
			mb.setMessage(nrstats+" statement"+(nrstats==1?"":"s")+" executed");
			mb.setText("OK");
			mb.open(); 
		}
		catch(KettleDatabaseException dbe)
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage("Unable to connect to the database!"+Const.CR+"Please check the connection setting for connection["+(connection==null?"":connection.getName())+"]"+Const.CR+dbe.getMessage());
			mb.setText("ERROR");
			mb.open(); 
		}
		finally
		{
			db.disconnect();
		}
	}
	
	public static final boolean onlySpaces(String str)
	{
		for (int i=0;i<str.length();i++)
		{
			int c = str.charAt(i);
			if (c!=' ' && c!='\t' && c!='\n' && c!='\r') 
			{
				return false;
			} 
		}
		return true;
	}
	
	public String toString()
	{
		return this.getClass().getName();
	}
}
