package org.pentaho.di.run.tableoutput;

import org.pentaho.di.core.Result;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.run.AllRunTests;
import org.pentaho.di.run.RunTestCase;
import org.pentaho.di.run.TimedTransRunner;

public class RunTableOutput extends RunTestCase
{
    private static void createTables() throws Exception
    {
        AllRunTests.executeStatementsOnOldAndNew(
                "DROP TABLE CSV_TABLE", 
                "CREATE TABLE CSV_TABLE"+
                "("+
                "  id INTEGER"+
                ", name VARCHAR(40)"+
                ", firstname VARCHAR(40)"+
                ", zip INTEGER"+
                ", city VARCHAR(20)"+
                ", birthdate TIMESTAMP"+
                ", street VARCHAR(20)"+
                ", housenr SMALLINT"+
                ", stateCode VARCHAR(20)"+
                ", state VARCHAR(40)"+
                ")"+
                ";"
            );

        AllRunTests.executeStatementsOnOldAndNew(
                "DROP TABLE SIMPLE_TABLE;",
                "CREATE TABLE SIMPLE_TABLE"+
                "("+
                "  stringField VARCHAR(30)"+
                ", dateField TIMESTAMP"+
                ", boolField CHAR(1)"+
                ", numField DOUBLE"+
                ", intField DOUBLE"+
                ", id INTEGER"+
                ")"+
                ";"
            );
    }

    public void test__TABLE_OUTPUT_00() throws Exception
    {
        System.out.println();
        System.out.println("TABLE OUTPUT");
        System.out.println("==================");
        System.out.println();
        createTables();
    }
    
    public void test__TABLE_OUTPUT_01_Simple() throws Exception
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "test/org/pentaho/di/run/tableoutput/TableOutputSimple.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                AllRunTests.getOldTargetDatabase(),
                AllRunTests.getNewTargetDatabase(),
                rowCount*1000
            );
        assertTrue( timedTransRunner.runOldAndNew() );
        
        be.ibridge.kettle.core.Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==0);
        
        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
    }
    
    public void test__TABLE_OUTPUT_02_FromCSV() throws Exception
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "test/org/pentaho/di/run/tableoutput/TableOutputFromCSV.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                AllRunTests.getOldTargetDatabase(),
                AllRunTests.getNewTargetDatabase(),
                rowCount*1000
            );
        assertTrue( timedTransRunner.runOldAndNew() );
        
        be.ibridge.kettle.core.Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==0);
        
        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
    }
    

}
