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

import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Properties;

/**
 * User: RFellows Date: 3/4/13
 */
public class BaseDatabaseMetaIT {

  @Test
  public void testPreserveReservedCase() {
    BaseDatabaseMeta meta = mock( BaseDatabaseMeta.class, Mockito.CALLS_REAL_METHODS );
    meta.setAttributes( new Properties() );
    assertTrue( "Default value of 'preserve reserved words case' attribute is FALSE", meta.preserveReservedCase() );
  }

  @Test
  public void testGetSafeFieldname_space() throws Exception {
    BaseDatabaseMeta meta = mock( BaseDatabaseMeta.class, Mockito.CALLS_REAL_METHODS );
    String expected = "hello_world";
    String fieldname = "hello world";
    String result = meta.getSafeFieldname( fieldname );

    assertEquals( expected, result );
  }

  @Test
  public void testGetSafeFieldname_beginingWithNumber() throws Exception {
    BaseDatabaseMeta meta = mock( BaseDatabaseMeta.class, Mockito.CALLS_REAL_METHODS );
    String expected = "_2B";
    String fieldname = "2B";
    String result = meta.getSafeFieldname( fieldname );

    assertEquals( expected, result );
  }

  @Test
  public void testGetSafeFieldname_reservedWord() throws Exception {
    BaseDatabaseMeta meta = mock( BaseDatabaseMeta.class, Mockito.CALLS_REAL_METHODS );
    when( meta.getReservedWords() ).thenReturn( new String[] { "CASE", "JOIN" } );
    String expected = "case_";
    String fieldname = "case";
    String result = meta.getSafeFieldname( fieldname );

    assertEquals( expected, result );
  }

  @Test
  public void testGetSafeFieldname_nonAlphaNumericChars() throws Exception {
    BaseDatabaseMeta meta = mock( BaseDatabaseMeta.class, Mockito.CALLS_REAL_METHODS );
    String expected = "what_the_";
    String fieldname = "what the *#&@(@!?";
    String result = meta.getSafeFieldname( fieldname );

    assertEquals( expected, result );
  }
}
