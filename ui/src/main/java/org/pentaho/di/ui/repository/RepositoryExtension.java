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
