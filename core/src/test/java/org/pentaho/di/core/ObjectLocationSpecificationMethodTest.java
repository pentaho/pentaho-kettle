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

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class ObjectLocationSpecificationMethodTest {
  @Test
  public void testClass() {
    ObjectLocationSpecificationMethod[] values = ObjectLocationSpecificationMethod.values();
    List<String> descriptions = Arrays.asList( ObjectLocationSpecificationMethod.FILENAME.getDescriptions() );

    for ( ObjectLocationSpecificationMethod method : values ) {
      assertNotNull( method.getCode() );
      assertFalse( method.getCode().isEmpty() );
      assertNotNull( method.getDescription() );
      assertFalse( method.getDescription().isEmpty() );
      assertTrue( descriptions.contains( method.getDescription() ) );
      assertEquals( ObjectLocationSpecificationMethod.getSpecificationMethodByCode( method.getCode() ), method );
      assertEquals( ObjectLocationSpecificationMethod.getSpecificationMethodByDescription( method.getDescription() ),
          method );
    }

    assertEquals( values.length, descriptions.size() );
    assertNull( ObjectLocationSpecificationMethod.getSpecificationMethodByCode( "Invalid code" ) );
    assertNull( ObjectLocationSpecificationMethod.getSpecificationMethodByDescription( "Invalid description" ) );
  }
}
