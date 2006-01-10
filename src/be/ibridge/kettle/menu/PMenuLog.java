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
 * Created on 17-mei-2003
 *
 */

package be.ibridge.kettle.menu;
import java.io.FileInputStream;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.GUIResource;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.schema.SchemaMeta;
import be.ibridge.kettle.spoon.dialog.LogSettingsDialog;

//import org.eclipse.swt.dnd.*;

public class PMenuLog extends Composite
{
	public final static String START_TEXT = "&Start transformation"; 
	public final static String STOP_TEXT  = "&Stop transformation"; 

	private Color white;
	private SchemaMeta schema;
	private Shell shell;
	private Display display;
	private LogWriter log;
	
	private Text   wText;
	private Button wRefresh;
	private Button wClear;
	private Button wLog;

	private FormData fdText, fdRefresh, fdClear, fdLog; 
	
	private SelectionListener lsRefresh, lsClear, lsLog;
	private StringBuffer message;


	private FileInputStream in;

    /** @deprecated */
    public PMenuLog(Composite parent, int style, LogWriter l, SchemaMeta sch, String fname)
    {
        this(parent, style, sch, fname);
    }

	public PMenuLog(Composite parent, int style, SchemaMeta sch, String fname)
	{
		super(parent, style);
		shell=parent.getShell();
		schema=sch;
		log=LogWriter.getInstance();
		display=shell.getDisplay();
		
		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;
		
		setLayout(formLayout);
		
		setVisible(true);
		white = GUIResource.getInstance().getColorBackground();

		wText = new Text(this, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY );
		wText.setBackground(white);
		wText.setVisible(true);

		fdText=new FormData();
		fdText.left   = new FormAttachment(0, 0);
		fdText.top    = new FormAttachment(0, 0);
		fdText.right  = new FormAttachment(100, 0);
		fdText.bottom = new FormAttachment(100,-40);
		wText.setLayoutData(fdText);
		
		wRefresh = new Button(this, SWT.PUSH);
		wRefresh.setText("&Refresh log");

		wClear = new Button(this, SWT.PUSH);
		wClear.setText("&Clear log");

		wLog = new Button(this, SWT.PUSH);
		wLog.setText("&Log settings");

		fdRefresh  = new FormData(); 
		fdClear    = new FormData(); 
		fdLog      = new FormData(); 

		fdRefresh.left   = new FormAttachment(25, 10);  
		fdRefresh.bottom = new FormAttachment(100, 0);
		wRefresh.setLayoutData(fdRefresh);

		fdClear.left   = new FormAttachment(wRefresh, 10);  
		fdClear.bottom = new FormAttachment(100, 0);
		wClear.setLayoutData(fdClear);

		fdLog.left   = new FormAttachment(wClear, 10);  
		fdLog.bottom = new FormAttachment(100, 0);
		wLog.setLayoutData(fdLog);

		pack();

		try
		{
			in = log.getFileInputStream();
		}
		catch(Exception e)
		{
			System.out.println("Couldn't create input-pipe connection to output-pipe!");
		}
		
		lsRefresh = new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e) 
			{
				readLog();
			}
		};
		
		final Timer tim = new Timer();
		TimerTask timtask = 
			new TimerTask() 
			{
				public void run() 
				{
					if (display!=null && !display.isDisposed())
					display.asyncExec(
						new Runnable() 
						{
							public void run() 
							{
								readLog(); 
							}
						}
					);
				}
			};
		tim.schedule( timtask, 2000L, 2000L);// refresh every 2 seconds... 
		
		lsClear = new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e) 
			{
				clearLog();
			}
		};
		
		lsLog = new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e) 
			{
				setLog();
			}
		};
		
		wRefresh.addSelectionListener(lsRefresh);
		wClear.addSelectionListener(lsClear);
		wLog.addSelectionListener(lsLog);

		addDisposeListener(
			new DisposeListener() 
			{
				public void widgetDisposed(DisposeEvent e) 
				{
					tim.cancel();
				}
			}
		);
	}
	
	public void readLog()
	{
		int i, n;

		if (message==null)  message = new StringBuffer(); else message.setLength(0);				
		try
		{	
			n = in.available();
					
			if (n>0)
			{
				byte buffer[] = new byte[n];
				int c = in.read(buffer, 0, n);
				for (i=0;i<c;i++) message.append((char)buffer[i]);
			}
						
		}
		catch(Exception ex)
		{
			message.append(ex.toString());
		}

		if (!wText.isDisposed() && message.length()>0) 
		{
			wText.setSelection(wText.getText().length());
			wText.clearSelection();
			wText.insert(message.toString());
		} 
	}
	
	private void clearLog()
	{
		wText.setText("");
	}
	
	private void setLog()
	{
		LogSettingsDialog lsd = new LogSettingsDialog(shell, SWT.NONE, log, schema.props);
		lsd.open();
		
	}
	
	public String toString()
	{
		return this.getClass().getName();
	}

}
