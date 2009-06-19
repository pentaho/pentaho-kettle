package org.pentaho.di.repository.kdr;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleSecurityException;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.ProfileMeta;
import org.pentaho.di.repository.RepositoryCapabilities;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.repository.RepositoryOperation;
import org.pentaho.di.repository.RepositorySecurityProvider;
import org.pentaho.di.repository.UserInfo;
import org.pentaho.di.repository.ProfileMeta.Permission;
import org.pentaho.di.repository.kdr.delegates.RepositoryConnectionDelegate;
import org.pentaho.di.repository.kdr.delegates.RepositoryPermissionDelegate;
import org.pentaho.di.repository.kdr.delegates.RepositoryProfileDelegate;
import org.pentaho.di.repository.kdr.delegates.RepositoryUserDelegate;

public class KettleDatabaseRepositorySecurityProvider implements RepositorySecurityProvider {

	private RepositoryMeta repositoryMeta;
	private UserInfo userInfo;
	private RepositoryCapabilities	capabilities;
	private KettleDatabaseRepository repository;
	private RepositoryUserDelegate	userDelegate;
	private RepositoryProfileDelegate	profileDelegate;
	private RepositoryPermissionDelegate	permissionDelegate;
	private RepositoryConnectionDelegate	connectionDelegate;
	
	/**
	 * @param repository 
	 * @param userInfo
	 */
	public KettleDatabaseRepositorySecurityProvider(KettleDatabaseRepository repository, RepositoryMeta repositoryMeta, UserInfo userInfo) {
		this.repository = repository;
		this.repositoryMeta = repositoryMeta;
		this.userInfo = userInfo;
		this.capabilities = repositoryMeta.getRepositoryCapabilities();
		
		// This object is initialized last in the KettleDatabaseRepository constructor.
		// As such it's safe to keep references here to the delegates...
		//
		userDelegate = repository.userDelegate;
		profileDelegate = repository.profileDelegate;
		permissionDelegate = repository.permissionDelegate;
		connectionDelegate = repository.connectionDelegate;
	}

	public UserInfo getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(UserInfo userInfo) {
		this.userInfo = userInfo;
	}
	
	/**
	 * @return the repositoryMeta
	 */
	public RepositoryMeta getRepositoryMeta() {
		return repositoryMeta;
	}

	/**
	 * @param repositoryMeta the repositoryMeta to set
	 */
	public void setRepositoryMeta(RepositoryMeta repositoryMeta) {
		this.repositoryMeta = repositoryMeta;
	}
	
	public boolean isReadOnly() {
		return capabilities.isReadOnly() || ( userInfo.isReadOnly() && !userInfo.isAdministrator());
	}

	public boolean supportsUsers() {
		return capabilities.supportsUsers();
	}
	
	public boolean supportsRevisions() {
		return capabilities.supportsRevisions();
	}
	
	public boolean supportsMetadata() {
		return capabilities.supportsMetadata();
	}
	
	public boolean isLockingPossible() {
		return capabilities.supportsLocking() && ( userInfo.supportsLocking() || userInfo.isAdministrator());
	}

	public void validateAction(RepositoryOperation...operations) throws KettleException, KettleSecurityException {

		// If there is no user available, we fail
		//
		if (userInfo==null) {
			throw new KettleException("A valid user is needed to use this repository");
		}
		
		// No questions asked for an administrator...
		//
		if (userInfo.isAdministrator()) {
			return;
		}
		
		// If the user is not enabled, not a single operation can take place...
		//
		if (!userInfo.isEnabled()) {
			throw new KettleException("The user is not enabled");
		}
		
		for (RepositoryOperation operation : operations) {
			switch(operation) {
			case READ_TRANSFORMATION :
				if (!userInfo.useTransformations()) throw new KettleException(operation+" : user can't use transformations");
				break;
			case MODIFY_TRANSFORMATION : 
				if (userInfo.isReadOnly()) throw new KettleException(operation+" : user is read-only");
				if (capabilities.isReadOnly()) throw new KettleException(operation+" : repository is read-only");
				if (!userInfo.useTransformations()) throw new KettleException(operation+" : user can't use transformations");
				break;
			case DELETE_TRANSFORMATION : 
				if (userInfo.isReadOnly()) throw new KettleException(operation+" : user is read-only");
				if (capabilities.isReadOnly()) throw new KettleException(operation+" : repository is read-only");
				if (!userInfo.useTransformations()) throw new KettleException(operation+" : user can't use transformations");
				break;
			case EXECUTE_TRANSFORMATION : 
				if (!userInfo.useTransformations()) throw new KettleException(operation+" : user can't use transformations");
				break;
			
			case READ_JOB :
				if (!userInfo.useJobs()) throw new KettleException(operation+" : user can't use jobs");
				break;
			case MODIFY_JOB :
				if (userInfo.isReadOnly()) throw new KettleException(operation+" : user is read-only");
				if (capabilities.isReadOnly()) throw new KettleException(operation+" : repository is read-only");
				if (!userInfo.useJobs()) throw new KettleException(operation+" : user can't use jobs");
				break;
			case DELETE_JOB :
				if (userInfo.isReadOnly()) throw new KettleException(operation+" : user is read-only");
				if (capabilities.isReadOnly()) throw new KettleException(operation+" : repository is read-only");
				if (!userInfo.useJobs()) throw new KettleException(operation+" : user can't use jobs");
				break;
			case EXECUTE_JOB :
				if (!userInfo.useJobs()) throw new KettleException(operation+" : user can't use jobs");
				break;
			
			case MODIFY_DATABASE :
				if (userInfo.isReadOnly()) throw new KettleException(operation+" : user is read-only");
				if (capabilities.isReadOnly()) throw new KettleException(operation+" : repository is read-only");
				if (!userInfo.useDatabases()) throw new KettleException(operation+" : user can't use databases");
				break;
			case DELETE_DATABASE :
				if (userInfo.isReadOnly()) throw new KettleException(operation+" : user is read-only");
				if (capabilities.isReadOnly()) throw new KettleException(operation+" : repository is read-only");
				if (!userInfo.useDatabases()) throw new KettleException(operation+" : user can't use databases");
				break;
			case EXPLORE_DATABASE :
				if (!userInfo.exploreDatabases()) throw new KettleException(operation+" : user can't explore databases");
				break;
	
			case MODIFY_SLAVE_SERVER:
			case MODIFY_CLUSTER_SCHEMA:
			case MODIFY_PARTITION_SCHEMA:
				if (userInfo.isReadOnly()) throw new KettleException(operation+" : user is read-only");
				if (capabilities.isReadOnly()) throw new KettleException(operation+" : repository is read-only");
				break;
			case DELETE_SLAVE_SERVER:
			case DELETE_CLUSTER_SCHEMA:
			case DELETE_PARTITION_SCHEMA:
				if (userInfo.isReadOnly()) throw new KettleException(operation+" : user is read-only");
				if (capabilities.isReadOnly()) throw new KettleException(operation+" : repository is read-only");
				break;

			default:
				throw new KettleException("Operation ["+operation+"] is unknown to the security handler.");
			}
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
    // ProfileMeta

    public ProfileMeta loadProfileMeta(ObjectId id_profile) throws KettleException {
    	return profileDelegate.loadProfileMeta(new ProfileMeta(), id_profile);
    }
    
    public void saveProfile(ProfileMeta profileMeta) throws KettleException {
    	profileDelegate.saveProfileMeta(profileMeta);
    }
    
    
    // UserInfo

    public UserInfo loadUserInfo(String login) throws KettleException {
    	return userDelegate.loadUserInfo(new UserInfo(), login);
    }
    
    public UserInfo loadUserInfo(String login, String password) throws KettleException {
    	return userDelegate.loadUserInfo(new UserInfo(), login, password);
    }
    
    public void saveUserInfo(UserInfo userInfo) throws KettleException {
    	userDelegate.saveUserInfo(userInfo);
    }
	 
    
    // PermissionMeta
    
	/**
	 * Load a permission from the repository
	 * 
	 * @param id_permission The id of the permission to load
	 * @throws KettleException
	 */
	public Permission loadPermission(ObjectId id_permission) throws KettleException {
		return permissionDelegate.loadPermissionMeta(id_permission);
	}
    
	
	public synchronized void delUser(ObjectId id_user) throws KettleException
	{
		String sql = "DELETE FROM "+repository.quoteTable(KettleDatabaseRepository.TABLE_R_USER)+" WHERE "+repository.quote(KettleDatabaseRepository.FIELD_USER_ID_USER)+" = " + id_user;
		repository.execStatement(sql);
	}

	public synchronized void delProfile(ObjectId id_profile) throws KettleException
	{
		String sql = "DELETE FROM "+repository.quoteTable(KettleDatabaseRepository.TABLE_R_PROFILE)+" WHERE "+repository.quote(KettleDatabaseRepository.FIELD_PROFILE_ID_PROFILE)+" = " + id_profile;
		repository.execStatement(sql);
	}

	public synchronized void delProfilePermissions(ObjectId id_profile) throws KettleException
	{
		String sql = "DELETE FROM "+repository.quoteTable(KettleDatabaseRepository.TABLE_R_PROFILE_PERMISSION)+" WHERE "+repository.quote(KettleDatabaseRepository.FIELD_PROFILE_PERMISSION_ID_PROFILE)+" = " + id_profile;
		repository.execStatement(sql);
	}
    

	public synchronized ObjectId getUserID(String login) throws KettleException {
		return userDelegate.getUserID(login);
	}

	public ObjectId[] getUserIDs() throws KettleException
	{
		return connectionDelegate.getIDs("SELECT "+repository.quote(KettleDatabaseRepository.FIELD_USER_ID_USER)+" FROM "+repository.quoteTable(KettleDatabaseRepository.TABLE_R_USER));
	}

	public synchronized String[] getUserLogins() throws KettleException
	{
		String loginField = repository.quote(KettleDatabaseRepository.FIELD_USER_LOGIN);
		return connectionDelegate.getStrings("SELECT "+loginField+" FROM "+repository.quoteTable(KettleDatabaseRepository.TABLE_R_USER)+" ORDER BY "+loginField);
	}

	public ObjectId[] getPermissionIDs(ObjectId id_profile) throws KettleException
	{
		return connectionDelegate.getIDs("SELECT "+repository.quote(KettleDatabaseRepository.FIELD_PROFILE_PERMISSION_ID_PERMISSION)+" FROM "+repository.quoteTable(KettleDatabaseRepository.TABLE_R_PROFILE_PERMISSION)+" WHERE "+repository.quote(KettleDatabaseRepository.FIELD_PROFILE_PERMISSION_ID_PROFILE)+" = " + id_profile);
	}

	public synchronized String[] getProfiles() throws KettleException
	{
		String nameField = repository.quote(KettleDatabaseRepository.FIELD_PROFILE_NAME);
		return connectionDelegate.getStrings("SELECT "+nameField+" FROM "+repository.quoteTable(KettleDatabaseRepository.TABLE_R_PROFILE)+" ORDER BY "+nameField);
	}

	public ObjectId getProfileID(String profilename) throws KettleException {
		return profileDelegate.getProfileID(profilename);
	}
	
	public synchronized void renameUser(ObjectId id_user, String newname) throws KettleException {
		userDelegate.renameUser(id_user, newname);
	}

	public synchronized void renameProfile(ObjectId id_profile, String newname) throws KettleException {
		profileDelegate.renameProfile(id_profile, newname);
	}
}
