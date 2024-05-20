/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019 by Hitachi Vantara : http://www.pentaho.com
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
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.AbstractFileNameParser;
import org.apache.commons.vfs2.provider.UriParser;
import org.apache.commons.vfs2.provider.VfsComponentContext;
import org.pentaho.di.connections.vfs.VFSConnectionDetails;

import java.util.Objects;

import static org.apache.commons.vfs2.FileName.SEPARATOR_CHAR;

/**
 * This class parses a PVFS URI. Parsed values are returned as {@link ConnectionFileName} instances.
 * <p>
 * The syntax of a PVFS URI is:
 * <code>pvfs://[(connection-name)/[(path)]]</code>.
 *
 * <p>
 * Unlike standard URLs or URIs, PVFS URIs can contain special characters in the connection name (authority) section.
 * Still, like with path segments, connection names cannot contain the characters "\" and "/", not even if
 * percent-encoded.
 *
 * <p>
 * The path section is constituted by multiple segments separated by a "/" file separator:
 * <code>segment_1[/segment_2...[/segment_N[/]]]</code>, optionally terminated by a "/" character, to indicate
 * representing a folder. Alternatively, path segments may be separated by the "\" character.
 *
 * <p>
 * Parsing a URI validates its syntax and transforms it into canonical form:
 * <ul>
 *   <li>validates the scheme is "pvfs" and is followed by "://"</li>
 *   <li>"\" file path separators in the path sections are transformed into the canonical file path separator, "/"</li>
 *   <li>empty path segments, e.g. "//", are removed</li>
 *   <li>"." path segments are removed</li>
 *   <li>".." path segments are validated and resolved</li>
 *   <li>a path with a trailing slash is recognized as a folder</li>
 *   <li>any percent-encoded characters in path segments which are not reserved are decoded; currently, only the "%"
 *       character is reserved</li>
 * </ul>
 *
 * <p>
 * See {@link #SPECIAL_CHARACTERS_FULL_SET} for examples of special characters allowed (without encoding) in connection
 * names and in path segments. Spaces and international characters are also supported.
 *
 * @see VFSConnectionDetails#getRootPath()
 * @see org.pentaho.di.connections.vfs.VFSConnectionManagerHelper#getResolvedRootPath(VFSConnectionDetails)
 */
public class ConnectionFileNameParser extends AbstractFileNameParser {
  private static final String[] SCHEMES = new String[] { ConnectionFileProvider.SCHEME };

  private static final char[] RESERVED_CHARS = new char[] { '%' };

  /**
   * Full set of special characters the connection name can be.
   * Only excluding the character '/', '\' and '%', which are the file path delimiter, the alternative file path
   * delimiter, and the URL-encoding escape character, respectively.
   * <p/>
   * Special characters in this context are characters that generally have to be encoded
   * otherwise the use of {@link java.net.URI} will throw {@link java.net.URISyntaxException} when during
   * object instantiation or subsequent method calls.
   */
  public static final String SPECIAL_CHARACTERS_FULL_SET = "!\"#$&'()*+,-.:;<=>?@[] \t\n\r^_`{|}~";

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

  /**
   * Gets an array of reserved characters.
   * <p>
   * This list is in sync with the results of {@link #encodeCharacter(char)}.
   * This is needed in this format for use by {@link UriParser#encode(String, char[])}.
   * <p>
   * Currently, only the "%" character is reserved.
   *
   * @return An array of characters.
   */
  @NonNull
  public char[] getReservedChars() {
    return RESERVED_CHARS;
  }

  @NonNull
  protected ConnectionFileNameUtils getConnectionFileNameUtils() {
    return connectionFileNameUtils;
  }

  /**
   * Parses a PVFS URI within a given Apache VFS context.
   *
   * @param vfsComponentContext The VFS component context.
   * @param baseFileName        The base file name.
   * @param pvfsUri             The PVFS URI to parse.
   * @return The connection file name.
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
   * @return The connection file name.
   */
  @NonNull
  public ConnectionFileName parseUri( String pvfsUri ) throws FileSystemException {
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
    String encodedConnectionName = UriParser.extractFirstElement( name );

    // Examples
    // - encodedConnectionName = null     /     name = ""
    // - encodedConnectionName = "My Ligação"   name = "" | "Folder/Sub Folder/100%25"

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

    return new ConnectionFileName( encodedConnectionName, path, fileType );
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
