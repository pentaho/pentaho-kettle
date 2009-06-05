package org.pentaho.di.repository;

import org.pentaho.di.core.exception.KettleException;

public interface RepositorySecurityInterface {

    public void saveProfile(ProfileMeta profileMeta) throws KettleException;
    public ProfileMeta loadProfileMeta(long id_profile) throws KettleException;
    
	public PermissionMeta loadPermissionMeta(long id_permission) throws KettleException;

	public long[] getPermissionIDs(long id_profile) throws KettleException;

	public String[] getProfiles() throws KettleException;

	public long getProfileID(String profilename) throws KettleException;

	public void renameProfile(long id_profile, String newname) throws KettleException;

	public void delProfile(long id_profile) throws KettleException;

	/**
	 * @param userinfo the UserInfo object to set
	 */
	public void setUserInfo(UserInfo userinfo);
	
	public UserInfo getUserInfo();

	public long getUserID(String login) throws KettleException;

	public void delUser(long id_user) throws KettleException;

	public long[] getUserIDs() throws KettleException;

	public String[] getUserLogins() throws KettleException;

    public UserInfo loadUserInfo(String login) throws KettleException;
    
    public UserInfo loadUserInfo(String login, String password) throws KettleException;
    
    public void saveUserInfo(UserInfo userInfo) throws KettleException;

    public void renameUser(long id_user, String newname) throws KettleException;


}
