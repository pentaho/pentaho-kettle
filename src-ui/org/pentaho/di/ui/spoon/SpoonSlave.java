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

package org.pentaho.di.ui.spoon;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.ObjectRevision;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.trans.step.StepStatus;
import org.pentaho.di.ui.core.ConstUI;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.dialog.EnterNumberDialog;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TreeMemory;
import org.pentaho.di.ui.core.widget.TreeUtil;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.www.SlaveServerJobStatus;
import org.pentaho.di.www.SlaveServerStatus;
import org.pentaho.di.www.SlaveServerTransStatus;
import org.pentaho.di.www.SniffStepServlet;
import org.pentaho.di.www.WebResult;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * SpoonSlave handles the display of the slave server information in a Spoon tab.
 *  
 * @see org.pentaho.di.spoon.Spoon
 * @author Matt
 * @since  12 nov 2006
 */
public class SpoonSlave extends Composite implements TabItemInterface
{
	private static Class<?> PKG = Spoon.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	public static final long UPDATE_TIME_VIEW = 30000L; // 30s
    
	public static final String STRING_SLAVE_LOG_TREE_NAME = "SLAVE_LOG : ";

	private Shell shell;
	private Display display;
    private SlaveServer slaveServer;
    private Map<String, Integer> lastLineTransMap;
    private Map<String, Integer> lastLineJobMap;
    private Map<String, String> transLoggingMap;
    private Map<String, String> jobLoggingMap;
    
    private Spoon spoon;

	private ColumnInfo[] colinf;

	private Tree wTree;
	private Text wText;

	private Button wError;
    private Button wStart;
    private Button wStop;
    private Button wSniff;
    private Button wRefresh;

    private FormData fdTree, fdText, fdSash;
    
    private boolean refreshBusy;
    private SlaveServerStatus slaveServerStatus;
    private Timer timer;
    private TimerTask timerTask;

	private TreeItem transParentItem;

	private TreeItem jobParentItem;

	private LogChannelInterface	log;

	public SpoonSlave(Composite parent, int style, final Spoon spoon, SlaveServer slaveServer)
	{
		super(parent, style);
		this.shell = parent.getShell();
        this.display = shell.getDisplay();
		this.spoon = spoon;
		this.slaveServer = slaveServer;
		this.log = spoon.getLog();

		lastLineTransMap = new HashMap<String, Integer>();
		transLoggingMap = new HashMap<String, String>();
		lastLineJobMap = new HashMap<String, Integer>();
		jobLoggingMap = new HashMap<String, String>();
		
		FormLayout formLayout = new FormLayout();
		formLayout.marginWidth = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		setLayout(formLayout);

		setVisible(true);
		spoon.props.setLook(this);

		SashForm sash = new SashForm(this, SWT.VERTICAL);
		
		sash.setLayout(new FillLayout());

		colinf = new ColumnInfo[] { 
                new ColumnInfo(BaseMessages.getString(PKG, "SpoonSlave.Column.Stepname"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
				new ColumnInfo(BaseMessages.getString(PKG, "SpoonSlave.Column.Copynr"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
				new ColumnInfo(BaseMessages.getString(PKG, "SpoonSlave.Column.Read"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
				new ColumnInfo(BaseMessages.getString(PKG, "SpoonSlave.Column.Written"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
				new ColumnInfo(BaseMessages.getString(PKG, "SpoonSlave.Column.Input"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
				new ColumnInfo(BaseMessages.getString(PKG, "SpoonSlave.Column.Output"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
				new ColumnInfo(BaseMessages.getString(PKG, "SpoonSlave.Column.Updated"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
                new ColumnInfo(BaseMessages.getString(PKG, "SpoonSlave.Column.Rejected"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
				new ColumnInfo(BaseMessages.getString(PKG, "SpoonSlave.Column.Errors"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
				new ColumnInfo(BaseMessages.getString(PKG, "SpoonSlave.Column.Active"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
				new ColumnInfo(BaseMessages.getString(PKG, "SpoonSlave.Column.Time"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
				new ColumnInfo(BaseMessages.getString(PKG, "SpoonSlave.Column.Speed"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
				new ColumnInfo(BaseMessages.getString(PKG, "SpoonSlave.Column.PriorityBufferSizes"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
		};

		colinf[1].setAllignement(SWT.RIGHT);
        colinf[2].setAllignement(SWT.RIGHT);
		colinf[3].setAllignement(SWT.RIGHT);
		colinf[4].setAllignement(SWT.RIGHT);
		colinf[5].setAllignement(SWT.RIGHT);
		colinf[6].setAllignement(SWT.RIGHT);
		colinf[7].setAllignement(SWT.RIGHT);
		colinf[8].setAllignement(SWT.RIGHT);
		colinf[9].setAllignement(SWT.RIGHT);
		colinf[10].setAllignement(SWT.RIGHT);
		colinf[11].setAllignement(SWT.RIGHT);
		colinf[12].setAllignement(SWT.RIGHT);

		wTree = new Tree(sash, SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);
        wTree.setHeaderVisible(true);
        TreeMemory.addTreeListener(wTree, STRING_SLAVE_LOG_TREE_NAME+slaveServer.toString());
        Rectangle bounds = spoon.tabfolder.getSwtTabset().getBounds();
        for (int i=0;i<colinf.length;i++)
        {
            ColumnInfo columnInfo = colinf[i];
            TreeColumn treeColumn = new TreeColumn(wTree, columnInfo.getAllignement());
            treeColumn.setText(columnInfo.getName());
            treeColumn.setWidth(bounds.width/colinf.length);
        }
        wTree.addSelectionListener(new SelectionAdapter() {
        	@Override
        	public void widgetSelected(SelectionEvent e) {
                TreeItem ti[] = wTree.getSelection();
                if (ti.length==1)
                {
                    TreeItem treeItem = ti[0];
                    String[] path = ConstUI.getTreeStrings(treeItem);
                    
                    // Make sure we're positioned on a step
                    //
                    wSniff.setEnabled(path.length>2);
                }
        	}
		});

        transParentItem = new TreeItem(wTree, SWT.NONE);
        transParentItem.setText(Spoon.STRING_TRANSFORMATIONS);
        transParentItem.setImage(GUIResource.getInstance().getImageTransGraph());
        
        jobParentItem = new TreeItem(wTree, SWT.NONE);
        jobParentItem.setText(Spoon.STRING_JOBS);
        jobParentItem.setImage(GUIResource.getInstance().getImageJobGraph());
        
        wTree.addSelectionListener(new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent arg0)
                {
                    showLog();
                }
            }
        );
        
        
		wText = new Text(sash, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY | SWT.BORDER);
		spoon.props.setLook(wText);
		wText.setVisible(true);
        
		wRefresh = new Button(this, SWT.PUSH);
		wRefresh.setText(BaseMessages.getString(PKG, "SpoonSlave.Button.Refresh"));
        wRefresh.setEnabled(true);
        wRefresh.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { refreshViewAndLog(); } });
        
		wError = new Button(this, SWT.PUSH);
		wError.setText(BaseMessages.getString(PKG, "SpoonSlave.Button.ShowErrorLines")); //$NON-NLS-1$
        wError.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { showErrors(); } } );

        wSniff= new Button(this, SWT.PUSH);
        wSniff.setText(BaseMessages.getString(PKG, "SpoonSlave.Button.Sniff"));
        wSniff.setEnabled(false);
        wSniff.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { sniff(); } });

        wStart= new Button(this, SWT.PUSH);
        wStart.setText(BaseMessages.getString(PKG, "SpoonSlave.Button.Start"));
        wStart.setEnabled(false);
        wStart.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { start(); } });

        wStop= new Button(this, SWT.PUSH);
        wStop.setText(BaseMessages.getString(PKG, "SpoonSlave.Button.Stop"));
        wStop.setEnabled(false);
        wStop.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { stop(); } });

        BaseStepDialog.positionBottomButtons(this, new Button[] { wRefresh, wSniff, wStart, wStop, wError }, Const.MARGIN, null);
        
        // Put tree on top
        fdTree = new FormData();
        fdTree.left = new FormAttachment(0, 0);
        fdTree.top = new FormAttachment(0, 0);
        fdTree.right = new FormAttachment(100, 0);
        fdTree.bottom = new FormAttachment(100, 0);
        wTree.setLayoutData(fdTree);

        
		// Put text in the middle
		fdText = new FormData();
		fdText.left = new FormAttachment(0, 0);
		fdText.top = new FormAttachment(0, 0);
		fdText.right = new FormAttachment(100, 0);
		fdText.bottom = new FormAttachment(100, 0);
		wText.setLayoutData(fdText);

		fdSash = new FormData();
		fdSash.left = new FormAttachment(0, 0); // First one in the left top corner
		fdSash.top = new FormAttachment(0, 0);
		fdSash.right = new FormAttachment(100, 0);
		fdSash.bottom = new FormAttachment(wRefresh, -5);
		sash.setLayoutData(fdSash);

		pack();


		timer = new Timer("SpoonSlave: " + getMeta().getName());
        
        timerTask = new TimerTask()
        {
            public void run()
            {
                if (display != null && !display.isDisposed())
                {
                    display.asyncExec(
                        new Runnable()
                        {
                            public void run()
                            {
                                refreshViewAndLog();
                            }
                        }
                    );
                }
            }
        };

        timer.schedule(timerTask, 0L, UPDATE_TIME_VIEW); // schedule to repeat a couple of times per second to get fast feedback 

		addDisposeListener(new DisposeListener() { public void widgetDisposed(DisposeEvent e) { timer.cancel(); } } );
	}
    
    protected void refreshViewAndLog()
    {
        String[] selectionPath = null;
        if (wTree.getSelectionCount()==1)
        {
            selectionPath = ConstUI.getTreeStrings( wTree.getSelection()[0] );
        }

        refreshView(); 

        if (selectionPath!=null) // Select the same one again
        {
            TreeItem treeItem = TreeUtil.findTreeItem(wTree, selectionPath);
            if (treeItem!=null) wTree.setSelection(treeItem);
        }
        
        showLog();
    }

    public boolean canBeClosed()
    {
        // It's OK to close this at any time.
        // We just have to make sure we stop the timers etc.
        //
        timerTask.cancel();
        timer.cancel();
        spoon.tabfolder.setSelected(0);
        return true; 
    }

    /**
     * Someone clicks on a line: show the log or error message associated with that in the text-box
     */
    public void showLog()
    {
        boolean stopEnabled=false;
        TreeItem ti[] = wTree.getSelection();
        if (ti.length==1)
        {
        	TreeItem treeItem = ti[0];
        	String[] path = ConstUI.getTreeStrings(treeItem);
        	
        	if (path.length<=1) {
        		return;
        	}
        	
            if (path[0].equals(transParentItem.getText())) 
            {
            	// It's a transformation that we clicked on...
            	//
            	SlaveServerTransStatus transStatus = slaveServerStatus.findTransStatus(path[1]);
                stopEnabled = transStatus.isRunning();
                
                StringBuffer message = new StringBuffer();
                String errorDescription = transStatus.getErrorDescription();
                if (!Const.isEmpty(errorDescription))
                {
                    message.append(errorDescription).append(Const.CR).append(Const.CR);
                }

                String logging = transLoggingMap.get(transStatus.getTransName());
                if (!Const.isEmpty(logging))
                {
                    message.append(logging).append(Const.CR);
                }
                    
                wText.setText(message.toString());
                wText.setSelection(wText.getText().length());
                wText.showSelection();
                // wText.setTopIndex(wText.getLineCount());
            }

            if (path[0].equals(jobParentItem.getText()))
            {
            	// We clicked on a job line item
            	//
            	SlaveServerJobStatus jobStatus = slaveServerStatus.findJobStatus(path[1]);
                stopEnabled = jobStatus.isRunning();
                
                StringBuffer message = new StringBuffer();
                String errorDescription = jobStatus.getErrorDescription();
                if (!Const.isEmpty(errorDescription))
                {
                    message.append(errorDescription).append(Const.CR).append(Const.CR);
                }
                
                String logging = jobLoggingMap.get(jobStatus.getJobName());
                if (!Const.isEmpty(logging))
                {
                    message.append(logging).append(Const.CR);
                }
                    
                wText.setText(message.toString());
                wText.setSelection(wText.getText().length());
                wText.showSelection();
                // wText.setTopIndex(wText.getLineCount());
            }
        }
        wStop.setEnabled(stopEnabled);
        wStart.setEnabled(!stopEnabled);
    }
    
    
    protected void start()
    {
        TreeItem ti[] = wTree.getSelection();
        if (ti.length==1)
        {
            TreeItem treeItem = ti[0];
            String[] path = ConstUI.getTreeStrings(treeItem);
            if (path.length<=1) {
            	return;
            }
            
            if (path[0].equals(transParentItem.getText())) 
            {
            	SlaveServerTransStatus transStatus = slaveServerStatus.findTransStatus(path[1]);
            	if (transStatus!=null)
            	{
            		if (!transStatus.isRunning())
            		{
            			try
            			{
            				WebResult webResult = slaveServer.startTransformation(path[1]);
            				if (!webResult.getResult().equalsIgnoreCase(WebResult.STRING_OK))
            				{
            					EnterTextDialog dialog = new EnterTextDialog(shell, BaseMessages.getString(PKG, "SpoonSlave.ErrorStartingTrans.Title"), BaseMessages.getString(PKG, "SpoonSlave.ErrorStartingTrans.Message"), webResult.getMessage());
            					dialog.setReadOnly();
            					dialog.open();
            				}
            			}
            			catch(Exception e)
            			{
            				new ErrorDialog(shell, BaseMessages.getString(PKG, "SpoonSlave.ErrorStartingTrans.Title"), BaseMessages.getString(PKG, "SpoonSlave.ErrorStartingTrans.Message"), e);
            			}
            		}
            	}
            }
            
            if (path[0].equals(jobParentItem.getText())) 
            {
            	SlaveServerJobStatus jobStatus = slaveServerStatus.findJobStatus(path[1]);
            	if (jobStatus!=null)
            	{
            		if (!jobStatus.isRunning())
            		{
            			try
            			{
            				WebResult webResult = slaveServer.startJob(path[1]);
            				if (!webResult.getResult().equalsIgnoreCase(WebResult.STRING_OK))
            				{
            					EnterTextDialog dialog = new EnterTextDialog(shell, BaseMessages.getString(PKG, "SpoonSlave.ErrorStartingJob.Title"), BaseMessages.getString(PKG, "SpoonSlave.ErrorStartingJob.Message"), webResult.getMessage());
            					dialog.setReadOnly();
            					dialog.open();
            				}
            			}
            			catch(Exception e)
            			{
            				new ErrorDialog(shell, BaseMessages.getString(PKG, "SpoonSlave.ErrorStartingJob.Title"), BaseMessages.getString(PKG, "SpoonSlave.ErrorStartingJob.Message"), e);
            			}
            		}
            	}
            }
        }
    }

    protected void stop()
    {
        TreeItem ti[] = wTree.getSelection();
        if (ti.length==1)
        {
            TreeItem treeItem = ti[0];
            String[] path = ConstUI.getTreeStrings(treeItem);
            if (path.length<=1) {
            	return;
            }
            
            String name = path[1];
            
            if (path[0].equals(transParentItem.getText())) 
            {
            	SlaveServerTransStatus transStatus = slaveServerStatus.findTransStatus(name);
            	if (transStatus!=null)
            	{
            		if (transStatus.isRunning())
            		{
            			try
            			{
            				WebResult webResult = slaveServer.stopTransformation(name);
            				if (!webResult.getResult().equalsIgnoreCase(WebResult.STRING_OK))
            				{
            					EnterTextDialog dialog = new EnterTextDialog(shell, BaseMessages.getString(PKG, "SpoonSlave.ErrorStoppingTrans.Title"), BaseMessages.getString(PKG, "SpoonSlave.ErrorStoppingTrans.Message"), webResult.getMessage());
            					dialog.setReadOnly();
            					dialog.open();
            				}
            			}
            			catch(Exception e)
            			{
            				new ErrorDialog(shell, BaseMessages.getString(PKG, "SpoonSlave.ErrorStoppingTrans.Title"), BaseMessages.getString(PKG, "SpoonSlave.ErrorStoppingTrans.Message"), e);
            			}
            		}
            	}
            }
            
            if (path[0].equals(jobParentItem.getText())) 
            {
            	SlaveServerJobStatus jobStatus = slaveServerStatus.findJobStatus(name);
            	if (jobStatus!=null)
            	{
            		if (jobStatus.isRunning())
            		{
            			try
            			{
            				WebResult webResult = slaveServer.stopJob(name);
            				if (!webResult.getResult().equalsIgnoreCase(WebResult.STRING_OK))
            				{
            					EnterTextDialog dialog = new EnterTextDialog(shell, BaseMessages.getString(PKG, "SpoonSlave.ErrorStoppingJob.Title"), BaseMessages.getString(PKG, "SpoonSlave.ErrorStoppingJob.Message"), webResult.getMessage());
            					dialog.setReadOnly();
            					dialog.open();
            				}
            			}
            			catch(Exception e)
            			{
            				new ErrorDialog(shell, BaseMessages.getString(PKG, "SpoonSlave.ErrorStoppingJob.Title"), BaseMessages.getString(PKG, "SpoonSlave.ErrorStoppingJob.Message"), e);
            			}
            		}
            	}
            }
        }
    }

	private synchronized void refreshView()
	{
  		if (wTree.isDisposed()) return;
		if (refreshBusy) return;
		refreshBusy = true;
        
        log.logDetailed("Refresh");
        
        transParentItem.removeAll();
        jobParentItem.removeAll();
        
        // Determine the transformations on the slave servers
        try
        {
            slaveServerStatus = slaveServer.getStatus();
        }
        catch(Exception e)
        {
            slaveServerStatus = new SlaveServerStatus("Error contacting server");
            slaveServerStatus.setErrorDescription(Const.getStackTracker(e));
            wText.setText(slaveServerStatus.getErrorDescription());
        }
        
        List<SlaveServerTransStatus> transStatusList = slaveServerStatus.getTransStatusList();
        for (int i = 0; i < transStatusList.size(); i++)
		{
            SlaveServerTransStatus transStatus =  transStatusList.get(i);
            TreeItem transItem = new TreeItem(transParentItem, SWT.NONE);
            transItem.setText(0, transStatus.getTransName());
            transItem.setText(9, transStatus.getStatusDescription());
            transItem.setImage(GUIResource.getInstance().getImageTransGraph());
            
            try
            {
                log.logDetailed("Getting transformation status for [{0}] on server [{1}]", transStatus.getTransName(), slaveServer);
                
                Integer lastLine = lastLineTransMap.get(transStatus.getTransName());
                int lastLineNr = lastLine==null ? 0 : lastLine.intValue();
                
                SlaveServerTransStatus ts = slaveServer.getTransStatus(transStatus.getTransName(), lastLineNr);
                log.logDetailed("Finished receiving transformation status for [{0}] from server [{1}]", transStatus.getTransName(), slaveServer);
                List<StepStatus> stepStatusList = ts.getStepStatusList();
                transStatus.setStepStatusList(stepStatusList);
                
                lastLineTransMap.put(transStatus.getTransName(), transStatus.getLastLoggingLineNr());
                String logging = transLoggingMap.get(transStatus.getTransName());
                if (logging==null) {
                	logging = ts.getLoggingString();
                } else {
                	logging = new StringBuffer(logging).append(ts.getLoggingString()).toString();
                }
                transLoggingMap.put(transStatus.getTransName(), logging);
                
                for (int s=0;s<stepStatusList.size();s++)
                {
                    StepStatus stepStatus = stepStatusList.get(s);
                    TreeItem stepItem = new TreeItem(transItem, SWT.NONE);
                    stepItem.setText(stepStatus.getSpoonSlaveLogFields());
                }
            }
            catch (Exception e)
            {
                transStatus.setErrorDescription("Unable to access transformation details : "+Const.CR+Const.getStackTracker(e));
            } 
		}
        
        for (int i = 0; i < slaveServerStatus.getJobStatusList().size(); i++)
		{
            SlaveServerJobStatus jobStatus =  slaveServerStatus.getJobStatusList().get(i);
            TreeItem jobItem = new TreeItem(jobParentItem, SWT.NONE);
            jobItem.setText(0, jobStatus.getJobName());
            jobItem.setText(9, jobStatus.getStatusDescription());
            jobItem.setImage(GUIResource.getInstance().getImageJobGraph());
            
            try
            {
                log.logDetailed("Getting job status for [{0}] on server [{1}]", jobStatus.getJobName(), slaveServer);
                
                Integer lastLine = lastLineJobMap.get(jobStatus.getJobName());
                int lastLineNr = lastLine==null ? 0 : lastLine.intValue();

                SlaveServerJobStatus ts = slaveServer.getJobStatus(jobStatus.getJobName(), lastLineNr);
                
                log.logDetailed("Finished receiving job status for [{0}] from server [{1}]", jobStatus.getJobName(), slaveServer);

                lastLineJobMap.put(jobStatus.getJobName(), jobStatus.getLastLoggingLineNr());
                String logging = jobLoggingMap.get(jobStatus.getJobName());
                if (logging==null) {
                	logging = ts.getLoggingString();
                } else {
                	logging = new StringBuffer(logging).append(ts.getLoggingString()).toString();
                }
                jobLoggingMap.put(jobStatus.getJobName(), logging);
                
                Result result = ts.getResult();
                if (result!=null)
                {
	                jobItem.setText(2, ""+result.getNrLinesRead());
	                jobItem.setText(3, ""+result.getNrLinesWritten());
	                jobItem.setText(4, ""+result.getNrLinesInput());
	                jobItem.setText(5, ""+result.getNrLinesOutput());
	                jobItem.setText(6, ""+result.getNrLinesUpdated());
	                jobItem.setText(7, ""+result.getNrLinesRejected());
	                jobItem.setText(8, ""+result.getNrErrors());
                }
            }
            catch (Exception e)
            {
                jobStatus.setErrorDescription("Unable to access transformation details : "+Const.CR+Const.getStackTracker(e));
            } 

		}
        
        TreeMemory.setExpandedFromMemory(wTree, STRING_SLAVE_LOG_TREE_NAME+slaveServer.toString());
        TreeUtil.setOptimalWidthOnColumns(wTree);

        
        
		refreshBusy = false;
	}


	public void showErrors()
	{
		String all = wText.getText();
		List<String> err = new ArrayList<String>();

		int i = 0;
		int startpos = 0;
		int crlen = Const.CR.length();

		while (i < all.length() - crlen)
		{
			if (all.substring(i, i + crlen).equalsIgnoreCase(Const.CR))
			{
				String line = all.substring(startpos, i);
				String uLine = line.toUpperCase();
				if (uLine.indexOf(BaseMessages.getString(PKG, "TransLog.System.ERROR")) >= 0 || //$NON-NLS-1$
						uLine.indexOf(BaseMessages.getString(PKG, "TransLog.System.EXCEPTION")) >= 0 || //$NON-NLS-1$
						uLine.indexOf("ERROR") >= 0 || // i18n for compatibilty to non translated steps a.s.o. //$NON-NLS-1$ 
						uLine.indexOf("EXCEPTION") >= 0 // i18n for compatibilty to non translated steps a.s.o. //$NON-NLS-1$
				)
				{
					err.add(line);
				}
				// New start of line
				startpos = i + crlen;
			}

			i++;
		}
		String line = all.substring(startpos);
		String uLine = line.toUpperCase();
		if (uLine.indexOf(BaseMessages.getString(PKG, "TransLog.System.ERROR2")) >= 0 || //$NON-NLS-1$
				uLine.indexOf(BaseMessages.getString(PKG, "TransLog.System.EXCEPTION2")) >= 0 || //$NON-NLS-1$
				uLine.indexOf("ERROR") >= 0 || // i18n for compatibilty to non translated steps a.s.o. //$NON-NLS-1$ 
				uLine.indexOf("EXCEPTION") >= 0 // i18n for compatibilty to non translated steps a.s.o. //$NON-NLS-1$
		)
		{
			err.add(line);
		}

		if (err.size() > 0)
		{
			String err_lines[] = new String[err.size()];
			for (i = 0; i < err_lines.length; i++)
				err_lines[i] = err.get(i);

			EnterSelectionDialog esd = new EnterSelectionDialog(shell, err_lines, BaseMessages.getString(PKG, "TransLog.Dialog.ErrorLines.Title"), BaseMessages.getString(PKG, "TransLog.Dialog.ErrorLines.Message")); //$NON-NLS-1$ //$NON-NLS-2$
			line = esd.open();
            /*
             * TODO: we have multiple transformation we can go to: which one should we pick?
			if (line != null)
			{
				for (i = 0; i < spoon.getTransMeta().nrSteps(); i++)
				{
					StepMeta stepMeta = spoon.getTransMeta().getStep(i);
					if (line.indexOf(stepMeta.getName()) >= 0)
					{
						spoon.editStep(stepMeta.getName());
					}
				}
				// System.out.println("Error line selected: "+line);
			}
            */
		}
	}

	public String toString()
	{
		return Spoon.APP_NAME;
	}
        
    public Object getManagedObject()
    {
        return slaveServer;
    }
    
    public boolean hasContentChanged()
    {
        return false;
    }

    public boolean applyChanges()
    {
        return true;
    }

    public int showChangedWarning()
    {
        return SWT.YES;
    }
    
    public EngineMetaInterface getMeta() {
    	return new EngineMetaInterface() {
		
    		public void setModifiedUser(String user) {
			}
		
			public void setModifiedDate(Date date) {
			}
		
			public void setInternalKettleVariables() {
			}
		
			public void setObjectId(ObjectId id) {
			}
		
			public void setFilename(String filename) {
			}
		
			public void setCreatedUser(String createduser) {
			}
		
			public void setCreatedDate(Date date) {
			}
		
			public void saveSharedObjects() {
			}
		
			public void nameFromFilename() {
			}
		
			public String getXML() {
				return null;
			}
			
			public boolean canSave() {
		     return true;
		  }			
		
			public String getName() {
				return slaveServer.getName();
			}
		
			public String getModifiedUser() {
				return null;
			}
		
			public Date getModifiedDate() {
				return null;
			}
		
			public String[] getFilterNames() {
				return null;
			}
		
			public String[] getFilterExtensions() {
				return null;
			}
		
			public String getFilename() {
				return null;
			}
		
			public String getFileType() {
				return null;
			}
		
			public RepositoryDirectory getRepositoryDirectory() {
				return null;
			}
		
			public String getDefaultExtension() {
				return null;
			}
		
			public String getCreatedUser() {
				return null;
			}
		
			public Date getCreatedDate() {
				return null;
			}
		
			public void clearChanged() {
			}

			public ObjectId getObjectId() {
				return null;
			}

			public RepositoryObjectType getRepositoryElementType() {
				return null;
			}

			public void setName(String name) {
			}

			public void setRepositoryDirectory(RepositoryDirectory repositoryDirectory) {
			}

			public String getDescription() {
				return null;
			}
			
			public void setDescription(String description) {
			}
			
			public ObjectRevision getObjectRevision() {
				return null;
			}
			
			public void setObjectRevision(ObjectRevision objectRevision) {
			}
		};
    }

	public void setControlStates() {
		// TODO Auto-generated method stub
		
	}
	
	public boolean canHandleSave(){
	  return false;
	}
	
    protected void sniff()
    {
        TreeItem ti[] = wTree.getSelection();
        if (ti.length==1)
        {
            TreeItem treeItem = ti[0];
            String[] path = ConstUI.getTreeStrings(treeItem);
            
            // Make sure we're positioned on a step
            //
            if (path.length<=2) {
            	return;
            }
            
            String name = path[1];
            String step = path[2];
            String copy = treeItem.getText(1);
            
            EnterNumberDialog numberDialog = new EnterNumberDialog(shell, 
            		PropsUI.getInstance().getDefaultPreviewSize(), 
            		BaseMessages.getString(PKG, "SpoonSlave.SniffSizeQuestion.Title"), 
            		BaseMessages.getString(PKG, "SpoonSlave.SniffSizeQuestion.Message")
            	);
            int lines = numberDialog.open();
            if (lines<=0) {
            	return;
            }
            
            EnterSelectionDialog selectionDialog = new EnterSelectionDialog(shell, new String[] { SniffStepServlet.TYPE_INPUT, SniffStepServlet.TYPE_OUTPUT, }, 
            		BaseMessages.getString(PKG, "SpoonSlave.SniffTypeQuestion.Title"), 
            		BaseMessages.getString(PKG, "SpoonSlave.SniffTypeQuestion.Message")
            	);
            String type = selectionDialog.open(1);
            if (type==null) {
            	return;
            }
            
            try {
	            String xml = slaveServer.sniffStep(name, step, copy, lines, type);
	            
	            Document doc = XMLHandler.loadXMLString(xml);
	            Node node = XMLHandler.getSubNode(doc, SniffStepServlet.XML_TAG);
	            Node metaNode = XMLHandler.getSubNode(node, RowMeta.XML_META_TAG);
	            RowMetaInterface rowMeta = new RowMeta(metaNode);
	            
	            int nrRows = Const.toInt(XMLHandler.getTagValue(node, "nr_rows"), 0);
	            List<Object[]> rowBuffer = new ArrayList<Object[]>();
	            for (int i=0;i<nrRows;i++) {
	            	Node dataNode = XMLHandler.getSubNodeByNr(node, RowMeta.XML_DATA_TAG, i);
	            	Object[] row = rowMeta.getRow(dataNode);
	            	rowBuffer.add(row);
	            }
	            
	            PreviewRowsDialog prd = new PreviewRowsDialog(shell, new Variables(), SWT.NONE, step, rowMeta, rowBuffer);
	            prd.open();
            } catch(Exception e) {
				new ErrorDialog(shell, BaseMessages.getString(PKG, "SpoonSlave.ErrorSniffingStep.Title"), BaseMessages.getString(PKG, "SpoonSlave.ErrorSniffingStep.Message"), e);
            }
        }
    }

    public ChangedWarningInterface getChangedWarning() {
      return null;  
    }
}
