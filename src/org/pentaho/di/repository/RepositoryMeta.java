 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/
 
package org.pentaho.di.repository;

import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.xml.XMLHandler;
import org.w3c.dom.Node;


/*
 * Created on 31-mar-2004
 */
public class RepositoryMeta 
{
	private String       name;
	private String       description;
	private DatabaseMeta databaseMeta;
	
	private boolean      lock;

	public RepositoryMeta(String name, String description, DatabaseMeta connection)
	{
		this.name = name;
		this.description = description;
		this.databaseMeta = connection;
		
		lock = false;
	}

	public RepositoryMeta()
	{
		this.name        = "";
		this.description = "";
		this.databaseMeta  = null;
	}

	public boolean loadXML(Node repnode, List<DatabaseMeta> databases)
	{
		try
		{
			name        = XMLHandler.getTagValue(repnode, "name") ;
			description = XMLHandler.getTagValue(repnode, "description") ;
			String conn = XMLHandler.getTagValue(repnode, "connection") ;
			databaseMeta  = DatabaseMeta.findDatabase(databases, conn);
			return true;
		}
		catch(Exception e)
		{
			return false;
		}
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setDescription(String description)
	{
		this.description = description;
	}
	
	public String getDescription()
	{
		return description;
	}
	
	public void setConnection(DatabaseMeta connection)
	{
		this.databaseMeta = connection;
	}
	
	public DatabaseMeta getConnection()
	{
		return databaseMeta;
	}
	
	public boolean isLocked()
	{
		return lock;
	}
	
	public void setLock(boolean lock)
	{
		this.lock = lock;
	}
	
	public String getXML()
	{
        StringBuffer retval = new StringBuffer(100);
		
		retval.append("  <repository>").append(Const.CR);
		retval.append("    ").append(XMLHandler.addTagValue("name",        name));
		retval.append("    ").append(XMLHandler.addTagValue("description", description));
		retval.append("    ").append(XMLHandler.addTagValue("connection",  databaseMeta!=null?databaseMeta.getName():null));
		retval.append("  </repository>").append(Const.CR);
        
		return retval.toString();
	}
}