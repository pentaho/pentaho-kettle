/*
 * Copyright (c) 2011 Pentaho Corporation.  All rights reserved. 
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
package org.pentaho.database;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;

/**
 * Tests the value returned from org.pentaho.di.core.database.DatabaseInterface.getSelectCountStatement
 * for the database the interface is fronting.
 * 
 * As this release, Hive uses the following to select the number of rows:
 * 
 *   SELECT COUNT(1) FROM ....
 *   
 * All other databases use:
 * 
 *   SELECT COUNT(*) FROM ....
 */
public class TestSelectCount {
    
    private final static String HiveSelect = "select count(1) from ";
    private final static String TableName = "NON_EXISTANT";
    
    public static final String HiveDatabaseXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
          "<connection>" +
            "<name>Hadoop Hive</name>" +
            "<server>127.0.0.1</server>" +
            "<type>Hadoop Hive</type>" +
            "<access></access>" + 
            "<database>default</database>" +
            "<port>10000</port>" +
            "<username>sean</username>" +
            "<password>sean</password>" +
          "</connection>"; 

    @Test
    public void testHiveDatabase() throws Exception {
        try {
            KettleEnvironment.init();
            String expectedSQL = HiveSelect+TableName;
            DatabaseMeta databaseMeta = new DatabaseMeta(HiveDatabaseXML);
            String sql= databaseMeta.getDatabaseInterface().getSelectCountStatement(TableName);
            assertTrue(sql.equalsIgnoreCase(expectedSQL));
        }
        catch (Exception e ) {
                e.printStackTrace();
        }
    }

}