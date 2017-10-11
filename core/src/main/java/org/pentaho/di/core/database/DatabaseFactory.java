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

package org.pentaho.di.core.database;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.SimpleLoggingObject;
import org.pentaho.di.i18n.BaseMessages;

/**
 *
 * @author matt
 *
 */
public class DatabaseFactory implements DatabaseFactoryInterface {

  private static Class<?> PKG = Database.class; // for i18n purposes, needed by Translator2!!

  public static final LoggingObjectInterface loggingObject = new SimpleLoggingObject(
    "Database factory", LoggingObjectType.GENERAL, null );

  public DatabaseFactory() {
  }

  @Override
  public String getConnectionTestReport( DatabaseMeta databaseMeta ) throws KettleDatabaseException {
    if ( databaseMeta.getAccessType() != DatabaseMeta.TYPE_ACCESS_PLUGIN ) {

      StringBuilder report = new StringBuilder();

      Database db = new Database( loggingObject, databaseMeta );
      if ( databaseMeta.isPartitioned() ) {
        PartitionDatabaseMeta[] partitioningInformation = databaseMeta.getPartitioningInformation();
        for ( int i = 0; i < partitioningInformation.length; i++ ) {
          try {
            db.connect( partitioningInformation[i].getPartitionId() );
            report.append( BaseMessages.getString( PKG, "DatabaseMeta.report.ConnectionWithPartOk", databaseMeta
              .getName(), partitioningInformation[i].getPartitionId() )
              + Const.CR );
          } catch ( KettleException e ) {
            report.append( BaseMessages.getString(
              PKG, "DatabaseMeta.report.ConnectionWithPartError", databaseMeta.getName(),
              partitioningInformation[i].getPartitionId(), e.toString() )
              + Const.CR );
            report.append( Const.getStackTracker( e ) + Const.CR );
          } finally {
            db.disconnect();
          }

          appendConnectionInfo( report, db.environmentSubstitute( partitioningInformation[i].getHostname() ), db
            .environmentSubstitute( partitioningInformation[i].getPort() ), db
            .environmentSubstitute( partitioningInformation[i].getDatabaseName() ) );
          report.append( Const.CR );
        }
      } else {
        try {
          db.connect();
          report.append( BaseMessages.getString( PKG, "DatabaseMeta.report.ConnectionOk", databaseMeta.getName() )
            + Const.CR );
        } catch ( KettleException e ) {
          report.append( BaseMessages.getString( PKG, "DatabaseMeta.report.ConnectionError", databaseMeta
            .getName() )
            + e.toString() + Const.CR );
          report.append( Const.getStackTracker( e ) + Const.CR );
        } finally {
          db.disconnect();
        }
        if ( databaseMeta.getAccessType() == DatabaseMeta.TYPE_ACCESS_JNDI ) {
          appendJndiConnectionInfo( report, db.environmentSubstitute( databaseMeta.getDatabaseName() ) );
        } else {
          appendConnectionInfo( report, db, databaseMeta );
        }
        report.append( Const.CR );
      }
      return report.toString();
    } else {
      return BaseMessages.getString( PKG, "BaseDatabaseMeta.TestConnectionReportNotImplemented.Message" );
    }

  }

  private StringBuilder appendJndiConnectionInfo( StringBuilder report, String jndiName ) {
    report.append( BaseMessages.getString( PKG, "DatabaseMeta.report.JndiName" ) ).append( jndiName ).append(
      Const.CR );
    return report;
  }

  private StringBuilder appendConnectionInfo( StringBuilder report, Database db, DatabaseMeta databaseMeta ) {

    // Check to see if the interface is of a type GenericDatabaseMeta, since it does not have hostname and port fields
    if ( databaseMeta.getDatabaseInterface() instanceof GenericDatabaseMeta ) {
      String customUrl = databaseMeta.getAttributes().getProperty( GenericDatabaseMeta.ATRRIBUTE_CUSTOM_URL );
      String customDriverClass =
        databaseMeta.getAttributes().getProperty( GenericDatabaseMeta.ATRRIBUTE_CUSTOM_DRIVER_CLASS );

      return report.append( BaseMessages.getString( PKG, "GenericDatabaseMeta.report.customUrl" ) ).append(
        db.environmentSubstitute( customUrl ) ).append( Const.CR ).append(
        BaseMessages.getString( PKG, "GenericDatabaseMeta.report.customDriverClass" ) ).append(
        db.environmentSubstitute( customDriverClass ) ).append( Const.CR );
    }

    return appendConnectionInfo( report, db.environmentSubstitute( databaseMeta.getHostname() ), db
      .environmentSubstitute( databaseMeta.getDatabasePortNumberString() ), db
      .environmentSubstitute( databaseMeta.getDatabaseName() ) );
  }

  //CHECKSTYLE:LineLength:OFF
  private StringBuilder appendConnectionInfo( StringBuilder report, String hostName, String portNumber, String dbName ) {
    report.append( BaseMessages.getString( PKG, "DatabaseMeta.report.Hostname" ) ).append( hostName ).append( Const.CR );
    report.append( BaseMessages.getString( PKG, "DatabaseMeta.report.Port" ) ).append( portNumber ).append( Const.CR );
    report.append( BaseMessages.getString( PKG, "DatabaseMeta.report.DatabaseName" ) ).append( dbName ).append( Const.CR );
    return report;
  }
}
