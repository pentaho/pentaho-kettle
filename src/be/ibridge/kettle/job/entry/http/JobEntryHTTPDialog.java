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

    private Label        wlRunEveryRow;
    private Button       wRunEveryRow;
    private FormData     fdlRunEveryRow, fdRunEveryRow;

    private Label        wlFieldURL;
    private Text         wFieldURL;
    private FormData     fdlFieldURL, fdFieldURL;
    
	private Label        wlTargetFile;
	private Text         wTargetFile;
	private FormData     fdlTargetFile, fdTargetFile;

    private Label        wlAppend;
    private Button       wAppend;
    private FormData     fdlAppend, fdAppend;

    private Label        wlDateTimeAdded;
    private Button       wDateTimeAdded;
    private FormData     fdlDateTimeAdded, fdDateTimeAdded;
    
    private Label        wlTargetExt;
    private Text         wTargetExt;
    private FormData     fdlTargetExt, fdTargetExt;
    
    
    private Label        wlUploadFile;
    private Text         wUploadFile;
    private FormData     fdlUploadFile, fdUploadFile;

    
    
    
    private Label        wlUserName;
    private Text         wUserName;
    private FormData     fdlUserName, fdUserName;
    
    private Label        wlPassword;
    private Text         wPassword;
    private FormData     fdlPassword, fdPassword;

    
    
    private Label        wlProxyServer;
    private Text         wProxyServer;
    private FormData     fdlProxyServer, fdProxyServer;

    private Label        wlProxyPort;
    private Text         wProxyPort;
    private FormData     fdlProxyPort, fdProxyPort;
    
    private Label        wlNonProxyHosts;
    private Text         wNonProxyHosts;
    private FormData     fdlNonProxyHosts, fdNonProxyHosts;
    
    
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

        // RunEveryRow line
        wlRunEveryRow=new Label(shell, SWT.RIGHT);
        wlRunEveryRow.setText("Run for every result row? ");
        props.setLook(wlRunEveryRow);
        fdlRunEveryRow=new FormData();
        fdlRunEveryRow.left = new FormAttachment(0, 0);
        fdlRunEveryRow.top  = new FormAttachment(wURL, margin);
        fdlRunEveryRow.right= new FormAttachment(middle, -margin);
        wlRunEveryRow.setLayoutData(fdlRunEveryRow);
        wRunEveryRow=new Button(shell, SWT.CHECK);
        props.setLook(wRunEveryRow);
        fdRunEveryRow=new FormData();
        fdRunEveryRow.left = new FormAttachment(middle, 0);
        fdRunEveryRow.top  = new FormAttachment(wURL, margin);
        fdRunEveryRow.right= new FormAttachment(100, 0);
        wRunEveryRow.setLayoutData(fdRunEveryRow);
        wRunEveryRow.addSelectionListener(new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent e)
                {
                    setFlags();
                }
            }
        );

        
        // FieldURL line
        wlFieldURL=new Label(shell, SWT.RIGHT);
        wlFieldURL.setText("Fieldname to get URL from");
        props.setLook(wlFieldURL);
        fdlFieldURL=new FormData();
        fdlFieldURL.left = new FormAttachment(0, 0);
        fdlFieldURL.top  = new FormAttachment(wRunEveryRow, margin);
        fdlFieldURL.right= new FormAttachment(middle, -margin);
        wlFieldURL.setLayoutData(fdlFieldURL);
        wFieldURL=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wFieldURL);
        wFieldURL.addModifyListener(lsMod);
        fdFieldURL=new FormData();
        fdFieldURL.left = new FormAttachment(middle, 0);
        fdFieldURL.top  = new FormAttachment(wRunEveryRow, margin);
        fdFieldURL.right= new FormAttachment(100, 0);
        wFieldURL.setLayoutData(fdFieldURL);

		// TargetFile line
		wlTargetFile=new Label(shell, SWT.RIGHT);
		wlTargetFile.setText("Target filename");
 		props.setLook(wlTargetFile);
		fdlTargetFile=new FormData();
		fdlTargetFile.left = new FormAttachment(0, 0);
		fdlTargetFile.top  = new FormAttachment(wFieldURL, margin);
		fdlTargetFile.right= new FormAttachment(middle, -margin);
		wlTargetFile.setLayoutData(fdlTargetFile);
		wTargetFile=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wTargetFile);
		wTargetFile.addModifyListener(lsMod);
		fdTargetFile=new FormData();
		fdTargetFile.left = new FormAttachment(middle, 0);
		fdTargetFile.top  = new FormAttachment(wFieldURL, margin);
		fdTargetFile.right= new FormAttachment(100, 0);
		wTargetFile.setLayoutData(fdTargetFile);

        // Append line
        wlAppend=new Label(shell, SWT.RIGHT);
        wlAppend.setText("Append to specified target file? ");
        props.setLook(wlAppend);
        fdlAppend=new FormData();
        fdlAppend.left = new FormAttachment(0, 0);
        fdlAppend.top  = new FormAttachment(wTargetFile, margin);
        fdlAppend.right= new FormAttachment(middle, -margin);
        wlAppend.setLayoutData(fdlAppend);
        wAppend=new Button(shell, SWT.CHECK);
        props.setLook(wAppend);
        fdAppend=new FormData();
        fdAppend.left = new FormAttachment(middle, 0);
        fdAppend.top  = new FormAttachment(wTargetFile, margin);
        fdAppend.right= new FormAttachment(100, 0);
        wAppend.setLayoutData(fdAppend);


        // DateTimeAdded line
        wlDateTimeAdded=new Label(shell, SWT.RIGHT);
        wlDateTimeAdded.setText("Add date and time to target file name? ");
        props.setLook(wlDateTimeAdded);
        fdlDateTimeAdded=new FormData();
        fdlDateTimeAdded.left = new FormAttachment(0, 0);
        fdlDateTimeAdded.top  = new FormAttachment(wAppend, margin);
        fdlDateTimeAdded.right= new FormAttachment(middle, -margin);
        wlDateTimeAdded.setLayoutData(fdlDateTimeAdded);
        wDateTimeAdded=new Button(shell, SWT.CHECK);
        props.setLook(wDateTimeAdded);
        fdDateTimeAdded=new FormData();
        fdDateTimeAdded.left = new FormAttachment(middle, 0);
        fdDateTimeAdded.top  = new FormAttachment(wAppend, margin);
        fdDateTimeAdded.right= new FormAttachment(100, 0);
        wDateTimeAdded.setLayoutData(fdDateTimeAdded);
        wDateTimeAdded.addSelectionListener(new SelectionAdapter()
                {
                    public void widgetSelected(SelectionEvent e)
                    {
                        setFlags();
                    }
                }
            );
        
        // TargetExt line
        wlTargetExt=new Label(shell, SWT.RIGHT);
        wlTargetExt.setText("Target filename extention");
        props.setLook(wlTargetExt);
        fdlTargetExt=new FormData();
        fdlTargetExt.left = new FormAttachment(0, 0);
        fdlTargetExt.top  = new FormAttachment(wDateTimeAdded, margin);
        fdlTargetExt.right= new FormAttachment(middle, -margin);
        wlTargetExt.setLayoutData(fdlTargetExt);
        wTargetExt=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wTargetExt);
        wTargetExt.addModifyListener(lsMod);
        fdTargetExt=new FormData();
        fdTargetExt.left = new FormAttachment(middle, 0);
        fdTargetExt.top  = new FormAttachment(wDateTimeAdded, margin);
        fdTargetExt.right= new FormAttachment(100, 0);
        wTargetExt.setLayoutData(fdTargetExt);

        
        
        // UploadFile line
        wlUploadFile=new Label(shell, SWT.RIGHT);
        wlUploadFile.setText("File to upload");
        props.setLook(wlUploadFile);
        fdlUploadFile=new FormData();
        fdlUploadFile.left = new FormAttachment(0, 0);
        fdlUploadFile.top  = new FormAttachment(wTargetExt, margin*5);
        fdlUploadFile.right= new FormAttachment(middle, -margin);
        wlUploadFile.setLayoutData(fdlUploadFile);
        wUploadFile=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wUploadFile);
        wUploadFile.addModifyListener(lsMod);
        fdUploadFile=new FormData();
        fdUploadFile.left = new FormAttachment(middle, 0);
        fdUploadFile.top  = new FormAttachment(wTargetExt, margin*5);
        fdUploadFile.right= new FormAttachment(100, 0);
        wUploadFile.setLayoutData(fdUploadFile);

        
        // UserName line
        wlUserName=new Label(shell, SWT.RIGHT);
        wlUserName.setText("Username");
        props.setLook(wlUserName);
        fdlUserName=new FormData();
        fdlUserName.left = new FormAttachment(0, 0);
        fdlUserName.top  = new FormAttachment(wUploadFile, margin*5);
        fdlUserName.right= new FormAttachment(middle, -margin);
        wlUserName.setLayoutData(fdlUserName);
        wUserName=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wUserName);
        wUserName.addModifyListener(lsMod);
        fdUserName=new FormData();
        fdUserName.left = new FormAttachment(middle, 0);
        fdUserName.top  = new FormAttachment(wUploadFile, margin*5);
        fdUserName.right= new FormAttachment(100, 0);
        wUserName.setLayoutData(fdUserName);

        // Password line
        wlPassword=new Label(shell, SWT.RIGHT);
        wlPassword.setText("Password");
        props.setLook(wlPassword);
        fdlPassword=new FormData();
        fdlPassword.left = new FormAttachment(0, 0);
        fdlPassword.top  = new FormAttachment(wUserName, margin);
        fdlPassword.right= new FormAttachment(middle, -margin);
        wlPassword.setLayoutData(fdlPassword);
        wPassword=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wPassword);
        wPassword.setEchoChar('*');
        wPassword.addModifyListener(lsMod);
        fdPassword=new FormData();
        fdPassword.left = new FormAttachment(middle, 0);
        fdPassword.top  = new FormAttachment(wUserName, margin);
        fdPassword.right= new FormAttachment(100, 0);
        wPassword.setLayoutData(fdPassword);

        
        // ProxyServer line
        wlProxyServer=new Label(shell, SWT.RIGHT);
        wlProxyServer.setText("Proxy hostname");
        props.setLook(wlProxyServer);
        fdlProxyServer=new FormData();
        fdlProxyServer.left = new FormAttachment(0, 0);
        fdlProxyServer.top  = new FormAttachment(wPassword, margin*5);
        fdlProxyServer.right= new FormAttachment(middle, -margin);
        wlProxyServer.setLayoutData(fdlProxyServer);
        wProxyServer=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wProxyServer);
        wProxyServer.addModifyListener(lsMod);
        fdProxyServer=new FormData();
        fdProxyServer.left = new FormAttachment(middle, 0);
        fdProxyServer.top  = new FormAttachment(wPassword, margin*5);
        fdProxyServer.right= new FormAttachment(100, 0);
        wProxyServer.setLayoutData(fdProxyServer);
        
        // ProxyPort line
        wlProxyPort=new Label(shell, SWT.RIGHT);
        wlProxyPort.setText("Proxy port (usually 8080)");
        props.setLook(wlProxyPort);
        fdlProxyPort=new FormData();
        fdlProxyPort.left = new FormAttachment(0, 0);
        fdlProxyPort.top  = new FormAttachment(wProxyServer, margin);
        fdlProxyPort.right= new FormAttachment(middle, -margin);
        wlProxyPort.setLayoutData(fdlProxyPort);
        wProxyPort=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wProxyPort);
        wProxyPort.addModifyListener(lsMod);
        fdProxyPort=new FormData();
        fdProxyPort.left = new FormAttachment(middle, 0);
        fdProxyPort.top  = new FormAttachment(wProxyServer, margin);
        fdProxyPort.right= new FormAttachment(100, 0);
        wProxyPort.setLayoutData(fdProxyPort);

        // IgnoreHosts line
        wlNonProxyHosts=new Label(shell, SWT.RIGHT);
        wlNonProxyHosts.setText("Ignore proxy for hosts: regexp | separated");
        props.setLook(wlNonProxyHosts);
        fdlNonProxyHosts=new FormData();
        fdlNonProxyHosts.left = new FormAttachment(0, 0);
        fdlNonProxyHosts.top  = new FormAttachment(wProxyPort, margin);
        fdlNonProxyHosts.right= new FormAttachment(middle, -margin);
        wlNonProxyHosts.setLayoutData(fdlNonProxyHosts);
        wNonProxyHosts=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wNonProxyHosts);
        wNonProxyHosts.addModifyListener(lsMod);
        fdNonProxyHosts=new FormData();
        fdNonProxyHosts.left = new FormAttachment(middle, 0);
        fdNonProxyHosts.top  = new FormAttachment(wProxyPort, margin);
        fdNonProxyHosts.right= new FormAttachment(100, 0);
        wNonProxyHosts.setLayoutData(fdNonProxyHosts);
        
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(" &OK ");
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(" &Cancel ");

		BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, null);

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

	private void setFlags()
    {
        wlURL.setEnabled(!wRunEveryRow.getSelection());
        wURL.setEnabled(!wRunEveryRow.getSelection());
        wlFieldURL.setEnabled(wRunEveryRow.getSelection());
        wFieldURL.setEnabled(wRunEveryRow.getSelection());
        
        wlTargetExt.setEnabled( wDateTimeAdded.getSelection() );
        wTargetExt.setEnabled( wDateTimeAdded.getSelection() );
        wlAppend.setEnabled( !wDateTimeAdded.getSelection() );
        wAppend.setEnabled( !wDateTimeAdded.getSelection() );
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
        wRunEveryRow.setSelection( jobentry.isRunForEveryRow() );
        wFieldURL.setText(Const.NVL(jobentry.getUrlFieldname(), "") );
		wTargetFile.setText(Const.NVL(jobentry.getTargetFilename(), ""));
        wAppend.setSelection( jobentry.isFileAppended() );
        wDateTimeAdded.setSelection( jobentry.isDateTimeAdded() );
        wTargetExt.setText(Const.NVL(jobentry.getTargetFilenameExtention(), ""));

        wUploadFile.setText(Const.NVL(jobentry.getUploadFilename(), ""));

        jobentry.setDateTimeAdded( wDateTimeAdded.getSelection() );
        jobentry.setTargetFilenameExtention( wTargetExt.getText() );

        wUserName.setText(Const.NVL(jobentry.getUsername(), ""));
        wPassword.setText(Const.NVL(jobentry.getPassword(), ""));
        
        wProxyServer.setText(Const.NVL(jobentry.getProxyHostname(), ""));
        wProxyPort.setText(""+jobentry.getProxyPort());
        wNonProxyHosts.setText(Const.NVL(jobentry.getNonProxyHosts(), ""));
        
        setFlags();
	}
	
	private void cancel()
	{
		jobentry.setChanged(changed);
		jobentry=null;
		dispose();
	}
	
	private void ok()
	{
		jobentry.setName( wName.getText() );
		jobentry.setUrl( wURL.getText() );
        jobentry.setRunForEveryRow( wRunEveryRow.getSelection() );
        jobentry.setUrlFieldname( wFieldURL.getText() );
		jobentry.setTargetFilename( wTargetFile.getText() );
        jobentry.setFileAppended( wAppend.getSelection() );
        
        jobentry.setDateTimeAdded( wDateTimeAdded.getSelection() );
        jobentry.setTargetFilenameExtention( wTargetExt.getText() );

        jobentry.setUploadFilename( wUploadFile.getText() );

		jobentry.setUsername( wUserName.getText() );
        jobentry.setPassword( wPassword.getText() );
        
        jobentry.setProxyHostname( wProxyServer.getText() );
        jobentry.setProxyPort( Const.toInt(wProxyPort.getText(), 0) );
        jobentry.setNonProxyHosts( wNonProxyHosts.getText() );
        
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
