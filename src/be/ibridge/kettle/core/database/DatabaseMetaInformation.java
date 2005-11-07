

package be.ibridge.kettle.core.database;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;

import be.ibridge.kettle.core.exception.KettleDatabaseException;

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
			if (dbmd.supportsCatalogsInTableDefinitions())
			{
				ArrayList catalogList = new ArrayList();
				ResultSet catalogs = dbmd.getCatalogs();
				while (catalogs.next())
				{
					String catalogName = catalogs.getString(1);
					ArrayList catalogItems = new ArrayList();
					try
					{
						ResultSet tables = dbmd.getTables(catalogName, null,  null, null );
						while (tables.next())
						{
							String table_name = tables.getString(3);
							
							if (!db.isSystemTable(table_name)) 
							{
								catalogItems.add(table_name);
							}
						}
						tables.close();
					}
					catch(Exception e)
					{
						// Obviously, we're not allowed to snoop around in this catalog.
						// Just ignore it!
					}
					
					Catalog catalog = new Catalog(catalogName, (String[])catalogItems.toArray(new String[catalogItems.size()]));
					catalogList.add(catalog);
				}
				catalogs.close();
				
				// Save for later...
				setCatalogs((Catalog[])catalogList.toArray(new Catalog[catalogList.size()]));
			}
			if (monitor!=null) monitor.worked(1);
	
			if (monitor!=null && monitor.isCanceled()) return;
			if (monitor!=null) monitor.subTask("Getting schema information");
			if (dbInfo.supportsSchemas() && dbmd.supportsSchemasInTableDefinitions())
			{
				ArrayList schemaList = new ArrayList();
				ResultSet schemas = dbmd.getSchemas();
				while (schemas.next())
				{
					ArrayList schemaItems = new ArrayList();
					String schemaName = schemas.getString(1);
					try
					{
						ResultSet tables = dbmd.getTables(null, schemaName,  null, null );
						while (tables.next())
						{
							String table_name = tables.getString(3);
							if (!db.isSystemTable(table_name)) 
							{
								schemaItems.add(table_name);
							}
						}
						tables.close();
					}
					catch(Exception e)
					{
						// Obviously, we're not allowed to snoop around in this catalog.
						// Just ignore it!
					}
					Schema schema = new Schema(schemaName, (String[])schemaItems.toArray(new String[schemaItems.size()]));
					schemaList.add(schema);
				}
				schemas.close();
				
				// Save for later...
				setSchemas((Schema[])schemaList.toArray(new Schema[schemaList.size()]));
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
