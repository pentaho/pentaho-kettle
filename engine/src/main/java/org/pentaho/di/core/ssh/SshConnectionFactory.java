package org.pentaho.di.core.ssh;

import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.ssh.mina.MinaSshConnection;
import org.pentaho.di.core.ssh.trilead.TrileadSshConnection;

public interface SshConnectionFactory {
  SshConnection open( SshConfig config ) throws SshConnectionException;

  SshConnection open( SshConfig config, LogChannelInterface log ) throws SshConnectionException;

  static SshConnectionFactory defaultFactory() {
    return new SshConnectionFactory() {
      @Override
      public SshConnection open( SshConfig config ) throws SshConnectionException {
        return open( config, null );
      }

      @Override
      public SshConnection open( SshConfig config, LogChannelInterface log ) throws SshConnectionException {
        SshImplementation impl = config.getImplementation() != null ? config.getImplementation()
          : SshImplementationSelector.resolve();
        switch ( impl ) {
          case MINA:
            return log != null ? new MinaSshConnection( config, log ) : new MinaSshConnection( config );
          case TRILEAD:
            return log != null ? new TrileadSshConnection( config, log ) : new TrileadSshConnection( config );
          default:
            throw new SshConnectionException( "Unknown SSH implementation: " + impl );
        }
      }
    };
  }
}
