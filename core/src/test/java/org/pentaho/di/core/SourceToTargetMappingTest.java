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
package org.pentaho.di.core;

import org.junit.Test;

import static org.junit.Assert.*;

public class SourceToTargetMappingTest {

  @Test
  public void testClass() throws Exception {
    SourceToTargetMapping mapping = new SourceToTargetMapping( 2, 3 );
    assertEquals( 2, mapping.getSourcePosition() );
    assertEquals( 3, mapping.getTargetPosition() );
    mapping.setSourcePosition( 0 );
    mapping.setTargetPosition( 1 );
    assertEquals( 0, mapping.getSourcePosition() );
    assertEquals( 1, mapping.getTargetPosition() );
    assertEquals( "foo", mapping.getSourceString( new String[]{ "foo", "bar" } ) );
    assertEquals( "bar", mapping.getTargetString( new String[]{ "foo", "bar" } ) );
  }
}
