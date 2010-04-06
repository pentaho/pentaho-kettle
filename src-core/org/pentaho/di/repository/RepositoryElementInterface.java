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
public interface RepositoryElementInterface extends RepositoryElementLocationInterface {
	public void setRepositoryDirectory(RepositoryDirectory repositoryDirectory);
	
	public void setName(String name);
	
	public void setDescription(String description);
	public String getDescription();
	
	public void setObjectId(ObjectId id);
	public ObjectId getObjectId();
			
	/**
	 * Clears the changed flag
	 */
	public void clearChanged();
	
	public void setObjectRevision(ObjectRevision objectRevision);
	public ObjectRevision getObjectRevision();
}
