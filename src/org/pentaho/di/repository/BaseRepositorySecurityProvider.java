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
		
		// If there is no user available, we fail
		//
		if (userInfo==null) {
			throw new KettleException("A valid user is needed to use this repository");
		}
		
		// No questions asked for an administrator...
		//
		if (userInfo.isAdministrator()) {
			return;
		}
		
		// If the user is not enabled, not a single operation can take place...
		//
		if (!userInfo.isEnabled()) {
			throw new KettleException("The user is not enabled");
		}
		
		for (RepositoryOperation operation : operations) {
			switch(operation) {
			case READ_TRANSFORMATION :
				if (!userInfo.useTransformations()) throw new KettleException(operation+" : user can't use transformations");
				break;
			case MODIFY_TRANSFORMATION : 
				if (userInfo.isReadOnly()) throw new KettleException(operation+" : user is read-only");
				if (capabilities.isReadOnly()) throw new KettleException(operation+" : repository is read-only");
				if (!userInfo.useTransformations()) throw new KettleException(operation+" : user can't use transformations");
				break;
			case DELETE_TRANSFORMATION : 
				if (userInfo.isReadOnly()) throw new KettleException(operation+" : user is read-only");
				if (capabilities.isReadOnly()) throw new KettleException(operation+" : repository is read-only");
				if (!userInfo.useTransformations()) throw new KettleException(operation+" : user can't use transformations");
				break;
			case EXECUTE_TRANSFORMATION : 
				if (!userInfo.useTransformations()) throw new KettleException(operation+" : user can't use transformations");
				break;
			
			case READ_JOB :
				if (!userInfo.useJobs()) throw new KettleException(operation+" : user can't use jobs");
				break;
			case MODIFY_JOB :
				if (userInfo.isReadOnly()) throw new KettleException(operation+" : user is read-only");
				if (capabilities.isReadOnly()) throw new KettleException(operation+" : repository is read-only");
				if (!userInfo.useJobs()) throw new KettleException(operation+" : user can't use jobs");
				break;
			case DELETE_JOB :
				if (userInfo.isReadOnly()) throw new KettleException(operation+" : user is read-only");
				if (capabilities.isReadOnly()) throw new KettleException(operation+" : repository is read-only");
				if (!userInfo.useJobs()) throw new KettleException(operation+" : user can't use jobs");
				break;
			case EXECUTE_JOB :
				if (!userInfo.useJobs()) throw new KettleException(operation+" : user can't use jobs");
				break;
			
			case MODIFY_DATABASE :
				if (userInfo.isReadOnly()) throw new KettleException(operation+" : user is read-only");
				if (capabilities.isReadOnly()) throw new KettleException(operation+" : repository is read-only");
				if (!userInfo.useDatabases()) throw new KettleException(operation+" : user can't use databases");
				break;
			case DELETE_DATABASE :
				if (userInfo.isReadOnly()) throw new KettleException(operation+" : user is read-only");
				if (capabilities.isReadOnly()) throw new KettleException(operation+" : repository is read-only");
				if (!userInfo.useDatabases()) throw new KettleException(operation+" : user can't use databases");
				break;
			case EXPLORE_DATABASE :
				if (!userInfo.exploreDatabases()) throw new KettleException(operation+" : user can't explore databases");
				break;
	
			case MODIFY_SLAVE_SERVER:
			case MODIFY_CLUSTER_SCHEMA:
			case MODIFY_PARTITION_SCHEMA:
				if (userInfo.isReadOnly()) throw new KettleException(operation+" : user is read-only");
				if (capabilities.isReadOnly()) throw new KettleException(operation+" : repository is read-only");
				break;
			case DELETE_SLAVE_SERVER:
			case DELETE_CLUSTER_SCHEMA:
			case DELETE_PARTITION_SCHEMA:
				if (userInfo.isReadOnly()) throw new KettleException(operation+" : user is read-only");
				if (capabilities.isReadOnly()) throw new KettleException(operation+" : repository is read-only");
				break;
	
			default:
				throw new KettleException("Operation ["+operation+"] is unknown to the security handler.");
			}
		}
	}
}
