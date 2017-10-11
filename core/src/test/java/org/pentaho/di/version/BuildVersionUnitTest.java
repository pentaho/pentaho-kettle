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

package org.pentaho.di.version;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.junit.Before;
import org.junit.Test;

public class BuildVersionUnitTest {
  private EnvironmentVariableGetter mockEnvironmentVariableGetter;
  private ManifestGetter mockManifestGetter;

  private void setNewMocks() {
    mockEnvironmentVariableGetter = mock( EnvironmentVariableGetter.class );
    mockManifestGetter = mock( ManifestGetter.class );
    BuildVersion.environmentVariableGetter = mockEnvironmentVariableGetter;
    BuildVersion.manifestGetter = mockManifestGetter;
  }

  @Before
  public void setup() {
    // This is to clear state between each run, init mocks
    setNewMocks();
    BuildVersion.refreshInstance();
    setNewMocks();
  }

  private void initManifestGetter( String version, String revision, String date, String user ) {
    Manifest manifest = mock( Manifest.class );
    try {
      when( mockManifestGetter.getManifest() ).thenReturn( manifest );
    } catch ( Exception e ) {
      // wont happen
    }
    Attributes attributes = mock( Attributes.class );
    when( manifest.getMainAttributes() ).thenReturn( attributes );
    when( manifest.getMainAttributes().getValue( Attributes.Name.IMPLEMENTATION_VERSION ) ).thenReturn( version );
    when( manifest.getMainAttributes().getValue( Attributes.Name.SPECIFICATION_VERSION ) ).thenReturn( revision );
    when( manifest.getMainAttributes().getValue( "Compile-Timestamp" ) ).thenReturn( date );
    when( manifest.getMainAttributes().getValue( "Compile-User" ) ).thenReturn( user );
  }

  private void initEnvironmentVariableGetter( String version, String revision, String date, String user ) {
    try {
      when( mockEnvironmentVariableGetter.getEnvVarible( BuildVersion.KETTLE_BUILD_VERSION ) ).thenReturn( version );
      when( mockEnvironmentVariableGetter.getEnvVarible( BuildVersion.KETTLE_BUILD_REVISION ) ).thenReturn( revision );
      when( mockEnvironmentVariableGetter.getEnvVarible( BuildVersion.KETTLE_BUILD_DATE ) ).thenReturn( date );
      when( mockEnvironmentVariableGetter.getEnvVarible( BuildVersion.KETTLE_BUILD_USER ) ).thenReturn( user );
    } catch ( Exception e ) {
      // wont happen
    }
  }

  @Test
  public void testGetInstance() {

    initManifestGetter( null, null, null, null ); // it's possible to have a manifest w/ no version, which causes an NPE
    BuildVersion.refreshInstance();
    BuildVersion version = BuildVersion.getInstance();
    if ( version == null || version.getVersion() == null || version.getVersion().isEmpty() ) {
      fail( "Unable to retrieve BuildVersion" );
    }

    initManifestGetter( "version", null, null, null );
    BuildVersion.refreshInstance();
    version = BuildVersion.getInstance();
    if ( version == null || version.getVersion().isEmpty() ) {
      fail( "Unable to retrieve BuildVersion" );
    }
    BuildVersion version2 = BuildVersion.getInstance();
    if ( version2 != version ) {
      fail( "Build version is required to be singleton" );
    }

    initManifestGetter( null, null, null, null ); // it's possible to have a manifest w/ no version, which causes an NPE
    initEnvironmentVariableGetter( null, null, null, null ); // it's possible to have a manifest w/ no version, which
                                                             // causes an NPE
    BuildVersion.refreshInstance();
    version = BuildVersion.getInstance();
    if ( version == null || version.getVersion() == null || version.getVersion().isEmpty() ) {
      fail( "Unable to retrieve BuildVersion" );
    }

    initEnvironmentVariableGetter( "version", null, null, null ); // it's possible to have a manifest w/ no version,
                                                                  // which causes an NPE
    BuildVersion.refreshInstance();
    version = BuildVersion.getInstance();
    if ( version == null || version.getVersion().isEmpty() ) {
      fail( "Unable to retrieve BuildVersion" );
    }
    version2 = BuildVersion.getInstance();
    if ( version2 != version ) {
      fail( "Build version is required to be singleton" );
    }

  }

  @Test
  public void testGetBuildDate() {
    initManifestGetter( null, null, new Date().toString(), null );
    BuildVersion.refreshInstance();
    BuildVersion version = BuildVersion.getInstance();
    String buildDate = version.getBuildDate();
    if ( buildDate == null || buildDate.isEmpty() ) {
      fail( "Unable to retrieve build date" );
    }

  }

  @Test
  public void testGetBuildDateAsLocalDate() {
    SimpleDateFormat sdf = new SimpleDateFormat( BuildVersion.JAR_BUILD_DATE_FORMAT );
    initManifestGetter( null, null, sdf.format( new Date() ), null );
    BuildVersion.refreshInstance();
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
  }

  @Test
  public void testGetVersion() {
    BuildVersion version = BuildVersion.getInstance();
    String buildversion = version.getVersion();
    if ( buildversion != BuildVersion.getInstance().getVersion() ) {
      fail( "Unexpected version number found." );
    }
  }

  private static String testVersionString = "Unit Test Version";

  @Test
  public void testSetVersion() {
    BuildVersion version = BuildVersion.getInstance();
    version.setVersion( testVersionString );
    String newVersion = version.getVersion();
    if ( newVersion == null || !newVersion.equals( testVersionString ) ) {
      fail( "Unable to set version" );
    }
  }

  @Test
  public void testGetRevision() {
    initEnvironmentVariableGetter( "version", "revision", new Date().toString(), "user" );
    BuildVersion.refreshInstance();
    BuildVersion version = BuildVersion.getInstance();
    String revision = version.getRevision();
    if ( !"revision".equals( revision ) ) {
      fail( "Unexpected revsision found : " + revision );
    }
  }

  private static String testRevisionString = "Unit Test Revision";

  @Test
  public void testSetRevision() {
    BuildVersion version = BuildVersion.getInstance();
    version.setRevision( testRevisionString );
    String newRevision = version.getRevision();
    if ( newRevision == null || !newRevision.equals( testRevisionString ) ) {
      fail( "Error setting revision." );
    }
  }

  @Test
  public void testGetBuildUser() {
    initManifestGetter( null, null, null, testUserString );
    BuildVersion.refreshInstance();
    BuildVersion version = BuildVersion.getInstance();
    String buser = version.getBuildUser();
    if ( buser == null || buser.isEmpty() ) {
      fail( "Unable to retrieve user." );
    }
  }

  private static String testUserString = "Unit Test User";

  @Test
  public void testSetBuildUser() {
    BuildVersion version = BuildVersion.getInstance();
    version.setBuildUser( testUserString );
    String newUser = version.getBuildUser();
    if ( newUser == null || !newUser.equals( testUserString ) ) {
      fail( "Error setting build user" );
    }
  }

  @Test
  public void testInitFromManifest() {
    String version = "manversion";
    String revision = "manrevision";
    String date = "mandate";
    String user = "manuser";
    initManifestGetter( version, revision, date, user );
    BuildVersion.refreshInstance();
    assertEquals( version, BuildVersion.getInstance().getVersion() );
    assertEquals( revision, BuildVersion.getInstance().getRevision() );
    assertEquals( date, BuildVersion.getInstance().getBuildDate() );
    assertEquals( user, BuildVersion.getInstance().getBuildUser() );
  }

  @Test
  public void testInitFromEnv() {
    String version = "envversion";
    String revision = "envrevision";
    String date = "envdate";
    String user = "envuser";
    initEnvironmentVariableGetter( version, revision, date, user );
    BuildVersion.refreshInstance();
    assertEquals( version, BuildVersion.getInstance().getVersion() );
    assertEquals( revision, BuildVersion.getInstance().getRevision() );
    assertEquals( date, BuildVersion.getInstance().getBuildDate() );
    assertEquals( user, BuildVersion.getInstance().getBuildUser() );
  }
}
