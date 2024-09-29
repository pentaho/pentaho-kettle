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


package org.pentaho.di.ui.repository.pur.services;

import org.pentaho.di.repository.pur.model.IRole;

/**
 * @author Andrey Khayrutdinov
 */
public interface RepositorySecurityRoleValidator extends IRoleSupportSecurityManager {

  /**
   * Validates {@code role} and returns {@code true} if all its data is valid.<br/>
   * Note: this method performs fail-fast approach and does not return any details.
   * 
   * @param role
   *          role's info
   * @return {@code true} or {@code false} depending on whether or not role's info is valid
   */
  boolean validateRoleInfo( IRole role );

  /**
   * Performs normalization over {@code user} due to validation rules.</br> Note: normalized is not guaranteed to pass
   * validation rules
   * 
   * @param role
   *          role's info
   */
  void normalizeRoleInfo( IRole role );
}
