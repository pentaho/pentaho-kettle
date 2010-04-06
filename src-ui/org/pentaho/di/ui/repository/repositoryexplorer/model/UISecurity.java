package org.pentaho.di.ui.repository.repositoryexplorer.model;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.repository.IUser;
import org.pentaho.di.repository.RepositorySecurityManager;
import org.pentaho.di.repository.ObjectRecipient.Type;
import org.pentaho.ui.xul.XulEventSourceAdapter;

public class UISecurity extends XulEventSourceAdapter {

  public static enum Mode { ADD, EDIT, EDIT_MEMBER  };
  
  private Type selectedDeck;

  protected IUIUser selectedUser;

  private int selectedUserIndex;

  protected List<IUIUser> userList;

  public UISecurity() {
    userList = new ArrayList<IUIUser>();
  }

  public UISecurity(RepositorySecurityManager rsm) throws Exception {
    this();
    if (rsm != null && rsm.getUsers() != null) {
      for (IUser user : rsm.getUsers()) {
        userList.add(UIObjectRegistry.getInstance().constructUIRepositoryUser(user));
      }
      this.firePropertyChange("userList", null, userList); //$NON-NLS-1$
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

  public IUIUser getSelectedUser() {
    return selectedUser;
  }

  public void setSelectedUser(IUIUser selectedUser) {
    this.selectedUser = selectedUser;
    this.firePropertyChange("selectedUser", null, selectedUser); //$NON-NLS-1$
    setSelectedUserIndex(getIndexOfUser(selectedUser));
  }

  public List<IUIUser> getUserList() {
    return userList;
  }

  public void setUserList(List<IUIUser> userList) {
    this.userList.clear();
    this.userList.addAll(userList);
    this.firePropertyChange("userList", null, userList); //$NON-NLS-1$
  }

  public void updateUser(IUIUser userToUpdate) {
    IUIUser user = getUser(userToUpdate.getName());
    user.setDescription(userToUpdate.getDescription());
    this.firePropertyChange("userList", null, userList); //$NON-NLS-1$
    setSelectedUser(user);
  }

  public void addUser(IUIUser userToAdd) {
    userList.add(userToAdd);
    this.firePropertyChange("userList", null, userList); //$NON-NLS-1$
    setSelectedUser(userToAdd);
  }

  public void removeUser(String name) {
    removeUser(getUser(name));
  }

  public void removeUser(IUIUser userToRemove) {
    int index = getIndexOfUser(userToRemove);
    userList.remove(userToRemove);
    this.firePropertyChange("userList", null, userList); //$NON-NLS-1$
    if (index - 1 >= 0) {
      setSelectedUser(getUserAtIndex(index - 1));
    }
  }

  protected IUIUser getUser(String name) {
    for (IUIUser user : userList) {
      if (user.getName().equals(name)) {
        return user;
      }
    }
    return null;
  }


  private IUIUser getUserAtIndex(int index) {
    return this.userList.get(index);
  }

  private int getIndexOfUser(IUIUser ru) {
    for (int i = 0; i < this.userList.size(); i++) {
      IUIUser user = this.userList.get(i);
      if (ru.getName().equals(user.getName())) {
        return i;
      }
    }
    return -1;
  }
}
