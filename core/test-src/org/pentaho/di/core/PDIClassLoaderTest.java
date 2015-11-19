package org.pentaho.di.core;

import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class PDIClassLoaderTest {
  @Test
  public void testConstructor() throws MalformedURLException {
    ClassLoader parent = mock( ClassLoader.class );
    PDIClassLoader cl = new PDIClassLoader( parent );
    assertSame( cl.getParent(), parent );
    URL[] urls = new URL[]{ new URL( "file://" ) };
    cl = new PDIClassLoader( urls, parent );
    assertSame( cl.getParent(), parent );
    assertEquals( 1, cl.getURLs().length );
    assertSame( urls[0], cl.getURLs()[0] );
  }

  @Test
  public void testLoadClass() throws Exception {
    final String classToLoad = "dummy.Class";
    final AtomicBoolean loadClassCalled = new AtomicBoolean();
    ClassLoader parent = new ClassLoader() {
      @Override protected Class<?> loadClass( String name, boolean resolve ) throws ClassNotFoundException {
        if ( name.equals( classToLoad ) && !resolve ) {
          loadClassCalled.set( true );
        }
        throw new NoClassDefFoundError();
      }
    };
    PDIClassLoader cl = new PDIClassLoader( parent );
    try {
      cl.loadClass( classToLoad, true );
      fail( "This class doesn't exist" );
    } catch ( ClassNotFoundException cnfe ) {
      assertTrue( loadClassCalled.get() );
    }
  }
}
