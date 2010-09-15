package org.pentaho.di.trans.steps.csvinput;

public class MultiByteLittleCrLfMatcher implements CrLfMatcherInterface {
  
  @Override
  public boolean isReturn(byte[] source, int location) {
    if (location >= 1) {
      return source[location - 1] == 0x0d && source[location] == 0x00;
    } else {
      return false;
    }
  }

  @Override
  public boolean isLineFeed(byte[] source, int location) {
    if (location >= 1) {
      return source[location - 1] == 0x0a && source[location] == 0x00;
    } else {
      return false;
    }
  }

}