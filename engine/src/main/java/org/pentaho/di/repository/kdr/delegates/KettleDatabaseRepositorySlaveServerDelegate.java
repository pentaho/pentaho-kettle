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

package org.pentaho.di.repository.kdr.delegates;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleObjectExistsException;
import org.pentaho.di.core.row.value.ValueMetaBoolean;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.kdr.KettleDatabaseRepository;

public class KettleDatabaseRepositorySlaveServerDelegate extends KettleDatabaseRepositoryBaseDelegate {

  private static Class<?> PKG = SlaveServer.class; // for i18n purposes, needed by Translator2!!

  public KettleDatabaseRepositorySlaveServerDelegate( KettleDatabaseRepository repository ) {
    super( repository );
  }

  public RowMetaAndData getSlaveServer( ObjectId id_slave ) throws KettleException {
    return repository.connectionDelegate.getOneRow(
      quoteTable( KettleDatabaseRepository.TABLE_R_SLAVE ),
      quote( KettleDatabaseRepository.FIELD_SLAVE_ID_SLAVE ), id_slave );
  }

  public synchronized ObjectId getSlaveID( String name ) throws KettleException {
    return repository.connectionDelegate.getIDWithValue(
      quoteTable( KettleDatabaseRepository.TABLE_R_SLAVE ),
      quote( KettleDatabaseRepository.FIELD_SLAVE_ID_SLAVE ),
      quote( KettleDatabaseRepository.FIELD_SLAVE_NAME ), name );
  }

  public void saveSlaveServer( SlaveServer slaveServer ) throws KettleException {
    saveSlaveServer( slaveServer, null, false );
  }

  public void saveSlaveServer( SlaveServer slaveServer, ObjectId id_transformation, boolean isUsedByTransformation ) throws KettleException {

    try {
      saveSlaveServer( slaveServer, id_transformation, isUsedByTransformation, false );
    } catch ( KettleObjectExistsException e ) {
      // This is an expected possibility here. Common objects are not going to overwrite database objects
      log.logBasic( e.getMessage() );
    }
  }

  public void saveSlaveServer( SlaveServer slaveServer, ObjectId id_transformation,
    boolean isUsedByTransformation, boolean overwrite ) throws KettleException {

    if ( slaveServer.getObjectId() == null ) {
      // See if the slave doesn't exist already (just for safety and PDI-5102
      //
      slaveServer.setObjectId( getSlaveID( slaveServer.getName() ) );
    }

    if ( slaveServer.getObjectId() == null ) {
      // New Slave Server
      slaveServer.setObjectId( insertSlave( slaveServer ) );
    } else {
      ObjectId existingSlaveId = slaveServer.getObjectId();
      if ( existingSlaveId != null
        && slaveServer.getObjectId() != null && !slaveServer.getObjectId().equals( existingSlaveId ) ) {
        // A slave with this name already exists
        if ( overwrite ) {
          // Proceed with save, removing the original version from the repository first
          repository.deleteSlave( existingSlaveId );
          updateSlave( slaveServer );
        } else {
          throw new KettleObjectExistsException( "Failed to save object to repository. Object ["
            + slaveServer.getName() + "] already exists." );
        }
      } else {
        // There are no naming collisions (either it is the same object or the name is unique)
        updateSlave( slaveServer );
      }
    }

    // Save the trans-slave relationship too.
    if ( id_transformation != null && isUsedByTransformation ) {
      repository.insertTransformationSlave( id_transformation, slaveServer.getObjectId() );
    }
  }

  public SlaveServer loadSlaveServer( ObjectId id_slave_server ) throws KettleException {
    SlaveServer slaveServer = new SlaveServer();

    slaveServer.setObjectId( id_slave_server );

    RowMetaAndData row = getSlaveServer( id_slave_server );
    if ( row == null ) {
      throw new KettleDatabaseException( BaseMessages.getString(
        PKG, "SlaveServer.SlaveCouldNotBeFound", id_slave_server.toString() ) );
    }

    slaveServer.setName( row.getString( KettleDatabaseRepository.FIELD_SLAVE_NAME, null ) );
    slaveServer.setHostname( row.getString( KettleDatabaseRepository.FIELD_SLAVE_HOST_NAME, null ) );
    slaveServer.setPort( row.getString( KettleDatabaseRepository.FIELD_SLAVE_PORT, null ) );
    slaveServer.setWebAppName( row.getString( KettleDatabaseRepository.FIELD_SLAVE_WEB_APP_NAME, null ) );
    slaveServer.setUsername( row.getString( KettleDatabaseRepository.FIELD_SLAVE_USERNAME, null ) );
    slaveServer.setPassword( Encr.decryptPasswordOptionallyEncrypted( row.getString(
      KettleDatabaseRepository.FIELD_SLAVE_PASSWORD, null ) ) );
    slaveServer.setProxyHostname( row.getString( KettleDatabaseRepository.FIELD_SLAVE_PROXY_HOST_NAME, null ) );
    slaveServer.setProxyPort( row.getString( KettleDatabaseRepository.FIELD_SLAVE_PROXY_PORT, null ) );
    slaveServer.setNonProxyHosts( row.getString( KettleDatabaseRepository.FIELD_SLAVE_NON_PROXY_HOSTS, null ) );
    slaveServer.setMaster( row.getBoolean( KettleDatabaseRepository.FIELD_SLAVE_MASTER, false ) );

    return slaveServer;
  }

  public synchronized ObjectId insertSlave( SlaveServer slaveServer ) throws KettleException {
    if ( getSlaveID( slaveServer.getName() ) != null ) {
      // This slave server name is already in use. Throw an exception.
      throw new KettleObjectExistsException( "Failed to create object in repository. Object ["
        + slaveServer.getName() + "] already exists." );
    }

    ObjectId id = repository.connectionDelegate.getNextSlaveServerID();

    RowMetaAndData table = new RowMetaAndData();

    table.addValue(
      new ValueMetaInteger( KettleDatabaseRepository.FIELD_SLAVE_ID_SLAVE ), id );
    table.addValue(
      new ValueMetaString( KettleDatabaseRepository.FIELD_SLAVE_NAME ), slaveServer
        .getName() );
    table.addValue(
      new ValueMetaString( KettleDatabaseRepository.FIELD_SLAVE_HOST_NAME ),
      slaveServer.getHostname() );
    table.addValue(
      new ValueMetaString( KettleDatabaseRepository.FIELD_SLAVE_PORT ), slaveServer
        .getPort() );
    table.addValue( new ValueMetaString(
      KettleDatabaseRepository.FIELD_SLAVE_WEB_APP_NAME ), slaveServer
      .getWebAppName() );
    table.addValue(
      new ValueMetaString( KettleDatabaseRepository.FIELD_SLAVE_USERNAME ),
      slaveServer.getUsername() );
    table.addValue(
      new ValueMetaString( KettleDatabaseRepository.FIELD_SLAVE_PASSWORD ), Encr
        .encryptPasswordIfNotUsingVariables( slaveServer.getPassword() ) );
    table.addValue( new ValueMetaString(
      KettleDatabaseRepository.FIELD_SLAVE_PROXY_HOST_NAME ), slaveServer
      .getProxyHostname() );
    table.addValue(
      new ValueMetaString( KettleDatabaseRepository.FIELD_SLAVE_PROXY_PORT ),
      slaveServer.getProxyPort() );
    table.addValue( new ValueMetaString(
      KettleDatabaseRepository.FIELD_SLAVE_NON_PROXY_HOSTS ), slaveServer
      .getNonProxyHosts() );
    table.addValue(
      new ValueMetaBoolean( KettleDatabaseRepository.FIELD_SLAVE_MASTER ), Boolean
        .valueOf( slaveServer.isMaster() ) );

    repository.connectionDelegate.getDatabase().prepareInsert(
      table.getRowMeta(), KettleDatabaseRepository.TABLE_R_SLAVE );
    repository.connectionDelegate.getDatabase().setValuesInsert( table );
    repository.connectionDelegate.getDatabase().insertRow();
    repository.connectionDelegate.getDatabase().closeInsert();

    return id;
  }

  public synchronized void updateSlave( SlaveServer slaveServer ) throws KettleException {
    RowMetaAndData table = new RowMetaAndData();
    table.addValue(
      new ValueMetaString( KettleDatabaseRepository.FIELD_SLAVE_NAME ), slaveServer
        .getName() );
    table.addValue(
      new ValueMetaString( KettleDatabaseRepository.FIELD_SLAVE_HOST_NAME ),
      slaveServer.getHostname() );
    table.addValue(
      new ValueMetaString( KettleDatabaseRepository.FIELD_SLAVE_PORT ), slaveServer
        .getPort() );
    table.addValue( new ValueMetaString(
      KettleDatabaseRepository.FIELD_SLAVE_WEB_APP_NAME ), slaveServer
      .getWebAppName() );
    table.addValue(
      new ValueMetaString( KettleDatabaseRepository.FIELD_SLAVE_USERNAME ),
      slaveServer.getUsername() );
    table.addValue(
      new ValueMetaString( KettleDatabaseRepository.FIELD_SLAVE_PASSWORD ), Encr
        .encryptPasswordIfNotUsingVariables( slaveServer.getPassword() ) );
    table.addValue( new ValueMetaString(
      KettleDatabaseRepository.FIELD_SLAVE_PROXY_HOST_NAME ), slaveServer
      .getProxyHostname() );
    table.addValue(
      new ValueMetaString( KettleDatabaseRepository.FIELD_SLAVE_PROXY_PORT ),
      slaveServer.getProxyPort() );
    table.addValue( new ValueMetaString(
      KettleDatabaseRepository.FIELD_SLAVE_NON_PROXY_HOSTS ), slaveServer
      .getNonProxyHosts() );
    table.addValue(
      new ValueMetaBoolean( KettleDatabaseRepository.FIELD_SLAVE_MASTER ), Boolean
        .valueOf( slaveServer.isMaster() ) );

    repository.connectionDelegate.updateTableRow(
      KettleDatabaseRepository.TABLE_R_SLAVE, KettleDatabaseRepository.FIELD_SLAVE_ID_SLAVE, table, slaveServer
        .getObjectId() );
  }
}
