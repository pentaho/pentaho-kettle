package org.pentaho.di.repository;



/**
 * A repository element is an object that can be saved or loaded from the repository.
 * As such, we need to be able to identify it.
 * It needs a RepositoryDirectory,a name and an ID.
 * 
 * We also need to identify the type of the element.
 *   
 * Finally, we need to be able to optionally identify the revision of the element. 
 * 
 * @author matt
 *
 */
public interface RepositoryElementInterface {
	public void setRepositoryDirectory(RepositoryDirectory repositoryDirectory);
	public RepositoryDirectory getRepositoryDirectory();
	
	public void setName(String name);
	public String getName();
	
	public void setObjectId(ObjectId id);
	public ObjectId getObjectId();
	
	public String getRepositoryElementType();
	
	public RepositoryRevision getRevision();
	
	public RepositoryLock getRepositoryLock();
}
