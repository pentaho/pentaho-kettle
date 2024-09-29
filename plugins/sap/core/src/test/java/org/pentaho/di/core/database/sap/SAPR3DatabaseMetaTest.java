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
