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
/**
 * 
 */
package org.pentaho.di.ui.core.widget;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.pentaho.di.ui.core.ConstUI;
import org.pentaho.di.ui.core.widget.DoubleClickInterface;

/**
 * This class can be used to define accelerators (actions) to a tree item that just got created.
 * @author Matt
 *
 */
public class TreeItemAccelerator
{
    public static final void addDoubleClick(final TreeItem treeItem, final DoubleClickInterface doubleClick)
    {
        final String[] path1 = ConstUI.getTreeStrings(treeItem);
        final Tree tree = treeItem.getParent();
        
        if (doubleClick!=null)
        {
            final SelectionAdapter selectionAdapter = new SelectionAdapter()
            {
                public void widgetDefaultSelected(SelectionEvent selectionEvent)
                {
                    TreeItem[] items = tree.getSelection();
                    for (int i=0;i<items.length;i++)
                    {
                        String[] path2 = ConstUI.getTreeStrings(items[i]);
                        if (equalPaths(path1, path2)) doubleClick.action(treeItem);
                    }
                }
            };
            tree.addSelectionListener(selectionAdapter);
            
            // Clean up when we do a refresh too.
            treeItem.addDisposeListener(new DisposeListener()
                {
                    public void widgetDisposed(DisposeEvent disposeEvent)
                    {
                        tree.removeSelectionListener(selectionAdapter);
                    }
                }
            );
        }
    }
    
    public static final boolean equalPaths(String[] path1, String[] path2)
    {
        if (path1==null || path2==null) return false;
        if (path1.length != path2.length) return false;
        
        for (int i=0;i<path1.length;i++) if (!path1[i].equals(path2[i])) return false;
        return true;
    }
}
