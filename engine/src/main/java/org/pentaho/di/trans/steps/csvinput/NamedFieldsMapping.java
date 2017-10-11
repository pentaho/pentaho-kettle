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

package org.pentaho.di.trans.steps.csvinput;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

public class NamedFieldsMapping implements FieldsMapping {

  private final int[] actualToMetaFieldMapping;

  public NamedFieldsMapping( int[] actualToMetaFieldMapping ) {
    this.actualToMetaFieldMapping = actualToMetaFieldMapping;
  }

  @Override
  public int fieldMetaIndex( int index ) {
    if ( index >= size() || index < 0 ) {
      return FIELD_DOES_NOT_EXIST;
    }
    return actualToMetaFieldMapping[index];
  }

  @Override
  public int size() {
    return actualToMetaFieldMapping.length;
  }

  public static NamedFieldsMapping mapping( String[] actualFieldNames, String[] metaFieldNames ) {
    LinkedHashMap<String, List<Integer>> metaNameToIndex = new LinkedHashMap<>();
    List<Integer> unmatchedMetaFields = new ArrayList<>();
    int[] actualToMetaFieldMapping = new int[actualFieldNames.length];

    for ( int i = 0; i < metaFieldNames.length; i++ ) {
      List<Integer> coll = metaNameToIndex.getOrDefault( metaFieldNames[i], new ArrayList<>() );
      coll.add( i );
      metaNameToIndex.put( metaFieldNames[i], coll );
    }

    for ( int i = 0; i < actualFieldNames.length; i++ ) {
      List<Integer> columnIndexes = metaNameToIndex.get( actualFieldNames[i] );
      if ( columnIndexes == null || columnIndexes.isEmpty() ) {
        unmatchedMetaFields.add( i );
        actualToMetaFieldMapping[i] = FIELD_DOES_NOT_EXIST;
        continue;
      }
      actualToMetaFieldMapping[i] = columnIndexes.remove( 0 );
    }

    Iterator<Integer> remainingMetaIndexes = metaNameToIndex.values().stream()
      .flatMap( List::stream )
      .sorted()
      .iterator();

    for ( int idx : unmatchedMetaFields ) {
      if ( !remainingMetaIndexes.hasNext() ) {
        break;
      }
      actualToMetaFieldMapping[ idx ] = remainingMetaIndexes.next();
    }

    return new NamedFieldsMapping( actualToMetaFieldMapping );
  }

}
