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

package org.pentaho.di.starmodeler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleObjectExistsException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.metastore.DatabaseMetaStoreUtil;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.shared.SharedObjects;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.metastore.api.IMetaStore;

public class SharedDatabaseUtil {

  /**
   * Add a database to the list of shared databases in ~/.kettle/shared.xml
   *
   * @param databaseMeta
   * @throws KettleException in case there is an error
   * @throws KettleObjectExistsException if a database with the same name already exists
   */
  public static void addSharedDatabase(DatabaseMeta databaseMeta) throws KettleObjectExistsException, KettleException {
    // First verify existence...
    //
    List<DatabaseMeta> sharedDatabases = loadSharedDatabases();
    DatabaseMeta found = DatabaseMeta.findDatabase(sharedDatabases, databaseMeta.getName());
    if (found!=null) {
      throw new KettleObjectExistsException("A database with name '"+databaseMeta.getName()+"' already exists in the shared databases list.");
    }
    try {
      SharedObjects sharedObjects = new SharedObjects();
      sharedObjects.storeObject(databaseMeta);
      sharedObjects.saveToFile();
    } catch(Exception e) {
      throw new KettleException("It was not possible to add database '"+databaseMeta.getName()+"' to the shared.xml file");
    }
  }

  public static List<DatabaseMeta> getDatabaseMetaList(IMetaStore metaStore) {
    try {
      return DatabaseMetaStoreUtil.getDatabaseElements(metaStore);
    } catch(Exception e) {
      new ErrorDialog(Spoon.getInstance().getShell(), "Error retrieving databases", "There was an error retrieving database from the MetaStore", e);
      return new ArrayList<DatabaseMeta>(); // empty list.
    }
  }

  public static List<DatabaseMeta> loadSharedDatabases() {
    List<DatabaseMeta> sharedDatabases= new ArrayList<DatabaseMeta>();
    try {
      SharedObjects sharedObjects = new SharedObjects();
      for (SharedObjectInterface sharedObject : sharedObjects.getObjectsMap().values()) {
        if (sharedObject instanceof DatabaseMeta) {
          sharedDatabases.add((DatabaseMeta) sharedObject);
        }
      }
    } catch(Exception e) {
      LogChannel.GENERAL.logError("Unable to load shared objects", e);
    }

    return sharedDatabases;
  }

  public static String[] getSortedDatabaseNames(List<DatabaseMeta> sharedDatabases) {

    String[] databaseNames = new String[sharedDatabases.size()];
    for (int i=0;i<sharedDatabases.size();i++) databaseNames[i] = sharedDatabases.get(i).getName();
    Arrays.sort(databaseNames);

    return databaseNames;
  }
}
