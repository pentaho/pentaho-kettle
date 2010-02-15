package org.pentaho.di.ui.repository.repositoryexplorer.model;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.RepositoryUserInterface;
import org.pentaho.di.repository.IRole;
import org.pentaho.di.repository.UserInfo;
import org.pentaho.di.repository.ObjectRecipient.Type;
import org.pentaho.ui.xul.XulEventSourceAdapter;

public class UISecurity  extends XulEventSourceAdapter{
	
	public static enum Mode {ADD, EDIT, EDIT_MEMBER};
	private Type selectedDeck;
	protected UIRepositoryUser selectedUser;
	protected UIRepositoryRole selectedRole;
	private int selectedUserIndex;
  private int selectedRoleIndex;
	protected List<UIRepositoryUser> userList;
	protected List<UIRepositoryRole> roleList;
	
	public UISecurity() {
		userList = new ArrayList<UIRepositoryUser>();		
		roleList = new ArrayList<UIRepositoryRole>();		
	}
	public UISecurity(RepositoryUserInterface rui) {
		this();
		try  {
			if(rui != null && rui.getUsers() != null) {
				for(UserInfo user:rui.getUsers()) {
					userList.add(new UIRepositoryUser(user));
				}
				this.firePropertyChange("userList", null, userList); //$NON-NLS-1$
				for(IRole role:rui.getRoles()) {
					roleList.add(new UIRepositoryRole(role));
					
				}
				this.firePropertyChange("roleList", null, roleList); //$NON-NLS-1$
			}
		} catch(KettleException ke) {
			
		}
	}
	public Type getSelectedDeck() {
		return selectedDeck;
	}
	public void setSelectedDeck(Type selectedDeck) {
		this.selectedDeck = selectedDeck;
		this.firePropertyChange("selectedDeck", null, selectedDeck); //$NON-NLS-1$
	}
	public int getSelectedUserIndex() {
    return selectedUserIndex;
  }
  public void setSelectedUserIndex(int selectedUserIndex) {
    this.selectedUserIndex = selectedUserIndex;
    this.firePropertyChange("selectedUserIndex", null, selectedUserIndex); //$NON-NLS-1$
  }
  public int getSelectedRoleIndex() {
    return selectedRoleIndex;
  }
  public void setSelectedRoleIndex(int selectedRoleIndex) {
    this.selectedRoleIndex = selectedRoleIndex;
    this.firePropertyChange("selectedRoleIndex", null, selectedRoleIndex); //$NON-NLS-1$
  }
	
	public UIRepositoryUser getSelectedUser() {
		return selectedUser;
	}
	public void setSelectedUser(UIRepositoryUser selectedUser) {
		this.selectedUser = selectedUser;
		this.firePropertyChange("selectedUser", null, selectedUser); //$NON-NLS-1$
    setSelectedUserIndex(getIndexOfUser(selectedUser));		
	}
	public UIRepositoryRole getSelectedRole() {
		return selectedRole;
	}
	public void setSelectedRole(UIRepositoryRole selectedRole) {
		this.selectedRole = selectedRole;
		this.firePropertyChange("selectedRole", null, selectedRole); //$NON-NLS-1$
    setSelectedRoleIndex(getIndexOfRole(selectedRole));				
	}
	public List<UIRepositoryUser> getUserList() {
		return userList;
	}
	public void setUserList(List<UIRepositoryUser> userList) {
		this.userList.clear();
		this.userList.addAll(userList);
		this.firePropertyChange("userList", null, userList); //$NON-NLS-1$
	}
	public List<UIRepositoryRole> getRoleList() {
		return roleList;
	}
	public void setRoleList(List<UIRepositoryRole> roleList) {
		this.roleList.clear();
		this.roleList.addAll(roleList);
		this.firePropertyChange("roleList", null, roleList); //$NON-NLS-1$		
	}
	public void addRole(UIRepositoryRole roleToAdd) {
		roleList.add(roleToAdd);
		this.firePropertyChange("roleList", null, roleList); //$NON-NLS-1$
		setSelectedRole(roleToAdd);
	}
	public void updateUser(UIRepositoryUser userToUpdate) {
		UIRepositoryUser user = getUser(userToUpdate.getName());
		user.setDescription(userToUpdate.getDescription());
		user.setRoles(userToUpdate.getRoles());
    this.firePropertyChange("userList", null, userList); //$NON-NLS-1$
		setSelectedUser(user);
	}
	
	public void updateRole(UIRepositoryRole roleToUpdate) {
		UIRepositoryRole role = getRole(roleToUpdate.getName());
		role.setDescription(roleToUpdate.getDescription());
		role.setUsers(roleToUpdate.getUsers());
		this.firePropertyChange("roleList", null, roleList); //$NON-NLS-1$
		setSelectedRole(role);
	}
	
	public void removeRole(String name) {
		removeRole(getRole(name));
	}
	public void removeRole(UIRepositoryRole roleToRemove) {
	  int index = getIndexOfRole(roleToRemove); 
		roleList.remove(roleToRemove);
		this.firePropertyChange("roleList", null, roleList); //$NON-NLS-1$
    if(index -1 >=0) {
      setSelectedRole(getRoleAtIndex(index-1));
    }
	}
	public void addUser(UIRepositoryUser userToAdd) {
		userList.add(userToAdd);
		this.firePropertyChange("userList", null, userList); //$NON-NLS-1$
		setSelectedUser(userToAdd);
	}
	public void removeUser(String name) {
		removeUser(getUser(name));
	}
	
	public void removeUser(UIRepositoryUser userToRemove) {
	  int index = getIndexOfUser(userToRemove); 
		userList.remove(userToRemove);
		this.firePropertyChange("userList", null, userList); //$NON-NLS-1$
		if(index -1 >=0) {
		  setSelectedUser(getUserAtIndex(index-1));
		}
	}
	
	private UIRepositoryRole getRole(String name) {
		for(UIRepositoryRole role:roleList) {
			if(role.getName().equals(name)) {
				return role;
			}
		}	
		return null;
	}

	private UIRepositoryUser getUser(String name) {
		for(UIRepositoryUser user:userList) {
			if(user.getName().equals(name)) {
				return user;
			}
		}	
		return null;
	}

  public void removeRolesFromSelectedUser(Collection<Object> roles) {
    for(Object o:roles) {
      UIRepositoryRole role = (UIRepositoryRole) o;
      removeRoleFromSelectedUser(role.getName());
    }
    this.firePropertyChange("selectedUser", null, selectedUser); //$NON-NLS-1$
  } 
	
	private void removeRoleFromSelectedUser(String roleName) {
	  selectedUser.getRoles().remove(findRoleInSelectedUser(roleName));
	} 

  public void removeUsersFromSelectedRole(Collection<Object> users) {
    for(Object o:users) {
      UIRepositoryUser user = (UIRepositoryUser) o;
      removeUserFromSelectedRole(user.getName());
    }
    this.firePropertyChange("selectedRole", null, selectedRole); //$NON-NLS-1$
  } 

	private void removeUserFromSelectedRole(String userName) {
    selectedRole.getUsers().remove(findUserInSelectedRole(userName));
	}

	private IRole findRoleInSelectedUser(String roleName) {
	   Set<IRole> roles = selectedUser.getRoles();
	   for(IRole role:roles) {
	     if(role.getName().equals(roleName)) {
	       return role;
	     }
	   }	  
	   return null;
	}
	
	 private UserInfo findUserInSelectedRole(String userName) {
     Set<UserInfo> users = selectedRole.getUsers();
     for(UserInfo user:users) {
       if(user.getName().equals(userName)) {
         return user;
       }
     }    
     return null;
  }
	 
	private UIRepositoryUser getUserAtIndex(int index) {
      return this.userList.get(index);
	}

	private int getIndexOfUser(UIRepositoryUser ru) {
	  for(int i=0;i<this.userList.size();i++) {
	    UIRepositoryUser user  = this.userList.get(i);
	    if(ru.getName().equals(user.getName())) {
	      return i;
	    }
	  }
	  return -1;
	}
	
  private UIRepositoryRole getRoleAtIndex(int index) {
    return this.roleList.get(index);
  }

  protected int getIndexOfRole(UIRepositoryRole rr) {
    for(int i=0;i<this.roleList.size();i++) {
      UIRepositoryRole role  = this.roleList.get(i);
      if(rr.getName().equals(role.getName())) {
        return i;
      }
    }
    return -1;
  }
	
}
