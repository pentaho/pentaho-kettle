package org.pentaho.di.trans.steps.csvinput;

public class MultiBytePatternMatcher implements PatternMatcherInterface {

  public boolean matchesPattern(byte[] source, int location, byte[] pattern) {
    if (location>=pattern.length-1) {
      int start = location-pattern.length+1;
      for (int i=0;i<pattern.length;i++) {
        if (source[start+i] != pattern[i]) {
          return false;
        }
      }
      return true;
    } else {
      return false;
    }
  }
  
}
