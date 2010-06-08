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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import org.pentaho.di.core.ProgressMonitorListener;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.logging.LogWriter;

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
	public static final String FILTER_CATALOG_LIST = "FILTER_CATALOG_LIST"; //$NON-NLS-1$
  public static final String FILTER_SCHEMA_LIST = "FILTER_SCHEMA_LIST"; //$NON-NLS-1$
	
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
	
	public void getData(ProgressMonitorListener monitor) throws KettleDatabaseException
	{
		if (monitor!=null)
		{
			monitor.beginTask(Messages.getString("DatabaseMeta.Info.GettingInfoFromDb"), 8);
		}

		Database db = new Database(dbInfo);	
		
		/*
		ResultSet tableResultSet = null;
		
		ResultSet schemaTablesResultSet = null;
		ResultSet schemaResultSet = null;
		
		ResultSet catalogResultSet = null;
		ResultSet catalogTablesResultSet = null;
		*/
		
		try
		{
			if (monitor!=null) monitor.subTask(Messages.getString("DatabaseMeta.Info.ConnectingDb"));
			db.connect();
			if (monitor!=null) monitor.worked(1);
			
			if (monitor!=null && monitor.isCanceled()) return;
			if (monitor!=null) monitor.subTask(Messages.getString("DatabaseMeta.Info.GettingMetaData"));
			DatabaseMetaData dbmd = db.getDatabaseMetaData();
			if (monitor!=null) monitor.worked(1);

			if (monitor!=null && monitor.isCanceled()) return;
			if (monitor!=null) monitor.subTask(Messages.getString("DatabaseMeta.Info.GettingInfo"));
			Map connectionExtraOptions = dbInfo.getExtraOptions();
			if (dbInfo.supportsCatalogs() && dbmd.supportsCatalogsInTableDefinitions())
			{
				ArrayList<Catalog> catalogList = new ArrayList<Catalog>();
				
        String catalogFilterKey = dbInfo.getDatabaseTypeDesc() + "." + FILTER_CATALOG_LIST; //$NON-NLS-1$
        if ( (connectionExtraOptions != null) && connectionExtraOptions.containsKey(catalogFilterKey) ) {
          String catsFilterCommaList =  (String)connectionExtraOptions.get(catalogFilterKey);
          String[] catsFilterArray = catsFilterCommaList.split(","); //$NON-NLS-1$
          for (int i=0; i<catsFilterArray.length; i++) {
            catalogList.add(new Catalog(catsFilterArray[i].trim()));
          }
        }
        if (catalogList.size() == 0 ) {
				ResultSet catalogResultSet = dbmd.getCatalogs();
				
				// Grab all the catalog names and put them in an array list
				// Then we can close the resultset as soon as possible.
				// This is the safest route to take for a lot of databases
				//
				while (catalogResultSet!=null && catalogResultSet.next())
				{
					String catalogName = catalogResultSet.getString(1);
					catalogList.add(new Catalog(catalogName));
				}
				
				// Close the catalogs resultset immediately
				//
				catalogResultSet.close();
        }
				
				// Now loop over the catalogs...
				//
				for (Catalog catalog : catalogList) 
				{
					ArrayList<String> catalogTables = new ArrayList<String>();
					
					try
					{
						ResultSet catalogTablesResultSet = dbmd.getTables(catalog.getCatalogName(), null,  null, null );
						while (catalogTablesResultSet.next())
						{
							String tableName = catalogTablesResultSet.getString(3);
							
							if (!db.isSystemTable(tableName)) 
							{
								catalogTables.add(tableName);
							}
						}
						// Immediately close the catalog tables ResultSet
						//
						catalogTablesResultSet.close();

						// Sort the tables by names
						Collections.sort(catalogTables);
					}
					catch(Exception e)
					{
						// Obviously, we're not allowed to snoop around in this catalog.
						// Just ignore it!
						LogWriter.getInstance().logError(getClass().getName(),Messages.getString("DatabaseMeta.Error.UnexpectedCatalogError"), e);
					}

					// Save the list of tables in the catalog (can be empty)
					//
					catalog.setItems( catalogTables.toArray(new String[catalogTables.size()]) );
				}
				
				// Save for later...
				setCatalogs(catalogList.toArray(new Catalog[catalogList.size()]));
			}
			if (monitor!=null) monitor.worked(1);
	
			if (monitor!=null && monitor.isCanceled()) return;
			if (monitor!=null) monitor.subTask(Messages.getString("DatabaseMeta.Info.GettingSchemaInfo"));
			if (dbInfo.supportsSchemas() && dbmd.supportsSchemasInTableDefinitions())
			{
				ArrayList<Schema> schemaList = new ArrayList<Schema>();
				try 
				{
				  String schemaFilterKey = dbInfo.getDatabaseTypeDesc() + "." +FILTER_SCHEMA_LIST; //$NON-NLS-1$
	        if ( (connectionExtraOptions != null) && connectionExtraOptions.containsKey(schemaFilterKey) ) {
	          String schemasFilterCommaList =  (String)connectionExtraOptions.get(schemaFilterKey);
	          String[] schemasFilterArray = schemasFilterCommaList.split(","); //$NON-NLS-1$
	          for (int i=0; i<schemasFilterArray.length; i++) {
	            schemaList.add(new Schema(schemasFilterArray[i].trim()));
	          }
	        }
	        if (schemaList.size() == 0) {
					// Support schemas for MS SQL server due to PDI-1531
					if (dbInfo.getDatabaseType()==DatabaseMeta.TYPE_DATABASE_MSSQL) {
						Statement schemaStatement = db.getConnection().createStatement();
						ResultSet schemaResultSet = schemaStatement.executeQuery("select name from sys.schemas");
						while (schemaResultSet!=null && schemaResultSet.next())
						{
							String schemaName = schemaResultSet.getString("name");
							schemaList.add(new Schema(schemaName));
						}
						schemaResultSet.close();
						schemaStatement.close();
					} else {
						ResultSet schemaResultSet = dbmd.getSchemas();
						while (schemaResultSet!=null && schemaResultSet.next())
						{
							String schemaName = schemaResultSet.getString(1);
							schemaList.add(new Schema(schemaName));
						}
						// Close the schema ResultSet immediately
						//
						schemaResultSet.close();
					}
	        }
					for (Schema schema : schemaList) 
					{
						ArrayList<String> schemaTables = new ArrayList<String>();
						
						try
						{
							ResultSet schemaTablesResultSet = dbmd.getTables(null, schema.getSchemaName(),  null, null );
							while (schemaTablesResultSet.next())
							{
								String tableName = schemaTablesResultSet.getString(3);
								if (!db.isSystemTable(tableName)) 
								{
									schemaTables.add(tableName);
								}
							}
							// Immediately close the schema tables ResultSet
							//
							schemaTablesResultSet.close();

							// Sort the tables by names
							Collections.sort(schemaTables);
						}
						catch(Exception e)
						{
							// Obviously, we're not allowed to snoop around in this catalog.
							// Just ignore it!
						}

						schema.setItems( schemaTables.toArray(new String[schemaTables.size()]) );
					}
				}
				catch(Exception e)
				{
					LogWriter.getInstance().logError(getClass().getName(), Messages.getString("DatabaseMeta.Error.UnexpectedError"), e);
				}
				
				// Save for later...
				setSchemas(schemaList.toArray(new Schema[schemaList.size()]));
			}
			if (monitor!=null) monitor.worked(1);
	
			if (monitor!=null && monitor.isCanceled()) return;
			if (monitor!=null) monitor.subTask(Messages.getString("DatabaseMeta.Info.GettingTables"));
			if (dbInfo.getDatabaseType()==DatabaseMeta.TYPE_DATABASE_MSSQL) {
				// Support schemas for MS SQL server due to PDI-1531
				setTables(db.getTablenames(true));
			} else {
				setTables(db.getTablenames());
			}
			if (monitor!=null) monitor.worked(1);
	
			if (monitor!=null && monitor.isCanceled()) return;
			if (monitor!=null) monitor.subTask(Messages.getString("DatabaseMeta.Info.GettingViews"));
			if (dbInfo.supportsViews())
			{
				if (dbInfo.getDatabaseType()==DatabaseMeta.TYPE_DATABASE_MSSQL) {
					// Support schemas for MS SQL server due to PDI-1531
					setViews(db.getViews(true));
				} else {
					setViews(db.getViews());
				}
			}
			if (monitor!=null) monitor.worked(1);
	
			if (monitor!=null && monitor.isCanceled()) return;
			if (monitor!=null) monitor.subTask(Messages.getString("DatabaseMeta.Info.GettingSynonyms"));
			if (dbInfo.supportsSynonyms())
			{
				if (dbInfo.getDatabaseType()==DatabaseMeta.TYPE_DATABASE_MSSQL) {
					// Support schemas for MS SQL server due to PDI-1531
					setSynonyms(db.getSynonyms(true));
				} else {
					setSynonyms(db.getSynonyms());
				}
			}
			if (monitor!=null) monitor.worked(1);
		}
		catch(Exception e)
		{
			throw new KettleDatabaseException(Messages.getString("DatabaseMeta.Error.UnableRetrieveDbInfo"), e);
		}
		finally
		{
			if (monitor!=null) monitor.subTask(Messages.getString("DatabaseMeta.Info.ClosingDbConnection"));

			db.disconnect();
			if (monitor!=null) monitor.worked(1);
		}
		if (monitor!=null) monitor.done();
	}
}
