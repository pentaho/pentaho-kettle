package org.pentaho.di.core.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDriver;
import org.apache.commons.pool.impl.GenericObjectPool;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.util.StringUtil;

public class ConnectionPoolUtil
{
    private static PoolingDriver pd=new PoolingDriver();
    
    public static final int defaultInitialNrOfConnections=5;
    public static final int defaultMaximumNrOfConnections=10;
    
    /**
     * @deprecated as it doesn't take the partitionId into account as it should.
     * @param dbDatabaseMeta
     * @param initialSize
     * @param maxiumumSize
     * @throws Exception
     */
    public static void createPoolingDriver(List dbDatabaseMeta, int initialSize, int maxiumumSize) throws Exception
    {
        //TODO:how to check if a given dbMeta has been processed
        
        for (Iterator iter = dbDatabaseMeta.iterator(); iter.hasNext();)
        {
            DatabaseMeta dbMeta = (DatabaseMeta) iter.next();
            if(isPoolRegiested(dbMeta, null))
                continue;
            
            createPool(dbMeta, null, initialSize, maxiumumSize);
        }       
    }
        
    private static boolean isPoolRegiested(DatabaseMeta dbMeta, String partitionId) throws KettleDatabaseException
    {
        try
        {
            String[] poolNames = pd.getPoolNames();
            if(poolNames!=null)
            {
                String name = dbMeta.getName()+Const.NVL(partitionId, "");
                for (int i = 0; i < poolNames.length; i++)
                {               
                    if(poolNames[i].equals(name))
                        return true;
                }
            }
            
            return false;
        } 
        catch (SQLException e)
        {
            throw new KettleDatabaseException("Error checking if the connection pool is registered", e);
        }
    }
    
    private static void createPool(DatabaseMeta databaseMeta, String partitionId, int initialSize, int maximumSize) throws KettleDatabaseException
    {
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
            throw new KettleDatabaseException("Unable to load driver for connect ["+databaseMeta.getName()+"], class ["+clazz+"]", e);
        }
        
        String url;
        String userName;
        String password;
        
        try
        {
            url= StringUtil.environmentSubstitute(databaseMeta.getURL(partitionId));       
            userName= StringUtil.environmentSubstitute(databaseMeta.getUsername());     
            password= StringUtil.environmentSubstitute(databaseMeta.getPassword());
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
        Iterator iterator = originalProperties.keySet().iterator();
        while (iterator.hasNext())
        {
            String key = (String) iterator.next();
            String value = originalProperties.getProperty(key);
            properties.put(key, StringUtil.environmentSubstitute(value));
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
                throw new KettleDatabaseException("Unable to pre-load connection to the connection pool", e);
            }
        }
        
        pd.registerPool(databaseMeta.getName(),gpool);
    
    }
    
    
    public static Connection getConnection(DatabaseMeta dbMeta, String partitionId) throws Exception
    {
        return getConnection(dbMeta, partitionId, dbMeta.getInitialPoolSize(), dbMeta.getMaximumPoolSize());
    }
    
    public static Connection getConnection(DatabaseMeta dbMeta, String partitionId,int initialSize, int maximumSize) throws Exception
    {
        if(!isPoolRegiested(dbMeta, partitionId))
        {
            createPool(dbMeta, partitionId, initialSize, maximumSize);
        }
        
        return DriverManager.getConnection("jdbc:apache:commons:dbcp:"+dbMeta.getName());
    
    }
    
}
