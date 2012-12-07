/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.core.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Properties;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDriver;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.i18n.BaseMessages;

public class ConnectionPoolUtil
{
	private static Class<?> PKG = Database.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    private static PoolingDriver pd=new PoolingDriver();
    
    public static final int defaultInitialNrOfConnections=5;
    public static final int defaultMaximumNrOfConnections=10;
        
    private static boolean isPoolRegistered(DatabaseMeta dbMeta, String partitionId) throws KettleDatabaseException
    {
        try
        {
        	String name = dbMeta.getName()+Const.NVL(partitionId, "");;
            return Const.indexOfString(name, pd.getPoolNames())>=0;
        } 
        catch (SQLException e)
        {
            throw new KettleDatabaseException(BaseMessages.getString(PKG, "Database.UnableToCheckIfConnectionPoolExists.Exception"), e);
        }
    }
    
    private static void createPool(LogChannelInterface log, DatabaseMeta databaseMeta, String partitionId, int initialSize, int maximumSize) throws KettleDatabaseException
    {
    	log.logBasic(BaseMessages.getString(PKG, "Database.CreatingConnectionPool", databaseMeta.getName()));
        GenericObjectPool gpool=new GenericObjectPool();
        
        gpool.setMaxIdle(-1);
        gpool.setWhenExhaustedAction(GenericObjectPool.WHEN_EXHAUSTED_GROW);
        gpool.setMaxActive(maximumSize);
        
        String clazz = databaseMeta.getDriverClass();
        try
        {
            Class.forName(clazz).newInstance();
        }
        catch(Exception e)
        {
            throw new KettleDatabaseException(BaseMessages.getString(PKG, "Database.UnableToLoadConnectionPoolDriver.Exception", databaseMeta.getName(), clazz), e);
        }
        
        String url;
        String userName;
        String password;
        
        try
        {
            url= databaseMeta.environmentSubstitute(databaseMeta.getURL(partitionId));       
            userName= databaseMeta.environmentSubstitute(databaseMeta.getUsername());     
            password= databaseMeta.environmentSubstitute(databaseMeta.getPassword());
        } 
        catch (RuntimeException e)
        {
            url=databaseMeta.getURL(partitionId);
            userName=databaseMeta.getUsername();
            password=databaseMeta.getPassword();
        }
        
        // Get the list of pool properties
        Properties originalProperties = databaseMeta.getConnectionPoolingProperties();
        //Add user/pass
        originalProperties.setProperty("user",     Const.NVL(userName, ""));
        originalProperties.setProperty("password", Const.NVL(password, ""));
        
        // Now, replace the environment variables in there...
        Properties properties = new Properties();
        Iterator<Object> iterator = originalProperties.keySet().iterator();
        while (iterator.hasNext())
        {
            String key = (String) iterator.next();
            String value = originalProperties.getProperty(key);
            properties.put(key, databaseMeta.environmentSubstitute(value));
        }
        
        // Create factory using these properties.
        //
        ConnectionFactory cf=new DriverManagerConnectionFactory(url,properties);
        
        new PoolableConnectionFactory(cf, gpool, null, null, false, false);
        
        for (int i = 0; i < initialSize; i++)
        {
            try
            {
                gpool.addObject();
            }
            catch(Exception e)
            {
                throw new KettleDatabaseException(BaseMessages.getString(PKG, "Database.UnableToPreLoadConnectionToConnectionPool.Exception"), e);
            }
        }
        
        pd.registerPool(databaseMeta.getName(),gpool);
    
        log.logBasic(BaseMessages.getString(PKG, "Database.CreatedConnectionPool", databaseMeta.getName()));
    }
    
    
    public static Connection getConnection(LogChannelInterface log, DatabaseMeta dbMeta, String partitionId) throws Exception
    {
        return getConnection(log, dbMeta, partitionId, dbMeta.getInitialPoolSize(), dbMeta.getMaximumPoolSize());
    }
    
    public static Connection getConnection(LogChannelInterface log, DatabaseMeta dbMeta, String partitionId,int initialSize, int maximumSize) throws Exception
    {
        if(!isPoolRegistered(dbMeta, partitionId))
        {
            createPool(log, dbMeta, partitionId, initialSize, maximumSize);
        }
        
        return DriverManager.getConnection("jdbc:apache:commons:dbcp:"+dbMeta.getName());
    
    }
    
}
