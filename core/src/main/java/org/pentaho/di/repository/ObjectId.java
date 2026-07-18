/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.di.repository;

/**
 * This interface allows an object to be identified with an ID in a repository (or elsewhere). In some cases, this ID is
 * a long integer (Database Repository), in some cases a filename, in other cases a UUID. So in general we made the ID
 * itself a String.
 *
 * @author matt
 *
 */
public interface ObjectId {
  public String getId();
}
