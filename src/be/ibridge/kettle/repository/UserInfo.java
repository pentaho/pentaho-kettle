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
import be.ibridge.kettle.core.Encr;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.value.Value;


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
				Row r = rep.getUser(getID());
				if (r!=null)
				{
					this.login  = r.searchValue("LOGIN").getString();
					password    = r.searchValue("PASSWORD").getString();
					name        = r.searchValue("NAME").getString();
					description = r.searchValue("DESCRIPTION").getString();
					enabled     = r.searchValue("ENABLED").getBoolean();
					id_profile  = r.searchValue("ID_PROFILE").getInteger();
					
					if (password!=null && password.startsWith("Encrypted ")) 
					{
						password = Encr.decryptPassword(password.substring(10));
					}
					else
					{ 
						password = Encr.decryptPassword(password);
					}
					
					profile = new ProfileMeta(rep, id_profile);
				}
				else
				{
					setID(-1L);
					throw new KettleDatabaseException("User ["+login+"] couldn't be found!");
				}
			}
		}
		catch(KettleDatabaseException dbe)
		{
			rep.log.logError(toString(), "Unable to load user with login ["+login+"] from the repository: "+dbe.getMessage());
			throw new KettleException("Unable to load user with login ["+login+"] from the repository", dbe);
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
			throw new KettleException("Incorrect password or login!");
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
			throw new KettleException("Error saving User in repository", dbe);
		}
		
	}
	
	public Row fillTableRow()
	{
		Row r = new Row();
		r.addValue( new Value("ID_USER", getID()) );
		r.addValue( new Value("LOGIN", login) );
		r.addValue( new Value("PASSWORD", Encr.encryptPassword(password)) );
		r.addValue( new Value("NAME", name) );
		r.addValue( new Value("DESCRIPTION", description) );
		r.addValue( new Value("ENABLED", enabled) );
		r.addValue( new Value("ID_PROFILE", profile.getID()) );

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

