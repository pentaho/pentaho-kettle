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



package org.pentaho.di.ui.repository.repositoryexplorer.model;

import org.pentaho.ui.xul.util.AbstractModelNode;

public class UIRepositoryDirectories extends AbstractModelNode<UIRepositoryObject> {

  private static final long serialVersionUID = -4080424591454426385L;

  public UIRepositoryDirectories() {
  }

  @Override
  protected void fireCollectionChanged() {
    this.changeSupport.firePropertyChange( "children", null, this );
  }
}
