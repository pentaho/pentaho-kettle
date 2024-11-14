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


package org.pentaho.di.ui.core.auth.model;

public abstract interface NamedModelObject<T> {
  public abstract String getName();

  public abstract void setName( String paramString );

  public abstract void setItem( T paramT );

  public abstract T getItem();
}
