package org.pentaho.di.run.delete;

import junit.framework.TestCase;

import org.pentaho.di.core.Result;
import org.pentaho.di.run.AllRunTests;
import org.pentaho.di.run.TimedTransRunner;

import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleXMLException;

public class RunDelete extends TestCase
{
    private static void createIndex() throws KettleException
    {
        AllRunTests.executeStatementsOnOldAndNew(
                "DROP INDEX idx_CSV_TABLE_lookup;", 
                "CREATE INDEX idx_CSV_TABLE_lookup ON CSV_TABLE(id);"
                );
    }

    public void test__DELETE_00() throws KettleException
    {
        System.out.println();
        System.out.println("DELETE");
        System.out.println("==================");
        System.out.println();
        createIndex();
    }
    
    public void test__DELETE_01_SimpleTest() throws KettleXMLException, KettleDatabaseException
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "experimental_test/org/pentaho/di/run/delete/DeleteRowsSimple.ktr", 
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
