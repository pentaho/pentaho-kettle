package org.pentaho.di.trans.steps.csvinput;

public class SingleBytePatternMatcher implements PatternMatcherInterface {

  public boolean matchesPattern(byte[] source, int location, byte[] pattern) {
    return source[location] == pattern[0];
  }
  
}
