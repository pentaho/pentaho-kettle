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

package org.pentaho.di.trans.steps.unique;

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
import org.pentaho.di.trans.steps.sort.SortRowsMeta;
import org.pentaho.di.trans.steps.uniquerows.UniqueRowsMeta;


/**
 * Test class for the Unique step.
 * 
 * TODO: These tests only cover the case (in)sensitive comparison of a single key field.
 *
 * @author Daniel Einspanjer
 */
public class UniqueRowsTest extends TestCase
{
	public static int MAX_COUNT = 1000;
	 
	public RowMetaInterface createRowMetaInterface()
	{
		RowMetaInterface rm = new RowMeta();
		
		ValueMetaInterface valuesMeta[] = {
			    new ValueMeta("KEY", ValueMeta.TYPE_STRING),
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
        Object[] r3 = new Object[] { "abc" };
        Object[] r4 = new Object[] { "ABC" };
        
        list.add(new RowMetaAndData(rm, r1));
        list.add(new RowMetaAndData(rm, r2));
        list.add(new RowMetaAndData(rm, r3));
        list.add(new RowMetaAndData(rm, r4));

        return list;
    }
    
    public List<RowMetaAndData> createResultDataCaseSensitiveNoPreviousSort()
    {
        List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();    
        
        RowMetaInterface rm = createRowMetaInterface();
        
        Object[] r1 = new Object[] { "abc" };
        Object[] r2 = new Object[] { "ABC" };
        Object[] r3 = new Object[] { "abc" };
        Object[] r4 = new Object[] { "ABC" };
        
        list.add(new RowMetaAndData(rm, r1));
        list.add(new RowMetaAndData(rm, r2));
        list.add(new RowMetaAndData(rm, r3));
        list.add(new RowMetaAndData(rm, r4));

        return list;
    }   

    
    public List<RowMetaAndData> createResultDataCaseInsensitiveNoPreviousSort()
    {
        List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();    
        
        RowMetaInterface rm = createRowMetaInterface();
        
        Object[] r1 = new Object[] { "abc" };
        
        list.add(new RowMetaAndData(rm, r1));

        return list;
    }   
    
    public List<RowMetaAndData> createResultDataSortCaseSensitiveUniqueCaseSensitive()
    {
        List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();    
        
        RowMetaInterface rm = createRowMetaInterface();
        
        Object[] r1 = new Object[] { "ABC" };
        Object[] r2 = new Object[] { "abc" };
        
        list.add(new RowMetaAndData(rm, r1));
        list.add(new RowMetaAndData(rm, r2));

        return list;
    }   
    
    public List<RowMetaAndData> createResultDataSortCaseSensitiveUniqueCaseInsensitive()
    {
        List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();    
        
        RowMetaInterface rm = createRowMetaInterface();
        
        Object[] r1 = new Object[] { "ABC" };
        
        list.add(new RowMetaAndData(rm, r1));

        return list;
    }   
    
    
    public List<RowMetaAndData> createResultDataSortCaseInsensitiveUniqueCaseSensitive()
    {
        List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();    
        
        RowMetaInterface rm = createRowMetaInterface();
        
        Object[] r1 = new Object[] { "abc" };
        Object[] r2 = new Object[] { "ABC" };
        Object[] r3 = new Object[] { "abc" };
        Object[] r4 = new Object[] { "ABC" };
        
        list.add(new RowMetaAndData(rm, r1));
        list.add(new RowMetaAndData(rm, r2));
        list.add(new RowMetaAndData(rm, r3));
        list.add(new RowMetaAndData(rm, r4));

        return list;
    }   
    
    public List<RowMetaAndData> createResultDataSortCaseInsensitiveUniqueCaseInsensitive()
    {
        List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();    
        
        RowMetaInterface rm = createRowMetaInterface();
        
        Object[] r1 = new Object[] { "abc" };
        
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
	
    public void testCaseSensitiveNoPreviousSort() throws Exception
    {
        KettleEnvironment.init();

        //
        // Create a new transformation...
        //
        TransMeta transMeta = new TransMeta();
        transMeta.setName("uniquerowstest");
    	
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
        // Create a unique rows step
        //
        String uniqueRowsStepname = "unique rows step";            
        UniqueRowsMeta urm = new UniqueRowsMeta();
        urm.setCompareFields(new String[] {"KEY"});
        urm.setCaseInsensitive(new boolean[] {false});

        String uniqueRowsStepPid = registry.getPluginId(StepPluginType.getInstance(), urm);
        StepMeta uniqueRowsStep = new StepMeta(uniqueRowsStepPid, uniqueRowsStepname, (StepMetaInterface)urm);
        transMeta.addStep(uniqueRowsStep);            

        transMeta.addTransHop(new TransHopMeta(injectorStep, uniqueRowsStep));        
        
        // 
        // Create a dummy step
        //
        String dummyStepname = "dummy step";            
        DummyTransMeta dm = new DummyTransMeta();

        String dummyPid = registry.getPluginId(StepPluginType.getInstance(), dm);
        StepMeta dummyStep = new StepMeta(dummyPid, dummyStepname, (StepMetaInterface)dm);
        transMeta.addStep(dummyStep);                              

        transMeta.addTransHop(new TransHopMeta(uniqueRowsStep, dummyStep));        
        
        // Now execute the transformation...
        Trans trans = new Trans(transMeta);

        trans.prepareExecution(null);
                
        StepInterface si = trans.getStepInterface(dummyStepname, 0);
        RowStepCollector dummyRc = new RowStepCollector();
        si.addRowListener(dummyRc);
        
        RowProducer rp = trans.addRowProducer(injectorStepname, 0);
        trans.startThreads();
        
        // add rows
        List<RowMetaAndData> inputList = createData();
        for ( RowMetaAndData rm : inputList )
        {
        	rp.putRow(rm.getRowMeta(), rm.getData());
        }   
        rp.finished();
 
        trans.waitUntilFinished();   
                                     
        List<RowMetaAndData> resultRows = dummyRc.getRowsWritten();
        checkRows(createResultDataCaseSensitiveNoPreviousSort(), resultRows);
    }   

    public void testCaseInsensitiveNoPreviousSort() throws Exception
    {
        KettleEnvironment.init();

        //
        // Create a new transformation...
        //
        TransMeta transMeta = new TransMeta();
        transMeta.setName("uniquerowstest");
        
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
        // Create a unique rows step
        //
        String uniqueRowsStepname = "unique rows step";            
        UniqueRowsMeta urm = new UniqueRowsMeta();
        urm.setCompareFields(new String[] {"KEY"});
        urm.setCaseInsensitive(new boolean[] {true});

        String uniqueRowsStepPid = registry.getPluginId(StepPluginType.getInstance(), urm);
        StepMeta uniqueRowsStep = new StepMeta(uniqueRowsStepPid, uniqueRowsStepname, (StepMetaInterface)urm);
        transMeta.addStep(uniqueRowsStep);            

        transMeta.addTransHop(new TransHopMeta(injectorStep, uniqueRowsStep));        
        
        // 
        // Create a dummy step
        //
        String dummyStepname = "dummy step";            
        DummyTransMeta dm = new DummyTransMeta();

        String dummyPid = registry.getPluginId(StepPluginType.getInstance(), dm);
        StepMeta dummyStep = new StepMeta(dummyPid, dummyStepname, (StepMetaInterface)dm);
        transMeta.addStep(dummyStep);                              

        transMeta.addTransHop(new TransHopMeta(uniqueRowsStep, dummyStep));        
        
        // Now execute the transformation...
        Trans trans = new Trans(transMeta);

        trans.prepareExecution(null);
                
        StepInterface si = trans.getStepInterface(dummyStepname, 0);
        RowStepCollector dummyRc = new RowStepCollector();
        si.addRowListener(dummyRc);
        
        RowProducer rp = trans.addRowProducer(injectorStepname, 0);
        trans.startThreads();
        
        // add rows
        List<RowMetaAndData> inputList = createData();
        for ( RowMetaAndData rm : inputList )
        {
            rp.putRow(rm.getRowMeta(), rm.getData());
        }   
        rp.finished();
 
        trans.waitUntilFinished();   
                                     
        List<RowMetaAndData> resultRows = dummyRc.getRowsWritten();
        checkRows(createResultDataCaseInsensitiveNoPreviousSort(), resultRows);
    }   
    
    public void testSortCaseSensitiveUniqueCaseSensitive() throws Exception
    {
        KettleEnvironment.init();

        //
        // Create a new transformation...
        //
        TransMeta transMeta = new TransMeta();
        transMeta.setName("uniquerowstest");
        
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
        // Create a sort rows step
        //
        String sortRowsStepname = "sort rows step";            
        SortRowsMeta srm = new SortRowsMeta();
        srm.setFieldName(new String[] {"KEY"});
        srm.setAscending(new boolean[] {true});
        srm.setCaseSensitive(new boolean[] {true});
        srm.setPrefix("SortRowsTest");
        srm.setDirectory(".");

        String sortRowsStepPid = registry.getPluginId(StepPluginType.getInstance(), srm);
        StepMeta sortRowsStep = new StepMeta(sortRowsStepPid, sortRowsStepname, (StepMetaInterface)srm);
        transMeta.addStep(sortRowsStep);            

        transMeta.addTransHop(new TransHopMeta(injectorStep, sortRowsStep));        

        // 
        // Create a unique rows step
        //
        String uniqueRowsStepname = "unique rows step";            
        UniqueRowsMeta urm = new UniqueRowsMeta();
        urm.setCompareFields(new String[] {"KEY"});
        urm.setCaseInsensitive(new boolean[] {false});

        String uniqueRowsStepPid = registry.getPluginId(StepPluginType.getInstance(), urm);
        StepMeta uniqueRowsStep = new StepMeta(uniqueRowsStepPid, uniqueRowsStepname, (StepMetaInterface)urm);
        transMeta.addStep(uniqueRowsStep);            

        transMeta.addTransHop(new TransHopMeta(sortRowsStep, uniqueRowsStep));        
        
        // 
        // Create a dummy step
        //
        String dummyStepname = "dummy step";            
        DummyTransMeta dm = new DummyTransMeta();

        String dummyPid = registry.getPluginId(StepPluginType.getInstance(), dm);
        StepMeta dummyStep = new StepMeta(dummyPid, dummyStepname, (StepMetaInterface)dm);
        transMeta.addStep(dummyStep);                              

        transMeta.addTransHop(new TransHopMeta(uniqueRowsStep, dummyStep));        
        
        // Now execute the transformation...
        Trans trans = new Trans(transMeta);

        trans.prepareExecution(null);
                
        StepInterface si = trans.getStepInterface(dummyStepname, 0);
        RowStepCollector dummyRc = new RowStepCollector();
        si.addRowListener(dummyRc);
        
        RowProducer rp = trans.addRowProducer(injectorStepname, 0);
        trans.startThreads();
        
        // add rows
        List<RowMetaAndData> inputList = createData();
        for ( RowMetaAndData rm : inputList )
        {
            rp.putRow(rm.getRowMeta(), rm.getData());
        }   
        rp.finished();
 
        trans.waitUntilFinished();   
                                     
        List<RowMetaAndData> resultRows = dummyRc.getRowsWritten();
        checkRows(createResultDataSortCaseSensitiveUniqueCaseSensitive(), resultRows);
    }   
    
    public void testSortCaseSensitiveUniqueCaseInsensitive() throws Exception
    {
        KettleEnvironment.init();

        //
        // Create a new transformation...
        //
        TransMeta transMeta = new TransMeta();
        transMeta.setName("uniquerowstest");
        
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
        // Create a sort rows step
        //
        String sortRowsStepname = "sort rows step";            
        SortRowsMeta srm = new SortRowsMeta();
        srm.setFieldName(new String[] {"KEY"});
        srm.setAscending(new boolean[] {true});
        srm.setCaseSensitive(new boolean[] {true});
        srm.setPrefix("SortRowsTest");
        srm.setDirectory(".");

        String sortRowsStepPid = registry.getPluginId(StepPluginType.getInstance(), srm);
        StepMeta sortRowsStep = new StepMeta(sortRowsStepPid, sortRowsStepname, (StepMetaInterface)srm);
        transMeta.addStep(sortRowsStep);            

        transMeta.addTransHop(new TransHopMeta(injectorStep, sortRowsStep));        

        // 
        // Create a unique rows step
        //
        String uniqueRowsStepname = "unique rows step";            
        UniqueRowsMeta urm = new UniqueRowsMeta();
        urm.setCompareFields(new String[] {"KEY"});
        urm.setCaseInsensitive(new boolean[] {true});

        String uniqueRowsStepPid = registry.getPluginId(StepPluginType.getInstance(), urm);
        StepMeta uniqueRowsStep = new StepMeta(uniqueRowsStepPid, uniqueRowsStepname, (StepMetaInterface)urm);
        transMeta.addStep(uniqueRowsStep);            

        transMeta.addTransHop(new TransHopMeta(sortRowsStep, uniqueRowsStep));        
        
        // 
        // Create a dummy step
        //
        String dummyStepname = "dummy step";            
        DummyTransMeta dm = new DummyTransMeta();

        String dummyPid = registry.getPluginId(StepPluginType.getInstance(), dm);
        StepMeta dummyStep = new StepMeta(dummyPid, dummyStepname, (StepMetaInterface)dm);
        transMeta.addStep(dummyStep);                              

        transMeta.addTransHop(new TransHopMeta(uniqueRowsStep, dummyStep));        
        
        // Now execute the transformation...
        Trans trans = new Trans(transMeta);

        trans.prepareExecution(null);
                
        StepInterface si = trans.getStepInterface(dummyStepname, 0);
        RowStepCollector dummyRc = new RowStepCollector();
        si.addRowListener(dummyRc);
        
        RowProducer rp = trans.addRowProducer(injectorStepname, 0);
        trans.startThreads();
        
        // add rows
        List<RowMetaAndData> inputList = createData();
        for ( RowMetaAndData rm : inputList )
        {
            rp.putRow(rm.getRowMeta(), rm.getData());
        }   
        rp.finished();
 
        trans.waitUntilFinished();   
                                     
        List<RowMetaAndData> resultRows = dummyRc.getRowsWritten();
        checkRows(createResultDataSortCaseSensitiveUniqueCaseInsensitive(), resultRows);
    }   
    
    public void testSortCaseInsensitiveUniqueCaseSensitive() throws Exception
    {
        KettleEnvironment.init();

        //
        // Create a new transformation...
        //
        TransMeta transMeta = new TransMeta();
        transMeta.setName("uniquerowstest");
        
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
        // Create a sort rows step
        //
        String sortRowsStepname = "sort rows step";            
        SortRowsMeta srm = new SortRowsMeta();
        srm.setFieldName(new String[] {"KEY"});
        srm.setAscending(new boolean[] {true});
        srm.setCaseSensitive(new boolean[] {false});
        srm.setPrefix("SortRowsTest");
        srm.setDirectory(".");

        String sortRowsStepPid = registry.getPluginId(StepPluginType.getInstance(), srm);
        StepMeta sortRowsStep = new StepMeta(sortRowsStepPid, sortRowsStepname, (StepMetaInterface)srm);
        transMeta.addStep(sortRowsStep);            

        transMeta.addTransHop(new TransHopMeta(injectorStep, sortRowsStep));        

        // 
        // Create a unique rows step
        //
        String uniqueRowsStepname = "unique rows step";            
        UniqueRowsMeta urm = new UniqueRowsMeta();
        urm.setCompareFields(new String[] {"KEY"});
        urm.setCaseInsensitive(new boolean[] {false});

        String uniqueRowsStepPid = registry.getPluginId(StepPluginType.getInstance(), urm);
        StepMeta uniqueRowsStep = new StepMeta(uniqueRowsStepPid, uniqueRowsStepname, (StepMetaInterface)urm);
        transMeta.addStep(uniqueRowsStep);            

        transMeta.addTransHop(new TransHopMeta(sortRowsStep, uniqueRowsStep));        
        
        // 
        // Create a dummy step
        //
        String dummyStepname = "dummy step";            
        DummyTransMeta dm = new DummyTransMeta();

        String dummyPid = registry.getPluginId(StepPluginType.getInstance(), dm);
        StepMeta dummyStep = new StepMeta(dummyPid, dummyStepname, (StepMetaInterface)dm);
        transMeta.addStep(dummyStep);                              

        transMeta.addTransHop(new TransHopMeta(uniqueRowsStep, dummyStep));        
        
        // Now execute the transformation...
        Trans trans = new Trans(transMeta);

        trans.prepareExecution(null);
                
        StepInterface si = trans.getStepInterface(dummyStepname, 0);
        RowStepCollector dummyRc = new RowStepCollector();
        si.addRowListener(dummyRc);
        
        RowProducer rp = trans.addRowProducer(injectorStepname, 0);
        trans.startThreads();
        
        // add rows
        List<RowMetaAndData> inputList = createData();
        for ( RowMetaAndData rm : inputList )
        {
            rp.putRow(rm.getRowMeta(), rm.getData());
        }   
        rp.finished();
 
        trans.waitUntilFinished();   
                                     
        List<RowMetaAndData> resultRows = dummyRc.getRowsWritten();
        checkRows(createResultDataSortCaseInsensitiveUniqueCaseSensitive(), resultRows);
    }   
    
    public void testSortCaseInsensitiveUniqueCaseInsensitive() throws Exception
    {
        KettleEnvironment.init();

        //
        // Create a new transformation...
        //
        TransMeta transMeta = new TransMeta();
        transMeta.setName("uniquerowstest");
        
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
        // Create a sort rows step
        //
        String sortRowsStepname = "sort rows step";            
        SortRowsMeta srm = new SortRowsMeta();
        srm.setFieldName(new String[] {"KEY"});
        srm.setAscending(new boolean[] {true});
        srm.setCaseSensitive(new boolean[] {false});
        srm.setPrefix("SortRowsTest");
        srm.setDirectory(".");

        String sortRowsStepPid = registry.getPluginId(StepPluginType.getInstance(), srm);
        StepMeta sortRowsStep = new StepMeta(sortRowsStepPid, sortRowsStepname, (StepMetaInterface)srm);
        transMeta.addStep(sortRowsStep);            

        transMeta.addTransHop(new TransHopMeta(injectorStep, sortRowsStep));        

        // 
        // Create a unique rows step
        //
        String uniqueRowsStepname = "unique rows step";            
        UniqueRowsMeta urm = new UniqueRowsMeta();
        urm.setCompareFields(new String[] {"KEY"});
        urm.setCaseInsensitive(new boolean[] {true});

        String uniqueRowsStepPid = registry.getPluginId(StepPluginType.getInstance(), urm);
        StepMeta uniqueRowsStep = new StepMeta(uniqueRowsStepPid, uniqueRowsStepname, (StepMetaInterface)urm);
        transMeta.addStep(uniqueRowsStep);            

        transMeta.addTransHop(new TransHopMeta(sortRowsStep, uniqueRowsStep));        
        
        // 
        // Create a dummy step
        //
        String dummyStepname = "dummy step";            
        DummyTransMeta dm = new DummyTransMeta();

        String dummyPid = registry.getPluginId(StepPluginType.getInstance(), dm);
        StepMeta dummyStep = new StepMeta(dummyPid, dummyStepname, (StepMetaInterface)dm);
        transMeta.addStep(dummyStep);                              

        transMeta.addTransHop(new TransHopMeta(uniqueRowsStep, dummyStep));        
        
        // Now execute the transformation...
        Trans trans = new Trans(transMeta);

        trans.prepareExecution(null);
                
        StepInterface si = trans.getStepInterface(dummyStepname, 0);
        RowStepCollector dummyRc = new RowStepCollector();
        si.addRowListener(dummyRc);
        
        RowProducer rp = trans.addRowProducer(injectorStepname, 0);
        trans.startThreads();
        
        // add rows
        List<RowMetaAndData> inputList = createData();
        for ( RowMetaAndData rm : inputList )
        {
            rp.putRow(rm.getRowMeta(), rm.getData());
        }   
        rp.finished();
 
        trans.waitUntilFinished();   
                                     
        List<RowMetaAndData> resultRows = dummyRc.getRowsWritten();
        checkRows(createResultDataSortCaseInsensitiveUniqueCaseInsensitive(), resultRows);
    }   
}