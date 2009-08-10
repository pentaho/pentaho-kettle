package org.pentaho.di.repository;

import java.util.List;

import org.pentaho.di.core.exception.KettleException;

/**
 * The repository version registry contains all the versions that are defined for a repository.
 * It is capable of handling versions, tagging revisions, etc.
 *  
 * @author matt
 *
 */
public interface RepositoryVersionRegistry {
	
	/**
	 * Add an object version to the registry
	 * 
	 * @param version The version to add
	 * @throws KettleException in case something goes wrong.
	 */
	public void addVersion(ObjectVersion version) throws KettleException;
	
	/**
	 * Update an object version in the registry
	 * 
	 * @param version The version to update in the registry.  The label is used to find the version to update.
	 * @throws KettleException in case something goes wrong.
	 */
	public void updateVersion(ObjectVersion version) throws KettleException;
	

	/**
	 * Get a list of all the object versions in the registry.
	 * 
	 * @return A list of all the versions defined
	 * @throws KettleException in case something goes wrong.
	 */
	public List<ObjectVersion> getVersions() throws KettleException;

	/**
	 * Remove a version from the registry
	 * 
	 * @param label the version to remove from the registry
	 */
	public void removeVersion(String label) throws KettleException;
}
