package org.pentaho.di.repository;

public class RepositoryElementLocation implements RepositoryElementLocationInterface {

	private String					name;
	private RepositoryDirectory		repositoryDirectory;
	private RepositoryObjectType	repositoryElementType;

	/**
	 * @param name
	 * @param repositoryDirectory
	 * @param repositoryElementType
	 */
	public RepositoryElementLocation(String name, RepositoryDirectory repositoryDirectory, RepositoryObjectType repositoryElementType) {
		this.name = name;
		this.repositoryDirectory = repositoryDirectory;
		this.repositoryElementType = repositoryElementType;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the repositoryDirectory
	 */
	public RepositoryDirectory getRepositoryDirectory() {
		return repositoryDirectory;
	}

	/**
	 * @return the repositoryElementType
	 */
	public RepositoryObjectType getRepositoryElementType() {
		return repositoryElementType;
	}
}
