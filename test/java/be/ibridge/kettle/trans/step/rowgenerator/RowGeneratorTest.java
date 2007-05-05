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

package be.ibridge.kettle.trans.step.rowgenerator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.util.EnvUtil;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.trans.StepLoader;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransHopMeta;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.RowStepCollector;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;
import be.ibridge.kettle.trans.step.dummytrans.DummyTransMeta;

/**
 * Test class for the RowGenerator step.
 * 
 * For the moment only the basic stuff is verified. Formats, lengths, precision
 * should best also be tested. TODO
 *
 * @author Sven Boden
 */
public class RowGeneratorTest extends TestCase
{
	public List createData()
	{
		List list = new ArrayList();
		Row r = new Row();
		
		Value values[] = {
			    new Value("string", "string_value"),
				new Value("boolean", true),
				new Value("integer", 20L)
		};

		for (int i=0; i < values.length; i++ )
		{
			r.addValue(values[i]);
		}
		
		list.add(r);
		list.add(r);
		list.add(r);
		
		return list;
	}

	/**
	 *  Check the 2 lists comparing the rows in order.
	 *  If they are not the same fail the test. 
	 */
    public void checkRows(List rows1, List rows2)
    {
    	int idx = 1;
        if ( rows1.size() != rows2.size() )
        {
        	fail("Number of rows is not the same: " + 
          		 rows1.size() + " and " + rows2.size());
        }
        Iterator it1 = rows1.iterator();
        Iterator it2 = rows2.iterator();
        
        while ( it1.hasNext() && it2.hasNext() )
        {
            Row r1 = (Row)it1.next();
            Row r2 = (Row)it2.next();
            if ( r1.compare(r2) != 0 )
            {
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
        LogWriter log = LogWriter.getInstance();
        EnvUtil.environmentInit();

        //
        // Create a new transformation...
        //
        TransMeta transMeta = new TransMeta();
        transMeta.setName("row generatortest");
    	
        StepLoader steploader = StepLoader.getInstance();            

        // 
        // create a row generator step...
        //
        String rowGeneratorStepname = "row generator step";
        RowGeneratorMeta rm = new RowGeneratorMeta();
        
        // Set the information of the row generator.                
        String rowGeneratorPid = steploader.getStepPluginID(rm);
        StepMeta rowGeneratorStep = new StepMeta(rowGeneratorPid, rowGeneratorStepname, (StepMetaInterface)rm);
        transMeta.addStep(rowGeneratorStep);
        
        //
        // Do the following specs 3 times.
        //
        String fieldName[] = { "string", "boolean", "integer" };
        String type[]      = { "String", "Boolean", "Integer" };
        String value[]     = { "string_value", "true", "20"   };
        int    intDummies[] =  { -1, -1, -1 };
                
        rm.setDefault();
        rm.setFieldName(fieldName);
        rm.setFieldType(type);
        rm.setValue(value);
        rm.setFieldLength(intDummies);
        rm.setFieldPrecision(intDummies);        
        rm.setRowLimit("3");

        // 
        // Create a dummy step
        //
        String dummyStepname = "dummy step";            
        DummyTransMeta dm = new DummyTransMeta();

        String dummyPid = steploader.getStepPluginID(dm);
        StepMeta dummyStep = new StepMeta(dummyPid, dummyStepname, (StepMetaInterface)dm);
        transMeta.addStep(dummyStep);                              

        TransHopMeta hi = new TransHopMeta(rowGeneratorStep, dummyStep);
        transMeta.addTransHop(hi);
                
        // Now execute the transformation...
        Trans trans = new Trans(log, transMeta);

        trans.prepareExecution(null);
                
        StepInterface si = trans.getStepInterface(dummyStepname, 0);
        RowStepCollector rc = new RowStepCollector();
        si.addRowListener(rc);
        
        trans.startThreads();        
        trans.waitUntilFinished();   
        
        List checkList = createData();
        List resultRows = rc.getRowsRead();
        checkRows(resultRows, checkList);
    }
}