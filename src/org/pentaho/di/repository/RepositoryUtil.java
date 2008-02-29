package org.pentaho.di.repository;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Properties;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaAndData;
import org.pentaho.di.core.row.ValueMetaInterface;

public class RepositoryUtil {
	/**
     *  
	 *  Load the Database Info 
     */ 
	public static DatabaseMeta loadDatabaseMeta(Repository rep, long id_database) throws KettleException
	{
        DatabaseMeta databaseMeta = new DatabaseMeta();
        
		try
		{
			RowMetaAndData r = rep.getDatabase(id_database);
			
			if (r!=null)
			{
				long id_database_type    = r.getInteger("ID_DATABASE_TYPE", 0); // con_type
				String dbTypeDesc = rep.getDatabaseTypeCode(id_database_type);
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
				databaseMeta.setName( r.getString("NAME", "") );

				long id_database_contype = r.getInteger("ID_DATABASE_CONTYPE", 0); // con_access 
				databaseMeta.setAccessType( DatabaseMeta.getAccessType( rep.getDatabaseConTypeCode( id_database_contype)) );

				databaseMeta.setHostname( r.getString("HOST_NAME", "") );
				databaseMeta.setDBName( r.getString("DATABASE_NAME", "") );
				databaseMeta.setDBPort( r.getString("PORT", "") );
				databaseMeta.setUsername( r.getString("USERNAME", "") );
				databaseMeta.setPassword( Encr.decryptPasswordOptionallyEncrypted( r.getString("PASSWORD", "") ) );
				databaseMeta.setServername( r.getString("SERVERNAME", "") );
				databaseMeta.setDataTablespace( r.getString("DATA_TBS", "") );
				databaseMeta.setIndexTablespace( r.getString("INDEX_TBS", "") );
                
                // Also, load all the properties we can find...
				final Collection<RowMetaAndData> attrs = rep.getDatabaseAttributes(id_database);
                for (RowMetaAndData row : attrs)
                {
                    String code = row.getString("CODE", "");
                    String attribute = row.getString("VALUE_STR", "");
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
	 * @param rep The repository to save the database into.
	 * 
	 * @throws KettleException if an error occurs.
	 */
	public static void saveDatabaseMeta(DatabaseMeta databaseMeta, Repository rep) throws KettleException
	{
		try
		{
            // If we don't have an ID, we don't know which entry in the database we need to update.
			// See if a database with the same name is already available...
			if (databaseMeta.getID()<=0)
			{
				databaseMeta.setID(rep.getDatabaseID(databaseMeta.getName()));
			}
			
			// Still not found? --> Insert
			if (databaseMeta.getID()<=0)
			{
				// Insert new Note in repository
				databaseMeta.setID(rep.insertDatabase(	databaseMeta.getName(), 
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
				rep.updateDatabase(	databaseMeta.getID(),
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
            rep.delDatabaseAttributes(databaseMeta.getID());
            
            // OK, now get a list of all the attributes set on the database connection...
            // 
            Properties attributes = databaseMeta.getAttributes();
            Enumeration<Object> keys = databaseMeta.getAttributes().keys();
            while (keys.hasMoreElements())
            {
                String code = (String) keys.nextElement();
                String attribute = (String)attributes.get(code);
                
                // Save this attribute
                rep.insertDatabaseAttribute(databaseMeta.getID(), code, attribute);
            }
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Error saving database connection or one of its attributes to the repository.", dbe);
		}
	}

	
	
	public static ValueMetaAndData loadValueMetaAndData(Repository rep, long id_value) throws KettleException
    {
		ValueMetaAndData valueMetaAndData = new ValueMetaAndData();
        try
        {
            RowMetaAndData r = rep.getValue(id_value);
            if (r!=null)
            {
                String name    = r.getString("NAME", null);
                int valtype    = ValueMeta.getType( r.getString("VALUE_TYPE", null) );
                boolean isNull = r.getBoolean("IS_NULL", false);
                valueMetaAndData.setValueMeta(new ValueMeta(name, valtype));

                if (isNull)
                {
                	valueMetaAndData.setValueData(null);
                }
                else
                {
                    ValueMetaInterface stringValueMeta = new ValueMeta(name, ValueMetaInterface.TYPE_STRING);
                    ValueMetaInterface valueMeta = valueMetaAndData.getValueMeta();
                    stringValueMeta.setConversionMetadata(valueMeta);
                    
                    valueMeta.setDecimalSymbol(ValueMetaAndData.VALUE_REPOSITORY_DECIMAL_SYMBOL);
                    valueMeta.setGroupingSymbol(ValueMetaAndData.VALUE_REPOSITORY_GROUPING_SYMBOL);
                    
                    switch(valueMeta.getType())
                    {
                    case ValueMetaInterface.TYPE_NUMBER:
                    	valueMeta.setConversionMask(ValueMetaAndData.VALUE_REPOSITORY_NUMBER_CONVERSION_MASK);
                    	break;
                    case ValueMetaInterface.TYPE_INTEGER:
                    	valueMeta.setConversionMask(ValueMetaAndData.VALUE_REPOSITORY_INTEGER_CONVERSION_MASK);
                    	break;
                    default:
                    	break;
                    }
                    
                    String string = r.getString("VALUE_STR", null);
                    valueMetaAndData.setValueData(stringValueMeta.convertDataUsingConversionMetaData(string));
                    
                    // OK, now comes the dirty part...
                    // We want the defaults back on there...
                    //
                    valueMeta = new ValueMeta(name, valueMeta.getType());
                }
            }
            
            return valueMetaAndData;
        }
        catch(KettleException dbe)
        {
            throw new KettleException("Unable to load Value from repository with id_value="+id_value, dbe);
        }
    }
    

}
