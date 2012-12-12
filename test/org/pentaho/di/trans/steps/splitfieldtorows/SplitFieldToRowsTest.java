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

package org.pentaho.di.trans.steps.splitfieldtorows;

import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import org.pentaho.di.core.Const;
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
 * Test class for the SplitFieldToRows step.
 * 
 * The expected results were obtained by running the "Split field To Rows" 
 * Kettle step with Spoon 4.3.0.
 * 
 * @author Sean Flatley
 */
public class SplitFieldToRowsTest extends TestCase {
	
	private static final String FIELD_TO_SPLIT_NAME = "FieldToSplit";
	private static final String NEW_FIELD_NAME = "NewFieldName";
	private static final String TEST_OUTPUT_HEADER_FOOTER = 
			"------------------------------------------------------------------------------";
	
	/**
	 * Change this boolean to TRUE to have a more verbose output
	 */
	private static boolean PRINT_RESULTS = false;
	
	/**
	 * Creates the row meta interface.
	 *
	 * @return the row meta interface
	 */
	public RowMetaInterface createRowMetaInterface() {
		RowMetaInterface rowMeta = new RowMeta();
		
		ValueMetaInterface valuesMeta[] = {
	       new ValueMeta(FIELD_TO_SPLIT_NAME,  ValueMeta.TYPE_STRING)
	    };

		for (int i=0; i < valuesMeta.length; i++ ) {
		   rowMeta.addValueMeta(valuesMeta[i]);
		}
		
		return rowMeta;
	}
	
	/**
	 * Create a list of RowMetaAndData from the passed value and returns it.
	 * @param value
	 * @return List<RowMetaAndData>
	 */
	public List<RowMetaAndData> createData(String value) {
		List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();	
		RowMetaInterface rm = createRowMetaInterface();
		Object[] r1 = new Object[] { value };		
		list.add(new RowMetaAndData(rm, r1));
	
		return list;					
	}
	
	/**
	 * Execute a test with the passed parameters.
	 * @param testName
	 * @param stringToSplit
	 * @param isDelimiterRegex
	 * @param delimiter
	 * @param delimiterVariableValue
	 * @param expectedResult
	 * @return
	 * @throws Exception
	 */
    public List<RowMetaAndData> test(String testName, String stringToSplit, boolean isDelimiterRegex, 
    		         String delimiter, String delimiterVariableValue, String[] expectedResult) throws Exception{
    	List<RowMetaAndData> result = splitFieldToRows(testName, stringToSplit, isDelimiterRegex, delimiter, delimiterVariableValue);
    	assertTrue(isSameData(expectedResult, result));
    	return result;
    }
	
	/**
	 * Execute a test with the passed parameters.
	 * @param testName
	 * @param stringToSplit
	 * @param isDelimiterRegex
	 * @param delimiter
	 * @param expectedResult
	 * @return
	 * @throws Exception
	 */
    public List<RowMetaAndData> test(String testName, String stringToSplit, boolean isDelimiterRegex, 
    		         String delimiter, String[] expectedResult) throws Exception{
    	List<RowMetaAndData> result = test(testName, stringToSplit, isDelimiterRegex, delimiter, null, expectedResult);
    	assertTrue(isSameData(expectedResult, result));
    	return result;
    }
	
    //  Non regex tests

    /**
     * Test: Non regex, delimiter = ";" 
     * @throws Exception
     */
    public void testNonRegex1() throws Exception{
    	String[] expectedResult = { "Sean", "Flatley" };
    	test("Non Regex Test 1", "Sean;Flatley", false, ";", expectedResult);
    }
    
    /**
     * Test: Non regex, delimiter = "!;"
     * @throws Exception
     */
    public void testNonRegex2() throws Exception{
    	String[] expectedResult = { "Sean", "Flatley" };
    	test("Non Regex Test 2", "Sean!;Flatley", false, "!;", expectedResult);
    }

    /**
     * Test: Non regex, delimiter = ";", "!" appears in field.
     * @throws Exception
     */
    public void testNonRegex3() throws Exception{
    	String[] expectedResult = { "Sean!", "Flatley" };
    	test("Non Regex Test 3", "Sean!;Flatley", false, ";", expectedResult);
    }
    
    /**
     * Test: Non regex, delimiter = "."
     * @throws Exception
     */
    public void testNonRegex4() throws Exception{
    	String[] expectedResult = { "Sean", "Flatley" };
    	test("Non Regex Test 4", "Sean.Flatley", false, ".", expectedResult);
    }
    
    /**
     * Test: Non regex, delimiter = "\\.", delmiter "." appears in field.
     * @throws Exception
     */
    public void testNonRegex5() throws Exception{
    	String[] expectedResult = { "Sean.Flatley" };
    	test("Non Regex Test 5", "Sean.Flatley", false, "\\.", expectedResult);
    }
    
    /**
     * Test: Non regex, delimiter = "${DELIMITER}, value of delimiter = ";"
     * @throws Exception
     */
    public void testNonRegex6() throws Exception {
    	String[] expectedResult = { "Sean", "Flatley" };
    	test("Non Regex Test 6", "Sean;Flatley", false, "${DELIMITER}", ";", expectedResult);
    }
    
    //  Regex tests
    
    /**
     * Test:  Regex test, delimiter = ";"
     * @throws Exception
     */
    public void testRegex1() throws Exception{
    	String[] expectedResult = { "Sean", "Flatley" };
    	test("Regex Test 1", "Sean;Flatley", true, ";", expectedResult);
    }
    
    /**
     * Test: Regex, delimiter = "!;"
     * @throws Exception
     */
    public void testRegex2() throws Exception{
    	String[] expectedResult = { "Sean", "Flatley" };
    	test("Regex Test 2", "Sean!;Flatley", true, "!;", expectedResult);
    }

    /**
     * Test: Regex, delimiter = ";", "!;" appears in the field. 
     * @throws Exception
     */
    public void testRegex3() throws Exception{
    	String[] expectedResult = { "Sean!", "Flatley" };
    	test("Regex Test 3", "Sean!;Flatley", true, ";", expectedResult);
    }
    
    /**
     * Test: Regex, delimiter = "."
     * @throws Exception
     */
    public void testRegex4() throws Exception{
    	String[] expectedResult = {};
    	test("Regex Test 4", "SeanFlatley", true, ".", expectedResult);
    }
    
    /**
     * Test: Regex, delimiter = "\\." 
     * @throws Exception
     */
    public void testRegex5() throws Exception{
    	String[] expectedResult = {"Sean", "Flatley"};
    	test("Regex Test 5", "Sean.Flatley", true, "\\.", expectedResult);
    }
    
    /**
     * Test: Regex, delimiter = "[0-9]"
     * @throws Exception
     */
    public void testRegex6() throws Exception{
    	String[] expectedResult = {"Sean", "Flatley"};
    	test("Regex Test 6", "Sean1Flatley", true, "[0-9]", expectedResult);
    }
    
    /**
     * Splits the "stringToSplit" with the passed "delimiter".  The "isDelimiterRegex" parameter will process the
     * use regex for pattern matching if true.  
     * @param testName
     * @param stringToSplit
     * @param isDelimiterRegex
     * @param delimiter
     * @return
     * @throws Exception
     */
    public List<RowMetaAndData> splitFieldToRows(String testName, String stringToSplit, 
    		                                     boolean isDelimiterRegex, String delimiter) throws Exception {
    	
    	return splitFieldToRows(testName, stringToSplit, isDelimiterRegex, delimiter, null);
    }
    
    /**
     * Splits the "stringToSplit" with the passed "delimiter".   The "delimiter" is assumed by this method to be a Kettle
     * variable.  The parameter "delimiterVariableValue" should contain the variables value.
     * 
     * The "isDelimiterRegex" parameter will process the use regex for pattern matching if true.  
     *
     * @param testName
     * @param stringToSplit
     * @param isDelimiterRegex
     * @param delimiter
     * @param delimiterVariableValue
     * @return
     * @throws Exception
     */
    public List<RowMetaAndData> splitFieldToRows(String testName, String stringToSplit, 
    		                                     boolean isDelimiterRegex, String delimiter, 
    		                                     String delimiterVariableValue) throws Exception {
        KettleEnvironment.init();

        // Create a new transformation...
        TransMeta transMeta = new TransMeta();
        
        transMeta.setName("Split field to rows test");
        PluginRegistry registry = PluginRegistry.getInstance();      
        
        // create an injector step...
        String injectorStepname = "injector step";
        InjectorMeta im = new InjectorMeta();
        
        // Set the information of the injector.                
        String injectorPid = registry.getPluginId(StepPluginType.class, im);
        StepMeta injectorStep = new StepMeta(injectorPid, injectorStepname, (StepMetaInterface)im);
        transMeta.addStep(injectorStep);

        // Create a Split Field to Rows step
        String splitfieldToRowsName = "Split field to rows";            
        SplitFieldToRowsMeta splitFieldtoRowsMeta = new SplitFieldToRowsMeta();
        splitFieldtoRowsMeta.setDelimiter(delimiter);
        splitFieldtoRowsMeta.setDelimiterRegex(isDelimiterRegex);
        splitFieldtoRowsMeta.setSplitField(FIELD_TO_SPLIT_NAME);
        splitFieldtoRowsMeta.setNewFieldname(NEW_FIELD_NAME);
        
        String splitFieldTotRowsPid = registry.getPluginId(StepPluginType.class, splitFieldtoRowsMeta);
        StepMeta splitFieldToRows = new StepMeta(splitFieldTotRowsPid, splitfieldToRowsName, (StepMetaInterface)splitFieldtoRowsMeta);
        transMeta.addStep(splitFieldToRows);
        
        //  hop the injector to the split field to rows step
        TransHopMeta hop_injector_splitfieldToRows = new TransHopMeta(injectorStep, splitFieldToRows);
        transMeta.addTransHop(hop_injector_splitfieldToRows);    
        
        // Create a dummy step
        String dummyStepname = "dummy step";            
        DummyTransMeta dm = new DummyTransMeta();

        String dummyPid = registry.getPluginId(StepPluginType.class, dm);
        StepMeta dummyStep = new StepMeta(dummyPid, dummyStepname, (StepMetaInterface)dm);
        transMeta.addStep(dummyStep);
        
        TransHopMeta hop_SplitFieldToRows_Dummy = new TransHopMeta(splitFieldToRows, dummyStep);
        transMeta.addTransHop(hop_SplitFieldToRows_Dummy);
                        
        if (!Const.isEmpty(delimiterVariableValue)) {
        	String delimiterVariableName = delimiter.replace("${", "");
        	delimiterVariableName = delimiterVariableName.replace("}", "");
        	transMeta.setVariable(delimiterVariableName, delimiterVariableValue);
        }
        System.out.println(transMeta.environmentSubstitute(delimiter));
        
        // Now execute the transformation...
        Trans trans = new Trans(transMeta);
        trans.prepareExecution(null);
        
        StepInterface si = trans.getStepInterface(dummyStepname, 0);
        RowStepCollector rc = new RowStepCollector();
        si.addRowListener(rc);
        RowProducer rowProducer = trans.addRowProducer(injectorStepname, 0);
        trans.startThreads();
        
        // add rows
        List<RowMetaAndData> inputList = createData(stringToSplit);
        for ( RowMetaAndData rm : inputList ) {
        	rowProducer.putRow(rm.getRowMeta(), rm.getData());
        }   
        rowProducer.finished();    
        trans.waitUntilFinished();   
        
        printTestInfo(testName, stringToSplit, splitFieldtoRowsMeta, transMeta);
        List<RowMetaAndData> resultRows = rc.getRowsWritten();
        if( resultRows != null && resultRows.size() > 0) {
        	print("Rows produced:");
        	for(RowMetaAndData row: resultRows) {
        		String string = row.getString(1, "");
        		print((string==null?"     [null]":"     "+string));
        	}
        }
        else {
        	print("No rows were produced.");
        }
        print(TEST_OUTPUT_HEADER_FOOTER);
        
        return resultRows;
    }
    
    /**
     * Compares the passed parameters and returns truf if they contain the same data.
     * @param expectedData
     * @param result
     * @return
     */
    private boolean isSameData(String[] expectedData, List<RowMetaAndData> result) {
    	for(int i=0; i < expectedData.length; i++) {
    		try {
    			String resultElement = result.get(i).getString("NewFieldName", "String");
    			if( !expectedData[i].equals(resultElement)) {
    				return false;
    			}
    		}
    		catch (ArrayIndexOutOfBoundsException aiobe) {
    			System.out.println(aiobe.getMessage());
    			return false;
    		}
    		catch (KettleValueException kve) {
    			System.out.println(kve.getMessage());
    			return false;
    		}
    	}
    	return true;
    }
       
    /**
     * Prints (more verbose) test information.
     * @param testName
     * @param stringToSplit
     * @param splitFieldToRowsMeta
     * @param transMeta
     */
    private void printTestInfo(String testName, String stringToSplit, SplitFieldToRowsMeta splitFieldToRowsMeta,
    		                   TransMeta transMeta) {
    	print(TEST_OUTPUT_HEADER_FOOTER);
    	print(testName);
    	print("Field to split: "+stringToSplit);
    	if (transMeta == null) {
    		print("Delimiter: "+splitFieldToRowsMeta.getDelimiter());
    	}
    	else {
    		print("Delimiter: "+transMeta.environmentSubstitute(splitFieldToRowsMeta.getDelimiter()));
    	}
    	print(splitFieldToRowsMeta.isDelimiterRegex()?"The delimiter is a regex":"The delimiter is not a regex");
    }
    
    /**
     * Will print the passed "string" if SplitFieldToRowsTest.PRINT_RESULTS is true.
     * @param string
     */
    private void print(String string) {
    	if (SplitFieldToRowsTest.PRINT_RESULTS) {
    		System.out.println(string);
    	}
    }
}