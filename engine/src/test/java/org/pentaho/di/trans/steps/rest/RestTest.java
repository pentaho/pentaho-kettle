/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.rest;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.junit.Test;
import org.pentaho.di.core.util.Assert;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.mockito.Mockito;

public class RestTest {
  @Test
  public void testCreateMultivalueMap() {
    StepMeta stepMeta = new StepMeta();
    stepMeta.setName( "TestRest" );
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "TestRest" );
    transMeta.addStep( stepMeta );
    Rest rest = new Rest( stepMeta, Mockito.mock( StepDataInterface.class ),
      1, transMeta, Mockito.mock( Trans.class ) );
    MultivaluedMapImpl map = rest.createMultivalueMap( "param1", "{a:{[val1]}}" );
    String val1 = map.getFirst( "param1" );
    Assert.assertTrue( val1.contains( "%7D" ) );
  }
}
