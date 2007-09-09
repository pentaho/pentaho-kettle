 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 **                                                                   **
 **                                                                   **
 **********************************************************************/

package be.ibridge.kettle.job.entry.dtdvalidator;

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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import be.ibridge.kettle.core.util.StringUtil;
import org.eclipse.swt.widgets.Text;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.job.JobMeta;
import be.ibridge.kettle.core.WindowProperty;
import be.ibridge.kettle.core.widget.TextVar;
import be.ibridge.kettle.job.dialog.JobDialog;
import be.ibridge.kettle.job.entry.JobEntryDialogInterface;
import be.ibridge.kettle.job.entry.JobEntryInterface;
import be.ibridge.kettle.trans.step.BaseStepDialog;


/**
 * This dialog allows you to edit the DTD Validator job entry settings.
 *
 * @author Samatar Hassan
 * @since  30-04-2007
 */

public class JobEntryDTDValidatorDialog extends Dialog implements JobEntryDialogInterface
{
   private static final String[] FILETYPES_XML = new String[] {
           Messages.getString("JobEntryDTDValidator.Filetype.Xml"),
		   Messages.getString("JobEntryDTDValidator.Filetype.All") };

	private static final String[] FILETYPES_DTD = new String[] 
		{
			Messages.getString("JobEntryDTDValidator.Filetype.Dtd"),
			Messages.getString("JobEntryDTDValidator.Filetype.All")};


	private Label        wlName;
	private Text         wName;
    private FormData     fdlName, fdName;

	private Label        wlxmlFilename;
	private Button       wbxmlFilename;
	private TextVar      wxmlFilename;
	private FormData     fdlxmlFilename, fdbxmlFilename, fdxmlFilename;

	private Label        wldtdFilename;
	private Button       wbdtdFilename;
	private TextVar      wdtdFilename;
	private FormData     fdldtdFilename, fdbdtdFilename, fddtdFilename;


	private Button wOK, wCancel;
	private Listener lsOK, lsCancel;

	private JobEntryDTDValidator jobEntry;
	private Shell       	shell;
	private Props       	props;


	private SelectionAdapter lsDef;
	
	private boolean changed;

	
    public JobEntryDTDValidatorDialog(Shell parent, JobEntryDTDValidator jobEntry, JobMeta jobMeta)
    {
    	super(parent, SWT.NONE);
		props=Props.getInstance();
		this.jobEntry=jobEntry;

        if (this.jobEntry.getName() == null)
			this.jobEntry.setName(Messages.getString("JobEntryDTDValidator.Name.Default"));
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
		shell.setText(Messages.getString("JobEntryDTDValidator.Title"));

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Name line
		wlName=new Label(shell, SWT.RIGHT);
		wlName.setText(Messages.getString("JobEntryDTDValidator.Name.Label"));
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

		// XML Filename
		wlxmlFilename=new Label(shell, SWT.RIGHT);
		wlxmlFilename.setText(Messages.getString("JobEntryDTDValidator.xmlFilename.Label"));
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

		// DTD Filename
		wldtdFilename=new Label(shell, SWT.RIGHT);
		wldtdFilename.setText(Messages.getString("JobEntryDTDValidator.DTDFilename.Label"));
 		props.setLook(wldtdFilename);
		fdldtdFilename=new FormData();
		fdldtdFilename.left = new FormAttachment(0, 0);
		fdldtdFilename.top  = new FormAttachment(wxmlFilename, margin);
		fdldtdFilename.right= new FormAttachment(middle, -margin);
		wldtdFilename.setLayoutData(fdldtdFilename);
		wbdtdFilename=new Button(shell, SWT.PUSH| SWT.CENTER);
 		props.setLook(wbdtdFilename);
		wbdtdFilename.setText(Messages.getString("System.Button.Browse"));
		fdbdtdFilename=new FormData();
		fdbdtdFilename.right= new FormAttachment(100, 0);
		fdbdtdFilename.top  = new FormAttachment(wxmlFilename, 0);
		wbdtdFilename.setLayoutData(fdbdtdFilename);
		wdtdFilename=new TextVar(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wdtdFilename);
		wdtdFilename.addModifyListener(lsMod);
		fddtdFilename=new FormData();
		fddtdFilename.left = new FormAttachment(middle, 0);
		fddtdFilename.top  = new FormAttachment(wxmlFilename, margin);
		fddtdFilename.right= new FormAttachment(wbdtdFilename, -margin);
		wdtdFilename.setLayoutData(fddtdFilename);

		// Whenever something changes, set the tooltip to the expanded version:
		wdtdFilename.addModifyListener(new ModifyListener()
			{
				public void modifyText(ModifyEvent e)
				{
					wdtdFilename.setToolTipText(StringUtil.environmentSubstitute( wdtdFilename.getText() ) );
				}
			}
		);

		wbdtdFilename.addSelectionListener
		(
			new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					FileDialog dialog = new FileDialog(shell, SWT.OPEN);
					dialog.setFilterExtensions(new String[] {"*.dtd;*.DTD","*"});
					if (wdtdFilename.getText()!=null)
					{
						dialog.setFileName(StringUtil.environmentSubstitute(wdtdFilename.getText()) );
					}
					dialog.setFilterNames(FILETYPES_DTD);
					if (dialog.open()!=null)
					{
						wdtdFilename.setText(dialog.getFilterPath()+Const.FILE_SEPARATOR+dialog.getFileName());
					}
				}
			}
		);



        wOK = new Button(shell, SWT.PUSH);
        wOK.setText(Messages.getString("System.Button.OK"));
        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(Messages.getString("System.Button.Cancel"));

		BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, wdtdFilename);

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };

		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener    (SWT.Selection, lsOK    );

		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };

		wName.addSelectionListener( lsDef );
		wxmlFilename.addSelectionListener( lsDef );
		wdtdFilename.addSelectionListener( lsDef );


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
		if (jobEntry.getdtdFilename()!= null) wdtdFilename.setText( jobEntry.getdtdFilename() );		
		

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
		jobEntry.setdtdFilename(wdtdFilename.getText());


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