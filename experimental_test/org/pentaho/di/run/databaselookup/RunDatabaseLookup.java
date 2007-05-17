package org.pentaho.di.run.databaselookup;

import junit.framework.TestCase;

import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.run.TimedTransRunner;

import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Result;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.core.util.EnvUtil;

public class RunDatabaseLookup extends TestCase
{
    private static void createIndex() throws KettleDatabaseException
    {
        EnvUtil.environmentInit();
        
        DatabaseMeta h2meta = new DatabaseMeta("H2 local", "H2", "JDBC", null, "experimental_test/testdata", null, null, null);
        Database h2db = new Database(h2meta);
        h2db.connect();
        try
        {
            h2db.execStatement("DROP INDEX IDX_CSV_TABLE_ID;");
            System.out.println("Index INDEX IDX_CSV_TABLE_ID dropped");
        }
        catch(KettleDatabaseException e)
        {
            System.out.println("INDEX IDX_CSV_TABLE_ID not dropped: "+e.getMessage());
        }
        System.out.println("Creating index INDEX IDX_CSV_TABLE_ID ...");
        h2db.execStatement(
                "CREATE INDEX IDX_CSV_TABLE_ID ON CSV_TABLE(id);");
        System.out.println("Index INDEX IDX_CSV_TABLE_ID created.");

        h2db.disconnect();
    }

    private static void dropIndex() throws KettleDatabaseException
    {
        EnvUtil.environmentInit();
        
        DatabaseMeta h2meta = new DatabaseMeta("H2 local", "H2", "JDBC", null, "experimental_test/testdata", null, null, null);
        Database h2db = new Database(h2meta);
        h2db.connect();
        try
        {
            h2db.execStatement("DROP INDEX IDX_CSV_TABLE_ID;");
            System.out.println("Index INDEX IDX_CSV_TABLE_ID dropped");
        }
        catch(KettleDatabaseException e)
        {
            System.out.println("INDEX IDX_CSV_TABLE_ID not dropped: "+e.getMessage());
        }

        h2db.disconnect();
    }

    public void test__DATABASE_LOOKUP_00() throws KettleDatabaseException
    {
        System.out.println();
        System.out.println("DATABASE LOOKUP");
        System.out.println("==================");
        System.out.println();
        createIndex();
    }
    
    public void test__DATABASE_LOOKUP_01_Simple() throws KettleXMLException
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "experimental_test/org/pentaho/di/run/databaselookup/DBLookupIDLookup.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                1000000
            );
        timedTransRunner.runOldAndNew();
        
        Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==0);
        
        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
    }
    
    public void test__DATABASE_LOOKUP_02_Simple() throws KettleXMLException
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "experimental_test/org/pentaho/di/run/databaselookup/DBLookupIDLookupNoCaching.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                1000000
            );
        timedTransRunner.runOldAndNew();
        
        Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==0);
        
        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
    }
    
    public void test__DATABASE_LOOKUP_99() throws KettleDatabaseException
    {
        System.out.println();
        dropIndex();
    }
}
