package org.pentaho.di.core.row;

import java.sql.ResultSet;

import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;

import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.util.EnvUtil;

import be.ibridge.kettle.core.Row;

public class DBSpeedTest
{
    public static final int FEEDBACK_SIZE = 10000000;
    public static final String SQL = "SELECT * FROM customer_speed";
    public DBSpeedTest()
    {
    }
    
    public void testNewCode() throws KettleDatabaseException
    {
        DatabaseMeta dbMeta = new DatabaseMeta("MySQL test", "MYSQL", "JDBC", "192.168.1.11", "test", "3306", "root", "abcd");
        Database database = new Database(dbMeta);
        database.connect();

        // Read the whole table..
        long startTime = System.currentTimeMillis();
            
        ResultSet resultSet = database.openQuery(SQL);
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
    
    public void testOldCode() throws be.ibridge.kettle.core.exception.KettleDatabaseException
    {
        be.ibridge.kettle.core.database.DatabaseMeta dbMeta = new be.ibridge.kettle.core.database.DatabaseMeta("MySQL test", "MYSQL", "JDBC", "localhost", "test", "3306", "matt", "abcd");
        be.ibridge.kettle.core.database.Database database = new be.ibridge.kettle.core.database.Database(dbMeta);
        database.connect();

        // Read the whole table..
        long startTime = System.currentTimeMillis();
            
        ResultSet resultSet = database.openQuery(SQL);
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
    
    public static void main(String[] args) throws KettleDatabaseException, be.ibridge.kettle.core.exception.KettleDatabaseException 
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
