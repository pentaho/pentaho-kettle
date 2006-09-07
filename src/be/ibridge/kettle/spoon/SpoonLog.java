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

package be.ibridge.kettle.spoon;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import be.ibridge.kettle.core.ColumnInfo;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.GUIResource;
import be.ibridge.kettle.core.KettleVariables;
import be.ibridge.kettle.core.LocalVariables;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.dialog.EnterSelectionDialog;
import be.ibridge.kettle.core.dialog.EnterStringsDialog;
import be.ibridge.kettle.core.dialog.ErrorDialog;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.core.widget.TableView;
import be.ibridge.kettle.spoon.dialog.EnterPreviewRowsDialog;
import be.ibridge.kettle.spoon.dialog.LogSettingsDialog;
import be.ibridge.kettle.spoon.dialog.PreviewSelectDialog;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStep;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepMeta;

/**
 * SpoonLog handles the display of the logging information in the Spoon logging window.
 *  
 * @see be.ibridge.kettle.spoon.Spoon
 * @author Matt
 * @since  17 may 2003
 */
public class SpoonLog extends Composite
{
	public static final long UPDATE_TIME_VIEW = 1000L;

	public static final long UPDATE_TIME_LOG = 2000L;

	public static final long REFRESH_TIME = 100L;

	public final static String START_TEXT = Messages.getString("SpoonLog.Button.StartTransformation"); //$NON-NLS-1$

	public final static String STOP_TEXT = Messages.getString("SpoonLog.Button.StopTransformation"); //$NON-NLS-1$

	private Shell shell;

	private Display display;

	private LogWriter log;

	private ColumnInfo[] colinf;

	private TableView wFields;

	private Button wOnlyActive;

	private Button wSafeMode;

	private Text wText;

	private Button wStart;

	private Button wPreview;

	private Button wError;

	private Button wClear;

	private Button wLog;

	private long lastUpdateView;

	private long lastUpdateLog;

	private FormData fdText, fdSash, fdStart, fdPreview, fdError, fdClear, fdLog, fdOnlyActive, fdSafeMode;

	private boolean running;
    private boolean preview;
    private boolean initialized;

	public boolean preview_shown = false;

	private SelectionListener lsStart, lsPreview, lsError, lsClear, lsLog;

	private StringBuffer message;

	private FileInputStream in;

	private Trans trans;

	private Spoon spoon;

    private boolean halted;


	public SpoonLog(Composite parent, int style, Spoon sp, LogWriter l, String fname)
	{
		super(parent, style);
		shell = parent.getShell();
		spoon = sp;
		log = l;
		trans = null;
		display = shell.getDisplay();

		running = false;
		preview = false;
		lastUpdateView = 0L;
		lastUpdateLog = 0L;

		FormLayout formLayout = new FormLayout();
		formLayout.marginWidth = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		setLayout(formLayout);

		setVisible(true);
		spoon.props.setLook(this);

		SashForm sash = new SashForm(this, SWT.VERTICAL);
		spoon.props.setLook(sash);

		sash.setLayout(new FillLayout());

		colinf = new ColumnInfo[] { new ColumnInfo(Messages.getString("SpoonLog.Column.Stepname"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
				new ColumnInfo(Messages.getString("SpoonLog.Column.Copynr"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
				new ColumnInfo(Messages.getString("SpoonLog.Column.Read"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
				new ColumnInfo(Messages.getString("SpoonLog.Column.Written"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
				new ColumnInfo(Messages.getString("SpoonLog.Column.Input"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
				new ColumnInfo(Messages.getString("SpoonLog.Column.Output"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
				new ColumnInfo(Messages.getString("SpoonLog.Column.Updated"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
				new ColumnInfo(Messages.getString("SpoonLog.Column.Errors"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
				new ColumnInfo(Messages.getString("SpoonLog.Column.Active"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
				new ColumnInfo(Messages.getString("SpoonLog.Column.Time"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
				new ColumnInfo(Messages.getString("SpoonLog.Column.Speed"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
				new ColumnInfo(Messages.getString("SpoonLog.Column.PriorityBufferSizes"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
				new ColumnInfo(Messages.getString("SpoonLog.Column.Sleeps"), ColumnInfo.COLUMN_TYPE_TEXT, false, true) //$NON-NLS-1$
		};

		colinf[1].setAllignement(SWT.RIGHT);
		colinf[2].setAllignement(SWT.RIGHT);
		colinf[3].setAllignement(SWT.RIGHT);
		colinf[4].setAllignement(SWT.RIGHT);
		colinf[5].setAllignement(SWT.RIGHT);
		colinf[6].setAllignement(SWT.RIGHT);
		colinf[7].setAllignement(SWT.RIGHT);
		colinf[8].setAllignement(SWT.RIGHT);
		colinf[9].setAllignement(SWT.RIGHT);
		colinf[10].setAllignement(SWT.RIGHT);
		colinf[11].setAllignement(SWT.RIGHT);
		colinf[12].setAllignement(SWT.RIGHT);

		wFields = new TableView(sash, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf, 1, true, // readonly!
				null, // Listener
				spoon.props);

		wText = new Text(sash, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY);
		spoon.props.setLook(wText);
		wText.setVisible(true);

		wStart = new Button(this, SWT.PUSH);
		wStart.setText(START_TEXT);
		wPreview = new Button(this, SWT.PUSH);
		wPreview.setText(Messages.getString("SpoonLog.Button.Preview")); //$NON-NLS-1$
		wError = new Button(this, SWT.PUSH);
		wError.setText(Messages.getString("SpoonLog.Button.ShowErrorLines")); //$NON-NLS-1$
		wClear = new Button(this, SWT.PUSH);
		wClear.setText(Messages.getString("SpoonLog.Button.ClearLog")); //$NON-NLS-1$
		wLog = new Button(this, SWT.PUSH);
		wLog.setText(Messages.getString("SpoonLog.Button.LogSettings")); //$NON-NLS-1$
		wOnlyActive = new Button(this, SWT.CHECK);
		wOnlyActive.setText(Messages.getString("SpoonLog.Button.ShowOnlyActiveSteps")); //$NON-NLS-1$
        spoon.props.setLook(wOnlyActive);
		wSafeMode = new Button(this, SWT.CHECK);
		wSafeMode.setText(Messages.getString("SpoonLog.Button.SafeMode")); //$NON-NLS-1$
        spoon.props.setLook(wSafeMode);

		fdStart = new FormData();
		fdPreview = new FormData();
		fdError = new FormData();
		fdClear = new FormData();
		fdLog = new FormData();
		fdOnlyActive = new FormData();
		fdSafeMode = new FormData();

		fdStart.left = new FormAttachment(0, 10);
		fdStart.bottom = new FormAttachment(100, 0);
		wStart.setLayoutData(fdStart);

		fdPreview.left = new FormAttachment(wStart, 10);
		fdPreview.bottom = new FormAttachment(100, 0);
		wPreview.setLayoutData(fdPreview);

		fdError.left = new FormAttachment(wPreview, 10);
		fdError.bottom = new FormAttachment(100, 0);
		wError.setLayoutData(fdError);

		fdClear.left = new FormAttachment(wError, 10);
		fdClear.bottom = new FormAttachment(100, 0);
		wClear.setLayoutData(fdClear);

		fdLog.left = new FormAttachment(wClear, 10);
		fdLog.bottom = new FormAttachment(100, 0);
		wLog.setLayoutData(fdLog);

		fdOnlyActive.left = new FormAttachment(wLog, Const.MARGIN);
		fdOnlyActive.bottom = new FormAttachment(100, 0);
		wOnlyActive.setLayoutData(fdOnlyActive);
		wOnlyActive.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				spoon.props.setOnlyActiveSteps(wOnlyActive.getSelection());
			}
		});
		wOnlyActive.setSelection(spoon.props.getOnlyActiveSteps());

		fdSafeMode.left = new FormAttachment(wOnlyActive, Const.MARGIN);
		fdSafeMode.bottom = new FormAttachment(100, 0);
		wSafeMode.setLayoutData(fdSafeMode);
		
		// Put text in the middle
		fdText = new FormData();
		fdText.left = new FormAttachment(0, 0);
		fdText.top = new FormAttachment(0, 0);
		fdText.right = new FormAttachment(100, 0);
		fdText.bottom = new FormAttachment(100, 0);
		wText.setLayoutData(fdText);

		fdSash = new FormData();
		fdSash.left = new FormAttachment(0, 0); // First one in the left top corner
		fdSash.top = new FormAttachment(0, 0);
		fdSash.right = new FormAttachment(100, 0);
		fdSash.bottom = new FormAttachment(wStart, -5);
		sash.setLayoutData(fdSash);

		pack();

		try
		{
			in = log.getFileInputStream();
		}
		catch (Exception e)
		{
			log.logError(Spoon.APP_NAME, Messages.getString("SpoonLog.Log.CouldNotLinkInputToOutputPipe")); //$NON-NLS-1$
		}

		lsError = new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				showErrors();
			}
		};

		final Timer tim = new Timer();
        final StringBuffer busy = new StringBuffer("N");

        TimerTask timtask = new TimerTask()
        {
            public void run()
            {
                if (display != null && !display.isDisposed())
                {
                    display.asyncExec(
                        new Runnable()
                        {
                            public void run()
                            {
                                if (busy.toString().equals("N"))
                                {
                                    busy.setCharAt(0, 'Y');
                                    checkStartThreads();
                                    checkTransEnded();
                                    checkErrors();
                                    readLog();
                                    refreshView();
                                    busy.setCharAt(0, 'N');
                                }
                            }
                        }
                    );
                }
            }
        };

        tim.schedule(timtask, 0L, REFRESH_TIME); // schedule to repeat a couple of times per second to get fast feedback 


		lsStart = new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				startstop();
			}
		};

		lsPreview = new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				preview();
			}
		};

		lsClear = new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				clearLog();
			}
		};

		lsLog = new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				setLog();
			}
		};

		wError.addSelectionListener(lsError);
		wStart.addSelectionListener(lsStart);
		wPreview.addSelectionListener(lsPreview);
		wClear.addSelectionListener(lsClear);
		wLog.addSelectionListener(lsLog);

		addDisposeListener(new DisposeListener()
		{
			public void widgetDisposed(DisposeEvent e)
			{
				tim.cancel();
			}
		});
	}
    
    private void checkStartThreads()
    {
        if (initialized && !running && trans!=null)
        {
            startThreads();
        }
    }

    private void checkTransEnded()
    {
        if (trans != null)
        {
            if (trans.isFinished() && ( running || halted ))
            {
                log.logMinimal(Spoon.APP_NAME, Messages.getString("SpoonLog.Log.TransformationHasFinished")); //$NON-NLS-1$
    
                running = false;
                initialized=false;
                halted = false;
                
                try
                {
                    trans.endProcessing("end"); //$NON-NLS-1$
                    spoonHistoryRefresher.markRefreshNeeded();
                }
                catch (KettleException e)
                {
                    new ErrorDialog(shell, spoon.props, Messages.getString("SpoonLog.Dialog.ErrorWritingLogRecord.Title"), Messages.getString("SpoonLog.Dialog.ErrorWritingLogRecord.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
                }
                
                wStart.setText(START_TEXT);
            }
        }
    }

	public void startstop()
	{
		startstop(null);
	}

	final class DateValidator implements IInputValidator
	{
		final SimpleDateFormat df = new SimpleDateFormat(Trans.REPLAY_DATE_FORMAT);

		Date date = null;

		public String isValid(String dateString)
		{
			try
			{
				date = df.parse(dateString);
				return null;
			}
			catch (ParseException e)
			{
				return Messages.getString("SpoonLog.Error.InvalidReplayDateFormat") //$NON-NLS-1$
						+ Trans.REPLAY_DATE_FORMAT;
			}
		}
	}

	public void startstopReplay()
	{
		DateValidator dateValidator = new DateValidator();
		InputDialog id = new InputDialog(shell, Messages.getString("SpoonLog.Dialog.EnterReplayDate.Title"), //$NON-NLS-1$
				Messages.getString("SpoonLog.Dialog.WhatIsTheExecutionDate1.Message") + Const.CR //$NON-NLS-1$
						+ Messages.getString("SpoonLog.Dialog.WhatIsTheExecutionDate2.Message"), dateValidator.df.format(new Date()), //$NON-NLS-1$
				dateValidator);
		int answer = id.open();
		if (answer == 1)
		{
			log.logDebug(toString(), Messages.getString("SpoonLog.Log.CancelReplay1")); //$NON-NLS-1$
			return;
		}
		if (dateValidator.date == null)
		{
			log.logDebug(toString(), Messages.getString("SpoonLog.Log.CancelReplay1")); //$NON-NLS-1$
			return;
		}
		startstop(dateValidator.date);
	}

	public synchronized void startstop(Date replayDate)
	{
		if (!running) // Not running, start the transformation...
		{
			// Auto save feature...
			if (spoon.getTransMeta().hasChanged())
			{
				if (spoon.props.getAutoSave())
				{
					spoon.saveFile();
				}
				else
				{
					MessageDialogWithToggle md = new MessageDialogWithToggle(shell, Messages.getString("SpoonLog.Dialog.FileHasChanged.Title"), //$NON-NLS-1$
							null, Messages.getString("SpoonLog.Dialog.FileHasChanged1.Message") + Const.CR + Messages.getString("SpoonLog.Dialog.FileHasChanged2.Message") + Const.CR, //$NON-NLS-1$ //$NON-NLS-2$
							MessageDialog.QUESTION, new String[] { Messages.getString("System.Button.Yes"), Messages.getString("System.Button.No") }, //$NON-NLS-1$ //$NON-NLS-2$
							0, Messages.getString("SpoonLog.Dialog.Option.AutoSaveTransformation"), //$NON-NLS-1$
							spoon.props.getAutoSave());
					int answer = md.open();
					if (answer == 0)
					{
						spoon.saveFile();
					}
					spoon.props.setAutoSave(md.getToggleState());
				}
			}

			if (((spoon.getTransMeta().getName() != null && spoon.rep != null) || // Repository available & name set
					(spoon.getTransMeta().getFilename() != null && spoon.rep == null) // No repository & filename set
					) && !spoon.getTransMeta().hasChanged() // Didn't change
			)
			{
				if (trans == null || (trans != null && trans.isFinished()))
				{
					try
					{
						trans = new Trans(log, spoon.getTransMeta().getFilename(), spoon.getTransMeta().getName(), new String[] { spoon.getTransMeta().getFilename() });
						trans.setReplayDate(replayDate);
						trans.open(spoon.rep, spoon.getTransMeta().getName(), spoon.getTransMeta().getDirectory().getPath(), spoon.getTransMeta().getFilename());

						trans.setMonitored(true);
						log.logBasic(toString(), Messages.getString("SpoonLog.Log.TransformationOpened")); //$NON-NLS-1$
					}
					catch (KettleException e)
					{
						trans = null;
						new ErrorDialog(shell, spoon.props,
								Messages.getString("SpoonLog.Dialog.ErrorOpeningTransformation.Title"), Messages.getString("SpoonLog.Dialog.ErrorOpeningTransformation.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
					}
					readLog();
					if (trans != null)
					{
                        final String args[];
						Row arguments = getArguments(trans.getTransMeta());
						if (arguments != null)
						{
							args = convertArguments(arguments);
                        }
                        else
                        {
                            args = null;
                        }
                        getVariables(trans.getTransMeta());
                        
						log.logMinimal(Spoon.APP_NAME, Messages.getString("SpoonLog.Log.LaunchingTransformation") + trans.getTransMeta().getName() + "]..."); //$NON-NLS-1$ //$NON-NLS-2$
						trans.setSafeModeEnabled(wSafeMode.getSelection());
                        
                        // Launch the step preparation in a different thread. 
                        // That way Spoon doesn't block anymore and that way we can follow the progress of the initialisation
                        //
                        
                        final Thread parentThread = Thread.currentThread();
                        
                        display.asyncExec(
                                new Runnable() 
                                {
                                    public void run() 
                                    {
                                        prepareTrans(parentThread, args);
                                    }
                                }
                            );
                        
						log.logMinimal(Spoon.APP_NAME, Messages.getString("SpoonLog.Log.StartedExecutionOfTransformation")); //$NON-NLS-1$
						wStart.setText(STOP_TEXT);
						readLog();
					}
				}
				else
				{
					MessageBox m = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
					m.setText(Messages.getString("SpoonLog.Dialog.DoNoStartTransformationTwice.Title")); //$NON-NLS-1$
					m.setMessage(Messages.getString("SpoonLog.Dialog.DoNoStartTransformationTwice.Message")); //$NON-NLS-1$
					m.open();
				}
			}
			else
			{
				if (spoon.getTransMeta().hasChanged())
				{
					MessageBox m = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
					m.setText(Messages.getString("SpoonLog.Dialog.SaveTransformationBeforeRunning.Title")); //$NON-NLS-1$
					m.setMessage(Messages.getString("SpoonLog.Dialog.SaveTransformationBeforeRunning.Message")); //$NON-NLS-1$
					m.open();
				}
				else if (spoon.rep != null && spoon.getTransMeta().getName() == null)
				{
					MessageBox m = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
					m.setText(Messages.getString("SpoonLog.Dialog.GiveTransformationANameBeforeRunning.Title")); //$NON-NLS-1$
					m.setMessage(Messages.getString("SpoonLog.Dialog.GiveTransformationANameBeforeRunning.Message")); //$NON-NLS-1$
					m.open();
				}
				else
				{
					MessageBox m = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
					m.setText(Messages.getString("SpoonLog.Dialog.SaveTransformationBeforeRunning2.Title")); //$NON-NLS-1$
					m.setMessage(Messages.getString("SpoonLog.Dialog.SaveTransformationBeforeRunning2.Message")); //$NON-NLS-1$
					m.open();
				}
			}
		}
		else
		{
			trans.stopAll();
			try
			{
				trans.endProcessing("stop"); //$NON-NLS-1$
				log.logMinimal(Spoon.APP_NAME, Messages.getString("SpoonLog.Log.ProcessingOfTransformationStopped")); //$NON-NLS-1$
			}
			catch (KettleException e)
			{
				new ErrorDialog(shell, spoon.props, Messages.getString("SpoonLog.Dialog.ErrorWritingLogRecord.Title"), Messages.getString("SpoonLog.Dialog.ErrorWritingLogRecord.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
			}
			wStart.setText(START_TEXT);
            running = false;
            initialized = false;
            halted = false;
			if (preview)
			{
				preview = false;
				showPreview();
			}
		}
	}

	private synchronized void prepareTrans(final Thread parentThread, final String[] args)
    {
        Runnable runnable = new Runnable()
        {
            public void run()
            {
                LocalVariables.getInstance().createKettleVariables(Thread.currentThread().getName(), parentThread.getName(), true);
                initialized = trans.prepareExecution(args);
                halted = trans.hasHaltedSteps();
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
        
        refreshView();
    }
    
    private synchronized void startThreads()
    {
        running=true;
        trans.startThreads();
    }

    private void getVariables(TransMeta transMeta)
    {
        Properties sp = new Properties();
        KettleVariables kettleVariables = KettleVariables.getInstance();
        sp.putAll(kettleVariables.getProperties());
        sp.putAll(System.getProperties());
 
        List vars = transMeta.getUsedVariables();
        if (vars!=null && vars.size()>0)
        {
            Row variables = new Row();
            for (int i=0;i<vars.size();i++) 
            {
                String varname = (String)vars.get(i);
                if (!varname.startsWith(Const.INTERNAL_VARIABLE_PREFIX))
                {
                    Value varval = new Value(varname, sp.getProperty(varname, ""));
                    variables.addValue( varval );
                }
            }
            
            if (variables.size()>0)
            {
                EnterStringsDialog esd = new EnterStringsDialog(shell, SWT.NONE, variables);
                if (esd.open()!=null)
                {
                    for (int i=0;i<variables.size();i++)
                    {
                        Value varval = variables.getValue(i);
                        kettleVariables.setVariable(varval.getName(), varval.getString());
                        System.out.println("Variable ${"+varval.getName()+"} set to ["+varval.getString()+"]");
                    }
                }
            }
        }
    }

    public Row getArguments(TransMeta transMeta)
	{
		// OK, see if we need to ask for some arguments first...
		//
		Row arguments = transMeta.getUsedArguments(spoon.getArguments());
		if (arguments.size() > 0)
		{
			EnterStringsDialog esd = new EnterStringsDialog(shell, SWT.NONE, arguments);
			if (esd.open() == null)
			{
				arguments = null;
			}
			else
			{
				spoon.props.setLastArguments(Props.convertArguments(arguments));
			}
		}
		return arguments;
	}

	public void checkErrors()
	{
		if (trans != null)
		{
			if (!trans.isFinished())
			{
				if (trans.getErrors() != 0)
				{
					trans.killAll();
				}
			}
		}
	}

	public void readLog()
	{
		long time = new Date().getTime();
		long msSinceLastUpdate = time - lastUpdateLog;
		if (msSinceLastUpdate < UPDATE_TIME_LOG)
		{
			return;
		}
		lastUpdateLog = time;

		if (message == null)
			message = new StringBuffer();
		else
			message.setLength(0);
		try
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(in, Const.XML_ENCODING));
			String line;
			while ((line = reader.readLine()) != null)
			{
				message.append(line);
				message.append(Const.CR);
			}
		}
		catch (Exception ex)
		{
			message.append("Unexpected error reading the log: " + ex.toString());
		}

		if (!wText.isDisposed() && message.length() > 0)
		{
			String mess = wText.getText();
			wText.setSelection(mess.length());
			wText.clearSelection();
			wText.insert(message.toString());
			mess = wText.getText();
		}

	}

	private boolean refresh_busy;

	private SpoonHistoryRefresher spoonHistoryRefresher;

	private void refreshView()
	{
		boolean insert = true;
		float lapsed;

  		if (wFields.isDisposed()) return;
		if (refresh_busy) return;

		refresh_busy = true;

		Table table = wFields.table;

		boolean doPreview = trans != null && trans.previewComplete() && preview;

		long time = new Date().getTime();
		long msSinceLastUpdate = time - lastUpdateView;
		if ((trans != null && msSinceLastUpdate > UPDATE_TIME_VIEW) || doPreview)
		{
            lastUpdateView = time;
			int nrSteps = trans.nrSteps();
			if (wOnlyActive.getSelection()) nrSteps = trans.nrActiveSteps();

			if (table.getItemCount() != nrSteps)
            {
				table.removeAll();
            }
			else
            {
				insert = false;
            }

			if (nrSteps == 0)
			{
				if (table.getItemCount() == 0) new TableItem(table, SWT.NONE);
			}

			int nr = 0;
			for (int i = 0; i < trans.nrSteps(); i++)
			{
				BaseStep baseStep = trans.getRunThread(i);
				if ( (baseStep.isAlive() && wOnlyActive.getSelection()) || baseStep.getStatus()!=StepDataInterface.STATUS_EMPTY)
				{
                    TableItem ti;
                    if (insert)
                    {
						ti = new TableItem(table, SWT.NONE);
                    }
					else
                    {
						ti = table.getItem(nr);
                    }

					// Proc: nr of lines processed: input + output!
					long in_proc = baseStep.linesInput + baseStep.linesRead;
					long out_proc = baseStep.linesOutput + baseStep.linesWritten + baseStep.linesUpdated;

					lapsed = ((float) baseStep.getRuntime()) / 1000;
					double in_speed = 0;
					double out_speed = 0;

					if (lapsed != 0)
					{
						in_speed = Math.floor(10 * (in_proc / lapsed)) / 10;
						out_speed = Math.floor(10 * (out_proc / lapsed)) / 10;
					}

					String fields[] = new String[colinf.length + 1];
					fields[1] = baseStep.getStepname();
					fields[2] = "" + baseStep.getCopy(); //$NON-NLS-1$
					fields[3] = "" + baseStep.linesRead; //$NON-NLS-1$
					fields[4] = "" + baseStep.linesWritten; //$NON-NLS-1$
					fields[5] = "" + baseStep.linesInput; //$NON-NLS-1$
					fields[6] = "" + baseStep.linesOutput; //$NON-NLS-1$
					fields[7] = "" + baseStep.linesUpdated; //$NON-NLS-1$
					fields[8] = "" + baseStep.getErrors(); //$NON-NLS-1$
					fields[9] = "" + baseStep.getStatusDescription(); //$NON-NLS-1$
					fields[10] = "" + Math.floor((lapsed * 10) + 0.5) / 10; //$NON-NLS-1$
					fields[11] = lapsed == 0 ? "-" : "" + (in_speed > out_speed ? in_speed : out_speed); //$NON-NLS-1$ //$NON-NLS-2$
					fields[12] = baseStep.isAlive() ? "" + baseStep.getPriority() + "/" + baseStep.rowsetInputSize() + "/" + baseStep.rowsetOutputSize() : "-"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					fields[13] = "" + baseStep.getNrGetSleeps() + "/" + baseStep.getNrPutSleeps();
					// Anti-flicker: if nothing has changed, don't change it on the screen!
					for (int f = 1; f < fields.length; f++)
					{
						if (!fields[f].equalsIgnoreCase(ti.getText(f)))
						{
							ti.setText(f, fields[f]);
						}
					}

					// Error lines should appear in red:
					if (baseStep.getErrors() > 0)
					{
						ti.setBackground(GUIResource.getInstance().getColorRed());
					}
					else
					{
						ti.setBackground(GUIResource.getInstance().getColorWhite());
					}

					nr++;
				}
			}
			wFields.setRowNums();
			wFields.optWidth(true);
		}
		else
		{
			// We need at least one table-item in a table!
			if (table.getItemCount() == 0) new TableItem(table, SWT.NONE);
		}

		if (doPreview)
		{
			// System.out.println("preview is complete, show preview dialog!");
			trans.stopAll();
			showPreview();
		}

		refresh_busy = false;
	}

	public void preview()
	{
		try
		{
			log.logDetailed(toString(), Messages.getString("SpoonLog.Log.DoPreview")); //$NON-NLS-1$
			PreviewSelectDialog psd = new PreviewSelectDialog(shell, SWT.NONE, log, spoon.props, spoon.getTransMeta());
			psd.open();
			if (psd.previewSteps != null)
			{
                String[] args=null;
				Row arguments = getArguments(spoon.getTransMeta());
				if (arguments != null)
				{
					args = convertArguments(arguments);
                }
                getVariables(spoon.getTransMeta());

				spoon.tabfolder.setSelection(1);
				trans = new Trans(log, spoon.getTransMeta(), psd.previewSteps, psd.previewSizes);
				trans.execute(args);
				preview = true;
				readLog();
				running = !running;
				wStart.setText(STOP_TEXT);
			}
		}
		catch (Exception e)
		{
			new ErrorDialog(shell, spoon.props, Messages.getString("SpoonLog.Dialog.UnexpectedErrorDuringPreview.Title"), Messages.getString("SpoonLog.Dialog.UnexpectedErrorDuringPreview.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	private String[] convertArguments(Row arguments)
	{
		String args[] = new String[10];
		for (int i = 0; i < args.length; i++)
		{
			for (int v = 0; v < arguments.size(); v++)
			{
				Value value = arguments.getValue(v);
				if (value.getName().equalsIgnoreCase("Argument " + (i + 1))) //$NON-NLS-1$
				{
					args[i] = value.getString();
				}
			}
		}
		return args;
	}

	public void showPreview()
	{
		if (preview_shown) return;
		if (trans == null || !trans.isFinished()) return;

		// Drop out of preview mode!
		preview = false;

		BaseStep rt;
		int i;

		ArrayList buffers = new ArrayList();
		ArrayList names = new ArrayList();
		for (i = 0; i < trans.nrSteps(); i++)
		{
			rt = trans.getRunThread(i);
			if (rt.previewSize > 0)
			{
				buffers.add(rt.previewBuffer);
				names.add(rt.getStepname());
				log.logBasic(toString(), Messages.getString("SpoonLog.Log.Step") + rt.getStepname() + " --> " + rt.previewBuffer.size() + Messages.getString("SpoonLog.Log.Rows")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		}
		// OK, now we're ready to show it all!
		EnterPreviewRowsDialog psd = new EnterPreviewRowsDialog(shell, SWT.NONE, names, buffers);
		preview_shown = true;
		psd.open();
		preview_shown = false;
	}

	private void clearLog()
	{
		wFields.table.removeAll();
		new TableItem(wFields.table, SWT.NONE);
		wText.setText(""); //$NON-NLS-1$
	}

	private void setLog()
	{
		LogSettingsDialog lsd = new LogSettingsDialog(shell, SWT.NONE, log, spoon.props);
		lsd.open();
	}

	public void showErrors()
	{
		String all = wText.getText();
		ArrayList err = new ArrayList();

		int i = 0;
		int startpos = 0;
		int crlen = Const.CR.length();

		while (i < all.length() - crlen)
		{
			if (all.substring(i, i + crlen).equalsIgnoreCase(Const.CR))
			{
				String line = all.substring(startpos, i);
				String uLine = line.toUpperCase();
				if (uLine.indexOf(Messages.getString("SpoonLog.System.ERROR")) >= 0 || //$NON-NLS-1$
						uLine.indexOf(Messages.getString("SpoonLog.System.EXCEPTION")) >= 0 || //$NON-NLS-1$
						uLine.indexOf("ERROR") >= 0 || // i18n for compatibilty to non translated steps a.s.o. //$NON-NLS-1$ 
						uLine.indexOf("EXCEPTION") >= 0 // i18n for compatibilty to non translated steps a.s.o. //$NON-NLS-1$
				)
				{
					err.add(line);
				}
				// New start of line
				startpos = i + crlen;
			}

			i++;
		}
		String line = all.substring(startpos);
		String uLine = line.toUpperCase();
		if (uLine.indexOf(Messages.getString("SpoonLog.System.ERROR2")) >= 0 || //$NON-NLS-1$
				uLine.indexOf(Messages.getString("SpoonLog.System.EXCEPTION2")) >= 0 || //$NON-NLS-1$
				uLine.indexOf("ERROR") >= 0 || // i18n for compatibilty to non translated steps a.s.o. //$NON-NLS-1$ 
				uLine.indexOf("EXCEPTION") >= 0 // i18n for compatibilty to non translated steps a.s.o. //$NON-NLS-1$
		)
		{
			err.add(line);
		}

		if (err.size() > 0)
		{
			String err_lines[] = new String[err.size()];
			for (i = 0; i < err_lines.length; i++)
				err_lines[i] = (String) err.get(i);

			EnterSelectionDialog esd = new EnterSelectionDialog(shell, spoon.props, err_lines,
					Messages.getString("SpoonLog.Dialog.ErrorLines.Title"), Messages.getString("SpoonLog.Dialog.ErrorLines.Message")); //$NON-NLS-1$ //$NON-NLS-2$
			line = esd.open();
			if (line != null)
			{
				for (i = 0; i < spoon.getTransMeta().nrSteps(); i++)
				{
					StepMeta stepMeta = spoon.getTransMeta().getStep(i);
					if (line.indexOf(stepMeta.getName()) >= 0)
					{
						spoon.editStep(stepMeta.getName());
					}
				}
				// System.out.println("Error line selected: "+line);
			}
		}
	}

	public String toString()
	{
		return Spoon.APP_NAME;
	}

	/**
	 * @return Returns the running.
	 */
	public boolean isRunning()
	{
		return running;
	}

	public void setSpoonHistoryRefresher(SpoonHistoryRefresher spoonHistoryRefresher)
	{
		this.spoonHistoryRefresher = spoonHistoryRefresher;
	}

}
