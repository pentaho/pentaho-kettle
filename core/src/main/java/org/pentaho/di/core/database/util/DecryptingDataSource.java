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


package org.pentaho.di.core.database.util;

import org.apache.commons.dbcp2.BasicDataSource;
import org.pentaho.support.encryption.Encr;
import org.pentaho.support.encryption.PasswordEncoderException;
import org.pentaho.support.utils.XmlParseException;

public class DecryptingDataSource extends BasicDataSource {

  @Override
  @SuppressWarnings( "squid:S00112" )
  public void setPassword( String password ) {
    try {
      super.setPassword( Encr.getInstance().decryptPasswordOptionallyEncrypted( password ) );
    } catch ( PasswordEncoderException | XmlParseException e ) {
      //Should only get here if configuration was setup incorrectly
      throw new RuntimeException( "Could not decrypt password", e );
    }
  }
}
