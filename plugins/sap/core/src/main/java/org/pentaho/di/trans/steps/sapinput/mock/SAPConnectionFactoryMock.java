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
