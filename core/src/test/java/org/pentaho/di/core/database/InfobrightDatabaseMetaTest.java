/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
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
  }

}
