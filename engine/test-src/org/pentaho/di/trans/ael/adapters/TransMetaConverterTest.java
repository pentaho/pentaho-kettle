/*
 * ! ******************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
 *
 * ******************************************************************************
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * *****************************************************************************
 */

package org.pentaho.di.trans.ael.adapters;

import java.util.List;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.engine.api.model.Transformation;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.dummytrans.DummyTransMeta;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@RunWith ( MockitoJUnitRunner.class )
public class TransMetaConverterTest {

  @Spy StepMetaInterface stepMetaInterface = new DummyTransMeta();

  final String XML = "<xml></xml>";

  @Before
  public void before() throws KettleException {
    when( stepMetaInterface.getXML() ).thenReturn( XML );
  }

  @Test
  public void simpleConvert() {
    TransMeta meta = new TransMeta();
    meta.setFilename( "fileName" );
    meta.addStep( new StepMeta( "stepName", stepMetaInterface ) );
    Transformation trans = TransMetaConverter.convert( meta );
    assertThat( trans.getId(), is( meta.getFilename() ) );
    assertThat( trans.getOperations().size(), is( 1 ) );
    assertThat( trans.getOperations().get( 0 ).getId(), is( "stepName" ) );
  }

  @Test
  public void transWithHops() {
    TransMeta meta = new TransMeta();
    meta.setFilename( "fileName" );
    StepMeta from = new StepMeta( "step1",  stepMetaInterface );
    meta.addStep( from );
    StepMeta to = new StepMeta( "step2",  stepMetaInterface );
    meta.addStep( to );
    meta.addTransHop( new TransHopMeta( from, to ) );
    Transformation trans = TransMetaConverter.convert( meta );
    assertThat( trans.getId(), is( meta.getFilename() ) );
    assertThat( trans.getOperations().size(), is( 2 ) );
    assertThat( trans.getHops().size(), is( 1 ) );
    assertThat( trans.getHops().get( 0 ).getFrom().getId(), is( from.getName() ) );
    assertThat( trans.getHops().get( 0 ).getTo().getId(), is( to.getName() ) );
  }

  @Test
  public void transIdFromRepo() throws Exception {
    TransMeta meta = new TransMeta();
    meta.setName( "transName" );
    Transformation trans = TransMetaConverter.convert( meta );
    assertThat( trans.getId(), is( "/transName" ) );
  }

  @Test
  public void testDisabledHops() {
    TransMeta trans = new TransMeta();
    StepMeta startStep = new StepMeta( "StartStep", stepMetaInterface );
    trans.addStep( startStep );
    StepMeta withEnabledHop = new StepMeta( "WithEnabledHop", stepMetaInterface );
    trans.addStep( withEnabledHop );
    StepMeta withDisabledHop = new StepMeta( "WithDisabledHop", stepMetaInterface );
    trans.addStep( withDisabledHop );
    StepMeta shouldStay = new StepMeta( "ShouldStay", stepMetaInterface );
    trans.addStep( shouldStay );
    StepMeta shouldNotStay = new StepMeta( "ShouldNotStay", stepMetaInterface );
    trans.addStep( shouldNotStay );

    trans.addTransHop( new TransHopMeta( startStep, withEnabledHop ) );
    trans.addTransHop( new TransHopMeta( startStep, withDisabledHop, false ) );
    trans.addTransHop( new TransHopMeta( withEnabledHop, shouldStay ) );
    trans.addTransHop( new TransHopMeta( withDisabledHop, shouldStay ) );
    trans.addTransHop( new TransHopMeta( withDisabledHop, shouldNotStay ) );

    Transformation transformation = TransMetaConverter.convert( trans );

    List<String>
        steps =
        transformation.getOperations().stream().map( op -> op.getId() ).collect( Collectors.toList() );
    assertThat( "Only 3 ops should exist", steps.size(), is( 3 ) );
    assertThat( steps, hasItems( "StartStep", "WithEnabledHop" ) );

    List<String> hops = transformation.getHops().stream().map( hop -> hop.getId() ).collect( Collectors.toList() );
    assertThat( "Only 2 hops should exist", hops.size(), is( 2 ) );
    assertThat( hops, hasItems( "StartStep -> WithEnabledHop", "WithEnabledHop -> ShouldStay" ) );
  }
}
