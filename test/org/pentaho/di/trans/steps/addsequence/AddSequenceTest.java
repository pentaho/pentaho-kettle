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

package org.pentaho.di.trans.steps.addsequence;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.plugins.PluginRegistry;
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
 * Test class for the Add sequence step.
 * 
 * TODO: - Test steps using the same counter (harder to test because of concurrency issues).
 *
 * @author Sven Boden
 */
public class AddSequenceTest extends TestCase
{
	public RowMetaInterface createResultRowMetaInterface()
	{
		RowMetaInterface rm = new RowMeta();
		
		ValueMetaInterface valuesMeta[] = {			    
			    new ValueMeta("counter",     ValueMeta.TYPE_INTEGER),
			    new ValueMeta("valuename",   ValueMeta.TYPE_INTEGER),
			    new ValueMeta("valuename_1", ValueMeta.TYPE_INTEGER),
			    new ValueMeta("valuename_2", ValueMeta.TYPE_INTEGER)
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
		
		Object[] r1  = new Object[] { new Long(10L), new Long(1L), new Long(1L),  new Long(10L) };
		Object[] r2  = new Object[] { new Long(11L), new Long(2L), new Long(4L),  new Long(8L)  };
		Object[] r3  = new Object[] { new Long(12L), new Long(3L), new Long(7L),  new Long(6L)  };
		Object[] r4  = new Object[] { new Long(13L), new Long(4L), new Long(10L), new Long(4L)  };
		Object[] r5  = new Object[] { new Long(14L), new Long(5L), new Long(13L), new Long(10L) };
		Object[] r6  = new Object[] { new Long(15L), new Long(1L), new Long(16L), new Long(8L)  };
		Object[] r7  = new Object[] { new Long(16L), new Long(2L), new Long(1L),  new Long(6L)  };
		Object[] r8  = new Object[] { new Long(17L), new Long(3L), new Long(4L),  new Long(4L)  };
		Object[] r9  = new Object[] { new Long(18L), new Long(4L), new Long(7L),  new Long(10L) };
		Object[] r10 = new Object[] { new Long(19L), new Long(5L), new Long(10L), new Long(8L)  };
		
		list.add(new RowMetaAndData(rm, r1));
		list.add(new RowMetaAndData(rm, r2));
		list.add(new RowMetaAndData(rm, r3));
		list.add(new RowMetaAndData(rm, r4));
		list.add(new RowMetaAndData(rm, r5));
		list.add(new RowMetaAndData(rm, r6));
		list.add(new RowMetaAndData(rm, r7));
		list.add(new RowMetaAndData(rm, r8));
		list.add(new RowMetaAndData(rm, r9));
		list.add(new RowMetaAndData(rm, r10));
		
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
	 * Test case for add sequence step. Row generator attached to several
	 * add sequence steps and checking whether the end result is as expected.
	 */
    public void testAddSequence() throws Exception
    {
        KettleEnvironment.init();

        //
        // Create a new transformation...
        //
        TransMeta transMeta = new TransMeta();
        transMeta.setName("addsequencetest");
    	
        PluginRegistry registry = PluginRegistry.getInstance();

        // 
        // create a row generator step...
        //
        String rowGeneratorStepname = "row generator step";
        RowGeneratorMeta rm = new RowGeneratorMeta();
        
        // Set the information of the row generator.                
        String rowGeneratorPid = registry.getPluginId(rm);
        StepMeta rowGeneratorStep = new StepMeta(rowGeneratorPid, rowGeneratorStepname, (StepMetaInterface)rm);
        transMeta.addStep(rowGeneratorStep);
        
        //
        // Generate 10 empty rows
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
        rm.setRowLimit("10");
        rm.setFieldFormat(fieldFormat);
        rm.setGroup(group);
        rm.setDecimal(decimal);        
        
        // 
        // Create first add sequence
        //
        String seqStepname1 = "add sequence 1";            
        AddSequenceMeta asm1 = new AddSequenceMeta();
        
        asm1.setUseCounter(true);
        asm1.setValuename("counter1");
        asm1.setStartAt(10);
        asm1.setIncrementBy(1);
        asm1.setMaxValue(100);

        String addSeqPid1 = registry.getPluginId(asm1);
        StepMeta addSeqStep1 = new StepMeta(addSeqPid1, seqStepname1, (StepMetaInterface)asm1);
        transMeta.addStep(addSeqStep1);                              

        TransHopMeta hi1 = new TransHopMeta(rowGeneratorStep, addSeqStep1);
        transMeta.addTransHop(hi1);

        // 
        // Create second add sequence
        //
        String seqStepname2 = "add sequence 2";            
        AddSequenceMeta asm2 = new AddSequenceMeta();
        
        asm2.setUseCounter(true);
        asm2.setValuename("valuename2");
        asm2.setStartAt(1);
        asm2.setIncrementBy(1);
        asm2.setMaxValue(5);

        String addSeqPid2 = registry.getPluginId(asm2);
        StepMeta addSeqStep2 = new StepMeta(addSeqPid2, seqStepname2, (StepMetaInterface)asm2);
        transMeta.addStep(addSeqStep2);                              

        TransHopMeta hi2 = new TransHopMeta(addSeqStep1, addSeqStep2);
        transMeta.addTransHop(hi2);

        // 
        // Create third add sequence
        //
        String seqStepname3 = "add sequence 3";            
        AddSequenceMeta asm3 = new AddSequenceMeta();
        
        asm3.setUseCounter(true);
        asm3.setValuename("valuename3");
        asm3.setStartAt(1);
        asm3.setIncrementBy(3);
        asm3.setMaxValue(17);

        String addSeqPid3 = registry.getPluginId(asm3);
        StepMeta addSeqStep3 = new StepMeta(addSeqPid3, seqStepname3, (StepMetaInterface)asm3);
        transMeta.addStep(addSeqStep3);                              

        TransHopMeta hi3 = new TransHopMeta(addSeqStep2, addSeqStep3);
        transMeta.addTransHop(hi3);
        
        // 
        // Create fourth add sequence
        //
        String seqStepname4 = "add sequence 4";            
        AddSequenceMeta asm4 = new AddSequenceMeta();
        
        asm4.setUseCounter(true);
        asm4.setValuename("valuename4");
        asm4.setStartAt(10);
        asm4.setIncrementBy(-2);
        asm4.setMaxValue(3);

        String addSeqPid4 = registry.getPluginId(asm4);
        StepMeta addSeqStep4 = new StepMeta(addSeqPid4, seqStepname4, (StepMetaInterface)asm4);
        transMeta.addStep(addSeqStep4);                              

        TransHopMeta hi4 = new TransHopMeta(addSeqStep3, addSeqStep4);
        transMeta.addTransHop(hi4);               
        
        // Now execute the transformation...
        Trans trans = new Trans(transMeta);

        trans.prepareExecution(null);
                
        StepInterface si = trans.getStepInterface(seqStepname4, 0);
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