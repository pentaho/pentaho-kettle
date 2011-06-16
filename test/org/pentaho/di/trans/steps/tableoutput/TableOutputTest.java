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

package org.pentaho.di.trans.steps.tableoutput;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.RowProducer;
import org.pentaho.di.trans.RowStepCollector;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.injector.InjectorMeta;


/**
 * Test class for tableinput. H2 is used as database in memory to get 
 * an easy playground for database tests. H2 does not support all SQL 
 * features but it should proof enough for most of our tests.
 *
 * @author Sven Boden
 */
public class TableOutputTest extends TestCase
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

    private static String target_table  = "table";
    private static String target_table1 = "table1";
    private static String target_table2 = "table2";
    private static String target_table3 = "table3";

	/**
	 * Create table for the normal case.
	 */
	public void createTable(Database db, String tableName, RowMetaInterface rm) throws Exception
	{		
		String source = db.getCreateTableStatement(tableName, rm, null, false, null, true);
		try  {
		    db.execStatement(source);
		}
		catch ( KettleException ex ) 
		{
			fail("failure while creating table " + tableName + ": " + ex.getMessage());	
		}						
	}
	
	/**
   * Drop table
   */
  public void dropTable(Database db, String tableName) throws Exception
  {   
    String source = "DROP TABLE "+tableName+";";
    try  {
        db.execStatement(source);
    }
    catch ( KettleException ex ) 
    {
      fail("failure while dropping table " + tableName + ": " + ex.getMessage()); 
    }           
  }

    
	public RowMetaInterface createSourceRowMetaInterface1()
	{
		RowMetaInterface rm = new RowMeta();

		ValueMetaInterface valuesMeta[] = {
			    new ValueMeta("ID",   ValueMeta.TYPE_INTEGER, 8, 0),
			    new ValueMeta("CODE", ValueMeta.TYPE_INTEGER, 8, 0),
	    };

		for (int i=0; i < valuesMeta.length; i++ )
		{
			rm.addValueMeta(valuesMeta[i]);
		}
		
		return rm;
	}       	
	
	
	/**
	 * Create the input rows used for the normal unit test.
	 */
	public List<RowMetaAndData> createNormalDataRows()
	{
		List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();	
		
		RowMetaInterface rm = createSourceRowMetaInterface1();
		
		Object[] r1 = new Object[] { new Long(100L), new Long(1000L) };
		Object[] r2 = new Object[] { new Long(101L), new Long(1001L) };
		Object[] r3 = new Object[] { new Long(102L), new Long(1002L) };
		Object[] r4 = new Object[] { new Long(103L), new Long(1003L) };
		Object[] r5 = new Object[] { new Long(104L), new Long(1004L) };
		Object[] r6 = new Object[] { new Long(105L), new Long(1005L) };
		Object[] r7 = new Object[] { new Long(106L), new Long(1006L) };					
		
		list.add(new RowMetaAndData(rm, r1));
		list.add(new RowMetaAndData(rm, r2));
		list.add(new RowMetaAndData(rm, r3));
		list.add(new RowMetaAndData(rm, r4));
		list.add(new RowMetaAndData(rm, r5));
		list.add(new RowMetaAndData(rm, r6));
		list.add(new RowMetaAndData(rm, r7));
		
		return list;
	}	

	
	public RowMetaInterface createJIRA897RowMetaInterface()
	{
		RowMetaInterface rm = new RowMeta();

		ValueMetaInterface valuesMeta[] = {
			    new ValueMeta("ID",    ValueMeta.TYPE_INTEGER,  8, 0),
			    new ValueMeta("TABLE", ValueMeta.TYPE_STRING,  30, 0),
			    new ValueMeta("CODE",  ValueMeta.TYPE_INTEGER,  8, 0),
	    };

		for (int i=0; i < valuesMeta.length; i++ )
		{
			rm.addValueMeta(valuesMeta[i]);
		}
		
		return rm;
	}       	
	
	
	/**
	 * Create the input rows used for the JIRA897 unit test.
	 */
	public List<RowMetaAndData> createJIRA897DataRows()
	{
		List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();	
		
		RowMetaInterface rm = createJIRA897RowMetaInterface();
		
		Object[] r1 = new Object[] { new Long(100L), target_table1, new Long(1000L) };
		Object[] r2 = new Object[] { new Long(101L), target_table2, new Long(1001L) };
		Object[] r3 = new Object[] { new Long(102L), target_table1, new Long(1002L) };
		Object[] r4 = new Object[] { new Long(103L), target_table2, new Long(1003L) };
		Object[] r5 = new Object[] { new Long(104L), target_table2, new Long(1004L) };
		Object[] r6 = new Object[] { new Long(105L), target_table1, new Long(1005L) };
		Object[] r7 = new Object[] { new Long(106L), target_table1, new Long(1006L) };					
		
		list.add(new RowMetaAndData(rm, r1));
		list.add(new RowMetaAndData(rm, r2));
		list.add(new RowMetaAndData(rm, r3));
		list.add(new RowMetaAndData(rm, r4));
		list.add(new RowMetaAndData(rm, r5));
		list.add(new RowMetaAndData(rm, r6));
		list.add(new RowMetaAndData(rm, r7));
		
		return list;
	}	

	
	/**
	 * Create the result rows for the JIRA897 case.
	 */
	public List<RowMetaAndData> createJIRA897ResultDataRows()
	{
		List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();	
		
		RowMetaInterface rm = createSourceRowMetaInterface1();
		
		Object[] r1  = new Object[] { new Long(100L),   new Long(1000L) };
		Object[] r2  = new Object[] { new Long(101L),   new Long(1001L) };
		Object[] r3  = new Object[] { new Long(102L),   new Long(1002L) };
		Object[] r4  = new Object[] { new Long(103L),   new Long(1003L) };
		Object[] r5  = new Object[] { new Long(104L),   new Long(1004L) };
		Object[] r6  = new Object[] { new Long(105L),   new Long(1005L) };
		Object[] r7  = new Object[] { new Long(106L),   new Long(1006L) };
			
		list.add(new RowMetaAndData(rm, r1));
		list.add(new RowMetaAndData(rm, r2));
		list.add(new RowMetaAndData(rm, r3));
		list.add(new RowMetaAndData(rm, r4));
		list.add(new RowMetaAndData(rm, r5));
		list.add(new RowMetaAndData(rm, r6));
		list.add(new RowMetaAndData(rm, r7));
		
		return list;
	} 	
	
	/**
	 *  Check the 2 lists comparing the rows in order.
	 *  If they are not the same fail the test. 
	 */
    public void checkRows(List<RowMetaAndData> rows1, List<RowMetaAndData> rows2)
    {
    	int idx = 1;
        if ( rows1.size() != rows2.size() )
        {
        	fail("Number of rows is not the same: " + 
          		 rows1.size() + " and " + rows2.size());
        }
        Iterator<RowMetaAndData> it1 = rows1.iterator();
        Iterator<RowMetaAndData> it2 = rows2.iterator();
        
        while ( it1.hasNext() && it2.hasNext() )
        {
        	RowMetaAndData rm1 = it1.next();
        	RowMetaAndData rm2 = it2.next();
        	
        	Object[] r1 = rm1.getData();
        	Object[] r2 = rm2.getData();
        	
        	if ( rm1.size() != rm2.size() )
        	{
        		fail("row nr " + idx + " is not equal");
        	}
        	int fields[] = new int[r1.length];
        	for ( int ydx = 0; ydx < r1.length; ydx++ )
        	{
        		fields[ydx] = ydx;
        	}
            try {
				if ( rm1.getRowMeta().compare(r1, r2, fields) != 0 )
				{
					fail("row nr " + idx + " is not equal");
				}
			} catch (KettleValueException e) {
				fail("row nr " + idx + " is not equal");
			}
            	
            idx++;
        }
    }
    
	/**
	 * Check the results in the target dimension table.
	 * 
	 * @param db database to use.
	 */
	public void checkResultsNormal(Database db) throws Exception
	{
		String query = "SELECT ID, CODE FROM " +
		               target_table + " ORDER BY ID";

		String correctResults[] =  {
		    "100|1000",
		    "101|1001",
		    "102|1002",
		    "103|1003",
		    "104|1004",
		    "105|1005",
		    "106|1006",
		};
	
		ResultSet rs = db.openQuery(query);
		int idx = 0;
		while (rs.next() )
		{
		   int id = rs.getInt("ID");
		   int code = rs.getInt("CODE");
		   
		   String result = id + "|" + code;
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
	 * Check the results in the target dimension table.
	 * 
	 * @param db database to use.
	 */
	public void checkResultsJIRA897(Database db) throws Exception
	{		
		// Table 1
		String query = "SELECT ID, CODE FROM " +
		               target_table1 + " ORDER BY ID";

		String correctResults[] =  {
		    "100|1000",
		    "102|1002",
		    "105|1005",
		    "106|1006",
		};
	
		ResultSet rs = db.openQuery(query);
		int idx = 0;
		while (rs.next() )
		{
		   int id = rs.getInt("ID");
		   int code = rs.getInt("CODE");
		   
		   String result = id + "|" + code;
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
  	    
  	    
		// Table 2
		query = "SELECT ID, CODE FROM " +
		               target_table2 + " ORDER BY ID";

		String correctResults1[] =  {
		    "101|1001",
		    "103|1003",
		    "104|1004",
		};
	
		rs = db.openQuery(query);
		idx = 0;
		while (rs.next() )
		{
		   int id = rs.getInt("ID");
		   int code = rs.getInt("CODE");
		   
		   String result = id + "|" + code;
		   if ( idx > correctResults1.length)
		   {
		       fail("more rows returned than expected");
		   }
		   if ( ! result.equals(correctResults1[idx]))  {
			   fail("row " + (idx + 1) + " is different than expected");
		   }
		   idx++;
  	    }
  	    if ( idx < correctResults1.length)
	    {
	       fail("less rows returned than expected");
	    }  	    
	}    
	
	/**
	 * Test case for normal table output case. 
	 */
    public void testTableOutputNormal() throws Exception
    {
        KettleEnvironment.init();

        try
        {
            //
            // Create a new transformation...
            //
            TransMeta transMeta = new TransMeta();
            transMeta.setName("table output normal test");

            // Add the database connections
            for (int i=0;i<databasesXML.length;i++)
            {
                DatabaseMeta databaseMeta = new DatabaseMeta(databasesXML[i]);
                transMeta.addDatabase(databaseMeta);
            }

            DatabaseMeta dbInfo = transMeta.findDatabase("db");

            // Execute our setup SQLs in the database.
            Database database = new Database(transMeta, dbInfo);
            database.connect();
            createTable(database, target_table, createSourceRowMetaInterface1());

            PluginRegistry registry = PluginRegistry.getInstance();            

            // 
            // create an injector step...
            //
            String injectorStepname = "injector step";
            InjectorMeta im = new InjectorMeta();
            
            // Set the information of the injector.                   
            String injectorPid = registry.getPluginId(StepPluginType.class, im);
            StepMeta injectorStep = new StepMeta(injectorPid, injectorStepname, (StepMetaInterface)im);
            transMeta.addStep(injectorStep);            
            
            // 
            // create the source step...
            //
            String outputname = "output to [" + target_table + "]";
            TableOutputMeta tom = new TableOutputMeta();
            tom.setDatabaseMeta(transMeta.findDatabase("db"));
            tom.setTablename(target_table);

            String fromid = registry.getPluginId(StepPluginType.class, tom);
            StepMeta fromstep = new StepMeta(fromid, outputname, (StepMetaInterface)tom);
            fromstep.setDescription("write data to table [" + target_table + "] on database [" + dbInfo + "]");
            transMeta.addStep(fromstep);
            
            TransHopMeta hi = new TransHopMeta(injectorStep, fromstep);
            transMeta.addTransHop(hi);

            // Now execute the transformation...
            Trans trans = new Trans(transMeta);

            trans.prepareExecution(null);
                    
            StepInterface si = trans.getStepInterface(outputname, 0);
            RowStepCollector rc = new RowStepCollector();
            si.addRowListener(rc);
            
            RowProducer rp = trans.addRowProducer(injectorStepname, 0);
            trans.startThreads();
            
            // add rows
            List<RowMetaAndData> inputList = createNormalDataRows();
            for (RowMetaAndData rm : inputList )
            {
            	rp.putRow(rm.getRowMeta(), rm.getData());
            }   
            rp.finished();

            trans.waitUntilFinished();   

            List<RowMetaAndData> resultRows = rc.getRowsWritten();
            List<RowMetaAndData> goldRows = createNormalDataRows();
            checkRows(goldRows, resultRows);
            checkResultsNormal(database);
        }    	
        finally {}    
    }
    
    
	/**
	 * Test case for normal table output where the table is included in the instream, but the tablename
	 * is not stored in the table. 
	 */
    public void testTableOutputJIRA897() throws Exception
    {
        KettleEnvironment.init();

        try
        {
            //
            // Create a new transformation...
            //
            TransMeta transMeta = new TransMeta();
            transMeta.setName("table output JIRA897 test");

            // Add the database connections
            for (int i=0;i<databasesXML.length;i++)
            {
                DatabaseMeta databaseMeta = new DatabaseMeta(databasesXML[i]);
                transMeta.addDatabase(databaseMeta);
            }

            DatabaseMeta dbInfo = transMeta.findDatabase("db");

            // Execute our setup SQLs in the database.
            Database database = new Database(transMeta, dbInfo);
            database.connect();
            createTable(database, target_table1, createSourceRowMetaInterface1());
            createTable(database, target_table2, createSourceRowMetaInterface1());

            PluginRegistry registry = PluginRegistry.getInstance();            

            // 
            // create an injector step...
            //
            String injectorStepname = "injector step";
            InjectorMeta im = new InjectorMeta();
            
            // Set the information of the injector.                   
            String injectorPid = registry.getPluginId(StepPluginType.class, im);
            StepMeta injectorStep = new StepMeta(injectorPid, injectorStepname, (StepMetaInterface)im);
            transMeta.addStep(injectorStep);            
            
            // 
            // create the source step...
            //
            String outputname = "output to [" + target_table1 + "] and [" + target_table2 + "]";
            TableOutputMeta tom = new TableOutputMeta();
            tom.setDatabaseMeta(transMeta.findDatabase("db"));
            tom.setTableNameInField(true);
            tom.setTableNameField("TABLE");
            tom.setTableNameInTable(false);

            String fromid = registry.getPluginId(StepPluginType.class, tom);
            StepMeta fromstep = new StepMeta(fromid, outputname, (StepMetaInterface)tom);
            fromstep.setDescription("write data to tables on database [" + dbInfo + "]");
            transMeta.addStep(fromstep);
            
            TransHopMeta hi = new TransHopMeta(injectorStep, fromstep);
            transMeta.addTransHop(hi);

            // Now execute the transformation...
            Trans trans = new Trans(transMeta);

            trans.prepareExecution(null);
                    
            StepInterface si = trans.getStepInterface(outputname, 0);
            RowStepCollector rc = new RowStepCollector();
            si.addRowListener(rc);
            
            RowProducer rp = trans.addRowProducer(injectorStepname, 0);
            trans.startThreads();
            
            // add rows
            List<RowMetaAndData> inputList = createJIRA897DataRows();
            for (RowMetaAndData rm : inputList )
            {
            	rp.putRow(rm.getRowMeta(), rm.getData());
            }   
            rp.finished();

            trans.waitUntilFinished();   

            List<RowMetaAndData> resultRows = rc.getRowsWritten();

            // The name of the table should still be in here.
            List<RowMetaAndData> goldRows = createJIRA897DataRows();
            checkRows(goldRows, resultRows);
            checkResultsJIRA897(database);
        }    	
        finally {}    
    }
    
	/**
	 * Test case for commitSize see PDI2733 in JIRA.
	 */
    public void disabledTestTableOutputJIRA2733() throws Exception
    {
    	int dataDelay = 10;  	// Delay in milliseconds between issuing records to output rows  
    	
        KettleEnvironment.init();

        try
        {
            //
            // Create a new transformation...
            //
            TransMeta transMeta = new TransMeta();
            transMeta.setName("table output JIRA2733 test");

            // Add the database connections
            for (int i=0;i<databasesXML.length;i++)
            {
                DatabaseMeta databaseMeta = new DatabaseMeta(databasesXML[i]);
                transMeta.addDatabase(databaseMeta);
            }

            DatabaseMeta dbInfo = transMeta.findDatabase("db");

            // Execute our setup SQLs in the database.
            Database database = new Database(transMeta, dbInfo);
            database.connect();
            createTable(database, target_table3, createSourceRowMetaInterface1());
            // Add "ts" timestamp field to target_table with a default value of NOW()
            database.execStatement("ALTER TABLE " + target_table3 + " ADD COLUMN ts TIMESTAMP DEFAULT NOW() ");
            
            PluginRegistry registry = PluginRegistry.getInstance();            

            // 
            // create an injector step...
            //
            String injectorStepname = "injector step";
            InjectorMeta im = new InjectorMeta();
            
            // Set the information of the injector.                   
            String injectorPid = registry.getPluginId(StepPluginType.class, im);
            StepMeta injectorStep = new StepMeta(injectorPid, injectorStepname, (StepMetaInterface)im);
            transMeta.addStep(injectorStep);            
            
            // 
            // create the source step...
            //
            String outputname = "output to [" + target_table3 + "]";
            TableOutputMeta tom = new TableOutputMeta();
            tom.setDatabaseMeta(transMeta.findDatabase("db"));
            tom.setTablename(target_table3);
            tom.setTruncateTable(true);
            tom.setUseBatchUpdate(true);

            String fromid = registry.getPluginId(StepPluginType.class, tom);
            StepMeta fromstep = new StepMeta(fromid, outputname, (StepMetaInterface)tom);
            fromstep.setDescription("write data to table [" + target_table3 + "] on database [" + dbInfo + "]");
            transMeta.addStep(fromstep);
            
            TransHopMeta hi = new TransHopMeta(injectorStep, fromstep);
            transMeta.addTransHop(hi);
            
            // With seven rows these are the number of commits that need to made 
            // for "commitSize"s ranging between 0 and 8. 
            long goldRowCounts[] = { 7, 7, 4, 3, 2, 2, 2, 1, 1 }; 
            
            for (int commitSize=0; commitSize<=8; commitSize++) {
            	
            	tom.setCommitSize(commitSize);

            	// Now execute the transformation...
            	Trans trans = new Trans(transMeta);

	            trans.prepareExecution(null);
	                    
	            StepInterface si = trans.getStepInterface(outputname, 0);
	            RowStepCollector rc = new RowStepCollector();
	            si.addRowListener(rc);
	            
	            RowProducer rp = trans.addRowProducer(injectorStepname, 0);
	            trans.startThreads();
	            
	            // add rows
	            List<RowMetaAndData> inputList = createNormalDataRows();
	            for (RowMetaAndData rm : inputList )
	            {
	            	Thread.sleep(dataDelay);
	            	rp.putRow(rm.getRowMeta(), rm.getData());
	            }   
	            rp.finished();
	
	            trans.waitUntilFinished();
	            
	            String query = "SELECT ts FROM " + target_table3 + " ORDER BY ts";
	            ResultSet rs = database.openQuery(query);
	            Timestamp last_ts = null;
	            int actual_commits = 0;
	            while (rs.next() )
	            {
	            	Timestamp ts = rs.getTimestamp("ts");

	            	// WARNING: This comparison is "fuzzy".  The "ts" value DEFAULT NOW() may be slightly 
	            	// different during the commit, which is why it is not a straight comparison, but
	            	// the ts timestamps difference between two records must not be more than 5ms.
	            	//
	            	if (last_ts == null || 
	            		(!ts.equals(last_ts) && (ts.getTime() - last_ts.getTime() > 5))) {
	            		
	            		actual_commits++;
	            		last_ts = ts;
	            	}
	            	//System.out.println("commitSize=<" + commitSize + "> ts=<" + ts.toString() + "> actual_commits=<" + actual_commits + ">");
	            }
	           
	            long expected_commits = goldRowCounts[commitSize];
	            
	            if ( expected_commits != actual_commits) {
	            	fail("With commitSize=" + commitSize + " expected " + expected_commits + 
	            			" commits but actually got " + actual_commits);;
	            }
            }
            
            dropTable(database, target_table3);
        }    	
        finally {
        }    
    }
    
    public static void main(String[] args) throws Exception {
      TableOutputTest test = new TableOutputTest();
      for (int i=0;i<100;i++) {
        test.disabledTestTableOutputJIRA2733();
      }
    }
}

