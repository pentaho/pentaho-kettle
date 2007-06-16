package org.pentaho.di.run.insertupdate;

import junit.framework.TestCase;

import org.pentaho.di.core.Result;
import org.pentaho.di.run.AllRunTests;
import org.pentaho.di.run.TimedTransRunner;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleXMLException;

public class RunInsertUpdate extends TestCase
{
    private static void createTableAndIndex() throws KettleException
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

    public void test__INSERT_UPDATE_00() throws KettleException
    {
        System.out.println();
        System.out.println("INSERT UPDATE");
        System.out.println("==================");
        System.out.println();
        createTableAndIndex();
    }
    
    public void test__INSERT_UPDATE_01_SimpleTest() throws KettleXMLException, KettleDatabaseException
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "experimental_test/org/pentaho/di/run/insertupdate/InsertUpdateSimple.ktr", 
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
