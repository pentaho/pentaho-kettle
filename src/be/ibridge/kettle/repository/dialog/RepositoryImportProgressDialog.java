/*
 *
 *
 */

package be.ibridge.kettle.repository.dialog;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.WindowProperty;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.dialog.ErrorDialog;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.job.JobMeta;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.repository.RepositoryDirectory;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStepDialog;


/**
 * Takes care of displaying a dialog that will handle the wait while we are importing a backup file from XML...
 * 
 * @author Matt
 * @since  03-jun-2005
 */
public class RepositoryImportProgressDialog extends Dialog
{
	private LogWriter log;
	private Shell     shell, parent;
	private Display   display;
	
	private Props props;
	private Repository rep;
	private String filename;
	private RepositoryDirectory baseDirectory;
	
	private ProgressBar wBar;
	private Label       wLabel;
	private Text        wLogging;
	private Button      wClose;

    /**
     * @deprecated
     */
	public RepositoryImportProgressDialog(Shell parent, int style, LogWriter log, Props props, Repository rep, String filename, RepositoryDirectory baseDirectory)
	{
	    super(parent, style);
	    
		this.log = log;
		this.props = props;
		this.parent = parent;
		this.rep = rep;
		this.filename = filename;
		this.baseDirectory = baseDirectory;
	}
    
    public RepositoryImportProgressDialog(Shell parent, int style, Repository rep, String filename, RepositoryDirectory baseDirectory)
    {
        super(parent, style);
        
        this.log   = LogWriter.getInstance();
        this.props = Props.getInstance();
        this.parent = parent;
        this.rep = rep;
        this.filename = filename;
        this.baseDirectory = baseDirectory;
    }

	
	public void open()
	{
	    display = parent.getDisplay();
	    
		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );
 		props.setLook(shell);
		
		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;
		
		shell.setText("Import repository objects from XML");
		shell.setLayout (formLayout);
 		
		//
		// The progress bar on top...
		////////////////////////////////////////////////////////////////////
		wBar = new ProgressBar(shell, SWT.HORIZONTAL);
 		props.setLook(wBar);
		
		FormData fdBar = new FormData();
		fdBar.left = new FormAttachment(0,0);
		fdBar.top = new FormAttachment(0,0);
		fdBar.right = new FormAttachment(100,0);
		wBar.setLayoutData(fdBar);

		// 
		// Then the task line...
		////////////////////////////////////////////////////////////////////
		
		wLabel = new Label(shell, SWT.LEFT);
 		props.setLook(wLabel);
		
		FormData fdLabel = new FormData();
		fdLabel.left = new FormAttachment(0,0);
		fdLabel.top = new FormAttachment(wBar, Const.MARGIN);
		fdLabel.right = new FormAttachment(100,0);
		wLabel.setLayoutData(fdLabel);
		
		
		//
		// The close button...
		////////////////////////////////////////////////////////////////////
		
		// Buttons
		wClose  = new Button(shell, SWT.PUSH); 
		wClose.setText(" &Close ");
		
		BaseStepDialog.positionBottomButtons(shell, new Button[] { wClose }, Const.MARGIN, (Control)null);

		wClose.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { dispose(); } });
		
		// 
		// Then the logging...
		////////////////////////////////////////////////////////////////////
		
		wLogging = new Text(shell, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
 		props.setLook(wLabel);
		
		FormData fdLogging = new FormData();
		fdLogging.left = new FormAttachment(0,0);
		fdLogging.top = new FormAttachment(wLabel, Const.MARGIN);
		fdLogging.right = new FormAttachment(100,0);
		fdLogging.bottom = new FormAttachment(wClose, -Const.MARGIN);
		wLogging.setLayoutData(fdLogging);
	
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { dispose(); } } );
		
		WindowProperty winprop = props.getScreen(shell.getText());
		if (winprop!=null) winprop.setShell(shell); else shell.pack();
		
		shell.open();
		
		display.asyncExec(
		    new Runnable()
	        {
	            public void run()
	            {
	                importAll();
	            }
	        }
		);
		
		while (!shell.isDisposed()) 
		{
			if (!display.readAndDispatch()) display.sleep();
		}
	}
	
	public void dispose()
	{
		props.setScreen(new WindowProperty(shell));
		shell.dispose();
	}
		
	private void addLog(String line)
	{
	    String rest = wLogging.getText();
	    wLogging.setText(rest+line+Const.CR);
	}
	
	private void importAll()
	{
		wLabel.setText("Importing repository objects from an XML file");
		try
		{
			boolean overwrite     = false;
			boolean askOverwrite  = true;
			boolean makeDirectory = false;
			boolean askDirectory  = true;
			
			addLog("Import objects from file ["+filename+"]");
			
			// To where?
			wLabel.setText("Asking in which directory to put the objects...");
			
		    Document doc = XMLHandler.loadXMLFile(filename);
			if (doc!=null)
			{
				//
				// HERE WE START
				//
				Node repnode    = XMLHandler.getSubNode(doc,      "repository");						
				Node transsnode = XMLHandler.getSubNode(repnode, "transformations");
				if (transsnode!=null) // Load transformations...
				{
					int nrtrans = XMLHandler.countNodes(transsnode, "transformation");
					
					wBar.setMinimum(0);
					wBar.setMaximum(nrtrans);
					for (int i=0;i<nrtrans;i++)
					{
					    wBar.setSelection(i+1);
						Node transnode = XMLHandler.getSubNodeByNr(transsnode, "transformation", i);
						
						//
						// Load transformation from XML into a directory, possibly created!
						//
						TransMeta ti = new TransMeta(transnode);

						wLabel.setText("Importing transformation "+(i+1)+"/"+nrtrans+" : "+ti.getName());

						// What's the directory path?
						String directoryPath = XMLHandler.getTagValue(transnode, "info", "directory");
						// remove the leading root, we never don't need it.
						directoryPath = directoryPath.substring(1);
						
						RepositoryDirectory targetDirectory = baseDirectory.findDirectory(directoryPath);
						if (targetDirectory==null)
						{
						    if (askDirectory)
						    {
							    MessageDialogWithToggle mb = new MessageDialogWithToggle(shell, 
										 "Create directory?", 
										 null,
										 "The Directory ["+directoryPath+"] doesn't exists."+Const.CR+"Do you want me to create this directory?",
										 MessageDialog.QUESTION,
										 new String[] { "Yes", "No", "Cancel" },
										 1,
										 "Don't ask me again.",
										 !askDirectory
										 );
								int answer = mb.open();
								makeDirectory = answer==0;
								askDirectory = !mb.getToggleState();
								
								// Cancel?
								if (answer==2) return;
						    }

						    if (makeDirectory)
						    {
						        addLog("Creating directory ["+directoryPath+"] in directory ["+baseDirectory+"]");
						        targetDirectory = baseDirectory.createDirectory(rep, directoryPath);
						    }
						    else
						    {
						        targetDirectory = baseDirectory;
						    }
						}
						
						// OK, we loaded the transformation from XML and all went well...
						// See if the transformation already existed!
						long id = rep.getTransformationID(ti.getName(), targetDirectory.getID());
						if (id>0 && askOverwrite)
						{
							MessageDialogWithToggle md = new MessageDialogWithToggle(shell, 
																					 "["+ti.getName()+"]", 
																					 null,
																					 "The transformation ["+ti.getName()+"] already exists in the repository."+Const.CR+"Do you want to overwrite the transformation?"+Const.CR,
																					 MessageDialog.QUESTION,
																					 new String[] { "Yes", "No" },
																					 1,
																					 "Don't ask me again.",
																					 !askOverwrite
																					 );
							int answer = md.open();
							overwrite = answer==0;
							askOverwrite = !md.getToggleState();
						}
						
						if (id<=0 || overwrite)
						{
							ti.setDirectory( targetDirectory ) ;
							
							ti.saveRep(rep);
							addLog("Saved transformation #"+i+" in the repository: ["+ti.getName()+"]");
						}
						else
						{
						    addLog("We didn't save transformation ["+ti.getName()+"]");
						}
					}
				}
				
				// Ask again for the jobs...
				overwrite = false;
				askOverwrite       = true;
				
				Node jobsnode = XMLHandler.getSubNode(repnode, "jobs");
				if (jobsnode!=null) // Load jobs...
				{
					int nrjobs = XMLHandler.countNodes(transsnode, "job");

					wBar.setMinimum(0);
					wBar.setMaximum(nrjobs);
					for (int i=0;i<nrjobs;i++)
					{
					    wBar.setSelection(i+1);
						Node jobnode = XMLHandler.getSubNodeByNr(jobsnode, "job", i);
						
						// Load the job from the XML node.
						JobMeta ji = new JobMeta(log, jobnode);
						
						wLabel.setText("Importing job "+(i+1)+"/"+nrjobs+" : "+ji.getName());

						// What's the directory path?
						String directoryPath = Const.NVL(XMLHandler.getTagValue(jobnode, "directory"), Const.FILE_SEPARATOR);
						
						RepositoryDirectory targetDirectory = baseDirectory.findDirectory(directoryPath);
						if (targetDirectory==null)
						{
						    if (askDirectory)
						    {
							    MessageDialogWithToggle mb = new MessageDialogWithToggle(shell, 
										 "Create directory?", 
										 null,
										 "The Directory ["+directoryPath+"] doesn't exists."+Const.CR+"Do you want me to create this directory?",
										 MessageDialog.QUESTION,
										 new String[] { "Yes", "No", "Cancel" },
										 1,
										 "Don't ask me again.",
										 !askDirectory
										 );
								int answer = mb.open();
								makeDirectory = answer==0;
								askDirectory = !mb.getToggleState();
								
								// Cancel?
								if (answer==2) return;
						    }

						    if (makeDirectory)
						    {
						        addLog("Creating directory ["+directoryPath+"] in directory ["+baseDirectory+"]");
						        targetDirectory = baseDirectory.createDirectory(rep, directoryPath);
						    }
						    else
						    {
						        targetDirectory = baseDirectory;
						    }										
						}
						
						// OK, we loaded the job from XML and all went well...
						// See if the job already exists!
						long id = rep.getJobID(ji.getName(), targetDirectory.getID());
						if (id>0 && askOverwrite)
						{
							MessageDialogWithToggle md = new MessageDialogWithToggle(shell, 
																					 "["+ji.getName()+"]", 
																					 null,
																					 "The job ["+ji.getName()+"] already exists in the repository."+Const.CR+"Do you want to overwrite the job?"+Const.CR,
																					 MessageDialog.QUESTION,
																					 new String[] { "Yes", "No" },
																					 1,
																					 "Don't ask me again.",
																					 askOverwrite
																					 );
							int answer = md.open();
							overwrite = answer==0;
							askOverwrite = md.getToggleState();
						}
						
						if (id<=0 || overwrite)
						{
						    ji.setDirectory(targetDirectory);
							ji.saveRep(rep);
							addLog("Saved job #"+i+" in the repository: ["+ji.getName()+"]");
						}
						else
						{
						    addLog("We didn't save job ["+ji.getName()+"]");
						}
					}
				}
				addLog("Finished importing.");
			}
			else
			{
				MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
				mb.setMessage("Sorry, this is not a valid XML file."+Const.CR+"Please check the log for more information.");
				mb.setText("ERROR");
				mb.open();
			}
		}
		catch(KettleException e)
		{
			new ErrorDialog(shell, props, "Error importing repository objects", "There was an error while importing repository objects from an XML file", e);
		}
	}
}



