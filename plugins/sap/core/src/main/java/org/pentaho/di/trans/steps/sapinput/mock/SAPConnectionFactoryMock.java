/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.sapinput.mock;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseFactoryInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.DatabaseTestResults;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.trans.steps.sapinput.sap.SAPConnection;
import org.pentaho.di.trans.steps.sapinput.sap.SAPException;

public class SAPConnectionFactoryMock implements DatabaseFactoryInterface {

  public static SAPConnection create() {
    return new SAPConnectionMock();
  }

  /**
   * The SAP connection to test, links to the TEST button in the database dialog.
   */
  public String getConnectionTestReport( DatabaseMeta databaseMeta ) throws KettleDatabaseException {

    StringBuilder report = new StringBuilder();

    SAPConnection sc = null;

    try {

      sc = create();

      sc.open( databaseMeta );

      // If the connection was successful
      //
      report.append( "Connecting to SAP ERP server [" ).append( databaseMeta.getName() ).append(
        "] succeeded without a problem." ).append( Const.CR );

    } catch ( SAPException e ) {
      report.append( "Unable to connect to the SAP ERP server: " ).append( e.getMessage() ).append( Const.CR );
      report.append( Const.getStackTracker( e ) );
    } finally {
      sc.close();
    }

    return report.toString();
  }

  public DatabaseTestResults getConnectionTestResults( DatabaseMeta databaseMeta ) {
    return null;
  }
}
