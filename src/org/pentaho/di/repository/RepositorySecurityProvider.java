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
import org.pentaho.di.core.exception.KettleSecurityException;

/**
 * This is the interface to the security provider for the repositories out there.<p>
 * 
 * This allows the repository to transparently implement any kind of authentication supported by Kettle.
 * 
 * @author matt
 *
 */
public interface RepositorySecurityProvider extends IRepositoryService {

	/**
	 * @return the user information set on the security provider
	 */
	public IUser getUserInfo();
	
	/**
	 * Validates the supplied operation.
	 * 
	 * @throws KettleSecurityException in case the provided user is not know or the password is incorrect
	 * @throws KettleException in case the action couldn't be validated because of an unexpected problem.
	 */
	public void validateAction(RepositoryOperation...operations) throws KettleException, KettleSecurityException;

	/**
	 * @return true if the repository or the user is read only
	 */
	public boolean isReadOnly();
	
	/**
	 * @return true if this repository supports file locking and if the user is allowed to lock a file
	 */
	public boolean isLockingPossible();
	
	/**
	 * @return true if the repository supports revisions AND if it is possible to give version comments
	 */
	public boolean allowsVersionComments();

	/**
	 * @return true if version comments are allowed and mandatory.
	 */
	public boolean isVersionCommentMandatory();
	
  /**
   * Retrieves all users in the system
   * 
   * @return list of username
   * @throws KettleSecurityException in case anything went wrong
   */
  public List<String> getAllUsers() throws KettleException;

  /**
   * Retrieves all roles in the system
   * 
   * @return list of role
   * @throws KettleSecurityException in case anything went wrong
   */
  public List<String> getAllRoles() throws KettleException;
  
  public String[] getUserLogins() throws KettleException;


}
