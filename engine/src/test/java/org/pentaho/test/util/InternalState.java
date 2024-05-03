package org.pentaho.test.util;

import java.lang.reflect.Field;
public class InternalState {
  public static void setInternalState(Object target, String field, Object value) {
    try {
      Field f = target.getClass().getDeclaredField( field );
      f.setAccessible( true );
      f.set(target, value);
    } catch (Exception e) {
      try {
        Field f = target.getClass().getSuperclass().getDeclaredField( field );
        f.setAccessible( true );
        f.set( target, value );
      } catch ( Exception e1 ) {
        throw new RuntimeException(
          "Unable to get internal state on field: " + field + " of class: " + target + " to value: " + value, e);
      }
    }
  }

  public static Object getInternalState( Object target, String field ) {
    try {
      Field f = target.getClass().getDeclaredField( field );
      f.setAccessible( true );
      return f.get( target );
    } catch (Exception e) {
      try {
        Field f = target.getClass().getField( field );
        f.setAccessible( true );
        Class<?> t = f.getType();
        Object value = null;
        if( t == int.class ){
          value = f.getInt( null );
        } else if( t == double.class ){
          value = f.getDouble( null );
        }
        return value;
      } catch ( NoSuchFieldException | IllegalAccessException ex ) {
        throw new RuntimeException(
          "Unable to get internal state on field: " + field + " of class: " + target, e );
      }
    }
  }
}

