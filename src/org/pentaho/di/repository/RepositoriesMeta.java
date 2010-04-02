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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleRepositoryNotSupportedException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.RepositoryPluginType;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.kdr.KettleDatabaseRepositoryMeta;
import org.w3c.dom.Document;
import org.w3c.dom.Node;




//import java.util.ArrayList;

/*
 * Created on 31-mrt-2004
 *
 * This class contains information regarding the defined Kettle repositories 
 */

public class RepositoriesMeta 
{
	private List<DatabaseMeta>   databases;    // Repository connections
	private List<RepositoryMeta> repositories;   // List of repositories
	private LogChannel	log;

	public RepositoriesMeta()
	{
		clear();
	}
	
	@Deprecated
	public RepositoriesMeta(LogWriter log)
	{
		clear();
	}

	public void clear() {
		databases = new ArrayList<DatabaseMeta>();
		repositories = new ArrayList<RepositoryMeta>();
		log = new LogChannel("RepositoriesMeta");
	}		
	
	public void addDatabase(DatabaseMeta ci)
	{
		databases.add(ci);
	}
	public void addRepository(RepositoryMeta ri)
	{
		repositories.add(ri);
	}

	public void addDatabase(int p, DatabaseMeta ci)
	{
		databases.add(p, ci);
	}
	public void addRepository(int p, RepositoryMeta ri)
	{
		repositories.add(p, ri);
	}

	public DatabaseMeta getDatabase(int i)
	{
		return (DatabaseMeta)databases.get(i);
	}
	
	public RepositoryMeta getRepository(int i)
	{
		return repositories.get(i);
	}

	public void removeDatabase(int i)
	{
		if (i<0 || i>=databases.size()) return;
		databases.remove(i);
	}
	
	public void removeRepository(int i)
	{
		if (i<0 || i>=repositories.size()) return;
		repositories.remove(i);
	}

	public int nrDatabases()   { return databases.size(); }
	public int nrRepositories()  { return repositories.size(); }

	public DatabaseMeta searchDatabase(String name)
	{
		for (int i=0;i<nrDatabases();i++)
		{
			if (getDatabase(i).getName().equalsIgnoreCase(name)) return getDatabase(i);
		}
		return null;
	}

	public RepositoryMeta searchRepository(String name)
	{
		for (int i=0;i<nrRepositories();i++)
		{
            String repName=getRepository(i).getName();
			if (repName!=null && repName.equalsIgnoreCase(name)) return getRepository(i);
		}
		return null;
	}
	
	public int indexOfDatabase(DatabaseMeta di)
	{
		return databases.indexOf(di);
	}
	
	public int indexOfRepository(RepositoryMeta ri)
	{
		return repositories.indexOf(ri);
	}
	
	public RepositoryMeta findRepository(String name)
	{
		for (int i=0;i<nrRepositories();i++)
		{
			RepositoryMeta ri = getRepository(i);
			if (ri.getName().equalsIgnoreCase(name)) return ri;
		}
		return null;
	}
	
	// We read the repositories from the file:
	// 
	public boolean readData() throws KettleException
	{
		// Clear the information
		//
		clear();
		
		File file = new File(Const.getKettleLocalRepositoriesFile());
		if (!file.exists() || !file.isFile())
		{
			log.logDetailed("No repositories file found in the local directory: "+file.getAbsolutePath());
			file = new File(Const.getKettleUserRepositoriesFile());
			if (!file.exists() || !file.isFile())
			{
				return true; // nothing to read!
			}
		}
		
		log.logBasic("Reading repositories XML file: "+file.getAbsoluteFile());
		
		DocumentBuilderFactory dbf;
		DocumentBuilder db;
		Document doc;
		
		try
		{			
			// Check and open XML document
			dbf  = DocumentBuilderFactory.newInstance();
			db   = dbf.newDocumentBuilder();
			try
			{
				doc  = db.parse(file);
			}
			catch(FileNotFoundException ef)
			{
				InputStream is = getClass().getResourceAsStream("/org/pentaho/di/repository/repositories.xml");
				if (is!=null)
				{
					doc = db.parse(is);
				}
				else
				{
					throw new KettleException("Error opening file: "+file.getAbsoluteFile(), ef);
				}
			}
			
			// Get the <repositories> node:
			Node repsnode = XMLHandler.getSubNode(doc, "repositories");
		
			// Handle connections
			int nrconn = XMLHandler.countNodes(repsnode, "connection");
			log.logDebug("We have "+nrconn+" connections...");
			for (int i=0;i<nrconn;i++)
			{
				log.logDebug("Looking at connection #"+i);
				Node dbnode = XMLHandler.getSubNodeByNr(repsnode, "connection", i);
				DatabaseMeta dbcon = new DatabaseMeta(dbnode);
				addDatabase(dbcon);
				log.logDebug("Read connection : "+dbcon.getName());
			}

			// Handle repositories...
			int nrreps = XMLHandler.countNodes(repsnode, RepositoryMeta.XML_TAG);
			log.logDebug("We have "+nrreps+" repositories...");
			KettleException kettleException = null;
			for (int i=0;i<nrreps;i++)
			{
				Node repnode = XMLHandler.getSubNodeByNr(repsnode, RepositoryMeta.XML_TAG, i);
				log.logDebug("Looking at repository #"+i);
				
				String id = XMLHandler.getTagValue(repnode, "id");
				if (Const.isEmpty(id)) {
			    	// Backward compatibility : if the id is not defined, it's the database repository!
					//
		    		id=KettleDatabaseRepositoryMeta.REPOSITORY_TYPE_ID;
				}
				try {
  				RepositoryMeta repositoryMeta = PluginRegistry.getInstance().loadClass(RepositoryPluginType.class, id, RepositoryMeta.class);
  				if(repositoryMeta != null) {
    				repositoryMeta.loadXML(repnode, databases);
    				addRepository(repositoryMeta);
    				log.logDebug("Read repository : "+repositoryMeta.getName()); //$NON-NLS-1$
  				} else {
            log.logDebug("Unable to read repository with id: "+id); //$NON-NLS-1$
  				}
				} catch (KettleException ex) {
				  // Get to the root cause
				  Throwable cause = ex;
				  kettleException = ex;
				  while(cause.getCause() != null) {
				    cause = cause.getCause();
				  }
				  
				  if(cause instanceof KettleRepositoryNotSupportedException) {
				    // If the root cause is a KettleRepositoryNotSupportedException, do not fail
				    log.logDebug("Repository type [" + id + "] is unrecognized");
				  }
				}
			}
			if(kettleException != null) {
			  throw kettleException;
			}
		}
		catch(Exception e)
		{
			throw new KettleException("Error reading information from file : ", e);
		}
		
		return true;
	}
	
	public String getXML()
	{
		String retval="";
		
		retval+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+Const.CR;
		retval+="<repositories>"+Const.CR;

		for (int i=0;i<nrDatabases();i++)
		{
			DatabaseMeta conn = getDatabase(i);
			retval+=conn.getXML();
		}

		for (int i=0;i<nrRepositories();i++)
		{
			RepositoryMeta ri = getRepository(i);
			retval+=ri.getXML();
		}
		
		retval+="  </repositories>"+Const.CR;
		return retval;
	}
	
	public void writeData() throws KettleException
	{
		try
		{
			FileOutputStream fos = new FileOutputStream(new File(Const.getKettleUserRepositoriesFile()));
			fos.write(getXML().getBytes());
			fos.close();
		}
		catch(Exception e)
		{
			throw new KettleException("Error writing repositories metadata", e);
		}
	}
	
	public String toString()
	{
		return getClass().getSimpleName();
	}
}