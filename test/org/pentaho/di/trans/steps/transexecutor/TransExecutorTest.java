/*
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
 *
 * **************************************************************************
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
 */

package org.pentaho.di.trans.steps.transexecutor;


import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.RowMetaAndData;
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
import org.pentaho.di.trans.steps.dummytrans.DummyTransMeta;
import org.pentaho.di.trans.steps.injector.InjectorMeta;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class TransExecutorTest {

  @Test
  public void testSubTrans() throws Exception {
    KettleEnvironment.init();

    // CREATE
    // ...transformation
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "transformation executor" );
    PluginRegistry registry = PluginRegistry.getInstance();

    // ...injector step
    String injectorStepname = "injector step";
    InjectorMeta im = new InjectorMeta();
    String injectorPid = registry.getPluginId( StepPluginType.class, im );
    StepMeta injectorStep = new StepMeta( injectorPid, injectorStepname, im );
    transMeta.addStep( injectorStep );

    // ...Transformation Executor step
    String transExecutorStepname = "TransExecutor";
    TransExecutorMeta transExecutorMeta = new TransExecutorMeta();
    String transExecutorPID = registry.getPluginId( StepPluginType.class, transExecutorMeta );
    StepMeta transExecStep = new StepMeta( transExecutorPID, transExecutorStepname, transExecutorMeta );
    transMeta.addStep( transExecStep );

    // ..dummy output step
    String dummyStepname = "Dummy Output";
    DummyTransMeta dummyTransMeta = new DummyTransMeta();
    String dummyStepPID = registry.getPluginId( StepPluginType.class, dummyTransMeta );
    StepMeta dummyStep = new StepMeta( dummyStepPID, dummyStepname, dummyTransMeta );
    transMeta.addStep( dummyStep );

    // INIT
    String subtrans = getClass().getResource( "subtrans.ktr" ).toString();
    transExecutorMeta.setFileName( subtrans );
    transExecutorMeta.setSpecificationMethod( ObjectLocationSpecificationMethod.FILENAME );
    transExecutorMeta.setOutputRowsSourceStepMeta( dummyStep );

    // hops :
    // injector -> trans executor
    TransHopMeta injectorExecutorHop = new TransHopMeta( injectorStep, transExecStep );
    transMeta.addTransHop( injectorExecutorHop );
    // trans executor -> dummy output
    TransHopMeta executorDummyHop = new TransHopMeta( transExecStep, dummyStep );
    transMeta.addTransHop( executorDummyHop );

    // Now execute the transformation...
    Trans trans = new Trans( transMeta );

    trans.prepareExecution( null );

    StepInterface si = trans.getStepInterface( transExecutorStepname, 0 );
    RowStepCollector endRc = new RowStepCollector();
    si.addRowListener( endRc );

    RowProducer rp = trans.addRowProducer( injectorStepname, 0 );

    trans.startThreads();

    // add rows
    List<RowMetaAndData> inputList = createData();
    Iterator<RowMetaAndData> it = inputList.iterator();
    while ( it.hasNext() ) {
      RowMetaAndData rm = it.next();
      rp.putRow( rm.getRowMeta(), rm.getData() );
    }
    rp.finished();
    trans.waitUntilFinished();

    long errors = trans.getResult().getNrErrors();
    assertEquals( "Transformation fails", 0, errors );
  }

  private List<RowMetaAndData> createData() {
    List<RowMetaAndData> list = new ArrayList<RowMetaAndData>();
    RowMetaInterface rm = createRowMetaInterface();
    Object[] r1 = new Object[] { "abc" };
    list.add( new RowMetaAndData( rm, r1 ) );
    return list;
  }

  private RowMetaInterface createRowMetaInterface() {
    RowMetaInterface rm = new RowMeta();
    ValueMetaInterface[] valuesMeta = { new ValueMeta( "field1", ValueMeta.TYPE_STRING ), };
    for ( int i = 0; i < valuesMeta.length; i++ ) {
      rm.addValueMeta( valuesMeta[i] );
    }
    return rm;
  }


}
