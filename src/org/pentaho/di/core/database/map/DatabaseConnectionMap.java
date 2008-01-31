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
package org.pentaho.di.core.database.map;

import java.util.Hashtable;
import java.util.Map;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.Database;

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
    private Map<String,Database> map;
    
    private static DatabaseConnectionMap connectionMap;
    
    public synchronized static final DatabaseConnectionMap getInstance()
    {
        if (connectionMap!=null) return connectionMap;
        connectionMap = new DatabaseConnectionMap();
        return connectionMap;
    }
    
    private DatabaseConnectionMap()
    {
        map = new Hashtable<String,Database>();
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
        return map.get(key);
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
    
    public Map<String, Database> getMap() {
		return map;
	}
}
