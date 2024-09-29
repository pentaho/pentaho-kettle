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


package org.pentaho.di.trans.steps.sapinput.sap;

import org.pentaho.di.core.database.DatabaseMeta;

public class SAPConnectionParamsHelper {

  public static SAPConnectionParams getFromDatabaseMeta( DatabaseMeta sapConnection ) {
    String name = sapConnection.getName();
    String host = sapConnection.environmentSubstitute( sapConnection.getHostname() );
    String sysnr =
      sapConnection.environmentSubstitute( sapConnection.getAttributes().getProperty( "SAPSystemNumber" ) );
    String client = sapConnection.environmentSubstitute( sapConnection.getAttributes().getProperty( "SAPClient" ) );
    String user = sapConnection.environmentSubstitute( sapConnection.getUsername() );
    String password = sapConnection.environmentSubstitute( sapConnection.getPassword() );
    String lang = "";
    return new SAPConnectionParams( name, host, sysnr, client, user, password, lang );
  }
}
