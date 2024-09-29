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

/**
 * @author Andrey Khayrutdinov
 */
public interface RepositorySecurityUserValidator extends RepositorySecurityManager {

  /**
   * Validates {@code user} and returns {@code true} if all its data is valid.<br/>
   * Note: this method performs fail-fast approach and does not return any details.
   *
   * @param user user's info
   * @return {@code true} or {@code false} depending on whether or not user's info is valid
   */
  boolean validateUserInfo( IUser user );

  /**
   * Performs normalization over {@code user} due to validation rules.</br>
   * Note: normalized is not guaranteed to pass validation rules
   *
   * @param user user's info
   */
  void normalizeUserInfo( IUser user );
}
