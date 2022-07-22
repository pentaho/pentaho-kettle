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
package org.pentaho.di.trans.steps.monetdbbulkloader;

import java.util.regex.Pattern;

import org.pentaho.di.i18n.BaseMessages;

/**
 * @author Tatsiana_Kasiankova
 *
 */
public class MonetDbVersion implements Comparable<MonetDbVersion> {
  private static Class<?> PKG = MonetDbVersion.class; // for i18n purposes, needed by Translator2!!

  private Integer minorVersion;

  private Integer majorVersion;

  private Integer patchVersion;

  private static final String SEPARATOR = "\\.";

  /**
   * The pattern to determine if the given string is a valid MonetDB version in the assumption that it should contain
   * only digits separated by dots. Ideally version should be represented as
   * <p>
   * <code>majorVersion.minorVersion.patchVersion</code>
   * <p>
   * But also we could have any additional groups of digits after <code>patchVersion</code>. Or have only major and
   * minor version parts.
   * <p>
   * <b>Examples of valid MonetDB versions:</b>
   * <p>
   * <code>
   * <p>11.17.17
   * <p>11.0
   * <p>11.5.17.1
   * </code>
   *
   */
  private static final Pattern VERSION_PATTERN = Pattern.compile( "^[0-9]+(\\.[0-9]+)*$" );

  /**
   * The major version of the Jan2014-SP2 release.
   */
  private static final int JAN_2014_SP2_RELEASE_DB_MAJOR_VERSION = 11;

  /**
   * The minor version of the Jan2014-SP2 release.
   */
  private static final int JAN_2014_SP2_RELEASE_DB_MINOR_VERSION = 17;

  /**
   * The patch version of the Jan2014-SP2 release.
   */
  private static final int JAN_2014_SP2_RELEASE_DB_PATCH_VERSION = 17;

  /**
   * Jan2014-SP2 release MonetDB version.
   */
  public static final MonetDbVersion JAN_2014_SP2_DB_VERSION = new MonetDbVersion(
      JAN_2014_SP2_RELEASE_DB_MAJOR_VERSION, JAN_2014_SP2_RELEASE_DB_MINOR_VERSION,
      JAN_2014_SP2_RELEASE_DB_PATCH_VERSION );

  public MonetDbVersion() {
    super();
  }

  public MonetDbVersion( int majorVersion, int minorVersion, int patchVersion ) {
    super();
    this.majorVersion = majorVersion;
    this.minorVersion = minorVersion;
    this.patchVersion = patchVersion;
  }

  /**
   * @param productVersion
   * @throws MonetDbVersionException
   */
  public MonetDbVersion( String productVersion ) throws MonetDbVersionException {
    super();
    parseVersion( productVersion );
  }

  /**
   * @return the minorVersion
   */
  public Integer getMinorVersion() {
    return minorVersion;
  }

  /**
   * @return the majorVersion
   */
  public Integer getMajorVersion() {
    return majorVersion;
  }

  /**
   * @return the patchVersion
   */
  public Integer getPatchVersion() {
    return patchVersion;
  }

  @Override
  public int compareTo( MonetDbVersion mDbVersion ) {
    int result = majorVersion.compareTo( mDbVersion.majorVersion );
    if ( result != 0 ) {
      return result;
    }

    result = minorVersion.compareTo( mDbVersion.minorVersion );
    if ( result != 0 ) {
      return result;
    }

    result = patchVersion.compareTo( mDbVersion.patchVersion );
    if ( result != 0 ) {
      return result;
    }
    return result;
  }

  /**
   * Parses string representation of MonetDb version. Sets up <code>majorVersion</code>. Also <code>minorVersion</code>
   * and <code>patchVersion</code> if they are present in the product version. Omits all other possible parts as
   * insignificant.
   *
   * @param productVersion
   *          a string representation of version
   * @throws MonetDbVersionException
   *           if productVersion is null or has incorrect format ( see {@link MonetDbVersion#VERSION_PATTERN} )
   */
  private void parseVersion( String productVersion ) throws MonetDbVersionException {
    if ( productVersion == null ) {
      throw new MonetDbVersionException( BaseMessages.getString( PKG, "MonetDBVersion.Exception.VersionIsNull" ) );
    }

    if ( !VERSION_PATTERN.matcher( productVersion ).matches() ) {
      throw new MonetDbVersionException( BaseMessages.getString( PKG,
          "MonetDBVersion.Exception.VersionFormatIsInvalid", productVersion ) );
    }

    int startIndex = 0;
    String[] versionParts = productVersion.split( SEPARATOR );
    majorVersion = Integer.valueOf( versionParts[startIndex] );
    if ( versionParts.length > 1 ) {
      minorVersion = Integer.valueOf( versionParts[startIndex + 1] );
    }
    if ( versionParts.length > 2 ) {
      patchVersion = Integer.valueOf( versionParts[startIndex + 2] );
    }

  }

  @Override
  public String toString() {
    return "MonetDbVersion: " + majorVersion + "." + minorVersion + "." + patchVersion;
  }

}
