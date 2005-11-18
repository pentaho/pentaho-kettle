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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.database.DatabaseMeta;



//import java.util.ArrayList;

/*
 * Created on 31-mrt-2004
 *
 * This class contains information regarding the defined Kettle repositories 
 */

public class RepositoriesMeta 
{
	private LogWriter log;
	private ArrayList databases;    // Repository connections
	private ArrayList repositories;   // List of repositories

	public RepositoriesMeta(LogWriter log)
	{
		this.log = log;
		
		clear();
	}
	
	public void clear()
	{
		databases = new ArrayList();
		repositories = new ArrayList();
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
		return (RepositoryMeta)repositories.get(i);
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
			if (getRepository(i).getName().equalsIgnoreCase(name)) return getRepository(i);
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
	public boolean readData()
	{
		File file = new File(Const.getKettleLocalRepositoriesFile());
		if (!file.exists() || !file.isFile())
		{
			log.logDetailed(toString(), "No repositories file found in the local directory: "+file.getAbsolutePath());
			file = new File(Const.getKettleUserRepositoriesFile());
		}
		
		log.logBasic(toString(), "Reading repositories XML file: "+file.getAbsoluteFile());
		
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
				InputStream is = getClass().getResourceAsStream("/be/ibridge/kettle/repository/repositories.xml");
				if (is!=null)
				{
					doc = db.parse(is);
				}
				else
				{
					log.logError(toString(), "Error opening file: "+file.getAbsoluteFile()+" : "+ef.toString());
					return false;
				}
			}
			// InfoHandler sir  = new InfoHandler(doc);
				
			// Clear the information
			clear();
			
			// Get the <repositories> node:
			Node repsnode = XMLHandler.getSubNode(doc, "repositories");
		
			// Handle connections
			int nrconn = XMLHandler.countNodes(repsnode, "connection");
			log.logDebug(toString(), "We have "+nrconn+" connections...");
			for (int i=0;i<nrconn;i++)
			{
				log.logDebug(toString(), "Looking at connection #"+i);
				Node dbnode = XMLHandler.getSubNodeByNr(repsnode, "connection", i);
				DatabaseMeta dbcon = new DatabaseMeta(dbnode);
				addDatabase(dbcon);
				log.logDebug(toString(), "Read connection : "+dbcon.getName());
			}

			// Handle repositories...
			int nrreps = XMLHandler.countNodes(repsnode, "repository");
			log.logDebug(toString(), "We have "+nrreps+" repositories...");
			for (int i=0;i<nrreps;i++)
			{
				Node repnode = XMLHandler.getSubNodeByNr(repsnode, "repository", i);
				log.logDebug(toString(), "Looking at repository #"+i);
				
				RepositoryMeta repinfo = new RepositoryMeta();
				if (repinfo.loadXML(repnode, databases))
				{
					addRepository(repinfo);
					log.logDebug(toString(), "Read repository : "+repinfo.getName());
				}
			}
		}
		catch(Exception e)
		{
			log.logError(toString(), "Error reading information from file : "+e.toString());
			e.printStackTrace();
			clear();
			return false;
		}
		return true;

	}
	
	public String getXML()
	{
		String retval="";
		
		retval+="<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>"+Const.CR;
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
	
	public boolean writeData()
	{
		try
		{
			FileOutputStream fos = new FileOutputStream(new File(Const.getKettleUserRepositoriesFile()));
			fos.write(getXML().getBytes());
			fos.close();
		}
		catch(FileNotFoundException e)
		{
			log.logError("Repository", "Writing repository we got error : "+e.toString());
			return false;
		}
		catch(IOException ie)
		{
			log.logError("Repository", "Writing repository we got IO error : "+ie.toString());
			return false;
		}
		return true;
	}
	
	public String toString()
	{
		return "Kettle";
	}

}
