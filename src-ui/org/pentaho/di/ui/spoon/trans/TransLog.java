/* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

package org.pentaho.di.ui.spoon.trans;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
import org.pentaho.di.core.Const;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.BufferChangedListener;
import org.pentaho.di.core.logging.Log4jStringAppender;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.debug.BreakPointListener;
import org.pentaho.di.trans.debug.StepDebugMeta;
import org.pentaho.di.trans.debug.TransDebugMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepStatus;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.spoon.Messages;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.TabItemInterface;
import org.pentaho.di.ui.spoon.dialog.EnterPreviewRowsDialog;
import org.pentaho.di.ui.spoon.dialog.LogSettingsDialog;


/**
 * TransLog handles the display of the transformation logging information in the Spoon logging window.
 *  
 * @see org.pentaho.di.ui.spoon.Spoon
 * @author Matt
 * @since  17 may 2003
 */
public class TransLog extends Composite implements TabItemInterface
{
    private static final LogWriter log = LogWriter.getInstance();
    
    public static final long UPDATE_TIME_VIEW = 1000L;
	public static final long UPDATE_TIME_LOG = 2000L;
	public static final long REFRESH_TIME = 100L;
	
	public final static String START_TEXT = Messages.getString("TransLog.Button.StartTransformation"); //$NON-NLS-1$
	public final static String PAUSE_TEXT = Messages.getString("TransLog.Button.PauseTransformation"); //$NON-NLS-1$
	public final static String RESUME_TEXT = Messages.getString("TransLog.Button.ResumeTransformation"); //$NON-NLS-1$
	public final static String STOP_TEXT = Messages.getString("TransLog.Button.StopTransformation"); //$NON-NLS-1$

	private Display display;
    private Shell shell;
    private TransMeta transMeta;
    
	private ColumnInfo[] colinf;
	private TableView wFields;
	private Button wOnlyActive;
	private Button wSafeMode;
	private Text wText;
	private Button wStart;
	private Button wPause;
    private Button wStop;
	private Button wPreview;
	private Button wError;
	private Button wClear;
	private Button wLog;
	private long lastUpdateView;

	private FormData fdText, fdSash, fdStart, fdPause, fdPreview, fdError, fdClear, fdLog, fdOnlyActive, fdSafeMode;

	private boolean running;
    private boolean initialized;

	private SelectionListener lsStart, lsPause, lsStop, lsPreview, lsError, lsClear, lsLog;

	private Trans trans;

	private Spoon spoon;

    private boolean halted;
    private boolean halting;
    private boolean debug;

    private FormData fdStop;

	private boolean pausing;

	private Log4jStringAppender stringAppender;    
	private int textSize;

	public TransLog(Composite parent, final Spoon spoon, final TransMeta transMeta)
	{
		super(parent, SWT.NONE);
		shell = parent.getShell();
		this.spoon = spoon;
        this.transMeta = transMeta;
		trans = null;
		display = shell.getDisplay();

		running = false;
		debug = false;
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
                new ColumnInfo(Messages.getString("TransLog.Column.Stepname"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
				new ColumnInfo(Messages.getString("TransLog.Column.Copynr"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
				new ColumnInfo(Messages.getString("TransLog.Column.Read"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
				new ColumnInfo(Messages.getString("TransLog.Column.Written"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
				new ColumnInfo(Messages.getString("TransLog.Column.Input"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
				new ColumnInfo(Messages.getString("TransLog.Column.Output"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
				new ColumnInfo(Messages.getString("TransLog.Column.Updated"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
                new ColumnInfo(Messages.getString("TransLog.Column.Rejected"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
				new ColumnInfo(Messages.getString("TransLog.Column.Errors"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
				new ColumnInfo(Messages.getString("TransLog.Column.Active"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
				new ColumnInfo(Messages.getString("TransLog.Column.Time"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
				new ColumnInfo(Messages.getString("TransLog.Column.Speed"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
				new ColumnInfo(Messages.getString("TransLog.Column.PriorityBufferSizes"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
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

		wFields = new TableView(transMeta, sash, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf, 1, true, // readonly!
				null, // Listener
				spoon.props);

		wText = new Text(sash, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY);
		spoon.props.setLook(wText);
		wText.setVisible(true);

		wStart = new Button(this, SWT.PUSH);
		wStart.setText(START_TEXT);
        wStart.setEnabled(true);

		wPause= new Button(this, SWT.PUSH);
		wPause.setText(PAUSE_TEXT);
        wPause.setEnabled(false);

        wStop= new Button(this, SWT.PUSH);
        wStop.setText(STOP_TEXT);
        wStop.setEnabled(false);
        
		wPreview = new Button(this, SWT.PUSH);
		wPreview.setText(Messages.getString("TransLog.Button.Preview")); //$NON-NLS-1$
        
		wError = new Button(this, SWT.PUSH);
		wError.setText(Messages.getString("TransLog.Button.ShowErrorLines")); //$NON-NLS-1$
        
		wClear = new Button(this, SWT.PUSH);
		wClear.setText(Messages.getString("TransLog.Button.ClearLog")); //$NON-NLS-1$
        
		wLog = new Button(this, SWT.PUSH);
		wLog.setText(Messages.getString("TransLog.Button.LogSettings")); //$NON-NLS-1$
        
		wOnlyActive = new Button(this, SWT.CHECK);
		wOnlyActive.setText(Messages.getString("TransLog.Button.ShowOnlyActiveSteps")); //$NON-NLS-1$
        spoon.props.setLook(wOnlyActive);
        
		wSafeMode = new Button(this, SWT.CHECK);
		wSafeMode.setText(Messages.getString("TransLog.Button.SafeMode")); //$NON-NLS-1$
        spoon.props.setLook(wSafeMode);

		fdStart = new FormData();
        fdStart.left = new FormAttachment(0, 10);
		fdStart.bottom = new FormAttachment(100, 0);
		wStart.setLayoutData(fdStart);

		fdPause= new FormData();
        fdPause.left = new FormAttachment(wStart, 10);
		fdPause.bottom = new FormAttachment(100, 0);
		wPause.setLayoutData(fdPause);

        fdStop = new FormData();
        fdStop.left = new FormAttachment(wPause, 10);
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

        
		final Timer tim = new Timer("TransLog: " + getMeta().getName());
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
				spoon.executeTransformation(transMeta, true, false, false, false, false, null, wSafeMode.getSelection());
			}
		};

		lsPause = new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				pauseResume();
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
				spoon.executeTransformation(transMeta, true, false, false, true, false, null, wSafeMode.getSelection());
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
		wPause.addSelectionListener(lsPause);
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
                log.logMinimal(Spoon.APP_NAME, Messages.getString("TransLog.Log.TransformationHasFinished")); //$NON-NLS-1$
    
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
                    new ErrorDialog(shell, Messages.getString("TransLog.Dialog.ErrorWritingLogRecord.Title"), Messages.getString("TransLog.Dialog.ErrorWritingLogRecord.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
                }
                
                wStart.setEnabled(true);
                wPause.setEnabled(false);
                wStop.setEnabled(false);
                
                // OK, also see if we had a debugging session going on.
                // If so and we didn't hit a breakpoint yet, display the show preview dialog...
                //
                if (debug && lastTransDebugMeta!=null && lastTransDebugMeta.getTotalNumberOfHits()==0) {
                	debug=false;
                	showLastPreviewResults();
                }
            	debug=false;
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
					spoon.saveToFile(transMeta);
				}
				else
				{
					MessageDialogWithToggle md = new MessageDialogWithToggle(shell, Messages.getString("TransLog.Dialog.FileHasChanged.Title"), //$NON-NLS-1$
							null, Messages.getString("TransLog.Dialog.FileHasChanged1.Message") + Const.CR + Messages.getString("TransLog.Dialog.FileHasChanged2.Message") + Const.CR, //$NON-NLS-1$ //$NON-NLS-2$
							MessageDialog.QUESTION, new String[] { Messages.getString("System.Button.Yes"), Messages.getString("System.Button.No") }, //$NON-NLS-1$ //$NON-NLS-2$
							0, Messages.getString("TransLog.Dialog.Option.AutoSaveTransformation"), //$NON-NLS-1$
							spoon.props.getAutoSave());
					int answer = md.open();
					if ( (answer & 0xFF) == 0)
					{
						spoon.saveToFile(transMeta);
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

						transMeta.injectVariables(executionConfiguration.getVariables());

						trans = new Trans(transMeta, spoon.rep, transMeta.getName(), transMeta.getDirectory().getPath(), transMeta.getFilename());
						trans.setReplayDate(executionConfiguration.getReplayDate());
						trans.setMonitored(true);
						log.logBasic(toString(), Messages.getString("TransLog.Log.TransformationOpened")); //$NON-NLS-1$
					}
					catch (KettleException e)
					{
						trans = null;
						new ErrorDialog(shell, Messages.getString("TransLog.Dialog.ErrorOpeningTransformation.Title"), Messages.getString("TransLog.Dialog.ErrorOpeningTransformation.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
					}
					if (trans != null)
					{
						Map<String,String> arguments = executionConfiguration.getArguments();
                        final String args[];
						if (arguments != null) args = convertArguments(arguments); else args = null;
                        
						log.logMinimal(Spoon.APP_NAME, Messages.getString("TransLog.Log.LaunchingTransformation") + trans.getTransMeta().getName() + "]..."); //$NON-NLS-1$ //$NON-NLS-2$
						trans.setSafeModeEnabled(executionConfiguration.isSafeModeEnabled());
                        
                        // Launch the step preparation in a different thread. 
                        // That way Spoon doesn't block anymore and that way we can follow the progress of the initialization
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
                        
						log.logMinimal(Spoon.APP_NAME, Messages.getString("TransLog.Log.StartedExecutionOfTransformation")); //$NON-NLS-1$
						wStart.setEnabled(false);
						wPause.setEnabled(true);
                        wStop.setEnabled(true);
					}
				}
				else
				{
					MessageBox m = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
					m.setText(Messages.getString("TransLog.Dialog.DoNoStartTransformationTwice.Title")); //$NON-NLS-1$
					m.setMessage(Messages.getString("TransLog.Dialog.DoNoStartTransformationTwice.Message")); //$NON-NLS-1$
					m.open();
				}
			}
			else
			{
				if (transMeta.hasChanged())
				{
					MessageBox m = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
					m.setText(Messages.getString("TransLog.Dialog.SaveTransformationBeforeRunning.Title")); //$NON-NLS-1$
					m.setMessage(Messages.getString("TransLog.Dialog.SaveTransformationBeforeRunning.Message")); //$NON-NLS-1$
					m.open();
				}
				else if (spoon.rep != null && transMeta.getName() == null)
				{
					MessageBox m = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
					m.setText(Messages.getString("TransLog.Dialog.GiveTransformationANameBeforeRunning.Title")); //$NON-NLS-1$
					m.setMessage(Messages.getString("TransLog.Dialog.GiveTransformationANameBeforeRunning.Message")); //$NON-NLS-1$
					m.open();
				}
				else
				{
					MessageBox m = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
					m.setText(Messages.getString("TransLog.Dialog.SaveTransformationBeforeRunning2.Title")); //$NON-NLS-1$
					m.setMessage(Messages.getString("TransLog.Dialog.SaveTransformationBeforeRunning2.Message")); //$NON-NLS-1$
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
                log.logMinimal(Spoon.APP_NAME, Messages.getString("TransLog.Log.ProcessingOfTransformationStopped")); //$NON-NLS-1$
            }
            catch (KettleException e)
            {
                new ErrorDialog(shell, Messages.getString("TransLog.Dialog.ErrorWritingLogRecord.Title"), Messages.getString("TransLog.Dialog.ErrorWritingLogRecord.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
            }
            wStart.setEnabled(true);
            wPause.setEnabled(false);
            wStop.setEnabled(false);
            running = false;
            initialized = false;
            halted = false;
            halting = false;
            transMeta.setInternalKettleVariables(); // set the original vars back as they may be changed by a mapping
        }
    }
    
    public synchronized void pauseResume()
    {
        if (running)
        {
        	if (!pausing)
        	{
                pausing = true;
                trans.pauseRunning();

                wPause.setText(RESUME_TEXT);
                wStart.setEnabled(false);
                wPause.setEnabled(true);
                wStop.setEnabled(true);
        	}
        	else
        	{
                pausing = false;
                trans.resumeRunning();

                wPause.setText(PAUSE_TEXT);
                wStart.setEnabled(false);
                wPause.setEnabled(true);
                wStop.setEnabled(true);
        	}
        }
    }
    
	private synchronized void prepareTrans(final Thread parentThread, final String[] args)
    {
        Runnable runnable = new Runnable()
        {
            public void run()
            {
                try {
					trans.prepareExecution(args);
					initialized = true;
				} catch (KettleException e) {
					initialized = false;
				}
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

	private TransHistoryRefresher spoonHistoryRefresher;

	private TransDebugMeta lastTransDebugMeta;

	private void refreshView()
	{
		boolean insert = true;

  		if (wFields.isDisposed()) return;
		if (refresh_busy) return;

		refresh_busy = true;

		Table table = wFields.table;

		long time = new Date().getTime();
		long msSinceLastUpdate = time - lastUpdateView;
		if ( trans != null  &&  msSinceLastUpdate > UPDATE_TIME_VIEW )
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

					String fields[] = stepStatus.getTransLogFields();

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

		refresh_busy = false;
	}

	public synchronized void debug(TransExecutionConfiguration executionConfiguration, TransDebugMeta transDebugMeta)
	{
        if (!running)
        {
    		try
    		{
    			this.lastTransDebugMeta = transDebugMeta;
    			
                log.setLogLevel(executionConfiguration.getLogLevel());
    			log.logDetailed(toString(), Messages.getString("TransLog.Log.DoPreview")); //$NON-NLS-1$
                String[] args=null;
				Map<String,String> arguments = executionConfiguration.getArguments();
				if (arguments != null)
				{
					args = convertArguments(arguments);
                }
				transMeta.injectVariables(executionConfiguration.getVariables());

                // Create a new transformation to execution
                //
				trans = new Trans(transMeta);
                trans.setSafeModeEnabled(executionConfiguration.isSafeModeEnabled());
				trans.prepareExecution(args);
				
				// Add the row listeners to the allocated threads
				//
				transDebugMeta.addRowListenersToTransformation(trans);
				
				// What method should we call back when a break-point is hit?
				//
				transDebugMeta.addBreakPointListers(new BreakPointListener() {
						public void breakPointHit(TransDebugMeta transDebugMeta, StepDebugMeta stepDebugMeta, RowMetaInterface rowBufferMeta, List<Object[]> rowBuffer) {
							showPreview(transDebugMeta, stepDebugMeta, rowBufferMeta, rowBuffer);
						}
					}
				);

				// Start the threads for the steps...
				//
				trans.startThreads();

				running = !running;
    			debug=true;

                wStart.setEnabled(false);
                wPause.setEnabled(true);
                wStop.setEnabled(true);
    		}
    		catch (Exception e)
    		{
    			new ErrorDialog(shell, Messages.getString("TransLog.Dialog.UnexpectedErrorDuringPreview.Title"), Messages.getString("TransLog.Dialog.UnexpectedErrorDuringPreview.Message"), e); //$NON-NLS-1$ //$NON-NLS-2$
    		}
        }
        else
        {
            MessageBox m = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
            m.setText(Messages.getString("TransLog.Dialog.DoNoPreviewWhileRunning.Title")); //$NON-NLS-1$
            m.setMessage(Messages.getString("TransLog.Dialog.DoNoPreviewWhileRunning.Message")); //$NON-NLS-1$
            m.open();
        }
	}

	private String[] convertArguments(Map<String, String> arguments)
	{
		String[] argumentNames = arguments.keySet().toArray(new String[arguments.size()]);
		Arrays.sort(argumentNames);
		
		String args[] = new String[argumentNames.length];
		for (int i = 0; i < args.length; i++)
		{
			String argumentName = argumentNames[i];
			args[i] = arguments.get(argumentName);
		}
		return args;
	}

	public void showPreview(final TransDebugMeta transDebugMeta, final StepDebugMeta stepDebugMeta, final RowMetaInterface rowBufferMeta, final List<Object[]> rowBuffer)
	{
		display.asyncExec(new Runnable() {
		
			public void run() {
				
				if (isDisposed() || wPause.isDisposed()) return;
				
				// The transformation is now paused, indicate this in the log dialog...
				//
				pausing=true;
				wPause.setText(RESUME_TEXT);
				
				PreviewRowsDialog previewRowsDialog = new PreviewRowsDialog(shell, transMeta, SWT.APPLICATION_MODAL, stepDebugMeta.getStepMeta().getName(), rowBufferMeta, rowBuffer);
				previewRowsDialog.setProposingToGetMoreRows(true);
				previewRowsDialog.setProposingToStop(true);
				previewRowsDialog.open();

				if (previewRowsDialog.isAskingForMoreRows()) {
					// clear the row buffer.
					// That way if you click resume, you get the next N rows for the step :-)
					//
					rowBuffer.clear();

					// Resume running: find more rows...
					//
					pauseResume();
				}

				if (previewRowsDialog.isAskingToStop()) {
					// Stop running
					//
					stop();
				}

			}
		
		});
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
		ArrayList<String> err = new ArrayList<String>();

		int i = 0;
		int startpos = 0;
		int crlen = Const.CR.length();

		while (i < all.length() - crlen)
		{
			if (all.substring(i, i + crlen).equalsIgnoreCase(Const.CR))
			{
				String line = all.substring(startpos, i);
				String uLine = line.toUpperCase();
				if (uLine.indexOf(Messages.getString("TransLog.System.ERROR")) >= 0 || //$NON-NLS-1$
						uLine.indexOf(Messages.getString("TransLog.System.EXCEPTION")) >= 0 || //$NON-NLS-1$
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
		if (uLine.indexOf(Messages.getString("TransLog.System.ERROR2")) >= 0 || //$NON-NLS-1$
				uLine.indexOf(Messages.getString("TransLog.System.EXCEPTION2")) >= 0 || //$NON-NLS-1$
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
				err_lines[i] = err.get(i);

			EnterSelectionDialog esd = new EnterSelectionDialog(shell, err_lines, Messages.getString("TransLog.Dialog.ErrorLines.Title"), Messages.getString("TransLog.Dialog.ErrorLines.Message")); //$NON-NLS-1$ //$NON-NLS-2$
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

	public void setTransHistoryRefresher(TransHistoryRefresher spoonHistoryRefresher)
	{
		this.spoonHistoryRefresher = spoonHistoryRefresher;
	}

    public boolean isSafeModeChecked()
    {
        return wSafeMode.getSelection();
    }

    public EngineMetaInterface getMeta() {
    	return transMeta;
    }

    /**
     * @return the transMeta
     * /
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
        return !running;
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

	/**
	 * @return the lastTransDebugMeta
	 */
	public TransDebugMeta getLastTransDebugMeta() {
		return lastTransDebugMeta;
	}

	public synchronized void showLastPreviewResults() {
		if (lastTransDebugMeta==null || lastTransDebugMeta.getStepDebugMetaMap().isEmpty()) return;
		
		List<String> stepnames = new ArrayList<String>();
		List<RowMetaInterface> rowMetas = new ArrayList<RowMetaInterface>();
		List<List<Object[]>> rowBuffers = new ArrayList<List<Object[]>>();

		// Assemble the buffers etc in the old style...
		//
		for (StepMeta stepMeta : lastTransDebugMeta.getStepDebugMetaMap().keySet() ) {
			StepDebugMeta stepDebugMeta = lastTransDebugMeta.getStepDebugMetaMap().get(stepMeta);
			
			stepnames.add(stepMeta.getName());
			rowMetas.add(stepDebugMeta.getRowBufferMeta());
			rowBuffers.add(stepDebugMeta.getRowBuffer());
		}
		
		EnterPreviewRowsDialog dialog = new EnterPreviewRowsDialog(shell, SWT.NONE, stepnames, rowMetas, rowBuffers);
		dialog.open();
	}
}
