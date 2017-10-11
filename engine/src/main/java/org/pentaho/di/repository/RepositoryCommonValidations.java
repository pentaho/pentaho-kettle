/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
