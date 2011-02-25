/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
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
package org.pentaho.di.repository.filerep;

import java.util.List;

import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.BaseRepositoryMeta;
import org.pentaho.di.repository.RepositoryCapabilities;
import org.pentaho.di.repository.RepositoryMeta;
import org.w3c.dom.Node;

public class KettleFileRepositoryMeta extends BaseRepositoryMeta implements RepositoryMeta {

	public static String REPOSITORY_TYPE_ID = "KettleFileRepository";

	private String baseDirectory;
	private boolean readOnly;
	private boolean hidingHiddenFiles;

	public KettleFileRepositoryMeta() {
		super(REPOSITORY_TYPE_ID);
	}
	
	public KettleFileRepositoryMeta(String id, String name, String description, String baseDirectory) {
		super(id, name, description);
		this.baseDirectory = baseDirectory;
	}
	
    public RepositoryCapabilities getRepositoryCapabilities() {
    	return new RepositoryCapabilities() {
    		public boolean supportsUsers() { return false; }
    		public boolean managesUsers() { return false; }
    		public boolean isReadOnly() { return readOnly; }
    		public boolean supportsRevisions() { return false; }
    		public boolean supportsMetadata() { return false; }
    		public boolean supportsLocking() { return false; }
    		public boolean hasVersionRegistry() { return false; }
    		public boolean supportsAcls() { return false; }    		
    	};
    }

	public String getXML()
	{
        StringBuffer retval = new StringBuffer(100);
		
		retval.append("  ").append(XMLHandler.openTag(XML_TAG));
		retval.append(super.getXML());
		retval.append("    ").append(XMLHandler.addTagValue("base_directory", baseDirectory));
		retval.append("    ").append(XMLHandler.addTagValue("read_only", readOnly));
        retval.append("    ").append(XMLHandler.addTagValue("hides_hidden_files", hidingHiddenFiles));
		retval.append("  ").append(XMLHandler.closeTag(XML_TAG));
        
		return retval.toString();
	}

	public void loadXML(Node repnode, List<DatabaseMeta> databases) throws KettleException
	{
		super.loadXML(repnode, databases);
		try
		{
			baseDirectory = XMLHandler.getTagValue(repnode, "base_directory") ;
			readOnly = "Y".equalsIgnoreCase(XMLHandler.getTagValue(repnode, "read_only"));
            hidingHiddenFiles = "Y".equalsIgnoreCase(XMLHandler.getTagValue(repnode, "hides_hidden_files"));
		}
		catch(Exception e)
		{
			throw new KettleException("Unable to load Kettle file repository meta object", e);
		}
	}

	/**
	 * @return the baseDirectory
	 */
	public String getBaseDirectory() {
		return baseDirectory;
	}

	/**
	 * @param baseDirectory the baseDirectory to set
	 */
	public void setBaseDirectory(String baseDirectory) {
		this.baseDirectory = baseDirectory;
	}

	/**
	 * @return the readOnly
	 */
	public boolean isReadOnly() {
		return readOnly;
	}

	/**
	 * @param readOnly the readOnly to set
	 */
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}
	

  public RepositoryMeta clone(){
    return  new KettleFileRepositoryMeta(REPOSITORY_TYPE_ID, getName(), getDescription(), getBaseDirectory());
  }

  /**
   * @return the hidingHiddenFiles
   */
  public boolean isHidingHiddenFiles() {
    return hidingHiddenFiles;
  }

  /**
   * @param hidingHiddenFiles the hidingHiddenFiles to set
   */
  public void setHidingHiddenFiles(boolean hidingHiddenFiles) {
    this.hidingHiddenFiles = hidingHiddenFiles;
  }
}
