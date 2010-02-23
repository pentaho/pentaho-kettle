package org.pentaho.di.repository;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleSecurityException;


public class BaseRepositorySecurityProvider {

	protected RepositoryMeta repositoryMeta;
	protected UserInfo userInfo;
	protected RepositoryCapabilities	capabilities;

	public BaseRepositorySecurityProvider(RepositoryMeta repositoryMeta, UserInfo userInfo) {
		this.repositoryMeta = repositoryMeta;
		this.userInfo = userInfo;
		this.capabilities = repositoryMeta.getRepositoryCapabilities();
	}
	
	public UserInfo getUserInfo() {
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
