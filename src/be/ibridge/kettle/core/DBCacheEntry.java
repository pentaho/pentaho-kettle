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

 

package be.ibridge.kettle.core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;

import be.ibridge.kettle.core.exception.KettleEOFException;
import be.ibridge.kettle.core.exception.KettleFileException;

/**
 * This class represents a single entry in a database cache.
 * A single entry in this case usually means: a single SQL query.
 * 
 * @author Matt
 * @since 15-01-04
 *
 */
public class DBCacheEntry
{
	public String dbname;
	public String sql;
	
	public DBCacheEntry(String dbname, String sql)
	{
		this.dbname = dbname;
		this.sql=sql;
	}

	public DBCacheEntry()
	{
		this(null, null);
	}

	public int hashCode()
	{
		int hashcode = dbname.hashCode() ^ sql.hashCode();
		 
		return hashcode;
	}
	
	public boolean equals(Object o)
	{
		DBCacheEntry obj = (DBCacheEntry)o;
		
		boolean retval = dbname.equalsIgnoreCase(obj.dbname) && sql.equalsIgnoreCase(obj.sql); 
		
		return retval;
	}
	
	/**
	 * Read the data for this Cache entry from a data input stream
	 * @param dis The DataInputStream to read this entry from.
	 * @throws KettleFileException if the cache can't be read from disk when it should be able to.  
	 * If the cache file doesn't exists, no exception is thrown 
	 */
	public DBCacheEntry(DataInputStream dis) throws KettleFileException
	{
		try
		{
			dbname  = dis.readUTF();
			sql     = dis.readUTF();
		}
		catch(EOFException eof)
		{
			throw new KettleEOFException("End of file reached", eof);
		}
		catch(Exception e)
		{
			throw new KettleFileException("Unable to read cache entry from data input stream", e);
		}
	}
	
	/**
	 * Write the data for this Cache entry to a data output stream
	 * @param dos The DataOutputStream to write this entry to.
	 * @return True if all went well, false if an error occured!
	 */
	public boolean write(DataOutputStream dos)
	{
		try
		{
			dos.writeUTF(dbname);
			dos.writeUTF(sql);
			
			return true;
		}
		catch(Exception e)
		{
			return false;
		}
	}
}
