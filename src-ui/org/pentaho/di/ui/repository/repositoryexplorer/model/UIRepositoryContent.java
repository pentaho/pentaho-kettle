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

import java.text.SimpleDateFormat;
import java.util.Date;

import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryElementMetaInterface;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryObjectType;

public abstract class UIRepositoryContent extends UIRepositoryObject {

  protected RepositoryElementMetaInterface rc;
  protected UIRepositoryDirectory uiParent;
  
  public UIRepositoryContent() {
    super();
  }
  
  public UIRepositoryContent(RepositoryElementMetaInterface rc, UIRepositoryDirectory parent, Repository rep) {
    super(rc, rep);
    this.rc = rc;
    this.uiParent = parent;
  }
  
  @Override
  public String getDescription() {
    return rc.getDescription();
  }

  @Override
  public Date getModifiedDate() {
    return rc.getModifiedDate();
  }

  @Override
  public String getModifiedUser() {
    return rc.getModifiedUser();
  }

  @Override
  public String getType() {
    return rc.getObjectType().name();
  }

  @Override
  public String getFormatModifiedDate() {
    Date date =  rc.getModifiedDate();
    String str = null;
    if (date != null){
      SimpleDateFormat sdf = new SimpleDateFormat("d MMM yyyy HH:mm:ss z"); //$NON-NLS-1$
      str = sdf.format(date);
    }
    return str;
  }

  // TODO: Remove references to the Kettle object RepositoryDirectory
  public RepositoryDirectory getRepositoryDirectory() {
    return (RepositoryDirectory) uiParent.getDirectory();
  }

  public RepositoryObjectType getRepositoryElementType() {
    return rc.getObjectType();
  }

  public void setName(String name) throws Exception{
    if (rc.getName().equalsIgnoreCase(name)){
      return;
    }
    rc.setName(name);
  }
  
  
  @Override
  public String getImage() {
    //TODO: a generic image for unknown content?
    return ""; //$NON-NLS-1$
  }

  @Override
  public void delete() throws Exception {
    
  }

  @Override
  public void move(UIRepositoryDirectory newParentDir) throws Exception {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getCategory() {
    return 20;
  }
  
  public UIRepositoryDirectory getParent() {
    return uiParent;
  }
}
