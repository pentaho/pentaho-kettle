package org.pentaho.di.run.sort;

import junit.framework.TestCase;

import org.pentaho.di.run.TimedTransRunner;

import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Result;
import be.ibridge.kettle.core.exception.KettleXMLException;

public class SortTableData extends TestCase
{
    public void test_SORT_00()
    {
        System.out.println();
        System.out.println("SORT ROWS");
        System.out.println("==================");
    }
    
    public void test_SORT_01_TableData() throws KettleXMLException
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "experimental_test/org/pentaho/di/run/sort/SortTableData.ktr",
                LogWriter.LOG_LEVEL_ERROR, 
                100000
            );
        timedTransRunner.runOldAndNew();
        
        Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==0);
        
        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
    }
    
    public void test_SORT_02_TableDataNoCompression() throws KettleXMLException
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "experimental_test/org/pentaho/di/run/sort/SortTableDataNoCompression.ktr",
                LogWriter.LOG_LEVEL_ERROR, 
                100000
            );
        timedTransRunner.runOldAndNew();
        
        Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==0);
        
        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
    }
    
    public void test_SORT_03_TableDataNoCompressionBuffer50k() throws KettleXMLException
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "experimental_test/org/pentaho/di/run/sort/SortTableDataNoCompressionBuffer50k.ktr",
                LogWriter.LOG_LEVEL_ERROR, 
                100000
            );
        timedTransRunner.runOldAndNew();
        
        Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==0);
        
        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
    }
    
    public void test_SORT_04_TableDataNoCompressionBuffer100k() throws KettleXMLException
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "experimental_test/org/pentaho/di/run/sort/SortTableDataNoCompressionBuffer100k.ktr",
                LogWriter.LOG_LEVEL_ERROR, 
                100000
            );
        timedTransRunner.runOldAndNew();
        
        Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==0);
        
        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
    }

}
