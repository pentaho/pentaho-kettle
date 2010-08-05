/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
/*
 *
 *
 */

package org.pentaho.di.ui.repository.dialog;

import java.util.Date;

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
import org.pentaho.di.ui.core.gui.GUIResource;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.ui.repository.dialog.Messages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Node;


/**
 * Takes care of displaying a dialog that will handle the wait while we are importing a backup file
 * from XML...
 * 
 * @author Matt
 * @since 03-jun-2005
 */
public class RepositoryImportProgressDialog extends Dialog
{
    private LogWriter log;
    private Shell shell, parent;
    private Display display;
    private PropsUI props;

    private Repository rep;
    private String fileDirectory;
    private String[] filenames;
    private RepositoryDirectory baseDirectory;
    private ProgressBar wBar;
    private Label wLabel;
    private Text wLogging;
    private Button wClose;

    private boolean overwrite = false;
    private boolean askOverwrite = true;
    private boolean makeDirectory = false;
    private boolean askDirectory = true;
    private int nrtrans;
    private int nrjobs;
    private int nrdirs;

    public RepositoryImportProgressDialog(Shell parent, int style, Repository rep, String fileDirectory, String[] filenames, RepositoryDirectory baseDirectory)
    {
        super(parent, style);

        this.log = LogWriter.getInstance();
        this.props = PropsUI.getInstance();
        this.parent = parent;
        this.rep = rep;
        this.fileDirectory = fileDirectory;
        this.filenames = filenames;
        this.baseDirectory = baseDirectory;
        
        rep.setImportBaseDirectory(baseDirectory);
    }

    public void open()
    {
        display = parent.getDisplay();

        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
        props.setLook(shell);

        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;

        shell.setText(Messages.getString("RepositoryImportDialog.Title"));
        shell.setImage(GUIResource.getInstance().getImageSpoon());
        shell.setLayout(formLayout);

        //
        // The progress bar on top...
        //////////////////////////////////////////////////////////////////
        wBar = new ProgressBar(shell, SWT.HORIZONTAL);
        props.setLook(wBar);

        FormData fdBar = new FormData();
        fdBar.left = new FormAttachment(0, 0);
        fdBar.top = new FormAttachment(0, 0);
        fdBar.right = new FormAttachment(100, 0);
        wBar.setLayoutData(fdBar);

        // 
        // Then the task line...
        //////////////////////////////////////////////////////////////////

        wLabel = new Label(shell, SWT.LEFT);
        props.setLook(wLabel);

        FormData fdLabel = new FormData();
        fdLabel.left = new FormAttachment(0, 0);
        fdLabel.top = new FormAttachment(wBar, Const.MARGIN);
        fdLabel.right = new FormAttachment(100, 0);
        wLabel.setLayoutData(fdLabel);

        //
        // The close button...
        //////////////////////////////////////////////////////////////////

        // Buttons
        wClose = new Button(shell, SWT.PUSH);
        wClose.setText(Messages.getString("System.Button.Close"));

        BaseStepDialog.positionBottomButtons(shell, new Button[] { wClose }, Const.MARGIN, (Control) null);

        wClose.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { dispose(); } });

        // 
        // Then the logging...
        //////////////////////////////////////////////////////////////////

        wLogging = new Text(shell, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        props.setLook(wLabel);

        FormData fdLogging = new FormData();
        fdLogging.left = new FormAttachment(0, 0);
        fdLogging.top = new FormAttachment(wLabel, Const.MARGIN);
        fdLogging.right = new FormAttachment(100, 0);
        fdLogging.bottom = new FormAttachment(wClose, -Const.MARGIN);
        wLogging.setLayoutData(fdLogging);

        // Detect X or ALT-F4 or something that kills this window...
        shell.addShellListener( new ShellAdapter() { public void shellClosed(ShellEvent e) { dispose(); } } );

        BaseStepDialog.setSize(shell, 640, 480, true);

        shell.open();

        display.asyncExec(new Runnable()
        {
            public void run()
            {
                importAll();
            }
        });

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
        wLogging.setText(rest + line + Const.CR);
        wLogging.setSelection(wLogging.getText().length()); // make it scroll
    }

    /**
     * 
     * @param transformationNumber the transformation number (for logging only)
     * @param transnode The XML DOM node to read the transformation from
     * @return false if the import should be canceled.
     * @throws KettleException in case there is an unexpected error
     */
    private boolean importTransformation(int transformationNumber, Node transnode) throws KettleException
    {
    	//
    	// Load transformation from XML into a directory, possibly created!
    	//
    	TransMeta ti = new TransMeta(transnode, rep);

    	wLabel.setText(Messages.getString("RepositoryImportDialog.ImportTrans.Label", Integer.toString(transformationNumber), Integer.toString(nrtrans), ti.getName()));

    	// What's the directory path?
    	String directoryPath = XMLHandler.getTagValue(transnode, "info", "directory");
    	// remove the leading root, we never don't need it.
    	directoryPath = directoryPath.substring(1);

    	RepositoryDirectory targetDirectory = baseDirectory.findDirectory(directoryPath);
    	if (targetDirectory == null)
    	{
    		if (askDirectory)
    		{
    			MessageDialogWithToggle mb = new MessageDialogWithToggle(shell,
    					Messages.getString("RepositoryImportDialog.CreateDir.Title"),
    					null,
    					Messages.getString("RepositoryImportDialog.CreateDir.Message", directoryPath),
    					MessageDialog.QUESTION,
    					new String[] {
    				Messages.getString("System.Button.Yes"),
    				Messages.getString("System.Button.No"),
    				Messages.getString("System.Button.Cancel") },
    				1,
    				Messages.getString("RepositoryImportDialog.DontAskAgain.Label"),
    				!askDirectory);
    			MessageDialogWithToggle.setDefaultImage(GUIResource.getInstance().getImageSpoon());
    			int answer = mb.open();
    			makeDirectory = (answer & 0xFF) == 0;
    			askDirectory = !mb.getToggleState();

    			// Cancel?
    			if ((answer & 0xFF) == 1)
    				return false;
    		}

    		if (makeDirectory)
    		{
    			addLog(Messages.getString("RepositoryImportDialog.CreateDir.Log", directoryPath, baseDirectory.toString()));
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
    	if (id > 0 && askOverwrite)
    	{
    		MessageDialogWithToggle md = new MessageDialogWithToggle(shell,
    				Messages.getString("RepositoryImportDialog.OverwriteTrans.Title"),
    				null,
    				Messages.getString("RepositoryImportDialog.OverwriteTrans.Message", ti.getName()),
    				MessageDialog.QUESTION,
    				new String[] { Messages.getString("System.Button.Yes"),
    			Messages.getString("System.Button.No") },
    			1,
    			Messages.getString("RepositoryImportDialog.DontAskAgain.Label"),
    			!askOverwrite);
    		MessageDialogWithToggle.setDefaultImage(GUIResource.getInstance().getImageSpoon());
    		int answer = md.open();
    		overwrite = (answer & 0xFF) == 0;
    		askOverwrite = !md.getToggleState();
    	}
    	else {
    		shell.getDisplay().update();
    	}

    	if (id <= 0 || overwrite)
    	{
    		ti.setDirectory(targetDirectory);

    		try
    		{
    			// Keep info on who & when this transformation was created...
    			if (  ti.getCreatedUser()==null || ti.getCreatedUser().equals("-"))
    			{
					ti.setCreatedDate( new Date() );   
    				ti.setCreatedUser( rep.getUserInfo().getLogin() );
    			}
    			
    			ti.saveRep(rep);
    			addLog(Messages.getString("RepositoryImportDialog.TransSaved.Log", Integer.toString(transformationNumber), ti.getName()));
    		}
    		catch (Exception e)
    		{
    			addLog(Messages.getString("RepositoryImportDialog.ErrorSavingTrans.Log", Integer.toString(transformationNumber), ti.getName(), e.toString()));
    			addLog(Const.getStackTracker(e));
    		}
    	}
    	else
    	{
    		addLog(Messages.getString("RepositoryImportDialog.ErrorSavingTrans2.Log", ti.getName()));
    	}
    	return true;
    }
	
	private boolean importJob(int nrthis, Node jobnode) throws KettleException
	{
        // Load the job from the XML node.
                        JobMeta ji = new JobMeta(log, jobnode, rep, SpoonFactory.getInstance());

        wLabel.setText(Messages.getString("RepositoryImportDialog.ImportJob.Label", Integer.toString(nrthis), Integer.toString(nrjobs), ji.getName()));

        // What's the directory path?
        String directoryPath = Const.NVL(XMLHandler.getTagValue(jobnode, "directory"), Const.FILE_SEPARATOR);

        RepositoryDirectory targetDirectory = baseDirectory.findDirectory(directoryPath);
        if (targetDirectory == null)
        {
            if (askDirectory)
            {
                MessageDialogWithToggle mb = new MessageDialogWithToggle(shell,
                    Messages.getString("RepositoryImportDialog.CreateDir.Title"),
                    null,
                    Messages.getString("RepositoryImportDialog.CreateDir.Message", directoryPath),
                    MessageDialog.QUESTION,
                    new String[] {
                                  Messages.getString("System.Button.Yes"),
                                  Messages.getString("System.Button.No"),
                                  Messages.getString("System.Button.Cancel") },
                    1,
                    Messages.getString("RepositoryImportDialog.DontAskAgain.Label"),
                    !askDirectory);
                MessageDialogWithToggle.setDefaultImage(GUIResource.getInstance().getImageSpoon());
                int answer = mb.open();
                makeDirectory = ((answer & 0xFF) == 0);
                askDirectory = !mb.getToggleState();

                // Cancel?
                if ((answer & 0xFF) == 2)
                    return false;
            }

            if (makeDirectory)
            {
                addLog(Messages.getString("RepositoryImportDialog.CreateDir.Log", directoryPath, baseDirectory.toString()));
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
        if (id > 0 && askOverwrite)
        {
            MessageDialogWithToggle md = new MessageDialogWithToggle(shell,
                Messages.getString("RepositoryImportDialog.OverwriteJob.Title"),
                null,
                Messages.getString("RepositoryImportDialog.OverwriteJob.Message", ji.getName()),
                MessageDialog.QUESTION,
                new String[] { Messages.getString("System.Button.Yes"),
                              Messages.getString("System.Button.No") },
                1,
                Messages.getString("RepositoryImportDialog.DontAskAgain.Label"),
                !askOverwrite);
            MessageDialogWithToggle.setDefaultImage(GUIResource.getInstance().getImageSpoon());
            int answer = md.open();
            overwrite = (answer & 0xFF) == 0;
            askOverwrite = !md.getToggleState();
        }
		else {
			shell.getDisplay().update();
		}

        if (id <= 0 || overwrite)
        {
            ji.setDirectory(targetDirectory);
            ji.saveRep(rep);
            addLog(Messages.getString("RepositoryImportDialog.JobSaved.Log", Integer.toString(nrthis), ji.getName()));
        }
        else
        {
            addLog(Messages.getString("RepositoryImportDialog.ErrorSavingJob.Log", ji.getName()));
        }
        return true;
    }
	private boolean importDirectory(int nrthis, Node dirnode) throws KettleException
	{
        
        // What's the directory path?
        String directoryPath = Const.NVL(dirnode.getChildNodes().item(0).getNodeValue(), Const.FILE_SEPARATOR);
        wLabel.setText(Messages.getString("RepositoryImportDialog.ImportDirectory.Label", Integer.toString(nrthis), Integer.toString(nrdirs), directoryPath));
        
        RepositoryDirectory targetDirectory = baseDirectory.findDirectory(directoryPath);
        if (targetDirectory == null)
        {
            if (askDirectory)
            {
                MessageDialogWithToggle mb = new MessageDialogWithToggle(shell,
                    Messages.getString("RepositoryImportDialog.CreateDir.Title"),
                    null,
                    Messages.getString("RepositoryImportDialog.CreateDir.Message", directoryPath),
                    MessageDialog.QUESTION,
                    new String[] {
                                  Messages.getString("System.Button.Yes"),
                                  Messages.getString("System.Button.No"),
                                  Messages.getString("System.Button.Cancel") },
                    1,
                    Messages.getString("RepositoryImportDialog.DontAskAgain.Label"),
                    !askDirectory);
                int answer = mb.open();
                makeDirectory = (answer & 0xFF) == 0;
                askDirectory = !mb.getToggleState();

                // Cancel?
                if ((answer & 0xFF) == 2)
                    return false;
            }

            if (makeDirectory)
            {
                addLog(Messages.getString("RepositoryImportDialog.CreateDir.Log", directoryPath, baseDirectory.toString()));
                targetDirectory = baseDirectory.createDirectory(rep, directoryPath);
                return true;
            }
            else {
            	// user denied creation of the current directory, no harm done, let's continue with the next one
            	return true;
            }
        }
        else {
        	// directory already exists, return true
        	return true;
        }
    }
	
    private void importAll() {
		wLabel.setText(Messages.getString("RepositoryImportDialog.ImportXML.Label"));
		try {
			for (int ii = 0; ii < filenames.length; ++ii) {
				
				final String filename = ((fileDirectory != null) && (fileDirectory.length() > 0)) ? fileDirectory + Const.FILE_SEPARATOR + filenames[ii] : filenames[ii];
				if(log.isBasic()) log.logBasic("Repository", "Import objects from XML file [" + filename + "]"); //$NON-NLS-1$ //$NON-NLS-2$
				addLog(Messages.getString("RepositoryImportDialog.WhichFile.Log", filename));

				// To where?
				wLabel.setText(Messages.getString("RepositoryImportDialog.WhichDir.Label"));

				Document doc = XMLHandler.loadXMLFile(filename);
				if (doc != null) {
					//
					// HERE WE START
					//
					Node repnode = XMLHandler.getSubNode(doc, "repository");
					if (null == repnode) {
						Node firstnode = doc.getFirstChild();
						// if top node is transformation then we have just a transformation
						if ((null != firstnode) && firstnode.getNodeName().equals("transformation")) {
							nrtrans = filenames.length;

							wBar.setMinimum(0);
							wBar.setMaximum(nrtrans);
							wBar.setSelection(ii);

							askDirectory = false; // just use the specified base directory
							try {
								if (!importTransformation(ii + 1, firstnode)) {
									return;
								}
							} catch (Exception e) {
								// Some unexpected error occurred during transformation import
								// This is usually a problem with a missing plugin or something like that...
								//
								new ErrorDialog(
										shell,
										Messages.getString("RepositoryImportDialog.UnexpectedErrorDuringTransformationImport.Title"),
										Messages.getString("RepositoryImportDialog.UnexpectedErrorDuringTransformationImport.Message"),
										e);

								MessageBox mb = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
								mb.setMessage(Messages.getString("RepositoryImportDialog.DoYouWantToContinue.Message"));
								mb.setText(Messages.getString("RepositoryImportDialog.DoYouWantToContinue.Title"));
								int answer = mb.open();
								if (answer == SWT.NO) return;
							}
							wBar.setSelection(ii + 1);
							continue;
						}

						// if top node is job then we have a single job ...
						if ((null != firstnode) && firstnode.getNodeName().equals("job")) {
							nrjobs = filenames.length;

							wBar.setMinimum(0);
							wBar.setMaximum(nrjobs);
							wBar.setSelection(ii);

							askDirectory = false; // just use the specified base directory
							try
							{
								if (!importJob(ii + 1, firstnode)) {
									return;
								}
							} catch (Exception e) {
								// Some unexpected error occurred during job import
								// This is usually a problem with a missing plugin or something like that...
								//
								new ErrorDialog(
										shell,
										Messages.getString("RepositoryImportDialog.UnexpectedErrorDuringJobImport.Title"),
										Messages.getString("RepositoryImportDialog.UnexpectedErrorDuringJobImport.Message"),
										e);
	
								MessageBox mb = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
								mb.setMessage(Messages.getString("RepositoryImportDialog.DoYouWantToContinue.Message"));
								mb.setText(Messages.getString("RepositoryImportDialog.DoYouWantToContinue.Title"));
								int answer = mb.open();
								if (answer == SWT.NO) return;
							}

							wBar.setSelection(ii + 1);
							continue;
						}
					}

					Node dirsnode = XMLHandler.getSubNode(repnode, "directories");
					if (dirsnode != null) // Load directories...
					{
						nrdirs = XMLHandler.countNodes(dirsnode, "directory");

						wBar.setMinimum(0);
						wBar.setMaximum(filenames.length - 1 + nrdirs);
						for (int i = 0; i < nrdirs; i++) {
							wBar.setSelection(ii + i);
							wBar.getDisplay().update();
							Node dirnode = XMLHandler.getSubNodeByNr(dirsnode, "directory", i);
							try
							{
								if (!importDirectory(i + 1, dirnode)) {
									return;
								}
							} catch (Exception e) {
								// Some unexpected error occurred during transformation import
								// This is usually a problem with a missing plugin or something like that...
								//
								new ErrorDialog(
										shell,
										Messages.getString("RepositoryImportDialog.UnexpectedErrorDuringTransformationImport.Title"),
										Messages.getString("RepositoryImportDialog.UnexpectedErrorDuringTransformationImport.Message"),
										e);
	
								MessageBox mb = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
								mb.setMessage(Messages.getString("RepositoryImportDialog.DoYouWantToContinue.Message"));
								mb.setText(Messages.getString("RepositoryImportDialog.DoYouWantToContinue.Title"));
								int answer = mb.open();
								if (answer == SWT.NO) return;
							}

							wBar.setSelection(ii + i + 1);
							wBar.getDisplay().update();
						}
					}
					Node transsnode = XMLHandler.getSubNode(repnode, "transformations");
					if (transsnode != null) // Load transformations...
					{
						nrtrans = XMLHandler.countNodes(transsnode, "transformation");

						wBar.setMinimum(0);
						wBar.setMaximum(filenames.length - 1 + nrtrans);
						for (int i = 0; i < nrtrans; i++) {
							wBar.setSelection(ii + i);
							wBar.getDisplay().update();
							Node transnode = XMLHandler.getSubNodeByNr(transsnode, "transformation", i);
							try
							{
								if (!importTransformation(i + 1, transnode)) {
									return;
								}
							} catch (Exception e) {
								// Some unexpected error occurred during transformation import
								// This is usually a problem with a missing plugin or something like that...
								//
								new ErrorDialog(
										shell,
										Messages.getString("RepositoryImportDialog.UnexpectedErrorDuringTransformationImport.Title"),
										Messages.getString("RepositoryImportDialog.UnexpectedErrorDuringTransformationImport.Message"),
										e);
	
								MessageBox mb = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
								mb.setMessage(Messages.getString("RepositoryImportDialog.DoYouWantToContinue.Message"));
								mb.setText(Messages.getString("RepositoryImportDialog.DoYouWantToContinue.Title"));
								int answer = mb.open();
								if (answer == SWT.NO) return;
							}

							wBar.setSelection(ii + i + 1);
							wBar.getDisplay().update();
						}
					}

					// Ask again for the jobs...
					overwrite = false;
					askOverwrite = true;

					Node jobsnode = XMLHandler.getSubNode(repnode, "jobs");
					if (jobsnode != null) // Load jobs...
					{
						nrjobs = XMLHandler.countNodes(jobsnode, "job");

						wBar.setMinimum(0);
						wBar.setMaximum(filenames.length - 1 + nrjobs);
						for (int i = 0; i < nrjobs; i++) {
							wBar.setSelection(ii + i);
							wBar.getDisplay().update();
							Node jobnode = XMLHandler.getSubNodeByNr(jobsnode, "job", i);
							try
							{
								if (!importJob(i + 1, jobnode)) {
									return;
								}
							} catch (Exception e) {
								// Some unexpected error occurred during job import
								// This is usually a problem with a missing plugin or something like that...
								//
								new ErrorDialog(
										shell,
										Messages.getString("RepositoryImportDialog.UnexpectedErrorDuringJobImport.Title"),
										Messages.getString("RepositoryImportDialog.UnexpectedErrorDuringJobImport.Message"),
										e);
	
								MessageBox mb = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
								mb.setMessage(Messages.getString("RepositoryImportDialog.DoYouWantToContinue.Message"));
								mb.setText(Messages.getString("RepositoryImportDialog.DoYouWantToContinue.Title"));
								int answer = mb.open();
								if (answer == SWT.NO) return;
							}

							wBar.setSelection(ii + i + 1);
							wBar.getDisplay().update();
						}
					}
				} else {
					MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
					mb.setMessage(Messages.getString("RepositoryImportDialog.ErrorInvalidXML.Message"));
					mb.setText(Messages.getString("RepositoryImportDialog.ErrorInvalidXML.Title"));
					mb.open();
				}
			}
			addLog(Messages.getString("RepositoryImportDialog.ImportFinished.Log"));
		} catch (KettleException e) {
			new ErrorDialog(shell, Messages.getString("RepositoryImportDialog.ErrorGeneral.Title"), Messages
					.getString("RepositoryImportDialog.ErrorGeneral.Message"), e);
		}
	}
}
