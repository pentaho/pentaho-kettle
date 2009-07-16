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
	public enum ObjectType { TRANSFORMATION_GRAPH, JOB_GRAPH, SLAVE_SERVER, BROWSER, };
	
    private TabItem tabItem;
    
    private String objectName;
    
    private String versionLabel;

    private TabItemInterface object;
    
    private ObjectType objectType;

    /**
     * @param tabName
     * @param objectName
     * @param objectType
     * @param object
     */
    public TabMapEntry(TabItem tabItem, String objectName, String versionLabel, TabItemInterface object, ObjectType objectType)
    {
        this.tabItem = tabItem;
        this.objectName = objectName;
        this.versionLabel = versionLabel;
        this.object = object;
        this.objectType = objectType;
    }
    
    public boolean equals(Object obj) {
    	TabMapEntry entry = (TabMapEntry) obj;
    	
    	boolean sameVersion = (versionLabel==null && entry.versionLabel==null) || (versionLabel!=null && versionLabel.equals(versionLabel)) ;
    	
    	return objectType.equals(entry.objectType) && objectName.equals(entry.objectName) && sameVersion;
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

    public String getVersionLabel() {
		return versionLabel;
	}
    
    public void setVersionLabel(String versionLabel) {
		this.versionLabel = versionLabel;
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
    public ObjectType getObjectType()
    {
        return objectType;
    }

    /**
     * @param objectType the objectType to set
     */
    public void setObjectType(ObjectType objectType)
    {
        this.objectType = objectType;
    }
}
