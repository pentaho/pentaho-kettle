package org.pentaho.di.ui.spoon.trans;

import java.net.URL;
import java.util.Date;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepStatus;
import org.pentaho.di.trans.step.BaseStepData.StepExecutionStatus;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.XulHelper;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.XulMessages;
import org.pentaho.di.ui.spoon.delegates.SpoonDelegate;
import org.pentaho.xul.toolbar.XulToolbar;
import org.pentaho.xul.toolbar.XulToolbarButton;

public class TransGridDelegate extends SpoonDelegate {
	private static Class<?> PKG = Spoon.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private static final String XUL_FILE_TRANS_GRID_TOOLBAR = "ui/trans-grid-toolbar.xul";
	public static final String XUL_FILE_TRANS_GRID_TOOLBAR_PROPERTIES = "ui/trans-grid-toolbar.properties";

	public static final long REFRESH_TIME = 100L;
    public static final long UPDATE_TIME_VIEW = 1000L;
    

	private TransGraph transGraph;

	private CTabItem transGridTab;
	private TableView transGridView;
	
	private boolean refresh_busy;
	private long lastUpdateView;
	
	private XulToolbar       toolbar;
	private Composite transGridComposite;
	private boolean hideInactiveSteps;
	
	/**
	 * @param spoon
	 * @param transGraph
	 */
	public TransGridDelegate(Spoon spoon, TransGraph transGraph) {
		super(spoon);
		this.transGraph = transGraph;
		
		hideInactiveSteps = false;
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

		transGridTab = new CTabItem(transGraph.extraViewTabFolder, SWT.NONE);
		transGridTab.setImage(GUIResource.getInstance().getImageShowGrid());
		transGridTab.setText(BaseMessages.getString(PKG, "Spoon.TransGraph.GridTab.Name"));

		transGridComposite = new Composite(transGraph.extraViewTabFolder, SWT.NONE);
		transGridComposite.setLayout(new FormLayout());
		
		addToolBar();
		addToolBarListeners();
		
		ColumnInfo[] colinf = new ColumnInfo[] { 
                new ColumnInfo(BaseMessages.getString(PKG, "TransLog.Column.Stepname"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
				new ColumnInfo(BaseMessages.getString(PKG, "TransLog.Column.Copynr"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
				new ColumnInfo(BaseMessages.getString(PKG, "TransLog.Column.Read"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
				new ColumnInfo(BaseMessages.getString(PKG, "TransLog.Column.Written"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
				new ColumnInfo(BaseMessages.getString(PKG, "TransLog.Column.Input"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
				new ColumnInfo(BaseMessages.getString(PKG, "TransLog.Column.Output"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
				new ColumnInfo(BaseMessages.getString(PKG, "TransLog.Column.Updated"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
                new ColumnInfo(BaseMessages.getString(PKG, "TransLog.Column.Rejected"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
				new ColumnInfo(BaseMessages.getString(PKG, "TransLog.Column.Errors"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
				new ColumnInfo(BaseMessages.getString(PKG, "TransLog.Column.Active"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
				new ColumnInfo(BaseMessages.getString(PKG, "TransLog.Column.Time"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
				new ColumnInfo(BaseMessages.getString(PKG, "TransLog.Column.Speed"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
				new ColumnInfo(BaseMessages.getString(PKG, "TransLog.Column.PriorityBufferSizes"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
		};

		colinf[1].setAllignement(SWT.RIGHT);
		colinf[2].setAllignement(SWT.RIGHT);
		colinf[3].setAllignement(SWT.RIGHT);
		colinf[4].setAllignement(SWT.RIGHT);
		colinf[5].setAllignement(SWT.RIGHT);
		colinf[6].setAllignement(SWT.RIGHT);
		colinf[7].setAllignement(SWT.RIGHT);
		colinf[8].setAllignement(SWT.RIGHT);
		colinf[9].setAllignement(SWT.LEFT);
		colinf[10].setAllignement(SWT.RIGHT);
		colinf[11].setAllignement(SWT.RIGHT);
        colinf[12].setAllignement(SWT.RIGHT);

		transGridView = new TableView(transGraph.getManagedObject(), transGridComposite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf, 1, true, // readonly!
				null, // Listener
				spoon.props);
		FormData fdView = new FormData();
		fdView.left = new FormAttachment(0,0);
		fdView.right = new FormAttachment(100,0);
		fdView.top = new FormAttachment((Control)toolbar.getNativeObject(),0);
		fdView.bottom = new FormAttachment(100,0);
		transGridView.setLayoutData(fdView);
		
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
		
		transGridTab.setControl(transGridComposite);

		transGraph.extraViewTabFolder.setSelection(transGridTab);		
	}

    private void addToolBar()
	{

		try {
			toolbar = XulHelper.createToolbar(XUL_FILE_TRANS_GRID_TOOLBAR, transGridComposite, TransGridDelegate.this, new XulMessages());
			
			
			// set the selected icon for the show inactive button.
			// This is not a XUL standard apparently
			//
			XulToolbarButton onlyActiveButton = toolbar.getButtonById("show-inactive");
			if (onlyActiveButton!=null) {
				onlyActiveButton.setSelectedImage(GUIResource.getInstance().getImageHideInactive());
			}
		
			// Add a few default key listeners
			//
			ToolBar toolBar = (ToolBar) toolbar.getNativeObject();
			toolBar.addKeyListener(spoon.defKeys);
			
			addToolBarListeners();
	        toolBar.layout(true, true);
		} catch (Throwable t ) {
			log.logError(Const.getStackTracker(t));
			new ErrorDialog(transGridComposite.getShell(), BaseMessages.getString(PKG, "Spoon.Exception.ErrorReadingXULFile.Title"), BaseMessages.getString(PKG, "Spoon.Exception.ErrorReadingXULFile.Message", XUL_FILE_TRANS_GRID_TOOLBAR), new Exception(t));
		}
	}

	public void addToolBarListeners()
	{
		try
		{
			// first get the XML document
			URL url = XulHelper.getAndValidate(XUL_FILE_TRANS_GRID_TOOLBAR_PROPERTIES);
			Properties props = new Properties();
			props.load(url.openStream());
			String ids[] = toolbar.getMenuItemIds();
			for (int i = 0; i < ids.length; i++)
			{
				String methodName = (String) props.get(ids[i]);
				if (methodName != null)
				{
					toolbar.addMenuListener(ids[i], this, methodName);

				}
			}

		} catch (Throwable t ) {
			t.printStackTrace();
			new ErrorDialog(transGridComposite.getShell(), BaseMessages.getString(PKG, "Spoon.Exception.ErrorReadingXULFile.Title"), 
					BaseMessages.getString(PKG, "Spoon.Exception.ErrorReadingXULFile.Message", XUL_FILE_TRANS_GRID_TOOLBAR_PROPERTIES), new Exception(t));
		}
	}

	public void showHideInactive() {
		hideInactiveSteps=!hideInactiveSteps;
		
		// TODO: change icon
		XulToolbarButton onlyActiveButton = toolbar.getButtonById("show-inactive");
		if (onlyActiveButton!=null) {
			onlyActiveButton.setSelection(hideInactiveSteps);
			if (hideInactiveSteps) {
				onlyActiveButton.setImage(GUIResource.getInstance().getImageHideInactive());
			} else {
				onlyActiveButton.setImage(GUIResource.getInstance().getImageShowInactive());
			}
		}
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
			if (hideInactiveSteps) nrSteps = transGraph.trans.nrActiveSteps();
			
			int sortColumn = transGridView.getSortField();
			boolean sortDescending = transGridView.isSortingDescending();

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
				StepInterface baseStep = transGraph.trans.getRunThread(i);
				//when "Hide active" steps is enabled show only alive steps
				//otherwise only those that have not STATUS_EMPTY
				if ( (hideInactiveSteps && baseStep.isRunning() ) || 
				 		( !hideInactiveSteps && baseStep.getStatus()!=StepExecutionStatus.STATUS_EMPTY) ) 
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
						if(i%2==0)
							ti.setBackground(GUIResource.getInstance().getColorWhite());
						else
							ti.setBackground(GUIResource.getInstance().getColorBlueCustomGrid());
					}

					nr++;
				}
			}
			transGridView.setRowNums();
			transGridView.optWidth(true);
			// Only need to resort if the output has been sorted differently to the default
			if (sortColumn != 0 || !sortDescending) {
				transGridView.sortTable(sortColumn, sortDescending);
			}
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
