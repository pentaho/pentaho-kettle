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
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;


/*
 * Created on 7-apr-2004
 *
 */

public class UserInfo 
{
	private long id;
	
	private String login;       // Login ID
	private String password;    // Password
	private String name;        // Long name
	private String description; // Description
	private boolean enabled;    // Enabled: yes or no
	
	private ProfileMeta profile; // user profile information
	
	public UserInfo(String login, String password, String name, String description, boolean enabled, ProfileMeta profile)
	{
		this.login = login;
		this.password = password;
		this.name = name;
		this.description = description;
		this.enabled = enabled;
		this.profile = profile;
	}
	
	public UserInfo()
	{
		this.login = null;
		this.password = null;
		this.name = null;
		this.description = null;
		this.enabled = true;
		this.profile = null;
	}

	// Load user with login from repository, don't verify password...
	public UserInfo(Repository rep, String login)
		throws KettleException
	{
		try
		{
			long id_profile;
			
			setID( rep.getUserID(login) );
			if (getID()>0)
			{
				RowMetaAndData r = rep.getUser(getID());
				if (r!=null)
				{
					this.login  = r.getString("LOGIN", null);
					password    = Encr.decryptPassword( r.getString("PASSWORD", null) );
					name        = r.getString("NAME", null);
					description = r.getString("DESCRIPTION", null);
					enabled     = r.getBoolean("ENABLED", false);
					id_profile  = r.getInteger("ID_PROFILE", 0);
					profile = new ProfileMeta(rep, id_profile);
				}
				else
				{
					setID(-1L);
					throw new KettleDatabaseException(Messages.getString("UserInfo.Error.UserNotFound", login));
				}
			}
            else
            {
                setID(-1L);
                throw new KettleDatabaseException(Messages.getString("UserInfo.Error.UserNotFound", login));
            }
		}
		catch(KettleDatabaseException dbe)
		{
            rep.log.logError(toString(), Messages.getString("UserInfo.Error.UserNotLoaded", login, dbe.getMessage()));
			throw new KettleException(Messages.getString("UserInfo.Error.UserNotLoaded", login, ""), dbe);
		}
	}
	

	// Load user with login from repository and verify the password...
	public UserInfo(Repository rep, String login, String passwd)
		throws KettleException
	{
		this(rep, login);
		
		// Verify the password:
		if ( getID()<0 || !passwd.equals(getPassword()) )
		{
            throw new KettleDatabaseException(Messages.getString("UserInfo.Error.IncorrectPasswortLogin"));
		}
	}
	
	public void saveRep(Repository rep)
		throws KettleException
	{
		try
		{
			// Do we have a user id already?
			if (getID()<=0)
			{
				setID(rep.getUserID(login)); // Get userid in the repository
			}
			
			if (getID()<=0)
			{
				// This means the login doesn't exist in the database 
				// and we have no id, so we don't know the old one...
				// Just grab the next user ID and do an insert:
				setID(rep.getNextUserID());
				rep.insertTableRow("R_USER", fillTableRow()); 
			}
			else
			{
				rep.updateTableRow("R_USER", "ID_USER", fillTableRow());
			}
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException(Messages.getString("UserInfo.Error.SavingUser", login), dbe);
		}
		
	}
	
	public RowMetaAndData fillTableRow()
	{
        RowMetaAndData r = new RowMetaAndData();
		r.addValue( new ValueMeta("ID_USER", ValueMetaInterface.TYPE_INTEGER), new Long(getID()) );
		r.addValue( new ValueMeta("LOGIN", ValueMetaInterface.TYPE_STRING), login );
		r.addValue( new ValueMeta("PASSWORD", ValueMetaInterface.TYPE_STRING), Encr.encryptPassword(password) );
		r.addValue( new ValueMeta("NAME", ValueMetaInterface.TYPE_STRING), name );
		r.addValue( new ValueMeta("DESCRIPTION", ValueMetaInterface.TYPE_STRING), description );
		r.addValue( new ValueMeta("ENABLED", ValueMetaInterface.TYPE_BOOLEAN), Boolean.valueOf(enabled) );
		r.addValue( new ValueMeta("ID_PROFILE", ValueMetaInterface.TYPE_INTEGER), Long.valueOf(profile.getID()) );

		return r;
	}

	
	public void setLogin(String login)
	{
		this.login = login;
	}
	
	public String getLogin()
	{
		return login;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}
	
	public String getPassword()
	{
		return password;
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
	
	public void setEnabled()
	{
		setEnabled( true );
	}

	public void setDisabled()
	{
		setEnabled( false );
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	public void setProfile(ProfileMeta profile)
	{
		this.profile = profile;
	}
	
	public ProfileMeta getProfile()
	{
		return profile;
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
		if (profile==null) return true;
		return profile.isReadonly();
	}
	
	public boolean isAdministrator()
	{
		if (profile==null) return false;
		return profile.isAdministrator();
	}
	
	public boolean useTransformations()
	{
		if (profile==null) return false;
		return profile.useTransformations();
	}

	public boolean useJobs()
	{
		if (profile==null) return false;
		return profile.useJobs();
	}

	public boolean useSchemas()
	{
		if (profile==null) return false;
		return profile.useSchemas();
	}
}

