package org.pentaho.di.jdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.pentaho.di.core.row.RowMeta;

public class KettleHelperUnitTest {

  @Test
  public void testKettleHelper() {
    // this could actually fail the constructor, since it throws an exception from Init...
    // so the test mostly just makes sure that (1) we can create one, and it's not null.
    KettleHelper helper = new KettleHelper();
    assertNotNull( helper );
  }

  private static RowMeta validRowMeta = new RowMeta();
  private static String singleColumnString = "column1";
  private static String multiColumnString = "column1,column2";
  private static String asColumn = "ascolumn";
  private static String asColumn1 = "ascolumn1";
  private static String asColumnString = asColumn1 + " as " + asColumn;

  @Test
  public void testConvertRowMetaString() {
    testNullRowMetaConvert();
    testNullColumnMetaConvert();
    testSingleColumnConvert();
    testMultiColumnConvert();
    testAsColumnConvert();
  }

  public void testSingleColumnConvert() {
    ColInfo[] convertedVals = KettleHelper.convert( validRowMeta, singleColumnString );
    assertNotNull( convertedVals );
    assertTrue( convertedVals.length == 1 );
    assertEquals( convertedVals[0].name, singleColumnString );
    assertEquals( convertedVals[0].realName, singleColumnString );
  }

  public void testMultiColumnConvert() {
    ColInfo[] convertedVals = KettleHelper.convert( validRowMeta, multiColumnString );
    assertNotNull( convertedVals );
    assertTrue( convertedVals.length == 2 );
    assertEquals( convertedVals[0].name, singleColumnString );
    assertEquals( convertedVals[0].realName, singleColumnString );
  }

  public void testAsColumnConvert() {
    ColInfo[] convertedVals = KettleHelper.convert( validRowMeta, asColumnString );
    assertNotNull( convertedVals );
    assertTrue( convertedVals.length == 1 );
    assertEquals( convertedVals[0].realName, asColumn1 );
    assertEquals( convertedVals[0].name, asColumn );
  }

  public void testNullRowMetaConvert() {
    try {
      ColInfo[] convertedVals = KettleHelper.convert( null, singleColumnString );
      assertNull( convertedVals ); // should be null on the return
    } catch ( Exception ex ) {
      fail( "Got exception when testing Null row meta conversion" + ex.getMessage() );
    }
  }

  public void testNullColumnMetaConvert() {
    try {
      ColInfo[] convertedVals = KettleHelper.convert( validRowMeta, null );
      assertNull( convertedVals ); // should return null on invalid column spec?
    } catch ( Exception ex ) {
      fail( "Got exception when testing Null Column meta conversion" + ex.getMessage() );

    }
  }

  /*
   * @Test public void testConvertRowMeta() { fail( "Not yet implemented" ); }
   * 
   * @Test public void testVisitDirectory() { fail( "Not yet implemented" ); }
   * 
   * @Test public void testGetSteps() { fail( "Not yet implemented" ); }
   * 
   * @Test public void testGetRowMeta() { fail( "Not yet implemented" ); }
   */
}
