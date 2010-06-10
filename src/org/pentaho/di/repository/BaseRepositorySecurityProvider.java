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

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleSecurityException;


public class BaseRepositorySecurityProvider {

	protected RepositoryMeta repositoryMeta;
	protected IUser userInfo;
	protected RepositoryCapabilities	capabilities;

	public BaseRepositorySecurityProvider(RepositoryMeta repositoryMeta, IUser userInfo) {
		this.repositoryMeta = repositoryMeta;
		this.userInfo = userInfo;
		this.capabilities = repositoryMeta.getRepositoryCapabilities();
	}
	
	public IUser getUserInfo() {
	  // return a copy of the user info, so that external editing cannot effect the database repo behavior
	  // this allows the user info to act as immutable.
		return userInfo != null ? new UserInfo(userInfo) : null;
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
	
	public void validateAction(RepositoryOperation...operations) throws KettleException, KettleSecurityException {

	}
}
