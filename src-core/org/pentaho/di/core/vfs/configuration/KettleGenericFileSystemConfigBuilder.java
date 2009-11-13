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

import org.apache.commons.vfs.FileSystemConfigBuilder;
import org.apache.commons.vfs.FileSystemOptions;

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

  public static KettleGenericFileSystemConfigBuilder getInstance() {
    return builder;
  }

  protected KettleGenericFileSystemConfigBuilder() {
    super();
  }

  @Override
  protected Class<?> getConfigClass() {
    return KettleGenericFileSystemConfigBuilder.class;
  }
  
  public void setParameter(FileSystemOptions opts, String name, String value, String fullParameterName, String vfsUrl) {
    this.setParameter(opts, name, (Object)value, fullParameterName, vfsUrl);
  }

  /**
   * Publicly expose a generic way to set parameters
   */
  public void setParameter(FileSystemOptions opts, String name, Object value, String fullParameterName, String vfsUrl) {
    this.setParam(opts, name, value);
  }

  /**
   * Publicly expose a generic way to get parameters
   */
  public Object getParameter(FileSystemOptions opts, String name) {
    return this.getParam(opts, name);
  }

  /**
   * Publicly expose a generic way to check for parameters
   */
  public boolean hasParameter(FileSystemOptions opts, String name) {
    return this.hasParameter(opts, name);
  }
}
