/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.test.util.impl;

import java.util.Collection;

import org.pentaho.test.util.ObjectProvider;
import org.pentaho.test.util.ObjectTester;
import org.pentaho.test.util.ObjectValidator;

public class DefaultObjectTester<T> implements ObjectTester<T> {
  private final ObjectProvider<T> provider;
  private final ObjectValidator<T> validator;

  public DefaultObjectTester( ObjectProvider<T> provider, ObjectValidator<T> validator ) {
    this.provider = provider;
    this.validator = validator;
  }

  @Override
  public Collection<T> getTestObjects() {
    return provider.getTestObjects();
  }

  @Override
  public void validate( T expected, Object actual ) {
    validator.validate( expected, actual );
  }
}
