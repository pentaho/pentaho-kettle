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


package org.pentaho.di.trans.steps.loadsave.setter;

import java.lang.reflect.Field;

public class FieldSetter<T> implements Setter<T> {
  private final Field field;

  public FieldSetter( Field field ) {
    this.field = field;
  }

  public void set( Object obj, T value ) {
    try {
      field.set( obj, value );
    } catch ( Exception e ) {
      throw new RuntimeException( "Error getting " + field + " on " + obj, e );
    }
  }
}
