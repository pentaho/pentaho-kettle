/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2016-2017 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.base;

import org.pentaho.di.core.xml.XMLInterface;
import org.pentaho.di.repository.ObjectId;

/**
 * This class defines a base hop from one job entry copy to another, or from one step to another.
 *
 * @author Alexander Buloichik
 */
public abstract class BaseHopMeta<T> implements Cloneable, XMLInterface {
  public static final String XML_TAG = "hop";

  public boolean split = false;
  protected T from, to;
  protected boolean enabled;
  protected boolean changed;
  protected ObjectId id;
  private boolean errorHop;

  public ObjectId getObjectId() {
    return id;
  }

  public void setObjectId( ObjectId id ) {
    this.id = id;
  }

  public Object clone() {
    try {
      Object retval = super.clone();
      return retval;
    } catch ( CloneNotSupportedException e ) {
      return null;
    }
  }

  public void setChanged() {
    setChanged( true );
  }

  public void setChanged( boolean ch ) {
    changed = ch;
  }

  public boolean hasChanged() {
    return changed;
  }

  public void setEnabled() {
    setEnabled( true );
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled( boolean en ) {
    if ( enabled != en ) {
      setChanged();
      enabled = en;
    }
  }

  public boolean isErrorHop() {
    return errorHop;
  }

  public void setErrorHop( boolean errorHop ) {
    this.errorHop = errorHop;
  }

}
