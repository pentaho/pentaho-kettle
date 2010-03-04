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
import java.util.List;

import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.dnd.DragAndDropContainer;
import org.pentaho.di.core.dnd.XMLTransfer;
import org.pentaho.di.core.plugins.JobEntryPluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.PluginTypeInterface;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.ui.core.ConstUI;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.TreeSelection;

public class SpoonTreeDelegate extends SpoonDelegate
{
	public SpoonTreeDelegate(Spoon spoon)
	{
		super(spoon);
	}

	/**
	 * @return The object that is selected in the tree or null if we couldn't
	 *         figure it out. (titles etc. == null)
	 */
	public TreeSelection[] getTreeObjects(final Tree tree, Tree selectionTree, Tree coreObjectsTree)
	{
		List<TreeSelection> objects = new ArrayList<TreeSelection>();

		if (selectionTree!=null && !selectionTree.isDisposed() && tree.equals(selectionTree))
		{
			TreeItem[] selection = selectionTree.getSelection();
			for (int s = 0; s < selection.length; s++)
			{
				TreeItem treeItem = selection[s];
				String[] path = ConstUI.getTreeStrings(treeItem);

				TreeSelection object = null;

				switch (path.length)
				{
				case 0:
					break;
				case 1: // ------complete-----
					if (path[0].equals(Spoon.STRING_TRANSFORMATIONS)) // the top level Transformations entry
					{
						object = new TreeSelection(path[0], TransMeta.class);
					}
					if (path[0].equals(Spoon.STRING_JOBS)) // the top level Jobs entry
					{
						object = new TreeSelection(path[0], JobMeta.class);
					}
					break;

				case 2: // ------complete-----
					if (path[0].equals(Spoon.STRING_BUILDING_BLOCKS)) // the top level Transformations entry
					{
						if (path[1].equals(Spoon.STRING_TRANS_BASE))
						{
							object = new TreeSelection(path[1], PluginInterface.class);
						}
					}
					if (path[0].equals(Spoon.STRING_TRANSFORMATIONS)) // Transformation title
					{
						object = new TreeSelection(path[1], spoon.delegates.trans.getTransformation(path[1]));
					}
					if (path[0].equals(Spoon.STRING_JOBS)) // Jobs title
					{
						object = new TreeSelection(path[1], spoon.delegates.jobs.getJob(path[1]));
					}
					break;

				case 3: // ------complete-----
					if (path[0].equals(Spoon.STRING_TRANSFORMATIONS)) // Transformations title
					{
						TransMeta transMeta = spoon.delegates.trans.getTransformation(path[1]);
						if (path[2].equals(Spoon.STRING_CONNECTIONS))
							object = new TreeSelection(path[2], DatabaseMeta.class, transMeta);
						if (path[2].equals(Spoon.STRING_STEPS))
							object = new TreeSelection(path[2], StepMeta.class, transMeta);
						if (path[2].equals(Spoon.STRING_HOPS))
							object = new TreeSelection(path[2], TransHopMeta.class, transMeta);
						if (path[2].equals(Spoon.STRING_PARTITIONS))
							object = new TreeSelection(path[2], PartitionSchema.class, transMeta);
						if (path[2].equals(Spoon.STRING_SLAVES))
							object = new TreeSelection(path[2], SlaveServer.class, transMeta);
						if (path[2].equals(Spoon.STRING_CLUSTERS))
							object = new TreeSelection(path[2], ClusterSchema.class, transMeta);
					}
					if (path[0].equals(Spoon.STRING_JOBS)) // Jobs title
					{
						JobMeta jobMeta = spoon.delegates.jobs.getJob(path[1]);
						if (path[2].equals(Spoon.STRING_CONNECTIONS))
							object = new TreeSelection(path[2], DatabaseMeta.class, jobMeta);
						if (path[2].equals(Spoon.STRING_JOB_ENTRIES))
							object = new TreeSelection(path[2], JobEntryCopy.class, jobMeta);
						if (path[2].equals(Spoon.STRING_SLAVES))
							object = new TreeSelection(path[2], SlaveServer.class, jobMeta);
					}
					break;

				case 4: // ------complete-----
					if (path[0].equals(Spoon.STRING_TRANSFORMATIONS)) // The name of a transformation
					{
						TransMeta transMeta = spoon.delegates.trans.getTransformation(path[1]);
						if (transMeta!=null) {
							if (path[2].equals(Spoon.STRING_CONNECTIONS))
								object = new TreeSelection(path[3], transMeta.findDatabase(path[3]), transMeta);
							if (path[2].equals(Spoon.STRING_STEPS))
								object = new TreeSelection(path[3], transMeta.findStep(path[3]), transMeta);
							if (path[2].equals(Spoon.STRING_HOPS))
								object = new TreeSelection(path[3], transMeta.findTransHop(path[3]), transMeta);
							if (path[2].equals(Spoon.STRING_PARTITIONS))
								object = new TreeSelection(path[3], transMeta.findPartitionSchema(path[3]), transMeta);
							if (path[2].equals(Spoon.STRING_SLAVES))
								object = new TreeSelection(path[3], transMeta.findSlaveServer(path[3]), transMeta);
							if (path[2].equals(Spoon.STRING_CLUSTERS))
								object = new TreeSelection(path[3], transMeta.findClusterSchema(path[3]), transMeta);
						}
					}
					if (path[0].equals(Spoon.STRING_JOBS)) // The name of a job
					{
						JobMeta jobMeta = spoon.delegates.jobs.getJob(path[1]);
						if (jobMeta != null && path[2].equals(Spoon.STRING_CONNECTIONS))
							object = new TreeSelection(path[3], jobMeta.findDatabase(path[3]), jobMeta);
						if (jobMeta != null && path[2].equals(Spoon.STRING_JOB_ENTRIES))
							object = new TreeSelection(path[3], jobMeta.findJobEntry(path[3]), jobMeta);
						if (jobMeta != null && path[2].equals(Spoon.STRING_SLAVES))
							object = new TreeSelection(path[3], jobMeta.findSlaveServer(path[3]), jobMeta);
					}
					break;

				case 5:
					if (path[0].equals(Spoon.STRING_TRANSFORMATIONS)) // The name of a transformation
					{
						TransMeta transMeta = spoon.delegates.trans.getTransformation(path[1]);
						if (transMeta != null && path[2].equals(Spoon.STRING_CLUSTERS))
						{
							ClusterSchema clusterSchema = transMeta.findClusterSchema(path[3]);
							object = new TreeSelection(path[4], clusterSchema.findSlaveServer(path[4]),
									clusterSchema, transMeta);
						}
					}
					break;
				default:
					break;
				}

				if (object != null)
				{
					objects.add(object);
				}
			}
		}
		if (tree!=null && coreObjectsTree!=null && tree.equals(coreObjectsTree))
		{
			TreeItem[] selection = coreObjectsTree.getSelection();
			for (int s = 0; s < selection.length; s++)
			{
				TreeItem treeItem = selection[s];
				String[] path = ConstUI.getTreeStrings(treeItem);

				TreeSelection object = null;

				switch (path.length)
				{
				case 0:
					break;
				case 2: // Job entries
					if (spoon.showJob) {
						PluginRegistry registry = PluginRegistry.getInstance();
						Class<? extends PluginTypeInterface>pluginType = JobEntryPluginType.class;
						PluginInterface plugin = registry.findPluginWithName(pluginType, path[1]);
						
						// Retry for Start
						//
						if (plugin == null) {
							if (path[1].equals(JobMeta.STRING_SPECIAL_START)) {
								plugin = registry.findPluginWithId(pluginType, JobMeta.STRING_SPECIAL);
							}							
						}
						// Retry for Dummy
						//
						if (plugin==null) {
							if (path[1].equals(JobMeta.STRING_SPECIAL_DUMMY)) {
								plugin = registry.findPluginWithId(pluginType, JobMeta.STRING_SPECIAL);
							}	
						}									

						if (plugin!=null) {
							object = new TreeSelection(path[1], plugin);
						}
					}
					
					if (spoon.showTrans)
					{
						// Steps
						object = new TreeSelection(path[1], PluginRegistry.getInstance().findPluginWithName(StepPluginType.class, path[1]));
					}
					break;
				default:
					break;
				}

				if (object != null)
				{
					objects.add(object);
				}
			}
		}

		return objects.toArray(new TreeSelection[objects.size()]);
	}

	public void addDragSourceToTree(final Tree tree,final Tree selectionTree,final Tree coreObjectsTree)
	{
		// Drag & Drop for steps
		Transfer[] ttypes = new Transfer[] { XMLTransfer.getInstance() };

		DragSource ddSource = new DragSource(tree, DND.DROP_MOVE);
		ddSource.setTransfer(ttypes);
		ddSource.addDragListener(new DragSourceListener()
		{
			public void dragStart(DragSourceEvent event)
			{
				TreeSelection[] treeObjects = getTreeObjects(tree,selectionTree,coreObjectsTree);
				if (treeObjects.length == 0)
				{
					event.doit = false;
					return;
				}
				
				spoon.hideToolTips();
				
				TreeSelection treeObject = treeObjects[0];
				Object object = treeObject.getSelection();
				TransMeta transMeta = spoon.getActiveTransformation();
				// JobMeta jobMeta = spoon.getActiveJob();
				
				if (object instanceof StepMeta ||
					object instanceof PluginInterface ||
					( object instanceof DatabaseMeta && transMeta!=null) ||
					object instanceof TransHopMeta || 
					object instanceof JobEntryCopy
					)
				{
					event.doit = true;
				}
				else
				{
					event.doit = false;
				}
			}

			public void dragSetData(DragSourceEvent event)
			{
				TreeSelection[] treeObjects = getTreeObjects(tree,selectionTree,coreObjectsTree);
				if (treeObjects.length == 0)
				{
					event.doit = false;
					return;
				}

				int type = 0;
				String data = null;

				TreeSelection treeObject = treeObjects[0];
				Object object = treeObject.getSelection();

				if (object instanceof StepMeta)
				{
					StepMeta stepMeta = (StepMeta) object;
					type = DragAndDropContainer.TYPE_STEP;
					data = stepMeta.getName(); // name of the step.
				} else if (object instanceof PluginInterface)
				{
					PluginInterface plugin = (PluginInterface) object;
					if (plugin.getPluginType().equals(StepPluginType.getInstance())) {
						type = DragAndDropContainer.TYPE_BASE_STEP_TYPE;
						data = plugin.getName(); // Step type name
					} else {
						type = DragAndDropContainer.TYPE_BASE_JOB_ENTRY;
						data = plugin.getName(); // job entry type name
						if (treeObject.getItemText().equals(JobMeta.createStartEntry().getName())){
							data = treeObject.getItemText();
						} else if (treeObject.getItemText().equals(JobMeta.createDummyEntry().getName())) {
							data = treeObject.getItemText();
						}
					}
				} else if (object instanceof DatabaseMeta)
				{
					DatabaseMeta databaseMeta = (DatabaseMeta) object;
					type = DragAndDropContainer.TYPE_DATABASE_CONNECTION;
					data = databaseMeta.getName();
				} else if (object instanceof TransHopMeta)
				{
					TransHopMeta hop = (TransHopMeta) object;
					type = DragAndDropContainer.TYPE_TRANS_HOP;
					data = hop.toString(); // nothing for really ;-)
				} else if (object instanceof JobEntryCopy)
				{
					JobEntryCopy jobEntryCopy = (JobEntryCopy) object;
					type = DragAndDropContainer.TYPE_JOB_ENTRY;
					data = jobEntryCopy.getName(); // name of the job entry.
				} /*else if (object instanceof Class<?> && object.equals(JobPlugin.class))
				{
				}*/ else
				{
					event.doit = false;
					return; // ignore anything else you drag.
				}

				event.data = new DragAndDropContainer(type, data);
			}

			public void dragFinished(DragSourceEvent event)
			{
			}
		});

	}

}
