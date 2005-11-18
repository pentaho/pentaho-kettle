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


package be.ibridge.kettle.chef.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.repository.RepositoryDirectory;
import be.ibridge.kettle.repository.dialog.SelectDirectoryDialog;


/**
 * 
 * On page one we select the name of the target job and the directory.
 * 
 * @author Matt
 * @since  17-apr-04
 */
public class RipDatabaseWizardPage3 extends WizardPage
{
	private Label        wlJobname;
	private Text         wJobname;
    private FormData     fdlJobname, fdJobname;

    private Label        wlDirectory;
	private Text         wDirectory;
	private Button       wbDirectory;
    private FormData     fdlDirectory, fdbDirectory, fdDirectory;    

	private Props props;
	private Repository rep;
	private RepositoryDirectory directory;
	private Shell shell;

    /** @deprecated */
    public RipDatabaseWizardPage3(String arg, LogWriter log, Props props, Repository rep)
    {
        this(arg, rep);
    }
    
	public RipDatabaseWizardPage3(String arg, Repository rep)
	{
		super(arg);
		this.props=Props.getInstance();
		this.rep = rep;
	
		setTitle("Enter the job details");
		setDescription("Enter the name of the target job and the directory to put everything in.");
		
		setPageComplete(false);
	}
	
	public void createControl(Composite parent)
	{
		shell = parent.getShell();

        int margin = Const.MARGIN;
		int middle = props.getMiddlePct();
		
		ModifyListener lsMod = new ModifyListener()
		{
			public void modifyText(ModifyEvent arg0)
			{
				setPageComplete(canFlipToNextPage());
			}
		};
		
		// create the composite to hold the widgets
		Composite composite = new Composite(parent, SWT.NONE);
 		props.setLook(composite);
	    
	    FormLayout compLayout = new FormLayout();
	    compLayout.marginHeight = Const.FORM_MARGIN;
	    compLayout.marginWidth  = Const.FORM_MARGIN;
		composite.setLayout(compLayout);

		// Job name:
		wlJobname=new Label(composite, SWT.RIGHT);
		wlJobname.setText("Job name :");
 		props.setLook(wlJobname);
		fdlJobname=new FormData();
		fdlJobname.left = new FormAttachment(0, 0);
		fdlJobname.right= new FormAttachment(middle, -margin);
		fdlJobname.top  = new FormAttachment(0, margin);
		wlJobname.setLayoutData(fdlJobname);
		wJobname=new Text(composite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wJobname);
		wJobname.addModifyListener(lsMod);
		fdJobname=new FormData();
		fdJobname.left = new FormAttachment(middle, 0);
		fdJobname.top  = new FormAttachment(0, margin);
		fdJobname.right= new FormAttachment(100, 0);
		wJobname.setLayoutData(fdJobname);
		
		// Directory:
		wlDirectory=new Label(composite, SWT.RIGHT);
		wlDirectory.setText("Directory :");
 		props.setLook(wlDirectory);
		fdlDirectory=new FormData();
		fdlDirectory.left = new FormAttachment(0, 0);
		fdlDirectory.right= new FormAttachment(middle, -margin);
		fdlDirectory.top  = new FormAttachment(wJobname, margin);
		wlDirectory.setLayoutData(fdlDirectory);

		wbDirectory=new Button(composite, SWT.PUSH);
		wbDirectory.setText("...");
 		props.setLook(wbDirectory);
		fdbDirectory=new FormData();
		fdbDirectory.right= new FormAttachment(100, 0);
		fdbDirectory.top  = new FormAttachment(wJobname, margin);
		wbDirectory.setLayoutData(fdbDirectory);
		wbDirectory.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				SelectDirectoryDialog sdd = new SelectDirectoryDialog(shell, SWT.NONE, rep);
				directory = sdd.open();
				if (directory!=null)
				{
					wDirectory.setText(directory.getPath());
					setPageComplete(canFlipToNextPage());
				}
			}
		});

		wDirectory=new Text(composite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wDirectory);
		wDirectory.setEditable(false);
		fdDirectory=new FormData();
		fdDirectory.left = new FormAttachment(middle, 0);
		fdDirectory.top  = new FormAttachment(wJobname, margin);
		fdDirectory.right= new FormAttachment(wbDirectory, 0);
		wDirectory.setLayoutData(fdDirectory);
		
		// set the composite as the control for this page
		setControl(composite);
	}
		
	public boolean canFlipToNextPage()
	{
		return false;  
	}	
	
	public String getJobname()
	{
		String jobname = wJobname.getText();
		if (jobname!=null && jobname.length()==0) jobname=null;
		
		return jobname;
	}

	/**
	 * @return Returns the directory.
	 */
	public RepositoryDirectory getDirectory()
	{
		return directory;
	}
	
	public boolean canFinish()
	{
		return getJobname()!=null && getDirectory()!=null;
	}
}
