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
package org.pentaho.di.core;

import org.junit.Test;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.MySQLDatabaseMeta;
import org.pentaho.di.core.database.SAPR3DatabaseMeta;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ConstDBTest {

  @Test
  public void testSelectSAPR3Databases() throws Exception {
    KettleClientEnvironment.init();
    final DatabaseMeta mysqlMeta = new DatabaseMeta();
    mysqlMeta.setDatabaseInterface( new MySQLDatabaseMeta() );
    final DatabaseMeta sapR3Meta = new DatabaseMeta();
    sapR3Meta.setDatabaseInterface( new SAPR3DatabaseMeta() );
    List<DatabaseMeta> databaseMetas = new ArrayList<>();
    databaseMetas.add( mysqlMeta );
    databaseMetas.add( sapR3Meta );

    List<DatabaseMeta> sapR3Metas = ConstDB.selectSAPR3Databases( databaseMetas );
    assertEquals( 1, sapR3Metas.size() );
    assertSame( sapR3Meta, sapR3Metas.get( 0 ) );
  }
}
