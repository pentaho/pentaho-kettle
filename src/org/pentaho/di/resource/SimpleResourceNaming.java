/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.resource;

import java.util.HashMap;
import java.util.Map;

public class SimpleResourceNaming implements ResourceNamingInterface {

  private final Map<String,String> namedResources = new HashMap<String, String>();
  
  /**
   * The fileSystemPrefix
   * would be appropriate for something like:
   *
   * zip://somefile.zip!
   * or
   * somefilesystem://export/folder/
   */
  private String fileSystemPrefix;

  private boolean useOriginalPathInTargetName;
  
  public SimpleResourceNaming() {
    super();
  }
  
  public SimpleResourceNaming(String fileSystemPrefix) {
    super();
    this.fileSystemPrefix = fileSystemPrefix;
  }
  
  public String nameResource(String prefix, String originalFilePath, String extension) {
    //
    // End result could look like any of the following:
    //
    // Inputs:
    //    Prefix       : Marc Sample Transformation
    //    Original Path: D:\japps\pentaho\kettle\samples
    //    Extension    : .ktr
    //
    // Output Example 1 (no file system prefix, no path used)
    //     Marc_Sample_Transformation.ktr
    // Output Example 2 (file system prefix: ${KETTLE_FILE_BASE}!, no path used)
    //     ${KETTLE_FILE_BASE}!Marc_Sample_Transformation.ktr
    // Output Example 3 (file system prefix: ${KETTLE_FILE_BASE}!, path is used)
    //     ${KETTLE_FILE_BASE}!japps/pentaho/kettle/samples/Marc_Sample_Transformation.ktr
    //
    //
    assert prefix != null;
    assert extension != null;
    String uniqueId = this.getFileNameUniqueIdentifier();
    String lookup = (originalFilePath != null ? originalFilePath : "") + "/" + prefix;  //$NON-NLS-1$ //$NON-NLS-2$
    String rtn = namedResources.get(lookup);
    if (rtn == null) {
      // Never generated a name for this... Generate a new file name
      String fixedPath = null;
      if (useOriginalPathInTargetName) {
        fixedPath = fixPath(originalFilePath);
      }
      rtn = (fileSystemPrefix != null ? fileSystemPrefix : "") + //$NON-NLS-1$ 
          (fixedPath != null ? fixedPath + (fixedPath.endsWith("/") ? "" : "/")  : "") + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
          fixFileName(prefix) + 
          (uniqueId != null ? "_" + uniqueId : "") +  //$NON-NLS-1$ //$NON-NLS-2$
          (extension.charAt(0) == '.' ? extension : "." + extension); //$NON-NLS-1$
      namedResources.put(lookup, rtn); // Keep track of already generated object names...
    }
    return rtn;
  }
  
  protected String getFileNameUniqueIdentifier() {
    //
    // This implementation assumes that the file name will be sufficiently unique.
    //
    return null;
  }
  
  protected String fixPath(String originalPathName) {
    // This should convert all of the following into foo\bar or foo/bar
    // D:\foo\bar
    // /foo/bar
    // \\server\share\foo\bar
    //
    String rtn = originalPathName.substring(getPrefixLength(originalPathName));
    // Now, rtn has either either foo/bar or foo\bar. So, convert the \ to / and return.
    return rtn.replace('\\', '/');
  }

  private int getPrefixLength(String fileName) {
    if (fileName.charAt(1) == ':') { // Handle D:\foo\bar\
      return 3;
    } else if (fileName.charAt(0) == '\\' && fileName.charAt(1) == '\\' ) { // Handle \\server\sharename\foo\bar
      int start = 0;
      int slashesFound=0;
      for (int i=2; i<fileName.length(); i++) {
        if (fileName.charAt(i) == '\\') {
          slashesFound++;
        }
        if (slashesFound == 2) {
          start = i+1;
          break;
        }
      }
      return start;
    } else if (fileName.charAt(0) == '/') { // handle /foo/bar
      return 1;
    }
    return 0;
  }
  
  
  /**
   * This method turns a friendly name which could contain all manner of invalid
   * characters for a file name into one that's more conducive to being a
   * file name.
   * @param name The name to fix up.
   * @return
   */
  protected String fixFileName(String name) {
    StringBuffer buff = new StringBuffer(name.length());
    char ch;
    for (int i=0; i<name.length(); i++) {
      ch = name.charAt(i);
      if ( (ch <='/') || (ch>=':' && ch<='?') || (ch>='[' && ch<='`') || (ch>='{' && ch<='~') ) {
        buff.append('_');
      } else {
        buff.append(ch);
      }
    }
    return buff.toString();
  }

  public String getFileSystemPrefix() {
    return this.fileSystemPrefix;
  }
  
  public void setFileSystemPrefix(String value) {
    this.fileSystemPrefix = value;
  }
  
  public boolean getUseOriginalPathInTargetName() {
    return this.useOriginalPathInTargetName;
  }
  
  public void setUseOriginalPathInTargetName(boolean value) {
    this.useOriginalPathInTargetName = value;
  }
}
