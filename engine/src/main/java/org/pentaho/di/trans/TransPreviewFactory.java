/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.trans;

import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.dummytrans.DummyTransMeta;

public class TransPreviewFactory {
  public static final TransMeta generatePreviewTransformation( VariableSpace parent, StepMetaInterface oneMeta,
    String oneStepname ) {
    PluginRegistry registry = PluginRegistry.getInstance();

    TransMeta previewMeta = new TransMeta( parent );
    // The following operation resets the internal variables!
    //
    previewMeta.setName( parent == null ? "Preview transformation" : parent.toString() );

    // At it to the first step.
    StepMeta one = new StepMeta( registry.getPluginId( StepPluginType.class, oneMeta ), oneStepname, oneMeta );
    one.setLocation( 50, 50 );
    one.setDraw( true );
    previewMeta.addStep( one );

    DummyTransMeta twoMeta = new DummyTransMeta();
    StepMeta two = new StepMeta( registry.getPluginId( StepPluginType.class, twoMeta ), "dummy", twoMeta );
    two.setLocation( 250, 50 );
    two.setDraw( true );
    previewMeta.addStep( two );

    TransHopMeta hop = new TransHopMeta( one, two );
    previewMeta.addTransHop( hop );

    return previewMeta;
  }
}
