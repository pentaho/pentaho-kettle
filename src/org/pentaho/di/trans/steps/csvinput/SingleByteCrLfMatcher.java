package org.pentaho.di.trans.steps.csvinput;

public class SingleByteCrLfMatcher implements CrLfMatcherInterface {

  @Override
  public boolean isReturn(byte[] source, int location) {
    return source[location] == '\n';
  }

  @Override
  public boolean isLineFeed(byte[] source, int location) {
    return source[location] == '\r';
  }


}
