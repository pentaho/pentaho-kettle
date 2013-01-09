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

package org.pentaho.di.trans.steps.mapping;

import junit.framework.TestCase;

import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.run.TimedTransRunner;

public class RunMapping extends TestCase
{
    private static String TARGET_CONNECTION_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
	    "<connection>"+
	    "  <name>TARGET</name>"+
	    "  <server/>"+
	    "  <type>GENERIC</type>"+
	    "  <access>Native</access>"+
	    "  <database>&#47;test</database>"+
	    "  <port/>"+
	    "  <username/>"+
	    "  <password>Encrypted </password>"+
	    "  <servername/>"+
	    "  <data_tablespace/>"+
	    "  <index_tablespace/>"+
	    "  <attributes>"+
	    "    <attribute><code>CUSTOM_DRIVER_CLASS</code><attribute>org.apache.derby.jdbc.EmbeddedDriver</attribute></attribute>"+
	    "    <attribute><code>CUSTOM_URL</code><attribute>jdbc:derby:test&#47;derbyNew;create=true</attribute></attribute>"+
	    "  </attributes>"+
	    "</connection>";
    
    public static DatabaseMeta getTargetDatabase() throws KettleXMLException
    {
        return new DatabaseMeta(TARGET_CONNECTION_XML);
    }
    
    public void test_MAPPING_INPUT_ONLY() throws Exception
    {
    	KettleEnvironment.init();
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "test/org/pentaho/di/trans/steps/mapping/filereader/use filereader.ktr", 
                LogLevel.ERROR, 
                getTargetDatabase(),
                1000
            );
        assertTrue( timedTransRunner.runEngine(true) );
        
        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
    }
    
    public void test_MAPPING_OUTPUT_ONLY() throws Exception
    {
    	KettleEnvironment.init();
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "test/org/pentaho/di/trans/steps/mapping/filewriter/use filewriter.ktr", 
                LogLevel.ERROR, 
                getTargetDatabase(),
                1000
            );
        assertTrue( timedTransRunner.runEngine(true) );
        
        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
    }
    
    public void test_MAPPING_MULTI_OUTPUT() throws Exception
    {
    	KettleEnvironment.init();
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "test/org/pentaho/di/trans/steps/mapping/multi_output/use filereader.ktr", 
                LogLevel.ERROR, 
                getTargetDatabase(),
                1000
            );
        assertTrue( timedTransRunner.runEngine(true) );
        
        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
    }
}
