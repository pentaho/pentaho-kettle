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

package org.pentaho.di.trans.steps.janino;

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
 * Test class for the Janino step.
 * 
 * @author Slawomir Chodnicki
 */
public class JaninoStepTest extends TestCase
{
	public RowMetaInterface createRowMetaInterface()
	{
		RowMetaInterface rm = new RowMeta();
		
		ValueMetaInterface valuesMeta[] = {
		    new ValueMeta("string",  ValueMeta.TYPE_STRING),
		    new ValueMeta("integer", ValueMeta.TYPE_INTEGER),
		    new ValueMeta("number",  ValueMeta.TYPE_NUMBER),
		    new ValueMeta("bigdecimal",  ValueMeta.TYPE_BIGNUMBER),
		    new ValueMeta("date",  ValueMeta.TYPE_DATE),
		    new ValueMeta("binary",  ValueMeta.TYPE_BINARY),
		    new ValueMeta("bool", ValueMeta.TYPE_BOOLEAN),
	    };
		
		for (int i=0; i < valuesMeta.length; i++ )
		{
			rm.addValueMeta(valuesMeta[i]);
		}
		
		return rm;
	}
	
	public List<RowMetaAndData> createInputList()
	{
		List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();	
		
		RowMetaInterface rm = createRowMetaInterface();
		
		Object[] r1 = new Object[] {
			"string-value",
			new Long(42L),
			new Double(23.0),
			new BigDecimal(11.0),
			new Date(),
			new byte[]{1,2,3,4,5},
			new Boolean(true)
		};
		
		Object[] n = {null, null, null, null, null, null, null};
		
		list.add(new RowMetaAndData(rm, n));
		list.add(new RowMetaAndData(rm, r1));
		list.add(new RowMetaAndData(rm, n));
		list.add(new RowMetaAndData(rm, r1));
		
		return list;					
	}
	
	public List<RowMetaAndData> createExpectedList()
	{
		List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();	
		
		RowMetaInterface rm = createRowMetaInterface();
		
		Object[] r1 = new Object[] {
			"string-value",
			new Long(42L),
			new Double(23.0),
			new BigDecimal(11.0),
			new Date(10000000),
			new byte[]{1,2,3,4,5},
			new Boolean(true)
		};
		
		Object[] n = {null, null, null, null, null, null, null};
		
		list.add(new RowMetaAndData(rm, n));
		list.add(new RowMetaAndData(rm, r1));
		list.add(new RowMetaAndData(rm, n));
		list.add(new RowMetaAndData(rm, r1));
			
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
					fail("row nr " + idx + "i s not equal ("+rm1.getRowMeta().getString(r1)+" != "+rm1.getRowMeta().getString(r2)+")");
				}
			} catch (KettleValueException e) {
				fail("row nr " + idx + " is not equal");
			}
            	
            idx++;
        }
    }
	
   	
	/**
	 * Test case for janino step.
	 */
    public void testJaninoStep() throws Exception
    {
        KettleEnvironment.init();

        //
        // Create a new transformation...
        //
        TransMeta transMeta = new TransMeta();
        transMeta.setName("janino test");
    	
        PluginRegistry registry = PluginRegistry.getInstance();            

		// create an injector step...
		String injectorStepName = "injector step";
		InjectorMeta im = new InjectorMeta();

		// Set the information of the injector.
		String injectorPid = registry.getPluginId(StepPluginType.class, im);
		StepMeta injectorStep = new StepMeta(injectorPid, injectorStepName, (StepMetaInterface) im);
		transMeta.addStep(injectorStep);
        
        // 
        // create a janino step...
        //
        String stepname = "janino";
        JaninoMeta jm = new JaninoMeta();
        
        // Set the information of the step                
        String janinoPid = registry.getPluginId(StepPluginType.class, jm);
        StepMeta janinoStep = new StepMeta(janinoPid, stepname, (StepMetaInterface)jm);
        transMeta.addStep(janinoStep);
        
        jm.setDefault();
        
        JaninoMetaFunction[] formulas = {
       		new JaninoMetaFunction("string", "(string==null)?null:\"string-value\"", ValueMeta.TYPE_STRING, -1, -1, "string"),
       		new JaninoMetaFunction("integer", "(integer==null)?null:new Long(42L)", ValueMeta.TYPE_INTEGER, -1, -1, "integer"),
       		new JaninoMetaFunction("number", "(number==null)?null:new Double(23.0)", ValueMeta.TYPE_NUMBER, -1, -1, "number"),
       		new JaninoMetaFunction("bigdecimal", "(bigdecimal==null)?null:new java.math.BigDecimal(11.0)", ValueMeta.TYPE_BIGNUMBER, -1, -1, "bigdecimal"),
       		new JaninoMetaFunction("date", "(date==null)?null:new java.util.Date(10000000)", ValueMeta.TYPE_DATE, -1, -1, "date"),
       		new JaninoMetaFunction("binary", "(binary==null)?null:new byte[]{1,2,3,4,5}", ValueMeta.TYPE_BINARY, -1, -1, "binary"),
       		new JaninoMetaFunction("bool", "(bool==null)?null:Boolean.TRUE", ValueMeta.TYPE_BOOLEAN, -1, -1, "bool"),
        };
        
        jm.setFormula(formulas);
        
        transMeta.addTransHop(new TransHopMeta(injectorStep, janinoStep));
        
        // 
        // Create a dummy step
        //
        String dummyStepname = "dummy step";            
        DummyTransMeta dm = new DummyTransMeta();

        String dummyPid = registry.getPluginId(StepPluginType.class, dm);
        StepMeta dummyStep = new StepMeta(dummyPid, dummyStepname, (StepMetaInterface)dm);
        transMeta.addStep(dummyStep);                              

        TransHopMeta hi = new TransHopMeta(janinoStep, dummyStep);
        transMeta.addTransHop(hi);
                
        // Now execute the transformation...
        Trans trans = new Trans(transMeta);
        
        trans.prepareExecution(null);
                
        StepInterface si = trans.getStepInterface(dummyStepname, 0);
        RowStepCollector rc = new RowStepCollector();
        si.addRowListener(rc);
        RowProducer rp = trans.addRowProducer(injectorStepName, 0);
        
        trans.startThreads();        

		for (RowMetaAndData rm : createInputList()) {
			rp.putRow(rm.getRowMeta(), rm.getData());
		}
		rp.finished();
        
        trans.waitUntilFinished();   
        
        List<RowMetaAndData> checkList = createExpectedList();
        List<RowMetaAndData> resultRows = rc.getRowsWritten();
        checkRows(resultRows, checkList);
    }
}