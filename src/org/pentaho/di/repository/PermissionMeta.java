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

import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;


/**
 * This class handles the different kinds of permissions that can be set on a profile.
 * 
 * @author Matt
 * @since 7-apr-2004
 *
 */
public class PermissionMeta 
{
	private long id;
	private int  type;
	
	public static final int TYPE_PERMISSION_NONE             = 0;
	public static final int TYPE_PERMISSION_READ_ONLY        = 1;
	public static final int TYPE_PERMISSION_ADMIN            = 2;
	public static final int TYPE_PERMISSION_TRANSFORMATION   = 3;
	public static final int TYPE_PERMISSION_JOB              = 4;
	public static final int TYPE_PERMISSION_SCHEMA           = 5;
	
	public static final String permissionTypeCode[] =
		{
			"-",
			"READONLY",
			"ADMIN",
			"TRANS",
			"JOB",
			"SCHEMA"
		};

	public static final String permissionTypeDesc[] =
	{
		 "-",
		 Messages.getString("PermissionMeta.Permission.ReadOnly"),
		 Messages.getString("PermissionMeta.Permission.Administrator"),
		 Messages.getString("PermissionMeta.Permission.UseTransformations"),
		 Messages.getString("PermissionMeta.Permission.UseJobs"),
		 Messages.getString("PermissionMeta.Permission.UseSchemas")
	};
	
	public PermissionMeta(int type)
	{
		this.type = type;
	}

	public PermissionMeta(String stype)
	{
		this.type = getType(stype);
	}

	public PermissionMeta(Repository rep, long id_permission)
		throws KettleException
	{
		try
		{
			RowMetaAndData r = rep.getPermission(id_permission);
			setID(id_permission);
			String code = r.getString("CODE", null);
			type = getType(code);
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException(Messages.getString("PermissionMeta.Error.LoadPermisson", Long.toString(id_permission)), dbe);
		}
	}
	
	public void setType(int type)
	{
		this.type = type;
	}
	
	public int getType()
	{
		return type;
	}
	
	public String getTypeDesc()
	{
		return getTypeDesc(type);
	}
	
	public static final String getTypeDesc(int i)
	{
		if (i<0 || i>=permissionTypeCode.length) return null;
		return permissionTypeCode[i];
	}
	
	public static final int getType(String str)
	{
		for (int i=0;i<permissionTypeCode.length;i++)
		{
			if (permissionTypeCode[i].equalsIgnoreCase(str)) return i;
		}
		
		for (int i=0;i<permissionTypeDesc.length;i++)
		{
			if (permissionTypeDesc[i].equalsIgnoreCase(str)) return i;
		}
		
		return TYPE_PERMISSION_NONE;
	}
	
	public long getID()
	{
		return id;
	}
	
	public void setID(long id)
	{
		this.id = id;
	}
	
	public boolean isReadonly()
	{
		return type == TYPE_PERMISSION_READ_ONLY;
	}

	public boolean isAdministrator()
	{
		return type == TYPE_PERMISSION_ADMIN;
	}

	public boolean useTransformations()
	{
		return type == TYPE_PERMISSION_TRANSFORMATION;
	}

	public boolean useJobs()
	{
		return type == TYPE_PERMISSION_JOB;
	}

	public boolean useSchemas()
	{
		return type == TYPE_PERMISSION_SCHEMA;
	}
	
	public String toString()
	{
		return getTypeDesc();
	}
}
