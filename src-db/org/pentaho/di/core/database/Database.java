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
 /**********************************************************************
 **                                                                   **
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

 

package org.pentaho.di.core.database;

import java.io.StringReader;
import java.sql.BatchUpdateException;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.DBCache;
import org.pentaho.di.core.DBCacheEntry;
import org.pentaho.di.core.ProgressMonitorListener;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.map.DatabaseConnectionMap;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleDatabaseBatchException;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogStatus;
import org.pentaho.di.core.logging.LogTableField;
import org.pentaho.di.core.logging.LogTableInterface;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.ObjectRevision;
import org.pentaho.di.repository.RepositoryDirectory;


/**
 * Database handles the process of connecting to, reading from, writing to and updating databases.
 * The database specific parameters are defined in DatabaseInfo.
 * 
 * @author Matt
 * @since 05-04-2003
 *
 */
public class Database implements VariableSpace, LoggingObjectInterface
{
	private static Class<?> PKG = Database.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private DatabaseMeta databaseMeta;
	
	private int    rowlimit;
	private int    commitsize;

	private Connection connection;
		
	private Statement sel_stmt;
	private PreparedStatement pstmt;
	private PreparedStatement prepStatementLookup;
	private PreparedStatement prepStatementUpdate;
	private PreparedStatement prepStatementInsert;
	private PreparedStatement pstmt_seq;
	private CallableStatement cstmt;
		
	// private ResultSetMetaData rsmd;
	private DatabaseMetaData  dbmd;
	
	private RowMetaInterface rowMeta; 
	
	private int written;
	
	private LogChannelInterface log;
  private LoggingObjectInterface parentLoggingObject;
  private int logLevel = LogWriter.LOG_LEVEL_DEFAULT;

    /**
     * Number of times a connection was opened using this object.
     * Only used in the context of a database connection map
     */
    private int opened;

    /**
     * The copy is equal to opened at the time of creation.
     */
    private int copy; 

    private String connectionGroup;
    private String partitionId;
    
    private VariableSpace variables = new Variables();
    
    
	/**
	 * Construct a new Database Connection
	 * @param databaseMeta The Database Connection Info to construct the connection with.
	 * @deprecated Please specify the parent object so that we can see which object is initiating a database connection
	 */
	public Database(DatabaseMeta databaseMeta)
	{
		this.parentLoggingObject = null;
		this.databaseMeta = databaseMeta;
		shareVariablesWith(databaseMeta);
		
		log=new LogChannel(this); // In this case we don't have the parent object, so we don't know which object makes the connection. 

		pstmt = null;
		rowMeta = null;
		dbmd = null;
		
		rowlimit=0;
		
		written=0;
				
		if(log.isDetailed()) log.logDetailed("New database connection defined");
	}
	
	/**
	 * Construct a new Database Connection
	 * @param databaseMeta The Database Connection Info to construct the connection with.
	 */
	public Database(LoggingObjectInterface parentObject, DatabaseMeta databaseMeta)
	{
		this.parentLoggingObject = parentObject;
		this.databaseMeta = databaseMeta;
		shareVariablesWith(databaseMeta);
		
		log=new LogChannel(this);

		pstmt = null;
		rowMeta = null;
		dbmd = null;
		
		rowlimit=0;
		
		written=0;
				
		if(log.isDetailed()) log.logDetailed("New database connection defined");
	}

    
    public boolean equals(Object obj)
    {
        Database other = (Database) obj;
        return other.databaseMeta.equals(other.databaseMeta);
    }
    
    /**
     * Allows for the injection of a "life" connection, generated by a piece of software outside of Kettle.
     * @param connection
     */
    public void setConnection(Connection connection) {
		this.connection = connection;
	}
	
	/**
     * @return Returns the connection.
     */
    public Connection getConnection()
    {
        return connection;
    }
	
	/**
	 * Set the maximum number of records to retrieve from a query.
	 * @param rows
	 */
	public void setQueryLimit(int rows)
	{
		rowlimit = rows;
	}
	
	/**
     * @return Returns the prepStatementInsert.
     */
    public PreparedStatement getPrepStatementInsert()
    {
        return prepStatementInsert;
    }
    
    /**
     * @return Returns the prepStatementLookup.
     */
    public PreparedStatement getPrepStatementLookup()
    {
        return prepStatementLookup;
    }
    
    /**
     * @return Returns the prepStatementUpdate.
     */
    public PreparedStatement getPrepStatementUpdate()
    {
        return prepStatementUpdate;
    }

    /**
     * Open the database connection.
     * @throws KettleDatabaseException if something went wrong.
     */
    public void connect() throws KettleDatabaseException
    {
        connect(null);
    }

    /**
     * Open the database connection.
     * @param partitionId the partition ID in the cluster to connect to.
     * @throws KettleDatabaseException if something went wrong.
     */
    public void connect(String partitionId) throws KettleDatabaseException
    {
        connect(null, partitionId);
    }

    public synchronized void connect(String group, String partitionId) throws KettleDatabaseException
    {
        // Before anything else, let's see if we already have a connection defined for this group/partition!
        // The group is called after the thread-name of the transformation or job that is running
        // The name of that threadname is expected to be unique (it is in Kettle)
        // So the deal is that if there is another thread using that, we go for it. 
        // 
        if (!Const.isEmpty(group))
        {
            this.connectionGroup = group;
            this.partitionId = partitionId;
            
            DatabaseConnectionMap map = DatabaseConnectionMap.getInstance();
            
            // Try to find the connection for the group
            Database lookup = map.getDatabase(group, partitionId, this);
            if (lookup==null) // We already opened this connection for the partition & database in this group
            {
                // Do a normal connect and then store this database object for later re-use.
                normalConnect(partitionId);
                opened++;
                copy = opened;
                
                map.storeDatabase(group, partitionId, this);
            }
            else
            {
                connection = lookup.getConnection();
                lookup.setOpened(lookup.getOpened()+1); // if this counter hits 0 again, close the connection.
                copy = lookup.getOpened();
            }
        }
        else
        {
            // Proceed with a normal connect
            normalConnect(partitionId);
        }
    }
    
	/**
	 * Open the database connection.
     * @param partitionId the partition ID in the cluster to connect to.
	 * @throws KettleDatabaseException if something went wrong.
	 */
	public void normalConnect(String partitionId) throws KettleDatabaseException
	{
        if (databaseMeta==null)
        {
            throw new KettleDatabaseException("No valid database connection defined!");
        }
        
        try
		{
            // First see if we use connection pooling...
            //
            if ( databaseMeta.isUsingConnectionPool() &&  // default = false for backward compatibility
                 databaseMeta.getAccessType()!=DatabaseMeta.TYPE_ACCESS_JNDI // JNDI does pooling on it's own.
                )
            {
                try
                {
                    this.connection = ConnectionPoolUtil.getConnection(log, databaseMeta, partitionId);
                } 
                catch (Exception e)
                {
                    throw new KettleDatabaseException("Error occured while trying to connect to the database", e);
                }
            }
            else
            {
    			connectUsingClass(databaseMeta.getDriverClass(), partitionId );
    			if(log.isDetailed()) log.logDetailed("Connected to database.");
                
                // See if we need to execute extra SQL statemtent...
                String sql = environmentSubstitute( databaseMeta.getConnectSQL() ); 
                
                // only execute if the SQL is not empty, null and is not just a bunch of spaces, tabs, CR etc.
                if (!Const.isEmpty(sql) && !Const.onlySpaces(sql))
                {
                    execStatements(sql);
                    if(log.isDetailed()) log.logDetailed("Executed connect time SQL statements:"+Const.CR+sql);
                }
            }
		}
		catch(Exception e)
		{
			throw new KettleDatabaseException("Error occured while trying to connect to the database", e);
		}
	}

	
	/**
	 * Initialize by getting the connection from a javax.sql.DataSource. This method uses the
	 * DataSourceProviderFactory to get the provider of DataSource objects.
	 * @param dataSourceName
	 * @throws KettleDatabaseException
	 */
	private void initWithNamedDataSource(String dataSourceName) throws KettleDatabaseException {
    connection = null;
    DataSource dataSource = DataSourceProviderFactory.getDataSourceProviderInterface().getNamedDataSource(dataSourceName);
    if (dataSource != null) {
      try {
        connection = dataSource.getConnection();
      } catch (SQLException e) {
        throw new KettleDatabaseException( "Invalid JNDI connection "+ dataSourceName + " : " + e.getMessage()); //$NON-NLS-1$
      }
      if (connection == null) {
        throw new KettleDatabaseException( "Invalid JNDI connection "+ dataSourceName); //$NON-NLS-1$
      }
    } else {
      throw new KettleDatabaseException( "Invalid JNDI connection "+ dataSourceName); //$NON-NLS-1$
    }
	}

	/**
	 * Connect using the correct classname 
	 * @param classname for example "org.gjt.mm.mysql.Driver"
	 * @return true if the connect was succesfull, false if something went wrong.
	 */
	private void connectUsingClass(String classname, String partitionId) throws KettleDatabaseException
	{
		// Install and load the jdbc Driver

		// first see if this is a JNDI connection
		if( databaseMeta.getAccessType() == DatabaseMeta.TYPE_ACCESS_JNDI ) {
		  initWithNamedDataSource( environmentSubstitute(databaseMeta.getDatabaseName()) );
			return;
		}
		
		try 
		{
			Class.forName(classname);
		}
		catch(NoClassDefFoundError e)
		{ 
			throw new KettleDatabaseException("Exception while loading class", e);
		}
		catch(ClassNotFoundException e)
		{
			throw new KettleDatabaseException("Exception while loading class", e);
		}
		catch(Exception e)
		{ 
			throw new KettleDatabaseException("Exception while loading class", e);
		}

		try 
		{
            String url;
            
            if (databaseMeta.isPartitioned() && !Const.isEmpty(partitionId))
            {
                url = environmentSubstitute(databaseMeta.getURL(partitionId));
            }
            else
            {
                url = environmentSubstitute(databaseMeta.getURL()); 
            }
            
            String clusterUsername=null;
            String clusterPassword=null;
            if (databaseMeta.isPartitioned() && !Const.isEmpty(partitionId))
            {
                // Get the cluster information...
                PartitionDatabaseMeta partition = databaseMeta.getPartitionMeta(partitionId);
                if (partition!=null)
                {
                    clusterUsername = partition.getUsername();
                    clusterPassword = Encr.decryptPasswordOptionallyEncrypted(partition.getPassword());
                }
            }
            
			String username;
            String password;
            if (!Const.isEmpty(clusterUsername))
            {
                username = clusterUsername;
                password = clusterPassword; 
            }
            else
            {
                username = environmentSubstitute(databaseMeta.getUsername());
                password = Encr.decryptPasswordOptionallyEncrypted(environmentSubstitute(databaseMeta.getPassword()));
            }

            if (databaseMeta.supportsOptionsInURL())
            {
                if (!Const.isEmpty(username) || !Const.isEmpty(password))
                {
                	// also allow for empty username with given password, in this case username must be given with one space 
                    connection = DriverManager.getConnection(url, Const.NVL(username, " "), Const.NVL(password, ""));
                }
                else
                {
                    // Perhaps the username is in the URL or no username is required...
                    connection = DriverManager.getConnection(url);
                }
            }
            else
            {
                Properties properties = databaseMeta.getConnectionProperties();
                if (!Const.isEmpty(username)) properties.put("user", username);
                if (!Const.isEmpty(password)) properties.put("password", password);
                
                connection = DriverManager.getConnection(url, properties);
            }
		} 
		catch(SQLException e) 
		{
			throw new KettleDatabaseException("Error connecting to database: (using class "+classname+")", e);
		}
        catch(Throwable e)
        {
            throw new KettleDatabaseException("Error connecting to database: (using class "+classname+")", e);
        }
	}

	/**
	 * Disconnect from the database and close all open prepared statements.
	 */
	public synchronized void disconnect()
	{	
		try
		{
			if (connection==null)
            {
                return ; // Nothing to do...
            }
			if (connection.isClosed())
            {
                return ; // Nothing to do...
            }

			if (pstmt    !=null) 
            { 
                pstmt.close(); 
                pstmt=null; 
            } 
			if (prepStatementLookup!=null) 
            { 
                prepStatementLookup.close(); 
                prepStatementLookup=null; 
            } 
			if (prepStatementInsert!=null) 
			{ 
                prepStatementInsert.close(); 
                prepStatementInsert=null; 
            } 
			if (prepStatementUpdate!=null) 
            { 
                prepStatementUpdate.close(); 
                prepStatementUpdate=null; 
            } 
			if (pstmt_seq!=null) 
            { 
                pstmt_seq.close(); 
                pstmt_seq=null; 
            } 
            
            // See if there are other steps using this connection in a connection group.
            // If so, we will hold commit & connection close until then.
            // 
            if (!Const.isEmpty(connectionGroup))
            {
                return;
            }
            else
            {
                if (!isAutoCommit()) // Do we really still need this commit??
                {
                    commit();
                }
            }

            closeConnectionOnly();
		}
		catch(SQLException ex) 
		{
			log.logError("Error disconnecting from database:"+Const.CR+ex.getMessage());
            log.logError(Const.getStackTracker(ex));
		}
		catch(KettleDatabaseException dbe)
		{
			log.logError("Error disconnecting from database:"+Const.CR+dbe.getMessage());
            log.logError(Const.getStackTracker(dbe));
		}
	}
	
	/**
	 * Only for unique connections usage, typically you use disconnect() to disconnect() from the database.
	 * @throws KettleDatabaseException in case there is an error during connection close. 
	 */
	public synchronized void closeConnectionOnly() throws KettleDatabaseException {
		try
		{
			if (connection!=null) 
			{ 
				connection.close(); 
				if (!databaseMeta.isUsingConnectionPool()) 
				{
					connection=null; 
				}
			} 
			
			if(log.isDetailed()) log.logDetailed("Connection to database closed!");
		}
		catch(SQLException e) {
			throw new KettleDatabaseException("Error disconnecting from database '"+toString()+"'", e);
		}
	}
	
    /**
     * Cancel the open/running queries on the database connection
     * @throws KettleDatabaseException
     */
	public void cancelQuery() throws KettleDatabaseException
	{
        cancelStatement(pstmt);
        cancelStatement(sel_stmt);
	}
    
    /**
     * Cancel an open/running SQL statement 
     * @param statement the statement to cancel
     * @throws KettleDatabaseException
     */
    public void cancelStatement(Statement statement) throws KettleDatabaseException
    {
        try
        {
            if (statement!=null) 
            { 
                statement.cancel(); 
            } 
            if(log.isDebug()) log.logDebug("Statement canceled!");
        }
        catch(SQLException ex) 
        {
            throw new KettleDatabaseException("Error cancelling statement", ex);
        }
    }

	/**
	 * Specify after how many rows a commit needs to occur when inserting or updating values.
	 * @param commsize The number of rows to wait before doing a commit on the connection.
	 */
	public void setCommit(int commsize)
	{
        commitsize=commsize;
        String onOff = (commitsize<=0?"on":"off");
        try
        {
            connection.setAutoCommit(commitsize<=0);
            if(log.isDetailed()) log.logDetailed("Auto commit "+onOff);
        }
        catch(Exception e)
        {
            log.logError("Can't turn auto commit "+onOff);
        }
	}
	
	public void setAutoCommit(boolean useAutoCommit) throws KettleDatabaseException {
		try {
			connection.setAutoCommit(useAutoCommit);
		} catch (SQLException e) {
			if (useAutoCommit) {
				throw new KettleDatabaseException(BaseMessages.getString(PKG, "Database.Exception.UnableToEnableAutoCommit", toString()));
			} else {
				throw new KettleDatabaseException(BaseMessages.getString(PKG, "Database.Exception.UnableToDisableAutoCommit", toString()));
			}
			
		}
	}
	
    /**
     * Perform a commit the connection if this is supported by the database
     */
    public void commit() throws KettleDatabaseException
    {
        commit(false);
    }
    
	public void commit(boolean force) throws KettleDatabaseException
	{
		try
		{
		    // Don't do the commit, wait until the end of the transformation.  
            // When the last database copy (opened counter) is about to be closed, we do a commit 
            // There is one catch, we need to catch the rollback
            // The transformation will stop everything and then we'll do the rollback.
            // The flag is in "performRollback", private only
            //
            if (!Const.isEmpty(connectionGroup) && !force)
            {
                return; 
            }
			if (getDatabaseMetaData().supportsTransactions())
			{
				if (log.isDebug()) log.logDebug("Commit on database connection ["+toString()+"]");
				connection.commit();
			}
			else
			{
				if(log.isDetailed()) log.logDetailed("No commit possible on database connection ["+toString()+"]");
			}
		}
		catch(Exception e)
		{
			if (databaseMeta.supportsEmptyTransactions())
				throw new KettleDatabaseException("Error comitting connection", e);
		}
	}

    public void rollback() throws KettleDatabaseException
    {
        rollback(false);
    }

	public void rollback(boolean force) throws KettleDatabaseException
	{
		try
		{
            if (!Const.isEmpty(connectionGroup) && !force)
            {
                return; // Will be handled by Trans --> endProcessing() 
            }
            if (getDatabaseMetaData().supportsTransactions())
            {
                if (connection!=null) {
                	if (log.isDebug()) log.logDebug("Rollback on database connection ["+toString()+"]");
                	connection.rollback();
                }
            }
            else
            {
            	if(log.isDetailed()) log.logDetailed("No rollback possible on database connection ["+toString()+"]");
            }
			
		}
		catch(SQLException e)
		{
			throw new KettleDatabaseException("Error performing rollback on connection", e);
		}
	}

    /**
     * Prepare inserting values into a table, using the fields & values in a Row
     * @param rowMeta The row metadata to determine which values need to be inserted
     * @param table The name of the table in which we want to insert rows
     * @throws KettleDatabaseException if something went wrong.
     */
    public void prepareInsert(RowMetaInterface rowMeta, String tableName) throws KettleDatabaseException
    {
        prepareInsert(rowMeta, null, tableName);
    }
    
	/**
	 * Prepare inserting values into a table, using the fields & values in a Row
	 * @param rowMeta The metadata row to determine which values need to be inserted
     * @param schemaName The name of the schema in which we want to insert rows
	 * @param tableName The name of the table in which we want to insert rows
	 * @throws KettleDatabaseException if something went wrong.
	 */
	public void prepareInsert(RowMetaInterface rowMeta, String schemaName, String tableName) throws KettleDatabaseException
	{
		if (rowMeta.size()==0)
		{
			throw new KettleDatabaseException("No fields in row, can't insert!");
		}
		
		String ins = getInsertStatement(schemaName, tableName, rowMeta);
				
		if(log.isDetailed()) log.logDetailed("Preparing statement: "+Const.CR+ins);
		prepStatementInsert=prepareSQL(ins);
	}

    /**
     * Prepare a statement to be executed on the database. (does not return generated keys)
     * @param sql The SQL to be prepared
     * @return The PreparedStatement object.
     * @throws KettleDatabaseException
     */
	public PreparedStatement prepareSQL(String sql)
	 throws KettleDatabaseException
	{
		return prepareSQL(sql, false);
	}

    /**
     * Prepare a statement to be executed on the database.
     * @param sql The SQL to be prepared
     * @param returnKeys set to true if you want to return generated keys from an insert statement 
     * @return The PreparedStatement object.
     * @throws KettleDatabaseException
     */
	public PreparedStatement prepareSQL(String sql, boolean returnKeys) throws KettleDatabaseException
	{
		try
		{
			if (returnKeys)
			{
				return connection.prepareStatement(databaseMeta.stripCR(sql), Statement.RETURN_GENERATED_KEYS);
			}
			else
			{
				return connection.prepareStatement(databaseMeta.stripCR(sql));
			}
		}
		catch(SQLException ex) 
		{
			throw new KettleDatabaseException("Couldn't prepare statement:"+Const.CR+sql, ex);
		}
	}

	public void closeLookup() throws KettleDatabaseException
	{
		closePreparedStatement(pstmt);
        pstmt=null;
	}

	public void closePreparedStatement(PreparedStatement ps) throws KettleDatabaseException

	{
		if (ps!=null)
		{
			try 
			{
				ps.close();
			}
			catch(SQLException e)
			{
				throw new KettleDatabaseException("Error closing prepared statement", e);
			}
		}
	}
	
	
	public void closeInsert() throws KettleDatabaseException

	{
		if (prepStatementInsert!=null)
		{
			try 
			{
				prepStatementInsert.close();
				prepStatementInsert = null;
			}
			catch(SQLException e)
			{
				throw new KettleDatabaseException("Error closing insert prepared statement.", e);
			}
		}
	}
	
	public void closeUpdate() throws KettleDatabaseException
	{
		if (prepStatementUpdate!=null)
		{
			try 
			{
				prepStatementUpdate.close();
                prepStatementUpdate=null;
			}
			catch(SQLException e)
			{
				throw new KettleDatabaseException("Error closing update prepared statement.", e);
			}
		}
	}


	public void setValues(RowMetaInterface rowMeta, Object[] data) throws KettleDatabaseException
	{
		setValues(rowMeta, data, pstmt);
	}

    public void setValues(RowMetaAndData row) throws KettleDatabaseException
    {
        setValues(row.getRowMeta(), row.getData());
    }
    
	public void setValuesInsert(RowMetaInterface rowMeta, Object[] data) throws KettleDatabaseException
	{
		setValues(rowMeta, data, prepStatementInsert);
	}
    
    public void setValuesInsert(RowMetaAndData row) throws KettleDatabaseException
    {
        setValues(row.getRowMeta(), row.getData(), prepStatementInsert);
    }

	public void setValuesUpdate(RowMetaInterface rowMeta, Object[] data) throws KettleDatabaseException
	{
		setValues(rowMeta, data, prepStatementUpdate);
	}
	
	public void setValuesLookup(RowMetaInterface rowMeta, Object[] data) throws KettleDatabaseException
	{
		setValues(rowMeta, data, prepStatementLookup);
	}
	
	public void setProcValues(RowMetaInterface rowMeta, Object[] data, int argnrs[], String argdir[], boolean result) throws KettleDatabaseException
	{
		int pos;
		
		if (result) pos=2; else pos=1;
		
		for (int i=0;i<argnrs.length;i++)
		{
			if (argdir[i].equalsIgnoreCase("IN") || argdir[i].equalsIgnoreCase("INOUT"))
			{
				ValueMetaInterface valueMeta = rowMeta.getValueMeta(argnrs[i]);
                Object value = data[argnrs[i]];
                
				setValue(cstmt, valueMeta, value, pos);
				pos++;
			} else {
				pos++; //next parameter when OUT
			}
		}
	}

	public void setValue(PreparedStatement ps, ValueMetaInterface v, Object object, int pos) throws KettleDatabaseException
	{
		String debug = "";

		try
		{
			switch(v.getType())
			{
			case ValueMetaInterface.TYPE_NUMBER :
                if (!v.isNull(object)) 
                {
                    debug="Number, not null, getting number from value";
                    double num = v.getNumber(object).doubleValue();
                    if (databaseMeta.supportsFloatRoundingOnUpdate() && v.getPrecision()>=0)
                    {
                        debug="Number, rounding to precision ["+v.getPrecision()+"]";
                        num = Const.round(num, v.getPrecision());
                    }
                    debug="Number, setting ["+num+"] on position #"+pos+" of the prepared statement";
                    ps.setDouble(pos, num);
                }
				else 
                {
                    ps.setNull(pos, java.sql.Types.DOUBLE);   
                }
				break;
			case ValueMetaInterface.TYPE_INTEGER:
				debug="Integer";
				if (!v.isNull(object)) 
				{
					if (databaseMeta.supportsSetLong())
					{
                        ps.setLong(pos, v.getInteger(object).longValue() );
					}
					else
					{
                        double d = v.getNumber(object).doubleValue();
					    if (databaseMeta.supportsFloatRoundingOnUpdate() && v.getPrecision()>=0)
                        {
                            ps.setDouble(pos, d );
                        }
                        else
                        {
                            ps.setDouble(pos, Const.round( d, v.getPrecision() ) );
                        }
					}
				}
				else 
                {
                    ps.setNull(pos, java.sql.Types.INTEGER);   
                }
				break;
			case ValueMetaInterface.TYPE_STRING : 
				debug="String";
				if (v.getLength()<DatabaseMeta.CLOB_LENGTH)
				{
					if (!v.isNull(object)) 
					{
						ps.setString(pos, v.getString(object));
					}
					else 
					{
						ps.setNull(pos, java.sql.Types.VARCHAR);
					}
				}
				else
				{
					if (!v.isNull(object))
					{
                        String string = v.getString(object);
                        
						int maxlen = databaseMeta.getMaxTextFieldLength();
						int len    = string.length();
						
						// Take the last maxlen characters of the string...
						int begin  = len - maxlen;
						if (begin<0) begin=0;
						
						// Get the substring!
						String logging = string.substring(begin);
	
						if (databaseMeta.supportsSetCharacterStream())
						{
                            StringReader sr = new StringReader(logging);
							ps.setCharacterStream(pos, sr, logging.length());
						}
						else
						{
							ps.setString(pos, logging);
						}
					}
					else
					{
						ps.setNull(pos, java.sql.Types.VARCHAR);
					}
				}
				break;
			case ValueMetaInterface.TYPE_DATE   : 
				debug="Date";
				if (!v.isNull(object)) 
				{
					long dat = v.getInteger(object).longValue(); // converts using Date.getTime()
                    
					if(v.getPrecision()==1 || !databaseMeta.supportsTimeStampToDateConversion())
                    {
                        // Convert to DATE!
					    java.sql.Date ddate = new java.sql.Date(dat);
                        ps.setDate(pos, ddate);
                    }
                    else
					{
                        java.sql.Timestamp sdate = new java.sql.Timestamp(dat);
					    ps.setTimestamp(pos, sdate);
					}
				}
				else
                {
                    if(v.getPrecision()==1 || !databaseMeta.supportsTimeStampToDateConversion())
                    {
                        ps.setNull(pos, java.sql.Types.DATE);
                    }
                    else
                    {
                        ps.setNull(pos, java.sql.Types.TIMESTAMP);
                    }
                }
				break;
			case ValueMetaInterface.TYPE_BOOLEAN:
				debug="Boolean";
                if (databaseMeta.supportsBooleanDataType())
                {
                    if (!v.isNull(object))
                    {
                        ps.setBoolean(pos, v.getBoolean(object).booleanValue());
                    }
                    else 
                    {
                        ps.setNull(pos, java.sql.Types.BOOLEAN);
                    }
                }
                else
                {
    				if (!v.isNull(object))
                    {
                        ps.setString(pos, v.getBoolean(object).booleanValue()?"Y":"N");
                    }
    				else 
                    {
                        ps.setNull(pos, java.sql.Types.CHAR);
                    }
                }
				break;
            case ValueMetaInterface.TYPE_BIGNUMBER:
                debug="BigNumber";
                if (!v.isNull(object)) 
                {
                    ps.setBigDecimal(pos, v.getBigNumber(object));
                }
                else 
                {
                    ps.setNull(pos, java.sql.Types.DECIMAL);   
                }
                break;
			case ValueMetaInterface.TYPE_BINARY:
				debug="Binary";
				if (!v.isNull(object)) 
				{
                    ps.setBytes(pos, v.getBinary(object));
				}
				else
				{
                    ps.setNull(pos, java.sql.Types.BINARY);   
                }
				break;                
			default:
				debug="default";
            // placeholder
                ps.setNull(pos, java.sql.Types.VARCHAR);
 				break;
			}
		}
		catch(SQLException ex) 
		{
			throw new KettleDatabaseException("Error setting value #"+pos+" ["+v.toString()+"] on prepared statement ("+debug+")"+Const.CR+ex.toString(), ex);
		}
		catch(Exception e)
		{
			throw new KettleDatabaseException("Error setting value #"+pos+" ["+(v==null?"NULL":v.toString())+"] on prepared statement ("+debug+")"+Const.CR+e.toString(), e);
		}
	}
    
    public void setValues(RowMetaAndData row, PreparedStatement ps) throws KettleDatabaseException
    {
        setValues(row.getRowMeta(), row.getData(), ps);
    }
    
    public void setValues(RowMetaInterface rowMeta, Object[] data, PreparedStatement ps) throws KettleDatabaseException
    {
        // now set the values in the row!
        for (int i=0;i<rowMeta.size();i++)
        {
            ValueMetaInterface v = rowMeta.getValueMeta(i);
            Object object = data[i];
            
            try
            {
                setValue(ps, v, object, i+1);
            }
            catch(KettleDatabaseException e)
            {
                throw new KettleDatabaseException("offending row : "+rowMeta, e);
            }
        }
    }	

    /**
     * Sets the values of the preparedStatement pstmt.
     * @param rowMeta
     * @param data
     */
    public void setValues(RowMetaInterface rowMeta, Object[] data, PreparedStatement ps, int ignoreThisValueIndex) throws KettleDatabaseException
	{
		// now set the values in the row!
        int index=0;
		for (int i=0;i<rowMeta.size();i++)
		{
            if (i!=ignoreThisValueIndex)
            {
                ValueMetaInterface v = rowMeta.getValueMeta(i);
                Object object = data[i];
                
                try
    			{
    				setValue(ps, v, object, index+1);
                    index++;
    			}
    			catch(KettleDatabaseException e)
    			{
    				throw new KettleDatabaseException("offending row : "+rowMeta, e);
    			}
            }
		}
	}

	/** 
	 * @param ps  The prepared insert statement to use
	 * @return The generated keys in auto-increment fields 
	 * @throws KettleDatabaseException in case something goes wrong retrieving the keys.
	 */
	public RowMetaAndData getGeneratedKeys(PreparedStatement ps) throws KettleDatabaseException 
	{
		ResultSet keys = null;
		try
		{
			keys=ps.getGeneratedKeys(); // 1 row of keys
			ResultSetMetaData resultSetMetaData = keys.getMetaData();
			RowMetaInterface rowMeta = getRowInfo(resultSetMetaData, false, false);

			return new RowMetaAndData(rowMeta, getRow(keys, resultSetMetaData, rowMeta));
		}
		catch(Exception ex) 
		{
			throw new KettleDatabaseException("Unable to retrieve key(s) from auto-increment field(s)", ex);
		}
		finally
		{
			if (keys!=null)
			{
				try
				{
					keys.close();
				}
				catch(SQLException e)
				{
					throw new KettleDatabaseException("Unable to close resultset of auto-generated keys", e);
				}
			}
		}
	}

    public Long getNextSequenceValue(String sequenceName, String keyfield) throws KettleDatabaseException
    {
        return getNextSequenceValue(null, sequenceName, keyfield);
    }
    
	public Long getNextSequenceValue(String schemaName, String sequenceName, String keyfield) throws KettleDatabaseException
	{
        Long retval=null;
        
        String schemaSequence = databaseMeta.getQuotedSchemaTableCombination(schemaName, sequenceName);
		
		try
		{
			if (pstmt_seq==null)
			{
				pstmt_seq=connection.prepareStatement(databaseMeta.getSeqNextvalSQL(databaseMeta.stripCR(schemaSequence)));
			}
			ResultSet rs=null;
			try 
			{
				rs = pstmt_seq.executeQuery();
			    if (rs.next())
			    {
				    retval = Long.valueOf( rs.getLong(1) );
			    }
			}
			finally 
			{
			    if ( rs != null ) rs.close();
			}
		}
		catch(SQLException ex)
		{
			throw new KettleDatabaseException("Unable to get next value for sequence : "+schemaSequence, ex);
		}
		
		return retval;
	}
	
	public void insertRow(String tableName, RowMetaInterface fields, Object[] data) throws KettleDatabaseException
	{
		insertRow(null, tableName, fields, data);
	}

	public void insertRow(String schemaName, String tableName, RowMetaInterface fields, Object[] data) throws KettleDatabaseException
	{
		prepareInsert(fields, schemaName, tableName);
		setValuesInsert(fields, data);
		insertRow();
		closeInsert();
	}

    public String getInsertStatement(String tableName, RowMetaInterface fields)
    {
        return getInsertStatement(null, tableName, fields);
    }

	public String getInsertStatement(String schemaName, String tableName, RowMetaInterface fields)
	{
		StringBuffer ins=new StringBuffer(128);
		
        String schemaTable = databaseMeta.getQuotedSchemaTableCombination(schemaName, tableName);
		ins.append("INSERT INTO ").append(schemaTable).append(" (");
		
		// now add the names in the row:
		for (int i=0;i<fields.size();i++)
		{
			if (i>0) ins.append(", ");
			String name = fields.getValueMeta(i).getName();
			ins.append(databaseMeta.quoteField(name));
		}
		ins.append(") VALUES (");
		
		// Add placeholders...
		for (int i=0;i<fields.size();i++)
		{
			if (i>0) ins.append(", ");
			ins.append(" ?");
		}
		ins.append(')');
		
		return ins.toString();
	}

	public void insertRow()
		throws KettleDatabaseException
	{
		insertRow(prepStatementInsert);	
	}

	public void insertRow(boolean batch) throws KettleDatabaseException
    {
        insertRow(prepStatementInsert, batch);
    }

	public void updateRow()
		throws KettleDatabaseException
	{
		insertRow(prepStatementUpdate);	
	}

	public void insertRow(PreparedStatement ps)
		throws KettleDatabaseException
	{
		insertRow(ps, false);
	}

    /**
     * Insert a row into the database using a prepared statement that has all values set.
     * @param ps The prepared statement
     * @param batch True if you want to use batch inserts (size = commit size)
     * @return true if the rows are safe: if batch of rows was sent to the database OR if a commit was done.
     * @throws KettleDatabaseException
     */
	public boolean insertRow(PreparedStatement ps, boolean batch) throws KettleDatabaseException
	{
		return insertRow(ps, false, true);
	}
    /**
     * Insert a row into the database using a prepared statement that has all values set.
     * @param ps The prepared statement
     * @param batch True if you want to use batch inserts (size = commit size)
     * @param handleCommit True if you want to handle the commit here after the commit size (False e.g. in case the step handles this, see TableOutput)
     * @return true if the rows are safe: if batch of rows was sent to the database OR if a commit was done.
     * @throws KettleDatabaseException
     */
	public boolean insertRow(PreparedStatement ps, boolean batch, boolean handleCommit) throws KettleDatabaseException
	{
	    String debug="insertRow start";
        boolean rowsAreSafe=false;
        
		try
		{
            // Unique connections and Batch inserts don't mix when you want to roll back on certain databases.
            // That's why we disable the batch insert in that case.
            //
            boolean useBatchInsert = batch && getDatabaseMetaData().supportsBatchUpdates() && databaseMeta.supportsBatchUpdates() && Const.isEmpty(connectionGroup);
            
			//
			// Add support for batch inserts...
			//
		    if (!isAutoCommit())
		    {
				if (useBatchInsert)
				{
				    debug="insertRow add batch";
					ps.addBatch(); // Add the batch, but don't forget to run the batch
				}
				else
				{
				    debug="insertRow exec update";
					ps.executeUpdate();
				}
		    }
		    else
		    {
		        ps.executeUpdate();
		    }

			written++;
			
			if (handleCommit) { // some steps handle the commit themselves (see e.g. TableOutput step)
				if (!isAutoCommit() && (written%commitsize)==0)
				{
					if (useBatchInsert)
					{
						debug="insertRow executeBatch commit";
	                    ps.executeBatch();
						commit();
	                    ps.clearBatch();
					}
					else
					{
					    debug="insertRow normal commit";
	                    commit();
					}
	                rowsAreSafe=true;
				}
			}
			
            return rowsAreSafe;
		}
		catch(BatchUpdateException ex)
		{
			KettleDatabaseBatchException kdbe = new KettleDatabaseBatchException("Error updating batch", ex);
		    kdbe.setUpdateCounts(ex.getUpdateCounts());
            List<Exception> exceptions = new ArrayList<Exception>();
            
            // 'seed' the loop with the root exception
            SQLException nextException = ex;
            do 
            {
                exceptions.add(nextException);
                // while current exception has next exception, add to list
            } 
            while ((nextException = nextException.getNextException())!=null);            
            kdbe.setExceptionsList(exceptions);
		    throw kdbe;
		}
		catch(SQLException ex) 
		{
		    // log.logError(Const.getStackTracker(ex));
			throw new KettleDatabaseException("Error inserting/updating row", ex);
		}
		catch(Exception e)
		{
		    // System.out.println("Unexpected exception in ["+debug+"] : "+e.getMessage());
			throw new KettleDatabaseException("Unexpected error inserting/updating row in part ["+debug+"]", e);
		}
	}
	
    /**
     * Clears batch of insert prepared statement
     * @deprecated
     * @throws KettleDatabaseException
     */
	public void clearInsertBatch() throws KettleDatabaseException
	{
        clearBatch(prepStatementInsert);
	}
    
    public void clearBatch(PreparedStatement preparedStatement) throws KettleDatabaseException
    {
        try
        {
            preparedStatement.clearBatch();
        }
        catch(SQLException e)
        {
            throw new KettleDatabaseException("Unable to clear batch for prepared statement", e);
        }
    }

	public void insertFinished(boolean batch) throws KettleDatabaseException
	{
	    insertFinished(prepStatementInsert, batch);
		prepStatementInsert = null;
	}
	
		/**
		 * Close the prepared statement of the insert statement.
		 * 
		 * @param ps The prepared statement to empty and close.
		 * @param batch true if you are using batch processing
		 * @param psBatchCounter The number of rows on the batch queue
		 * @throws KettleDatabaseException
		 */
	public void emptyAndCommit(PreparedStatement ps, boolean batch, int batchCounter) throws KettleDatabaseException {

		try
		{
			if (ps!=null)
			{
			    if (!isAutoCommit())
			    {
			    	// Execute the batch or just perform a commit.
			    	//
					if (batch && getDatabaseMetaData().supportsBatchUpdates() && batchCounter>0)
					{
					    // The problem with the batch counters is that you can't just execute the current batch.
						// Certain databases have a problem if you execute the batch and if there are no statements in it.
						// You can't just catch the exception either because you would have to roll back on certain databases before you can then continue to do anything.
						// That leaves the task of keeping track of the number of rows up to our responsibility.
						//
						ps.executeBatch();
						commit();
					}
					else
					{
						commit();
					}
			    }
	
			    // Let's not forget to close the prepared statement.
			    //
				ps.close();
			}
		}
        catch(BatchUpdateException ex)
        {
            KettleDatabaseBatchException kdbe = new KettleDatabaseBatchException("Error updating batch", ex);
            kdbe.setUpdateCounts(ex.getUpdateCounts());
            List<Exception> exceptions = new ArrayList<Exception>();
            SQLException nextException = ex.getNextException();
            SQLException oldException = null;
            
            // This construction is specifically done for some JDBC drivers, these drivers
            // always return the same exception on getNextException() (and thus go into an infinite loop).
            // So it's not "equals" but != (comments from Sven Boden).
            while ( (nextException != null) && (oldException != nextException) )
            {
                exceptions.add(nextException);
                oldException = nextException;
               	nextException = nextException.getNextException();
            }
            kdbe.setExceptionsList(exceptions);
            throw kdbe;
        }
		catch(SQLException ex) 
		{
			throw new KettleDatabaseException("Unable to empty ps and commit connection.", ex);
		}
	}

	/**
	 * Close the prepared statement of the insert statement.
	 * 
	 * @param ps The prepared statement to empty and close.
	 * @param batch true if you are using batch processing (typically true for this method)
	 * @param psBatchCounter The number of rows on the batch queue
	 * @throws KettleDatabaseException
	 * 
	 * @deprecated use emptyAndCommit() instead (pass in the number of rows left in the batch)
	 */
	public void insertFinished(PreparedStatement ps, boolean batch) throws KettleDatabaseException
	{		
		try
		{
			if (ps!=null)
			{
			    if (!isAutoCommit())
			    {
			    	// Execute the batch or just perform a commit.
			    	//
					if (batch && getDatabaseMetaData().supportsBatchUpdates())
					{
					    // The problem with the batch counters is that you can't just execute the current batch.
						// Certain databases have a problem if you execute the batch and if there are no statements in it.
						// You can't just catch the exception either because you would have to roll back on certain databases before you can then continue to do anything.
						// That leaves the task of keeping track of the number of rows up to our responsibility.
						//
						ps.executeBatch();
						commit();
					}
					else
					{
						commit();
					}
			    }
	
			    // Let's not forget to close the prepared statement.
			    //
				ps.close();
			}
		}
        catch(BatchUpdateException ex)
        {
            KettleDatabaseBatchException kdbe = new KettleDatabaseBatchException("Error updating batch", ex);
            kdbe.setUpdateCounts(ex.getUpdateCounts());
            List<Exception> exceptions = new ArrayList<Exception>();
            SQLException nextException = ex.getNextException();
            SQLException oldException = null;
            
            // This construction is specifically done for some JDBC drivers, these drivers
            // always return the same exception on getNextException() (and thus go into an infinite loop).
            // So it's not "equals" but != (comments from Sven Boden).
            while ( (nextException != null) && (oldException != nextException) )
            {
                exceptions.add(nextException);
                oldException = nextException;
               	nextException = nextException.getNextException();
            }
            kdbe.setExceptionsList(exceptions);
            throw kdbe;
        }
		catch(SQLException ex) 
		{
			throw new KettleDatabaseException("Unable to commit connection after having inserted rows.", ex);
		}
	}
	
	

    /**
     * Execute an SQL statement on the database connection (has to be open)
     * @param sql The SQL to execute
     * @return a Result object indicating the number of lines read, deleted, inserted, updated, ...
     * @throws KettleDatabaseException in case anything goes wrong.
     */
	public Result execStatement(String sql) throws KettleDatabaseException
	{
		return execStatement(sql, null, null);
	}
	
	public Result execStatement(String sql, RowMetaInterface params, Object[] data) throws KettleDatabaseException
	{
        Result result = new Result();
		try
		{
            boolean resultSet;
            int count;
			if (params!=null)
			{
				PreparedStatement prep_stmt = connection.prepareStatement(databaseMeta.stripCR(sql));
				setValues(params, data, prep_stmt); // set the parameters!
				resultSet = prep_stmt.execute();
                count = prep_stmt.getUpdateCount();
				prep_stmt.close();
			}
			else
			{
                String sqlStripped = databaseMeta.stripCR(sql);
                // log.logDetailed("Executing SQL Statement: ["+sqlStripped+"]");
				Statement stmt = connection.createStatement();
                resultSet = stmt.execute(sqlStripped);
                count = stmt.getUpdateCount();
				stmt.close();
			}
            if (resultSet)
            {
                // the result is a resultset, but we don't do anything with it!
                // You should have called something else!
                // log.logDetailed("What to do with ResultSet??? (count="+count+")");
            }
            else
            {
                if (count > 0)
                {
                    if (sql.toUpperCase().startsWith("INSERT")) result.setNrLinesOutput(count);
                    if (sql.toUpperCase().startsWith("UPDATE")) result.setNrLinesUpdated(count);
                    if (sql.toUpperCase().startsWith("DELETE")) result.setNrLinesDeleted(count);
                }
            }
            
            // See if a cache needs to be cleared...
            if (sql.toUpperCase().startsWith("ALTER TABLE") || 
                sql.toUpperCase().startsWith("DROP TABLE") ||
                sql.toUpperCase().startsWith("CREATE TABLE")
                )
            {
                DBCache.getInstance().clear(databaseMeta.getName());
            }
		}
		catch(SQLException ex)
		{
			throw new KettleDatabaseException("Couldn't execute SQL: "+sql+Const.CR, ex);
		}
		catch(Exception e)
		{
			throw new KettleDatabaseException("Unexpected error executing SQL: "+Const.CR, e);
		}
        
        return result;
	}

    /**
     * Execute a series of SQL statements, separated by ;
     * 
     * We are already connected...
     
     * Multiple statements have to be split into parts
     * We use the ";" to separate statements...
     *
     * We keep the results in Result object from Jobs
     *
     * @param script The SQL script to be execute
     * @throws KettleDatabaseException In case an error occurs
     * @return A result with counts of the number or records updates, inserted, deleted or read.
     */
	public Result execStatements(String script) throws KettleDatabaseException
	{
        Result result = new Result();
        
		String all = script;
		int from=0;
		int to=0;
		int length = all.length();
		int nrstats = 0;
			
		while (to<length)
		{
			char c = all.charAt(to);
			if (c=='"')
			{
				c=' ';
				while (to<length && c!='"') { to++; c=all.charAt(to); }
			}
			else
			if (c=='\'') // skip until next '
			{
				c=' ';
				while (to<length && c!='\'') { to++; c=all.charAt(to); }
			}
			else
			if (all.substring(to).startsWith("--"))  // -- means: ignore comment until end of line...
			{
				while (to<length && c!='\n' && c!='\r') { to++; c=all.charAt(to); }
			}
			if (c==';' || to>=length-1) // end of statement
			{
				if (to>=length-1) to++; // grab last char also!
                
                String stat;
                if (to<=length) stat = all.substring(from, to);
                else stat = all.substring(from);
                
                // If it ends with a ; remove that ;
                // Oracle for example can't stand it when this happens...
                if (stat.length()>0 && stat.charAt(stat.length()-1)==';')
                {
                    stat = stat.substring(0,stat.length()-1);
                }
                
				if (!Const.onlySpaces(stat))
				{
					String sql=Const.trim(stat);
					if (sql.toUpperCase().startsWith("SELECT"))
					{
						// A Query
						if(log.isDetailed()) log.logDetailed("launch SELECT statement: "+Const.CR+sql);
						
						nrstats++;
						ResultSet rs = null;
						try 
						{
							rs = openQuery(sql);
							if (rs!=null)
							{
                                Object[] row = getRow(rs);
								while (row!=null)
								{
									result.setNrLinesRead(result.getNrLinesRead()+1);
									if (log.isDetailed()) log.logDetailed(rowMeta.getString(row));
                                    row = getRow(rs);
								}
								
							}
							else
							{
                                if (log.isDebug()) log.logDebug("Error executing query: "+Const.CR+sql);
							}
						} catch (KettleValueException e) {
							throw new KettleDatabaseException(e); // just pass the error upwards.
						}
						finally 
						{
							try 
							{
							   if ( rs != null ) rs.close();
							}
							catch (SQLException ex )
							{
                                if (log.isDebug()) log.logDebug("Error closing query: "+Const.CR+sql);
							}
						}						
					}
                    else // any kind of statement
                    {
                    	if(log.isDetailed()) log.logDetailed("launch DDL statement: "+Const.CR+sql);

                        // A DDL statement
                        nrstats++;
                        Result res = execStatement(sql);
                        result.add(res);
                    }
				}
				to++;
				from=to;
			}
			else
			{
				to++;
			}
		}
		
		if(log.isDetailed()) log.logDetailed(nrstats+" statement"+(nrstats==1?"":"s")+" executed");
        
        return result;
	}


	public ResultSet openQuery(String sql) throws KettleDatabaseException
	{
		return openQuery(sql, null, null);
	}

    /**
     * Open a query on the database with a set of parameters stored in a Kettle Row
     * @param sql The SQL to launch with question marks (?) as placeholders for the parameters
     * @param params The parameters or null if no parameters are used.
     * @data the parameter data to open the query with 
     * @return A JDBC ResultSet
     * @throws KettleDatabaseException when something goes wrong with the query.
     */
	public ResultSet openQuery(String sql, RowMetaInterface params, Object[] data) throws KettleDatabaseException
	{
		return openQuery(sql, params, data, ResultSet.FETCH_FORWARD);
	}

	public ResultSet openQuery(String sql, RowMetaInterface params, Object[] data, int fetch_mode) throws KettleDatabaseException
	{
		return openQuery(sql, params, data, fetch_mode, false);
	}
	
	public ResultSet openQuery(String sql, RowMetaInterface params, Object[] data, int fetch_mode, boolean lazyConversion) throws KettleDatabaseException
	{
		ResultSet res;
		String debug = "Start";
		
		// Create a Statement
		try
		{
			if (params!=null)
			{
				debug = "P create prepared statement (con==null? "+(connection==null)+")";
				pstmt = connection.prepareStatement(databaseMeta.stripCR(sql), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
				debug = "P Set values";
				setValues(params, data); // set the dates etc!
				if (canWeSetFetchSize(pstmt) )  
				{
					debug = "P Set fetchsize";
                    int fs = Const.FETCH_SIZE<=pstmt.getMaxRows()?pstmt.getMaxRows():Const.FETCH_SIZE;
                    
                    // System.out.println("Setting pstmt fetchsize to : "+fs);
                    {
                        if (databaseMeta.getDatabaseInterface() instanceof MySQLDatabaseMeta && databaseMeta.isStreamingResults())
                        {
                            pstmt.setFetchSize(Integer.MIN_VALUE);
                        }
                        else
                        pstmt.setFetchSize(fs);
                    }
					debug = "P Set fetch direction";
					pstmt.setFetchDirection(fetch_mode);
				} 
				debug = "P Set max rows";
				if (rowlimit>0 && databaseMeta.supportsSetMaxRows()) pstmt.setMaxRows(rowlimit);
				debug = "exec query";
				res = pstmt.executeQuery();
			}
			else
			{
				debug = "create statement";
				sel_stmt = connection.createStatement();
                if (canWeSetFetchSize(sel_stmt)) 
				{
					debug = "Set fetchsize";
                    int fs = Const.FETCH_SIZE<=sel_stmt.getMaxRows()?sel_stmt.getMaxRows():Const.FETCH_SIZE;
                    if (databaseMeta.getDatabaseInterface() instanceof MySQLDatabaseMeta && databaseMeta.isStreamingResults())
                    {
                        sel_stmt.setFetchSize(Integer.MIN_VALUE);
                    }
                    else
                    {
                        sel_stmt.setFetchSize(fs);
                    }
					debug = "Set fetch direction";
					sel_stmt.setFetchDirection(fetch_mode);
				} 
				debug = "Set max rows";
				if (rowlimit>0 && databaseMeta.supportsSetMaxRows()) sel_stmt.setMaxRows(rowlimit);

				debug = "exec query";
				res=sel_stmt.executeQuery(databaseMeta.stripCR(sql));
			}
			debug = "openQuery : get rowinfo";
            
            // MySQL Hack only. It seems too much for the cursor type of operation on MySQL, to have another cursor opened
            // to get the length of a String field.  So, on MySQL, we ingore the length of Strings in result rows.
            // 
			rowMeta = getRowInfo(res.getMetaData(), databaseMeta.getDatabaseInterface() instanceof MySQLDatabaseMeta, lazyConversion);
		}
		catch(SQLException ex)
		{
			// log.logError("ERROR executing ["+sql+"]");
			// log.logError("ERROR in part: ["+debug+"]");
			// printSQLException(ex);
            throw new KettleDatabaseException("An error occurred executing SQL: "+Const.CR+sql, ex);
		}
		catch(Exception e)
		{
			log.logError("ERROR executing query: "+e.toString());
			log.logError("ERROR in part: "+debug);
            throw new KettleDatabaseException("An error occurred executing SQL in part ["+debug+"]:"+Const.CR+sql, e);
		}

		return res;
	}

	private boolean canWeSetFetchSize(Statement statement) throws SQLException
    {
        return databaseMeta.isFetchSizeSupported() && 
            ( statement.getMaxRows()>0 || 
              databaseMeta.getDatabaseInterface() instanceof PostgreSQLDatabaseMeta || 
              ( databaseMeta.getDatabaseInterface() instanceof MySQLDatabaseMeta && databaseMeta.isStreamingResults() ) 
            );     
    }
    
    public ResultSet openQuery(PreparedStatement ps, RowMetaInterface params, Object[] data) throws KettleDatabaseException
	{
		ResultSet res;
		String debug = "Start";
		
		// Create a Statement
		try
		{
			debug = "OQ Set values";
			setValues(params, data, ps); // set the parameters!
			
			if (canWeSetFetchSize(ps)) 
			{
				debug = "OQ Set fetchsize";
                int fs = Const.FETCH_SIZE<=ps.getMaxRows()?ps.getMaxRows():Const.FETCH_SIZE;
                if (databaseMeta.getDatabaseInterface() instanceof MySQLDatabaseMeta && databaseMeta.isStreamingResults())
                {
                    ps.setFetchSize(Integer.MIN_VALUE);
                }
                else
                {
                    ps.setFetchSize(fs);
                }
				
				debug = "OQ Set fetch direction";
				ps.setFetchDirection(ResultSet.FETCH_FORWARD);
			} 
			
			debug = "OQ Set max rows";
			if (rowlimit>0 && databaseMeta.supportsSetMaxRows()) ps.setMaxRows(rowlimit);
			
			debug = "OQ exec query";
			res = ps.executeQuery();

			debug = "OQ getRowInfo()";
			// rowinfo = getRowInfo(res.getMetaData());
            
             // MySQL Hack only. It seems too much for the cursor type of operation on MySQL, to have another cursor opened
            // to get the length of a String field.  So, on MySQL, we ignore the length of Strings in result rows.
            // 
            rowMeta = getRowInfo(res.getMetaData(), databaseMeta.getDatabaseInterface() instanceof MySQLDatabaseMeta, false);
		}
		catch(SQLException ex)
		{
			throw new KettleDatabaseException("ERROR executing query in part["+debug+"]", ex);
		}
		catch(Exception e)
		{
			throw new KettleDatabaseException("ERROR executing query in part["+debug+"]", e);
		}

		return res;
	}
	
	public RowMetaInterface getTableFields(String tablename) throws KettleDatabaseException
	{
		return getQueryFields(databaseMeta.getSQLQueryFields(tablename), false);
	}

	public RowMetaInterface getQueryFields(String sql, boolean param) throws KettleDatabaseException
	{
		return getQueryFields(sql, param, null, null);
	}
    
	/**
	 * See if the table specified exists by reading
	 * @param tablename The name of the table to check.<br>
	 *        This is supposed to be the properly quoted name of the table or the complete schema-table name combination.
	 * @return true if the table exists, false if it doesn't.
	 */
	public boolean checkTableExists(String tablename) throws KettleDatabaseException
	{
		try
		{
			if(log.isDebug()) log.logDebug("Checking if table ["+tablename+"] exists!");
			
            // Just try to read from the table.
            String sql = databaseMeta.getSQLTableExists(tablename);
            try
            {
                getOneRow(sql);
                return true;
            }
            catch(KettleDatabaseException e)
            {
                return false;
            }
            
            /*
			if (getDatabaseMetaData()!=null)
			{
				ResultSet alltables = getDatabaseMetaData().getTables(null, null, "%" , new String[] { "TABLE", "VIEW", "SYNONYM" } );
				boolean found = false;
				if (alltables!=null)
				{
                    while (alltables.next() && !found)
					{
                        String schemaName = alltables.getString("TABLE_SCHEM");
						String name       = alltables.getString("TABLE_NAME");
						if ( tablename.equalsIgnoreCase(name) || 
                             ( schemaName!=null && tablename.equalsIgnoreCase( databaseMeta.getSchemaTableCombination(schemaName, name)) )
                           )
						{
							log.logDebug("table ["+tablename+"] was found!");
							found=true;
						}
					}
					alltables.close();

					return found;
				}
				else
				{
					throw new KettleDatabaseException("Unable to read table-names from the database meta-data.");
				}
			}
			else
			{
				throw new KettleDatabaseException("Unable to get database meta-data from the database.");
			}
            */
		}
		catch(Exception e)
		{
			throw new KettleDatabaseException("Unable to check if table ["+tablename+"] exists on connection ["+databaseMeta.getName()+"]", e);
		}
	}
	/**
	 * See if the column specified exists by reading
	 * @param columnname The name of the column to check.
	 * @param tablename The name of the table to check.<br>
	 *        This is supposed to be the properly quoted name of the table or the complete schema-table name combination.
	 * @return true if the table exists, false if it doesn't.
	 */
	public boolean checkColumnExists(String columnname, String tablename) throws KettleDatabaseException
	{
		try
		{
			if(log.isDebug()) log.logDebug("Checking if column [" + columnname + "] exists in table ["+tablename+"] !");

            // Just try to read from the table.
            String sql = databaseMeta.getSQLColumnExists(columnname,tablename);
        
            try
            {
                getOneRow(sql);
                return true;
            }
            catch(KettleDatabaseException e)
            {
                return false;
            }
		}
		catch(Exception e)
		{
			throw new KettleDatabaseException("Unable to check if column [" + columnname + "] exists in table ["+tablename+"] on connection ["+databaseMeta.getName()+"]", e);
		}
	}
    /**
     * Check whether the sequence exists, Oracle only! 
     * @param sequenceName The name of the sequence
     * @return true if the sequence exists.
     */
    public boolean checkSequenceExists(String sequenceName) throws KettleDatabaseException
    {
        return checkSequenceExists(null, sequenceName);
    }
    
	/**
	 * Check whether the sequence exists, Oracle only! 
	 * @param sequenceName The name of the sequence
	 * @return true if the sequence exists.
	 */
	public boolean checkSequenceExists(String schemaName, String sequenceName) throws KettleDatabaseException
	{
		boolean retval=false;
		
		if (!databaseMeta.supportsSequences()) return retval;
		
        String schemaSequence = databaseMeta.getQuotedSchemaTableCombination(schemaName, sequenceName);
		try
		{
			//
			// Get the info from the data dictionary...
			//
			String sql = databaseMeta.getSQLSequenceExists(schemaSequence);
			ResultSet res = openQuery(sql);
			if (res!=null)
			{
				Object[] row = getRow(res);
				if (row!=null)
				{
					retval=true;
				}
				closeQuery(res);
			}
		}
		catch(Exception e)
		{
			throw new KettleDatabaseException("Unexpected error checking whether or not sequence ["+schemaSequence+"] exists", e);
		}
		
		return retval;
	}
    
    /**
     * Check if an index on certain fields in a table exists.
     * @param tableName The table on which the index is checked
     * @param idx_fields The fields on which the indexe is checked
     * @return True if the index exists
     */
    public boolean checkIndexExists(String tableName, String idx_fields[]) throws KettleDatabaseException
    {
        return checkIndexExists(null, tableName, idx_fields);
    }
	
	/**
	 * Check if an index on certain fields in a table exists.
	 * @param tablename The table on which the index is checked
	 * @param idx_fields The fields on which the indexe is checked
	 * @return True if the index exists
	 */
	public boolean checkIndexExists(String schemaName, String tableName, String idx_fields[]) throws KettleDatabaseException
	{
        String tablename = databaseMeta.getQuotedSchemaTableCombination(schemaName, tableName);
		if (!checkTableExists(tablename)) return false;
		
		if(log.isDebug()) log.logDebug("CheckIndexExists() tablename = "+tablename+" type = "+databaseMeta.getPluginId());
		
		return databaseMeta.getDatabaseInterface().checkIndexExists(this, schemaName, tableName, idx_fields);
	}

    public String getCreateIndexStatement(String tablename, String indexname, String idx_fields[], boolean tk, boolean unique, boolean bitmap, boolean semi_colon)
    {
        return getCreateIndexStatement(null, tablename, indexname, idx_fields, tk, unique, bitmap, semi_colon);
    }
    
	public String getCreateIndexStatement(String schemaname, String tablename, String indexname, String idx_fields[], boolean tk, boolean unique, boolean bitmap, boolean semi_colon)
	{
		String cr_index="";
		
		cr_index += "CREATE ";
	
		if (unique || ( tk && databaseMeta.getDatabaseInterface() instanceof SybaseDatabaseMeta))
			cr_index += "UNIQUE ";
		
		if (bitmap && databaseMeta.supportsBitmapIndex()) 
			cr_index += "BITMAP ";
		
		cr_index += "INDEX "+databaseMeta.quoteField(indexname)+Const.CR+" ";
		cr_index += "ON ";
  	    // assume table has already been quoted (and possibly includes schema)
		cr_index += tablename;
		cr_index += Const.CR + "( "+Const.CR;
		for (int i=0;i<idx_fields.length;i++)
		{
			if (i>0) cr_index+=", "; else cr_index+="  ";
			cr_index += databaseMeta.quoteField(idx_fields[i])+Const.CR;
		}
		cr_index+=")"+Const.CR;
		
		if (databaseMeta.getDatabaseInterface() instanceof OracleDatabaseMeta &&
			databaseMeta.getIndexTablespace()!=null && databaseMeta.getIndexTablespace().length()>0)
		{
			cr_index+="TABLESPACE "+databaseMeta.quoteField(databaseMeta.getIndexTablespace());
		}
		
		if (semi_colon)
		{
			cr_index+=";"+Const.CR;
		}

		return cr_index;
	}

    public String getCreateSequenceStatement(String sequence, long start_at, long increment_by, long max_value, boolean semi_colon)
    {
        return getCreateSequenceStatement(null, sequence, Long.toString(start_at), Long.toString(increment_by), Long.toString(max_value), semi_colon);
    }
	
    public String getCreateSequenceStatement(String sequence, String start_at, String increment_by, String max_value, boolean semi_colon)
    {
        return getCreateSequenceStatement(null, sequence, start_at, increment_by, max_value, semi_colon);
    }

    public String getCreateSequenceStatement(String schemaName, String sequence, long start_at, long increment_by, long max_value, boolean semi_colon)
    {
        return getCreateSequenceStatement(schemaName, sequence, Long.toString(start_at), Long.toString(increment_by), Long.toString(max_value), semi_colon);
    }
    
	public String getCreateSequenceStatement(String schemaName, String sequenceName, String start_at, String increment_by, String max_value, boolean semi_colon)
	{
		String cr_seq="";
		
		if (Const.isEmpty(sequenceName)) return cr_seq;
		
		if (databaseMeta.supportsSequences())
		{
            String schemaSequence = databaseMeta.getQuotedSchemaTableCombination(schemaName, sequenceName);
			cr_seq += "CREATE SEQUENCE "+schemaSequence+" "+Const.CR;  // Works for both Oracle and PostgreSQL :-)
			cr_seq += "START WITH "+start_at+" "+Const.CR;
			cr_seq += "INCREMENT BY "+increment_by+" "+Const.CR;
			if (max_value != null) {
				// "-1" means there is no maxvalue, must be handles different by DB2 / AS400
				//
				if (databaseMeta.supportsSequenceNoMaxValueOption() && max_value.trim().equals("-1")) {
					cr_seq += "NOMAXVALUE"+Const.CR;
				} else {
					// set the max value
					cr_seq += "MAXVALUE "+max_value+Const.CR;
				}
			}
			
			if (semi_colon) cr_seq+=";"+Const.CR;
		}

		return cr_seq;
	}
	
	public RowMetaInterface getQueryFields(String sql, boolean param, RowMetaInterface inform, Object[] data) throws KettleDatabaseException
	{
		RowMetaInterface fields;
		DBCache dbcache = DBCache.getInstance();
		
		DBCacheEntry entry=null;
		
		// Check the cache first!
		//
		if (dbcache!=null)
		{
			entry = new DBCacheEntry(databaseMeta.getName(), sql);
			fields = dbcache.get(entry);
			if (fields!=null) 
			{
				return fields;
			} 
		}
		if (connection==null) return null; // Cache test without connect.

		// No cache entry found 

		// The new method of retrieving the query fields fails on Oracle because
		// they failed to implement the getMetaData method on a prepared statement. (!!!)
		// Even recent drivers like 10.2 fail because of it.
		// 
		// There might be other databases that don't support it (we have no knowledge of this at the time of writing).
		// If we discover other RDBMSs, we will create an interface for it.
		// For now, we just try to get the field layout on the re-bound in the exception block below.
		//
		if (databaseMeta.supportsPreparedStatementMetadataRetrieval()) {
			// On with the regular program.
			//
			
			PreparedStatement preparedStatement = null; 
			try
			{
				preparedStatement = connection.prepareStatement(databaseMeta.stripCR(sql), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
				ResultSetMetaData rsmd = preparedStatement.getMetaData();
				fields = getRowInfo(rsmd, false, false);
			}
			catch(Exception e)
			{
				fields = getQueryFieldsFallback(sql, param, inform, data);
			}
			finally
			{
				if (preparedStatement!=null)
				{
					try 
					{
						preparedStatement.close();
					} 
					catch (SQLException e) 
					{
						throw new KettleDatabaseException("Unable to close prepared statement after determining SQL layout", e);
					}
				}
			}
		} else {
			/*
					databaseMeta.getDatabaseType()==DatabaseMeta.TYPE_DATABASE_SYBASEIQ
				  )
				{
				*/
					fields=getQueryFieldsFallback(sql, param, inform, data);
		}
		
		// Store in cache!!
		if (dbcache!=null && entry!=null)
		{
			if (fields!=null)
			{
				dbcache.put(entry, fields);
			}
		}
		
		return fields;
	}
	
	private RowMetaInterface getQueryFieldsFallback(String sql, boolean param, RowMetaInterface inform, Object[] data) throws KettleDatabaseException
	{
		RowMetaInterface fields;

		try
		{
			if (inform==null 
					// Hack for MSSQL jtds 1.2 when using xxx NOT IN yyy we have to use a prepared statement (see BugID 3214)
					&& databaseMeta.getDatabaseInterface() instanceof MSSQLServerDatabaseMeta )
			{
				sel_stmt = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
				
				if (databaseMeta.isFetchSizeSupported() && sel_stmt.getMaxRows()>=1)
				{
	                if (databaseMeta.getDatabaseInterface() instanceof MySQLDatabaseMeta) {
	                    sel_stmt.setFetchSize(Integer.MIN_VALUE);
	                } else {
	                    sel_stmt.setFetchSize(1);
	                }
				}
	            if (databaseMeta.supportsSetMaxRows()) sel_stmt.setMaxRows(1);
				
				ResultSet r=sel_stmt.executeQuery(databaseMeta.stripCR(sql));
				fields = getRowInfo(r.getMetaData(), false, false);
				r.close();
				sel_stmt.close();
				sel_stmt=null;
			}
			else
			{
				PreparedStatement ps = connection.prepareStatement(databaseMeta.stripCR(sql));
				if (param)
				{
					RowMetaInterface par = inform;
					
					if (par==null || par.isEmpty()) par = getParameterMetaData(ps);
	                
					if (par==null || par.isEmpty()) par = getParameterMetaData(sql, inform, data);
	
					setValues(par, data, ps);
				}
				ResultSet r = ps.executeQuery();
				fields=getRowInfo(ps.getMetaData(), false, false);
				r.close();
				ps.close();
			}
		}
		catch(Exception ex)
		{
			throw new KettleDatabaseException("Couldn't get field info from ["+sql+"]"+Const.CR, ex);
		}
		
		return fields;
	}


	public void closeQuery(ResultSet res) throws KettleDatabaseException
	{
		// close everything involved in the query!
		try
		{
			if (res!=null) res.close();
			if (sel_stmt!=null) { sel_stmt.close(); sel_stmt=null; } 
			if (pstmt!=null) { pstmt.close(); pstmt=null;} 
		}
		catch(SQLException ex)
		{
			throw new KettleDatabaseException("Couldn't close query: resultset or prepared statements", ex);
		}
	}

	/**
	 * Build the row using ResultSetMetaData rsmd
     * @param rm The resultset metadata to inquire
     * @param ignoreLength true if you want to ignore the length (workaround for MySQL bug/problem)
     * @param lazyConversion true if lazy conversion needs to be enabled where possible
	 */
	private RowMetaInterface getRowInfo(ResultSetMetaData rm, boolean ignoreLength, boolean lazyConversion) throws KettleDatabaseException
	{
        if (rm==null) {
        	throw new KettleDatabaseException("No result set metadata available to retrieve row metadata!");
        }
		
		rowMeta = new RowMeta();
		
		try
		{
			// TODO If we do lazy conversion, we need to find out about the encoding
			//
            int fieldNr = 1;
			int nrcols=rm.getColumnCount();	
			for (int i=1;i<=nrcols;i++)
			{
				String name=new String(rm.getColumnName(i));
                
                // Check the name, sometimes it's empty.
                //
                if (Const.isEmpty(name) || Const.onlySpaces(name))
                {
                    name = "Field"+fieldNr;
                    fieldNr++;
                }
                
				ValueMetaInterface v = getValueFromSQLType(name, rm, i, ignoreLength, lazyConversion);
				rowMeta.addValueMeta(v);			
			}
			return rowMeta;
		}
		catch(SQLException ex)
		{
			throw new KettleDatabaseException("Error getting row information from database: ", ex);
		}
	}

	private ValueMetaInterface getValueFromSQLType(String name, ResultSetMetaData rm, int index, boolean ignoreLength, boolean lazyConversion) throws SQLException
    {
        int length=-1; 
        int precision=-1;
        int valtype=ValueMetaInterface.TYPE_NONE;
        boolean isClob = false;

        int type = rm.getColumnType(index);
        boolean signed = rm.isSigned(index);
        switch(type)
        {
        case java.sql.Types.CHAR:
        case java.sql.Types.VARCHAR: 
        case java.sql.Types.LONGVARCHAR:  // Character Large Object
            valtype=ValueMetaInterface.TYPE_STRING;
            if (!ignoreLength) length=rm.getColumnDisplaySize(index);
            break;
            
        case java.sql.Types.CLOB:  
            valtype=ValueMetaInterface.TYPE_STRING;
            length=DatabaseMeta.CLOB_LENGTH;
            isClob=true;
            break;

        case java.sql.Types.BIGINT:
        	// verify Unsigned BIGINT overflow!
        	//
        	if (signed) 
        	{
	            valtype=ValueMetaInterface.TYPE_INTEGER;
	            precision=0;   // Max 9.223.372.036.854.775.807
	            length=15;
        	} 
        	else 
        	{
	            valtype=ValueMetaInterface.TYPE_BIGNUMBER;
	            precision=0;   // Max 18.446.744.073.709.551.615
	            length=16;
        	}
            break;
            
        case java.sql.Types.INTEGER:
            valtype=ValueMetaInterface.TYPE_INTEGER;
            precision=0;    // Max 2.147.483.647
            length=9;
            break;
            
        case java.sql.Types.SMALLINT:
            valtype=ValueMetaInterface.TYPE_INTEGER;
            precision=0;   // Max 32.767
            length=4;
            break;
            
        case java.sql.Types.TINYINT: 
            valtype=ValueMetaInterface.TYPE_INTEGER;
            precision=0;   // Max 127
            length=2;
            break;
            
        case java.sql.Types.DECIMAL:
        case java.sql.Types.DOUBLE:
        case java.sql.Types.FLOAT:
        case java.sql.Types.REAL:
        case java.sql.Types.NUMERIC:
            valtype=ValueMetaInterface.TYPE_NUMBER;
            length=rm.getPrecision(index); 
            precision=rm.getScale(index);
            if (length    >=126) length=-1;
            if (precision >=126) precision=-1;
            
            if (type==java.sql.Types.DOUBLE || type==java.sql.Types.FLOAT || type==java.sql.Types.REAL)
            {
                if (precision==0) 
                {
                    precision=-1; // precision is obviously incorrect if the type if Double/Float/Real
                }
                
                // If we're dealing with PostgreSQL and double precision types 
                if (databaseMeta.getDatabaseInterface() instanceof PostgreSQLDatabaseMeta && type==java.sql.Types.DOUBLE && precision==16 && length==16)
                {
                    precision=-1;
                    length=-1;
                }
                
                // MySQL: max resolution is double precision floating point (double)
                // The (12,31) that is given back is not correct
                if (databaseMeta.getDatabaseInterface() instanceof MySQLDatabaseMeta)
                {
                	if (precision >= length) {
                        precision=-1;
                        length=-1;
                  	}
                }
                // if the length or precision needs a BIGNUMBER
                if (length>15 || precision>15) valtype=ValueMetaInterface.TYPE_BIGNUMBER;
            }
            else
            {
                if (precision==0) {
                	if (length<=18 && length>0) { // Among others Oracle is affected here.
                		valtype=ValueMetaInterface.TYPE_INTEGER;  // Long can hold up to 18 significant digits
                	} else if (length>18) {
                		valtype=ValueMetaInterface.TYPE_BIGNUMBER;
                	}
                } else { // we have a precision: keep NUMBER or change to BIGNUMBER?
                    if (length>15 || precision>15) valtype=ValueMetaInterface.TYPE_BIGNUMBER;
                }
            }
            
            if (databaseMeta.getDatabaseInterface() instanceof OracleDatabaseMeta)
            {
            	if (precision == 0 && length == 38 )
            	{
            		valtype=ValueMetaInterface.TYPE_INTEGER;
            	}
                if (precision<=0 && length<=0) // undefined size: BIGNUMBER, precision on Oracle can be 38, too big for a Number type
                {
                    valtype=ValueMetaInterface.TYPE_BIGNUMBER;
                    length=-1;
                    precision=-1;
                }
            }
            break;

        case java.sql.Types.DATE:
            if (databaseMeta.getDatabaseInterface() instanceof TeradataDatabaseMeta) {
            	precision = 1;
            }
        case java.sql.Types.TIME:
        case java.sql.Types.TIMESTAMP: 
            valtype=ValueMetaInterface.TYPE_DATE;
            // 
            if (databaseMeta.getDatabaseInterface() instanceof MySQLDatabaseMeta) {
                String property = databaseMeta.getConnectionProperties().getProperty("yearIsDateType");
                if (property != null && property.equalsIgnoreCase("false")
                		&& rm.getColumnTypeName(index).equalsIgnoreCase("YEAR")) {
                	valtype = ValueMetaInterface.TYPE_INTEGER;
                	precision = 0;
                	length = 4;
                	break;
                }
            } 
            break;

        case java.sql.Types.BOOLEAN:
        case java.sql.Types.BIT:
            valtype=ValueMetaInterface.TYPE_BOOLEAN;
            break;
            
        case java.sql.Types.BINARY:
        case java.sql.Types.BLOB:
        case java.sql.Types.VARBINARY:
        case java.sql.Types.LONGVARBINARY:
            valtype=ValueMetaInterface.TYPE_BINARY;
            
            if (databaseMeta.isDisplaySizeTwiceThePrecision() && (2 * rm.getPrecision(index)) == rm.getColumnDisplaySize(index)) 
            {
                // set the length for "CHAR(X) FOR BIT DATA"
                length = rm.getPrecision(index);
            }
            else
            if (databaseMeta.getDatabaseInterface() instanceof OracleDatabaseMeta &&
                ( type==java.sql.Types.VARBINARY || type==java.sql.Types.LONGVARBINARY )
               )
            {
                // set the length for Oracle "RAW" or "LONGRAW" data types
                valtype = ValueMetaInterface.TYPE_STRING;
                length = rm.getColumnDisplaySize(index);
            }
            else
            {
                length=-1; 
            }
            precision=-1;
            break;

        default:
            valtype=ValueMetaInterface.TYPE_STRING;
            precision=rm.getScale(index);                    
            break;
        }
        
        // Grab the comment as a description to the field as well.
        String comments=rm.getColumnLabel(index);
        
        // get & store more result set meta data for later use
        int originalColumnType=rm.getColumnType(index);
        String originalColumnTypeName=rm.getColumnTypeName(index);
        int originalPrecision=-1;
        if (!ignoreLength) rm.getPrecision(index); // Throws exception on MySQL
        int originalScale=rm.getScale(index);
        // boolean originalAutoIncrement=rm.isAutoIncrement(index);  DISABLED FOR PERFORMANCE REASONS : PDI-1788
        // int originalNullable=rm.isNullable(index);                DISABLED FOR PERFORMANCE REASONS : PDI-1788
        boolean originalSigned=rm.isSigned(index);

        ValueMetaInterface v=new ValueMeta(name, valtype);
        v.setLength(length);
        v.setPrecision(precision);
        v.setComments(comments);
        v.setLargeTextField(isClob);
        v.setOriginalColumnType(originalColumnType);
        v.setOriginalColumnTypeName(originalColumnTypeName);
        v.setOriginalPrecision(originalPrecision);
        v.setOriginalScale(originalScale);
        // v.setOriginalAutoIncrement(originalAutoIncrement);  DISABLED FOR PERFORMANCE REASONS : PDI-1788
        // v.setOriginalNullable(originalNullable);            DISABLED FOR PERFORMANCE REASONS : PDI-1788
        v.setOriginalSigned(originalSigned);

        // See if we need to enable lazy conversion...
        //
        if (lazyConversion && valtype==ValueMetaInterface.TYPE_STRING) {
        	v.setStorageType(ValueMetaInterface.STORAGE_TYPE_BINARY_STRING);
        	// TODO set some encoding to go with this.

        	// Also set the storage metadata. a copy of the parent, set to String too.
        	// 
        	ValueMetaInterface storageMetaData = v.clone();
        	storageMetaData.setType(ValueMetaInterface.TYPE_STRING);
        	storageMetaData.setStorageType(ValueMetaInterface.STORAGE_TYPE_NORMAL);
        	v.setStorageMetadata(storageMetaData);
        }


        return v;
    }

    public boolean absolute(ResultSet rs, int position) throws KettleDatabaseException
	{
		try
		{
			return rs.absolute(position);
		}
		catch(SQLException e)
		{
			throw new KettleDatabaseException("Unable to move resultset to position "+position, e);
		}
	}

	public boolean relative(ResultSet rs, int rows)  throws KettleDatabaseException
	{
		try
		{
			return rs.relative(rows);
		}
		catch(SQLException e)
		{
			throw new KettleDatabaseException("Unable to move the resultset forward "+rows+" rows", e);
		}
	}

	public void afterLast(ResultSet rs)
		throws KettleDatabaseException
	{
		try
		{
			rs.afterLast();
		}
		catch(SQLException e)
		{
			throw new KettleDatabaseException("Unable to move resultset to after the last position", e);
		}
	}

	public void first(ResultSet rs) throws KettleDatabaseException
	{
		try
		{
			rs.first();
		}
		catch(SQLException e)
		{
			throw new KettleDatabaseException("Unable to move resultset to the first position", e);
		}
	}

	/**
	 * Get a row from the resultset.  Do not use lazy conversion
	 * @param rs The resultset to get the row from
	 * @return one row or null if no row was found on the resultset or if an error occurred.
	 */
	public Object[] getRow(ResultSet rs) throws KettleDatabaseException
	{
		return getRow(rs, false);
	}
	
	/**
	 * Get a row from the resultset.
	 * @param rs The resultset to get the row from
	 * @param lazyConversion set to true if strings need to have lazy conversion enabled
	 * @return one row or null if no row was found on the resultset or if an error occurred.
	 */
	public Object[] getRow(ResultSet rs, boolean lazyConversion) throws KettleDatabaseException
	{
        if (rowMeta==null)
        {
            ResultSetMetaData rsmd = null;
            try
            {
                rsmd = rs.getMetaData();
            }
            catch(SQLException e)
            {
                throw new KettleDatabaseException("Unable to retrieve metadata from resultset", e);
            }

            rowMeta = getRowInfo(rsmd, false, lazyConversion);
        }

		return getRow(rs, null, rowMeta);
	}

	/**
	 * Get a row from the resultset.
	 * @param rs The resultset to get the row from
	 * @return one row or null if no row was found on the resultset or if an error occurred.
	 */
	public Object[] getRow(ResultSet rs, ResultSetMetaData dummy, RowMetaInterface rowInfo) throws KettleDatabaseException
	{
		try
		{
			int nrcols=rowInfo.size();
			Object[] data = RowDataUtil.allocateRowData(nrcols);
            
			if (rs.next())
			{
				for (int i=0;i<nrcols;i++)
				{
                    ValueMetaInterface val = rowInfo.getValueMeta(i);
                    
					switch(val.getType())
					{
					case ValueMetaInterface.TYPE_BOOLEAN   : data[i] = Boolean.valueOf( rs.getBoolean(i+1) ); break;
					case ValueMetaInterface.TYPE_NUMBER    : data[i] = new Double( rs.getDouble(i+1) ); break;
                    case ValueMetaInterface.TYPE_BIGNUMBER : data[i] = rs.getBigDecimal(i+1); break;
					case ValueMetaInterface.TYPE_INTEGER   : data[i] = Long.valueOf( rs.getLong(i+1) ); break;
					case ValueMetaInterface.TYPE_STRING    : 
						{
							if (val.isStorageBinaryString()) {
								data[i] = rs.getBytes(i+1);
							}
							else {
								data[i] = rs.getString(i+1);
							}
						}
						break;
					case ValueMetaInterface.TYPE_BINARY    : 
                        {
                            if (databaseMeta.supportsGetBlob())
                            {
                                Blob blob = rs.getBlob(i+1);
                                if (blob!=null)
                                {
                                    data[i] = blob.getBytes(1L, (int)blob.length());
                                }
                                else
                                {
                                    data[i] = null;
                                }
                            }
                            else
                            {
                                data[i] = rs.getBytes(i+1);
                            }
                        }
                        break;
					case ValueMetaInterface.TYPE_DATE      :
						if (databaseMeta.getDatabaseInterface() instanceof NeoviewDatabaseMeta && val.getOriginalColumnType()==java.sql.Types.TIME)
						{
							// Neoview can not handle getDate / getTimestamp for a Time column
							data[i] = rs.getTime(i+1); break;  // Time is a subclass of java.util.Date, the default date will be 1970-01-01
						}
						else if (val.getPrecision()!=1 && databaseMeta.supportsTimeStampToDateConversion())
                        {
                            data[i] = rs.getTimestamp(i+1); break; // Timestamp extends java.util.Date
                        }
                        else 
                        {
                            data[i] = rs.getDate(i+1); break;
                        }
					default: break;
					}
					if (rs.wasNull()) data[i] = null; // null value, it's the default but we want it just to make sure we handle this case too.
				}
			}
			else
			{
                data=null;
			}
			
			return data;
		}
		catch(SQLException ex)
		{
			throw new KettleDatabaseException("Couldn't get row from result set", ex);
		}
	}

	public void printSQLException(SQLException ex)
	{
		log.logError("==> SQLException: ");
		while (ex != null) 
		{
			log.logError("Message:   " + ex.getMessage ());
			log.logError("SQLState:  " + ex.getSQLState ());
			log.logError("ErrorCode: " + ex.getErrorCode ());
			ex = ex.getNextException();
			log.logError("");
		}
	}

	public void setLookup(String table, String codes[], String condition[], 
            String gets[], String rename[], String orderby
            ) throws KettleDatabaseException
    {
		setLookup(table, codes, condition, gets, rename, orderby, false);
    }
    
    public void setLookup(String schema, String table, String codes[], String condition[], 
            String gets[], String rename[], String orderby
            ) throws KettleDatabaseException
    {
        setLookup(schema, table, codes, condition, gets, rename, orderby, false);
    }

    public void setLookup(String tableName, String codes[], String condition[], 
            String gets[], String rename[], String orderby,
            boolean checkForMultipleResults) throws KettleDatabaseException
    {
        setLookup(null, tableName, codes, condition, gets, rename, orderby, checkForMultipleResults);
    }

	// Lookup certain fields in a table
	public void setLookup(String schemaName, String tableName, String codes[], String condition[], 
	                      String gets[], String rename[], String orderby,
	                      boolean checkForMultipleResults) throws KettleDatabaseException
	{
        String table = databaseMeta.getQuotedSchemaTableCombination(schemaName, tableName);
        
		String sql = "SELECT ";
		
		for (int i=0;i<gets.length;i++)
		{
			if (i!=0) sql += ", ";
			sql += databaseMeta.quoteField(gets[i]);
			if (rename!=null && rename[i]!=null && !gets[i].equalsIgnoreCase(rename[i]))
			{
				sql+=" AS "+databaseMeta.quoteField(rename[i]);
			}
		}

		sql += " FROM "+table+" WHERE ";

		for (int i=0;i<codes.length;i++)
		{
			if (i!=0) sql += " AND ";
			sql += databaseMeta.quoteField(codes[i]);
			if ("BETWEEN".equalsIgnoreCase(condition[i]))
			{
				sql+=" BETWEEN ? AND ? ";
			}
			else
			if ("IS NULL".equalsIgnoreCase(condition[i]) || "IS NOT NULL".equalsIgnoreCase(condition[i]))
			{
				sql+=" "+condition[i]+" ";
			}
			else
			{
				sql+=" "+condition[i]+" ? ";
			}
		}

		if (orderby!=null && orderby.length()!=0)
		{
			sql += " ORDER BY "+orderby;
		}

		try
		{
			if(log.isDetailed()) log.logDetailed("Setting preparedStatement to ["+sql+"]");
			prepStatementLookup=connection.prepareStatement(databaseMeta.stripCR(sql));
			if (!checkForMultipleResults && databaseMeta.supportsSetMaxRows())
			{
				prepStatementLookup.setMaxRows(1); // alywas get only 1 line back!
			}
		}
		catch(SQLException ex) 
		{
			throw new KettleDatabaseException("Unable to prepare statement for update ["+sql+"]", ex);
		}
	}

    public boolean prepareUpdate(String table, String codes[], String condition[], String sets[])
    {
        return prepareUpdate(null, table, codes, condition, sets);
    }
    
	// Lookup certain fields in a table
	public boolean prepareUpdate(String schemaName, String tableName, String codes[], String condition[], String sets[])
	{
		StringBuffer sql = new StringBuffer(128);

        String schemaTable = databaseMeta.getQuotedSchemaTableCombination(schemaName, tableName);
        
		sql.append("UPDATE ").append(schemaTable).append(Const.CR).append("SET ");

		for (int i=0;i<sets.length;i++)
		{
			if (i!=0) sql.append(",   ");
			sql.append(databaseMeta.quoteField(sets[i]));
			sql.append(" = ?").append(Const.CR);
		}

		sql.append("WHERE ");

		for (int i=0;i<codes.length;i++)
		{
			if (i!=0) sql.append("AND   ");
			sql.append(databaseMeta.quoteField(codes[i]));
			if ("BETWEEN".equalsIgnoreCase(condition[i]))
			{
				sql.append(" BETWEEN ? AND ? ");
			}
			else
			if ("IS NULL".equalsIgnoreCase(condition[i]) || "IS NOT NULL".equalsIgnoreCase(condition[i]))
			{
				sql.append(' ').append(condition[i]).append(' ');
			}
			else
			{
				sql.append(' ').append(condition[i]).append(" ? ");
			}
		}

		try
		{
			String s = sql.toString();
			if(log.isDetailed()) log.logDetailed("Setting update preparedStatement to ["+s+"]");
			prepStatementUpdate=connection.prepareStatement(databaseMeta.stripCR(s));
		}
		catch(SQLException ex) 
		{
			printSQLException(ex);
			return false;
		}

		return true;
	}


    /**
     * Prepare a delete statement by giving it the tablename, fields and conditions to work with.
     * @param table The table-name to delete in
     * @param codes  
     * @param condition
     * @return true when everything went OK, false when something went wrong.
     */
    public boolean prepareDelete(String table, String codes[], String condition[])
    {
        return prepareDelete(null, table, codes, condition);
    }
    
    /**
     * Prepare a delete statement by giving it the tablename, fields and conditions to work with.
     * @param schemaName the schema-name to delete in
     * @param tableName The table-name to delete in
     * @param codes  
     * @param condition
     * @return true when everything went OK, false when something went wrong.
     */
    public boolean prepareDelete(String schemaName, String tableName, String codes[], String condition[])
    {
        String sql;

        String table = databaseMeta.getQuotedSchemaTableCombination(schemaName, tableName);
        sql = "DELETE FROM "+table+Const.CR;
        sql+= "WHERE ";
        
        for (int i=0;i<codes.length;i++)
        {
            if (i!=0) sql += "AND   ";
            sql += codes[i];
            if ("BETWEEN".equalsIgnoreCase(condition[i]))
            {
                sql+=" BETWEEN ? AND ? ";
            }
            else
            if ("IS NULL".equalsIgnoreCase(condition[i]) || "IS NOT NULL".equalsIgnoreCase(condition[i]))
            {
                sql+=" "+condition[i]+" ";
            }
            else
            {
                sql+=" "+condition[i]+" ? ";
            }
        }

        try
        {
        	if(log.isDetailed()) log.logDetailed("Setting update preparedStatement to ["+sql+"]");
            prepStatementUpdate=connection.prepareStatement(databaseMeta.stripCR(sql));
        }
        catch(SQLException ex) 
        {
            printSQLException(ex);
            return false;
        }

        return true;
    }

    public void setProcLookup(String proc, String arg[], String argdir[], int argtype[], String returnvalue, int returntype)
		throws KettleDatabaseException
	{
		String sql;
		int pos=0;
		
		sql = "{ ";
		if (returnvalue!=null && returnvalue.length()!=0)
		{
			sql+="? = ";
		}
		sql+="call "+proc+" ";
		
		if (arg.length>0) sql+="(";
		
		for (int i=0;i<arg.length;i++)
		{
			if (i!=0) sql += ", ";
			sql += " ?";
		}
		
		if (arg.length>0) sql+=")"; 
		
		sql+="}";
		
		try
		{
			if(log.isDetailed()) log.logDetailed("DBA setting callableStatement to ["+sql+"]");
			cstmt=connection.prepareCall(sql);
			pos=1;
			if (!Const.isEmpty(returnvalue))
			{
				switch(returntype)
				{
				case ValueMetaInterface.TYPE_NUMBER    : cstmt.registerOutParameter(pos, java.sql.Types.DOUBLE); break;
                case ValueMetaInterface.TYPE_BIGNUMBER : cstmt.registerOutParameter(pos, java.sql.Types.DECIMAL); break;
				case ValueMetaInterface.TYPE_INTEGER   : cstmt.registerOutParameter(pos, java.sql.Types.BIGINT); break;
				case ValueMetaInterface.TYPE_STRING    : cstmt.registerOutParameter(pos, java.sql.Types.VARCHAR);	break;
				case ValueMetaInterface.TYPE_DATE      : cstmt.registerOutParameter(pos, java.sql.Types.TIMESTAMP); break;
				case ValueMetaInterface.TYPE_BOOLEAN   : cstmt.registerOutParameter(pos, java.sql.Types.BOOLEAN); break;
				default: break;
				}
				pos++;
			}
			for (int i=0;i<arg.length;i++)
			{
				if (argdir[i].equalsIgnoreCase("OUT") || argdir[i].equalsIgnoreCase("INOUT"))
				{
					switch(argtype[i])
					{
					case ValueMetaInterface.TYPE_NUMBER    : cstmt.registerOutParameter(i+pos, java.sql.Types.DOUBLE); break;
                    case ValueMetaInterface.TYPE_BIGNUMBER : cstmt.registerOutParameter(i+pos, java.sql.Types.DECIMAL); break;
					case ValueMetaInterface.TYPE_INTEGER   : cstmt.registerOutParameter(i+pos, java.sql.Types.BIGINT); break;
					case ValueMetaInterface.TYPE_STRING    : cstmt.registerOutParameter(i+pos, java.sql.Types.VARCHAR); break;
					case ValueMetaInterface.TYPE_DATE      : cstmt.registerOutParameter(i+pos, java.sql.Types.TIMESTAMP); break;
					case ValueMetaInterface.TYPE_BOOLEAN   : cstmt.registerOutParameter(i+pos, java.sql.Types.BOOLEAN); break;
					default: break;
					}
				} 
			}
		}
		catch(SQLException ex)
		{
			throw new KettleDatabaseException("Unable to prepare database procedure call", ex);
		}
	}

	public Object[] getLookup() throws KettleDatabaseException
	{
		return getLookup(prepStatementLookup);
	}

    public Object[] getLookup(boolean failOnMultipleResults) throws KettleDatabaseException
    {
        return getLookup(prepStatementLookup, failOnMultipleResults);
    }

    public Object[] getLookup(PreparedStatement ps) throws KettleDatabaseException
    {
        return getLookup(ps, false);
    }

	public Object[] getLookup(PreparedStatement ps, boolean failOnMultipleResults) throws KettleDatabaseException
	{
        ResultSet res = null;
		try
		{
			res = ps.executeQuery();
			
			rowMeta = getRowInfo(res.getMetaData(), false, false);
			
			Object[] ret = getRow(res);
			
            if (failOnMultipleResults)
            {
                if (ret != null && res.next()) 
                {
                	// if the previous row was null, there's no reason to try res.next() again.
                	// on DB2 this will even cause an exception (because of the buggy DB2 JDBC driver).
                    throw new KettleDatabaseException("Only 1 row was expected as a result of a lookup, and at least 2 were found!");
                }
            }			
			return ret;
		}
		catch(SQLException ex) 
		{
			throw new KettleDatabaseException("Error looking up row in database", ex);
		}
        finally
        {
            try
            {
                if (res!=null) res.close(); // close resultset!
            }
            catch(SQLException e)
            {
                throw new KettleDatabaseException("Unable to close resultset after looking up data", e);
            }
        }
	}
	
	public DatabaseMetaData getDatabaseMetaData() throws KettleDatabaseException
	{
		try
		{
			if (dbmd==null) dbmd = connection.getMetaData(); // Only get the metadata once!
		}
		catch(Exception e)
		{
			throw new KettleDatabaseException("Unable to get database metadata from this database connection", e);
		}
		
		return dbmd;
	}
	
	public String getDDL(String tablename, RowMetaInterface fields) throws KettleDatabaseException
	{
		return getDDL(tablename, fields, null, false, null, true);
	}

	public String getDDL(String tablename, RowMetaInterface fields, String tk, boolean use_autoinc, String pk) throws KettleDatabaseException
	{
		return getDDL(tablename, fields, tk, use_autoinc, pk, true);
	}

	public String getDDL(String tableName, RowMetaInterface fields, String tk, boolean use_autoinc, String pk, boolean semicolon) throws KettleDatabaseException
	{
		String retval;
		
		// First, check for reserved SQL in the input row r...
		databaseMeta.quoteReservedWords(fields);
		String quotedTk = tk != null ? databaseMeta.quoteField(tk) : null;
		
		if (checkTableExists(tableName))
		{
			retval=getAlterTableStatement(tableName, fields, quotedTk, use_autoinc, pk, semicolon);
		}
		else
		{
			retval=getCreateTableStatement(tableName, fields, quotedTk, use_autoinc, pk, semicolon);
		}
		
		return retval;
	}
	
    /**
     * Generates SQL
     * @param tableName the table name or schema/table combination: this needs to be quoted properly in advance.
     * @param fields the fields
     * @param tk the name of the technical key field
     * @param use_autoinc true if we need to use auto-increment fields for a primary key
     * @param pk the name of the primary/technical key field
     * @param semicolon append semicolon to the statement
     * @param pkc primary key composite ( name of the key fields)
     * @return the SQL needed to create the specified table and fields.
     */
	public String getCreateTableStatement(String tableName, RowMetaInterface fields, String tk, boolean use_autoinc, String pk, boolean semicolon)
	{
		StringBuilder retval = new StringBuilder("CREATE TABLE ");
		
		retval.append(tableName+Const.CR);
		retval.append("(").append(Const.CR);
		for (int i=0;i<fields.size();i++)
		{
			if (i>0) retval.append(", "); else retval.append("  ");
			
			ValueMetaInterface v=fields.getValueMeta(i);
			retval.append(databaseMeta.getFieldDefinition(v, tk, pk, use_autoinc));
		}
		// At the end, before the closing of the statement, we might need to add some constraints...
		// Technical keys
		if (tk!=null)
		{
			if (databaseMeta.requiresCreateTablePrimaryKeyAppend())
			{
				retval.append(", PRIMARY KEY (").append(tk).append(")").append(Const.CR);
			}
		}
		
		// Primary keys
		if (pk!=null)
		{
			if (databaseMeta.requiresCreateTablePrimaryKeyAppend())
			{
				retval.append(", PRIMARY KEY (").append(pk).append(")").append(Const.CR);
			}
		}
		retval.append(")").append(Const.CR);
		
		if (databaseMeta.getDatabaseInterface() instanceof OracleDatabaseMeta &&
				databaseMeta.getIndexTablespace()!=null && databaseMeta.getIndexTablespace().length()>0)
		{
			retval.append("TABLESPACE ").append(databaseMeta.getDataTablespace());
		}

		if (pk==null && tk==null && databaseMeta.getDatabaseInterface() instanceof NeoviewDatabaseMeta)
		{
			retval.append("NO PARTITION"); // use this as a default when no pk/tk is there, otherwise you get an error 
		}
		
		
		if (semicolon) retval.append(";");
		
		// TODO: All this custom database code shouldn't really be in Database.java.  It should be in the DB implementations.
		//
		if (databaseMeta.getDatabaseInterface() instanceof VerticaDatabaseMeta)
		{
		    retval.append(Const.CR).append("CREATE PROJECTION ").append(tableName).append("_unseg_super").append(Const.CR);
		    
	        retval.append("(").append(Const.CR);
	        for (int i=0;i<fields.size();i++)
	        {
	            if (i>0) retval.append(", "); else retval.append("  ");
	            
	            retval.append(fields.getValueMeta(i).getName()).append(Const.CR);
	        }
            retval.append(")").append(Const.CR);
            
            retval.append("AS SELECT").append(Const.CR);
            for (int i=0;i<fields.size();i++)
            {
                if (i>0) retval.append(", "); else retval.append("  ");
                
                retval.append(fields.getValueMeta(i).getName()).append(Const.CR);
            }
            retval.append("FROM ").append(tableName).append(Const.CR);
            retval.append("-- Replace UNSEGMENTED with a hash segmentation for optimum performance").append(Const.CR);
            retval.append("--SEGMENTED BY HASH(X,Y,Z)").append(Const.CR);
            retval.append("UNSEGMENTED ALL NODES").append(Const.CR);
            retval.append(";");
		}
		
		return retval.toString();
	}
	public String getAlterTableStatement(String tableName, RowMetaInterface fields, String tk, boolean use_autoinc, String pk, boolean semicolon) throws KettleDatabaseException
	{
		String retval="";
		
		// Get the fields that are in the table now:
        RowMetaInterface tabFields = getTableFields(tableName);
		
        // Don't forget to quote these as well...
        databaseMeta.quoteReservedWords(tabFields);
        
		// Find the missing fields
        RowMetaInterface missing = new RowMeta();
		for (int i=0;i<fields.size();i++)
		{
			ValueMetaInterface v = fields.getValueMeta(i);
			// Not found?
			if (tabFields.searchValueMeta( v.getName() )==null )
			{
				missing.addValueMeta(v); // nope --> Missing!
			}
		}

		if (missing.size()!=0)
		{
			for (int i=0;i<missing.size();i++)
			{
				ValueMetaInterface v=missing.getValueMeta(i);
				retval+=databaseMeta.getAddColumnStatement(tableName, v, tk, use_autoinc, pk, true);
			}
		}

		// Find the surplus fields
		RowMetaInterface surplus = new RowMeta();
		for (int i=0;i<tabFields.size();i++)
		{
			ValueMetaInterface v = tabFields.getValueMeta(i);
		    // Found in table, not in input ?        
		    if (fields.searchValueMeta( v.getName() )==null )
		    {
			    surplus.addValueMeta(v); // yes --> surplus!
		    }
		}

		if (surplus.size()!=0)
		{
			for (int i=0;i<surplus.size();i++)
			{
				ValueMetaInterface v=surplus.getValueMeta(i);
				retval+=databaseMeta.getDropColumnStatement(tableName, v, tk, use_autoinc, pk, true);
			}
		}
		
		//
		// OK, see if there are fields for which we need to modify the type... (length, precision)
		//
		RowMetaInterface modify = new RowMeta();
		for (int i=0;i<fields.size();i++)
		{
			ValueMetaInterface desiredField = fields.getValueMeta(i);
			ValueMetaInterface currentField = tabFields.searchValueMeta( desiredField.getName());
			if (desiredField!=null && currentField!=null)
			{
				String desiredDDL = databaseMeta.getFieldDefinition(desiredField, tk, pk, use_autoinc);
				String currentDDL = databaseMeta.getFieldDefinition(currentField, tk, pk, use_autoinc);
				
                boolean mod = !desiredDDL.equalsIgnoreCase(currentDDL);
				if (mod)
				{
                    // System.out.println("Desired field: ["+desiredField.toStringMeta()+"], current field: ["+currentField.toStringMeta()+"]");
                    modify.addValueMeta(desiredField);
				}
			}
		}
		
		if (modify.size()>0)
		{
			for (int i=0;i<modify.size();i++)
			{
				ValueMetaInterface v=modify.getValueMeta(i);
				retval+=databaseMeta.getModifyColumnStatement(tableName, v, tk, use_autoinc, pk, true);
			}
		}

		return retval;
	}


	public void truncateTable(String tablename) throws KettleDatabaseException
	{
        if (Const.isEmpty(connectionGroup))
        {
            execStatement(databaseMeta.getTruncateTableStatement(null, tablename));
        }
        else
        {
            execStatement("DELETE FROM "+databaseMeta.quoteField(tablename));
        }
	}
	
	public void truncateTable(String schema, String tablename) throws KettleDatabaseException
	{
        if (Const.isEmpty(connectionGroup))
        {
        	execStatement(databaseMeta.getTruncateTableStatement(schema, tablename));
        }
        else
        {
            execStatement("DELETE FROM "+databaseMeta.getQuotedSchemaTableCombination(schema, tablename));
        }
	}
	
	/**
	 * Execute a query and return at most one row from the resultset
	 * @param sql The SQL for the query
	 * @return one Row with data or null if nothing was found.
	 */
	public RowMetaAndData getOneRow(String sql) throws KettleDatabaseException
	{
		ResultSet rs = openQuery(sql);
		if (rs!=null)
		{
			Object[] row = getRow(rs); // One row only;
            
			try { rs.close(); } catch(Exception e) { throw new KettleDatabaseException("Unable to close resultset", e); }
			
			if (pstmt!=null)
			{
				try { pstmt.close(); } catch(Exception e) { throw new KettleDatabaseException("Unable to close prepared statement pstmt", e); }
				pstmt=null;
			}
			if (sel_stmt!=null)
			{
				try { sel_stmt.close(); } catch(Exception e) { throw new KettleDatabaseException("Unable to close prepared statement sel_stmt", e); }
				sel_stmt=null;
			}
			return new RowMetaAndData(rowMeta, row);
		}
		else
		{
			throw new KettleDatabaseException("error opening resultset for query: "+sql);
		}
	}

	public RowMeta getMetaFromRow( Object[] row, ResultSetMetaData md ) throws SQLException {
		RowMeta meta = new RowMeta();
		
		for( int i=0; i<md.getColumnCount(); i++ ) {
           	String name = md.getColumnName(i+1);
           	ValueMetaInterface valueMeta = getValueFromSQLType( name, md, i+1, true, false );
           	meta.addValueMeta( valueMeta );
		}

		return meta;
	}
	
	public RowMetaAndData getOneRow(String sql, RowMetaInterface param, Object[] data) throws KettleDatabaseException
	{
		ResultSet rs = openQuery(sql, param, data);
		if (rs!=null)
		{		
			Object[] row = getRow(rs); // One value: a number;
			
            rowMeta=null;
            RowMeta tmpMeta = null;
        	try {

                ResultSetMetaData md = rs.getMetaData();
                tmpMeta = getMetaFromRow( row, md );

            } catch (Exception e) {
        		e.printStackTrace();
        	} finally {
    			try { rs.close(); } catch(Exception e) { throw new KettleDatabaseException("Unable to close resultset", e); }
    			
    			if (pstmt!=null)
    			{
    				try { pstmt.close(); } catch(Exception e) { throw new KettleDatabaseException("Unable to close prepared statement pstmt", e); }
    				pstmt=null;
    			}
    			if (sel_stmt!=null)
    			{
    				try { sel_stmt.close(); } catch(Exception e) { throw new KettleDatabaseException("Unable to close prepared statement sel_stmt", e); }
    				sel_stmt=null;
    			}
        		
        	}
            
			return new RowMetaAndData(tmpMeta, row);
		}
		else
		{
			return null;
		}
	}
	
	public RowMetaInterface getParameterMetaData(PreparedStatement ps)
	{
        RowMetaInterface par = new RowMeta();
		try
		{
			ParameterMetaData pmd = ps.getParameterMetaData();
			for (int i=1;i<=pmd.getParameterCount();i++)
			{
				String name = "par"+i;
				int sqltype = pmd.getParameterType(i);
                int length = pmd.getPrecision(i);
                int precision = pmd.getScale(i);
				ValueMeta val;
				
				switch(sqltype)
				{
				case java.sql.Types.CHAR:
				case java.sql.Types.VARCHAR: 
					val=new ValueMeta(name, ValueMetaInterface.TYPE_STRING);
					break;
				case java.sql.Types.BIGINT:
				case java.sql.Types.INTEGER:
				case java.sql.Types.NUMERIC:
				case java.sql.Types.SMALLINT:
				case java.sql.Types.TINYINT: 
					val=new ValueMeta(name, ValueMetaInterface.TYPE_INTEGER);
					break;
				case java.sql.Types.DECIMAL:
				case java.sql.Types.DOUBLE:
				case java.sql.Types.FLOAT:
				case java.sql.Types.REAL:
					val=new ValueMeta(name, ValueMetaInterface.TYPE_NUMBER);
					break;
				case java.sql.Types.DATE:
				case java.sql.Types.TIME:
				case java.sql.Types.TIMESTAMP: 
					val=new ValueMeta(name, ValueMetaInterface.TYPE_DATE); 
					break;
				case java.sql.Types.BOOLEAN:
				case java.sql.Types.BIT:
					val=new ValueMeta(name, ValueMetaInterface.TYPE_BOOLEAN);
					break;
				default:
					val=new ValueMeta(name, ValueMetaInterface.TYPE_NONE);
					break;
				}
                
                if (val.isNumeric() && ( length>18 || precision>18) )
                {
                    val = new ValueMeta(name, ValueMetaInterface.TYPE_BIGNUMBER);
                }
                
				par.addValueMeta(val);
			}
		}
		// Oops: probably the database or JDBC doesn't support it.
		catch(AbstractMethodError e) { return null;	}
		catch(SQLException e) { return null; }
		catch(Exception e) { return null;	}
		
		return par;
	}
	
	public int countParameters(String sql)
	{
		int q=0;
		boolean quote_opened=false;
		boolean dquote_opened=false;
		
		for (int x=0;x<sql.length();x++)
		{
			char c = sql.charAt(x);
			
			switch(c)
			{
			case '\'':  quote_opened= !quote_opened; break;
			case '"' :  dquote_opened=!dquote_opened; break;
			case '?' :  if (!quote_opened && !dquote_opened) q++; break;
			}
		}
		
		return q;
	}

	// Get the fields back from an SQL query
	public RowMetaInterface getParameterMetaData(String sql, RowMetaInterface inform, Object[] data)
	{
		// The database couldn't handle it: try manually!
		int q=countParameters(sql);
		
		RowMetaInterface par=new RowMeta();
		
		if (inform!=null && q==inform.size())
		{
			for (int i=0;i<q;i++)
			{
				ValueMetaInterface inf=inform.getValueMeta(i);
				ValueMetaInterface v = inf.clone();
				par.addValueMeta(v);
			}
		}
		else
		{
			for (int i=0;i<q;i++)
			{
				ValueMetaInterface v = new ValueMeta("name"+i, ValueMetaInterface.TYPE_NUMBER);
				par.addValueMeta(v);
			}
		}

		return par;
	}
	
	public void writeLogRecord(LogTableInterface logTable, LogStatus status, Object subject) throws KettleException {
		try {
			RowMetaAndData logRecord = logTable.getLogRecord(status, subject);
			boolean update = (logTable.getKeyField()!=null) && !status.equals(LogStatus.START);
			String schemaTable = databaseMeta.getSchemaTableCombination(logTable.getSchemaName(), logTable.getTableName());
			RowMetaInterface rowMeta = logRecord.getRowMeta();
			Object[] rowData = logRecord.getData();
			
			if (update) {
				RowMetaInterface updateRowMeta = new RowMeta();
				Object[] updateRowData = new Object[rowMeta.size()];
				ValueMetaInterface keyValueMeta = rowMeta.getValueMeta(0);

				String sql = "UPDATE " + schemaTable + " SET ";
				for (int i = 1; i < rowMeta.size() ; i++) // Without ID_JOB or ID_BATCH
				{
					ValueMetaInterface valueMeta = rowMeta.getValueMeta(i);
					if (i > 1) {
						sql += ", ";
					}
					sql += databaseMeta.quoteField(valueMeta.getName()) + "=? ";
					updateRowMeta.addValueMeta(valueMeta);
					updateRowData[i-1] = rowData[i];
				}
				sql += "WHERE ";
				sql += databaseMeta.quoteField(keyValueMeta.getName()) + "=? ";
				updateRowMeta.addValueMeta(keyValueMeta);
				updateRowData[rowMeta.size()-1] = rowData[0];
				
				execStatement(sql, updateRowMeta, updateRowData);
				
			} else {
				
				insertRow(logTable.getSchemaName(), logTable.getTableName(), logRecord.getRowMeta(), logRecord.getData());

			}
			 
		} catch(Exception e) {
			throw new KettleDatabaseException("Unable to write log record to log table " + logTable.getTableName(), e);
		}
	}
	
	public void cleanupLogRecords(LogTableInterface logTable) throws KettleException {
		try {
			double timeout = Const.toDouble( Const.trim( environmentSubstitute( logTable.getTimeoutInDays())), 0.0 );
			if (timeout>0.000001) { 
				// The timeout has to be at least a few seconds, otherwise we don't bother
				//
				String schemaTable = databaseMeta.getSchemaTableCombination(logTable.getSchemaName(), logTable.getTableName());
				
				// The log date field
				//
				LogTableField logField = logTable.getLogDateField();
				if (logField!=null) {
					String sql = "DELETE FROM "+schemaTable+" WHERE "+databaseMeta.quoteField(logField.getFieldName())+" < ?"; // $NON-NLS$1 
					
					// Now calculate the date...
					//
					long now = System.currentTimeMillis();
					long limit = now - Math.round(timeout*24*60*60*1000);
					RowMetaAndData row = new RowMetaAndData();
					row.addValue(logField.getFieldName(), ValueMetaInterface.TYPE_DATE, new Date(limit));
					
					execStatement(sql, row.getRowMeta(), row.getData());
					
				} else {
					throw new KettleException(BaseMessages.getString(PKG, "Database.Exception.LogTimeoutDefinedOnTableWithoutLogField", logTable.getTableName()));
				}
			}
		} catch(Exception e) {
			throw new KettleDatabaseException(BaseMessages.getString(PKG, "Database.Exception.UnableToCleanUpOlderRecordsFromLogTable", logTable.getTableName()), e);
		}
	}
	

	public Object[] getLastLogDate( String logtable, String name, boolean job, LogStatus status ) throws KettleDatabaseException
	{
        Object[] row = null;
        
        String jobtrans = job?databaseMeta.quoteField("JOBNAME"):databaseMeta.quoteField("TRANSNAME");
		
		String sql = "";
		sql+=" SELECT "+databaseMeta.quoteField("ENDDATE")+", "+databaseMeta.quoteField("DEPDATE")+", "+databaseMeta.quoteField("STARTDATE");
		sql+=" FROM "+logtable;
		sql+=" WHERE  "+databaseMeta.quoteField("ERRORS")+"    = 0";
		sql+=" AND    "+databaseMeta.quoteField("STATUS")+"    = 'end'";
		sql+=" AND    "+jobtrans+" = ?";
		sql+=" ORDER BY "+databaseMeta.quoteField("LOGDATE")+" DESC, "+databaseMeta.quoteField("ENDDATE")+" DESC";

		try
		{
			pstmt = connection.prepareStatement(databaseMeta.stripCR(sql));
			
			RowMetaInterface r = new RowMeta();
			r.addValueMeta( new ValueMeta("TRANSNAME", ValueMetaInterface.TYPE_STRING));
			setValues(r, new Object[] { name });
			
			ResultSet res = pstmt.executeQuery();
			if (res!=null)
			{
				rowMeta = getRowInfo(res.getMetaData(), false, false);
				row = getRow(res);
				res.close();
			}
			pstmt.close(); pstmt=null;
		}
		catch(SQLException ex) 
		{
			throw new KettleDatabaseException("Unable to obtain last logdate from table "+logtable, ex);
		}
		
		return row;
	}
    


    public synchronized Long getNextValue(Hashtable<String,Counter> counters, String tableName, String val_key) throws KettleDatabaseException
    {
        return getNextValue(counters, null, tableName, val_key);
    }
    
	public synchronized Long getNextValue(Hashtable<String,Counter> counters, String schemaName, String tableName, String val_key) throws KettleDatabaseException
	{
        Long nextValue = null;
        
        String schemaTable = databaseMeta.getQuotedSchemaTableCombination(schemaName, tableName);
    
		String lookup = schemaTable+"."+databaseMeta.quoteField(val_key);
		
		// Try to find the previous sequence value...
		Counter counter = null;
        if (counters!=null) counter=counters.get(lookup);
        
		if (counter==null)
		{
			RowMetaAndData rmad = getOneRow("SELECT MAX("+databaseMeta.quoteField(val_key)+") FROM "+schemaTable);
			if (rmad!=null)
			{
                long previous;
                try
                {
                	Long tmp = rmad.getRowMeta().getInteger(rmad.getData(), 0);
                	
                	// A "select max(x)" on a table with no matching rows will return null.
                	if ( tmp != null )
                		previous = tmp.longValue();
                	else
                		previous = 0L;
                }
                catch (KettleValueException e)
                {
                    throw new KettleDatabaseException("Error getting the first long value from the max value returned from table : "+schemaTable);
                }
				counter = new Counter(previous+1, 1);
				nextValue = Long.valueOf( counter.next() );
				if (counters!=null) counters.put(lookup, counter);
			}
			else
			{
				throw new KettleDatabaseException("Couldn't find maximum key value from table "+schemaTable);
			}
		}
		else
		{
			nextValue = Long.valueOf( counter.next() );
		}
        
        return nextValue;
	}
			
	public String toString()
	{
		if (databaseMeta!=null) return databaseMeta.getName();
		else return "-";
	}

	public boolean isSystemTable(String table_name)
	{
		return databaseMeta.isSystemTable(table_name);
	}
	
	/** Reads the result of an SQL query into an ArrayList
	 * 
	 * @param sql The SQL to launch
	 * @param limit <=0 means unlimited, otherwise this specifies the maximum number of rows read.
	 * @return An ArrayList of rows.
	 * @throws KettleDatabaseException if something goes wrong.
	 */
	public List<Object[]> getRows(String sql, int limit) throws KettleDatabaseException
	{
	    return getRows(sql, limit, null);
	}

	/** Reads the result of an SQL query into an ArrayList
	 * 
	 * @param sql The SQL to launch
	 * @param limit <=0 means unlimited, otherwise this specifies the maximum number of rows read.
	 * @param monitor The progress monitor to update while getting the rows.
	 * @return An ArrayList of rows.
	 * @throws KettleDatabaseException if something goes wrong.
	 */
	public List<Object[]> getRows(String sql, int limit, ProgressMonitorListener monitor) throws KettleDatabaseException
	{
		if (monitor!=null) monitor.setTaskName("Opening query...");
		ResultSet rset = openQuery(sql);
		
        return getRows(rset, limit, monitor);
	}

    /** Reads the result of a ResultSet into an ArrayList
     * 
     * @param rset the ResultSet to read out
     * @param limit <=0 means unlimited, otherwise this specifies the maximum number of rows read.
     * @param monitor The progress monitor to update while getting the rows.
     * @return An ArrayList of rows.
     * @throws KettleDatabaseException if something goes wrong.
     */
	public List<Object[]> getRows(ResultSet rset, int limit, ProgressMonitorListener monitor) throws KettleDatabaseException
    {
        try
        {
            List<Object[]> result = new ArrayList<Object[]>();
            boolean stop=false;
            int i=0;
            
            if (rset!=null)
            {
                if (monitor!=null && limit>0) monitor.beginTask("Reading rows...", limit);
                while ((limit<=0 || i<limit) && !stop)
                {
                    Object[] row = getRow(rset);
                    if (row!=null)
                    {
                        result.add(row);
                        i++;
                    }
                    else
                    {
                        stop=true;
                    }
                    if (monitor!=null && limit>0) monitor.worked(1);
                }
                closeQuery(rset);
                if (monitor!=null) monitor.done();
            }
            
            return result;
        }
        catch(Exception e)
        {
            throw new KettleDatabaseException("Unable to get list of rows from ResultSet : ", e);
        }
    }

    public List<Object[]> getFirstRows(String table_name, int limit) throws KettleDatabaseException
	{
	    return getFirstRows(table_name, limit, null);
	}


    /**
     * Get the first rows from a table (for preview) 
     * @param table_name The table name (or schema/table combination): this needs to be quoted properly
     * @param limit limit <=0 means unlimited, otherwise this specifies the maximum number of rows read.
     * @param monitor The progress monitor to update while getting the rows.
     * @return An ArrayList of rows.
     * @throws KettleDatabaseException in case something goes wrong
     */
	public List<Object[]> getFirstRows(String table_name, int limit, ProgressMonitorListener monitor) throws KettleDatabaseException
	{
		String sql = "SELECT";
		if (databaseMeta.getDatabaseInterface() instanceof NeoviewDatabaseMeta)
		{
			sql+=" [FIRST " + limit +"]";
		}
		else if (databaseMeta.getDatabaseInterface() instanceof SybaseIQDatabaseMeta)  // improve support Sybase IQ
		{
			sql+=" TOP " + limit +" ";
		}
		sql += " * FROM "+table_name;
		
        if (limit>0)
		{
		    sql+=databaseMeta.getLimitClause(limit);
		}
		
		return getRows(sql, limit, monitor);
	}
	
	public RowMetaInterface getReturnRowMeta()
	{
		return rowMeta;
	}
	
	public String[] getTableTypes() throws KettleDatabaseException
	{
		try
		{
			ArrayList<String> types = new ArrayList<String>();
		
			ResultSet rstt = getDatabaseMetaData().getTableTypes();
	        while(rstt.next()) 
	        {
	            String ttype = rstt.getString("TABLE_TYPE");
	            types.add(ttype);
	        }
	        
	        return types.toArray(new String[types.size()]);
		}
		catch(SQLException e)
		{
			throw new KettleDatabaseException("Unable to get table types from database!", e);
		}
	}
		
	public String[] getTablenames() throws KettleDatabaseException
	{
		return getTablenames(false);
	}
	public String[] getTablenames(boolean includeSchema) throws KettleDatabaseException
	{
		return getTablenames(null, includeSchema);
	}
	public String[] getTablenames(String schemanamein, boolean includeSchema) throws KettleDatabaseException
	{
		String schemaname=schemanamein;
		if(schemaname==null) {
			if (databaseMeta.useSchemaNameForTableList()) schemaname = environmentSubstitute(databaseMeta.getUsername()).toUpperCase();
		}
		List<String> names = new ArrayList<String>();
		ResultSet alltables=null;
		try
		{
			alltables = getDatabaseMetaData().getTables(null, schemaname, null, databaseMeta.getTableTypes() );
			while (alltables.next())
			{
				// due to PDI-743 with ODBC and MS SQL Server the order is changed and try/catch included for safety
				String cat = "";
				try {
					cat = alltables.getString("TABLE_CAT");
				} catch (Exception e) {
					// ignore
					if(log.isDebug()) log.logDebug("Error getting tables for field TABLE_CAT (ignored): "+e.toString());
				}
				
				String schema = "";
				try {
					schema = alltables.getString("TABLE_SCHEM");
				} catch (Exception e) {
					// ignore
					if(log.isDebug()) log.logDebug("Error getting tables for field TABLE_SCHEM (ignored): "+e.toString());
				}
				
				if (Const.isEmpty(schema)) schema = cat;
				
				String table = alltables.getString("TABLE_NAME");
				
				String schemaTable;
				if (includeSchema) schemaTable = databaseMeta.getQuotedSchemaTableCombination(schema, table);
				else schemaTable = table;
				
                if (log.isRowLevel()) log.logRowlevel(toString(), "got table from meta-data: "+schemaTable);
				names.add(schemaTable);
			}
		}
		catch(SQLException e)
		{
			log.logError("Error getting tablenames from schema ["+schemaname+"]");
		}
		finally
		{
			try
			{
				if (alltables!=null) alltables.close();
			}
			catch(SQLException e)
			{
                throw new KettleDatabaseException("Error closing resultset after getting views from schema ["+schemaname+"]", e);
			}
		}

		if(log.isDetailed()) log.logDetailed("read :"+names.size()+" table names from db meta-data.");

		return names.toArray(new String[names.size()]);
	}
	
	public String[] getViews() throws KettleDatabaseException
	{
		return getViews(false);
	}
	public String[] getViews(boolean includeSchema) throws KettleDatabaseException
	{
		return  getViews(null, includeSchema);
	}
	public String[] getViews(String schemanamein, boolean includeSchema) throws KettleDatabaseException
	{
		if (!databaseMeta.supportsViews()) return new String[] {};

		String schemaname = schemanamein;
		if(schemaname==null) {
			if (databaseMeta.useSchemaNameForTableList()) schemaname = environmentSubstitute(databaseMeta.getUsername()).toUpperCase();
		}
		
		ArrayList<String> names = new ArrayList<String>();
		ResultSet alltables=null;
		try
		{
			alltables = dbmd.getTables(null, schemaname, null, databaseMeta.getViewTypes() );
			while (alltables.next())
			{
				// due to PDI-743 with ODBC and MS SQL Server the order is changed and try/catch included for safety
				String cat = "";
				try {
					cat = alltables.getString("TABLE_CAT");
				} catch (Exception e) {
					// ignore
					if(log.isDebug()) log.logDebug("Error getting views for field TABLE_CAT (ignored): "+e.toString());
				}
				
				String schema = "";
				try {
					schema = alltables.getString("TABLE_SCHEM");
				} catch (Exception e) {
					// ignore
					if(log.isDebug()) log.logDebug("Error getting views for field TABLE_SCHEM (ignored): "+e.toString());
				}
				
				if (Const.isEmpty(schema)) schema = cat;
				
				String table = alltables.getString("TABLE_NAME");
				
				String schemaTable;
				if (includeSchema) schemaTable = databaseMeta.getQuotedSchemaTableCombination(schema, table);
				else schemaTable = table;
				
				if (log.isRowLevel()) log.logRowlevel(toString(), "got view from meta-data: "+schemaTable);
				names.add(schemaTable);
			}
		}
		catch(SQLException e)
		{
			throw new KettleDatabaseException("Error getting views from schema ["+schemaname+"]", e);
		}
		finally
		{
			try
			{
				if (alltables!=null) alltables.close();
			}
			catch(SQLException e)
			{
				throw new KettleDatabaseException("Error closing resultset after getting views from schema ["+schemaname+"]", e);
			}
		}

		if(log.isDetailed()) log.logDetailed("read :"+names.size()+" views from db meta-data.");

		return names.toArray(new String[names.size()]);
	}

	public String[] getSynonyms() throws KettleDatabaseException
	{
		return getSynonyms(false);
	}
	public String[] getSynonyms(boolean includeSchema) throws KettleDatabaseException
	{
		return getSynonyms(null,includeSchema);
	}
	public String[] getSynonyms(String schemanamein, boolean includeSchema) throws KettleDatabaseException
	{
		if (!databaseMeta.supportsSynonyms()) return new String[] {};
		
		String schemaname = schemanamein;
		if(schemaname==null) {
			if (databaseMeta.useSchemaNameForTableList()) schemaname = environmentSubstitute(databaseMeta.getUsername()).toUpperCase();
		}
		
		ArrayList<String> names = new ArrayList<String>();
		ResultSet alltables=null;
		try
		{
			alltables = dbmd.getTables(null, schemaname, null, databaseMeta.getSynonymTypes() );
			while (alltables.next())
			{
				// due to PDI-743 with ODBC and MS SQL Server the order is changed and try/catch included for safety
				String cat = "";
				try {
					cat = alltables.getString("TABLE_CAT");
				} catch (Exception e) {
					// ignore
					if(log.isDebug()) log.logDebug("Error getting synonyms for field TABLE_CAT (ignored): "+e.toString());
				}
				
				String schema = "";
				try {
					schema = alltables.getString("TABLE_SCHEM");
				} catch (Exception e) {
					// ignore
					if(log.isDebug()) log.logDebug("Error getting synonyms for field TABLE_SCHEM (ignored): "+e.toString());
				}
				
				if (Const.isEmpty(schema)) schema = cat;
				
				String table = alltables.getString("TABLE_NAME");
				
				String schemaTable;
				if (includeSchema) schemaTable = databaseMeta.getQuotedSchemaTableCombination(schema, table);
				else schemaTable = table;
				
				if (log.isRowLevel()) log.logRowlevel(toString(), "got view from meta-data: "+schemaTable);
				names.add(schemaTable);
			}
		}
		catch(SQLException e)
		{
			throw new KettleDatabaseException("Error getting synonyms from schema ["+schemaname+"]", e);
		}
		finally
		{
			try
			{
				if (alltables!=null) alltables.close();
			}
			catch(SQLException e)
			{
				throw new KettleDatabaseException("Error closing resultset after getting synonyms from schema ["+schemaname+"]", e);
			}
		}
	
		if(log.isDetailed()) log.logDetailed("read :"+names.size()+" views from db meta-data.");
	
		return names.toArray(new String[names.size()]);
	}
	public String[] getSchemas() throws KettleDatabaseException
	{
		ArrayList<String> catalogList = new ArrayList<String>();
		ResultSet catalogResultSet=null;
		try
		{
			catalogResultSet =getDatabaseMetaData().getSchemas();
			// Grab all the catalog names and put them in an array list
			while (catalogResultSet!=null && catalogResultSet.next())
			{
				catalogList.add(catalogResultSet.getString(1));
			}
		}
		catch(SQLException e)
		{
			throw new KettleDatabaseException("Error getting schemas!", e);
		}
		finally
		{
			try
			{
				if (catalogResultSet!=null) catalogResultSet.close();
			}
			catch(SQLException e)
			{
				throw new KettleDatabaseException("Error closing resultset after getting schemas!", e);
			}
		}
	
		if(log.isDetailed()) log.logDetailed("read :"+catalogList.size()+" schemas from db meta-data.");
	
		return catalogList.toArray(new String[catalogList.size()]);
	}
	public String[] getCatalogs() throws KettleDatabaseException
	{
		ArrayList<String> catalogList = new ArrayList<String>();
		ResultSet catalogResultSet=null;
		try
		{
			catalogResultSet =getDatabaseMetaData().getCatalogs();
			// Grab all the catalog names and put them in an array list
			while (catalogResultSet!=null && catalogResultSet.next())
			{
				catalogList.add(catalogResultSet.getString(1));
			}
		}
		catch(SQLException e)
		{
			throw new KettleDatabaseException("Error getting catalogs!", e);
		}
		finally
		{
			try
			{
				if (catalogResultSet!=null) catalogResultSet.close();
			}
			catch(SQLException e)
			{
				throw new KettleDatabaseException("Error closing resultset after getting catalogs!", e);
			}
		}
	
		if(log.isDetailed()) log.logDetailed(toString(), "read :"+catalogList.size()+" catalogs from db meta-data.");
	
		return catalogList.toArray(new String[catalogList.size()]);
	}
	public String[] getProcedures() throws KettleDatabaseException
	{
		String sql = databaseMeta.getSQLListOfProcedures();
		if (sql!=null)
		{
			//System.out.println("SQL= "+sql);
			List<Object[]> procs = getRows(sql, 1000);
			//System.out.println("Found "+procs.size()+" rows");
			String[] str = new String[procs.size()];
			for (int i=0;i<procs.size();i++)
			{
				str[i] = ((Object[])procs.get(i))[0].toString();
			}
			return str;
		}
        else
        {
            ResultSet rs = null;
            try
            {
                DatabaseMetaData dbmd = getDatabaseMetaData();
                rs = dbmd.getProcedures(null, null, null);
                List<Object[]> rows = getRows(rs, 0, null);
                String result[] = new String[rows.size()];
                for (int i=0;i<rows.size();i++)
                {
                    Object[] row = (Object[])rows.get(i);
                    String procCatalog = rowMeta.getString(row, "PROCEDURE_CAT", null);
                    String procSchema  = rowMeta.getString(row, "PROCEDURE_SCHEMA", null);
                    String procName    = rowMeta.getString(row, "PROCEDURE_NAME", "");
 
                    String name = "";
                    if (procCatalog!=null) name+=procCatalog+".";
                    else if (procSchema!=null) name+=procSchema+".";
                    
                    name+=procName;
                    
                    result[i] = name;
                }
                return result;
            }
            catch(Exception e)
            {
                throw new KettleDatabaseException("Unable to get list of procedures from database meta-data: ", e);
            }
            finally
            {
                if (rs!=null) try { rs.close(); } catch(Exception e) {}
            }
        }
	}

    public boolean isAutoCommit()
    {
        return commitsize<=0;
    }

    /**
     * @return Returns the databaseMeta.
     */
    public DatabaseMeta getDatabaseMeta()
    {
        return databaseMeta;
    }

    /**
     * Lock a tables in the database for write operations
     * @param tableNames The tables to lock
     * @throws KettleDatabaseException
     */
    public void lockTables(String tableNames[]) throws KettleDatabaseException
    {
    	if (Const.isEmpty(tableNames)) return;
    	
    	// Quote table names too...
    	//
    	String[] quotedTableNames = new String[tableNames.length];
    	for (int i=0;i<tableNames.length;i++) quotedTableNames[i] = databaseMeta.getQuotedSchemaTableCombination(null, tableNames[i]);
    	
    	// Get the SQL to lock the (quoted) tables
    	//
        String sql = databaseMeta.getSQLLockTables(quotedTableNames);
        if (sql!=null)
        {
            execStatements(sql);
        }
    }
	
    /**
     * Unlock certain tables in the database for write operations
     * @param tableNames The tables to unlock
     * @throws KettleDatabaseException
     */
    public void unlockTables(String tableNames[]) throws KettleDatabaseException
    {
    	if (Const.isEmpty(tableNames)) return;
    	
    	// Quote table names too...
    	//
    	String[] quotedTableNames = new String[tableNames.length];
    	for (int i=0;i<tableNames.length;i++) quotedTableNames[i] = databaseMeta.getQuotedSchemaTableCombination(null, tableNames[i]);
    	
    	// Get the SQL to unlock the (quoted) tables
    	//
        String sql = databaseMeta.getSQLUnlockTables(quotedTableNames);
        if (sql!=null)
        {
            execStatement(sql);
        }
    }

    /**
     * @return the opened
     */
    public int getOpened()
    {
        return opened;
    }

    /**
     * @param opened the opened to set
     */
    public void setOpened(int opened)
    {
        this.opened = opened;
    }

    /**
     * @return the connectionGroup
     */
    public String getConnectionGroup()
    {
        return connectionGroup;
    }

    /**
     * @param connectionGroup the connectionGroup to set
     */
    public void setConnectionGroup(String connectionGroup)
    {
        this.connectionGroup = connectionGroup;
    }

    /**
     * @return the partitionId
     */
    public String getPartitionId()
    {
        return partitionId;
    }

    /**
     * @param partitionId the partitionId to set
     */
    public void setPartitionId(String partitionId)
    {
        this.partitionId = partitionId;
    }

    /**
     * @return the copy
     */
    public int getCopy()
    {
        return copy;
    }

    /**
     * @param copy the copy to set
     */
    public void setCopy(int copy)
    {
        this.copy = copy;
    }
    
	public void copyVariablesFrom(VariableSpace space) 
	{
		variables.copyVariablesFrom(space);		
	}

	public String environmentSubstitute(String aString) 
	{
		return variables.environmentSubstitute(aString);
	}	

	public String[] environmentSubstitute(String aString[]) 
	{
		return variables.environmentSubstitute(aString);
	}		

	public VariableSpace getParentVariableSpace() 
	{
		return variables.getParentVariableSpace();
	}
	
	public void setParentVariableSpace(VariableSpace parent) 
	{
		variables.setParentVariableSpace(parent);
	}

	public String getVariable(String variableName, String defaultValue) 
	{
		return variables.getVariable(variableName, defaultValue);
	}

	public String getVariable(String variableName) 
	{
		return variables.getVariable(variableName);
	}
	
	public boolean getBooleanValueOfVariable(String variableName, boolean defaultValue) {
		if (!Const.isEmpty(variableName))
		{
			String value = environmentSubstitute(variableName);
			if (!Const.isEmpty(value))
			{
				return ValueMeta.convertStringToBoolean(value);
			}
		}
		return defaultValue;
	}

	public void initializeVariablesFrom(VariableSpace parent) 
	{
		variables.initializeVariablesFrom(parent);	
	}

	public String[] listVariables() 
	{
		return variables.listVariables();
	}

	public void setVariable(String variableName, String variableValue) 
	{
		variables.setVariable(variableName, variableValue);		
	}

	public void shareVariablesWith(VariableSpace space) 
	{
		variables = space;
		
		// Also share the variables with the meta data object
		// Make sure it's not the databaseMeta object itself. We would get an infinite loop in that case.
		//
		if (space!=databaseMeta) databaseMeta.shareVariablesWith(space);
	}

	public void injectVariables(Map<String,String> prop) 
	{
		variables.injectVariables(prop);		
	}  
	
	public RowMetaAndData callProcedure(String arg[], String argdir[], int argtype[],
			String resultname, int resulttype) throws KettleDatabaseException {
		RowMetaAndData ret;
		try {
			cstmt.execute();

			ret = new RowMetaAndData();
			int pos = 1;
			if (resultname != null && resultname.length() != 0) {
				ValueMeta vMeta = new ValueMeta(resultname, resulttype);
				Object v =null;
				switch (resulttype) {
				case ValueMetaInterface.TYPE_BOOLEAN:
					v=Boolean.valueOf(cstmt.getBoolean(pos));
					break;
				case ValueMetaInterface.TYPE_NUMBER:
					v=new Double(cstmt.getDouble(pos));
					break;
				case ValueMetaInterface.TYPE_BIGNUMBER:
					v=cstmt.getBigDecimal(pos);
					break;
				case ValueMetaInterface.TYPE_INTEGER:
					v=Long.valueOf(cstmt.getLong(pos));
					break;
				case ValueMetaInterface.TYPE_STRING:
					v=cstmt.getString(pos);
					break;
				case ValueMetaInterface.TYPE_BINARY: 
                    if (databaseMeta.supportsGetBlob())
                    {
                        Blob blob = cstmt.getBlob(pos);
                        if (blob!=null)
                        {
                            v = blob.getBytes(1L, (int)blob.length());
                        }
                        else
                        {
                            v = null;
                        }
                    }
                    else
                    {
                        v = cstmt.getBytes(pos);
                    }
	                break;					
				case ValueMetaInterface.TYPE_DATE:
					if (databaseMeta.supportsTimeStampToDateConversion())
                    {
						v=cstmt.getTimestamp(pos);
                    }
                    else 
                    {
                    	v=cstmt.getDate(pos); 
                    }					
					break;
				}
				ret.addValue(vMeta, v);
				pos++;
			}
			for (int i = 0; i < arg.length; i++) {
				if (argdir[i].equalsIgnoreCase("OUT")
						|| argdir[i].equalsIgnoreCase("INOUT")) {
					ValueMeta vMeta = new ValueMeta(arg[i], argtype[i]);
					Object v=null;
					switch (argtype[i]) {
					case ValueMetaInterface.TYPE_BOOLEAN:
						v=Boolean.valueOf(cstmt.getBoolean(pos + i));
						break;
					case ValueMetaInterface.TYPE_NUMBER:
						v=new Double(cstmt.getDouble(pos + i));
						break;
					case ValueMetaInterface.TYPE_BIGNUMBER:
						v=cstmt.getBigDecimal(pos + i);
						break;
					case ValueMetaInterface.TYPE_INTEGER:
						v=Long.valueOf(cstmt.getLong(pos + i));
						break;
					case ValueMetaInterface.TYPE_STRING:
						v=cstmt.getString(pos + i);
						break;
					case ValueMetaInterface.TYPE_BINARY: 
	                    if (databaseMeta.supportsGetBlob())
	                    {
	                        Blob blob = cstmt.getBlob(pos + i);
	                        if (blob!=null)
	                        {
	                            v = blob.getBytes(1L, (int)blob.length());
	                        }
	                        else
	                        {
	                            v = null;
	                        }
	                    }
	                    else
	                    {
	                        v = cstmt.getBytes(pos + i);
	                    }
		                break;					
					case ValueMetaInterface.TYPE_DATE:
						if (databaseMeta.supportsTimeStampToDateConversion())
	                    {
							v=cstmt.getTimestamp(pos + i);
	                    }
	                    else 
	                    {
	                    	v=cstmt.getDate(pos + i); 
	                    }					
						break;
					}
					ret.addValue(vMeta, v);
				}
			}

			return ret;
		} catch (SQLException ex) {
			throw new KettleDatabaseException("Unable to call procedure", ex);
		}
	}
	
	
	/**
     * Return SQL CREATION statement for a Table
     * @param tableName The table to create
     * @throws KettleDatabaseException
     */
	
	public String getDDLCreationTable(String tableName, RowMetaInterface fields) throws KettleDatabaseException
	{
		String retval;
		
		// First, check for reserved SQL in the input row r...
		databaseMeta.quoteReservedWords(fields);
		String quotedTk=databaseMeta.quoteField(null);
		retval=getCreateTableStatement(tableName, fields, quotedTk, false, null, true);
		
		return retval;
	}
	
	/**
	 * Return SQL TRUNCATE statement for a Table
	 * @param schema The schema
	 * @param tableNameWithSchema The table to create
	 * @throws KettleDatabaseException
	 */
	public String getDDLTruncateTable(String schema, String tablename) throws KettleDatabaseException
	{
        if (Const.isEmpty(connectionGroup))
        {
            return(databaseMeta.getTruncateTableStatement(schema, tablename));
        }
        else
        {
            return("DELETE FROM "+databaseMeta.getQuotedSchemaTableCombination(schema, tablename));
        }
	}
	
	/**
	 * Return SQL statement (INSERT INTO TableName ...
	 * @param schemaName tableName The schema 
	 * @param tableName
	 * @param fields
	 * @param dateFormat date format of field
	 * @throws KettleDatabaseException
	 */

    public String getSQLOutput(String schemaName, String tableName, RowMetaInterface fields, Object[] r,String dateFormat) throws KettleDatabaseException
	{
		StringBuffer ins=new StringBuffer(128);
		
		try{
        String schemaTable = databaseMeta.getQuotedSchemaTableCombination(schemaName, tableName);
		ins.append("INSERT INTO ").append(schemaTable).append('(');

		// now add the names in the row:
		for (int i=0;i<fields.size();i++)
		{
			if (i>0) ins.append(", ");
			String name = fields.getValueMeta(i).getName();
			ins.append(databaseMeta.quoteField(name));
			
		}
		ins.append(") VALUES (");

		java.text.SimpleDateFormat[] fieldDateFormatters  = new java.text.SimpleDateFormat[fields.size()];

		// new add values ...
		for (int i=0;i<fields.size();i++)
		{
			ValueMetaInterface valueMeta = fields.getValueMeta(i);
			Object valueData = r[i];
			
			
			if (i>0) ins.append(",");
			
			// Check for null values...
			//
			if (valueMeta.isNull(valueData)) {
				ins.append("null");
			} else {
				// Normal cases...
				//
				switch(valueMeta.getType()) {
				case ValueMetaInterface.TYPE_BOOLEAN:
				case ValueMetaInterface.TYPE_STRING:
					String string = valueMeta.getString(valueData);
					if (databaseMeta.getDatabaseInterface() instanceof MySQLDatabaseMeta) {
						string = string.replaceAll("'", "\\\\'"); 
						// string = string.replaceAll("'", "''"); 
						string = string.replaceAll("\\n", "\\\\n");
						string = string.replaceAll("\\r", "\\\\r");
					} else {
						string = string.replaceAll("'", "\\\\'"); 
						string = string.replaceAll("\\n", "\\\\n");
						string = string.replaceAll("\\r", "\\\\r");
					}

					ins.append("'" + string + "'") ;
					break;
				case ValueMetaInterface.TYPE_DATE:
					Date date = fields.getDate(r,i);
					
					if (Const.isEmpty(dateFormat))
						if (databaseMeta.getDatabaseInterface() instanceof OracleDatabaseMeta) {
							if (fieldDateFormatters[i]==null) {
								fieldDateFormatters[i]=new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
							}
							ins.append("TO_DATE('").append(fieldDateFormatters[i].format(date)).append("', 'YYYY/MM/DD HH24:MI:SS')");
						} else {
							ins.append("'" +  fields.getString(r,i)+ "'") ;
						}
					else
					{
						try 
						{
							java.text.SimpleDateFormat formatter  = new java.text.SimpleDateFormat(dateFormat);
							ins.append("'" + formatter.format(fields.getDate(r,i))+ "'") ;
						}
						catch(Exception e)
			            {
			                throw new KettleDatabaseException("Error : ", e);
			            }
					}
					break;
				default:
					ins.append( fields.getString(r,i)) ;
					break;
				}
			}
			
		}
		ins.append(')');
		}catch (Exception e)
		{
			throw new KettleDatabaseException(e);
		}
		return ins.toString();
	}
    
    public Savepoint setSavepoint() throws KettleDatabaseException {
    	try {
			return connection.setSavepoint();
		} catch (SQLException e) {
			throw new KettleDatabaseException(BaseMessages.getString(PKG, "Database.Exception.UnableToSetSavepoint"), e);
		}
    }

    public Savepoint setSavepoint(String savePointName) throws KettleDatabaseException {
    	try {
			return connection.setSavepoint(savePointName);
		} catch (SQLException e) {
			throw new KettleDatabaseException(BaseMessages.getString(PKG, "Database.Exception.UnableToSetSavepointName", savePointName), e);
		}
    }

	public void releaseSavepoint(Savepoint savepoint) throws KettleDatabaseException {
		try {
			connection.releaseSavepoint(savepoint);
		} catch (SQLException e) {
			throw new KettleDatabaseException(BaseMessages.getString(PKG, "Database.Exception.UnableToReleaseSavepoint"), e);
		}
	}

	public void rollback(Savepoint savepoint) throws KettleDatabaseException {
		try {
			connection.rollback(savepoint);
		} catch (SQLException e) {
			throw new KettleDatabaseException(BaseMessages.getString(PKG, "Database.Exception.UnableToRollbackToSavepoint"), e);
		}
	}

	public Object getParentObject() {
		return parentLoggingObject;
	}
	
	/**
	 * Return primary key column names ...
	 * @param tablename 
	 * @throws KettleDatabaseException
	 */
	public String[] getPrimaryKeyColumnNames(String tablename) throws KettleDatabaseException {
	List<String> names = new ArrayList<String>();
	ResultSet allkeys=null;
	try {
		allkeys=getDatabaseMetaData().getPrimaryKeys(null, null, tablename);
		while (allkeys.next()) {
			String keyname=allkeys.getString("PK_NAME");
			String col_name=allkeys.getString("COLUMN_NAME");
			if(!names.contains(col_name)) names.add(col_name);
            if (log.isRowLevel()) log.logRowlevel(toString(), "getting key : "+keyname + " on column "+col_name);
		}
	}
	catch(SQLException e) {
		log.logError(toString(), "Error getting primary keys columns from table ["+tablename+"]");
	}
	finally {
		try {
			if (allkeys!=null) allkeys.close();
		} catch(SQLException e) {
            throw new KettleDatabaseException("Error closing connection while searching primary keys in table ["+tablename+"]", e);
		}
	}
	return names.toArray(new String[names.size()]);
  }

	public String getFilename() {
		return null;
	}

	public String getLogChannelId() {
		return log.getLogChannelId();
	}

	public String getObjectName() {
		return databaseMeta.getName();
	}

	public String getObjectCopy() {
		return null;
	}

	public ObjectId getObjectId() {
		return databaseMeta.getObjectId();
	}

	public ObjectRevision getObjectRevision() {
		return databaseMeta.getObjectRevision();
	}

	public LoggingObjectType getObjectType() {
		return LoggingObjectType.DATABASE;
	}

	public LoggingObjectInterface getParent() {
		return parentLoggingObject;
	}

	public RepositoryDirectory getRepositoryDirectory() {
		return null;
	}

  @Override
  public int getLogLevel() {
    return logLevel;
  }

  @Override
  public void setLogLevel(int logLevel) {
   this.logLevel = logLevel;
  }
}
