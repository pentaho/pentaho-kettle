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

package org.pentaho.di.ui.repository.repositoryexplorer.model;

import java.util.List;

import org.pentaho.ui.xul.util.AbstractModelNode;

public class UIRepositoryObjects extends AbstractModelNode<UIRepositoryObject> {

  private static final long serialVersionUID = -8589134015520102516L;

  public UIRepositoryObjects() {
  }

  public UIRepositoryObjects( List<UIRepositoryObject> objects ) {
    super( objects );
  }

  @Override
  protected void fireCollectionChanged() {
    this.changeSupport.firePropertyChange( "children", null, this );
  }

}
