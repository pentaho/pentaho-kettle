/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.s3common;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.i18n.BaseMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Class that handles operations dealing with kettle property file.
 */
public class S3KettleProperty {
  private static final Class<?> PKG = S3KettleProperty.class;
  private static final Logger logger = LoggerFactory.getLogger( S3KettleProperty.class );

  public static final String S3VFS_PART_SIZE = "s3.vfs.partSize";

  public String getPartSize() {
    return getProperty( S3VFS_PART_SIZE );
  }

  public String getProperty( String property ) {
    String filename =  Const.getKettlePropertiesFilename();
    Properties properties;
    String partSizeString = "";
    try {
      properties = EnvUtil.readProperties( filename );
      partSizeString = properties.getProperty( property );
    } catch ( KettleException ke ) {
      logger.error( BaseMessages.getString( PKG, "WARN.S3Common.PropertyNotFound",
        property, filename ) );
    }
    return partSizeString;
  }
}
