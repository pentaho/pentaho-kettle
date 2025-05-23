/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.trans.step;

import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;

/**
 * Base step extension that is responsible for Database connection and datasource management
 * for steps that require it.
 * handles common functionalities for steps that require Database connections.
 */
public abstract class BaseDatabaseStep extends BaseStep implements StepInterface {
  /**
   * Whether the step should connect to the database on init or just retrieve the necessary datasource,
   * to avoid deadlocking step initialization when, for instance, using a pooled connection that does not
   * have enough available connections.
   *
   * False by default
   */
  private boolean connectToDatabaseOnInit;

  public BaseDatabaseStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans ) {
    this( stepMeta, stepDataInterface, copyNr, transMeta, trans, false );
  }

  public BaseDatabaseStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans, boolean connectToDatabaseOnInit ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
    this.connectToDatabaseOnInit = connectToDatabaseOnInit;
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    if ( !super.init( smi, sdi ) ) {
      return false;
    }
    if ( !hasValidTypes( smi, sdi ) ) {
      return false;
    }
    BaseDatabaseStepMeta meta = (BaseDatabaseStepMeta) smi;
    BaseDatabaseStepData data = (BaseDatabaseStepData) sdi;
    if ( meta.getDatabaseMeta() == null ) {
      logError( BaseMessages.getString( getPKG(), "BaseDatabaseStep.Init.ConnectionMissing", getStepname() ) );
      return false;
    }

    try {
      connectToDatabaseOrInitDataSource( meta, data );
      return true;
    } catch ( KettleDatabaseException e ) {
      logError( BaseMessages.getString( getPKG(), "BaseDatabaseStep.Log.ErrorOccurred", e.getMessage() ) );
      setErrors( 1 );
      stopAll();
    }
    return false;
  }

  private boolean hasValidTypes( StepMetaInterface smi, StepDataInterface sdi ) {
    boolean isValid = true;
    if ( !( smi instanceof BaseDatabaseStepMeta ) ) {
      log.logError( "StepMetaInterface is of type [" + sdi.getClass() + "], expected type BaseDatabaseStepMeta. Unable to initialize step" );
      isValid = false;
    }
    if ( !( sdi instanceof BaseDatabaseStepData ) ) {
      log.logError( "StepDataInterface is of type [" + sdi.getClass() + "], expected type BaseDatabaseStepData. Unable to initialize step" );
      isValid = false;
    }
    return isValid;
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    if ( sdi instanceof BaseDatabaseStepData ) {
      BaseDatabaseStepData data = (BaseDatabaseStepData) sdi;
      if ( data.db != null ) {
        data.db.close();
      }
    } else {
      log.logBasic( "StepDataInterface is of type [" + sdi.getClass() + "], expected type BaseDatabaseStepData. Unable to disconnect from possible DB in use" );
    }

    super.dispose( smi, sdi );
  }

  public boolean beforeStartProcessing( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    if ( !( sdi instanceof BaseDatabaseStepData ) ) {
      log.logBasic( "StepDataInterface is of type [" + sdi.getClass() + "], expected type BaseDatabaseStepData. Unable to connect to DB" );
      return false;
    }
    BaseDatabaseStepData data = (BaseDatabaseStepData) sdi;
    // Connection might have been made on step init, if connectToDatabaseOnInit was set to true
    if ( data.db.getConnection() != null ) {
      return true;
    }
    connectToDatabase( data );
    return true;
  }

  public boolean connectToDatabaseOrInitDataSource( BaseDatabaseStepMeta meta, BaseDatabaseStepData data ) throws KettleDatabaseException {
    data.db = new Database( this, meta.getDatabaseMeta() );
    data.db.shareVariablesWith( this );
    try {
      if ( isConnectToDatabaseOnInit() ) {
        return connectToDatabase( data );
      } else {
        return initializeConnectionDataSource( data );
      }
    } catch ( KettleDatabaseException e ) {
      throw e;
    }
  }

  protected boolean connectToDatabase( BaseDatabaseStepData data ) throws KettleDatabaseException {
    try {
      if ( getTransMeta().isUsingUniqueConnections() ) {
        synchronized ( getTrans() ) {
          data.db.connect( getTrans().getTransactionId(), getPartitionID() );
        }
      } else {
        data.db.connect( getPartitionID() );
      }

      if ( log.isDetailed() ) {
        logDetailed( BaseMessages.getString( getPKG(), "TableInput.Log.ConnectedToDatabase" ) );
      }
      data.db.setAutoCommit();

      return true;
    } catch ( KettleDatabaseException e ) {
      throw e;
    }
  }

  private boolean initializeConnectionDataSource( BaseDatabaseStepData data ) throws KettleDatabaseException {
    data.db.initializeConnectionDataSource( getPartitionID() );
    data.db.setOwnerName( getStepExecutionId() );
    return true;
  }

  /**
   * Returns the specific step class needed for logging
   * @return the specific step class needed for logging
   */
  protected abstract Class<?> getPKG();

  protected boolean isConnectToDatabaseOnInit() {
    return connectToDatabaseOnInit;
  }

  public String getStepExecutionId() {
    return getTrans().getContainerObjectId() + "-" + getObjectName();
  }
}
