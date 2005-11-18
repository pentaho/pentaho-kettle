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
 
package be.ibridge.kettle.repository;

import java.util.ArrayList;

import org.w3c.dom.Node;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.database.DatabaseMeta;



/*
 * Created on 31-mrt-2004
 *
 */

public class RepositoryMeta 
{
	private String       name;
	private String       description;
	private DatabaseMeta connection;
	
	private boolean      lock;

	public RepositoryMeta(String name, String description, DatabaseMeta connection)
	{
		this.name = name;
		this.description = description;
		this.connection = connection;
		
		lock = false;
	}

	public RepositoryMeta()
	{
		this.name        = "";
		this.description = "";
		this.connection  = null;
	}

	public boolean loadXML(Node repnode, ArrayList databases)
	{
		try
		{
			name        = XMLHandler.getTagValue(repnode, "name") ;
			description = XMLHandler.getTagValue(repnode, "description") ;
			String conn = XMLHandler.getTagValue(repnode, "connection") ;
			connection  = Const.findDatabase(databases, conn);
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
		this.connection = connection;
	}
	
	public DatabaseMeta getConnection()
	{
		return connection;
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
		String retval="";
		
		retval+="  <repository>"+Const.CR;
		retval+="    "+XMLHandler.addTagValue("name",        name);
		retval+="    "+XMLHandler.addTagValue("description", description);
		retval+="    "+XMLHandler.addTagValue("connection",  connection!=null?connection.getName():null);
		retval+="    </repository>"+Const.CR;
		return retval;
	}
}
