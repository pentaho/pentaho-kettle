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

package org.pentaho.di.resource;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.pentaho.di.core.variables.VariableSpace;

public class SimpleResourceNaming implements ResourceNamingInterface {

  private final Map<String, String> namedResources = new HashMap<String, String>();

  /**
   * The fileSystemPrefix would be appropriate for something like:
   *
   * zip://somefile.zip! or somefilesystem://export/folder/
   */
  private String fileSystemPrefix;

  private boolean useOriginalPathInTargetName;

  private Map<String, String> directoryMap;
  private int parameterNr;

  public SimpleResourceNaming() {
    super();

    directoryMap = new HashMap<String, String>();
    parameterNr = 0;
  }

  public SimpleResourceNaming( String fileSystemPrefix ) {
    super();
    this.fileSystemPrefix = fileSystemPrefix;
  }

  public String nameResource( FileObject fileObject, VariableSpace space, boolean includeFileName ) throws FileSystemException {
    if ( includeFileName ) {
      return handleDataFile( fileObject.getName().getBaseName(), fileObject.getParent().getURL().toString(), "" );
    } else {
      return handleDataFile( "", fileObject.getURL().toString(), "" );
    }
  }

  public String nameResource( String prefix, String originalFilePath, String extension, FileNamingType namingType ) {
    switch ( namingType ) {
      case DATA_FILE:
        return handleDataFile( prefix, originalFilePath, extension );

      case SHELL_SCRIPT:
        return handleScript( prefix, originalFilePath, extension );

      case TRANSFORMATION:
      case JOB:
        return handleTransformationOrJob( prefix, originalFilePath, extension );

      default:
        throw new AssertionError( "Unknown file naming type: " + namingType );
    }
  }

  private String handleTransformationOrJob( String prefix, String originalFilePath, String extension ) {
    //
    // End result could look like any of the following:
    //
    // Inputs:
    // Prefix : Marc Sample Transformation
    // Original Path: D:\japps\pentaho\kettle\samples
    // Extension : .ktr
    //
    // Output Example 1 (no file system prefix, no path used)
    // Marc_Sample_Transformation.ktr
    // Output Example 2 (file system prefix: ${KETTLE_FILE_BASE}!, no path used)
    // ${KETTLE_FILE_BASE}!Marc_Sample_Transformation.ktr
    // Output Example 3 (file system prefix: ${KETTLE_FILE_BASE}!, path is used)
    // ${KETTLE_FILE_BASE}!japps/pentaho/kettle/samples/Marc_Sample_Transformation.ktr
    //
    //
    assert prefix != null;
    assert extension != null;

    String lookup = ( originalFilePath != null ? originalFilePath : "" ) + "/" + prefix + "." + extension;
    String rtn = namedResources.get( lookup );
    if ( rtn == null ) {
      // Never generated a name for this... Generate a new file name
      String fixedPath = null;
      if ( useOriginalPathInTargetName ) {
        fixedPath = fixPath( originalFilePath );
      }

      rtn =
        ( fileSystemPrefix != null ? fileSystemPrefix : "" )
          + ( fixedPath != null ? fixedPath + ( fixedPath.endsWith( "/" ) ? "" : "/" ) : "" )
          + fixFileName( prefix, extension );

      String ext = ( extension.charAt( 0 ) == '.' ? extension : "." + extension );

      String uniqueId = this.getFileNameUniqueIdentifier( rtn, extension );
      rtn += ( uniqueId != null ? "_" + uniqueId : "" );
      rtn += ext;

      namedResources.put( lookup, rtn ); // Keep track of already generated object names...
    }
    return rtn;
  }

  private String handleScript( String prefix, String originalFilePath, String extension ) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * We have a data file and we need to create a reference to this (relative/absolute) path name. The cleanest way to do
   * this is by calculating the absolute filename. Then we put the path to the file in a parameter that we remember.
   *
   * FILE_LOCATION_01, FILE_LOCATION_02, etc.
   *
   * We keep a unique list of parameters this way.
   *
   * @param prefix
   *          the name of the file (foo.csv)
   * @param originalFilePath
   *          directory in which the file lives (file://path/to/foo/bar/)
   * @param extension
   *          ignored (null)
   * @return the new filename including the created parameter...
   */
  private String handleDataFile( String prefix, String originalFilePath, String extension ) {

    // See if we find the path in the directory map that we have...
    //
    String parameter = directoryMap.get( originalFilePath );
    if ( parameter == null ) {

      parameter = createNewParameterName( originalFilePath );

      // Add it to the map...
      //
      directoryMap.put( originalFilePath, parameter );
    }

    return "${" + parameter + "}/" + prefix;
  }

  /**
   * Create a parameter name from an path TODO: actually use the original path
   *
   * @param originalFilePath
   * @return the new parameter name
   */
  private String createNewParameterName( String originalFilePath ) {
    parameterNr++;
    return "DATA_PATH_" + parameterNr;
  }

  protected String getFileNameUniqueIdentifier( String filename, String extension ) {
    //
    // This implementation assumes that the file name will be sufficiently
    // unique.
    //
    return null;
  }

  protected String fixPath( String originalPathName ) {
    // This should convert all of the following into foo\bar or foo/bar
    // D:\foo\bar
    // /foo/bar
    // \\server\share\foo\bar
    //
    String rtn = originalPathName.substring( getPrefixLength( originalPathName ) );
    // Now, rtn has either either foo/bar or foo\bar. So, convert the \ to /
    // and return.
    return rtn.replace( '\\', '/' );
  }

  private int getPrefixLength( String fileName ) {
    if ( fileName.charAt( 1 ) == ':' ) { // Handle D:\foo\bar\
      return 3;
    } else if ( fileName.charAt( 0 ) == '\\' && fileName.charAt( 1 ) == '\\' ) { // Handle \\server\sharename\foo\bar
      int start = 0;
      int slashesFound = 0;
      for ( int i = 2; i < fileName.length(); i++ ) {
        if ( fileName.charAt( i ) == '\\' ) {
          slashesFound++;
        }
        if ( slashesFound == 2 ) {
          start = i + 1;
          break;
        }
      }
      return start;
    } else if ( fileName.charAt( 0 ) == '/' ) { // handle /foo/bar
      return 1;
    }
    return 0;
  }

  /**
   * This method turns a friendly name which could contain all manner of invalid characters for a file name into one
   * that's more conducive to being a file name.
   *
   * @param name
   *          The name to fix up.
   * @param extension
   *          the requested extension to see if we don't end up with 2 extensions (export of XML to XML)
   * @return
   */
  protected String fixFileName( String name, String extension ) {
    int length = name.length();
    if ( name.endsWith( "." + extension ) ) {
      length -= 1 + extension.length();
    } else if ( name.endsWith( extension ) ) {
      length -= extension.length();
    }

    StringBuilder buff = new StringBuilder( length );
    char ch;
    for ( int i = 0; i < length; i++ ) {
      ch = name.charAt( i );
      if ( ch == ' ' ) {
        buff.append( ch );
      } else {
        if ( ( ch <= '/' )
          || ( ch >= ':' && ch <= '?' ) || ( ch >= '[' && ch <= '`' ) || ( ch >= '{' && ch <= '~' ) ) {
          buff.append( '_' );
        } else {
          buff.append( ch );
        }
      }
    }
    return buff.toString();
  }

  public String getFileSystemPrefix() {
    return this.fileSystemPrefix;
  }

  public void setFileSystemPrefix( String value ) {
    this.fileSystemPrefix = value;
  }

  public boolean getUseOriginalPathInTargetName() {
    return this.useOriginalPathInTargetName;
  }

  public void setUseOriginalPathInTargetName( boolean value ) {
    this.useOriginalPathInTargetName = value;
  }

  /**
   * @return the directoryMap
   */
  public Map<String, String> getDirectoryMap() {
    return directoryMap;
  }

  /**
   * @param directoryMap
   *          the directoryMap to set
   */
  public void setDirectoryMap( Map<String, String> directoryMap ) {
    this.directoryMap = directoryMap;
  }
}
