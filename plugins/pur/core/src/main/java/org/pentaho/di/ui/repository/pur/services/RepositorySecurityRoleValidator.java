/*!
 * Copyright 2010 - 2017 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
