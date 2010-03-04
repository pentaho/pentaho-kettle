/* Copyright (c) 2009 Pentaho Corporation.  All rights reserved. 
* This software was developed by Pentaho Corporation and is provided under the terms 
* of the GNU Lesser General Public License, Version 2.1. You may not use 
* this file except in compliance with the license. If you need a copy of the license, 
* please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
* Data Integration.  The Initial Developer is Pentaho Corporation.
*
* Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
* basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
* the license for the specific language governing your rights and limitations.*/

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
