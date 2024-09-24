/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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
