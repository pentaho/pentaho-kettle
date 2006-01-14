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
import java.io.FileInputStream;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import be.ibridge.kettle.core.ColumnInfo;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.GUIResource;
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
import be.ibridge.kettle.trans.step.StepMeta;



/**
 * SpoonLog handles the display of the logging information in the Spoon logging window.
 *  
 * @see be.ibridge.kettle.spoon.Spoon
 * @author Matt
 * @since  17-mei-2003
 */
public class SpoonLog extends Composite
{
    public static final long UPDATE_TIME_VIEW  = 1000L;
    public static final long UPDATE_TIME_LOG   = 2000L;
    public static final long REFRESH_TIME      =  100L;
    
	public final static String START_TEXT = " &Start transformation "; 
	public final static String STOP_TEXT  = " &Stop transformation "; 

	private Shell shell;
	private Display display;
	private LogWriter log;

	private ColumnInfo[] colinf;	
	private TableView    wFields;
	
	private Label  wlOnlyActive;
	private Button wOnlyActive;

	private Text   wText;
	private Button wStart;	
	private Button wPreview;	
	private Button wError;
	private Button wClear;
	private Button wLog;
    
    private long   lastUpdateView;
    private long   lastUpdateLog;

	private FormData fdText, fdSash, fdStart, fdPreview, fdError, fdClear, fdLog, fdlOnlyActive, fdOnlyActive; 
	
	private boolean running, preview;
	public  boolean preview_shown = false;
		
	private SelectionListener lsStart, lsPreview, lsError, lsClear, lsLog;
	private StringBuffer message;

	private FileInputStream in;
	private Trans trans;
	private Spoon spoon;
	
	public SpoonLog(Composite parent, int style, Spoon sp, LogWriter l, String fname)
	{
		super(parent, style);
		shell=parent.getShell();
		spoon = sp;
		log=l;
		trans=null;
		display=shell.getDisplay();
		
		running = false;
		preview = false;
        lastUpdateView = 0L;
        lastUpdateLog  = 0L;
		
		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;
		
		setLayout(formLayout);
		
		setVisible(true);
        spoon.props.setLook(this);
		
		SashForm sash = new SashForm(this, SWT.VERTICAL);
		spoon.props.setLook(sash);
		
		sash.setLayout(new FillLayout());

		final int FieldsCols=12;
		final int FieldsRows=1;
		
		colinf=new ColumnInfo[FieldsCols];
		colinf[ 0]=new ColumnInfo("Stepname",       ColumnInfo.COLUMN_TYPE_TEXT, false, true);
		colinf[ 1]=new ColumnInfo("Copynr",         ColumnInfo.COLUMN_TYPE_TEXT, false, true);
		colinf[ 2]=new ColumnInfo("Read",           ColumnInfo.COLUMN_TYPE_TEXT, false, true);
		colinf[ 3]=new ColumnInfo("Written",        ColumnInfo.COLUMN_TYPE_TEXT, false, true);
		colinf[ 4]=new ColumnInfo("Input",          ColumnInfo.COLUMN_TYPE_TEXT, false, true);
		colinf[ 5]=new ColumnInfo("Output",         ColumnInfo.COLUMN_TYPE_TEXT, false, true);
		colinf[ 6]=new ColumnInfo("Updated",        ColumnInfo.COLUMN_TYPE_TEXT, false, true);
		colinf[ 7]=new ColumnInfo("Errors",         ColumnInfo.COLUMN_TYPE_TEXT, false, true);
		colinf[ 8]=new ColumnInfo("Active",         ColumnInfo.COLUMN_TYPE_TEXT, false, true);
		colinf[ 9]=new ColumnInfo("Time",           ColumnInfo.COLUMN_TYPE_TEXT, false, true);
		colinf[10]=new ColumnInfo("Speed (r/s)",    ColumnInfo.COLUMN_TYPE_TEXT, false, true);
		colinf[11]=new ColumnInfo("Pri/in/out",     ColumnInfo.COLUMN_TYPE_TEXT, false, true);
		
		colinf[ 1].setAllignement(SWT.RIGHT);
		colinf[ 2].setAllignement(SWT.RIGHT);
		colinf[ 3].setAllignement(SWT.RIGHT);
		colinf[ 4].setAllignement(SWT.RIGHT);
		colinf[ 5].setAllignement(SWT.RIGHT);
		colinf[ 6].setAllignement(SWT.RIGHT);
		colinf[ 7].setAllignement(SWT.RIGHT);
		colinf[ 8].setAllignement(SWT.RIGHT);
		colinf[ 9].setAllignement(SWT.RIGHT);
		colinf[10].setAllignement(SWT.RIGHT);
		colinf[11].setAllignement(SWT.RIGHT);

		wFields=new TableView(sash, 
							  SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, 
							  colinf, 
							  FieldsRows,  
							  true, // readonly!
							  null,
							  spoon.props
							  );
		
		/*
		fdFields=new FormData();
		fdFields.left   = new FormAttachment(0, 0);
		fdFields.top    = new FormAttachment(wOnlyActive, 0);
		fdFields.right  = new FormAttachment(100, 0);
		fdFields.bottom = new FormAttachment(100, 0);
		fdFields.left   = new FormAttachment(0, 0);
		wFields.setLayoutData(fdFields);
		*/
		
		wText = new Text(sash, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY );
		spoon.props.setLook(wText);
		wText.setVisible(true);
		
		wStart = new Button(this, SWT.PUSH);
		wStart.setText(START_TEXT);
		wPreview = new Button(this, SWT.PUSH);
		wPreview.setText(" &Preview ");
		wError = new Button(this, SWT.PUSH);
		wError.setText(" &Show error lines ");
		wClear = new Button(this, SWT.PUSH);
		wClear.setText(" &Clear log ");
		wLog = new Button(this, SWT.PUSH);
		wLog.setText(" &Log settings ");
		wlOnlyActive=new Label(this, SWT.RIGHT);
		wlOnlyActive.setText("Only show active steps");
		wOnlyActive=new Button(this, SWT.CHECK);

		fdStart    = new FormData(); 
		fdPreview  = new FormData(); 
		fdError    = new FormData(); 
		fdClear    = new FormData(); 
		fdLog      = new FormData(); 

		fdStart.left   = new FormAttachment(15, 0);  
		fdStart.bottom = new FormAttachment(100, 0);
		wStart.setLayoutData(fdStart);

		fdPreview.left   = new FormAttachment(wStart, 10);  
		fdPreview.bottom = new FormAttachment(100, 0);
		wPreview.setLayoutData(fdPreview);

		fdError.left   = new FormAttachment(wPreview, 10);  
		fdError.bottom = new FormAttachment(100, 0);
		wError.setLayoutData(fdError);

		fdClear.left   = new FormAttachment(wError, 10);  
		fdClear.bottom = new FormAttachment(100, 0);
		wClear.setLayoutData(fdClear);

		fdLog.left   = new FormAttachment(wClear, 10);  
		fdLog.bottom = new FormAttachment(100, 0);
		wLog.setLayoutData(fdLog);

        spoon.props.setLook(wlOnlyActive);
		fdlOnlyActive=new FormData();
		fdlOnlyActive.left  = new FormAttachment(wLog, 10);
		fdlOnlyActive.top   = new FormAttachment(wLog, 0, SWT.CENTER);
		wlOnlyActive.setLayoutData(fdlOnlyActive);

        spoon.props.setLook(wOnlyActive);
		fdOnlyActive=new FormData();
		fdOnlyActive.left  = new FormAttachment(wlOnlyActive, Const.MARGIN);
		fdOnlyActive.top   = new FormAttachment(wLog, 0, SWT.CENTER);
		wOnlyActive.setLayoutData(fdOnlyActive);
		wOnlyActive.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				spoon.props.setOnlyActiveSteps( wOnlyActive.getSelection() );
			}
		});
		wOnlyActive.setSelection( spoon.props.getOnlyActiveSteps() );

		// Put text in the middle
		fdText=new FormData();
		fdText.left   = new FormAttachment(0, 0);
		fdText.top    = new FormAttachment(0, 0);
		fdText.right  = new FormAttachment(100, 0);
		fdText.bottom = new FormAttachment(100, 0);
		wText.setLayoutData(fdText);

		
		fdSash     = new FormData(); 
		fdSash.left   = new FormAttachment(0, 0);  // First one in the left top corner
		fdSash.top    = new FormAttachment(0, 0);
		fdSash.right  = new FormAttachment(100, 0);
		fdSash.bottom = new FormAttachment(wStart, -5);
		sash.setLayoutData(fdSash);
		
		// sash.setWeights(new int[] { 60, 40} );

		pack();
		
		try
		{
			in = log.getFileInputStream();
		}
		catch(Exception e)
		{
			log.logError(Spoon.APP_NAME, "Couldn't create input-pipe connection to output-pipe!");
		}
		
		lsError = new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e) 
			{
				showErrors();
			}
		};
		
		final Timer tim = new Timer();
		TimerTask timtask = 
			new TimerTask() 
			{
				public void run() 
				{
					if (display!=null && !display.isDisposed())
					display.asyncExec(
						new Runnable() 
						{
							public void run() 
							{
								checkErrors();
								readLog(); 
								refreshView(); 
							}
						}
					);
				}
			};
			
		tim.schedule( timtask, 0L, REFRESH_TIME);// refresh every 2 seconds... 
		
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

		addDisposeListener(
			new DisposeListener() 
			{
				public void widgetDisposed(DisposeEvent e) 
				{
					tim.cancel();
				}
			}
		);
	}
	
	public void startstop()
	{
		if (!running) // Not running, start the transformation...
		{
			// Auto save feature...
			if (spoon.transMeta.hasChanged())
			{
				if (spoon.props.getAutoSave()) 
				{
					spoon.saveFile();
				}
				else
				{
					MessageDialogWithToggle md = new MessageDialogWithToggle(shell, 
																			 "File has changed!", 
																			 null,
																			 "You need to save your transformation before you can run it."+Const.CR+"Do you want to save the transformation now?"+Const.CR,
																			 MessageDialog.QUESTION,
																			 new String[] { "Yes", "No" },
																			 0,
																			 "Automatically save the transformation.",
																			 spoon.props.getAutoSave()
																			 );
					int answer = md.open();
					if (answer == 0)
					{
						spoon.saveFile();
					}
					spoon.props.setAutoSave(md.getToggleState());
				}
			}
			
            if ( ((spoon.transMeta.getName()!=null && spoon.rep!=null) ||     // Repository available & name set
			      (spoon.transMeta.getFilename()!=null && spoon.rep==null )   // No repository & filename set
			      ) && !spoon.transMeta.hasChanged()                              // Didn't change
			   )
			{
				if (trans==null || (trans!=null && trans.isFinished()) )
				{
					try
					{
						trans = new Trans(log, spoon.transMeta.getFilename(), spoon.transMeta.getName(), new String[] { spoon.transMeta.getFilename()} );
						trans.open(spoon.rep, spoon.transMeta.getName(), spoon.transMeta.getDirectory().getPath(), spoon.transMeta.getFilename());
						trans.setMonitored(true);
						log.logBasic(toString(), "Transformation opened.");
					}
					catch(KettleException e)
					{
						trans=null;
						new ErrorDialog(shell, spoon.props, "Error opening transformation", "Transformation failed to open: ", e);
					}
					readLog();
					if (trans!=null)
					{
                        Row arguments = getArguments(trans.getTransMeta());
                        if (arguments!=null)
                        {
                            String args[] = convertArguments(arguments);
    					    log.logBasic(Spoon.APP_NAME, "Excecuting transformation ["+trans.getTransMeta().getName()+"]...");
    						trans.execute(args);
                            log.logBasic(Spoon.APP_NAME, "Started.");
    						running=!running;
    						wStart.setText(STOP_TEXT);
    						readLog();
                        }
					}
				}
				else
				{
					MessageBox m = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
					m.setText("Warning!");
					m.setMessage("The transformation is running, don't start it twice!");	
					m.open();
				}
			}
			else
			{
				if (spoon.transMeta.hasChanged())
				{
					MessageBox m = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
					m.setText("File has changed!");
					m.setMessage("Please save your transformation first!");	
					m.open();
				}
				else
				if (spoon.rep!=null && spoon.transMeta.getName()==null)
				{
					MessageBox m = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
					m.setText("Transformation has no name!");
					m.setMessage("Please give your transformation a name to identify it by!");	
					m.open();
				}
				else
				{
					MessageBox m = new MessageBox(shell, SWT.OK | SWT.ICON_WARNING);
					m.setText("No filename!");
					m.setMessage("Before running, please save your transformation first!");	
					m.open();
				}
			}
		} 
		else
		{
			trans.stopAll();
			try
			{
				trans.endProcessing("stop");
			}
			catch(KettleException e)
			{
				new ErrorDialog(shell, spoon.props, "Error writing log record", "Unable to write log record to the logging table", e); 
			}
			wStart.setText(START_TEXT);
			running=!running;
			if (preview)
			{
				preview=false;
				showPreview();
			}
		}
	}
    
    public Row getArguments(TransMeta transMeta)
    {
        // OK, see if we need to ask for some arguments first...
        //
        Row arguments = transMeta.getUsedArguments(spoon.getArguments());
        if (arguments.size()>0)
        {
            EnterStringsDialog esd = new EnterStringsDialog(shell, SWT.NONE, arguments);
            if (esd.open()==null)
            {
                arguments=null;
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
		if (trans!=null)
		{
			if (!trans.isFinished())
			{
				if (trans.getErrors()!=0)
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
        if (msSinceLastUpdate<UPDATE_TIME_LOG)
        {
            return;
        }
        lastUpdateLog = time;

		if (message==null) message = new StringBuffer(); else message.setLength(0);				
		try
		{	
			int n = in.available();
			if (n>0)
			{
				byte buffer[] = new byte[n];
				int c = in.read(buffer, 0, n);
				for (int i=0;i<c;i++) message.append((char)buffer[i]);
			}
						
		}
		catch(Exception ex)
		{
			message.append(ex.toString());
		}

		if (!wText.isDisposed() && message.length()>0) 
		{
			String mess = wText.getText();
			wText.setSelection(mess.length());
			wText.clearSelection();
			wText.insert(message.toString());
			mess = wText.getText();
		}
        
        
	}
	
	private boolean refresh_busy;
	
	private void refreshView()
	{
		boolean insert = true;
		TableItem ti;
		float lapsed;
		
		if (wFields.isDisposed()) return;
		if (refresh_busy) return;
		
		refresh_busy = true;
		
		Table table = wFields.table;
        
        boolean doPreview = trans!=null && trans.previewComplete() && preview;
        
        long time = new Date().getTime(); 
        long msSinceLastUpdate = time - lastUpdateView;
        if ( ( trans!=null && msSinceLastUpdate>UPDATE_TIME_VIEW ) || doPreview)
		{
            lastUpdateView = time;
			int nrSteps = trans.nrSteps();
			if (wOnlyActive.getSelection()) nrSteps = trans.nrActiveSteps();
			
			if (table.getItemCount() != nrSteps) table.removeAll(); else insert=false;
				
			if (nrSteps==0) 
			{
				if (table.getItemCount()==0) ti = new TableItem(table, SWT.NONE);
			} 
			
			int nr = 0;
			for (int i=0;i<trans.nrSteps();i++)
			{
				BaseStep rt=trans.getRunThread(i);
				if (rt.isAlive() || !wOnlyActive.getSelection())
				{
					if (insert)  ti = new TableItem(table, SWT.NONE);
					else ti = table.getItem(nr);
					
					// Proc: nr of lines processed: input + output!
					long in_proc = rt.linesInput+rt.linesRead;
					long out_proc = rt.linesOutput+rt.linesWritten+rt.linesUpdated;
					
					lapsed = ((float)rt.getRuntime())/1000;
					double in_speed =0;
					double out_speed = 0;
					
					if (lapsed!=0)
					{
						in_speed = Math.floor(10*(in_proc / lapsed))/10;
						out_speed = Math.floor(10*(out_proc / lapsed))/10;
					}
					
					String fields[] = new String[colinf.length+1];
					fields[1] =    rt.getStepname();
					fields[2] = ""+rt.getCopy();
					fields[3] = ""+rt.linesRead;
					fields[4] = ""+rt.linesWritten;
					fields[5] = ""+rt.linesInput;
					fields[6] = ""+rt.linesOutput;
					fields[7] = ""+rt.linesUpdated;
					fields[8] = ""+rt.getErrors();
					fields[9] = ""+rt.getStatus();
					fields[10] = ""+Math.floor((lapsed*10) + 0.5)/10;
					fields[11] = lapsed==0?"-":""+( in_speed>out_speed?in_speed:out_speed );
					fields[12] = rt.isAlive()?""+rt.getPriority()+"/"+rt.rowsetInputSize()+"/"+rt.rowsetOutputSize():"-";
					
					// Anti-flicker: if nothing has changed, don't change it on the screen!
					for (int f=1;f<fields.length;f++)
					{
						if (!fields[f].equalsIgnoreCase(ti.getText(f)))
						{
							ti.setText(f, fields[f]);
						}
					}
					
					// Error lines should appear in red:
					if (rt.getErrors()>0)
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
			if (table.getItemCount()==0) ti = new TableItem(table, SWT.NONE);
		}
		
		if ( doPreview )
		{
			// System.out.println("preview is complete, show preview dialog!");
			trans.stopAll();
			showPreview();
		}
		
		if ( trans!=null && trans.isFinished() && running )
		{
		    log.logBasic(Spoon.APP_NAME, "The transformation has finished!!");
		    
			wStart.setText(START_TEXT);
			running=false;
			try
			{
				trans.endProcessing("end");
			}
			catch(KettleException e)
			{
				new ErrorDialog(shell, spoon.props, "Error writing log record", "Unable to write log record to the logging table", e); 
			}
		}
		
		refresh_busy = false;
	}
	
	public void preview()
	{
		log.logDetailed(toString(), "PREVIEW!!!");
		PreviewSelectDialog psd = new PreviewSelectDialog(shell, SWT.NONE, log, spoon.props, spoon.transMeta);
		psd.open();
		if (psd.previewSteps!=null)
		{
            Row arguments = getArguments(spoon.transMeta);
            if (arguments!=null)
            {
                String args[] = convertArguments(arguments);
                
    			spoon.tabfolder.setSelection(1);
    			trans=new Trans(log, spoon.transMeta, psd.previewSteps, psd.previewSizes);
    			trans.execute(args);
    			preview=true;
    			readLog();
    			running=!running;
    			wStart.setText(STOP_TEXT);
            }
		}
	}

	private String[] convertArguments(Row arguments)
    {
        String args[] = new String[10];
        for (int i=0;i<args.length;i++)
        {
            for (int v = 0; v < arguments.size() ; v++ )
            {
                Value value = arguments.getValue(v);
                if (value.getName().equalsIgnoreCase("Argument "+(i+1)))
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
		if (trans==null || !trans.isFinished()) return;
		
		// Drop out of preview mode!
		preview=false;

		BaseStep rt;
		int i;
		
		ArrayList buffers=new ArrayList();
		ArrayList names  =new ArrayList();
		for (i=0;i<trans.nrSteps();i++)
		{
			rt = trans.getRunThread(i);
			if (rt.previewSize>0)
			{
				buffers.add(rt.previewBuffer);
				names.add(rt.getStepname());
				log.logBasic(toString(), "Step: "+rt.getStepname()+" --> "+rt.previewBuffer.size()+" rows");
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
		wText.setText("");
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
		
		while (i<all.length()-crlen)
		{
			if (all.substring(i, i+crlen).equalsIgnoreCase(Const.CR))
			{
				String line = all.substring(startpos, i);
				if (line.toUpperCase().indexOf("ERROR")>=0 ||
				    line.toUpperCase().indexOf("EXCEPTION")>=0
				    ) 
				{
					err.add(line);
				}
				// New start of line
				startpos=i+crlen;
			}
			
			i++;
		}
		String line = all.substring(startpos);
		if (line.toUpperCase().indexOf("ERROR")>=0 ||
		    line.toUpperCase().indexOf("EXCEPTION")>=0
		    ) 
		{
			err.add(line);
		}
		
		if (err.size()>0)
		{
			String err_lines[] = new String[err.size()];
			for (i=0;i<err_lines.length;i++) err_lines[i] = (String)err.get(i);
			
			EnterSelectionDialog esd = new EnterSelectionDialog(shell, spoon.props, err_lines, "Error lines", "Select the step to edit:");
			line = esd.open();
			if (line!=null)
			{
				for (i=0;i<spoon.transMeta.nrSteps();i++)
				{
					StepMeta stepMeta = spoon.transMeta.getStep(i);
					if (line.indexOf( stepMeta.getName() ) >=0 )
					{
						spoon.editStep( stepMeta.getName() );
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

}
