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


package org.pentaho.di.core.util;

import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.junit.rules.RestorePDIEnvironment;

import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Created by Yury_Bakhmutski on 11/4/2015.
 */
public class EnvUtilTest {
  @ClassRule public static RestorePDIEnvironment env = new RestorePDIEnvironment();

  @Test
  public void vfsUserDirIsRoot_IsPublishedOnInitialisation() throws Exception {
    EnvUtil.environmentInit();
    //See PDI-14522, PDI-14821
    // don't check the exact value, because the initialisation depends on local settings
    // instead, simply check the value exists
    assertNotNull( Variables.getADefaultVariableSpace().getVariable( Const.VFS_USER_DIR_IS_ROOT ) );
    assertNotNull( System.getProperty( Const.VFS_USER_DIR_IS_ROOT ) );
  }

  @Test
  public void createLocale_Null() throws Exception {
    assertNull( EnvUtil.createLocale( null ) );
  }

  @Test
  public void createLocale_Empty() throws Exception {
    assertNull( EnvUtil.createLocale( "" ) );
  }

  @Test
  public void createLocale_SingleCode() throws Exception {
    assertEquals( Locale.ENGLISH, EnvUtil.createLocale( "en" ) );
  }

  @Test
  public void createLocale_DoubleCode() throws Exception {
    assertEquals( Locale.US, EnvUtil.createLocale( "en_US" ) );
  }

  @Test
  public void createLocale_DoubleCode_Variant() throws Exception {
    assertEquals( new Locale( "no", "NO", "NY" ), EnvUtil.createLocale( "no_NO_NY" ) );
  }

  @Test
  public void testClearProperty() {
    String dummyPropertyTest = "KETTLE_CORE_JUNIT_DUMMY_PROPERTY_TEST_"+Math.random();
    try {
      assertNull( EnvUtil.getSystemProperty( dummyPropertyTest ) );
      System.setProperty( dummyPropertyTest, "dummyValue" );
      assertEquals( "dummyValue", EnvUtil.getSystemProperty( dummyPropertyTest ) );
      assertEquals( "dummyValue", EnvUtil.clearSystemProperty( dummyPropertyTest ) );
      assertNull( EnvUtil.getSystemProperty( dummyPropertyTest ) );
    } finally {
      //assures that the test property is removed from the System
      System.clearProperty( dummyPropertyTest );
    }
  }
}
