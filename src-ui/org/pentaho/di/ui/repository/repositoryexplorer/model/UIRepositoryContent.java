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
import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.ObjectRevision;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryContent;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryElementLocationInterface;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.ui.repository.repositoryexplorer.AccessDeniedException;

public abstract class UIRepositoryContent extends UIRepositoryObject implements RepositoryElementLocationInterface{

  private RepositoryContent rc;
  private UIRepositoryObjectRevisions revisions;
  protected UIRepositoryDirectory uiParent;
  
  public UIRepositoryContent() {
    super();
  }
  
  public UIRepositoryContent(RepositoryContent rc, UIRepositoryDirectory parent, Repository rep) {
    super(rc, rep);
    this.rc = rc;
    this.uiParent = parent;
  }
  @Override
  public String getDescription() {
    return rc.getDescription();
  }

  @Override
  public String getLockMessage() {
    return rc.getLockMessage();
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

  public UIRepositoryObjectRevisions getRevisions() throws KettleException {
    if (revisions != null){
      return revisions;
    }
    
    revisions = new UIRepositoryObjectRevisions();
    
    List <ObjectRevision> or = getRepository().getRevisions(getObjectId());

    for (ObjectRevision rev : or) {
      revisions.add(new UIRepositoryObjectRevision(rev));
    }
    return revisions;
  }
  
  protected void refreshRevisions() throws KettleException {
    revisions = null;
    getRevisions();
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

  public void readAcls(UIRepositoryObjectAcls acls) throws AccessDeniedException{
    try {
      acls.setObjectAcl(getRepository().getAcl(getObjectId()));
    } catch(KettleException ke) {
      throw new AccessDeniedException(ke);
    }
  }

  public void setAcls(UIRepositoryObjectAcls security) throws AccessDeniedException{
    try {
      getRepository().setAcl(getObjectId(), security.getObjectAcl());
    } catch (KettleException e) {
      throw new AccessDeniedException(e);
    }
  }

  @Override
  public int getCategory() {
    return 20;
  }
  
  public abstract void restoreVersion(UIRepositoryObjectRevision revision, String commitMessage) throws KettleException;
}
