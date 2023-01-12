/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2022 by Hitachi Vantara : http://www.pentaho.com
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
