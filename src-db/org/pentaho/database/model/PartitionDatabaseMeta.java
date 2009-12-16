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
package org.pentaho.database.model;

import java.io.Serializable;

/**
 * Class to contain the information needed to parition (cluster): id, hostname, port, database
 * 
 * @author Matt
 *
 */
public class PartitionDatabaseMeta implements Serializable {

  private static final long serialVersionUID = -4252906914407231458L;

    String partitionId;
    
    String hostname;
    String port;
    String databaseName;
    String username;
    String password;
    
    public PartitionDatabaseMeta()
    {
    }
    
    /**
     * @param partitionId
     * @param hostname
     * @param port
     * @param database
     */
    public PartitionDatabaseMeta(String partitionId, String hostname, String port, String database)
    {
        super();
        
        this.partitionId = partitionId;
        this.hostname = hostname;
        this.port = port;
        this.databaseName = database;
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
     * @return the database
     */
    public String getDatabaseName()
    {
        return databaseName;
    }
    
    /**
     * @param database the database to set
     */
    public void setDatabaseName(String database)
    {
        this.databaseName = database;
    }
    
    /**
     * @return the hostname
     */
    public String getHostname()
    {
        return hostname;
    }
    
    /**
     * @param hostname the hostname to set
     */
    public void setHostname(String hostname)
    {
        this.hostname = hostname;
    }
    
    /**
     * @return the port
     */
    public String getPort()
    {
        return port;
    }
    
    /**
     * @param port the port to set
     */
    public void setPort(String port)
    {
        this.port = port;
    }

    /**
     * @return the password
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password)
    {
        this.password = password;
    }

    /**
     * @return the username
     */
    public String getUsername()
    {
        return username;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username)
    {
        this.username = username;
    }
    
    
}
