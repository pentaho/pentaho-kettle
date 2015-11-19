package org.pentaho.di.core;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * //TODO Add some javadoc or remove this comment
 *
 * @author Pavel Sakun
 */
public class JndiUtilTest {

  @Test
  public void testInitJNDI() throws Exception {
    final String factoryInitialKey = "java.naming.factory.initial";
    final String factoryInitialBak = System.getProperty( factoryInitialKey );
    final String sjRootKey = "org.osjava.sj.root";
    final String sjRootBak = System.getProperty( sjRootKey );
    final String sjDelimiterKey = "org.osjava.sj.root";
    final String sjDelimiterBak = System.getProperty( sjDelimiterKey );

    System.clearProperty( factoryInitialKey );
    System.clearProperty( sjRootKey );
    System.clearProperty( sjDelimiterKey );

    JndiUtil.initJNDI();

    try {
      assertFalse( System.getProperty( factoryInitialKey ).isEmpty() );
      assertFalse( System.getProperty( sjRootKey ).isEmpty() );
      assertFalse( System.getProperty( sjDelimiterKey ).isEmpty() );
      assertEquals( System.getProperty( sjRootKey ), Const.JNDI_DIRECTORY );
    } finally {
      if ( factoryInitialBak != null ) {
        System.setProperty( factoryInitialKey, factoryInitialBak );
      }
      if ( sjRootBak != null ) {
        System.setProperty( sjRootKey, sjRootBak );
      }
      if ( sjDelimiterBak != null ) {
        System.setProperty( sjDelimiterKey, sjDelimiterBak );
      }
    }
  }
}
