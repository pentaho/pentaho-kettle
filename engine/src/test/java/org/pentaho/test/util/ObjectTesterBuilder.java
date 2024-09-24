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

package org.pentaho.test.util;

import java.util.ArrayList;
import java.util.Collection;

import org.pentaho.test.util.impl.CollectionObjectProvider;
import org.pentaho.test.util.impl.DefaultObjectTester;
import org.pentaho.test.util.impl.DotEqualsValidator;
import org.pentaho.test.util.impl.EqualsEqualsValidator;

public class ObjectTesterBuilder<T> {
  private Collection<T> objects;

  private ObjectProvider<T> provider;

  private boolean useEqualsEquals = false;

  private ObjectValidator<T> validator;

  public ObjectTester<T> build() {
    ObjectProvider<T> provider = this.provider;
    if ( provider == null ) {
      if ( objects != null ) {
        provider = new CollectionObjectProvider<T>( objects );
      }
    }
    ObjectValidator<T> validator = this.validator;
    if ( validator == null ) {
      if ( useEqualsEquals ) {
        validator = new EqualsEqualsValidator<T>();
      } else {
        validator = new DotEqualsValidator<T>();
      }
    }
    return new DefaultObjectTester<T>( provider, validator );
  }

  public ObjectTesterBuilder<T> setObjects( Collection<T> objects ) {
    this.objects = objects;
    return this;
  }

  public ObjectTesterBuilder<T> addObject( T object ) {
    if ( this.objects == null ) {
      this.objects = new ArrayList<T>();
    }
    this.objects.add( object );
    return this;
  }

  public ObjectTesterBuilder<T> setProvider( ObjectProvider<T> provider ) {
    this.provider = provider;
    return this;
  }

  public ObjectTesterBuilder<T> setUseEqualsEquals( boolean useDotEquals ) {
    this.useEqualsEquals = useDotEquals;
    return this;
  }

  public ObjectTesterBuilder<T> useEqualsEquals() {
    return setUseEqualsEquals( true );
  }

  public ObjectTesterBuilder<T> setValidator( ObjectValidator<T> validator ) {
    this.validator = validator;
    return this;
  }
}
