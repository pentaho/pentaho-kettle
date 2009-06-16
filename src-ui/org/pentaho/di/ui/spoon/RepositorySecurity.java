package org.pentaho.di.ui.spoon;

import org.eclipse.swt.widgets.Display;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryCapabilities;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.repository.RepositoryOperation;
import org.pentaho.di.repository.UserInfo;
import org.pentaho.di.ui.core.dialog.ErrorDialog;

public class RepositorySecurity {

	/**
	 * See if this user is allowed to perform ALL the specified operation.
	 * 
	 * @param user the user to verify
	 * @param capabilities the capabilities of the repository to verify.
	 * @param operations the operations to verify
	 * 
	 * @throws KettleException in case the user is not allowed to perform the operation.
	 */
	public static void verifyOperation(UserInfo user, RepositoryCapabilities capabilities, RepositoryOperation...operations) throws KettleException {
		
		if (!capabilities.supportsUsers()) return; // No users, no security
		
		// If there is no user available and the repository supports users, we fail
		//
		if (user==null) {
			throw new KettleException("A valid user is needed to use this repository");
		}
		
		// No questions asked for an administrator...
		//
		if (user.isAdministrator()) {
			return;
		}
		
		// If the user is not enabled, not a single operation can take place...
		//
		if (!user.isEnabled()) {
			throw new KettleException("The user is not enabled");
		}
		
		for (RepositoryOperation operation : operations) {
			switch(operation) {
			case READ_TRANSFORMATION :
				if (!user.useTransformations()) throw new KettleException(operation+" : user can't use transformations");
				break;
			case MODIFY_TRANSFORMATION : 
				if (user.isReadOnly()) throw new KettleException(operation+" : user is read-only");
				if (capabilities.isReadOnly()) throw new KettleException(operation+" : repository is read-only");
				if (!user.useTransformations()) throw new KettleException(operation+" : user can't use transformations");
				break;
			case DELETE_TRANSFORMATION : 
				if (user.isReadOnly()) throw new KettleException(operation+" : user is read-only");
				if (capabilities.isReadOnly()) throw new KettleException(operation+" : repository is read-only");
				if (!user.useTransformations()) throw new KettleException(operation+" : user can't use transformations");
				break;
			case EXECUTE_TRANSFORMATION : 
				if (!user.useTransformations()) throw new KettleException(operation+" : user can't use transformations");
				break;
			
			case READ_JOB :
				if (!user.useJobs()) throw new KettleException(operation+" : user can't use jobs");
				break;
			case MODIFY_JOB :
				if (user.isReadOnly()) throw new KettleException(operation+" : user is read-only");
				if (capabilities.isReadOnly()) throw new KettleException(operation+" : repository is read-only");
				if (!user.useJobs()) throw new KettleException(operation+" : user can't use jobs");
				break;
			case DELETE_JOB :
				if (user.isReadOnly()) throw new KettleException(operation+" : user is read-only");
				if (capabilities.isReadOnly()) throw new KettleException(operation+" : repository is read-only");
				if (!user.useJobs()) throw new KettleException(operation+" : user can't use jobs");
				break;
			case EXECUTE_JOB :
				if (!user.useJobs()) throw new KettleException(operation+" : user can't use jobs");
				break;
			
			case MODIFY_DATABASE :
				if (user.isReadOnly()) throw new KettleException(operation+" : user is read-only");
				if (capabilities.isReadOnly()) throw new KettleException(operation+" : repository is read-only");
				if (!user.useDatabases()) throw new KettleException(operation+" : user can't use databases");
				break;
			case DELETE_DATABASE :
				if (user.isReadOnly()) throw new KettleException(operation+" : user is read-only");
				if (capabilities.isReadOnly()) throw new KettleException(operation+" : repository is read-only");
				if (!user.useDatabases()) throw new KettleException(operation+" : user can't use databases");
				break;
			case EXPLORE_DATABASE :
				if (!user.exploreDatabases()) throw new KettleException(operation+" : user can't explore databases");
				break;

			}
		}
	}
	
	/**
	 * Verify a repository operation, show an error dialog if needed.
	 * 
	 * @param repositoryMeta The repository meta object
	 * @param userinfo The user information
	 * @param operations the operations you want to perform with the supplied user.
	 * 
	 * @return true if there is an error, false if all is OK.
	 */
	public static boolean verifyOperations(Repository repository, RepositoryOperation...operations) {
		
		String operationsDesc = "[";
		for (RepositoryOperation operation : operations) {
			if (operationsDesc.length()>1) operationsDesc+=", ";
			operationsDesc+=operation.getDescription();
		}
		operationsDesc+="]";
		
		try {
			if (repository==null) {
				throw new KettleException("You are not logged on to a repository");
			}
			
			RepositoryMeta repositoryMeta = repository.getRepositoryMeta();
			UserInfo userInfo = repository.getUserInfo();
			RepositoryCapabilities capabilities = repositoryMeta.getRepositoryCapabilities();
			
			RepositorySecurity.verifyOperation(userInfo, capabilities, operations);
		} catch(KettleException e) {
			new ErrorDialog(Display.getCurrent().getActiveShell(), "Security error", "There was a security error performing operations:"+Const.CR+operationsDesc, e);
			return true;
		}
		return false;
	}

	public static boolean isReadOnly(Repository repository) {
		RepositoryMeta repositoryMeta = repository.getRepositoryMeta();
		RepositoryCapabilities capabilities = repositoryMeta.getRepositoryCapabilities();
		UserInfo userInfo = repository.getUserInfo();
		
		return capabilities.isReadOnly() || ( !capabilities.supportsUsers() && !userInfo.isReadOnly());
	}

	public static boolean supportsUsers(Repository repository) {
		RepositoryMeta repositoryMeta = repository.getRepositoryMeta();
		RepositoryCapabilities capabilities = repositoryMeta.getRepositoryCapabilities();
		
		return capabilities.supportsUsers();
	}
	
	public static boolean supportsRevisions(Repository repository) {
		RepositoryMeta repositoryMeta = repository.getRepositoryMeta();
		RepositoryCapabilities capabilities = repositoryMeta.getRepositoryCapabilities();
		
		return capabilities.supportsRevisions();
	}

}
