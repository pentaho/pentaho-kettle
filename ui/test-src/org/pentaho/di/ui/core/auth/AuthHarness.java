/*
 * !
 *  * This program is free software; you can redistribute it and/or modify it under the
 *  * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 *  * Foundation.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public License along with this
 *  * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 *  * or from the Free Software Foundation, Inc.,
 *  * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *  *
 *  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  * See the GNU Lesser General Public License for more details.
 *  *
 *  * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 *
 */

package org.pentaho.di.ui.core.auth;

import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.ui.core.auth.controller.AuthProviderController;
import org.pentaho.di.ui.core.auth.model.BasicAuthProvider;
import org.pentaho.di.ui.core.auth.model.KerberosAuthProvider;
import org.pentaho.di.ui.core.auth.model.NamedProvider;
import org.pentaho.di.ui.core.auth.model.NoAuthAuthProvider;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.swt.SwtBindingFactory;
import org.pentaho.ui.xul.swt.SwtXulLoader;
import org.pentaho.ui.xul.swt.SwtXulRunner;

import java.util.ArrayList;
import java.util.List;

public class AuthHarness
{
  public static void main(String[] args)
  {
    try
    {

      KettleEnvironment.init();

      AuthProviderDialog dialog = new AuthProviderDialog( null );
      dialog.addProviders( getProviders(dialog.getBindingFactory()) );
      dialog.show();
      
    }
    catch (Exception e) {
      System.out.println(e.getMessage());
      e.printStackTrace(System.out);
    }
  }

  public static List<NamedProvider> getProviders(BindingFactory bf) {

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
