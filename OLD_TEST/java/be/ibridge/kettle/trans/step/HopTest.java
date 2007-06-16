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

package be.ibridge.kettle.trans.step;

import java.util.ArrayList;
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

/**
 * Test class for the use of hops, specifically we want to check the
 * copy and distribute mode.
 *
 * @author Sven Boden
 */
public class HopTest extends TestCase
{
	public List createData()
	{
		List list = new ArrayList();

        Row r = new Row();		
		Value value = new Value("field1", 1L);
		r.addValue(value);
		
        Row r1 = new Row();		
		value = new Value("field1", 2L);
		r1.addValue(value);		

        Row r2 = new Row();		
		value = new Value("field1", 3L);
		r2.addValue(value);		

        Row r3 = new Row();		
		value = new Value("field1", 4L);
		r3.addValue(value);		

        Row r4 = new Row();		
		value = new Value("field1", 5L);
		r4.addValue(value);		
		
        Row r5 = new Row();		
		value = new Value("field1", 6L);
		r5.addValue(value);		

        Row r6 = new Row();		
		value = new Value("field1", 7L);
		r6.addValue(value);		
				
		list.add(r);
		list.add(r1);
		list.add(r2);
		list.add(r3);
		list.add(r4);
		list.add(r5);
		list.add(r6);
		
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
	 * Test case for hop using copy.
	 * 
	 * The transformation is as follows: an injector step links to
	 * a dummy step, which in turn links to 2 target dummy steps.
	 * 
	 * Both dummy1 and dummy2 should get all rows.
	 */
    public void testCopyHops() throws Exception
    {
        LogWriter log = LogWriter.getInstance();
        EnvUtil.environmentInit();

        //
        // Create a new transformation...
        //
        TransMeta transMeta = new TransMeta();
        transMeta.setName("hop test default");
    	
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
        // Create a dummy step
        //
        String dummyStepname = "dummy step";            
        DummyTransMeta dm = new DummyTransMeta();

        String dummyPid = steploader.getStepPluginID(dm);
        StepMeta dummyStep = new StepMeta(dummyPid, dummyStepname, (StepMetaInterface)dm);
        transMeta.addStep(dummyStep);                              

        TransHopMeta hi = new TransHopMeta(injectorStep, dummyStep);
        transMeta.addTransHop(hi);
        
        // THIS DETERMINES THE COPY OR DISTRIBUTE BEHAVIOUR
        dummyStep.setDistributes(false);

        // 
        // Create a dummy target step 1
        //
        String dummyStepname1 = "dummy step 1";            
        DummyTransMeta dm1 = new DummyTransMeta();

        String dummyPid1 = steploader.getStepPluginID(dm1);
        StepMeta dummyStep1 = new StepMeta(dummyPid1, dummyStepname1, (StepMetaInterface)dm1);
        transMeta.addStep(dummyStep1);                              

        TransHopMeta hop1 = new TransHopMeta(dummyStep, dummyStep1);
        transMeta.addTransHop(hop1);        

        // 
        // Create a dummy target step 2
        //
        String dummyStepname2 = "dummy step 2";            
        DummyTransMeta dm2 = new DummyTransMeta();

        String dummyPid2 = steploader.getStepPluginID(dm2);
        StepMeta dummyStep2 = new StepMeta(dummyPid2, dummyStepname2, (StepMetaInterface)dm2);
        transMeta.addStep(dummyStep2);                              

        TransHopMeta hop2 = new TransHopMeta(dummyStep, dummyStep2);
        transMeta.addTransHop(hop2);                

        
        // Now execute the transformation...
        Trans trans = new Trans(log, transMeta);

        trans.prepareExecution(null);
                
        StepInterface si1 = trans.getStepInterface(dummyStepname1, 0);
        RowStepCollector rc1 = new RowStepCollector();
        si1.addRowListener(rc1);

        StepInterface si2 = trans.getStepInterface(dummyStepname2, 0);
        RowStepCollector rc2 = new RowStepCollector();
        si2.addRowListener(rc2);

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
        
        List resultRows = rc1.getRowsRead();
        checkRows(resultRows, inputList);
    }
    
	/**
	 * Test case for hop use.
	 * 
	 * The transformation is as follows: an injector step links to
	 * a dummy step, which in turn links to 2 target dummy steps.
	 * 
	 * The default in the GUI of spoon is copy mode, but here it seems to be
	 * distribute.
	 */
    public void testDefaultConfiguration() throws Exception
    {
        LogWriter log = LogWriter.getInstance();
        EnvUtil.environmentInit();

        //
        // Create a new transformation...
        //
        TransMeta transMeta = new TransMeta();
        transMeta.setName("hop test default");
    	
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
        // Create a dummy step
        //
        String dummyStepname = "dummy step";            
        DummyTransMeta dm = new DummyTransMeta();

        String dummyPid = steploader.getStepPluginID(dm);
        StepMeta dummyStep = new StepMeta(dummyPid, dummyStepname, (StepMetaInterface)dm);
        transMeta.addStep(dummyStep);                              

        TransHopMeta hi = new TransHopMeta(injectorStep, dummyStep);
        transMeta.addTransHop(hi);

        // 
        // Create a dummy target step 1
        //
        String dummyStepname1 = "dummy step 1";            
        DummyTransMeta dm1 = new DummyTransMeta();

        String dummyPid1 = steploader.getStepPluginID(dm1);
        StepMeta dummyStep1 = new StepMeta(dummyPid1, dummyStepname1, (StepMetaInterface)dm1);
        transMeta.addStep(dummyStep1);                              

        TransHopMeta hop1 = new TransHopMeta(dummyStep, dummyStep1);
        transMeta.addTransHop(hop1);        

        // 
        // Create a dummy target step 2
        //
        String dummyStepname2 = "dummy step 2";            
        DummyTransMeta dm2 = new DummyTransMeta();

        String dummyPid2 = steploader.getStepPluginID(dm2);
        StepMeta dummyStep2 = new StepMeta(dummyPid2, dummyStepname2, (StepMetaInterface)dm2);
        transMeta.addStep(dummyStep2);                              

        TransHopMeta hop2 = new TransHopMeta(dummyStep, dummyStep2);
        transMeta.addTransHop(hop2);                

        // THIS DETERMINES THE COPY OR DISTRIBUTE BEHAVIOUR
        dummyStep.setDistributes(true);        
        
        // Now execute the transformation...
        Trans trans = new Trans(log, transMeta);

        trans.prepareExecution(null);
                
        StepInterface si1 = trans.getStepInterface(dummyStepname1, 0);
        RowStepCollector rc1 = new RowStepCollector();
        si1.addRowListener(rc1);

        StepInterface si2 = trans.getStepInterface(dummyStepname2, 0);
        RowStepCollector rc2 = new RowStepCollector();
        si2.addRowListener(rc2);
        
                
        RowProducer rp = trans.addRowProducer(injectorStepname, 0);
        trans.startThreads();
        
        // add rows
        List compareList1 = new ArrayList();
        List compareList2 = new ArrayList();
        int counter = 1;
        
        List inputList = createData();
        Iterator it = inputList.iterator();
        while ( it.hasNext() )
        {
        	Row r = (Row)it.next();
        	rp.putRow(r);
        	if ( counter % 2 == 0 )
        	{
        		compareList2.add(r);
        	}
        	else
        	{
        		compareList1.add(r);
        	}
        	counter++;
        }   
        rp.finished();

        trans.waitUntilFinished();   
        
        // Dummy1 should get 4 rows: 1 3 5 7
        // Dummy2 should get 3 rows: 2 4 6          
        
        List resultRows1 = rc1.getRowsRead();
        checkRows(resultRows1, compareList1);
        
        List resultRows2 = rc2.getRowsRead();
        checkRows(resultRows2, compareList2);        
    }
    
	/**
	 * Test case for hop use.
	 * 
	 * The transformation is as follows: an injector step links to
	 * a dummy step, which in turn links to 2 target dummy steps.
	 * 
	 * This testcase uses distribute mode, so each hop in turn should get
	 * a row.
	 */
    public void testDistributeHops() throws Exception
    {
        LogWriter log = LogWriter.getInstance();
        EnvUtil.environmentInit();

        //
        // Create a new transformation...
        //
        TransMeta transMeta = new TransMeta();
        transMeta.setName("hop test default");
    	
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
        // Create a dummy step
        //
        String dummyStepname = "dummy step";            
        DummyTransMeta dm = new DummyTransMeta();

        String dummyPid = steploader.getStepPluginID(dm);
        StepMeta dummyStep = new StepMeta(dummyPid, dummyStepname, (StepMetaInterface)dm);
        transMeta.addStep(dummyStep);                              

        TransHopMeta hi = new TransHopMeta(injectorStep, dummyStep);
        transMeta.addTransHop(hi);

        // 
        // Create a dummy target step 1
        //
        String dummyStepname1 = "dummy step 1";            
        DummyTransMeta dm1 = new DummyTransMeta();

        String dummyPid1 = steploader.getStepPluginID(dm1);
        StepMeta dummyStep1 = new StepMeta(dummyPid1, dummyStepname1, (StepMetaInterface)dm1);
        transMeta.addStep(dummyStep1);                              

        TransHopMeta hop1 = new TransHopMeta(dummyStep, dummyStep1);
        transMeta.addTransHop(hop1);        

        // 
        // Create a dummy target step 2
        //
        String dummyStepname2 = "dummy step 2";            
        DummyTransMeta dm2 = new DummyTransMeta();

        String dummyPid2 = steploader.getStepPluginID(dm2);
        StepMeta dummyStep2 = new StepMeta(dummyPid2, dummyStepname2, (StepMetaInterface)dm2);
        transMeta.addStep(dummyStep2);                              

        TransHopMeta hop2 = new TransHopMeta(dummyStep, dummyStep2);
        transMeta.addTransHop(hop2);                

        // THIS DETERMINES THE COPY OR DISTRIBUTE BEHAVIOUR
        dummyStep.setDistributes(true);        
        
        // Now execute the transformation...
        Trans trans = new Trans(log, transMeta);

        trans.prepareExecution(null);
                
        StepInterface si1 = trans.getStepInterface(dummyStepname1, 0);
        RowStepCollector rc1 = new RowStepCollector();
        si1.addRowListener(rc1);

        StepInterface si2 = trans.getStepInterface(dummyStepname2, 0);
        RowStepCollector rc2 = new RowStepCollector();
        si2.addRowListener(rc2);
        
                
        RowProducer rp = trans.addRowProducer(injectorStepname, 0);
        trans.startThreads();
        
        // add rows
        List compareList1 = new ArrayList();
        List compareList2 = new ArrayList();
        int counter = 1;
        
        List inputList = createData();
        Iterator it = inputList.iterator();
        while ( it.hasNext() )
        {
        	Row r = (Row)it.next();
        	rp.putRow(r);
        	if ( counter % 2 == 0 )
        	{
        		compareList2.add(r);
        	}
        	else
        	{
        		compareList1.add(r);
        	}
        	counter++;
        }   
        rp.finished();

        trans.waitUntilFinished();   
        
        // Dummy1 should get 4 rows: 1 3 5 7
        // Dummy2 should get 3 rows: 2 4 6          
        
        List resultRows1 = rc1.getRowsRead();
        checkRows(resultRows1, compareList1);
        
        List resultRows2 = rc2.getRowsRead();
        checkRows(resultRows2, compareList2);        
    }        
}