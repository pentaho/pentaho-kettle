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
  
  public boolean isManaged() throws KettleException;
}
