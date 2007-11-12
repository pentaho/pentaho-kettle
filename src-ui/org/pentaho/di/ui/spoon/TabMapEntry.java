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

import org.pentaho.di.ui.spoon.TabItemInterface;
import org.pentaho.xul.swt.tab.TabItem;

public class TabMapEntry
{
    public static final int OBJECT_TYPE_TRANSFORMATION_GRAPH   = 1;
    public static final int OBJECT_TYPE_TRANSFORMATION_LOG     = 2;
    public static final int OBJECT_TYPE_TRANSFORMATION_HISTORY = 3;
    public static final int OBJECT_TYPE_JOB_GRAPH              = 4;
    public static final int OBJECT_TYPE_JOB_LOG                = 5;
    public static final int OBJECT_TYPE_JOB_HISTORY            = 6;
    public static final int OBJECT_TYPE_SLAVE_SERVER              = 7;
    public static final int OBJECT_TYPE_BROWSER                = 8;
    
    private TabItem tabItem;
    
    private String objectName;

    private TabItemInterface object;
    
    private int objectType;

    /**
     * @param tabName
     * @param objectName
     * @param objectType
     * @param object
     */
    public TabMapEntry(TabItem tabItem, String objectName, TabItemInterface object, int objectType)
    {
        this.tabItem = tabItem;
        this.objectName = objectName;
        this.object = object;
        this.objectType = objectType;
    }

    /**
     * @return the objectName
     */
    public String getObjectName()
    {
        return objectName;
    }

    /**
     * @param objectName the objectName to set
     */
    public void setObjectName(String objectName)
    {
        this.objectName = objectName;
    }

    /**
     * @return the object
     */
    public TabItemInterface getObject()
    {
        return object;
    }

    /**
     * @param object the object to set
     */
    public void setObject(TabItemInterface object)
    {
        this.object = object;
    }

    /**
     * @return the tabItem
     */
    public TabItem getTabItem()
    {
        return tabItem;
    }

    /**
     * @param tabItem the tabItem to set
     */
    public void setTabItem(TabItem tabItem)
    {
        this.tabItem = tabItem;
    }

    /**
     * @return the objectType
     */
    public int getObjectType()
    {
        return objectType;
    }

    /**
     * @param objectType the objectType to set
     */
    public void setObjectType(int objectType)
    {
        this.objectType = objectType;
    }
}
