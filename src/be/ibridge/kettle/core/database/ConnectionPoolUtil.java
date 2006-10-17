package be.ibridge.kettle.core.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDriver;
import org.apache.commons.pool.impl.GenericObjectPool;

import be.ibridge.kettle.core.util.StringUtil;

public class ConnectionPoolUtil
{
    private static PoolingDriver pd=new PoolingDriver();
    
    public static final int defaultInitialNrOfConnections=5;
    public static final int defaultMaximumNrOfConnections=10;
    
    public static void createPoolingDriver(List dbDatabaseMeta, int initialSize, int maxiumumSize) throws Exception
    {
        //TODO:how to check if a given dbMeta has been processed
        
        for (Iterator iter = dbDatabaseMeta.iterator(); iter.hasNext();)
        {
            DatabaseMeta dbMeta = (DatabaseMeta) iter.next();
            if(isPoolRegiested(dbMeta))
                continue;
            
            createPool(dbMeta, initialSize, maxiumumSize);
        }       
    }
        
    private static boolean isPoolRegiested(DatabaseMeta dbMeta)
    {
        String[] poolNames=null;
        try
        {
            poolNames = pd.getPoolNames();
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
        if(poolNames!=null)
        {
            String name = dbMeta.getName();
            for (int i = 0; i < poolNames.length; i++)
            {               
                if(poolNames[i].equals(name))
                    return true;
            }
        }
        
        return false;
    }
    
    private static void createPool(DatabaseMeta databaseMeta, int initialSize, int maximumSize) throws Exception
    {
        GenericObjectPool gpool=new GenericObjectPool();
        //no limit to the number of connections holded in the pool
        gpool.setMaxIdle(-1);
        gpool.setWhenExhaustedAction(GenericObjectPool.WHEN_EXHAUSTED_GROW);
        gpool.setMaxActive(maximumSize);
        
        String clazz = databaseMeta.getDriverClass();
        //TODO:exception handle
        try
        {
            Class.forName(clazz).newInstance();
        }
        catch(Exception e)
        {
            throw new Exception("Unable to load driver for connect ["+databaseMeta.getName()+"], class ["+clazz+"]", e);
        }
        
        String url;
        String userName;
        String password;
        
        try
        {
            url= StringUtil.environmentSubstitute(databaseMeta.getURL());       
            userName= StringUtil.environmentSubstitute(databaseMeta.getUsername());     
            password= StringUtil.environmentSubstitute(databaseMeta.getPassword());
        } 
        catch (RuntimeException e)
        {
            url=databaseMeta.getURL();
            userName=databaseMeta.getUsername();
            password=databaseMeta.getPassword();
        }
        
        ConnectionFactory cf=new DriverManagerConnectionFactory(url,userName,password);
        //TODO:the last parameter:commitsize
        /*PoolableConnectionFactory pcf=*/
        new PoolableConnectionFactory(cf, gpool, null, null, false, false);
        
        for (int i = 0; i < initialSize; i++)
        {
            gpool.addObject();
        }
        
        pd.registerPool(databaseMeta.getName(),gpool);
    
    }
    
    
    public static Connection getConnection(DatabaseMeta dbMeta) throws Exception
    {
        return getConnection(dbMeta, dbMeta.getInitialPoolSize(), dbMeta.getMaximumPoolSize());
    }
    
    public static Connection getConnection(DatabaseMeta dbMeta,int initialSize, int maximumSize) throws Exception
    {
        if(!isPoolRegiested(dbMeta))
        {
            createPool(dbMeta, initialSize, maximumSize);
        }
        
        return DriverManager.getConnection("jdbc:apache:commons:dbcp:"+dbMeta.getName());
    
    }
    
}
