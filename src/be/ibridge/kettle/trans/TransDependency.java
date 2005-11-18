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
package be.ibridge.kettle.trans;
import java.util.ArrayList;

import org.w3c.dom.Node;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.XMLInterface;
import be.ibridge.kettle.core.database.DatabaseMeta;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.repository.Repository;


/*
 * Created on 5-apr-2004
 *
 */

public class TransDependency implements XMLInterface
{
	private DatabaseMeta db;
	private String tablename;
	private String fieldname;
	
	private long id;
	
	public TransDependency(DatabaseMeta db, String tablename, String fieldname)
	{
		this.db = db;
		this.tablename = tablename;
		this.fieldname = fieldname;
	}

	public TransDependency()
	{
		this(null, null, null);
	}

	public TransDependency(Node depnode, ArrayList databases)
		throws KettleXMLException
	{
		try
		{
			String depcon    = XMLHandler.getTagValue(depnode, "connection");
			db = Const.findDatabase(databases, depcon);
			tablename        = XMLHandler.getTagValue(depnode, "table");
			fieldname        = XMLHandler.getTagValue(depnode, "field");
		}
		catch(Exception e)
		{
			throw new KettleXMLException("Unable to load transformation dependency from XML", e);
		}
	}

	public void setID(long id)
	{
		this.id = id;
	}
	
	public long getID()
	{
		return id;
	}
	
	public void setDatabase(DatabaseMeta db)
	{
		this.db = db;
	}
	
	public DatabaseMeta getDatabase()
	{
		return db;
	}
	
	public void setTablename(String tablename)
	{
		this.tablename = tablename;
	}
	
	public String getTablename()
	{
		return tablename;
	}
	
	public void setFieldname(String fieldname)
	{
		this.fieldname = fieldname;
	}
	
	public String getFieldname()
	{
		return fieldname;
	}
	
	public String getXML()
	{
		String retval = "";
		retval+="      <dependency>"+Const.CR;
		retval+="        "+XMLHandler.addTagValue("connection", db==null?"":db.getName());
		retval+="        "+XMLHandler.addTagValue("table",      tablename);
		retval+="        "+XMLHandler.addTagValue("field",      fieldname);
		retval+="        </dependency>"+Const.CR;
		
		return retval;
	}
	
	public TransDependency(Repository rep, long id_dependency, ArrayList databases)
		throws KettleException
	{
		try
		{
			setID(id_dependency);
			
			Row r = rep.getTransDependency(id_dependency);
			
			if (r!=null)
			{
				long id_connection = r.searchValue("ID_DATABASE").getInteger();
				db        = Const.findDatabase(databases, id_connection);
				tablename = r.searchValue("TABLE_NAME").getString();
				fieldname = r.searchValue("FIELD_NAME").getString();
			}
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to load transformation dependency from the repository with id_dependency="+id_dependency, dbe);
		}
	}

	public void saveRep(Repository rep, long id_transformation)
		throws KettleException
	{
		try
		{
			long id_database = db==null?-1:db.getID();
			
			setID( rep.insertDependency(id_transformation, id_database, tablename, fieldname) );
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to save transformation dependency to the repository for id_transformation="+id_transformation, dbe);
		}
	}

}
