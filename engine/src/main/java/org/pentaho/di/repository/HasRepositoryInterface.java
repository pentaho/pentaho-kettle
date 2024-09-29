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


package org.pentaho.di.repository;

public interface HasRepositoryInterface {
  /**
   * @return the repository
   */
  public Repository getRepository();

  /**
   * @param repository
   *          the repository to set
   */
  public void setRepository( Repository repository );

}
