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

import java.util.HashMap;
import java.util.Map;

public class NamedFieldsMapping implements FieldsMapping {

  private final Map<Integer, Integer> actualToMetaFieldMapping;

  public NamedFieldsMapping( Map<Integer, Integer> actualToMetaFieldMapping ) {
    this.actualToMetaFieldMapping = actualToMetaFieldMapping;
  }

  @Override
  public int fieldMetaIndex( int index ) {
    Integer metaIndex = actualToMetaFieldMapping.get( index );
    return metaIndex == null ? FieldsMapping.FIELD_DOES_NOT_EXIST : metaIndex.intValue();
  }

  @Override
  public int size() {
    return actualToMetaFieldMapping.size();
  }

  public static NamedFieldsMapping mapping( String[] actualFieldNames, String[] metaFieldNames ) {
    Map<String, Integer> metaNameToIndex = new HashMap<>();
    for ( int j = 0; j < metaFieldNames.length; j++ ) {
      metaNameToIndex.put( metaFieldNames[j], Integer.valueOf( j ) );
    }
    Map<Integer, Integer> actualToMetaFieldMapping = new HashMap<>();
    for ( int i = 0; i < actualFieldNames.length; i++ ) {
      actualToMetaFieldMapping.put( i, metaNameToIndex.get( actualFieldNames[i] ) );
    }
    return new NamedFieldsMapping( actualToMetaFieldMapping );
  }

}
