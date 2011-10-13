/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */
package org.pentaho.di.repository.kdr;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleSecurityException;
import org.pentaho.di.repository.BaseRepositorySecurityProvider;
import org.pentaho.di.repository.IUser;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.RepositoryCapabilities;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.repository.RepositoryOperation;
import org.pentaho.di.repository.RepositorySecurityManager;
import org.pentaho.di.repository.RepositorySecurityProvider;
import org.pentaho.di.repository.UserInfo;
import org.pentaho.di.repository.kdr.delegates.KettleDatabaseRepositoryConnectionDelegate;
import org.pentaho.di.repository.kdr.delegates.KettleDatabaseRepositoryUserDelegate;

public class KettleDatabaseRepositorySecurityProvider extends BaseRepositorySecurityProvider implements
    RepositorySecurityProvider, RepositorySecurityManager {

  private RepositoryCapabilities capabilities;

  private KettleDatabaseRepository repository;

  private KettleDatabaseRepositoryUserDelegate userDelegate;

  private KettleDatabaseRepositoryConnectionDelegate connectionDelegate;

  /**
   * @param repository 
   * @param userInfo
   */
  public KettleDatabaseRepositorySecurityProvider(KettleDatabaseRepository repository, RepositoryMeta repositoryMeta,
      IUser userInfo) {
    super(repositoryMeta, userInfo);
    this.repository = repository;
    this.capabilities = repositoryMeta.getRepositoryCapabilities();

    // This object is initialized last in the KettleDatabaseRepository constructor.
    // As such it's safe to keep references here to the delegates...
    //
    userDelegate = repository.userDelegate;
    connectionDelegate = repository.connectionDelegate;
  }

  public boolean isReadOnly() {
    return capabilities.isReadOnly();
  }

  public boolean isLockingPossible() {
    return capabilities.supportsLocking();
  }

  public boolean allowsVersionComments() {
    return false;
  }

  public boolean isVersionCommentMandatory() {
    return false;
  }

  // UserInfo

  public IUser loadUserInfo(String login) throws KettleException {
    return userDelegate.loadUserInfo(new UserInfo(), login);
  }

  public void saveUserInfo(IUser userInfo) throws KettleException {
    userDelegate.saveUserInfo(userInfo);
  }

  public void validateAction(RepositoryOperation... operations) throws KettleException, KettleSecurityException {

  }
  public synchronized void delUser(ObjectId id_user) throws KettleException {
    repository.connectionDelegate.performDelete("DELETE FROM " + repository.quoteTable(KettleDatabaseRepository.TABLE_R_USER) + 
        " WHERE " + repository.quote(KettleDatabaseRepository.FIELD_USER_ID_USER) + " = ? ", id_user);
  }

  public synchronized ObjectId getUserID(String login) throws KettleException {
    return userDelegate.getUserID(login);
  }

  public ObjectId[] getUserIDs() throws KettleException {
    return connectionDelegate.getIDs("SELECT " + repository.quote(KettleDatabaseRepository.FIELD_USER_ID_USER)
        + " FROM " + repository.quoteTable(KettleDatabaseRepository.TABLE_R_USER));
  }

  public synchronized String[] getUserLogins() throws KettleException {
    String loginField = repository.quote(KettleDatabaseRepository.FIELD_USER_LOGIN);
    return connectionDelegate.getStrings("SELECT " + loginField + " FROM "
        + repository.quoteTable(KettleDatabaseRepository.TABLE_R_USER) + " ORDER BY " + loginField);
  }

  public synchronized void renameUser(ObjectId id_user, String newname) throws KettleException {
    userDelegate.renameUser(id_user, newname);
  }

  public void deleteUsers(List<IUser> users) throws KettleException {
    throw new UnsupportedOperationException();
  }

  public List<IUser> getUsers() throws KettleException {
    String[] userLogins = getUserLogins();
    List<IUser> users = new ArrayList<IUser>();
    for(String userLogin:userLogins) {
      users.add(loadUserInfo(userLogin));
    }
    return users;
  }

  public void setUsers(List<IUser> users) throws KettleException {
    throw new UnsupportedOperationException();
  }

  public void delUser(String name) throws KettleException {
    delUser(getUserID(name));
  }
  public void updateUser(IUser user) throws KettleException {
    userDelegate.saveUserInfo(user);
  }

  public IUser constructUser() throws KettleException {
    return new UserInfo();
  }

  public List<String> getAllRoles() throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  public List<String> getAllUsers() throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean isManaged() throws KettleException {
    return true;
  }
}
