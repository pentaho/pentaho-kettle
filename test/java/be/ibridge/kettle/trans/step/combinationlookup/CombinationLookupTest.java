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

package be.ibridge.kettle.trans.step.combinationlookup;

import java.sql.ResultSet;

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
import be.ibridge.kettle.trans.step.tableinput.TableInputMeta;

/**
 * Test class for combination lookup/update. HSQL is used
 * as database in memory to get an easy playground for database
 * tests. HSQL does not support all SQL features but it should
 * proof enough for most of our tests.
 *
 * @author Sven Boden
 */
public class CombinationLookupTest extends TestCase
{
    public static final String[] databasesXML = {
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<connection>" +
            "<name>lookup</name>" +
            "<server>127.0.0.1</server>" +
            "<type>HYPERSONIC</type>" +
            "<access>Native</access>" +
            "<database>mem:db</database>" +
            "<port></port>" +
            "<username>sa</username>" +
            "<password></password>" +
          "</connection>",
    };
    
    private static String target_table = "type1_dim";
    private static String source_table = "source";
    
    private static String insertStatement[] = 
    {
    	// New rows for the target
        "INSERT INTO " + source_table + "(ORDNO, DLR_CD, DLR_NM, DLR_DESC)" +
        "VALUES (1, 'BE010001', 'Frijters', 'Frijters NV');",
        "INSERT INTO " + source_table + "(ORDNO, DLR_CD, DLR_NM, DLR_DESC)" +
        "VALUES (2, 'BE010002', 'Sebrechts', 'Sebrechts NV');",
        "INSERT INTO " + source_table + "(ORDNO, DLR_CD, DLR_NM, DLR_DESC)" +
        "VALUES (3, 'DE010003', 'Gelden', 'Gelden Distribution Center');",
                
        // Existing business key
        "INSERT INTO " + source_table + "(ORDNO, DLR_CD, DLR_NM, DLR_DESC)" +
        "VALUES (4, 'BE010001', 'Frijters', 'Frijters BVBA');",
        
        // New row again
        "INSERT INTO " + source_table + "(ORDNO, DLR_CD, DLR_NM, DLR_DESC)" +
        "VALUES (5, 'DE010004', 'Germania', 'German Distribution Center');"               
    };
    
	/**
	 * Create source and target table.
	 */
	public void createTables(Database db) throws Exception
	{		
		Row r = new Row();
		r.addValue(new Value("ID",       Value.VALUE_TYPE_INTEGER, 8, 0));
		r.addValue(new Value("DLR_CD",   Value.VALUE_TYPE_STRING,  8, 0));
		r.addValue(new Value("DLR_NM",   Value.VALUE_TYPE_STRING, 30, 0));
		r.addValue(new Value("DLR_DESC", Value.VALUE_TYPE_STRING, 30, 0));
		
		String target = db.getCreateTableStatement(target_table, r, null, false, null, true);
		try  {
		    db.execStatement(target);
		}
		catch ( KettleException ex ) 
		{
		   fail("failure while creating table " + target_table + ": " + ex.getMessage());	
		}		

		r = new Row();
		r.addValue(new Value("ORDNO",    Value.VALUE_TYPE_INTEGER, 8, 0));		
		r.addValue(new Value("DLR_CD",   Value.VALUE_TYPE_STRING,  8, 0));
		r.addValue(new Value("DLR_NM",   Value.VALUE_TYPE_STRING, 30, 0));
		r.addValue(new Value("DLR_DESC", Value.VALUE_TYPE_STRING, 30, 0));
		
		String source = db.getCreateTableStatement(source_table, r, null, false, null, true);
		try  {
		    db.execStatement(source);
		}
		catch ( KettleException ex ) 
		{
			fail("failure while creating table " + source_table + ": " + ex.getMessage());	
		}						
	}

	/**
	 * Insert data in the source table.
	 * 
	 * @param db database to use. 
	 */
	private void createData(Database db) throws Exception
	{		
		for ( int idx = 0; idx < insertStatement.length; idx++ )
		{
		    db.execStatement(insertStatement[idx]);
		}
	}

	/**
	 * Check the results in the target dimension table.
	 * 
	 * @param db database to use.
	 */
	public void checkResults(Database db) throws Exception
	{
		String query = "SELECT ID, DLR_CD, DLR_NM, DLR_DESC FROM " +
		               target_table + " ORDER BY ID";
		
		String correctResults[] =  {
		    "1|BE010001|null|null",
		    "2|BE010002|null|null",
		    "3|DE010003|null|null",
		    "4|DE010004|null|null",
		};
		
		ResultSet rs = db.openQuery(query);
		int idx = 0;
		while (rs.next() )
		{
		   int id = rs.getInt("ID");
		   String dlr_cd = rs.getString("DLR_CD");
		   String dlr_nm = rs.getString("DLR_NM");
		   String dlr_desc = rs.getString("DLR_DESC");
		   String result = id + "|" + dlr_cd + "|" + dlr_nm + "|" + dlr_desc;
		   if ( idx > correctResults.length)
		   {
		       fail("more rows returned than expected");
		   }
		   if ( ! result.equals(correctResults[idx]))  {
			   fail("row " + (idx + 1) + " is different than expected");
		   }
		   idx++;
  	    }
  	    if ( idx < correctResults.length)
	    {
	       fail("less rows returned than expected");
	    }
	}
	
	/**
	 * Test case for Combination lookup/update.
	 */
    public void testCombinationLookup() throws Exception
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
            
            DatabaseMeta lookupDBInfo = transMeta.findDatabase("lookup");
            
            // Execute our setup SQLs in the database.
            Database lookupDatabase = new Database(lookupDBInfo);
            lookupDatabase.connect();
            createTables(lookupDatabase);
            createData(lookupDatabase);
            
            StepLoader steploader = StepLoader.getInstance();            

            // 
            // create the source step...
            //
            String fromstepname = "read from [" + source_table + "]";
            TableInputMeta tii = new TableInputMeta();
            tii.setDatabaseMeta(transMeta.findDatabase("lookup"));
            String selectSQL = "SELECT "+Const.CR;
            selectSQL+="DLR_CD, DLR_NM, DLR_DESC ";
            selectSQL+="FROM " + source_table + " ORDER BY ORDNO;";
            tii.setSQL(selectSQL);

            String fromstepid = steploader.getStepPluginID(tii);
            StepMeta fromstep = new StepMeta(log, fromstepid, fromstepname, (StepMetaInterface) tii);
            fromstep.setLocation(150, 100);
            fromstep.setDraw(true);
            fromstep.setDescription("Reads information from table [" + source_table + "] on database [" + lookupDBInfo + "]");
            transMeta.addStep(fromstep);
                        
            // 
            // create the combination lookup/update step...
            //
            String lookupstepname = "lookup from [lookup]";
            CombinationLookupMeta clm = new CombinationLookupMeta();
            String lookupKey[] = { "DLR_CD" };
            clm.setTablename(target_table);
            clm.setKeyField(lookupKey);
            clm.setKeyLookup(lookupKey);
            clm.setTechnicalKeyField("ID");
            clm.setTechKeyCreation(CombinationLookupMeta.CREATION_METHOD_TABLEMAX);
            clm.setDatabase(lookupDBInfo);

            String lookupstepid = steploader.getStepPluginID(clm);
            StepMeta lookupstep = new StepMeta(log, lookupstepid, lookupstepname, (StepMetaInterface) clm);
            lookupstep.setDescription("Looks up information from table [lookup] on database [" + lookupDBInfo + "]");
            transMeta.addStep(lookupstep);                              

            TransHopMeta hi = new TransHopMeta(fromstep, lookupstep);
            transMeta.addTransHop(hi);

            // Now execute the transformation...
            Trans trans = new Trans(log, transMeta);
            trans.execute(null);            
                        
            trans.waitUntilFinished();            
            
            checkResults(lookupDatabase);
        }    	
        catch ( Exception ex ) { 
        	System.out.println(ex);
        }
    
    }    
}
