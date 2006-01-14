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

 

package be.ibridge.kettle.job.entry.shell;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import be.ibridge.kettle.core.ColumnInfo;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.WindowProperty;
import be.ibridge.kettle.core.widget.TableView;
import be.ibridge.kettle.job.entry.JobEntryDialogInterface;
import be.ibridge.kettle.job.entry.JobEntryInterface;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.trans.step.BaseStepDialog;


/**
 * Dialog that allows you to enter the settings for a Shell job entry.
 * 
 * @author Matt
 * @since 19-06-2003
 * 
 */
public class JobEntryShellDialog extends Dialog implements JobEntryDialogInterface
{
	private Label        wlName;
	private Text         wName;
	private FormData     fdlName, fdName;

	private Label        wlFilename;
	private Button       wbFilename;
	private Text         wFilename;
	private FormData     fdlFilename, fdbFilename, fdFilename;

    private Group        wLogging;
	private FormData     fdLogging;

	private Label        wlSetLogfile;
	private Button       wSetLogfile;
	private FormData     fdlSetLogfile, fdSetLogfile;

	private Label        wlLogfile;
	private Text         wLogfile;
	private FormData     fdlLogfile, fdLogfile;

	private Label        wlLogext;
	private Text         wLogext;
	private FormData     fdlLogext, fdLogext;

	private Label        wlAddDate;
	private Button       wAddDate;
	private FormData     fdlAddDate, fdAddDate;

	private Label        wlAddTime;
	private Button       wAddTime;
	private FormData     fdlAddTime, fdAddTime;

	private Label        wlLoglevel;
	private CCombo       wLoglevel;
	private FormData     fdlLoglevel, fdLoglevel;
	
	private Label        wlPrevious;
	private Button       wPrevious;
	private FormData     fdlPrevious, fdPrevious;

	private Label        wlFields;
	private TableView    wFields;
	private FormData     fdlFields, fdFields;


	private Button wOK, wCancel;
	private Listener lsOK, lsCancel;

	private Shell  shell;
	private SelectionAdapter lsDef;
	
	private JobEntryShell jobentry;
	private boolean  backupChanged, backupLogfile, backupDate, backupTime;
	private Props    props;
	private Display  display;
	
	public JobEntryShellDialog(Shell parent, JobEntryShell je, Repository rep)
	{
		super(parent, SWT.NONE);
		props=Props.getInstance();
		jobentry=je;
	}

	public JobEntryInterface open()
	{
		Shell parent = getParent();
		display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE);
 		props.setLook(shell);

		ModifyListener lsMod = new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				jobentry.setChanged();
			}
		};
		backupChanged = jobentry.hasChanged();
		backupLogfile = jobentry.setLogfile;
		backupDate    = jobentry.addDate;
		backupTime    = jobentry.addTime;

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText("Job entry details for this shell script:");
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Name line
		wlName=new Label(shell, SWT.RIGHT);
		wlName.setText("Name of shell script: ");
 		props.setLook(wlName);
		fdlName=new FormData();
		fdlName.left = new FormAttachment(0, 0);
		fdlName.top  = new FormAttachment(0, 0);
		fdlName.right= new FormAttachment(middle, 0);
		wlName.setLayoutData(fdlName);

		wName=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wName);
		wName.addModifyListener(lsMod);
		fdName=new FormData();
		fdName.top  = new FormAttachment(0, 0);
		fdName.left = new FormAttachment(middle, 0);
		fdName.right= new FormAttachment(100, 0);
		wName.setLayoutData(fdName);

		// Filename line
		wlFilename=new Label(shell, SWT.RIGHT);
		wlFilename.setText("Filename : ");
 		props.setLook(wlFilename);
		fdlFilename=new FormData();
		fdlFilename.left = new FormAttachment(0, 0);
		fdlFilename.top  = new FormAttachment(wName, margin);
		fdlFilename.right= new FormAttachment(middle, 0);
		wlFilename.setLayoutData(fdlFilename);

		wbFilename=new Button(shell, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbFilename);
		wbFilename.setText("&Browse...");
		fdbFilename=new FormData();
		fdbFilename.top   = new FormAttachment(wName, margin);
		fdbFilename.right = new FormAttachment(100, 0);
		wbFilename.setLayoutData(fdbFilename);

		wFilename=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wFilename);
		wFilename.addModifyListener(lsMod);
		fdFilename=new FormData();
		fdFilename.left = new FormAttachment(middle, 0);
		fdFilename.right= new FormAttachment(wbFilename, -margin);
		fdFilename.top  = new FormAttachment(wName, margin);
		wFilename.setLayoutData(fdFilename);

		// logging grouping?
		//////////////////////////
		// START OF LOGGING GROUP///
		///
		wLogging=new Group(shell, SWT.SHADOW_NONE);
 		props.setLook(wLogging);
		wLogging.setText("Log file settings");
		
		FormLayout groupLayout = new FormLayout ();
		groupLayout.marginWidth  = 10;
		groupLayout.marginHeight = 10;
		
		wLogging.setLayout(groupLayout);

		// Set the logfile?
		wlSetLogfile=new Label(wLogging, SWT.RIGHT);
		wlSetLogfile.setText("Specify logfile?");
 		props.setLook(wlSetLogfile);
		fdlSetLogfile=new FormData();
		fdlSetLogfile.left = new FormAttachment(0, 0);
		fdlSetLogfile.top  = new FormAttachment(0, margin);
		fdlSetLogfile.right= new FormAttachment(middle, -margin);
		wlSetLogfile.setLayoutData(fdlSetLogfile);
		wSetLogfile=new Button(wLogging, SWT.CHECK);
 		props.setLook(wSetLogfile);
		fdSetLogfile=new FormData();
		fdSetLogfile.left = new FormAttachment(middle, 0);
		fdSetLogfile.top  = new FormAttachment(0, margin);
		fdSetLogfile.right= new FormAttachment(100, 0);
		wSetLogfile.setLayoutData(fdSetLogfile);
		wSetLogfile.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					jobentry.setLogfile=!jobentry.setLogfile;
					jobentry.setChanged();
					setActive();
				}
			}
		);

		// Set the logfile path + base-name
		wlLogfile=new Label(wLogging, SWT.RIGHT);
		wlLogfile.setText("Name of logfile ");
 		props.setLook(wlLogfile);
		fdlLogfile=new FormData();
		fdlLogfile.left = new FormAttachment(0, 0);
		fdlLogfile.top  = new FormAttachment(wlSetLogfile, margin);
		fdlLogfile.right= new FormAttachment(middle, 0);
		wlLogfile.setLayoutData(fdlLogfile);
		wLogfile=new Text(wLogging, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wLogfile.setText("");
 		props.setLook(wLogfile);
		fdLogfile=new FormData();
		fdLogfile.left = new FormAttachment(middle, 0);
		fdLogfile.top  = new FormAttachment(wlSetLogfile, margin);
		fdLogfile.right= new FormAttachment(100, 0);
		wLogfile.setLayoutData(fdLogfile);

		// Set the logfile filename extention
		wlLogext=new Label(wLogging, SWT.RIGHT);
		wlLogext.setText("Extention of logfile ");
 		props.setLook(wlLogext);
		fdlLogext=new FormData();
		fdlLogext.left = new FormAttachment(0, 0);
		fdlLogext.top  = new FormAttachment(wLogfile, margin);
		fdlLogext.right= new FormAttachment(middle, 0);
		wlLogext.setLayoutData(fdlLogext);
		wLogext=new Text(wLogging, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wLogext.setText("");
 		props.setLook(wLogext);
		fdLogext=new FormData();
		fdLogext.left = new FormAttachment(middle, 0);
		fdLogext.top  = new FormAttachment(wLogfile, margin);
		fdLogext.right= new FormAttachment(100, 0);
		wLogext.setLayoutData(fdLogext);

		// Add date to logfile name?
		wlAddDate=new Label(wLogging, SWT.RIGHT);
		wlAddDate.setText("Include date in filename?");
 		props.setLook(wlAddDate);
		fdlAddDate=new FormData();
		fdlAddDate.left = new FormAttachment(0, 0);
		fdlAddDate.top  = new FormAttachment(wLogext, margin);
		fdlAddDate.right= new FormAttachment(middle, -margin);
		wlAddDate.setLayoutData(fdlAddDate);
		wAddDate=new Button(wLogging, SWT.CHECK);
 		props.setLook(wAddDate);
		fdAddDate=new FormData();
		fdAddDate.left = new FormAttachment(middle, 0);
		fdAddDate.top  = new FormAttachment(wLogext, margin);
		fdAddDate.right= new FormAttachment(100, 0);
		wAddDate.setLayoutData(fdAddDate);
		wAddDate.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					jobentry.addDate=!jobentry.addDate;
					jobentry.setChanged();
				}
			}
		);

		// Add time to logfile name?
		wlAddTime=new Label(wLogging, SWT.RIGHT);
		wlAddTime.setText("Include time in filename? ");
 		props.setLook(wlAddTime);
		fdlAddTime=new FormData();
		fdlAddTime.left = new FormAttachment(0, 0);
		fdlAddTime.top  = new FormAttachment(wlAddDate, margin);
		fdlAddTime.right= new FormAttachment(middle, -margin);
		wlAddTime.setLayoutData(fdlAddTime);
		wAddTime=new Button(wLogging, SWT.CHECK);
 		props.setLook(wAddTime);
		fdAddTime=new FormData();
		fdAddTime.left = new FormAttachment(middle, 0);
		fdAddTime.top  = new FormAttachment(wlAddDate, margin);
		fdAddTime.right= new FormAttachment(100, 0);
		wAddTime.setLayoutData(fdAddTime);
		wAddTime.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					jobentry.addTime=!jobentry.addTime;
					jobentry.setChanged();
				}
			}
		);

		wlLoglevel=new Label(wLogging, SWT.RIGHT);
		wlLoglevel.setText("Loglevel ");
 		props.setLook(wlLoglevel);
		fdlLoglevel=new FormData();
		fdlLoglevel.left = new FormAttachment(0, 0);
		fdlLoglevel.right= new FormAttachment(middle, -margin);
		fdlLoglevel.top  = new FormAttachment(wlAddTime, margin);
		wlLoglevel.setLayoutData(fdlLoglevel);
		wLoglevel=new CCombo(wLogging, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
		for (int i=0;i<LogWriter.logLevelDescription.length;i++) 
			wLoglevel.add(LogWriter.logLevelDescription[i]);
		wLoglevel.select( jobentry.loglevel+1); //+1: starts at -1	
		
 		props.setLook(wLoglevel);
		fdLoglevel=new FormData();
		fdLoglevel.left = new FormAttachment(middle, 0);
		fdLoglevel.top  = new FormAttachment(wlAddTime, margin);
		fdLoglevel.right= new FormAttachment(100, 0);
		wLoglevel.setLayoutData(fdLoglevel);

		fdLogging=new FormData();
		fdLogging.left = new FormAttachment(0, margin);
		fdLogging.top  = new FormAttachment(wbFilename, margin);
		fdLogging.right= new FormAttachment(100, -margin);
		wLogging.setLayoutData(fdLogging);
		/////////////////////////////////////////////////////////////
		/// END OF LOGGING GROUP
		/////////////////////////////////////////////////////////////

		wlPrevious=new Label(shell, SWT.RIGHT);
		wlPrevious.setText("Copy prev.results to args");
 		props.setLook(wlPrevious);
		fdlPrevious=new FormData();
		fdlPrevious.left = new FormAttachment(0, 0);
		fdlPrevious.top  = new FormAttachment(wLogging, margin*3);
		fdlPrevious.right= new FormAttachment(middle, -margin);
		wlPrevious.setLayoutData(fdlPrevious);
		wPrevious=new Button(shell, SWT.CHECK );
 		props.setLook(wPrevious);
		wPrevious.setSelection(jobentry.argFromPrevious);
		wPrevious.setToolTipText("Check this to pass the results of the previous entry to the arguments of this entry.");
		fdPrevious=new FormData();
		fdPrevious.left = new FormAttachment(middle, 0);
		fdPrevious.top  = new FormAttachment(wLogging, margin*3);
		fdPrevious.right= new FormAttachment(100, 0);
		wPrevious.setLayoutData(fdPrevious);
		wPrevious.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					jobentry.argFromPrevious=!jobentry.argFromPrevious;
					jobentry.setChanged();
					wlFields.setEnabled(!jobentry.argFromPrevious);
					wFields.setEnabled(!jobentry.argFromPrevious);
				}
			}
		);


		wlFields=new Label(shell, SWT.NONE);
		wlFields.setText("Fields : ");
 		props.setLook(wlFields);
		fdlFields=new FormData();
		fdlFields.left = new FormAttachment(0, 0);
		fdlFields.top  = new FormAttachment(wPrevious, margin);
		wlFields.setLayoutData(fdlFields);
		
		final int FieldsCols=1;
		int rows = jobentry.arguments==null?1:(jobentry.arguments.length==0?0:jobentry.arguments.length);
		final int FieldsRows= rows;
		
		ColumnInfo[] colinf=new ColumnInfo[FieldsCols];
		colinf[0]=new ColumnInfo("Argument",  ColumnInfo.COLUMN_TYPE_TEXT,   false);
		
		wFields=new TableView(shell, 
							  SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, 
							  colinf, 
							  FieldsRows,  
							  lsMod,
							  props
							  );

		fdFields=new FormData();
		fdFields.left = new FormAttachment(0, 0);
		fdFields.top  = new FormAttachment(wlFields, margin);
		fdFields.right  = new FormAttachment(100, 0);
		fdFields.bottom = new FormAttachment(100, -50);
		wFields.setLayoutData(fdFields);

		wlFields.setEnabled(!jobentry.argFromPrevious);
		wFields.setEnabled(!jobentry.argFromPrevious);

		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText("  &OK  ");
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText("  &Cancel  ");

		BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, wFields);

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		
		wOK.addListener    (SWT.Selection, lsOK     );
		wCancel.addListener(SWT.Selection, lsCancel );
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		wName.addSelectionListener(lsDef);
		wFilename.addSelectionListener(lsDef);

		wbFilename.addSelectionListener
		(
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e) 
				{
					FileDialog dialog = new FileDialog(shell, SWT.OPEN);
					dialog.setFilterExtensions(new String[] {"*.sh;*.bat;*.BAT", "*;*.*"});
					dialog.setFilterNames(new String[] {"Shell scripts", "All files"});
					
					if (wFilename.getText()!=null)
					{
						dialog.setFileName(wFilename.getText());
					}
					
					if (dialog.open()!=null)
					{
						wFilename.setText(dialog.getFilterPath()+Const.FILE_SEPARATOR+dialog.getFileName());
						wName.setText(dialog.getFileName());
					}
				}
			}
		);

		// Detect [X] or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );


		getData();
		setActive();

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
	
	public void setActive()
	{
		wlLogfile.setEnabled(jobentry.setLogfile);
		wLogfile.setEnabled(jobentry.setLogfile);
		
		wlLogext.setEnabled(jobentry.setLogfile);
		wLogext.setEnabled(jobentry.setLogfile);

		wlAddDate.setEnabled(jobentry.setLogfile);
		wAddDate.setEnabled(jobentry.setLogfile);

		wlAddTime.setEnabled(jobentry.setLogfile);
		wAddTime.setEnabled(jobentry.setLogfile);

		wlLoglevel.setEnabled(jobentry.setLogfile);
		wLoglevel.setEnabled(jobentry.setLogfile);
		if (jobentry.setLogfile)
		{
			wLoglevel.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
		}
		else
		{
			wLoglevel.setForeground(display.getSystemColor(SWT.COLOR_GRAY));
		}
	}
	
	public void getData()
	{
		if (jobentry.getName()!=null)     wName.setText(jobentry.getName());
		if (jobentry.getFileName()!=null) wFilename.setText(jobentry.getFileName());
		if (jobentry.arguments!=null)
		{
			for (int i=0;i<jobentry.arguments.length;i++)
			{
				TableItem ti = wFields.table.getItem(i);
				if (jobentry.arguments[i]!=null) ti.setText(1, jobentry.arguments[i]);
			}
			wFields.setRowNums();
			wFields.optWidth(true);
		}
		wPrevious.setSelection(jobentry.argFromPrevious);
		wSetLogfile.setSelection(jobentry.setLogfile);
		if (jobentry.logfile!=null) wLogfile.setText(jobentry.logfile);
		if (jobentry.logext!=null) wLogext.setText(jobentry.logext);
		wAddDate.setSelection(jobentry.addDate);
		wAddTime.setSelection(jobentry.addTime);

		wLoglevel.select(jobentry.loglevel+1);
	}
	
	private void cancel()
	{
		jobentry.setChanged(backupChanged);
		jobentry.setLogfile = backupLogfile;
		jobentry.addDate = backupDate;
		jobentry.addTime = backupTime;
		
		jobentry=null;
		dispose();
	}
	
	private void ok()
	{
		jobentry.setFileName(wFilename.getText());
		jobentry.setName(wName.getText());
		
		int nritems = wFields.nrNonEmpty();
		int nr = 0;
		for (int i=0;i<nritems;i++)
		{
			String arg = wFields.getNonEmpty(i).getText(1);
			if (arg!=null && arg.length()!=0) nr++;
		}
		jobentry.arguments = new String[nr];
		nr=0;
		for (int i=0;i<nritems;i++)
		{
			String arg = wFields.getNonEmpty(i).getText(1);
			if (arg!=null && arg.length()!=0) 
			{
				jobentry.arguments[nr]=arg;
				nr++;
			} 
		}

		jobentry.logfile=wLogfile.getText();
		jobentry.logext =wLogext.getText();
		jobentry.loglevel = wLoglevel.getSelectionIndex()-1;
		dispose();
	}
}
