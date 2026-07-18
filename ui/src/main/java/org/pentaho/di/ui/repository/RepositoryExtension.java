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



package org.pentaho.di.ui.repository;

import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryObject;

public class RepositoryExtension {

  private UIRepositoryObject repositoryObject;

  public RepositoryExtension( UIRepositoryObject repositoryObject ) {
    this.repositoryObject = repositoryObject;
  }

  public UIRepositoryObject getRepositoryObject() {
    return repositoryObject;
  }

  public void setRepositoryObject( UIRepositoryObject repositoryObject ) {
    this.repositoryObject = repositoryObject;
  }
}
