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

package org.pentaho.di.trans.steps.constant;

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
import org.pentaho.di.trans.steps.rowgenerator.RowGeneratorMeta;


/**
 * Test class for the Constant step.
 * 
 * @author Sven Boden
 */
public class ConstantTest extends TestCase
{
	public RowMetaInterface createResultRowMetaInterface()
	{
		RowMetaInterface rm = new RowMeta();
		
		ValueMetaInterface valuesMeta[] = {			    
			    new ValueMeta("boolean1",     ValueMeta.TYPE_BOOLEAN),
			    new ValueMeta("boolean2",     ValueMeta.TYPE_BOOLEAN),
			    new ValueMeta("boolean3",     ValueMeta.TYPE_BOOLEAN),
			    new ValueMeta("boolean4",     ValueMeta.TYPE_BOOLEAN),
			    new ValueMeta("boolean5",     ValueMeta.TYPE_BOOLEAN),
			    new ValueMeta("boolean6",     ValueMeta.TYPE_BOOLEAN),
			    new ValueMeta("boolean7",     ValueMeta.TYPE_BOOLEAN),
			    new ValueMeta("string1",      ValueMeta.TYPE_STRING),
			    new ValueMeta("string2",      ValueMeta.TYPE_STRING),
			    new ValueMeta("string3",      ValueMeta.TYPE_STRING),
			    new ValueMeta("integer1",     ValueMeta.TYPE_INTEGER),
			    new ValueMeta("integer2",     ValueMeta.TYPE_INTEGER),
			    new ValueMeta("integer3",     ValueMeta.TYPE_INTEGER),
			    new ValueMeta("integer4",     ValueMeta.TYPE_INTEGER),
			    new ValueMeta("number1",      ValueMeta.TYPE_NUMBER),
			    new ValueMeta("number2",      ValueMeta.TYPE_NUMBER),
			    new ValueMeta("number3",      ValueMeta.TYPE_NUMBER),
			    new ValueMeta("number4",      ValueMeta.TYPE_NUMBER),
	    };

		for (int i=0; i < valuesMeta.length; i++ )
		{
			rm.addValueMeta(valuesMeta[i]);
		}
		
		return rm;
	}
	
	public List<RowMetaAndData> createResultData1()
	{
		List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();	
		
		RowMetaInterface rm = createResultRowMetaInterface();
		
		Object[] r1  = new Object[] { Boolean.TRUE, Boolean.FALSE, Boolean.FALSE,
				                      Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, null,
				                      "AAAAAAAAAAAAAA", "   ", null,
				                      Long.valueOf(-100L), Long.valueOf(0L), Long.valueOf(212L), null,
				                      new Double(-100.2), new Double(0.0), new Double(212.23), null};
		
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
	 * Test case for Constant step. Row generator attached to a constant step.
	 */
    public void testConstant1() throws Exception
    {
        KettleEnvironment.init();

        //
        // Create a new transformation...
        //
        TransMeta transMeta = new TransMeta();
        transMeta.setName("constanttest1");
    	
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
        // Generate 1 empty row
        //
        String fieldName[]   = {  };
        String type[]        = {  };
        String value[]       = {  };
        String fieldFormat[] = {  };
        String group[]       = {  };
        String decimal[]     = {  };
        int    intDummies[]  = {  };
                
        rm.setDefault();
        rm.setFieldName(fieldName);
        rm.setFieldType(type);
        rm.setValue(value);
        rm.setFieldLength(intDummies);
        rm.setFieldPrecision(intDummies);        
        rm.setRowLimit("1");
        rm.setFieldFormat(fieldFormat);
        rm.setGroup(group);
        rm.setDecimal(decimal);        
        
        // 
        // Add constant step.
        //
        String constStepname1 = "constant 1";            
        ConstantMeta cnst1 = new ConstantMeta();
        
        String fieldName1[]   = { "boolean1",
        		                  "boolean2",
        		                  "boolean3",
        		                  "boolean4",
        		                  "boolean5",
        		                  "boolean6",
        		                  "boolean7",
        		                  "string1",
        		                  "string2",
        		                  "string3",
        		                  "integer1",
        		                  "integer2",
        		                  "integer3",
        		                  "integer4",
        		                  "number1",
        		                  "number2",
        		                  "number3",
        		                  "number4",
        		                };
        String type1[]        = { "boolean",
        		                  "Boolean",
        		                  "bOOLEAN",
        		                  "BOOLEAN",
        		                  "boolean",
        		                  "boolean",
        		                  "boolean",
        		                  "string",
        		                  "string",
        		                  "String",
        		                  "integer",
        		                  "integer",
        		                  "integer",
        		                  "integer",
        		                  "number",
        		                  "number",
        		                  "number",
        		                  "number"};
        String value1[]       = { "Y",
        		                  "T",
        		                  "a",
        		                  "TRUE",
        		                  "0",
        		                  "9",
        		                  "",
        		                  "AAAAAAAAAAAAAA",
        		                  "   ",
        		                  "",
        		                  "-100", "0", "212", "",
        		                  "-100.2", "0.0", "212.23", ""};
        String fieldFormat1[] = { "", "", "", "", "", "", "", "", "", "",
        		                  "", "", "", "", "", "", "", ""};
        String group1[]       = { "", "", "", "", "", "", "", "", "", "", 
        		                  "", "", "", "", ",", ",", ",", ","};
        String decimal1[]     = { "", "", "", "", "", "", "", "", "", "",
        		                  "", "", "", "", ".", ".", ".", "."};
        String currency[]     = { "", "", "", "", "", "", "", "", "", "",
                                  "", "", "", "", "", "", "", ""};
        int    intDummies1[]  = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        		                  0, 0, 0, 0, 0, 0, 0, 0};
        
        boolean    setEmptystring[]  = { false, false, false, false, false, false, false, false, false, false,
        		false, false, false, false, false, false, false, false};
        
        
        cnst1.setFieldName(fieldName1);
        cnst1.setFieldType(type1);
        cnst1.setValue(value1);
        cnst1.setFieldLength(intDummies1);
        cnst1.setFieldPrecision(intDummies1);               
        cnst1.setFieldFormat(fieldFormat1);
        cnst1.setGroup(group1);
        cnst1.setDecimal(decimal1);
        cnst1.setCurrency(currency);
        cnst1.setEmptyString(setEmptystring);

        String addSeqPid1 = registry.getPluginId(StepPluginType.class, cnst1);
        StepMeta addSeqStep1 = new StepMeta(addSeqPid1, constStepname1, (StepMetaInterface)cnst1);
        transMeta.addStep(addSeqStep1);                              

        TransHopMeta hi1 = new TransHopMeta(rowGeneratorStep, addSeqStep1);
        transMeta.addTransHop(hi1);

        
        // Now execute the transformation...
        Trans trans = new Trans(transMeta);

        trans.prepareExecution(null);
                
        StepInterface si = trans.getStepInterface(constStepname1, 0);
        RowStepCollector endRc = new RowStepCollector();
        si.addRowListener(endRc);
                       
        trans.startThreads();
        
        trans.waitUntilFinished();   
        
        // Now check whether the output is still as we expect.
        List<RowMetaAndData> goldenImageRows = createResultData1();
        List<RowMetaAndData> resultRows1 = endRc.getRowsWritten();
        checkRows(resultRows1, goldenImageRows);
    }    
}