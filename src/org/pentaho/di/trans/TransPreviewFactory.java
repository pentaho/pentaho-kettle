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

import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.dummytrans.DummyTransMeta;

public class TransPreviewFactory
{
    public static final TransMeta generatePreviewTransformation(VariableSpace parent, StepMetaInterface oneMeta, String oneStepname)
    {
        StepLoader stepLoader = StepLoader.getInstance();

        TransMeta previewMeta = new TransMeta(parent);
        previewMeta.setName(parent==null ? "Preview transformation" : parent.toString());
        
        // At it to the first step.
        StepMeta one = new StepMeta(stepLoader.getStepPluginID(oneMeta), oneStepname, oneMeta);
        one.setLocation(50,50);
        one.setDraw(true);
        previewMeta.addStep(one);
        
        DummyTransMeta twoMeta = new DummyTransMeta();
        StepMeta two = new StepMeta(stepLoader.getStepPluginID(twoMeta), "dummy", twoMeta); //$NON-NLS-1$
        two.setLocation(250,50);
        two.setDraw(true);
        previewMeta.addStep(two);
        
        TransHopMeta hop = new TransHopMeta(one, two);
        previewMeta.addTransHop(hop);
        
        return previewMeta;
    }
}
