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

import java.util.Date;

/**
 * The RepositoryElementMetaInterface is used to provide metadata about repository elements without requiring loading
 * the entire element from the repository.
 */
public interface RepositoryElementMetaInterface extends RepositoryObjectInterface {

  public Date getModifiedDate();

  public String getModifiedUser();

  public RepositoryObjectType getObjectType();

  public String getDescription();

  public boolean isDeleted();

  public void setName( String name );

  public RepositoryDirectoryInterface getRepositoryDirectory();

}
