package org.pentaho.di.core.ssh;

public class MinaSshConnectionContractTest extends AbstractSshConnectionContractTest {
  @Override
  protected SshImplementation getImplementation() {
    return SshImplementation.MINA;
  }
}
