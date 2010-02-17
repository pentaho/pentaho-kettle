/* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

package org.pentaho.di.repository;

import java.util.HashSet;
import java.util.Set;

/*
 * Created on 7-apr-2004
 *
 */

public class UserInfo {
	// private static Class<?> PKG = UserInfo.class; // for i18n purposes,
	// needed by Translator2!! $NON-NLS-1$

	public static final String REPOSITORY_ELEMENT_TYPE = "user"; //$NON-NLS-1$

	private ObjectId id;

	private String login; // Login ID
	private String password; // Password
	private String username; // Long name
	private String description; // Description
	private boolean enabled; // Enabled: yes or no

	private ProfileMeta profile; // user profile information
	private Set<IRole> roles;
	
	/**
	 * copy constructor
	 * 
	 * @param copyFrom
	 */
	public UserInfo(UserInfo copyFrom) {
	  this.id = copyFrom.id;
	  this.login = copyFrom.login;
	  this.password = copyFrom.password;
	  this.username = copyFrom.username;
	  this.description = copyFrom.description;
	  this.enabled = copyFrom.enabled;
	  this.profile = copyFrom.profile != null ? new ProfileMeta(copyFrom.profile) : null;
	  this.roles = copyFrom.roles != null ? new HashSet<IRole>(copyFrom.roles) : null;
	}
	
  public UserInfo(String login, String password, String username, String description, boolean enabled,
      Set<IRole> roles) {
    this(login, password, username, description, enabled);
    this.roles = roles;
  }	
	public UserInfo(String login, String password, String username,
			String description, boolean enabled) {
		this.login = login;
		this.password = password;
		this.username = username;
		this.description = description;
		this.enabled = enabled;
    this.roles = new HashSet<IRole>();
	}
	public UserInfo(String login, String password, String username,
			String description, boolean enabled, ProfileMeta profile) {
		this(login, password, username, description, enabled);
		this.profile = profile;
	}

	public UserInfo(String login) {
		this();
		this.login = login;
	}

	public UserInfo() {
		this.login = null;
		this.password = null;
		this.username = null;
		this.description = null;
		this.enabled = true;
		this.profile = null;
		this.roles = new HashSet<IRole>();
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getLogin() {
		return login;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return password;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public void setEnabled() {
		setEnabled(true);
	}

	public void setDisabled() {
		setEnabled(false);
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void setProfile(ProfileMeta profile) {
		this.profile = profile;
	}

	public ProfileMeta getProfile() {
		return profile;
	}

	public ObjectId getObjectId() {
		return id;
	}

	public void setObjectId(ObjectId id) {
		this.id = id;
	}

	// Helper functions...

	public boolean isReadOnly() {
		if (profile == null)
			return true;
		return profile.isReadonly();
	}

	public boolean isAdministrator() {
		if (profile == null)
			return false;
		return profile.isAdministrator();
	}

	public boolean useTransformations() {
		if (profile == null)
			return false;
		return profile.useTransformations();
	}

	public boolean useJobs() {
		if (profile == null)
			return false;
		return profile.useJobs();
	}

	public boolean useSchemas() {
		if (profile == null)
			return false;
		return profile.useSchemas();
	}

	public boolean useDatabases() {
		if (profile == null)
			return false;
		return profile.useDatabases();
	}

	public boolean exploreDatabases() {
		if (profile == null)
			return false;
		return profile.exploreDatabases();
	}

	/**
	 * @return the enabled
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Not used in this case, simply return root /
	 */
	public RepositoryDirectory getRepositoryDirectory() {
		return new RepositoryDirectory();
	}

	public void setRepositoryDirectory(RepositoryDirectory repositoryDirectory) {
		throw new RuntimeException(
				"Setting a directory on a database connection is not supported");
	}

	public String getRepositoryElementType() {
		return REPOSITORY_ELEMENT_TYPE;
	}

	/**
	 * The name of the user maps to the login id
	 */
	public String getName() {
		return login;
	}

	/**
	 * Set the name of the user.
	 * 
	 * @param name
	 *            The name of the user maps to the login id.
	 */
	public void setName(String name) {
		this.login = name;
	}

	public boolean supportsLocking() {
		if (profile == null)
			return false;
		return profile.supportsLocking();
	}

	public boolean addRole(IRole role) {
		return this.roles.add(role);
	}

	public boolean removeRole(IRole role) {
		return this.roles.remove(role);
	}

	public void clearRoles() {
		this.roles.clear();
	}

	public void setRoles(Set<IRole> roles) {
		this.roles = roles;
	}

	public Set<IRole> getRoles() {
		return this.roles;
	}
}
