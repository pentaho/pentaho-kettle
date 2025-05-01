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

package org.pentaho.di.repository.pur;

import java.util.List;
import java.util.Map;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.RepositoryElementInterface;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.shared.SharedObjects;

public interface ISharedObjectsTransformer extends ITransformer {

  /**
   * @deprecated Shared Objects from the Repository are now accessed through a Bowl, they are no longer loaded into or
   *             saved from the Meta.
   */
  @Deprecated
  void loadSharedObjects( final RepositoryElementInterface element,
      final Map<RepositoryObjectType, List<? extends SharedObjectInterface>> sharedObjectsByType )
    throws KettleException;

  /**
   * @deprecated Shared Objects from the Repository are now accessed through a Bowl, they are no longer loaded into or
   *             saved from the Meta.
   */
  @Deprecated
  void saveSharedObjects( final RepositoryElementInterface element, final String versionComment )
    throws KettleException;
}
