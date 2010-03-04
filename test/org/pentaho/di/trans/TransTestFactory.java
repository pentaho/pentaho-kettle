/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.trans;

import java.util.Iterator;
import java.util.List;

import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.dummytrans.DummyTransMeta;
import org.pentaho.di.trans.steps.injector.InjectorMeta;

/**
 * We can use this factory to create transformations with a source and target step.<br>
 * The source step is an Injector step.<br>
 * The target step is a dummy step.<br>
 * The middle step is the step specified.<br>
 * 
 * @author Matt Casters (mcasters@pentaho.org)
 *
 */
public class TransTestFactory
{
    public static final String INJECTOR_STEPNAME = "injector";
    public static final String DUMMY_STEPNAME = "dummy";

	public static TransMeta generateTestTransformation(VariableSpace parent, StepMetaInterface oneMeta, String oneStepname)
    {
        PluginRegistry registry = PluginRegistry.getInstance();

        TransMeta previewMeta = new TransMeta(parent);
        
        // First the injector step...
        //
        InjectorMeta zeroMeta = new InjectorMeta();
        StepMeta zero = new StepMeta(registry.getPluginId(StepPluginType.class, zeroMeta), INJECTOR_STEPNAME, zeroMeta);
        zero.setLocation(50,50);
        zero.setDraw(true);
        previewMeta.addStep(zero);
        
        // Then the middle step to test...
        //
        StepMeta one = new StepMeta(registry.getPluginId(StepPluginType.class, oneMeta), oneStepname, oneMeta);
        one.setLocation(150,50);
        one.setDraw(true);
        previewMeta.addStep(one);
        
        // Then we add the dummy step to read the results from
        //
        DummyTransMeta twoMeta = new DummyTransMeta();
        StepMeta two = new StepMeta(registry.getPluginId(StepPluginType.class, twoMeta), DUMMY_STEPNAME, twoMeta); //$NON-NLS-1$
        two.setLocation(250,50);
        two.setDraw(true);
        previewMeta.addStep(two);
        
        // Add the hops between the 3 steps.
        //
        TransHopMeta zeroOne = new TransHopMeta(zero, one);
        previewMeta.addTransHop(zeroOne);
        TransHopMeta oneTwo= new TransHopMeta(one, two);
        previewMeta.addTransHop(oneTwo);
        
        return previewMeta;
    }
	
	public static List<RowMetaAndData> executeTestTransformation(TransMeta transMeta, String injectorStepname, String testStepname, String dummyStepname, List<RowMetaAndData> inputData) throws KettleException {
        // Now execute the transformation...
        Trans trans = new Trans(transMeta);

        trans.prepareExecution(null);

        // Capture the rows that come out of the dummy step...
        //
        StepInterface si = trans.getStepInterface(dummyStepname, 0);
        RowStepCollector dummyRc = new RowStepCollector();
        si.addRowListener(dummyRc);
        
        // Add a row producer...
        //
        RowProducer rp = trans.addRowProducer(injectorStepname, 0);
        
        // Start the steps...
        //
        trans.startThreads();
        
        // Inject the actual test rows...
        //
        List<RowMetaAndData> inputList = inputData;
        Iterator<RowMetaAndData> it = inputList.iterator();
        while ( it.hasNext() )
        {
        	RowMetaAndData rm = (RowMetaAndData)it.next();
        	rp.putRow(rm.getRowMeta(), rm.getData());
        }   
        rp.finished();

        // Wait until the transformation is finished...
        //
        trans.waitUntilFinished();   
        
        // If there is an error in the result, throw an exception here...
        //
        if (trans.getResult().getNrErrors()>0) {
        	throw new KettleException("Test transformation finished with errors. Check the log.");
        }

        // Return the result from the dummy step...
        //
        return dummyRc.getRowsRead();
	}
}
