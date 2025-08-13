package org.pentaho.di.core.ssh;

import java.io.IOException;

public interface SshConnection extends AutoCloseable {
  void connect() throws Exception;

  ExecResult exec( String command, long timeoutMs ) throws IOException, Exception;

  SftpSession openSftp() throws IOException, Exception;

  @Override
  void close();
}
