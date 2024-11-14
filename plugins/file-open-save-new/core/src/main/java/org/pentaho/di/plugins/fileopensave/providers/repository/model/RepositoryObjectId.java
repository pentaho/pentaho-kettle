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


package org.pentaho.di.plugins.fileopensave.providers.repository.model;

import org.pentaho.di.repository.ObjectId;

/**
 * Created by bmorrise on 2/25/19.
 */
public class RepositoryObjectId implements ObjectId {

  public RepositoryObjectId( String id ) {
    this.id = id;
  }

  private String id;

  @Override public String getId() {
    return id;
  }
}
