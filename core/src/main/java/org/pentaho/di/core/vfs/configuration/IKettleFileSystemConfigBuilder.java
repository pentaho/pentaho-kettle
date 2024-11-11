/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.core.vfs.configuration;

import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.variables.VariableSpace;

import java.io.IOException;

import org.apache.commons.vfs2.FileSystemOptions;

/**
 * @author cboyden
 */
public interface IKettleFileSystemConfigBuilder {

  /**
   * Extract the FileSystemOptions parameter name from a Kettle variable
   *
   * @param parameter
   * @return
   */
  public String parseParameterName( String parameter, String scheme );

  /**
   * Publicly expose a generic way to set parameters
   */
  public void setParameter( FileSystemOptions opts, String name, String value, String fullParameterName,
    String vfsUrl ) throws IOException;

  default void setParameter( FileSystemOptions opts, String name, VariableSpace value, String vfsUrl ) {
    //noop
  }

  default Object getVariableSpace( FileSystemOptions fileSystemOptions ) {
    return null;
  };

  default void setBowl( FileSystemOptions opts, Bowl bowl ) {
    // noop
  }

  default Bowl getBowl( FileSystemOptions opts ) {
    return DefaultBowl.getInstance();
  }
}

