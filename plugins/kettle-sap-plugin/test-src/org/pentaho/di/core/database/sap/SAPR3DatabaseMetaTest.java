package org.pentaho.di.core.database.sap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseInterface;

public class SAPR3DatabaseMetaTest {

  @Test
  public void testSequences() {
    DatabaseInterface db = new SAPR3DatabaseMeta();
    String dbType = db.getClass().getSimpleName();

    String sequenceName = "sequence_name";
    assertEquals( "", db.getSQLNextSequenceValue( sequenceName ) );
    assertEquals( "", db.getSQLCurrentSequenceValue( sequenceName ) );

    // TODO: Get a Kettle-Core Test artifact, and use the SequenceMetaTests.assertSupports() method instead
    assertFalse( db.getClass().getSimpleName(), db.supportsSequences() );
    assertTrue( dbType + ": List of Sequences", Const.isEmpty( db.getSQLListOfSequences() ) );
    assertTrue( dbType + ": Sequence Exists", Const.isEmpty( db.getSQLSequenceExists( "testSeq" ) ) );
    assertTrue( dbType + ": Current Value", Const.isEmpty( db.getSQLCurrentSequenceValue( "testSeq" ) ) );
    assertTrue( dbType + ": Next Value", Const.isEmpty( db.getSQLNextSequenceValue( "testSeq" ) ) );
  }
}
