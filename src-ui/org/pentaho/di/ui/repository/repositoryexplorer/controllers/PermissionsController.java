/*
 * This/./// m    ml;./;
 * ;/
 *
 *
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.ObjectAcl;
import org.pentaho.di.repository.ObjectPermission;
import org.pentaho.di.repository.RepositoryUserInterface;
import org.pentaho.di.ui.repository.repositoryexplorer.AccessDeniedException;
import org.pentaho.di.ui.repository.repositoryexplorer.ContextChangeListener;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryContent;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryDirectory;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryObject;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryObjectAcl;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryObjectAclModel;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryObjectAcls;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryRole;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryUser;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.components.XulCheckbox;
import org.pentaho.ui.xul.components.XulConfirmBox;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.components.XulMessageBox;
import org.pentaho.ui.xul.components.XulPromptBox;
import org.pentaho.ui.xul.components.XulRadio;
import org.pentaho.ui.xul.containers.XulDeck;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.containers.XulListbox;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.util.XulDialogCallback;

/**
 *
 * This is the XulEventHandler for the browse panel of the repository explorer. It sets up the bindings for
 * browse functionality.
 *
 */
public class PermissionsController extends AbstractXulEventHandler implements ContextChangeListener {

  private static final int NO_ACL = 0;

  private static final int ACL = 1;

  private XulDeck aclDeck;

  private XulListbox userRoleList;

  private XulListbox availableUserList;

  private XulListbox availableRoleList;

  private XulListbox selectedUserList;

  private XulListbox selectedRoleList;

  private UIRepositoryDirectory repositoryDirectory;

  private XulCheckbox createCheckbox;

  private XulCheckbox updateCheckbox;

  private XulCheckbox readCheckbox;

  private XulCheckbox deleteCheckbox;

  private XulCheckbox inheritParentPermissionCheckbox;

  private XulButton addAclButton;

  private XulButton removeAclButton;

  private XulCheckbox modifyCheckbox;

  private XulDialog manageAclsDialog;

  private XulDialog applyAclConfirmationDialog;

  private XulButton assignUserButton;

  private XulButton unassignUserButton;

  private XulButton assignRoleButton;

  private XulButton unassignRoleButton;

  private XulRadio applyOnlyRadioButton;

  private XulRadio applyRecursiveRadioButton;

  private Binding securityBinding;

  private XulLabel fileFolderLabel;

  BindingFactory bf;
  private UIRepositoryObjectAcls viewAclsModel;
  UIRepositoryObjectAclModel manageAclsModel = null;
  XulConfirmBox confirmBox = null;
  XulMessageBox messageBox = null;
  
  List<UIRepositoryObject> repoObject = new ArrayList<UIRepositoryObject>();

  private RepositoryUserInterface rui;
  private BrowseController browseController;
  ObjectAcl acl;
  TYPE returnType;

  public PermissionsController() {
  }

  public void init() {
    try {
      confirmBox = (XulConfirmBox) document.createElement("confirmbox");
      messageBox = (XulMessageBox) document.createElement("messagebox");
      viewAclsModel = new UIRepositoryObjectAcls();
      manageAclsModel = new UIRepositoryObjectAclModel(viewAclsModel);
      browseController.addContextChangeListener(this);
    } catch (Exception e) {

    }
    createBindings();
  }

  private void createBindings() {
    fileFolderLabel = (XulLabel) document.getElementById("file-folder-name");
    aclDeck = (XulDeck) document.getElementById("acl-deck");
    // Permission Tab Binding
    userRoleList = (XulListbox) document.getElementById("user-role-list");
    createCheckbox = (XulCheckbox) document.getElementById("create-checkbox");
    updateCheckbox = (XulCheckbox) document.getElementById("update-checkbox");
    readCheckbox = (XulCheckbox) document.getElementById("read-checkbox");
    deleteCheckbox = (XulCheckbox) document.getElementById("delete-checkbox");

    inheritParentPermissionCheckbox = (XulCheckbox) document.getElementById("inherit-from-parent-permission-checkbox");
    modifyCheckbox = (XulCheckbox) document.getElementById("modify-checkbox");
    manageAclsDialog = (XulDialog) document.getElementById("manage-acls-dialog");
    addAclButton = (XulButton) document.getElementById("add-acl-button");
    removeAclButton = (XulButton) document.getElementById("remove-acl-button");

    // Add/Remove Acl Binding
    availableUserList = (XulListbox) document.getElementById("available-user-list");
    selectedUserList = (XulListbox) document.getElementById("selected-user-list");
    availableRoleList = (XulListbox) document.getElementById("available-role-list");
    selectedRoleList = (XulListbox) document.getElementById("selected-role-list");

    assignRoleButton = (XulButton) document.getElementById("assign-role");
    unassignRoleButton = (XulButton) document.getElementById("unassign-role");
    assignUserButton = (XulButton) document.getElementById("assign-user");
    unassignUserButton = (XulButton) document.getElementById("unassign-user");

    applyAclConfirmationDialog = (XulDialog) document.getElementById("apply-acl-confirmation-dialog");
    applyOnlyRadioButton = (XulRadio) document.getElementById("apply-only-radio-button");
    applyRecursiveRadioButton = (XulRadio) document.getElementById("apply-recursive-radio-button");

    // Binding the model user or role list to the ui user or role list
    bf.setBindingType(Binding.Type.ONE_WAY);
    bf.createBinding(manageAclsModel, "availableUserList", availableUserList, "elements");
    bf.createBinding(manageAclsModel, "selectedUserList", selectedUserList, "elements");
    bf.createBinding(manageAclsModel, "availableRoleList", availableRoleList, "elements");
    bf.createBinding(manageAclsModel, "selectedRoleList", selectedRoleList, "elements");
    
    // indicesToObjectsConverter convert the selected indices to the list of objects and vice versa
    BindingConvertor<int[], List<UIRepositoryObjectAcl>> indicesToObjectsConverter = new BindingConvertor<int[], List<UIRepositoryObjectAcl>>() {

      @Override
      public int[] targetToSource(List<UIRepositoryObjectAcl> acls) {
        if (acls != null) {
          int i=0;
          int[] retVal = new int[acls.size()];
          for (UIRepositoryObjectAcl acl : acls) {
            retVal[i++] = viewAclsModel.getAceIndex(acl.getAce());
          }
          return retVal;
        }
        return null;
      }

      @Override
      public List<UIRepositoryObjectAcl> sourceToTarget(int[] indices) {
        if (indices != null && indices.length > 0) {
          List<UIRepositoryObjectAcl> retVal = new ArrayList<UIRepositoryObjectAcl>();
          for (int i = 0; i < indices.length; i++) {
            retVal.add(new UIRepositoryObjectAcl(viewAclsModel.getAceAtIndex(indices[i])));
          }
          return retVal;
        }
        return null;
      }

    };

    // indexToAvalableUserConverter convert the selected indices to the list of objects and vice versa
    BindingConvertor<int[], List<UIRepositoryUser>> indexToAvalableUserConverter = new BindingConvertor<int[], List<UIRepositoryUser>>() {

      @Override
      public List<UIRepositoryUser> sourceToTarget(int[] indices) {
        List<UIRepositoryUser> userList = new ArrayList<UIRepositoryUser>();
        for (int i = 0; i < indices.length; i++) {
          userList.add(manageAclsModel.getAvailableUser(indices[i]));
        }
        return userList;
      }

      @Override
      public int[] targetToSource(List<UIRepositoryUser> userList) {
        int[] indices = new int[userList.size()];
        int i = 0;
        for (UIRepositoryUser user : userList) {
          indices[i++] = manageAclsModel.getAvailableUserIndex(user);
        }
        return indices;
      }

    };

    BindingConvertor<int[], List<UIRepositoryRole>> indexToAvalableRoleConverter = new BindingConvertor<int[], List<UIRepositoryRole>>() {

      @Override
      public List<UIRepositoryRole> sourceToTarget(int[] indices) {
        List<UIRepositoryRole> roleList = new ArrayList<UIRepositoryRole>();
        for (int i = 0; i < indices.length; i++) {
          roleList.add(manageAclsModel.getAvailableRole(indices[i]));
        }
        return roleList;
      }

      @Override
      public int[] targetToSource(List<UIRepositoryRole> roleList) {
        int[] indices = new int[roleList.size()];
        int i = 0;
        for (UIRepositoryRole role : roleList) {
          indices[i++] = manageAclsModel.getAvailableRoleIndex(role);
        }
        return indices;
      }
    };

    BindingConvertor<int[], List<UIRepositoryObjectAcl>> indexToSelectedUserConverter = new BindingConvertor<int[], List<UIRepositoryObjectAcl>>() {

      @Override
      public List<UIRepositoryObjectAcl> sourceToTarget(int[] indices) {
        List<UIRepositoryObjectAcl> userList = new ArrayList<UIRepositoryObjectAcl>();
        for (int i = 0; i < indices.length; i++) {
          userList.add(manageAclsModel.getSelectedUser(indices[i]));
        }
        return userList;
      }

      @Override
      public int[] targetToSource(List<UIRepositoryObjectAcl> userList) {
        int[] indices = new int[userList.size()];
        int i = 0;
        for (UIRepositoryObjectAcl user : userList) {
          indices[i++] = manageAclsModel.getSelectedUserIndex(user);
        }
        return indices;
      }

    };

    BindingConvertor<int[], List<UIRepositoryObjectAcl>> indexToSelctedRoleConverter = new BindingConvertor<int[], List<UIRepositoryObjectAcl>>() {

      @Override
      public List<UIRepositoryObjectAcl> sourceToTarget(int[] indices) {
        List<UIRepositoryObjectAcl> roleList = new ArrayList<UIRepositoryObjectAcl>();
        for (int i = 0; i < indices.length; i++) {
          roleList.add(manageAclsModel.getSelectedRole(indices[i]));
        }
        return roleList;
      }

      @Override
      public int[] targetToSource(List<UIRepositoryObjectAcl> roleList) {
        int[] indices = new int[roleList.size()];
        int i = 0;
        for (UIRepositoryObjectAcl role : roleList) {
          indices[i++] = manageAclsModel.getSelectedRoleIndex(role);
        }
        return indices;
      }
    };

    // Binding betwee the selected incides of the lists to the mode list objects
    bf.setBindingType(Binding.Type.BI_DIRECTIONAL);

    bf.createBinding(availableUserList, "selectedIndices", manageAclsModel, "selectedAvailableUsers",
        indexToAvalableUserConverter);
    bf.createBinding(selectedUserList, "selectedIndices", manageAclsModel, "selectedAssignedUsers",
        indexToSelectedUserConverter);
    bf.createBinding(availableRoleList, "selectedIndices", manageAclsModel, "selectedAvailableRoles",
        indexToAvalableRoleConverter);
    bf.createBinding(selectedRoleList, "selectedIndices", manageAclsModel, "selectedAssignedRoles",
        indexToSelctedRoleConverter);

    // accumulatorButtonConverter determine whether to enable of disable the accumulator buttons
    BindingConvertor<Integer, Boolean> accumulatorButtonConverter = new BindingConvertor<Integer, Boolean>() {

      @Override
      public Boolean sourceToTarget(Integer value) {
        if (value != null && value >= 0) {
          return true;
        }
        return false;
      }

      @Override
      public Integer targetToSource(Boolean value) {
        // TODO Auto-generated method stub
        return null;
      }
    };
    bf.setBindingType(Binding.Type.ONE_WAY);
    bf.createBinding(selectedUserList, "selectedIndex", manageAclsModel, "userUnassignmentPossible",
        accumulatorButtonConverter);
    bf
        .createBinding(availableUserList, "selectedIndex", manageAclsModel, "userAssignmentPossible",
            accumulatorButtonConverter);
    bf.createBinding(manageAclsModel, "userUnassignmentPossible", unassignUserButton, "!disabled");
    bf.createBinding(manageAclsModel, "userAssignmentPossible", assignUserButton, "!disabled");

    bf.createBinding(selectedRoleList, "selectedIndex", manageAclsModel, "roleUnassignmentPossible",
        accumulatorButtonConverter);
    bf
        .createBinding(availableRoleList, "selectedIndex", manageAclsModel, "roleAssignmentPossible",
            accumulatorButtonConverter);

    bf.createBinding(manageAclsModel, "roleUnassignmentPossible", unassignRoleButton, "!disabled");
    bf.createBinding(manageAclsModel, "roleAssignmentPossible", assignRoleButton, "!disabled");

    bf.setBindingType(Binding.Type.ONE_WAY);

    // Binding between the selected repository objects and the user role list for acls
    securityBinding = bf.createBinding(getBrowseController(), "repositoryObjects", userRoleList, "elements",
        new BindingConvertor<List<UIRepositoryObject>, List<UIRepositoryObjectAcl>>() {
          @Override
          public List<UIRepositoryObjectAcl> sourceToTarget(List<UIRepositoryObject> ro) {
            if (ro == null) {
              return null;
            }
            if (ro.size() <= 0) {
              return null;
            }
            setSelectedRepositoryObject(ro);
            viewAclsModel.setRemoveEnabled(false);
            uncheckAllPermissionBox();
            if (ro.get(0) instanceof UIRepositoryDirectory) {
              UIRepositoryDirectory rd = (UIRepositoryDirectory) ro.get(0);
              try {
                rd.readAcls(viewAclsModel);
                fileFolderLabel.setValue(rd.getName());
                bf.setBindingType(Binding.Type.ONE_WAY);
                bf.createBinding(viewAclsModel, "acls", userRoleList, "elements");
              } catch (AccessDeniedException ade) {
                messageBox.setTitle("Error");
                messageBox.setAcceptLabel("Ok");
                messageBox.setMessage("Unable to get acls information for " + rd.getName()+ "cause : " + ade.getLocalizedMessage());
                messageBox.open();
              } catch (Exception e) {
                messageBox.setTitle("Error");
                messageBox.setAcceptLabel("Ok");
                messageBox.setMessage("Unable to get acls information for " + rd.getName()+ "cause : " + e.getLocalizedMessage());
                messageBox.open();
              }

            } else if (ro.get(0) instanceof UIRepositoryContent) {
              UIRepositoryContent rc = (UIRepositoryContent) ro.get(0);
              try {
                fileFolderLabel.setValue(rc.getName());
                rc.readAcls(viewAclsModel);
                bf.setBindingType(Binding.Type.ONE_WAY);
                bf.createBinding(viewAclsModel, "acls", userRoleList, "elements");
              } catch (AccessDeniedException ade) {
                messageBox.setTitle("Error");
                messageBox.setAcceptLabel("Ok");
                messageBox.setMessage("Unable to get acls information for " + rc.getName()+ "cause : " + ade.getLocalizedMessage());
                messageBox.open();
              } catch (Exception e) {
                messageBox.setTitle("Error");
                messageBox.setAcceptLabel("Ok");
                messageBox.setMessage("Unable to get acls information for " + rc.getName()+ "cause : " + e.getLocalizedMessage());
                messageBox.open();
              }
            }
            aclDeck.setSelectedIndex(ACL);
            return viewAclsModel.getAcls();
          }

          @Override
          public List<UIRepositoryObject> targetToSource(List<UIRepositoryObjectAcl> elements) {
            return null;
          }
        });
    
    
    
    bf.setBindingType(Binding.Type.BI_DIRECTIONAL);

    // Binding Add Remove button to the inherit check box. If the checkbox is checked that disable add remove
    bf.createBinding(viewAclsModel, "entriesInheriting", inheritParentPermissionCheckbox, "checked");
    // Binding the selected indices of acl list to the list of acl objects in the mode
    bf.createBinding(userRoleList, "selectedIndices", viewAclsModel, "selectedAclList", indicesToObjectsConverter);
    
    bf.setBindingType(Binding.Type.ONE_WAY);
    // Only enable add Acl button if the entries checkbox is unchecked
    bf.createBinding(viewAclsModel, "entriesInheriting", addAclButton, "disabled");
    // Only enable remove Acl button if the entries checkbox is unchecked and acl is selected from the list
    bf.createBinding(viewAclsModel, "removeEnabled", removeAclButton, "!disabled");
    bf.createBinding(viewAclsModel, "removeEnabled", createCheckbox, "!disabled");
    bf.createBinding(viewAclsModel, "removeEnabled", updateCheckbox, "!disabled");
    bf.createBinding(viewAclsModel, "removeEnabled", readCheckbox, "!disabled");
    bf.createBinding(viewAclsModel, "removeEnabled", deleteCheckbox, "!disabled");
    bf.createBinding(viewAclsModel, "removeEnabled", modifyCheckbox, "!disabled");
    bf.setBindingType(Binding.Type.ONE_WAY);
    // Binding when the user select from the list
    bf.createBinding(userRoleList, "selectedItem", this, "recipientChanged");
    // Binding the selected folder or folder to the ACL Deck
    bf.createBinding(getBrowseController(), "repositoryDirectories", this, "switchAclDeck");
    // Setting the default Deck to show no permission
    aclDeck.setSelectedIndex(NO_ACL);
    try {
      if (securityBinding != null) {
        securityBinding.fireSourceChanged();
      }
    } catch (Exception e) {
      System.out.println(e.getMessage());
      e.printStackTrace();
    }
  }

  public void setSelectedRepositoryObject(List<UIRepositoryObject> roList) {
    if (roList != null) {
      repoObject.clear();
      repoObject.addAll(roList);
    }
  }

  public void setBrowseController(BrowseController controller) {
   this.browseController = controller; 
  }
  public BrowseController getBrowseController() {
    return this.browseController;
  }
  public List<UIRepositoryObject> getSelectedRepositoryObject() {
    return repoObject;
  }

  public RepositoryUserInterface getRepositoryUserInterface() {
    return rui;
  }

  public void setRepositoryUserInterface(RepositoryUserInterface rui) {
    this.rui = rui;
  }

  public void setBindingFactory(BindingFactory bf) {
    this.bf = bf;
  }

  public String getName() {
    return "permissionsController";
  }

  public UIRepositoryDirectory getRepositoryDirectory() {
    return repositoryDirectory;
  }

  public void setRepositoryDirectory(UIRepositoryDirectory repositoryDirectory) {
    this.repositoryDirectory = repositoryDirectory;
  }

  /**
   * 
   * assignUsers method is call to add  selected user(s) to the assign users list
   */
  public void assignUsers() {
    manageAclsModel.assignUsers(Arrays.asList(availableUserList.getSelectedItems()));
  }
  
  /**
   * unassignUsers method is call to add  unselected user(s) from the assign users list
   */
  public void unassignUsers() {
    manageAclsModel.unassign(Arrays.asList(selectedUserList.getSelectedItems()));
  }
  
  /**
   * assignRoles method is call to add  selected role(s) to the assign roles list
   */
  public void assignRoles() {
    manageAclsModel.assignRoles(Arrays.asList(availableRoleList.getSelectedItems()));
  }

  /**
   * unassignRoles method is call to add  unselected role(s) from the assign roles list
   */
  public void unassignRoles() {
    manageAclsModel.unassign(Arrays.asList(selectedRoleList.getSelectedItems()));
  }

  public void showManageAclsDialog() throws Exception {
    try {
      manageAclsModel.setUserList(rui.getUsers(), rui.getRoles());
    } catch (KettleException ke) {
      messageBox.setTitle("Error");
      messageBox.setAcceptLabel("Ok");
      messageBox.setMessage("Unable to get user(s) or role(s)" + ke.getLocalizedMessage());
      messageBox.open();
    }
    manageAclsDialog.show();
  }

  public void closeManageAclsDialog() throws Exception {
    manageAclsModel.clear();
    manageAclsDialog.hide();
  }

  /**
   * updateAcls method is called when the user click ok on the manage acl dialog. It updates the selection to
   * the model
   * @throws Exception
   */
  public void updateAcls() throws Exception {
    manageAclsModel.updateSelectedAcls();
    closeManageAclsDialog();
  }

  /**
   * removeAcl method is called when the user select a or a list of acls to remove from the list.
   * It first display a confirmation box to the user asking to confirm the removal. If the user
   * selected ok, it deletes selected acls from the list
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  public void removeAcl() throws Exception {
    confirmBox.setTitle("Security");
    confirmBox.setMessage("You are about to remove selected acl(s). Do you want to continue?");
    confirmBox.setAcceptLabel("Ok");
    confirmBox.setCancelLabel("Cancel");
    confirmBox.addDialogCallback(new XulDialogCallback(){

      public void onClose(XulComponent sender, Status returnCode, Object retVal) {
         if(returnCode == Status.ACCEPT){
           viewAclsModel.removeSelectedAcls();
         } 
      }
      public void onError(XulComponent sender, Throwable t) {
        
      }
    });
    confirmBox.open();
  }

  /**
   * apply method is called when the user clicks the apply button on the UI
   */
  public void apply() {
    List<UIRepositoryObject> roList = getSelectedRepositoryObject();
    if (roList != null && roList.size() == 1 && (roList.get(0) instanceof UIRepositoryDirectory)) {
      applyAclConfirmationDialog.show();
    } else {
      applyOnObjectOnly(roList, false);
    }

  }

  /**
   * applyOnObjectOnly is called to save acl for a file object only
   * @param roList
   * @param hideDialog
   */
  private void applyOnObjectOnly(List<UIRepositoryObject> roList, boolean hideDialog) {
    try {
      if (roList.get(0) instanceof UIRepositoryDirectory) {
        UIRepositoryDirectory rd = (UIRepositoryDirectory) roList.get(0);
        rd.setAcls(viewAclsModel);
      } else {
        UIRepositoryContent rc = (UIRepositoryContent) roList.get(0);
        rc.setAcls(viewAclsModel);
      }
      if (hideDialog) {
        applyAclConfirmationDialog.hide();
      }
      viewAclsModel.setModelDirty(false);
      messageBox.setTitle("Success");
      messageBox.setAcceptLabel("Ok");
      messageBox.setMessage("Permission were applied successfully");
      messageBox.open();
    } catch (AccessDeniedException ade) {
      if (hideDialog) {
        applyAclConfirmationDialog.hide();
      }
      messageBox.setAcceptLabel("Ok");
      messageBox.setMessage(ade.getLocalizedMessage());
      messageBox.open();
    }
  }

  public void setApplyOnly() {
    applyOnlyRadioButton.setSelected(true);
    applyRecursiveRadioButton.setSelected(false);
  }

  public void setApplyRecursive() {
    applyOnlyRadioButton.setSelected(false);
    applyRecursiveRadioButton.setSelected(true);
  }

  /**
   * applyAcl is called to save the acls back to the repository
   * @throws Exception
   */
  public void applyAcl() throws Exception {
    // We will call the the server apply method that only applies this acls changes on the current object
    if (applyOnlyRadioButton.isSelected()) {
      List<UIRepositoryObject> roList = getSelectedRepositoryObject();
      applyOnObjectOnly(roList, true);
    } else {
      // TODO We will call the the server apply method that applies this acls changes on the current object and its children
      applyAclConfirmationDialog.hide();
      messageBox.setAcceptLabel("Ok");
      messageBox.setMessage("The functionality is not currently supported");
      messageBox.open();      
    }
  }

  public void closeApplyAclConfirmationDialog() {
    applyAclConfirmationDialog.hide();
  }

  public void confirmRemoveAcl() throws Exception {
    manageAclsDialog.hide();
  }

  public void setSwitchAclDeck(List<UIRepositoryDirectory> dirList) {
    if (dirList != null && dirList.size() > 0) {
      aclDeck.setSelectedIndex(NO_ACL);
    }
  }

  /*
   * The method is called when a user select an acl from the acl list. This method reads the current selected
   * acl and populates the UI with the details
   */
  public void setRecipientChanged(UIRepositoryObjectAcl acl) throws Exception {
    List<UIRepositoryObjectAcl> acls = new ArrayList<UIRepositoryObjectAcl>();
    acls.add(acl);
    viewAclsModel.setSelectedAclList(acls);
    uncheckAllPermissionBox();
    if (acl != null && acl.getPermissionSet() != null) {
      for (ObjectPermission permission : acl.getPermissionSet()) {
        if (permission.equals(ObjectPermission.ALL)) {
          checkAllPermissionBox();
          break;
        } else if (permission.equals(ObjectPermission.DELETE)) {
          deleteCheckbox.setChecked(true);
        } else if (permission.equals(ObjectPermission.READ)) {
          readCheckbox.setChecked(true);
        } else if (permission.equals(ObjectPermission.WRITE)) {
          createCheckbox.setChecked(true);
        } else if (permission.equals(ObjectPermission.EXECUTE)) {
          updateCheckbox.setChecked(true);
        } else if (permission.equals(ObjectPermission.WRITE_ACL)) {
          modifyCheckbox.setChecked(true);
        }
      }
    }
  }

  private boolean areAllPermissionBoxChecked() {
    return (deleteCheckbox.isChecked() && readCheckbox.isChecked() && createCheckbox.isChecked()
        && updateCheckbox.isChecked() && updateCheckbox.isChecked() && modifyCheckbox.isChecked());
  }

  private void uncheckAllPermissionBox() {
    deleteCheckbox.setChecked(false);
    readCheckbox.setChecked(false);
    createCheckbox.setChecked(false);
    updateCheckbox.setChecked(false);
    modifyCheckbox.setChecked(false);
  }

  private void checkAllPermissionBox() {
    deleteCheckbox.setChecked(true);
    readCheckbox.setChecked(true);
    createCheckbox.setChecked(true);
    updateCheckbox.setChecked(true);
    modifyCheckbox.setChecked(true);
  }

  /*
   * updatePermission method is called when the user checks or uncheck any permission checkbox.
   * This method updates the current model with the update value from the UI
   */
  public void updatePermission() {
    UIRepositoryObjectAcl acl = (UIRepositoryObjectAcl) userRoleList.getSelectedItem();
    if (acl != null) {
      EnumSet<ObjectPermission> permissions = acl.getPermissionSet();
      if (permissions == null) {
        permissions = EnumSet.noneOf(ObjectPermission.class);
      }
      if (areAllPermissionBoxChecked()) {
        permissions.add(ObjectPermission.ALL);
      } else {
        permissions.remove(ObjectPermission.ALL);
      }
      if (!areAllPermissionBoxChecked()) {
        if (deleteCheckbox.isChecked()) {
          permissions.add(ObjectPermission.DELETE);
        } else {
          permissions.remove(ObjectPermission.DELETE);
        }
        if (readCheckbox.isChecked()) {
          permissions.add(ObjectPermission.READ);
        } else {
          permissions.remove(ObjectPermission.READ);
        }
        if (createCheckbox.isChecked()) {
          permissions.add(ObjectPermission.WRITE);
        } else {
          permissions.remove(ObjectPermission.WRITE);
        }
        if (updateCheckbox.isChecked()) {
          permissions.add(ObjectPermission.EXECUTE);
        } else {
          permissions.remove(ObjectPermission.EXECUTE);
        }
        if (modifyCheckbox.isChecked()) {
          permissions.add(ObjectPermission.WRITE_ACL);
        } else {
          permissions.remove(ObjectPermission.WRITE_ACL);
        }
      }
      acl.setPermissionSet(permissions);
      viewAclsModel.updateAcl(acl);
    }
  }

  /*
   * If the user check or unchecks the inherit from parent checkbox, this method is called.
   */
  public void updateInheritFromParentPermission() {
    viewAclsModel.clear();
    viewAclsModel.setEntriesInheriting(inheritParentPermissionCheckbox.isChecked());
    if (inheritParentPermissionCheckbox.isChecked()) {
      uncheckAllPermissionBox();
    }
  }

  /*
   * (non-Javadoc)
   * @see org.pentaho.di.ui.repository.repositoryexplorer.ContextChangeListener#onContextChange()
   * This method is called whenever user change the folder or file selection
   */
  @SuppressWarnings("unchecked")
  public TYPE onContextChange() {
    if(viewAclsModel.isModelDirty()) {
      confirmBox.setTitle("Context Change Warning");
      confirmBox.setMessage("You are about to change the context. All changes will be lost. Do you want to continue?");
      confirmBox.setAcceptLabel("Yes");
      confirmBox.setCancelLabel("No");
      confirmBox.addDialogCallback(new XulDialogCallback(){
  
        public void onClose(XulComponent sender, Status returnCode, Object retVal) {
           if(returnCode == Status.ACCEPT){
             returnType = TYPE.OK;
             viewAclsModel.clear();
             viewAclsModel.setModelDirty(false);
           } else {
             returnType = TYPE.CANCEL;
           }
        }
        public void onError(XulComponent sender, Throwable t) {
          returnType = TYPE.NO_OP;
        }        
      });
      confirmBox.open();
    } else {
      returnType = TYPE.NO_OP;
    }
    return returnType;
  }
}
