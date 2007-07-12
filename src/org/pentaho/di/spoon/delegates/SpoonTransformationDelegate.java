package org.pentaho.di.spoon.delegates;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.NotePadMeta;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.dialog.ShowMessageDialog;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.gui.GUIResource;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.core.gui.SpoonInterface;
import org.pentaho.di.core.undo.TransAction;
import org.pentaho.di.spoon.Messages;
import org.pentaho.di.spoon.Spoon;
import org.pentaho.di.spoon.TabMapEntry;
import org.pentaho.di.spoon.job.JobGraph;
import org.pentaho.di.spoon.trans.TransGraph;
import org.pentaho.di.spoon.trans.TransHistory;
import org.pentaho.di.spoon.trans.TransHistoryRefresher;
import org.pentaho.di.spoon.trans.TransLog;
import org.pentaho.di.trans.TransConfiguration;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.cluster.TransSplitter;
import org.pentaho.di.trans.dialog.TransExecutionConfigurationDialog;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.www.AddTransServlet;
import org.pentaho.di.www.PrepareExecutionTransServlet;
import org.pentaho.di.www.StartExecutionTransServlet;
import org.pentaho.di.www.WebResult;
import org.pentaho.xul.swt.tab.TabItem;

public class SpoonTransformationDelegate extends SpoonDelegate
{
	/**
	 * This contains a map between the name of a transformation and the
	 * TransMeta object. If the transformation has no name it will be mapped
	 * under a number [1], [2] etc.
	 */
	private Map<String, TransMeta> transformationMap;

	public SpoonTransformationDelegate(Spoon spoon)
	{
		super(spoon);
		transformationMap = new Hashtable<String, TransMeta>();
	}

	/**
	 * Add a transformation to the
	 * 
	 * @param transMeta
	 *            the transformation to add to the map
	 * @return the key used to store the transformation in the map
	 */
	public String addTransformation(TransMeta transMeta)
	{
		String key = spoon.delegates.tabs.makeTransGraphTabName(transMeta);

		if (transformationMap.get(key) == null)
		{
			transformationMap.put(key, transMeta);
		} else
		{
			ShowMessageDialog dialog = new ShowMessageDialog(spoon.getShell(), SWT.OK | SWT.ICON_INFORMATION,
					Messages.getString("Spoon.Dialog.TransAlreadyLoaded.Title"), "'" + key + "'" + Const.CR
							+ Const.CR + Messages.getString("Spoon.Dialog.TransAlreadyLoaded.Message"));
			dialog.setTimeOut(6);
			dialog.open();
			/*
			 * MessageBox mb = new MessageBox(shell, SWT.OK |
			 * SWT.ICON_INFORMATION);
			 * mb.setMessage("'"+key+"'"+Const.CR+Const.CR+Messages.getString("Spoon.Dialog.TransAlreadyLoaded.Message")); //
			 * Transformation is already loaded
			 * mb.setText(Messages.getString("Spoon.Dialog.TransAlreadyLoaded.Title")); //
			 * Sorry! mb.open();
			 */
		}

		return key;
	}

	/**
	 * @param transMeta
	 *            the transformation to close, make sure it's ok to dispose of
	 *            it BEFORE you call this.
	 */
	public void closeTransformation(TransMeta transMeta)
	{
		String tabName = spoon.delegates.tabs.makeTransGraphTabName(transMeta);
		transformationMap.remove(tabName);

		// Close the associated tabs...
		TabItem graphTab = spoon.delegates.tabs.findTabItem(tabName, TabMapEntry.OBJECT_TYPE_TRANSFORMATION_GRAPH);
		if (graphTab != null)
		{
			graphTab.dispose();
			spoon.delegates.tabs.removeTab(tabName);
		}

		// Logging
		String logTabName = spoon.delegates.tabs.makeLogTabName(transMeta);
		TabItem logTab = spoon.delegates.tabs.findTabItem(logTabName, TabMapEntry.OBJECT_TYPE_TRANSFORMATION_LOG);
		if (logTab != null)
		{
			logTab.dispose();
			spoon.delegates.tabs.removeTab(logTabName);
		}

		// History
		String historyTabName = spoon.delegates.tabs.makeHistoryTabName(transMeta);
		TabItem historyTab = spoon.delegates.tabs.findTabItem(historyTabName,
				TabMapEntry.OBJECT_TYPE_TRANSFORMATION_HISTORY);
		if (historyTab != null)
		{
			historyTab.dispose();
			spoon.delegates.tabs.removeTab(historyTabName);
		}

		spoon.refreshTree();
	}

	public void addTransGraph(TransMeta transMeta)
	{
		String key = addTransformation(transMeta);
		if (key != null)
		{
			// See if there already is a tab for this graph
			// If no, add it
			// If yes, select that tab
			//
			String tabName = spoon.delegates.tabs.makeTransGraphTabName(transMeta);
			TabItem tabItem = spoon.delegates.tabs.findTabItem(tabName, TabMapEntry.OBJECT_TYPE_TRANSFORMATION_GRAPH);
			if (tabItem == null)
			{
				TransGraph transGraph = new TransGraph(spoon.tabfolder.getSwtTabset(), spoon, transMeta);
				tabItem = new TabItem(spoon.tabfolder, tabName, tabName);
				tabItem.setToolTipText(Messages.getString("Spoon.TabTrans.Tooltip", spoon.delegates.tabs
						.makeTransGraphTabName(transMeta)));
				tabItem.setImage(GUIResource.getInstance().getImageTransGraph());
				tabItem.setControl(transGraph);

				spoon.delegates.tabs.addTab(tabName, new TabMapEntry(tabItem, tabName, transGraph,
						TabMapEntry.OBJECT_TYPE_TRANSFORMATION_GRAPH));
			}
			int idx = spoon.tabfolder.indexOf(tabItem);

			// OK, also see if we need to open a new history window.
			if (transMeta.getLogConnection() != null && !Const.isEmpty(transMeta.getLogTable()))
			{
				addTransHistory(transMeta, false);
			}
			// keep the focus on the graph
			spoon.tabfolder.setSelected(idx);

			spoon.setUndoMenu(transMeta);
			spoon.enableMenus();
		}
	}

	public TransHistory findTransHistoryOfTransformation(TransMeta transMeta)
	{
		if (transMeta == null)
			return null;

		// Now loop over the entries in the tab-map
		for (TabMapEntry mapEntry : spoon.delegates.tabs.getTabs())
		{
			if (mapEntry.getObject() instanceof TransHistory)
			{
				TransHistory transHistory = (TransHistory) mapEntry.getObject();
				if (transHistory.getMeta() != null && transHistory.getMeta().equals(transMeta))
					return transHistory;
			}
		}
		return null;
	}

	public TransLog findTransLogOfTransformation(TransMeta transMeta)
	{
		// Now loop over the entries in the tab-map
		for (TabMapEntry mapEntry : spoon.delegates.tabs.getTabs())
		{
			if (mapEntry.getObject() instanceof TransLog)
			{
				TransLog transLog = (TransLog) mapEntry.getObject();
				if (transLog.getMeta().equals(transMeta))
					return transLog;
			}
		}
		return null;
	}

	public void addTransLog(TransMeta transMeta, boolean setActive)
	{
		// See if there already is a tab for this log
		// If no, add it
		// If yes, select that tab
		// if setActive is true
		//
		String tabName = spoon.delegates.tabs.makeLogTabName(transMeta);
		TabItem tabItem = spoon.delegates.tabs.findTabItem(tabName, TabMapEntry.OBJECT_TYPE_TRANSFORMATION_LOG);
		if (tabItem == null)
		{
			TransLog transLog = new TransLog(spoon.tabfolder.getSwtTabset(), spoon, transMeta);
			tabItem = new TabItem(spoon.tabfolder, tabName, tabName);
			tabItem.setToolTipText(Messages.getString("Spoon.Title.ExecLogTransView.Tooltip", spoon.delegates.tabs
					.makeTransGraphTabName(transMeta)));
			tabItem.setControl(transLog);

			// If there is an associated history window, we want to keep that
			// one up-to-date as well.
			//
			TransHistory transHistory = findTransHistoryOfTransformation(transMeta);
			TabItem historyItem = spoon.delegates.tabs.findTabItem(spoon.delegates.tabs.makeHistoryTabName(transMeta),
					TabMapEntry.OBJECT_TYPE_TRANSFORMATION_HISTORY);

			if (transHistory != null && historyItem != null)
			{
				TransHistoryRefresher transHistoryRefresher = new TransHistoryRefresher(historyItem,
						transHistory);
				spoon.tabfolder.addListener(transHistoryRefresher);
				transLog.setTransHistoryRefresher(transHistoryRefresher);
			}

			spoon.delegates.tabs.addTab(tabName, new TabMapEntry(tabItem, tabName, transLog,
					TabMapEntry.OBJECT_TYPE_TRANSFORMATION_LOG));
		}
		if (setActive)
		{
			int idx = spoon.tabfolder.indexOf(tabItem);
			spoon.tabfolder.setSelected(idx);
		}
	}

	public void addTransHistory(TransMeta transMeta, boolean select)
	{
		// See if there already is a tab for this history view
		// If no, add it
		// If yes, select that tab
		//
		String tabName = spoon.delegates.tabs.makeHistoryTabName(transMeta);
		TabItem tabItem = spoon.delegates.tabs.findTabItem(tabName, TabMapEntry.OBJECT_TYPE_TRANSFORMATION_HISTORY);
		if (tabItem == null)
		{
			TransHistory transHistory = new TransHistory(spoon.tabfolder.getSwtTabset(), spoon, transMeta);
			tabItem = new TabItem(spoon.tabfolder, tabName, tabName);
			tabItem.setToolTipText(Messages.getString("Spoon.Title.ExecHistoryTransView.Tooltip", spoon.delegates.tabs
					.makeTransGraphTabName(transMeta)));
			tabItem.setControl(transHistory);

			// If there is an associated log window that's open, find it and add
			// a refresher
			TransLog transLog = findTransLogOfTransformation(transMeta);
			if (transLog != null)
			{
				TransHistoryRefresher transHistoryRefresher = new TransHistoryRefresher(tabItem, transHistory);
				spoon.tabfolder.addListener(transHistoryRefresher);
				transLog.setTransHistoryRefresher(transHistoryRefresher);
			}
			transHistory.markRefreshNeeded(); // will refresh when first
			// selected

			spoon.delegates.tabs.addTab(tabName, new TabMapEntry(tabItem, tabName, transHistory,
					TabMapEntry.OBJECT_TYPE_TRANSFORMATION_HISTORY));
		}
		if (select)
		{
			int idx = spoon.tabfolder.indexOf(tabItem);
			spoon.tabfolder.setSelected(idx);
		}
	}

	public void tabSelected(TabItem item)
	{
		List<TabMapEntry> collection = spoon.delegates.tabs.getTabs();

		// See which core objects to show
		//
		for (TabMapEntry entry : collection)
		{
			if (item.equals(entry.getTabItem()))
			{
				// TabItemInterface itemInterface = entry.getObject();

				//
				// Another way to implement this may be to keep track of the
				// state of the core object tree in method
				// addCoreObjectsToTree()
				//
				if (entry.getObject() instanceof TransGraph || entry.getObject() instanceof JobGraph)
				{
					EngineMetaInterface meta = entry.getObject().getMeta();
					if (meta != null)
					{
						meta.setInternalKettleVariables();
					}
					if (spoon.getCoreObjectsState() != SpoonInterface.STATE_CORE_OBJECTS_SPOON)
					{
						spoon.refreshCoreObjects();
					}
				}
			}
		}

		// Also refresh the tree
		spoon.refreshTree();
		spoon.enableMenus();
	}

	public List<TransMeta> getTransformationList()
	{
		return new ArrayList<TransMeta>(transformationMap.values());
	}

	public TransMeta getTransformation(String tabItemText)
	{
		return transformationMap.get(tabItemText);
	}

	public void addTransformation(String key, TransMeta entry)
	{
		transformationMap.put(key, entry);
	}

	public void removeTransformation(String key)
	{
		transformationMap.remove(key);
	}

	public TransMeta[] getLoadedTransformations()
	{
		List<TransMeta> list = new ArrayList<TransMeta>(transformationMap.values());
		return list.toArray(new TransMeta[list.size()]);
	}

	public TransGraph findTransGraphOfTransformation(TransMeta transMeta)
	{
		// Now loop over the entries in the tab-map
		for (TabMapEntry mapEntry : spoon.delegates.tabs.getTabs())
		{
			if (mapEntry.getObject() instanceof TransGraph)
			{
				TransGraph transGraph = (TransGraph) mapEntry.getObject();
				if (transGraph.getMeta().equals(transMeta))
					return transGraph;
			}
		}
		return null;
	}

	public boolean isDefaultTransformationName(String name)
	{
		if (!name.startsWith(SpoonInterface.STRING_TRANSFORMATION))
			return false;

		// see if there are only digits behind the transformation...
		// This will detect:
		// "Transformation"
		// "Transformation "
		// "Transformation 1"
		// "Transformation 2"
		// ...
		for (int i = SpoonInterface.STRING_TRANSFORMATION.length() + 1; i < name.length(); i++)
		{
			if (!Character.isDigit(name.charAt(i)))
				return false;
		}
		return true;
	}

	public void undoTransformationAction(TransMeta transMeta, TransAction transAction)
	{
		switch (transAction.getType())
		{
		// We created a new step : undo this...
		case TransAction.TYPE_ACTION_NEW_STEP:
			// Delete the step at correct location:
			for (int i = transAction.getCurrent().length - 1; i >= 0; i--)
			{
				int idx = transAction.getCurrentIndex()[i];
				transMeta.removeStep(idx);
			}
			spoon.refreshTree();
			spoon.refreshGraph();
			break;

		// We created a new connection : undo this...
		case TransAction.TYPE_ACTION_NEW_CONNECTION:
			// Delete the connection at correct location:
			for (int i = transAction.getCurrent().length - 1; i >= 0; i--)
			{
				int idx = transAction.getCurrentIndex()[i];
				transMeta.removeDatabase(idx);
			}
			spoon.refreshTree();
			spoon.refreshGraph();
			break;

		// We created a new note : undo this...
		case TransAction.TYPE_ACTION_NEW_NOTE:
			// Delete the note at correct location:
			for (int i = transAction.getCurrent().length - 1; i >= 0; i--)
			{
				int idx = transAction.getCurrentIndex()[i];
				transMeta.removeNote(idx);
			}
			spoon.refreshTree();
			spoon.refreshGraph();
			break;

		// We created a new hop : undo this...
		case TransAction.TYPE_ACTION_NEW_HOP:
			// Delete the hop at correct location:
			for (int i = transAction.getCurrent().length - 1; i >= 0; i--)
			{
				int idx = transAction.getCurrentIndex()[i];
				transMeta.removeTransHop(idx);
			}
			spoon.refreshTree();
			spoon.refreshGraph();
			break;

		// We created a new slave : undo this...
		case TransAction.TYPE_ACTION_NEW_SLAVE:
			// Delete the slave at correct location:
			for (int i = transAction.getCurrent().length - 1; i >= 0; i--)
			{
				int idx = transAction.getCurrentIndex()[i];
				transMeta.getSlaveServers().remove(idx);
			}
			spoon.refreshTree();
			spoon.refreshGraph();
			break;

		// We created a new slave : undo this...
		case TransAction.TYPE_ACTION_NEW_CLUSTER:
			// Delete the slave at correct location:
			for (int i = transAction.getCurrent().length - 1; i >= 0; i--)
			{
				int idx = transAction.getCurrentIndex()[i];
				transMeta.getClusterSchemas().remove(idx);
			}
			spoon.refreshTree();
			spoon.refreshGraph();
			break;

		//
		// DELETE
		//

		// We delete a step : undo this...
		case TransAction.TYPE_ACTION_DELETE_STEP:
			// un-Delete the step at correct location: re-insert
			for (int i = 0; i < transAction.getCurrent().length; i++)
			{
				StepMeta stepMeta = (StepMeta) transAction.getCurrent()[i];
				int idx = transAction.getCurrentIndex()[i];
				transMeta.addStep(idx, stepMeta);
			}
			spoon.refreshTree();
			spoon.refreshGraph();
			break;

		// We deleted a connection : undo this...
		case TransAction.TYPE_ACTION_DELETE_CONNECTION:
			// re-insert the connection at correct location:
			for (int i = 0; i < transAction.getCurrent().length; i++)
			{
				DatabaseMeta ci = (DatabaseMeta) transAction.getCurrent()[i];
				int idx = transAction.getCurrentIndex()[i];
				transMeta.addDatabase(idx, ci);
			}
			spoon.refreshTree();
			spoon.refreshGraph();
			break;

		// We delete new note : undo this...
		case TransAction.TYPE_ACTION_DELETE_NOTE:
			// re-insert the note at correct location:
			for (int i = 0; i < transAction.getCurrent().length; i++)
			{
				NotePadMeta ni = (NotePadMeta) transAction.getCurrent()[i];
				int idx = transAction.getCurrentIndex()[i];
				transMeta.addNote(idx, ni);
			}
			spoon.refreshTree();
			spoon.refreshGraph();
			break;

		// We deleted a hop : undo this...
		case TransAction.TYPE_ACTION_DELETE_HOP:
			// re-insert the hop at correct location:
			for (int i = 0; i < transAction.getCurrent().length; i++)
			{
				TransHopMeta hi = (TransHopMeta) transAction.getCurrent()[i];
				int idx = transAction.getCurrentIndex()[i];
				// Build a new hop:
				StepMeta from = transMeta.findStep(hi.getFromStep().getName());
				StepMeta to = transMeta.findStep(hi.getToStep().getName());
				TransHopMeta hinew = new TransHopMeta(from, to);
				transMeta.addTransHop(idx, hinew);
			}
			spoon.refreshTree();
			spoon.refreshGraph();
			break;

		//
		// CHANGE
		//

		// We changed a step : undo this...
		case TransAction.TYPE_ACTION_CHANGE_STEP:
			// Delete the current step, insert previous version.
			for (int i = 0; i < transAction.getCurrent().length; i++)
			{
				StepMeta prev = (StepMeta) ((StepMeta) transAction.getPrevious()[i]).clone();
				int idx = transAction.getCurrentIndex()[i];

				transMeta.getStep(idx).replaceMeta(prev);
			}
			spoon.refreshTree();
			spoon.refreshGraph();
			break;

		// We changed a connection : undo this...
		case TransAction.TYPE_ACTION_CHANGE_CONNECTION:
			// Delete & re-insert
			for (int i = 0; i < transAction.getCurrent().length; i++)
			{
				DatabaseMeta prev = (DatabaseMeta) transAction.getPrevious()[i];
				int idx = transAction.getCurrentIndex()[i];

				transMeta.getDatabase(idx).replaceMeta((DatabaseMeta) prev.clone());
			}
			spoon.refreshTree();
			spoon.refreshGraph();
			break;

		// We changed a note : undo this...
		case TransAction.TYPE_ACTION_CHANGE_NOTE:
			// Delete & re-insert
			for (int i = 0; i < transAction.getCurrent().length; i++)
			{
				int idx = transAction.getCurrentIndex()[i];
				transMeta.removeNote(idx);
				NotePadMeta prev = (NotePadMeta) transAction.getPrevious()[i];
				transMeta.addNote(idx, (NotePadMeta) prev.clone());
			}
			spoon.refreshTree();
			spoon.refreshGraph();
			break;

		// We changed a hop : undo this...
		case TransAction.TYPE_ACTION_CHANGE_HOP:
			// Delete & re-insert
			for (int i = 0; i < transAction.getCurrent().length; i++)
			{
				TransHopMeta prev = (TransHopMeta) transAction.getPrevious()[i];
				int idx = transAction.getCurrentIndex()[i];

				transMeta.removeTransHop(idx);
				transMeta.addTransHop(idx, (TransHopMeta) prev.clone());
			}
			spoon.refreshTree();
			spoon.refreshGraph();
			break;

		//
		// POSITION
		//

		// The position of a step has changed: undo this...
		case TransAction.TYPE_ACTION_POSITION_STEP:
			// Find the location of the step:
			for (int i = 0; i < transAction.getCurrentIndex().length; i++)
			{
				StepMeta stepMeta = transMeta.getStep(transAction.getCurrentIndex()[i]);
				stepMeta.setLocation(transAction.getPreviousLocation()[i]);
			}
			spoon.refreshGraph();
			break;

		// The position of a note has changed: undo this...
		case TransAction.TYPE_ACTION_POSITION_NOTE:
			for (int i = 0; i < transAction.getCurrentIndex().length; i++)
			{
				int idx = transAction.getCurrentIndex()[i];
				NotePadMeta npi = transMeta.getNote(idx);
				Point prev = transAction.getPreviousLocation()[i];
				npi.setLocation(prev);
			}
			spoon.refreshGraph();
			break;
		default:
			break;
		}

		// OK, now check if we need to do this again...
		if (transMeta.viewNextUndo() != null)
		{
			if (transMeta.viewNextUndo().getNextAlso())
				spoon.undoAction(transMeta);
		}
	}

	public void redoTransformationAction(TransMeta transMeta, TransAction transAction)
	{
		switch (transAction.getType())
		{
		case TransAction.TYPE_ACTION_NEW_STEP:
			// re-delete the step at correct location:
			for (int i = 0; i < transAction.getCurrent().length; i++)
			{
				StepMeta stepMeta = (StepMeta) transAction.getCurrent()[i];
				int idx = transAction.getCurrentIndex()[i];
				transMeta.addStep(idx, stepMeta);

				spoon.refreshTree();
				spoon.refreshGraph();
			}
			break;

		case TransAction.TYPE_ACTION_NEW_CONNECTION:
			// re-insert the connection at correct location:
			for (int i = 0; i < transAction.getCurrent().length; i++)
			{
				DatabaseMeta ci = (DatabaseMeta) transAction.getCurrent()[i];
				int idx = transAction.getCurrentIndex()[i];
				transMeta.addDatabase(idx, ci);
				spoon.refreshTree();
				spoon.refreshGraph();
			}
			break;

		case TransAction.TYPE_ACTION_NEW_NOTE:
			// re-insert the note at correct location:
			for (int i = 0; i < transAction.getCurrent().length; i++)
			{
				NotePadMeta ni = (NotePadMeta) transAction.getCurrent()[i];
				int idx = transAction.getCurrentIndex()[i];
				transMeta.addNote(idx, ni);
				spoon.refreshTree();
				spoon.refreshGraph();
			}
			break;

		case TransAction.TYPE_ACTION_NEW_HOP:
			// re-insert the hop at correct location:
			for (int i = 0; i < transAction.getCurrent().length; i++)
			{
				TransHopMeta hi = (TransHopMeta) transAction.getCurrent()[i];
				int idx = transAction.getCurrentIndex()[i];
				transMeta.addTransHop(idx, hi);
				spoon.refreshTree();
				spoon.refreshGraph();
			}
			break;

		//  
		// DELETE
		//
		case TransAction.TYPE_ACTION_DELETE_STEP:
			// re-remove the step at correct location:
			for (int i = transAction.getCurrent().length - 1; i >= 0; i--)
			{
				int idx = transAction.getCurrentIndex()[i];
				transMeta.removeStep(idx);
			}
			spoon.refreshTree();
			spoon.refreshGraph();
			break;

		case TransAction.TYPE_ACTION_DELETE_CONNECTION:
			// re-remove the connection at correct location:
			for (int i = transAction.getCurrent().length - 1; i >= 0; i--)
			{
				int idx = transAction.getCurrentIndex()[i];
				transMeta.removeDatabase(idx);
			}
			spoon.refreshTree();
			spoon.refreshGraph();
			break;

		case TransAction.TYPE_ACTION_DELETE_NOTE:
			// re-remove the note at correct location:
			for (int i = transAction.getCurrent().length - 1; i >= 0; i--)
			{
				int idx = transAction.getCurrentIndex()[i];
				transMeta.removeNote(idx);
			}
			spoon.refreshTree();
			spoon.refreshGraph();
			break;

		case TransAction.TYPE_ACTION_DELETE_HOP:
			// re-remove the hop at correct location:
			for (int i = transAction.getCurrent().length - 1; i >= 0; i--)
			{
				int idx = transAction.getCurrentIndex()[i];
				transMeta.removeTransHop(idx);
			}
			spoon.refreshTree();
			spoon.refreshGraph();
			break;

		//
		// CHANGE
		//

		// We changed a step : undo this...
		case TransAction.TYPE_ACTION_CHANGE_STEP:
			// Delete the current step, insert previous version.
			for (int i = 0; i < transAction.getCurrent().length; i++)
			{
				StepMeta stepMeta = (StepMeta) ((StepMeta) transAction.getCurrent()[i]).clone();
				transMeta.getStep(transAction.getCurrentIndex()[i]).replaceMeta(stepMeta);
			}
			spoon.refreshTree();
			spoon.refreshGraph();
			break;

		// We changed a connection : undo this...
		case TransAction.TYPE_ACTION_CHANGE_CONNECTION:
			// Delete & re-insert
			for (int i = 0; i < transAction.getCurrent().length; i++)
			{
				DatabaseMeta databaseMeta = (DatabaseMeta) transAction.getCurrent()[i];
				int idx = transAction.getCurrentIndex()[i];

				transMeta.getDatabase(idx).replaceMeta((DatabaseMeta) databaseMeta.clone());
			}
			spoon.refreshTree();
			spoon.refreshGraph();
			break;

		// We changed a note : undo this...
		case TransAction.TYPE_ACTION_CHANGE_NOTE:
			// Delete & re-insert
			for (int i = 0; i < transAction.getCurrent().length; i++)
			{
				NotePadMeta ni = (NotePadMeta) transAction.getCurrent()[i];
				int idx = transAction.getCurrentIndex()[i];

				transMeta.removeNote(idx);
				transMeta.addNote(idx, (NotePadMeta) ni.clone());
			}
			spoon.refreshTree();
			spoon.refreshGraph();
			break;

		// We changed a hop : undo this...
		case TransAction.TYPE_ACTION_CHANGE_HOP:
			// Delete & re-insert
			for (int i = 0; i < transAction.getCurrent().length; i++)
			{
				TransHopMeta hi = (TransHopMeta) transAction.getCurrent()[i];
				int idx = transAction.getCurrentIndex()[i];

				transMeta.removeTransHop(idx);
				transMeta.addTransHop(idx, (TransHopMeta) hi.clone());
			}
			spoon.refreshTree();
			spoon.refreshGraph();
			break;

		//
		// CHANGE POSITION
		//
		case TransAction.TYPE_ACTION_POSITION_STEP:
			for (int i = 0; i < transAction.getCurrentIndex().length; i++)
			{
				// Find & change the location of the step:
				StepMeta stepMeta = transMeta.getStep(transAction.getCurrentIndex()[i]);
				stepMeta.setLocation(transAction.getCurrentLocation()[i]);
			}
			spoon.refreshGraph();
			break;
		case TransAction.TYPE_ACTION_POSITION_NOTE:
			for (int i = 0; i < transAction.getCurrentIndex().length; i++)
			{
				int idx = transAction.getCurrentIndex()[i];
				NotePadMeta npi = transMeta.getNote(idx);
				Point curr = transAction.getCurrentLocation()[i];
				npi.setLocation(curr);
			}
			spoon.refreshGraph();
			break;
		default:
			break;
		}

		// OK, now check if we need to do this again...
		if (transMeta.viewNextUndo() != null)
		{
			if (transMeta.viewNextUndo().getNextAlso())
				spoon.redoAction(transMeta);
		}
	}

	public void executeTransformation(TransMeta transMeta, boolean local, boolean remote, boolean cluster,
			boolean preview, Date replayDate) throws KettleException
	{
		if (transMeta == null)
			return;

		TransExecutionConfiguration executionConfiguration = spoon.getExecutionConfiguration();
		executionConfiguration.setExecutingLocally(local);
		executionConfiguration.setExecutingRemotely(remote);
		executionConfiguration.setExecutingClustered(cluster);

		Object data[] = spoon.variables.getData();
		String fields[] = spoon.variables.getRowMeta().getFieldNames();
		Map<String, String> variableMap = new HashMap<String, String>();
		for (int idx = 0; idx < fields.length; idx++)
		{
			variableMap.put(fields[idx], data[idx].toString());
		}

		executionConfiguration.getUsedVariables(transMeta);
		executionConfiguration.getUsedArguments(transMeta, spoon.getArguments());
		executionConfiguration.setVariables(variableMap);
		executionConfiguration.setReplayDate(replayDate);
		executionConfiguration.setLocalPreviewing(preview);

		executionConfiguration.setLogLevel(spoon.getLog().getLogLevel());
		// executionConfiguration.setSafeModeEnabled( transLog!=null &&
		// transLog.isSafeModeChecked() );

		TransExecutionConfigurationDialog dialog = new TransExecutionConfigurationDialog(spoon.getShell(),
				executionConfiguration, transMeta);
		if (dialog.open())
		{
			addTransLog(transMeta, !executionConfiguration.isLocalPreviewing());
			TransLog transLog = spoon.getActiveTransLog();

			if (executionConfiguration.isExecutingLocally())
			{
				if (executionConfiguration.isLocalPreviewing())
				{
					transLog.preview(executionConfiguration);
				} else
				{
					transLog.start(executionConfiguration);
				}
			} else if (executionConfiguration.isExecutingRemotely())
			{
				if (executionConfiguration.getRemoteServer() != null)
				{
					spoon.sendXMLToSlaveServer(transMeta, executionConfiguration);
					spoon.addSpoonSlave(executionConfiguration.getRemoteServer());
				} else
				{
					MessageBox mb = new MessageBox(spoon.getShell(), SWT.OK | SWT.ICON_INFORMATION);
					mb.setMessage(Messages.getString("Spoon.Dialog.NoRemoteServerSpecified.Message"));
					mb.setText(Messages.getString("Spoon.Dialog.NoRemoteServerSpecified.Title"));
					mb.open();
				}
			} else if (executionConfiguration.isExecutingClustered())
			{
				splitTrans(transMeta, executionConfiguration.isClusterShowingTransformation(),
						executionConfiguration.isClusterPosting(), executionConfiguration
								.isClusterPreparing(), executionConfiguration.isClusterStarting());
			}
		}
		spoon.setArguments(executionConfiguration.getArgumentStrings());
	}

	public void splitTrans(TransMeta transMeta, boolean show, boolean post, boolean prepare, boolean start)
			throws KettleException
	{
		try
		{
			if (Const.isEmpty(transMeta.getName()))
				throw new KettleException(
						"The transformation needs a name to uniquely identify it by on the remote server.");

			TransSplitter transSplitter = new TransSplitter(transMeta);
			transSplitter.splitOriginalTransformation();

			// Send the transformations to the servers...
			//
			// First the master...
			//
			TransMeta master = transSplitter.getMaster();
			SlaveServer masterServer = null;
			List<StepMeta> masterSteps = master.getTransHopSteps(false);
			if (masterSteps.size() > 0) // If there is something that needs to
			// be done on the master...
			{
				masterServer = transSplitter.getMasterServer();
				if (show)
					addTransGraph(master);
				if (post)
				{
					String masterReply = masterServer.sendXML(new TransConfiguration(master, spoon
							.getExecutionConfiguration()).getXML(), AddTransServlet.CONTEXT_PATH + "/?xml=Y");
					WebResult webResult = WebResult.fromXMLString(masterReply);
					if (!webResult.getResult().equalsIgnoreCase(WebResult.STRING_OK))
					{
						throw new KettleException("An error occurred sending the master transformation: "
								+ webResult.getMessage());
					}
				}
			}

			// Then the slaves...
			//
			SlaveServer slaves[] = transSplitter.getSlaveTargets();
			for (int i = 0; i < slaves.length; i++)
			{
				TransMeta slaveTrans = (TransMeta) transSplitter.getSlaveTransMap().get(slaves[i]);
				if (show)
					addTransGraph(slaveTrans);
				if (post)
				{
					TransConfiguration transConfiguration = new TransConfiguration(slaveTrans, spoon
							.getExecutionConfiguration());
					Map<String, String> variables = transConfiguration.getTransExecutionConfiguration()
							.getVariables();
					variables.put(Const.INTERNAL_VARIABLE_SLAVE_TRANS_NUMBER, Integer.toString(i));
					variables.put(Const.INTERNAL_VARIABLE_CLUSTER_SIZE, Integer.toString(slaves.length));
					String slaveReply = slaves[i].sendXML(transConfiguration.getXML(),
							AddTransServlet.CONTEXT_PATH + "/?xml=Y");
					WebResult webResult = WebResult.fromXMLString(slaveReply);
					if (!webResult.getResult().equalsIgnoreCase(WebResult.STRING_OK))
					{
						throw new KettleException("An error occurred sending a slave transformation: "
								+ webResult.getMessage());
					}
				}
			}

			if (post)
			{
				if (prepare)
				{
					// Prepare the master...
					if (masterSteps.size() > 0) // If there is something that
					// needs to be done on the
					// master...
					{
						String masterReply = masterServer
								.getContentFromServer(PrepareExecutionTransServlet.CONTEXT_PATH + "/?name="
										+ master.getName() + "&xml=Y");
						WebResult webResult = WebResult.fromXMLString(masterReply);
						if (!webResult.getResult().equalsIgnoreCase(WebResult.STRING_OK))
						{
							throw new KettleException(
									"An error occurred while preparing the execution of the master transformation: "
											+ webResult.getMessage());
						}
					}

					// Prepare the slaves
					for (int i = 0; i < slaves.length; i++)
					{
						TransMeta slaveTrans = (TransMeta) transSplitter.getSlaveTransMap().get(slaves[i]);
						String slaveReply = slaves[i]
								.getContentFromServer(PrepareExecutionTransServlet.CONTEXT_PATH + "/?name="
										+ slaveTrans.getName() + "&xml=Y");
						WebResult webResult = WebResult.fromXMLString(slaveReply);
						if (!webResult.getResult().equalsIgnoreCase(WebResult.STRING_OK))
						{
							throw new KettleException(
									"An error occurred while preparing the execution of a slave transformation: "
											+ webResult.getMessage());
						}
					}
				}

				if (start)
				{
					// Start the master...
					if (masterSteps.size() > 0) // If there is something that
					// needs to be done on the
					// master...
					{
						String masterReply = masterServer
								.getContentFromServer(StartExecutionTransServlet.CONTEXT_PATH + "/?name="
										+ master.getName() + "&xml=Y");
						WebResult webResult = WebResult.fromXMLString(masterReply);
						if (!webResult.getResult().equalsIgnoreCase(WebResult.STRING_OK))
						{
							throw new KettleException(
									"An error occurred while starting the execution of the master transformation: "
											+ webResult.getMessage());
						}
					}

					// Start the slaves
					for (int i = 0; i < slaves.length; i++)
					{
						TransMeta slaveTrans = (TransMeta) transSplitter.getSlaveTransMap().get(slaves[i]);
						String slaveReply = slaves[i]
								.getContentFromServer(StartExecutionTransServlet.CONTEXT_PATH + "/?name="
										+ slaveTrans.getName() + "&xml=Y");
						WebResult webResult = WebResult.fromXMLString(slaveReply);
						if (!webResult.getResult().equalsIgnoreCase(WebResult.STRING_OK))
						{
							throw new KettleException(
									"An error occurred while starting the execution of a slave transformation: "
											+ webResult.getMessage());
						}
					}
				}

				// Now add monitors for the master and all the slave servers
				//
				if (masterServer != null)
				{
					spoon.addSpoonSlave(masterServer);
					for (int i = 0; i < slaves.length; i++)
					{
						spoon.addSpoonSlave(slaves[i]);
					}
				}
			}
		} catch (Exception e)
		{
			throw new KettleException(e);
		}

	}

}
