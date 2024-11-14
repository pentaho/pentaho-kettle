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


package org.pentaho.di.ui.repository.repositoryexplorer.model;

import java.util.List;

import org.pentaho.ui.xul.util.AbstractModelList;

public class UIDatabaseConnections extends AbstractModelList<UIDatabaseConnection> {

  private static final long serialVersionUID = -5835985536358581894L;

  public UIDatabaseConnections() {
  }

  public UIDatabaseConnections( List<UIDatabaseConnection> dbConns ) {
    super( dbConns );
  }

  @Override
  protected void fireCollectionChanged() {
    this.changeSupport.firePropertyChange( "children", null, this.getChildren() );
  }

}
