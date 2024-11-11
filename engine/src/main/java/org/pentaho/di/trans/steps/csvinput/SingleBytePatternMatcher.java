/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.trans.steps.csvinput;

public class SingleBytePatternMatcher implements PatternMatcherInterface {

  public boolean matchesPattern( byte[] source, int location, byte[] pattern ) {
    return source[location] == pattern[0];
  }

}
