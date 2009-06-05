package org.pentaho.di.repository.delegates;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleDependencyException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.KettleDatabaseRepository;

public class RepositoryDatabaseDelegate extends BaseRepositoryDelegate {

	private static Class<?> PKG = DatabaseMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	public RepositoryDatabaseDelegate(KettleDatabaseRepository repository) {
		super(repository);
	}
	
	public synchronized long getDatabaseID(String name) throws KettleException
	{
		return repository.connectionDelegate.getIDWithValue(quoteTable(KettleDatabaseRepository.TABLE_R_DATABASE), quote(KettleDatabaseRepository.FIELD_DATABASE_ID_DATABASE), quote(KettleDatabaseRepository.FIELD_DATABASE_NAME), name);
	}
    
	public synchronized String getDatabaseTypeCode(long id_database_type) throws KettleException
	{
		return repository.connectionDelegate.getStringWithID(quoteTable(KettleDatabaseRepository.TABLE_R_DATABASE_TYPE), quote(KettleDatabaseRepository.FIELD_DATABASE_TYPE_ID_DATABASE_TYPE), id_database_type, quote(KettleDatabaseRepository.FIELD_DATABASE_TYPE_CODE));
	}

	public synchronized String getDatabaseConTypeCode(long id_database_contype) throws KettleException
	{
		return repository.connectionDelegate.getStringWithID(quoteTable(KettleDatabaseRepository.TABLE_R_DATABASE_CONTYPE), quote(KettleDatabaseRepository.FIELD_DATABASE_CONTYPE_ID_DATABASE_CONTYPE), id_database_contype, quote(KettleDatabaseRepository.FIELD_DATABASE_CONTYPE_CODE));
	}

	public RowMetaAndData getDatabase(long id_database) throws KettleException
	{
		return repository.connectionDelegate.getOneRow(quoteTable(KettleDatabaseRepository.TABLE_R_DATABASE), quote(KettleDatabaseRepository.FIELD_DATABASE_ID_DATABASE), id_database);
	}


    public RowMetaAndData getDatabaseAttribute(long id_database_attribute) throws KettleException
    {
        return repository.connectionDelegate.getOneRow(quoteTable(KettleDatabaseRepository.TABLE_R_DATABASE_ATTRIBUTE), quote(KettleDatabaseRepository.FIELD_DATABASE_ATTRIBUTE_ID_DATABASE_ATTRIBUTE), id_database_attribute);
    }
    

    public Collection<RowMetaAndData> getDatabaseAttributes() throws KettleDatabaseException, KettleValueException
    {
    	List<RowMetaAndData> attrs = new ArrayList<RowMetaAndData>();
    	List<Object[]> rows = repository.connectionDelegate.getRows("SELECT * FROM " + quoteTable(KettleDatabaseRepository.TABLE_R_DATABASE_ATTRIBUTE),0);
    	for (Object[] row : rows) 
    	{
    		RowMetaAndData rowWithMeta = new RowMetaAndData(repository.connectionDelegate.getReturnRowMeta(), row);
    		long id = rowWithMeta.getInteger(quote(KettleDatabaseRepository.FIELD_DATABASE_ATTRIBUTE_ID_DATABASE_ATTRIBUTE), 0);
    		if (id >0) {
    			attrs.add(rowWithMeta);
    		}
    	}
    	return attrs;
    }

    
	/**
     *  
	 *  Load the Database Info 
     */ 
	public DatabaseMeta loadDatabaseMeta(long id_database) throws KettleException
	{
		DatabaseMeta databaseMeta = new DatabaseMeta();
		try
		{
			RowMetaAndData r = getDatabase(id_database);
			
			if (r!=null)
			{
				long id_database_type = r.getInteger( KettleDatabaseRepository.FIELD_DATABASE_ID_DATABASE_TYPE, 0); // con_type
				String dbTypeDesc = getDatabaseTypeCode(id_database_type);
				if (dbTypeDesc!=null)
				{
					databaseMeta.setDatabaseInterface(DatabaseMeta.getDatabaseInterface(dbTypeDesc));
					databaseMeta.setAttributes(new Properties()); // new attributes
				}
				else
				{
					// throw new KettleException("No database type was specified [id_database_type="+id_database_type+"]");
				}

				databaseMeta.setID(id_database);
				databaseMeta.setName( r.getString(KettleDatabaseRepository.FIELD_DATABASE_NAME, "") );

				long id_database_contype = r.getInteger(KettleDatabaseRepository.FIELD_DATABASE_ID_DATABASE_CONTYPE, 0); // con_access 
				databaseMeta.setAccessType( DatabaseMeta.getAccessType( getDatabaseConTypeCode( id_database_contype)) );

				databaseMeta.setHostname( r.getString(KettleDatabaseRepository.FIELD_DATABASE_HOST_NAME, "") );
				databaseMeta.setDBName( r.getString(KettleDatabaseRepository.FIELD_DATABASE_DATABASE_NAME, "") );
				databaseMeta.setDBPort( r.getString(KettleDatabaseRepository.FIELD_DATABASE_PORT, "") );
				databaseMeta.setUsername( r.getString(KettleDatabaseRepository.FIELD_DATABASE_USERNAME, "") );
				databaseMeta.setPassword( Encr.decryptPasswordOptionallyEncrypted( r.getString(KettleDatabaseRepository.FIELD_DATABASE_PASSWORD, "") ) );
				databaseMeta.setServername( r.getString(KettleDatabaseRepository.FIELD_DATABASE_SERVERNAME, "") );
				databaseMeta.setDataTablespace( r.getString(KettleDatabaseRepository.FIELD_DATABASE_DATA_TBS, "") );
				databaseMeta.setIndexTablespace( r.getString(KettleDatabaseRepository.FIELD_DATABASE_INDEX_TBS, "") );
                
                // Also, load all the properties we can find...
				final Collection<RowMetaAndData> attrs = getDatabaseAttributes(id_database);
                for (RowMetaAndData row : attrs)
                {
                    String code = row.getString(KettleDatabaseRepository.FIELD_DATABASE_ATTRIBUTE_CODE, "");
                    String attribute = row.getString(KettleDatabaseRepository.FIELD_DATABASE_ATTRIBUTE_VALUE_STR, "");
                    // System.out.println("Attributes: "+(getAttributes()!=null)+", code: "+(code!=null)+", attribute: "+(attribute!=null));
                    databaseMeta.getAttributes().put(code, Const.NVL(attribute, ""));
                }
			}
			
			return databaseMeta;
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Error loading database connection from repository (id_database="+id_database+")", dbe);
		}
	}

	
	/**
	 * Saves the database information into a given repository.
	 * 
	 * @param databaseMeta The database metadata object to store
	 * 
	 * @throws KettleException if an error occurs.
	 */
	public void saveDatabaseMeta(DatabaseMeta databaseMeta) throws KettleException
	{
		try
		{
            // If we don't have an ID, we don't know which entry in the database we need to update.
			// See if a database with the same name is already available...
			if (databaseMeta.getID()<=0)
			{
				databaseMeta.setID(getDatabaseID(databaseMeta.getName()));
			}
			
			// Still not found? --> Insert
			if (databaseMeta.getID()<=0)
			{
				// Insert new Note in repository
				databaseMeta.setID(insertDatabase(databaseMeta.getName(), 
											DatabaseMeta.getDatabaseTypeCode(databaseMeta.getDatabaseType()), 
											DatabaseMeta.getAccessTypeDesc(databaseMeta.getAccessType()), 
											databaseMeta.getHostname(), 
											databaseMeta.getDatabaseName(), 
											databaseMeta.getDatabasePortNumberString(), 
											databaseMeta.getUsername(), 
											databaseMeta.getPassword(),
											databaseMeta.getServername(),
											databaseMeta.getDataTablespace(),
											databaseMeta.getIndexTablespace()
										)
					); 
			}
			else // --> found entry with the same name...
			{
				// Update the note...
				updateDatabase(	databaseMeta.getID(),
						databaseMeta.getName(), 
											DatabaseMeta.getDatabaseTypeCode(databaseMeta.getDatabaseType()), 
											DatabaseMeta.getAccessTypeDesc(databaseMeta.getAccessType()), 
											databaseMeta.getHostname(), 
											databaseMeta.getDatabaseName(), 
											databaseMeta.getDatabasePortNumberString(), 
											databaseMeta.getUsername(), 
											databaseMeta.getPassword(),
											databaseMeta.getServername(),
											databaseMeta.getDataTablespace(),
											databaseMeta.getIndexTablespace()
										);
			}
            
            // For the extra attributes, just delete them and re-add them.
            delDatabaseAttributes(databaseMeta.getID());
            
            // OK, now get a list of all the attributes set on the database connection...
            // 
            Properties attributes = databaseMeta.getAttributes();
            Enumeration<Object> keys = databaseMeta.getAttributes().keys();
            while (keys.hasMoreElements())
            {
                String code = (String) keys.nextElement();
                String attribute = (String)attributes.get(code);
                
                // Save this attribute
                //
                insertDatabaseAttribute(databaseMeta.getID(), code, attribute);
            }
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Error saving database connection or one of its attributes to the repository.", dbe);
		}
	}

	public synchronized long insertDatabase(String name, String type, String access, String host, String dbname, String port,
			String user, String pass, String servername, String data_tablespace, String index_tablespace)
			throws KettleException
	{

		long id = repository.connectionDelegate.getNextDatabaseID();

		long id_database_type = getDatabaseTypeID(type);
		if (id_database_type < 0) // New support database type: add it!
		{
			id_database_type = repository.connectionDelegate.getNextDatabaseTypeID();

			String tablename = KettleDatabaseRepository.TABLE_R_DATABASE_TYPE;
			RowMetaInterface tableMeta = new RowMeta();
            
            tableMeta.addValueMeta(new ValueMeta(KettleDatabaseRepository.FIELD_DATABASE_TYPE_ID_DATABASE_TYPE, ValueMetaInterface.TYPE_INTEGER, 5, 0));
            tableMeta.addValueMeta(new ValueMeta(KettleDatabaseRepository.FIELD_DATABASE_TYPE_CODE, ValueMetaInterface.TYPE_STRING, KettleDatabaseRepository.REP_STRING_CODE_LENGTH, 0));
            tableMeta.addValueMeta(new ValueMeta(KettleDatabaseRepository.FIELD_DATABASE_TYPE_DESCRIPTION, ValueMetaInterface.TYPE_STRING, KettleDatabaseRepository.REP_STRING_LENGTH, 0));

			repository.connectionDelegate.getDatabase().prepareInsert(tableMeta, tablename);

			Object[] tableData = new Object[3];
            int tableIndex = 0;
            
			tableData[tableIndex++] = new Long(id_database_type);
            tableData[tableIndex++] = type;
            tableData[tableIndex++] = type;

            repository.connectionDelegate.getDatabase().setValuesInsert(tableMeta, tableData);
            repository.connectionDelegate.getDatabase().insertRow();
            repository.connectionDelegate.getDatabase().closeInsert();
		}

		long id_database_contype = getDatabaseConTypeID(access);

		RowMetaAndData table = new RowMetaAndData();
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_DATABASE_ID_DATABASE, ValueMetaInterface.TYPE_INTEGER), new Long(id));
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_DATABASE_NAME, ValueMetaInterface.TYPE_STRING), name);
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_DATABASE_ID_DATABASE_TYPE, ValueMetaInterface.TYPE_INTEGER), new Long(id_database_type));
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_DATABASE_ID_DATABASE_CONTYPE, ValueMetaInterface.TYPE_INTEGER), new Long(id_database_contype));
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_DATABASE_HOST_NAME, ValueMetaInterface.TYPE_STRING), host);
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_DATABASE_DATABASE_NAME, ValueMetaInterface.TYPE_STRING), dbname);
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_DATABASE_PORT, ValueMetaInterface.TYPE_INTEGER), new Long(Const.toInt(port, -1)));
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_DATABASE_USERNAME, ValueMetaInterface.TYPE_STRING), user);
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_DATABASE_PASSWORD, ValueMetaInterface.TYPE_STRING), Encr.encryptPasswordIfNotUsingVariables(pass));
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_DATABASE_SERVERNAME, ValueMetaInterface.TYPE_STRING), servername);
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_DATABASE_DATA_TBS, ValueMetaInterface.TYPE_STRING), data_tablespace);
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_DATABASE_INDEX_TBS, ValueMetaInterface.TYPE_STRING), index_tablespace);

		repository.connectionDelegate.getDatabase().prepareInsert(table.getRowMeta(), KettleDatabaseRepository.TABLE_R_DATABASE);
		repository.connectionDelegate.getDatabase().setValuesInsert(table);
		repository.connectionDelegate.getDatabase().insertRow();
		repository.connectionDelegate.getDatabase().closeInsert();

		return id;
	}

	public synchronized void updateDatabase(long id_database, String name, String type, String access, String host, String dbname,
			String port, String user, String pass, String servername, String data_tablespace, String index_tablespace)
			throws KettleException
	{
		long id_database_type = getDatabaseTypeID(type);
		long id_database_contype = getDatabaseConTypeID(access);

		RowMetaAndData table = new RowMetaAndData();
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_DATABASE_NAME, ValueMetaInterface.TYPE_STRING), name);
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_DATABASE_ID_DATABASE_TYPE, ValueMetaInterface.TYPE_INTEGER), new Long(id_database_type));
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_DATABASE_ID_DATABASE_CONTYPE, ValueMetaInterface.TYPE_INTEGER), new Long(id_database_contype));
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_DATABASE_HOST_NAME, ValueMetaInterface.TYPE_STRING), host);
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_DATABASE_DATABASE_NAME, ValueMetaInterface.TYPE_STRING), dbname);
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_DATABASE_PORT, ValueMetaInterface.TYPE_INTEGER), new Long(Const.toInt(port, -1)));
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_DATABASE_USERNAME, ValueMetaInterface.TYPE_STRING), user);
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_DATABASE_PASSWORD, ValueMetaInterface.TYPE_STRING), Encr.encryptPasswordIfNotUsingVariables(pass));
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_DATABASE_SERVERNAME, ValueMetaInterface.TYPE_STRING), servername);
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_DATABASE_DATA_TBS, ValueMetaInterface.TYPE_STRING), data_tablespace);
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_DATABASE_INDEX_TBS, ValueMetaInterface.TYPE_STRING), index_tablespace);

		repository.connectionDelegate.updateTableRow(KettleDatabaseRepository.TABLE_R_DATABASE, KettleDatabaseRepository.FIELD_DATABASE_ID_DATABASE, table, id_database);
	}


	public synchronized long getDatabaseTypeID(String code) throws KettleException
	{
		return repository.connectionDelegate.getIDWithValue(quoteTable(KettleDatabaseRepository.TABLE_R_DATABASE_TYPE), quote(KettleDatabaseRepository.FIELD_DATABASE_TYPE_ID_DATABASE_TYPE), quote(KettleDatabaseRepository.FIELD_DATABASE_TYPE_CODE), code);
	}

	public synchronized long getDatabaseConTypeID(String code) throws KettleException
	{
		return repository.connectionDelegate.getIDWithValue(quoteTable(KettleDatabaseRepository.TABLE_R_DATABASE_CONTYPE), quote(KettleDatabaseRepository.FIELD_DATABASE_CONTYPE_ID_DATABASE_CONTYPE), quote(KettleDatabaseRepository.FIELD_DATABASE_CONTYPE_CODE), code);
	}

	/**
	 * Remove a database connection from the repository
	 * @param databaseName The name of the connection to remove
	 * @throws KettleException In case something went wrong: database error, insufficient permissions, depending objects, etc.
	 */
	public void deleteDatabaseMeta(String databaseName) throws KettleException {
		if (!repository.getUserInfo().isReadonly())
		{
			try {
				long id_database = getDatabaseID(databaseName);
				delDatabase(id_database);

			}  catch (KettleException dbe) {
				throw new KettleException(BaseMessages.getString(PKG, "Spoon.Dialog.ErrorDeletingConnection.Message", databaseName), dbe);
			}
		} else {
			throw new KettleException(BaseMessages.getString(PKG, "Spoon.Dialog.Exception.ReadOnlyUser"));
		}
	}

	public synchronized void delDatabase(long id_database) throws KettleException
	{
		// First, see if the database connection is still used by other connections...
		// If so, generate an error!!
		// We look in table R_STEP_DATABASE to see if there are any steps using this database.
		//
		String[] transList = repository.getTransformationsUsingDatabase(id_database);
		String[] jobList = repository.getJobsUsingDatabase(id_database);
		
		if (jobList.length==0 && transList.length==0)
		{
			String sql = "DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_DATABASE)+" WHERE "+quote(KettleDatabaseRepository.FIELD_DATABASE_ID_DATABASE)+" = " + id_database;
			repository.connectionDelegate.getDatabase().execStatement(sql);
		}
		else
		{
			String message=" Database used by the following "+Const.CR;
			if(jobList.length>0)
			{
				message = "jobs :"+Const.CR;
				for (int i = 0; i < jobList.length; i++)
				{
					message+="	 "+jobList[i]+Const.CR;
				}
			}
			
			message+= "transformations:"+Const.CR;
			for (int i = 0; i < transList.length; i++)
			{
				message+="	"+transList[i]+Const.CR;
			}
			KettleDependencyException e = new KettleDependencyException(message);
			throw new KettleDependencyException("This database is still in use by " + jobList.length + " jobs and "+transList.length+" transformations references", e);
		}
	}
    
    public synchronized void delDatabaseAttributes(long id_database) throws KettleException
    {
        String sql = "DELETE FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_DATABASE_ATTRIBUTE)+" WHERE "+quote(KettleDatabaseRepository.FIELD_DATABASE_ATTRIBUTE_ID_DATABASE)+" = " + id_database;
        repository.connectionDelegate.getDatabase().execStatement(sql);
    }

	public synchronized int getNrDatabases() throws KettleException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_DATABASE);
		RowMetaAndData r = repository.connectionDelegate.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0L);
		}

		return retval;
	}

	public synchronized int getNrDatabases(long id_transformation) throws KettleException
	{
		int retval = 0;

		String sql = "SELECT COUNT(*) FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_STEP_DATABASE)+" WHERE "+quote(KettleDatabaseRepository.FIELD_STEP_DATABASE_ID_TRANSFORMATION)+" = " + id_transformation;
		RowMetaAndData r = repository.connectionDelegate.getOneRow(sql);
		if (r != null)
		{
			retval = (int) r.getInteger(0, 0L);
		}

		return retval;
	}


    public synchronized int getNrDatabaseAttributes(long id_database) throws KettleException
    {
        int retval = 0;

        String sql = "SELECT COUNT(*) FROM "+quoteTable(KettleDatabaseRepository.TABLE_R_DATABASE_ATTRIBUTE)+" WHERE "+quote(KettleDatabaseRepository.FIELD_DATABASE_ATTRIBUTE_ID_DATABASE)+" = "+id_database;
        RowMetaAndData r = repository.connectionDelegate.getOneRow(sql);
        if (r != null)
        {
            retval = (int) r.getInteger(0, 0L);
        }

        return retval;
    }
    
	public Collection<RowMetaAndData> getDatabaseAttributes(long id_database) throws KettleDatabaseException, KettleValueException
    {
    	List<RowMetaAndData> attrs = new ArrayList<RowMetaAndData>();
    	List<Object[]> rows = repository.connectionDelegate.getRows("SELECT * FROM " + quoteTable(KettleDatabaseRepository.TABLE_R_DATABASE_ATTRIBUTE) + " WHERE "+quote(KettleDatabaseRepository.FIELD_DATABASE_ID_DATABASE) +" = "+id_database, 0);
    	for (Object[] row : rows) 
    	{
    		RowMetaAndData rowWithMeta = new RowMetaAndData(repository.connectionDelegate.getReturnRowMeta(), row);
    		long id = rowWithMeta.getInteger(quote(KettleDatabaseRepository.FIELD_DATABASE_ATTRIBUTE_ID_DATABASE_ATTRIBUTE), 0);
    		if (id >0) {
    			attrs.add(rowWithMeta);
    		}
    	}
    	return attrs;
    }
    
    private synchronized long insertDatabaseAttribute(long id_database, String code, String value_str) throws KettleException
    {
        long id = repository.connectionDelegate.getNextDatabaseAttributeID();

        RowMetaAndData table = new RowMetaAndData();

        table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_DATABASE_ATTRIBUTE_ID_DATABASE_ATTRIBUTE, ValueMetaInterface.TYPE_INTEGER), new Long(id));
        table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_DATABASE_ATTRIBUTE_ID_DATABASE, ValueMetaInterface.TYPE_INTEGER), new Long(id_database));
        table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_DATABASE_ATTRIBUTE_CODE, ValueMetaInterface.TYPE_STRING), code);
        table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_DATABASE_ATTRIBUTE_VALUE_STR, ValueMetaInterface.TYPE_STRING), value_str);

        /* If we have prepared the insert, we don't do it again.
         * We assume that all the step insert statements come one after the other.
         */
        repository.connectionDelegate.getDatabase().prepareInsert(table.getRowMeta(), KettleDatabaseRepository.TABLE_R_DATABASE_ATTRIBUTE);
        repository.connectionDelegate.getDatabase().setValuesInsert(table);
        repository.connectionDelegate.getDatabase().insertRow();
        repository.connectionDelegate.getDatabase().closeInsert();
        
        if (log.isDebug()) log.logDebug(toString(), "saved database attribute ["+code+"]");
        
        return id;
    }

	public synchronized void renameDatabase(long id_database, String newname) throws KettleException
	{
		String sql = "UPDATE "+quoteTable(KettleDatabaseRepository.TABLE_R_DATABASE)+" SET "+quote(KettleDatabaseRepository.FIELD_DATABASE_NAME)+" = ? WHERE "+quote(KettleDatabaseRepository.FIELD_DATABASE_ID_DATABASE)+" = ?";

		RowMetaAndData table = new RowMetaAndData();
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_DATABASE_NAME, ValueMetaInterface.TYPE_STRING), newname);
		table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_DATABASE_ID_DATABASE, ValueMetaInterface.TYPE_INTEGER), new Long(id_database));

		repository.connectionDelegate.getDatabase().execStatement(sql, table.getRowMeta(), table.getData());
	}

	

}
