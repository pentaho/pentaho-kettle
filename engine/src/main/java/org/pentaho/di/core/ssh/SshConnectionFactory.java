package org.pentaho.di.core.ssh;

import org.pentaho.di.core.ssh.mina.MinaSshConnection;
import org.pentaho.di.core.ssh.trilead.TrileadSshConnection;

public interface SshConnectionFactory {
  SshConnection open( SshConfig config ) throws SshConnectionException;

  static SshConnectionFactory defaultFactory() {
    return config -> {
      SshImplementation impl = config.getImplementation() != null ? config.getImplementation()
        : SshImplementationSelector.resolve();
      switch ( impl ) {
        case MINA:
          return new MinaSshConnection( config );
        case TRILEAD:
          return new TrileadSshConnection( config );
        default:
          throw new SshConnectionException( "Unknown SSH implementation: " + impl );
      }
    };
  }
}
