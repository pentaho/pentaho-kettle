package org.pentaho.di.ui.spoon.job;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.logging.BufferChangedListener;
import org.pentaho.di.core.logging.Log4jStringAppender;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.spoon.Messages;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.delegates.SpoonDelegate;

public class JobLogDelegate extends SpoonDelegate {
	
	private static final LogWriter log = LogWriter.getInstance();
	
	private JobGraph jobGraph;

	private CTabItem jobLogTab;
	
	public  Text jobLogText;
	
    /**
     * The number of lines in the log tab
     */
	private int textSize;
	
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
		jobLogTab = new CTabItem(jobGraph.extraViewTabFolder, SWT.CLOSE | SWT.MAX);
		jobLogTab.setImage(GUIResource.getInstance().getImageShowLog());
		jobLogTab.setText(Messages.getString("Spoon.TransGraph.LogTab.Name"));
		// TODO: set image on tab item.
		jobLogText = new Text(jobGraph.extraViewTabFolder, SWT.READ_ONLY | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		jobLogTab.setControl(jobLogText);
		
		// Create a new String appender to the log and capture that directly...
		//
		final Log4jStringAppender stringAppender = LogWriter.createStringAppender();
		stringAppender.setMaxNrLines(Props.getInstance().getMaxNrLinesInLog());
		stringAppender.addBufferChangedListener(new BufferChangedListener() {
		
			public void contentWasAdded(final StringBuffer content, final String extra, final int nrLines) {
				spoon.getDisplay().asyncExec(new Runnable() {
				

					public void run() 
					{
						if (!jobLogText.isDisposed())
						{
							textSize++;
							
							// OK, now what if the number of lines gets too big?
							// We allow for a few hundred lines buffer over-run.
							// That way we reduce flicker...
							//
							if (textSize>=nrLines+200)
							{
								jobLogText.setText(content.toString());
								jobLogText.setSelection(content.length());
								jobLogText.showSelection();
								jobLogText.clearSelection();
								textSize=nrLines;
							}
							else
							{
								jobLogText.append(extra);
							}
						}
					}
				
				});
			}
		
		});
		log.addAppender(stringAppender);
		jobLogTab.addDisposeListener(new DisposeListener() { public void widgetDisposed(DisposeEvent e) { log.removeAppender(stringAppender); } });
		
		jobGraph.extraViewTabFolder.setSelection(jobLogTab);
	}

    
    public void showLogView() {
    	
    	// What button?
    	//
    	// XulToolbarButton showLogXulButton = toolbar.getButtonById("trans-show-log");
    	// ToolItem toolBarButton = (ToolItem) showLogXulButton.getNativeObject();
    	
    	if (jobLogTab==null || jobLogTab.isDisposed()) {
    		addJobLog();
    	} else {
    		jobLogTab.dispose();
    		
    		jobGraph.checkEmptyExtraView();
    	}
    	
    	// spoon.addTransLog(transMeta);
    }
    
	public void clearLog()
	{
		jobLogText.setText(""); //$NON-NLS-1$
	}

	public void showErrors()
	{
		String all = jobLogTab.getText();
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
			
			EnterSelectionDialog esd = new EnterSelectionDialog(jobGraph.getShell(), err_lines, Messages.getString("JobLog.Dialog.ErrorLines.Title"), Messages.getString("JobLog.Dialog.ErrorLines.Message")); //$NON-NLS-1$ //$NON-NLS-2$
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
	
}
