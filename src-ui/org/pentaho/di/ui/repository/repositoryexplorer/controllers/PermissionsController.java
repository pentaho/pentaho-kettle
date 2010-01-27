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
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.components.XulCheckbox;
import org.pentaho.ui.xul.components.XulConfirmBox;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.components.XulRadio;
import org.pentaho.ui.xul.containers.XulDeck;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.containers.XulListbox;
import org.pentaho.ui.xul.containers.XulTree;
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

  private XulTree fileTable;

  private XulTree folderTree;

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

  private XulDialog removeAclConfirmationDialog;

  private XulDialog applyAclConfirmationDialog;

  private XulButton assignUserButton;

  private XulButton unassignUserButton;

  private XulButton assignRoleButton;

  private XulButton unassignRoleButton;

  private XulRadio applyOnlyRadioButton;

  private XulRadio applyRecursiveRadioButton;

  private Binding securityBinding;

  private XulDialog messageDialog;

  private XulLabel messageLabel;

  private XulLabel fileFolderLabel;

  BindingFactory bf;

  UIRepositoryObjectAclModel aclModel = null;

  List<UIRepositoryObject> repoObject = new ArrayList<UIRepositoryObject>();

  private RepositoryUserInterface rui;
  private BrowseController browseController;
  ObjectAcl acl;
  TYPE returnType;

  public PermissionsController() {
  }

  public void init() {
    try {
      aclModel = new UIRepositoryObjectAclModel();
      browseController.addContextChangeListener(this);
    } catch (Exception e) {

    }
    createBindings();
  }

  private void createBindings() {
    fileFolderLabel = (XulLabel) document.getElementById("file-folder-name");
    messageDialog = (XulDialog) document.getElementById("message-dialog");
    messageLabel = (XulLabel) document.getElementById("message-to-display");
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

    removeAclConfirmationDialog = (XulDialog) document.getElementById("remove-acl-confirmation-dialog");

    bf.setBindingType(Binding.Type.ONE_WAY);
    bf.createBinding(aclModel, "availableUserList", availableUserList, "elements");
    bf.createBinding(aclModel, "selectedUserList", selectedUserList, "elements");
    bf.createBinding(aclModel, "availableRoleList", availableRoleList, "elements");
    bf.createBinding(aclModel, "selectedRoleList", selectedRoleList, "elements");
    BindingConvertor<Object[], List<UIRepositoryObjectAcl>> arrayToListConverter = new BindingConvertor<Object[], List<UIRepositoryObjectAcl>>() {

      @Override
      public Object[] targetToSource(List<UIRepositoryObjectAcl> roles) {
        if (roles != null) {
          Object[] retVal = new Object[roles.size()];
          int i = 0;
          for (Object role : roles) {
            retVal[i++] = role;
          }
          return retVal;
        }
        return null;
      }

      @Override
      public List<UIRepositoryObjectAcl> sourceToTarget(Object[] roles) {
        if (roles != null) {
          List<UIRepositoryObjectAcl> retVal = new ArrayList<UIRepositoryObjectAcl>();
          for (int i = 0; i < roles.length; i++) {
            retVal.add((UIRepositoryObjectAcl) roles[i]);
          }
          return retVal;
        }
        return null;
      }

    };
    BindingConvertor<int[], List<UIRepositoryUser>> indexToAvalableUserConverter = new BindingConvertor<int[], List<UIRepositoryUser>>() {

      @Override
      public List<UIRepositoryUser> sourceToTarget(int[] indices) {
        List<UIRepositoryUser> userList = new ArrayList<UIRepositoryUser>();
        for (int i = 0; i < indices.length; i++) {
          userList.add(aclModel.getAvailableUser(indices[i]));
        }
        return userList;
      }

      @Override
      public int[] targetToSource(List<UIRepositoryUser> userList) {
        int[] indices = new int[userList.size()];
        int i = 0;
        for (UIRepositoryUser user : userList) {
          indices[i++] = aclModel.getAvailableUserIndex(user);
        }
        return indices;
      }

    };

    BindingConvertor<int[], List<UIRepositoryRole>> indexToAvalableRoleConverter = new BindingConvertor<int[], List<UIRepositoryRole>>() {

      @Override
      public List<UIRepositoryRole> sourceToTarget(int[] indices) {
        List<UIRepositoryRole> roleList = new ArrayList<UIRepositoryRole>();
        for (int i = 0; i < indices.length; i++) {
          roleList.add(aclModel.getAvailableRole(indices[i]));
        }
        return roleList;
      }

      @Override
      public int[] targetToSource(List<UIRepositoryRole> roleList) {
        int[] indices = new int[roleList.size()];
        int i = 0;
        for (UIRepositoryRole role : roleList) {
          indices[i++] = aclModel.getAvailableRoleIndex(role);
        }
        return indices;
      }
    };

    BindingConvertor<int[], List<UIRepositoryObjectAcl>> indexToSelectedUserConverter = new BindingConvertor<int[], List<UIRepositoryObjectAcl>>() {

      @Override
      public List<UIRepositoryObjectAcl> sourceToTarget(int[] indices) {
        List<UIRepositoryObjectAcl> userList = new ArrayList<UIRepositoryObjectAcl>();
        for (int i = 0; i < indices.length; i++) {
          userList.add(aclModel.getSelectedUser(indices[i]));
        }
        return userList;
      }

      @Override
      public int[] targetToSource(List<UIRepositoryObjectAcl> userList) {
        int[] indices = new int[userList.size()];
        int i = 0;
        for (UIRepositoryObjectAcl user : userList) {
          indices[i++] = aclModel.getSelectedUserIndex(user);
        }
        return indices;
      }

    };

    BindingConvertor<int[], List<UIRepositoryObjectAcl>> indexToSelctedRoleConverter = new BindingConvertor<int[], List<UIRepositoryObjectAcl>>() {

      @Override
      public List<UIRepositoryObjectAcl> sourceToTarget(int[] indices) {
        List<UIRepositoryObjectAcl> roleList = new ArrayList<UIRepositoryObjectAcl>();
        for (int i = 0; i < indices.length; i++) {
          roleList.add(aclModel.getSelectedRole(indices[i]));
        }
        return roleList;
      }

      @Override
      public int[] targetToSource(List<UIRepositoryObjectAcl> roleList) {
        int[] indices = new int[roleList.size()];
        int i = 0;
        for (UIRepositoryObjectAcl role : roleList) {
          indices[i++] = aclModel.getSelectedRoleIndex(role);
        }
        return indices;
      }
    };

    bf.setBindingType(Binding.Type.BI_DIRECTIONAL);

    bf.createBinding(availableUserList, "selectedIndices", aclModel, "selectedAvailableUsers",
        indexToAvalableUserConverter);
    bf.createBinding(selectedUserList, "selectedIndices", aclModel, "selectedAssignedUsers",
        indexToSelectedUserConverter);
    bf.createBinding(availableRoleList, "selectedIndices", aclModel, "selectedAvailableRoles",
        indexToAvalableRoleConverter);
    bf.createBinding(selectedRoleList, "selectedIndices", aclModel, "selectedAssignedRoles",
        indexToSelctedRoleConverter);
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
    bf.createBinding(selectedUserList, "selectedIndex", aclModel, "userUnassignmentPossible",
        accumulatorButtonConverter);
    bf
        .createBinding(availableUserList, "selectedIndex", aclModel, "userAssignmentPossible",
            accumulatorButtonConverter);
    bf.createBinding(aclModel, "userUnassignmentPossible", unassignUserButton, "!disabled");
    bf.createBinding(aclModel, "userAssignmentPossible", assignUserButton, "!disabled");

    bf.createBinding(selectedRoleList, "selectedIndex", aclModel, "roleUnassignmentPossible",
        accumulatorButtonConverter);
    bf
        .createBinding(availableRoleList, "selectedIndex", aclModel, "roleAssignmentPossible",
            accumulatorButtonConverter);

    bf.createBinding(aclModel, "roleUnassignmentPossible", unassignRoleButton, "!disabled");
    bf.createBinding(aclModel, "roleAssignmentPossible", assignRoleButton, "!disabled");

    bf.setBindingType(Binding.Type.ONE_WAY);

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
            aclModel.getSelectedAcls().setRemoveEnabled(false);
            uncheckAllPermissionBox();
            if (ro.get(0) instanceof UIRepositoryDirectory) {
              UIRepositoryDirectory rd = (UIRepositoryDirectory) ro.get(0);
              try {
                rd.readAcls(aclModel.getSelectedAcls());
                fileFolderLabel.setValue(rd.getName());
                bf.setBindingType(Binding.Type.ONE_WAY);
                bf.createBinding(aclModel.getSelectedAcls(), "acls", userRoleList, "elements");
              } catch (AccessDeniedException ade) {
                // Access is denied to retrieve acl information.
                // TODO We need to figure out whether to throw a dialog box or disable
                // the tab
              } catch (Exception e) {
                // how do we handle exceptions in a binding? dialog here?
                // TODO: handle exception
              }

            } else if (ro.get(0) instanceof UIRepositoryContent) {
              try {
                UIRepositoryContent rc = (UIRepositoryContent) ro.get(0);
                fileFolderLabel.setValue(rc.getName());
                rc.readAcls(aclModel.getSelectedAcls());
                bf.setBindingType(Binding.Type.ONE_WAY);
                bf.createBinding(aclModel.getSelectedAcls(), "acls", userRoleList, "elements");
              } catch (AccessDeniedException ade) {
                // Access is denied to retrieve acl information.
                // TODO We need to figure out whether to throw a dialog box or disable
                // the tab
              } catch (Exception e) {
                // how do we handle exceptions in a binding? dialog here?
                // TODO: handle exception
              }
            }
            aclDeck.setSelectedIndex(ACL);
            return aclModel.getSelectedAcls().getAcls();
          }

          @Override
          public List<UIRepositoryObject> targetToSource(List<UIRepositoryObjectAcl> elements) {
            return null;
          }
        });
    bf.setBindingType(Binding.Type.BI_DIRECTIONAL);
    // Binding Add Remove button to the inherit check box. If the checkbox is checked that disable add remove
    bf.createBinding(aclModel.getSelectedAcls(), "entriesInheriting", inheritParentPermissionCheckbox, "checked");
    bf
        .createBinding(userRoleList, "selectedItems", aclModel.getSelectedAcls(), "selectedAclList",
            arrayToListConverter);

    bf.setBindingType(Binding.Type.ONE_WAY);
    // Only enable add Acl button if the entries checkbox is unchecked
    bf.createBinding(aclModel.getSelectedAcls(), "entriesInheriting", addAclButton, "disabled");
    // Only enable remove Acl button if the entries checkbox is unchecked and acl is selected from the list
    bf.createBinding(aclModel.getSelectedAcls(), "removeEnabled", removeAclButton, "!disabled");

    bf.createBinding(aclModel.getSelectedAcls(), "removeEnabled", createCheckbox, "!disabled");
    bf.createBinding(aclModel.getSelectedAcls(), "removeEnabled", updateCheckbox, "!disabled");
    bf.createBinding(aclModel.getSelectedAcls(), "removeEnabled", readCheckbox, "!disabled");
    bf.createBinding(aclModel.getSelectedAcls(), "removeEnabled", deleteCheckbox, "!disabled");
    bf.createBinding(aclModel.getSelectedAcls(), "removeEnabled", modifyCheckbox, "!disabled");
    bf.setBindingType(Binding.Type.ONE_WAY);
    // Binding when the user select from the list
    bf.createBinding(userRoleList, "selectedItem", this, "recipientChanged");

    bf.createBinding(getBrowseController(), "repositoryDirectories", this, "switchAclDeck");

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

  public void assignUsers() {
    aclModel.assignUsers(Arrays.asList(availableUserList.getSelectedItems()));
  }

  public void unassignUsers() {
    aclModel.unassign(Arrays.asList(selectedUserList.getSelectedItems()));
  }

  public void assignRoles() {
    aclModel.assignRoles(Arrays.asList(availableRoleList.getSelectedItems()));
  }

  public void unassignRoles() {
    aclModel.unassign(Arrays.asList(selectedRoleList.getSelectedItems()));
  }

  public void showManageAclsDialog() throws Exception {
    try {
      aclModel.setUserList(rui.getUsers(), rui.getRoles());
    } catch (KettleException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    manageAclsDialog.show();
  }

  public void closeManageAclsDialog() throws Exception {
    aclModel.clear();
    manageAclsDialog.hide();
  }

  public void updateAcls() throws Exception {
    aclModel.updateSelectedAcls();
    closeManageAclsDialog();
  }

  public void removeAcl() throws Exception {
    UIRepositoryObjectAcls repositoryObjectacls = aclModel.getSelectedAcls();
    List<UIRepositoryObjectAcl> acls = repositoryObjectacls.getSelectedAclList();
    repositoryObjectacls.removeAcls(acls);
    closeRemoveAclConfirmationDialog();
  }

  public void apply() {
    List<UIRepositoryObject> roList = getSelectedRepositoryObject();
    if (roList != null && roList.size() == 1 && (roList.get(0) instanceof UIRepositoryDirectory)) {
      applyAclConfirmationDialog.show();
    } else {
      applyOnObjectOnly(roList, false);
    }

  }

  private void applyOnObjectOnly(List<UIRepositoryObject> roList, boolean hideDialog) {
    try {
      if (roList.get(0) instanceof UIRepositoryDirectory) {
        UIRepositoryDirectory rd = (UIRepositoryDirectory) roList.get(0);
        rd.setAcls(aclModel.getSelectedAcls());
      } else {
        UIRepositoryContent rc = (UIRepositoryContent) roList.get(0);
        rc.setAcls(aclModel.getSelectedAcls());
      }
      if (hideDialog) {
        applyAclConfirmationDialog.hide();
      }
      aclModel.getSelectedAcls().setModelDirty(false);
      messageLabel.setValue("Permission were applied successfully");
      messageDialog.show();
    } catch (AccessDeniedException ade) {
      if (hideDialog) {
        applyAclConfirmationDialog.hide();
      }
      messageLabel.setValue(ade.getLocalizedMessage());
      messageDialog.show();
      // Access is denied to retrieve acl information.
      // TODO We need to figure out whether to throw a dialog box or disable
      // the tab
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

  public void applyAcl() throws Exception {
    // We will call the the server apply method that only applies this acls changes on the current object
    if (applyOnlyRadioButton.isSelected()) {
      List<UIRepositoryObject> roList = getSelectedRepositoryObject();
      applyOnObjectOnly(roList, true);
    } else {
      // TODO We will call the the server apply method that applies this acls changes on the current object and its children
      applyAclConfirmationDialog.hide();
      messageLabel.setValue("The functionality is not currently supported");
      messageDialog.show();

    }
  }

  public void closeMessageDialog() {
    messageDialog.hide();
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

  public void setRecipientChanged(UIRepositoryObjectAcl acl) throws Exception {
    List<UIRepositoryObjectAcl> acls = new ArrayList<UIRepositoryObjectAcl>();
    acls.add(acl);
    aclModel.getSelectedAcls().setSelectedAclList(acls);
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
      aclModel.getSelectedAcls().updateAcl(acl);
    }
  }

  public void updateInheritFromParentPermission() {
    aclModel.getSelectedAcls().clear();
    aclModel.getSelectedAcls().setEntriesInheriting(inheritParentPermissionCheckbox.isChecked());
    if (inheritParentPermissionCheckbox.isChecked()) {
      uncheckAllPermissionBox();
    }
  }

  public void showRemoveAclConfirmationDialog() {
    removeAclConfirmationDialog.show();
  }

  public void closeRemoveAclConfirmationDialog() {
    removeAclConfirmationDialog.hide();
  }

  @SuppressWarnings("unchecked")
  public TYPE onContextChange() {
    if(aclModel.getSelectedAcls().isModelDirty()) {
      XulConfirmBox confirmBox = null;
      
      try {
        confirmBox = (XulConfirmBox) document.createElement("confirmbox");
      } catch (XulException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      confirmBox.setTitle("Context Change Warning");
      confirmBox.setMessage("You are about to change the context. All changes will be lost. Do you want to continue?");
      confirmBox.setAcceptLabel("Yes");
      confirmBox.setCancelLabel("No");
      confirmBox.addDialogCallback(new XulDialogCallback(){
  
        public void onClose(XulComponent sender, Status returnCode, Object retVal) {
           if(returnCode == Status.ACCEPT){
             returnType = TYPE.OK;
             aclModel.getSelectedAcls().clear();
             aclModel.getSelectedAcls().setModelDirty(false);
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
