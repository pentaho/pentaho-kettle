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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.pentaho.di.repository.Directory;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryContent;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.repository.StringObjectId;

public class UIRepositoryDirectory extends UIRepositoryObject {

  private Directory rd;
  private List <UIRepositoryDirectory> kidDirectoryCache = null;
  private List<UIRepositoryObject> kidElementCache = null;
  
  public UIRepositoryDirectory() {
    super();
  }
  
  public UIRepositoryDirectory(Directory rd, Repository rep) {
    super(rd, rep);
    this.rd = rd;
  }

  
  public List<UIRepositoryDirectory> getChildren(){
    // We've been here before.. use the cache
    if (kidDirectoryCache != null){
      return kidDirectoryCache;
    }
    
    kidDirectoryCache = new ArrayList<UIRepositoryDirectory>();
    if (rd.getChildren()==null){
      return kidDirectoryCache;
    }

    for (Directory child : rd.getChildren()) {
      kidDirectoryCache.add(new UIRepositoryDirectory(child, rep));
    }
    return kidDirectoryCache;
  }
  
  // TODO: Abstract working model; should throw RepositoryException
  // TODO: We will need a way to reset this cache when a directory or element changes
  public List<UIRepositoryObject> getRepositoryObjects()throws Exception {
    // We've been here before.. use the cache
    if (kidElementCache != null){
      return kidElementCache;
    }
    
    kidElementCache = new ArrayList<UIRepositoryObject>();

    for (Directory child : rd.getChildren()) {
      kidElementCache.add(new UIRepositoryDirectory(child, rep));
    }
    List<? extends RepositoryContent> transformations;
    transformations = rep.getTransformationObjects(new StringObjectId(getId()), true);
    for (RepositoryContent child : transformations) {
      kidElementCache.add(new UIRepositoryContent(child, rd, rep));
    }
    List<? extends RepositoryContent> jobs;
    jobs = rep.getJobObjects(new StringObjectId(getId()), true);
    for (RepositoryContent child : jobs) {
      
      kidElementCache.add(new UIRepositoryContent(child, rd, rep));
    }
    return kidElementCache;
  }

  public boolean isRevisionsSupported(){
    return rep.getRepositoryMeta().getRepositoryCapabilities().supportsRevisions();
  }
 
  public String toString(){
    return getName();
  }

  public void setName(String name)throws Exception{
    if (getDirectory().getName().equalsIgnoreCase(name)){
      return;
    }
    //getDirectory().setName(name);
    //rep.renameRepositoryDirectory(getDirectory());
  }
  
  public String getDescription() {
    return null;
  }

  public String getLockMessage() {
    return null;
  }

  public Date getModifiedDate() {
    return null;
  }

  public String getModifiedUser() {
    return null;
  }

  public RepositoryObjectType getRepositoryElementType() {
    return null;
  }

  @Override
  public boolean isDeleted() {
    return super.isDeleted();
  }

  @Override
  public String getType() {
    return null;
  }

  @Override
  public String getFormatModifiedDate() {
    return null;
  }
  
  public RepositoryDirectory getDirectory(){
    return (RepositoryDirectory)rd;
  }
  
  public void contentChanged()throws Exception{
    this.kidDirectoryCache = null;
    this.kidElementCache = null;
    rd = rep.loadRepositoryDirectoryTree();
  }

  @Override
  public String getImage() {
    return "images/treeClosed.png";
  }
  
}
