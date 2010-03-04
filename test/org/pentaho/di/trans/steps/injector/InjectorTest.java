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

package org.pentaho.di.trans.steps.injector;

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


/**
 * Test class for the Injector step.
 *
 * @author Sven Boden
 */
public class InjectorTest extends TestCase
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
	 * Test case for injector step... also a show case on how
	 * to use injector.
	 */
    public void testInjector() throws Exception
    {
        KettleEnvironment.init();

        //
        // Create a new transformation...
        //
        TransMeta transMeta = new TransMeta();
        transMeta.setName("injectortest");
    	
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
        // Create a dummy step
        //
        String dummyStepname = "dummy step";            
        DummyTransMeta dm = new DummyTransMeta();

        String dummyPid = registry.getPluginId(StepPluginType.class, dm);
        StepMeta dummyStep = new StepMeta(dummyPid, dummyStepname, (StepMetaInterface)dm);
        transMeta.addStep(dummyStep);                              

        TransHopMeta hi = new TransHopMeta(injectorStep, dummyStep);
        transMeta.addTransHop(hi);
                
        // Now execute the transformation...
        Trans trans = new Trans(transMeta);

        trans.prepareExecution(null);
                
        StepInterface si = trans.getStepInterface(dummyStepname, 0);
        RowStepCollector rc = new RowStepCollector();
        si.addRowListener(rc);
        
        RowProducer rp = trans.addRowProducer(injectorStepname, 0);
        trans.startThreads();
        
        // add rows
        List<RowMetaAndData> inputList = createData();
        for (RowMetaAndData rm : inputList )
        {
        	rp.putRow(rm.getRowMeta(), rm.getData());
        }   
        rp.finished();

        trans.waitUntilFinished();   
        
        List<RowMetaAndData> resultRows = rc.getRowsWritten();
        checkRows(resultRows, inputList);
    }
}