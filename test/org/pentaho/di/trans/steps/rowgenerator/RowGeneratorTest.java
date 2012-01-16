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

package org.pentaho.di.trans.steps.rowgenerator;

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
import org.pentaho.di.trans.RowStepCollector;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.dummytrans.DummyTransMeta;


/**
 * Test class for the RowGenerator step.
 * 
 * TODO For the moment only the basic stuff is verified. Formats, lengths, precision should best also be tested. 
 *
 * @author Sven Boden
 */
public class RowGeneratorTest extends TestCase
{
	public RowMetaInterface createRowMetaInterface()
	{
		RowMetaInterface rm = new RowMeta();
		
		ValueMetaInterface valuesMeta[] = {
			    new ValueMeta("string",  ValueMeta.TYPE_STRING),
			    new ValueMeta("boolean", ValueMeta.TYPE_BOOLEAN),
			    new ValueMeta("integer", ValueMeta.TYPE_INTEGER),
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
		
		Object[] r1 = new Object[] { "string_value", Boolean.TRUE, 
				                     new Long(20L)};
		
		list.add(new RowMetaAndData(rm, r1));
		list.add(new RowMetaAndData(rm, r1));
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
					fail("row nr " + idx + "is not equal");
				}
			} catch (KettleValueException e) {
				fail("row nr " + idx + "is not equal");
			}
            	
            idx++;
        }
    }
	
   	
	/**
	 * Test case for Row Generator step.
	 */
    public void testRowGenerator() throws Exception
    {
        KettleEnvironment.init();

        //
        // Create a new transformation...
        //
        TransMeta transMeta = new TransMeta();
        transMeta.setName("row generatortest");
    	
        PluginRegistry registry = PluginRegistry.getInstance();            

        // 
        // create a row generator step...
        //
        String rowGeneratorStepname = "row generator step";
        RowGeneratorMeta rm = new RowGeneratorMeta();
        
        // Set the information of the row generator.                
        String rowGeneratorPid = registry.getPluginId(StepPluginType.class, rm);
        StepMeta rowGeneratorStep = new StepMeta(rowGeneratorPid, rowGeneratorStepname, (StepMetaInterface)rm);
        transMeta.addStep(rowGeneratorStep);
        
        //
        // Do the following specs 3 times.
        //
        String fieldName[]   = { "string", "boolean", "integer" };
        String type[]        = { "String", "Boolean", "Integer" };
        String value[]       = { "string_value", "true", "20"   };
        String fieldFormat[] = { "", "", ""  };
        String group[]       = { "", "", ""  };
        String decimal[]     = { "", "", ""  };
        int    intDummies[]  = { -1, -1, -1 };
        boolean    setEmptystring[]  = { false, false, false};
                
        rm.setDefault();
        rm.setFieldName(fieldName);
        rm.setFieldType(type);
        rm.setValue(value);
        rm.setFieldLength(intDummies);
        rm.setFieldPrecision(intDummies);        
        rm.setRowLimit("3");
        rm.setFieldFormat(fieldFormat);
        rm.setGroup(group);
        rm.setDecimal(decimal);
        rm.setEmptyString(setEmptystring);

        // 
        // Create a dummy step
        //
        String dummyStepname = "dummy step";            
        DummyTransMeta dm = new DummyTransMeta();

        String dummyPid = registry.getPluginId(StepPluginType.class, dm);
        StepMeta dummyStep = new StepMeta(dummyPid, dummyStepname, (StepMetaInterface)dm);
        transMeta.addStep(dummyStep);                              

        TransHopMeta hi = new TransHopMeta(rowGeneratorStep, dummyStep);
        transMeta.addTransHop(hi);
                
        // Now execute the transformation...
        Trans trans = new Trans(transMeta);

        trans.prepareExecution(null);
                
        StepInterface si = trans.getStepInterface(dummyStepname, 0);
        RowStepCollector rc = new RowStepCollector();
        si.addRowListener(rc);
        
        trans.startThreads();        
        trans.waitUntilFinished();   
        
        List<RowMetaAndData> checkList = createData();
        List<RowMetaAndData> resultRows = rc.getRowsWritten();
        checkRows(resultRows, checkList);
    }
}