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


package org.pentaho.di.core.database;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;

import org.eclipse.core.runtime.IProgressMonitor;
import org.pentaho.di.core.exception.KettleDatabaseException;

/**
 * Contains the schema's, catalogs, tables, views, synonyms, etc we can find in the databases...
 * 
 * @author Matt
 * @since  7-apr-2005
 */
public class DatabaseMetaInformation
{
	private String[] tables;
	private String[] views;
	private String[] synonyms;
	private Catalog[] catalogs;
	private Schema[] schemas;

	private DatabaseMeta dbInfo;
	
	/**
	 * Create a new DatabaseMetaData object for the given database connection
	 */
	public DatabaseMetaInformation(DatabaseMeta dbInfo)
	{
		this.dbInfo = dbInfo;
	}
	
	/**
	 * @return Returns the catalogs.
	 */
	public Catalog[] getCatalogs()
	{
		return catalogs;
	}
	
	/**
	 * @param catalogs The catalogs to set.
	 */
	public void setCatalogs(Catalog[] catalogs)
	{
		this.catalogs = catalogs;
	}
	
	/**
	 * @return Returns the dbInfo.
	 */
	public DatabaseMeta getDbInfo()
	{
		return dbInfo;
	}
	
	/**
	 * @param dbInfo The dbInfo to set.
	 */
	public void setDbInfo(DatabaseMeta dbInfo)
	{
		this.dbInfo = dbInfo;
	}
	
	/**
	 * @return Returns the schemas.
	 */
	public Schema[] getSchemas()
	{
		return schemas;
	}
	
	/**
	 * @param schemas The schemas to set.
	 */
	public void setSchemas(Schema[] schemas)
	{
		this.schemas = schemas;
	}
	
	/**
	 * @return Returns the tables.
	 */
	public String[] getTables()
	{
		return tables;
	}
	
	/**
	 * @param tables The tables to set.
	 */
	public void setTables(String[] tables)
	{
		this.tables = tables;
	}
	
	/**
	 * @return Returns the views.
	 */
	public String[] getViews()
	{
		return views;
	}
	
	/**
	 * @param views The views to set.
	 */
	public void setViews(String[] views)
	{
		this.views = views;
	}
	
	/**
	 * @param synonyms The synonyms to set.
	 */
	public void setSynonyms(String[] synonyms)
	{
		this.synonyms = synonyms;
	}
	
	/**
	 * @return Returns the synonyms.
	 */
	public String[] getSynonyms()
	{
		return synonyms;
	}
	
	public void getData(IProgressMonitor monitor) throws KettleDatabaseException
	{
		if (monitor!=null)
		{
			monitor.beginTask("Getting information from the database...", 8);
		}

		Database db = new Database(dbInfo);	
		try
		{
			if (monitor!=null) monitor.subTask("Connecting to database");
			db.connect();
			if (monitor!=null) monitor.worked(1);
			
			if (monitor!=null && monitor.isCanceled()) return;
			if (monitor!=null) monitor.subTask("Getting database metadata");
			DatabaseMetaData dbmd = db.getDatabaseMetaData();
			if (monitor!=null) monitor.worked(1);

			if (monitor!=null && monitor.isCanceled()) return;
			if (monitor!=null) monitor.subTask("Getting catalog information");
			if (dbInfo.supportsCatalogs() && dbmd.supportsCatalogsInTableDefinitions())
			{
				ArrayList<Catalog> catalogList = new ArrayList<Catalog>();
				ResultSet catalogs = dbmd.getCatalogs();
				while (catalogs!=null && catalogs.next())
				{
					String catalogName = catalogs.getString(1);
					ArrayList<String> catalogItems = new ArrayList<String>();
					ResultSet tables = null;
					try
					{
						tables = dbmd.getTables(catalogName, null,  null, null );
						while (tables.next())
						{
							String table_name = tables.getString(3);
							
							if (!db.isSystemTable(table_name)) 
							{
								catalogItems.add(table_name);
							}
						}
					}
					catch(Exception e)
					{
						// Obviously, we're not allowed to snoop around in this catalog.
						// Just ignore it!
					}
					finally 
					{
						if ( tables != null ) tables.close();
					}
					
					Catalog catalog = new Catalog(catalogName, catalogItems.toArray(new String[catalogItems.size()]));
					catalogList.add(catalog);
				}
				catalogs.close();
				
				// Save for later...
				setCatalogs(catalogList.toArray(new Catalog[catalogList.size()]));
			}
			if (monitor!=null) monitor.worked(1);
	
			if (monitor!=null && monitor.isCanceled()) return;
			if (monitor!=null) monitor.subTask("Getting schema information");
			if (dbInfo.supportsSchemas() && dbmd.supportsSchemasInTableDefinitions())
			{
				ArrayList<Schema> schemaList = new ArrayList<Schema>();
				ResultSet schemas = null;
				try 
				{
					schemas = dbmd.getSchemas();
					while (schemas!=null && schemas.next())
					{
						ArrayList<String> schemaItems = new ArrayList<String>();
						String schemaName = schemas.getString(1);
						ResultSet tables = null;
						try
						{
							tables = dbmd.getTables(null, schemaName,  null, null );
							while (tables.next())
							{
								String table_name = tables.getString(3);
								if (!db.isSystemTable(table_name)) 
								{
									schemaItems.add(table_name);
								}
							}
							Collections.sort(schemaItems);
						}
						catch(Exception e)
						{
							// Obviously, we're not allowed to snoop around in this catalog.
							// Just ignore it!
						}
						finally
						{
							if ( tables != null ) tables.close();
						}
						Schema schema = new Schema(schemaName, schemaItems.toArray(new String[schemaItems.size()]));
						schemaList.add(schema);
					}
				}
				finally 
				{
				    if ( schemas != null ) schemas.close();
				}				
				
				// Save for later...
				setSchemas(schemaList.toArray(new Schema[schemaList.size()]));
			}
			if (monitor!=null) monitor.worked(1);
	
			if (monitor!=null && monitor.isCanceled()) return;
			if (monitor!=null) monitor.subTask("Getting tables");
			setTables(db.getTablenames());
			if (monitor!=null) monitor.worked(1);
	
			if (monitor!=null && monitor.isCanceled()) return;
			if (monitor!=null) monitor.subTask("Getting views");
			if (dbInfo.supportsViews())
			{
				setViews(db.getViews());
			}
			if (monitor!=null) monitor.worked(1);
	
			if (monitor!=null && monitor.isCanceled()) return;
			if (monitor!=null) monitor.subTask("Getting synonyms");
			if (dbInfo.supportsSynonyms())
			{
				setSynonyms(db.getSynonyms());
			}
			if (monitor!=null) monitor.worked(1);
		}
		catch(Exception e)
		{
			throw new KettleDatabaseException("Unable to retrieve database information because of an error", e);
		}
		finally
		{
			if (monitor!=null) monitor.subTask("Closing database connection");
			db.disconnect();
			if (monitor!=null) monitor.worked(1);
		}
		if (monitor!=null) monitor.done();
	}
}
