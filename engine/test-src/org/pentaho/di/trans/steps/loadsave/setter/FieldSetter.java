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
