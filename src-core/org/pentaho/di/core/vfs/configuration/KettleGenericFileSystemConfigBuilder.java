/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.core.vfs.configuration;

import java.io.IOException;

import org.apache.commons.vfs.FileSystemConfigBuilder;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.util.DelegatingFileSystemOptionsBuilder;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.vfs.KettleVFS;

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
  private final static LogChannelInterface log = new LogChannel("cfgbuilder");
  
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
      DelegatingFileSystemOptionsBuilder delegateFSOptionsBuilder = new DelegatingFileSystemOptionsBuilder(KettleVFS.getInstance().getFileSystemManager());
      if(scheme != null) {
        delegateFSOptionsBuilder.setConfigString(opts, scheme, name, value);
      } else {
    	  log.logMinimal("Warning: Cannot process VFS parameters if no scheme is specified: " + vfsUrl); //$NON-NLS-1$
      }
    } catch (FileSystemException e) {
      if(e.getCode().equalsIgnoreCase("vfs.provider/config-key-invalid.error")) { //$NON-NLS-1$
        // This key is not supported by the default scheme config builder. This may be a custom key of another config builder
        log.logMinimal("Warning: The configuration parameter [" + name + "] is not supported by the default configuration builder for scheme: " + scheme);  //$NON-NLS-1$//$NON-NLS-2$
      } else {
        // An unexpected error has occurred loading in parameters
        throw new IOException(e.getLocalizedMessage());
      }
    }
  }
}
