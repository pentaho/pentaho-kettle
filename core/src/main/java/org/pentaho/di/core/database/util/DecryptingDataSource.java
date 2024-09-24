/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2023 by Hitachi Vantara : http://www.pentaho.com
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
