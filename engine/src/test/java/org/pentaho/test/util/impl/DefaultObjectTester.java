/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
