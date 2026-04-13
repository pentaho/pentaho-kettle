package org.pentaho.di.core.util;

import java.util.function.Supplier;

/**
 * Calls given supplier once (if non-null) and caches the result. Thread-safe.
 * <br>
 * Null values will not be cached. If you wish to use this with a supplier
 * that returns null, consider wrapping it in an {@link java.util.Optional Optional}
 */
public class LazyLoader<T> implements Supplier<T> {
  private final Supplier<T> loader;

    // yes, sonar, volatile doesn't make T thread-safe...
  @SuppressWarnings( "java:S3077" )
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
