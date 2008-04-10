package org.pentaho.di.ui.spoon.job;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.delegates.SpoonDelegate;

public class JobHistoryDelegate extends SpoonDelegate {
	
	// private static final LogWriter log = LogWriter.getInstance();
	
	private JobGraph jobGraph;

	private CTabItem jobHistoryTab;
	
    private ColumnInfo[] colinf;	
	
	private Text   wText;
	private Button wRefresh, wReplay, wClear;
    private TableView wFields;
    
	private FormData fdText, fdSash, fdRefresh, fdReplay, fdClear; 

    private List<RowMetaAndData> rowList;

	private ValueMetaInterface durationMeta;
	private ValueMetaInterface replayDateMeta;

	/**
	 * @param spoon
	 * @param jobGraph
	 */
	public JobHistoryDelegate(Spoon spoon, JobGraph jobGraph) {
		super(spoon);
		this.jobGraph = jobGraph;
	}
	
	public void addJobHistory() {
		// First, see if we need to add the extra view...
		//
		if (jobGraph.extraViewComposite==null || jobGraph.extraViewComposite.isDisposed()) {
			jobGraph.addExtraView();
		} else {
			if (jobHistoryTab!=null && !jobHistoryTab.isDisposed()) {
				// just set this one active and get out...
				//
				jobGraph.extraViewTabFolder.setSelection(jobHistoryTab);
				return; 
			}
		}
		
		// Add a transLogTab : display the logging...
		//
		jobHistoryTab = new CTabItem(jobGraph.extraViewTabFolder, SWT.CLOSE | SWT.MAX);
		jobHistoryTab.setImage(GUIResource.getInstance().getImageShowHistory());
		jobHistoryTab.setText(Messages.getString("Spoon.TransGraph.HistoryTab.Name"));
		
		// Create a composite, slam everything on there like it was in the history tab.
		//
		final Composite historyComposite = new Composite(jobGraph.extraViewTabFolder, SWT.NONE);
		historyComposite.setLayout(new FormLayout());
		
        spoon.props.setLook(historyComposite);
		
		SashForm sash = new SashForm(historyComposite, SWT.VERTICAL);
		spoon.props.setLook(sash);
		
		sash.setLayout(new FillLayout());
		
		final int FieldsRows=1;
		colinf=new ColumnInfo[] {
            new ColumnInfo(Messages.getString("TransHistory.Column.Name"),           ColumnInfo.COLUMN_TYPE_TEXT, true , true), //$NON-NLS-1$
            new ColumnInfo(Messages.getString("TransHistory.Column.BatchID"),        ColumnInfo.COLUMN_TYPE_TEXT, true , true), //$NON-NLS-1$
    		new ColumnInfo(Messages.getString("TransHistory.Column.Status"),         ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
    		new ColumnInfo(Messages.getString("TransHistory.Column.Duration"),       ColumnInfo.COLUMN_TYPE_TEXT, true , true), //$NON-NLS-1$
            new ColumnInfo(Messages.getString("TransHistory.Column.Read"),           ColumnInfo.COLUMN_TYPE_TEXT, true , true), //$NON-NLS-1$
    		new ColumnInfo(Messages.getString("TransHistory.Column.Written"),        ColumnInfo.COLUMN_TYPE_TEXT, true , true), //$NON-NLS-1$
            new ColumnInfo(Messages.getString("TransHistory.Column.Updated"),        ColumnInfo.COLUMN_TYPE_TEXT, true , true), //$NON-NLS-1$
    		new ColumnInfo(Messages.getString("TransHistory.Column.Input"),          ColumnInfo.COLUMN_TYPE_TEXT, true , true), //$NON-NLS-1$
    		new ColumnInfo(Messages.getString("TransHistory.Column.Output"),         ColumnInfo.COLUMN_TYPE_TEXT, true , true), //$NON-NLS-1$
    		new ColumnInfo(Messages.getString("TransHistory.Column.Errors"),         ColumnInfo.COLUMN_TYPE_TEXT, true , true), //$NON-NLS-1$
            new ColumnInfo(Messages.getString("TransHistory.Column.StartDate"),      ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
            new ColumnInfo(Messages.getString("TransHistory.Column.EndDate"),        ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
    		new ColumnInfo(Messages.getString("TransHistory.Column.LogDate"),        ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
            new ColumnInfo(Messages.getString("TransHistory.Column.DependencyDate"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
            new ColumnInfo(Messages.getString("TransHistory.Column.ReplayDate"),     ColumnInfo.COLUMN_TYPE_TEXT, false, true) //$NON-NLS-1$
        };
		
        for (int i=3;i<10;i++) colinf[i].setAllignement(SWT.RIGHT);
        
        // Create the duration value meta data
        //
        durationMeta = new ValueMeta("DURATION", ValueMetaInterface.TYPE_NUMBER);
        durationMeta.setConversionMask("0");
        colinf[2].setValueMeta(durationMeta);
        
        wFields=new TableView(jobGraph.getManagedObject(), 
        		              sash, 
							  SWT.BORDER | SWT.FULL_SELECTION | SWT.SINGLE, 
							  colinf, 
							  FieldsRows,  
							  true, // readonly!
							  null,
							  spoon.props
							  );
		
		wText = new Text(sash, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY );
		spoon.props.setLook(wText);
		wText.setVisible(true);
        // wText.setText(Messages.getString("TransHistory.PleaseRefresh.Message"));
		
		wRefresh = new Button(historyComposite, SWT.PUSH);
		wRefresh.setText(Messages.getString("TransHistory.Button.Refresh")); //$NON-NLS-1$

		fdRefresh    = new FormData(); 
		fdRefresh.left   = new FormAttachment(15, 0);  
		fdRefresh.bottom = new FormAttachment(100, 0);
		wRefresh.setLayoutData(fdRefresh);
		
		wReplay = new Button(historyComposite, SWT.PUSH);
		wReplay.setText(Messages.getString("TransHistory.Button.Replay")); //$NON-NLS-1$

		fdReplay    = new FormData(); 
		fdReplay.left   = new FormAttachment(wRefresh, Const.MARGIN);  
		fdReplay.bottom = new FormAttachment(100, 0);
		wReplay.setLayoutData(fdReplay);

		wClear = new Button(historyComposite, SWT.PUSH);
		wClear.setText(Messages.getString("JobHistory.Button.Clear")); //$NON-NLS-1$

		fdClear    = new FormData(); 
		fdClear.left   = new FormAttachment(wReplay, Const.MARGIN);  
		fdClear.bottom = new FormAttachment(100, 0);
		wClear.setLayoutData(fdClear);

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
		fdSash.bottom = new FormAttachment(wRefresh, -5);
		sash.setLayoutData(fdSash);
		
		sash.setWeights(new int[] { 60, 40} );

		historyComposite.pack();
		
		setupReplayListener();
        
        SelectionAdapter lsRefresh = new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent e)
                {
                    refreshHistory();
                }
            };
		
		wRefresh.addSelectionListener(lsRefresh);
        
        wFields.table.addSelectionListener(new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent e)
                {
                    showLogEntry();
                }
            }
        );
        wFields.table.addKeyListener(new KeyListener()
            {
                public void keyReleased(KeyEvent e)
                {
                    showLogEntry();
                }
            
                public void keyPressed(KeyEvent e)
                {
                }
            
            }
        );

		
		jobHistoryTab.setControl(historyComposite);
		
		jobGraph.extraViewTabFolder.setSelection(jobHistoryTab);
		
		
		// Also add a listener to JobGraph to see if a transformation finished...
		//
		final RefreshListener jobRefreshListener = new RefreshListener() {
		
			public void refreshNeeded() {
				jobGraph.getDisplay().asyncExec(new Runnable() {
					
					public void run() {
						refreshHistory();
					}
				});

			}
		
		};
		jobGraph.addRefreshListener(jobRefreshListener);
		
		// Make sure to clean it up afterwards too.
		jobHistoryTab.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				jobGraph.getRefreshListeners().remove(jobRefreshListener);
			}
		});
		
		// Launch a refresh in the background...
		//
		jobGraph.getDisplay().asyncExec(new Runnable() {
			public void run() {
				refreshHistory();
				historyComposite.layout(true, true);
			}
		});
	}

    /**
     * User requested to clear the log table.<br>
     * Better ask confirmation
     */
    protected void clearLogTable() {
    	String logTable = jobGraph.getManagedObject().getLogTable();
    	DatabaseMeta databaseMeta = jobGraph.getManagedObject().getLogConnection();
    	
    	if (databaseMeta!=null && !Const.isEmpty(logTable)) {
    	
	    	MessageBox mb = new MessageBox(jobGraph.getShell(), SWT.YES | SWT.NO | SWT.ICON_QUESTION);
	        mb.setMessage(Messages.getString("JobGraph.Dialog.AreYouSureYouWantToRemoveAllLogEntries.Message", logTable)); // Nothing found that matches your criteria
			mb.setText(Messages.getString("JobGraph.Dialog.AreYouSureYouWantToRemoveAllLogEntries.Title")); // Sorry!
			if (mb.open()==SWT.YES) {
				Database database = new Database(databaseMeta);
				try {
					database.connect();
					database.truncateTable(logTable);
				}
				catch(Exception e) {
					new ErrorDialog(jobGraph.getShell(), Messages.getString("JobGraph.Dialog.ErrorClearningLoggingTable.Title"), 
							Messages.getString("JobGraph.Dialog.ErrorClearningLoggingTable.Message"), e);
				}
				finally
				{
					if (database!=null) {
						database.disconnect();
					}
					
					refreshHistory();
					wText.setText("");
				}
			}

    	}
	}
    
    public void showHistoryView() {
    	
    	// What button?
    	//
    	// XulToolbarButton showLogXulButton = toolbar.getButtonById("trans-show-log");
    	// ToolItem toolBarButton = (ToolItem) showLogXulButton.getNativeObject();
    	
    	if (jobHistoryTab==null || jobHistoryTab.isDisposed()) {
    		addJobHistory();
    	} else {
    		jobHistoryTab.dispose();
    		
    		jobGraph.checkEmptyExtraView();
    	}
    }
    
    
	private void setupReplayListener() {
		SelectionAdapter lsReplay = new SelectionAdapter()
        {
			public void widgetSelected(SelectionEvent e) {
				int idx = wFields.getSelectionIndex();
				if (idx >= 0) {
					String fields[] = wFields.getItem(idx);
					String dateString = fields[13];
					try {
						ValueMetaInterface stringValueMeta = replayDateMeta.clone();
						stringValueMeta.setType(ValueMetaInterface.TYPE_STRING);
						
						Date replayDate = stringValueMeta.getDate(dateString);
						
						spoon.executeJob(jobGraph.getManagedObject(), true, false, replayDate, false);
					} catch (KettleException e1) {
						new ErrorDialog(jobGraph.getShell(), 
								Messages.getString("TransHistory.Error.ReplayingTransformation2"), //$NON-NLS-1$
								Messages.getString("TransHistory.Error.InvalidReplayDate") + dateString, e1); //$NON-NLS-1$
					}
				}
			}
		};
	
        wReplay.addSelectionListener(lsReplay);
	}

    /**
     * Refreshes the history window in Spoon: reads entries from the specified log table in the Job Settings dialog.
     */
	public void refreshHistory()
	{
		JobMeta jobMeta = jobGraph.getManagedObject();
		
        // See if there is a job loaded that has a connection table specified.
        if (jobMeta!=null && !Const.isEmpty(jobMeta.getName()))
        {
            if (jobMeta.getLogConnection()!=null)
            {
                if (!Const.isEmpty(jobMeta.getLogTable()))
                {
                    Database database = null;
                    try
                    {
                        // open a connection
                        database = new Database(jobMeta.getLogConnection());
                        database.shareVariablesWith(jobMeta);
                        database.connect();
                        
                        RowMetaAndData params = new RowMetaAndData();
                        params.addValue(new ValueMeta("transname", ValueMetaInterface.TYPE_STRING), jobMeta.getName()); //$NON-NLS-1$
                        ResultSet resultSet = database.openQuery("SELECT * FROM "+jobMeta.getLogTable()+" WHERE JOBNAME = ? ORDER BY ID_JOB desc", params.getRowMeta(), params.getData()); //$NON-NLS-1$ //$NON-NLS-2$
                        
                        rowList = new ArrayList<RowMetaAndData>();
                        Object[] rowData = database.getRow(resultSet);
                        while (rowData!=null)
                        {
                            RowMetaInterface rowMeta = database.getReturnRowMeta();
                            rowList.add(new RowMetaAndData(rowMeta, rowData));
                            rowData = database.getRow(resultSet);
                        }
                        database.closeQuery(resultSet);

                        if (rowList.size()>0)
                        {
                            wFields.table.clearAll();
                            
                            // OK, now that we have a series of rows, we can add them to the table view...
                            for (int i=0;i<rowList.size();i++)
                            {
                                RowMetaAndData row = rowList.get(i);
                                TableItem item = new TableItem(wFields.table, SWT.NONE);
                                String batchID = row.getString("ID_JOB", "");
                                if(batchID != null)
                                item.setText( 1, batchID);           //$NON-NLS-1$ //$NON-NLS-2$
                                item.setText( 2, Const.NVL( row.getString("STATUS", ""), ""));           //$NON-NLS-1$ //$NON-NLS-2$
                                item.setText( 3, Const.NVL( row.getString("LINES_READ", ""), ""));           //$NON-NLS-1$ //$NON-NLS-2$
                                item.setText( 4, Const.NVL( row.getString("LINES_WRITTEN", ""), ""));           //$NON-NLS-1$ //$NON-NLS-2$
                                item.setText( 5, Const.NVL( row.getString("LINES_UPDATED", ""), ""));           //$NON-NLS-1$ //$NON-NLS-2$
                                item.setText( 6, Const.NVL( row.getString("LINES_INPUT", ""), ""));           //$NON-NLS-1$ //$NON-NLS-2$
                                item.setText( 7, Const.NVL( row.getString("LINES_OUTPUT", ""), ""));           //$NON-NLS-1$ //$NON-NLS-2$
                                item.setText( 8, Const.NVL( row.getString("ERRORS", ""), ""));           //$NON-NLS-1$ //$NON-NLS-2$
                                item.setText( 9, Const.NVL( row.getString("STARTDATE", ""), ""));           //$NON-NLS-1$ //$NON-NLS-2$
                                item.setText(10, Const.NVL( row.getString("ENDDATE", ""), ""));           //$NON-NLS-1$ //$NON-NLS-2$
                                item.setText(11, Const.NVL( row.getString("LOGDATE", ""), ""));           //$NON-NLS-1$ //$NON-NLS-2$
                                item.setText(12, Const.NVL( row.getString("DEPDATE", ""), ""));   //$NON-NLS-1$ //$NON-NLS-2$
                                String replayDate = row.getString("REPLAYDATE", ""); //$NON-NLS-1$ //$NON-NLS-2$
                                if(replayDate == null)
                                	replayDate = Const.NULL_STRING;
								item.setText(13, replayDate);
                            }
                            
                            wFields.removeEmptyRows();
                            wFields.setRowNums();
                            wFields.optWidth(true);
                            wFields.table.setSelection(0);
                            
                            showLogEntry();
                        }
                    }
                    catch(KettleException e)
                    {
                        new ErrorDialog(jobGraph.getShell(), Messages.getString("JobHistory.Error.GettingLoggingInfo"), Messages.getString("JobHistory.Error.GettingInfoFromLoggingTable"), e); //$NON-NLS-1$ //$NON-NLS-2$
                        wFields.clearAll(false);
                    }
                    finally
                    {
                        if (database!=null) database.disconnect();
                    }
                    
                }
                else
                {
                    wFields.clearAll(false);
                }
            }
            else
            {
                wFields.clearAll(false);
            }
        }
        else
        {
            wFields.clearAll(false);
        }
	}
    
	public void showLogEntry()
    {
        if (rowList==null) 
        {
            wText.setText(""); //$NON-NLS-1$
            return;
        }
        
        // grab the selected line in the table:
        int nr = wFields.table.getSelectionIndex();
        if (nr>=0 && rowList!=null && nr<rowList.size())
        {
            // OK, grab this one from the buffer...
            RowMetaAndData row = rowList.get(nr);
            String logging=null;
            try
            {
                logging = row.getString("LOG_FIELD", ""); //$NON-NLS-1$ //$NON-NLS-2$
            }
            catch (KettleValueException e)
            {
            	new ErrorDialog(jobGraph.getShell(), Messages.getString("JobHistory.Error.GettingLoggingInfo"), Messages.getString("JobHistory.Error.GettingLogFieldFromLoggingTable"), e); //$NON-NLS-1$ //$NON-NLS-2$
            } 
            if (logging!=null) 
            {
                wText.setText(logging);
            }
            else
            {
                wText.setText(""); //$NON-NLS-1$
            }
        }
    }


	
	/**
	 * @return the transHistoryTab
	 */
	public CTabItem getJobHistoryTab() {
		return jobHistoryTab;
	}

}
