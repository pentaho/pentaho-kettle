package org.pentaho.di.repository;


public class RepositorySecurity {

	public static boolean isReadOnly(Repository repository) {
		RepositoryMeta repositoryMeta = repository.getRepositoryMeta();
		RepositoryCapabilities capabilities = repositoryMeta.getRepositoryCapabilities();
		UserInfo userInfo = repository.getUserInfo();
		
		return capabilities.isReadOnly() || ( !capabilities.supportsUsers() && !userInfo.isReadOnly());
	}

	public static boolean supportsUsers(Repository repository) {
		RepositoryMeta repositoryMeta = repository.getRepositoryMeta();
		RepositoryCapabilities capabilities = repositoryMeta.getRepositoryCapabilities();
		
		return capabilities.supportsUsers();
	}
	
	public static boolean supportsRevisions(Repository repository) {
		RepositoryMeta repositoryMeta = repository.getRepositoryMeta();
		RepositoryCapabilities capabilities = repositoryMeta.getRepositoryCapabilities();
		
		return capabilities.supportsRevisions();
	}

}
