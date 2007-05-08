package org.pentaho.pdi.core.row;

import java.sql.ResultSet;

import org.pentaho.pdi.core.database.Database;
import org.pentaho.pdi.core.database.DatabaseMeta;

import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.util.EnvUtil;

public class DBSpeedTest
{
    public static final int FEEDBACK_SIZE = 10000000;
    public DBSpeedTest()
    {
    }
    
    public void testNewCode() throws KettleDatabaseException
    {
        DatabaseMeta dbMeta = new DatabaseMeta("MySQL test", "MYSQL", "JDBC", "localhost", "test", "3306", "matt", "abcd");
        Database database = new Database(dbMeta);
        database.connect();

        // Read the whole table..
        long startTime = System.currentTimeMillis();
            
        ResultSet resultSet = database.openQuery("SELECT * FROM customer_speed");
        Object[] data = database.getRow(resultSet);
        int nr=0;
        while(data!=null)
        {
            nr++;
            data = database.getRow(resultSet);
            if ((nr%FEEDBACK_SIZE)==0) System.out.println("Read "+nr+" lines.");
        }
        if ((nr%FEEDBACK_SIZE)!=0) System.out.println("Read "+nr+" lines.");

        long endTime = System.currentTimeMillis();

        database.disconnect();
        
        long lapsed = endTime - startTime;
        System.out.println("NEW CODE: time lapsed : "+lapsed+" ms");

    }
    
    public void testOldCode() throws KettleDatabaseException
    {
        be.ibridge.kettle.core.database.DatabaseMeta dbMeta = new be.ibridge.kettle.core.database.DatabaseMeta("MySQL test", "MYSQL", "JDBC", "localhost", "test", "3306", "matt", "abcd");
        be.ibridge.kettle.core.database.Database database = new be.ibridge.kettle.core.database.Database(dbMeta);
        database.connect();

        // Read the whole table..
        long startTime = System.currentTimeMillis();
            
        ResultSet resultSet = database.openQuery("SELECT * FROM customer_speed");
        Row data = database.getRow(resultSet);
        int nr=0;
        while(data!=null)
        {
            nr++;
            data = database.getRow(resultSet);
            if ((nr%FEEDBACK_SIZE)==0) System.out.println("Read "+nr+" lines.");
        }
        if ((nr%FEEDBACK_SIZE)!=0) System.out.println("Read "+nr+" lines.");

        long endTime = System.currentTimeMillis();

        database.disconnect();
        
        long lapsed = endTime - startTime;
        System.out.println("OLD CODE: time lapsed : "+lapsed+" ms");
    }
    
    public static void main(String[] args) throws KettleDatabaseException
    {
        EnvUtil.environmentInit();
        
        DBSpeedTest dbSpeedTest = new DBSpeedTest();
        
        for (int i=0;i<5;i++)
        {
            dbSpeedTest.testNewCode();
            dbSpeedTest.testOldCode();
        }
    }
}
