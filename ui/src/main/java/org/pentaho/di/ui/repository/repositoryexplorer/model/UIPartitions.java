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

public class UIPartitions extends AbstractModelList<UIPartition> {

  private static final long serialVersionUID = -2251943529719911169L;

  public UIPartitions() {
  }

  public UIPartitions( List<UIPartition> partitions ) {
    super( partitions );
  }

}
