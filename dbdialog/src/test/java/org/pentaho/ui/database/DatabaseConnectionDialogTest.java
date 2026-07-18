/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/


package org.pentaho.ui.database;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * Unit tests for DatabaseConnectionDialog.
 */
public class DatabaseConnectionDialogTest {

  DatabaseConnectionDialog dialog;

  @Before
  public void setUp() throws Exception {
    dialog = new DatabaseConnectionDialog();
  }

  @Test
  public void testRegisterClass() throws Exception {
    assertTrue( dialog.extendedClasses.isEmpty() );
    dialog.registerClass( "MyClass", "org.pentaho.test.MyClass" );
    assertFalse( dialog.extendedClasses.isEmpty() );
    assertEquals( "org.pentaho.test.MyClass", dialog.extendedClasses.get( "MyClass" ) );
  }

  @Test
  public void testGetSwtInstance() throws Exception {

  }
}
