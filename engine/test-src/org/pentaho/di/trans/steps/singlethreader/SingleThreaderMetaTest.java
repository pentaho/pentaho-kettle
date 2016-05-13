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
package org.pentaho.di.trans.steps.singlethreader;

import org.junit.Test;
import java.util.Arrays;
import static org.junit.Assert.*;

public class SingleThreaderMetaTest {

  @Test
  public void cloneTest() throws Exception {
    SingleThreaderMeta meta = new SingleThreaderMeta();
    meta.allocate( 2 );
    meta.setParameters( new String[] { "pname1", "pname2" } );
    meta.setParameterValues( new String[] { "pvalue1", "pvalue2" } );
    // scalars should be cloned using super.clone() - makes sure they're calling super.clone()
    meta.setTransName( "testTransName" );
    SingleThreaderMeta aClone = (SingleThreaderMeta) meta.clone();
    assertFalse( aClone == meta );
    assertTrue( Arrays.equals( meta.getParameters(), aClone.getParameters() ) );
    assertTrue( Arrays.equals( meta.getParameterValues(), aClone.getParameterValues() ) );
    assertEquals( meta.getTransName(), aClone.getTransName() );
  }

}
