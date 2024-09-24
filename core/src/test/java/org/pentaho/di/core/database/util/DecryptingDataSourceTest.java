/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2023 by Hitachi Vantara : http://www.pentaho.com
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