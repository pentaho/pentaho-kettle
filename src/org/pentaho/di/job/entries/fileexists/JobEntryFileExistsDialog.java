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

package org.pentaho.di.job.entries.fileexists;

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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.gui.WindowProperty;
import org.pentaho.di.core.widget.TextVar;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.step.BaseStepDialog;


/**
 * This dialog allows you to edit the SQL job entry settings. (select the connection and the sql
 * script to be executed)
 * 
 * @author Matt
 * @since 19-06-2003
 */
public class JobEntryFileExistsDialog extends JobEntryDialog implements JobEntryDialogInterface
{
    private static final String[] FILETYPES = new String[] {
                                                            Messages
                                                                .getString("JobFileExists.Filetype.Text"),
                                                            Messages
                                                                .getString("JobFileExists.Filetype.CSV"),
                                                            Messages
                                                                .getString("JobFileExists.Filetype.All") };

    private Label wlName;

    private Text wName;

    private FormData fdlName, fdName;

    private Label wlFilename;

    private Button wbFilename;

    private TextVar wFilename;

    private FormData fdlFilename, fdbFilename, fdFilename;

    private Button wOK, wCancel;

    private Listener lsOK, lsCancel;

    private JobEntryFileExists jobEntry;

    private Shell shell;

    private SelectionAdapter lsDef;
    
    private boolean changed;

    public JobEntryFileExistsDialog(Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta)
    {
        super(parent, jobEntryInt, rep, jobMeta);
        jobEntry = (JobEntryFileExists) jobEntryInt;
        if (this.jobEntry.getName() == null)
            this.jobEntry.setName(Messages.getString("JobFileExists.Name.Default"));
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

        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;

        shell.setLayout(formLayout);
        shell.setText(Messages.getString("JobFileExists.Title"));

        int middle = props.getMiddlePct();
        int margin = Const.MARGIN;

        // Filename line
        wlName = new Label(shell, SWT.RIGHT);
        wlName.setText(Messages.getString("JobFileExists.Name.Label"));
        props.setLook(wlName);
        fdlName = new FormData();
        fdlName.left = new FormAttachment(0, 0);
        fdlName.right = new FormAttachment(middle, -margin);
        fdlName.top = new FormAttachment(0, margin);
        wlName.setLayoutData(fdlName);
        wName = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wName);
        wName.addModifyListener(lsMod);
        fdName = new FormData();
        fdName.left = new FormAttachment(middle, 0);
        fdName.top = new FormAttachment(0, margin);
        fdName.right = new FormAttachment(100, 0);
        wName.setLayoutData(fdName);

        // Filename line
        wlFilename = new Label(shell, SWT.RIGHT);
        wlFilename.setText(Messages.getString("JobFileExists.Filename.Label"));
        props.setLook(wlFilename);
        fdlFilename = new FormData();
        fdlFilename.left = new FormAttachment(0, 0);
        fdlFilename.top = new FormAttachment(wName, margin);
        fdlFilename.right = new FormAttachment(middle, -margin);
        wlFilename.setLayoutData(fdlFilename);

        wbFilename = new Button(shell, SWT.PUSH | SWT.CENTER);
        props.setLook(wbFilename);
        wbFilename.setText(Messages.getString("System.Button.Browse"));
        fdbFilename = new FormData();
        fdbFilename.right = new FormAttachment(100, 0);
        fdbFilename.top = new FormAttachment(wName, 0);
        // fdbFilename.height = 22;
        wbFilename.setLayoutData(fdbFilename);

        wFilename = new TextVar(jobMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wFilename);
        wFilename.addModifyListener(lsMod);
        fdFilename = new FormData();
        fdFilename.left = new FormAttachment(middle, 0);
        fdFilename.top = new FormAttachment(wName, margin);
        fdFilename.right = new FormAttachment(wbFilename, -margin);
        wFilename.setLayoutData(fdFilename);

        // Whenever something changes, set the tooltip to the expanded version:
        wFilename.addModifyListener(new ModifyListener()
        {
            public void modifyText(ModifyEvent e)
            {
                wFilename.setToolTipText(jobMeta.environmentSubstitute(wFilename.getText()));
            }
        });

        wbFilename.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                FileDialog dialog = new FileDialog(shell, SWT.OPEN);
                dialog.setFilterExtensions(new String[] { "*.txt", "*.csv", "*" });
                if (wFilename.getText() != null)
                {
                    dialog.setFileName(jobMeta.environmentSubstitute(wFilename.getText()));
                }
                dialog.setFilterNames(FILETYPES);
                if (dialog.open() != null)
                {
                    wFilename.setText(dialog.getFilterPath() + Const.FILE_SEPARATOR
                                      + dialog.getFileName());
                }
            }
        });

        wOK = new Button(shell, SWT.PUSH);
        wOK.setText(Messages.getString("System.Button.OK"));
        FormData fd = new FormData();
        fd.right = new FormAttachment(50, -10);
        fd.bottom = new FormAttachment(100, 0);
        fd.width = 100;
        wOK.setLayoutData(fd);

        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(Messages.getString("System.Button.Cancel"));
        fd = new FormData();
        fd.left = new FormAttachment(50, 10);
        fd.bottom = new FormAttachment(100, 0);
        fd.width = 100;
        wCancel.setLayoutData(fd);

        // Add listeners
        lsCancel = new Listener()
        {
            public void handleEvent(Event e)
            {
                cancel();
            }
        };
        lsOK = new Listener()
        {
            public void handleEvent(Event e)
            {
                ok();
            }
        };

        wCancel.addListener(SWT.Selection, lsCancel);
        wOK.addListener(SWT.Selection, lsOK);

        lsDef = new SelectionAdapter()
        {
            public void widgetDefaultSelected(SelectionEvent e)
            {
                ok();
            }
        };

        wName.addSelectionListener(lsDef);
        wFilename.addSelectionListener(lsDef);

        // Detect X or ALT-F4 or something that kills this window...
        shell.addShellListener(new ShellAdapter()
        {
            public void shellClosed(ShellEvent e)
            {
                cancel();
            }
        });

        getData();

        BaseStepDialog.setSize(shell);

        shell.open();
        props.setDialogSize(shell, "JobFileExistsDialogSize");
        while (!shell.isDisposed())
        {
            if (!display.readAndDispatch())
                display.sleep();
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
        if (jobEntry.getName() != null)
            wName.setText(jobEntry.getName());
        wName.selectAll();
        if (jobEntry.getFilename() != null)
            wFilename.setText(jobEntry.getFilename());
    }

    private void cancel()
    {
        jobEntry.setChanged(changed);
        jobEntry = null;
        dispose();
    }

    private void ok()
    {
        jobEntry.setName(wName.getText());
        jobEntry.setFilename(wFilename.getText());
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
