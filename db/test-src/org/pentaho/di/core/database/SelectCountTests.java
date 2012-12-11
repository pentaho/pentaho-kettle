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

import static org.junit.Assert.*;

import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;

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
public class SelectCountTests {
    
    /** 
     * 
     */
    private final static String NonHiveSelect = "select count(*) from ";
    private final static String TableName = "NON_EXISTANT";
    
    public static final String h2DatabaseXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
          "<connection>" +
            "<name>H2</name>" +
            "<server>127.0.0.1</server>" +
            "<type>H2</type>" +
            "<access>Native</access>" + 
            "<database>mem:db</database>" +
            "<port></port>" +
            "<username>sa</username>" +
            "<password></password>" +
          "</connection>";
          
    public static final String OracleDatabaseXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
          "<connection>" +
            "<name>Oracle</name>" +
            "<server>127.0.0.1</server>" +
            "<type>Oracle</type>" +
            "<access>Native</access>" + 
            "<database>test</database>" +
            "<port>1024</port>" +
            "<username>scott</username>" +
            "<password>tiger</password>" +
          "</connection>";
          
    public static final String MySQLDatabaseXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
          "<connection>" +
            "<name>MySQL</name>" +
            "<server>127.0.0.1</server>" +
            "<type>MySQL</type>" +
            "<access></access>" + 
            "<database>test</database>" +
            "<port>3306</port>" +
            "<username>sa</username>" +
            "<password></password>" +
          "</connection>";

    @Test
    public void testH2Database() {
        try {
            KettleEnvironment.init();
            String expectedSQL = NonHiveSelect+TableName;
            DatabaseMeta databaseMeta = new DatabaseMeta(h2DatabaseXML);
            String sql= databaseMeta.getDatabaseInterface().getSelectCountStatement(TableName);
            assertTrue(sql.equalsIgnoreCase(expectedSQL));
        }
        catch (Exception e ) {
            e.printStackTrace();
        }
    }

    @Test
    public void testOracleDatabase() {
        try {
            KettleEnvironment.init();
            String expectedSQL = NonHiveSelect+TableName;
            DatabaseMeta databaseMeta = new DatabaseMeta(OracleDatabaseXML);
            String sql= databaseMeta.getDatabaseInterface().getSelectCountStatement(TableName);
            assertTrue(sql.equalsIgnoreCase(expectedSQL));
        }
        catch (Exception e ) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testMySQLDatabase() {
        try {
            KettleEnvironment.init();
            String expectedSQL = NonHiveSelect+TableName;
            DatabaseMeta databaseMeta = new DatabaseMeta(MySQLDatabaseXML);
            String sql= databaseMeta.getDatabaseInterface().getSelectCountStatement(TableName);
            assertTrue(sql.equalsIgnoreCase(expectedSQL));
            }
            catch (Exception e ) {
                e.printStackTrace();
        }
    }
}