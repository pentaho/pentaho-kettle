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
package org.pentaho.di.ui.spoon;

public class TreeSelection
{
    private Object selection;

    private Object parent;

    private Object grandParent;

    private String itemText;

    /**
     * @param selection
     * @param parent
     * @param grandParent
     */
    public TreeSelection(String itemText, Object selection, Object parent, Object grandParent)
    {
        this.itemText = itemText;
        this.selection = selection;
        this.parent = parent;
        this.grandParent = grandParent;
    }

    /**
     * @param selection
     * @param parent
     */
    public TreeSelection(String itemText, Object selection, Object parent)
    {
        this(itemText, selection, parent, null);
    }

    /**
     * @param selection
     */
    public TreeSelection(String itemText, Object selection)
    {
        this(itemText, selection, null, null);
    }

    /**
     * @return the grandParent
     */
    public Object getGrandParent()
    {
        return grandParent;
    }

    /**
     * @param grandParent the grandParent to set
     */
    public void setGrandParent(Object grandParent)
    {
        this.grandParent = grandParent;
    }

    /**
     * @return the parent
     */
    public Object getParent()
    {
        return parent;
    }

    /**
     * @param parent the parent to set
     */
    public void setParent(Object parent)
    {
        this.parent = parent;
    }

    /**
     * @return the selection
     */
    public Object getSelection()
    {
        return selection;
    }

    /**
     * @param selection the selection to set
     */
    public void setSelection(Object selection)
    {
        this.selection = selection;
    }

    /**
     * @return the description
     */
    public String getItemText()
    {
        return itemText;
    }

    /**
     * @param description the description to set
     */
    public void setItemText(String description)
    {
        this.itemText = description;
    }

}
