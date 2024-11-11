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


package org.pentaho.di.core.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBinary;
import org.pentaho.di.core.row.value.ValueMetaString;

/**
 * Tests for the Vertica Database Meta classes.
 *
 * @author sflatley
 *
 */
public class VerticaDatabaseMetaIT {

  /**
   *
   */
  private static final String PRIMARY_KEY_NAME = "PrimaryKeyName";
  /**
   *
   */
  private static final String TECHNICAL_KEY_NAME = "TechnicalKeyName";

  /**
   * Tests the supportsTimeStampToDateConversion method.
   */
  @Test
  public void testSupportsTimeStampToDateConversion() {
    DatabaseInterface databaseInterface = new VerticaDatabaseMeta();
    assertFalse( databaseInterface.supportsTimeStampToDateConversion() );

    databaseInterface = new Vertica5DatabaseMeta();
    assertFalse( databaseInterface.supportsTimeStampToDateConversion() );

  }

  @Test
  public void testSupportBlob() {
    DatabaseInterface databaseInterface = new VerticaDatabaseMeta();
    assertFalse( databaseInterface.supportsGetBlob() );
  }

  @Test
  public void testIsDisplaySizeTwiceThePrecision() {
    DatabaseInterface databaseInterface = new VerticaDatabaseMeta();
    assertTrue( databaseInterface.isDisplaySizeTwiceThePrecision() );
  }

  @Test
  public void testGetDefaultBinaryFieldDefinition() {

    ValueMetaInterface vm = new ValueMetaBinary( "TestFieldBinary", -1, 1 );
    String expDefaultBinaryField = "VARBINARY";

    DatabaseInterface databaseInterface = new VerticaDatabaseMeta();
    String defaultBinaryField =
        databaseInterface.getFieldDefinition( vm, TECHNICAL_KEY_NAME, PRIMARY_KEY_NAME, false, false, false );
    String assertMessage = defaultBinaryField + " should be equal to expected " + expDefaultBinaryField;

    assertTrue( assertMessage, expDefaultBinaryField.equals( defaultBinaryField ) );
  }

  @Test
  public void testGetOneByteBinaryFieldDefinition() {

    ValueMetaInterface vm = new ValueMetaBinary( "TestFieldBinary", 1, 1 );
    String expDefaultBinaryField = "VARBINARY(1)";

    DatabaseInterface databaseInterface = new VerticaDatabaseMeta();
    String defaultBinaryField =
        databaseInterface.getFieldDefinition( vm, TECHNICAL_KEY_NAME, PRIMARY_KEY_NAME, false, false, false );
    String assertMessage = defaultBinaryField + " should be equal to expected " + expDefaultBinaryField;

    assertTrue( assertMessage, expDefaultBinaryField.equals( defaultBinaryField ) );
  }

  @Test
  public void testGetMaximumByteBinaryFieldDefinition() {

    ValueMetaInterface vm = new ValueMetaBinary( "TestFieldBinary", 65000, 1 );
    String expDefaultBinaryField = "VARBINARY(65000)";

    DatabaseInterface databaseInterface = new VerticaDatabaseMeta();
    String defaultBinaryField =
        databaseInterface.getFieldDefinition( vm, TECHNICAL_KEY_NAME, PRIMARY_KEY_NAME, false, false, false );
    String assertMessage = defaultBinaryField + " should be equal to expected " + expDefaultBinaryField;

    assertTrue( assertMessage, expDefaultBinaryField.equals( defaultBinaryField ) );
  }

  /**
   * Test for <a href="http://jira.pentaho.com/browse/PDI-12978">
   * PDI-12978: Vertica - execute SQL 'Alter table'
   * </a>
   */
  @Test
  public void testModifyColumn() {
    ValueMetaInterface valueMeta = new ValueMetaString( "field1", 9, -1 );
    DatabaseInterface databaseInterface = new VerticaDatabaseMeta();
    String alterColumn =
      databaseInterface.getModifyColumnStatement( "table1", valueMeta, null, false, null, false );
    // ignore comments and case
    alterColumn = alterColumn.replaceAll( "(?m)^\\s*--.*$", "" ).trim().toLowerCase();
    final String expected = "alter table table1 alter column field1 set data type varchar(9)";
    assertEquals( expected, alterColumn );
  }

}
