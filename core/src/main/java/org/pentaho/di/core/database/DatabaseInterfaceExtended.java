// CHECKSTYLE:FileLength:OFF
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
