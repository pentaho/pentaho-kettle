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
public interface RepositorySecurityProvider extends RepositoryUserInterface {

	/**
	 * Set the user information on this security provider.  If the repository doesn't support users, it's OK to set this to null.
	 * In that case, if the user is not null, the user information is ignored. 
	 * 
	 * @param userInfo The user information to set.
	 */
	public void setUserInfo(UserInfo userInfo);
	
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
	 * @return true if the repository supports user authentication/security
	 */
	public boolean supportsUsers();

	/**
	 * @return true if this repository supports revisions: check in/out, etc.
	 * Note that this also takes into account the user settings.
	 */
	public boolean supportsRevisions();
	
	/**
	 * @return true if the repository supports storing metadata like names, descriptions, ... outside of the object definitions (XML)
	 */
	public boolean supportsMetadata();
	
	/**
	 * @return true if this repository supports file locking and if the user is allowed to lock a file
	 */
	public boolean isLockingPossible();
}
