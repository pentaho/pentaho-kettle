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

package org.pentaho.di.ui.repository.pur.services;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.IRepositoryService;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.pur.model.ObjectAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
/**
 * Repository service which adds ACL feature to the repository. Using this feature, the user
 * of the repository can retrieve and update ACL for a particular object in the repository
 * @author rmansoor
 *
 */
public interface IAclService extends IRepositoryService{

  /**
   * Get the Permissions of a repository object.
   * 
   * @param Object Id of the repository object
   * @param forceParentInheriting retrieve the effective ACLs as if 'inherit from parent' were true
   * 
   * @return The permissions.
   * @throws KettleException in case something goes horribly wrong
   */
  public ObjectAcl getAcl(ObjectId id, boolean forceParentInheriting) throws KettleException;

  /**
   * Set the Permissions of a repository element.
   * 
   * @param Acl object that needs to be set.
   * @param Object Id of a file for which the acl are being set.
   * 
   * @throws KettleException in case something goes horribly wrong
   */
  public  void setAcl(ObjectId id, ObjectAcl aclObject) throws KettleException;
  
  public boolean hasAccess(ObjectId id, RepositoryFilePermission perm) throws KettleException;
}
