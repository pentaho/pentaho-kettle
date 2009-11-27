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
package org.pentaho.di.ui.repository.repositoryexplorer.controllers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.pentaho.di.repository.Directory;
import org.pentaho.di.ui.repository.repositoryexplorer.RepositoryExplorerCallback;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryContent;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryDirectory;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryObject;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryObjectRevision;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryObjectRevisions;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;


/**
 *
 * This is the XulEventHandler for the browse panel of the repository explorer. It sets up the bindings for  
 * browse functionality.
 * 
 */
public class BrowseController extends AbstractXulEventHandler{

  private XulTree folderTree;
  private XulTree fileTable;
  private XulTree revisionTable;

  private UIRepositoryDirectory repositoryDirectory; 
  private RepositoryExplorerCallback callback;
  
  BindingFactory bf;
  Binding directoryBinding;
  
  public BrowseController() {
  }
  
  public void init() {
    createBindings();
  }
  
  private void createBindings(){
    folderTree = (XulTree) document.getElementById("folder-tree");
    fileTable = (XulTree) document.getElementById("file-table");
    revisionTable = (XulTree) document.getElementById("revision-table");

    // Bind the repository folder structure to the folder tree.
    bf.setBindingType(Binding.Type.ONE_WAY);
    directoryBinding = bf.createBinding(repositoryDirectory, "children", folderTree, "elements");
    
    
    // Bind the selected index from the folder tree to the list of repository objects in the file table. 
    bf.setBindingType(Binding.Type.ONE_WAY);
    bf.createBinding(folderTree, "selectedItems", fileTable, "elements", 
        new BindingConvertor<List<UIRepositoryDirectory>, List<UIRepositoryObject>>() {
          @Override
          public List<UIRepositoryObject> sourceToTarget(List<UIRepositoryDirectory> rd) {
            List<UIRepositoryObject> listOfObjects = new ArrayList<UIRepositoryObject>();
            
            if(rd==null){
              return null;
            }
            if (rd.size()<=0){
              return null;
            }
            try {
              listOfObjects = rd.get(0).getRepositoryObjects();
            } catch (Exception e) {
              // how do we handle exceptions in a binding? dialog here? 
              // TODO: handle exception
            }
            return listOfObjects;
          }
          @Override
          public List<UIRepositoryDirectory> targetToSource(List<UIRepositoryObject> elements) {
            return null;
          }
        });

    // will need this binding to control a double-click in the file table on a child folder
    /*
    bf.setBindingType(Binding.Type.ONE_WAY);
    bf.createBinding(fileTable, "selectedItem", folderTree, "selectedItem");
    */

    bf.setBindingType(Binding.Type.ONE_WAY);
    Binding revisionTreeBinding = bf.createBinding(repositoryDirectory,"revisionsSupported", "revision-table", "!disabled");
    Binding revisionLabelBinding = bf.createBinding(repositoryDirectory,"revisionsSupported", "revision-label", "!disabled");

    BindingConvertor<int[], Boolean> forButtons = new BindingConvertor<int[], Boolean>() {

      @Override
      public Boolean sourceToTarget(int[] value) {
        return value != null && !(value.length<=0);
      }

      @Override
      public int[] targetToSource(Boolean value) {
        return null;
      }
    };
    
    bf.createBinding(revisionTable,"selectedRows", "revision-open", "!disabled", forButtons);
    bf.createBinding(revisionTable,"selectedRows", "revision-remove", "!disabled", forButtons);
    
    Binding revisionBinding = null;
    if (repositoryDirectory.isRevisionsSupported()){
      bf.setBindingType(Binding.Type.ONE_WAY);
      revisionBinding = bf.createBinding(fileTable,"selectedItems", revisionTable, "elements",
        new BindingConvertor<List<UIRepositoryObject>, UIRepositoryObjectRevisions>() {
          @Override
          public UIRepositoryObjectRevisions sourceToTarget(List<UIRepositoryObject> ro) {
            UIRepositoryObjectRevisions revisions = new UIRepositoryObjectRevisions();
            
            if(ro==null){
              return null;
            }
            if (ro.size()<=0){
              return null;
            }
            if (ro.get(0) instanceof UIRepositoryDirectory){
              return null;
            }
            try {
              UIRepositoryContent rc = (UIRepositoryContent)ro.get(0);
              revisions = rc.getRevisions();
              bf.setBindingType(Binding.Type.ONE_WAY);
              bf.createBinding(revisions,"children", revisionTable, "elements");
              
            } catch (Exception e) {
              // how do we handle exceptions in a binding? dialog here? 
              // TODO: handle exception
            }
            return revisions;
          }
          @Override
          public List<UIRepositoryObject> targetToSource(UIRepositoryObjectRevisions elements) {
            return null;
          }
        });
    }

    try {
      // Fires the population of the repository tree of folders. 
      directoryBinding.fireSourceChanged();
      revisionTreeBinding.fireSourceChanged();
      revisionLabelBinding.fireSourceChanged();
      if (revisionBinding != null){
        revisionBinding.fireSourceChanged();
      }
    } catch (Exception e) {
      System.out.println(e.getMessage()); e.printStackTrace();
    }
    
    this.addPropertyChangeListener(new PropertyChangeListener(){

      public void propertyChange(PropertyChangeEvent evt) {
      }
      
    });
    
  }

  public void setBindingFactory(BindingFactory bf) {
    this.bf = bf;
  }
  
  public String getName() {
    return "browseController";
  }
  
  public UIRepositoryDirectory getRepositoryDirectory() {
    return repositoryDirectory;
  }

  public void setRepositoryDirectory(UIRepositoryDirectory repositoryDirectory) {
    this.repositoryDirectory = repositoryDirectory;
  }
  
  public void setCallback(RepositoryExplorerCallback callback) {
    this.callback = callback;
  }

  public void expandAllFolders(){
    folderTree.expandAll();
  }
  
  public void collapseAllFolders(){
    folderTree.collapseAll();
  }

  public void openContent(){
    Collection<UIRepositoryContent> content = fileTable.getSelectedItems();
    UIRepositoryContent contentToOpen = content.iterator().next();
    if (callback != null) {
      if (callback.open(contentToOpen, null)){
        //TODO: fire request to close dialog
      }
    }
  }
  
  public void openRevision(){
    Collection<UIRepositoryContent> content = fileTable.getSelectedItems();
    UIRepositoryContent contentToOpen = content.iterator().next();

    Collection<UIRepositoryObjectRevision> revision = revisionTable.getSelectedItems();
    
    // TODO: Is it a requirement to allow opening multiple revisions? 
    UIRepositoryObjectRevision revisionToOpen = revision.iterator().next();
    if (callback != null) {
      if (callback.open(contentToOpen, revisionToOpen.getName())){
        //TODO: fire request to close dialog
      }
    }
  }
  
  public void createFolder() throws Exception{
    Collection<UIRepositoryDirectory> directory = folderTree.getSelectedItems();
    UIRepositoryDirectory selectedFolder = directory.iterator().next();
    if (selectedFolder==null){
      selectedFolder = repositoryDirectory;
    }
    Directory newDirectory = 
      selectedFolder.getRepository().createRepositoryDirectory(selectedFolder.getDirectory(), "new");
    
    System.out.println(newDirectory.getName() + ", " + newDirectory.getObjectId().getId());

    repositoryDirectory.contentChanged();
    directoryBinding.fireSourceChanged();
    folderTree.update();
    //this.firePropertyChange("selectedItems", toDelete, null);
  }

  public void deleteFolder() throws Exception{
    Collection<UIRepositoryDirectory> directory = folderTree.getSelectedItems();
    UIRepositoryDirectory toDelete = directory.iterator().next();
    toDelete.getRepository().deleteRepositoryDirectory(toDelete.getDirectory());
    
    repositoryDirectory.contentChanged();
    directoryBinding.fireSourceChanged();
    folderTree.update();

  }


}
