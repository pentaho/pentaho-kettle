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

package org.pentaho.di.trans.steps.scriptvalues_mod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.EnvUtil;
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
 * Test class for the Modified Javascript step for the special functions. 
 * Things tested: LuhnCheck().
 *
 * @author Sven Boden
 */
public class JavaScriptSpecialTest extends TestCase
{
	public RowMetaInterface createRowMetaInterface1()
	{
		RowMetaInterface rm = new RowMeta();

		ValueMetaInterface valuesMeta[] = {
			    new ValueMeta("string", ValueMeta.TYPE_STRING),
	    };

		for (int i=0; i < valuesMeta.length; i++ )
		{
			rm.addValueMeta(valuesMeta[i]);
		}

		return rm;
	}

	public List<RowMetaAndData> createData1()
	{
		List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();

		RowMetaInterface rm = createRowMetaInterface1();

		Object[] r1 = new Object[] { "446-667-651" };
		Object[] r2 = new Object[] { "446667651" };
		Object[] r3 = new Object[] { "4444333322221111" };
		Object[] r4 = new Object[] { "4444 3333 2222 1111" };
		Object[] r5 = new Object[] { "444433332aa2221111" };
		Object[] r6 = new Object[] { "4444333322221111aa" };
		
		list.add(new RowMetaAndData(rm, r1));
		list.add(new RowMetaAndData(rm, r2));
		list.add(new RowMetaAndData(rm, r3));
		list.add(new RowMetaAndData(rm, r4));
		list.add(new RowMetaAndData(rm, r5));
		list.add(new RowMetaAndData(rm, r6));

		return list;
	}


	/**
	 * Create the meta data for the results (ltrim/rtrim/trim).
	 */
	public RowMetaInterface createRowMetaInterfaceResult1()
	{
		RowMetaInterface rm = new RowMeta();

		ValueMetaInterface valuesMeta[] = {
			    new ValueMeta("string",   ValueMeta.TYPE_STRING),
			    new ValueMeta("bool",     ValueMeta.TYPE_BOOLEAN)
	    };

		for (int i=0; i < valuesMeta.length; i++ )
		{
			rm.addValueMeta(valuesMeta[i]);
		}

		return rm;
	}

	/**
	 * Create result data for test case 1.
	 */
	public List<RowMetaAndData> createResultData1()
	{
		List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();

		RowMetaInterface rm = createRowMetaInterfaceResult1();

		Object[] r1 = new Object[] { "446-667-651",         Boolean.FALSE };
		Object[] r2 = new Object[] { "446667651",           Boolean.TRUE  };
		Object[] r3 = new Object[] { "4444333322221111",    Boolean.TRUE  };
		Object[] r4 = new Object[] { "4444 3333 2222 1111", Boolean.FALSE };
		Object[] r5 = new Object[] { "444433332aa2221111",  Boolean.FALSE };
		Object[] r6 = new Object[] { "4444333322221111aa",  Boolean.FALSE };

		list.add(new RowMetaAndData(rm, r1));
		list.add(new RowMetaAndData(rm, r2));
		list.add(new RowMetaAndData(rm, r3));
		list.add(new RowMetaAndData(rm, r4));
		list.add(new RowMetaAndData(rm, r5));
		list.add(new RowMetaAndData(rm, r6));

		return list;
	}
	
	public List<RowMetaAndData> createResultData2()
	{
		List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();

		RowMetaInterface rm = createRowMetaInterface1();

		Object[] r1 = new Object[] { "446-667-651" };
		Object[] r2 = new Object[] { "446667651" };
		Object[] r3 = new Object[] { "4444333322221111" };
		
		list.add(new RowMetaAndData(rm, r1));
		list.add(new RowMetaAndData(rm, r2));
		list.add(new RowMetaAndData(rm, r3));

		return list;
	}		

	public RowMetaInterface createRowMetaInterface3()
	{
		RowMetaInterface rm = new RowMeta();

		ValueMetaInterface valuesMeta[] = {
				new ValueMeta("int_in",    ValueMeta.TYPE_INTEGER),
			    new ValueMeta("number_in", ValueMeta.TYPE_NUMBER),
			    new ValueMeta("string_in", ValueMeta.TYPE_STRING),
	    };

		for (int i=0; i < valuesMeta.length; i++ )
		{
			rm.addValueMeta(valuesMeta[i]);
		}

		return rm;
	}

	public List<RowMetaAndData> createData3()
	{
		List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();

		RowMetaInterface rm = createRowMetaInterface3();

		Object[] r1 = new Object[] { new Long(1), new Double(1.0D), "1" };
		Object[] r2 = new Object[] { new Long(2), new Double(2.0D), "2" };
		Object[] r3 = new Object[] { new Long(3), new Double(3.0D), "3" };
		Object[] r4 = new Object[] { new Long(4), new Double(4.0D), "4" };
		
		list.add(new RowMetaAndData(rm, r1));
		list.add(new RowMetaAndData(rm, r2));
		list.add(new RowMetaAndData(rm, r3));
		list.add(new RowMetaAndData(rm, r4));

		return list;
	}	


	public RowMetaInterface createRowMetaInterface4()
	{
		RowMetaInterface rm = new RowMeta();

		ValueMetaInterface valuesMeta[] = {
				new ValueMeta("int_in",    ValueMeta.TYPE_INTEGER),
			    new ValueMeta("number_in", ValueMeta.TYPE_NUMBER),
			    new ValueMeta("string_in", ValueMeta.TYPE_STRING),
				new ValueMeta("long1",     ValueMeta.TYPE_INTEGER),
			    new ValueMeta("number1",   ValueMeta.TYPE_NUMBER),
			    new ValueMeta("string1",   ValueMeta.TYPE_STRING),
				new ValueMeta("long2",     ValueMeta.TYPE_INTEGER),
			    new ValueMeta("number2",   ValueMeta.TYPE_NUMBER),
			    new ValueMeta("string2",   ValueMeta.TYPE_STRING),			    
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

		RowMetaInterface rm = createRowMetaInterface4();

		Object[] r1 = new Object[] { new Long(1), new Double(1.0D), "1", new Long(2), new Double(2.0D), "2", new Long(2), new Double(2.0D), "2" };
		Object[] r2 = new Object[] { new Long(2), new Double(2.0D), "2", new Long(3), new Double(3.0D), "3", new Long(3), new Double(3.0D), "3" };
		Object[] r3 = new Object[] { new Long(3), new Double(3.0D), "3", new Long(4), new Double(4.0D), "4", new Long(4), new Double(4.0D), "4" };
		Object[] r4 = new Object[] { new Long(4), new Double(4.0D), "4", new Long(5), new Double(5.0D), "5", new Long(5), new Double(5.0D), "5" };
		
		list.add(new RowMetaAndData(rm, r1));
		list.add(new RowMetaAndData(rm, r2));
		list.add(new RowMetaAndData(rm, r3));
		list.add(new RowMetaAndData(rm, r4));

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
	 * Test case for javascript functionality: ltrim(), rtrim(), trim().
	 */
    public void testLuhnCheck() throws Exception
    {
        EnvUtil.environmentInit();

        //
        // Create a new transformation...
        //
        TransMeta transMeta = new TransMeta();
        transMeta.setName("test javascript LuhnCheck");

        PluginRegistry registry = PluginRegistry.getInstance();

        //
        // create an injector step...
        //
        String injectorStepname = "injector step";
        InjectorMeta im = new InjectorMeta();

        // Set the information of the injector.
        String injectorPid = registry.getPluginId(StepPluginType.getInstance(), im);
        StepMeta injectorStep = new StepMeta(injectorPid, injectorStepname, (StepMetaInterface)im);
        transMeta.addStep(injectorStep);

        //
        // Create a javascript step
        //
        String javaScriptStepname = "javascript step";
        ScriptValuesMetaMod svm = new ScriptValuesMetaMod();

        ScriptValuesScript[] js = new ScriptValuesScript[] {new ScriptValuesScript(ScriptValuesScript.TRANSFORM_SCRIPT,
        		                                              "script",
                                                              "var str = string;\n" +
                                                              "var bool = LuhnCheck(str);") };
        svm.setJSScripts(js);
        svm.setFieldname(new String[] { "bool" });
        svm.setRename(new String[] { "" });
        svm.setType(new int[] { ValueMetaInterface.TYPE_BOOLEAN });
        svm.setLength(new int[] { -1 });
        svm.setPrecision(new int[] { -1 });
        svm.setReplace(new boolean[] { false });
        svm.setCompatible(false);

        String javaScriptStepPid = registry.getPluginId(StepPluginType.getInstance(), svm);
        StepMeta javaScriptStep = new StepMeta(javaScriptStepPid, javaScriptStepname, (StepMetaInterface)svm);
        transMeta.addStep(javaScriptStep);

        TransHopMeta hi1 = new TransHopMeta(injectorStep, javaScriptStep);
        transMeta.addTransHop(hi1);

        //
        // Create a dummy step
        //
        String dummyStepname = "dummy step";
        DummyTransMeta dm = new DummyTransMeta();

        String dummyPid = registry.getPluginId(StepPluginType.getInstance(), dm);
        StepMeta dummyStep = new StepMeta(dummyPid, dummyStepname, (StepMetaInterface)dm);
        transMeta.addStep(dummyStep);

        TransHopMeta hi2 = new TransHopMeta(javaScriptStep, dummyStep);
        transMeta.addTransHop(hi2);

        // Now execute the transformation...
        Trans trans = new Trans(transMeta);

        trans.prepareExecution(null);

        StepInterface si;

        si = trans.getStepInterface(javaScriptStepname, 0);
        RowStepCollector javaScriptRc = new RowStepCollector();
        si.addRowListener(javaScriptRc);

        si = trans.getStepInterface(dummyStepname, 0);
        RowStepCollector dummyRc = new RowStepCollector();
        si.addRowListener(dummyRc);

        RowProducer rp = trans.addRowProducer(injectorStepname, 0);
        trans.startThreads();

        // add rows
        List<RowMetaAndData> inputList = createData1();
        Iterator<RowMetaAndData> it = inputList.iterator();
        while ( it.hasNext() )
        {
        	RowMetaAndData rm = (RowMetaAndData)it.next();
        	rp.putRow(rm.getRowMeta(), rm.getData());
        }
        rp.finished();

        trans.waitUntilFinished();

        List<RowMetaAndData> goldenImageRows = createResultData1();
        List<RowMetaAndData> resultRows1 = javaScriptRc.getRowsWritten();
        checkRows(resultRows1, goldenImageRows);

        List<RowMetaAndData> resultRows2 = dummyRc.getRowsRead();
        checkRows(resultRows2, goldenImageRows);
    }
    
	/**
	 * Test case for javascript functionality: trans_Status and SKIP_TRANSFORMATION.
	 * Regression test case for JIRA defect PDI-364.
	 */
    public void testTransStatus() throws Exception
    {
        EnvUtil.environmentInit();

        //
        // Create a new transformation...
        //
        TransMeta transMeta = new TransMeta();
        transMeta.setName("test javascript trans_Status");

        PluginRegistry registry = PluginRegistry.getInstance();

        //
        // create an injector step...
        //
        String injectorStepname = "injector step";
        InjectorMeta im = new InjectorMeta();

        // Set the information of the injector.
        String injectorPid = registry.getPluginId(StepPluginType.getInstance(), im);
        StepMeta injectorStep = new StepMeta(injectorPid, injectorStepname, (StepMetaInterface)im);
        transMeta.addStep(injectorStep);

        //
        // Create a javascript step
        //
        String javaScriptStepname = "javascript step";
        ScriptValuesMetaMod svm = new ScriptValuesMetaMod();

        // process 3 rows and skip the rest.
        ScriptValuesScript[] js = new ScriptValuesScript[] {new ScriptValuesScript(ScriptValuesScript.TRANSFORM_SCRIPT,
        		                                              "script",
        		                                              "trans_Status = CONTINUE_TRANSFORMATION;\n" +
        		                                              "if (getProcessCount(\"r\") > 3) {\n" +
        		                                              " 	trans_Status = SKIP_TRANSFORMATION;\n" +
        		                                              "}")};
        svm.setJSScripts(js);
        svm.setFieldname(new String[] {});
        svm.setRename(new String[] { });
        svm.setType(new int[] { });
        svm.setLength(new int[] { });
        svm.setPrecision(new int[] { });
        svm.setCompatible(false);

        String javaScriptStepPid = registry.getPluginId(StepPluginType.getInstance(), svm);
        StepMeta javaScriptStep = new StepMeta(javaScriptStepPid, javaScriptStepname, (StepMetaInterface)svm);
        transMeta.addStep(javaScriptStep);

        TransHopMeta hi1 = new TransHopMeta(injectorStep, javaScriptStep);
        transMeta.addTransHop(hi1);

        //
        // Create a dummy step
        //
        String dummyStepname = "dummy step";
        DummyTransMeta dm = new DummyTransMeta();

        String dummyPid = registry.getPluginId(StepPluginType.getInstance(), dm);
        StepMeta dummyStep = new StepMeta(dummyPid, dummyStepname, (StepMetaInterface)dm);
        transMeta.addStep(dummyStep);

        TransHopMeta hi2 = new TransHopMeta(javaScriptStep, dummyStep);
        transMeta.addTransHop(hi2);

        // Now execute the transformation...
        Trans trans = new Trans(transMeta);

        trans.prepareExecution(null);

        StepInterface si;

        si = trans.getStepInterface(javaScriptStepname, 0);
        RowStepCollector javaScriptRc = new RowStepCollector();
        si.addRowListener(javaScriptRc);

        si = trans.getStepInterface(dummyStepname, 0);
        RowStepCollector dummyRc = new RowStepCollector();
        si.addRowListener(dummyRc);

        RowProducer rp = trans.addRowProducer(injectorStepname, 0);
        trans.startThreads();

        // add rows
        List<RowMetaAndData> inputList = createData1();
        Iterator<RowMetaAndData> it = inputList.iterator();
        while ( it.hasNext() )
        {
        	RowMetaAndData rm = (RowMetaAndData)it.next();
        	rp.putRow(rm.getRowMeta(), rm.getData());
        }
        rp.finished();

        trans.waitUntilFinished();

        List<RowMetaAndData> goldenImageRows = createResultData2();
        List<RowMetaAndData> resultRows1 = javaScriptRc.getRowsWritten();
        checkRows(resultRows1, goldenImageRows);

        List<RowMetaAndData> resultRows2 = dummyRc.getRowsRead();
        checkRows(resultRows2, goldenImageRows);
    }
    
	/**
	 * Test case for JavaScript/Java/JavaScript interfacing.
	 */
    public void testJavaInterface() throws Exception
    {
        EnvUtil.environmentInit();

        //
        // Create a new transformation...
        //
        TransMeta transMeta = new TransMeta();
        transMeta.setName("test javascript interface");

        PluginRegistry registry = PluginRegistry.getInstance();

        //
        // create an injector step...
        //
        String injectorStepname = "injector step";
        InjectorMeta im = new InjectorMeta();

        // Set the information of the injector.
        String injectorPid = registry.getPluginId(StepPluginType.getInstance(), im);
        StepMeta injectorStep = new StepMeta(injectorPid, injectorStepname, (StepMetaInterface)im);
        transMeta.addStep(injectorStep);

        //
        // Create a javascript step
        //
        String javaScriptStepname = "javascript step";
        ScriptValuesMetaMod svm = new ScriptValuesMetaMod();

        // process 3 rows and skip the rest.
        ScriptValuesScript[] js = new ScriptValuesScript[] {new ScriptValuesScript(ScriptValuesScript.TRANSFORM_SCRIPT,
        		                                              "script1",
        		                                              "java;\n\n" + 
        		                                              "var obj     = new Packages.org.pentaho.di.trans.steps.scriptvalues_mod.JavaScriptTest();\n"  +
        		                                              "var long1   = obj.add1ToLong(getInputRowMeta().getInteger(row, 0));\n" +
        		                                              "var number1 = obj.add1ToNumber(getInputRowMeta().getNumber(row, 1));\n" +
        		                                              "var string1 = obj.add1ToString(getInputRowMeta().getString(row, 2));\n" +
        		                                              "var long2   = Packages.org.pentaho.di.trans.steps.scriptvalues_mod.JavaScriptTest.add1ToLongStatic(getInputRowMeta().getInteger(row, 0));\n" +
        		                                              "var number2 = Packages.org.pentaho.di.trans.steps.scriptvalues_mod.JavaScriptTest.add1ToNumberStatic(getInputRowMeta().getNumber(row, 1));\n" +
        		                                              "var string2 = Packages.org.pentaho.di.trans.steps.scriptvalues_mod.JavaScriptTest.add1ToStringStatic(getInputRowMeta().getString(row, 2));\n" 
        		                                              )};
        svm.setJSScripts(js);
        svm.setFieldname(new String[] { "long1", "number1", "string1", "long2", "number2", "string2"});
        svm.setRename(new String[] { "long1", "number1", "string1", "long2", "number2", "string2" });
        svm.setType(new int[] { ValueMeta.TYPE_INTEGER, ValueMeta.TYPE_NUMBER, ValueMeta.TYPE_STRING,
        		                ValueMeta.TYPE_INTEGER, ValueMeta.TYPE_NUMBER, ValueMeta.TYPE_STRING,});
        svm.setLength(new int[] { -1, -1, -1, -1, -1, -1, -1 } );
        svm.setPrecision(new int[] { -1, -1, -1, -1, -1, -1, -1 });
        svm.setReplace(new boolean[] { false, false, false, false, false, false, });
        svm.setCompatible(false);

        String javaScriptStepPid = registry.getPluginId(StepPluginType.getInstance(), svm);
        StepMeta javaScriptStep = new StepMeta(javaScriptStepPid, javaScriptStepname, (StepMetaInterface)svm);
        transMeta.addStep(javaScriptStep);

        TransHopMeta hi1 = new TransHopMeta(injectorStep, javaScriptStep);
        transMeta.addTransHop(hi1);

        //
        // Create a dummy step
        //
        String dummyStepname = "dummy step";
        DummyTransMeta dm = new DummyTransMeta();

        String dummyPid = registry.getPluginId(StepPluginType.getInstance(), dm);
        StepMeta dummyStep = new StepMeta(dummyPid, dummyStepname, (StepMetaInterface)dm);
        transMeta.addStep(dummyStep);

        TransHopMeta hi2 = new TransHopMeta(javaScriptStep, dummyStep);
        transMeta.addTransHop(hi2);

        // Now execute the transformation...
        Trans trans = new Trans(transMeta);

        trans.prepareExecution(null);

        StepInterface si;

        si = trans.getStepInterface(javaScriptStepname, 0);
        RowStepCollector javaScriptRc = new RowStepCollector();
        si.addRowListener(javaScriptRc);

        si = trans.getStepInterface(dummyStepname, 0);
        RowStepCollector dummyRc = new RowStepCollector();
        si.addRowListener(dummyRc);

        RowProducer rp = trans.addRowProducer(injectorStepname, 0);
        trans.startThreads();

        // add rows
        List<RowMetaAndData> inputList = createData3();
        Iterator<RowMetaAndData> it = inputList.iterator();
        while ( it.hasNext() )
        {
        	RowMetaAndData rm = (RowMetaAndData)it.next();
        	rp.putRow(rm.getRowMeta(), rm.getData());
        }
        rp.finished();

        trans.waitUntilFinished();

        List<RowMetaAndData> goldenImageRows = createResultData3();
        List<RowMetaAndData> resultRows1 = javaScriptRc.getRowsWritten();
        checkRows(resultRows1, goldenImageRows);

        List<RowMetaAndData> resultRows2 = dummyRc.getRowsRead();
        checkRows(resultRows2, goldenImageRows);
    }        
}