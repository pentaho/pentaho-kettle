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
package org.pentaho.di.repository.pur;

import org.pentaho.di.repository.RepositoryElementMetaInterface;

public interface PurRepositoryElementMetaInterface extends RepositoryElementMetaInterface {

  /**
   * Will be Null if not loaded
   * 
   * @return whether RepositoryObject is should have versioning enabled
   */
  Boolean getVersioningEnabled();

  /**
   * Will be Null if not loaded
   * 
   * @return whether RepositoryObject should ask for version comments on storage
   */
  Boolean getVersionCommentEnabled();
}
