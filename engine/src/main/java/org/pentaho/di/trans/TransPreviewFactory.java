/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
