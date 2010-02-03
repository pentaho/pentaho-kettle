package org.pentaho.di.repository.filerep;

import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleSecurityException;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.ProfileMeta;
import org.pentaho.di.repository.RepositoryCapabilities;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.repository.RepositoryOperation;
import org.pentaho.di.repository.RepositorySecurityProvider;
import org.pentaho.di.repository.RoleInfo;
import org.pentaho.di.repository.UserInfo;
import org.pentaho.di.repository.ProfileMeta.Permission;

public class KettleFileRepositorySecurityProvider implements RepositorySecurityProvider {

	private RepositoryMeta	repositoryMeta;
	private RepositoryCapabilities	capabilities;

	public KettleFileRepositorySecurityProvider(RepositoryMeta repositoryMeta) {
		this.repositoryMeta = repositoryMeta;
		this.capabilities = repositoryMeta.getRepositoryCapabilities();
	}
	
	public UserInfo getUserInfo() {
		return null;
	}

	public void setUserInfo(UserInfo userInfo) {
	}
	
	public RepositoryMeta getRepositoryMeta() {
		return repositoryMeta;
	}

	public void validateAction(RepositoryOperation...operations) throws KettleException, KettleSecurityException {

		for (RepositoryOperation operation : operations) {
			switch(operation) {
			case READ_TRANSFORMATION :
				break;
			case MODIFY_TRANSFORMATION : 
				if (capabilities.isReadOnly()) throw new KettleException(operation+" : repository is read-only");
				break;
			case DELETE_TRANSFORMATION : 
				if (capabilities.isReadOnly()) throw new KettleException(operation+" : repository is read-only");
				break;
			case EXECUTE_TRANSFORMATION : 
				break;
			case LOCK_TRANSFORMATION : 
				break;
			
			case READ_JOB :
				break;
			case MODIFY_JOB :
				if (capabilities.isReadOnly()) throw new KettleException(operation+" : repository is read-only");
				break;
			case DELETE_JOB :
				if (capabilities.isReadOnly()) throw new KettleException(operation+" : repository is read-only");
				break;
			case EXECUTE_JOB :
				break;
			case LOCK_JOB :
				break;
			
			case MODIFY_DATABASE :
				if (capabilities.isReadOnly()) throw new KettleException(operation+" : repository is read-only");
				break;
			case DELETE_DATABASE :
				if (capabilities.isReadOnly()) throw new KettleException(operation+" : repository is read-only");
				break;
			case EXPLORE_DATABASE :
				break;

			case MODIFY_SLAVE_SERVER:
			case MODIFY_CLUSTER_SCHEMA:
			case MODIFY_PARTITION_SCHEMA:
				if (capabilities.isReadOnly()) throw new KettleException(operation+" : repository is read-only");
				break;
			case DELETE_SLAVE_SERVER:
			case DELETE_CLUSTER_SCHEMA:
			case DELETE_PARTITION_SCHEMA:
				if (capabilities.isReadOnly()) throw new KettleException(operation+" : repository is read-only");
				break;

			default:
				throw new KettleException("Operation ["+operation+"] is unknown to the security handler.");
				
			}
		}
	}

	public boolean isReadOnly() {
		return capabilities.isReadOnly();
	}

	public boolean supportsUsers() {
		return capabilities.supportsUsers();
	}
	
	public boolean supportsRevisions() {
		return capabilities.supportsRevisions();
	}
	
	public boolean isLockingPossible() {
		return capabilities.supportsLocking();
	}
	
	public boolean supportsMetadata() {
		return capabilities.supportsMetadata();
	}
	
	public boolean allowsVersionComments() {
		return false;
	}
	
	public boolean isVersionCommentMandatory() {
		return false;
	}

	// The file repository does not support users
	//
	
	public void delProfile(ObjectId id_profile) throws KettleException {}
	public void delUser(ObjectId id_user) throws KettleException {}
	public ObjectId[] getPermissionIDs(ObjectId id_profile) throws KettleException { return null; }
	public ObjectId getProfileID(String profilename) throws KettleException { return null; }
	public String[] getProfiles() throws KettleException { return null; }
	public ObjectId getUserID(String login) throws KettleException { return null; }
	public ObjectId[] getUserIDs() throws KettleException { return null; }
	public String[] getUserLogins() throws KettleException { return null; }
	public Permission loadPermission(ObjectId id_permission) throws KettleException { return null; }
	public ProfileMeta loadProfileMeta(ObjectId id_profile) throws KettleException { return null; }
	public UserInfo loadUserInfo(String login) throws KettleException { return null; }
	public UserInfo loadUserInfo(String login, String password) throws KettleException { return null; }
	public void renameProfile(ObjectId id_profile, String newname) throws KettleException { }
	public void renameUser(ObjectId id_user, String newname) throws KettleException { }
	public void saveProfile(ProfileMeta profileMeta) throws KettleException {}
	public void saveUserInfo(UserInfo userInfo) throws KettleException {}

	public void createRole(RoleInfo role) throws KettleException {}
	public void deleteRole(RoleInfo role) throws KettleException {}
	public RoleInfo getRole(String name) throws KettleException {return null;}
	public List<RoleInfo> getRoles() throws KettleException {return null;}
	public void updateRole(RoleInfo role) throws KettleException {}

	public List<UserInfo> getUsers() throws KettleException {
		// TODO Auto-generated method stub
		return null;
	}

	public void setRoles(List<RoleInfo> roles) throws KettleException {
		// TODO Auto-generated method stub
		
	}

	public void setUsers(List<UserInfo> roles) throws KettleException {
		// TODO Auto-generated method stub
		
	}

	public void delUser(String name) throws KettleException {
		// TODO Auto-generated method stub
		
	}

	public void deleteRole(String name) throws KettleException {
		// TODO Auto-generated method stub
		
	}

	public void updateUser(UserInfo role) throws KettleException {
		// TODO Auto-generated method stub
		
	}

  public List<String> getAllRoles() throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  public List<String> getAllUsers() throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  public List<String> getAllUsersInRole(String role) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  public List<String> getRolesForUser(String userName) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }
}
