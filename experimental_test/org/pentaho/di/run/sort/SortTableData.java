package org.pentaho.di.run.sort;

import junit.framework.TestCase;

import org.pentaho.di.run.TimedTransRunner;

import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Result;
import be.ibridge.kettle.core.exception.KettleXMLException;

public class SortTableData extends TestCase
{
    public void testSortTableData() throws KettleXMLException
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
    
    public void testSortTableDataNoCompression() throws KettleXMLException
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
    
    public void testSortTableDataFromMySQL() throws KettleXMLException
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "experimental_test/org/pentaho/di/run/sort/SortTableDataMySQL.ktr",
                LogWriter.LOG_LEVEL_ERROR, 
                100000
            );
        timedTransRunner.runOldAndNew();
        
        Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==0);
        
        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
    }
    
    public void testSortTableDataNoCompressionFromMySQL() throws KettleXMLException
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "experimental_test/org/pentaho/di/run/sort/SortTableDataNoCompressionMySQL.ktr",
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
