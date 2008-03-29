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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.trans.RowProducer;
import org.pentaho.di.trans.RowStepCollector;
import org.pentaho.di.trans.StepLoader;
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
	 * Create table for the normal case.
	 */
	public void createTableNormalCase(Database db) throws Exception
	{		
		String source = db.getCreateTableStatement(target_table, createSourceRowMetaInterface1(), null, false, null, true);
		try  {
		    db.execStatement(source);
		}
		catch ( KettleException ex ) 
		{
			fail("failure while creating table " + target_table + ": " + ex.getMessage());	
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

	/**
	 * Create the result rows for the normal case.
	 *
	public List<RowMetaAndData> createNormalResultDataRows()
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
	} */	
	
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
	 * Test case for normal table output case. 
	 */
    public void testTableInputWithParam() throws Exception
    {
        EnvUtil.environmentInit();
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
            Database database = new Database(dbInfo);
            database.connect();
            createTableNormalCase(database);

            StepLoader steploader = StepLoader.getInstance();            

            // 
            // create an injector step...
            //
            String injectorStepname = "injector step";
            InjectorMeta im = new InjectorMeta();
            
            // Set the information of the injector.                   
            String injectorPid = steploader.getStepPluginID(im);
            StepMeta injectorStep = new StepMeta(injectorPid, injectorStepname, (StepMetaInterface)im);
            transMeta.addStep(injectorStep);            
            
            // 
            // create the source step...
            //
            String outputname = "output to [" + target_table + "]";
            TableOutputMeta tom = new TableOutputMeta();
            tom.setDatabaseMeta(transMeta.findDatabase("db"));
            tom.setTablename(target_table);

            String fromid = steploader.getStepPluginID(tom);
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
        }    	
        finally {}    
    }    
}