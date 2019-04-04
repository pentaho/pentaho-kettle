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
package org.pentaho.di.repository.pur;

import org.pentaho.di.repository.RepositoryElementMetaInterface;

public interface PurRepositoryElementMetaInterface extends RepositoryElementMetaInterface {

  /**
   * Will be Null if not loaded
   * 
   * @return whether RepositoryObject is should have versioning enabled
   */
  Boolean getVersioningEnabled();

  /**
   * Will be Null if not loaded
   * 
   * @return whether RepositoryObject should ask for version comments on storage
   */
  Boolean getVersionCommentEnabled();
}
