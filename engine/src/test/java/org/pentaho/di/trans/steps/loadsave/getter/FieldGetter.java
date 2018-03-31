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
