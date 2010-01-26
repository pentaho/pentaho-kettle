package org.pentaho.di.ui.repository.repositoryexplorer.model;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.repository.ObjectAce;
import org.pentaho.di.repository.ObjectAcl;
import org.pentaho.di.repository.ObjectRecipient;
import org.pentaho.ui.xul.XulEventSourceAdapter;

public class UIRepositoryObjectAcls extends XulEventSourceAdapter{

	protected ObjectAcl obj;
	private List<ObjectAce> aces;
  private List<UIRepositoryObjectAcl> selectedAclList = new ArrayList<UIRepositoryObjectAcl>();
  private boolean removeEnabled;
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
	  if(obj != null) {
  	  List<UIRepositoryObjectAcl> acls = new  ArrayList<UIRepositoryObjectAcl>();
  	  for(ObjectAce ace:obj.getAces()) {
  	    acls.add(new UIRepositoryObjectAcl(ace));
  	  }
  		return acls;
	  } 
	  return null;
	}

	public void setAcls(List<UIRepositoryObjectAcl> acls) {
		this.obj.getAces().clear();
		if(acls != null) {
  		for(UIRepositoryObjectAcl acl:acls) {
  		  obj.getAces().add(acl.getAce());
  		}
		}
    this.firePropertyChange("acls", null, getAcls());
	}

	public void addAcl(UIRepositoryObjectAcl aclToAdd) {
	  int index = getIndex(aclToAdd);
	  this.obj.getAces().add(aclToAdd.getAce());
    setRemoveEnabled((!obj.isEntriesInheriting() && getSelectedAclList() != null));
    if(index >=0) {
      List<UIRepositoryObjectAcl> aclList = new ArrayList<UIRepositoryObjectAcl>();
      aclList.add(new UIRepositoryObjectAcl(obj.getAces().get(index-1)));
      setSelectedAclList(aclList);
    }
    this.firePropertyChange("acls", null, getAcls());
 }
	
	public void removeAcls(List<UIRepositoryObjectAcl> aclsToRemove) {
	  for(UIRepositoryObjectAcl acl:aclsToRemove) {
	    removeAcl(acl.getRecipientName());
	  }
    this.firePropertyChange("acls", null, getAcls());
    setRemoveEnabled((!obj.isEntriesInheriting() && getSelectedAclList() != null));
	}
	public void removeAcl(String recipientName) {
	  ObjectAce aceToRemove = null;
	  for(ObjectAce ace:obj.getAces()) {
	    if(ace.getRecipient().getName().equals(recipientName)) {
	      aceToRemove = ace;
	      break;
	    }
	  }
		obj.getAces().remove(aceToRemove);
	}
	
	public void updateAcl(UIRepositoryObjectAcl aclToUpdate) {
	  List<ObjectAce> aces = obj.getAces();
	  for(ObjectAce ace:aces) {
	    if(ace.getRecipient().getName().equals(aclToUpdate.getRecipientName())) {
	      ace.setPermissions(aclToUpdate.getPermissionSet());
	    }
	  }
	  UIRepositoryObjectAcl acl = getAcl(aclToUpdate.getRecipientName());
	  acl.setPermissionSet(aclToUpdate.getPermissionSet());
    this.firePropertyChange("acls", null, getAcls());
	}
	public UIRepositoryObjectAcl getAcl(String recipient) {
		for(ObjectAce ace: obj.getAces()) {
			if(ace.getRecipient().getName().equals(recipient)) {
				return new UIRepositoryObjectAcl(ace);
			}
		}
		return null;
	}
	
	public List<UIRepositoryObjectAcl> getSelectedAclList() {
		return selectedAclList;
	}

	public void setSelectedAclList(List<UIRepositoryObjectAcl> selectedAclList) {
		List<UIRepositoryObjectAcl> previousVal = new ArrayList<UIRepositoryObjectAcl>();
		previousVal.addAll(selectedAclList);
		this.selectedAclList.clear();
		this.selectedAclList.addAll(selectedAclList);
		this.firePropertyChange("selectedAclList", previousVal, this.selectedAclList); //$NON-NLS-1$
		setRemoveEnabled((!isEntriesInheriting() && getSelectedAclList() != null));
	}
	public boolean isEntriesInheriting() {
		if(obj != null) {
			return obj.isEntriesInheriting();
		} else return false;
	}
	
	public void setEntriesInheriting(boolean entriesInheriting) {
		if(obj != null) {
			boolean previousVal = isEntriesInheriting();
			obj.setEntriesInheriting(entriesInheriting);
			this.firePropertyChange("entriesInheriting", previousVal, entriesInheriting); //$NON-NLS-1$
			setRemoveEnabled((!entriesInheriting && getSelectedAclList() != null));
		}
	}
	
	public ObjectRecipient getOwner() {
		if(obj != null) {
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
	  private int getIndex(UIRepositoryObjectAcl acl) {
	    List<ObjectAce> aceList = obj.getAces();
	    for(int i=0;i< aceList.size();i++) {
	      if(acl.equals(aceList.get(i))) {
	        return i;
	      }
	    }
	    return -1;
	  }
}
