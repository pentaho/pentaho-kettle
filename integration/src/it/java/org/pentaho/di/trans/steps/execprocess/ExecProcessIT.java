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

package org.pentaho.di.trans.steps.execprocess;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;

public class ExecProcessIT {

  private ExecProcess execStep;
  private ExecProcessData execData;
  private ExecProcessMeta execMeta;
  private StepMeta stepMeta;
  private TransMeta transMeta;
  private Trans trans;

  @BeforeClass
  public static void setUpBeforeClass() throws KettleException {
    KettleEnvironment.init( false );
  }

  @Before
  public void setUp() {
    execMeta = mock( ExecProcessMeta.class );
    execData = new ExecProcessData();
    stepMeta = mock( StepMeta.class );
    transMeta = mock( TransMeta.class );
    trans = mock( Trans.class );

    when( stepMeta.getName() ).thenReturn( "The Step Name" );
    when( stepMeta.getTargetStepPartitioningMeta() ).thenReturn( null );
    when( transMeta.findStep( "The Step Name" ) ).thenReturn( stepMeta );
    when( trans.getLogLevel() ).thenReturn( LogLevel.BASIC );
  }

  @Test
  public void testInit() {
    execStep = new ExecProcess( stepMeta, execData, 0, transMeta, trans );
    assertFalse( execStep.init( execMeta, execData ) );

    when( execMeta.getResultFieldName() ).thenReturn( UUID.randomUUID().toString() );
    assertTrue( execStep.init( execMeta, execData ) );
    assertNotNull( execData.runtime );
  }
}
