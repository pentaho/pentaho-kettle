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

package org.pentaho.di.trans.steps.sapinput.sap;

public class SAPLibraryTester {

  private static final String JCO_LIB_EXISTENCE_TEST_CLASS = "com.sap.conn.jco.JCoDestinationManager";

  private static final String JCO_IMPL_EXISTENCE_TEST_CLASS = "com.sap.conn.rfc.driver.CpicDriver";

  public static boolean isJCoLibAvailable() {
    try {
      Object c = Class.forName( JCO_LIB_EXISTENCE_TEST_CLASS );
      if ( c == null ) {
        return false;
      }
      return true;
    } catch ( NoClassDefFoundError e ) {
      e.printStackTrace();
      return false;
    } catch ( ClassNotFoundException e ) {
      e.printStackTrace();
      return false;
    }
  }

  public static boolean isJCoImplAvailable() {
    // we simply skip the test because it does not work for sapjco3 >=3.0.2
    return true;
  }

  public static boolean isJCoImplAvailableNotUsed() {
    try {
      Object c = Class.forName( JCO_IMPL_EXISTENCE_TEST_CLASS );
      if ( c == null ) {
        return false;
      }
      return true;
    } catch ( UnsatisfiedLinkError e ) {
      e.printStackTrace();
      return false;
    } catch ( NoClassDefFoundError e ) {
      e.printStackTrace();
      return false;
    } catch ( ClassNotFoundException e ) {
      e.printStackTrace();
      return false;
    } catch ( Exception e ) {
      e.printStackTrace();
      return false;
    } catch ( Throwable e ) {
      e.printStackTrace();
      return false;
    }
  }

}
