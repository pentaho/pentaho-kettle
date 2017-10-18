/*
 * ! ******************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.engine.api.ExecutionContext;
import org.pentaho.di.engine.api.model.Operation;
import org.pentaho.di.engine.api.model.Rows;
import org.pentaho.di.engine.api.reporting.Metrics;
import org.pentaho.di.engine.api.reporting.Status;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;

import java.util.function.Consumer;

import static org.hamcrest.CoreMatchers.any;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class StepInterfaceEngineAdapterTest {

  @Mock Operation op;
  @Mock ExecutionContext executionContext;
  @Mock StepMeta stepMeta;
  @Mock TransMeta transMeta;
  @Mock StepDataInterface dataInterface;
  @Mock Trans tran;
  StepInterfaceEngineAdapter stepInterfaceEngineAdapter;

  @Before
  public void before() {
    when( stepMeta.getName() ).thenReturn( "foo" );
    stepInterfaceEngineAdapter =
      new StepInterfaceEngineAdapter( op, executionContext, stepMeta, transMeta, dataInterface, tran );
  }

  @Test
  public void verifyEventSubscription() {
    verify( executionContext ).subscribe(
      argThat( equalTo( op ) ), argThat( equalTo( Metrics.class ) ), argThat( any( Consumer.class ) ) );
    verify( executionContext ).subscribe(
      argThat( equalTo( op ) ), argThat( equalTo( Status.class ) ), argThat( any( Consumer.class ) ) );
    verify( executionContext ).subscribe(
      argThat( equalTo( op ) ), argThat( equalTo( Rows.class ) ), argThat( any( Consumer.class ) ) );
  }

}
