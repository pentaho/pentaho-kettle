/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.csvinput;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class NamedFieldsMappingTest {

  private NamedFieldsMapping fieldsMapping;

  @Before
  public void before() {
    Map<Integer, Integer> actualToMetaFieldsMap = new HashMap<>();
    actualToMetaFieldsMap.put( 0, 3 );
    actualToMetaFieldsMap.put( 1, 4 );
    fieldsMapping = new NamedFieldsMapping( actualToMetaFieldsMap );
  }

  @Test
  public void fieldMetaIndex() {
    assertEquals( 3, fieldsMapping.fieldMetaIndex( 0 ) );
  }

  @Test
  public void fieldMetaIndexWithUnexistingField() {
    assertEquals( FieldsMapping.FIELD_DOES_NOT_EXIST, fieldsMapping.fieldMetaIndex( 4 ) );
  }

  @Test
  public void size() {
    assertEquals( 2, fieldsMapping.size() );
  }

  @Test
  public void mapping() {
    NamedFieldsMapping mapping =
        NamedFieldsMapping.mapping( new String[] { "FIRST", "SECOND", "THIRD" }, new String[] { "SECOND", "THIRD" } );
    assertEquals( 0, mapping.fieldMetaIndex( 1 ) );
  }

}
