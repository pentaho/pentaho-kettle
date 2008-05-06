package org.pentaho.di.trans.steps.regexeval;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.trans.RowProducer;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.dummytrans.DummyTransMeta;
import org.pentaho.di.trans.steps.injector.InjectorMeta;

import junit.framework.TestCase;

public abstract class TransformationTestCase extends TestCase
{

    public TransformationTestCase()
    {
        super();
        EnvUtil.environmentInit();
    }

    public TransformationTestCase(String name)
    {
        super(name);
        EnvUtil.environmentInit();
    }

    public RowMetaInterface createRowMetaInterface(ValueMeta... valueMetas)
    {
    	RowMetaInterface rm = new RowMeta();
    	
    	for (ValueMeta vm : valueMetas)
    	{
    	    rm.addValueMeta(vm);
    	}

    	return rm;
    }

    public List<RowMetaAndData> createData(RowMetaInterface rm, Object[][] rows)
    {
    	List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();	

    	for (Object[] row : rows)
    	{
    	    list.add(new RowMetaAndData(rm, row));
    	}
    	
    	return list;
    }

    /**
     *  Check the 2 lists comparing the rows in order.
     *  If they are not the same fail the test. 
     */
    public void checkRows(List<RowMetaAndData> rows1, List<RowMetaAndData> rows2)
    {
        if ( rows1.size() != rows2.size() )
        {
        	fail("Number of rows is not the same: " + 
          		 rows1.size() + " and " + rows2.size());
        }
        ListIterator<RowMetaAndData> it1 = rows1.listIterator();
        ListIterator<RowMetaAndData> it2 = rows2.listIterator();
        
        while ( it1.hasNext() && it2.hasNext() )
        {
        	RowMetaAndData rm1 = it1.next();
        	RowMetaAndData rm2 = it2.next();
        	
        	Object[] r1 = rm1.getData();
        	Object[] r2 = rm2.getData();
        	
        	if ( rm1.size() != rm2.size() )
        	{
        		fail("row nr " + it1.nextIndex() + " is not equal");
        	}
        	int fields[] = new int[r1.length];
        	for ( int ydx = 0; ydx < r1.length; ydx++ )
        	{
        		fields[ydx] = ydx;
        	}
            try {
    			if ( rm1.getRowMeta().compare(r1, r2, fields) != 0 )
    			{
    				fail("row nr " + it1.nextIndex() + " is not equal");
    			}
    		} catch (KettleValueException e) {
    			fail("row nr " + it1.nextIndex() + " is not equal");
    		}
        }
    }

    public StepMeta createInjectorStepForTrans(String name, TransMeta transMeta)
    {
        InjectorMeta injectorMeta = new InjectorMeta();
        
        StepMeta injectorStep = new StepMeta(name, injectorMeta);
        transMeta.addStep(injectorStep);
        
        return injectorStep;
    }

    public StepMeta createConnectedDummyStepForTrans(String name, TransMeta transMeta, StepMeta connectTo)
    {
        DummyTransMeta dummyTransMeta = new DummyTransMeta();
    
        StepMeta dummyStep = new StepMeta(name, dummyTransMeta);
        transMeta.addStep(dummyStep);                              
        TransHopMeta hop = new TransHopMeta(connectTo, dummyStep);
        transMeta.addTransHop(hop);
        
        return dummyStep;
    }

    public StepMeta createConnectedStepForTrans(String name, StepMetaInterface stepMetaInterface, TransMeta transMeta, StepMeta connectTo)
    {
        StepMeta stepMeta = new StepMeta(name, stepMetaInterface);
        transMeta.addStep(stepMeta);                              
    
        TransHopMeta hop = new TransHopMeta(connectTo, stepMeta);
        transMeta.addTransHop(hop);    
        
        return stepMeta;
    }

    public void feedSourceRows(RowProducer rp, List<RowMetaAndData> inputList)
    {
        Iterator<RowMetaAndData> it = inputList.iterator();
        while ( it.hasNext() )
        {
        	RowMetaAndData rm = it.next();
        	rp.putRow(rm.getRowMeta(), rm.getData());
        }   
        rp.finished();
    }

}