/**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/

package be.ibridge.kettle.trans.step.sortrows;

import java.sql.ResultSet;
import java.util.Random;

import junit.framework.TestCase;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.database.Database;
import be.ibridge.kettle.core.database.DatabaseMeta;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.util.EnvUtil;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.trans.StepLoader;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransHopMeta;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;
import be.ibridge.kettle.trans.step.combinationlookup.CombinationLookupMeta;
import be.ibridge.kettle.trans.step.tableinput.TableInputMeta;

/**
 * Test class for sort rows step. HSQL is used
 * as database in memory to get an easy playground for database
 * tests. HSQL does not support all SQL features but it should
 * proof enough for most of our tests.
 * 
 * What will be done... a database table will be filled with rows
 * consisting of 2 keyfields (randomly generated, but unique). 
 * A database step will read the table, link it to a sort rows step,
 * linking it to a combination lookup step.
 * 
 * The combination lookup step is abused to assign a increasing number
 * to the rows. So that later on we can the rows order by that number
 * in order to check whether sort is working.
 *
 * @author Sven Boden
 */
public class SortRowsTest extends TestCase
{
    public static final String[] databasesXML = {
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<connection>" +
            "<name>db</name>" +
            "<server>127.0.0.1</server>" +
            "<type>HYPERSONIC</type>" +
            "<access>Native</access>" +
            "<database>mem:db</database>" +
            "<port></port>" +
            "<username>sa</username>" +
            "<password></password>" +
          "</connection>",
    };
    
    private static String target_table = "sort_target";
    private static String source_table = "sort_source";
    
    public static int MAX_COUNT = 15000;
    
	/**
	 * Create source and target table.
	 */
	public void createTables(Database db) throws Exception
	{		
		Row r = new Row();
		r.addValue(new Value("ID",       Value.VALUE_TYPE_INTEGER, 8, 0));
		r.addValue(new Value("KEY1",     Value.VALUE_TYPE_STRING, 40, 0));
		r.addValue(new Value("KEY2",     Value.VALUE_TYPE_STRING, 40, 0));
		
		String target = db.getCreateTableStatement(target_table, r, null, false, null, true);
		try  {
		    db.execStatement(target);
		}
		catch ( KettleException ex ) 
		{
		   // Table already existed probably	
		}		

		r = new Row();
		r.addValue(new Value("KEY1",     Value.VALUE_TYPE_STRING, 40, 0));
		r.addValue(new Value("KEY2",     Value.VALUE_TYPE_STRING, 40, 0));
		
		String source = db.getCreateTableStatement(source_table, r, null, false, null, true);
		try  {
		    db.execStatement(source);
		}
		catch ( KettleException ex ) 
		{
			   // Table already existed probably	
		}						
	}

	/**
	 * Insert data in the source table.
	 * 
	 * @param db database to use. 
	 */
	private void createData(Database db) throws Exception
	{		
		String old_key1 = null;
		
		Random rand = new Random();		
		for ( int idx = 0; idx < MAX_COUNT; idx++ )
		{
			int key1 = Math.abs(rand.nextInt() % 1000000);
			int key2 = Math.abs(rand.nextInt() % 1000000);

			String key1_string = "" + key1 + "." + idx;
			String key2_string = "" + key2 + "." + idx;
			if ( ((idx % 100) == 0) && old_key1 != null )
			{
				// have duplicate key1's sometimes
			    key1_string = old_key1;
			}
			old_key1 = key1_string;
			String statement = "INSERT INTO " + source_table + "(KEY1, KEY2) " +
                    "VALUES ('" + key1_string + "', '" + key2_string + "');";
			db.execStatement(statement);			
		}
	}

	/**
	 * Check the results in the target dimension table.
	 * 
	 * @param db database to use.
	 */
	public void checkResults(Database db) throws Exception
	{
		String query = "SELECT ID, KEY1, KEY2 FROM " +
		               target_table + " ORDER BY ID;";
		
		String prev_key1 = null, prev_key2 = null;
		ResultSet rs = db.openQuery(query);
		int idx = 0;
		while (rs.next())
		{
		   String key1 = rs.getString("KEY1");
		   String key2 = rs.getString("KEY2");
		   
		   if (prev_key1 != null && prev_key2 != null)
		   {
			   if (prev_key1.compareTo(key1) == 0) 
			   {
				   if ( prev_key2.compareTo(key2) > 0 )
				   {
					   fail("error in sort");
				   }
			   }
			   else if (prev_key1.compareTo(key1) > 0)
			   {
				   fail("error in sort");
			   }
		   }
		   prev_key1 = key1;
		   prev_key2 = key2;

		   idx++;
  	    }
  	    if (idx != MAX_COUNT)
	    {
	       fail("less rows returned than expected" + idx);
	    }
	}
	
	/**
	 * Test case for Combination lookup/update.
	 */
    public void testSortRows() throws Exception
    {
        LogWriter log = LogWriter.getInstance();
        EnvUtil.environmentInit();
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
            
            // Execute our setup SQLs in the database.
            Database database = new Database(dbInfo);
            database.connect();
            createTables(database);
            createData(database);
            
            StepLoader steploader = StepLoader.getInstance();            

            // 
            // create the source step...
            //
            String fromstepname = "read from [" + source_table + "]";
            TableInputMeta tii = new TableInputMeta();
            tii.setDatabaseMeta(transMeta.findDatabase("db"));
            String selectSQL = "SELECT "+Const.CR;
            selectSQL+="KEY1, KEY2 ";
            selectSQL+="FROM " + source_table + ";";
            tii.setSQL(selectSQL);

            String fromstepid = steploader.getStepPluginID(tii);
            StepMeta fromstep = new StepMeta(log, fromstepid, fromstepname, (StepMetaInterface) tii);
            fromstep.setLocation(150, 100);
            fromstep.setDraw(true);
            fromstep.setDescription("Reads information from table [" + source_table + "] on database [" + dbInfo + "]");
            transMeta.addStep(fromstep);

            // 
            // Sort step
            //
            String sortstepname = "sort step";
            SortRowsMeta srm = new SortRowsMeta();            
            srm.setSortSize(MAX_COUNT/10);
            String [] sortFields = { "KEY1", "KEY2" };
            boolean [] ascendingFields = { true, true };
            srm.setFieldName(sortFields);
            srm.setAscending(ascendingFields);
            srm.setPrefix("SortRowsTest");
            srm.setDirectory(".");

            String sortstepid = steploader.getStepPluginID(srm);
            StepMeta sortstep = new StepMeta(log, sortstepid, sortstepname, (StepMetaInterface) srm);
            sortstep.setLocation(250, 100);
            sortstep.setDraw(true);
            sortstep.setDescription("sort the table");
            transMeta.addStep(sortstep);

            // 
            // create the combination lookup/update step... which we abuse to
            // assign an ascending number.
            //
            String savestepname = "save step";            
            CombinationLookupMeta clm = new CombinationLookupMeta();
            String lookupKey[] = { "KEY1", "KEY2" };
            clm.setTablename(target_table);
            clm.setKeyField(lookupKey);
            clm.setKeyLookup(lookupKey);
            clm.setTechnicalKeyField("ID");
            clm.setTechKeyCreation(CombinationLookupMeta.CREATION_METHOD_TABLEMAX);
            clm.setCacheSize(10);
            sortstep.setLocation(350, 100);
            clm.setDatabase(dbInfo);

            String savestepid = steploader.getStepPluginID(clm);
            StepMeta savestep = new StepMeta(log, savestepid, savestepname, (StepMetaInterface) clm);
            savestep.setDescription("saves information in database [" + dbInfo + "]");
            transMeta.addStep(savestep);                              

            TransHopMeta hi = new TransHopMeta(fromstep, sortstep);
            transMeta.addTransHop(hi);
            hi = new TransHopMeta(sortstep, savestep);
            transMeta.addTransHop(hi);
            
            // Now execute the transformation...
            Trans trans = new Trans(log, transMeta);
            trans.execute(null);            
                        
            trans.waitUntilFinished();            
            
            checkResults(database);
        }    	
        finally {}
    }    
}
