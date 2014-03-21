package org.pentaho.di.trans.steps.loadsave.getter;

import java.lang.reflect.Type;

public interface Getter<T> {
  public T get( Object obj );

  public Class<T> getType();

  public Type getGenericType();
}
