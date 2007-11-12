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

 
package org.pentaho.di.ui.spoon.job;

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
import org.pentaho.di.core.Const;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleJobException;
import org.pentaho.di.core.gui.JobTracker;
import org.pentaho.di.core.logging.BufferChangedListener;
import org.pentaho.di.core.logging.Log4jStringAppender;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobEntryResult;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.TreeMemory;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.TabItemInterface;
import org.pentaho.di.ui.spoon.dialog.LogSettingsDialog;


/*** 
 * Handles the display of the job execution log in the Chef application.
 * 
 * @author Matt
 * @since 17-05-2003
 *
 */
public class JobLog extends Composite implements TabItemInterface
{
	private Color white;
	private Shell shell;
	private Display display;
	private LogWriter log;
	private Spoon spoon;
    private JobMeta jobMeta;
	
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

	private Job job;
    private int previousNrItems;
    private boolean isRunning;
    private JobTracker jobTracker;
    private JobHistoryRefresher chefHistoryRefresher;
	private Log4jStringAppender stringAppender;
	private int textSize;

    private static final String STRING_CHEF_LOG_TREE_NAME = "Job Log Tree";
    
	public JobLog(Composite parent, final Spoon spoon, final JobMeta jobMeta)
	{
		super(parent, SWT.NONE);
		
		shell=parent.getShell();
		this.log=LogWriter.getInstance();
		display=shell.getDisplay();
		this.spoon=spoon;
        this.jobMeta = jobMeta;
		
		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;
		
		setLayout(formLayout);
		
		setVisible(true);
		white = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
		
		SashForm sash = new SashForm(this, SWT.VERTICAL);
								
		// Create the tree table...
		wTree = new Tree(sash, SWT.V_SCROLL | SWT.H_SCROLL);
        wTree.setHeaderVisible(true);
        TreeMemory.addTreeListener(wTree, STRING_CHEF_LOG_TREE_NAME);
        
        TreeColumn column1 = new TreeColumn(wTree, SWT.LEFT);
        column1.setText(Messages.getString("JobLog.Column.JobJobEntry")); //$NON-NLS-1$
        column1.setWidth(200);
        
        TreeColumn column2 = new TreeColumn(wTree, SWT.LEFT);
        column2.setText(Messages.getString("JobLog.Column.Comment")); //$NON-NLS-1$
        column2.setWidth(200);
        
        TreeColumn column3 = new TreeColumn(wTree, SWT.LEFT);
        column3.setText(Messages.getString("JobLog.Column.Result")); //$NON-NLS-1$
        column3.setWidth(100);

        TreeColumn column4 = new TreeColumn(wTree, SWT.LEFT);
        column4.setText(Messages.getString("JobLog.Column.Reason")); //$NON-NLS-1$
        column4.setWidth(200);

        TreeColumn column5 = new TreeColumn(wTree, SWT.LEFT);
        column5.setText(Messages.getString("JobLog.Column.Filename")); //$NON-NLS-1$
        column5.setWidth(200);

        TreeColumn column6 = new TreeColumn(wTree, SWT.RIGHT);
        column6.setText(Messages.getString("JobLog.Column.Nr")); //$NON-NLS-1$
        column6.setWidth(50);

        TreeColumn column7 = new TreeColumn(wTree, SWT.RIGHT);
        column7.setText(Messages.getString("JobLog.Column.LogDate")); //$NON-NLS-1$
        column7.setWidth(120);

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
		wStart.setText(Messages.getString("JobLog.Button.Start"));  //$NON-NLS-1$

        wStop = new Button(this, SWT.PUSH);
        wStop.setText(Messages.getString("JobLog.Button.Stop"));  //$NON-NLS-1$

		wRefresh = new Button(this, SWT.PUSH);
		wRefresh.setText(Messages.getString("JobLog.Button.RefreshLog")); //$NON-NLS-1$

		wError = new Button(this, SWT.PUSH);
		wError.setText(Messages.getString("JobLog.Button.ShowErrorLines")); //$NON-NLS-1$

		wClear = new Button(this, SWT.PUSH);
		wClear.setText(Messages.getString("JobLog.Button.ClearLog")); //$NON-NLS-1$

		wLog = new Button(this, SWT.PUSH);
		wLog.setText(Messages.getString("JobLog.Button.LogSettings")); //$NON-NLS-1$

        wAuto = new Button(this, SWT.CHECK);
        wAuto.setText(Messages.getString("JobLog.Button.AutoRefresh")); //$NON-NLS-1$
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

		// Create a new String appender to the log and capture that directly...
		//
		stringAppender = LogWriter.createStringAppender();
		stringAppender.setMaxNrLines(Props.getInstance().getMaxNrLinesInLog());
		stringAppender.addBufferChangedListener(new BufferChangedListener() {
		
			public void contentWasAdded(final StringBuffer content, final String extra, final int nrLines) {
				display.asyncExec(new Runnable() {
				
					public void run() 
					{
						if (!wText.isDisposed())
						{
							textSize++;
							
							// OK, now what if the number of lines gets too big?
							// We allow for a few hundred lines buffer over-run.
							// That way we reduce flicker...
							//
							if (textSize>=nrLines+200)
							{
								wText.setText(content.toString());
								wText.setSelection(content.length());
								wText.showSelection();
								wText.clearSelection();
								textSize=nrLines;
							}
							else
							{
								wText.append(extra);
							}
						}
					}
				
				});
			}
		
		});
		log.addAppender(stringAppender);
		addDisposeListener(new DisposeListener() { public void widgetDisposed(DisposeEvent e) { log.removeAppender(stringAppender); } });

		lsRefresh = new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e) 
			{
				readLog();
				checkEnded();
			}
		};
		
		final Timer tim = new Timer("JobLog: " + this.getMeta().getName());
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
		
		// Key listeners...
		//
		addKeyListener(spoon.defKeys);
		wTree.addKeyListener(spoon.defKeys);
		wText.addKeyListener(spoon.defKeys);
		sash.addKeyListener(spoon.defKeys);

		wRefresh.addKeyListener(spoon.defKeys);
		wStart.addKeyListener(spoon.defKeys);
        wStop.addKeyListener(spoon.defKeys);
		wError.addKeyListener(spoon.defKeys);
		wClear.addKeyListener(spoon.defKeys);
		wLog.addKeyListener(spoon.defKeys);
	}
    
    private synchronized void startJob()
    {
    	spoon.executeJob();
    }
    
	
	public synchronized void startJob(Date replayDate)
	{
		if (job==null) // Not running, start the transformation...
		{
			// Auto save feature...
			if (jobMeta.hasChanged())
			{
				if (spoon.props.getAutoSave()) 
				{
					log.logDetailed(toString(), Messages.getString("JobLog.Log.AutoSaveFileBeforeRunning")); //$NON-NLS-1$
					System.out.println(Messages.getString("JobLog.Log.AutoSaveFileBeforeRunning2")); //$NON-NLS-1$
					spoon.saveToFile(jobMeta);
				}
				else
				{
					MessageDialogWithToggle md = new MessageDialogWithToggle(shell, 
						 Messages.getString("JobLog.Dialog.SaveChangedFile.Title"),  //$NON-NLS-1$
						 null,
						 Messages.getString("JobLog.Dialog.SaveChangedFile.Message")+Const.CR+Messages.getString("JobLog.Dialog.SaveChangedFile.Message2")+Const.CR, //$NON-NLS-1$ //$NON-NLS-2$
						 MessageDialog.QUESTION,
						 new String[] { Messages.getString("System.Button.Yes"), Messages.getString("System.Button.No") }, //$NON-NLS-1$ //$NON-NLS-2$
						 0,
						 Messages.getString("JobLog.Dialog.SaveChangedFile.Toggle"), //$NON-NLS-1$
						 spoon.props.getAutoSave()
						 );
					int answer = md.open();
					if ( (answer&0xFF) == 0)
					{
						spoon.saveToFile(jobMeta);
					}
					spoon.props.setAutoSave(md.getToggleState());
				}
			}
			
            if ( ((jobMeta.getName()!=null && spoon.rep!=null) ||     // Repository available & name set
			      (jobMeta.getFilename()!=null && spoon.rep==null )   // No repository & filename set
			      ) && !jobMeta.hasChanged()                             // Didn't change
			   )
			{
				if (job==null || (job!=null && !job.isActive()) )
				{
					try
					{
                        // TODO: clean up this awful mess...
                        //
                        job = new Job(log, jobMeta.getName(), jobMeta.getFilename(), null);
						job.open(spoon.rep, jobMeta.getFilename(), jobMeta.getName(), jobMeta.getDirectory().getPath(), spoon);
                        job.getJobMeta().setArguments(jobMeta.getArguments());
                        job.shareVariablesWith(jobMeta);
                        log.logMinimal(Spoon.APP_NAME, Messages.getString("JobLog.Log.StartingJob")); //$NON-NLS-1$
						job.start();
                        // Link to the new jobTracker!
                        jobTracker = job.getJobTracker();
                        isRunning=true;
					}
					catch(KettleException e)
					{
						new ErrorDialog(shell, Messages.getString("JobLog.Dialog.CanNotOpenJob.Title"), Messages.getString("JobLog.Dialog.CanNotOpenJob.Message"), e);  //$NON-NLS-1$ //$NON-NLS-2$
						job=null;
					}
				}
				else
				{
					MessageBox m = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
					m.setText(Messages.getString("JobLog.Dialog.JobIsAlreadyRunning.Title")); //$NON-NLS-1$
					m.setMessage(Messages.getString("JobLog.Dialog.JobIsAlreadyRunning.Message"));	 //$NON-NLS-1$
					m.open();
				}
			}
			else
			{
				if (jobMeta.hasChanged())
				{
					MessageBox m = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
					m.setText(Messages.getString("JobLog.Dialog.JobHasChangedSave.Title")); //$NON-NLS-1$
					m.setMessage(Messages.getString("JobLog.Dialog.JobHasChangedSave.Message"));	 //$NON-NLS-1$
					m.open();
				}
				else
				if (spoon.rep!=null && jobMeta.getName()==null)
				{
					MessageBox m = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
					m.setText(Messages.getString("JobLog.Dialog.PleaseGiveThisJobAName.Title")); //$NON-NLS-1$
					m.setMessage(Messages.getString("JobLog.Dialog.PleaseGiveThisJobAName.Message"));	 //$NON-NLS-1$
					m.open();
				}
				else
				{
					MessageBox m = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
					m.setText(Messages.getString("JobLog.Dialog.NoFilenameSaveYourJobFirst.Title")); //$NON-NLS-1$
					m.setMessage(Messages.getString("JobLog.Dialog.NoFilenameSaveYourJobFirst.Message"));	 //$NON-NLS-1$
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
            if (job!=null && isRunning && job.isInitialized()) 
            {
                job.stopAll();
                job.endProcessing("stop", new Result()); //$NON-NLS-1$
                job.waitUntilFinished(5000); // wait until everything is stopped, maximum 5 seconds...
                job=null;
                isRunning=false;
                log.logMinimal(Spoon.APP_NAME, Messages.getString("JobLog.Log.JobWasStopped")); //$NON-NLS-1$
            }
        }
        catch(KettleJobException je)
        {
            MessageBox m = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
            m.setText(Messages.getString("JobLog.Dialog.UnableToSaveStopLineInLoggingTable.Title")); //$NON-NLS-1$
            m.setMessage(Messages.getString("JobLog.Dialog.UnableToSaveStopLineInLoggingTable.Message")+Const.CR+je.toString());    //$NON-NLS-1$
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
        // spoon.tiFileRun.setEnabled(!isRunning); TODO: make spoons menu's smarter
    }

    public void readLog()
	{
		try
		{
			refreshTreeTable();
		}
		catch(Exception ex)
		{
			wText.append(ex.toString());
		}
	}
	

    /**
     * Refresh the data in the tree-table...
     * Use the data from the JobTracker in the job
     */
	private void refreshTreeTable()
    {
        if (jobTracker!=null)
        {
            int nrItems = jobTracker.getTotalNumberOfItems();
            
            if (nrItems!=previousNrItems)
            {
                // Allow some flickering for now ;-)
                wTree.removeAll();
                
                // Re-populate this...
                TreeItem treeItem = new TreeItem(wTree, SWT.NONE);
                String jobName = jobTracker.getJobName();

                if(Const.isEmpty(jobName)) 
                {
                    if (!Const.isEmpty(jobTracker.getJobFilename())) jobName = jobTracker.getJobFilename();
                    else jobName = Messages.getString("JobLog.Tree.StringToDisplayWhenJobHasNoName"); //$NON-NLS-1$
                }
                treeItem.setText(0, jobName);
                TreeMemory.getInstance().storeExpanded(STRING_CHEF_LOG_TREE_NAME, new String[] { jobName }, true);

                for (int i=0;i<jobTracker.nrJobTrackers();i++)
                {
                    addTrackerToTree(jobTracker.getJobTracker(i), treeItem);
                }
                previousNrItems = nrItems;
                
                TreeMemory.setExpandedFromMemory(wTree, STRING_CHEF_LOG_TREE_NAME);
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
                    treeItem.setText( 0, Messages.getString("JobLog.Tree.JobPrefix")+jobTracker.getJobName() ); //$NON-NLS-1$
                    
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
                            
                            if (jec.getEntry()!=null)
                            {
                                treeItem.setText( 4, Const.NVL( jec.getEntry().getRealFilename(), "") );
                            }
                        }
                        else
                        {
                            treeItem.setText( 0, Messages.getString("JobLog.Tree.JobPrefix2")+jobTracker.getJobName()); //$NON-NLS-1$
                        }
                        String comment = result.getComment();
                        if (comment!=null)
                        {
                            treeItem.setText(1, comment);
                        }
                        Result res = result.getResult();
                        if (res!=null)
                        {
                            treeItem.setText(2, res.getResult()?Messages.getString("JobLog.Tree.Success"):Messages.getString("JobLog.Tree.Failure")); //$NON-NLS-1$ //$NON-NLS-2$
                            treeItem.setText(5, Long.toString(res.getEntryNr())); //$NON-NLS-1$
                        }
                        String reason = result.getReason();
                        if (reason!=null)
                        {
                            treeItem.setText(3, reason );
                        }
                        Date logDate = result.getLogDate();
                        if (logDate!=null)
                        {
                            treeItem.setText(6, new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(logDate)); //$NON-NLS-1$
                        }
                    }
                }
                treeItem.setExpanded(true);
            }
        }
        catch(Exception e)
        {
            log.logError(toString(), Const.getStackTracker(e));
        }
    }

    private synchronized void checkEnded()
	{
		if (isRunning && job!=null && job.isInitialized() && !job.isAlive())
        {
            job=null;
            isRunning=false;
            if (chefHistoryRefresher!=null) chefHistoryRefresher.markRefreshNeeded();
            log.logMinimal(Spoon.APP_NAME, Messages.getString("JobLog.Log.JobHasEnded")); //$NON-NLS-1$
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
		LogSettingsDialog lsd = new LogSettingsDialog(shell, SWT.NONE, log, spoon.props);
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
		ArrayList<String> err = new ArrayList<String>();
		
		int i = 0;
		int startpos = 0;
		int crlen = Const.CR.length();
		
		while (i<all.length()-crlen)
		{
			if (all.substring(i, i+crlen).equalsIgnoreCase(Const.CR))
			{
				String line = all.substring(startpos, i);
				if (line.toUpperCase().indexOf(Messages.getString("JobLog.System.ERROR"))>=0 || //$NON-NLS-1$
				    line.toUpperCase().indexOf(Messages.getString("JobLog.System.EXCEPTION"))>=0 //$NON-NLS-1$
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
		if (line.toUpperCase().indexOf(Messages.getString("JobLog.System.ERROR"))>=0 || //$NON-NLS-1$
		    line.toUpperCase().indexOf(Messages.getString("JobLog.System.EXCEPTION"))>=0 //$NON-NLS-1$
		    ) 
		{
			err.add(line);
		}
		
		if (err.size()>0)
		{
			String err_lines[] = new String[err.size()];
			for (i=0;i<err_lines.length;i++) err_lines[i] = err.get(i);
			
			EnterSelectionDialog esd = new EnterSelectionDialog(shell, err_lines, Messages.getString("JobLog.Dialog.ErrorLines.Title"), Messages.getString("JobLog.Dialog.ErrorLines.Message")); //$NON-NLS-1$ //$NON-NLS-2$
			line = esd.open();
			if (line!=null)
			{
				for (i=0;i<jobMeta.nrJobEntries();i++)
				{
					JobEntryCopy entryCopy = jobMeta.getJobEntry(i);
					if (line.indexOf( entryCopy.getName() ) >=0 )
					{
						spoon.editJobEntry(jobMeta, entryCopy );
					}
				}
				// System.out.println("Error line selected: "+line);
			}
		}
	}
    

    public void setJobHistoryRefresher(JobHistoryRefresher chefHistoryRefresher)
    {
        this.chefHistoryRefresher = chefHistoryRefresher;
    }

    public EngineMetaInterface getMeta() {
    	return jobMeta;
    }

    /**
     * @return the jobMeta
     * /
    public JobMeta getJobMeta()
    {
        return jobMeta;
    }

    /**
     * @param jobMeta the jobMeta to set
     */
    public void setJobMeta(JobMeta jobMeta)
    {
        this.jobMeta = jobMeta;
    }

    public boolean applyChanges()
    {
        return true;
    }

    public boolean canBeClosed()
    {
        return !isRunning;
    }

    public Object getManagedObject()
    {
        return jobMeta;
    }

    public boolean hasContentChanged()
    {
        return false;
    }

    public int showChangedWarning()
    {
        // show running error.
        MessageBox mb = new MessageBox(shell, SWT.YES | SWT.NO | SWT.ICON_QUESTION);
        mb.setMessage(Messages.getString("JobLog.Message.Warning.PromptExitWhenRunJob"));// There is a running job.  Do you want to stop it and quit Spoon?
        mb.setText(Messages.getString("System.Warning")); //Warning
        int answer = mb.open();
        if (answer==SWT.NO) return SWT.CANCEL;
        return answer;
    }
}
