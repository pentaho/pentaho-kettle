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


package org.pentaho.di.core.database.util;

import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.Test;

import javax.sql.DataSource;

import static org.junit.Assert.*;

public class DecryptingDataSourceTest {

  @Test
  public void setPassword() {
    assertEquals( "password", tryAPassword( "password" ) );
    assertEquals( "password", tryAPassword( "Encrypted 2be98afc86aa7f2e4bb18bd63c99dbdde" ) );
    assertEquals( "", tryAPassword( "Encrypted somethingCorrupt" ) );
  }

  private String tryAPassword( String password ) {
    BasicDataSource dataSource = new DecryptingDataSource();
    dataSource.setPassword( password );
    return dataSource.getPassword();
  }
}