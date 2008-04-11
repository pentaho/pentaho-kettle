package org.pentaho.di.ui.spoon.trans;

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
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.spoon.Messages;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.delegates.SpoonDelegate;

public class TransLogDelegate extends SpoonDelegate {
	
	private static final LogWriter log = LogWriter.getInstance();
	
	private TransGraph transGraph;

	private CTabItem transLogTab;
	
	private Text transLogText;
	
    /**
     * The number of lines in the log tab
     */
	private int textSize;
	
	/**
	 * @param spoon
	 */
	public TransLogDelegate(Spoon spoon, TransGraph transGraph) {
		super(spoon);
		this.transGraph = transGraph;
	}
	
	public void addTransLog() {
		// First, see if we need to add the extra view...
		//
		if (transGraph.extraViewComposite==null || transGraph.extraViewComposite.isDisposed()) {
			transGraph.addExtraView();
		} else {
			if (transLogTab!=null && !transLogTab.isDisposed()) {
				// just set this one active and get out...
				//
				transGraph.extraViewTabFolder.setSelection(transLogTab);
				return; 
			}
		}
		
		// Add a transLogTab : display the logging...
		//
		transLogTab = new CTabItem(transGraph.extraViewTabFolder, SWT.NONE);
		transLogTab.setImage(GUIResource.getInstance().getImageShowLog());
		transLogTab.setText(Messages.getString("Spoon.TransGraph.LogTab.Name"));
		// TODO: set image on tab item.
		transLogText = new Text(transGraph.extraViewTabFolder, SWT.READ_ONLY | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		transLogTab.setControl(transLogText);
		
		// Create a new String appender to the log and capture that directly...
		//
		final Log4jStringAppender stringAppender = LogWriter.createStringAppender();
		stringAppender.setMaxNrLines(Props.getInstance().getMaxNrLinesInLog());
		stringAppender.addBufferChangedListener(new BufferChangedListener() {
		
			public void contentWasAdded(final StringBuffer content, final String extra, final int nrLines) {
				spoon.getDisplay().asyncExec(new Runnable() {
				

					public void run() 
					{
						if (!transLogText.isDisposed())
						{
							textSize++;
							
							// OK, now what if the number of lines gets too big?
							// We allow for a few hundred lines buffer over-run.
							// That way we reduce flicker...
							//
							if (textSize>=nrLines+200)
							{
								transLogText.setText(content.toString());
								transLogText.setSelection(content.length());
								transLogText.showSelection();
								transLogText.clearSelection();
								textSize=nrLines;
							}
							else
							{
								transLogText.append(extra);
							}
						}
					}
				
				});
			}
		
		});
		log.addAppender(stringAppender);
		transLogTab.addDisposeListener(new DisposeListener() { public void widgetDisposed(DisposeEvent e) { log.removeAppender(stringAppender); } });
		
		transGraph.extraViewTabFolder.setSelection(transLogTab);
	}

    
    public void showLogView() {
    	
    	// What button?
    	//
    	// XulToolbarButton showLogXulButton = toolbar.getButtonById("trans-show-log");
    	// ToolItem toolBarButton = (ToolItem) showLogXulButton.getNativeObject();
    	
    	if (transLogTab==null || transLogTab.isDisposed()) {
    		addTransLog();
    	} else {
    		transLogTab.dispose();
    		
    		transGraph.checkEmptyExtraView();
    	}
    	
    	// spoon.addTransLog(transMeta);
    }
    
	public void clearLog()
	{
		transLogText.setText(""); //$NON-NLS-1$
	}

	public void showErrors()
	{
		String all = transLogText.getText();
		ArrayList<String> err = new ArrayList<String>();

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

			EnterSelectionDialog esd = new EnterSelectionDialog(transGraph.getShell(), err_lines, Messages.getString("TransLog.Dialog.ErrorLines.Title"), Messages.getString("TransLog.Dialog.ErrorLines.Message")); //$NON-NLS-1$ //$NON-NLS-2$
			line = esd.open();
			if (line != null)
			{
				TransMeta transMeta = transGraph.getManagedObject();
				for (i = 0; i < transMeta.nrSteps(); i++)
				{
					StepMeta stepMeta = transMeta.getStep(i);
					if (line.indexOf(stepMeta.getName()) >= 0)
					{
						spoon.editStep(transMeta, stepMeta);
					}
				}
				// System.out.println("Error line selected: "+line);
			}
		}
	}

	/**
	 * @return the transLogTab
	 */
	public CTabItem getTransLogTab() {
		return transLogTab;
	}
	
}
