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
package org.pentaho.di.repository.filerep;

import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleSecurityException;
import org.pentaho.di.repository.RepositoryCapabilities;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.repository.RepositoryOperation;
import org.pentaho.di.repository.RepositorySecurityProvider;
import org.pentaho.di.repository.UserInfo;

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
	
	public boolean isLockingPossible() {
		return capabilities.supportsLocking();
	}
	
	public boolean allowsVersionComments() {
		return false;
	}
	
	public boolean isVersionCommentMandatory() {
		return false;
	}

  public List<String> getAllRoles() throws KettleException {
    throw new UnsupportedOperationException();
  }

  public List<String> getAllUsers() throws KettleException {
    throw new UnsupportedOperationException();
  }

  public String[] getUserLogins() throws KettleException {
    throw new UnsupportedOperationException();
  }
}
