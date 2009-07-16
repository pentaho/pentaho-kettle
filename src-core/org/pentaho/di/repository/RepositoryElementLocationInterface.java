package org.pentaho.di.repository;

public interface RepositoryElementLocationInterface {
	public String getName();
	public RepositoryDirectory getRepositoryDirectory();
	public RepositoryObjectType getRepositoryElementType();
}
