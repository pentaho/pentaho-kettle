/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.core.version;

import static org.junit.Assert.fail;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.version.BuildVersion;

public class BuildVersionUnitTest {

  @Test
  public void testGetInstance() {
    BuildVersion version = BuildVersion.getInstance();
    if ( version == null || version.getVersion().isEmpty() ) {
      fail( "Unable to retrieve BuildVersion" );
    }
    BuildVersion version2 = BuildVersion.getInstance();
    if ( version2 != version ) {
      fail( "Build version is required to be singleton" );
    }
  }

  @Test
  public void testGetBuildDate() {
    BuildVersion version = BuildVersion.getInstance();
    String buildDate = version.getBuildDate();
    if ( buildDate == null || buildDate.isEmpty() ) {
      fail( "Unable to retrieve build date" );
    }

  }

  @Test
  public void testGetBuildDateAsLocalDate() {
    BuildVersion version = BuildVersion.getInstance();
    Date buildDate = version.getBuildDateAsLocalDate();
    if ( buildDate == null ) {
      fail( "Unable to retrieve build date as Local date" );
    }
  }

  private static String buildDateTestString = "2014-12-13 10.20.30";
  private static String buildDateFailString = "this better fail!";

  @Test
  public void testSetBuildDate() {
    BuildVersion version = BuildVersion.getInstance();
    // since this is a singleton, preserve the initial
    String originalDate = version.getBuildDate();
    try {
      version.setBuildDate( buildDateTestString );
    } catch ( Exception ex ) {
      fail( "Error setting valid date" );
    }
    String newDate = version.getBuildDate();
    if ( newDate != buildDateTestString ) {
      fail( "Error setting build date" );
    }
    SimpleDateFormat formatter = new SimpleDateFormat( "yyyy-MM-dd HH.mm.ss" );
    try {
      Date newDateAsDate = version.getBuildDateAsLocalDate();
      Date buildDateTestDate = formatter.parse( buildDateTestString );
      if ( !newDateAsDate.equals( buildDateTestDate ) ) {
        fail( "Date fields don't match: " + newDateAsDate + " -> " + buildDateTestDate );
      }
    } catch ( Exception ex ) {
      fail( "Exception parsing fields : " + ex.getMessage() );
    }

    // try to set a bogus date...
    boolean bogusGotException = false;
    try {
      version.setBuildDate( buildDateFailString );
      fail( "Should have gotten exception setting invalid date." );
    } catch ( Exception ex ) {
      bogusGotException = true; // we SHOULD get here...
    }
    if ( !bogusGotException ) {
      fail( "Should have gotten exception setting invalid date." );
    }
    // make sure it's still the previous set...
    String stillGood = version.getBuildDate();
    if ( stillGood == null || !stillGood.equals( buildDateTestString ) ) {
      fail( "Failed setting date to invalid date - but left date as invalid." );
    }
    // set it back to the original
    try {
      version.setBuildDate( originalDate );
    } catch ( Exception ex ) {
      fail( "Error setting date back to original..." );
    }
  }

  @Test
  public void testGetVersion() {
    BuildVersion version = BuildVersion.getInstance();
    String buildversion = version.getVersion();
    if ( buildversion != Const.VERSION ) {
      fail( "Unexpected version number found." );
    }
  }

  private static String testVersionString = "Unit Test Version";

  @Test
  public void testSetVersion() {
    BuildVersion version = BuildVersion.getInstance();
    String origVersion = version.getVersion();
    version.setVersion( testVersionString );
    String newVersion = version.getVersion();
    if ( newVersion == null || !newVersion.equals( testVersionString ) ) {
      fail( "Unable to set version" );
    }
    // put it back to where it was
    version.setVersion( origVersion );

  }

  @Test
  public void testGetRevision() {
    BuildVersion version = BuildVersion.getInstance();
    String revision = version.getRevision();
    if ( revision == null || !revision.isEmpty() ) {
      fail( "Unexpected revsision found : " + revision );
    }
  }

  private static String testRevisionString = "Unit Test Revision";

  @Test
  public void testSetRevision() {
    BuildVersion version = BuildVersion.getInstance();
    String revision = version.getRevision();
    version.setRevision( testRevisionString );
    String newRevision = version.getRevision();
    if ( newRevision == null || !newRevision.equals( testRevisionString ) ) {
      fail( "Error setting revision." );
    }
    // restore it since it's a singleton
    version.setRevision( revision );
  }

  @Test
  public void testGetBuildUser() {
    BuildVersion version = BuildVersion.getInstance();
    String buser = version.getBuildUser();
    if ( buser == null | !buser.isEmpty() ) {
      fail( "Unable to retrieve user." );
    }
  }

  private static String testUserString = "Unit Test User";

  @Test
  public void testSetBuildUser() {
    BuildVersion version = BuildVersion.getInstance();
    String buser = version.getBuildUser();
    version.setBuildUser( testUserString );
    String newUser = version.getBuildUser();
    if ( newUser == null || !newUser.equals( testUserString ) ) {
      fail( "Error setting build user" );
    }
    // reset the singleton
    version.setBuildUser( buser );

  }

}
