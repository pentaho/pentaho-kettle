/*
 * ******************************************************************************
 * Pentaho
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
 * ******************************************************************************
 */

package org.pentaho.di.core.database;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * User: RFellows
 * Date: 3/4/13
 */
public class MonetDBDatabaseMetaTest
{

  @Test
  public void testGetSafeFieldname() throws Exception {
    MonetDBDatabaseMeta meta = new MonetDBDatabaseMeta();
    String expected = "hello_world";
    String fieldname = "hello world";
    String result = meta.getSafeFieldname(fieldname);

    assertEquals(expected, result);
  }

  @Test
  public void testGetSafeFieldname_beginingWithNumber() throws Exception {
    MonetDBDatabaseMeta meta = new MonetDBDatabaseMeta();
    String expected = "_2B";
    String fieldname = "2B";
    String result = meta.getSafeFieldname(fieldname);

    assertEquals(expected, result);
  }

  @Test
  public void testGetSafeFieldname_reservedWord() throws Exception {
    MonetDBDatabaseMeta meta = new MonetDBDatabaseMeta();
    String expected = "drop_";
    String fieldname = "drop";
    String result = meta.getSafeFieldname(fieldname);

    assertEquals(expected, result);
  }

  @Test
  public void testGetSafeFieldname_nonAlphaNumericChars() throws Exception {
    MonetDBDatabaseMeta meta = new MonetDBDatabaseMeta();
    String expected = "what_the_";
    String fieldname = "what the *#&@(@!?";
    String result = meta.getSafeFieldname(fieldname);

    assertEquals(expected, result);
  }
}
