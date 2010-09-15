package org.pentaho.di.trans.steps.csvinput;

public interface CrLfMatcherInterface {
  public boolean isReturn(byte[] source, int location);
  public boolean isLineFeed(byte[] source, int location);
}
