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
package org.pentaho.di.core.vfs.configuration;

import java.io.IOException;

import org.apache.commons.vfs.FileSystemConfigBuilder;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.util.DelegatingFileSystemOptionsBuilder;
import org.mortbay.log.Log;

/**
 * A generic FileSystemConfigBuilder that inserts parameters and values as literally specified.
 * 
 * Note: ALL parameters are case sensitive!
 * Please see individual FileSystemConfigBuilder for list of available parameters.
 * Please also see Kettle FileSystemConfigBuilder overrides for additional parameters.
 * 
 * @see KettleSftpFileSystemConfigBuilder
 * 
 * @author cboyden
 */
public class KettleGenericFileSystemConfigBuilder extends FileSystemConfigBuilder implements IKettleFileSystemConfigBuilder {

  private final static KettleGenericFileSystemConfigBuilder builder = new KettleGenericFileSystemConfigBuilder();

  public String parseParameterName(String parameter, String scheme) {
    String result = null;
    
    // Frame the parameter name
    int begin = parameter.indexOf(".", parameter.indexOf(".") + 1) + 1; // Get the index of the second "." (vfs.scheme.parameter)  //$NON-NLS-1$//$NON-NLS-2$
    int end = -1;
    
    end = parameter.indexOf('.', begin);
    
    if(end < 0) {
      end = parameter.length();
    }
    
    if(end > begin) {
      result = parameter.substring(begin, end);
    }
    
    return result;
  }
  
  public static KettleGenericFileSystemConfigBuilder getInstance() {
    return builder;
  }
  
  /**
   * Extract the scheme from a Kettle VFS configuration paramter (vfs.scheme.parameter)
   * @param fullParameterName A VFS configuration parameter in the form of 'vfs.scheme.parameter'
   */
  public static String extractScheme(String fullParameterName) throws IllegalArgumentException {
    String result = null;
    
    // Verify that this is a Kettle VFS configuration parameter
    if( (fullParameterName != null) && (fullParameterName.length() > 4) && (fullParameterName.startsWith("vfs.")) ) { //$NON-NLS-1$
      int schemeEnd = fullParameterName.indexOf(".", 4); //$NON-NLS-1$
      if(schemeEnd > 4) {
        result = fullParameterName.substring(4, schemeEnd);
      } else {
        throw new IllegalArgumentException("The configuration parameter does not match a valid scheme: " + fullParameterName); //$NON-NLS-1$
      }
    } else {
      throw new IllegalArgumentException("The configuration parameter does not match a valid scheme: " + fullParameterName); //$NON-NLS-1$
    }
    
    return result;
  }

  protected KettleGenericFileSystemConfigBuilder() {
    super();
  }

  @Override
  protected Class<?> getConfigClass() {
    return KettleGenericFileSystemConfigBuilder.class;
  }
  
  public void setParameter(FileSystemOptions opts, String name, String value, String fullParameterName, String vfsUrl) throws IOException {
    // Use the DelgatingFileSystemOptionsBuilder to insert generic parameters
    // This must be done to assure the correct VFS FileSystem drivers will process the parameters
    String scheme = extractScheme(fullParameterName);
    try {
      DelegatingFileSystemOptionsBuilder delegateFSOptionsBuilder = new DelegatingFileSystemOptionsBuilder(VFS.getManager());
      if(scheme != null) {
        delegateFSOptionsBuilder.setConfigString(opts, scheme, name, value);
      } else {
        Log.warn("Cannot process VFS parameters if no scheme is specified: " + vfsUrl); //$NON-NLS-1$
      }
    } catch (FileSystemException e) {
      if(e.getCode().equalsIgnoreCase("vfs.provider/config-key-invalid.error")) { //$NON-NLS-1$
        // This key is not supported by the default scheme config builder. This may be a custom key of another config builder
        Log.warn("The configuration parameter [" + name + "] is not supported by the default configuration builder for scheme: " + scheme);  //$NON-NLS-1$//$NON-NLS-2$
      } else {
        // An unexpected error has occurred loading in parameters
        throw new IOException(e.getLocalizedMessage());
      }
    }
  }
}
