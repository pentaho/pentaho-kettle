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
import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.i18n.BaseMessages;


/*
 * Created on 7-apr-2004
 *
 */

public class ProfileMeta 
{
	private static Class<?> PKG = ProfileMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	/**
	 * These are the permissions for the various repositories around.
	 * Over time we will refine these but for now we'll keep the onese we have.
	 * 
	 * @author matt
	 */
	public enum Permission {
		
		READ_ONLY( "READONLY", BaseMessages.getString(PKG, "PermissionMeta.Permission.ReadOnly")),
		ADMIN( "ADMIN", BaseMessages.getString(PKG, "PermissionMeta.Permission.Administrator")),
		TRANSFORMATION( "TRANS", BaseMessages.getString(PKG, "PermissionMeta.Permission.UseTransformations")),
		JOB( "JOB", BaseMessages.getString(PKG, "PermissionMeta.Permission.UseJobs")),
		SCHEMA( "SCHEMA", BaseMessages.getString(PKG, "PermissionMeta.Permission.UseSchemas")),
		DATABASE( "DB", BaseMessages.getString(PKG, "PermissionMeta.Permission.UseDatabases")),
		EXPLORE_DATABASE( "EXPLORE_DB", BaseMessages.getString(PKG, "PermissionMeta.Permission.ExploreDatabases")),
		LOCK_FILE( "LOCK_FILE", BaseMessages.getString(PKG, "PermissionMeta.Permission.LockFiles")),
		;
		
		private String code;
		private String description;
		
		Permission(String code, String description) {
			this.code = code;
			this.description=description;
		}
		
		public String getCode() {
			return code;
		}
		
		public String getDescription() {
			return description;
		}
		
		public static Permission getPermissionWithCode(String code)
		{
			for (Permission permission : Permission.values()) {
				if (permission.getCode().equals(code)) return permission;
			}
			return null;
		}

		public static Permission getPermissionWithDescription(String permissionDescription) {
			for (Permission permission : Permission.values()) {
				if (permission.getDescription().equals(permissionDescription)) return permission;
			}
			return null;
		}
	};

	private ObjectId profileId;
	
	private String name;        // Long name
	private String description; // Description
	
	private List<Permission> permissions; // List of permissions in this profile...

	public ProfileMeta(ProfileMeta copyFrom) {
	  this.profileId = copyFrom.profileId;
	  this.name = copyFrom.name;
	  this.description = copyFrom.description;
	  this.permissions = new ArrayList<Permission>(copyFrom.permissions);
	}
	
	public ProfileMeta(String name, String description)
	{
		this.name = name;
		this.description = description;
		this.permissions = new ArrayList<Permission>();
	}

	public ProfileMeta()
	{
		this(null, null);
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public String getName()
	{
		return name;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}
	
	public String getDescription()
	{
		return description;
	}
	
	public void addPermission(Permission permission)
	{
		permissions.add(permission);
	}

	public void addPermission(int p, Permission permission)
	{
		permissions.add(p, permission);
	}

	public int nrPermissions()
	{
		if (permissions==null) return 0;
		return permissions.size();
	}
	
	public void removePermission(int i)
	{
		permissions.remove(i);
	}
	
	public Permission getPermission(int i)
	{
		return permissions.get(i);
	}
	
	public void removeAllPermissions()
	{
		permissions.clear();
	}

	public int indexOfPermission(Permission permission)
	{
		return permissions.indexOf(permission);
	}
	
	public ObjectId getObjectId()
	{
		return profileId;
	}
	
	public void setObjectId(ObjectId id)
	{
		this.profileId = id;
	}
	
	// Helper functions...
	
	public boolean isReadonly() {
		return checkPermission(Permission.READ_ONLY);
	}

	public boolean isAdministrator() {
		return checkPermission(Permission.ADMIN);
	}

	public boolean useTransformations() {
		return checkPermission(Permission.TRANSFORMATION);
	}

	public boolean useJobs() {
		return checkPermission(Permission.JOB);
	}

	public boolean useSchemas() {
		return checkPermission(Permission.SCHEMA);
	}

	public boolean useDatabases() {
		return checkPermission(Permission.DATABASE);
	}

	public boolean exploreDatabases() {
		return checkPermission(Permission.EXPLORE_DATABASE);
	}

	public boolean supportsLocking() {
		return checkPermission(Permission.LOCK_FILE);
	}

	private boolean checkPermission(Permission lookup) {
		for (Permission permission : Permission.values()) {
			if (permission.getCode().equals(lookup.getCode())) return true;
		}
		return false;
	}

}
