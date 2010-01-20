/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2009 Pentaho Corporation.  All rights reserved.
 */
package org.pentaho.di.ui.repository.repositoryexplorer.model;

import java.util.Comparator;
import java.util.Date;

import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryElement;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.ui.xul.XulEventSourceAdapter;

public abstract class UIRepositoryObject extends XulEventSourceAdapter {
  
  // This object can be a Directory or a RepositoryContent
  protected RepositoryElement obj;
  protected Repository rep;
  private RepositoryObjectComparator roc;

  public UIRepositoryObject() {
    super();
    roc = new RepositoryObjectComparator();
  }

  public UIRepositoryObject(RepositoryElement obj) {
    this();
    this.obj = obj;
  }
  
  public UIRepositoryObject(RepositoryElement obj, Repository rep) {
    this(obj);
    this.rep = rep;
  }

  public String getId() {
    return obj.getObjectId().getId();
  }

  public ObjectId getObjectId() {
    return obj.getObjectId();
  }

  public String getName() {
    return obj.getName();
  }

  public abstract void setName(String name)throws Exception;
  
  public abstract void move(UIRepositoryDirectory newParentDir) throws Exception;
  
  public abstract void delete()throws Exception;

  public abstract Date getModifiedDate();

  public abstract String getFormatModifiedDate();

  public abstract String getModifiedUser();

  public abstract RepositoryObjectType getRepositoryElementType();

  public abstract String getType();

  public abstract String getDescription();

  public abstract String getLockMessage();
  
  public boolean isDeleted(){
    return false;
  }

  public abstract String getImage();

  public Repository getRepository() {
    return rep;
  }

  public void setRepository(Repository rep) {
    this.rep = rep;
  }
  
  static class RepositoryObjectComparator implements Comparator<UIRepositoryObject> {

    public int compare(UIRepositoryObject o1, UIRepositoryObject o2) {
      if (!(o1 instanceof UIRepositoryObject)){
        return -1;
      }
      if (!(o2 instanceof UIRepositoryObject)){
        return -1;
      }

      int cat1 = o1.getCategory();
      int cat2 = o2.getCategory();
      if (cat1 != cat2) {
        return cat1 - cat2;
      }
      String t1 = o1.getName();
      String t2 = o2.getName();
      if (t1 == null) t1 = "";
      if (t2 == null) t2 = "";
      return t1.compareToIgnoreCase(t2);
    }
    
  }

  public RepositoryObjectComparator getComparator() {
    return roc;
  }

  public void setComparator(RepositoryObjectComparator roc) {
    this.roc = roc;
  }
  
  public abstract int getCategory();


}
