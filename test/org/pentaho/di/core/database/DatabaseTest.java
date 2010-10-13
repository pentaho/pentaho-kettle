package org.pentaho.di.core.database;

import junit.framework.TestCase;

import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransMeta;


/**
 * Try to test database functionality using a hypersonic database. This
 * is just a small fraction of the functionality, but could already trap a few
 * problems.
 * 
 * @author Sven Boden
 */
public class DatabaseTest extends TestCase
{
    public static final String[] databasesXML = {
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
          "<connection>" +
            "<name>db</name>" +
            "<server>127.0.0.1</server>" +
            "<type>H2</type>" +
            "<access>Native</access>" + 
            "<database>mem:db</database>" +
            "<port></port>" +
            "<username>sa</username>" +
            "<password></password>" +
          "</connection>",
    };	

	public Database setupDatabase() throws Exception
	{
		Database database = null;
		
        KettleEnvironment.init();

        try
        {
            //
            // Create a new transformation...
            //
            TransMeta transMeta = new TransMeta();
            transMeta.setName("transname");

            // Add the database connections
            for (int i=0;i<databasesXML.length;i++)
            {
                DatabaseMeta databaseMeta = new DatabaseMeta(databasesXML[i]);
                transMeta.addDatabase(databaseMeta);
            }

            DatabaseMeta dbInfo = transMeta.findDatabase("db");

            database = new Database(transMeta, dbInfo);
            database.connect();
        }
        finally { };
        
        return database;
	}
    
	public void testDatabaseCasing() throws Exception
	{
		String tableName = "mIxCaSiNG";
        Database db = setupDatabase();            

		RowMetaInterface rm = new RowMeta();
		
		ValueMetaInterface valuesMeta[] = {
			    new ValueMeta("ID",     ValueMeta.TYPE_INTEGER),
			    new ValueMeta("DLR_CD", ValueMeta.TYPE_INTEGER),
	    };

		for (int i=0; i < valuesMeta.length; i++ )
		{
			valuesMeta[i].setLength(8);
			valuesMeta[i].setPrecision(0);
			rm.addValueMeta(valuesMeta[i]);
		}

		String createStatement = db.getCreateTableStatement(tableName, rm, null, false, null, true);
	    db.execStatement(createStatement);
	    
	    // Make sure that the tablename is of mixed case
	    assertFalse(tableName.equals(tableName.toLowerCase()));
	    
	    assertTrue(db.checkTableExists(tableName));	    
	    assertEquals(false, db.checkTableExists("unknown"));
	    
	    // We're testing here whether tables names are case insensitive.
	    // If this would fail, it can either be a problem with PDI or 
	    // be a problem with a new H2 JDBC driver.
	    assertTrue(db.checkTableExists(tableName.toLowerCase()));
	    
	    db.disconnect();
	}

	public void testQuoting() throws Exception
	{
		Database database = null;
		
        KettleEnvironment.init();

        try
        {
            //
            // Create a new transformation...
            //
            TransMeta transMeta = new TransMeta();
            transMeta.setName("transname");

            // Add the database connections
            for (int i=0;i<databasesXML.length;i++)
            {
                DatabaseMeta databaseMeta = new DatabaseMeta(databasesXML[i]);
                transMeta.addDatabase(databaseMeta);
            }

            DatabaseMeta dbInfo = transMeta.findDatabase("db");

            database = new Database(transMeta, dbInfo);
            database.connect();
            
            assertNull(dbInfo.quoteField(null));
            assertEquals("table1", dbInfo.quoteField("table1"));
            assertEquals("\"table 1\"", dbInfo.quoteField("table 1"));
            assertEquals("\"table-1\"", dbInfo.quoteField("table-1"));
            assertEquals("\"table+1\"", dbInfo.quoteField("table+1"));
            assertEquals("\"table.1\"", dbInfo.quoteField("table.1"));

            assertNull(dbInfo.getQuotedSchemaTableCombination(null, null));
            assertEquals("table1", dbInfo.getQuotedSchemaTableCombination(null, "table1"));
            assertEquals("\"table 1\"", dbInfo.getQuotedSchemaTableCombination(null, "table 1"));
            assertEquals("\"table-1\"", dbInfo.getQuotedSchemaTableCombination(null, "table-1"));
            assertEquals("\"table+1\"", dbInfo.getQuotedSchemaTableCombination(null, "table+1"));
            assertEquals("\"table.1\"", dbInfo.getQuotedSchemaTableCombination(null, "table.1"));

            assertEquals("\"schema1\".\"null\"", dbInfo.getQuotedSchemaTableCombination("schema1", null));
            assertEquals("\"schema1\".\"table1\"", dbInfo.getQuotedSchemaTableCombination("schema1", "table1"));
            
            // These 2 are maybe dodgy, but current behaviour
            assertEquals("\"schema 1\".\"table 1\"", dbInfo.getQuotedSchemaTableCombination("schema 1", "table 1"));
            assertEquals("\"schema1\".\"table1\"", dbInfo.getQuotedSchemaTableCombination("schema1", "\"table1\""));
            
            database.disconnect();
        }     
        finally { }
	}		
}