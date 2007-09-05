package org.pentaho.di.run.insertupdate;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.run.AllRunTests;
import org.pentaho.di.run.RunTestCase;
import org.pentaho.di.run.TimedTransRunner;

public class RunInsertUpdate extends RunTestCase
{
    private static void createTableAndIndex() throws Exception
    {
        AllRunTests.executeStatementsOnOldAndNew(
                "DROP TABLE IU_CSV;",                
                "CREATE TABLE IU_CSV"+Const.CR+
                "("+Const.CR+
                "  id INTEGER"+Const.CR+
                ", name VARCHAR(30)"+Const.CR+
                ", firstname VARCHAR(30)"+Const.CR+
                ", zip INTEGER"+Const.CR+
                ", city VARCHAR(30)"+Const.CR+
                ", birthdate TIMESTAMP"+Const.CR+
                ", street VARCHAR(30)"+Const.CR+
                ", housenr SMALLINT"+Const.CR+
                ", stateCode VARCHAR(20)"+Const.CR+
                ", state VARCHAR(35)"+Const.CR+
                ")"+Const.CR+
                ";"+Const.CR+                
                "CREATE UNIQUE INDEX idx_IU_CSV_lookup"+Const.CR+
                " ON IU_CSV"+Const.CR+
                "( "+Const.CR+
                "  id"+Const.CR+
                ")"+Const.CR+
                ";"+Const.CR
            );
    }

    public void test__INSERT_UPDATE_00() throws Exception
    {
        System.out.println();
        System.out.println("INSERT UPDATE");
        System.out.println("==================");
        System.out.println();
        createTableAndIndex();
    }
    
    public void test__INSERT_UPDATE_01_SimpleTest() throws Exception
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "test/org/pentaho/di/run/insertupdate/InsertUpdateSimple.ktr", 
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
