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


import org.junit.Test;
import org.mockito.Mock;
import org.pentaho.di.engine.api.model.Transformation;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


public class TransMetaConverterTest {

  @Mock StepMetaInterface stepMetaInterface;

  @Test
  public void simpleConvert() {
    TransMeta meta = new TransMeta();
    meta.setName( "transName" );
    meta.addStep( new StepMeta( "stepName", null ) );
    Transformation trans = TransMetaConverter.convert( meta );
    assertThat( trans.getId(), is( meta.getName() ) );
    assertThat( trans.getOperations().size(), is( 1 ) );
    assertThat( trans.getOperations().get( 0 ).getId(), is( "stepName" ) );
  }

}
