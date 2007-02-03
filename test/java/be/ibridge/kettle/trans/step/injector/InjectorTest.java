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

package be.ibridge.kettle.trans.step.injector;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.util.EnvUtil;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.trans.RowProducer;
import be.ibridge.kettle.trans.StepLoader;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransHopMeta;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.RowStepCollector;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;
import be.ibridge.kettle.trans.step.dummytrans.DummyTransMeta;

/**
 * Test class for the Injector step.
 *
 * @author Sven Boden
 */
public class InjectorTest extends TestCase
{
	public List createData()
	{
		List list = new ArrayList();
		Row r = new Row();
		
		Value values[] = {
			    new Value("field1", "KETTLE1"),               // String
				new Value("field2", 123L),                    // integer
				new Value("field3", 10.5D),                   // double
				new Value("field4", new Date()),              // Date
				new Value("field5", true),                    // Boolean
				new Value("field6", new BigDecimal(123.45)),  // BigDecimal
				new Value("field7", new BigDecimal(123.60))   // BigDecimal
		};

		for (int i=0; i < values.length; i++ )
		{
			r.addValue(values[i]);
		}
		
		Row r1 = new Row();
		Value values1[] = {
			    new Value("field1", "KETTLE2"),               // String
				new Value("field2", 500L),                    // integer
				new Value("field3", 20.0D),                   // double
				new Value("field4", new Date()),              // Date
				new Value("field5", false),                   // Boolean
				new Value("field6", new BigDecimal(123.45)),  // BigDecimal
				new Value("field7", new BigDecimal(123.60))   // BigDecimal				
		};

		for (int i=0; i < values1.length; i++ )
		{
			r1.addValue(values1[i]);
		}

		Row r2 = new Row();
		Value values2[] = {
			    new Value("field1", "KETTLE3"),               // String
				new Value("field2", 500L),                    // integer
				new Value("field3", 20.0D),                   // double
				new Value("field4", new Date()),              // Date
				new Value("field5", false),                   // Boolean
				new Value("field6", new BigDecimal(123.45)),  // BigDecimal
				new Value("field7", new BigDecimal(123.60))   // BigDecimal				
		};

		for (int i=0; i < values2.length; i++ )
		{
			r2.addValue(values2[i]);
		}
		
		list.add(r);
		list.add(r1);
		list.add(r2);
		
		return list;
	}

	/**
	 *  Check the 2 lists comparing the rows in order.
	 *  If they are not the same fail the test. 
	 */
    public void checkRows(List rows1, List rows2)
    {
    	int idx = 1;
        if ( rows1.size() != rows2.size() )
        {
        	fail("Number of rows is not the same: " + 
          		 rows1.size() + " and " + rows2.size());
        }
        Iterator it1 = rows1.iterator();
        Iterator it2 = rows2.iterator();
        
        while ( it1.hasNext() && it2.hasNext() )
        {
            Row r1 = (Row)it1.next();
            Row r2 = (Row)it2.next();
            if ( r1.compare(r2) != 0 )
            {
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
        LogWriter log = LogWriter.getInstance();
        EnvUtil.environmentInit();

        //
        // Create a new transformation...
        //
        TransMeta transMeta = new TransMeta();
        transMeta.setName("injectortest");
    	
        StepLoader steploader = StepLoader.getInstance();            

        // 
        // create an injector step...
        //
        String injectorStepname = "injector step";
        InjectorMeta im = new InjectorMeta();
        
        // Set the information of tje injector.
                
        String injectorPid = steploader.getStepPluginID(im);
        StepMeta injectorStep = new StepMeta(injectorPid, injectorStepname, (StepMetaInterface)im);
        transMeta.addStep(injectorStep);

        // 
        // Create a dummy step
        //
        String dummyStepname = "dummy step";            
        DummyTransMeta dm = new DummyTransMeta();

        String dummyPid = steploader.getStepPluginID(dm);
        StepMeta dummyStep = new StepMeta(dummyPid, dummyStepname, (StepMetaInterface)dm);
        transMeta.addStep(dummyStep);                              

        TransHopMeta hi = new TransHopMeta(injectorStep, dummyStep);
        transMeta.addTransHop(hi);
                
        // Now execute the transformation...
        Trans trans = new Trans(log, transMeta);

        trans.prepareExecution(null);
                
        StepInterface si = trans.getStepInterface(dummyStepname, 0);
        RowStepCollector rc = new RowStepCollector();
        si.addRowListener(rc);
        
        RowProducer rp = trans.addRowProducer(injectorStepname, 0);
        trans.startThreads();
        
        // add rows
        List inputList = createData();
        Iterator it = inputList.iterator();
        while ( it.hasNext() )
        {
        	Row r = (Row)it.next();
        	rp.putRow(r);
        }   
        rp.finished();

        trans.waitUntilFinished();   
        
        List resultRows = rc.getRowsRead();
        checkRows(resultRows, inputList);
    }
}