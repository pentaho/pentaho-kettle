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

import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;


/*
 * Created on 7-apr-2004
 *
 */

public class ProfileMeta 
{
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
	
	public ProfileMeta(Repository rep, long id_profile)
		throws KettleException
	{
		try
		{
			RowMetaAndData r = rep.getProfile(id_profile);
			if (r!=null)
			{
				setID(id_profile);
				name = r.getString("NAME", null);
				description = r.getString("DESCRIPTION", null);
				
				long pid[] = rep.getPermissionIDs(id_profile);
				
				// System.out.println("Profile "+name+" has "+pid.length+" permissions.");
				
				permissions = new ArrayList<PermissionMeta>();
			
				for (int i=0;i<pid.length;i++)
				{
					PermissionMeta pi = new PermissionMeta(rep, pid[i]);
					//System.out.println("Adding permission #"+i+" : "+pi+", id="+pi.getID());
					if (pi.getID()>0) addPermission(pi);
				}
			}
			else
			{
				throw new KettleException(Messages.getString("ProfileMeta.Error.NotFound", Long.toString(id_profile)));
			}
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException(Messages.getString("ProfileMeta.Error.NotCreated", Long.toString(id_profile)), dbe);
		}
	}
	
	public boolean saveRep(Repository rep)
		throws KettleException
	{
		try
		{
			if (getID()<=0)
			{
				setID(rep.getProfileID(getName()));
			}
			
			if (getID()<=0) // Insert...
			{
				setID(rep.getNextProfileID());
				
				// First save Profile info
				rep.insertTableRow("R_PROFILE", fillTableRow());
				
				// Save permission-profile relations
				saveProfilePermissions(rep);			
			}
			else  // Update
			{
				// First save permissions
				rep.updateTableRow("R_PROFILE", "ID_PROFILE", fillTableRow());
				
				// Then save profile_permission relationships
				rep.delProfilePermissions(getID());
				
				// Save permission-profile relations
				saveProfilePermissions(rep);			
			}
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException(Messages.getString("ProfileMeta.Error.NotSaved", Long.toString(getID())), dbe);
		}
		return true;
	}
	
	public RowMetaAndData fillTableRow()
	{
		RowMetaAndData r = new RowMetaAndData();
		r.addValue(new ValueMeta("ID_PROFILE", ValueMetaInterface.TYPE_INTEGER), new Long(getID()));
		r.addValue(new ValueMeta("NAME", ValueMetaInterface.TYPE_STRING), name);
		r.addValue(new ValueMeta("DESCRIPTION", ValueMetaInterface.TYPE_STRING), description);
		
		return r;		
	}
	
	private void saveProfilePermissions(Repository rep)
		throws KettleException
	{
		try
		{
			// Then save profile_permission relationships
			for (int i=0;i<nrPermissions();i++)
			{
				PermissionMeta pi = getPermission(i);
				long id_permission = rep.getPermissionID(pi.getTypeDesc());
					
				RowMetaAndData pr = new RowMetaAndData();
				pr.addValue(new ValueMeta("ID_PROFILE", ValueMetaInterface.TYPE_INTEGER), new Long(getID()));
				pr.addValue(new ValueMeta("ID_PERMISSION", ValueMetaInterface.TYPE_INTEGER), new Long(id_permission));
					
				rep.insertTableRow("R_PROFILE_PERMISSION", pr);
			}
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException(Messages.getString("ProfileMeta.Error.PermissionNotSaved", Long.toString(getID())), dbe);
		}
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
