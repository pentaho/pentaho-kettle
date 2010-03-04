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
import java.util.Locale;

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
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.trans.RowProducer;
import org.pentaho.di.trans.RowStepCollector;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransTestFactory;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.dummytrans.DummyTransMeta;
import org.pentaho.di.trans.steps.injector.InjectorMeta;


/**
 * Test class for the Modified Javascript step. Things tested:
 * ltrim(), rtrim(), trim(), lpad(), rpad(), upper(), lower(),
 * isNum(), str2num(), num2str().
 * 
 * Still to do:
 * - Use multiple arguments in str2num/num2str (tests only with 1 argument)
 * - Defined error handling
 *
 * @author Sven Boden
 */
public class JavaScriptStringTest extends TestCase
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
		
		Object[] r1 = new Object[] { null }; 
		Object[] r2 = new Object[] { "" };
		Object[] r3 = new Object[] { "    " };
		Object[] r4 = new Object[] { "small" };
		Object[] r5 = new Object[] { "longer string" };
		Object[] r6 = new Object[] { "spaces right    " };
		Object[] r7 = new Object[] { "   spaces left" };
		Object[] r8 = new Object[] { "   spaces   " };
		
		list.add(new RowMetaAndData(rm, r1));
		list.add(new RowMetaAndData(rm, r2));
		list.add(new RowMetaAndData(rm, r3));
		list.add(new RowMetaAndData(rm, r4));
		list.add(new RowMetaAndData(rm, r5));
		list.add(new RowMetaAndData(rm, r6));
		list.add(new RowMetaAndData(rm, r7));
		list.add(new RowMetaAndData(rm, r8));
		
		return list;
	}

	
	public List<RowMetaAndData> createData2()
	{
		List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();	
		
		RowMetaInterface rm = createRowMetaInterface1();
		
		Object[] r1 = new Object[] { null }; 
		Object[] r2 = new Object[] { "" };
		Object[] r3 = new Object[] { "    " };
		Object[] r4 = new Object[] { "TeSt1" };
		Object[] r5 = new Object[] { "loNgeR st1ing" };
		Object[] r6 = new Object[] { "SPACES RIGHT    " };
		Object[] r7 = new Object[] { "   spacEs lEft" };
		Object[] r8 = new Object[] { "   spaces   " };
		
		list.add(new RowMetaAndData(rm, r1));
		list.add(new RowMetaAndData(rm, r2));
		list.add(new RowMetaAndData(rm, r3));
		list.add(new RowMetaAndData(rm, r4));
		list.add(new RowMetaAndData(rm, r5));
		list.add(new RowMetaAndData(rm, r6));
		list.add(new RowMetaAndData(rm, r7));
		list.add(new RowMetaAndData(rm, r8));
		
		return list;
	}	

	/*
	 * Bug PDI-50 information: Str2num in Javascript steps fails on leading spaces.
	 *                         Fix was to left trim strings in str2num.
	 */
	public List<RowMetaAndData> createData3()
	{
		List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();	
		
		RowMetaInterface rm = createRowMetaInterface1();

		list.add(new RowMetaAndData(rm, new Object[] { "3.5a" }));
		list.add(new RowMetaAndData(rm, new Object[] { "3.a" }));
		list.add(new RowMetaAndData(rm, new Object[] { "2.0" }));
		list.add(new RowMetaAndData(rm, new Object[] { "1.12" }));
		list.add(new RowMetaAndData(rm, new Object[] { "  5.3" }));  /* Data for bug JIRA PDI-50 */
		
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
			    new ValueMeta("original", ValueMeta.TYPE_STRING),
			    new ValueMeta("ltrimStr", ValueMeta.TYPE_STRING),
			    new ValueMeta("rtrimStr", ValueMeta.TYPE_STRING),
			    new ValueMeta("trimStr",  ValueMeta.TYPE_STRING),
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
		
		Object[] r1 = new Object[] { null, "bnulle", "bnulle", "bnulle", "bnulle" };
		Object[] r2 = new Object[] { null, "bnulle", "bnulle", "bnulle", "bnulle" };
		Object[] r3 = new Object[] { "    ",  "b    e", "be", "be", "be"  };
		Object[] r4 = new Object[] { "small", "bsmalle", "bsmalle", "bsmalle", "bsmalle" };	
		Object[] r5 = new Object[] { "longer string", "blonger stringe", "blonger stringe", "blonger stringe", "blonger stringe" };
		Object[] r6 = new Object[] { "spaces right    ", "bspaces right    e", "bspaces right    e", "bspaces righte", "bspaces righte" };
		Object[] r7 = new Object[] { "   spaces left", "b   spaces lefte", "bspaces lefte", "b   spaces lefte", "bspaces lefte" };
		Object[] r8 = new Object[] { "   spaces   ", "b   spaces   e", "bspaces   e", "b   spacese", "bspacese" };
		
		list.add(new RowMetaAndData(rm, r1));
		list.add(new RowMetaAndData(rm, r2));
		list.add(new RowMetaAndData(rm, r3));
		list.add(new RowMetaAndData(rm, r4));
		list.add(new RowMetaAndData(rm, r5));
		list.add(new RowMetaAndData(rm, r6));
		list.add(new RowMetaAndData(rm, r7));
		list.add(new RowMetaAndData(rm, r8));	
		
		return list;
	}	

	/**
	 * Create the meta data for the results (lpad/rpad/upper/lower).
	 */
	public RowMetaInterface createRowMetaInterfaceResult2()
	{
		RowMetaInterface rm = new RowMeta();
	
		ValueMetaInterface valuesMeta[] = {
			    new ValueMeta("string",   ValueMeta.TYPE_STRING),
			    new ValueMeta("lpadded1", ValueMeta.TYPE_STRING),
			    new ValueMeta("lpadded2", ValueMeta.TYPE_STRING),
			    new ValueMeta("rpadded1", ValueMeta.TYPE_STRING),
			    new ValueMeta("rpadded2", ValueMeta.TYPE_STRING),
			    new ValueMeta("upperStr", ValueMeta.TYPE_STRING),
			    new ValueMeta("lowerStr", ValueMeta.TYPE_STRING),
	    };

		for (int i=0; i < valuesMeta.length; i++ )
		{
			rm.addValueMeta(valuesMeta[i]);
		}
		
		return rm;
	}	

	/**
	 * Create the meta data for the results (isnum/num2str/str2num).
	 */
	public RowMetaInterface createRowMetaInterfaceResult3()
	{
		RowMetaInterface rm = new RowMeta();
	
		ValueMetaInterface valuesMeta[] = {
			    new ValueMeta("string",   ValueMeta.TYPE_STRING),
			    new ValueMeta("numb1",    ValueMeta.TYPE_NUMBER),
			    new ValueMeta("bool1",    ValueMeta.TYPE_BOOLEAN),
			    new ValueMeta("str1",     ValueMeta.TYPE_STRING),
	    };

		for (int i=0; i < valuesMeta.length; i++ )
		{
			rm.addValueMeta(valuesMeta[i]);
		}
		
		return rm;
	}	
	
	/**
	 * Create result data for test case 2: lpad/rpad/upper/lower.
	 */
	public List<RowMetaAndData> createResultData2()
	{
		List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();	
		
		RowMetaInterface rm = createRowMetaInterfaceResult2();
		
		Object[] r1 = new Object[] { null, "xxxxxxxxxx", "         ", "xxxxxxxxxx", "         ", "", "" };
		Object[] r2 = new Object[] { null, "xxxxxxxxxx", "         ", "xxxxxxxxxx", "         ", "", "" };
		Object[] r3 = new Object[] { "    ", "xxxxxx    ", "         ", "    xxxxxx", "         ", "    ", "    " };
		Object[] r4 = new Object[] { "TeSt1", "xxxxxTeSt1", "    TeSt1", "TeSt1xxxxx", "TeSt1    ", "TEST1", "test1" };
		Object[] r5 = new Object[] { "loNgeR st1ing", "loNgeR st1ing", "loNgeR st1ing", "loNgeR st1ing", "loNgeR st1ing", "LONGER ST1ING", "longer st1ing" };
		Object[] r6 = new Object[] { "SPACES RIGHT    ", "SPACES RIGHT    ", "SPACES RIGHT    ", "SPACES RIGHT    ", "SPACES RIGHT    ", "SPACES RIGHT    ", "spaces right    " };
		Object[] r7 = new Object[] { "   spacEs lEft", "   spacEs lEft", "   spacEs lEft", "   spacEs lEft", "   spacEs lEft", "   SPACES LEFT", "   spaces left" };
		Object[] r8 = new Object[] { "   spaces   ", "   spaces   ", "   spaces   ", "   spaces   ", "   spaces   ", "   SPACES   ", "   spaces   " };
		
		list.add(new RowMetaAndData(rm, r1));
		list.add(new RowMetaAndData(rm, r2));
		list.add(new RowMetaAndData(rm, r3));
		list.add(new RowMetaAndData(rm, r4));
		list.add(new RowMetaAndData(rm, r5));
		list.add(new RowMetaAndData(rm, r6));
		list.add(new RowMetaAndData(rm, r7));
		list.add(new RowMetaAndData(rm, r8));	
		
		return list;
	}	

	/**
	 * Create result data for test case 3: isNum, num2str, str2num.
	 */
	public List<RowMetaAndData> createResultData3()
	{
		List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();	
		
		RowMetaInterface rm = createRowMetaInterfaceResult3();
		
		Object[] r1 = new Object[] { "3.5a",   3.5D, false, "3.5"  };
		Object[] r2 = new Object[] { "3.a",    3.0D, false, "3"    };
		Object[] r3 = new Object[] { "2.0",    2.0D, true,  "2"    };
		Object[] r4 = new Object[] { "1.12",  1.12D, true,  "1.12" };
		Object[] r5 = new Object[] { "  5.3",  5.3D, true,  "5.3"  };
		
		list.add(new RowMetaAndData(rm, r1));
		list.add(new RowMetaAndData(rm, r2));
		list.add(new RowMetaAndData(rm, r3));
		list.add(new RowMetaAndData(rm, r4));
		list.add(new RowMetaAndData(rm, r5));
		
		return list;
	}		

	public RowMetaInterface createRowMetaInterface2()
	{
		RowMetaInterface rm = new RowMeta();
		
		ValueMetaInterface valuesMeta[] = {
			    new ValueMeta("string",  ValueMeta.TYPE_STRING),
			    new ValueMeta("search",  ValueMeta.TYPE_STRING),
			    new ValueMeta("offset1", ValueMeta.TYPE_INTEGER),
			    new ValueMeta("offset2", ValueMeta.TYPE_INTEGER),
	    };

		for (int i=0; i < valuesMeta.length; i++ )
		{
			rm.addValueMeta(valuesMeta[i]);
		}
		
		return rm;
	}
	
	/*
	 */
	public List<RowMetaAndData> createData4()
	{
		List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();	
		
		RowMetaInterface rm = createRowMetaInterface2();
		
		Object[] r1 = new Object[] { "abcdefgh", "ef", new Long(3), new Long(10) }; 
		Object[] r2 = new Object[] { "abcdefgh", "h", new Long(0), new Long(7)   };
		Object[] r3 = new Object[] { "abcdefgh", "h", new Long(1), new Long(6)   };
		Object[] r4 = new Object[] { "abcdefgh", null, new Long(1), new Long(2)  };
		Object[] r5 = new Object[] { "abcdefgh", "invalid", new Long(1), new Long(1) };
		Object[] r6 = new Object[] { "abcdefgh", "invalidlonger", new Long(1), new Long(1) };
		
		list.add(new RowMetaAndData(rm, r1));
		list.add(new RowMetaAndData(rm, r2));
		list.add(new RowMetaAndData(rm, r3));
		list.add(new RowMetaAndData(rm, r4));
		list.add(new RowMetaAndData(rm, r5));
		list.add(new RowMetaAndData(rm, r6));
		
		return list;
	}		

	/**
	 * Create the meta data for the results (indexOf).
	 */
	public RowMetaInterface createRowMetaInterfaceResult4()
	{
		RowMetaInterface rm = new RowMeta();
	
		ValueMetaInterface valuesMeta[] = {
			    new ValueMeta("string",  ValueMeta.TYPE_STRING),
			    new ValueMeta("search",  ValueMeta.TYPE_STRING),
			    new ValueMeta("offset1", ValueMeta.TYPE_INTEGER),
			    new ValueMeta("offset2", ValueMeta.TYPE_INTEGER),
			    new ValueMeta("index1",  ValueMeta.TYPE_INTEGER),
			    new ValueMeta("index2",  ValueMeta.TYPE_INTEGER),
			    new ValueMeta("index3",  ValueMeta.TYPE_INTEGER),			    
	    };

		for (int i=0; i < valuesMeta.length; i++ )
		{
			rm.addValueMeta(valuesMeta[i]);
		}
		
		return rm;
	}	
	
	
	/**
	 * Create result data for test case 4: indexOf.
	 */
	public List<RowMetaAndData> createResultData4()
	{
		List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();	
		
		RowMetaInterface rm = createRowMetaInterfaceResult4();
		
		Object[] r1 = new Object[] { "abcdefgh", "ef", new Long(3), new Long(10), new Long(4), new Long(4), new Long(-1) }; 
		Object[] r2 = new Object[] { "abcdefgh", "h", new Long(0), new Long(7), new Long(7), new Long(7), new Long(7)   };
		Object[] r3 = new Object[] { "abcdefgh", "h", new Long(1), new Long(6), new Long(7), new Long(7), new Long(7)   };
		Object[] r4 = new Object[] { "abcdefgh", null, new Long(1), new Long(2), new Long(-1), new Long(-1), new Long(-1)  };
		Object[] r5 = new Object[] { "abcdefgh", "invalid", new Long(1), new Long(1), new Long(-1), new Long(-1), new Long(-1) };
		Object[] r6 = new Object[] { "abcdefgh", "invalidlonger", new Long(1), new Long(1), new Long(-1), new Long(-1), new Long(-1) };
		
		list.add(new RowMetaAndData(rm, r1));
		list.add(new RowMetaAndData(rm, r2));
		list.add(new RowMetaAndData(rm, r3));
		list.add(new RowMetaAndData(rm, r4));
		list.add(new RowMetaAndData(rm, r5));
		list.add(new RowMetaAndData(rm, r6));
		
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
    public void testStringsTrim() throws Exception
    {
        KettleEnvironment.init();

        //
        // Create a new transformation...
        //
        TransMeta transMeta = new TransMeta();
        transMeta.setName("test javascript trim");
    	
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
        // Create a javascript step
        //
        String javaScriptStepname = "javascript step";            
        ScriptValuesMetaMod svm = new ScriptValuesMetaMod();
        
        ScriptValuesScript[] js = new ScriptValuesScript[] {new ScriptValuesScript(ScriptValuesScript.TRANSFORM_SCRIPT,
        		                                              "script",
                                                              "var original = 'b' + string.getString() + 'e';\n" +
                                                              "var ltrimStr = 'b' + ltrim(string.getString()) + 'e';\n" +
                                                              "var rtrimStr = 'b' + rtrim(string.getString()) + 'e';\n" +
                                                              "var trimStr  = 'b' + trim(string.getString()) + 'e';\n") };
        svm.setJSScripts(js);
        svm.setFieldname(new String[] { "original", "ltrimStr", "rtrimStr", "trimStr" });
        svm.setRename(new String[] { "", "", "", "" });
        svm.setType(new int[] { ValueMetaInterface.TYPE_STRING,
        		                ValueMetaInterface.TYPE_STRING,
        		                ValueMetaInterface.TYPE_STRING,
        		                ValueMetaInterface.TYPE_STRING});        
        svm.setLength(new int[] { -1, -1, -1, -1 });
        svm.setPrecision(new int[] { -1, -1, -1, -1 });
        svm.setReplace(new boolean[] { false, false, false, false, });
        svm.setCompatible(true);

        String javaScriptStepPid = registry.getPluginId(StepPluginType.class, svm);
        StepMeta javaScriptStep = new StepMeta(javaScriptStepPid, javaScriptStepname, (StepMetaInterface)svm);
        transMeta.addStep(javaScriptStep);            

        TransHopMeta hi1 = new TransHopMeta(injectorStep, javaScriptStep);
        transMeta.addTransHop(hi1);        
        
        // 
        // Create a dummy step 
        //
        String dummyStepname = "dummy step";            
        DummyTransMeta dm = new DummyTransMeta();

        String dummyPid = registry.getPluginId(StepPluginType.class, dm);
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
	 * Test case for javascript functionality: lpad(), rpad(), upper(), lower().
	 */
    public void testStringsPadCase() throws Exception
    {
        KettleEnvironment.init();

        //
        // Create a new transformation...
        //
        TransMeta transMeta = new TransMeta();
        transMeta.setName("test javascript pad casing");
    	
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
        // Create a javascript step
        //
        String javaScriptStepname = "javascript step";            
        ScriptValuesMetaMod svm = new ScriptValuesMetaMod();
        
        ScriptValuesScript[] js = new ScriptValuesScript[] {new ScriptValuesScript(ScriptValuesScript.TRANSFORM_SCRIPT,
        		                                              "script",
        		                                              "var lpadded1 = lpad(string, \"x\", 10);\n" +
        		                                              "var lpadded2 = lpad(string, \" \", 9);\n" +
        		                                              "var rpadded1 = rpad(string, \"x\", 10);\n" +
        		                                              "var rpadded2 = rpad(string, \" \", 9);\n" +
        		                                              "var upperStr = upper(string);\n" +
        		                                              "var lowerStr = lower(string);\n") };
        svm.setJSScripts(js);
        svm.setFieldname(new String[] { "lpadded1", "lpadded2", "rpadded1", "rpadded2", "upperStr", "lowerStr" });
        svm.setRename(new String[] { "", "", "", "", "", "", "" });
        svm.setType(new int[] { ValueMetaInterface.TYPE_STRING,
        		                ValueMetaInterface.TYPE_STRING,
        		                ValueMetaInterface.TYPE_STRING,        		                
        		                ValueMetaInterface.TYPE_STRING,
        		                ValueMetaInterface.TYPE_STRING,        		                
        		                ValueMetaInterface.TYPE_STRING,
        		                ValueMetaInterface.TYPE_STRING});        
        svm.setLength(new int[] { -1, -1, -1, -1, -1, -1, -1 });
        svm.setPrecision(new int[] { -1, -1, -1, -1, -1, -1, -1 });
        svm.setReplace(new boolean[] { false, false, false, false, false, false, false, });
        svm.setCompatible(true);

        String javaScriptStepPid = registry.getPluginId(StepPluginType.class, svm);
        StepMeta javaScriptStep = new StepMeta(javaScriptStepPid, javaScriptStepname, (StepMetaInterface)svm);
        transMeta.addStep(javaScriptStep);            

        TransHopMeta hi1 = new TransHopMeta(injectorStep, javaScriptStep);
        transMeta.addTransHop(hi1);        
        
        // 
        // Create a dummy step 
        //
        String dummyStepname = "dummy step";            
        DummyTransMeta dm = new DummyTransMeta();

        String dummyPid = registry.getPluginId(StepPluginType.class, dm);
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
        List<RowMetaAndData> inputList = createData2();
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
    
    public void testStringsNum() throws Exception
    {
        KettleEnvironment.init();
    	Locale.setDefault(Locale.ENGLISH);
    	
        // 
        // Create a javascript step
        //
        ScriptValuesMetaMod svm = new ScriptValuesMetaMod();
        
        ScriptValuesScript[] js = new ScriptValuesScript[] {new ScriptValuesScript(ScriptValuesScript.TRANSFORM_SCRIPT,
        		                                              "script",
        		                                              "var numb1 = str2num(trim(string.getString()), \"#.#\", \"en\");\n" +
        		                                              "var bool1 = isNum(string.getString());\n" +
        		                                              "var str1  = num2str(numb1);\n") };
        svm.setJSScripts(js);
        svm.setFieldname(new String[] { "numb1", "bool1", "str1" });
        svm.setRename(new String[] { "", "", "" });
        svm.setType(new int[] { ValueMetaInterface.TYPE_NUMBER,
        		                ValueMetaInterface.TYPE_BOOLEAN,
        		                ValueMetaInterface.TYPE_STRING});        
        svm.setLength(new int[] { -1, -1, -1 });
        svm.setPrecision(new int[] { -1, -1, -1 });
        svm.setReplace(new boolean[] { false, false, false, });
        svm.setCompatible(true);

        // Generate a test transformation with an injector and a dummy:
        //
        String testStepname = "javascript";
        TransMeta transMeta = TransTestFactory.generateTestTransformation(new Variables(), svm, testStepname);
        
        // Now execute the transformation and get the result from the dummy step.
        //
        List<RowMetaAndData> result = TransTestFactory.executeTestTransformation(
        		transMeta, 
        		TransTestFactory.INJECTOR_STEPNAME, 
        		testStepname, 
        		TransTestFactory.DUMMY_STEPNAME, 
        		createData3()
        	);

        // Verify that this is what we expected...
        //
        checkRows(result, createResultData3());
    }
    
    
	/**
	 * Test case for javascript functionality: indexOf().
	 */
    public void testIndexOf() throws Exception
    {
        KettleEnvironment.init();

        // 
        // Create a javascript step
        //
        String javaScriptStepname = "javascript step";            
        ScriptValuesMetaMod svm = new ScriptValuesMetaMod();
        
        ScriptValuesScript[] js = new ScriptValuesScript[] {new ScriptValuesScript(ScriptValuesScript.TRANSFORM_SCRIPT,
        		                                              "script",
        		                                              "var index1 = indexOf(string.getString(), search.getString());\n" +
        		                                              "var index2 = indexOf(string.getString(), search.getString(), offset1.getInteger());\n" +
        		                                              "var index3 = indexOf(string.getString(), search.getString(), offset2.getInteger());\n") };
        svm.setJSScripts(js);
        svm.setFieldname(new String[] { "index1", "index2", "index3" });
        svm.setRename(new String[] { "", "", "" });
        svm.setType(new int[] { ValueMetaInterface.TYPE_INTEGER,
        		                ValueMetaInterface.TYPE_INTEGER,
        		                ValueMetaInterface.TYPE_INTEGER});        
        svm.setLength(new int[] { -1, -1, -1 });
        svm.setPrecision(new int[] { -1, -1, -1 });
        svm.setReplace(new boolean[] { false, false, false, });
        svm.setCompatible(true);

        // Generate a test transformation with an injector and a dummy:
        //
        TransMeta transMeta = TransTestFactory.generateTestTransformation(new Variables(), svm, javaScriptStepname);
        
        // Now execute the transformation and get the result from the dummy step.
        //
        List<RowMetaAndData> result = TransTestFactory.executeTestTransformation
        		(
        			transMeta, 
        			TransTestFactory.INJECTOR_STEPNAME, 
        			javaScriptStepname, 
        			TransTestFactory.DUMMY_STEPNAME, 
        			createData4()
        		);

        // Verify that this is what we expected...
        //
        checkRows(result, createResultData4());
    }        
}