/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */
package org.pentaho.di.repository.kdr.delegates;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleObjectExistsException;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.kdr.KettleDatabaseRepository;
import org.pentaho.di.core.encryption.Encr;

public class KettleDatabaseRepositorySlaveServerDelegate extends KettleDatabaseRepositoryBaseDelegate {

  private static Class<?> PKG = SlaveServer.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

  public KettleDatabaseRepositorySlaveServerDelegate(KettleDatabaseRepository repository) {
    super(repository);
  }

  public RowMetaAndData getSlaveServer(ObjectId id_slave) throws KettleException {
    return repository.connectionDelegate.getOneRow(quoteTable(KettleDatabaseRepository.TABLE_R_SLAVE),
        quote(KettleDatabaseRepository.FIELD_SLAVE_ID_SLAVE), id_slave);
  }

  public synchronized ObjectId getSlaveID(String name) throws KettleException {
    return repository.connectionDelegate.getIDWithValue(quoteTable(KettleDatabaseRepository.TABLE_R_SLAVE),
        quote(KettleDatabaseRepository.FIELD_SLAVE_ID_SLAVE), quote(KettleDatabaseRepository.FIELD_SLAVE_NAME), name);
  }

  public void saveSlaveServer(SlaveServer slaveServer) throws KettleException {
    saveSlaveServer(slaveServer, null, false);
  }

  public void saveSlaveServer(SlaveServer slaveServer, ObjectId id_transformation, boolean isUsedByTransformation)
      throws KettleException {
    try {
      saveSlaveServer(slaveServer, id_transformation, isUsedByTransformation, false);
    } catch (KettleObjectExistsException e) {
      // This is an expected possibility here. Common objects are not going to overwrite database objects
      log.logBasic(e.getMessage());
    }
  }

  public void saveSlaveServer(SlaveServer slaveServer, ObjectId id_transformation, boolean isUsedByTransformation,
      boolean overwrite) throws KettleException {

    if (slaveServer.getObjectId() == null) {
      // New Slave Server
      slaveServer.setObjectId(insertSlave(slaveServer));
    } else {
      ObjectId existingSlaveId = getSlaveID(slaveServer.getName());
      if (existingSlaveId != null && slaveServer.getObjectId() != null
          && !slaveServer.getObjectId().equals(existingSlaveId)) {
        // A slave with this name already exists
        if (overwrite) {
          // Proceed with save, removing the original version from the repository first
          repository.deleteSlave(existingSlaveId);
          updateSlave(slaveServer);
        } else {
          throw new KettleObjectExistsException("Failed to save object to repository. Object [" + slaveServer.getName()
              + "] already exists.");
        }
      } else {
        // There are no naming collisions (either it is the same object or the name is unique)
        updateSlave(slaveServer);
      }
    }

    // Save the trans-slave relationship too.
    if (id_transformation != null && isUsedByTransformation) {
      repository.insertTransformationSlave(id_transformation, slaveServer.getObjectId());
    }
  }

  public SlaveServer loadSlaveServer(ObjectId id_slave_server) throws KettleException {
    SlaveServer slaveServer = new SlaveServer();

    slaveServer.setObjectId(id_slave_server);

    RowMetaAndData row = getSlaveServer(id_slave_server);
    if (row == null) {
      throw new KettleDatabaseException(BaseMessages.getString(PKG, "SlaveServer.SlaveCouldNotBeFound", id_slave_server.toString())); //$NON-NLS-1$
    }

    slaveServer.setName(row.getString(KettleDatabaseRepository.FIELD_SLAVE_NAME, null)); //$NON-NLS-1$
    slaveServer.setHostname(row.getString(KettleDatabaseRepository.FIELD_SLAVE_HOST_NAME, null)); //$NON-NLS-1$
    slaveServer.setPort(row.getString(KettleDatabaseRepository.FIELD_SLAVE_PORT, null)); //$NON-NLS-1$
    slaveServer.setWebAppName(row.getString(KettleDatabaseRepository.FIELD_SLAVE_WEB_APP_NAME, null)); //$NON-NLS-1$
    slaveServer.setUsername(row.getString(KettleDatabaseRepository.FIELD_SLAVE_USERNAME, null)); //$NON-NLS-1$
    slaveServer.setPassword(Encr.decryptPasswordOptionallyEncrypted(row.getString(
        KettleDatabaseRepository.FIELD_SLAVE_PASSWORD, null))); //$NON-NLS-1$
    slaveServer.setProxyHostname(row.getString(KettleDatabaseRepository.FIELD_SLAVE_PROXY_HOST_NAME, null)); //$NON-NLS-1$
    slaveServer.setProxyPort(row.getString(KettleDatabaseRepository.FIELD_SLAVE_PROXY_PORT, null)); //$NON-NLS-1$
    slaveServer.setNonProxyHosts(row.getString(KettleDatabaseRepository.FIELD_SLAVE_NON_PROXY_HOSTS, null)); //$NON-NLS-1$
    slaveServer.setMaster(row.getBoolean(KettleDatabaseRepository.FIELD_SLAVE_MASTER, false)); //$NON-NLS-1$

    return slaveServer;
  }

  public synchronized ObjectId insertSlave(SlaveServer slaveServer) throws KettleException {
    if (getSlaveID(slaveServer.getName()) != null) {
      // This slave server name is already in use. Throw an exception.
      throw new KettleObjectExistsException("Failed to create object in repository. Object [" + slaveServer.getName()
          + "] already exists.");
    }

    ObjectId id = repository.connectionDelegate.getNextSlaveServerID();

    RowMetaAndData table = new RowMetaAndData();

    table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_SLAVE_ID_SLAVE, ValueMetaInterface.TYPE_INTEGER), id);
    table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_SLAVE_NAME, ValueMetaInterface.TYPE_STRING),
        slaveServer.getName());
    table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_SLAVE_HOST_NAME, ValueMetaInterface.TYPE_STRING),
        slaveServer.getHostname());
    table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_SLAVE_PORT, ValueMetaInterface.TYPE_STRING),
        slaveServer.getPort());
    table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_SLAVE_WEB_APP_NAME, ValueMetaInterface.TYPE_STRING),
        slaveServer.getWebAppName());
    table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_SLAVE_USERNAME, ValueMetaInterface.TYPE_STRING),
        slaveServer.getUsername());
    table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_SLAVE_PASSWORD, ValueMetaInterface.TYPE_STRING), Encr
        .encryptPasswordIfNotUsingVariables(slaveServer.getPassword()));
    table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_SLAVE_PROXY_HOST_NAME, ValueMetaInterface.TYPE_STRING),
        slaveServer.getProxyHostname());
    table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_SLAVE_PROXY_PORT, ValueMetaInterface.TYPE_STRING),
        slaveServer.getProxyPort());
    table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_SLAVE_NON_PROXY_HOSTS, ValueMetaInterface.TYPE_STRING),
        slaveServer.getNonProxyHosts());
    table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_SLAVE_MASTER, ValueMetaInterface.TYPE_BOOLEAN), Boolean
        .valueOf(slaveServer.isMaster()));

    repository.connectionDelegate.getDatabase().prepareInsert(table.getRowMeta(),
        KettleDatabaseRepository.TABLE_R_SLAVE);
    repository.connectionDelegate.getDatabase().setValuesInsert(table);
    repository.connectionDelegate.getDatabase().insertRow();
    repository.connectionDelegate.getDatabase().closeInsert();

    return id;
  }

  public synchronized void updateSlave(SlaveServer slaveServer) throws KettleException {
    RowMetaAndData table = new RowMetaAndData();
    table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_SLAVE_NAME, ValueMetaInterface.TYPE_STRING),
        slaveServer.getName());
    table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_SLAVE_HOST_NAME, ValueMetaInterface.TYPE_STRING),
        slaveServer.getHostname());
    table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_SLAVE_PORT, ValueMetaInterface.TYPE_STRING),
        slaveServer.getPort());
    table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_SLAVE_WEB_APP_NAME, ValueMetaInterface.TYPE_STRING),
        slaveServer.getWebAppName());
    table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_SLAVE_USERNAME, ValueMetaInterface.TYPE_STRING),
        slaveServer.getUsername());
    table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_SLAVE_PASSWORD, ValueMetaInterface.TYPE_STRING), Encr
        .encryptPasswordIfNotUsingVariables(slaveServer.getPassword()));
    table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_SLAVE_PROXY_HOST_NAME, ValueMetaInterface.TYPE_STRING),
        slaveServer.getProxyHostname());
    table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_SLAVE_PROXY_PORT, ValueMetaInterface.TYPE_STRING),
        slaveServer.getProxyPort());
    table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_SLAVE_NON_PROXY_HOSTS, ValueMetaInterface.TYPE_STRING),
        slaveServer.getNonProxyHosts());
    table.addValue(new ValueMeta(KettleDatabaseRepository.FIELD_SLAVE_MASTER, ValueMetaInterface.TYPE_BOOLEAN), Boolean
        .valueOf(slaveServer.isMaster()));

    repository.connectionDelegate.updateTableRow(KettleDatabaseRepository.TABLE_R_SLAVE,
        KettleDatabaseRepository.FIELD_SLAVE_ID_SLAVE, table, slaveServer.getObjectId());
  }
}
