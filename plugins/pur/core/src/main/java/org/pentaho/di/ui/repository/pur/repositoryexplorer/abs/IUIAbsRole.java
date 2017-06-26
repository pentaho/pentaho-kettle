/*!
 * Copyright 2010 - 2015 Pentaho Corporation.  All rights reserved.
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
package org.pentaho.di.ui.repository.pur.repositoryexplorer.abs;

import java.util.List;

import org.pentaho.di.ui.repository.pur.repositoryexplorer.IUIRole;

public interface IUIAbsRole extends IUIRole {
  /**
   * Associate a logical role to the runtime role
   * 
   * @param logical
   *          role name to be associated
   */
  public void addLogicalRole( String logicalRole );

  /**
   * Remove the logical role association from this particular runtime role
   * 
   * @param logical
   *          role name to be un associated
   */
  public void removeLogicalRole( String logicalRole );

  /**
   * Check whether a logical role is associated to this runtime role
   * 
   * @param logical
   *          role name to be checked
   */
  public boolean containsLogicalRole( String logicalRole );

  /**
   * Associate set of logical roles to this particular runtime role
   * 
   * @param list
   *          of logical role name
   */
  public void setLogicalRoles( List<String> logicalRoles );

  /**
   * Retrieve the list of roles association for this particular runtime role
   * 
   * @return list of associated roles
   */
  public List<String> getLogicalRoles();

}
