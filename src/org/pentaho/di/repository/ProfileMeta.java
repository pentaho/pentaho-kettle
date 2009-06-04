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


/*
 * Created on 7-apr-2004
 *
 */

public class ProfileMeta 
{
//	private static Class<?> PKG = ProfileMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private long id;
	
	private String name;        // Long name
	private String description; // Description
	
	private List<PermissionMeta> permissions; // List of permissions in this profile...

	public ProfileMeta(String name, String description)
	{
		this.name = name;
		this.description = description;
		this.permissions = new ArrayList<PermissionMeta>();
	}

	public ProfileMeta()
	{
		this.name = null;
		this.description = null;
		this.permissions = new ArrayList<PermissionMeta>();
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
	
	public void addPermission(PermissionMeta permission)
	{
		permissions.add(permission);
	}

	public void addPermission(int p, PermissionMeta permission)
	{
		permissions.add(p, permission);
	}

	public PermissionMeta getPermission(int i)
	{
		return (PermissionMeta)permissions.get(i);
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
	
	public void removeAllPermissions()
	{
		permissions.clear();
	}

	public int indexOfPermission(PermissionMeta permission)
	{
		return permissions.indexOf(permission);
	}
	
	public long getID()
	{
		return id;
	}
	
	public void setID(long id)
	{
		this.id = id;
	}
	
	// Helper functions...
	
	public boolean isReadonly()
	{
		for (int i=0;i<nrPermissions();i++)
		{
			PermissionMeta pi = getPermission(i);
			if (pi.isReadonly()) return true;
		}
		return false;
	}

	public boolean isAdministrator()
	{
		for (int i=0;i<nrPermissions();i++)
		{
			PermissionMeta pi = getPermission(i);
			if (pi.isAdministrator()) return true;
		}
		return false;
	}

	public boolean useTransformations()
	{
		for (int i=0;i<nrPermissions();i++)
		{
			PermissionMeta pi = getPermission(i);
			if (pi.useTransformations()) return true;
		}
		return false;
	}

	public boolean useJobs()
	{
		for (int i=0;i<nrPermissions();i++)
		{
			PermissionMeta pi = getPermission(i);
			if (pi.useJobs()) return true;
		}
		return false;
	}

	public boolean useSchemas()
	{
		for (int i=0;i<nrPermissions();i++)
		{
			PermissionMeta pi = getPermission(i);
			if (pi.useSchemas()) return true;
		}
		return false;
	}
}
