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

package org.pentaho.di.trans.steps.mapping;

import junit.framework.Assert;

import org.junit.Test;

public class MappingValueRenameTest {

  /**
   * PDI-11420 target and source values does not becomes nulls.
   */
  @Test
  public void testMappingValueRenameNullAwareSetters() {
    MappingValueRename mr = new MappingValueRename( "a", "b" );

    Assert.assertEquals( "Source value name is correct", "a", mr.getSourceValueName() );
    Assert.assertEquals( "Target value name is correct", "b", mr.getTargetValueName() );

    mr.setSourceValueName( null );
    mr.setTargetValueName( null );

    Assert.assertEquals( "Source value name is set to empty String", "", mr.getSourceValueName() );
    Assert.assertEquals( "Target value name is set to empty String", "", mr.getTargetValueName() );

    mr.setSourceValueName( "c" );
    mr.setTargetValueName( "d" );

    Assert.assertEquals( "Source value name is correct", "c", mr.getSourceValueName() );
    Assert.assertEquals( "Target value name is correct", "d", mr.getTargetValueName() );
  }

}
