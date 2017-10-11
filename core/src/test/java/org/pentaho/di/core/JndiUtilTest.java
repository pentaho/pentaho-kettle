/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/
package org.pentaho.di.core;

import org.junit.Test;

import static org.junit.Assert.*;

public class JndiUtilTest {

  @Test
  public void testInitJNDI() throws Exception {
    final String factoryInitialKey = "java.naming.factory.initial";
    final String factoryInitialBak = System.getProperty( factoryInitialKey );
    final String sjRootKey = "org.osjava.sj.root";
    final String sjRootBak = System.getProperty( sjRootKey );
    final String sjDelimiterKey = "org.osjava.sj.root";
    final String sjDelimiterBak = System.getProperty( sjDelimiterKey );

    System.clearProperty( factoryInitialKey );
    System.clearProperty( sjRootKey );
    System.clearProperty( sjDelimiterKey );

    JndiUtil.initJNDI();

    try {
      assertFalse( System.getProperty( factoryInitialKey ).isEmpty() );
      assertFalse( System.getProperty( sjRootKey ).isEmpty() );
      assertFalse( System.getProperty( sjDelimiterKey ).isEmpty() );
      assertEquals( System.getProperty( sjRootKey ), Const.JNDI_DIRECTORY );
    } finally {
      if ( factoryInitialBak != null ) {
        System.setProperty( factoryInitialKey, factoryInitialBak );
      }
      if ( sjRootBak != null ) {
        System.setProperty( sjRootKey, sjRootBak );
      }
      if ( sjDelimiterBak != null ) {
        System.setProperty( sjDelimiterKey, sjDelimiterBak );
      }
    }
  }
}
