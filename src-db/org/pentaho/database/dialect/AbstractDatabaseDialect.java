package org.pentaho.database.dialect;

import java.util.Iterator;
import java.util.Map;

import org.pentaho.database.model.DatabaseAccessType;
import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.model.IDatabaseType;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.row.ValueMetaInterface;

public abstract class AbstractDatabaseDialect implements IDatabaseDialect {

  /**
   * Use this length in a String value to indicate that you want to use a CLOB in stead of a normal text field.
   */
  public static final int CLOB_LENGTH = 9999999;

  public abstract IDatabaseType getDatabaseType();
  
  /* 
   ********************************************************************************
   * DEFAULT SETTINGS FOR ALL DATABASES                                           *
   ********************************************************************************
   */ 

  /* (non-Javadoc)
   * @see org.pentaho.database.dialect.IDatabaseDialect#getDefaultDatabasePort()
   */
  public int getDefaultDatabasePort()
  {
    return getDatabaseType().getDefaultDatabasePort();
  }

  /* (non-Javadoc)
   * @see org.pentaho.database.dialect.IDatabaseDialect#supportsSetCharacterStream()
   */
  public boolean supportsSetCharacterStream()
  {
    return true;
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.database.dialect.IDatabaseDialect#supportsAutoInc()
   */
  public boolean supportsAutoInc()
  {
    return true;
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.database.dialect.IDatabaseDialect#getLimitClause(int)
   */
  public String getLimitClause(int nrRows)
  {
    return "";  
  }

  /* (non-Javadoc)
   * @see org.pentaho.database.dialect.IDatabaseDialect#getNotFoundTK(boolean)
   */
  public int getNotFoundTK(boolean use_autoinc)
  {
    return 0;
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.database.dialect.IDatabaseDialect#getSQLNextSequenceValue(java.lang.String)
   */
  public String getSQLNextSequenceValue(String sequenceName)
  {
    return "";
  }
    
    /* (non-Javadoc)
     * @see org.pentaho.database.dialect.IDatabaseDialect#getSQLCurrentSequenceValue(java.lang.String)
     */
    public String getSQLCurrentSequenceValue(String sequenceName)
    {
        return "";
    }
    
    /* (non-Javadoc)
     * @see org.pentaho.database.dialect.IDatabaseDialect#getSQLSequenceExists(java.lang.String)
     */
    public String getSQLSequenceExists(String sequenceName)
    {
        return "";
    }


  /* (non-Javadoc)
   * @see org.pentaho.database.dialect.IDatabaseDialect#isFetchSizeSupported()
   */
  public boolean isFetchSizeSupported()
  {
    return true;
  }

  /* (non-Javadoc)
   * @see org.pentaho.database.dialect.IDatabaseDialect#needsPlaceHolder()
   */
  public boolean needsPlaceHolder()
  {
    return false;
  }

  /* (non-Javadoc)
   * @see org.pentaho.database.dialect.IDatabaseDialect#supportsSchemas()
   */
  public boolean supportsSchemas()
  {
    return true;
  }

    /* (non-Javadoc)
     * @see org.pentaho.database.dialect.IDatabaseDialect#supportsCatalogs()
     */
    public boolean supportsCatalogs()
    {
        return true;
    }

  /* (non-Javadoc)
   * @see org.pentaho.database.dialect.IDatabaseDialect#supportsEmptyTransactions()
   */
  public boolean supportsEmptyTransactions()
  {
    return true;
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.database.dialect.IDatabaseDialect#getFunctionSum()
   */
  public String getFunctionSum()
  {
    return "SUM";
  }

  /* (non-Javadoc)
   * @see org.pentaho.database.dialect.IDatabaseDialect#getFunctionAverage()
   */
  public String getFunctionAverage()
  {
    return "AVG";
  }

  /* (non-Javadoc)
   * @see org.pentaho.database.dialect.IDatabaseDialect#getFunctionMinimum()
   */
  public String getFunctionMinimum()
  {
    return "MIN";
  }


  /* (non-Javadoc)
   * @see org.pentaho.database.dialect.IDatabaseDialect#getFunctionMaximum()
   */
  public String getFunctionMaximum()
  {
    return "MAX";
  }

  /* (non-Javadoc)
   * @see org.pentaho.database.dialect.IDatabaseDialect#getFunctionCount()
   */
  public String getFunctionCount()
  {
    return "COUNT";
  }

  /* (non-Javadoc)
   * @see org.pentaho.database.dialect.IDatabaseDialect#getSchemaTableCombination(java.lang.String, java.lang.String)
   */
  public String getSchemaTableCombination(String schema_name, String table_part)
  {
    return schema_name+"."+table_part;
  }

  /* (non-Javadoc)
   * @see org.pentaho.database.dialect.IDatabaseDialect#getMaxTextFieldLength()
   */
  public int getMaxTextFieldLength()
  {
    return CLOB_LENGTH;
  }

  /* (non-Javadoc)
   * @see org.pentaho.database.dialect.IDatabaseDialect#getMaxVARCHARLength()
   */
  public int getMaxVARCHARLength()
  {
    return CLOB_LENGTH;
  }

  
  /* (non-Javadoc)
   * @see org.pentaho.database.dialect.IDatabaseDialect#supportsTransactions()
   */
  public boolean supportsTransactions()
  {
    return true;
  }

  /* (non-Javadoc)
   * @see org.pentaho.database.dialect.IDatabaseDialect#supportsSequences()
   */
  public boolean supportsSequences()
  {
    return false;
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.database.dialect.IDatabaseDialect#supportsBitmapIndex()
   */
  public boolean supportsBitmapIndex()
  {
    return true;
  }

  /* (non-Javadoc)
   * @see org.pentaho.database.dialect.IDatabaseDialect#supportsSetLong()
   */
  public boolean supportsSetLong()
  {
    return true;
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.database.dialect.IDatabaseDialect#getDropColumnStatement(java.lang.String, org.pentaho.di.core.row.ValueMetaInterface, java.lang.String, boolean, java.lang.String, boolean)
   */
  public String getDropColumnStatement(String tablename, ValueMetaInterface v, String tk, boolean use_autoinc, String pk, boolean semicolon)
  {
    return "ALTER TABLE "+tablename+" DROP "+v.getName()+Const.CR;
  }

  /* (non-Javadoc)
   * @see org.pentaho.database.dialect.IDatabaseDialect#getReservedWords()
   */
  public String[] getReservedWords()
  {
    return new String[] {};
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.database.dialect.IDatabaseDialect#quoteReservedWords()
   */
  public boolean quoteReservedWords()
  {
    return true;
  }

  /* (non-Javadoc)
   * @see org.pentaho.database.dialect.IDatabaseDialect#getStartQuote()
   */
  public String getStartQuote()
  {
    return "\"";
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.database.dialect.IDatabaseDialect#getEndQuote()
   */
  public String getEndQuote()
  {
    return "\"";
  }

  /* (non-Javadoc)
   * @see org.pentaho.database.dialect.IDatabaseDialect#supportsRepository()
   */
  public boolean supportsRepository()
  {
    return true;
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.database.dialect.IDatabaseDialect#getTableTypes()
   */
  public String[] getTableTypes()
  {
    return new String[] { "TABLE" };
  }

  /* (non-Javadoc)
   * @see org.pentaho.database.dialect.IDatabaseDialect#getViewTypes()
   */
  public String[] getViewTypes()
  {
    return new String[] { "VIEW" };
  }

  /* (non-Javadoc)
   * @see org.pentaho.database.dialect.IDatabaseDialect#getSynonymTypes()
   */
  public String[] getSynonymTypes()
  {
    return new String[] { "SYNONYM" };
  }


  /* (non-Javadoc)
   * @see org.pentaho.database.dialect.IDatabaseDialect#useSchemaNameForTableList()
   */
  public boolean useSchemaNameForTableList()
  {
    return false;
  }

  /* (non-Javadoc)
   * @see org.pentaho.database.dialect.IDatabaseDialect#supportsViews()
   */
  public boolean supportsViews()
  {
    return true;
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.database.dialect.IDatabaseDialect#supportsSynonyms()
   */
  public boolean supportsSynonyms()
  {
    return false;
  }
  
  

  /* (non-Javadoc)
   * @see org.pentaho.database.dialect.IDatabaseDialect#getSQLListOfProcedures()
   */
  public String getSQLListOfProcedures(IDatabaseConnection connection)
  {
    return null;
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.database.dialect.IDatabaseDialect#getTruncateTableStatement(java.lang.String)
   */
  public String getTruncateTableStatement(String tableName)
  {
      return "TRUNCATE TABLE "+tableName;
  }

  /* (non-Javadoc)
   * @see org.pentaho.database.dialect.IDatabaseDialect#getSQLQueryFields(java.lang.String)
   */
  public String getSQLQueryFields(String tableName)
  {
      return "SELECT * FROM "+tableName;
  }

    /* (non-Javadoc)
     * @see org.pentaho.database.dialect.IDatabaseDialect#supportsFloatRoundingOnUpdate()
     */
    public boolean supportsFloatRoundingOnUpdate()
    {
        return true;
    }
    
    /* (non-Javadoc)
     * @see org.pentaho.database.dialect.IDatabaseDialect#getSQLLockTables(java.lang.String[])
     */
    public String getSQLLockTables(String tableNames[])
    {
        return null;
    }
    
    /* (non-Javadoc)
     * @see org.pentaho.database.dialect.IDatabaseDialect#getSQLUnlockTables(java.lang.String[])
     */
    public String getSQLUnlockTables(String tableNames[])
    {
        return null;
    }
    

    /* (non-Javadoc)
     * @see org.pentaho.database.dialect.IDatabaseDialect#supportsTimeStampToDateConversion()
     */
    public boolean supportsTimeStampToDateConversion()
    {
        return true;
    }

    /* (non-Javadoc)
     * @see org.pentaho.database.dialect.IDatabaseDialect#supportsBatchUpdates()
     */
    public boolean supportsBatchUpdates()
    {
        return true;
    }

    /* (non-Javadoc)
     * @see org.pentaho.database.dialect.IDatabaseDialect#supportsBooleanDataType()
     */
    public boolean supportsBooleanDataType()
    {
//        String usePool = attributes.getProperty(ATTRIBUTE_SUPPORTS_BOOLEAN_DATA_TYPE, "N");
//        return "Y".equalsIgnoreCase(usePool);
      return true;
    }
    
//    /**
//     * @param b Set to true if the database supports a boolean, bit, logical, ... datatype
//     */
//  public void setSupportsBooleanDataType(boolean b) 
//  {
//    attributes.setProperty(ATTRIBUTE_SUPPORTS_BOOLEAN_DATA_TYPE, b?"Y":"N");
//  }

    /* (non-Javadoc)
     * @see org.pentaho.database.dialect.IDatabaseDialect#isDefaultingToUppercase()
     */
    public boolean isDefaultingToUppercase()
    {
        return true;
    }
    

    /* (non-Javadoc)
     * @see org.pentaho.database.dialect.IDatabaseDialect#supportsSetMaxRows()
     */
    public boolean supportsSetMaxRows()
    {
        return true;
    }
    

    /* (non-Javadoc)
     * @see org.pentaho.database.dialect.IDatabaseDialect#getSQLTableExists(java.lang.String)
     */
    public String getSQLTableExists(String tablename)
    {
        return "SELECT 1 FROM "+tablename;
    }
    /* (non-Javadoc)
     * @see org.pentaho.database.dialect.IDatabaseDialect#getSQLColumnExists(java.lang.String, java.lang.String)
     */
    public String getSQLColumnExists(String columnname, String tablename)
    {
        return "SELECT " + columnname + " FROM "+tablename;
    }
    /* (non-Javadoc)
     * @see org.pentaho.database.dialect.IDatabaseDialect#needsToLockAllTables()
     */
    public boolean needsToLockAllTables()
    {
        return true;
    }
    

    /* (non-Javadoc)
     * @see org.pentaho.database.dialect.IDatabaseDialect#isRequiringTransactionsOnQueries()
     */
    public boolean isRequiringTransactionsOnQueries()
    {
      return true;
    }

	public String getURL(IDatabaseConnection connection) throws KettleDatabaseException {
		if (connection.getAccessType() == DatabaseAccessType.ODBC) {
			return "jdbc:odbc:" + connection.getDatabaseName();
		} else {
			// Embedded databases don't specify a hostname or a port, just a database...
			//
			if (Const.isEmpty(connection.getHostname())) {
				return getNativeJdbcPre() + connection.getHostname() + "/" + connection.getDatabaseName();
			} else {
				// Database and port are optionally for remote databases...
				//
				if (Const.isEmpty(connection.getDatabaseName())) {
					if (Const.isEmpty(connection.getDatabasePort())) {
						return getNativeJdbcPre() + connection.getHostname();
					} else {
						return getNativeJdbcPre() + connection.getHostname() + ":" + connection.getDatabasePort();
					}
				} else {
					if (Const.isEmpty(connection.getDatabasePort())) {
						return getNativeJdbcPre() + connection.getHostname() + "/" + connection.getDatabaseName();
					} else {
						return getNativeJdbcPre() + connection.getHostname() + ":" + connection.getDatabasePort() + "/" + connection.getDatabaseName();
					}
				}
			}
		}
	}

    public String getURLWithExtraOptions(IDatabaseConnection connection) throws KettleDatabaseException {
      StringBuffer url = new StringBuffer(getURL(connection));
      if (supportsOptionsInURL()) {
          // OK, now add all the options...
          String optionIndicator = getExtraOptionIndicator();
          String optionSeparator = getExtraOptionSeparator();
          String valueSeparator = getExtraOptionValueSeparator();
          
          Map<String, String> map = connection.getExtraOptions();
          if (map.size()>0) {
              Iterator<String> iterator = map.keySet().iterator();
              boolean first=true;
              while (iterator.hasNext()) {
                  String typedParameter=(String)iterator.next();
                  int dotIndex = typedParameter.indexOf('.');
                  if (dotIndex>=0) {
                      String typeCode = typedParameter.substring(0,dotIndex);
                      String parameter = typedParameter.substring(dotIndex+1);
                      String value = map.get(typedParameter);
                      
                      // Only add to the URL if it's the same database type code...
                      //
                      if (connection.getDatabaseType().getShortName().equals(typeCode)) {
                          if (first && url.indexOf(valueSeparator) == -1) { 
                            url.append(optionIndicator);
                          } else {
                            url.append(optionSeparator);
                          }

                          url.append(parameter);
                          if (!Const.isEmpty(value)) {
                              url.append(valueSeparator).append(value);
                          }
                          first=false;
                      }
                  }
              }
          }
      } else {
          // We need to put all these options in a Properties file later (Oracle & Co.)
          // This happens at connect time...
      }
      return url.toString();
    }
    
    // public abstract String getSQLQueryColumnFields(String columnname, String tableName);
    
    public abstract String getAddColumnStatement(String tablename, ValueMetaInterface v, String tk, boolean use_autoinc, String pk, boolean semicolon);
    
    public abstract String getModifyColumnStatement(String tablename, ValueMetaInterface v, String tk, boolean use_autoinc, String pk, boolean semicolon);

    public abstract String getFieldDefinition(ValueMetaInterface v, String tk, String pk, boolean use_autoinc, boolean add_fieldname, boolean add_cr);
    
    public String getExtraOptionsHelpText() {
      return getDatabaseType().getExtraOptionsHelpUrl();
    }
    
    public abstract String[] getUsedLibraries();
    
    public abstract String getNativeDriver();
    
    /**
     * @return true if the database supports connection options in the URL, false if they are put in a Properties object.
     */
    public boolean supportsOptionsInURL()
    {
        return true;
    }
    
    protected abstract String getNativeJdbcPre();
    
    public IDatabaseConnection createNativeConnection(String jdbcUrl) {
      if (!jdbcUrl.startsWith(getNativeJdbcPre())) {
        throw new RuntimeException("JDBC URL " + jdbcUrl + " does not start with " + getNativeJdbcPre());
      }
      DatabaseConnection dbconn = new DatabaseConnection();
      dbconn.setDatabaseType(getDatabaseType());
      dbconn.setAccessType(DatabaseAccessType.NATIVE);
      String str = jdbcUrl.substring(getNativeJdbcPre().length());
      String hostname = null;
      String port = null;
      String databaseNameAndParams = null;
      
      // hostname:port/dbname
      // hostname:port
      // hostname/dbname
      // dbname
      
      if (str.indexOf(":") >= 0) {
        hostname = str.substring(0, str.indexOf(":"));
        str = str.substring(str.indexOf(":") + 1);
        if (str.indexOf("/") >= 0) {
          port = str.substring(0, str.indexOf("/")); 
          databaseNameAndParams = str.substring(str.indexOf("/")+1);
        } else {
          port = str;
        }
      } else {
        if (str.indexOf("/") >= 0) {
          hostname = str.substring(0, str.indexOf("/"));
          databaseNameAndParams = str.substring(str.indexOf("/")+1);
        } else {
          databaseNameAndParams = str;
        }
      }
      if (hostname != null) {
        dbconn.setHostname(hostname);
      }
      if (port != null) {
        dbconn.setDatabasePort(port);
      }
      
      // parse parameters out of URL
      if (databaseNameAndParams != null) {
        setDatabaseNameAndParams(dbconn, databaseNameAndParams);
      }
      return dbconn;
    }
    
    protected void setDatabaseNameAndParams(DatabaseConnection dbconn, String databaseNameAndParams) {
      if (supportsOptionsInURL()) {
        int paramIndex = databaseNameAndParams.indexOf(getExtraOptionIndicator());
        if (paramIndex >= 0) {
          String params = databaseNameAndParams.substring(paramIndex + 1);
          databaseNameAndParams = databaseNameAndParams.substring(0, paramIndex);
          String paramData[] = params.split(getExtraOptionSeparator());
          for (String param : paramData) {
            String nameAndValue[] = param.split(getExtraOptionValueSeparator());
            if (nameAndValue[0] != null && nameAndValue[0].trim().length() > 0) {
              if (nameAndValue.length == 1) {
                dbconn.addExtraOption(dbconn.getDatabaseType().getShortName(), nameAndValue[0], "");
              } else {
                dbconn.addExtraOption(dbconn.getDatabaseType().getShortName(), nameAndValue[0], nameAndValue[1]);
              }
            }
          }
        }
      }
      dbconn.setDatabaseName(databaseNameAndParams);      
    }
    
    public String getDriverClass(IDatabaseConnection connection)
    {
      if (connection.getAccessType()==DatabaseAccessType.ODBC)
      {
        return "sun.jdbc.odbc.JdbcOdbcDriver";
      }
      else
      {
        return getNativeDriver();
      }
    }
    
    /**
     * @return true if the database JDBC driver supports getBlob on the resultset.  If not we must use getBytes() to get the data.
     */
    public boolean supportsGetBlob()
    {
        return true;
    }

    /**
     * @return The extra option separator in database URL for this platform (usually this is semicolon ; ) 
     */
    public String getExtraOptionSeparator()
    {
        return ";";
    }
    
    /**
     * @return The extra option value separator in database URL for this platform (usually this is the equal sign = ) 
     */
    public String getExtraOptionValueSeparator()
    {
        return "=";
    }

    
    /**
     * @return This indicator separates the normal URL from the options
     */
    public String getExtraOptionIndicator()
    {
        return ";";
    }
    
    public String getDatabaseFactoryName() {
    	return null;
    }
}
