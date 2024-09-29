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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class InfobrightDatabaseMetaTest extends MySQLDatabaseMetaTest {

  @Test
  public void mysqlTestOverrides() throws Exception {
    InfobrightDatabaseMeta idm = new InfobrightDatabaseMeta();
    idm.setAccessType( DatabaseMeta.TYPE_ACCESS_NATIVE );
    assertEquals( 5029, idm.getDefaultDatabasePort() );
    idm.setAccessType( DatabaseMeta.TYPE_ACCESS_ODBC );
    assertEquals( -1, idm.getDefaultDatabasePort() );
  }

}
