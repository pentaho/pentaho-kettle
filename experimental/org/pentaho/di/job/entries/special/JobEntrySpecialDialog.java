package org.pentaho.di.job.entries.special;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.trans.step.BaseStepDialog;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.GUIResource;
import org.pentaho.di.core.Props;
import be.ibridge.kettle.core.WindowProperty;



public class JobEntrySpecialDialog extends Dialog implements JobEntryDialogInterface
{
    private final static String NOSCHEDULING = Messages.getString("JobSpecial.Type.NoScheduling");

    private final static String INTERVAL = Messages.getString("JobSpecial.Type.Interval");

    private final static String DAILY = Messages.getString("JobSpecial.Type.Daily");

    private final static String WEEKLY = Messages.getString("JobSpecial.Type.Weekly");

    private final static String MONTHLY = Messages.getString("JobSpecial.Type.Monthly");

    private Button wOK, wCancel;

    private Listener lsOK, lsCancel;

    private Shell shell;

    private SelectionAdapter lsDef;

    private JobEntrySpecial jobEntry;

    private boolean backupChanged;

    private Props props;

    private Display display;

    private Button wRepeat;

    private Spinner wIntervalSeconds, wIntervalMinutes;

    private CCombo wType;

    private Spinner wHour;

    private Spinner wMinutes;

    private CCombo wDayOfWeek;

    private Spinner wDayOfMonth;

    public JobEntrySpecialDialog(Shell parent, JobEntrySpecial jobEntry)
    {
        super(parent, SWT.NONE);
        props = Props.getInstance();
        this.jobEntry = jobEntry;
    }

    public JobEntryInterface open()
    {
        Shell parent = getParent();
        display = parent.getDisplay();

        shell = new Shell(parent, props.getJobsDialogStyle());
        props.setLook(shell);
        shell.setImage(GUIResource.getInstance().getImageStart());

        ModifyListener lsMod = new ModifyListener()
        {
            public void modifyText(ModifyEvent e)
            {
                jobEntry.setChanged();
            }
        };
        backupChanged = jobEntry.hasChanged();

        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;

        shell.setLayout(formLayout);
        shell.setText(Messages.getString("JobSpecial.Scheduling.Label"));

        int margin = Const.MARGIN;

        wRepeat = new Button(shell, SWT.CHECK);
        wRepeat.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event arg0)
            {
                enableDisableControls();
            }
        });
        placeControl(shell, Messages.getString("JobSpecial.Repeat.Label"), wRepeat, null);

        wType = new CCombo(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wType.addModifyListener(lsMod);
        wType.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event arg0)
            {
                enableDisableControls();
            }
        });
        wType.add(NOSCHEDULING);
        wType.add(INTERVAL);
        wType.add(DAILY);
        wType.add(WEEKLY);
        wType.add(MONTHLY);
        wType.setEditable(false);
        wType.setVisibleItemCount(wType.getItemCount());
        placeControl(shell, Messages.getString("JobSpecial.Type.Label"), wType, wRepeat);

        wIntervalSeconds = new Spinner(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        placeControl(shell, Messages.getString("JobSpecial.IntervalSeconds.Label"), wIntervalSeconds,
            wType);

        wIntervalMinutes = new Spinner(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        placeControl(shell, Messages.getString("JobSpecial.IntervalMinutes.Label"), wIntervalMinutes,
            wIntervalSeconds);

        Composite time = new Composite(shell, SWT.NONE);
        time.setLayout(new FillLayout());
        wHour = new Spinner(time, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wHour.setMinimum(0);
        wHour.setMaximum(23);
        wMinutes = new Spinner(time, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wMinutes.setMinimum(0);
        wMinutes.setMaximum(59);
        placeControl(shell, Messages.getString("JobSpecial.TimeOfDay.Label"), time, wIntervalMinutes);

        wDayOfWeek = new CCombo(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wDayOfWeek.addModifyListener(lsMod);
        wDayOfWeek.add(Messages.getString("JobSpecial.DayOfWeek.Sunday"));
        wDayOfWeek.add(Messages.getString("JobSpecial.DayOfWeek.Monday"));
        wDayOfWeek.add(Messages.getString("JobSpecial.DayOfWeek.Tuesday"));
        wDayOfWeek.add(Messages.getString("JobSpecial.DayOfWeek.Wednesday"));
        wDayOfWeek.add(Messages.getString("JobSpecial.DayOfWeek.Thursday"));
        wDayOfWeek.add(Messages.getString("JobSpecial.DayOfWeek.Friday"));
        wDayOfWeek.add(Messages.getString("JobSpecial.DayOfWeek.Saturday"));
        wDayOfWeek.setEditable(false);
        wDayOfWeek.setVisibleItemCount(wDayOfWeek.getItemCount());
        placeControl(shell, Messages.getString("JobSpecial.DayOfWeek.Label"), wDayOfWeek, time);

        wDayOfMonth = new Spinner(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wDayOfMonth.addModifyListener(lsMod);
        wDayOfMonth.setMinimum(1);
        wDayOfMonth.setMaximum(30);
        placeControl(shell, Messages.getString("JobSpecial.DayOfMonth.Label"), wDayOfMonth,
            wDayOfWeek);

        // Some buttons
        wOK = new Button(shell, SWT.PUSH);
        wOK.setText(Messages.getString("System.Button.OK"));
        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(Messages.getString("System.Button.Cancel"));

        BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, null);

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

        wOK.addListener(SWT.Selection, lsOK);
        wCancel.addListener(SWT.Selection, lsCancel);

        lsDef = new SelectionAdapter()
        {
            public void widgetDefaultSelected(SelectionEvent e)
            {
                ok();
            }
        };
        wType.addSelectionListener(lsDef);

        // Detect [X] or ALT-F4 or something that kills this window...
        shell.addShellListener(new ShellAdapter()
        {
            public void shellClosed(ShellEvent e)
            {
                cancel();
            }
        });

        getData();
        enableDisableControls();

        BaseStepDialog.setSize(shell, 350, 200, true);

        shell.open();
        props.setDialogSize(shell, "JobSpecialDialogSize");
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

    public void getData()
    {
        wRepeat.setSelection(jobEntry.isRepeat());
        wType.select(jobEntry.getSchedulerType());
        wIntervalSeconds.setSelection(jobEntry.getIntervalSeconds());
        wIntervalMinutes.setSelection(jobEntry.getIntervalMinutes());
        wHour.setSelection(jobEntry.getHour());
        wMinutes.setSelection(jobEntry.getMinutes());
        wDayOfWeek.select(jobEntry.getWeekDay());
        wDayOfMonth.setSelection(jobEntry.getDayOfMonth());
        wType.addSelectionListener(lsDef);
    }

    private void cancel()
    {
        jobEntry.setChanged(backupChanged);

        jobEntry = null;
        dispose();
    }

    private void ok()
    {
        jobEntry.setRepeat(wRepeat.getSelection());
        jobEntry.setSchedulerType(wType.getSelectionIndex());
        jobEntry.setIntervalSeconds(wIntervalSeconds.getSelection());
        jobEntry.setIntervalMinutes(wIntervalMinutes.getSelection());
        jobEntry.setHour(wHour.getSelection());
        jobEntry.setMinutes(wMinutes.getSelection());
        jobEntry.setWeekDay(wDayOfWeek.getSelectionIndex());
        jobEntry.setDayOfMonth(wDayOfMonth.getSelection());
        dispose();
    }

    private void placeControl(Shell pShell, String text, Control control, Control under)
    {
        int middle = props.getMiddlePct();
        int margin = Const.MARGIN;
        Label label = new Label(pShell, SWT.RIGHT);
        label.setText(text);
        props.setLook(label);
        FormData formDataLabel = new FormData();
        formDataLabel.left = new FormAttachment(0, 0);
        if (under != null)
        {
            formDataLabel.top = new FormAttachment(under, margin);
        }
        else
        {
            formDataLabel.top = new FormAttachment(0, 0);
        }
        formDataLabel.right = new FormAttachment(middle, 0);
        label.setLayoutData(formDataLabel);

        props.setLook(control);
        FormData formDataControl = new FormData();
        formDataControl.left = new FormAttachment(middle, 0);
        if (under != null)
        {
            formDataControl.top = new FormAttachment(under, margin);
        }
        else
        {
            formDataControl.top = new FormAttachment(0, 0);
        }
        formDataControl.right = new FormAttachment(100, 0);
        control.setLayoutData(formDataControl);
    }

    private void enableDisableControls()
    {
        // if(wRepeat.getSelection()) {
        wType.setEnabled(true);
        if (NOSCHEDULING.equals(wType.getText()))
        {
            wIntervalSeconds.setEnabled(false);
            wIntervalMinutes.setEnabled(false);
            wDayOfWeek.setEnabled(false);
            wDayOfMonth.setEnabled(false);
            wHour.setEnabled(false);
            wMinutes.setEnabled(false);
        }
        else if (INTERVAL.equals(wType.getText()))
        {
            wIntervalSeconds.setEnabled(true);
            wIntervalMinutes.setEnabled(true);
            wDayOfWeek.setEnabled(false);
            wDayOfMonth.setEnabled(false);
            wHour.setEnabled(false);
            wMinutes.setEnabled(false);
        }
        else if (DAILY.equals(wType.getText()))
        {
            wIntervalSeconds.setEnabled(false);
            wIntervalMinutes.setEnabled(false);
            wDayOfWeek.setEnabled(false);
            wDayOfMonth.setEnabled(false);
            wHour.setEnabled(true);
            wMinutes.setEnabled(true);
        }
        else if (WEEKLY.equals(wType.getText()))
        {
            wIntervalSeconds.setEnabled(false);
            wIntervalMinutes.setEnabled(false);
            wDayOfWeek.setEnabled(true);
            wDayOfMonth.setEnabled(false);
            wHour.setEnabled(true);
            wMinutes.setEnabled(true);
        }
        else if (MONTHLY.equals(wType.getText()))
        {
            wIntervalSeconds.setEnabled(false);
            wIntervalMinutes.setEnabled(false);
            wDayOfWeek.setEnabled(false);
            wDayOfMonth.setEnabled(true);
            wHour.setEnabled(true);
            wMinutes.setEnabled(true);
        }
        // } else {
        // wType.setEnabled(false);
        // wInterval.setEnabled(false);
        // wDayOfWeek.setEnabled(false);
        // wDayOfMonth.setEnabled(false);
        // wHour.setEnabled(false);
        // wMinutes.setEnabled(false);
    }

}
