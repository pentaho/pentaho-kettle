package org.pentaho.di.plugins.repofvs.pur.converter;

import java.util.function.Supplier;

/**
 * Calls given supplier once (if successful) and caches the result. Thread-safe.
 */
public class LazyLoader<T> implements Supplier<T> {
  private final Supplier<T> loader;
  private volatile T value;

  public LazyLoader( Supplier<T> loader ) {
    this.loader = loader;
  }

  @Override
  public T get() {
    var res = value;
    if ( res != null ) {
      return res;
    }
    synchronized ( this ) {
      if ( value == null ) {
        value = loader.get();
      }
      return value;
    }
  }


}
