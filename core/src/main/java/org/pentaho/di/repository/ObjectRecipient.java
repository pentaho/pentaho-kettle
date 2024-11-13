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


package org.pentaho.di.repository;

public interface ObjectRecipient {

  public static enum Type {
    USER, ROLE, SYSTEM_ROLE;
  }

  public String getName();

  public void setName( String name );

  public Type getType();

  public void setType( Type type );
}
