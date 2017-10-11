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
