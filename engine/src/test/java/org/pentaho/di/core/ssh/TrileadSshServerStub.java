package org.pentaho.di.core.ssh;

/**
 * A simple stub for testing Trilead SSH connections.
 * Since Trilead SSH2 build213 is incompatible with modern MINA SSHD servers,
 * we use a mock approach for testing the Trilead implementation.
 */
public class TrileadSshServerStub {

  public static void validateTrileadImplementation() {
    // For now, we'll create a basic validation that the Trilead classes exist
    // and can be instantiated without throwing runtime errors
    try {
      Class.forName( "com.trilead.ssh2.Connection" );
      Class.forName( "com.trilead.ssh2.SFTPv3Client" );
      System.out.println( "Trilead SSH2 classes are available and can be loaded" );
    } catch ( ClassNotFoundException e ) {
      throw new RuntimeException( "Trilead SSH2 classes not found", e );
    }
  }
}
