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

package org.pentaho.di.trans.steps.valuemapper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
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
 * Test class for the ValueMapper step.
 *
 * @author Sven Boden
 */
public class ValueMapperTest extends TestCase
{
	public RowMetaInterface createRowMetaInterface()
	{
		RowMetaInterface rm = new RowMeta();
		
		ValueMetaInterface valuesMeta[] = {
			    new ValueMeta("field1", ValueMeta.TYPE_STRING),
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
		
		Object[] r1 = new Object[] { "abc" };
		Object[] r2 = new Object[] { "ABC" };
		Object[] r3 = new Object[] { "def" };
		Object[] r4 = new Object[] { "def" };
		Object[] r5 = new Object[] { "ghij" };
		Object[] r6 = new Object[] { "zzz" };
		Object[] r7 = new Object[] { "" };
		Object[] r8 = new Object[] { null };
		Object[] r9 = new Object[] { "abc" };
		
		list.add(new RowMetaAndData(rm, r1));
		list.add(new RowMetaAndData(rm, r2));
		list.add(new RowMetaAndData(rm, r3));
		list.add(new RowMetaAndData(rm, r4));
		list.add(new RowMetaAndData(rm, r5));
		list.add(new RowMetaAndData(rm, r6));
		list.add(new RowMetaAndData(rm, r7));
		list.add(new RowMetaAndData(rm, r8));
		list.add(new RowMetaAndData(rm, r9));
		
		return list;
	}

	/**
	 * Create the meta data for the results of test case 1.
	 */
	public RowMetaInterface createRowMetaInterface1()
	{
		RowMetaInterface rm = new RowMeta();
		
		ValueMetaInterface valuesMeta[] = {
			    new ValueMeta("field1",    ValueMeta.TYPE_STRING),
			    new ValueMeta("new_field", ValueMeta.TYPE_STRING),
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
		
		RowMetaInterface rm = createRowMetaInterface1();
		
		Object[] r1 = new Object[] { "abc",  "begin"      };
		Object[] r2 = new Object[] { "ABC",  "BEG_LONGER" };
		Object[] r3 = new Object[] { "def",  "test"       };
		Object[] r4 = new Object[] { "def",  "test"       };
		Object[] r5 = new Object[] { "ghij", null         };
		Object[] r6 = new Object[] { "zzz",  null         };
		Object[] r7 = new Object[] { "",     null         };
		Object[] r8 = new Object[] { null,   null         };
		Object[] r9 = new Object[] { "abc",  "begin"      };
		
		list.add(new RowMetaAndData(rm, r1));
		list.add(new RowMetaAndData(rm, r2));
		list.add(new RowMetaAndData(rm, r3));
		list.add(new RowMetaAndData(rm, r4));
		list.add(new RowMetaAndData(rm, r5));
		list.add(new RowMetaAndData(rm, r6));
		list.add(new RowMetaAndData(rm, r7));
		list.add(new RowMetaAndData(rm, r8));
		list.add(new RowMetaAndData(rm, r9));
		
		return list;
	}	

	/**
	 * Create result data for test case 2.
	 */
	public List<RowMetaAndData> createResultData2()
	{
		List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();	
		
		RowMetaInterface rm = createRowMetaInterface();
		
		Object[] r1 = new Object[] { "begin"      };
		Object[] r2 = new Object[] { "BEG_LONGER" };
		Object[] r3 = new Object[] { "test"       };
		Object[] r4 = new Object[] { "test"       };
		Object[] r5 = new Object[] { null         };
		Object[] r6 = new Object[] { "zzz"        };
		Object[] r7 = new Object[] { null         };
		Object[] r8 = new Object[] { null         };
		Object[] r9 = new Object[] { "begin"      };
		
		list.add(new RowMetaAndData(rm, r1));
		list.add(new RowMetaAndData(rm, r2));
		list.add(new RowMetaAndData(rm, r3));
		list.add(new RowMetaAndData(rm, r4));
		list.add(new RowMetaAndData(rm, r5));
		list.add(new RowMetaAndData(rm, r6));
		list.add(new RowMetaAndData(rm, r7));
		list.add(new RowMetaAndData(rm, r8));
		list.add(new RowMetaAndData(rm, r9));
		
		return list;
	}		

	/**
	 * Create result data for test case 3.
	 */
	public List<RowMetaAndData> createResultData3()
	{
		List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();	
		
		RowMetaInterface rm = createRowMetaInterface();
		
		Object[] r1 = new Object[] { "begin"      };
		Object[] r2 = new Object[] { "BEG_LONGER" };
		Object[] r3 = new Object[] { "test"       };
		Object[] r4 = new Object[] { "test"       };
		Object[] r5 = new Object[] { null         };
		Object[] r6 = new Object[] { "zzz"        };
		Object[] r7 = new Object[] { "emptyField" };
		Object[] r8 = new Object[] { "emptyField" };
		Object[] r9 = new Object[] { "begin"      };
		
		list.add(new RowMetaAndData(rm, r1));
		list.add(new RowMetaAndData(rm, r2));
		list.add(new RowMetaAndData(rm, r3));
		list.add(new RowMetaAndData(rm, r4));
		list.add(new RowMetaAndData(rm, r5));
		list.add(new RowMetaAndData(rm, r6));
		list.add(new RowMetaAndData(rm, r7));
		list.add(new RowMetaAndData(rm, r8));
		list.add(new RowMetaAndData(rm, r9));
		
		return list;
	}		

	/**
	 * Create result data for test case 4.
	 */
	public List<RowMetaAndData> createResultData4()
	{
		List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();	
		
		RowMetaInterface rm = createRowMetaInterface1();
		
		Object[] r1 = new Object[] { "abc",  "begin"      };
		Object[] r2 = new Object[] { "ABC",  "BEG_LONGER" };
		Object[] r3 = new Object[] { "def",  "test"       };
		Object[] r4 = new Object[] { "def",  "test"       };
		Object[] r5 = new Object[] { "ghij", null         };
		Object[] r6 = new Object[] { "zzz",  "default"    };
		Object[] r7 = new Object[] { "",     null         };
		Object[] r8 = new Object[] { null,   null         };
		Object[] r9 = new Object[] { "abc",  "begin"      };
		
		list.add(new RowMetaAndData(rm, r1));
		list.add(new RowMetaAndData(rm, r2));
		list.add(new RowMetaAndData(rm, r3));
		list.add(new RowMetaAndData(rm, r4));
		list.add(new RowMetaAndData(rm, r5));
		list.add(new RowMetaAndData(rm, r6));
		list.add(new RowMetaAndData(rm, r7));
		list.add(new RowMetaAndData(rm, r8));
		list.add(new RowMetaAndData(rm, r9));
		
		return list;
	}		

	/**
	 * Create result data for test case 5.
	 */
	public List<RowMetaAndData> createResultData5()
	{
		List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();	
		
		RowMetaInterface rm = createRowMetaInterface();
		
		Object[] r1 = new Object[] { "begin"      };
		Object[] r2 = new Object[] { "BEG_LONGER" };
		Object[] r3 = new Object[] { "test"       };
		Object[] r4 = new Object[] { "test"       };
		Object[] r5 = new Object[] { null         };
		Object[] r6 = new Object[] { "default"    };
		Object[] r7 = new Object[] { null         };
		Object[] r8 = new Object[] { null         };
		Object[] r9 = new Object[] { "begin"      };
		
		list.add(new RowMetaAndData(rm, r1));
		list.add(new RowMetaAndData(rm, r2));
		list.add(new RowMetaAndData(rm, r3));
		list.add(new RowMetaAndData(rm, r4));
		list.add(new RowMetaAndData(rm, r5));
		list.add(new RowMetaAndData(rm, r6));
		list.add(new RowMetaAndData(rm, r7));
		list.add(new RowMetaAndData(rm, r8));
		list.add(new RowMetaAndData(rm, r9));
		
		return list;
	}			
	
	/**
	 * Create result data for test case 6.
	 */
	public List<RowMetaAndData> createResultData6()
	{
		List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();	
		
		RowMetaInterface rm = createRowMetaInterface();
		
		Object[] r1 = new Object[] { "begin"      };
		Object[] r2 = new Object[] { "BEG_LONGER" };
		Object[] r3 = new Object[] { "test"       };
		Object[] r4 = new Object[] { "test"       };
		Object[] r5 = new Object[] { "default"    };
		Object[] r6 = new Object[] { "default"    };
		Object[] r7 = new Object[] { "emptyField" };
		Object[] r8 = new Object[] { "emptyField" };
		Object[] r9 = new Object[] { "begin"      };
		
		list.add(new RowMetaAndData(rm, r1));
		list.add(new RowMetaAndData(rm, r2));
		list.add(new RowMetaAndData(rm, r3));
		list.add(new RowMetaAndData(rm, r4));
		list.add(new RowMetaAndData(rm, r5));
		list.add(new RowMetaAndData(rm, r6));
		list.add(new RowMetaAndData(rm, r7));
		list.add(new RowMetaAndData(rm, r8));
		list.add(new RowMetaAndData(rm, r9));
		
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
        	int fields[] = new int[r1.length];
        	for ( int ydx = 0; ydx < r1.length; ydx++ )
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
	 * Test case for valuemapper step. Injector step to a valuemapper step
	 * to a dummy step. Rows go in and should be mapped accordingly.
	 * 
	 * This test will write the mappings in a new field.
	 */
    public void testValueMapper1() throws Exception
    {
        KettleEnvironment.init();

        //
        // Create a new transformation...
        //
        TransMeta transMeta = new TransMeta();
        transMeta.setName("valuemappertest1");
    	
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
        // Create a dummy step 1
        //
        String dummyStepname1 = "dummy step 1";            
        DummyTransMeta dm1 = new DummyTransMeta();

        String dummyPid1 = registry.getPluginId(StepPluginType.class, dm1);
        StepMeta dummyStep1 = new StepMeta(dummyPid1, dummyStepname1, (StepMetaInterface)dm1);
        transMeta.addStep(dummyStep1);                              

        TransHopMeta hi = new TransHopMeta(injectorStep, dummyStep1);
        transMeta.addTransHop(hi);

        // 
        // Create a ValueMapper step
        //
        String valueMapperName = "valuemapper step";            
        ValueMapperMeta vm = new ValueMapperMeta();
        
        vm.setFieldToUse("field1");
        vm.setTargetField("new_field");
        vm.setSourceValue(new String[] {"abc", "ABC", "def", "ghij"});
        vm.setTargetValue(new String[] {"begin", "BEG_LONGER", "test", ""});

        String valueMapperPid = registry.getPluginId(StepPluginType.class, vm);
        StepMeta valueMapperStep = new StepMeta(valueMapperPid, valueMapperName, (StepMetaInterface)vm);
        transMeta.addStep(valueMapperStep);                              

        TransHopMeta hi2 = new TransHopMeta(dummyStep1, valueMapperStep);
        transMeta.addTransHop(hi2);        
        
        // 
        // Create a dummy step 2
        //
        String dummyStepname2 = "dummy step 2";            
        DummyTransMeta dm2 = new DummyTransMeta();

        String dummyPid2 = registry.getPluginId(StepPluginType.class, dm2);
        StepMeta dummyStep2 = new StepMeta(dummyPid2, dummyStepname2, (StepMetaInterface)dm2);
        transMeta.addStep(dummyStep2);                              

        TransHopMeta hi3 = new TransHopMeta(valueMapperStep, dummyStep2);
        transMeta.addTransHop(hi3);        
        
                
        // Now execute the transformation...
        Trans trans = new Trans(transMeta);

        trans.prepareExecution(null);
                
        StepInterface si = trans.getStepInterface(dummyStepname1, 0);
        RowStepCollector dummyRc1 = new RowStepCollector();
        si.addRowListener(dummyRc1);

        si = trans.getStepInterface(valueMapperName, 0);
        RowStepCollector valueMapperRc = new RowStepCollector();
        si.addRowListener(valueMapperRc);
               
        si = trans.getStepInterface(dummyStepname2, 0);
        RowStepCollector dummyRc = new RowStepCollector();
        si.addRowListener(dummyRc);
        
        RowProducer rp = trans.addRowProducer(injectorStepname, 0);
        trans.startThreads();
        
        // add rows
        List<RowMetaAndData> inputList = createData();
        Iterator<RowMetaAndData> it = inputList.iterator();
        while ( it.hasNext() )
        {
        	RowMetaAndData rm = it.next();
        	rp.putRow(rm.getRowMeta(), rm.getData());
        }   
        rp.finished();

        trans.waitUntilFinished();   
        
        // Compare the results                        
        List<RowMetaAndData> resultRows = dummyRc.getRowsWritten();
        List<RowMetaAndData> goldenImageRows = createResultData1();
        
        checkRows(goldenImageRows, resultRows);
    }
    
	/**
	 * Test case for valuemapper step. Injector step to a valuemapper step
	 * to a dummy step. Rows go in and should be mapped accordingly.
	 * 
	 * This test will write the mappings in the same field.
	 */
    public void testValueMapper2() throws Exception
    {
        KettleEnvironment.init();

        //
        // Create a new transformation...
        //
        TransMeta transMeta = new TransMeta();
        transMeta.setName("valuemappertest2");
    	
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
        // Create a dummy step 1
        //
        String dummyStepname1 = "dummy step 1";            
        DummyTransMeta dm1 = new DummyTransMeta();

        String dummyPid1 = registry.getPluginId(StepPluginType.class, dm1);
        StepMeta dummyStep1 = new StepMeta(dummyPid1, dummyStepname1, (StepMetaInterface)dm1);
        transMeta.addStep(dummyStep1);                              

        TransHopMeta hi = new TransHopMeta(injectorStep, dummyStep1);
        transMeta.addTransHop(hi);

        // 
        // Create a  step
        //
        String valueMapperName = "valuemapper step";            
        ValueMapperMeta vm = new ValueMapperMeta();
        
        vm.setFieldToUse("field1");
        vm.setTargetField("");
        vm.setSourceValue(new String[] {"abc", "ABC", "def", "ghij"});
        vm.setTargetValue(new String[] {"begin", "BEG_LONGER", "test", ""});

        String valueMapperPid = registry.getPluginId(StepPluginType.class, vm);
        StepMeta valueMapperStep = new StepMeta(valueMapperPid, valueMapperName, (StepMetaInterface)vm);
        transMeta.addStep(valueMapperStep);                              

        TransHopMeta hi2 = new TransHopMeta(dummyStep1, valueMapperStep);
        transMeta.addTransHop(hi2);        
        
        // 
        // Create a dummy step 2
        //
        String dummyStepname2 = "dummy step 2";            
        DummyTransMeta dm2 = new DummyTransMeta();

        String dummyPid2 = registry.getPluginId(StepPluginType.class, dm2);
        StepMeta dummyStep2 = new StepMeta(dummyPid2, dummyStepname2, (StepMetaInterface)dm2);
        transMeta.addStep(dummyStep2);                              

        TransHopMeta hi3 = new TransHopMeta(valueMapperStep, dummyStep2);
        transMeta.addTransHop(hi3);        
        
                
        // Now execute the transformation...
        Trans trans = new Trans(transMeta);

        trans.prepareExecution(null);
                
        StepInterface si = trans.getStepInterface(dummyStepname1, 0);
        RowStepCollector dummyRc1 = new RowStepCollector();
        si.addRowListener(dummyRc1);

        si = trans.getStepInterface(valueMapperName, 0);
        RowStepCollector valueMapperRc = new RowStepCollector();
        si.addRowListener(valueMapperRc);
               
        si = trans.getStepInterface(dummyStepname2, 0);
        RowStepCollector dummyRc = new RowStepCollector();
        si.addRowListener(dummyRc);
        
        RowProducer rp = trans.addRowProducer(injectorStepname, 0);
        trans.startThreads();
        
        // add rows
        List<RowMetaAndData> inputList = createData();
        Iterator<RowMetaAndData> it = inputList.iterator();
        while ( it.hasNext() )
        {
        	RowMetaAndData rm = it.next();
        	rp.putRow(rm.getRowMeta(), rm.getData());
        }   
        rp.finished();

        trans.waitUntilFinished();   
        
        // Compare the results                        
        List<RowMetaAndData> resultRows = dummyRc.getRowsWritten();
        List<RowMetaAndData> goldenImageRows = createResultData2();
        
        checkRows(goldenImageRows, resultRows);
    }
    
	/**
	 * Test case for valuemapper step. Injector step to a valuemapper step
	 * to a dummy step. Rows go in and should be mapped accordingly.
	 * 
	 * This test will explicitly test the empty field processing.
	 */
    public void testValueMapper3() throws Exception
    {
        KettleEnvironment.init();

        //
        // Create a new transformation...
        //
        TransMeta transMeta = new TransMeta();
        transMeta.setName("valuemappertest3");
    	
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
        // Create a dummy step 1
        //
        String dummyStepname1 = "dummy step 1";            
        DummyTransMeta dm1 = new DummyTransMeta();

        String dummyPid1 = registry.getPluginId(StepPluginType.class, dm1);
        StepMeta dummyStep1 = new StepMeta(dummyPid1, dummyStepname1, (StepMetaInterface)dm1);
        transMeta.addStep(dummyStep1);                              

        TransHopMeta hi = new TransHopMeta(injectorStep, dummyStep1);
        transMeta.addTransHop(hi);

        // 
        // Create a  step
        //
        String valueMapperName = "valuemapper step";            
        ValueMapperMeta vm = new ValueMapperMeta();
        
        vm.setFieldToUse("field1");
        vm.setTargetField("");
        vm.setSourceValue(new String[] {"abc", "ABC", "def", "ghij", null});
        vm.setTargetValue(new String[] {"begin", "BEG_LONGER", "test", "", "emptyField"});

        String valueMapperPid = registry.getPluginId(StepPluginType.class, vm);
        StepMeta valueMapperStep = new StepMeta(valueMapperPid, valueMapperName, (StepMetaInterface)vm);
        transMeta.addStep(valueMapperStep);                              

        TransHopMeta hi2 = new TransHopMeta(dummyStep1, valueMapperStep);
        transMeta.addTransHop(hi2);        
        
        // 
        // Create a dummy step 2
        //
        String dummyStepname2 = "dummy step 2";            
        DummyTransMeta dm2 = new DummyTransMeta();

        String dummyPid2 = registry.getPluginId(StepPluginType.class, dm2);
        StepMeta dummyStep2 = new StepMeta(dummyPid2, dummyStepname2, (StepMetaInterface)dm2);
        transMeta.addStep(dummyStep2);                              

        TransHopMeta hi3 = new TransHopMeta(valueMapperStep, dummyStep2);
        transMeta.addTransHop(hi3);        
        
                
        // Now execute the transformation...
        Trans trans = new Trans(transMeta);

        trans.prepareExecution(null);
                
        StepInterface si = trans.getStepInterface(dummyStepname1, 0);
        RowStepCollector dummyRc1 = new RowStepCollector();
        si.addRowListener(dummyRc1);

        si = trans.getStepInterface(valueMapperName, 0);
        RowStepCollector valueMapperRc = new RowStepCollector();
        si.addRowListener(valueMapperRc);
               
        si = trans.getStepInterface(dummyStepname2, 0);
        RowStepCollector dummyRc = new RowStepCollector();
        si.addRowListener(dummyRc);
        
        RowProducer rp = trans.addRowProducer(injectorStepname, 0);
        trans.startThreads();
        
        // add rows
        List<RowMetaAndData> inputList = createData();
        Iterator<RowMetaAndData> it = inputList.iterator();
        while ( it.hasNext() )
        {
        	RowMetaAndData rm = it.next();
        	rp.putRow(rm.getRowMeta(), rm.getData());
        }   
        rp.finished();

        trans.waitUntilFinished();   
        
        // Compare the results                        
        List<RowMetaAndData> resultRows = dummyRc.getRowsWritten();
        List<RowMetaAndData> goldenImageRows = createResultData3();
        
        checkRows(goldenImageRows, resultRows);
    }
    /*----------------- TODO */
	/**
	 * Test case for valuemapper step. Injector step to a valuemapper step
	 * to a dummy step. Rows go in and should be mapped accordingly.
	 * 
	 * This test will write the mappings in a new field, using a non
	 * matching default.
	 */
    public void testValueMapper4() throws Exception
    {
        KettleEnvironment.init();

        //
        // Create a new transformation...
        //
        TransMeta transMeta = new TransMeta();
        transMeta.setName("valuemappertest4");
    	
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
        // Create a dummy step 1
        //
        String dummyStepname1 = "dummy step 1";            
        DummyTransMeta dm1 = new DummyTransMeta();

        String dummyPid1 = registry.getPluginId(StepPluginType.class, dm1);
        StepMeta dummyStep1 = new StepMeta(dummyPid1, dummyStepname1, (StepMetaInterface)dm1);
        transMeta.addStep(dummyStep1);                              

        TransHopMeta hi = new TransHopMeta(injectorStep, dummyStep1);
        transMeta.addTransHop(hi);

        // 
        // Create a  step
        //
        String valueMapperName = "valuemapper step";            
        ValueMapperMeta vm = new ValueMapperMeta();
        
        vm.setFieldToUse("field1");
        vm.setTargetField("new_field");
        vm.setNonMatchDefault("default");
        vm.setSourceValue(new String[] {"abc", "ABC", "def", "ghij"});
        vm.setTargetValue(new String[] {"begin", "BEG_LONGER", "test", ""});

        String valueMapperPid = registry.getPluginId(StepPluginType.class, vm);
        StepMeta valueMapperStep = new StepMeta(valueMapperPid, valueMapperName, (StepMetaInterface)vm);
        transMeta.addStep(valueMapperStep);                              

        TransHopMeta hi2 = new TransHopMeta(dummyStep1, valueMapperStep);
        transMeta.addTransHop(hi2);        
        
        // 
        // Create a dummy step 2
        //
        String dummyStepname2 = "dummy step 2";            
        DummyTransMeta dm2 = new DummyTransMeta();

        String dummyPid2 = registry.getPluginId(StepPluginType.class, dm2);
        StepMeta dummyStep2 = new StepMeta(dummyPid2, dummyStepname2, (StepMetaInterface)dm2);
        transMeta.addStep(dummyStep2);                              

        TransHopMeta hi3 = new TransHopMeta(valueMapperStep, dummyStep2);
        transMeta.addTransHop(hi3);        
        
                
        // Now execute the transformation...
        Trans trans = new Trans(transMeta);

        trans.prepareExecution(null);
                
        StepInterface si = trans.getStepInterface(dummyStepname1, 0);
        RowStepCollector dummyRc1 = new RowStepCollector();
        si.addRowListener(dummyRc1);

        si = trans.getStepInterface(valueMapperName, 0);
        RowStepCollector valueMapperRc = new RowStepCollector();
        si.addRowListener(valueMapperRc);
               
        si = trans.getStepInterface(dummyStepname2, 0);
        RowStepCollector dummyRc = new RowStepCollector();
        si.addRowListener(dummyRc);
        
        RowProducer rp = trans.addRowProducer(injectorStepname, 0);
        trans.startThreads();
        
        // add rows
        List<RowMetaAndData> inputList = createData();
        Iterator<RowMetaAndData> it = inputList.iterator();
        while ( it.hasNext() )
        {
        	RowMetaAndData rm = it.next();
        	rp.putRow(rm.getRowMeta(), rm.getData());
        }   
        rp.finished();

        trans.waitUntilFinished();   
        
        // Compare the results                        
        List<RowMetaAndData> resultRows = dummyRc.getRowsWritten();
        List<RowMetaAndData> goldenImageRows = createResultData4();
        
        checkRows(goldenImageRows, resultRows);
    }
    
	/**
	 * Test case for valuemapper step. Injector step to a valuemapper step
	 * to a dummy step. Rows go in and should be mapped accordingly.
	 * 
	 * This test will write the mappings in the same field. Using a non
	 * matching default.
	 */
    public void testValueMapper5() throws Exception
    {
        KettleEnvironment.init();

        //
        // Create a new transformation...
        //
        TransMeta transMeta = new TransMeta();
        transMeta.setName("valuemappertest5");
    	
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
        // Create a dummy step 1
        //
        String dummyStepname1 = "dummy step 1";            
        DummyTransMeta dm1 = new DummyTransMeta();

        String dummyPid1 = registry.getPluginId(StepPluginType.class, dm1);
        StepMeta dummyStep1 = new StepMeta(dummyPid1, dummyStepname1, (StepMetaInterface)dm1);
        transMeta.addStep(dummyStep1);                              

        TransHopMeta hi = new TransHopMeta(injectorStep, dummyStep1);
        transMeta.addTransHop(hi);

        // 
        // Create a  step
        //
        String valueMapperName = "valuemapper step";            
        ValueMapperMeta vm = new ValueMapperMeta();
        
        vm.setFieldToUse("field1");
        vm.setTargetField("");
        vm.setNonMatchDefault("default");
        vm.setSourceValue(new String[] {"abc", "ABC", "def", "ghij"});
        vm.setTargetValue(new String[] {"begin", "BEG_LONGER", "test", ""});

        String valueMapperPid = registry.getPluginId(StepPluginType.class, vm);
        StepMeta valueMapperStep = new StepMeta(valueMapperPid, valueMapperName, (StepMetaInterface)vm);
        transMeta.addStep(valueMapperStep);                              

        TransHopMeta hi2 = new TransHopMeta(dummyStep1, valueMapperStep);
        transMeta.addTransHop(hi2);        
        
        // 
        // Create a dummy step 2
        //
        String dummyStepname2 = "dummy step 2";            
        DummyTransMeta dm2 = new DummyTransMeta();

        String dummyPid2 = registry.getPluginId(StepPluginType.class, dm2);
        StepMeta dummyStep2 = new StepMeta(dummyPid2, dummyStepname2, (StepMetaInterface)dm2);
        transMeta.addStep(dummyStep2);                              

        TransHopMeta hi3 = new TransHopMeta(valueMapperStep, dummyStep2);
        transMeta.addTransHop(hi3);        
        
                
        // Now execute the transformation...
        Trans trans = new Trans(transMeta);

        trans.prepareExecution(null);
                
        StepInterface si = trans.getStepInterface(dummyStepname1, 0);
        RowStepCollector dummyRc1 = new RowStepCollector();
        si.addRowListener(dummyRc1);

        si = trans.getStepInterface(valueMapperName, 0);
        RowStepCollector valueMapperRc = new RowStepCollector();
        si.addRowListener(valueMapperRc);
               
        si = trans.getStepInterface(dummyStepname2, 0);
        RowStepCollector dummyRc = new RowStepCollector();
        si.addRowListener(dummyRc);
        
        RowProducer rp = trans.addRowProducer(injectorStepname, 0);
        trans.startThreads();
        
        // add rows
        List<RowMetaAndData> inputList = createData();
        Iterator<RowMetaAndData> it = inputList.iterator();
        while ( it.hasNext() )
        {
        	RowMetaAndData rm = it.next();
        	rp.putRow(rm.getRowMeta(), rm.getData());
        }   
        rp.finished();

        trans.waitUntilFinished();   
        
        // Compare the results                        
        List<RowMetaAndData> resultRows = dummyRc.getRowsWritten();
        List<RowMetaAndData> goldenImageRows = createResultData5();
        
        checkRows(goldenImageRows, resultRows);
    }
    
	/**
	 * Test case for valuemapper step. Injector step to a valuemapper step
	 * to a dummy step. Rows go in and should be mapped accordingly.
	 * 
	 * This test will explicitly test the empty field processing. using a non
	 * matching default.
	 */
    public void testValueMapper6() throws Exception
    {
        KettleEnvironment.init();

        //
        // Create a new transformation...
        //
        TransMeta transMeta = new TransMeta();
        transMeta.setName("valuemappertest6");
    	
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
        // Create a dummy step 1
        //
        String dummyStepname1 = "dummy step 1";            
        DummyTransMeta dm1 = new DummyTransMeta();

        String dummyPid1 = registry.getPluginId(StepPluginType.class, dm1);
        StepMeta dummyStep1 = new StepMeta(dummyPid1, dummyStepname1, (StepMetaInterface)dm1);
        transMeta.addStep(dummyStep1);                              

        TransHopMeta hi = new TransHopMeta(injectorStep, dummyStep1);
        transMeta.addTransHop(hi);

        // 
        // Create a ValueMapper step
        //
        String valueMapperName = "valuemapper step";            
        ValueMapperMeta vm = new ValueMapperMeta();
        
        vm.setFieldToUse("field1");
        vm.setTargetField("");
        vm.setNonMatchDefault("default");
        vm.setSourceValue(new String[] {"abc", "ABC", "def", "ghijk", null});
        vm.setTargetValue(new String[] {"begin", "BEG_LONGER", "test", "blah", "emptyField"});

        String valueMapperPid = registry.getPluginId(StepPluginType.class, vm);
        StepMeta valueMapperStep = new StepMeta(valueMapperPid, valueMapperName, (StepMetaInterface)vm);
        transMeta.addStep(valueMapperStep);                              

        TransHopMeta hi2 = new TransHopMeta(dummyStep1, valueMapperStep);
        transMeta.addTransHop(hi2);        
        
        // 
        // Create a dummy step 2
        //
        String dummyStepname2 = "dummy step 2";            
        DummyTransMeta dm2 = new DummyTransMeta();

        String dummyPid2 = registry.getPluginId(StepPluginType.class, dm2);
        StepMeta dummyStep2 = new StepMeta(dummyPid2, dummyStepname2, (StepMetaInterface)dm2);
        transMeta.addStep(dummyStep2);                              

        TransHopMeta hi3 = new TransHopMeta(valueMapperStep, dummyStep2);
        transMeta.addTransHop(hi3);        
        
                
        // Now execute the transformation...
        Trans trans = new Trans(transMeta);

        boolean prepare;
        try {
        	trans.prepareExecution(null);
        	prepare=true;
        }
        catch (KettleException e) {
			prepare=false;
		}
        assertTrue( prepare );       
        StepInterface si = trans.getStepInterface(dummyStepname1, 0);
        RowStepCollector dummyRc1 = new RowStepCollector();
        si.addRowListener(dummyRc1);

        si = trans.getStepInterface(valueMapperName, 0);
        RowStepCollector valueMapperRc = new RowStepCollector();
        si.addRowListener(valueMapperRc);
               
        si = trans.getStepInterface(dummyStepname2, 0);
        RowStepCollector dummyRc = new RowStepCollector();
        si.addRowListener(dummyRc);
        
        RowProducer rp = trans.addRowProducer(injectorStepname, 0);
        trans.startThreads();
        
        // add rows
        List<RowMetaAndData> inputList = createData();
        Iterator<RowMetaAndData> it = inputList.iterator();
        while ( it.hasNext() )
        {
        	RowMetaAndData rm = it.next();
        	rp.putRow(rm.getRowMeta(), rm.getData());
        }   
        rp.finished();

        trans.waitUntilFinished();   
        
        // Compare the results                        
        List<RowMetaAndData> resultRows = dummyRc.getRowsWritten();
        List<RowMetaAndData> goldenImageRows = createResultData6();
        
        checkRows(goldenImageRows, resultRows);
    }                
}