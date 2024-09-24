// CHECKSTYLE:FileLength:OFF
/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

/**
 * @deprecated As of release 6.1. The methods will be added to the existing interface at the next major release.
 * {@link DatabaseMeta#getDropTableIfExistsStatement(String)} which is using those new methods can be freely used,
 * because it contains fallbacks in case of existing interface ({@link DatabaseInterface}) using.
 */
@Deprecated
public interface DatabaseInterfaceExtended extends DatabaseInterface {

  /**
   * Forms the drop table statement specific for a certain RDBMS.
   *
   * @param tableName Name of the table to drop
   * @return Drop table statement specific for the current database
   */
  String getDropTableIfExistsStatement( String tableName );

  /**
   * Returns false if exception doesn't require
   * full exception log. Could be used in cases of DB vendor
   * specific error which doesn't require stack trace log.
   *
   * @param e exception to check
   * @return decision result
   */
  public boolean fullExceptionLog( Exception e );
}
