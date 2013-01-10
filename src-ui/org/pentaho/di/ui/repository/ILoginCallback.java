/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.ui.repository;

import org.pentaho.di.repository.Repository;

/**
* This interface defines a Spoon Login callback.
* 
* @author rmansoor
*
*/
public interface ILoginCallback {

  /**
   * On a successful login to the repository, this method is invoked
   * @param repository
   */
  void onSuccess(Repository repository);
  /**
   * On a user cancelation from the repository login dialog, this
   * method is invoked
   */
  void onCancel();
  /**
   * On any error caught during the login process, this method is
   * invoked 
   * @param t
   */
  void onError(Throwable t);
}
