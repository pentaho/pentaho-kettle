/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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
