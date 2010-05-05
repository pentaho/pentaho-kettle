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

package org.pentaho.di.trans.steps.append;

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
import org.pentaho.di.trans.step.errorhandling.StreamInterface;
import org.pentaho.di.trans.steps.dummytrans.DummyTransMeta;
import org.pentaho.di.trans.steps.injector.InjectorMeta;


/**
 * Test class for the Append step.
 *
 * @author Sven Boden
 */
public class AppendTest extends TestCase
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
	
	/**
	 * Create data for the first hop.
	 */
	public List<RowMetaAndData> createData1()
	{
		List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();	
		
		RowMetaInterface rm = createRowMetaInterface();
		
		Object[] r1 = new Object[] { "KETTLE1", new Long(123L), 
                                     new Double(10.5D), new Date(),
                                     Boolean.TRUE, BigDecimal.valueOf(123.45),
                                     BigDecimal.valueOf(123.60) };
		Object[] r2 = new Object[] { "KETTLE1", new Long(500L), 
                                     new Double(20.0D), new Date(),
                                     Boolean.FALSE, BigDecimal.valueOf(123.45),
                                     BigDecimal.valueOf(123.60) };
		Object[] r3 = new Object[] { "KETTLE1", new Long(501L), 
                                     new Double(21.0D), new Date(),
                                     Boolean.FALSE, BigDecimal.valueOf(123.45),
                                     BigDecimal.valueOf(123.70) };
		
		list.add(new RowMetaAndData(rm, r1));
		list.add(new RowMetaAndData(rm, r2));
		list.add(new RowMetaAndData(rm, r3));
		
		return list;
	}
	
	/**
	 * Create data for the second hop.
	 */	
	public List<RowMetaAndData> createData2()
	{
		List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();	
		
		RowMetaInterface rm = createRowMetaInterface();
		
		Object[] r1 = new Object[] { "KETTLE1", Long.valueOf(123L), 
                                     new Double(10.5D), new Date(),
                                     Boolean.TRUE, BigDecimal.valueOf(123.45),
                                     BigDecimal.valueOf(123.60) };
		Object[] r2 = new Object[] { "KETTLE1", Long.valueOf(500L), 
                                     new Double(20.0D), new Date(),
                                     Boolean.FALSE, BigDecimal.valueOf(123.45),
                                     BigDecimal.valueOf(123.60) };
		Object[] r3 = new Object[] { "KETTLE1", Long.valueOf(501L), 
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
        	RowMetaAndData rm1 = (RowMetaAndData)it1.next();
        	RowMetaAndData rm2 = (RowMetaAndData)it2.next();
        	
        	Object[] r1 = rm1.getData();
        	Object[] r2 = rm2.getData();
        	
        	if ( r1.length != r2.length )
        	{
        		fail("row nr " + idx + "is not equal");
        	}
        	int fields[] = new int[r1.length];
        	for ( int ydx = 0; ydx < r1.length; ydx++ )
        	{
        		fields[ydx] = ydx;
        	}
            try {
				if ( rm1.getRowMeta().compare(r1, r2, fields) != 0 )
				{
					fail("row nr " + idx + "is not equal");
				}
			} catch (KettleValueException e) {
				fail("row nr " + idx + "is not equal");
			}
            	
            idx++;
        }
    }

	
	/**
	 * Test case for Append step. 2 Injector steps to an append step
	 * to a dummy step. Rows go in, the order should be as defined
	 * in the append step.
	 */
    public void testAppendStep() throws Exception
    {
        KettleEnvironment.init();

        //
        // Create a new transformation...
        //
        TransMeta transMeta = new TransMeta();
        transMeta.setName("Appendtest");
    	
        PluginRegistry registry = PluginRegistry.getInstance();
        
        // 
        // create an injector step 1...
        //
        String injectorStepname1 = "injector step 1";
        InjectorMeta im1 = new InjectorMeta();
        
        // Set the information of the injector.                
        String injectorPid1 = registry.getPluginId(StepPluginType.class, im1);
        StepMeta injectorStep1 = new StepMeta(injectorPid1, injectorStepname1, (StepMetaInterface)im1);
        transMeta.addStep(injectorStep1);

        // 
        // create an injector step 2...
        //
        String injectorStepname2 = "injector step 2";
        InjectorMeta im2 = new InjectorMeta();
        
        // Set the information of the injector.                
        String injectorPid2 = registry.getPluginId(StepPluginType.class, im2);
        StepMeta injectorStep2 = new StepMeta(injectorPid2, injectorStepname2, (StepMetaInterface)im2);
        transMeta.addStep(injectorStep2);
                
        // 
        // Create an append step
        //
        String appendName = "append step";            
        AppendMeta am = new AppendMeta();
        List<StreamInterface> infoStreams = am.getStepIOMeta().getInfoStreams();
        infoStreams.get(0).setStepMeta(injectorStep1);
        infoStreams.get(1).setStepMeta(injectorStep2);
        
        String appendPid = registry.getPluginId(StepPluginType.class, am);
        StepMeta append = new StepMeta(appendPid, appendName, (StepMetaInterface)am);
        transMeta.addStep(append);            

        TransHopMeta hi2 = new TransHopMeta(injectorStep1, append);
        transMeta.addTransHop(hi2);

        TransHopMeta hi3 = new TransHopMeta(injectorStep2, append);
        transMeta.addTransHop(hi3);

        
        // 
        // Create a dummy step 1
        //
        String dummyStepname1 = "dummy step 1";            
        DummyTransMeta dm1 = new DummyTransMeta();

        String dummyPid1 = registry.getPluginId(StepPluginType.class, dm1);
        StepMeta dummyStep1 = new StepMeta(dummyPid1, dummyStepname1, (StepMetaInterface)dm1);
        transMeta.addStep(dummyStep1);                              

        TransHopMeta hi4 = new TransHopMeta(append, dummyStep1);
        transMeta.addTransHop(hi4);        
        
        // Now execute the transformation...
        Trans trans = new Trans(transMeta);

        trans.prepareExecution(null);
                
        StepInterface si = trans.getStepInterface(appendName, 0);
        RowStepCollector blockingRc = new RowStepCollector();
        si.addRowListener(blockingRc);
               
        si = trans.getStepInterface(dummyStepname1, 0);
        RowStepCollector dummyRc1 = new RowStepCollector();
        si.addRowListener(dummyRc1);
        
        RowProducer rp1 = trans.addRowProducer(injectorStepname1, 0);
        RowProducer rp2 = trans.addRowProducer(injectorStepname2, 0);
        trans.startThreads();        

        // add rows to tail step
        List<RowMetaAndData> inputList2 = createData2();
        Iterator<RowMetaAndData> it2 = inputList2.iterator();
        while ( it2.hasNext() )
        {
        	RowMetaAndData rm = (RowMetaAndData)it2.next();
        	rp2.putRow(rm.getRowMeta(), rm.getData());
        }   
        rp2.finished();        
        
        // add rows to head step
        List<RowMetaAndData> inputList1 = createData1();
        Iterator<RowMetaAndData> it1 = inputList1.iterator();
        while ( it1.hasNext() )
        {
        	RowMetaAndData rm = (RowMetaAndData)it1.next();
        	rp1.putRow(rm.getRowMeta(), rm.getData());
        }   
        rp1.finished();

        trans.waitUntilFinished();   
        
        // The result should be that first all rows from injector 1 and
        // then all rows from injector step 2        
        List<RowMetaAndData> expectedList = new ArrayList<RowMetaAndData>();
        expectedList.addAll(inputList1);
        expectedList.addAll(inputList2);
        
        List<RowMetaAndData> resultRows1 = dummyRc1.getRowsWritten();
        checkRows(resultRows1, expectedList);
    }    
}