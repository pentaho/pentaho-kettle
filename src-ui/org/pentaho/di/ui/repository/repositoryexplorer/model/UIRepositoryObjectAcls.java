package org.pentaho.di.ui.repository.repositoryexplorer.model;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.repository.ObjectAce;
import org.pentaho.di.repository.ObjectAcl;
import org.pentaho.di.repository.ObjectRecipient;
import org.pentaho.ui.xul.XulEventSourceAdapter;

public class UIRepositoryObjectAcls extends XulEventSourceAdapter {

  protected ObjectAcl obj;

  private List<ObjectAce> aces;

  private List<UIRepositoryObjectAcl> selectedAclList = new ArrayList<UIRepositoryObjectAcl>();

  private boolean removeEnabled;

  private boolean modelDirty;

  public UIRepositoryObjectAcls() {
    super();
  }

  // ~ Methods
  // =========================================================================================================

  public List<ObjectAce> getAces() {
    return aces;
  }

  public void setAces(List<ObjectAce> aces) {
    this.aces = aces;
  }

  public void setObjectAcl(ObjectAcl obj) {
    this.obj = obj;
    this.firePropertyChange("acls", null, getAcls());
    this.firePropertyChange("entriesInheriting", null, isEntriesInheriting());
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
    this.obj.getAces().clear();
    if (acls != null) {
      for (UIRepositoryObjectAcl acl : acls) {
        obj.getAces().add(acl.getAce());
      }
    }
    this.firePropertyChange("acls", null, getAcls());
  }

  public void addAcl(UIRepositoryObjectAcl aclToAdd) {
    this.obj.getAces().add(aclToAdd.getAce());
    setRemoveEnabled((!obj.isEntriesInheriting() && getSelectedAclList() != null && getSelectedAclList().size() > 0));
    this.firePropertyChange("acls", null, getAcls());
    // Setting the selected index
    List<UIRepositoryObjectAcl> aclList = new ArrayList<UIRepositoryObjectAcl>();
    aclList.add(aclToAdd);
    setSelectedAclList(aclList);
    setModelDirty(true);
  }

  public void removeAcls(List<UIRepositoryObjectAcl> aclsToRemove) {
    for (UIRepositoryObjectAcl acl : aclsToRemove) {
      removeAcl(acl.getRecipientName());
    }
    this.firePropertyChange("acls", null, getAcls());
    setRemoveEnabled((!obj.isEntriesInheriting() && getSelectedAclList() != null && getSelectedAclList().size() > 0));
    setModelDirty(true);
  }

  public void removeAcl(String recipientName) {
    ObjectAce aceToRemove = null;

    for (ObjectAce ace : obj.getAces()) {
      if (ace.getRecipient().getName().equals(recipientName)) {
        aceToRemove = ace;
        break;
      }
    }
    int index = getIndex(aceToRemove);
    obj.getAces().remove(aceToRemove);
    // Set the selected index if there is something left in the list
    ObjectAce ace = getAceAtIndex(index -1);
    if(ace != null) {
      // Setting the selected index
      List<UIRepositoryObjectAcl> aclList = new ArrayList<UIRepositoryObjectAcl>();
      aclList.add(new UIRepositoryObjectAcl(ace));
      setSelectedAclList(aclList);
    }
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
    this.firePropertyChange("acls", null, getAcls());
    // Setting the selected index
    List<UIRepositoryObjectAcl> aclList = new ArrayList<UIRepositoryObjectAcl>();
    aclList.add(aclToUpdate);
    setSelectedAclList(aclList);

    setModelDirty(true);
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
    selectedAclList.addAll(list);
    this.firePropertyChange("selectedAclList", previousVal, list); //$NON-NLS-1$
    setRemoveEnabled((!isEntriesInheriting() && getSelectedAclList() != null && getSelectedAclList().size() > 0));
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
      setRemoveEnabled((!entriesInheriting && getSelectedAclList() != null && getSelectedAclList().size() > 0));
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

  private int getIndex(ObjectAce ace) {
    List<ObjectAce> aceList = obj.getAces();
    for (int i = 0; i < aceList.size(); i++) {
      if (ace.equals(aceList.get(i))) {
        return i;
      }
    }
    return -1;
  }

  private ObjectAce getAceAtIndex(int index) {
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
}
