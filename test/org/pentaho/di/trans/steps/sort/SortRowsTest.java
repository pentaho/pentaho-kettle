/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.trans.steps.sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import junit.framework.TestCase;

import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.RowMetaAndData;
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
 * Test class for the Sort step.
 * 
 * TODO: ascii data case sensitive and case insensitive.
 *
 * @author Sven Boden
 */
public class SortRowsTest extends TestCase
{
	public static int MAX_COUNT = 1000;
	 
	public RowMetaInterface createRowMetaInterface()
	{
		RowMetaInterface rm = new RowMeta();
		
		ValueMetaInterface valuesMeta[] = {
			    new ValueMeta("KEY1", ValueMeta.TYPE_STRING),
			    new ValueMeta("KEY2", ValueMeta.TYPE_STRING),
	    };

		for (int i=0; i < valuesMeta.length; i++ )
		{
			rm.addValueMeta(valuesMeta[i]);
		}
		
		return rm;
	}
	
	public List<RowMetaAndData> createIntegerData()
	{
		// Create 
		List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();
		String old_key1 = null;
		
		RowMetaInterface rm = createRowMetaInterface();		
		
		Random rand = new Random();		
		for ( int idx = 0; idx < MAX_COUNT; idx++ )
		{
			int key1 = Math.abs(rand.nextInt() % 1000000);
			int key2 = Math.abs(rand.nextInt() % 1000000);

			String key1_string = "" + key1 + "." + idx;
			String key2_string = "" + key2 + "." + idx;
			if ( ((idx % 100) == 0) && old_key1 != null )
			{
				// have duplicate key1's sometimes
			    key1_string = old_key1;
			}
			Object[] r1 = new Object[] { key1_string, key2_string };	
		    list.add(new RowMetaAndData(rm, r1));
		    
		    old_key1 = key1_string;
		}
		return list;
	}
	
	/**
	 *  Check the list, the list has to be sorted. 
	 */
	public void checkRows(List<RowMetaAndData> rows, boolean ascending) throws Exception
	{
		String prev_key1 = null, prev_key2 = null;
		int idx = 0;
		
        for ( RowMetaAndData rm : rows ) {
        	Object[] r1 = rm.getData();
        	RowMetaInterface rmi = rm.getRowMeta();
		
		    String key1 = rmi.getString(r1, "KEY1", "");
		    String key2 = rmi.getString(r1, "KEY2", "");
		   
		    if (prev_key1 != null && prev_key2 != null)
		    { 
		       if ( ascending )
		       {
		    	   if (prev_key1.compareTo(key1) == 0) 
		    	   {
		    		   if ( prev_key2.compareTo(key2) > 0 )
		    		   {
		    			   fail("error in sort");
		    		   }
		    	   }
		    	   else if (prev_key1.compareTo(key1) > 0)
		    	   {
		    		   fail("error in sort");
		    	   }
		       }
		       else
		       {
		    	   if (prev_key1.compareTo(key1) == 0) 
		    	   {
		    		   if ( prev_key2.compareTo(key2) < 0 )
		    		   {
		    			   fail("error in sort");
		    		   }
		    	   }
		    	   else if (prev_key1.compareTo(key1) < 0)
		    	   {
		    		   fail("error in sort");
		    	   }
		       }		       
		   }
		   prev_key1 = key1;
		   prev_key2 = key2;

		   idx++;
  	    }
  	    if (idx != MAX_COUNT)
	    {
	       fail("less rows returned than expected: " + idx);
	    }
	}
	
	
	/**
	 * Test case for sorting step .. ascending order on "numeric" data.
	 */
    public void testSortRows1() throws Exception
    {
        KettleEnvironment.init();

        //
        // Create a new transformation...
        //
        TransMeta transMeta = new TransMeta();
        transMeta.setName("sortrowstest");
    	
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
        // Create a sort rows step
        //
        String sortRowsStepname = "sort rows step";            
        SortRowsMeta srm = new SortRowsMeta();
        srm.setSortSize(Integer.toString(MAX_COUNT/10));
        String [] sortFields = { "KEY1", "KEY2" };
        boolean [] ascendingFields = { true, true };
        boolean [] caseSensitive = { true, true };
        srm.setFieldName(sortFields);
        srm.setAscending(ascendingFields);
        srm.setCaseSensitive(caseSensitive);
        srm.setPrefix("SortRowsTest");
        srm.setDirectory(".");

        String sortRowsStepPid = registry.getPluginId(StepPluginType.class, srm);
        StepMeta sortRowsStep = new StepMeta(sortRowsStepPid, sortRowsStepname, (StepMetaInterface)srm);
        transMeta.addStep(sortRowsStep);            

        TransHopMeta hi = new TransHopMeta(injectorStep, sortRowsStep);
        transMeta.addTransHop(hi);        
        
        // 
        // Create a dummy step
        //
        String dummyStepname = "dummy step";            
        DummyTransMeta dm = new DummyTransMeta();

        String dummyPid = registry.getPluginId(StepPluginType.class, dm);
        StepMeta dummyStep = new StepMeta(dummyPid, dummyStepname, (StepMetaInterface)dm);
        transMeta.addStep(dummyStep);                              

        TransHopMeta hi3 = new TransHopMeta(sortRowsStep, dummyStep);
        transMeta.addTransHop(hi3);        
        
        // Now execute the transformation...
        Trans trans = new Trans(transMeta);

        trans.prepareExecution(null);
                
        StepInterface si = trans.getStepInterface(dummyStepname, 0);
        RowStepCollector dummyRc = new RowStepCollector();
        si.addRowListener(dummyRc);
        
        RowProducer rp = trans.addRowProducer(injectorStepname, 0);
        trans.startThreads();
        
        // add rows
        List<RowMetaAndData> inputList = createIntegerData();
        for ( RowMetaAndData rm : inputList )
        {
        	rp.putRow(rm.getRowMeta(), rm.getData());
        }   
        rp.finished();
 
        trans.waitUntilFinished();   
                                     
        List<RowMetaAndData> resultRows = dummyRc.getRowsWritten();
        checkRows(resultRows, true);
    }
    
	/**
	 * Test case for sorting step .. descending order on "numeric" data.
	 */
    public void testSortRows2() throws Exception
    {
        KettleEnvironment.init();

        //
        // Create a new transformation...
        //
        TransMeta transMeta = new TransMeta();
        transMeta.setName("sortrowstest");
    	
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
        // Create a sort rows step
        //
        String sortRowsStepname = "sort rows step";            
        SortRowsMeta srm = new SortRowsMeta();
        srm.setSortSize(Integer.toString(MAX_COUNT/10));
        String [] sortFields = { "KEY1", "KEY2" };
        boolean [] ascendingFields = { false, false };
        boolean [] caseSensitive = { true, true };
        srm.setFieldName(sortFields);
        srm.setAscending(ascendingFields);
        srm.setCaseSensitive(caseSensitive);
        srm.setPrefix("SortRowsTest");
        srm.setDirectory(".");

        String sortRowsStepPid = registry.getPluginId(StepPluginType.class, srm);
        StepMeta sortRowsStep = new StepMeta(sortRowsStepPid, sortRowsStepname, (StepMetaInterface)srm);
        transMeta.addStep(sortRowsStep);            

        TransHopMeta hi = new TransHopMeta(injectorStep, sortRowsStep);
        transMeta.addTransHop(hi);        
        
        // 
        // Create a dummy step
        //
        String dummyStepname = "dummy step";            
        DummyTransMeta dm = new DummyTransMeta();

        String dummyPid = registry.getPluginId(StepPluginType.class, dm);
        StepMeta dummyStep = new StepMeta(dummyPid, dummyStepname, (StepMetaInterface)dm);
        transMeta.addStep(dummyStep);                              

        TransHopMeta hi3 = new TransHopMeta(sortRowsStep, dummyStep);
        transMeta.addTransHop(hi3);        
        
        // Now execute the transformation...
        Trans trans = new Trans(transMeta);

        trans.prepareExecution(null);
                
        StepInterface si = trans.getStepInterface(dummyStepname, 0);
        RowStepCollector dummyRc = new RowStepCollector();
        si.addRowListener(dummyRc);
        
        RowProducer rp = trans.addRowProducer(injectorStepname, 0);
        trans.startThreads();
        
        // add rows
        List<RowMetaAndData> inputList = createIntegerData();
        for ( RowMetaAndData rm : inputList )
        {
        	rp.putRow(rm.getRowMeta(), rm.getData());
        }   
        rp.finished();
 
        trans.waitUntilFinished();   
                                     
        List<RowMetaAndData> resultRows = dummyRc.getRowsWritten();
        checkRows(resultRows, false);
    }        
}