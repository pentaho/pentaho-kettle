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
