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


package org.pentaho.di.ui.repository.pur.repositoryexplorer.model;

import java.util.List;

import org.pentaho.ui.xul.util.AbstractModelNode;

public class UIRepositoryObjectRevisions extends AbstractModelNode<UIRepositoryObjectRevision> implements
    java.io.Serializable {

  private static final long serialVersionUID = -5243106180726024567L; /* EESOURCE: UPDATE SERIALVERUID */

  public UIRepositoryObjectRevisions() {
  }

  public UIRepositoryObjectRevisions( List<UIRepositoryObjectRevision> revisions ) {
    super( revisions );
  }

  @Override
  protected void fireCollectionChanged() {
    this.changeSupport.firePropertyChange( "children", null, this );
  }

}
