/**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **                                                                   **
 **********************************************************************/

package org.pentaho.di.trans.steps.detectlastrow;

import java.math.BigDecimal;
import java.util.ArrayList;
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
import org.pentaho.di.trans.steps.dummytrans.DummyTransMeta;
import org.pentaho.di.trans.steps.injector.InjectorMeta;


/**
 * Test class for the Detect Last Row (also now as "Identify 
 * last row in stream) step.
 *
 * @author Sven Boden
 */
public class DetectLastRowStepTest extends TestCase
{
	public RowMetaInterface createRowMetaInterface()
	{
		RowMetaInterface rm = new RowMeta();
		
		ValueMetaInterface[] valuesMeta = {
			    new ValueMeta("field1", ValueMeta.TYPE_STRING),
			    new ValueMeta("field2", ValueMeta.TYPE_INTEGER),
			    new ValueMeta("field3", ValueMeta.TYPE_NUMBER),
			    new ValueMeta("field5", ValueMeta.TYPE_BOOLEAN),
			    new ValueMeta("field6", ValueMeta.TYPE_BIGNUMBER), 
	    };

		for (int i=0; i < valuesMeta.length; i++ )
		{
			rm.addValueMeta(valuesMeta[i]);
		}
		
		return rm;
	}
	
	/**
	 * Create data rows.
	 * 
	 * @param nrRows nr of rows to insert (from 0 to 3 for the moment)
	 * 
	 * @return List of row and meta data
	 */
	public List<RowMetaAndData> createData(int nrRows)
	{
		List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();	
		
		RowMetaInterface rm = createRowMetaInterface();
		
		Object[] r1 = new Object[] { "KETTLE1", new Long(123L), 
                                     new Double(10.5D), 
                                     Boolean.TRUE, BigDecimal.valueOf(123.45) };
		Object[] r2 = new Object[] { "KETTLE2", new Long(500L), 
                                     new Double(20.0D), Boolean.FALSE, BigDecimal.valueOf(123.45) };
		Object[] r3 = new Object[] { "KETTLE3", new Long(501L), 
                                     new Double(21.0D), Boolean.FALSE, BigDecimal.valueOf(123.45) };
		
		list.add(new RowMetaAndData(rm, r1));
		list.add(new RowMetaAndData(rm, r2));
		list.add(new RowMetaAndData(rm, r3));
		
		return list.subList(0, nrRows);
	}
	
	public RowMetaInterface createResultRowMetaInterface()
	{
		RowMetaInterface rm = new RowMeta();
		
		ValueMetaInterface[] valuesMeta = {
			    new ValueMeta("field1", ValueMeta.TYPE_STRING),
			    new ValueMeta("field2", ValueMeta.TYPE_INTEGER),
			    new ValueMeta("field3", ValueMeta.TYPE_NUMBER),
			    new ValueMeta("field5", ValueMeta.TYPE_BOOLEAN),
			    new ValueMeta("field6", ValueMeta.TYPE_BIGNUMBER),
			    new ValueMeta("result", ValueMeta.TYPE_BOOLEAN),
	    };

		for (int i=0; i < valuesMeta.length; i++ )
		{
			rm.addValueMeta(valuesMeta[i]);
		}
		
		return rm;
	}
	
	/**
	 * Create result data rows.
	 * 
	 * @param nrRows nr of rows to insert (from 0 to 3 for the moment)
	 * 
	 * @return List of row and meta data
	 */
	public List<RowMetaAndData> createResultData(int nrRows)
	{
		List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();	
		
		RowMetaInterface rm = createResultRowMetaInterface();
		
		Object[] r1 = new Object[] { "KETTLE1", new Long(123L), 
                                     new Double(10.5D), Boolean.TRUE, BigDecimal.valueOf(123.45), 
                                     (nrRows == 1 ? Boolean.TRUE : Boolean.FALSE) };
		Object[] r2 = new Object[] { "KETTLE2", new Long(500L), 
                                     new Double(20.0D), Boolean.FALSE, BigDecimal.valueOf(123.45), 
                                     (nrRows == 2 ? Boolean.TRUE : Boolean.FALSE) };
		Object[] r3 = new Object[] { "KETTLE3", new Long(501L), 
                                     new Double(21.0D), Boolean.FALSE, BigDecimal.valueOf(123.45), 
                                     (nrRows == 3 ? Boolean.TRUE : Boolean.FALSE) };
		
		list.add(new RowMetaAndData(rm, r1));
		list.add(new RowMetaAndData(rm, r2));
		list.add(new RowMetaAndData(rm, r3));
				
		return list.subList(0, nrRows);
	}
	
	
	/**
	 *  Check the 2 lists comparing the rows in order.
	 *  If they are not the same fail the test. 
	 *  
	 *  @param rows1 first row set to compare
	 *  @param rows2 second row set to compare
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
        	int fields[] = new int[rm1.size()];
        	for ( int ydx = 0; ydx < rm1.size(); ydx++ )
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
	 * Test case Detect Last Row step. Nr of rows to test with as argument.
	 * 
	 * @param nrRows Number of rows to test.
	 * 
	 * @throws Exception upon any exception
	 */
    public void detectLastRowStepTest(int nrRows) throws Exception
    {
        KettleEnvironment.init();

        //
        // Create a new transformation...
        //
        TransMeta transMeta = new TransMeta();
        transMeta.setName("detectlastrowtest1");
    	
        PluginRegistry registry = PluginRegistry.getInstance();            

        // 
        // create an injector step...
        //
        String injectorStepname = "injector step";
        InjectorMeta im = new InjectorMeta();
        
        // Set the information of the injector.                
        String injectorPid = registry.getPluginId(StepPluginType.getInstance(), im);
        StepMeta injectorStep = new StepMeta(injectorPid, injectorStepname, im);
        transMeta.addStep(injectorStep);

        // 
        // Create a dummy step 1
        //
        String dummyStepname1 = "dummy step 1";            
        DummyTransMeta dm1 = new DummyTransMeta();

        String dummyPid1 = registry.getPluginId(StepPluginType.getInstance(), dm1);
        StepMeta dummyStep1 = new StepMeta(dummyPid1, dummyStepname1, dm1);
        transMeta.addStep(dummyStep1);                              

        TransHopMeta hi = new TransHopMeta(injectorStep, dummyStep1);
        transMeta.addTransHop(hi);

        // 
        // Create a detect last row step
        //
        String delectLastRowStepname = "detect last row step";            
        DetectLastRowMeta dlrm = new DetectLastRowMeta();
        
        dlrm.setResultFieldName("result");

        String detectLastRowStepPid = registry.getPluginId(StepPluginType.getInstance(), dlrm);
        StepMeta detectLastRowStep = new StepMeta(detectLastRowStepPid, delectLastRowStepname, dlrm);
        transMeta.addStep(detectLastRowStep);                              

        TransHopMeta hi2 = new TransHopMeta(dummyStep1, detectLastRowStep);
        transMeta.addTransHop(hi2);        
        
        // 
        // Create a dummy step 2
        //
        String dummyStepname2 = "dummy step 2";            
        DummyTransMeta dm2 = new DummyTransMeta();

        String dummyPid2 = registry.getPluginId(StepPluginType.getInstance(), dm2);
        StepMeta dummyStep2 = new StepMeta(dummyPid2, dummyStepname2, dm2);
        transMeta.addStep(dummyStep2);                              

        TransHopMeta hi3 = new TransHopMeta(detectLastRowStep, dummyStep2);
        transMeta.addTransHop(hi3);        
        
                
        // Now execute the transformation...
        Trans trans = new Trans(transMeta);

        trans.prepareExecution(null);
                
        StepInterface si = trans.getStepInterface(dummyStepname1, 0);
        RowStepCollector dummyRc1 = new RowStepCollector();
        si.addRowListener(dummyRc1);

        si = trans.getStepInterface(delectLastRowStepname, 0);
        RowStepCollector detectLastRc = new RowStepCollector();
        si.addRowListener(detectLastRc);
                       
        RowProducer rp = trans.addRowProducer(injectorStepname, 0);
        trans.startThreads();
        
        // add rows
        List<RowMetaAndData> inputList = createData(3);
        Iterator<RowMetaAndData> it = inputList.iterator();
        while ( it.hasNext() )
        {
        	RowMetaAndData rm = it.next();
        	rp.putRow(rm.getRowMeta(), rm.getData());
        }   
        rp.finished();

        trans.waitUntilFinished();   
                
        List<RowMetaAndData> resultRows1 = dummyRc1.getRowsRead();
        checkRows(resultRows1, inputList);
        
        List<RowMetaAndData> goldRows = createResultData(3);
        List<RowMetaAndData> resultRows2 = detectLastRc.getRowsWritten();
        checkRows(resultRows2, goldRows);                       
    }
    
    /**
     * Test with 0 rows
     *  
     * @throws Exception Upon any error.
     */
    public void testDetectLastRowStep0() throws Exception
    {
    	detectLastRowStepTest(0);
    }
    
    /**
     * Test with 1 rows
     *  
     * @throws Exception Upon any error.
     */
    public void testDetectLastRowStep1() throws Exception
    {
    	detectLastRowStepTest(1);
    }
    
    /**
     * Test with 2 rows
     *  
     * @throws Exception Upon any error.
     */
    public void testDetectLastRowStep2() throws Exception
    {
    	detectLastRowStepTest(2);
    }
    
    /**
     * Test with 3 rows
     *  
     * @throws Exception Upon any error.
     */
    public void testDetectLastRowStep3() throws Exception
    {
    	detectLastRowStepTest(3);
    }       
}