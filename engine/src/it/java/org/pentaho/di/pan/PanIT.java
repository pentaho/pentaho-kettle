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
package org.pentaho.di.pan;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.security.Permission;

import static org.junit.Assert.assertFalse;

public class PanIT {

  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();

  private PrintStream oldOut;
  private PrintStream oldErr;
  private SecurityManager oldSecurityManager;

  @Before
  public void setUp() throws KettleException {
    KettleEnvironment.init();
    oldSecurityManager = System.getSecurityManager();
    System.setSecurityManager( new PanIT.MySecurityManager( oldSecurityManager ) );
  }

  @After
  public void tearDown() {
    System.setSecurityManager( oldSecurityManager );
  }


  @Test
  public void testArchivedTransExecution() throws Exception {
    String file = this.getClass().getResource( "test-ktr.zip" ).getFile();
    String[] args = new String[] { "/file:zip:file://" + file + "!Pan.ktr" };
    oldOut = System.out;
    oldErr = System.err;
    System.setOut( new PrintStream( outContent ) );
    System.setErr( new PrintStream( errContent ) );
    try {
      Pan.main( args );
    } catch ( SecurityException e ) {
      System.setOut( oldOut );
      System.setErr( oldErr );
      System.out.println( outContent );
      assertFalse( outContent.toString().contains( "error" ) );
      assertFalse( outContent.toString().contains( "stopped" ) );
    }
  }

  @Test
  public void testFileTransExecution() throws Exception {
    String file = this.getClass().getResource( "Pan.ktr" ).getFile();
    String[] args = new String[] { "/file:" + file };
    oldOut = System.out;
    oldErr = System.err;
    System.setOut( new PrintStream( outContent ) );
    System.setErr( new PrintStream( errContent ) );
    try {
      Pan.main( args );
    } catch ( SecurityException e ) {
      System.setOut( oldOut );
      System.setErr( oldErr );
      System.out.println( outContent );
      assertFalse( outContent.toString().contains( "error" ) );
      assertFalse( outContent.toString().contains( "stopped" ) );
    }
  }

  public class MySecurityManager extends SecurityManager {

    private SecurityManager baseSecurityManager;

    public MySecurityManager( SecurityManager baseSecurityManager ) {
      this.baseSecurityManager = baseSecurityManager;
    }

    @Override
    public void checkPermission( Permission permission ) {
      if ( permission.getName().startsWith( "exitVM" ) ) {
        throw new SecurityException( "System exit not allowed" );
      }
      if ( baseSecurityManager != null ) {
        baseSecurityManager.checkPermission( permission );
      } else {
        return;
      }
    }

  }
}
