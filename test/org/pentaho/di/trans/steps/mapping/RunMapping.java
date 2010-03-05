package org.pentaho.di.trans.steps.mapping;

import junit.framework.TestCase;

import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.LogWriter;
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
                LogWriter.LOG_LEVEL_ERROR, 
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
                LogWriter.LOG_LEVEL_ERROR, 
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
                LogWriter.LOG_LEVEL_ERROR, 
                getTargetDatabase(),
                1000
            );
        assertTrue( timedTransRunner.runEngine(true) );
        
        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
    }
}
