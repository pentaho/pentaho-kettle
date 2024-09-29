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


package org.pentaho.di.trans.steps.loadsave.getter;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

public class FieldGetter<T> implements Getter<T> {
  private final Field field;

  public FieldGetter( Field field ) {
    this.field = field;
  }

  @SuppressWarnings( "unchecked" )
  @Override
  public T get( Object obj ) {
    try {
      return (T) field.get( obj );
    } catch ( Exception e ) {
      throw new RuntimeException( "Error getting " + field + " on " + obj, e );
    }
  }

  @SuppressWarnings( "unchecked" )
  @Override
  public Class<T> getType() {
    return (Class<T>) field.getType();
  }

  @Override
  public Type getGenericType() {
    return field.getGenericType();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ( ( field == null ) ? 0 : field.hashCode() );
    return result;
  }

  @Override
  public boolean equals( Object obj ) {
    if ( this == obj ) {
      return true;
    }
    if ( obj == null ) {
      return false;
    }
    if ( getClass() != obj.getClass() ) {
      return false;
    }
    FieldGetter<?> other = (FieldGetter<?>) obj;
    if ( field == null ) {
      if ( other.field != null ) {
        return false;
      }
    } else {
      if ( !field.equals( other.field ) ) {
        return false;
      }
    }
    return true;
  }

}
