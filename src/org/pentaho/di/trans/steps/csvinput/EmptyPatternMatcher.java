package org.pentaho.di.trans.steps.csvinput;

public class EmptyPatternMatcher implements PatternMatcherInterface {

  public boolean matchesPattern(byte[] source, int location, byte[] pattern) {
    return false;
  }
  
}
