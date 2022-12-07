/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2022 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.step;

import org.pentaho.di.core.database.CachedManagedDataSourceInterface;
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
  protected boolean connectToDatabaseOnInit;

  public BaseDatabaseStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans ) {
    this( stepMeta, stepDataInterface, copyNr, transMeta, trans, false );
  }

  public BaseDatabaseStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans, boolean connectToDatabaseOnInit ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
    this.connectToDatabaseOnInit = connectToDatabaseOnInit;
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    return super.init( smi, sdi );
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    if ( sdi instanceof BaseDatabaseStepData ) {
      BaseDatabaseStepData data = (BaseDatabaseStepData) sdi;
      if ( data.db != null ) {
        data.db.disconnect();
      }
    }
    super.dispose( smi, sdi );
  }

  public boolean beforeStartProcessing( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    if ( !( sdi instanceof BaseDatabaseStepData ) ) {
      return false;
    }
    BaseDatabaseStepData data = (BaseDatabaseStepData) sdi;
    if ( data.db.getConnection() != null ) {
      return true;
    }
    connectToDatabase( data );
    return true;
  }

  public boolean connectToDatabaseOrAssignDataSource( BaseDatabaseStepMeta meta, BaseDatabaseStepData data ) throws KettleDatabaseException {
    data.db = new Database( this, meta.getDatabaseMeta() );
    data.db.shareVariablesWith( this );
    try {
      if ( connectToDatabaseOnInit() ) {
        return connectToDatabase(  data );
      } else {
        return getConnDataSource( data );
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
      if ( data.db.commitSizeWasSet() ) {
        data.db.setAutoCommit();
      }

      return true;
    } catch ( KettleDatabaseException e ) {
      throw e;
    }
  }

  private boolean getConnDataSource( BaseDatabaseStepData data ) throws KettleDatabaseException {
    data.db.getConnectionDataSource( getPartitionID() );
    if ( data.db.getDataSource() instanceof CachedManagedDataSourceInterface ) {
      data.db.setOwnerName( getTrans().getContainerObjectId() + "-" + getObjectId().getId() );
      ( (CachedManagedDataSourceInterface) data.db.getDataSource() ).setInUseBy( data.db.getOwnerName() );
    }
    return true;
  }

  /**
   * Returns the specific step class needed for logging
   * @return the specific step class needed for logging
   */
  protected abstract Class<?> getPKG();

  protected boolean connectToDatabaseOnInit() {
    return connectToDatabaseOnInit;
  }
}
