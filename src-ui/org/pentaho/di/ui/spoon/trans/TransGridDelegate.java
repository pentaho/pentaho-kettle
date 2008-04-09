package org.pentaho.di.ui.spoon.trans;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepStatus;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.spoon.Messages;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.delegates.SpoonDelegate;

public class TransGridDelegate extends SpoonDelegate {
	
	// private static final LogWriter log = LogWriter.getInstance();
	
	public static final long REFRESH_TIME = 100L;
    public static final long UPDATE_TIME_VIEW = 1000L;
    

	private TransGraph transGraph;

	private CTabItem transGridTab;
	private TableView transGridView;
	
	private boolean refresh_busy;
	private long lastUpdateView;
	
	/**
	 * @param spoon
	 * @param transGraph
	 */
	public TransGridDelegate(Spoon spoon, TransGraph transGraph) {
		super(spoon);
		this.transGraph = transGraph;
	}
	
	
    public void showGridView() {
    	
    	if (transGridTab==null || transGridTab.isDisposed()) {
    		addTransGrid();
    	} else {
    		transGridTab.dispose();
    		
    		transGraph.checkEmptyExtraView();
    	}
    }
    
	/**
	 *  Add a grid with the execution metrics per step in a table view
	 *  
	 */ 
	public void addTransGrid() {

		// First, see if we need to add the extra view...
		//
		if (transGraph.extraViewComposite==null || transGraph.extraViewComposite.isDisposed()) {
			transGraph.addExtraView();
		} else {
			if (transGridTab!=null && !transGridTab.isDisposed()) {
				// just set this one active and get out...
				//
				transGraph.extraViewTabFolder.setSelection(transGridTab);
				return; 
			}
		}

		transGridTab = new CTabItem(transGraph.extraViewTabFolder, SWT.CLOSE | SWT.MAX);
		transGridTab.setImage(GUIResource.getInstance().getImageShowGrid());
		transGridTab.setText(Messages.getString("Spoon.TransGraph.GridTab.Name"));
		// wFields = new Text(extraViewTabFolder, SWT.READ_ONLY | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		
		ColumnInfo[] colinf = new ColumnInfo[] { 
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

		transGridView = new TableView(transGraph.getManagedObject(), transGraph.extraViewTabFolder, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf, 1, true, // readonly!
				null, // Listener
				spoon.props);
		
		// Add a timer to update this view every couple of seconds...
		//
		final Timer tim = new Timer("TransGraph: " + transGraph.getMeta().getName());
        final AtomicBoolean busy = new AtomicBoolean(false);

        TimerTask timtask = new TimerTask()
        {
            public void run()
            {
                if (!spoon.getDisplay().isDisposed())
                {
                    spoon.getDisplay().asyncExec(
                        new Runnable()
                        {
                            public void run()
                            {
                                if (!busy.get())
                                {
                                    busy.set(true);
                                    refreshView();
                                    busy.set(false);
                                }
                            }
                        }
                    );
                }
            }
        };

        tim.schedule(timtask, 0L, REFRESH_TIME); // schedule to repeat a couple of times per second to get fast feedback 

        transGridTab.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent disposeEvent) {
				tim.cancel();
			}
		});
		
		transGridTab.setControl(transGridView);

		transGraph.extraViewTabFolder.setSelection(transGridTab);		
	}

	
	private void refreshView()
	{
		boolean insert = true;

  		if (transGridView==null || transGridView.isDisposed()) return;
		if (refresh_busy) return;

		refresh_busy = true;

		Table table = transGridView.table;

		long time = new Date().getTime();
		long msSinceLastUpdate = time - lastUpdateView;
		if ( transGraph.trans != null  &&  msSinceLastUpdate > UPDATE_TIME_VIEW )
		{
            lastUpdateView = time;
			int nrSteps = transGraph.trans.nrSteps();
			// if (wOnlyActive.getSelection()) nrSteps = trans.nrActiveSteps(); TODO: re-add this check button

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
			for (int i = 0; i < transGraph.trans.nrSteps(); i++)
			{
				BaseStep baseStep = transGraph.trans.getRunThread(i);
				//when "Hide active" steps is enabled show only alive steps
				//otherwise only those that have not STATUS_EMPTY
				// if ( (wOnlyActive.getSelection() && baseStep.isAlive() ) || 
				// 		( !wOnlyActive.getSelection() && baseStep.getStatus()!=StepDataInterface.STATUS_EMPTY) )  TODO: re-add check box
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
			transGridView.setRowNums();
			transGridView.optWidth(true);
		}
		else
		{
			// We need at least one table-item in a table!
			if (table.getItemCount() == 0) new TableItem(table, SWT.NONE);
		}

		refresh_busy = false;
	}


	public CTabItem getTransGridTab() {
		return transGridTab;
	}

	

}
