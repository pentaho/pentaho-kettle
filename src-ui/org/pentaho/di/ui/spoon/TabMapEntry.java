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

import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.ui.spoon.TabItemInterface;
import org.pentaho.xul.swt.tab.TabItem;

public class TabMapEntry
{
	public enum ObjectType { TRANSFORMATION_GRAPH, JOB_GRAPH, SLAVE_SERVER, BROWSER, };
	
    private TabItem tabItem;
    
    private String filename;
    
    private String objectName;
    
    private RepositoryDirectory repositoryDirectory;
    
    private String versionLabel;

    private TabItemInterface object;
    
    private ObjectType objectType;

    /**
     * @param tabName
     * @param objectName
     * @param objectType
     * @param object
     */
    public TabMapEntry(TabItem tabItem, String filename, String objectName, RepositoryDirectory repositoryDirectory, String versionLabel, TabItemInterface object, ObjectType objectType)
    {
        this.tabItem = tabItem;
        this.filename = filename;
        this.objectName = objectName;
        this.repositoryDirectory = repositoryDirectory;
        this.versionLabel = versionLabel;
        this.object = object;
        this.objectType = objectType;
    }
    
    public boolean equals(Object obj) {
    	TabMapEntry entry = (TabMapEntry) obj;
    
    	boolean sameFile = (filename==null && entry.filename==null) || (filename!=null && filename.equals(entry.versionLabel));
    	boolean sameVersion = (versionLabel==null && entry.versionLabel==null) || (versionLabel!=null && versionLabel.equals(entry.versionLabel)) ;
    	boolean sameDirectory = (repositoryDirectory==null && entry.repositoryDirectory==null) || 
    		(repositoryDirectory!=null && entry.repositoryDirectory!=null && repositoryDirectory.getPath().equals(entry.repositoryDirectory.getPath()) );
    	return objectType.equals(entry.objectType) && objectName.equals(entry.objectName) && sameVersion && sameFile && sameDirectory;
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

	/**
	 * @return the filename
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * @param filename the filename to set
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}

	/**
	 * @return the repositoryDirectory
	 */
	public RepositoryDirectory getRepositoryDirectory() {
		return repositoryDirectory;
	}

	/**
	 * @param repositoryDirectory the repositoryDirectory to set
	 */
	public void setRepositoryDirectory(RepositoryDirectory repositoryDirectory) {
		this.repositoryDirectory = repositoryDirectory;
	}
}
