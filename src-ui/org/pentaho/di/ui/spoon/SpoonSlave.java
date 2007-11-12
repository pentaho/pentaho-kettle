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
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.core.runtime.IProgressMonitor;
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
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.ui.spoon.Messages;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.TabItemInterface;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.trans.step.StepStatus;
import org.pentaho.di.ui.core.ConstUI;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TreeMemory;
import org.pentaho.di.ui.core.widget.TreeUtil;
import org.pentaho.di.www.SlaveServerJobStatus;
import org.pentaho.di.www.SlaveServerStatus;
import org.pentaho.di.www.SlaveServerTransStatus;
import org.pentaho.di.www.WebResult;

/**
 * SpoonSlave handles the display of the slave server information in a Spoon tab.
 *  
 * @see org.pentaho.di.spoon.Spoon
 * @author Matt
 * @since  12 nov 2006
 */
public class SpoonSlave extends Composite implements TabItemInterface
{
	public static final long UPDATE_TIME_VIEW = 30000L; // 30s
    
    public static final String STRING_SLAVE_LOG_TREE_NAME = "SLAVE_LOG : ";

	private Shell shell;
	private Display display;
    private SlaveServer slaveServer;
    private Spoon spoon;

	private ColumnInfo[] colinf;

	private Tree wTree;
	private Text wText;

	private Button wError;
    private Button wStart;
    private Button wStop;
    private Button wRefresh;

    private FormData fdTree, fdText, fdSash;
    
    private boolean refreshBusy;
    private SlaveServerStatus slaveServerStatus;
    private Timer timer;
    private TimerTask timerTask;

	public SpoonSlave(Composite parent, int style, final Spoon spoon, SlaveServer slaveServer)
	{
		super(parent, style);
		this.shell = parent.getShell();
        this.display = shell.getDisplay();
		this.spoon = spoon;
		this.slaveServer = slaveServer;

		FormLayout formLayout = new FormLayout();
		formLayout.marginWidth = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		setLayout(formLayout);

		setVisible(true);
		spoon.props.setLook(this);

		SashForm sash = new SashForm(this, SWT.VERTICAL);
		spoon.props.setLook(sash);

		sash.setLayout(new FillLayout());

		colinf = new ColumnInfo[] { 
                new ColumnInfo(Messages.getString("SpoonSlave.Column.Stepname"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
				new ColumnInfo(Messages.getString("SpoonSlave.Column.Copynr"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
				new ColumnInfo(Messages.getString("SpoonSlave.Column.Read"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
				new ColumnInfo(Messages.getString("SpoonSlave.Column.Written"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
				new ColumnInfo(Messages.getString("SpoonSlave.Column.Input"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
				new ColumnInfo(Messages.getString("SpoonSlave.Column.Output"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
				new ColumnInfo(Messages.getString("SpoonSlave.Column.Updated"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
                new ColumnInfo(Messages.getString("SpoonSlave.Column.Rejected"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
				new ColumnInfo(Messages.getString("SpoonSlave.Column.Errors"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
				new ColumnInfo(Messages.getString("SpoonSlave.Column.Active"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
				new ColumnInfo(Messages.getString("SpoonSlave.Column.Time"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
				new ColumnInfo(Messages.getString("SpoonSlave.Column.Speed"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
				new ColumnInfo(Messages.getString("SpoonSlave.Column.PriorityBufferSizes"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
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
		wRefresh.setText(Messages.getString("SpoonSlave.Button.Refresh"));
        wRefresh.setEnabled(true);
        wRefresh.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { refreshViewAndLog(); } });
        
		wError = new Button(this, SWT.PUSH);
		wError.setText(Messages.getString("SpoonSlave.Button.ShowErrorLines")); //$NON-NLS-1$
        wError.addSelectionListener( new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { showErrors(); } } );

        wStart= new Button(this, SWT.PUSH);
        wStart.setText(Messages.getString("SpoonSlave.Button.Start"));
        wStart.setEnabled(false);
        wStart.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { start(); } });

        wStop= new Button(this, SWT.PUSH);
        wStop.setText(Messages.getString("SpoonSlave.Button.Stop"));
        wStop.setEnabled(false);
        wStop.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { stop(); } });

        BaseStepDialog.positionBottomButtons(this, new Button[] { wRefresh, wStart, wStop, wError }, Const.MARGIN, null);
        
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
            
            // Search for the top level entry...
            //
            while (treeItem.getParentItem()!=null) treeItem = treeItem.getParentItem();
            
            int index = wTree.indexOf(treeItem);
            
            if (index<0) return;
            
            if (index<slaveServerStatus.getTransStatusList().size()) 
            {
            	// It's a transformation that we clicked on...
            	//
            	SlaveServerTransStatus transStatus = slaveServerStatus.getTransStatusList().get(index);
                stopEnabled = transStatus.isRunning();
                
                StringBuffer message = new StringBuffer();
                String errorDescription = transStatus.getErrorDescription();
                if (!Const.isEmpty(errorDescription))
                {
                    message.append(errorDescription).append(Const.CR).append(Const.CR);
                }
                
                if (!Const.isEmpty(transStatus.getLoggingString()))
                {
                    message.append(transStatus.getLoggingString()).append(Const.CR);
                }
                    
                wText.setText(message.toString());
                wText.setTopIndex(wText.getLineCount());
            }
            else
            {
            	index-=slaveServerStatus.getTransStatusList().size();
            	
            	// We clicked on a job line item
            	//
            	SlaveServerJobStatus jobStatus = slaveServerStatus.getJobStatusList().get(index);
                stopEnabled = jobStatus.isRunning();
                
                StringBuffer message = new StringBuffer();
                String errorDescription = jobStatus.getErrorDescription();
                if (!Const.isEmpty(errorDescription))
                {
                    message.append(errorDescription).append(Const.CR).append(Const.CR);
                }
                
                if (!Const.isEmpty(jobStatus.getLoggingString()))
                {
                    message.append(jobStatus.getLoggingString()).append(Const.CR);
                }
                    
                wText.setText(message.toString());
                wText.setTopIndex(wText.getLineCount());
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
            if (path.length==1) // transformation name
            {
                String transName = path[0];
                SlaveServerTransStatus transStatus = slaveServerStatus.findTransStatus(transName);
                if (transStatus!=null)
                {
                    if (!transStatus.isRunning())
                    {
                        try
                        {
                            WebResult webResult = slaveServer.startTransformation(transName);
                            if (!webResult.getResult().equalsIgnoreCase(WebResult.STRING_OK))
                            {
                                EnterTextDialog dialog = new EnterTextDialog(shell, Messages.getString("SpoonSlave.ErrorStartingTrans.Title"), Messages.getString("SpoonSlave.ErrorStartingTrans.Message"), webResult.getMessage());
                                dialog.setReadOnly();
                                dialog.open();
                            }
                        }
                        catch(Exception e)
                        {
                            new ErrorDialog(shell, Messages.getString("SpoonSlave.ErrorStartingTrans.Title"), Messages.getString("SpoonSlave.ErrorStartingTrans.Message"), e);
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
            if (path.length==1) // transformation name
            {
                String transName = path[0];
                SlaveServerTransStatus transStatus = slaveServerStatus.findTransStatus(transName);
                if (transStatus!=null)
                {
                    if (transStatus.isRunning())
                    {
                        try
                        {
                            WebResult webResult = slaveServer.stopTransformation(transName);
                            if (!webResult.getResult().equalsIgnoreCase(WebResult.STRING_OK))
                            {
                                EnterTextDialog dialog = new EnterTextDialog(shell, Messages.getString("SpoonSlave.ErrorStoppingTrans.Title"), Messages.getString("SpoonSlave.ErrorStoppingTrans.Message"), webResult.getMessage());
                                dialog.setReadOnly();
                                dialog.open();
                            }
                        }
                        catch(Exception e)
                        {
                            new ErrorDialog(shell, Messages.getString("SpoonSlave.ErrorStoppingTrans.Title"), Messages.getString("SpoonSlave.ErrorStoppingTrans.Message"), e);
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
        
        LogWriter.getInstance().logDetailed(Spoon.APP_NAME, "Refresh");
        
        wTree.removeAll();
        
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
        
        for (int i = 0; i < slaveServerStatus.getTransStatusList().size(); i++)
		{
            SlaveServerTransStatus transStatus =  slaveServerStatus.getTransStatusList().get(i);
            TreeItem transItem = new TreeItem(wTree, SWT.NONE);
            transItem.setText(0, transStatus.getTransName());
            transItem.setText(9, transStatus.getStatusDescription());
            transItem.setImage(GUIResource.getInstance().getImageTransGraph());
            
            try
            {
                LogWriter.getInstance().logDetailed(toString(), "Getting transformation status for [{0}] on server [{1}]", transStatus.getTransName(), slaveServer);
                SlaveServerTransStatus ts = slaveServer.getTransStatus(transStatus.getTransName());
                LogWriter.getInstance().logDetailed(toString(), "Finished receiving transformation status for [{0}] from server [{1}]", transStatus.getTransName(), slaveServer);
                List<StepStatus> stepStatusList = ts.getStepStatusList();
                transStatus.setStepStatusList(stepStatusList);
                transStatus.setLoggingString(ts.getLoggingString());
                
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
            TreeItem jobItem = new TreeItem(wTree, SWT.NONE);
            jobItem.setText(0, jobStatus.getJobName());
            jobItem.setText(9, jobStatus.getStatusDescription());
            jobItem.setImage(GUIResource.getInstance().getImageJobGraph());
            
            try
            {
                LogWriter.getInstance().logDetailed(toString(), "Getting job status for [{0}] on server [{1}]", jobStatus.getJobName(), slaveServer);
                SlaveServerJobStatus ts = slaveServer.getJobStatus(jobStatus.getJobName());
                LogWriter.getInstance().logDetailed(toString(), "Finished receiving job status for [{0}] from server [{1}]", jobStatus.getJobName(), slaveServer);
                jobStatus.setLoggingString(ts.getLoggingString());
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
				if (uLine.indexOf(Messages.getString("TransLog.System.ERROR")) >= 0 || //$NON-NLS-1$
						uLine.indexOf(Messages.getString("TransLog.System.EXCEPTION")) >= 0 || //$NON-NLS-1$
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
		if (uLine.indexOf(Messages.getString("TransLog.System.ERROR2")) >= 0 || //$NON-NLS-1$
				uLine.indexOf(Messages.getString("TransLog.System.EXCEPTION2")) >= 0 || //$NON-NLS-1$
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

			EnterSelectionDialog esd = new EnterSelectionDialog(shell, err_lines, Messages.getString("TransLog.Dialog.ErrorLines.Title"), Messages.getString("TransLog.Dialog.ErrorLines.Message")); //$NON-NLS-1$ //$NON-NLS-2$
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
		
			public boolean showReplaceWarning(Repository rep) {
				return false;
			}
		
			public void setModifiedUser(String user) {
			}
		
			public void setModifiedDate(Date date) {
			}
		
			public void setInternalKettleVariables() {
			}
		
			public void setID(long id) {
			}
		
			public void setFilename(String filename) {
			}
		
			public void setCreatedUser(String createduser) {
			}
		
			public void setCreatedDate(Date date) {
			}
		
			public boolean saveSharedObjects() {
				return false;
			}
		
			public void saveRep(Repository rep, IProgressMonitor monitor) throws KettleException {
			}
		
			public void nameFromFilename() {
			}
		
			public String getXML() {
				return null;
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
		
			public RepositoryDirectory getDirectory() {
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
		
		};
    }

}
