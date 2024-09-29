/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2020 by Hitachi Vantara : http://www.pentaho.com
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

import java.util.Enumeration;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

import org.apache.tomcat.jdbc.pool.DataSourceFactory;
import org.pentaho.support.encryption.Encr;
import org.pentaho.support.encryption.PasswordEncoderException;
import org.pentaho.support.utils.XmlParseException;

public class DecryptingDataSourceFactory extends DataSourceFactory {

  @Override
  public Object getObjectInstance( final Object obj, final Name name, final Context nameCtx,
                                   final Hashtable environment ) throws Exception {
    if ( obj instanceof Reference ) {
      setPassword( (Reference) obj );
    }
    return super.getObjectInstance( obj, name, nameCtx, environment );
  }

  private void setPassword( final Reference ref ) throws PasswordEncoderException, XmlParseException {
    findDecryptAndReplace( "password", ref );
  }

  private void findDecryptAndReplace( final String refType, final Reference ref )
    throws PasswordEncoderException, XmlParseException {
    final int idx = find( refType, ref );
    final String decrypted = decrypt( idx, ref );
    replace( idx, refType, decrypted, ref );
  }

  private void replace( final int idx, final String refType, final String newValue, final Reference ref ) {
    ref.remove( idx );
    ref.add( idx, new StringRefAddr( refType, newValue ) );
  }

  private String decrypt( final int idx, final Reference ref ) throws PasswordEncoderException, XmlParseException {
    return Encr.getInstance().decryptPasswordOptionallyEncrypted( ref.get( idx ).getContent().toString() );
  }

  private int find( final String addrType, final Reference ref ) {
    final Enumeration enu = ref.getAll();
    for ( int i = 0; enu.hasMoreElements(); i++ ) {
      final RefAddr addr = (RefAddr) enu.nextElement();
      if ( addr.getType().compareTo( addrType ) == 0 ) {
        return i;
      }
    }

    throw new IllegalArgumentException(
      "The '" + addrType + "' name/value pair was not found in the Reference object. The reference Object is"
        + " " + ref.toString() );
  }
}
