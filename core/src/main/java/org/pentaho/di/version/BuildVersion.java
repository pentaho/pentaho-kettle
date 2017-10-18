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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.pentaho.di.core.exception.KettleVersionException;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.xml.XMLHandler;

/**
 * Singleton class to allow us to see on which date & time the kettle3.jar was built.
 * 
 * @author Matt
 * @since 2006-aug-12
 */
public class BuildVersion {
  public static final String REFERENCE_FILE = "/kettle-steps.xml";

  public static final String JAR_BUILD_DATE_FORMAT = "yyyy-MM-dd HH.mm.ss";

  public static final String KETTLE_BUILD_VERSION = "KETTLE_BUILD_VERSION";

  public static final String KETTLE_BUILD_REVISION = "KETTLE_BUILD_REVISION";

  public static final String KETTLE_BUILD_DATE = "KETTLE_BUILD_DATE";

  public static final String KETTLE_BUILD_USER = "KETTLE_BUILD_USER";

  protected static ManifestGetter manifestGetter = new ManifestGetter();

  protected static EnvironmentVariableGetter environmentVariableGetter = new EnvironmentVariableGetter();

  private static BuildVersion buildVersion = new BuildVersion();

  /**
   * @return the instance of the BuildVersion singleton
   */
  public static final BuildVersion getInstance() {
    return buildVersion;
  }

  protected static void refreshInstance() {
    buildVersion = new BuildVersion();
  }

  private String version;
  private String revision;
  private String buildDate;
  private String buildUser;

  private void loadBuildInfoFromManifest() throws Exception {
    Manifest manifest = manifestGetter.getManifest();

    version = manifest.getMainAttributes().getValue( Attributes.Name.IMPLEMENTATION_VERSION );
    revision = manifest.getMainAttributes().getValue( Attributes.Name.SPECIFICATION_VERSION );
    buildDate = manifest.getMainAttributes().getValue( "Compile-Timestamp" );
    buildUser = manifest.getMainAttributes().getValue( "Compile-User" );
    if ( version == null ) {
      throw new Exception( "Error:  Version can't be NULL in manifest." );
    }
  }

  private void loadBuildInfoFromEnvironmentVariables() throws Exception {
    version = environmentVariableGetter.getEnvVarible( "KETTLE_BUILD_VERSION" );
    revision = environmentVariableGetter.getEnvVarible( "KETTLE_BUILD_REVISION" );
    buildDate = environmentVariableGetter.getEnvVarible( "KETTLE_BUILD_DATE" );
    buildUser = environmentVariableGetter.getEnvVarible( "KETTLE_BUILD_USER" );
    if ( version == null ) {
      throw new Exception( "Error : Version can't be null in environment variables" );
    }
  }

  private BuildVersion() {
    try {
      loadBuildInfoFromManifest();
    } catch ( Throwable e ) {
      try {
        loadBuildInfoFromEnvironmentVariables();
      } catch ( Throwable e2 ) {
        version = "Unknown";
        revision = "0";
        buildDate = XMLHandler.date2string( new Date() );
        buildUser = System.getProperty( "user.name" );
      }
    }
  }

  /**
   * @return the buildDate
   */
  public String getBuildDate() {
    return buildDate;
  }

  public Date getBuildDateAsLocalDate() {

    SimpleDateFormat sdf = new SimpleDateFormat( JAR_BUILD_DATE_FORMAT );
    try {
      Date d = sdf.parse( buildDate );
      return d;
      // ignore failure, retry using standard format
    } catch ( ParseException e ) {
      // Ignore
    }

    sdf = new SimpleDateFormat( ValueMeta.DEFAULT_DATE_FORMAT_MASK );
    try {
      Date d = sdf.parse( buildDate );
      return d;
      // ignore failure and return null
    } catch ( ParseException e ) {
      // Ignore
    }

    return null;

  }

  /**
   * @param buildDate
   *          the buildDate to set
   */
  public void setBuildDate( String buildDate ) throws KettleVersionException {
    // don't let them set a bogus date...
    String tempDate = this.buildDate;
    this.buildDate = buildDate;
    Date testDate = getBuildDateAsLocalDate();
    if ( testDate == null ) {
      // reset it to the old date...
      this.buildDate = tempDate;
      throw new KettleVersionException( "Error:  Invalid date being set as build date" ); // this should be
                                                                                          // localizable... next
      // pass....
    }
  }

  /**
   * @return the version
   */
  public String getVersion() {
    return version;
  }

  /**
   * @param revision
   *          the version to set
   */
  public void setVersion( String version ) {
    this.version = version;
  }

  /**
   * @return the revision
   */
  public String getRevision() {
    return revision;
  }

  /**
   * @param revision
   *          the revision to set
   */
  public void setRevision( String revision ) {
    this.revision = revision;
  }

  /**
   * @return the buildUser
   */
  public String getBuildUser() {
    return buildUser;
  }

  /**
   * @param buildUser
   *          the buildUser to set
   */
  public void setBuildUser( String buildUser ) {
    this.buildUser = buildUser;
  }
}
