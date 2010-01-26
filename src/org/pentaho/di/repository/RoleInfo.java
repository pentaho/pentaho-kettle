package org.pentaho.di.repository;

import java.util.HashSet;
import java.util.Set;

public class RoleInfo {

	public static final String REPOSITORY_ELEMENT_TYPE = "role";

	// ~ Instance fields
	// =================================================================================================

	private String name;

	private String description;

	private Set<UserInfo> users = new HashSet<UserInfo>();

	// ~ Constructors
	// ====================================================================================================

	public RoleInfo() {
		this.name = null;
		this.description = null;
	}

	public void setName(String name) {
		this.name = name;
	}

	public RoleInfo(String name) {
		this(name, null);
	}

	public RoleInfo(String name, String description) {
		this.name = name;
		this.description = description;
	}


	// ~ Methods
	// =========================================================================================================

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setUsers(Set<UserInfo> users) {
		this.users = users;
	}

	public Set<UserInfo> getUsers() {
		return users;
	}

	public boolean addUser(UserInfo user) {
		return users.add(user);
	}

	public boolean removeUser(UserInfo user) {
		return users.remove(user);
	}

	public void clearUsers() {
		users.clear();
	}
}
