/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */
package org.pentaho.di.ui.spoon.job;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.gui.JobTracker;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobEntryResult;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.TreeMemory;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.delegates.SpoonDelegate;

public class JobGridDelegate extends SpoonDelegate {
	
	private static Class<?> PKG = JobGraph.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	public static final long REFRESH_TIME = 100L;
    public static final long UPDATE_TIME_VIEW = 1000L;
	private static final String STRING_CHEF_LOG_TREE_NAME = "Job Log Tree";
    
	private JobGraph jobGraph;
	private CTabItem jobGridTab;
	private Tree wTree;

	public JobTracker jobTracker;
    public int previousNrItems;
    
    private int nrRow=0;
	
	/**
	 * @param spoon
	 * @param transGraph
	 */
	public JobGridDelegate(Spoon spoon, JobGraph transGraph) {
		super(spoon);
		this.jobGraph = transGraph;
	}
	
	/**
	 *  Add a grid with the execution metrics per step in a table view
	 *  
	 */ 
	public void addJobGrid() {

		// First, see if we need to add the extra view...
		//
		if (jobGraph.extraViewComposite==null || jobGraph.extraViewComposite.isDisposed()) {
			jobGraph.addExtraView();
		} else {
			if (jobGridTab!=null && !jobGridTab.isDisposed()) {
				// just set this one active and get out...
				//
				jobGraph.extraViewTabFolder.setSelection(jobGridTab);
				return; 
			}
		}

		jobGridTab = new CTabItem(jobGraph.extraViewTabFolder, SWT.NONE);
		jobGridTab.setImage(GUIResource.getInstance().getImageShowGrid());
		jobGridTab.setText(BaseMessages.getString(PKG, "Spoon.TransGraph.GridTab.Name"));

		addControls();
		
		
		jobGridTab.setControl(wTree);

		jobGraph.extraViewTabFolder.setSelection(jobGridTab);		
	}
	
	/**
	 * Add the controls to the tab
	 */
	private void addControls() {
		
		// Create the tree table...
		wTree = new Tree(jobGraph.extraViewTabFolder, SWT.V_SCROLL | SWT.H_SCROLL);
        wTree.setHeaderVisible(true);
        TreeMemory.addTreeListener(wTree, STRING_CHEF_LOG_TREE_NAME);
        
        TreeColumn column1 = new TreeColumn(wTree, SWT.LEFT);
        column1.setText(BaseMessages.getString(PKG, "JobLog.Column.JobJobEntry")); //$NON-NLS-1$
        column1.setWidth(200);
        
        TreeColumn column2 = new TreeColumn(wTree, SWT.LEFT);
        column2.setText(BaseMessages.getString(PKG, "JobLog.Column.Comment")); //$NON-NLS-1$
        column2.setWidth(200);
        
        TreeColumn column3 = new TreeColumn(wTree, SWT.LEFT);
        column3.setText(BaseMessages.getString(PKG, "JobLog.Column.Result")); //$NON-NLS-1$
        column3.setWidth(100);

        TreeColumn column4 = new TreeColumn(wTree, SWT.LEFT);
        column4.setText(BaseMessages.getString(PKG, "JobLog.Column.Reason")); //$NON-NLS-1$
        column4.setWidth(200);

        TreeColumn column5 = new TreeColumn(wTree, SWT.LEFT);
        column5.setText(BaseMessages.getString(PKG, "JobLog.Column.Filename")); //$NON-NLS-1$
        column5.setWidth(200);

        TreeColumn column6 = new TreeColumn(wTree, SWT.RIGHT);
        column6.setText(BaseMessages.getString(PKG, "JobLog.Column.Nr")); //$NON-NLS-1$
        column6.setWidth(50);

        TreeColumn column7 = new TreeColumn(wTree, SWT.RIGHT);
        column7.setText(BaseMessages.getString(PKG, "JobLog.Column.LogDate")); //$NON-NLS-1$
        column7.setWidth(120);

		FormData fdTree=new FormData();
		fdTree.left   = new FormAttachment(0, 0);
		fdTree.top    = new FormAttachment(0, 0);
		fdTree.right  = new FormAttachment(100, 0);
		fdTree.bottom = new FormAttachment(100, 0);
		wTree.setLayoutData(fdTree);
		
		
		final Timer tim = new Timer("JobGrid: " + jobGraph.getMeta().getName());
		TimerTask timtask = 
			new TimerTask() 
			{
				public void run() 
				{
					Display display = jobGraph.getDisplay();
					if (display!=null && !display.isDisposed())
					display.asyncExec(
						new Runnable() 
						{
							public void run() 
							{
								// Check if the widgets are not disposed.  
								// This happens is the rest of the window is not yet disposed.
								// We ARE running in a different thread after all.
								//
								// TODO: add a "auto refresh" check box somewhere
								if (!wTree.isDisposed())
								{
    								refreshTreeTable();
								}
							}
						}
					);
				}
			};
		tim.schedule( timtask, 10L, 10L);// refresh every 2 seconds... 
		
        jobGraph.jobLogDelegate.getJobLogTab().addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent disposeEvent) {
				tim.cancel();
			}
		});

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
                    else jobName = BaseMessages.getString(PKG, "JobLog.Tree.StringToDisplayWhenJobHasNoName"); //$NON-NLS-1$
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
                if(nrRow%2!=0) treeItem.setBackground(GUIResource.getInstance().getColorBlueCustomGrid());
                nrRow++;
                if (jobTracker.nrJobTrackers()>0)
                {
                    // This is a sub-job: display the name at the top of the list...
                    treeItem.setText( 0, BaseMessages.getString(PKG, "JobLog.Tree.JobPrefix")+jobTracker.getJobName() ); //$NON-NLS-1$
                    
                    // then populate the sub-job entries ...
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
                        String jobEntryName = result.getJobEntryName();
                        if (!Const.isEmpty(jobEntryName))
                        {
                            treeItem.setText( 0, jobEntryName );
                            treeItem.setText( 4, Const.NVL( result.getJobEntryFilename(), "") );
                        }
                        else
                        {
                            treeItem.setText( 0, BaseMessages.getString(PKG, "JobLog.Tree.JobPrefix2")+jobTracker.getJobName()); //$NON-NLS-1$
                        }
                        String comment = result.getComment();
                        if (comment!=null)
                        {
                            treeItem.setText(1, comment);
                        }
                        Result res = result.getResult();
                        if (res!=null)
                        {
                            treeItem.setText(2, res.getResult()?BaseMessages.getString(PKG, "JobLog.Tree.Success"):BaseMessages.getString(PKG, "JobLog.Tree.Failure")); //$NON-NLS-1$ //$NON-NLS-2$
                            treeItem.setText(5, Long.toString(res.getEntryNr())); //$NON-NLS-1$
                            if(res.getResult())
                            {
                            	treeItem.setForeground(GUIResource.getInstance().getColorSuccessGreen());
                            }
                            else
                            {
                            	treeItem.setForeground(GUIResource.getInstance().getColorRed());
                            }
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
            log.logError(Const.getStackTracker(e));
        }
    }

	public CTabItem getJobGridTab() {
		return jobGridTab;
	}

	public void setJobTracker(JobTracker jobTracker) {
		this.jobTracker = jobTracker;
		
	}

	

}
