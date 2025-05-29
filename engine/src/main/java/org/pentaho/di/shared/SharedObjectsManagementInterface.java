/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.shared;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.RepositoryElementInterface;
import java.util.List;

/**
 * This is the management interface used by the UI to perform CRUD operation for all shared objects. The implementors of this interface will
 * be scoped based on the bowl and can be retrieved using bowl's getManager()
 *
 */
public interface SharedObjectsManagementInterface<T extends SharedObjectInterface<T> & RepositoryElementInterface> {

  /**
   * Add the SharedObject to global or project specific file store(shared.xml) depending on the bowl
   * @param sharedObjectInterface
   * @throws KettleException
   */
  void add( T sharedObjectInterface ) throws KettleException;

  /**
   * Get the list of SharedObjects based  on the current bowl
   * @return List<DatabaseMeta> Returns the list of DatabaseMeta
   * @throws KettleException
   */
  List<T> getAll() throws KettleException;

  /**
   * Get a single SharedObject by name.
   *
   * @param name name of the SharedObject
   * @return SharedObjectInterface SharedObject instance
   * @throws KettleException
   */
  T get( String name ) throws KettleException;


  /**
   * Remove the SharedObject
   * @param sharedObjectInterface SharedObject to remove
   * @throws KettleException
   */
  void remove( T sharedObjectInterface ) throws KettleException;

  /**
   * Remove the provided database
   * @param sharedObjectName name of the SharedObject to remove
   * @throws KettleException
   */
  void remove( String sharedObjectName ) throws KettleException;

  /**
   * Removes all sharedObjects for a type
   * @throws KettleException
   */
  void clear() throws KettleException;

}
