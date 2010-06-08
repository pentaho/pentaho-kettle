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

import org.pentaho.di.core.Const;
import org.pentaho.di.core.ProgressMonitorListener;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.i18n.BaseMessages;

/**
 * Contains the schema's, catalogs, tables, views, synonyms, etc we can find in the databases...
 * 
 * @author Matt
 * @since  7-apr-2005
 */
public class DatabaseMetaInformation
{
	private static Class<?> PKG = Database.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private String[] tables;
	private String[] views;
	private String[] synonyms;
	private Catalog[] catalogs;
	private Schema[] schemas;
	private String[] procedures;

	private DatabaseMeta databaseMeta;
	public static final String FILTER_CATALOG_LIST = "FILTER_CATALOG_LIST"; //$NON-NLS-1$
  public static final String FILTER_SCHEMA_LIST = "FILTER_SCHEMA_LIST"; //$NON-NLS-1$
	
	/**
	 * Create a new DatabaseMetaData object for the given database connection
	 */
	public DatabaseMetaInformation(DatabaseMeta databaseMeta)
	{
		this.databaseMeta = databaseMeta;
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
	 * @return Returns the DatabaseMeta.
	 */
	public DatabaseMeta getDbInfo()
	{
		return databaseMeta;
	}
	
	/**
	 * @param value The DatabaseMeta to set.
	 */
	public void setDbInfo(DatabaseMeta value)
	{
		this.databaseMeta = value;
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
	/**
	 * @return Returns the procedures.
	 */
	public String[] getProcedures()
	{
		return procedures;
	}
	
	/**
	 * @param procedures The procedures to set.
	 */
	public void setProcedures(String[] procedures)
	{
		this.procedures = procedures;
	}
	
	public void getData(LoggingObjectInterface parentLoggingObject, ProgressMonitorListener monitor) throws KettleDatabaseException
	{
		if (monitor!=null)
		{
			monitor.beginTask(BaseMessages.getString(PKG, "DatabaseMeta.Info.GettingInfoFromDb"), 8);
		}

		Database db = new Database(parentLoggingObject, databaseMeta);	
		
		/*
		ResultSet tableResultSet = null;
		
		ResultSet schemaTablesResultSet = null;
		ResultSet schemaResultSet = null;
		
		ResultSet catalogResultSet = null;
		ResultSet catalogTablesResultSet = null;
		*/
		
		try
		{
			if (monitor!=null) monitor.subTask(BaseMessages.getString(PKG, "DatabaseMeta.Info.ConnectingDb"));
			db.connect();
			if (monitor!=null) monitor.worked(1);
			
			if (monitor!=null && monitor.isCanceled()) return;
			if (monitor!=null) monitor.subTask(BaseMessages.getString(PKG, "DatabaseMeta.Info.GettingMetaData"));
			DatabaseMetaData dbmd = db.getDatabaseMetaData();
			if (monitor!=null) monitor.worked(1);

			if (monitor!=null && monitor.isCanceled()) return;
			if (monitor!=null) monitor.subTask(BaseMessages.getString(PKG, "DatabaseMeta.Info.GettingInfo"));
			Map connectionExtraOptions = databaseMeta.getExtraOptions();
			if (databaseMeta.supportsCatalogs() && dbmd.supportsCatalogsInTableDefinitions())
			{
				ArrayList<Catalog> catalogList = new ArrayList<Catalog>();
				
        String catalogFilterKey = databaseMeta.getDatabaseTypeDesc() + "." + FILTER_CATALOG_LIST; //$NON-NLS-1$
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
						// LogWriter.getInstance().logError(getClass().getName(),BaseMessages.getString(PKG, "DatabaseMeta.Error.UnexpectedCatalogError"), e);
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
			if (monitor!=null) monitor.subTask(BaseMessages.getString(PKG, "DatabaseMeta.Info.GettingSchemaInfo"));
			if (databaseMeta.supportsSchemas() && dbmd.supportsSchemasInTableDefinitions())
			{
				ArrayList<Schema> schemaList = new ArrayList<Schema>();
				try 
				{
				  String schemaFilterKey = databaseMeta.getDatabaseTypeDesc() + "." +FILTER_SCHEMA_LIST; //$NON-NLS-1$
	        if ( (connectionExtraOptions != null) && connectionExtraOptions.containsKey(schemaFilterKey) ) {
	          String schemasFilterCommaList =  (String)connectionExtraOptions.get(schemaFilterKey);
	          String[] schemasFilterArray = schemasFilterCommaList.split(","); //$NON-NLS-1$
	          for (int i=0; i<schemasFilterArray.length; i++) {
	            schemaList.add(new Schema(schemasFilterArray[i].trim()));
	          }
	        }
	        if (schemaList.size() == 0) {
					  // Support schemas for MS SQL server due to PDI-1531
					  //
					  String sql = databaseMeta.getSQLListOfSchemas();
					  if (!Const.isEmpty(sql)) {
						  Statement schemaStatement = db.getConnection().createStatement();
						  ResultSet schemaResultSet = schemaStatement.executeQuery(sql);
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
					// LogWriter.getInstance().logError(getClass().getName(), BaseMessages.getString(PKG, "DatabaseMeta.Error.UnexpectedError"), e);
				}
				
				// Save for later...
				setSchemas(schemaList.toArray(new Schema[schemaList.size()]));
			}
			if (monitor!=null) monitor.worked(1);
	
			if (monitor!=null && monitor.isCanceled()) return;
			if (monitor!=null) monitor.subTask(BaseMessages.getString(PKG, "DatabaseMeta.Info.GettingTables"));
			setTables(db.getTablenames(databaseMeta.supportsSchemas()));
			if (monitor!=null) monitor.worked(1);
	
			if (monitor!=null && monitor.isCanceled()) return;
			if (monitor!=null) monitor.subTask(BaseMessages.getString(PKG, "DatabaseMeta.Info.GettingViews"));
			if (databaseMeta.supportsViews())
			{
				setViews(db.getViews(databaseMeta.supportsSchemas()));
			}
			if (monitor!=null) monitor.worked(1);
	
			if (monitor!=null && monitor.isCanceled()) return;
			if (monitor!=null) monitor.subTask(BaseMessages.getString(PKG, "DatabaseMeta.Info.GettingSynonyms"));
			if (databaseMeta.supportsSynonyms())
			{
				setSynonyms(db.getSynonyms(databaseMeta.supportsSchemas()));
			}
			if (monitor!=null) monitor.worked(1);
			
			if (monitor!=null && monitor.isCanceled()) return;
			if (monitor!=null) monitor.subTask(BaseMessages.getString(PKG, "DatabaseMeta.Info.GettingProcedures"));
			setProcedures(db.getProcedures());
			if (monitor!=null) monitor.worked(1);
			
		}
		catch(Exception e)
		{
			throw new KettleDatabaseException(BaseMessages.getString(PKG, "DatabaseMeta.Error.UnableRetrieveDbInfo"), e);
		}
		finally
		{
			if (monitor!=null) monitor.subTask(BaseMessages.getString(PKG, "DatabaseMeta.Info.ClosingDbConnection"));

			db.disconnect();
			if (monitor!=null) monitor.worked(1);
		}
		if (monitor!=null) monitor.done();
	}
}
