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


package org.pentaho.di.ui.core.auth;

import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.ui.core.auth.model.BasicAuthProvider;
import org.pentaho.di.ui.core.auth.model.KerberosAuthProvider;
import org.pentaho.di.ui.core.auth.model.NamedProvider;
import org.pentaho.di.ui.core.auth.model.NoAuthAuthProvider;
import org.pentaho.ui.xul.binding.BindingFactory;

import java.util.ArrayList;
import java.util.List;

public class AuthHarness {
  public static void main( String[] args ) {
    try {

      KettleEnvironment.init();

      AuthProviderDialog dialog = new AuthProviderDialog( null );
      dialog.addProviders( getProviders( dialog.getBindingFactory() ) );
      dialog.show();

    } catch ( Exception e ) {
      System.out.println( e.getMessage() );
      e.printStackTrace( System.out );
    }
  }

  public static List<NamedProvider> getProviders( BindingFactory bf ) {

    List<NamedProvider> providers = new ArrayList<NamedProvider>();

    KerberosAuthProvider kProvider = new KerberosAuthProvider( bf );
    kProvider.setPrincipal( "kerbname1" );
    kProvider.setPassword( "password" );

    providers.add( new NamedProvider( "kerberos1", kProvider ) );

    kProvider = new KerberosAuthProvider( bf );
    kProvider.setUseKeytab( true );
    kProvider.setKeytabFile( "/Users/gmoran/file.tmp" );

    providers.add( new NamedProvider( "kerberos2", kProvider ) );

    BasicAuthProvider bProvider = new BasicAuthProvider( bf );
    bProvider.setPrincipal( "basicname1" );
    bProvider.setPassword( "password" );

    providers.add( new NamedProvider( "basic1", bProvider ) );

    bProvider = new BasicAuthProvider( bf );
    bProvider.setPrincipal( "basicname2" );
    bProvider.setPassword( "password" );

    providers.add( new NamedProvider( "basic2", bProvider ) );

    NoAuthAuthProvider naProvider = new NoAuthAuthProvider( bf );

    providers.add( new NamedProvider( "noAuth1", naProvider ) );

    naProvider = new NoAuthAuthProvider( bf );

    providers.add( new NamedProvider( "noAuth2", naProvider ) );

    bProvider = new BasicAuthProvider( bf );
    bProvider.setPrincipal( "basicname3" );
    bProvider.setPassword( "password" );

    providers.add( new NamedProvider( "basic3", bProvider ) );

    kProvider = new KerberosAuthProvider( bf );
    kProvider.setPrincipal( "kerberos3" );
    kProvider.setPassword( "password" );

    providers.add( new NamedProvider( "kerberos3", kProvider ) );

    return providers;
  }
}
