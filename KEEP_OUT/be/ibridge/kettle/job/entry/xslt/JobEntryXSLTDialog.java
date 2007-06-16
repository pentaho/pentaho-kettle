 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 **                                                                   **
 **                                                                   **
 **********************************************************************/


package be.ibridge.kettle.job.entry.xslt;

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
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.WindowProperty;
import be.ibridge.kettle.core.util.StringUtil;
import be.ibridge.kettle.core.widget.TextVar;
import be.ibridge.kettle.job.JobMeta;
import be.ibridge.kettle.job.dialog.JobDialog;
import be.ibridge.kettle.job.entry.JobEntryDialogInterface;
import be.ibridge.kettle.job.entry.JobEntryInterface;
import be.ibridge.kettle.trans.step.BaseStepDialog;


/**
 * This dialog allows you to edit the XSLT job entry settings.
 *
 * @author Samatar Hassan
 * @since  02-03-2007
 */
public class JobEntryXSLTDialog extends Dialog implements JobEntryDialogInterface
{
   private static final String[] FILETYPES_XML = new String[] {
           Messages.getString("JobEntryXSLT.Filetype.Xml"),
		   Messages.getString("JobEntryXSLT.Filetype.All") };

	private static final String[] FILETYPES_XSL = new String[] 
		{
			Messages.getString("JobEntryXSLT.Filetype.Xsl"),
			Messages.getString("JobEntryXSLT.Filetype.Xslt"),
			Messages.getString("JobEntryXSLT.Filetype.All")};


	private Label        wlName;
	private Text         wName;
    private FormData     fdlName, fdName;

	private Label        wlxmlFilename;
	private Button       wbxmlFilename;
	private TextVar      wxmlFilename;
	private FormData     fdlxmlFilename, fdbxmlFilename, fdxmlFilename;

	private Label        wlxslFilename;
	private Button       wbxslFilename;
	private TextVar      wxslFilename;
	private FormData     fdlxslFilename, fdbxslFilename, fdxslFilename;

	private Label wlOutputFilename;
	private TextVar wOutputFilename;
	private FormData fdlOutputFilename, fdOutputFilename;

	private Label wlIfFileExists;
	private  CCombo wIfFileExists;
	private FormData fdlIfFileExists, fdIfFileExists;

	private Button wOK, wCancel;
	private Listener lsOK, lsCancel;

	private JobEntryXSLT jobEntry;
	private Shell       	shell;
	private Props       	props;

	private SelectionAdapter lsDef;

	private boolean changed;

	public JobEntryXSLTDialog(Shell parent, JobEntryXSLT jobEntry, JobMeta jobMeta)
	{
		super(parent, SWT.NONE);
		props=Props.getInstance();
		this.jobEntry=jobEntry;

		if (this.jobEntry.getName() == null)
			this.jobEntry.setName(Messages.getString("JobEntryXSLT.Name.Default"));
	}

	public JobEntryInterface open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

        shell = new Shell(parent, props.getJobsDialogStyle());
        props.setLook(shell);
        JobDialog.setShellImage(shell, jobEntry);

		ModifyListener lsMod = new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				jobEntry.setChanged();
			}
		};
		changed = jobEntry.hasChanged();

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(Messages.getString("JobEntryXSLT.Title"));

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Name line
		wlName=new Label(shell, SWT.RIGHT);
		wlName.setText(Messages.getString("JobEntryXSLT.Name.Label"));
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

		// Filename 1 line
		wlxmlFilename=new Label(shell, SWT.RIGHT);
		wlxmlFilename.setText(Messages.getString("JobEntryXSLT.xmlFilename.Label"));
 		props.setLook(wlxmlFilename);
		fdlxmlFilename=new FormData();
		fdlxmlFilename.left = new FormAttachment(0, 0);
		fdlxmlFilename.top  = new FormAttachment(wName, margin);
		fdlxmlFilename.right= new FormAttachment(middle, -margin);
		wlxmlFilename.setLayoutData(fdlxmlFilename);
		wbxmlFilename=new Button(shell, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbxmlFilename);
		wbxmlFilename.setText(Messages.getString("System.Button.Browse"));
		fdbxmlFilename=new FormData();
		fdbxmlFilename.right= new FormAttachment(100, 0);
		fdbxmlFilename.top  = new FormAttachment(wName, 0);
		wbxmlFilename.setLayoutData(fdbxmlFilename);
		wxmlFilename=new TextVar(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wxmlFilename);
		wxmlFilename.addModifyListener(lsMod);
		fdxmlFilename=new FormData();
		fdxmlFilename.left = new FormAttachment(middle, 0);
		fdxmlFilename.top  = new FormAttachment(wName, margin);
		fdxmlFilename.right= new FormAttachment(wbxmlFilename, -margin);
		wxmlFilename.setLayoutData(fdxmlFilename);

		// Whenever something changes, set the tooltip to the expanded version:
		wxmlFilename.addModifyListener(new ModifyListener()
			{
				public void modifyText(ModifyEvent e)
				{
					wxmlFilename.setToolTipText(StringUtil.environmentSubstitute( wxmlFilename.getText() ) );
				}
			}
		);

		wbxmlFilename.addSelectionListener
		(
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					FileDialog dialog = new FileDialog(shell, SWT.OPEN);
					dialog.setFilterExtensions(new String[] {"*.xml;*.XML", "*"});
					if (wxmlFilename.getText()!=null)
					{
						dialog.setFileName(StringUtil.environmentSubstitute(wxmlFilename.getText()) );
					}
					dialog.setFilterNames(FILETYPES_XML);
					if (dialog.open()!=null)
					{
						wxmlFilename.setText(dialog.getFilterPath()+Const.FILE_SEPARATOR+dialog.getFileName());
					}
				}
			}
		);

		// Filename 2 line
		wlxslFilename=new Label(shell, SWT.RIGHT);
		wlxslFilename.setText(Messages.getString("JobEntryXSLT.xslFilename.Label"));
 		props.setLook(wlxslFilename);
		fdlxslFilename=new FormData();
		fdlxslFilename.left = new FormAttachment(0, 0);
		fdlxslFilename.top  = new FormAttachment(wxmlFilename, margin);
		fdlxslFilename.right= new FormAttachment(middle, -margin);
		wlxslFilename.setLayoutData(fdlxslFilename);
		wbxslFilename=new Button(shell, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbxslFilename);
		wbxslFilename.setText(Messages.getString("System.Button.Browse"));
		fdbxslFilename=new FormData();
		fdbxslFilename.right= new FormAttachment(100, 0);
		fdbxslFilename.top  = new FormAttachment(wxmlFilename, 0);
		wbxslFilename.setLayoutData(fdbxslFilename);
		wxslFilename=new TextVar(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wxslFilename);
		wxslFilename.addModifyListener(lsMod);
		fdxslFilename=new FormData();
		fdxslFilename.left = new FormAttachment(middle, 0);
		fdxslFilename.top  = new FormAttachment(wxmlFilename, margin);
		fdxslFilename.right= new FormAttachment(wbxslFilename, -margin);
		wxslFilename.setLayoutData(fdxslFilename);

		// Whenever something changes, set the tooltip to the expanded version:
		wxslFilename.addModifyListener(new ModifyListener()
			{
				public void modifyText(ModifyEvent e)
				{
					wxslFilename.setToolTipText(StringUtil.environmentSubstitute( wxslFilename.getText() ) );
				}
			}
		);

		wbxslFilename.addSelectionListener
		(
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					FileDialog dialog = new FileDialog(shell, SWT.OPEN);
					dialog.setFilterExtensions(new String[] {"*.xsl;*.XSL", "*.xslt;*.XSLT", "*"});
					if (wxslFilename.getText()!=null)
					{
						dialog.setFileName(StringUtil.environmentSubstitute(wxslFilename.getText()) );
					}
					dialog.setFilterNames(FILETYPES_XSL);
					if (dialog.open()!=null)
					{
						wxslFilename.setText(dialog.getFilterPath()+Const.FILE_SEPARATOR+dialog.getFileName());
					}
				}
			}
		);



	
	
		// OutputFilename
		wlOutputFilename = new Label(shell, SWT.RIGHT);
		wlOutputFilename.setText(Messages.getString("JobEntryXSLT.OutputFilename.Label"));
		props.setLook(wlOutputFilename);
		fdlOutputFilename = new FormData();
		fdlOutputFilename.left = new FormAttachment(0, 0);
		fdlOutputFilename.top = new FormAttachment(wxslFilename, margin);
		fdlOutputFilename.right = new FormAttachment(middle, -margin);
		wlOutputFilename.setLayoutData(fdlOutputFilename);
		wOutputFilename = new TextVar(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wOutputFilename);
		wOutputFilename.addModifyListener(lsMod);
		fdOutputFilename = new FormData();
		fdOutputFilename.left = new FormAttachment(middle, 0);
		fdOutputFilename.top = new FormAttachment(wxslFilename, margin);
		fdOutputFilename.right = new FormAttachment(100, 0);
		wOutputFilename.setLayoutData(fdOutputFilename);



		// Whenever something changes, set the tooltip to the expanded version:
		wOutputFilename.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				wOutputFilename.setToolTipText(StringUtil.environmentSubstitute( wOutputFilename.getText() ) );
			}
		}
			);

		

		//IF File Exists
		wlIfFileExists = new Label(shell, SWT.RIGHT);
		wlIfFileExists.setText(Messages.getString("JobEntryXSLT.IfZipFileExists.Label"));
		props.setLook(wlIfFileExists);
		fdlIfFileExists = new FormData();
		fdlIfFileExists.left = new FormAttachment(0, 0);
		fdlIfFileExists.right = new FormAttachment(middle, 0);
		fdlIfFileExists.top = new FormAttachment(wOutputFilename, margin);
		wlIfFileExists.setLayoutData(fdlIfFileExists);
		wIfFileExists = new CCombo(shell, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
		wIfFileExists.add(Messages.getString("JobEntryXSLT.Create_NewFile_IfFileExists.Label"));
		wIfFileExists.add(Messages.getString("JobEntryXSLT.Do_Nothing_IfFileExists.Label"));
		wIfFileExists.add(Messages.getString("JobEntryXSLT.Fail_IfFileExists.Label"));
		wIfFileExists.select(1); // +1: starts at -1

		props.setLook(wIfFileExists);
		fdIfFileExists= new FormData();
		fdIfFileExists.left = new FormAttachment(middle, 0);
		fdIfFileExists.top = new FormAttachment(wOutputFilename, margin);
		fdIfFileExists.right = new FormAttachment(100, 0);
		wIfFileExists.setLayoutData(fdIfFileExists);

		fdIfFileExists = new FormData();
		fdIfFileExists.left = new FormAttachment(middle, 0);
		fdIfFileExists.top = new FormAttachment(wOutputFilename, margin);
		fdIfFileExists.right = new FormAttachment(100, 0);
		wIfFileExists.setLayoutData(fdIfFileExists);


        wOK = new Button(shell, SWT.PUSH);
        wOK.setText(Messages.getString("System.Button.OK"));
        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(Messages.getString("System.Button.Cancel"));

		BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, wIfFileExists);

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };

		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener    (SWT.Selection, lsOK    );

		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };

		wName.addSelectionListener( lsDef );
		wxmlFilename.addSelectionListener( lsDef );
		wxslFilename.addSelectionListener( lsDef );


		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		getData();

		BaseStepDialog.setSize(shell);

		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return jobEntry;
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
		if (jobEntry.getName()    != null) wName.setText( jobEntry.getName() );
		wName.selectAll();
		if (jobEntry.getxmlFilename()!= null) wxmlFilename.setText( jobEntry.getxmlFilename() );
		if (jobEntry.getxslFilename()!= null) wxslFilename.setText( jobEntry.getxslFilename() );
		if (jobEntry.getoutputFilename()!= null) wOutputFilename.setText( jobEntry.getoutputFilename() );
		
		if (jobEntry.iffileexists>=0) 
		{
			wIfFileExists.select(jobEntry.iffileexists );
		}
		else
		{
			wIfFileExists.select(2); // NOTHING
		}

	}

	private void cancel()
	{
		jobEntry.setChanged(changed);
		jobEntry=null;
		dispose();
	}

	private void ok()
	{
		jobEntry.setName(wName.getText());
		jobEntry.setxmlFilename(wxmlFilename.getText());
		jobEntry.setxslFilename(wxslFilename.getText());
		jobEntry.setoutputFilename(wOutputFilename.getText());
		jobEntry.iffileexists = wIfFileExists.getSelectionIndex();

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