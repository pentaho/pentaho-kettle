/*
 *
 *
 */

package org.pentaho.di.repository.dialog;

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
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepDialog;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.Props;
import be.ibridge.kettle.core.WindowProperty;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.dialog.ErrorDialog;
import org.pentaho.di.core.exception.KettleException;


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
    private Props props;

    private Repository rep;
    private String filename;
    private RepositoryDirectory baseDirectory;
    private ProgressBar wBar;
    private Label wLabel;
    private Text wLogging;
    private Button wClose;

    /**
     * @deprecated Use CT without <i>log</i> and <i>props</i> parameters
     */
    public RepositoryImportProgressDialog(Shell parent, int style, LogWriter log, Props props, Repository rep, String filename, RepositoryDirectory baseDirectory)
    {
        this(parent, style, rep, filename, baseDirectory);
        this.log = log;
        this.props = props;
    }
    
    public RepositoryImportProgressDialog(Shell parent, int style, Repository rep, String filename, RepositoryDirectory baseDirectory)
    {
        super(parent, style);

        this.log = LogWriter.getInstance();
        this.props = Props.getInstance();
        this.parent = parent;
        this.rep = rep;
        this.filename = filename;
        this.baseDirectory = baseDirectory;
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

    private void importAll()
    {
        wLabel.setText(Messages.getString("RepositoryImportDialog.ImportXML.Label"));
        try
        {
            boolean overwrite = false;
            boolean askOverwrite = true;
            boolean makeDirectory = false;
            boolean askDirectory = true;

            addLog(Messages.getString("RepositoryImportDialog.WhichFile.Log", filename));

            // To where?
            wLabel.setText(Messages.getString("RepositoryImportDialog.WhichDir.Label"));

            Document doc = XMLHandler.loadXMLFile(filename);
            if (doc != null)
            {
                //
                // HERE WE START
                //
                Node repnode = XMLHandler.getSubNode(doc, "repository");
                Node transsnode = XMLHandler.getSubNode(repnode, "transformations");
                if (transsnode != null) // Load transformations...
                {
                    int nrtrans = XMLHandler.countNodes(transsnode, "transformation");

                    wBar.setMinimum(0);
                    wBar.setMaximum(nrtrans);
                    for (int i = 0; i < nrtrans; i++)
                    {
                        wBar.setSelection(i + 1);
                        Node transnode = XMLHandler.getSubNodeByNr(transsnode, "transformation", i);

                        //
                        // Load transformation from XML into a directory, possibly created!
                        //
                        TransMeta ti = new TransMeta(transnode);

                        wLabel.setText(Messages.getString("RepositoryImportDialog.ImportTrans.Label", Integer.toString(i + 1), Integer.toString(nrtrans), ti.getName()));

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
                                int answer = mb.open();
                                makeDirectory = (answer & 0xFF) == 0;
                                askDirectory = !mb.getToggleState();

                                // Cancel?
                                if ((answer & 0xFF) == 1)
                                    return;
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
                            int answer = md.open();
                            overwrite = (answer & 0xFF) == 0;
                            askOverwrite = !md.getToggleState();
                        }

                        if (id <= 0 || overwrite)
                        {
                            ti.setDirectory(targetDirectory);

                            try
                            {
                                ti.saveRep(rep);
                                addLog(Messages.getString("RepositoryImportDialog.TransSaved.Log", Integer.toString(i), ti.getName()));
                            }
                            catch (Exception e)
                            {
                                addLog(Messages.getString("RepositoryImportDialog.ErrorSavingTrans.Log", Integer.toString(i), ti.getName(), e.toString()));
                                addLog(Const.getStackTracker(e));
                            }
                        }
                        else
                        {
                            addLog(Messages.getString("RepositoryImportDialog.ErrorSavingTrans2.Log", ti.getName()));
                        }
                    }
                }

                // Ask again for the jobs...
                overwrite = false;
                askOverwrite = true;

                Node jobsnode = XMLHandler.getSubNode(repnode, "jobs");
                if (jobsnode != null) // Load jobs...
                {
                    int nrjobs = XMLHandler.countNodes(jobsnode, "job");

                    wBar.setMinimum(0);
                    wBar.setMaximum(nrjobs);
                    for (int i = 0; i < nrjobs; i++)
                    {
                        wBar.setSelection(i + 1);
                        Node jobnode = XMLHandler.getSubNodeByNr(jobsnode, "job", i);

                        // Load the job from the XML node.
                        JobMeta ji = new JobMeta(log, jobnode, rep);

                        wLabel.setText(Messages.getString("RepositoryImportDialog.ImportJob.Label", Integer.toString(i + 1), Integer.toString(nrjobs), ji.getName()));

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
                                int answer = mb.open();
                                makeDirectory = answer == 0;
                                askDirectory = !mb.getToggleState();

                                // Cancel?
                                if ((answer & 0xFF) == 2)
                                    return;
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
                            int answer = md.open();
                            overwrite = (answer & 0xFF) == 0;
                            askOverwrite = !md.getToggleState();
                        }

                        if (id <= 0 || overwrite)
                        {
                            ji.setDirectory(targetDirectory);
                            ji.saveRep(rep);
                            addLog(Messages.getString("RepositoryImportDialog.JobSaved.Log", Integer.toString(i), ji.getName()));
                        }
                        else
                        {
                            addLog(Messages.getString("RepositoryImportDialog.ErrorSavingJob.Log", ji.getName()));
                        }
                    }
                }
                addLog(Messages.getString("RepositoryImportDialog.ImportFinished.Log"));
            }
            else
            {
                MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
                mb.setMessage(Messages.getString("RepositoryImportDialog.ErrorInvalidXML.Message"));
                mb.setText(Messages.getString("RepositoryImportDialog.ErrorInvalidXML.Title"));
                mb.open();
            }
        }
        catch (KettleException e)
        {
            new ErrorDialog(shell, Messages.getString("RepositoryImportDialog.ErrorGeneral.Title"), Messages.getString("RepositoryImportDialog.ErrorGeneral.Message"), e);
        }
    }
}
