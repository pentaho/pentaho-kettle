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

package org.pentaho.di.trans.steps.tableinput;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.pentaho.di.core.Const;
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
public class TableInputTest extends TestCase
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

    private static String source_table = "table_source1";

    private static String insertStatement[] = 
    {
    	// New rows for the source
        "INSERT INTO " + source_table + "(ID, CODE) " +
        "VALUES (1, 100)",
        
        "INSERT INTO " + source_table + "(ID, CODE) " +
        "VALUES (2, 100)",
        
        "INSERT INTO " + source_table + "(ID, CODE) " +
        "VALUES (3, 100)",

        "INSERT INTO " + source_table + "(ID, CODE) " +
        "VALUES (4, 100)",

        "INSERT INTO " + source_table + "(ID, CODE) " +
        "VALUES (5, 101)",
        
        "INSERT INTO " + source_table + "(ID, CODE) " +
        "VALUES (6, 101)",
        
        "INSERT INTO " + source_table + "(ID, CODE) " +
        "VALUES (7, 101)",

        "INSERT INTO " + source_table + "(ID, CODE) " +
        "VALUES (8, 101)",
        
        "INSERT INTO " + source_table + "(ID, CODE) " +
        "VALUES (9,  102)",
        
        "INSERT INTO " + source_table + "(ID, CODE) " +
        "VALUES (10, 102)",
        
        "INSERT INTO " + source_table + "(ID, CODE) " +
        "VALUES (11, 102)",

        "INSERT INTO " + source_table + "(ID, CODE) " +
        "VALUES (12, 102)",
        
        "INSERT INTO " + source_table + "(ID, CODE) " +
        "VALUES (13, 103)",

        "INSERT INTO " + source_table + "(ID, CODE) " +
        "VALUES (14, 103)",
        
        "INSERT INTO " + source_table + "(ID, CODE) " +
        "VALUES (15, 103)"        
    };
    
	public RowMetaInterface createSourceRowMetaInterface()
	{
		RowMetaInterface rm = new RowMeta();

		ValueMetaInterface valuesMeta[] = {
			    new ValueMeta("ID",   ValueMeta.TYPE_INTEGER, 8, 0),
			    new ValueMeta("CODE", ValueMeta.TYPE_INTEGER,  8, 0),
	    };

		for (int i=0; i < valuesMeta.length; i++ )
		{
			rm.addValueMeta(valuesMeta[i]);
		}
		
		return rm;
	}       	
	
	/**
	 * Create source table.
	 */
	public void createTables(Database db) throws Exception
	{		
		String source = db.getCreateTableStatement(source_table, createSourceRowMetaInterface(), null, false, null, true);
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
	
	public RowMetaInterface createRowMetaInterface()
	{
		RowMetaInterface rm = new RowMeta();
		
		ValueMetaInterface valuesMeta[] = {
			    new ValueMeta("int_field", ValueMeta.TYPE_INTEGER)
	    };

		for (int i=0; i < valuesMeta.length; i++ )
		{
			rm.addValueMeta(valuesMeta[i]);
		}
		
		return rm;
	}

	/**
	 * Create the input rows used for a unit test.
	 */
	public List<RowMetaAndData> createDataRows()
	{
		List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();	
		
		RowMetaInterface rm = createRowMetaInterface();
		
		Object[] r1 = new Object[] { new Long(100L) };
		Object[] r2 = new Object[] { new Long(101L) };
		Object[] r3 = new Object[] { new Long(103L) };
		
		list.add(new RowMetaAndData(rm, r1));
		list.add(new RowMetaAndData(rm, r2));
		list.add(new RowMetaAndData(rm, r3));
		
		return list;
	}	

	/**
	 * Create the result rows for a test.
	 */
	public List<RowMetaAndData> createResultDataRows()
	{
		List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();	
		
		RowMetaInterface rm = createSourceRowMetaInterface();
		
		Object[] r1  = new Object[] { new Long(1L),   new Long(100L) };
		Object[] r2  = new Object[] { new Long(2L),   new Long(100L) };
		Object[] r3  = new Object[] { new Long(3L),   new Long(100L) };
		Object[] r4  = new Object[] { new Long(4L),   new Long(100L) };
		Object[] r5  = new Object[] { new Long(5L),   new Long(101L) };
		Object[] r6  = new Object[] { new Long(6L),   new Long(101L) };
		Object[] r7  = new Object[] { new Long(7L),   new Long(101L) };
		Object[] r8  = new Object[] { new Long(8L),   new Long(101L) };
		Object[] r9  = new Object[] { new Long(13L),  new Long(103L) };
		Object[] r10 = new Object[] { new Long(14L),  new Long(103L) };
		Object[] r11 = new Object[] { new Long(15L),  new Long(103L) };
		
		list.add(new RowMetaAndData(rm, r1));
		list.add(new RowMetaAndData(rm, r2));
		list.add(new RowMetaAndData(rm, r3));
		list.add(new RowMetaAndData(rm, r4));
		list.add(new RowMetaAndData(rm, r5));
		list.add(new RowMetaAndData(rm, r6));
		list.add(new RowMetaAndData(rm, r7));
		list.add(new RowMetaAndData(rm, r8));
		list.add(new RowMetaAndData(rm, r9));
		list.add(new RowMetaAndData(rm, r10));
		list.add(new RowMetaAndData(rm, r11));
		
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
	 * Test case for table input which is taking its input from a hop. This is
	 * a regression test case for JIRA PDI-588.
	 * 
	 * The query in the table input step has one '?' and this parameter is filled
	 * by values read from an input hop.
	 */
    public void testTableInputWithParam() throws Exception
    {
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

            // Execute our setup SQLs in the database.
            Database database = new Database(transMeta, dbInfo);
            database.connect();
            createTables(database);
            createData(database);

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
            String fromstepname = "read from [" + source_table + "]";
            TableInputMeta tii = new TableInputMeta();
            tii.setDatabaseMeta(transMeta.findDatabase("db"));
            tii.setLookupFromStep(injectorStep);
            tii.setExecuteEachInputRow(true);
            String selectSQL = "SELECT "+Const.CR;
            selectSQL+="ID, CODE ";
            selectSQL+="FROM " + source_table + " WHERE CODE = ? ORDER BY ID, CODE;";
            tii.setSQL(selectSQL);

            String fromstepid = registry.getPluginId(StepPluginType.class, tii);
            StepMeta fromstep = new StepMeta(fromstepid, fromstepname, (StepMetaInterface) tii);
            fromstep.setDescription("Reads information from table [" + source_table + "] on database [" + dbInfo + "]");
            transMeta.addStep(fromstep);
            
            TransHopMeta hi = new TransHopMeta(injectorStep, fromstep);
            transMeta.addTransHop(hi);

            // Now execute the transformation...
            Trans trans = new Trans(transMeta);

            trans.prepareExecution(null);
                    
            StepInterface si = trans.getStepInterface(fromstepname, 0);
            RowStepCollector rc = new RowStepCollector();
            si.addRowListener(rc);
            
            RowProducer rp = trans.addRowProducer(injectorStepname, 0);
            trans.startThreads();
            
            // add rows
            List<RowMetaAndData> inputList = createDataRows();
            for (RowMetaAndData rm : inputList )
            {
            	rp.putRow(rm.getRowMeta(), rm.getData());
            }   
            rp.finished();

            trans.waitUntilFinished();   

            List<RowMetaAndData> resultRows = rc.getRowsWritten();
            List<RowMetaAndData> goldRows = createResultDataRows();
            checkRows(goldRows, resultRows);
        }    	
        finally {}    
    }    
}