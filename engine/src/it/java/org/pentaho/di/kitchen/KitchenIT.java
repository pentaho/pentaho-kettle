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
package org.pentaho.di.kitchen;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.security.Permission;

import static org.junit.Assert.assertFalse;

public class KitchenIT {

  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();

  private PrintStream oldOut;
  private PrintStream oldErr;
  private SecurityManager oldSecurityManager;

  @Before
  public void setUp() {
    oldSecurityManager = System.getSecurityManager();
    System.setSecurityManager( new MySecurityManager( oldSecurityManager ) );
  }

  @After
  public void tearDown() {
    System.setSecurityManager( oldSecurityManager );
  }


  @Test
  public void testArchivedJobsExecution() throws Exception {
    String file = this.getClass().getResource( "test-kjb.zip" ).getFile();
    String[] args = new String[] { "/file:zip:file://" + file + "!Job.kjb" };
    oldOut = System.out;
    oldErr = System.err;
    System.setOut( new PrintStream( outContent ) );
    System.setErr( new PrintStream( errContent ) );
    try {
      Kitchen.main( args );
    } catch ( SecurityException e ) {
      System.setOut( oldOut );
      System.setErr( oldErr );
      System.out.println( outContent );
      assertFalse( outContent.toString().contains( "result=[false]" ) );
      assertFalse( outContent.toString().contains( "ERROR" ) );
    }
  }

  @Test
  public void testFileJobsExecution() throws Exception {
    String file = this.getClass().getResource( "Job.kjb" ).getFile();
    String[] args = new String[] { "/file:" + file };
    oldOut = System.out;
    oldErr = System.err;
    System.setOut( new PrintStream( outContent ) );
    System.setErr( new PrintStream( errContent ) );
    try {
      Kitchen.main( args );
    } catch ( SecurityException e ) {
      System.setOut( oldOut );
      System.setErr( oldErr );
      System.out.println( outContent );
      assertFalse( outContent.toString().contains( "result=[false]" ) );
      assertFalse( outContent.toString().contains( "ERROR" ) );
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
