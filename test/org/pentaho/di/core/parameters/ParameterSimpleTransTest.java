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

package org.pentaho.di.core.parameters;

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
import org.pentaho.di.trans.steps.dummytrans.DummyTransMeta;
import org.pentaho.di.trans.steps.getvariable.GetVariableMeta;


/**
 * Test class for parameters in transformations.
 * 
 * @author Sven Boden
 */
public class ParameterSimpleTransTest extends TestCase
{
	public RowMetaInterface createResultRowMetaInterface1()
	{
		RowMetaInterface rm = new RowMeta();
		
		ValueMetaInterface[] valuesMeta = {			    
			    new ValueMeta("PARAM1", ValueMeta.TYPE_STRING),
			    new ValueMeta("PARAM2", ValueMeta.TYPE_STRING),
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
		
		RowMetaInterface rm = createResultRowMetaInterface1();
		
		Object[] r1  = new Object[] { "ParamValue1", "PARAMVALUE2" };
		
		list.add(new RowMetaAndData(rm, r1));
		
		return list;
	}

	public RowMetaInterface createResultRowMetaInterface2()
	{
		RowMetaInterface rm = new RowMeta();
		
		ValueMetaInterface[] valuesMeta = {			    
			    new ValueMeta("PARAM1", ValueMeta.TYPE_STRING),
			    new ValueMeta("PARAM2", ValueMeta.TYPE_STRING),
	    };

		for (int i=0; i < valuesMeta.length; i++ )
		{
			rm.addValueMeta(valuesMeta[i]);
		}
		
		return rm;
	}
	
	
	public List<RowMetaAndData> createResultData2()
	{
		List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();	
		
		RowMetaInterface rm = createResultRowMetaInterface2();
		
		Object[] r1  = new Object[] { "ParamValue1", "default2" };
		
		list.add(new RowMetaAndData(rm, r1));
		
		return list;
	}

	public RowMetaInterface createResultRowMetaInterface3()
	{
		RowMetaInterface rm = new RowMeta();
		
		ValueMetaInterface[] valuesMeta = {			    
			    new ValueMeta("${JAVA_HOME}", ValueMeta.TYPE_STRING),
			    new ValueMeta("PARAM2",       ValueMeta.TYPE_STRING),
	    };

		for (int i=0; i < valuesMeta.length; i++ )
		{
			rm.addValueMeta(valuesMeta[i]);
		}
		
		return rm;
	}
	
	
	public List<RowMetaAndData> createResultData3()
	{
		List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();	
		
		RowMetaInterface rm = createResultRowMetaInterface3();
		
		Object[] r1  = new Object[] { "${JAVA_HOME}", "default2" };
		
		list.add(new RowMetaAndData(rm, r1));
		
		return list;
	}	

	public RowMetaInterface createResultRowMetaInterface5()
	{
		RowMetaInterface rm = new RowMeta();
		
		ValueMetaInterface[] valuesMeta = {			    
			    new ValueMeta("PARAM1", ValueMeta.TYPE_STRING),
			    new ValueMeta("PARAM2", ValueMeta.TYPE_STRING),
	    };

		for (int i=0; i < valuesMeta.length; i++ )
		{
			rm.addValueMeta(valuesMeta[i]);
		}
		
		return rm;
	}	
	
	public List<RowMetaAndData> createResultData5()
	{
		List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();	
		
		RowMetaInterface rm = createResultRowMetaInterface5();
		
		Object[] r1  = new Object[] { "default1", "PARAMVALUE2" };
		
		list.add(new RowMetaAndData(rm, r1));
		
		return list;
	}	
		
	public RowMetaInterface createResultRowMetaInterface6()
	{
		RowMetaInterface rm = new RowMeta();
		
		ValueMetaInterface[] valuesMeta = {			    
			    new ValueMeta("PARAM1", ValueMeta.TYPE_STRING),
			    new ValueMeta("PARAM2", ValueMeta.TYPE_STRING),
	    };

		for (int i=0; i < valuesMeta.length; i++ )
		{
			rm.addValueMeta(valuesMeta[i]);
		}
		
		return rm;
	}	
	
	public List<RowMetaAndData> createResultData6()
	{
		List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();	
		
		RowMetaInterface rm = createResultRowMetaInterface5();
		
		Object[] r1  = new Object[] { "", "PARAMVALUE2" };
		
		list.add(new RowMetaAndData(rm, r1));
		
		return list;
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
        	int[] fields = new int[rm1.size()];
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
	 * Test case for parameters using a simple transformation.
	 * 
  	 * @throws Exception exception on any problem.
	 */
    public void testParameterSimpleTrans1() throws Exception
    {
        KettleEnvironment.init();

        //
        // Create a new transformation...
        //
        TransMeta transMeta = new TransMeta();
        transMeta.setName("parameter_simple_trans1");
    	
        PluginRegistry registry = PluginRegistry.getInstance();            

        // 
        // create a get variables step...
        //
        String getVariablesStepname = "get variables step";
        GetVariableMeta gvm = new GetVariableMeta();
        
        // Set the information of the get variables step.           
        String getVariablesPid = registry.getPluginId(StepPluginType.class, gvm);
        StepMeta getVariablesStep = new StepMeta(getVariablesPid, getVariablesStepname, gvm);
        transMeta.addStep(getVariablesStep);
        
        //
        // Generate 1 row
        //        
        String[] fieldName  = { "PARAM1", "PARAM2" };
        String[] varName    = { "${Param1}", "%%PARAM2%%" };
        int[] fieldType     = { ValueMeta.TYPE_STRING, ValueMeta.TYPE_STRING };
        int[] length        = { -1, -1 };
        int[] precision     = { -1, -1 };
        String[] format     = { "", "" };
        String[] currency   = { "", "" };
        String[] decimal    = { "", "" };
        String[] grouping   = { "", "" };
        int[] trimType      = { ValueMeta.TRIM_TYPE_NONE, ValueMeta.TRIM_TYPE_NONE };
        
        gvm.setFieldName(fieldName);
        gvm.setVariableString(varName);
        gvm.setFieldType(fieldType);
        gvm.setFieldLength(length);
        gvm.setFieldPrecision(precision);
        gvm.setFieldFormat(format);
        gvm.setCurrency(currency);
        gvm.setDecimal(decimal);    
        gvm.setGroup(grouping);
        gvm.setTrimType(trimType);
        
		// 
		// Create a dummy step 1
		//
		String dummyStepname1 = "dummy step 1";
		DummyTransMeta dm1 = new DummyTransMeta();

		String dummyPid1 = registry.getPluginId(StepPluginType.class, dm1);
		StepMeta dummyStep1 = new StepMeta(dummyPid1, dummyStepname1, dm1);
		transMeta.addStep(dummyStep1);
        
        TransHopMeta hi1 = new TransHopMeta(getVariablesStep, dummyStep1);
        transMeta.addTransHop(hi1);
        
        // Now execute the transformation...
        Trans trans = new Trans(transMeta);
        trans.addParameterDefinition("Param1", "", "Parameter 1");
        trans.addParameterDefinition("PARAM2", "", "Parameter 2");
        trans.setParameterValue("Param1", "ParamValue1");
        trans.setParameterValue("PARAM2", "PARAMVALUE2");        
        
        trans.prepareExecution(null);
                
        StepInterface si = trans.getStepInterface(dummyStepname1, 0);
        RowStepCollector endRc = new RowStepCollector();
        si.addRowListener(endRc);
                       
        trans.startThreads();
        
        trans.waitUntilFinished();   
        
        // Now check whether the output is still as we expect.
        List<RowMetaAndData> goldenImageRows = createResultData1();
        List<RowMetaAndData> resultRows1 = endRc.getRowsWritten();
        checkRows(resultRows1, goldenImageRows);
    }

	/**
	 * Test case for parameters using a simple transformation. Here 1 parameter is not
	 * provided as value, so the default will be used. 
	 * 
	 * @throws Exception exception on any problem.
	 */
    public void testParameterSimpleTrans2() throws Exception
    {
        KettleEnvironment.init();

        //
        // Create a new transformation...
        //
        TransMeta transMeta = new TransMeta();
        transMeta.setName("parameter_simple_trans2");
    	
        PluginRegistry registry = PluginRegistry.getInstance();            

        // 
        // create a get variables step...
        //
        String getVariablesStepname = "get variables step";
        GetVariableMeta gvm = new GetVariableMeta();
        
        // Set the information of the get variables step.           
        String getVariablesPid = registry.getPluginId(StepPluginType.class, gvm);
        StepMeta getVariablesStep = new StepMeta(getVariablesPid, getVariablesStepname, gvm);
        transMeta.addStep(getVariablesStep);
        
        //
        // Generate 1 row
        //        
        String[] fieldName  = { "Param1", "PARAM2" };
        String[] varName    = { "${Param1}", "%%PARAM2%%" };
        int[] fieldType    = { ValueMeta.TYPE_STRING, ValueMeta.TYPE_STRING };
        int[] length        = { -1, -1 };
        int[] precision     = { -1, -1 };
        String[] format     = { "", "" };
        String[] currency   = { "", "" };
        String[] decimal    = { "", "" };
        String[] grouping   = { "", "" };
        int[] trimType      = { ValueMeta.TRIM_TYPE_NONE, ValueMeta.TRIM_TYPE_NONE };        
               
        gvm.setFieldName(fieldName);
        gvm.setVariableString(varName);
        gvm.setFieldType(fieldType);
        gvm.setFieldLength(length);
        gvm.setFieldPrecision(precision);
        gvm.setFieldFormat(format);
        gvm.setCurrency(currency);
        gvm.setDecimal(decimal);    
        gvm.setGroup(grouping);
        gvm.setTrimType(trimType);
               
		// 
		// Create a dummy step 1
		//
		String dummyStepname1 = "dummy step 1";
		DummyTransMeta dm1 = new DummyTransMeta();

		String dummyPid1 = registry.getPluginId(StepPluginType.class, dm1);
		StepMeta dummyStep1 = new StepMeta(dummyPid1, dummyStepname1, dm1);
		transMeta.addStep(dummyStep1);
        
        TransHopMeta hi1 = new TransHopMeta(getVariablesStep, dummyStep1);
        transMeta.addTransHop(hi1);
        
        // Now execute the transformation...
        Trans trans = new Trans(transMeta);
        trans.addParameterDefinition("Param1", "default1", "Parameter 1");
        trans.addParameterDefinition("PARAM2", "default2", "Parameter 2");
        trans.setParameterValue("Param1", "ParamValue1");
        // PARAM2 is not set
        
        trans.prepareExecution(null);
                
        StepInterface si = trans.getStepInterface(dummyStepname1, 0);
        RowStepCollector endRc = new RowStepCollector();
        si.addRowListener(endRc);
                       
        trans.startThreads();
        
        trans.waitUntilFinished();   
        
        // Now check whether the output is still as we expect.
        List<RowMetaAndData> goldenImageRows = createResultData2();
        List<RowMetaAndData> resultRows1 = endRc.getRowsWritten();
        checkRows(resultRows1, goldenImageRows);
    } 
       
	/**
	 * Test case for parameters using a simple transformation. Here blocking some unwise
	 * usage of parameters.
	 * 
	 * @throws Exception exception on any problem.
	 */
    public void testParameterSimpleTrans3() throws Exception
    {
        KettleEnvironment.init();

        //
        // Create a new transformation...
        //
        TransMeta transMeta = new TransMeta();
        transMeta.setName("parameter_simple_trans3");
    	
        PluginRegistry registry = PluginRegistry.getInstance();            

        // 
        // create a get variables step...
        //
        String getVariablesStepname = "get variables step";
        GetVariableMeta gvm = new GetVariableMeta();
        
        // Set the information of the get variables step.           
        String getVariablesPid = registry.getPluginId(StepPluginType.class, gvm);
        StepMeta getVariablesStep = new StepMeta(getVariablesPid, getVariablesStepname, gvm);
        transMeta.addStep(getVariablesStep);
        
        //
        // Generate 1 row
        //        
        String[] fieldName  = { "PARAM1", "PARAM2" };
        String[] varName    = { "${JAVA_HOME}", "%%PARAM2%%" };
        int[] fieldType     = { ValueMeta.TYPE_STRING, ValueMeta.TYPE_STRING };
        int[] length        = { -1, -1 };
        int[] precision     = { -1, -1 };
        String[] format     = { "", "" };
        String[] currency   = { "", "" };
        String[] decimal    = { "", "" };
        String[] grouping   = { "", "" };
        int[] trimType      = { ValueMeta.TRIM_TYPE_NONE, ValueMeta.TRIM_TYPE_NONE };
        
        gvm.setFieldName(fieldName);
        gvm.setVariableString(varName);
        gvm.setFieldType(fieldType);
        gvm.setFieldLength(length);
        gvm.setFieldPrecision(precision);
        gvm.setFieldFormat(format);
        gvm.setCurrency(currency);
        gvm.setDecimal(decimal);    
        gvm.setGroup(grouping);
        gvm.setTrimType(trimType);
        
		// 
		// Create a dummy step 1
		//
		String dummyStepname1 = "dummy step 1";
		DummyTransMeta dm1 = new DummyTransMeta();

		String dummyPid1 = registry.getPluginId(StepPluginType.class, dm1);
		StepMeta dummyStep1 = new StepMeta(dummyPid1, dummyStepname1, dm1);
		transMeta.addStep(dummyStep1);
        
        TransHopMeta hi1 = new TransHopMeta(getVariablesStep, dummyStep1);
        transMeta.addTransHop(hi1);
        
        // Now execute the transformation...
        Trans trans = new Trans(transMeta);
        trans.addParameterDefinition("${JAVA_HOME}", "default1", "Parameter 1");
        trans.addParameterDefinition("PARAM2", "default2", "Parameter 2");
        trans.setParameterValue("${JAVA_HOME}", "param1");
        // PARAM2 is not set
        
        trans.prepareExecution(null);
                
        StepInterface si = trans.getStepInterface(dummyStepname1, 0);
        RowStepCollector endRc = new RowStepCollector();
        si.addRowListener(endRc);
                       
        trans.startThreads();
        
        trans.waitUntilFinished();   
        
        // Now check whether the output is still as we expect.
        List<RowMetaAndData> goldenImageRows = createResultData3();
        List<RowMetaAndData> resultRows1 = endRc.getRowsWritten();
        checkRows(resultRows1, goldenImageRows);
    }
    
	/**
	 * Test case for parameters using a simple transformation. Check whether parameters 
	 * override variables.
	 * 
  	 * @throws Exception exception on any problem.
	 */
    public void testParameterSimpleTrans4() throws Exception
    {
        KettleEnvironment.init();

        //
        // Create a new transformation...
        //
        TransMeta transMeta = new TransMeta();
        transMeta.setName("parameter_simple_trans4");
    	
        PluginRegistry registry = PluginRegistry.getInstance();            

        // 
        // create a get variables step...
        //
        String getVariablesStepname = "get variables step";
        GetVariableMeta gvm = new GetVariableMeta();
        
        // Set the information of the get variables step.           
        String getVariablesPid = registry.getPluginId(StepPluginType.class, gvm);
        StepMeta getVariablesStep = new StepMeta(getVariablesPid, getVariablesStepname, gvm);
        transMeta.addStep(getVariablesStep);
        
        //
        // Generate 1 row
        //        
        String[] fieldName  = { "PARAM1", "PARAM2" };
        String[] varName    = { "${Param1}", "%%PARAM2%%" };
        int[] fieldType     = { ValueMeta.TYPE_STRING, ValueMeta.TYPE_STRING };
        int[] length        = { -1, -1 };
        int[] precision     = { -1, -1 };
        String[] format     = { "", "" };
        String[] currency   = { "", "" };
        String[] decimal    = { "", "" };
        String[] grouping   = { "", "" };
        int[] trimType      = { ValueMeta.TRIM_TYPE_NONE, ValueMeta.TRIM_TYPE_NONE };
        
        gvm.setFieldName(fieldName);
        gvm.setVariableString(varName);
        gvm.setFieldType(fieldType);
        gvm.setFieldLength(length);
        gvm.setFieldPrecision(precision);
        gvm.setFieldFormat(format);
        gvm.setCurrency(currency);
        gvm.setDecimal(decimal);    
        gvm.setGroup(grouping);
        gvm.setTrimType(trimType);
        
		// 
		// Create a dummy step 1
		//
		String dummyStepname1 = "dummy step 1";
		DummyTransMeta dm1 = new DummyTransMeta();

		String dummyPid1 = registry.getPluginId(StepPluginType.class, dm1);
		StepMeta dummyStep1 = new StepMeta(dummyPid1, dummyStepname1, dm1);
		transMeta.addStep(dummyStep1);
        
        TransHopMeta hi1 = new TransHopMeta(getVariablesStep, dummyStep1);
        transMeta.addTransHop(hi1);
        
        // Now execute the transformation...
        Trans trans = new Trans(transMeta);
        trans.addParameterDefinition("Param1", "", "Parameter 1");
        trans.addParameterDefinition("PARAM2", "", "Parameter 2");
        trans.setParameterValue("Param1", "ParamValue1");
        trans.setParameterValue("PARAM2", "PARAMVALUE2");
        
        // See whether this variable overrides the parameter... it should NOT.
        trans.setVariable("Param1", "Variable1");
        
        trans.prepareExecution(null);
                
        StepInterface si = trans.getStepInterface(dummyStepname1, 0);
        RowStepCollector endRc = new RowStepCollector();
        si.addRowListener(endRc);
                       
        trans.startThreads();
        
        trans.waitUntilFinished();   
        
        // Now check whether the output is still as we expect.
        List<RowMetaAndData> goldenImageRows = createResultData1();
        List<RowMetaAndData> resultRows1 = endRc.getRowsWritten();
        checkRows(resultRows1, goldenImageRows);
    }
    
	/**
	 * Test case for parameters using a simple transformation. Check whether parameters 
	 * override variables.
	 * 
  	 * @throws Exception exception on any problem.
	 */
    public void testParameterSimpleTrans5() throws Exception
    {
        KettleEnvironment.init();

        //
        // Create a new transformation...
        //
        TransMeta transMeta = new TransMeta();
        transMeta.setName("parameter_simple_trans4");
    	
        PluginRegistry registry = PluginRegistry.getInstance();            

        // 
        // create a get variables step...
        //
        String getVariablesStepname = "get variables step";
        GetVariableMeta gvm = new GetVariableMeta();
        
        // Set the information of the get variables step.           
        String getVariablesPid = registry.getPluginId(StepPluginType.class, gvm);
        StepMeta getVariablesStep = new StepMeta(getVariablesPid, getVariablesStepname, gvm);
        transMeta.addStep(getVariablesStep);
        
        //
        // Generate 1 row
        //        
        String[] fieldName  = { "PARAM1", "PARAM2" };
        String[] varName    = { "${Param1}", "%%PARAM2%%" };
        int[] fieldType     = { ValueMeta.TYPE_STRING, ValueMeta.TYPE_STRING };
        int[] length        = { -1, -1 };
        int[] precision     = { -1, -1 };
        String[] format     = { "", "" };
        String[] currency   = { "", "" };
        String[] decimal    = { "", "" };
        String[] grouping   = { "", "" };
        int[] trimType      = { ValueMeta.TRIM_TYPE_NONE, ValueMeta.TRIM_TYPE_NONE };
        
        gvm.setFieldName(fieldName);
        gvm.setVariableString(varName);
        gvm.setFieldType(fieldType);
        gvm.setFieldLength(length);
        gvm.setFieldPrecision(precision);
        gvm.setFieldFormat(format);
        gvm.setCurrency(currency);
        gvm.setDecimal(decimal);    
        gvm.setGroup(grouping);
        gvm.setTrimType(trimType);
        
		// 
		// Create a dummy step 1
		//
		String dummyStepname1 = "dummy step 1";
		DummyTransMeta dm1 = new DummyTransMeta();

		String dummyPid1 = registry.getPluginId(StepPluginType.class, dm1);
		StepMeta dummyStep1 = new StepMeta(dummyPid1, dummyStepname1, dm1);
		transMeta.addStep(dummyStep1);
        
        TransHopMeta hi1 = new TransHopMeta(getVariablesStep, dummyStep1);
        transMeta.addTransHop(hi1);
        
        // Now execute the transformation...
        Trans trans = new Trans(transMeta);
        trans.addParameterDefinition("Param1", "default1", "Parameter 1");
        trans.addParameterDefinition("PARAM2", "", "Parameter 2");
        trans.setParameterValue("PARAM2", "PARAMVALUE2");
        
        // See whether this variable overrides the parameter... it should NOT. Param1
        // is defined but not set, so defaults should kick in.
        trans.setVariable("Param1", "Variable1");
        
        trans.prepareExecution(null);
                
        StepInterface si = trans.getStepInterface(dummyStepname1, 0);
        RowStepCollector endRc = new RowStepCollector();
        si.addRowListener(endRc);
                       
        trans.startThreads();
        
        trans.waitUntilFinished();   
        
        // Now check whether the output is still as we expect.
        List<RowMetaAndData> goldenImageRows = createResultData5();
        List<RowMetaAndData> resultRows1 = endRc.getRowsWritten();
        checkRows(resultRows1, goldenImageRows);
    }
    
	/**
	 * Test case for parameters using a simple transformation. Check whether parameters 
	 * override variables.
	 * 
  	 * @throws Exception exception on any problem.
	 */
    public void testParameterSimpleTrans6() throws Exception
    {
        KettleEnvironment.init();

        //
        // Create a new transformation...
        //
        TransMeta transMeta = new TransMeta();
        transMeta.setName("parameter_simple_trans4");
    	
        PluginRegistry registry = PluginRegistry.getInstance();            

        // 
        // create a get variables step...
        //
        String getVariablesStepname = "get variables step";
        GetVariableMeta gvm = new GetVariableMeta();
        
        // Set the information of the get variables step.           
        String getVariablesPid = registry.getPluginId(StepPluginType.class, gvm);
        StepMeta getVariablesStep = new StepMeta(getVariablesPid, getVariablesStepname, gvm);
        transMeta.addStep(getVariablesStep);
        
        //
        // Generate 1 row
        //        
        String[] fieldName  = { "PARAM1", "PARAM2" };
        String[] varName    = { "${Param1}", "%%PARAM2%%" };
        int[] fieldType     = { ValueMeta.TYPE_STRING, ValueMeta.TYPE_STRING };
        int[] length        = { -1, -1 };
        int[] precision     = { -1, -1 };
        String[] format     = { "", "" };
        String[] currency   = { "", "" };
        String[] decimal    = { "", "" };
        String[] grouping   = { "", "" };
        int[] trimType      = { ValueMeta.TRIM_TYPE_NONE, ValueMeta.TRIM_TYPE_NONE };
        
        gvm.setFieldName(fieldName);
        gvm.setVariableString(varName);
        gvm.setFieldType(fieldType);
        gvm.setFieldLength(length);
        gvm.setFieldPrecision(precision);
        gvm.setFieldFormat(format);
        gvm.setCurrency(currency);
        gvm.setDecimal(decimal);    
        gvm.setGroup(grouping);
        gvm.setTrimType(trimType);
        
		// 
		// Create a dummy step 1
		//
		String dummyStepname1 = "dummy step 1";
		DummyTransMeta dm1 = new DummyTransMeta();

		String dummyPid1 = registry.getPluginId(StepPluginType.class, dm1);
		StepMeta dummyStep1 = new StepMeta(dummyPid1, dummyStepname1, dm1);
		transMeta.addStep(dummyStep1);
        
        TransHopMeta hi1 = new TransHopMeta(getVariablesStep, dummyStep1);
        transMeta.addTransHop(hi1);
        
        // Now execute the transformation...
        Trans trans = new Trans(transMeta);
        trans.addParameterDefinition("Param1", "", "Parameter 1");
        trans.addParameterDefinition("PARAM2", "", "Parameter 2");
        trans.setParameterValue("PARAM2", "PARAMVALUE2");
        
        // See whether this variable overrides the parameter... it should NOT. Param1
        // is defined but not set. And no default... so the variable will be set to "". not
        // to "Variable1"
        trans.setVariable("Param1", "Variable1");
        
        trans.prepareExecution(null);
                
        StepInterface si = trans.getStepInterface(dummyStepname1, 0);
        RowStepCollector endRc = new RowStepCollector();
        si.addRowListener(endRc);
                       
        trans.startThreads();
        
        trans.waitUntilFinished();   
        
        // Now check whether the output is still as we expect.
        List<RowMetaAndData> goldenImageRows = createResultData6();
        List<RowMetaAndData> resultRows1 = endRc.getRowsWritten();
        checkRows(resultRows1, goldenImageRows);
    }    
}