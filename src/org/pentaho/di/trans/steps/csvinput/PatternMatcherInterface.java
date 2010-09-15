package org.pentaho.di.trans.steps.csvinput;

public interface PatternMatcherInterface {
  public boolean matchesPattern(byte[] source, int location, byte[] pattern);
}
