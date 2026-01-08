package org.pentaho.di.plugins.repofvs.pur.converter;

/** Switch context ClassLoader in a try-with-resources */
public class WithClassLoader implements AutoCloseable {
    private final ClassLoader originalClassLoader;

    public WithClassLoader( ClassLoader runClassLoader ) {
      this.originalClassLoader = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader( runClassLoader );
    }

    @Override
    public void close() {
      Thread.currentThread().setContextClassLoader( originalClassLoader );
    }

  }