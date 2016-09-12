/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.core.database.sap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.database.DatabaseInterface;

public class SAPR3DatabaseMetaTest {

  DatabaseInterface db;

  @Before
  public void setup() {
    db = new SAPR3DatabaseMeta();
  }

  @Test
  public void testSequences() {
    String dbType = db.getClass().getSimpleName();

    String sequenceName = "sequence_name";
    assertEquals( "", db.getSQLNextSequenceValue( sequenceName ) );
    assertEquals( "", db.getSQLCurrentSequenceValue( sequenceName ) );

    // TODO: Get a Kettle-Core Test artifact, and use the SequenceMetaTests.assertSupports() method instead
    assertFalse( db.getClass().getSimpleName(), db.supportsSequences() );
    assertTrue( dbType + ": List of Sequences", Utils.isEmpty( db.getSQLListOfSequences() ) );
    assertTrue( dbType + ": Sequence Exists", Utils.isEmpty( db.getSQLSequenceExists( "testSeq" ) ) );
    assertTrue( dbType + ": Current Value", Utils.isEmpty( db.getSQLCurrentSequenceValue( "testSeq" ) ) );
    assertTrue( dbType + ": Next Value", Utils.isEmpty( db.getSQLNextSequenceValue( "testSeq" ) ) );
  }

  @Test
  public void testReleaseSavePoint() {
    assertTrue( db.releaseSavepoint() );
  }
}
