package be.ibridge.kettle.job.entry.special;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.WindowProperty;
import be.ibridge.kettle.job.entry.JobEntryDialogInterface;
import be.ibridge.kettle.job.entry.JobEntryInterface;
import be.ibridge.kettle.trans.step.BaseStepDialog;

public class JobEntrySpecialDialog extends Dialog implements JobEntryDialogInterface {

	private final static String NOSCHEDULING = "No Scheduling";
	private final static String INTERVAL = "Interval";
	private final static String DAILY = "Daily";
	private final static String WEEKLY = "Weekly";
	private final static String MONTHLY = "Monthly";
	

	private Button wOK, wCancel;
	private Listener lsOK, lsCancel;

	private Shell  shell;
	private SelectionAdapter lsDef;
	
	private JobEntrySpecial  scheduler;
	private boolean  backupChanged;
	private Props    props;
	private Display  display;
	
	private Button wRepeat;
	private Spinner wInterval;
	private CCombo wType;
	private Spinner wHour;
	private Spinner wMinutes;
	private CCombo wDayOfWeek;
	private Spinner wDayOfMonth;
	
	public JobEntrySpecialDialog(Shell parent, JobEntrySpecial scheduler)
	{
		super(parent, SWT.NONE);
		props=Props.getInstance();
		this.scheduler=scheduler;
	}

	public JobEntryInterface open()
	{
		Shell parent = getParent();
		display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE);
 		props.setLook(shell);

		ModifyListener lsMod = new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				scheduler.setChanged();
			}
		};
		backupChanged = scheduler.hasChanged();

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText("Job Scheduling");
		
		int margin = Const.MARGIN;

		wRepeat=new Button(shell, SWT.CHECK);
		wRepeat.addSelectionListener(new SelectionListener(){

			public void widgetSelected(SelectionEvent arg0) {
				enableDisableControls();
			}

			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				
			}});
		placeControl(shell,"Repeat: ",wRepeat,null,null);

		wType=new CCombo(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wType.addModifyListener(lsMod);
		wType.addSelectionListener(new SelectionListener(){

			public void widgetSelected(SelectionEvent arg0) {
				enableDisableControls();
			}

			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				
			}});
		wType.add(NOSCHEDULING);
		wType.add(INTERVAL);
		wType.add(DAILY);
		wType.add(WEEKLY);
		wType.add(MONTHLY);
		wType.setEditable(false);
		wType.setVisibleItemCount(wType.getItemCount());
		placeControl(shell,"Type: ",wType,null,wRepeat);
		
		wInterval=new Spinner(shell,SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		placeControl(shell,"Interval in minutes: ",wInterval,null,wType);

		Composite time = new Composite(shell,SWT.NONE);
		time.setLayout(new FillLayout());
		wHour=new Spinner(time,SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wHour.setMinimum(0);
		wHour.setMaximum(23);
		wMinutes=new Spinner(time,SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wMinutes.setMinimum(0);
		wMinutes.setMaximum(59);
		placeControl(shell,"Time: ",time,null,wInterval);

		wDayOfWeek=new CCombo(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wDayOfWeek.addModifyListener(lsMod);
		wDayOfWeek.add("Sunday");
		wDayOfWeek.add("Monday");
		wDayOfWeek.add("Tuesday");
		wDayOfWeek.add("Wednesday");
		wDayOfWeek.add("Thursday");
		wDayOfWeek.add("Friday");
		wDayOfWeek.add("Saturday");
		wDayOfWeek.setEditable(false);
		wDayOfWeek.setVisibleItemCount(wDayOfWeek.getItemCount());
		placeControl(shell,"Day of Week: ",wDayOfWeek,null,time);

		wDayOfMonth=new Spinner(shell,SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wDayOfMonth.addModifyListener(lsMod);
		wDayOfMonth.setMinimum(1);
		wDayOfMonth.setMaximum(30);
		placeControl(shell,"Day of Month: ",wDayOfMonth,null,wDayOfWeek);

		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText("  &OK  ");
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText("  &Cancel  ");

		BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, null);

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		
		wOK.addListener    (SWT.Selection, lsOK     );
		wCancel.addListener(SWT.Selection, lsCancel );
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		wType.addSelectionListener(lsDef);

		// Detect [X] or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		getData();
		enableDisableControls();
		
		BaseStepDialog.setSize(shell, 350, 200, true);

		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return scheduler;
	}

	public void dispose()
	{
		WindowProperty winprop = new WindowProperty(shell);
		props.setScreen(winprop);
		shell.dispose();
	}
		
	public void getData()
	{
		wRepeat.setSelection(scheduler.isRepeat());
		wType.select(scheduler.getSchedulerType());
		wInterval.setSelection(scheduler.getInterval());
		wHour.setSelection(scheduler.getHour());
		wMinutes.setSelection(scheduler.getMinutes());
		wDayOfWeek.select(scheduler.getWeekDay());
		wDayOfMonth.setSelection(scheduler.getDayOfMonth());
		wType.addSelectionListener(lsDef);
	}
	
	private void cancel()
	{
		scheduler.setChanged(backupChanged);
		
		scheduler=null;
		dispose();
	}
	
	private void ok()
	{
		scheduler.setRepeat(wRepeat.getSelection());
		scheduler.setSchedulerType(wType.getSelectionIndex());
		scheduler.setInterval(wInterval.getSelection());
		scheduler.setHour(wHour.getSelection());
		scheduler.setMinutes(wMinutes.getSelection());
		scheduler.setWeekDay(wDayOfWeek.getSelectionIndex());
		scheduler.setDayOfMonth(wDayOfMonth.getSelection());
		dispose();
	}
	
	private void placeControl(Shell shell,String text,Control control, Control nextTo,Control under) {
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;
		Label label =new Label(shell, SWT.RIGHT);
		label.setText(text);
 		props.setLook(label);
		FormData formDataLabel=new FormData();
		formDataLabel.left = new FormAttachment(0, 0);
		if(under!=null) {
			formDataLabel.top  = new FormAttachment(under, margin);
		} else {
			formDataLabel.top  = new FormAttachment(0, 0);
		}
		formDataLabel.right= new FormAttachment(middle, 0);
		label.setLayoutData(formDataLabel);
		
 		props.setLook(control);
		FormData formDataControl =new FormData();
		formDataControl.left = new FormAttachment(middle, 0);
		if(under!=null) {
			formDataControl.top  = new FormAttachment(under, margin);
		} else {
			formDataControl.top  = new FormAttachment(0, 0);
		}
		formDataControl.right= new FormAttachment(100, 0);
		control.setLayoutData(formDataControl);
	}
	
	private void enableDisableControls() {
//		if(wRepeat.getSelection()) {
			wType.setEnabled(true);
			if(NOSCHEDULING.equals(wType.getText())) {
				wInterval.setEnabled(false);
				wDayOfWeek.setEnabled(false);
				wDayOfMonth.setEnabled(false);
				wHour.setEnabled(false);
				wMinutes.setEnabled(false);
			} else if(INTERVAL.equals(wType.getText())) {
				wInterval.setEnabled(true);
				wDayOfWeek.setEnabled(false);
				wDayOfMonth.setEnabled(false);
				wHour.setEnabled(false);
				wMinutes.setEnabled(false);
			} else if(DAILY.equals(wType.getText())) {
				wInterval.setEnabled(false);
				wDayOfWeek.setEnabled(false);
				wDayOfMonth.setEnabled(false);
				wHour.setEnabled(true);
				wMinutes.setEnabled(true);
			} else if (WEEKLY.equals(wType.getText())) {
				wInterval.setEnabled(false);
				wDayOfWeek.setEnabled(true);
				wDayOfMonth.setEnabled(false);
				wHour.setEnabled(true);
				wMinutes.setEnabled(true);
			} else if (MONTHLY.equals(wType.getText())) {
				wInterval.setEnabled(false);
				wDayOfWeek.setEnabled(false);
				wDayOfMonth.setEnabled(true);
				wHour.setEnabled(true);
				wMinutes.setEnabled(true);
			}
//		} else {
//			wType.setEnabled(false);
//			wInterval.setEnabled(false);
//			wDayOfWeek.setEnabled(false);
//			wDayOfMonth.setEnabled(false);
//			wHour.setEnabled(false);
//			wMinutes.setEnabled(false);
		}

}
