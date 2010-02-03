package org.pentaho.di.repository;

import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.ProfileMeta.Permission;

public interface RepositoryUserInterface {

  public void saveProfile(ProfileMeta profileMeta) throws KettleException;

  public ProfileMeta loadProfileMeta(ObjectId id_profile) throws KettleException;

  public Permission loadPermission(ObjectId id_permission) throws KettleException;

  public ObjectId[] getPermissionIDs(ObjectId id_profile) throws KettleException;

  public String[] getProfiles() throws KettleException;

  public ObjectId getProfileID(String profilename) throws KettleException;

  public void renameProfile(ObjectId id_profile, String newname) throws KettleException;

  public void delProfile(ObjectId id_profile) throws KettleException;

  /**
   * @param userinfo the UserInfo object to set
   */
  public void setUserInfo(UserInfo userinfo);

  public UserInfo getUserInfo();

  public List<UserInfo> getUsers() throws KettleException;

  public void setUsers(List<UserInfo> roles) throws KettleException;

  public ObjectId getUserID(String login) throws KettleException;

  public void delUser(ObjectId id_user) throws KettleException;

  public void delUser(String name) throws KettleException;

  public ObjectId[] getUserIDs() throws KettleException;

  public String[] getUserLogins() throws KettleException;

  public UserInfo loadUserInfo(String login) throws KettleException;

  public UserInfo loadUserInfo(String login, String password) throws KettleException;

  public void saveUserInfo(UserInfo userInfo) throws KettleException;

  public void renameUser(ObjectId id_user, String newname) throws KettleException;

  public void createRole(RoleInfo role) throws KettleException;

  public RoleInfo getRole(String name) throws KettleException;

  public List<RoleInfo> getRoles() throws KettleException;

  public void setRoles(List<RoleInfo> roles) throws KettleException;

  public void updateUser(UserInfo role) throws KettleException;

  public void updateRole(RoleInfo role) throws KettleException;

  public void deleteRole(RoleInfo role) throws KettleException;

  public void deleteRole(String name) throws KettleException;
  
  public List<String> getAllUsers() throws KettleException;
  
  public List<String> getAllRoles() throws KettleException;

  public List<String> getAllUsersInRole(String role) throws KettleException;

  public List<String> getRolesForUser(String userName) throws KettleException;

}
