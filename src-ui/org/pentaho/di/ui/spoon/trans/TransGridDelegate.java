package org.pentaho.di.ui.spoon.trans;

import java.util.Date;
import java.util.ResourceBundle;
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
import org.pentaho.di.i18n.LanguageChoice;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepStatus;
import org.pentaho.di.trans.step.BaseStepData.StepExecutionStatus;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.delegates.SpoonDelegate;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulLoader;
import org.pentaho.ui.xul.containers.XulToolbar;
import org.pentaho.ui.xul.impl.XulEventHandler;
import org.pentaho.ui.xul.swt.SwtXulLoader;
import org.pentaho.ui.xul.swt.tags.SwtToolbarbutton;

public class TransGridDelegate extends SpoonDelegate implements XulEventHandler {
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
		fdView.top = new FormAttachment((Control)toolbar.getManagedObject(),0);
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

  private void addToolBar() {

    try {
      XulLoader loader = new SwtXulLoader();
      ResourceBundle bundle = ResourceBundle.getBundle("org/pentaho/di/ui/spoon/messages/messages", LanguageChoice.getInstance().getDefaultLocale());
      XulDomContainer xulDomContainer = loader.loadXul(XUL_FILE_TRANS_GRID_TOOLBAR, bundle);
      xulDomContainer.addEventHandler(this);
      toolbar = (XulToolbar) xulDomContainer.getDocumentRoot().getElementById("nav-toolbar");

      ToolBar swtToolBar = (ToolBar) toolbar.getManagedObject();
      swtToolBar.layout(true, true);
    } catch (Throwable t) {
      log.logError(toString(), Const.getStackTracker(t));
      new ErrorDialog(transGridComposite.getShell(), BaseMessages.getString(PKG, "Spoon.Exception.ErrorReadingXULFile.Title"), BaseMessages.getString(PKG, "Spoon.Exception.ErrorReadingXULFile.Message", XUL_FILE_TRANS_GRID_TOOLBAR), new Exception(t));
    }
  }
  
	public void showHideInactive() {
		hideInactiveSteps=!hideInactiveSteps;
		
		// TODO: change icon
		SwtToolbarbutton onlyActiveButton = (SwtToolbarbutton) toolbar.getElementById("show-inactive");
		if (onlyActiveButton!=null) {
			onlyActiveButton.setSelected(hideInactiveSteps);
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

	

	/* (non-Javadoc)
   * @see org.pentaho.ui.xul.impl.XulEventHandler#getData()
   */
  public Object getData() {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.pentaho.ui.xul.impl.XulEventHandler#getName()
   */
  public String getName() {
    return "transgrid";
  }

  /* (non-Javadoc)
   * @see org.pentaho.ui.xul.impl.XulEventHandler#getXulDomContainer()
   */
  public XulDomContainer getXulDomContainer() {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.pentaho.ui.xul.impl.XulEventHandler#setData(java.lang.Object)
   */
  public void setData(Object data) {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see org.pentaho.ui.xul.impl.XulEventHandler#setName(java.lang.String)
   */
  public void setName(String name) {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see org.pentaho.ui.xul.impl.XulEventHandler#setXulDomContainer(org.pentaho.ui.xul.XulDomContainer)
   */
  public void setXulDomContainer(XulDomContainer xulDomContainer) {
    // TODO Auto-generated method stub

  }
}
