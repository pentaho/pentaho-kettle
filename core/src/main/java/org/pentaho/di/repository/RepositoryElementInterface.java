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


package org.pentaho.di.repository;

import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.shared.SharedObjectInterface;
import org.w3c.dom.Node;

/**
 * A repository element is an object that can be saved or loaded from the repository. As such, we need to be able to
 * identify it. It needs a RepositoryDirectory, a name and an ID.
 *
 * We also need to identify the type of the element.
 *
 * Finally, we need to be able to optionally identify the revision of the element.
 *
 * @author matt
 *
 */
public interface RepositoryElementInterface extends RepositoryObjectInterface {

  public RepositoryDirectoryInterface getRepositoryDirectory();

  public void setRepositoryDirectory( RepositoryDirectoryInterface repositoryDirectory );

  /**
   * Gets the name of the repository object.
   */
  @Override
  public String getName();

  /**
   * Sets the name of the repository object.
   *
   * @param name
   */
  public void setName( String name );

  /**
   * Gets the description of the repository object.
   *
   * @return
   */
  public String getDescription();

  /**
   * Sets the description of the repository object.
   *
   * @param description
   */
  public void setDescription( String description );

  /**
   * Gets the database ID in the repository for this object.
   *
   * @return the database ID in the repository for this object
   */
  @Override
  public ObjectId getObjectId();

  /**
   * Sets the database ID in the repository for this object.
   *
   * @return the database ID in the repository for this object
   */
  public void setObjectId( ObjectId id );

  /**
   * Gets the repository element type for this object.
   *
   * @return the repository element type for this object
   */
  public RepositoryObjectType getRepositoryElementType();

  /**
   * Gets the object revision.
   *
   * @return the object revision
   */
  public ObjectRevision getObjectRevision();

  /**
   * Sets the object revision.
   *
   * @param objectRevision
   */
  public void setObjectRevision( ObjectRevision objectRevision );

  default void appendObjectId( StringBuilder builder ) {
    if ( getObjectId() != null ) {
      builder.append( "    " ).append( XMLHandler.addTagValue( SharedObjectInterface.OBJECT_ID,
        getObjectId().toString() ) );
    }
  }

  default void readObjectId( Node node ) {
    String objectId = XMLHandler.getTagValue( node, SharedObjectInterface.OBJECT_ID );
    if ( objectId != null ) {
      setObjectId( new StringObjectId( objectId ) );
    }
  }


}
