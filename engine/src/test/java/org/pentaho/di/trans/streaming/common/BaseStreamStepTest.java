/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.streaming.common;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogChannelInterfaceFactory;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaDataCombi;
import org.pentaho.di.trans.streaming.api.StreamSource;
import org.pentaho.di.trans.streaming.api.StreamWindow;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class BaseStreamStepTest {
  StepMeta stepMeta;
  TransMeta transMeta;
  Trans trans;
  BaseStreamStep baseStreamStep;

  @Mock BaseStreamStepMeta meta;
  @Mock StepDataInterface stepData;
  @Mock StreamSource streamSource;
  @Mock StreamWindow streamWindow;
  @Mock LogChannelInterfaceFactory logChannelFactory;
  @Mock LogChannelInterface logChannel;


  @Before
  public void setUp() throws KettleException {
    KettleLogStore.setLogChannelInterfaceFactory( logChannelFactory );
    when( logChannelFactory.create( any(), any() ) ).thenReturn( logChannel );

    stepMeta = new StepMeta( "BaseStreamStep", meta );

    transMeta = new TransMeta();
    transMeta.addStep( stepMeta );
    trans = new Trans( transMeta );

    baseStreamStep = new BaseStreamStep( stepMeta, stepData, 1, transMeta, trans );
    baseStreamStep.source = streamSource;
    baseStreamStep.window = streamWindow;

    StepMetaDataCombi stepMetaDataCombi = new StepMetaDataCombi();
    stepMetaDataCombi.step = baseStreamStep;
    stepMetaDataCombi.data = stepData;
    stepMetaDataCombi.stepMeta = stepMeta;
    stepMetaDataCombi.meta = meta;

    trans.prepareExecution( new String[ 0 ] );
    trans.getSteps().add( stepMetaDataCombi );
  }

  @Test
  public void testStop() throws KettleException {
    Result result = new Result();
    result.setSafeStop( false );
    result.setRows( Collections.emptyList() );
    when( streamWindow.buffer( any() ) ).thenReturn( Arrays.asList( result ) );

    baseStreamStep.processRow( meta, stepData );
    assertFalse( baseStreamStep.isSafeStopped() );
    verify( streamSource ).close();
  }

  @Test
  public void testSafeStop() throws KettleException {
    Result result = new Result();
    result.setSafeStop( true );
    when( streamWindow.buffer( any() ) ).thenReturn( Arrays.asList( result ) );

    baseStreamStep.processRow( meta, stepData );
    assertTrue( baseStreamStep.isSafeStopped() );
    verify( streamSource, times( 2 ) ).close();
  }
}
