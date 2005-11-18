 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/
 
package be.ibridge.kettle.repository;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleException;


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
	
	public static final String permission_type_desc[] =
		{
			"-",
			"READONLY",
			"ADMIN",
			"TRANS",
			"JOB",
			"SCHEMA"
		};

	public static final String permissionTypeLongDesc[] =
		{
			"-",
			"Read only access",
			"Administrator",
			"Use transformations",
			"Use jobs",
			"Use schema's"
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
			Row r = rep.getPermission(id_permission);
			setID(id_permission);
			String code = r.searchValue("CODE").getString();
			type = getType(code);
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to load permission information from the repository with id_premission="+id_permission, dbe);
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
		if (i<0 || i>=permission_type_desc.length) return null;
		return permission_type_desc[i];
	}
	
	public static final int getType(String str)
	{
		for (int i=0;i<permission_type_desc.length;i++)
		{
			if (permission_type_desc[i].equalsIgnoreCase(str)) return i;
		}
		
		for (int i=0;i<permissionTypeLongDesc.length;i++)
		{
			if (permissionTypeLongDesc[i].equalsIgnoreCase(str)) return i;
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
