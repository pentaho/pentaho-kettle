/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019-2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.connections.vfs.provider;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.AbstractFileNameParser;
import org.apache.commons.vfs2.provider.FileNameParser;
import org.apache.commons.vfs2.provider.UriParser;
import org.apache.commons.vfs2.provider.VfsComponentContext;
import org.pentaho.di.connections.vfs.VFSConnectionDetails;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.vfs.KettleVFSFileSystemException;

import java.util.Objects;
import java.util.regex.Pattern;

import static org.apache.commons.vfs2.FileName.SEPARATOR;
import static org.apache.commons.vfs2.FileName.SEPARATOR_CHAR;
import static org.apache.commons.vfs2.provider.UriParser.TRANS_SEPARATOR;

/**
 * This class parses a PVFS URI. Parsed values are returned as {@link ConnectionFileName} instances.
 * <p>
 * The syntax of a PVFS URI is:
 * <code>pvfs://[(connection)/[(path)]]</code>.
 *
 * <p>
 * Unlike standard URLs and URIs, PVFS URIs can contain special characters in the connection name (authority) section,
 * without encoding. However, certain characters are still considered invalid in the connection name,
 * {@link #CONNECTION_NAME_INVALID_CHARACTERS}.
 *
 * <p>
 * The path section is constituted by multiple segments separated by {@link FileName#SEPARATOR}:
 * <code>segment_1[/segment_2...[/segment_N[/]]]</code>, optionally terminated by a {@link FileName#SEPARATOR}
 * character, to indicate representing a folder. Alternatively, path segments may be separated by the
 * {@link UriParser#TRANS_SEPARATOR} character. Path segments cannot contain the characters in
 * {@link #INVALID_CHARACTERS}. Path segments can contain the percent character, "%", but only/always percent-encoded.
 *
 * <p>
 * Parsing a URI validates its syntax and transforms it into canonical form:
 * <ul>
 *   <li>validates the scheme is "pvfs" and is followed by "://"</li>
 *   <li>{@link UriParser#TRANS_SEPARATOR} file path separators in the path sections are transformed into the canonical
 *       file path separator, {@link FileName#SEPARATOR}</li>
 *   <li>empty path segments, e.g. "//", are removed</li>
 *   <li>"." path segments are removed</li>
 *   <li>".." path segments are validated and resolved</li>
 *   <li>a path with a trailing slash is recognized as a folder</li>
 *   <li>any percent-encoded characters in path segments which are not {@link #encodeCharacter(char)} reserved} are
 *       decoded</li>
 * </ul>
 *
 * <p>
 * See {@link #SPECIAL_CHARACTERS} for examples of special characters allowed (without encoding) in connection
 * names and in path segments. Spaces and international characters are also supported.
 *
 * <p>
 * See {@code AbstractFileName#RESERVED_URI_CHARS} for some more context on Apache VFS file names and reserved
 * characters.
 *
 * @see VFSConnectionDetails#getRootPath()
 * @see org.pentaho.di.connections.vfs.VFSConnectionManagerHelper#getResolvedRootPath(VFSConnectionDetails)
 */
public class ConnectionFileNameParser extends AbstractFileNameParser {
  /**
   * Helper array containing the single scheme accepted by PVFS file names, {@link ConnectionFileProvider#SCHEME}.
   */
  private static final String[] SCHEMES = new String[] { ConnectionFileProvider.SCHEME };

  /**
   * The characters which are invalid for both connection names and path segments.
   */
  public static final String INVALID_CHARACTERS = SEPARATOR + TRANS_SEPARATOR;

  /**
   * The characters which are invalid for connection names.
   */
  public static final String CONNECTION_NAME_INVALID_CHARACTERS = INVALID_CHARACTERS + "%";

  /**
   * The pattern of invalid connection name characters.
   */
  public static final Pattern CONNECTION_NAME_INVALID_CHARACTERS_PATTERN = Pattern.compile(
    "[" + Pattern.quote( CONNECTION_NAME_INVALID_CHARACTERS ) + "]" );

  // Overriding just to be able to add the custom doclet. Had to add `final` to quiet Sonar, for complaining about it.

  /**
   * Indicates if a character is reserved w.r.t. to percent-encoding, and is thus always encoded in canonical encoding
   * form, as implemented by {@link UriParser#canonicalizePath(StringBuilder, int, int, FileNameParser)}.
   * <p>
   * Currently, only the "%" character — the URL-encoding escape character — is reserved.
   *
   * @param ch The character to test.
   * @return {@code true} if the character is reserved; {@code false}, otherwise.
   */
  @Override
  public final boolean encodeCharacter( char ch ) {
    return super.encodeCharacter( ch );
  }

  /**
   * Full set of special characters the connection name can be.
   * Only excluding the characters: {@link FileName#SEPARATOR}, {@link UriParser#TRANS_SEPARATOR} and those which are
   * {@link #encodeCharacter(char) reserved}.
   * <p/>
   * Special characters in this context are characters that generally have to be encoded, otherwise the use of
   * {@link java.net.URI} will throw {@link java.net.URISyntaxException} when during object instantiation or subsequent
   * method calls.
   */
  public static final String SPECIAL_CHARACTERS = "!\"#$&'()*+,-.:;<=>?@[] \t\n\r^_`{|}~";

  private static ConnectionFileNameParser instance;

  @NonNull
  private final ConnectionFileNameUtils connectionFileNameUtils;

  public ConnectionFileNameParser() {
    this( ConnectionFileNameUtils.getInstance() );
  }

  public ConnectionFileNameParser( @NonNull ConnectionFileNameUtils connectionFileNameUtils ) {
    this.connectionFileNameUtils = Objects.requireNonNull( connectionFileNameUtils );
  }

  @NonNull
  public static ConnectionFileNameParser getInstance() {
    if ( instance == null ) {
      instance = new ConnectionFileNameParser();
    }

    return instance;
  }

  @NonNull
  protected ConnectionFileNameUtils getConnectionFileNameUtils() {
    return connectionFileNameUtils;
  }

  public void validateConnectionName( @Nullable String connectionName ) throws FileSystemException {
    if ( StringUtils.isEmpty( connectionName ) ) {
      throw new KettleVFSFileSystemException( "ConnectionFileNameParser.ConnectionNameEmpty" );
    }

    for ( char c : connectionName.toCharArray() ) {
      if ( !isValidConnectionNameCharacter( c ) ) {
        throw new KettleVFSFileSystemException(
          "ConnectionFileNameParser.ConnectionNameInvalidCharacter",
          connectionName,
          c );
      }
    }
  }

  /**
   * Determines if a given character is a valid in a connection name.
   *
   * @param c The character to test.
   * @return {@code true} if the character is valid; {@code false}, otherwise.
   */
  public boolean isValidConnectionNameCharacter( char c ) {
    return CONNECTION_NAME_INVALID_CHARACTERS.indexOf( c ) < 0;
  }

  /**
   * Removes any invalid characters from a potential connection name.
   *
   * @param connectionName The potential connection name.
   * @return A corresponding sanitized connection name.
   */
  public String sanitizeConnectionName( String connectionName ) {
    return Const.NVL( connectionName, "" )
      .replaceAll( "[" + Pattern.quote( CONNECTION_NAME_INVALID_CHARACTERS ) + "]", "" );
  }

  /**
   * Parses a PVFS URI under an Apache VFS context.
   *
   * @param vfsComponentContext The VFS component context.
   * @param baseFileName        The base file name.
   * @param pvfsUri             The PVFS URI to parse.
   * @return The connection file name, never {@code null}.
   * @throws FileSystemException When the given PVFS URI is not valid.
   */
  @Override
  public FileName parseUri( VfsComponentContext vfsComponentContext, FileName baseFileName, String pvfsUri )
    throws FileSystemException {
    return parseUri( pvfsUri );
  }

  /**
   * Parses a PVFS URI.
   *
   * @param pvfsUri The PVFS URI to parse.
   * @return The corresponding connection file name.
   * @throws FileSystemException When the given PVFS URI is not valid.
   */
  @NonNull
  public ConnectionFileName parseUri( @NonNull String pvfsUri ) throws FileSystemException {
    Objects.requireNonNull( pvfsUri );

    // Examples
    // "Ligação" means "Connection" in Portuguese
    // "%20" = " "
    // "%25" = "%"
    // - pvfsUri = "pvfs://"
    // - pvfsUri = "pvfs:///"
    // - pvfsUri = "pvfs://My Ligação"
    // - pvfsUri = "pvfs://My%20Liga%C3%A7%C3%A3o/Folder\Sub%20Folder/100%25"

    StringBuilder name = new StringBuilder();

    extractPvfsScheme( pvfsUri, name );

    // Examples: name = "" | "My Ligação" |  "My%20Liga%C3%A7%C3%A3o/Folder\Sub%20Folder/100%25"

    // Canonicalize the path's percent-encoding.
    UriParser.canonicalizePath( name, 0, name.length(), this );

    // Examples: name = "" | "My Ligação" |  "My Ligação/Folder\Sub Folder/100%25"
    // To get a fully decoded path, later call fileName.getPathDecoded() = "My Ligação/Folder/Sub Folder/100%".

    // Canonicalize separators, by converting "\" to "/".
    // Must be done before extracting the connection.
    UriParser.fixSeparators( name );

    // Examples: name = "" | "My Ligação" |  "My Ligação/Folder/Sub Folder/100%25"

    // Extract the connection name.
    // May be null/empty, in case pvfsUri = "pvfs://".
    @Nullable
    String connectionName = UriParser.extractFirstElement( name );
    if ( StringUtils.isNotEmpty( connectionName )) {
      // A connection with a percent (as %25) would throw here.
      validateConnectionName( connectionName );
    }

    // Examples
    // - connectionName = null     /     name = ""
    // - connectionName = "My Ligação"   name = "" | "Folder/Sub Folder/100%25"

    // Normalize the path section:
    // - Remove empty or "." segments
    // - Resolve ".." segments (including throwing if trying to go above the connection, e.g. "/../my-folder")
    // - Remove trailing separator and return file type (e.g. "/my-folder/")
    // When name is empty, fileType is FOLDER, which is the correct type for either the root or connection folder.
    FileType fileType = UriParser.normalisePath( name );

    // Examples: name = "" | "Folder/Sub Folder/100%25"

    getConnectionFileNameUtils().ensureLeadingSeparator( name );

    // Examples: name = "/" | "/Folder/Sub Folder/100%25"

    String path = name.toString();

    return new ConnectionFileName( connectionName, path, fileType );
  }

  private void extractPvfsScheme( String uri, StringBuilder name ) throws FileSystemException {
    // UriParser.extractScheme initializes `name` with the contents of uri, and only then extracts the scheme.
    // Scheme may not be present, or invalid (e.g. drive letter), in which case null is returned.
    if ( UriParser.extractScheme( SCHEMES, uri, name ) == null ) {
      throw new FileSystemException( "vfs.provider/invalid-scheme" );
    }

    // Extract "//" (based on HostFileNameParser#parseUri(..))
    // These two are not supported as "\".
    if ( name.length() < 2 || name.charAt( 0 ) != SEPARATOR_CHAR || name.charAt( 1 ) != SEPARATOR_CHAR ) {
      throw new FileSystemException( "vfs.provider/missing-double-slashes.error", uri );
    }

    name.delete( 0, 2 );
  }
}
