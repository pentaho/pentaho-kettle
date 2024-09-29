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

import org.pentaho.ui.xul.util.AbstractModelList;

public class UIClusters extends AbstractModelList<UICluster> {

  private static final long serialVersionUID = -3044131509454994992L;

  public UIClusters() {
  }

  public UIClusters( List<UICluster> clusters ) {
    super( clusters );
  }
}
