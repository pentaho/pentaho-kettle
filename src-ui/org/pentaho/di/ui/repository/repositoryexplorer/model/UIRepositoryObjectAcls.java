package org.pentaho.di.ui.repository.repositoryexplorer.model;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.pentaho.di.repository.ObjectAce;
import org.pentaho.di.repository.ObjectAcl;
import org.pentaho.di.repository.ObjectPermission;
import org.pentaho.di.repository.ObjectRecipient;
import org.pentaho.ui.xul.XulEventSourceAdapter;

public class UIRepositoryObjectAcls extends XulEventSourceAdapter {

  protected ObjectAcl obj;

  private List<UIRepositoryObjectAcl> selectedAclList = new ArrayList<UIRepositoryObjectAcl>();

  private boolean removeEnabled;

  private boolean modelDirty;

  public UIRepositoryObjectAcls() {
    super();
  }

  // ~ Methods
  // =========================================================================================================

  public void setObjectAcl(ObjectAcl obj) {
    this.obj = obj;
    isApplyValid();
    this.firePropertyChange("acls", null, getAcls()); //$NON-NLS-1$
    this.firePropertyChange("entriesInheriting", null, isEntriesInheriting()); //$NON-NLS-1$
  }

  public ObjectAcl getObjectAcl() {
    return this.obj;
  }

  public List<UIRepositoryObjectAcl> getAcls() {
    if (obj != null) {
      List<UIRepositoryObjectAcl> acls = new ArrayList<UIRepositoryObjectAcl>();
      for (ObjectAce ace : obj.getAces()) {
        acls.add(new UIRepositoryObjectAcl(ace));
      }
      return acls;
    }
    return null;
  }

  public void setAcls(List<UIRepositoryObjectAcl> acls) {
    List<UIRepositoryObjectAcl> prevousVal = new ArrayList<UIRepositoryObjectAcl>();
    prevousVal.addAll(getAcls());

    this.obj.getAces().clear();
    if (acls != null) {
      for (UIRepositoryObjectAcl acl : acls) {
        obj.getAces().add(acl.getAce());
      }
    }
    this.firePropertyChange("acls", prevousVal, getAcls()); //$NON-NLS-1$
  }

  public void addAcls(List<UIRepositoryObjectAcl> aclsToAdd) {
    for (UIRepositoryObjectAcl acl : aclsToAdd) {
      addAcl(acl);
    }
    this.firePropertyChange("acls", null, getAcls()); //$NON-NLS-1$
    // Setting the selected index to the first item in the list
    if (obj.getAces().size() > 0) {
      List<UIRepositoryObjectAcl> aclList = new ArrayList<UIRepositoryObjectAcl>();
      aclList.add(new UIRepositoryObjectAcl(getAceAtIndex(0)));
      setSelectedAclList(aclList);
    }
    setRemoveEnabled((!obj.isEntriesInheriting() && !isEmpty()));
    setModelDirty(true);
    isApplyValid();
  }

  public void addAcl(UIRepositoryObjectAcl aclToAdd) {
    this.obj.getAces().add(aclToAdd.getAce());
  }

  public void removeAcls(List<UIRepositoryObjectAcl> aclsToRemove) {
    for (UIRepositoryObjectAcl acl : aclsToRemove) {
      removeAcl(acl.getRecipientName());
    }

    this.firePropertyChange("acls", null, getAcls()); //$NON-NLS-1$
    if (obj.getAces().size() > 0) {
      List<UIRepositoryObjectAcl> aclList = new ArrayList<UIRepositoryObjectAcl>();
      aclList.add(new UIRepositoryObjectAcl(getAceAtIndex(0)));
      setSelectedAclList(aclList);
    } else {
      setSelectedAclList(null);
    }
    setRemoveEnabled((!obj.isEntriesInheriting() && !isEmpty()));
    setModelDirty(true);
    isApplyValid();
  }

  public void removeAcl(String recipientName) {
    ObjectAce aceToRemove = null;

    for (ObjectAce ace : obj.getAces()) {
      if (ace.getRecipient().getName().equals(recipientName)) {
        aceToRemove = ace;
        break;
      }
    }
    obj.getAces().remove(aceToRemove);
  }

  public void removeSelectedAcls() {
    // side effect deletes multiple acls when only one selected.
    List<UIRepositoryObjectAcl> removalList = new ArrayList<UIRepositoryObjectAcl>();
    for (UIRepositoryObjectAcl rem : getSelectedAclList()) {
      removalList.add(rem);
    }
    removeAcls(removalList);
  }

  public void updateAcl(UIRepositoryObjectAcl aclToUpdate) {
    List<ObjectAce> aces = obj.getAces();
    for (ObjectAce ace : aces) {
      if (ace.getRecipient().getName().equals(aclToUpdate.getRecipientName())) {
        ace.setPermissions(aclToUpdate.getPermissionSet());
      }
    }
    UIRepositoryObjectAcl acl = getAcl(aclToUpdate.getRecipientName());
    acl.setPermissionSet(aclToUpdate.getPermissionSet());
    this.firePropertyChange("acls", null, getAcls()); //$NON-NLS-1$
    // Setting the selected index
    List<UIRepositoryObjectAcl> aclList = new ArrayList<UIRepositoryObjectAcl>();
    aclList.add(aclToUpdate);
    setSelectedAclList(aclList);
    setModelDirty(true);
    isApplyValid();
  }

  public UIRepositoryObjectAcl getAcl(String recipient) {
    for (ObjectAce ace : obj.getAces()) {
      if (ace.getRecipient().getName().equals(recipient)) {
        return new UIRepositoryObjectAcl(ace);
      }
    }
    return null;
  }

  public List<UIRepositoryObjectAcl> getSelectedAclList() {
    return selectedAclList;
  }

  public void setSelectedAclList(List<UIRepositoryObjectAcl> list) {
    List<UIRepositoryObjectAcl> previousVal = new ArrayList<UIRepositoryObjectAcl>();
    previousVal.addAll(selectedAclList);
    selectedAclList.clear();
    if (list != null) {
      selectedAclList.addAll(list);
      this.firePropertyChange("selectedAclList", null, list); //$NON-NLS-1$
    }
    setRemoveEnabled((!isEntriesInheriting() && !isEmpty()));
  }

  public boolean isEntriesInheriting() {
    if (obj != null) {
      return obj.isEntriesInheriting();
    } else
      return false;
  }

  public void setEntriesInheriting(boolean entriesInheriting) {
    if (obj != null) {
      boolean previousVal = isEntriesInheriting();
      obj.setEntriesInheriting(entriesInheriting);
      this.firePropertyChange("entriesInheriting", previousVal, entriesInheriting); //$NON-NLS-1$
      setRemoveEnabled((!entriesInheriting && !isEmpty()));
      setModelDirty(true);
    }
  }

  public ObjectRecipient getOwner() {
    if (obj != null) {
      return obj.getOwner();
    } else {
      return null;
    }
  }

  public void setRemoveEnabled(boolean removeEnabled) {
    this.removeEnabled = removeEnabled;
    this.firePropertyChange("removeEnabled", null, removeEnabled); //$NON-NLS-1$
  }

  public boolean isRemoveEnabled() {
    return removeEnabled;
  }

  public int getAceIndex(ObjectAce ace) {
    List<ObjectAce> aceList = obj.getAces();
    for (int i = 0; i < aceList.size(); i++) {
      if (ace.equals(aceList.get(i))) {
        return i;
      }
    }
    return -1;
  }

  public ObjectAce getAceAtIndex(int index) {
    if (index >= 0) {
      return obj.getAces().get(index);
    } else {
      return null;
    }
  }

  public void setModelDirty(boolean modelDirty) {
    this.modelDirty = modelDirty;
  }

  public boolean isModelDirty() {
    return modelDirty;
  }

  public void clear() {
    setRemoveEnabled(false);
    setModelDirty(false);
    setAcls(null);
    setSelectedAclList(null);
  }

  private boolean isEmpty() {
    return getSelectedAclList() == null || getSelectedAclList().size() <= 0;
  }

  public boolean isModelValid() {
    List<UIRepositoryObjectAcl> uiAcls = getAcls();
    if (uiAcls != null) {
      for (UIRepositoryObjectAcl uiAcl : uiAcls) {
        if (uiAcl != null && uiAcl.getRecipientName() != null) {
          EnumSet<ObjectPermission> permissions = uiAcl.getPermissionSet();
          if (permissions == null || permissions.size() == 0) {
            return false;
          }
        }
      }
    }
    return true;    
  }
  
  public boolean isApplyValid() {
    if(isModelValid() && isModelDirty()) {
      this.firePropertyChange("applyValid", null, true); //$NON-NLS-1$
      return true;
    } else {
      this.firePropertyChange("applyValid", null, false); //$NON-NLS-1$
      return false;
    }
  }
}
