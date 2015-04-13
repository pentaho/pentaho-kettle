/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.core.jdbc;

import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Properties;

import static org.junit.Assert.*;

public class ThinDriverTest {

  private ThinDriver driver;

  @Before
  public void setUp() throws Exception {
    driver = new ThinDriver();
  }

  @Test
  public void testAcceptsURL() throws Exception {
    assertTrue( driver.acceptsURL( "jdbc:pdi://slaveserver:8181/kettle/?webappname=pdi" ) );
    assertFalse( driver.acceptsURL( "jdbc:mysql://localhost" ) );
  }

  @Test
  public void testConnectNull() throws Exception {
    Properties properties = new Properties();
    properties.setProperty( "user", "user" );
    properties.setProperty( "password", "password" );
    assertNull( driver.connect( "jdbc:mysql://localhost", properties ) );
  }

  @Test( expected = SQLException.class )
  public void testConnectError() throws Exception {
    Properties properties = new Properties();
    properties.setProperty( "user", "user" );
    properties.setProperty( "password", "password" );
    assertNull( driver.connect( "jdbc:pdi://localhost", properties ) );
  }
}
