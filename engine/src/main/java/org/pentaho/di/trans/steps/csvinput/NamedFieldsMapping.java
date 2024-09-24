/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
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
    int[] actualToMetaFieldMapping = new int[ actualFieldNames == null ? 0 : actualFieldNames.length];

    for ( int i = 0; i < metaFieldNames.length; i++ ) {
      List<Integer> coll = metaNameToIndex.getOrDefault( metaFieldNames[i], new ArrayList<>() );
      coll.add( i );
      metaNameToIndex.put( metaFieldNames[i], coll );
    }

    if ( actualFieldNames != null ) {
      for ( int i = 0; i < actualFieldNames.length; i++ ) {
        List<Integer> columnIndexes = metaNameToIndex.get( actualFieldNames[ i ] );
        if ( columnIndexes == null || columnIndexes.isEmpty() ) {
          unmatchedMetaFields.add( i );
          actualToMetaFieldMapping[ i ] = FIELD_DOES_NOT_EXIST;
          continue;
        }
        actualToMetaFieldMapping[ i ] = columnIndexes.remove( 0 );
      }
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
