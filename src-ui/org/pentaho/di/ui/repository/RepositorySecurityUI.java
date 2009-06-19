package org.pentaho.di.ui.repository;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryOperation;
import org.pentaho.di.ui.core.dialog.ErrorDialog;

public class RepositorySecurityUI {
	
	/**
	 * Verify a repository operation, show an error dialog if needed.
	 * 
	 * @param repositoryMeta The repository meta object
	 * @param userinfo The user information
	 * @param operations the operations you want to perform with the supplied user.
	 * 
	 * @return true if there is an error, false if all is OK.
	 */
	public static boolean verifyOperations(Shell shell, Repository repository, RepositoryOperation...operations) {
		
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
			repository.getSecurityProvider().validateAction(operations);
		} catch(KettleException e) {
			new ErrorDialog(shell, "Security error", "There was a security error performing operations:"+Const.CR+operationsDesc, e);
			return true;
		}
		return false;
	}

}
