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

 
package be.ibridge.kettle.chef;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
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
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import be.ibridge.kettle.core.ColumnInfo;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.dialog.ErrorDialog;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleJobException;
import be.ibridge.kettle.job.Job;
import be.ibridge.kettle.spoon.dialog.LogSettingsDialog;
import de.kupzog.ktable.KTable;

/**
 * TODO add name of job in list (subjobs etc)
 * TODO add stopped property to Result
 * 
 * Handles the display of the job execution log in the Chef application.
 * 
 * @author Matt
 * @since 17-05-2003
 *
 */
public class ChefLog extends Composite
{
	public final static String START_TEXT = "St&art Job"; 
	public final static String STOP_TEXT  = "St&op Job"; 

	private Color white;
	private Shell shell;
	private Display display;
	private LogWriter log;
	private Chef chef;
	
	private KTable wTable;
	private ArrayList jobEntryResults;
	
	private Text   wText;
	private Button wStart;	
	private Button wRefresh;
	private Button wClear;
	private Button wLog;

	private FormData fdText, fdSash, fdStart, fdRefresh, fdClear, fdLog; 
	private SelectionListener lsStart, lsRefresh, lsClear, lsLog;
	private StringBuffer message;

	private FileInputStream in;
	private Job job;

    /** @deprecated */
    public ChefLog(Composite parent, int style, LogWriter log, Chef chef)
    {
        this(parent, style, chef);
    }

	public ChefLog(Composite parent, int style, Chef chef)
	{
		super(parent, style);
		
		shell=parent.getShell();
		this.log=LogWriter.getInstance();
		display=shell.getDisplay();
		this.chef=chef;
		
		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;
		
		setLayout(formLayout);
		
		setVisible(true);
		white = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
		addDisposeListener(new DisposeListener() { public void widgetDisposed(DisposeEvent e) { white.dispose(); } });

		SashForm sash = new SashForm(this, SWT.VERTICAL);
 		// props.setLook(sash); TODO: Check this
		
		ColumnInfo colinf[] = 
			{
			 new ColumnInfo("Nr",       	  ColumnInfo.COLUMN_TYPE_TEXT, false, true),
			 new ColumnInfo("Icon",           ColumnInfo.COLUMN_TYPE_ICON ),
			 new ColumnInfo("Job name",       ColumnInfo.COLUMN_TYPE_TEXT, false, true),
			 new ColumnInfo("Entry name",     ColumnInfo.COLUMN_TYPE_TEXT, false, true),
			 new ColumnInfo("Result",         ColumnInfo.COLUMN_TYPE_TEXT, false, true),
			 new ColumnInfo("Previous entry", ColumnInfo.COLUMN_TYPE_TEXT, false, true),
			 new ColumnInfo("Comment",        ColumnInfo.COLUMN_TYPE_TEXT, false, true)
			};
		
		// Default: empty!
		jobEntryResults = new ArrayList();
				
		// Create the KTable...
		wTable = new KTable(sash, SWT.V_SCROLL | SWT.H_SCROLL);
		wTable.setRowSelectionMode(true);
		wTable.setModel(new ChefLogTableModel(colinf, jobEntryResults, chef.getChefGraph()));
 		// props.setLook(wTable); TODO: check this
		
		FormData fdTable=new FormData();
		fdTable.left   = new FormAttachment(0, 0);
		fdTable.top    = new FormAttachment(0, 0);
		fdTable.right  = new FormAttachment(100, 0);
		fdTable.bottom = new FormAttachment(100, 0);
		wTable.setLayoutData(fdTable);


		wText = new Text(sash, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY );
		wText.setBackground(white);
		wText.setVisible(true);

		wStart = new Button(this, SWT.PUSH);
		wStart.setText(START_TEXT);

		wRefresh = new Button(this, SWT.PUSH);
		wRefresh.setText("&Refresh log");

		wClear = new Button(this, SWT.PUSH);
		wClear.setText("&Clear log");

		wLog = new Button(this, SWT.PUSH);
		wLog.setText("&Log settings");

		fdStart    = new FormData(); 
		fdRefresh  = new FormData(); 
		fdClear    = new FormData(); 
		fdLog      = new FormData(); 

		fdStart.left   = new FormAttachment(15, 0);  
		fdStart.bottom = new FormAttachment(100, 0);
		wStart.setLayoutData(fdStart);

		fdRefresh.left   = new FormAttachment(wStart, 10);  
		fdRefresh.bottom = new FormAttachment(100, 0);
		wRefresh.setLayoutData(fdRefresh);

		fdClear.left   = new FormAttachment(wRefresh, 10);  
		fdClear.bottom = new FormAttachment(100, 0);
		wClear.setLayoutData(fdClear);

		fdLog.left   = new FormAttachment(wClear, 10);  
		fdLog.bottom = new FormAttachment(100, 0);
		wLog.setLayoutData(fdLog);

		fdText=new FormData();
		fdText.left   = new FormAttachment(0, 0);
		fdText.top    = new FormAttachment(0, 0);
		fdText.right  = new FormAttachment(100, 0);
		fdText.bottom = new FormAttachment(100, -30);
		wText.setLayoutData(fdText);

		fdSash     = new FormData(); 
		fdSash.left   = new FormAttachment(0, 0);  // First one in the left top corner
		fdSash.top    = new FormAttachment(0, 0);
		fdSash.right  = new FormAttachment(100, 0);
		fdSash.bottom = new FormAttachment(wStart, -5);
		sash.setLayoutData(fdSash);

		sash.setWeights(new int[] { 60, 40} );

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
				refreshView();
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
								checkErrors();	
								readLog(); 
								refreshView(); 
							}
						}
					);
				}
			};
		tim.schedule( timtask, 2000L, 2000L);// refresh every 2 seconds... 
		
		lsStart = new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e) 
			{
				startstop();
			}
		};

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
		wStart.addSelectionListener(lsStart);
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
	
	public void startstop()
	{
		if (job==null) // Not running, start the transformation...
		{
			// Auto save feature...
			if (chef.jobMeta.hasChanged())
			{
				if (chef.props.getAutoSave()) 
				{
					log.logDetailed(toString(), "Save file automatically before running...");
					System.out.println("Save file automatically before running...");
					chef.saveFile();
				}
				else
				{
					MessageDialogWithToggle md = new MessageDialogWithToggle(shell, 
																			 "File has changed!", 
																			 null,
																			 "You need to save your job before you can run it."+Const.CR+"Do you want to save the job now?"+Const.CR,
																			 MessageDialog.QUESTION,
																			 new String[] { "Yes", "No" },
																			 0,
																			 "Automatically save the job.",
																			 chef.props.getAutoSave()
																			 );
					int answer = md.open();
					if (answer == 0)
					{
						chef.saveFile();
					}
					chef.props.setAutoSave(md.getToggleState());
				}
			}
			
            if ( ((chef.jobMeta.getName()!=null && chef.rep!=null) ||     // Repository available & name set
			      (chef.jobMeta.getFilename()!=null && chef.rep==null )   // No repository & filename set
			      ) && !chef.jobMeta.hasChanged()                             // Didn't change
			   )
			{
				if (job==null || (job!=null && job.isActive()) )
				{
					try
					{
						wStart.setText(STOP_TEXT);
						job = new Job(log, chef.jobMeta.getName(), chef.jobMeta.getFilename(), null);
						job.open(chef.rep, chef.jobMeta.getFilename(), chef.jobMeta.getName(), chef.jobMeta.getDirectory().getPath());
						job.setJobEntryResults(jobEntryResults);
						job.start();
						readLog();
					}
					catch(KettleException e)
					{
						new ErrorDialog(shell, chef.props, "Can't open job", "Job failed to open", e); 
						job=null;
					}
				}
				else
				{
					MessageBox m = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
					m.setText("Warning!");
					m.setMessage("The job is running, don't start it twice!");	
					m.open();
				}
			}
			else
			{
				if (chef.jobMeta.hasChanged())
				{
					MessageBox m = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
					m.setText("File has changed!");
					m.setMessage("Please save your job first!");	
					m.open();
				}
				else
				if (chef.rep!=null && chef.jobMeta.getName()==null)
				{
					MessageBox m = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
					m.setText("This job has no name!");
					m.setMessage("Please give your job a name to identify it by!");	
					m.open();
				}
				else
				{
					MessageBox m = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
					m.setText("No filename!");
					m.setMessage("Before running, please save your job first!");	
					m.open();
				}
			}
		} 
		else
		{
			try
			{
				if (job!=null) 
				{
					job.stopAll();
					job.endProcessing("stop");
					job=null;
				}
			}
			catch(KettleJobException je)
			{
				MessageBox m = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
				m.setText("Error!");
				m.setMessage("I was unable to log the stop signal to the log table:"+Const.CR+je.toString());	
				m.open();
			}
			finally
			{
				job=null;
				wStart.setText(START_TEXT);
			}
		}
	}

	public void checkErrors()
	{
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
			
			wTable.redraw();
			/*
			System.out.println("jobEntryResults has "+jobEntryResults.size()+" results ");
			System.out.println("job has "+job.getJobEntryResults().size()+" results ");
			System.out.println("table has "+wTable.getModel().getRowCount());
			System.out.println();
			*/
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
	
	
	private void refreshView()
	{
		if (job!=null && !job.isActive()) job=null;
		
		if (!wStart.isDisposed())
		{
			if (job!=null) wStart.setText(STOP_TEXT); else wStart.setText(START_TEXT);
		}
	}
	
    private void clearLog()
	{
		wText.setText("");
	}
	
	private void setLog()
	{
		LogSettingsDialog lsd = new LogSettingsDialog(shell, SWT.NONE, log, chef.props);
		lsd.open();
	}
	
	public String toString()
	{
		return this.getClass().getName();
	}

    public boolean isRunning()
    {
        return job!=null;
    }

}
