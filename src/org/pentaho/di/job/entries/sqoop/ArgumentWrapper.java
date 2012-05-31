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

import org.pentaho.ui.xul.XulEventSource;

import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;

/**
 * Represents a command line argument. This is required to display an argument in a list of arguments for the UI.
 */
public class ArgumentWrapper implements XulEventSource {
  private String name;
  private String displayName;
  private boolean flag;
  private Object target;
  private Method getter;
  private Method setter;

  public ArgumentWrapper(String name, String displayName, boolean flag, Object target, Method getter, Method setter) {
    if (name == null || target == null || getter == null || setter == null) {
      throw new NullPointerException();
    }
    validateAccessors(getter, setter);

    this.name = name;
    this.displayName = displayName;
    this.flag = flag;
    this.target = target;
    this.getter = getter;
    this.setter = setter;
  }

  private void validateAccessors(Method getter, Method setter) {
    if (getter.getReturnType() != String.class) {
      throw new IllegalArgumentException("Invalid getter method. Method must return a String,");
    }
    if (setter.getParameterTypes().length < 1 || setter.getParameterTypes()[0] != String.class) {
      throw new IllegalArgumentException("Invalid setter method. Method must accept a single String parameter.");
    }
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public void setValue(String value) {
    try {
      setter.invoke(target, value);
    } catch (Exception ex) {
      throw new RuntimeException("error setting value for argument " + getName(), ex);
    }
  }

  public String getValue() {
    try {
      return String.class.cast(getter.invoke(target));
    } catch (Exception ex) {
      throw new RuntimeException("error retrieving value for argument " + getName(), ex);
    }
  }

  public boolean isFlag() {
    return flag;
  }

  public void setFlag(boolean flag) {
    this.flag = flag;
  }

  /**
   * Uses the argument's name to determine equality.
   *
   * @param o another argument
   * @return {@code true} if {@code o} is an {@link ArgumentWrapper} and its name equals this argument's name
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ArgumentWrapper that = (ArgumentWrapper) o;

    return this.name.equals(that.name);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    // Do nothing, this object is a wrapper and firing events here propagates to too many objects
  }

  @Override
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    // Do nothing, this object is a wrapper and firing events here propagates to too many objects
  }
}
