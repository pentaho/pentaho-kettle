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

 

package org.pentaho.di.core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;

import org.pentaho.di.core.exception.KettleEOFException;
import org.pentaho.di.core.exception.KettleFileException;

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
	private String dbname;
	private String sql;
	private int hashCode;
	
	public DBCacheEntry(String dbname, String sql)
	{
		this.dbname = dbname;
		this.sql=sql;
	}

	public DBCacheEntry()
	{
		this(null, null);
	}

	public boolean sameDB(String otherDb)
	{
		// ESCA-JAVA0071:
		if (dbname == otherDb)
		{
			// String comparison is actually ok here!!! This will check whether the strings
			// are really the same string object. If they're not the same String object, but they
			// contain the same value this will be catched later on in this method.
			//
			// This is supposed to be an optimization (not by me). Sven Boden
			
			return true; // short-circuit object equivalence, treat nulls as
                         // equal
		}
		if (null != dbname)
		{
			return dbname.equalsIgnoreCase(otherDb);
		}
		return false;
	}
	
	public int hashCode()
	{
		if ((0 >= hashCode) && (null != dbname) && (null != sql)) {
		     hashCode = dbname.toLowerCase().hashCode() ^ sql.toLowerCase().hashCode();
		}
		return hashCode;
	}
	
	public boolean equals(Object o)
	{
		if ((null != o) && (o instanceof DBCacheEntry)) {
		    DBCacheEntry obj = (DBCacheEntry)o;
		
		    boolean retval = dbname.equalsIgnoreCase(obj.dbname) && sql.equalsIgnoreCase(obj.sql); 
		
		    return retval;
		}
		return false;
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
