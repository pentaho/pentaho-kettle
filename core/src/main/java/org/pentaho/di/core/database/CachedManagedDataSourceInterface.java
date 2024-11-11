/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.core.database;


import javax.sql.DataSource;
import java.util.List;

public interface CachedManagedDataSourceInterface extends DataSource {

  /**
   * Validates if the Datasource has been expired and can no longer be used.
   */
  boolean isExpired();

  /**
   * Sets the state of the Datasource
   */
  void expire();

  /**
   * Validate whther or not the Datasaource is being used.
   * @return
   */
  boolean isInUse();

  /**
   * Sets the owner of this Datasource
   * @param ownerList
   */
  void setInUseBy( List<String> ownerList );

  /**
   * Adds an owner to this Datasource
   * @param ownerName
   */
  void addInUseBy( String ownerName );

  /**
   * Removes an owner to this Datasource
   * @param ownerName
   */
  void removeInUseBy( String ownerName );

  /**
   * Gets the Datasource hash
   */
  String getHash();

  /**
   * Sets the Datasource hash
   * @param dataSource
   */
  void setHash( String dataSource );

  /**
   * Try to invalidate the Datasource
   * @param invalidatedBy
   */
  void tryInvalidateDataSource( String invalidatedBy );
}
