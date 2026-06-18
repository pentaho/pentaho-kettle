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


package org.pentaho.di.repository;


import org.apache.commons.lang.StringUtils;

/**
 * @author Andrey Khayrutdinov
 */
public class RepositoryCommonValidations {

  /**
   * Validates {@code user}'s data. Common rule for all repositories is: both login and name must contain at least one
   * meaningful char.
   *
   * @param user user
   * @return {@code true} if user's login and name are not empty
   * @throws NullPointerException is {@code user} is {@code null}
   */
  public static boolean checkUserInfo( IUser user ) {
    return !StringUtils.isBlank( user.getLogin() ) && !StringUtils.isBlank( user.getName() );
  }

  /**
   * Normalizes {@code user}'s data. According to {@linkplain #checkUserInfo(IUser) common rules}, simply trims login
   * and name.
   *
   * @param user user
   * @return normalized instance
   * @throws NullPointerException if {@code user} is {@code null} or {@code user}'s login and name
   */
  public static IUser normalizeUserInfo( IUser user ) {
    user.setLogin( user.getLogin().trim() );
    user.setName( user.getName().trim() );
    return user;
  }
}
