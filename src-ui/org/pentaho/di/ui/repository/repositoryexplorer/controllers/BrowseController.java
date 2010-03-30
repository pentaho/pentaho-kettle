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
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.ui.repository.repositoryexplorer.ContextChangeVetoer;
import org.pentaho.di.ui.repository.repositoryexplorer.ContextChangeVetoerCollection;
import org.pentaho.di.ui.repository.repositoryexplorer.ControllerInitializationException;
import org.pentaho.di.ui.repository.repositoryexplorer.IUISupportController;
import org.pentaho.di.ui.repository.repositoryexplorer.RepositoryExplorer;
import org.pentaho.di.ui.repository.repositoryexplorer.ContextChangeVetoer.TYPE;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryContent;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryDirectory;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryObject;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryObjectRevision;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryObjectRevisions;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryObjects;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.components.XulPromptBox;
import org.pentaho.ui.xul.components.XulTab;
import org.pentaho.ui.xul.containers.XulDeck;
import org.pentaho.ui.xul.containers.XulTabbox;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.dnd.DropEvent;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.swt.custom.DialogConstant;
import org.pentaho.ui.xul.util.XulDialogCallback;

/**
 *
 * This is the XulEventHandler for the browse panel of the repository explorer. It sets up the bindings for  
 * browse functionality.
 * 
 */
public class BrowseController extends AbstractXulEventHandler implements IUISupportController, IBrowseController {

  private ResourceBundle messages = new ResourceBundle() {

    @Override
    public Enumeration<String> getKeys() {
      return null;
    }

    @Override
    protected Object handleGetObject(String key) {
      return BaseMessages.getString(RepositoryExplorer.class, key);
    }

  };

  private XulTab historyTab;
  
  protected XulTree folderTree;

  protected XulTree fileTable;

  protected XulTree revisionTable;

  protected UIRepositoryDirectory repositoryDirectory;

  protected ContextChangeVetoerCollection contextChangeVetoers;

  protected BindingFactory bf;

  protected Binding directoryBinding;

  protected List<UIRepositoryDirectory> selectedFolderItems;

  protected List<UIRepositoryObject> selectedFileItems;

  protected List<UIRepositoryDirectory> repositoryDirectories;
  
  protected Repository repository;

  List<UIRepositoryObject> repositoryObjects;

  private MainController mainController;
  /**
   * Allows for lookup of a UIRepositoryDirectory by ObjectId. This allows the reuse of instances that are inside a UI
   * tree.
   */
  protected Map<ObjectId, UIRepositoryDirectory> dirMap;

  private XulTabbox  filePropertiesTabbox;
  
  public BrowseController() {
  }

  // begin PDI-3326 hack

  private void fireRepositoryDirectoryChange() {
    firePropertyChange("repositoryDirectory", null, repositoryDirectory);
  }
  
  // end PDI-3326 hack

  public void init(Repository repository) throws ControllerInitializationException {
    try {
      this.repository = repository; 
      
      mainController = (MainController) this.getXulDomContainer().getEventHandler("mainController");
      this.repositoryDirectory = new UIRepositoryDirectory(repository.loadRepositoryDirectoryTree(), repository);
      dirMap = new HashMap<ObjectId, UIRepositoryDirectory>();
      populateDirMap(repositoryDirectory);

      bf = new DefaultBindingFactory();
      bf.setDocument(this.getXulDomContainer().getDocumentRoot());

      createBindings();
    } catch (Exception e) {
      throw new ControllerInitializationException(e);
    }
  }

  protected void createBindings() {
    historyTab = (XulTab) document.getElementById("history"); //$NON-NLS-1$     
    folderTree = (XulTree) document.getElementById("folder-tree"); //$NON-NLS-1$
    fileTable = (XulTree) document.getElementById("file-table"); //$NON-NLS-1$ 
    filePropertiesTabbox = (XulTabbox) document.getElementById("file-properties-tabs"); //$NON-NLS-1$
    
    if (!repositoryDirectory.isVisible()) {
      folderTree.setHiddenrootnode(true);
    } else {
      folderTree.setHiddenrootnode(false);
    }
    
    // begin PDI-3326 hack
    
    PropertyChangeListener childrenListener = new PropertyChangeListener() {

      public void propertyChange(PropertyChangeEvent evt) {
        fireRepositoryDirectoryChange();
      }

    };

    repositoryDirectory.addPropertyChangeListener("children", childrenListener);
    
    // end PDI-3326 hack
    
    directoryBinding = createDirectoryBinding();

    // Bind the selected index from the folder tree to the list of repository objects in the file table. 
    bf.setBindingType(Binding.Type.ONE_WAY);

    bf.createBinding(folderTree, "selectedItems", this, "selectedFolderItems"); //$NON-NLS-1$  //$NON-NLS-2$
    bf.createBinding(this, "repositoryDirectories", fileTable, "elements", //$NON-NLS-1$  //$NON-NLS-2$
        new BindingConvertor<List<UIRepositoryDirectory>, UIRepositoryObjects>() {
          @Override
          public UIRepositoryObjects sourceToTarget(List<UIRepositoryDirectory> rd) {
            UIRepositoryObjects listOfObjects = new UIRepositoryObjects();

            if (rd == null) {
              return null;
            }
            if (rd.size() <= 0) {
              return null;
            }
            try {
              listOfObjects = rd.get(0).getRepositoryObjects();
            } catch (KettleException e) {
              // convert to runtime exception so it bubbles up through the UI
              throw new RuntimeException(e);
            }
            bf.setBindingType(Binding.Type.ONE_WAY);
            bf.createBinding(listOfObjects, "children", fileTable, "elements"); //$NON-NLS-1$  //$NON-NLS-2$
            return listOfObjects;
          }

          @Override
          public List<UIRepositoryDirectory> targetToSource(UIRepositoryObjects elements) {
            return null;
          }
        });

    // bindings can be added here in subclasses
    doCreateBindings();
    
    try {
      // Fires the population of the repository tree of folders. 
      directoryBinding.fireSourceChanged();
    } catch (Exception e) {
      // convert to runtime exception so it bubbles up through the UI
      throw new RuntimeException(e);
    }

    if (repositoryDirectory.isRevisionsSupported()) {
      createRevisionBindings();
    }
    
    try {
      // Set the initial selected directory as the users home directory
      RepositoryDirectory homeDir = repository.getUserHomeDirectory();
      int currentDir = 0;
      String[] homePath = homeDir.getPathArray();
      if(homePath != null) {
        UIRepositoryDirectory tempRoot = repositoryDirectory;
    
        // Check to see if the first item in homePath is the root directory
        if(homePath.length > 0 && tempRoot.getName().equalsIgnoreCase(homePath[currentDir])) {
          if(homePath.length == 1) {
            // The home directory is home root
            setSelectedFolderItems(Arrays.asList(tempRoot));
          }
          // We have used the first element. Increment to the next
          currentDir++;
        }
        
        // Traverse the tree until we find our destination
        for(; currentDir < homePath.length; currentDir++) {
          for(UIRepositoryObject uiObj : tempRoot.getChildren()) {
            if(uiObj instanceof UIRepositoryDirectory) {
              if(uiObj.getName().equalsIgnoreCase(homePath[currentDir])) {
                // We have a match. Let's move on to the next
                tempRoot = (UIRepositoryDirectory)uiObj;
                break;
              }
            }
          }
        }
        // If we have traversed as many directories as there are in the path, we have found the directory
        if(homePath.length == currentDir) {
          setSelectedFolderItems(Arrays.asList(tempRoot));
          folderTree.setSelectedItems(this.selectedFolderItems);
        }
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  protected void doCreateBindings() {}

  protected Binding createDirectoryBinding() {
    bf.setBindingType(Binding.Type.ONE_WAY);
    return bf.createBinding(this, "repositoryDirectory", folderTree, "elements"); //$NON-NLS-1$  //$NON-NLS-2$
  }

  private void createRevisionBindings() {
    revisionTable = (XulTree) document.getElementById("revision-table"); //$NON-NLS-1$
    bf.setBindingType(Binding.Type.ONE_WAY);
    Binding revisionTreeBinding = bf.createBinding(repositoryDirectory, "revisionsSupported", "revision-table", //$NON-NLS-1$ //$NON-NLS-2$
        "!disabled"); //$NON-NLS-1$
    Binding revisionLabelBinding = bf.createBinding(repositoryDirectory, "revisionsSupported", "revision-label", //$NON-NLS-1$ //$NON-NLS-2$
        "!disabled"); //$NON-NLS-1$

    BindingConvertor<int[], Boolean> forButtons = new BindingConvertor<int[], Boolean>() {

      @Override
      public Boolean sourceToTarget(int[] value) {
        return value != null && !(value.length <= 0);
      }

      @Override
      public int[] targetToSource(Boolean value) {
        return null;
      }
    };

    Binding buttonBinding = bf.createBinding(revisionTable, "selectedRows", "revision-open", "!disabled", forButtons); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    //bf.createBinding(revisionTable,"selectedRows", "revision-remove", "!disabled", forButtons);

    Binding revisionBinding = null;

    bf.setBindingType(Binding.Type.ONE_WAY);
    bf.createBinding(folderTree, "selectedItems", this, "historyTabVisibility"); //$NON-NLS-1$  //$NON-NLS-2$

    bf.setBindingType(Binding.Type.ONE_WAY);
    BindingConvertor<List<UIRepositoryObject>, Boolean> checkIfMultipleItemsAreSelected = new BindingConvertor<List<UIRepositoryObject>, Boolean>() {

      @Override
      public Boolean sourceToTarget(List<UIRepositoryObject> value) {
        return value != null && value.size() == 1 && value.get(0) != null;
      }

      @Override
      public List<UIRepositoryObject> targetToSource(Boolean value) {
        return null;
      }
    };
    bf.createBinding(fileTable, "selectedItems", "file-context-rename", "!disabled", checkIfMultipleItemsAreSelected); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    bf.createBinding(fileTable, "selectedItems", "file-context-delete", "!disabled", checkIfMultipleItemsAreSelected); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    bf.createBinding(fileTable, "selectedItems", this, "selectedFileItems"); //$NON-NLS-1$ //$NON-NLS-2$
    revisionBinding = bf.createBinding(this, "repositoryObjects", revisionTable, "elements", //$NON-NLS-1$ //$NON-NLS-2$
        new BindingConvertor<List<UIRepositoryObject>, UIRepositoryObjectRevisions>() {
          @Override
          public UIRepositoryObjectRevisions sourceToTarget(List<UIRepositoryObject> ro) {
            UIRepositoryObjectRevisions revisions = new UIRepositoryObjectRevisions();

            if (ro == null) {
              return null;
            }
            if (ro.size() <= 0) {
              return null;
            }
            if (ro.get(0) instanceof UIRepositoryDirectory) {
              historyTab.setVisible(false);
              filePropertiesTabbox.setSelectedIndex(0);
              return null;
            }
            try {
              UIRepositoryContent rc = (UIRepositoryContent) ro.get(0);
              revisions = rc.getRevisions();
              bf.setBindingType(Binding.Type.ONE_WAY);
              bf.createBinding(revisions, "children", revisionTable, "elements"); //$NON-NLS-1$ //$NON-NLS-2$

            } catch (KettleException e) {
              // convert to runtime exception so it bubbles up through the UI
              throw new RuntimeException(e);
            }
            historyTab.setVisible(true);
            return revisions;
          }

          @Override
          public List<UIRepositoryObject> targetToSource(UIRepositoryObjectRevisions elements) {
            return null;
          }
        });

    try {
      revisionTreeBinding.fireSourceChanged();
      revisionLabelBinding.fireSourceChanged();
      buttonBinding.fireSourceChanged();
      revisionBinding.fireSourceChanged();
    } catch (Exception e) {
      // convert to runtime exception so it bubbles up through the UI
      throw new RuntimeException(e);
    }

  }

  public <T> void setHistoryTabVisibility(Collection<T> items) {
      historyTab.setVisible(false);
      filePropertiesTabbox.setSelectedIndex(0);
  }

  public String getName() {
    return "browseController"; //$NON-NLS-1$
  }

  public UIRepositoryDirectory getRepositoryDirectory() {
    return repositoryDirectory;
  }
  
  protected void populateDirMap(UIRepositoryDirectory repDir) {
    dirMap.put(repDir.getObjectId(), repDir);
    for (UIRepositoryObject obj : repDir.getChildren()) {
      if (obj instanceof UIRepositoryDirectory) {
        populateDirMap((UIRepositoryDirectory) obj);
      }
    }
  }

  public void expandAllFolders() {
    folderTree.expandAll();
  }

  public void collapseAllFolders() {
    folderTree.collapseAll();
  }

  public void openContent() {
    Collection<UIRepositoryObject> content = fileTable.getSelectedItems();
    openContent(content.toArray());
  }

  public void openContent(Object[] items) {
    if ((items != null) && (items.length > 0)) {
      for (Object o : items) {
        if (o instanceof UIRepositoryDirectory) {
          ((UIRepositoryDirectory) o).toggleExpanded();
          List<Object> selectedFolder = new ArrayList<Object>();
          selectedFolder.add(o);
          folderTree.setSelectedItems(selectedFolder);
        } else if ((mainController != null && mainController.getCallback() != null)
            && (o instanceof UIRepositoryContent)) {
          if (mainController.getCallback().open((UIRepositoryContent) o, null)) {
            //TODO: fire request to close dialog
          }
        }
      }
    }
  }

  public void renameContent() throws Exception {
    Collection<UIRepositoryContent> content = fileTable.getSelectedItems();
    UIRepositoryObject contentToRename = content.iterator().next();
    renameRepositoryObject(contentToRename);
    if (contentToRename instanceof UIRepositoryDirectory) {
      directoryBinding.fireSourceChanged();
    }
  }

  public void deleteContent() throws Exception {
    Collection<UIRepositoryObject> content = fileTable.getSelectedItems();
    UIRepositoryObject toDelete = content.iterator().next();
    toDelete.delete();
    if (toDelete instanceof UIRepositoryDirectory) {
      directoryBinding.fireSourceChanged();
    }
  }

  public void openRevision() {
    Collection<UIRepositoryContent> content = fileTable.getSelectedItems();
    UIRepositoryContent contentToOpen = content.iterator().next();

    Collection<UIRepositoryObjectRevision> revision = revisionTable.getSelectedItems();

    // TODO: Is it a requirement to allow opening multiple revisions? 
    UIRepositoryObjectRevision revisionToOpen = revision.iterator().next();
    if (mainController != null && mainController.getCallback() != null) {
      if (mainController.getCallback().open(contentToOpen, revisionToOpen.getName())) {
        //TODO: fire request to close dialog
      }
    }
  }

  public void restoreRevision() {
    try {
      Collection<UIRepositoryContent> content = fileTable.getSelectedItems();
      final UIRepositoryContent contentToRestore = content.iterator().next();

      Collection<UIRepositoryObjectRevision> versions = revisionTable.getSelectedItems();
      final UIRepositoryObjectRevision versionToRestore = versions.iterator().next();

      XulPromptBox commitPrompt = RepositoryExplorer.promptCommitComment(document, messages, null);

      commitPrompt.addDialogCallback(new XulDialogCallback<String>() {
        public void onClose(XulComponent component, Status status, String value) {

          if (!status.equals(Status.CANCEL)) {
            try {
              contentToRestore.restoreVersion(versionToRestore, value);
            } catch (Exception e) {
              // convert to runtime exception so it bubbles up through the UI
              throw new RuntimeException(e);
            }
          }
        }

        public void onError(XulComponent component, Throwable err) {
          throw new RuntimeException(err);
        }
      });

      commitPrompt.open();
    } catch (Exception e) {
      throw new RuntimeException(new KettleException(e));
    }
  }

  private String newName = null;

  public void createFolder() throws Exception {

    // First, ask for a name for the folder
    XulPromptBox prompt = promptForName(null);
    prompt.addDialogCallback(new XulDialogCallback<String>() {
      public void onClose(XulComponent component, Status status, String value) {
        newName = value;
      }

      public void onError(XulComponent component, Throwable err) {
        // TODO: Deal with errors
        System.out.println(err.getMessage());
      }
    });

    prompt.open();

    if (newName != null) {
      Collection<UIRepositoryDirectory> directory = folderTree.getSelectedItems();
      UIRepositoryDirectory selectedFolder = directory.iterator().next();
      if (selectedFolder == null) {
        selectedFolder = repositoryDirectory;
      }
      UIRepositoryDirectory newDirectory = selectedFolder.createFolder(newName);
      directoryBinding.fireSourceChanged();
      System.out.println(newDirectory.getName() + ", " + newDirectory.getObjectId().getId());
    }
    newName = null;
  }

  public void deleteFolder() throws Exception {
    Collection<UIRepositoryDirectory> directory = folderTree.getSelectedItems();
    UIRepositoryDirectory toDelete = directory.iterator().next();
    toDelete.delete();
    directoryBinding.fireSourceChanged();
  }

  public void renameFolder() throws Exception {
    Collection<UIRepositoryDirectory> directory = folderTree.getSelectedItems();
    final UIRepositoryDirectory toRename = directory.iterator().next();
    renameRepositoryObject(toRename);
    directoryBinding.fireSourceChanged();
  }

  private void renameRepositoryObject(final UIRepositoryObject object) throws XulException {
    XulPromptBox prompt = promptForName(object);
    prompt.addDialogCallback(new XulDialogCallback<String>() {
      public void onClose(XulComponent component, Status status, String value) {

        try {
          object.setName(value);
        } catch (Exception e) {
          // convert to runtime exception so it bubbles up through the UI
          throw new RuntimeException(e);
        }
        System.out.println("Component: " + component.getName());
        System.out.println("Status: " + status.name());
        System.out.println("Value: " + value);
      }

      public void onError(XulComponent component, Throwable err) {
        // TODO: Deal with errors
        System.out.println(err.getMessage());
      }
    });

    prompt.open();
  }

  private XulPromptBox promptForName(final UIRepositoryObject object) throws XulException {
    XulPromptBox prompt = (XulPromptBox) document.createElement("promptbox"); //$NON-NLS-1$
    String currentName = (object == null) ? messages.getString("BrowserController.NewFolder") //$NON-NLS-1$
        : object.getName();

    prompt.setTitle(messages.getString("BrowserController.Name").concat(currentName));//$NON-NLS-1$
    prompt.setButtons(new DialogConstant[] { DialogConstant.OK, DialogConstant.CANCEL });

    prompt.setMessage(messages.getString("BrowserController.NameLabel").concat(currentName));//$NON-NLS-1$
    prompt.setValue(currentName);
    return prompt;
  }

  // Object being dragged from the hierarchical folder tree 
  public void onDragFromGlobalTree(DropEvent event) {
    event.setAccepted(true);
  }

  // Object being dragged from the file listing table
  public void onDragFromLocalTable(DropEvent event) {
    event.setAccepted(true);
  }

  public void onDrop(DropEvent event) {
    boolean result = false;
    try {
      List<Object> dirList = new ArrayList<Object>();
      List<UIRepositoryObject> moveList = new ArrayList<UIRepositoryObject>();
      UIRepositoryDirectory targetDirectory = null;
      
      if (event.getDropParent() != null && event.getDropParent() instanceof UIRepositoryDirectory) {
        targetDirectory = (UIRepositoryDirectory) event.getDropParent();        
        if (event.getDataTransfer().getData().size() > 0) {
          for(Object o : event.getDataTransfer().getData()) {
            if (o instanceof UIRepositoryObject) {
              moveList.add((UIRepositoryObject) o);
  
              // Make sure only Folders are copied to the Directory Tree
              if (o instanceof UIRepositoryDirectory) {
                dirList.add(o);
              }
              result = true;
            }
          }
        }
      }
      
      if(result == true) {
        // Perform move
        for(UIRepositoryObject o : moveList) {
          o.move(targetDirectory);
        }
        // Set UI objects to appear in folder directory
        event.getDataTransfer().setData(dirList);
      }
    } catch (Exception e) {
      result = false;
      event.setAccepted(false);
      // convert to runtime exception so it bubbles up through the UI
      throw new RuntimeException(e);
    }
    
    event.setAccepted(result);
  }
  
  public void onDoubleClick(Object[] selectedItems) {
    openContent(selectedItems);
  }

  public List<UIRepositoryDirectory> getSelectedFolderItems() {
    return selectedFolderItems;
  }

  public void setSelectedFolderItems(List<UIRepositoryDirectory> selectedFolderItems) {
    if (!compareFolderList(selectedFolderItems, this.selectedFolderItems)) {
      List<TYPE> pollResults = pollContextChangeVetoResults();
      if (!contains(TYPE.CANCEL, pollResults)) {
        this.selectedFolderItems = selectedFolderItems;
        setRepositoryDirectories(selectedFolderItems);
      } else if (contains(TYPE.CANCEL, pollResults)) {
        folderTree.setSelectedItems(this.selectedFolderItems);
      }
    }
  }

  public List<UIRepositoryObject> getSelectedFileItems() {
    return selectedFileItems;
  }

  public void setSelectedFileItems(List<UIRepositoryObject> selectedFileItems) {
    if (!compareFileList(selectedFileItems, this.selectedFileItems)) {
      List<TYPE> pollResults = pollContextChangeVetoResults();
      if (!contains(TYPE.CANCEL, pollResults)) {
        this.selectedFileItems = selectedFileItems;
        setRepositoryObjects(selectedFileItems);
      } else if (contains(TYPE.CANCEL, pollResults)) {
        fileTable.setSelectedItems(this.selectedFileItems);
      }
    }
  }

  public void setRepositoryObjects(List<UIRepositoryObject> selectedFileItems) {
    this.repositoryObjects = selectedFileItems;
    firePropertyChange("repositoryObjects", null, selectedFileItems);//$NON-NLS-1$
  }

  public List<UIRepositoryObject> getRepositoryObjects() {
    return repositoryObjects;
  }

  public List<UIRepositoryDirectory> getRepositoryDirectories() {
    return repositoryDirectories;
  }

  public void setRepositoryDirectories(List<UIRepositoryDirectory> selectedFolderItems) {
    this.repositoryDirectories = selectedFolderItems;
    firePropertyChange("repositoryDirectories", null, selectedFolderItems); //$NON-NLS-1$
  }

  public void addContextChangeVetoer(ContextChangeVetoer listener) {
    if (contextChangeVetoers == null) {
      contextChangeVetoers = new ContextChangeVetoerCollection();
    }
    contextChangeVetoers.add(listener);
  }

  public void removeContextChangeVetoer(ContextChangeVetoer listener) {
    if (contextChangeVetoers != null) {
      contextChangeVetoers.remove(listener);
    }
  }

  private boolean contains(TYPE type, List<TYPE> typeList) {
    for (TYPE t : typeList) {
      if (t.equals(type)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Fire all current {@link ContextChangeVetoer}.
   * Every on who has added their self as a vetoer has a change to vote on what
   * should happen. 
   */
  List<TYPE> pollContextChangeVetoResults() {
    if (contextChangeVetoers != null) {
      return contextChangeVetoers.fireContextChange();
    } else {
      List<TYPE> returnValue = new ArrayList<TYPE>();
      returnValue.add(TYPE.NO_OP);
      return returnValue;
    }
  }

  boolean compareFolderList(List<UIRepositoryDirectory> rd1, List<UIRepositoryDirectory> rd2) {
    if (rd1 != null && rd2 != null) {
      if (rd1.size() != rd2.size()) {
        return false;
      }
      for (int i = 0; i < rd1.size(); i++) {
        if (!rd1.get(i).getName().equals(rd2.get(i).getName())) {
          return false;
        }
      }
    } else {
      return false;
    }
    return true;
  }

  boolean compareFileList(List<UIRepositoryObject> ro1, List<UIRepositoryObject> ro2) {
    if (ro1 != null && ro2 != null) {
      if (ro1.size() != ro2.size()) {
        return false;
      }
      for (int i = 0; i < ro1.size(); i++) {
        if (!ro1.get(i).getName().equals(ro2.get(i).getName())) {
          return false;
        }
      }
    } else {
      return false;
    }
    return true;
  }
}
