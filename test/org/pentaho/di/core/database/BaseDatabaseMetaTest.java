/*
 * ******************************************************************************
 * Pentaho
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
 * ******************************************************************************
 */

package org.pentaho.di.core.database;

import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * User: RFellows
 * Date: 3/4/13
 */
public class BaseDatabaseMetaTest {

  @Test
  public void testGetSafeFieldname_space() throws Exception {
    BaseDatabaseMeta meta = mock(BaseDatabaseMeta.class, Mockito.CALLS_REAL_METHODS);
    String expected = "hello_world";
    String fieldname = "hello world";
    String result = meta.getSafeFieldname(fieldname);

    assertEquals(expected, result);
  }
  @Test
  public void testGetSafeFieldname_beginingWithNumber() throws Exception {
    BaseDatabaseMeta meta = mock(BaseDatabaseMeta.class, Mockito.CALLS_REAL_METHODS);
    String expected = "_2B";
    String fieldname = "2B";
    String result = meta.getSafeFieldname(fieldname);

    assertEquals(expected, result);
  }

  @Test
  public void testGetSafeFieldname_reservedWord() throws Exception {
    BaseDatabaseMeta meta = mock(BaseDatabaseMeta.class, Mockito.CALLS_REAL_METHODS);
    when(meta.getReservedWords()).thenReturn(new String[] { "CASE", "JOIN" } );
    String expected = "case_";
    String fieldname = "case";
    String result = meta.getSafeFieldname(fieldname);

    assertEquals(expected, result);
  }

  @Test
  public void testGetSafeFieldname_nonAlphaNumericChars() throws Exception {
    BaseDatabaseMeta meta = mock(BaseDatabaseMeta.class, Mockito.CALLS_REAL_METHODS);
    String expected = "what_the_";
    String fieldname = "what the *#&@(@!?";
    String result = meta.getSafeFieldname(fieldname);

    assertEquals(expected, result);
  }
}
