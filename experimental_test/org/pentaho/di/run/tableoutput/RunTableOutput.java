package org.pentaho.di.run.tableoutput;

import junit.framework.TestCase;

import org.pentaho.di.core.Result;
import org.pentaho.di.run.AllRunTests;
import org.pentaho.di.run.TimedTransRunner;

import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleXMLException;

public class RunTableOutput extends TestCase
{
    private static void createTables() throws KettleException
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

    public void test__TABLE_OUTPUT_00() throws KettleException
    {
        System.out.println();
        System.out.println("TABLE OUTPUT");
        System.out.println("==================");
        System.out.println();
        createTables();
    }
    
    public void test__TABLE_OUTPUT_01_Simple() throws KettleXMLException
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "experimental_test/org/pentaho/di/run/tableoutput/TableOutputSimple.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                AllRunTests.getOldTargetDatabase(),
                AllRunTests.getNewTargetDatabase(),
                100000
            );
        timedTransRunner.runOldAndNew();
        
        be.ibridge.kettle.core.Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==0);
        
        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
    }
    
    public void test__TABLE_OUTPUT_02_FromCSV() throws KettleXMLException
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "experimental_test/org/pentaho/di/run/tableoutput/TableOutputFromCSV.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                AllRunTests.getOldTargetDatabase(),
                AllRunTests.getNewTargetDatabase(),
                100000
            );
        timedTransRunner.runOldAndNew();
        
        be.ibridge.kettle.core.Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==0);
        
        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
    }
    

}
