/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.ui.repository.repositoryexplorer.model;

import java.util.List;

import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.repository.ObjectRevision;
import org.pentaho.ui.xul.XulEventSourceAdapter;

public class UIDatabaseConnection extends XulEventSourceAdapter {

  private DatabaseMeta dbMeta;
  
  private List<ObjectRevision> revHistory;
  
  public UIDatabaseConnection() {
    super();
  }
  
  public UIDatabaseConnection(DatabaseMeta databaseMeta) {
    super();
    this.dbMeta = databaseMeta;
  }
  
  public UIDatabaseConnection(DatabaseMeta databaseMeta, List<ObjectRevision> revHistory) {
    this(databaseMeta);
    this.revHistory = revHistory;
  }
  
  public String getName() {
    if(dbMeta != null) {
      return dbMeta.getName();
    }
    return null;
  }

  public String getType() {
    if(dbMeta != null) {
      return dbMeta.getPluginId();
    }
    return null;
  }
 /* 
  public UIRepositoryObjectRevisions getRevisions() {
    if(revHistory == null || revHistory.size() <= 0) {
      // Revision history does not exist for this database connection
      return null;
    }
    
    UIRepositoryObjectRevisions revisions = new UIRepositoryObjectRevisions();
    
    for(ObjectRevision rev : revHistory) {
      revisions.add(new UIRepositoryObjectRevision(rev));
    }
    
    return revisions;
  }
*/
  public String getDateModified() {
    if(revHistory != null && revHistory.size() > 0) {
      return revHistory.get(revHistory.size() - 1).getCreationDate().toString();
    }
    return null;
  }
  
  public DatabaseMeta getDatabaseMeta() {
    return dbMeta;
  }

}
