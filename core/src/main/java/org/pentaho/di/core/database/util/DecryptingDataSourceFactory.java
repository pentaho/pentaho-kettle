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
