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

package org.pentaho.di.repository.pur.model;

import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.TransMeta;

public class EETransMeta extends TransMeta implements ILockable, java.io.Serializable {

  private static final long serialVersionUID = -5959504570945456271L; /* EESOURCE: UPDATE SERIALVERUID */
  private RepositoryLock repositoryLock;

  public EETransMeta( VariableSpace parent ) {
    super( parent );
  }

  /**
   * @return the repositoryLock
   */
  public RepositoryLock getRepositoryLock() {
    return repositoryLock;
  }

  /**
   * @param repositoryLock
   *          the repositoryLock to set
   */
  public void setRepositoryLock( RepositoryLock repositoryLock ) {
    this.repositoryLock = repositoryLock;
  }
}
