package org.pentaho.di.repository;


/**
 * A repository element is an object that can be saved or loaded from the repository.
 * As such, we need to be able to identify it.
 * It needs a RepositoryDirectory,a name and an ID.
 * 
 * We also need to identify the type of the element.
 *   
 * @author matt
 *
 */
public interface RepositoryElementInterface {
	public void setRepositoryDirectory(RepositoryDirectory repositoryDirectory);
	public RepositoryDirectory getRepositoryDirectory();
	
	public void setName(String name);
	public String getName();
	
	public void setID(long id);
	public long getID();
	
	public String getRepositoryElementType();
	
	// public void saveInRepository(Repository repository, ProgressMonitorListener monitor) throws KettleException;
}
