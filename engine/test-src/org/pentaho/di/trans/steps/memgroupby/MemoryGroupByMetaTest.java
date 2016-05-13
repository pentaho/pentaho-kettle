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
package org.pentaho.di.trans.steps.memgroupby;

import org.junit.Test;
import java.util.Arrays;
import static org.junit.Assert.*;

public class MemoryGroupByMetaTest {

  @Test
  public void testClone() throws Exception {
    MemoryGroupByMeta meta = new MemoryGroupByMeta();
    meta.allocate( 2, 3 );
    meta.setGroupField( new String[] { "group1", "group2" } );
    meta.setAggregateField( new String[] { " agg1", "agg2" } );
    meta.setSubjectField( new String[] { "subj1", "subj2" } );
    meta.setAggregateType( new int[] { 10, 20 } );
    meta.setValueField( new String[] { "value1", "value2" } );
    MemoryGroupByMeta aClone = (MemoryGroupByMeta) meta.clone();
    assertFalse( meta == aClone );
    assertTrue( Arrays.equals( meta.getGroupField(), aClone.getGroupField() ) );
    assertTrue( Arrays.equals( meta.getAggregateField(), aClone.getAggregateField() ) );
    assertTrue( Arrays.equals( meta.getSubjectField(), aClone.getSubjectField() ) );
    assertTrue( Arrays.equals( meta.getAggregateType(), aClone.getAggregateType() ) );
    assertTrue( Arrays.equals( meta.getValueField(), aClone.getValueField() ) );
  }

}
