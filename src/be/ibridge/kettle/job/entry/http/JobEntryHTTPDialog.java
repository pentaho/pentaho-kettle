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
 * Created on 19-jun-2003
 *
 */

package be.ibridge.kettle.job.entry.http;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.WindowProperty;
import be.ibridge.kettle.job.JobMeta;
import be.ibridge.kettle.job.entry.JobEntryDialogInterface;
import be.ibridge.kettle.job.entry.JobEntryInterface;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.trans.step.BaseStepDialog;


/**
 * This dialog allows you to edit the SQL job entry settings. (select the connection and the sql script to be executed)
 *  
 * @author Matt
 * @since  19-06-2003
 */
public class JobEntryHTTPDialog extends Dialog implements JobEntryDialogInterface
{
	private Label        wlName;
	private Text         wName;
    private FormData     fdlName, fdName;

	private Label        wlURL;
	private Text         wURL;
	private FormData     fdlURL, fdURL;
	
	private Label        wlTargetFile;
	private Text         wTargetFile;
	private FormData     fdlTargetFile, fdTargetFile;
		
	private Button wOK, wCancel;
	private Listener lsOK, lsCancel;

	private JobEntryHTTP     jobentry;
	private Shell       	 shell;
	private Props       	 props;

	private SelectionAdapter lsDef;

	private boolean changed;
	
	public JobEntryHTTPDialog(Shell parent, JobEntryHTTP je, Repository rep, JobMeta ji)
	{
			super(parent, SWT.NONE);
			props=Props.getInstance();
			jobentry=je;
	
			if (jobentry.getName() == null) jobentry.setName("FTP Files");
	}

	public JobEntryInterface open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
 		props.setLook(shell);

		ModifyListener lsMod = new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				jobentry.setChanged();
			}
		};
		changed = jobentry.hasChanged();

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText("Get files by FTP");
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Job entry name line
		wlName=new Label(shell, SWT.RIGHT);
		wlName.setText("Job entry name ");
 		props.setLook(wlName);
		fdlName=new FormData();
		fdlName.left = new FormAttachment(0, 0);
		fdlName.right= new FormAttachment(middle, -margin);
		fdlName.top  = new FormAttachment(0, margin);
		wlName.setLayoutData(fdlName);
		wName=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wName);
		wName.addModifyListener(lsMod);
		fdName=new FormData();
		fdName.left = new FormAttachment(middle, 0);
		fdName.top  = new FormAttachment(0, margin);
		fdName.right= new FormAttachment(100, 0);
		wName.setLayoutData(fdName);

		// URL line
		wlURL=new Label(shell, SWT.RIGHT);
		wlURL.setText("URL (HTTP)");
 		props.setLook(wlURL);
		fdlURL=new FormData();
		fdlURL.left = new FormAttachment(0, 0);
		fdlURL.top  = new FormAttachment(wName, margin);
		fdlURL.right= new FormAttachment(middle, -margin);
		wlURL.setLayoutData(fdlURL);
		wURL=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wURL);
		wURL.addModifyListener(lsMod);
		fdURL=new FormData();
		fdURL.left = new FormAttachment(middle, 0);
		fdURL.top  = new FormAttachment(wName, margin);
		fdURL.right= new FormAttachment(100, 0);
		wURL.setLayoutData(fdURL);

		// TargetFile line
		wlTargetFile=new Label(shell, SWT.RIGHT);
		wlTargetFile.setText("Target filename");
 		props.setLook(wlTargetFile);
		fdlTargetFile=new FormData();
		fdlTargetFile.left = new FormAttachment(0, 0);
		fdlTargetFile.top  = new FormAttachment(wURL, margin);
		fdlTargetFile.right= new FormAttachment(middle, -margin);
		wlTargetFile.setLayoutData(fdlTargetFile);
		wTargetFile=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wTargetFile);
		wTargetFile.addModifyListener(lsMod);
		fdTargetFile=new FormData();
		fdTargetFile.left = new FormAttachment(middle, 0);
		fdTargetFile.top  = new FormAttachment(wURL, margin);
		fdTargetFile.right= new FormAttachment(100, 0);
		wTargetFile.setLayoutData(fdTargetFile);

		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(" &OK ");
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(" &Cancel ");

		BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, wTargetFile);

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		
		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener    (SWT.Selection, lsOK    );
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wName.addSelectionListener( lsDef );
        wURL.addSelectionListener( lsDef );
        wTargetFile.addSelectionListener( lsDef );
        
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );
				
		getData();
		
		WindowProperty winprop = props.getScreen(shell.getText());
		if (winprop!=null) winprop.setShell(shell); else shell.pack();

		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return jobentry;
	}

	public void dispose()
	{
		WindowProperty winprop = new WindowProperty(shell);
		props.setScreen(winprop);
		shell.dispose();
	}
	
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		if (jobentry.getName()    != null) wName.setText( jobentry.getName() );
		wName.selectAll();

		wURL.setText(Const.NVL(jobentry.getUrl(), ""));
		wTargetFile.setText(Const.NVL(jobentry.getTargetFilename(), ""));
	}
	
	private void cancel()
	{
		jobentry.setChanged(changed);
		jobentry=null;
		dispose();
	}
	
	private void ok()
	{
		jobentry.setName(wName.getText());
		jobentry.setUrl(wURL.getText());
		jobentry.setTargetFilename(wTargetFile.getText());

		dispose();
	}

	public String toString()
	{
		return this.getClass().getName();
	}
	
	public boolean evaluates()
	{
		return true;
	}

	public boolean isUnconditional()
	{
		return false;
	}

}
