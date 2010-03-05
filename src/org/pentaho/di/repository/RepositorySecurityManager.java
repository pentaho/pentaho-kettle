package org.pentaho.di.repository;

import java.util.List;

import org.pentaho.di.core.exception.KettleException;

/**
 * This interface defines any security management related
 * APIs that are required for a repository.
 *  
 */
public interface RepositorySecurityManager extends IRepositoryService {

  public List<IUser> getUsers() throws KettleException;

  public void setUsers(List<IUser> users) throws KettleException;

  public ObjectId getUserID(String login) throws KettleException;

  public void delUser(ObjectId id_user) throws KettleException;

  public void delUser(String name) throws KettleException;

  public ObjectId[] getUserIDs() throws KettleException;

  public void saveUserInfo(IUser user) throws KettleException;

  public void renameUser(ObjectId id_user, String newname) throws KettleException;

  public IUser constructUser()  throws KettleException;

  public void updateUser(IUser user) throws KettleException;

  public void deleteUsers(List<IUser> users) throws KettleException;
  
  public IUser loadUserInfo(String username) throws KettleException;
}
