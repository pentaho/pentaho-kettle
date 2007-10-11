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

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

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
import be.ibridge.kettle.core.logging.BufferChangedListener;
import be.ibridge.kettle.core.logging.Log4jStringAppender;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.core.widget.TableView;
import be.ibridge.kettle.spoon.dialog.EnterPreviewRowsDialog;
import be.ibridge.kettle.spoon.dialog.LogSettingsDialog;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransExecutionConfiguration;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStep;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepStatus;

/**
 * SpoonLog handles the display of the logging information in the Spoon logging window.
 *  
 * @see be.ibridge.kettle.spoon.Spoon
 * @author Matt
 * @since  17 may 2003
 */
public class SpoonLog extends Composite implements TabItemInterface
{
    private static final LogWriter log = LogWriter.getInstance();
    
    public static final long UPDATE_TIME_VIEW = 1000L;
	public static final long UPDATE_TIME_LOG = 2000L;
	public static final long REFRESH_TIME = 100L;

	public final static String START_TEXT = Messages.getString("SpoonLog.Button.StartTransformation"); //$NON-NLS-1$
	public final static String STOP_TEXT = Messages.getString("SpoonLog.Button.StopTransformation"); //$NON-NLS-1$

	private Display display;
    private Shell shell;
    private TransMeta transMeta;
    

	private ColumnInfo[] colinf;
	private TableView wFields;
	private Button wOnlyActive;
	private Button wSafeMode;
	private Text wText;
	private Button wStart;
    private Button wStop;
	private Button wPreview;
	private Button wError;
	private Button wClear;
	private Button wLog;
	private long lastUpdateView;

	private FormData fdText, fdSash, fdStart, fdPreview, fdError, fdClear, fdLog, fdOnlyActive, fdSafeMode;

	private boolean running;
    private boolean preview;
    private boolean initialized;

	public boolean preview_shown = false;

	private SelectionListener lsStart, lsStop, lsPreview, lsError, lsClear, lsLog;

	private Trans trans;

	private Spoon spoon;

    private boolean halted;
    private boolean halting;

    private FormData fdStop;    
    
	private Log4jStringAppender stringAppender;    
	private int textSize;

	public SpoonLog(Composite parent, final Spoon spoon, final TransMeta transMeta)
	{
		super(parent, SWT.NONE);
		shell = parent.getShell();
		this.spoon = spoon;
        this.transMeta = transMeta;
		trans = null;
		display = shell.getDisplay();

		running = false;
		preview = false;
		lastUpdateView = 0L;

		FormLayout formLayout = new FormLayout();
		formLayout.marginWidth = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		setLayout(formLayout);

		setVisible(true);
		spoon.props.setLook(this);

		SashForm sash = new SashForm(this, SWT.VERTICAL);
		spoon.props.setLook(sash);

		sash.setLayout(new FillLayout());

		colinf = new ColumnInfo[] { 
                new ColumnInfo(Messages.getString("SpoonLog.Column.Stepname"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
				new ColumnInfo(Messages.getString("SpoonLog.Column.Copynr"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
				new ColumnInfo(Messages.getString("SpoonLog.Column.Read"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
				new ColumnInfo(Messages.getString("SpoonLog.Column.Written"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
				new ColumnInfo(Messages.getString("SpoonLog.Column.Input"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
				new ColumnInfo(Messages.getString("SpoonLog.Column.Output"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
				new ColumnInfo(Messages.getString("SpoonLog.Column.Updated"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
                new ColumnInfo(Messages.getString("SpoonLog.Column.Rejected"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
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
		colinf[13].setAllignement(SWT.RIGHT);

		wFields = new TableView(sash, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf, 1, true, // readonly!
				null, // Listener
				spoon.props);

		wText = new Text(sash, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY);
		spoon.props.setLook(wText);
		wText.setVisible(true);

		wStart = new Button(this, SWT.PUSH);
		wStart.setText(START_TEXT);
        wStart.setEnabled(true);
        
        wStop= new Button(this, SWT.PUSH);
        wStop.setText(STOP_TEXT);
        wStop.setEnabled(false);
        
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
        fdStart.left = new FormAttachment(0, 10);
		fdStart.bottom = new FormAttachment(100, 0);
		wStart.setLayoutData(fdStart);

        fdStop = new FormData();
        fdStop.left = new FormAttachment(wStart, 10);
        fdStop.bottom = new FormAttachment(100, 0);
        wStop.setLayoutData(fdStop);

        fdPreview = new FormData();
        fdPreview.left = new FormAttachment(wStop, 10);
		fdPreview.bottom = new FormAttachment(100, 0);
		wPreview.setLayoutData(fdPreview);

        fdError = new FormData();
        fdError.left = new FormAttachment(wPreview, 10);
		fdError.bottom = new FormAttachment(100, 0);
		wError.setLayoutData(fdError);

        fdClear = new FormData();
        fdClear.left = new FormAttachment(wError, 10);
		fdClear.bottom = new FormAttachment(100, 0);
		wClear.setLayoutData(fdClear);

        fdLog = new FormData();
        fdLog.left = new FormAttachment(wClear, 10);
		fdLog.bottom = new FormAttachment(100, 0);
		wLog.setLayoutData(fdLog);

        fdOnlyActive = new FormData();
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

        fdSafeMode = new FormData();
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

		// Create a new String appender to the log and capture that directly...
		//
		stringAppender = LogWriter.createStringAppender();
		stringAppender.setMaxNrLines(Props.getInstance().getMaxNrLinesInLog());
		stringAppender.addBufferChangedListener(new BufferChangedListener() {
		
			public void contentWasAdded(final StringBuffer content, final String extra, final int nrLines) {
				display.asyncExec(new Runnable() {
				
					public void run() 
					{
						if (!wText.isDisposed())
						{
							textSize++;
							
							// OK, now what if the number of lines gets too big?
							// We allow for a few hundred lines buffer over-run.
							// That way we reduce flicker...
							//
							if (textSize>=nrLines+200)
							{
								wText.setText(content.toString());
								wText.setSelection(content.length());
								wText.showSelection();
								wText.clearSelection();
								textSize=nrLines;
							}
							else
							{
								wText.append(extra);
							}
						}
					}
				
				});
			}
		
		});
		log.addAppender(stringAppender);
		addDisposeListener(new DisposeListener() { public void widgetDisposed(DisposeEvent e) { log.removeAppender(stringAppender); } });


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
				spoon.executeTransformation(transMeta, true, false, false, false, null);
			}
		};

        lsStop = new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                stop();
            }
        };

		lsPreview = new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				spoon.executeTransformation(transMeta, true, false, false, true, null);
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
        wStop.addSelectionListener(lsStop);
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
                halting = false;
                
                try
                {
                    trans.endProcessing("end"); //$NON-NLS-1$
                    if (spoonHistoryRefresher!=null) spoonHistoryRefresher.markRefreshNeeded();
                }
                catch (KettleException e)
                {
                    new ErrorDialog(shell, Messages.getString("SpoonLog.Dialog.ErrorWritingLogRecord.Title"), Messages.getString("SpoonLog.Dialog.ErrorWritingLogRecord.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
                }
                
                wStart.setEnabled(true);
                wStop.setEnabled(false);
            }
        }
    }

	public synchronized void start(TransExecutionConfiguration executionConfiguration)
	{
		if (!running) // Not running, start the transformation...
		{
			// Auto save feature...
			if (transMeta.hasChanged())
			{
				if (spoon.props.getAutoSave())
				{
					spoon.saveTransFile(transMeta);
				}
				else
				{
					MessageDialogWithToggle md = new MessageDialogWithToggle(shell, Messages.getString("SpoonLog.Dialog.FileHasChanged.Title"), //$NON-NLS-1$
							null, Messages.getString("SpoonLog.Dialog.FileHasChanged1.Message") + Const.CR + Messages.getString("SpoonLog.Dialog.FileHasChanged2.Message") + Const.CR, //$NON-NLS-1$ //$NON-NLS-2$
							MessageDialog.QUESTION, new String[] { Messages.getString("System.Button.Yes"), Messages.getString("System.Button.No") }, //$NON-NLS-1$ //$NON-NLS-2$
							0, Messages.getString("SpoonLog.Dialog.Option.AutoSaveTransformation"), //$NON-NLS-1$
							spoon.props.getAutoSave());
					int answer = md.open();
					if ( (answer & 0xFF) == 0)
					{
						spoon.saveTransFile(transMeta);
					}
					spoon.props.setAutoSave(md.getToggleState());
				}
			}

			if (((transMeta.getName() != null && spoon.rep != null) || // Repository available & name set
					(transMeta.getFilename() != null && spoon.rep == null) // No repository & filename set
					) && !transMeta.hasChanged() // Didn't change
			)
			{
				if (trans == null || (trans != null && trans.isFinished()))
				{
					try
					{
                        // Set the requested logging level.
                        log.setLogLevel(executionConfiguration.getLogLevel());

						trans = new Trans(log, transMeta.getFilename(), transMeta.getName(), new String[] { transMeta.getFilename() });
						trans.setReplayDate(executionConfiguration.getReplayDate());
						trans.open(spoon.rep, transMeta.getName(), transMeta.getDirectory().getPath(), transMeta.getFilename());

						trans.setMonitored(true);
						log.logBasic(toString(), Messages.getString("SpoonLog.Log.TransformationOpened")); //$NON-NLS-1$
					}
					catch (KettleException e)
					{
						trans = null;
						new ErrorDialog(shell,
								Messages.getString("SpoonLog.Dialog.ErrorOpeningTransformation.Title"), Messages.getString("SpoonLog.Dialog.ErrorOpeningTransformation.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
					}

					if (trans != null)
					{
						Row arguments = executionConfiguration.getArguments();
                        final String args[];
						if (arguments != null) args = convertArguments(arguments); else args = null;
                        setVariables(executionConfiguration);
                        
						log.logMinimal(Spoon.APP_NAME, Messages.getString("SpoonLog.Log.LaunchingTransformation") + trans.getTransMeta().getName() + "]..."); //$NON-NLS-1$ //$NON-NLS-2$
						trans.setSafeModeEnabled(executionConfiguration.isSafeModeEnabled());
                        
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
						wStart.setEnabled(false);
                        wStop.setEnabled(true);
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
				if (transMeta.hasChanged())
				{
					MessageBox m = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
					m.setText(Messages.getString("SpoonLog.Dialog.SaveTransformationBeforeRunning.Title")); //$NON-NLS-1$
					m.setMessage(Messages.getString("SpoonLog.Dialog.SaveTransformationBeforeRunning.Message")); //$NON-NLS-1$
					m.open();
				}
				else if (spoon.rep != null && transMeta.getName() == null)
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
	}
    
    public synchronized void stop()
    {
        if (running && !halting)
        {
            halting = true;
            trans.stopAll();
            try
            {
                trans.endProcessing("stop"); //$NON-NLS-1$
                log.logMinimal(Spoon.APP_NAME, Messages.getString("SpoonLog.Log.ProcessingOfTransformationStopped")); //$NON-NLS-1$
            }
            catch (KettleException e)
            {
                new ErrorDialog(shell, Messages.getString("SpoonLog.Dialog.ErrorWritingLogRecord.Title"), Messages.getString("SpoonLog.Dialog.ErrorWritingLogRecord.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
            }
            wStart.setEnabled(true);
            wStop.setEnabled(false);
            running = false;
            initialized = false;
            halted = false;
            halting = false;
            if (preview)
            {
                preview = false;
                showPreview();
            }
            transMeta.setInternalKettleVariables(); // set the original vars back as they may be changed by a mapping
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

    private void setVariables(TransExecutionConfiguration executionConfiguration)
    {
        Row variables = executionConfiguration.getVariables();
        KettleVariables.getInstance().setVariables(variables);
    }

    public Row getArguments(TransMeta transMeta)
	{
		// OK, see if we need to ask for some arguments first...
		//
		Row arguments = transMeta.getUsedArguments(spoon.getArguments());
		if (arguments.size() > 0)
		{
			EnterStringsDialog esd = new EnterStringsDialog(shell, SWT.NONE, arguments);
            esd.setReadOnly(false);
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

	private boolean refresh_busy;

	private SpoonHistoryRefresher spoonHistoryRefresher;

	private void refreshView()
	{
		boolean insert = true;

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
                    StepStatus stepStatus = new StepStatus(baseStep);
                    TableItem ti;
                    if (insert)
                    {
						ti = new TableItem(table, SWT.NONE);
                    }
					else
                    {
						ti = table.getItem(nr);
                    }

					String fields[] = stepStatus.getSpoonLogFields();

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

	public synchronized void preview(TransExecutionConfiguration executionConfiguration)
	{
        if (!running)
        {
    		try
    		{
                log.setLogLevel(executionConfiguration.getLogLevel());
    			log.logDetailed(toString(), Messages.getString("SpoonLog.Log.DoPreview")); //$NON-NLS-1$
                String[] args=null;
				Row arguments = executionConfiguration.getArguments();
				if (arguments != null)
				{
					args = convertArguments(arguments);
                }
                setVariables(executionConfiguration);

				// SB: don't set it to the first tabfolder
                // spoon.tabfolder.setSelection(1);
				trans = new Trans(log, transMeta, executionConfiguration.getPreviewSteps(), executionConfiguration.getPreviewSizes());
                trans.setSafeModeEnabled(executionConfiguration.isSafeModeEnabled());
				trans.execute(args);
				preview = true;
				running = !running;
                wStart.setEnabled(false);
                wStop.setEnabled(true);
    		}
    		catch (Exception e)
    		{
    			new ErrorDialog(shell, Messages.getString("SpoonLog.Dialog.UnexpectedErrorDuringPreview.Title"), Messages.getString("SpoonLog.Dialog.UnexpectedErrorDuringPreview.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
    		}
        }
        else
        {
            MessageBox m = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
            m.setText(Messages.getString("SpoonLog.Dialog.DoNoPreviewWhileRunning.Title")); //$NON-NLS-1$
            m.setMessage(Messages.getString("SpoonLog.Dialog.DoNoPreviewWhileRunning.Message")); //$NON-NLS-1$
            m.open();
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

			EnterSelectionDialog esd = new EnterSelectionDialog(shell, err_lines, Messages.getString("SpoonLog.Dialog.ErrorLines.Title"), Messages.getString("SpoonLog.Dialog.ErrorLines.Message")); //$NON-NLS-1$ //$NON-NLS-2$
			line = esd.open();
			if (line != null)
			{
				for (i = 0; i < transMeta.nrSteps(); i++)
				{
					StepMeta stepMeta = transMeta.getStep(i);
					if (line.indexOf(stepMeta.getName()) >= 0)
					{
						spoon.editStep(transMeta, stepMeta);
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

    public boolean isSafeModeChecked()
    {
        return wSafeMode.getSelection();
    }

    /**
     * @return the transMeta
     */
    public TransMeta getTransMeta()
    {
        return transMeta;
    }

    /**
     * @param transMeta the transMeta to set
     */
    public void setTransMeta(TransMeta transMeta)
    {
        this.transMeta = transMeta;
    }

    public boolean canBeClosed()
    {
        return !running && !preview;
    }
    
    public Object getManagedObject()
    {
        return transMeta;
    }
    
    public boolean hasContentChanged()
    {
        return false;
    }
    
    public int showChangedWarning()
    {
        // show running error.
        MessageBox mb = new MessageBox(shell,  SWT.YES | SWT.NO | SWT.ICON_QUESTION);
        mb.setMessage(Messages.getString("Spoon.Message.Warning.PromptExitWhenRunTransformation"));// There is a running transformation.  Do you want to stop it and quit Spoon?
        mb.setText(Messages.getString("System.Warning")); //Warning
        int answer = mb.open();
        if (answer==SWT.NO) return SWT.CANCEL;
        return answer;
    }
    
    public boolean applyChanges()
    {
        return true;
    }
}
