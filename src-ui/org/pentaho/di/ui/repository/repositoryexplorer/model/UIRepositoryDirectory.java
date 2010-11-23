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

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryElementMetaInterface;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.repository.StringObjectId;

public class UIRepositoryDirectory extends UIRepositoryObject {

  private static final long serialVersionUID = -2003651575793768451L;

  private RepositoryDirectoryInterface rd;
  private UIRepositoryDirectory uiParent = null;
  private UIRepositoryDirectories kidDirectoryCache = null;
  private UIRepositoryObjects kidElementCache = null;
  private boolean expanded = false;
  
  public UIRepositoryDirectory() {
    super();
    kidDirectoryCache = null;
    kidElementCache = null;
  }

  public UIRepositoryDirectory(RepositoryDirectoryInterface rd, UIRepositoryDirectory uiParent, Repository rep) {
    super(rd, rep);
    this.uiParent = uiParent;
    this.rd = rd;
    kidDirectoryCache = null;
    kidElementCache = null;    
  }
  
  public UIRepositoryDirectories getChildren(){
    // We've been here before.. use the cache
    if (kidDirectoryCache != null && kidDirectoryCache.isEmpty() == false){
      return kidDirectoryCache;
    }
    if(kidDirectoryCache == null){
      kidDirectoryCache = new UIRepositoryDirectories();
    }
    if (rd.getChildren()==null){
      return kidDirectoryCache;
    }

    for (RepositoryDirectoryInterface child : rd.getChildren()) {
      try {
        kidDirectoryCache.add(UIObjectRegistry.getInstance().constructUIRepositoryDirectory(child, this, rep));
      } catch (UIObjectCreationException e) {
        kidDirectoryCache.add(new UIRepositoryDirectory(child, this, rep));
      }
    }
    return kidDirectoryCache;
  }
  
  public void setChildren(UIRepositoryDirectories children){
    kidDirectoryCache = children;
  }

  // TODO: Abstract working model; should throw RepositoryException
  // TODO: We will need a way to reset this cache when a directory or element changes
  public UIRepositoryObjects getRepositoryObjects() throws KettleException {
    // We've been here before.. use the cache
    
    if (kidElementCache != null && !kidElementCache.isEmpty() ){
      return kidElementCache;
    }
    
    if(kidElementCache == null){
      kidElementCache = new UIRepositoryObjects();
    }
    for (UIRepositoryObject child : getChildren()) {
      kidElementCache.add(child);
    }
    
    List<? extends RepositoryElementMetaInterface> jobsAndTransformations = getDirectory().getRepositoryObjects();
    
    if (jobsAndTransformations == null || jobsAndTransformations.size() == 0) {
      jobsAndTransformations = rep.getJobAndTransformationObjects(new StringObjectId(getId()), false);
    }
    for (RepositoryElementMetaInterface child : jobsAndTransformations) {
      if (child.getObjectType().equals(RepositoryObjectType.TRANSFORMATION)) {
       	try {
          kidElementCache.add(UIObjectRegistry.getInstance().constructUITransformation(child, this, rep));
        } catch (UIObjectCreationException e) {
          kidElementCache.add(new UITransformation(child, this, rep));
        }
    	} else if (child.getObjectType().equals(RepositoryObjectType.JOB)){
        try {
    	    kidElementCache.add(UIObjectRegistry.getInstance().constructUIJob(child, this, rep));
    	  } catch (UIObjectCreationException e) {
    	    kidElementCache.add(new UIJob(child, this, rep));
    	  }      
    	}
    }
    return kidElementCache;
  }

  public String toString(){
    return getName();
  }

  public void setName(String name)throws Exception{
    if (getDirectory().getName().equalsIgnoreCase(name)){
      return;
    }

    rep.renameRepositoryDirectory(getDirectory().getObjectId(), null, name);
    refresh();
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
  

  @Override
  public String getImage() {
    return "images/treeClosed.png";
  }
  
  public void delete()throws Exception{
    rep.deleteRepositoryDirectory(getDirectory());
    uiParent.getChildren().remove(this);
    if(uiParent.getRepositoryObjects().contains(this))
      uiParent.getRepositoryObjects().remove(this);
  }
  
  public UIRepositoryDirectory createFolder(String name) throws Exception{
    RepositoryDirectoryInterface dir = getRepository().createRepositoryDirectory(getDirectory(), name);
    UIRepositoryDirectory newDir = null;
    try {
      newDir = UIObjectRegistry.getInstance().constructUIRepositoryDirectory(dir, this, rep);
    }  catch(UIObjectCreationException uoe) {
      newDir = new UIRepositoryDirectory(dir, this, rep);
    }
    UIRepositoryDirectories directories = getChildren();
    if(!contains(directories, newDir)) {
      directories.add(newDir);
    }
    kidElementCache.clear(); // rebuild the element cache for correct positioning.
    return newDir;
  }

  public void fireCollectionChanged() {
    
    firePropertyChange("children", null, getChildren());

    if (kidDirectoryCache != null)
      kidDirectoryCache.fireCollectionChanged();
    if (kidElementCache != null)
      kidElementCache.fireCollectionChanged();
  }

  @Override
  public void move(UIRepositoryDirectory newParentDir) throws Exception {
    if(newParentDir != null) {
      rep.renameRepositoryDirectory(obj.getObjectId(), newParentDir.getDirectory(), null);
      newParentDir.refresh();
    }
  }
  
  protected UIRepositoryDirectory getParentDirectory() {
    return uiParent;
  }
  
  protected UIRepositoryDirectory getRootDirectory() {
    UIRepositoryDirectory parent = uiParent, result = this;

    while(parent != null) {
      result = parent;
      parent = parent.getParentDirectory();
    }
    
    return result;
  }
  
  /**
   * Synchronize this folder with the back-end
   * 
   * 
   */
  public void refresh() {
    try {
      if(kidElementCache != null){
        kidElementCache.clear();
      }
      if(kidDirectoryCache != null){
        kidDirectoryCache.clear();
      }
      if(this == getRootDirectory()) {
        RepositoryDirectoryInterface localRoot = rep.loadRepositoryDirectoryTree().findDirectory(rd.getObjectId());
        rd = localRoot;
        //Rebuild caches
        fireCollectionChanged();
      } else {
        getRootDirectory().refresh();
      }
    } catch (Exception e) {
      // TODO: Better error handling
      e.printStackTrace();
    }
  }
  
  @Override
  public int getCategory() {
    return 10;
  }
  
  public boolean isExpanded() {
    return expanded;
  }
  
  public void setExpanded(boolean expand) {
    this.expanded = expand;
  }
  
  public void toggleExpanded() {
    setExpanded(!isExpanded());
    firePropertyChange("expanded", null, this.expanded); //$NON-NLS-1$
  }
  
  public UIRepositoryDirectory getParent() {
    return uiParent;
  }
  
  public String getPath() {
    return ((RepositoryDirectory) rd).getPath();
  }

  public boolean isVisible() {
    return rd.isVisible();
  }

  // begin PDI-3326 hack
  
  @Override
  public int size() {
    return getChildren().size();
  }

  @Override
  public UIRepositoryObject get(int index) {
    return getChildren().get(index);
  }
  
  @Override
  public Iterator<UIRepositoryObject> iterator() {
    return getChildren().iterator();
  }
  
  private boolean contains(UIRepositoryDirectories directories, UIRepositoryDirectory searchDir) {
    for(int i=0; i < directories.size(); i++) {
      UIRepositoryObject dir = directories.get(i);
      if(dir instanceof UIRepositoryDirectory) {
        return dir.getName() != null && dir.getName().equals(searchDir.getName());
      }
    }
    return false;
  }
  // end PDI-3326 hack
}
