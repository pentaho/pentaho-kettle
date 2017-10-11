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

public interface RepositoryCapabilities {

  /**
   * @return true if the repository supports users.
   */
  public boolean supportsUsers();

  /**
   * @return true if users can be managed in the repository
   */
  public boolean managesUsers();

  /**
   * @return true if this repository is read-only
   */
  public boolean isReadOnly();

  /**
   * @return true if the repository supports revisions.
   */
  public boolean supportsRevisions();

  /**
   * @return true if the repository supports storing metadata like names, descriptions, ... outside of the object
   *         definitions (XML)
   */
  public boolean supportsMetadata();

  /**
   * @return true if this repository supports file locking
   */
  public boolean supportsLocking();

  /**
   * @return true if the repository has a version registry
   */
  public boolean hasVersionRegistry();

  /**
   * @return true if the repository supports ACLs
   */
  public boolean supportsAcls();

  /**
   * @return true if the repository is capable of representing objects as references (IDs)...
   */
  public boolean supportsReferences();

}
