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

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

public class TreeUtil
{
    public static final void setOptimalWidthOnColumns(Tree tree)
    {
        int nrCols = tree.getColumnCount();
        int[] max = new int[nrCols];
        Image image = new Image(tree.getDisplay(), 10, 10);
        GC gc = new GC(image);
        
        for (int i=0;i<max.length;i++)
        {
            TreeColumn treeColumn = tree.getColumn(i); 
            Point point = gc.textExtent(treeColumn.getText());
            max[i] = point.x;
        }
        
        getMaxWidths(tree.getItems(), max, gc);
        
        gc.dispose();
        image.dispose();
        
        for (int i=0;i<max.length;i++)
        {
            TreeColumn treeColumn = tree.getColumn(i); 
            treeColumn.setWidth(max[i]+30);
        }
    }

    private static final void getMaxWidths(TreeItem[] items, int[] max, GC gc)
    {
        for (int i=0;i<items.length;i++)
        {
            for (int c=0;c<max.length;c++)
            {
                String string = items[i].getText(c);
                Point point = gc.textExtent(string);
                if (point.x>max[c]) max[c]=point.x;
            }
            getMaxWidths(items[i].getItems(), max, gc);
        }
    }
    
    public static final TreeItem findTreeItem(Tree tree, String[] path)
    {
        TreeItem[] items = tree.getItems();
        for (int i=0;i<items.length;i++)
        {
            TreeItem treeItem = findTreeItem(items[i], path, 0);
            if (treeItem!=null) return treeItem;
        }
        return null;
    }
    
    private static final TreeItem findTreeItem(TreeItem treeItem, String[] path, int level)
    {
        if (treeItem.getText().equals(path[level]))
        {
            if (level==path.length-1) return treeItem;
            
            TreeItem[] items = treeItem.getItems();
            for (int i=0;i<items.length;i++)
            {
                TreeItem found = findTreeItem(items[i], path, level+1);
                if (found!=null) return found;
            }
        }
        return null;
    }
}
