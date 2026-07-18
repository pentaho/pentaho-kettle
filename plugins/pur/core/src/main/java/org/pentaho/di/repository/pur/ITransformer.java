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


package org.pentaho.di.repository.pur;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.RepositoryElementInterface;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;

public interface ITransformer {

  DataNode elementToDataNode( final RepositoryElementInterface element ) throws KettleException;

  RepositoryElementInterface dataNodeToElement( final DataNode rootNode ) throws KettleException;

  void dataNodeToElement( final DataNode rootNode, final RepositoryElementInterface element ) throws KettleException;

}
