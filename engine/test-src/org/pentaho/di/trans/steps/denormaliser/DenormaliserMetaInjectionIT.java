/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.denormaliser;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;

public class DenormaliserMetaInjectionIT {

  private static final String EXPECTED_VALUE = " 1";

  private TransMeta transMeta;

  private Trans trans;

  @BeforeClass
  public static void initKettle() throws Exception {
    KettleEnvironment.init( false );
  }

  @Before
  public void setUp() throws Exception {
    transMeta = new TransMeta( "test-src/org/pentaho/di/trans/steps/denormaliser/pdi-11947.ktr" );
    transMeta.setTransformationType( TransMeta.TransformationType.Normal );
    trans = new Trans( transMeta );
  }

  @Test
  public void addedFieldsAvalibleOutsideInjectedTransformation() throws Exception {
    runTransformation( trans );
    String actualValue = trans.getVariable( "X_FIELD_VALUE" );
    assertEquals( EXPECTED_VALUE, actualValue );
  }

  private static void runTransformation( Trans trans ) throws Exception {
    trans.prepareExecution( null );
    trans.startThreads();
    trans.waitUntilFinished();
    assertEquals( 0, trans.getErrors() );
  }

}
