package org.pentaho.di.trans.steps.csvinput;

public class SingleByteCrLfMatcher implements CrLfMatcherInterface {

  public boolean isReturn(byte[] source, int location) {
    return source[location] == '\n';
  }

  public boolean isLineFeed(byte[] source, int location) {
    return source[location] == '\r';
  }


}
