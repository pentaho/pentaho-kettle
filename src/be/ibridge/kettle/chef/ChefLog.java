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
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LocalVariables;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Result;
import be.ibridge.kettle.core.dialog.EnterSelectionDialog;
import be.ibridge.kettle.core.dialog.ErrorDialog;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleJobException;
import be.ibridge.kettle.job.Job;
import be.ibridge.kettle.job.JobEntryResult;
import be.ibridge.kettle.job.entry.JobEntryCopy;
import be.ibridge.kettle.spoon.dialog.LogSettingsDialog;

/*** 
 * Handles the display of the job execution log in the Chef application.
 * 
 * @author Matt
 * @since 17-05-2003
 *
 */
public class ChefLog extends Composite
{
	// public final static String START_TEXT = 
	// public final static String STOP_TEXT  = 

	private Color white;
	private Shell shell;
	private Display display;
	private LogWriter log;
	private Chef chef;
	
	private Tree wTree;
	
	private Text   wText;
	private Button wStart;	
    private Button wStop;  
	private Button wRefresh;
	private Button wError;
	private Button wClear;
    private Button wLog;
	private Button wAuto;

	private FormData fdText, fdSash, fdStart, fdStop, fdRefresh, fdError, fdClear, fdLog, fdAuto; 
	private SelectionListener lsStart, lsStop, lsRefresh, lsError, lsClear, lsLog;
	private StringBuffer message;

	private FileInputStream in;
	private Job job;
    private int previousNrItems;
    private boolean isRunning;

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
								
		// Create the tree table...
		wTree = new Tree(sash, SWT.V_SCROLL | SWT.H_SCROLL);
        wTree.setHeaderVisible(true);
        
        TreeColumn column1 = new TreeColumn(wTree, SWT.LEFT);
        column1.setText(Messages.getString("ChefLog.Column.JobJobEntry")); //$NON-NLS-1$
        column1.setWidth(200);
        
        TreeColumn column2 = new TreeColumn(wTree, SWT.LEFT);
        column2.setText(Messages.getString("ChefLog.Column.Comment")); //$NON-NLS-1$
        column2.setWidth(200);
        
        TreeColumn column3 = new TreeColumn(wTree, SWT.LEFT);
        column3.setText(Messages.getString("ChefLog.Column.Result")); //$NON-NLS-1$
        column3.setWidth(100);

        TreeColumn column4 = new TreeColumn(wTree, SWT.LEFT);
        column4.setText(Messages.getString("ChefLog.Column.Reason")); //$NON-NLS-1$
        column4.setWidth(200);

        TreeColumn column5 = new TreeColumn(wTree, SWT.RIGHT);
        column5.setText(Messages.getString("ChefLog.Column.Nr")); //$NON-NLS-1$
        column5.setWidth(50);

        TreeColumn column6 = new TreeColumn(wTree, SWT.RIGHT);
        column6.setText(Messages.getString("ChefLog.Column.LogDate")); //$NON-NLS-1$
        column6.setWidth(120);

		FormData fdTable=new FormData();
		fdTable.left   = new FormAttachment(0, 0);
		fdTable.top    = new FormAttachment(0, 0);
		fdTable.right  = new FormAttachment(100, 0);
		fdTable.bottom = new FormAttachment(100, 0);
		wTree.setLayoutData(fdTable);


		wText = new Text(sash, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY );
		wText.setBackground(white);
		wText.setVisible(true);

		wStart = new Button(this, SWT.PUSH);
		wStart.setText(Messages.getString("ChefLog.Button.Start"));  //$NON-NLS-1$

        wStop = new Button(this, SWT.PUSH);
        wStop.setText(Messages.getString("ChefLog.Button.Stop"));  //$NON-NLS-1$

		wRefresh = new Button(this, SWT.PUSH);
		wRefresh.setText(Messages.getString("ChefLog.Button.RefreshLog")); //$NON-NLS-1$

		wError = new Button(this, SWT.PUSH);
		wError.setText(Messages.getString("ChefLog.Button.ShowErrorLines")); //$NON-NLS-1$

		wClear = new Button(this, SWT.PUSH);
		wClear.setText(Messages.getString("ChefLog.Button.ClearLog")); //$NON-NLS-1$

		wLog = new Button(this, SWT.PUSH);
		wLog.setText(Messages.getString("ChefLog.Button.LogSettings")); //$NON-NLS-1$

        wAuto = new Button(this, SWT.CHECK);
        wAuto.setText(Messages.getString("ChefLog.Button.AutoRefresh")); //$NON-NLS-1$
        wAuto.setSelection(true);

        enableFields();
        
		fdStart    = new FormData(); 
        fdStop     = new FormData(); 
		fdRefresh  = new FormData(); 
		fdError    = new FormData(); 
		fdClear    = new FormData(); 
		fdLog      = new FormData(); 
        fdAuto     = new FormData(); 

		fdStart.left   = new FormAttachment(0, 10);  
		fdStart.bottom = new FormAttachment(100, 0);
		wStart.setLayoutData(fdStart);

        fdStop.left   = new FormAttachment(wStart, 10);  
        fdStop.bottom = new FormAttachment(100, 0);
        wStop.setLayoutData(fdStop);

		fdRefresh.left   = new FormAttachment(wStop, 10);  
		fdRefresh.bottom = new FormAttachment(100, 0);
		wRefresh.setLayoutData(fdRefresh);

		fdError.left   = new FormAttachment(wRefresh, 10);  
		fdError.bottom = new FormAttachment(100, 0);
		wError.setLayoutData(fdError);

		fdClear.left   = new FormAttachment(wError, 10);  
		fdClear.bottom = new FormAttachment(100, 0);
		wClear.setLayoutData(fdClear);

		fdLog.left   = new FormAttachment(wClear, 10);  
		fdLog.bottom = new FormAttachment(100, 0);
		wLog.setLayoutData(fdLog);

        fdAuto.left   = new FormAttachment(wLog, 10);  
        fdAuto.bottom = new FormAttachment(100, 0);
        wAuto.setLayoutData(fdAuto);

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
			System.out.println(Messages.getString("ChefLog.Error.CouldNotCreateInputPipe")); //$NON-NLS-1$
		}
		
		lsRefresh = new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e) 
			{
				readLog();
				checkEnded();
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
								// Chef if the widgets are not disposed.  
								// This happens is the rest of the window is not yet disposed.
								// We ARE running in a different thread after all.
								//
								if (!wAuto.isDisposed() && !wText.isDisposed() && !wStart.isDisposed() && !wTree.isDisposed())
								{
                                    if (wAuto.getSelection())
	                                {
                                        readLog(); 
	    								checkEnded();
	                                }
								}
							}
						}
					);
				}
			};
		tim.schedule( timtask, 10L, 10L);// refresh every 2 seconds... 
		
		lsStart = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { startJob(); } };
        lsStop = new SelectionAdapter()  { public void widgetSelected(SelectionEvent e) { stopJob(); } };
		lsError = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { showErrors(); } };
		lsClear = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { clearLog(); } };
		lsLog = new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { setLog(); } };
		
		wRefresh.addSelectionListener(lsRefresh);
		wStart.addSelectionListener(lsStart);
        wStop.addSelectionListener(lsStop);
		wError.addSelectionListener(lsError);
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
	
	public synchronized void startJob()
	{
		if (job==null) // Not running, start the transformation...
		{
			// Auto save feature...
			if (chef.jobMeta.hasChanged())
			{
				if (chef.props.getAutoSave()) 
				{
					log.logDetailed(toString(), Messages.getString("ChefLog.Log.AutoSaveFileBeforeRunning")); //$NON-NLS-1$
					System.out.println(Messages.getString("ChefLog.Log.AutoSaveFileBeforeRunning2")); //$NON-NLS-1$
					chef.saveFile();
				}
				else
				{
					MessageDialogWithToggle md = new MessageDialogWithToggle(shell, 
																			 Messages.getString("ChefLog.Dialog.SaveChangedFile.Title"),  //$NON-NLS-1$
																			 null,
																			 Messages.getString("ChefLog.Dialog.SaveChangedFile.Message")+Const.CR+Messages.getString("ChefLog.Dialog.SaveChangedFile.Message2")+Const.CR, //$NON-NLS-1$ //$NON-NLS-2$
																			 MessageDialog.QUESTION,
																			 new String[] { Messages.getString("System.Button.Yes"), Messages.getString("System.Button.No") }, //$NON-NLS-1$ //$NON-NLS-2$
																			 0,
																			 Messages.getString("ChefLog.Dialog.SaveChangedFile.Toggle"), //$NON-NLS-1$
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
                        job = new Job(log, chef.jobMeta.getName(), chef.jobMeta.getFilename(), null);
						job.open(chef.rep, chef.jobMeta.getFilename(), chef.jobMeta.getName(), chef.jobMeta.getDirectory().getPath());
						
                        log.logMinimal(Chef.APP_NAME, Messages.getString("ChefLog.Log.StartingJob")); //$NON-NLS-1$
						job.start();
                        isRunning=true;
					}
					catch(KettleException e)
					{
						new ErrorDialog(shell, chef.props, Messages.getString("ChefLog.Dialog.CanNotOpenJob.Title"), Messages.getString("ChefLog.Dialog.CanNotOpenJob.Message"), e);  //$NON-NLS-1$ //$NON-NLS-2$
						job=null;
					}
				}
				else
				{
					MessageBox m = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
					m.setText(Messages.getString("ChefLog.Dialog.JobIsAlreadyRunning.Title")); //$NON-NLS-1$
					m.setMessage(Messages.getString("ChefLog.Dialog.JobIsAlreadyRunning.Message"));	 //$NON-NLS-1$
					m.open();
				}
			}
			else
			{
				if (chef.jobMeta.hasChanged())
				{
					MessageBox m = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
					m.setText(Messages.getString("ChefLog.Dialog.JobHasChangedSave.Title")); //$NON-NLS-1$
					m.setMessage(Messages.getString("ChefLog.Dialog.JobHasChangedSave.Message"));	 //$NON-NLS-1$
					m.open();
				}
				else
				if (chef.rep!=null && chef.jobMeta.getName()==null)
				{
					MessageBox m = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
					m.setText(Messages.getString("ChefLog.Dialog.PleaseGiveThisJobAName.Title")); //$NON-NLS-1$
					m.setMessage(Messages.getString("ChefLog.Dialog.PleaseGiveThisJobAName.Message"));	 //$NON-NLS-1$
					m.open();
				}
				else
				{
					MessageBox m = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
					m.setText(Messages.getString("ChefLog.Dialog.NoFilenameSaveYourJobFirst.Title")); //$NON-NLS-1$
					m.setMessage(Messages.getString("ChefLog.Dialog.NoFilenameSaveYourJobFirst.Message"));	 //$NON-NLS-1$
					m.open();
				}
			}
            enableFields();
		} 
	}
	
	private synchronized void stopJob()
    {
        try
        {
            if (job!=null) 
            {
                job.stopAll();
                job.endProcessing("stop"); //$NON-NLS-1$
                LocalVariables.getInstance().removeKettleVariables(job.getName());
                job=null;
                isRunning=false;
                log.logMinimal(Chef.APP_NAME, Messages.getString("ChefLog.Log.JobWasStopped")); //$NON-NLS-1$
            }
        }
        catch(KettleJobException je)
        {
            MessageBox m = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
            m.setText(Messages.getString("ChefLog.Dialog.UnableToSaveStopLineInLoggingTable.Title")); //$NON-NLS-1$
            m.setMessage(Messages.getString("ChefLog.Dialog.UnableToSaveStopLineInLoggingTable.Message")+Const.CR+je.toString());    //$NON-NLS-1$
            m.open();
        }
        finally
        {
            enableFields();
        }
    }

    public void enableFields()
    {
        wStart.setEnabled(!isRunning);
        wStop.setEnabled(isRunning);
        chef.tiFileRun.setEnabled(!isRunning);
    }

    public void readLog()
	{
		if (message==null)  message = new StringBuffer(); else message.setLength(0);				
		try		
		{
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, Const.XML_ENCODING));
            String line;
            while ( (line=reader.readLine()) != null )
            {
                message.append(line);
                message.append(Const.CR);
            }
			refreshTreeTable();
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
	

    /**
     * Refresh the data in the tree-table...
     * Use the data from the JobTracker in the job
     */
	private void refreshTreeTable()
    {
        if (job!=null)
        {
            JobTracker jobTracker = job.getJobTracker();
            
            int nrItems = jobTracker.getTotalNumberOfItems();
            
            if (nrItems!=previousNrItems)
            {
                // Allow some flickering for now ;-)
                wTree.removeAll();
                
                // Re-populate this...
                TreeItem treeItem = new TreeItem(wTree, SWT.NONE);
                String jobName = jobTracker.getJobMeta().getName();
                if(Const.isEmpty(jobName)) 
                {
                    if (!Const.isEmpty(jobTracker.getJobMeta().getFilename())) jobName = jobTracker.getJobMeta().getFilename();
                    else jobName = Messages.getString("ChefLog.Tree.StringToDisplayWhenJobHasNoName"); //$NON-NLS-1$
                }
                treeItem.setText( 0,jobName);
                for (int i=0;i<jobTracker.nrJobTrackers();i++)
                {
                    addTrackerToTree(jobTracker.getJobTracker(i), treeItem);
                }
                treeItem.setExpanded(true);
                previousNrItems = nrItems;
            }
        }
    }

    private void addTrackerToTree(JobTracker jobTracker, TreeItem parentItem)
    {
        try
        {
            if (jobTracker!=null)
            {
                TreeItem treeItem = new TreeItem(parentItem, SWT.NONE);
                if (jobTracker.nrJobTrackers()>0)
                {
                    // This is a sub-job: display the name at the top of the list...
                    treeItem.setText( 0, Messages.getString("ChefLog.Tree.JobPrefix")+jobTracker.getJobMeta().getName() ); //$NON-NLS-1$
                    
                    // then populare the sub-job entries ...
                    for (int i=0;i<jobTracker.nrJobTrackers();i++)
                    {
                        addTrackerToTree(jobTracker.getJobTracker(i), treeItem);
                    }
                }
                else
                {
                    JobEntryResult result = jobTracker.getJobEntryResult();
                    if (result!=null)
                    {
                        JobEntryCopy jec = result.getJobEntry();
                        if (jec!=null)
                        {
                            treeItem.setText( 0, jec.getName() );
                        }
                        else
                        {
                            treeItem.setText( 0, Messages.getString("ChefLog.Tree.JobPrefix2")+jobTracker.getJobMeta().getName()); //$NON-NLS-1$
                        }
                        String comment = result.getComment();
                        if (comment!=null)
                        {
                            treeItem.setText(1, comment);
                        }
                        Result res = result.getResult();
                        if (res!=null)
                        {
                            treeItem.setText(2, res.getResult()?Messages.getString("ChefLog.Tree.Success"):Messages.getString("ChefLog.Tree.Failure")); //$NON-NLS-1$ //$NON-NLS-2$
                            treeItem.setText(4, ""+res.getEntryNr()); //$NON-NLS-1$
                        }
                        String reason = result.getReason();
                        if (reason!=null)
                        {
                            treeItem.setText(3, reason );
                        }
                        Date logDate = result.getLogDate();
                        if (logDate!=null)
                        {
                            treeItem.setText(5, new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(logDate)); //$NON-NLS-1$
                        }
                    }
                }
                treeItem.setExpanded(true);
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    private synchronized void checkEnded()
	{
		if (isRunning && job!=null && job.isInitialized() && !job.isAlive())
        {
            LocalVariables.getInstance().removeKettleVariables(job.getName());

            job=null;
            isRunning=false;
            log.logMinimal(Chef.APP_NAME, Messages.getString("ChefLog.Log.JobHasEnded")); //$NON-NLS-1$
        }
		
		if (!wStart.isDisposed())
		{
            enableFields();
		}
	}
	
    private void clearLog()
	{
		wText.setText(""); //$NON-NLS-1$
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
    
	public void showErrors()
	{
		String all = wText.getText();
		ArrayList err = new ArrayList();
		
		int i = 0;
		int startpos = 0;
		int crlen = Const.CR.length();
		
		while (i<all.length()-crlen)
		{
			if (all.substring(i, i+crlen).equalsIgnoreCase(Const.CR))
			{
				String line = all.substring(startpos, i);
				if (line.toUpperCase().indexOf(Messages.getString("ChefLog.System.ERROR"))>=0 || //$NON-NLS-1$
				    line.toUpperCase().indexOf(Messages.getString("ChefLog.System.EXCEPTION"))>=0 //$NON-NLS-1$
				    ) 
				{
					err.add(line);
				}
				// New start of line
				startpos=i+crlen;
			}
			
			i++;
		}
		String line = all.substring(startpos);
		if (line.toUpperCase().indexOf(Messages.getString("ChefLog.System.ERROR"))>=0 || //$NON-NLS-1$
		    line.toUpperCase().indexOf(Messages.getString("ChefLog.System.EXCEPTION"))>=0 //$NON-NLS-1$
		    ) 
		{
			err.add(line);
		}
		
		if (err.size()>0)
		{
			String err_lines[] = new String[err.size()];
			for (i=0;i<err_lines.length;i++) err_lines[i] = (String)err.get(i);
			
			EnterSelectionDialog esd = new EnterSelectionDialog(shell, chef.props, err_lines, Messages.getString("ChefLog.Dialog.ErrorLines.Title"), Messages.getString("ChefLog.Dialog.ErrorLines.Message")); //$NON-NLS-1$ //$NON-NLS-2$
			line = esd.open();
			if (line!=null)
			{
				for (i=0;i<chef.getJobMeta().nrJobEntries();i++)
				{
					JobEntryCopy entryCopy = chef.getJobMeta().getJobEntry(i);
					if (line.indexOf( entryCopy.getName() ) >=0 )
					{
						chef.editChefGraphEntry( entryCopy );
					}
				}
				// System.out.println("Error line selected: "+line);
			}
		}
	}
}
