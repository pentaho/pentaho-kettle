package org.pentaho.di.core.database.map;

import java.util.Hashtable;
import java.util.Map;

import org.pentaho.di.core.database.Database;

import org.pentaho.di.core.Const;

/**
 * This class contains a map between on the one hand
 * 
 *      the transformation name/thread 
 *      the partition ID
 *      the connection group
 *      
 * And on the other hand 
 *      
 *      The database connection
 *      The number of times it was opened
 *      
 * @author Matt
 *
 */
public class DatabaseConnectionMap
{
    private Map map;
    
    private static DatabaseConnectionMap connectionMap;
    
    public synchronized static final DatabaseConnectionMap getInstance()
    {
        if (connectionMap!=null) return connectionMap;
        connectionMap = new DatabaseConnectionMap();
        return connectionMap;
    }
    
    private DatabaseConnectionMap()
    {
        map = new Hashtable();
    }
    
    public synchronized void storeDatabase(String connectionGroup, String partitionID, Database database)
    {
        String key = createEntryKey(connectionGroup, partitionID, database);
        map.put(key, database);
    }
    
    public synchronized void removeConnection(String connectionGroup, String partitionID, Database database)
    {
        String key = createEntryKey(connectionGroup, partitionID, database);
        map.remove(key);
    }
        
    
    public synchronized Database getDatabase(String connectionGroup, String partitionID, Database database)
    {
        String key = createEntryKey(connectionGroup, partitionID, database);
        return (Database) map.get(key);
    }
    
    public static final String createEntryKey(String connectionGroup, String partitionID, Database database)
    {
        StringBuffer key = new StringBuffer(connectionGroup);
        
        key.append(':').append(database.getDatabaseMeta().getName());
        if (!Const.isEmpty(partitionID))
        {
            key.append(':').append(partitionID);
        }

        return key.toString();
    }
}
