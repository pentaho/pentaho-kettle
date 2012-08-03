/*******************************************************************************
 *
 * Pentaho Big Data
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