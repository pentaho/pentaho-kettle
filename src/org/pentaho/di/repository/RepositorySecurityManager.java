package org.pentaho.di.repository;

import java.util.EnumSet;
import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.ProfileMeta.Permission;

/**
 * This interface defines any security management related
 * APIs that are required for a repository.
 *  
 */
public interface RepositorySecurityManager {

  public void saveProfile(ProfileMeta profileMeta) throws KettleException;

  public ProfileMeta loadProfileMeta(ObjectId id_profile) throws KettleException;

//  public Permission loadPermission(ObjectId id_permission) throws KettleException;

//  public ObjectId[] getPermissionIDs(ObjectId id_profile) throws KettleException;

  public String[] getProfiles() throws KettleException;

  public ObjectId getProfileID(String profilename) throws KettleException;

  public void renameProfile(ObjectId id_profile, String newname) throws KettleException;

  public void delProfile(ObjectId id_profile) throws KettleException;

  public List<UserInfo> getUsers() throws KettleException;

  public void setUsers(List<UserInfo> users) throws KettleException;

  public ObjectId getUserID(String login) throws KettleException;

  public void delUser(ObjectId id_user) throws KettleException;

  public void delUser(String name) throws KettleException;

  public ObjectId[] getUserIDs() throws KettleException;

  public String[] getUserLogins() throws KettleException;

  public void saveUserInfo(UserInfo userInfo) throws KettleException;

  public void renameUser(ObjectId id_user, String newname) throws KettleException;

  public IRole constructRole()  throws KettleException;

  public void createRole(IRole role) throws KettleException;

  public IRole getRole(String name) throws KettleException;

  public List<IRole> getRoles() throws KettleException;

  public void setRoles(List<IRole> roles) throws KettleException;

  public void updateUser(UserInfo role) throws KettleException;

  public void updateRole(IRole role) throws KettleException;

  public void deleteUsers(List<UserInfo> users) throws KettleException;
  
  public void deleteRoles(List<IRole> roles) throws KettleException;

  public void deleteRole(String name) throws KettleException;
  
  public List<String> getAllUsers() throws KettleException;
  
  public List<String> getAllRoles() throws KettleException;

  public UserInfo loadUserInfo(String username) throws KettleException;
}
