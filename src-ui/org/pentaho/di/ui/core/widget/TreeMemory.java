/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.ui.core.widget;

import java.util.Hashtable;
import java.util.Map;

import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.pentaho.di.ui.core.ConstUI;

/**
 * This singleton class remembers whether or not a TreeItem is expanded.
 * When the tree is refreshed, it remembers, making for a better user experience.
 * 
 * @author Matt
 *
 */
public class TreeMemory
{
    private static TreeMemory treeMemory;
    
    private Map<TreeMemoryEntry,Boolean> map;
    
    public static final TreeMemory getInstance()
    {
        if (treeMemory!=null) return treeMemory;
        
        treeMemory = new TreeMemory();
        
        return treeMemory;
    }
    
    private TreeMemory()
    {
        map = new Hashtable<TreeMemoryEntry,Boolean>(100);
    }
    
    private class TreeMemoryEntry
    {
        private String treeName;
        private String[] path;

        TreeMemoryEntry(String treeName, String[] path)
        {
            this.path = path;
            this.treeName = treeName;
        }
        
        public int hashCode()
        {
            int code = treeName.hashCode();
            for (String p : path) {
            	code ^= p.hashCode();
            }
            return code;
        }
        
        public boolean equals(Object obj)
        {
            TreeMemoryEntry entry = (TreeMemoryEntry) obj;
            if (!entry.treeName.equals(treeName)) return false;
            if (entry.path.length!=path.length) return false;
            for (int i=0;i<path.length;i++)
            {
                if (!path[i].equals(entry.path[i])) return false;
            }
            return true;
        }
        
        public String toString() {
        	StringBuffer string = new StringBuffer(50);
        	string.append("{");
        	for (int i=0;i<path.length;i++) {
        		if (i>0) string.append("|");
        		string.append(path[i]);
        	}
        	string.append(":").append(treeName).append("}");
        	return string.toString();
        }
    }

    public void storeExpanded(String treeName, TreeItem treeItem, boolean expanded)
    {
        String[] path = ConstUI.getTreeStrings(treeItem);
        storeExpanded(treeName, path, expanded);
    }

    public void storeExpanded(String treeName, String[] path, boolean expanded)
    {
    	TreeMemoryEntry key = new TreeMemoryEntry(treeName, path);  // key.hashCode();
        if (expanded)
        {
            map.put(key, Boolean.valueOf(expanded));
        }
        else
        {
            map.remove(key);
        }
    }
    
    public boolean isExpanded(String treeName, String[] path)
    {
    	TreeMemoryEntry key = new TreeMemoryEntry(treeName, path);
    	/*
    	// key.hashCode()
    	Set<TreeMemoryEntry> keySet = map.keySet();
    	for (Iterator<TreeMemoryEntry> iterator = keySet.iterator(); iterator.hasNext();) {
			TreeMemoryEntry entry = iterator.next();
			System.out.println("Entry: "+entry.toString()+", hashCode="+entry.hashCode());
			if (key.equals(entry)) {
				System.out.println("FOUND!!! Entry: "+entry.toString()+", hashCode="+entry.hashCode());
			}
		}
    	*/
    	
    	Boolean expanded = map.get(key);
        if (expanded!=null)
        {
            return expanded.booleanValue();
        }
        else
        {
            return false;
        }
    }
    
    public void clear()
    {
        map.clear();
    }

    /**
     * This method creates, adds and returns a tree listener that will keep track of the expanded/collapsed state of the TreeItems.
     * This state will then be stored in the TreeMemory singleton.
     * 
     * @param tree The tree to add the listener to
     * @return The created/added TreeListener
     */
    public static final TreeListener addTreeListener(final Tree tree, final String treeName)
    {
        TreeListener treeListener = new TreeListener()
        {        
            public void treeExpanded(TreeEvent e)
            {
                TreeItem treeItem = (TreeItem) e.item;
                String[] path = ConstUI.getTreeStrings(treeItem);
                TreeMemory treeMemory = TreeMemory.getInstance();
                treeMemory.storeExpanded(treeName, path, true);
            }
        
            public void treeCollapsed(TreeEvent e)
            {
                TreeItem treeItem = (TreeItem) e.item;
                String[] path = ConstUI.getTreeStrings(treeItem);
                TreeMemory treeMemory = TreeMemory.getInstance();
                treeMemory.storeExpanded(treeName, path, false);
            }
        
        };
        tree.addTreeListener(treeListener);
        return treeListener;
    }

    /**
     * Expand of collapse all TreeItems in the complete tree based on the values stored in memory.
     *  
     * @param tree The tree to format.
     */
    public static void setExpandedFromMemory(Tree tree, String treeName)
    {
        TreeItem[] items = tree.getItems();
        for (int i=0;i<items.length;i++)
        {
            setExpandedFromMemory(tree, treeName, items[i]);
        }
    }
    
    private static void setExpandedFromMemory(Tree tree, String treeName, TreeItem treeItem)
    {
        TreeMemory treeMemory = TreeMemory.getInstance();
        
        String[] path = ConstUI.getTreeStrings(treeItem);
        boolean expanded = treeMemory.isExpanded(treeName, path);
        treeItem.setExpanded(expanded);
        
        TreeItem[] items = treeItem.getItems();
        for (int i=0;i<items.length;i++)
        {
            setExpandedFromMemory(tree, treeName, items[i]);
        }
    }
}
