package org.pentaho.di.ui.repository.repositoryexplorer.model;

import java.util.HashSet;
import java.util.Set;

import org.pentaho.di.repository.IRole;
import org.pentaho.di.repository.UserInfo;
import org.pentaho.ui.xul.XulEventSourceAdapter;

public class UIRepositoryRole extends XulEventSourceAdapter implements IUIRole{

	private IRole rri;

	public UIRepositoryRole() {
	}

	public UIRepositoryRole(IRole rri) {
		this.rri = rri;
	}

	public String getName() {
		return rri.getName();
	}

	public void setName(String name) {
		rri.setName(name);
	}
	
	public String getDescription() {
		return rri.getDescription();
	}

	public void setDescription(String description) {
		rri.setDescription(description);
	}

	public void setUsers(Set<UIRepositoryUser> users) {
	  Set<UserInfo> rusers = new HashSet<UserInfo>();
	  for(UIRepositoryUser user:users) {
	    rusers.add(user.getUserInfo());
	  }
		rri.setUsers(rusers);
	}

	public Set<UIRepositoryUser> getUsers() {
	  Set<UIRepositoryUser> rusers = new HashSet<UIRepositoryUser>();
	  for(UserInfo userInfo:rri.getUsers()) {
	    rusers.add(new UIRepositoryUser(userInfo));
	  }
		return rusers;
	}

	public boolean addUser(UIRepositoryUser user) {
		return rri.addUser(user.getUserInfo());
	}

	public boolean removeUser(UIRepositoryUser user) {
		return removeUser(user.getUserInfo().getLogin());
	}

	public void clearUsers() {
		rri.clearUsers();
	}

	public IRole getRole() {
	  return rri;
	}

	private boolean removeUser(String userName) {
	  UserInfo userInfo = null;
	  for(UserInfo user:rri.getUsers()) {
	    if(user.getLogin().equals(userName)) {
	      userInfo = user;
	      break;
	    }
	  }
	  if(userInfo != null) {
	    return rri.removeUser(userInfo);
	  } else {
	    return false;
	  }
	}
}
