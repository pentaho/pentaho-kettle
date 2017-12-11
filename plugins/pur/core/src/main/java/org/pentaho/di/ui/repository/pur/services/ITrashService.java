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

import java.util.Date;
import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.IRepositoryService;
import org.pentaho.di.repository.ObjectId;

/**
 * Repository Service used to add a trash bin feature to the repository
 * 
 * @author mlowery
 * 
 */

public interface ITrashService extends IRepositoryService {

  /**
   * Delete the list of files matching ids
   * 
   * @param ids
   * @throws KettleException
   *           if something bad happens
   */
  void delete( final List<ObjectId> ids ) throws KettleException;

  /**
   * Un deletes the list of files matching the ids
   * 
   * @param ids
   * @throws KettleException
   */
  void undelete( final List<ObjectId> ids ) throws KettleException;

  /**
   * Retrieves the current trash items for the user
   * 
   * @return
   * @throws KettleException
   */
  List<IDeletedObject> getTrash() throws KettleException;

  interface IDeletedObject {

    String getOriginalParentPath();

    Date getDeletedDate();

    /**
     * Directory ({@code null}), Transformation, or Job.
     */
    String getType();

    ObjectId getId();

    String getName();

    default String getOwner() {
      return "";
    }
  }
}
