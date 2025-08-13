package org.pentaho.di.core.ssh;

import org.pentaho.di.core.Props;

public class SshImplementationSelector {
  private SshImplementationSelector() { }

  public static final String PROP_KEY = "kettle.ssh.impl"; // value: trilead|mina

  public static SshImplementation resolve() {
    String v = System.getProperty( PROP_KEY );
    if ( v == null ) {
      try {
        if ( Props.isInitialized() ) {
          v = Props.getInstance().getProperty( PROP_KEY );
        }
      } catch ( Exception ignored ) {
      }
    }
    if ( v == null ) {
      return SshImplementation.TRILEAD;
    }
    v = v.trim().toLowerCase();
    if ( "mina".equals( v ) ) {
      return SshImplementation.MINA;
    }
    return SshImplementation.TRILEAD;
  }
}
