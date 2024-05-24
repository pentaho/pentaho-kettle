/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2024 Hitachi Vantara. All rights reserved.
 */

package org.pentaho.di.connections.utils;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class to extract information from the
 * {@value org.pentaho.di.connections.vfs.provider.ConnectionFileProvider#SCHEME} URI and more general VFS URIs.
 * This class handles special characters in the connection name section of the
 * {@value org.pentaho.di.connections.vfs.provider.ConnectionFileProvider#SCHEME} URI by simply parsing
 * the connection name as is. This class has limited functionality.
 * <p/>
 * The goal of this class is to support international characters and {@link #SPECIAL_CHARACTERS_FULL_SET}
 * in the connection name {@link #connectionName}.
 * <p/>
 * Main motivation for this class is not being able to use directly {@link java.net.URI}
 * due to special characters in the {@value org.pentaho.di.connections.vfs.provider.ConnectionFileProvider#SCHEME} URI.
 * Future efforts should look into {@link org.apache.commons.vfs2.provider.UriParser#encode(String, char[])}.
 *
 * @see #SPECIAL_CHARACTERS_FULL_SET
 */
public class ConnectionUriParser {

  /**
   * Full set of special characters the connection name can be. Only excluding the character '/' which is a file path deliminator.
   * <p/>
   * Special characters in this context are characters that generally have to be encoded
   * otherwise the use of {@link java.net.URI } will throw {@link java.net.URISyntaxException} when during
   * object instantiation or subsequent method calls.
   *
   */
  public static final String SPECIAL_CHARACTERS_FULL_SET = "!\"#$%&'()*+,-.:;<=>?@[\\] \t\n\r^_`{|}~";

  /**
   * Pattern to match a {@value org.pentaho.di.connections.vfs.provider.ConnectionFileProvider#SCHEME} URI
   *  with scheme, connection name, and connection path.
   * <p/> Some Examples:
   * <ul>
   *   <li>For PVFS URI: "pvfs://connectionName/someFolderA/SomeFolderB/someFile.txt"</li>
   *      <ul>
   *        <li>scehme: "pvfs"</li>
   *        <li>connection Name: "connectionName"</li>
   *        <li>pvfs path: "/someFolderA/SomeFolderB/someFile.txt"</li>
   *
   *      </ul>
   * </ul>
   * Regex should be encompass {@link org.pentaho.di.connections.vfs.provider.ConnectionFileSystem#DOMAIN_ROOT}
   */
  public static final Pattern CONNECTION_URI_WITH_CONNECTION_NAME_AND_PATH_PATTERN
    = Pattern.compile(  "^(\\w+)://([^/]+)(/.+)" );

  /**
   * Pattern to match a URI with scheme and connection name or first path segment.
   * <p/> Some Examples:
   * <ul>
   *   <li>For PVFS URI: "pvfs://connectionName"</li>
   *   <li>For VFS URI: "xyz://firstSegment"</li>
   * </ul>
   * Regex should be encompass {@link org.pentaho.di.connections.vfs.provider.ConnectionFileSystem#DOMAIN_ROOT}
   */
  public static final Pattern CONNECTION_URI_WITH_CONNECTION_NAME_PATTERN
      = Pattern.compile(  "^(\\w+)://([^/]+)/?" );

  /**
   * Pattern to match a URI with just a scheme.
   * <p/> Some Examples:
   * <ul>
   *   <li>For PVFS URI: "pvfs://"</li>
   *   <li>For VFS URI: "xyz://"</li>
   * </ul>
   * Regex should be encompass {@link org.pentaho.di.connections.vfs.provider.ConnectionFileSystem#DOMAIN_ROOT}
   */
  public static final Pattern CONNECTION_URI_NAME_PATTERN = Pattern.compile(  "^(\\w+)://" );

  /**
   * {@link #scheme} index for {@link Matcher#group(int)}
   */
  private static final int GROUP_INDEX_SCHEME = 1;

  /**
   * {@link #connectionName} index for {@link Matcher#group(int)}
   */
  private static final int GROUP_INDEX_CONNECTION_NAME = 2;

  /**
   * {@link #connectionPath} index for {@link Matcher#group(int)}
   */
  private static final int GROUP_INDEX_CONNECTION_PATH = 3;

  /**
   * VFS URI. Can be general 'vfs" URI
   * or {@value org.pentaho.di.connections.vfs.provider.ConnectionFileProvider#SCHEME} URI
   */
  private final String vfsUri;

  /**
   * URI scheme or prefix
   */
  private String scheme;

  /**
   * URI connection name for {@value org.pentaho.di.connections.vfs.provider.ConnectionFileProvider#SCHEME} URI
   * or first path segment for VFS URI
   */
  private String connectionName;

  /**
   * Actual path segment for {@value org.pentaho.di.connections.vfs.provider.ConnectionFileProvider#SCHEME} URI
   */
  private String connectionPath;


  public ConnectionUriParser( String vfsUri ) {
    this.vfsUri = vfsUri;
    executeMatchers();
  }

  /**
   * Call the matchers to determine the various variables:
   * <ul>
   *   <li>{@link #scheme}</li>
   *   <li>{@link #connectionName}</li>
   *   <li>{@link #connectionPath}</li>
   * </ul>
   */
  private void executeMatchers() {
    try {
      // order of precedence for matchers
      Matcher[] matchers = new Matcher[] {
        CONNECTION_URI_WITH_CONNECTION_NAME_AND_PATH_PATTERN.matcher( this.vfsUri ),
        CONNECTION_URI_WITH_CONNECTION_NAME_PATTERN.matcher( this.vfsUri ),
        CONNECTION_URI_NAME_PATTERN.matcher( this.vfsUri )
      };

      Matcher matcher = Arrays.stream( matchers ).filter( Matcher::find ).findFirst().orElse( null );

      if ( matcher != null ) {
        setVariables( matcher );
      }

    } catch ( NullPointerException e ) {
      // do nothing
    }
  }

  /**
   * sets the variables based on the <code>matcher</code>:
   * <p/>
   * Variables to set, if matched:
   * <ul>
   *   <li>{@link #scheme}</li>
   *   <li>{@link #connectionName}</li>
   *   <li>{@link #connectionPath}</li>
   * </ul>
   */
  private void setVariables( Matcher matcher ) {
    this.scheme = getGroup( matcher, GROUP_INDEX_SCHEME, null );
    this.connectionName = getGroup( matcher, GROUP_INDEX_CONNECTION_NAME, null );
    this.connectionPath = getGroup( matcher, GROUP_INDEX_CONNECTION_PATH, null );
  }

  /**
   * Wrapper around {@link Matcher#group(String)}. If <code>index</code> is out of bounds,
   * then <code>defaultValue</code> will be returned.
   * @param matcher
   * @param index group index see {@link Matcher#group(String)}
   * @param defaultValue
   * @return value from {@link Matcher#group(String)}, otherwise <code>defaultValue</code>
   */
  private String getGroup( Matcher matcher, int index, String defaultValue ) {
    return index <= matcher.groupCount() ? matcher.group( index ) : defaultValue;
  }

  /**
   * Get the scheme
   * @see #scheme
   * @return scheme or null otherwise.
   */
  public String getScheme() {
    return scheme;
  }

  /**
   * Get the connection name or first segment of URI.
   * @see #connectionName
   * @return connection name or null otherwise
   */
  public String getConnectionName() {
    return connectionName;
  }

  /**
   * Get connection path of {@value org.pentaho.di.connections.vfs.provider.ConnectionFileProvider#SCHEME} URI,
   * does not include the {@link #connectionName}
   * @see #connectionPath
   * @return connection path or null otherwise
   */
  public String getConnectionPath() {
    return connectionPath;
  }

}
