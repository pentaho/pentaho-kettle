package org.pentaho.di.repository;


/**
 * This singleton keeps the location of a repository import.
 * 
 * NOT THREAD SAFE, ONLY ONE IMPORT AT A TIME PLEASE!!

 * @author matt
 *
 */
public class RepositoryImportLocation {

	private static RepositoryImportLocation location;
	
	private RepositoryDirectoryInterface repositoryDirectory;
	
	private RepositoryImportLocation() {
		repositoryDirectory = null;
	}
	
	/**
	 * Get the repository import location.
	 * WARNING: NOT THREAD SAFE, ONLY ONE IMPORT AT A TIME PLEASE!!
	 * 
	 * @return the import location in the repository in the form of a repository directory.
	 *         If no import location is set, null is returned.
	 */
	public static RepositoryDirectoryInterface getRepositoryImportLocation() {
		if (location==null) location = new RepositoryImportLocation();
		return location.repositoryDirectory;
	}
	
	/**
	 * Sets the repository import location.
	 * WARNING: NOT THREAD SAFE, ONLY ONE IMPORT AT A TIME PLEASE!!
	 * 
	 * ALSO MAKE SURE TO CLEAR THE IMPORT DIRECTORY AFTER IMPORT!!
	 * (sorry for shouting)
	 * 
	 * @param repositoryDirectory the import location in the repository in the form of a repository directory.
	 *    
	 */
	public static void setRepositoryImportLocation(RepositoryDirectoryInterface repositoryDirectory) {
		if (location==null) location = new RepositoryImportLocation();
		location.repositoryDirectory = repositoryDirectory;
	}
}
