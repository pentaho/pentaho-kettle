package org.pentaho.di.ui.spoon.job;

import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.CentralLogStore;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.XulHelper;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.delegates.SpoonDelegate;
import org.pentaho.di.ui.spoon.trans.LogBrowser;
import org.pentaho.xul.swt.toolbar.Toolbar;
import org.pentaho.xul.toolbar.XulToolbarButton;

public class JobLogDelegate extends SpoonDelegate {
	private static Class<?> PKG = JobGraph.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private static final String XUL_FILE_TRANS_LOG_TOOLBAR = "ui/job-log-toolbar.xul";
	public static final String XUL_FILE_TRANS_LOG_TOOLBAR_PROPERTIES = "ui/job-log-toolbar.properties";

	private JobGraph jobGraph;

	private CTabItem jobLogTab;
	
	public  StyledText jobLogText;
	
    /**
     * The number of lines in the log tab
     */
	// private int textSize;

	private Composite jobLogComposite;
	private Toolbar toolbar;

	private LogBrowser	logBrowser;
	
	/**
	 * @param spoon
	 */
	public JobLogDelegate(Spoon spoon, JobGraph jobGraph) {
		super(spoon);
		this.jobGraph = jobGraph;
	}
	
	public void addJobLog() {
		// First, see if we need to add the extra view...
		//
		if (jobGraph.extraViewComposite==null || jobGraph.extraViewComposite.isDisposed()) {
			jobGraph.addExtraView();
		} else {
			if (jobLogTab!=null && !jobLogTab.isDisposed()) {
				// just set this one active and get out...
				//
				jobGraph.extraViewTabFolder.setSelection(jobLogTab);
				return; 
			}
		}
		
		// Add a transLogTab : display the logging...
		//
		jobLogTab = new CTabItem(jobGraph.extraViewTabFolder, SWT.NONE);
		jobLogTab.setImage(GUIResource.getInstance().getImageShowLog());
		jobLogTab.setText(BaseMessages.getString(PKG, "JobGraph.LogTab.Name"));

		jobLogComposite = new Composite(jobGraph.extraViewTabFolder, SWT.NONE);
		jobLogComposite.setLayout(new FormLayout());

		addToolBar();
		addToolBarListeners();
		
		jobLogText = new StyledText(jobLogComposite, SWT.READ_ONLY | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		spoon.props.setLook(jobLogText);
		FormData fdText = new FormData();
		fdText.left = new FormAttachment(0,0);
		fdText.right = new FormAttachment(100,0);
		fdText.top = new FormAttachment((Control)toolbar.getNativeObject(),0);
		fdText.bottom = new FormAttachment(100,0);
		jobLogText.setLayoutData(fdText);

		logBrowser = new LogBrowser(jobLogText, jobGraph);
		logBrowser.installLogSniffer();
		
		// If the job is closed, we should dispose of all the logging information in the buffer and registry for it
		//
		jobGraph.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				if (jobGraph.job!=null) {
					CentralLogStore.discardLines(jobGraph.job.getLogChannelId(), true);
				}
			}
		});

		jobLogTab.setControl(jobLogComposite);


		
		jobGraph.extraViewTabFolder.setSelection(jobLogTab);
	}

    private void addToolBar()
	{

		try {
			toolbar = XulHelper.createToolbar(XUL_FILE_TRANS_LOG_TOOLBAR, jobLogComposite, JobLogDelegate.this, new XulMessages());
			
			// Add a few default key listeners
			//
			ToolBar toolBar = (ToolBar) toolbar.getNativeObject();
			
			addToolBarListeners();
	        toolBar.layout(true, true);
		} catch (Throwable t ) {
			log.logError(Const.getStackTracker(t));
			new ErrorDialog(jobLogComposite.getShell(), BaseMessages.getString(PKG, "Spoon.Exception.ErrorReadingXULFile.Title"), BaseMessages.getString(PKG, "Spoon.Exception.ErrorReadingXULFile.Message", XUL_FILE_TRANS_LOG_TOOLBAR), new Exception(t));
		}
	}

	public void addToolBarListeners()
	{
		try
		{
			// first get the XML document
			URL url = XulHelper.getAndValidate(XUL_FILE_TRANS_LOG_TOOLBAR_PROPERTIES);
			Properties props = new Properties();
			props.load(url.openStream());
			String ids[] = toolbar.getMenuItemIds();
			for (int i = 0; i < ids.length; i++)
			{
				String methodName = (String) props.get(ids[i]);
				if (methodName != null)
				{
					toolbar.addMenuListener(ids[i], this, methodName);

				}
			}

		} catch (Throwable t ) {
			t.printStackTrace();
			new ErrorDialog(jobLogComposite.getShell(), BaseMessages.getString(PKG, "Spoon.Exception.ErrorReadingXULFile.Title"), 
					BaseMessages.getString(PKG, "Spoon.Exception.ErrorReadingXULFile.Message", XUL_FILE_TRANS_LOG_TOOLBAR_PROPERTIES), new Exception(t));
		}
	}

	public void clearLog()
	{
		if (jobLogText!=null && !jobLogText.isDisposed()) jobLogText.setText(""); //$NON-NLS-1$
	}
	
	public void showLogSettings() {
		spoon.setLog();
	}

	public void showErrors()
	{
		String all = jobLogText.getText();
		ArrayList<String> err = new ArrayList<String>();
		
		int i = 0;
		int startpos = 0;
		int crlen = Const.CR.length();
		
		while (i<all.length()-crlen)
		{
			if (all.substring(i, i+crlen).equalsIgnoreCase(Const.CR))
			{
				String line = all.substring(startpos, i);
				if (line.toUpperCase().indexOf(BaseMessages.getString(PKG, "JobLog.System.ERROR"))>=0 || //$NON-NLS-1$
				    line.toUpperCase().indexOf(BaseMessages.getString(PKG, "JobLog.System.EXCEPTION"))>=0 //$NON-NLS-1$
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
		if (line.toUpperCase().indexOf(BaseMessages.getString(PKG, "JobLog.System.ERROR"))>=0 || //$NON-NLS-1$
		    line.toUpperCase().indexOf(BaseMessages.getString(PKG, "JobLog.System.EXCEPTION"))>=0 //$NON-NLS-1$
		    ) 
		{
			err.add(line);
		}
		
		if (err.size()>0)
		{
			String err_lines[] = new String[err.size()];
			for (i=0;i<err_lines.length;i++) err_lines[i] = err.get(i);
			
			EnterSelectionDialog esd = new EnterSelectionDialog(jobGraph.getShell(), err_lines, BaseMessages.getString(PKG, "JobLog.Dialog.ErrorLines.Title"), BaseMessages.getString(PKG, "JobLog.Dialog.ErrorLines.Message")); //$NON-NLS-1$ //$NON-NLS-2$
			line = esd.open();
			if (line!=null)
			{
				JobMeta jobMeta = jobGraph.getManagedObject();
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

	/**
	 * @return the transLogTab
	 */
	public CTabItem getJobLogTab() {
		return jobLogTab;
	}

	public void pauseLog() {
		XulToolbarButton pauseContinueButton = toolbar.getButtonById("log-pause");

		if (logBrowser.isPaused()) {
			logBrowser.setPaused(false);
			if (pauseContinueButton!=null) { 
				pauseContinueButton.setImage(GUIResource.getInstance().getImagePauseLog());
			}
		} else {
			logBrowser.setPaused(true);
			if (pauseContinueButton!=null) { 
				pauseContinueButton.setImage(GUIResource.getInstance().getImageContinueLog());
			}
		}
	}

}
