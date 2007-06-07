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

package be.ibridge.kettle.trans.step.append;

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
import be.ibridge.kettle.trans.step.injector.InjectorMeta;
import be.ibridge.kettle.trans.step.append.AppendMeta;

/**
 * Test class for the Append step.
 *
 * @author Sven Boden
 */
public class AppendTest extends TestCase
{
	/**
	 * Create data for the first hop.
	 */
	public List createData1()
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
			    new Value("field1", "KETTLE1"),               // String
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
			    new Value("field1", "KETTLE1"),               // String
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
	 * Create data for the second hop.
	 */	
	public List createData2()
	{
		List list = new ArrayList();
		Row r = new Row();
		
		Value values[] = {
			    new Value("field1", "KETTLE2"),               // String
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
			    new Value("field1", "KETTLE2"),               // String
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
	 * Test case for Append step. 2 Injector steps to an append step
	 * to a dummy step. Rows go in, the order should be as defined
	 * in the append step.
	 */
    public void testAppendStep() throws Exception
    {
        LogWriter log = LogWriter.getInstance();
        EnvUtil.environmentInit();

        //
        // Create a new transformation...
        //
        TransMeta transMeta = new TransMeta();
        transMeta.setName("Appendtest");
    	
        StepLoader steploader = StepLoader.getInstance();            

        // 
        // create an injector step 1...
        //
        String injectorStepname1 = "injector step 1";
        InjectorMeta im1 = new InjectorMeta();
        
        // Set the information of the injector.                
        String injectorPid1 = steploader.getStepPluginID(im1);
        StepMeta injectorStep1 = new StepMeta(injectorPid1, injectorStepname1, (StepMetaInterface)im1);
        transMeta.addStep(injectorStep1);

        // 
        // create an injector step 2...
        //
        String injectorStepname2 = "injector step 2";
        InjectorMeta im2 = new InjectorMeta();
        
        // Set the information of the injector.                
        String injectorPid2 = steploader.getStepPluginID(im2);
        StepMeta injectorStep2 = new StepMeta(injectorPid2, injectorStepname2, (StepMetaInterface)im2);
        transMeta.addStep(injectorStep2);
                
        // 
        // Create an append step
        //
        String appendName = "append step";            
        AppendMeta am = new AppendMeta();
        am.setHeadStepMeta(injectorStep1);
        am.setTailStepMeta(injectorStep2);
        
        // Followign are for GUI purposes only.
        //am.setHeadStepName("injector step 1");        
        //am.setTailStepName("injector step 2");        

        String appendPid = steploader.getStepPluginID(am);
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

        String dummyPid1 = steploader.getStepPluginID(dm1);
        StepMeta dummyStep1 = new StepMeta(dummyPid1, dummyStepname1, (StepMetaInterface)dm1);
        transMeta.addStep(dummyStep1);                              

        TransHopMeta hi4 = new TransHopMeta(append, dummyStep1);
        transMeta.addTransHop(hi4);        
        
        // Now execute the transformation...
        Trans trans = new Trans(log, transMeta);

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
        List inputList2 = createData2();
        Iterator it2 = inputList2.iterator();
        while ( it2.hasNext() )
        {
        	Row r = (Row)it2.next();
        	rp2.putRow(r);
        }   
        rp2.finished();        
        
        // add rows to head step
        List inputList1 = createData1();
        Iterator it1 = inputList1.iterator();
        while ( it1.hasNext() )
        {
        	Row r = (Row)it1.next();
        	rp1.putRow(r);
        }   
        rp1.finished();

        trans.waitUntilFinished();   
        
        // The result should be that first all rows from injector 1 and
        // then all rows from injector step 2        
        ArrayList expectedList = new ArrayList();
        expectedList.addAll(inputList1);
        expectedList.addAll(inputList2);
        
        List resultRows1 = dummyRc1.getRowsRead();
        checkRows(resultRows1, expectedList);
    }    
}