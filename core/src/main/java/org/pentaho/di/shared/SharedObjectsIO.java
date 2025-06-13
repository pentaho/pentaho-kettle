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

import java.util.Map;
import java.util.Set;
import org.w3c.dom.Node;

/**
 * This interface provide methods to retrieve and save the shared objects defined in shared.xml irrespective of
 * where the shared objects are persisted.
 */
public interface SharedObjectsIO {

  public enum SharedObjectType {
    CONNECTION( "connection" ),
    SLAVESERVER( "slaveserver" ),
    PARTITIONSCHEMA( "partitionschema" ),
    CLUSTERSCHEMA( "clusterschema" );

    private final String name;

    SharedObjectType( String name ) {
      this.name = name;
    }
    public String getName() {
      return name;
    }
  }

  /**
   * Get the collection of SharedObjects of the given type
   * @param type The type is shared object type for example, "connection", "slaveserver", "partitionschema" and clusterschema"
   * @return Map<String,Node>  The collection of name of sharedObject and the Xml Node containing the details. For example,
   *                            {"my-postgres", node} where  my-postgres is the name of
   *                            the type - database connection and the Node is xml node containing the details of the db connection.
   * @throws KettleException
   */
  Map<String, Node> getSharedObjects( String type ) throws KettleException;

  /**
   * Save the SharedObject node for the type and name. If the SharedObject name exist but in a
   * different case, the existing entry will be deleted and the new entry will be saved with provided name
   * @param type The type is shared object type for example, "connection", "slaveserver", "partitionschema" and clusterschema"
   * @param name The name is the name of the sharedObject
   * @param node The Xml node containing the details of the shared object
   * @throws KettleException
   */
  void saveSharedObject( String type, String name, Node node ) throws KettleException;

  /**
   * Return the Shared Object Node for the given type and name. The lookup for the SharedObject
   * using the name will be case-insensitive
   * @param type The type is shared object type for example, "connection", "slaveserver", "partitionschema" and clusterschema"
   * @param name The name is the name of the sharedObject
   * @return Xml node
   * @throws KettleException
   */
  Node getSharedObject( String type, String name ) throws KettleException;

  /**
   * Delete the SharedObject for the given type and name. The delete operation will be case-insensitive.
   * @param type The type is shared object type for example, "connection", "slaveserver", "partitionschema" and clusterschema"
   * @param name The name is the name of the sharedObject
   * @throws KettleException
   */
  void delete( String type, String name ) throws KettleException;

  /**
   * Remove all the connections of a given type.
   *
   *
   * @param type Type to clear
   */
  void clear( String type ) throws KettleException;

  static String findSharedObjectIgnoreCase( String name, Set<String> existingKeys ) {
    for ( String key : existingKeys ) {
      if ( key.equalsIgnoreCase( name ) ) {
        return key;
      }
    }
    return null;
  }

}
