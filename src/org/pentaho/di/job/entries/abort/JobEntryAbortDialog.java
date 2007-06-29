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
 * Created on 10-03-2007
 *
 */

package org.pentaho.di.job.entries.abort;

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
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.gui.WindowProperty;
import org.pentaho.di.core.widget.TextVar;
import org.pentaho.di.job.dialog.JobDialog;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.trans.step.BaseStepDialog;


/**
 * This dialog allows you to edit a JobEntry Abort object.
 * 
 * @author Samatar
 * @since 10-03-2007
 */
public class JobEntryAbortDialog extends Dialog implements JobEntryDialogInterface
{
    private Label wlName;

    private Text wName;

    private FormData fdlName, fdName;

  
    private Button wOK, wCancel;

    private Listener lsOK, lsCancel;

    private JobEntryAbort jobEntry;

    private Shell shell;

    private Props props;

    private SelectionAdapter lsDef;

    private boolean changed;

	private Label wlMessageAbort;

	private TextVar wMessageAbort;
	
	private FormData fdlMessageAbort, fdMessageAbort;


    public JobEntryAbortDialog(Shell parent, JobEntryAbort jobEntry)
    {
        super(parent, SWT.NONE);
        props = Props.getInstance();
        this.jobEntry = jobEntry;

        if (this.jobEntry.getName() == null)
            this.jobEntry.setName(Messages.getString("JobEntryAbortDialog.Jobname.Label"));
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
        shell.setText(Messages.getString("JobEntryAbortDialog.Title"));

        int middle = props.getMiddlePct();
        int margin = Const.MARGIN;

        wOK = new Button(shell, SWT.PUSH);
        wOK.setText(Messages.getString("System.Button.OK"));
        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(Messages.getString("System.Button.Cancel"));

        // at the bottom
        BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, null);

        // Filename line
        wlName = new Label(shell, SWT.RIGHT);
        wlName.setText(Messages.getString("JobEntryAbortDialog.Label"));
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

		// Message line
		wlMessageAbort = new Label(shell, SWT.RIGHT);
		wlMessageAbort.setText(Messages.getString("JobEntryAbortDialog.MessageAbort.Label"));
		props.setLook(wlMessageAbort);
		fdlMessageAbort = new FormData();
		fdlMessageAbort.left = new FormAttachment(0, 0);
		fdlMessageAbort.right = new FormAttachment(middle, 0);
		fdlMessageAbort.top = new FormAttachment(wName, margin);
		wlMessageAbort.setLayoutData(fdlMessageAbort);

		wMessageAbort = new TextVar(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wMessageAbort);
		wMessageAbort.setToolTipText(Messages.getString("JobEntryAbortDialog.MessageAbort.Tooltip"));
		wMessageAbort.addModifyListener(lsMod);
		fdMessageAbort = new FormData();
		fdMessageAbort.left = new FormAttachment(middle, 0);
		fdMessageAbort.top = new FormAttachment(wName, margin);
		fdMessageAbort.right = new FormAttachment(100, 0);
		wMessageAbort.setLayoutData(fdMessageAbort);
	
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
        props.setDialogSize(shell, "JobAbortDialogSize");
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
		if (jobEntry.getMessageabort() != null)
			wMessageAbort.setText(jobEntry.getMessageabort());
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
		jobEntry.setMessageabort(wMessageAbort.getText());
        dispose();
    }

    public String toString()
    {
        return this.getClass().getName();
    }
}