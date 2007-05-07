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

package be.ibridge.kettle.job.entry.ping;

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
import be.ibridge.kettle.core.util.StringUtil;
import be.ibridge.kettle.core.widget.TextVar;
import be.ibridge.kettle.job.JobMeta;
import be.ibridge.kettle.job.dialog.JobDialog;
import be.ibridge.kettle.job.entry.JobEntryDialogInterface;
import be.ibridge.kettle.job.entry.JobEntryInterface;
import be.ibridge.kettle.trans.step.BaseStepDialog;

/**
 * This dialog allows you to edit the ping job entry settings. 
 * 
 * @author Samatar Hassan
 * @since  Mar-2007
 */
public class JobEntryPingDialog extends Dialog implements JobEntryDialogInterface
{
    private Label    wlName;
    private Text     wName;
    private FormData fdlName, fdName;

    private Label    wlHostname;
    private TextVar  wHostname;
    private FormData fdlHostname,  fdHostname;

	private Label    wlNbrPackets;
	private TextVar  wNbrPackets;
	private FormData fdlNbrPackets, fdNbrPackets;

    private Button   wOK, wCancel;

    private Listener lsOK, lsCancel;

    private JobEntryPing jobEntry;

    private Shell shell;

    private Props props;

    private SelectionAdapter lsDef;

    private boolean changed;

    public JobEntryPingDialog(Shell parent, JobEntryPing jobEntry, JobMeta jobMeta)
    {
        super(parent, SWT.NONE);
        props = Props.getInstance();
        this.jobEntry = jobEntry;

        if (this.jobEntry.getName() == null)
            this.jobEntry.setName(Messages.getString("JobPing.Name.Default"));
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
        shell.setText(Messages.getString("JobPing.Title"));

        int middle = props.getMiddlePct();
        int margin = Const.MARGIN;

        // Filename line
        wlName = new Label(shell, SWT.RIGHT);
        wlName.setText(Messages.getString("JobPing.Name.Label"));
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

        // hostname line
        wlHostname = new Label(shell, SWT.RIGHT);
        wlHostname.setText(Messages.getString("JobPing.Hostname.Label"));
        props.setLook(wlHostname);
        fdlHostname = new FormData();
        fdlHostname.left = new FormAttachment(0, 0);
        fdlHostname.top = new FormAttachment(wName, margin);
        fdlHostname.right = new FormAttachment(middle, 0);
        wlHostname.setLayoutData(fdlHostname);

        wHostname = new TextVar(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wHostname);
        wHostname.addModifyListener(lsMod);
        fdHostname = new FormData();
        fdHostname.left = new FormAttachment(middle, 0);
        fdHostname.top = new FormAttachment(wName, margin);
        fdHostname.right = new FormAttachment(100, 0);
        wHostname.setLayoutData(fdHostname);

        // Whenever something changes, set the tooltip to the expanded version:
        wHostname.addModifyListener(new ModifyListener()
        {
            public void modifyText(ModifyEvent e)
            {
                wHostname.setToolTipText(StringUtil.environmentSubstitute(wHostname.getText()));
            }
        });

		// Nbr response to get
		wlNbrPackets = new Label(shell, SWT.RIGHT);
		wlNbrPackets.setText(Messages.getString("JobPing.NbrPaquets.Label"));
		props.setLook(wlNbrPackets);
		fdlNbrPackets = new FormData();
		fdlNbrPackets.left = new FormAttachment(0, 0);
		fdlNbrPackets.right = new FormAttachment(middle, 0);
		fdlNbrPackets.top = new FormAttachment(wHostname, margin);
		wlNbrPackets.setLayoutData(fdlNbrPackets);

		wNbrPackets = new TextVar(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wNbrPackets);
		wNbrPackets.addModifyListener(lsMod);
		fdNbrPackets = new FormData();
		fdNbrPackets.left = new FormAttachment(middle, 0);
		fdNbrPackets.top = new FormAttachment(wHostname, margin);
		fdNbrPackets.right = new FormAttachment(100, 0);
		wNbrPackets.setLayoutData(fdNbrPackets);

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
        wHostname.addSelectionListener(lsDef);

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
        props.setDialogSize(shell, "JobPingDialogSize");
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
        if (jobEntry.getHostname() != null)
            wHostname.setText(jobEntry.getHostname());
		if (jobEntry.getNbrPackets() != null)
			wNbrPackets.setText(jobEntry.getNbrPackets());
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
        jobEntry.setHostname(wHostname.getText());
		jobEntry.setNbrPackets(wNbrPackets.getText());
	
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