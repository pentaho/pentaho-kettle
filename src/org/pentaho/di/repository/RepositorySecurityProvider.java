package org.pentaho.di.repository;

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
public interface RepositorySecurityProvider {

	/**
	 * @return the user information set on the security provider
	 */
	public UserInfo getUserInfo();
	
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
}
