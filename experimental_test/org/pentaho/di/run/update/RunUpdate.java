package org.pentaho.di.run.update;

import junit.framework.TestCase;

import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.run.TimedTransRunner;

import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Result;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.core.util.EnvUtil;

public class RunUpdate extends TestCase
{
    private static DatabaseMeta h2meta = new DatabaseMeta("H2 local", "H2", "JDBC", null, "experimental_test/testdata", null, null, null);

    private static void createIndex() throws KettleDatabaseException
    {
        EnvUtil.environmentInit();
        
        Database h2db = new Database(h2meta);
        h2db.connect();
        try
        {
            h2db.execStatement("DROP INDEX idx_CSV_TABLE_lookup;");
        }
        catch(KettleDatabaseException e)
        {
            // System.out.println("Table DIM_CUSTOMER not dropped : "+e.getMessage());
        }
        // System.out.println("Creating table DIM_CUSTOMER ...");
        
        h2db.execStatement("CREATE INDEX idx_CSV_TABLE_lookup ON CSV_TABLE(id);");

        h2db.disconnect();
    }

    public void test__DIMENSION_LOOKUP_00() throws KettleDatabaseException
    {
        System.out.println();
        System.out.println("UPDATE");
        System.out.println("==================");
        System.out.println();
        createIndex();
    }
    
    public void test__UPDATE_01_SimpleTest() throws KettleXMLException, KettleDatabaseException
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "experimental_test/org/pentaho/di/run/update/UpdateRowsSimple.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                100000
            );
        timedTransRunner.runOldAndNew();

        Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==0);
        
        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
    }
    
    public void test__UPDATE_02_SimpleTestWithUpdates() throws KettleXMLException, KettleDatabaseException
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "experimental_test/org/pentaho/di/run/update/UpdateRowsSimpleWithUpdates.ktr", 
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
