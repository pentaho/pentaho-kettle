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

import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.RepositorySecurityProvider;

/**
 * Core security API for Action based security
 * 
 * <p>
 * Reponsible for determining if access to a given action should be allowed or denied.
 * 
 * @author rmansoor
 */

public interface IAbsSecurityProvider extends RepositorySecurityProvider {

  /**
   * Returns {@code true} if the the action should be allowed.
   * 
   * @param actionName
   *          name of action (e.g. {@code org.pentaho.di.repository.create})
   * @return {@code true} to allow
   */
  public boolean isAllowed( String actionName ) throws KettleException;

  /**
   * Returns all actions in the given namespace that are currently allowed.
   * 
   * @param actionNamespace
   *          action namespace (e.g. {@code org.pentaho.di.repository}); {@code null} means all allowed actions
   * @return list of actions
   */
  public List<String> getAllowedActions( String actionNamespace ) throws KettleException;
}
