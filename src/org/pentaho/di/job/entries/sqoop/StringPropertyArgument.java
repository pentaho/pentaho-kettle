/*******************************************************************************
 *
 * Pentaho Big Data
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.job.entries.sqoop;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * An argument that is a String property of another object.
 */
public class StringPropertyArgument extends Argument {

  private Object obj;
  private Method setter;
  private Method getter;

  public StringPropertyArgument(String name, String displayName, Object obj, Method setter, Method getter) {
    super(name, displayName, null, false);
    if (name == null || displayName == null || obj == null || setter == null || getter == null) {
      throw new NullPointerException();
    }
    if (!String.class.isAssignableFrom(getter.getReturnType())) {
      throw new IllegalArgumentException("getter method must return a String");
    }
    if (setter.getParameterTypes().length != 1 || !String.class.isAssignableFrom(setter.getParameterTypes()[0])) {
      throw new IllegalArgumentException("setter method must accept a String value");
    }

    this.obj = obj;
    this.setter = setter;
    this.getter = getter;
  }

  @Override
  public String getValue() {
    try {
      return String.class.cast(getter.invoke(obj));
    } catch (InvocationTargetException ite) {
      throw new RuntimeException(ite);
    } catch (IllegalAccessException iae) {
      throw new RuntimeException(iae);
    }
  }

  @Override
  public void setValue(String value) {
    try {
      setter.invoke(obj, value);
    } catch (InvocationTargetException ite) {
      throw new RuntimeException(ite);
    } catch (IllegalAccessException iae) {
      throw new RuntimeException(iae);
    }
  }
}
