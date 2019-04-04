/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

import java.util.Comparator;
import java.util.Date;

import org.pentaho.di.repository.IRepositoryService;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryObjectInterface;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.ui.xul.util.AbstractModelNode;

public abstract class UIRepositoryObject extends AbstractModelNode<UIRepositoryObject> {

  private static final long serialVersionUID = -456272921332284281L;

  // This object can be a Directory or a RepositoryContent
  protected RepositoryObjectInterface obj;
  protected Repository rep;
  private RepositoryObjectComparator roc;
  private static final DateObjectComparator doc = new DateObjectComparator();
  private IRepositoryService repositoryService;

  public UIRepositoryObject() {
    super();
    roc = new RepositoryObjectComparator();
  }

  public UIRepositoryObject( RepositoryObjectInterface obj ) {
    this();
    this.obj = obj;
  }

  public UIRepositoryObject( RepositoryObjectInterface obj, Repository rep ) {
    this( obj );
    this.rep = rep;
  }

  public String getId() {
    if ( obj != null && obj.getObjectId() != null ) {
      return obj.getObjectId().getId();
    } else {
      return null;
    }

  }

  public ObjectId getObjectId() {
    if ( obj == null ) {
      return null;
    }
    return obj.getObjectId();
  }

  public String getName() {
    return obj.getName();
  }

  public abstract void setName( String name ) throws Exception;

  public abstract void move( UIRepositoryDirectory newParentDir ) throws Exception;

  public abstract void delete() throws Exception;

  public abstract Date getModifiedDate();

  public abstract String getFormatModifiedDate();

  public abstract String getModifiedUser();

  public abstract RepositoryObjectType getRepositoryElementType();

  public abstract String getType();

  public abstract String getDescription();

  public abstract UIRepositoryDirectory getParent();

  public String getParentPath() {
    return getParent() != null ? getParent().getPath() : null;
  }

  public boolean isDeleted() {
    return false;
  }

  public abstract String getImage();

  public Repository getRepository() {
    return rep;
  }

  public void setRepository( Repository rep ) {
    this.rep = rep;
  }

  static class DateObjectComparator implements Comparator<UIRepositoryObject> {
    @Override
    public int compare( UIRepositoryObject o1, UIRepositoryObject o2 ) {
      Date d1 = o1 != null ? o1.getModifiedDate() : null;
      Date d2 = o2 != null ? o2.getModifiedDate() : null;

      long t1 = d1 != null ? d1.getTime() : 0;
      long t2 = d2 != null ? d2.getTime() : 0;

      int res = 0;

      if ( t1 > t2 ) {
        res = 1;
      } else if ( t1 < t2 ) {
        res = -1;
      }

      return res;

    }
  }

  static class RepositoryObjectComparator implements Comparator<UIRepositoryObject> {

    public int compare( UIRepositoryObject o1, UIRepositoryObject o2 ) {
      int cat1 = o1.getCategory();
      int cat2 = o2.getCategory();
      if ( cat1 != cat2 ) {
        return cat1 - cat2;
      }
      String t1 = o1.getName();
      String t2 = o2.getName();
      if ( t1 == null ) {
        t1 = "";
      }
      if ( t2 == null ) {
        t2 = "";
      }
      return t1.compareToIgnoreCase( t2 );
    }

  }

  public String getPath() {
    return getParentPath() + "/" + getName();
  }

  public RepositoryObjectComparator getComparator() {
    return roc;
  }

  public void setComparator( RepositoryObjectComparator roc ) {
    this.roc = roc;
  }

  public DateObjectComparator getDateComparator() {
    return UIRepositoryObject.doc;
  }

  public IRepositoryService getRepositoryService() {
    return repositoryService;
  }

  public void setRepositoryService( IRepositoryService repositoryService ) {
    this.repositoryService = repositoryService;
  }

  public abstract int getCategory();
}
