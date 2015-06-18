package org.pentaho.di.trans.steps.loadsave.setter;

public interface Setter<T> {
  public void set( Object obj, T value );
}
