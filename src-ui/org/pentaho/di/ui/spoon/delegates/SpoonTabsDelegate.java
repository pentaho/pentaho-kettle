/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.ui.spoon.delegates;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.gui.SpoonInterface;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.spoon.Messages;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.SpoonBrowser;
import org.pentaho.di.ui.spoon.TabItemInterface;
import org.pentaho.di.ui.spoon.TabMapEntry;
import org.pentaho.di.ui.spoon.job.JobGraph;
import org.pentaho.di.ui.spoon.job.JobHistory;
import org.pentaho.di.ui.spoon.job.JobLog;
import org.pentaho.di.ui.spoon.trans.TransGraph;
import org.pentaho.di.ui.spoon.trans.TransHistory;
import org.pentaho.di.ui.spoon.trans.TransLog;
import org.pentaho.xul.swt.tab.TabItem;
import org.pentaho.xul.swt.tab.TabSet;

public class SpoonTabsDelegate extends SpoonDelegate
{
	/**
	 * This contains a map between the name of the tab name and the object name
	 * and type
	 */
	private Map<String, TabMapEntry> tabMap;

	public SpoonTabsDelegate(Spoon spoon)
	{
		super(spoon);
		tabMap = new Hashtable<String, TabMapEntry>();
	}

	public boolean tabClose(TabItem item)
	{
		// Try to find the tab-item that's being closed.
		List<TabMapEntry> collection = new ArrayList<TabMapEntry>();
		collection.addAll(tabMap.values());
		int idx = 0;
		
		boolean close = true;
		for (TabMapEntry entry : collection)
		{
			if (item.equals(entry.getTabItem()))
			{
				TabItemInterface itemInterface = entry.getObject();

				// Can we close this tab?
				if (!itemInterface.canBeClosed())
				{
					int reply = itemInterface.showChangedWarning();
					if (reply == SWT.YES)
					{
						close = itemInterface.applyChanges();
					} else
					{
						if (reply == SWT.CANCEL)
						{
							close = false;
						} else
						{
							close = true;
						}
					}
				}

				// Also clean up the log/history associated with this
				// transformation/job
				//                            
				if (close)
				{
					if (entry.getObject() instanceof TransGraph)
					{
						TransMeta transMeta = (TransMeta) entry.getObject().getManagedObject();
						spoon.delegates.trans.closeTransformation(transMeta);
						spoon.refreshTree();
						//spoon.refreshCoreObjects();
					} else if (entry.getObject() instanceof JobGraph)
					{
						JobMeta jobMeta = (JobMeta) entry.getObject().getManagedObject();
						spoon.delegates.jobs.closeJob(jobMeta);
						spoon.refreshTree();
						//spoon.refreshCoreObjects();
					} else if (entry.getObject() instanceof SpoonBrowser)
					{
						spoon.closeSpoonBrowser();
						spoon.refreshTree();
					}

					else if (entry.getObject() instanceof Composite)
					{
						Composite comp = (Composite) entry.getObject();
						if (comp != null && !comp.isDisposed())
							comp.dispose();
					}
				}
				
				break;
			}
			
			idx+=1;
		}
		
		//select a tab
		if (idx-1>=0)
		{
			TabMapEntry entry = collection.get(idx-1);
			if (entry!=null){
				spoon.tabfolder.setSelected(entry.getTabItem());
				tabSelected(entry.getTabItem());
				spoon.showSelection();
			}
		}
		
		return close;
	}

	public void removeTab(String key)
	{
		tabMap.remove(key);
	}

	public List<TabMapEntry> getTabs()
	{
		List<TabMapEntry> list = new ArrayList<TabMapEntry>();
		list.addAll(tabMap.values());
		return list;
	}

	public TabMapEntry getTab(String key)
	{
		return tabMap.get(key);
	}

	public EngineMetaInterface getActiveMeta()
	{
		TabSet tabfolder = spoon.tabfolder;
		if (tabfolder == null)
			return null;
		TabItem tabItem = tabfolder.getSelected();
		if (tabItem == null)
			return null;

		// What transformation is in the active tab?
		// TransLog, TransGraph & TransHist contain the same transformation
		//
		TabMapEntry mapEntry = tabMap.get(tabfolder.getSelected().getText());
		EngineMetaInterface meta = null;
		if (mapEntry != null)
		{
			if (mapEntry.getObject() instanceof TransGraph)
				meta = (mapEntry.getObject()).getMeta();
			if (mapEntry.getObject() instanceof TransLog)
				meta = (mapEntry.getObject()).getMeta();
			if (mapEntry.getObject() instanceof TransHistory)
				meta = (mapEntry.getObject()).getMeta();
			if (mapEntry.getObject() instanceof JobGraph)
				meta = (mapEntry.getObject()).getMeta();
			if (mapEntry.getObject() instanceof JobLog)
				meta = (mapEntry.getObject()).getMeta();
			if (mapEntry.getObject() instanceof JobHistory)
				meta = (mapEntry.getObject()).getMeta();
		}

		return meta;
	}

	public String makeLogTabName(TransMeta transMeta)
	{
		return Messages.getString("Spoon.Title.LogTransView", makeTransGraphTabName(transMeta));
	}

	public String makeHistoryTabName(TransMeta transMeta)
	{
		return Messages.getString("Spoon.Title.LogTransHistoryView", makeTransGraphTabName(transMeta));
	}

	public String makeSlaveTabName(SlaveServer slaveServer)
	{
		return "Slave server: " + slaveServer.getName();
	}

	public boolean addSpoonBrowser(String name, String urlString)
	{
		TabSet tabfolder = spoon.tabfolder;

		try
		{
			// OK, now we have the HTML, create a new browset tab.

			// See if there already is a tab for this browser
			// If no, add it
			// If yes, select that tab
			//
			TabItem tabItem = findTabItem(name, TabMapEntry.OBJECT_TYPE_BROWSER);
			if (tabItem == null)
			{
				SpoonBrowser browser = new SpoonBrowser(tabfolder.getSwtTabset(), spoon, urlString);
				tabItem = new TabItem(tabfolder, name, name);
				tabItem.setImage(GUIResource.getInstance().getImageLogoSmall());
				tabItem.setControl(browser.getComposite());

				tabMap.put(name, new TabMapEntry(tabItem, name, browser, TabMapEntry.OBJECT_TYPE_BROWSER));
			}
			int idx = tabfolder.indexOf(tabItem);

			// keep the focus on the graph
			tabfolder.setSelected(idx);
			return true;
		} catch (Throwable e)
		{
			LogWriter.getInstance().logError(spoon.toString(), "Unable to open browser tab", e);
			return false;
		}
	}

	public TabItem findTabItem(String tabItemText, int objectType)
	{
		for (TabMapEntry entry : tabMap.values())
		{
			if (entry.getTabItem().isDisposed())
				continue;
			if (objectType == entry.getObjectType()
					&& entry.getTabItem().getText().equalsIgnoreCase(tabItemText))
			{
				return entry.getTabItem();
			}
		}
		return null;
	}

	/**
	 * Rename the tabs
	 */
	public void renameTabs()
	{
		Collection<TabMapEntry> collection = tabMap.values();
		List<TabMapEntry> list = new ArrayList<TabMapEntry>();
		list.addAll(collection);

		for (TabMapEntry entry : list)
		{
			if (entry.getTabItem().isDisposed())
			{
				// this should not be in the map, get rid of it.
				tabMap.remove(entry.getObjectName());
				continue;
			}

			String before = entry.getTabItem().getText();
			Object managedObject = entry.getObject().getManagedObject();
			if (managedObject != null)
			{

				if (entry.getObject() instanceof TransGraph)
				{
					TransMeta transMeta = (TransMeta) managedObject;
					entry.getTabItem().setText(makeTransGraphTabName(transMeta));
					String toolTipText = Messages.getString("Spoon.TabTrans.Tooltip", makeTransGraphTabName(transMeta));
					if (Const.isWindows() && !Const.isEmpty(transMeta.getFilename())) toolTipText+=Const.CR+Const.CR+transMeta.getFilename();
					entry.getTabItem().setToolTipText(toolTipText);
				} 
				else if (entry.getObject() instanceof TransLog)
				{
					entry.getTabItem().setText(makeLogTabName((TransMeta) managedObject));
				}
				else if (entry.getObject() instanceof TransHistory)
				{
					entry.getTabItem().setText(makeHistoryTabName((TransMeta) managedObject));
				}
				else if (entry.getObject() instanceof JobGraph)
				{
					JobMeta jobMeta = (JobMeta) managedObject;
					entry.getTabItem().setText(makeJobGraphTabName(jobMeta));
					String toolTipText = Messages.getString("Spoon.TabJob.Tooltip", makeJobGraphTabName(jobMeta));
					if (Const.isWindows() && !Const.isEmpty(jobMeta.getFilename())) toolTipText+=Const.CR+Const.CR+jobMeta.getFilename();
					entry.getTabItem().setToolTipText(toolTipText);
				} else if (entry.getObject() instanceof JobLog)
				{
					entry.getTabItem().setText(makeJobLogTabName((JobMeta) managedObject));
				}
				else if (entry.getObject() instanceof JobHistory)
				{
					entry.getTabItem().setText(makeJobHistoryTabName((JobMeta) managedObject));
				}
			}

			String after = entry.getTabItem().getText();

			if (!before.equals(after))
			{
				entry.setObjectName(after);
				tabMap.remove(before);
				tabMap.put(after, entry);

				// Also change the transformation map
				if (entry.getObject() instanceof TransGraph)
				{
					spoon.delegates.trans.removeTransformation(before);
					spoon.delegates.trans.addTransformation(after, (TransMeta) entry.getObject().getManagedObject());
				}
				// Also change the job map
				if (entry.getObject() instanceof JobGraph)
				{
					spoon.delegates.jobs.removeJob(before);
					spoon.delegates.jobs.addJob(after, (JobMeta) entry.getObject().getManagedObject());
				}
			}
		}
		spoon.setShellText();
	}

	public void addTab(String key, TabMapEntry entry)
	{
		tabMap.put(key, entry);
	}

	public String makeTransGraphTabName(TransMeta transMeta)
	{
		if (Const.isEmpty(transMeta.getName()) && Const.isEmpty(transMeta.getFilename()))
			return Spoon.STRING_TRANS_NO_NAME;

		if (Const.isEmpty(transMeta.getName())
				|| spoon.delegates.trans.isDefaultTransformationName(transMeta.getName()))
		{
			transMeta.nameFromFilename();
		}

		return transMeta.getName();
	}

	public String makeJobLogTabName(JobMeta jobMeta)
	{
		return Messages.getString("Spoon.Title.LogJobView", makeJobGraphTabName(jobMeta));
	}

	public String makeJobHistoryTabName(JobMeta jobMeta)
	{
		return Messages.getString("Spoon.Title.LogJobHistoryView", makeJobGraphTabName(jobMeta));
	}

	public String makeJobGraphTabName(JobMeta jobMeta)
	{
		if (Const.isEmpty(jobMeta.getName()) && Const.isEmpty(jobMeta.getFilename()))
			return Spoon.STRING_JOB_NO_NAME;

		if (Const.isEmpty(jobMeta.getName()) || spoon.delegates.jobs.isDefaultJobName(jobMeta.getName()))
		{
			jobMeta.nameFromFilename();
		}

		return jobMeta.getName();
	}

	public void tabSelected(TabItem item)
	{
		ArrayList<TabMapEntry> collection = new ArrayList<TabMapEntry>();
		collection.addAll(tabMap.values());

		// See which core objects to show
		//
		for (TabMapEntry entry : collection)
		{
			boolean isTrans = (entry.getObject() instanceof TransGraph);
			
			if (item.equals(entry.getTabItem()))
			{
				// TabItemInterface itemInterface = entry.getObject();

				//
				// Another way to implement this may be to keep track of the
				// state of the core object tree in method
				// addCoreObjectsToTree()
				//
				
				if (isTrans || entry.getObject() instanceof JobGraph)
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
				
				setEnabled("trans-preview",isTrans);
				setEnabled("trans-verify",isTrans);
				setEnabled("trans-impact",isTrans);
				
				break;
			}
		}

		// Also refresh the tree
		spoon.refreshTree();
		spoon.enableMenus();
		
		
	}
	
	private void setEnabled(String id,boolean enable)
	{
		spoon.getToolbar().getButtonById(id).setEnable(enable);
	}
	
}
