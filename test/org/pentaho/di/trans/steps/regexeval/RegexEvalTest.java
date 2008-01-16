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

package org.pentaho.di.trans.steps.regexeval;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.trans.RowProducer;
import org.pentaho.di.trans.RowStepCollector;
import org.pentaho.di.trans.StepLoader;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.dummytrans.DummyTransMeta;
import org.pentaho.di.trans.steps.injector.InjectorMeta;

/**
 * Test class for the RegexEval step.
 * 
 * Needs a lot more cases.
 *
 * @author Sven Boden
 */
public class RegexEvalTest extends TestCase
{
	public RowMetaInterface createRowMetaInterface()
	{
		RowMetaInterface rm = new RowMeta();
		
		ValueMetaInterface valuesMeta[] = {
			    new ValueMeta("field1", ValueMeta.TYPE_STRING)
	    };

		for (int i=0; i < valuesMeta.length; i++ )
		{
			rm.addValueMeta(valuesMeta[i]);
		}
		
		return rm;
	}
	
	public RowMetaInterface createResultRowMetaInterface()
	{
		RowMetaInterface rm = new RowMeta();
		
		ValueMetaInterface valuesMeta[] = {
			    new ValueMeta("field1", ValueMeta.TYPE_STRING),
			    new ValueMeta("res",    ValueMeta.TYPE_BOOLEAN)			   
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
		Object[] r3 = new Object[] { "123" };
		Object[] r4 = new Object[] { "abc" };
		
		list.add(new RowMetaAndData(rm, r1));
		list.add(new RowMetaAndData(rm, r2));
		list.add(new RowMetaAndData(rm, r3));
		list.add(new RowMetaAndData(rm, r4));
		
		return list;
	}

	
	/**
	 * Create result data for test case 1.
	 */
	public List<RowMetaAndData> createResultData1()
	{
		List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();	
		
		RowMetaInterface rm = createResultRowMetaInterface();
		
		Object[] r1 = new Object[] { "abc", Boolean.valueOf(true)  };
		Object[] r2 = new Object[] { "ABC", Boolean.valueOf(false) };
		Object[] r3 = new Object[] { "123", Boolean.valueOf(false) };
		Object[] r4 = new Object[] { "abc", Boolean.valueOf(true)  };
		
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
	 * Test case for regexeval step. Injector step to a regexeval step
	 * to a dummy step. 
	 */
    public void testRegexEval1() throws Exception
    {
        EnvUtil.environmentInit();

        //
        // Create a new transformation...
        //
        TransMeta transMeta = new TransMeta();
        transMeta.setName("regexeval1");
    	
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
        // Create a dummy step 1
        //
        String dummyStepname1 = "dummy step 1";            
        DummyTransMeta dm1 = new DummyTransMeta();

        String dummyPid1 = steploader.getStepPluginID(dm1);
        StepMeta dummyStep1 = new StepMeta(dummyPid1, dummyStepname1, (StepMetaInterface)dm1);
        transMeta.addStep(dummyStep1);                              

        TransHopMeta hi = new TransHopMeta(injectorStep, dummyStep1);
        transMeta.addTransHop(hi);

        // 
        // Create a RegexEval step
        //
        String regexEvalName = "regexeval step";            
        RegexEvalMeta re = new RegexEvalMeta();
        
        re.setScript("[a-z]*");
        re.setMatcher("field1");
        re.setResultfieldname("res");
        re.setuseVar(false);
        re.setcanoneq(false);
        re.setcaseinsensitive(false);
        re.setcomment(false);
        re.setdotall(false);
        re.setmultiline(false);
        re.setunicode(false);
        re.setunix(false);

        String regexEvalPid = steploader.getStepPluginID(re);
        StepMeta regexEvalStep = new StepMeta(regexEvalPid, regexEvalName, (StepMetaInterface)re);
        transMeta.addStep(regexEvalStep);                              

        TransHopMeta hi2 = new TransHopMeta(dummyStep1, regexEvalStep);
        transMeta.addTransHop(hi2);        
        
        // 
        // Create a dummy step 2
        //
        String dummyStepname2 = "dummy step 2";            
        DummyTransMeta dm2 = new DummyTransMeta();

        String dummyPid2 = steploader.getStepPluginID(dm2);
        StepMeta dummyStep2 = new StepMeta(dummyPid2, dummyStepname2, (StepMetaInterface)dm2);
        transMeta.addStep(dummyStep2);                              

        TransHopMeta hi3 = new TransHopMeta(regexEvalStep, dummyStep2);
        transMeta.addTransHop(hi3);        
        
                
        // Now execute the transformation...
        Trans trans = new Trans(transMeta);

        trans.prepareExecution(null);
                
        StepInterface si = trans.getStepInterface(dummyStepname1, 0);
        RowStepCollector dummyRc1 = new RowStepCollector();
        si.addRowListener(dummyRc1);

        si = trans.getStepInterface(regexEvalName, 0);
        RowStepCollector nullIfRc = new RowStepCollector();
        si.addRowListener(nullIfRc);
               
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
}