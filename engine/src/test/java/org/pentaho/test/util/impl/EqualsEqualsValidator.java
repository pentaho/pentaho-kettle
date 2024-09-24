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

package org.pentaho.test.util.impl;

import static org.junit.Assert.assertTrue;

import org.pentaho.test.util.ObjectValidator;

public class EqualsEqualsValidator<T> implements ObjectValidator<T> {

  @Override
  public void validate( T expected, Object actual ) {
    assertTrue( "Expected " + expected + " == " + actual + " to evaluate to true.", expected == actual );
  }
}
