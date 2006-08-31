package be.ibridge.kettle.core.widget;

import java.util.Hashtable;
import java.util.Map;

import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import be.ibridge.kettle.core.Const;

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
    
    private Map map;
    
    public static final TreeMemory getInstance()
    {
        if (treeMemory!=null) return treeMemory;
        
        treeMemory = new TreeMemory();
        
        return treeMemory;
    }
    
    private TreeMemory()
    {
        map = new Hashtable(5000);
    }
    
    private class TreeMemoryEntry
    {
        private Tree tree;
        private String[] path;

        TreeMemoryEntry(Tree tree, String[] path)
        {
            this.tree = tree;
            this.path = path;
        }
        
        public int hashCode()
        {
            return path.hashCode() ^ tree.hashCode();
        }
        
        public boolean equals(Object obj)
        {
            TreeMemoryEntry entry = (TreeMemoryEntry) obj;
            if (!entry.tree.equals(tree)) return false;
            if (entry.path.length!=path.length) return false;
            for (int i=0;i<path.length;i++)
            {
                if (!path[i].equals(entry.path[i])) return false;
            }
            return true;
        }
    }
    
    public void storeExpanded(Tree tree, String[] path, boolean expanded)
    {
        map.put(new TreeMemoryEntry(tree, path), new Boolean(expanded));
    }
    
    public boolean isExpanded(Tree tree, String[] path)
    {
        Boolean expanded = (Boolean) map.get(new TreeMemoryEntry(tree, path));
        return expanded.booleanValue();
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
    public static final TreeListener addTreeListener(final Tree tree)
    {
        TreeListener treeListener = new TreeListener()
        {        
            public void treeExpanded(TreeEvent e)
            {
                TreeItem treeItem = (TreeItem) e.item;
                String[] path = Const.getTreeStrings(treeItem);
                TreeMemory treeMemory = TreeMemory.getInstance();
                treeMemory.storeExpanded(tree, path, true);
            }
        
            public void treeCollapsed(TreeEvent e)
            {
                TreeItem treeItem = (TreeItem) e.item;
                String[] path = Const.getTreeStrings(treeItem);
                TreeMemory treeMemory = TreeMemory.getInstance();
                treeMemory.storeExpanded(tree, path, false);
            }
        
        };
        tree.addTreeListener(treeListener);
        return treeListener;
    }
}
