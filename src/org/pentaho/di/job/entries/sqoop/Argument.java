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
import java.beans.PropertyChangeSupport;
import java.util.List;

/**
 * Declares a Command Line argument.
 */
public class Argument implements XulEventSource {
  private String name;
  private String displayName;
  private String value;
  private Boolean flag;

  /**
   * Default constructor to support {@link JobEntrySerializationHelper}. Not intended to be used programmatically.
   */
  public Argument() {
    this(null, null);
  }

  /**
   * @see #Argument(String, boolean) Argument(name, false)
   */
  public Argument(String name) {
    this(name, false);
  }

  /**
   * @see #Argument(String, String, String, boolean) Argument(name, name, null, flag)
   */
  public Argument(String name, boolean flag) { this(name, name, null, flag);
  }

  /**
   * @see #Argument(String, String, String, boolean) Argument(name, name, value, false)
   */
  public Argument(String name, String value) {
    this(name, name, value, false);
  }

  /**
   * Creates an argument that represents a command line argument whose name, value, and flag values will be used
   * to generate the appropriate command line options in {@link #appendTo(java.util.List, String)}.
   *
   * @param name Name of command line argument
   * @param displayName Display name to show in a UI
   * @param value Value of the command line argument
   * @param flag Indicates if this argument is a flag/switch. If {@code true}, the value will be used to determine if the option is enabled or not.
   */
  public Argument(String name, String displayName, String value, boolean flag) {
    this.name = name;
    this.displayName = displayName;
    this.value = value;
    this.flag = flag;
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

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public Boolean isFlag() {
    return flag == null ? Boolean.FALSE : flag;
  }

  public void setFlag(Boolean flag) {
    if (flag == null) {
      throw new NullPointerException();
    }
    this.flag = flag;
  }

  /**
   * @see {@link PropertyChangeSupport#addPropertyChangeListener(java.beans.PropertyChangeListener)}
   */
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    // Do nothing, don't actually wire anything up.
  }

  /**
   * @see {@link PropertyChangeSupport#removePropertyChangeListener(java.beans.PropertyChangeListener)}
   */
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    // Do nothing, don't actually wire anything up.
  }

  /**
   * Uses the argument's name to determine equality.
   * @param o another object
   * @return {@code true} if {@code o} is an {@link Argument} and its name equals this argument's name
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Argument that = (Argument) o;

    if (name != null ? !name.equals(that.name) : that.name != null) return false;

    return true;
  }

  /**
   * Uses the argument's name to determine it's hashcode.
   * @return the hash code value of this argument's name, or {@code 0} if not set
   */
  @Override
  public int hashCode() {
    return name != null ? name.hashCode() : 0;
  }
}
