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
