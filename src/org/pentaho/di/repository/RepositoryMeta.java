 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/
 
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