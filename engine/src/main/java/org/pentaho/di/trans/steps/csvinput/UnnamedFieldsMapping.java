/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.trans.steps.csvinput;

public class UnnamedFieldsMapping implements FieldsMapping {

  private final int fieldsCount;

  public UnnamedFieldsMapping( int fieldsCount ) {
    this.fieldsCount = fieldsCount;
  }

  @Override
  public int fieldMetaIndex( int index ) {
    return ( index >= fieldsCount || index < 0 ) ? FieldsMapping.FIELD_DOES_NOT_EXIST : index;
  }

  @Override
  public int size() {
    return fieldsCount;
  }

  public static UnnamedFieldsMapping mapping( int fieldsCount ) {
    return new UnnamedFieldsMapping( fieldsCount );
  }

}
