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

package org.pentaho.di.trans.steps.monetdbbulkloader;

import static org.junit.Assert.fail;

import java.util.ArrayList;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.MonetDBDatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransTestFactory;

public class MonetDBBulkLoaderIT {

  static final String DB = "pentaho-instaview";
  static final int PORT = 50000;
  static final String USER = "monetdb";
  static final String PASSWORD = "monetdb";
  static final String HOST = "localhost";

  @BeforeClass
  public static void setUpBeforeClass() throws KettleException {
    KettleEnvironment.init( false );
  }

  // not a real unit test: ignore it. depends on monetdb running and a specific table existing in it.
  // was created to help test/verify the transition away from mclient process to MapiSocket API.
  @Ignore
  @Test
  public void testExecuteQuery() throws Exception {

    String query = "SELECT * FROM instaview_20121031113843842";
    MonetDBBulkLoader.executeSql( query, HOST, PORT, USER, PASSWORD, DB );

    query = "SELECT * FROM badtable";
    MonetDBBulkLoader.executeSql( query, HOST, PORT, USER, PASSWORD, DB );

  }

  @Test
  public void testNoInput() {
    String oneStepname = "Monet Bulk Loader";
    MonetDBBulkLoaderMeta meta = new MonetDBBulkLoaderMeta();
    DatabaseMeta database = new DatabaseMeta();
    database.setDatabaseInterface( new MonetDBDatabaseMeta() );
    meta.setDefault();
    meta.setDatabaseMeta( database );
    TransMeta transMeta = TransTestFactory.generateTestTransformation( new Variables(), meta, oneStepname );

    try {
      TransTestFactory.executeTestTransformation( transMeta, oneStepname, new ArrayList<RowMetaAndData>() );
    } catch ( KettleException e ) {
      // The Monet DB Bulk Loader step should finish quietly if no input rows
      fail();
    }
  }
}
