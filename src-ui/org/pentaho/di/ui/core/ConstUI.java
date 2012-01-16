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

package org.pentaho.di.ui.core;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TreeItem;
import org.pentaho.di.core.Const;

/**
 * This class is used to define a number of default values for various settings throughout Kettle.
 * It also contains a number of static final methods to make your life easier.
 *
 * @author Matt 
 * @since 07-05-2003
 *
 */
public class ConstUI
{

	/**
	 * Default font name for the fixed width font
	 */
	public static final String FONT_FIXED_NAME = "Courier";

	/**
	 * Default font size for the fixed width font
	 */
	public static final int FONT_FIXED_SIZE = 9;

	/**
	 * Default font type for the fixed width font
	 */
	public static final int FONT_FIXED_TYPE = SWT.NORMAL;

	/**
	 * Default icon size
	 */
	public static final int ICON_SIZE = 32;

	/**
	 * Default line width for arrows & around icons
	 */
	public static final int LINE_WIDTH = 1;

	/**
	 * Default grid size to which the graphical views snap.
	 */
	public static final int GRID_SIZE = 20;

	/**
	 * The minimal size of a note on a graphical view (width & height)
	 */
	public static final int NOTE_MIN_SIZE = 20;
	
	/**
	 * Offset between pointer and tooltip position.
	 */
	public static final int TOOLTIP_OFFSET = 5;

	/**
	 * The default red-component of the background color
	 */
	public static final int COLOR_BACKGROUND_RED = 255;

	/**
	 * The default green-component of the background color
	 */
	public static final int COLOR_BACKGROUND_GREEN = 255;

	/**
	 * The default blue-component of the background color
	 */
	public static final int COLOR_BACKGROUND_BLUE = 255;

	/**
	 * The default red-component of the graph background color
	 */
	public static final int COLOR_GRAPH_RED = 255;

	/**
	 * The default green-component of the graph background color
	 */
	public static final int COLOR_GRAPH_GREEN = 255;

	/**
	 * The default blue-component of the graph background color
	 */
	public static final int COLOR_GRAPH_BLUE = 255;

	/**
	 * The default red-component of the tab selected color
	 */
	public static final int COLOR_TAB_RED = 200;

	/**
	 * The default green-component of the tab selected color
	 */
	public static final int COLOR_TAB_GREEN = 200;

	/**
	 * The default blue-component of the tab selected color
	 */
	public static final int COLOR_TAB_BLUE = 255;

	
	/**
	 * the default canvas refresh interval for running transformations
	 */
	public static final int INTERVAL_MS_TRANS_CANVAS_REFRESH = 1000;
	
	/**
	 * Determine the level of where the TreeItem is position in a tree.
	 * @param ti The TreeItem
	 * @return The level of the item in the tree
	 */
	public static final int getTreeLevel(TreeItem ti)
	{
		int level = 0;
		TreeItem parent = ti.getParentItem();
		while (parent != null)
		{
			level++;
			parent = parent.getParentItem();
		}

		return level;
	}

	/**
	 * Get an array of strings containing the path from the given TreeItem to the parent.
	 * @param ti The TreeItem to look at
	 * @return An array of string describing the path to the TreeItem.
	 */
	public static final String[] getTreeStrings(TreeItem ti)
	{
		int nrlevels = getTreeLevel(ti) + 1;
		String retval[] = new String[nrlevels];
		int level = 0;

		retval[nrlevels - 1] = ti.getText();
		TreeItem parent = ti.getParentItem();
		while (parent != null)
		{
			level++;
			retval[nrlevels - level - 1] = parent.getText();
			parent = parent.getParentItem();
		}

		return retval;
	}

	/**
	 * Return the tree path seperated by Const.FILE_SEPARATOR, starting from a certain depth in the tree.
	 *
	 * @param ti The TreeItem to get the path for 
	 * @param from The depth to start at, use 0 to get the complete tree.
	 * @return The tree path.
	 */
	public static final String getTreePath(TreeItem ti, int from)
	{
		String path[] = getTreeStrings(ti);

		if (path == null)
			return null;

		String retval = "";

		for (int i = from; i < path.length; i++)
		{
			if (!path[i].equalsIgnoreCase(Const.FILE_SEPARATOR))
			{
				retval += Const.FILE_SEPARATOR + path[i];
			}
		}

		return retval;
	}

	/**
	 * Flips the TreeItem from expanded to not expanded or vice-versa.
	 * @param ti The TreeItem to flip.
	 */
	public static final void flipExpanded(TreeItem ti)
	{
		ti.setExpanded(!ti.getExpanded());
	}

    public static final TreeItem findTreeItem(TreeItem parent, String name)
    {
        return findTreeItem(parent, null, name);
    }
    
	/**
	 * Finds a TreeItem with a certain label (name) in a (part of a) tree.
	 * @param parent The TreeItem where we start looking.
     * @param parentName The name of the parent to match as well (null=not used)
	 * @param name The name or item label to look for.
	 * @return The TreeItem if the label was found, null if nothing was found.
	 */
	public static final TreeItem findTreeItem(TreeItem parent, String parentName, String name)
	{
		return findTreeItem(null, parent, parentName, name);
	}
    
    private static final TreeItem findTreeItem(TreeItem grandParent, TreeItem parent, String parentName, String name)
    {
        if (Const.isEmpty(parentName))
        {
            if (parent.getText().equalsIgnoreCase(name))
            {
                return parent;
            }
        }
        else
        {
            if (grandParent!=null && grandParent.getText().equalsIgnoreCase("OTHER"))
            {
                System.out.println("Other");
            }
            if (grandParent!=null && grandParent.getText().equalsIgnoreCase(parentName) &&
                parent.getText().equalsIgnoreCase(name))
            {
                return parent;
            }
        }

        TreeItem ti[] = parent.getItems();
        for (int i = 0; i < ti.length; i++)
        {
            TreeItem child = findTreeItem(parent, ti[i], parentName, name);
            if (child != null)
            {
                return child;
            }
        }
        return null;
    }

    public static void displayMenu(Menu menu, Control control) {
        Menu oldMenu = control.getMenu();
        if (oldMenu != null && oldMenu != menu) {
            oldMenu.setVisible(false);
        }

        //XXX: Stubbing out this line prevents context dialogs from appearing twice
        // on OS X.  Tested on Windows to be sure there is no adverse effect.
        // Unfortunately, I do *not* understand why this works.  I ran it by
        // mcasters and he didn't know for sure either.
        //control.setMenu(menu);
        menu.setVisible(true);
    }

}
