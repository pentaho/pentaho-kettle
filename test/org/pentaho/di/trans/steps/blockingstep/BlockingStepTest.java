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

package org.pentaho.di.trans.steps.blockingstep;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.RowMetaAndData;
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
import org.pentaho.di.trans.steps.dummytrans.DummyTransMeta;
import org.pentaho.di.trans.steps.injector.InjectorMeta;


/**
 * Test class for the BlockingStep step.
 *
 * @author Sven Boden
 */
public class BlockingStepTest extends TestCase
{
	public RowMetaInterface createRowMetaInterface()
	{
		RowMetaInterface rm = new RowMeta();
		
		ValueMetaInterface valuesMeta[] = {
			    new ValueMeta("field1", ValueMeta.TYPE_STRING),
			    new ValueMeta("field2", ValueMeta.TYPE_INTEGER),
			    new ValueMeta("field3", ValueMeta.TYPE_NUMBER),
			    new ValueMeta("field4", ValueMeta.TYPE_DATE),
			    new ValueMeta("field5", ValueMeta.TYPE_BOOLEAN),
			    new ValueMeta("field6", ValueMeta.TYPE_BIGNUMBER),
			    new ValueMeta("field7", ValueMeta.TYPE_BIGNUMBER) 
	    };

		for (int i=0; i < valuesMeta.length; i++ )
		{
			rm.addValueMeta(valuesMeta[i]);
		}
		
		return rm;
	}
	
	public List<RowMetaAndData> createData()
	{
		List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();	
		
		RowMetaInterface rm = createRowMetaInterface();
		
		Object[] r1 = new Object[] { "KETTLE1", new Long(123L), 
                                     new Double(10.5D), new Date(),
                                     Boolean.TRUE, BigDecimal.valueOf(123.45),
                                     BigDecimal.valueOf(123.60) };
		Object[] r2 = new Object[] { "KETTLE2", new Long(500L), 
                                     new Double(20.0D), new Date(),
                                     Boolean.FALSE, BigDecimal.valueOf(123.45),
                                     BigDecimal.valueOf(123.60) };
		Object[] r3 = new Object[] { "KETTLE3", new Long(501L), 
                                     new Double(21.0D), new Date(),
                                     Boolean.FALSE, BigDecimal.valueOf(123.45),
                                     BigDecimal.valueOf(123.70) };
		
		list.add(new RowMetaAndData(rm, r1));
		list.add(new RowMetaAndData(rm, r2));
		list.add(new RowMetaAndData(rm, r3));
		
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
        	
        	if ( r1.length != r2.length )
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
	 * Test case for blocking step step. Injector step to a blocking step
	 * to a dummy step. rows go in, only 1 row should be output (the last one).
	 */
    public void testBlockingStep() throws Exception
    {
        KettleEnvironment.init();

        //
        // Create a new transformation...
        //
        TransMeta transMeta = new TransMeta();
        transMeta.setName("blockingsteptest");
    	
        PluginRegistry registry = PluginRegistry.getInstance();            

        // 
        // create an injector step...
        //
        String injectorStepname = "injector step";
        InjectorMeta im = new InjectorMeta();
        
        // Set the information of the injector.                
        String injectorPid = registry.getPluginId(StepPluginType.getInstance(), im);
        StepMeta injectorStep = new StepMeta(injectorPid, injectorStepname, (StepMetaInterface)im);
        transMeta.addStep(injectorStep);

        // 
        // Create a dummy step 1
        //
        String dummyStepname1 = "dummy step 1";            
        DummyTransMeta dm1 = new DummyTransMeta();

        String dummyPid1 = registry.getPluginId(StepPluginType.getInstance(), dm1);
        StepMeta dummyStep1 = new StepMeta(dummyPid1, dummyStepname1, (StepMetaInterface)dm1);
        transMeta.addStep(dummyStep1);                              

        TransHopMeta hi = new TransHopMeta(injectorStep, dummyStep1);
        transMeta.addTransHop(hi);

        // 
        // Create a blocking step
        //
        String blockingStepname = "blocking step";            
        BlockingStepMeta bm = new BlockingStepMeta();

        String blockingStepPid = registry.getPluginId(StepPluginType.getInstance(), bm);
        StepMeta blockingStep = new StepMeta(blockingStepPid, blockingStepname, (StepMetaInterface)bm);
        transMeta.addStep(blockingStep);                              

        TransHopMeta hi2 = new TransHopMeta(dummyStep1, blockingStep);
        transMeta.addTransHop(hi2);        
        
        // 
        // Create a dummy step 2
        //
        String dummyStepname2 = "dummy step 2";            
        DummyTransMeta dm2 = new DummyTransMeta();

        String dummyPid2 = registry.getPluginId(StepPluginType.getInstance(), dm2);
        StepMeta dummyStep2 = new StepMeta(dummyPid2, dummyStepname2, (StepMetaInterface)dm2);
        transMeta.addStep(dummyStep2);                              

        TransHopMeta hi3 = new TransHopMeta(blockingStep, dummyStep2);
        transMeta.addTransHop(hi3);        
        
                
        // Now execute the transformation...
        Trans trans = new Trans(transMeta);

        trans.prepareExecution(null);
                
        StepInterface si = trans.getStepInterface(dummyStepname1, 0);
        RowStepCollector dummyRc1 = new RowStepCollector();
        si.addRowListener(dummyRc1);

        si = trans.getStepInterface(blockingStepname, 0);
        RowStepCollector blockingRc = new RowStepCollector();
        si.addRowListener(blockingRc);
               
        si = trans.getStepInterface(dummyStepname2, 0);
        RowStepCollector dummyRc2 = new RowStepCollector();
        si.addRowListener(dummyRc2);
        
        RowProducer rp = trans.addRowProducer(injectorStepname, 0);
        trans.startThreads();
        
        // add rows
        List<RowMetaAndData> inputList = createData();
        Iterator<RowMetaAndData> it = inputList.iterator();
        while ( it.hasNext() )
        {
        	RowMetaAndData rm = it.next();
        	rp.putRow(rm.getRowMeta(), rm.getData());
        }   
        rp.finished();

        trans.waitUntilFinished();   
        
        // The results should be that dummy1 gets all rows.
        // blocking step should receive all rows (but only send the 
        // last one through). dummy2 should only get the last row.
        
        List<RowMetaAndData> resultRows1 = dummyRc1.getRowsRead();
        checkRows(resultRows1, inputList);
        
        List<RowMetaAndData> resultRows2 = blockingRc.getRowsRead();
        checkRows(resultRows2, inputList);
                
        List<RowMetaAndData> resultRows3 = dummyRc2.getRowsRead();
        List<RowMetaAndData> lastList = new ArrayList<RowMetaAndData>();
        lastList.add(inputList.get(inputList.size() - 1));
        checkRows(resultRows3, lastList);
    }
    
	/**
	 * Test case for blocking step step passing all rows. Injector step to a 
	 * blocking step to a dummy step. rows go in, all rows should be output.
	 */
    public void testBlockingStepPassAll() throws Exception
    {
        KettleEnvironment.init();

        //
        // Create a new transformation...
        //
        TransMeta transMeta = new TransMeta();
        transMeta.setName("blockingsteptest");
    	
        PluginRegistry registry = PluginRegistry.getInstance();            

        // 
        // create an injector step...
        //
        String injectorStepname = "injector step";
        InjectorMeta im = new InjectorMeta();
        
        // Set the information of the injector.                
        String injectorPid = registry.getPluginId(StepPluginType.getInstance(), im);
        StepMeta injectorStep = new StepMeta(injectorPid, injectorStepname, (StepMetaInterface)im);
        transMeta.addStep(injectorStep);

        // 
        // Create a dummy step 1
        //
        String dummyStepname1 = "dummy step 1";            
        DummyTransMeta dm1 = new DummyTransMeta();

        String dummyPid1 = registry.getPluginId(StepPluginType.getInstance(), dm1);
        StepMeta dummyStep1 = new StepMeta(dummyPid1, dummyStepname1, (StepMetaInterface)dm1);
        transMeta.addStep(dummyStep1);                              

        TransHopMeta hi = new TransHopMeta(injectorStep, dummyStep1);
        transMeta.addTransHop(hi);

        // 
        // Create a blocking step
        //
        String blockingStepname = "blocking step";            
        BlockingStepMeta bm = new BlockingStepMeta();
        bm.setPassAllRows(true);

        String blockingStepPid = registry.getPluginId(StepPluginType.getInstance(), bm);
        StepMeta blockingStep = new StepMeta(blockingStepPid, blockingStepname, (StepMetaInterface)bm);
        transMeta.addStep(blockingStep);            

        TransHopMeta hi2 = new TransHopMeta(dummyStep1, blockingStep);
        transMeta.addTransHop(hi2);        
        
        // 
        // Create a dummy step 2
        //
        String dummyStepname2 = "dummy step 2";            
        DummyTransMeta dm2 = new DummyTransMeta();

        String dummyPid2 = registry.getPluginId(StepPluginType.getInstance(), dm2);
        StepMeta dummyStep2 = new StepMeta(dummyPid2, dummyStepname2, (StepMetaInterface)dm2);
        transMeta.addStep(dummyStep2);                              

        TransHopMeta hi3 = new TransHopMeta(blockingStep, dummyStep2);
        transMeta.addTransHop(hi3);        
        
        // Now execute the transformation...
        Trans trans = new Trans(transMeta);

        trans.prepareExecution(null);
                
        StepInterface si = trans.getStepInterface(dummyStepname1, 0);
        RowStepCollector dummyRc1 = new RowStepCollector();
        si.addRowListener(dummyRc1);

        si = trans.getStepInterface(blockingStepname, 0);
        RowStepCollector blockingRc = new RowStepCollector();
        si.addRowListener(blockingRc);
               
        si = trans.getStepInterface(dummyStepname2, 0);
        RowStepCollector dummyRc2 = new RowStepCollector();
        si.addRowListener(dummyRc2);
        
        RowProducer rp = trans.addRowProducer(injectorStepname, 0);
        trans.startThreads();
        
        // add rows
        List<RowMetaAndData> inputList = createData();
        Iterator<RowMetaAndData> it = inputList.iterator();
        while ( it.hasNext() )
        {
        	RowMetaAndData rm = (RowMetaAndData)it.next();
        	rp.putRow(rm.getRowMeta(), rm.getData());
        }   
        rp.finished();

        trans.waitUntilFinished();   
        
        // The results should be that dummy1 gets all rows.
        // blocking step should receive all rows (but only send the 
        // last one through). dummy2 should only get the last row.
        
        List<RowMetaAndData> resultRows1 = dummyRc1.getRowsWritten();
        checkRows(resultRows1, inputList);
        
        List<RowMetaAndData> resultRows2 = blockingRc.getRowsWritten();
        checkRows(resultRows2, inputList);
                
        List<RowMetaAndData> resultRows3 = dummyRc2.getRowsWritten();
        checkRows(resultRows3, inputList);
    }    
}