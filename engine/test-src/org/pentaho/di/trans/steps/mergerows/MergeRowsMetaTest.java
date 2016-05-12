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

package org.pentaho.di.trans.steps.mergerows;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class MergeRowsMetaTest {


  @Test
  public void cloneTest() throws Exception {
    MergeRowsMeta meta = new MergeRowsMeta();
    meta.allocate( 2, 3 );
    meta.setKeyFields( new String[] { "key1", "key2" } );
    meta.setValueFields( new String[] { "value1", "value2" } );
    // scalars should be cloned using super.clone() - makes sure they're calling super.clone()
    meta.setFlagField( "randomFlagField" );
    MergeRowsMeta aClone = (MergeRowsMeta) meta.clone();
    assertFalse( aClone == meta ); // Make sure that return object isn't the same object
    assertTrue( Arrays.equals( meta.getKeyFields(), aClone.getKeyFields() ) );
    assertTrue( Arrays.equals( meta.getValueFields(), aClone.getValueFields() ) );
    assertEquals( meta.getFlagField(), aClone.getFlagField() );
  }

}
